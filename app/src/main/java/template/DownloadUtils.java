package template;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import androidx.core.content.FileProvider;

import java.io.File;

public class DownloadUtils {
    //下载器
    private DownloadManager dm;
    private Context mContext;
    //下载任务ID
    private long downloadId;
    private String mUrl;
    private File file;

    public static DownloadUtils du;

    public DownloadUtils(Context context, String url) {
        mContext = context;
        mUrl = url;
    }

    public static void downloadAPK(Context context, String url) {
        du = new DownloadUtils(context, url);
        du.downloadAPK();
    }

    //下载apk
    private void downloadAPK() {
        //创建下载请求
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(mUrl));
        //移动网络情况下是否允许漫游
        request.setAllowedOverRoaming(false);
        //在通知栏中显示，默认就是显示的
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        //隐藏通知栏 manifest必须配置权限：android.permission.DOWNLOAD_WITHOUT_NOTIFICATION;
//        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
        request.setTitle("正在下载最新APK");
        request.setDescription("my.apk");
        request.setVisibleInDownloadsUi(true);

        //设置下载的路径
        file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "my.apk");
        if (file.exists()) file.delete();
        //设置文件存放路径，路径必须是Context.getExternalFilesDir()目录下的，或Environment.getExternalStoragePublicDirectory()目录下的，否则不支持；还可能需要有Manifest.permission.WRITE_EXTERNAL_STORAGE，具体请看注释。
        request.setDestinationUri(Uri.fromFile(file));//Context.getExternalFilesDir(null)，API29以上此目录下不需要读写权限
        //获取DownloadManager
        if (dm == null)
            dm = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
        if (dm != null) {
            //将请求加入队列，加入后会给该任务返回id，通过该id可以取消任务、重启任务、获取下载的文件等等
            downloadId = dm.enqueue(request);
        }

        //注册广播接收者，监听下载状态
        mContext.registerReceiver(receiver,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    //广播监听下载的各个状态
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            checkStatus();
            long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
        }
    };

    //检查下载状态
    private void checkStatus() {
        switch (getStatus()) {
            case DownloadManager.STATUS_PAUSED://下载暂停
                break;

            case DownloadManager.STATUS_PENDING://下载延迟
                break;

            case DownloadManager.STATUS_RUNNING://正在下载
                break;

            case DownloadManager.STATUS_SUCCESSFUL://下载完成
                du = null;
                mContext.unregisterReceiver(receiver);
                installAPK();//下载完成安装APK
                break;

            case DownloadManager.STATUS_FAILED://下载失败
                du = null;
                mContext.unregisterReceiver(receiver);
                break;
        }
    }

    @SuppressLint("Range")
    public int getStatus() {
        int status = 0;
        DownloadManager.Query query = new DownloadManager.Query();
        //通过id查找
        query.setFilterById(downloadId);
        Cursor cursor = dm.query(query);
        if (cursor.moveToFirst()) {
            status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
        }
        cursor.close();
        return status;
    }

    private void installAPK() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        // 由于没有在Activity环境下启动Activity,设置下面的标签
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //Android7.0(24)以上要使用FileProvider
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //"test.mxm.FileProvider"在AndroidManifest.xml注册
            Uri apkUri = FileProvider.getUriForFile(mContext, "test.mxm.FileProvider", file);
            //添加这一句表示对目标应用临时授权该Uri所代表的文件
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(file),
                    "application/vnd.android.package-archive");
        }
        mContext.startActivity(intent);
    }

    /*private final String TAG = "DownloadUtils";
    //修改文件权限
    private void setPermission(String absolutePath) {
        String command = "chmod " + "777" + " " + absolutePath;
        Runtime runtime = Runtime.getRuntime();
        try {
            runtime.exec(command);
        } catch (IOException e) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "setPermission: ", e);
            }
        }
    }*/

    // 这个几个常量值在AOSP能找到 https://github.com/aosp-mirror/platform_frameworks_base/blob/master/core/java/android/provider/Downloads.java
    private static final String CONTENT_URI = "content://downloads/my_downloads";
    private static final int CONTROL_RUN = 0;
    private static final int CONTROL_PAUSED = 1;
    private static boolean resumeDownload(Context context, long id) {
        return controlDownload(context, id, CONTROL_RUN);
    }

    private static boolean pauseDownload(Context context, long id) {
        return controlDownload(context, id, CONTROL_PAUSED);
    }

    private static boolean controlDownload(Context context, long id, int control) {
        int updatedRows = 0;
        ContentValues controls = new ContentValues();
        // 以下写法可参考 DownloadManager.forceDownload() 方法 和 https://github.com/aosp-mirror/platform_frameworks_base/blob/master/core/java/android/provider/Downloads.java
        controls.put("control", control); // Resume Control Value
//        controls.put(DownloadManager.COLUMN_STATUS, 190); // 190: STATUS_PENDING
        try {
            updatedRows = context.getContentResolver().update(
//                    Uri.parse(CONTENT_URI),
//                    controls,
//                    DownloadManager.COLUMN_ID + "=?",
//                    new String[]{ String.valueOf(id) }
                    ContentUris.withAppendedId(Uri.parse(CONTENT_URI), id),
                    controls, null, null
            );
        } catch (Exception ignored) {}

        return updatedRows > 0;
    }
}
