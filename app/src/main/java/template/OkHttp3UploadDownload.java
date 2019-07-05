package template;

import android.media.MediaMetadataRetriever;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.EventListener;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

//此模板类容未测试过，请谨慎使用
public class OkHttp3UploadDownload {

    static ResponseBody upload(String url, String filePath, final String fileName) throws Exception {
        OkHttpClient client = new OkHttpClient.Builder()
                //监控上传进度 方式一：
                .eventListener(new EventListener() {
                    Request mRequest;
                    Timer timer;

                    @Override
                    public void requestHeadersEnd(Call call, Request request) {
                        mRequest = request;
                    }

                    @Override
                    public void requestBodyStart(Call call) {
                        //Content-Length
                        timer = new Timer();
                        //这里才能使用mResponse.body()
                        long contentLength = Long.parseLong(mRequest.header("Content-Length"));
                        timer.schedule(new UploadProgressTask(mRequest.body(), contentLength), 1000, 1000);
                    }

                    @Override
                    public void requestBodyEnd(Call call, long byteCount) {
                        timer.cancel();
                    }
                })
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
                FileInputStream fis = new FileInputStream(new File(fileName));
                fis.skip(102400);//跳到指定位置，断点续传
                /*//方式一：
                int length;
                byte[] buffer = new byte[8192];
                OutputStream outputStream = sink.outputStream();
                while ((length = fis.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, length);
                    //或者
                    sink.write(buffer, 0, length);
                }*/
                //方式二：
                try (Source source = Okio.source(fis)) {
                    sink.writeAll(source);
                }
            }
        };

        //方式一：
        String extension = MimeTypeMap.getFileExtensionFromUrl(filePath).toLowerCase();
        String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

        //方式二：
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(filePath);
        mime = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);

        //无法识别的文件用"application/octet-stream"
        RequestBody byteBody = RequestBody.create(MediaType.parse("application/octet-stream"), new byte[23]);
        RequestBody fileBody = RequestBody.create(MediaType.parse(mime), new File(filePath));
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
                .header("Authorization", "efsd3f223rfd;fa;")
                .url(url)
                .post(streamBody)
//                .post(multiBody)
                .post(formBody)
                .build();

        try {
            Response response = client.newCall(request).execute();//同步
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            return response.body();
        } catch (SocketTimeoutException | ConnectException e) {//连接超时，或者断网

        } catch (Exception e) {

        }

        client.newCall(request).enqueue(new Callback() {//异步
            @Override
            public void onFailure(Call call, IOException e) {
                if (call.isCanceled()) {

                } else if (e instanceof SocketTimeoutException
                        || e instanceof ConnectException) {//连接超时，或者断网

                } else {

                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });

        return null;
    }

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

    private void download() {
        OkHttpClient ohc = new OkHttpClient.Builder()
                //监控下载进度 方式一：下面还有方式二
                .eventListener(new EventListener() {
                    Response mResponse;

                    Timer timer;
                    @Override
                    public void responseHeadersEnd(Call call, Response response) {
                        //It is an error to access the body of this response
                        mResponse = response;
                    }

                    @Override
                    public void responseBodyStart(Call call) {
                        timer = new Timer();
                        //这里才能使用mResponse.body()
                        long contentLength = Long.parseLong(mResponse.header("Content-Length"));
                        timer.schedule(new DownloadProgressTask(mResponse.body(), contentLength), 1000, 1000);
                    }

                    @Override
                    public void responseBodyEnd(Call call, long byteCount) {
                        timer.cancel();
                    }
                })
                .build();

        String url = "http://img.my.csdn.net/uploads/201603/26/1458988468_5804.jpg";
        Request request = new Request.Builder().url(url).build();
        ohc.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (call.isCanceled()) {

                } else if (e instanceof SocketTimeoutException
                        || e instanceof ConnectException) {//连接超时，或者断网

                } else {

                }
            }

            @Override
            public void onResponse(Call call, Response response) {//这种方式貌似不太好

                if (!response.isSuccessful()) return;//失败了
                //监控下载进度 方式二：
                Timer timer = new Timer();
                long contentLength = Long.parseLong(response.header("Content-Length"));
                timer.schedule(new DownloadProgressTask(response.body(), contentLength), 1000, 1000);

                InputStream inputStream = response.body().byteStream();
                FileOutputStream fos = null;
                try {
                    //方式一：
                    fos = new FileOutputStream(new File("/sdcard/wangshu.jpg"));
                    byte[] buffer = new byte[2048];
                    int len = 0;
                    while ((len = inputStream.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                    }
                    fos.flush();

                    //方式二：
                    File file = new File("/sdcard/wangshu.jpg");
                    file.createNewFile();
                    BufferedSink sink = Okio.buffer(Okio.sink(file));
//                    BufferedSink sink = Okio.buffer(Okio.appendingSink(file));
                    sink.writeAll(response.body().source());
                    sink.close();

                } catch (IOException e) {
                    if (call.isCanceled()) {

                    } else if (e instanceof SocketTimeoutException
                            || e instanceof ConnectException) {//连接超时，或者断网

                    } else {

                    }
                } finally {
                    if (null != fos) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }
}

class DownloadProgressTask extends TimerTask {
    private ResponseBody mBody;
    private long lastLen = 0;
    private final long contentLen;

    public DownloadProgressTask(ResponseBody body, Long contentLen) {
        this.mBody = body;
        this.contentLen = contentLen;
    }

    @Override
    public void run() {
        long len = mBody.contentLength();//返回-1表示没数据？
        long reciver = len - lastLen; //这个应该是接收到
        lastLen = len;
        //UI更新下载速度

        //
        if (contentLen == len) {
            this.cancel();
        }
    }
}

class UploadProgressTask extends TimerTask {
    private RequestBody mBody;
    private long lastLen = 0;
    private final long contentLen;

    public UploadProgressTask(RequestBody body, Long contentLen) {
        this.mBody = body;
        this.contentLen = contentLen;
    }

    @Override
    public void run() {
        long len = 0;//返回-1表示没数据？
        try {
            len = mBody.contentLength();
        } catch (IOException e) {
            e.printStackTrace();
        }
        long reciver = len - lastLen; //这个应该是接收到
        lastLen = len;
        //UI更新下载速度

        //
        if (contentLen == len) {
            this.cancel();
        }
    }
}
