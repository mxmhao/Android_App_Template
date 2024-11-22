package template;

import android.os.Environment;
import android.webkit.MimeTypeMap;

import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.Util;
import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import okio.Sink;
import okio.Source;
import okio.Timeout;


public class OkHttp3UploadDownload {
    Timer timer = new Timer();

    ResponseBody upload(String url, String filePath, final String fileName) throws Exception {
        OkHttpClient client = new OkHttpClient.Builder()
                .build();

        RequestBody streamBody = new RequestBody() {

            @Override
            public long contentLength() throws IOException {
                return 100000000;//若是断点续传则返回剩余的字节数
            }

            @Override
            public MediaType contentType() {
                return MediaType.parse("image/png");
                //这个根据上传文件的后缀变化，要是不知道用application/octet-stream
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                /*
                //方式一：
                FileInputStream fis = new FileInputStream(new File(fileName));
                fis.skip(102400);//跳到指定位置，断点续传

                int length;
                byte[] buffer = new byte[8192];
                OutputStream outputStream = sink.outputStream();
                while ((length = fis.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, length);
                    //或者
                    sink.write(buffer, 0, length);
                }*/

                //方式二：
//                try (BufferedSource source = Okio.buffer(Okio.source(new File(fileName)))) {
//                    source.skip(102400);//跳到指定位置，断点续传
//                    sink.writeAll(source);
//                }

                //监听当前body的上传进度
                try (BufferedSource source = Okio.buffer(Okio.source(new File(fileName)))) {
                    source.skip(102400);//跳到指定位置，断点续传

                    ProgressSink pSink = new ProgressSink(sink, contentLength());
                    ProgressTask task = new ProgressTask(pSink);
                    timer.schedule(task, 1000, 1000);
                    pSink.writeAll(source);
                    task.cancel();
                }
            }
        };

        String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension("png");

        //无法识别的文件用"application/octet-stream"
//        RequestBody byteBody = RequestBody.create(MediaType.parse("application/octet-stream"), new byte[23]);
//        RequestBody fileBody = RequestBody.create(MediaType.parse(mime), new File(filePath));
        // 旧写法
//        RequestBody byteBody = RequestBody.create(new byte[23], MediaType.parse("application/octet-stream"));
//        RequestBody fileBody = RequestBody.create(new File(filePath), MediaType.parse(mime));
        // 这个是 Okhttp3 换成 kotlin 之后的写法
        RequestBody byteBody = RequestBody.Companion.create(new byte[23], MediaType.Companion.parse("application/octet-stream"));
        RequestBody fileBody = RequestBody.Companion.create(new File(filePath), MediaType.Companion.parse(mime));
//        RequestBody fileBody = RequestBody.create(MediaType.parse("image/jpeg"), new File(filePath));
        //这么设置不太正确
//        RequestBody byteBody = RequestBody.create(MediaType.parse("multipart/form-data"), new byte[23]);
//        RequestBody fileBody = RequestBody.create(MediaType.parse("multipart/form-data"), new File(filePath));

        //复杂的表单，可以包含文件和二进制
        RequestBody multiBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("current", "1")
                .addFormDataPart("total", "2")
                .addFormDataPart("file", fileName, fileBody)
                .addFormDataPart("file", "123.png", byteBody)
                .build();

        //简单的表单
        RequestBody formBody = new FormBody.Builder()
                .add("current", "1")
                .add("total", "2")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(streamBody)
//                .post(multiBody)
//                .post(formBody)
                .build();

        try {
            Response response = client.newCall(request).execute();//同步
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            return response.body();
        } catch (SocketTimeoutException | ConnectException e) {//连接超时，或者断网
        } catch (Exception e) {}

        client.newCall(request).enqueue(new Callback() {//异步
            @Override
            public void onFailure(Call call, IOException e) {
                if (call.isCanceled()) {
                } else if (e instanceof SocketTimeoutException
                        || e instanceof ConnectException) {//连接超时，或者断网
                } else {}
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {}
        });

        return null;
    }

    //上传时填写的MIME类型
    /**
     * "application/x-www-form-urlencoded"，是默认的MIME内容编码类型，一般可以用于所有的情况，但是在传输比较大的二进制或者文本数据时效率低。
     这时候应该使用"multipart/form-data"。如上传文件或者二进制数据和非ASCII数据。
     */
    public static final MediaType MEDIA_TYPE_NORAML_FORM = MediaType.get("application/x-www-form-urlencoded;charset=utf-8");

    //既可以提交普通键值对，也可以提交(多个)文件键值对。
    public static final MediaType MEDIA_TYPE_MULTIPART_FORM = MediaType.get("multipart/form-data;charset=utf-8");

    //只能提交二进制，而且只能提交一个二进制，如果提交文件的话，只能提交一个文件,后台接收参数只能有一个，而且只能是流（或者字节数组）
    public static final MediaType MEDIA_TYPE_STREAM = MediaType.get("application/octet-stream");

    public static final MediaType MEDIA_TYPE_TEXT = MediaType.get("text/plain;charset=utf-8");

    public static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json;charset=utf-8");



    //-------------------------下载------------------------------------------------------------------
    private void download() {
        OkHttpClient ohc = new OkHttpClient.Builder()
                //监控下载进度 方式一：需要配合“文件保存方式二”使用，下面还有方式二，推荐使用方式二
                /*.eventListener(new EventListener() {
                    Response mResponse;
                    ProgressTask task;

                    @Override
                    public void responseHeadersEnd(Call call, Response response) {
                        //It is an error to access the body of this response
                        mResponse = response;
                    }

                    @Override
                    public void responseBodyStart(Call call) {
                        //这里才能使用mResponse.body()
                        ProgressSource source = new ProgressSource(mResponse.body().source(), mResponse.body().contentLength());
                        task = new ProgressTask(source);
                        timer.schedule(task, 1000, 1000);
                    }

                    @Override
                    public void responseBodyEnd(Call call, long byteCount) {
                        task.cancel();
                    }
                })*/
                .build();

        String url = "http://localhost:8080/1458988468_5804.jpg";
        Request request = new Request.Builder().url(url).build();
        ohc.newCall(request).enqueue(new Callback() {

            ProgressTask task;
            BufferedSink sink;
            ProgressSource source;

            @Override
            public void onFailure(Call call, IOException e) {
                if (null != task) task.cancel();
                Util.closeQuietly(sink);
                Util.closeQuietly(source);

                if (call.isCanceled()) {
                } else if (e instanceof SocketTimeoutException
                        || e instanceof ConnectException) {//连接超时，或者断网
                } else {}
            }

            @Override
            public void onResponse(Call call, Response response) {//这种方式貌似不太好

                if (!response.isSuccessful()) return;//失败了

                InputStream inputStream = response.body().byteStream();
                FileOutputStream fos = null;
                try {
                    //文件保存 方式一：
                    fos = new FileOutputStream(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "test.jpg"));
                    byte[] buffer = new byte[2048];
                    int len = 0;
                    while ((len = inputStream.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                    }
                    fos.flush();

                    //监控下载进度 方式二：需要配合“文件保存方式二”使用
                    source = new ProgressSource(response.body().source(), response.body().contentLength());
                    task = new ProgressTask(source);
                    timer.schedule(task, 1000, 1000);

                    //文件保存 方式二：
                    //多并发用RandomAccessFile
                    File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "test.jpg");//文件存放位置
                    file.createNewFile();
                    sink = Okio.buffer(Okio.sink(file));//断点续传Okio.appendingSink(file)
                    sink.writeAll(source);
                    sink.flush();//这个不要忘了

                } catch (IOException e) {
                    if (call.isCanceled()) {

                    } else if (e instanceof SocketTimeoutException
                            || e instanceof ConnectException) {//连接超时，或者断网
                    } else {}
                } finally {
                    if (null != fos) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (null != task) task.cancel();
                    Util.closeQuietly(sink);
                    Util.closeQuietly(source);
                }
            }
        });
    }
}

