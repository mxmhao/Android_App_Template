package template;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class ImagePickActivity extends AppCompatActivity {

    // registerForActivityResult 可以在构造的时候调用
    private ActivityResultLauncher<String> lip = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
        @Override
        public void onActivityResult(Uri result) {
            // 这里拿到的是 文件的uri
            // 这种方式读取数据是不需要 Manifest.permission.READ_EXTERNAL_STORAGE 权限
//                getContentResolver().openInputStream(result);
        }
    });

    // 把文件写到沙盒之外时用的路径选择器，选择器中还可以编辑指定文件名。调用方法：fileExporter.launch("默认的文件名.json")
    final ActivityResultLauncher<String> fileExporter = registerForActivityResult(
            new ActivityResultContracts.CreateDocument("application/json"), uri -> {
                if (uri != null) { // 返回文件完整路径
                }
            });

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
        搭载 Android 11（API 级别 30）或更高版本 1.7.0 版或更高版本的 androidx.activity 库
        https://developer.android.google.cn/training/data-storage/shared/photopicker?hl=zh-cn
        def activity_version = "1.7.0"
        // Java language implementation
        implementation "androidx.activity:activity:$activity_version"
        // Kotlin
        implementation "androidx.activity:activity-ktx:$activity_version"
         */
        // 选择单个媒体文件
        ActivityResultLauncher<PickVisualMediaRequest> imagePickLauncher4 = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {
                // 这里拿到的是 图片的uri
                // 这种方式读取数据是不需要 Manifest.permission.READ_EXTERNAL_STORAGE 权限
//                getContentResolver().openInputStream(result);
                // 默认情况下，系统会授予应用对媒体文件的访问权限，直到设备重启或应用停止运行。如果您的应用执行长时间运行的工作（例如在后台上传大型文件），您可能需要将此访问权限保留更长时间。为此，请调用
                getContentResolver().takePersistableUriPermission(result, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
        });
        // 只视频
//        new PickVisualMediaRequest.Builder().setMediaType(ActivityResultContracts.PickVisualMedia.VideoOnly.INSTANCE).build();
        // 默认是图片和视频都包含
        imagePickLauncher4.launch(new PickVisualMediaRequest.Builder().build());

        // 选择多个媒体项，maxItems为最大可选数量。最大数量查询 MediaStore.getPickImagesMaxLimit()
        ActivityResultLauncher<PickVisualMediaRequest> imagePickLauncher6 = registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(9), new ActivityResultCallback<List<Uri>>() {
            @Override
            public void onActivityResult(List<Uri> result) {
                // 这里拿到的是 图片的uri
                // 这种方式读取数据是不需要 Manifest.permission.READ_EXTERNAL_STORAGE 权限
//                getContentResolver().openInputStream(result);
            }
        });

        imagePickLauncher6.launch(new PickVisualMediaRequest.Builder().build());

        // 方式5: 此方式可以指定多个文件类型
        ActivityResultLauncher<String[]> imagePickLauncher5 = registerForActivityResult(new ActivityResultContracts.OpenDocument(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {
                // 这里拿到的是 图片的uri
                // 这种方式读取数据是不需要 Manifest.permission.READ_EXTERNAL_STORAGE 权限
//                getContentResolver().openInputStream(result);
            }
        });
        imagePickLauncher5.launch(new String[]{"image/*", "video/*", ""});
    }
}