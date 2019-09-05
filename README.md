# Android_App_Template
安卓App工具类和模板代码，工具类一般可以直接使用，模板类主要用来参考，引入到自己的项目时可能会报错，请自行修改。

## [工具类，在“/app/src/main/java/utils/”目录下](/app/src/main/java/utils)
1. [获取全局Context的工具类，无须传入Context：get_context_no_dependence_anything/](/app/src/main/java/utils/get_context_no_dependence_anything)
2. [Utils工具，Utils.java](/app/src/main/java/utils/Utils.java)
    包含：获取MIME类型、判断字符串是否为IP地址（模板）、分享到邮件
3. [AES加解密](/app/src/main/java/utils/encryption/aes)

## [模板，在“/app/src/main/java/template/”目录下](/app/src/main/java/template)
1. [RecyclerView分组的模板代码：recyclerview_group/](/app/src/main/java/template/recyclerview_group)
2. [自定义HUD指示器模板代码，改造AlertDialog，可以禁止用户交互和允许用户交互：ProgressHUD.java](/app/src/main/java/template/ProgressHUD.java)
3. [获取本地图片或视频的缩略图：ThumbnailImage.java](/app/src/main/java/template/ThumbnailImage.java)
4. [安卓8.0官方BottomNavigationView使用的问题：BottomNavigationFragment.java](/app/src/main/java/template/BottomNavigationFragment.java)
5. [Fragment的壳Activity，主要是想App就用这一个Activity就够了，其他的都用Fragment：fragment_activity/](/app/src/main/java/template/fragment_activity)
6. [使用系统的文件多选，文件夹（目录）选择器：FileMultipleSelectionFragment.java](/app/src/main/java/template/FileMultipleSelectionFragment.java)
7. [自定义改造Toast：ToastUtils.java](/app/src/main/java/template/ToastUtils.java)
8. [Okhttp3上传或者下载文件的模板：OkHttp3UploadDownload.java](/app/src/main/java/template/OkHttp3UploadDownload.java)
9. [WebView常用设置：WebViewActivity.java](/app/src/main/java/template/WebViewActivity.java)
10. [UDP模板：UDP.java](/app/src/main/java/template/UDP.java)
11. [给RecyclerView的Item添加上下文菜单(ContextMenu)和点击事件(onClick)模板，极简：RecyclerViewItem.java](/app/src/main/java/template/RecyclerViewItem.java)
12. [获取外置SD卡（TF卡）的绝对路径：SdcardFragment.java](/app/src/main/java/template/SdcardFragment.java)
13. [修改AlertDialog的主题：AlertDialogTheme.java](/app/src/main/java/template/AlertDialogTheme.java)、[R.style.AlertDialog](/app/src/main/res/values/styles.xml)
14. [仿iOS的弹出框，但API和AlertDialog.Builder一致：AlertDialogTheme.java#Builder](/app/src/main/java/template/AlertDialogTheme.java)、[R.style.AlertDialogTheme](/app/src/main/res/values/styles.xml)