interface Progress {
    long getLoadedBytes();
    long getTotalBytes();
}

class ProgressTask extends TimerTask {
    private Progress progress;
    private long lastLen = 0;
    private final long contentLen;

    public ProgressTask(Progress progress) {
        this.progress = progress;
        this.contentLen = progress.getTotalBytes();
    }

    @Override
    public void run() {
        long loaded = progress.getLoadedBytes();
        long bytes = loaded - lastLen; //这个在一秒内接收到的数据，可以当作速度
        lastLen = loaded;
        //把速度bytes和进度(lastLen/contentLen)更新到UI

        if (contentLen == loaded) {//下载完了
            this.cancel();
        }
    }
}

/**
 * 此类使用来包装Response.body().source()，需要配合“文件保存方式二”使用；
 */
class ProgressSource implements Source, Progress {

    private Source source;
    private long loadedBytes = 0;//已下载的字节数
    public final long totalBytes;//总字节数

    public ProgressSource(Source source, long totalBytes) {
        this.source = source;
        this.totalBytes = totalBytes;
    }

    @Override
    public long read(Buffer sink, long byteCount) throws IOException {
        long readCount = source.read(sink, byteCount);//从网络流中读取数据
        if (readCount != -1) loadedBytes += readCount;//读取的就是下载进度
        return readCount;
    }

    @Override
    public Timeout timeout() {
        return source.timeout();
    }

    @Override
    public void close() throws IOException {
        source.close();
    }

    @Override
    public long getLoadedBytes() {
        return loadedBytes;
    }

    @Override
    public long getTotalBytes() {
        return totalBytes;
    }
}

class ProgressSink implements Progress/*, Sink*/{

    private BufferedSink sink;
    private long loadedBytes = 0;//已上传的字节数
    public final long totalBytes;//总字节数

    ProgressSink(BufferedSink sink, long totalBytes) {
        this.sink = sink;
        this.totalBytes = totalBytes;
    }

    //此方法就是对RealBufferedSink.writeAll方法改造
    public long writeAll(Source source) throws IOException {
        if (source == null) throw new IllegalArgumentException("source == null");

        long totalBytesRead = 0;
        Buffer buffer = sink.buffer();
        final int size = 8192;//=Segment.SIZE;
        for (long readCount; (readCount = source.read(buffer, size)) != -1; ) {
            sink.emitCompleteSegments();//这个应该是发射到网络流中
            totalBytesRead += readCount;
            loadedBytes += readCount;//发送完成才能计算进度
        }
        return totalBytesRead;
    }

    public void write(Source source, long byteCount) throws IOException {
        Buffer buffer = sink.buffer();
        while (byteCount > 0) {
            long read = source.read(buffer, byteCount);
            if (read == -1) throw new EOFException();
            byteCount -= read;
            sink.emitCompleteSegments();
            loadedBytes += read;
        }
    }

    public void write(ByteBuffer source) throws IOException {
        loadedBytes += sink.write(source);
    }

    /*@Override
    public void write(Buffer source, long byteCount) throws IOException {
        sink.write(source, byteCount);
        loadedBytes += byteCount;
    }

    @Override
    public void flush() throws IOException {
        sink.flush();
    }

    @Override
    public Timeout timeout() {
        return sink.timeout();
    }

    @Override
    public void close() throws IOException {
        sink.close();
    }*/

    @Override
    public long getLoadedBytes() {
        return loadedBytes;
    }

    @Override
    public long getTotalBytes() {
        return totalBytes;
    }
}
