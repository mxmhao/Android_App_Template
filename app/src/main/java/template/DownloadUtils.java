package template;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
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
        request.setTitle("正在下载最新APK");
        request.setDescription("my.apk");
        request.setVisibleInDownloadsUi(true);

        //设置下载的路径
        file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "my.apk");
        if (file.exists()) file.delete();

        request.setDestinationUri(Uri.fromFile(file));
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
}
