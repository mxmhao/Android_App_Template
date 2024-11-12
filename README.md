# Android_App_Template
[国内链接gitee](https://gitee.com/maoxm/Android_App_Template)  
安卓App工具类和模板代码，工具类一般可以直接使用，模板类主要用来参考，引入到自己的项目时可能会报错，请自行修改。  
开发安卓前，请去了解 <font color='#ff0000'>Jetpack</font> 和 <font color='#ff0000'>Hilt</font>，能使你的开发方便很多。

## [工具类，在“/app/src/main/java/utils/”目录下](/app/src/main/java/utils)
1. [获取全局Context的工具类，无须传入Context；get_context_no_dependence_anything/](/app/src/main/java/utils/get_context_no_dependence_anything)
2. [Utils工具；Utils.java](/app/src/main/java/utils/Utils.java)
    包含：获取MIME类型、判断字符串是否为IP地址（模板）、分享到第三方（不用集成第三方SDK）、WiFi连接判断、获取其他语言的字符串、HarmonyOS判断、HarmonyOS系统版本号获取、判断activity是否正在显示  
3. [AES加解密，字节数组与16进制字符串互转；aes/](/app/src/main/java/utils/encryption/aes)、[Android 加密工具](https://developer.android.google.cn/guide/topics/security/cryptography)、[Android 密钥库系统](https://developer.android.google.cn/training/articles/keystore)
4. [App启动icon不显示在 Launcher 上的方法：在启动 Activity 的 intent-filter 中加点料](/app/src/main/AndroidManifest.xml)  
5. [获取视频文件的第一帧，远程或本地视频都可](/app/src/main/java/utils/Utils.java#L309)
6. [判断谷歌Play商店是否安装](/app/src/main/java/utils/Utils.java#L349)

## [模板，在“/app/src/main/java/template/”目录下](/app/src/main/java/template)
1. [RecyclerView分组的模板代码；recyclerview_group/](/app/src/main/java/template/recyclerview_group)
2. [自定义HUD指示器模板代码，改造AlertDialog，可以禁止用户交互和允许用户交互；ProgressHUD.java](/app/src/main/java/template/ProgressHUD.java)
3. [获取本地图片或视频的缩略图；ThumbnailImage.java](/app/src/main/java/template/ThumbnailImage.java)
4. [安卓8.0官方BottomNavigationView使用的问题；BottomNavigationFragment.java](/app/src/main/java/template/BottomNavigationFragment.java)
5. [Fragment的壳Activity，主要是想App就用这一个Activity就够了，其他的都用Fragment；fragment_activity/](/app/src/main/java/template/fragment_activity)
6. [使用系统的文件多选，文件夹（目录）选择器；FileMultipleSelectionFragment.java，已过时，最新方法请参考第32条](/app/src/main/java/template/FileMultipleSelectionFragment.java)
7. [自定义改造Toast；ToastUtils.java](/app/src/main/java/template/ToastUtils.java)
8. [Okhttp3上传或者下载文件的模板；OkHttp3UploadDownload.java](/app/src/main/java/template/OkHttp3UploadDownload.java)
9. [WebView常用设置，<input type='file'>文件选择适配；WebViewActivity.java](/app/src/main/java/template/WebViewActivity.java)
10. [UDP模板；UDP.java](/app/src/main/java/template/UDP.java)
11. [给RecyclerView的Item添加上下文菜单(ContextMenu)和点击事件(onClick)模板，极简；RecyclerViewItem.java](/app/src/main/java/template/RecyclerViewItem.java)
12. [获取外置SD卡（TF卡）的绝对路径；SdcardFragment.java](/app/src/main/java/template/SdcardFragment.java)
13. [修改AlertDialog的主题；AlertDialogTheme.java](/app/src/main/java/template/AlertDialogTheme.java)、[R.style.AlertDialog](/app/src/main/res/values/styles.xml)
14. [仿iOS的弹出框，但API和AlertDialog.Builder一致；AlertDialogTheme.java#Builder](/app/src/main/java/template/AlertDialogTheme.java)、[R.style.AlertDialogTheme](/app/src/main/res/values/styles.xml)
15. [使用DownloadManager下载APK，并且安装；DownloadUtils.java](/app/src/main/java/template/DownloadUtils.java)、[FileProvider](/app/src/main/res/xml/share_dir.xml)
16. [BottomSheetDialog去掉背景，方便自定义圆角等；BottomSheetDialogTheme.java](/app/src/main/java/template/BottomSheetDialogTheme.java)、[R.style.BottomSheetBgNullTheme](/app/src/main/res/values/styles.xml)
17. [仿iOS导航箭头图标，用的矢量图(vector)，去掉了系统创建vector的多余的空白；](/app/src/main/res/drawable)[R.drawable.v_back](/app/src/main/res/drawable/v_back.xml)、[R.drawable.v_next](/app/src/main/res/drawable/v_next.xml)
18. [单击时间间隔控制，使用的是聚合模式，而不是abstract类；SingleClickController.java](/app/src/main/java/template/SingleClickController.java)
19. [多击（谷歌工程师写的），使用的是聚合模式；MultipleClicksController.java](/app/src/main/java/template/MultipleClicksController.java)
20. [系统日历事件提醒增删和系统闹钟增删；CalendarAndAlarmClock.java](/app/src/main/java/template/CalendarAndAlarmClock.java)
21. [透明状态栏，App顶部栏与状态栏(StatusBar)颜色一体化，沉浸(jin)式状态栏；R.style.TranslucentBarTheme](/app/src/main/res/values/styles.xml)、[API>=30透明状态栏，及延长启动页（可当广告页）时间；TranslucentBarActivity.java](/app/src/main/java/template/TranslucentBarActivity.java)
22. [最简、最省存储的启动页设置；R.style.LauncherTheme](/app/src/main/res/values/styles.xml)
23. [获取WiFi列表，获取当前链接的WiFi名称，API>=29连接WiFi，扫描WiFi和蓝牙等外设的建议；WiFiActivity.java](/app/src/main/java/template/WiFiActivity.java)
24. [图片创建缩略图；ThumbnailImage.java#createThumbImage](/app/src/main/java/template/ThumbnailImage.java)
25. [文字转语音(TTS)；UtilTemplates.java#speak](/app/src/main/java/template/UtilTemplates.java#L57)
26. [获取对应 density 下的原图；UtilTemplates.java#getNoScaledImage](/app/src/main/java/template/UtilTemplates.java#L95)
27. [获取剪切板的内容；UtilTemplates.java#getClipboardText](/app/src/main/java/template/UtilTemplates.java#L104)
28. [获取指定文件夹大小；UtilTemplates.java#getDirSize](/app/src/main/java/template/UtilTemplates.java#L128)
29. [获取存储可用空间大小；UtilTemplates.java#getAvailableSpace](/app/src/main/java/template/UtilTemplates.java#L153)  
30. [各种自建证书加载；UtilTemplates.java#getSSLSocketFactory](/app/src/main/java/template/UtilTemplates.java#L238)  
31. [自定义悬浮可拖动按钮；FloatDragBtnActivity.java](/app/src/main/java/template/FloatDragBtnActivity.java)  
32. [安卓自带图片选择器的正确打开方式；ImagePickActivity.java](/app/src/main/java/template/ImagePickActivity.java)  
33. [EditText输入限制；UtilTemplates.java#editTextLimiter](/app/src/main/java/template/UtilTemplates.java#L368)  
34. [把本地 aar 文件发布到本地 maven 源，可以像远程 maven 库一样引入](/app/build.gradle#L64)
35. [keystore文件的指纹计算方法](/app/src/main/java/template/FingerprintUtils.java)


## 长见识（自己去搜，去了解，去使用）
1. 并发：ReentrantLock CountDownLatch CyclicBarrier Phaser ReadWriteLock StampedLock Semaphore Exchanger LockSupport Condition  
2. App内部角标 BadgeDrawable；MediaSession 框架
3. AndroidUtilCode 非常好的工具类，有些功能不知道怎么实现可以参考，Github上能搜到
4. WifiManager.WifiLock 的 WIFI_MODE_FULL_HIGH_PERF 模式可以防止WiFi在息屏时休眠
5. HTTP上传文件的断点续传协议可参考(苹果公司为其NSURLSession上传文件定制的)：https://datatracker.ietf.org/doc/draft-ietf-httpbis-resumable-upload/
6. Android 官方性能监控和检测，也介绍了要引入的库[https://developer.android.google.cn/topic/performance/inspecting-overview?hl=zh-cn](https://developer.android.google.cn/topic/performance/inspecting-overview?hl=zh-cn)
7. 自定义指定 JDK Home 路径可以在项目根目录的[gradle.properties](gradle.properties)中添加 org.gradle.java.home 参数
8. 第三方依赖下载加速，阿里云 Maven 仓库镜像介绍：[https://developer.aliyun.com/mvn/guide](https://developer.aliyun.com/mvn/guide)
9. gradle 下载加速，腾讯云 gradle 下载地址，按需更新链接中的版本号即可：[https://mirrors.cloud.tencent.com/gradle/gradle-7.6.3-all.zip](https://mirrors.cloud.tencent.com/gradle/gradle-7.6.3-all.zip)