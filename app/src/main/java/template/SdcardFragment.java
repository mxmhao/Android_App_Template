package template;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import androidx.fragment.app.Fragment;

import java.io.File;

import test.mxm.android_app_template.BuildConfig;

/**
 * 获取外置SD卡的绝对路径模板
 */
public class SdcardFragment extends Fragment {
    private final String TAG = "SdcardFragment";

    public SdcardFragment() {
        // Required empty public constructor
    }

    /**
     * 必须在Android4.4以上版本使用
     * OEM厂商需要适配https://source.android.com/devices/storage/config-example.html
     * 否则getExternalFilesDirs获取不到SD卡的路径
     * https://codeday.me/bug/20180924/264484.html
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        File[] files = context.getExternalFilesDirs(null);
        if (files.length < 2) return;//没有外置SD卡

        if (BuildConfig.DEBUG) {
            for (File f: files) {
                Log.e(TAG, "choose: " + f.getPath());
            }
        }

        String sdcard = files[1].getPath();//如果有多张外置SD卡，下标就是1，2，3，4，.....
        //这个就是外置SD卡的绝对路径
        sdcard = sdcard.substring(0, sdcard.indexOf("/Android"));
        File sdFile = new File(sdcard);
        Log.e(TAG, "sdcard: " + sdcard
                + " : " + Environment.getExternalStorageState(sdFile)
                + " : " + Environment.isExternalStorageRemovable(sdFile)
                + " : " + Environment.isExternalStorageEmulated(sdFile));
    }

    public void choose(View view) {
        //这种方式可以获取到外置SD卡的URI，
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK || data == null) {
            return;
        }

        Uri fileUri = data.getData();
        if (BuildConfig.DEBUG) {
            Log.e(TAG, "onActivityResult: " + fileUri.getPath()
                + " : " + fileUri.getAuthority()
                + " : " + fileUri.getScheme());
        }
        //把Uri转换成文件的绝对路径，Google，也可参考FileMultipleSelectionFragment.java中的转换
    }
}
