package template;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class ImagePickActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 方式1，这种直接打开的文件选择器，但是经过了 "image/*" 过滤：
        ActivityResultLauncher<String> imagePickLauncher1 = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {
                // 这里拿到的是 图片的uri
                // 这种方式读取数据是不需要 Manifest.permission.READ_EXTERNAL_STORAGE 权限
//                getContentResolver().openInputStream(result);
            }
        });
        imagePickLauncher1.launch("image/*");

        // 方式2，这种方式会有图片选择器或文件选择器两种方式弹窗提供选择：
        ActivityResultLauncher<Intent> imagePickLauncher2 = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (null == result || result.getResultCode() != RESULT_OK || result.getData() == null || result.getData().getData() == null) return;
                // 这种方式读取数据是不需要 Manifest.permission.READ_EXTERNAL_STORAGE 权限
//                getContentResolver().openInputStream(result.getData().getData());
            }
        });
        imagePickLauncher2.launch(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI));
        // 这个应该是拍照，还有一些别的参数要设置，另外研究吧
//        imagePickLauncher2.launch(new Intent(MediaStore.ACTION_IMAGE_CAPTURE));

        /*
        方式3：必须是API 33 (安卓13)，才能使用
        https://developer.android.google.cn/about/versions/13/features/photopicker?hl=zh-cn
         */
        ActivityResultLauncher<Intent> imagePickLauncher3 = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (null == result || result.getResultCode() != RESULT_OK || result.getData() == null || result.getData().getData() == null) return;
            // 这种方式读取数据是不需要 Manifest.permission.READ_EXTERNAL_STORAGE 权限
//                getContentResolver().openInputStream(result.getData().getData());
        });
        Intent intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
        // 默认图片视频都可选，这里可以自己过滤
        intent.setType("image/*");
//        intent.setType("video/*");
        imagePickLauncher3.launch(intent);

        /*
        方式4:
        1.7.0 版或更高版本的 androidx.activity 库，这些库目前都是测试版本，不建议使用
        https://developer.android.google.cn/training/data-storage/shared/photopicker?hl=zh-cn
        def activity_version = "1.7.0"
        // Java language implementation
        implementation "androidx.activity:activity:$activity_version"
        // Kotlin
        implementation "androidx.activity:activity-ktx:$activity_version"
         */
        /*ActivityResultLauncher<PickVisualMediaRequest> imagePickLauncher4 = registerForActivityResult(new PickVisualMedia(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {
                // 这里拿到的是 图片的uri
                // 这种方式读取数据是不需要 Manifest.permission.READ_EXTERNAL_STORAGE 权限
//                getContentResolver().openInputStream(result);
            }
        });*/
    }
}