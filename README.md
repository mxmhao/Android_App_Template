# Android_App_Template
安卓App工具类和模板代码，工具类一般可以直接使用，模板类主要用来参考，引入到自己的项目时可能会报错，请自行修改。  
开发安卓前，请去了解 <font color='#ff0000'>Jetpack</font> 和 <font color='#ff0000'>Hilt</font>，能使你的开发方便很多。

## [工具类，在“/app/src/main/java/utils/”目录下](/app/src/main/java/utils)
1. [获取全局Context的工具类，无须传入Context；get_context_no_dependence_anything/](/app/src/main/java/utils/get_context_no_dependence_anything)
2. [Utils工具；Utils.java](/app/src/main/java/utils/Utils.java)
    包含：获取MIME类型、判断字符串是否为IP地址（模板）、分享到邮件、WiFi连接判断、获取其他语言的字符串、HarmonyOS判断、HarmonyOS系统版本号获取、判断activity是否正在显示  
3. [AES加解密，字节数组与16进制字符串互转；aes/](/app/src/main/java/utils/encryption/aes)、[Android 加密工具](https://developer.android.google.cn/guide/topics/security/cryptography)、[Android 密钥库系统](https://developer.android.google.cn/training/articles/keystore)
4. [App启动icon不显示在 Launcher 上的方法：在启动 Activity 的 intent-filter 中加点料](/app/src/main/AndroidManifest.xml)

## [模板，在“/app/src/main/java/template/”目录下](/app/src/main/java/template)
1. [RecyclerView分组的模板代码；recyclerview_group/](/app/src/main/java/template/recyclerview_group)
2. [自定义HUD指示器模板代码，改造AlertDialog，可以禁止用户交互和允许用户交互；ProgressHUD.java](/app/src/main/java/template/ProgressHUD.java)
3. [获取本地图片或视频的缩略图；ThumbnailImage.java](/app/src/main/java/template/ThumbnailImage.java)
4. [安卓8.0官方BottomNavigationView使用的问题；BottomNavigationFragment.java](/app/src/main/java/template/BottomNavigationFragment.java)
5. [Fragment的壳Activity，主要是想App就用这一个Activity就够了，其他的都用Fragment；fragment_activity/](/app/src/main/java/template/fragment_activity)
6. [使用系统的文件多选，文件夹（目录）选择器；FileMultipleSelectionFragment.java](/app/src/main/java/template/FileMultipleSelectionFragment.java)
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
25. [文字转语音(TTS)；UtilTemplates.java#speak](/app/src/main/java/template/UtilTemplates.java)
26. [获取对应 density 下的原图；UtilTemplates.java#getNoScaledImage](/app/src/main/java/template/UtilTemplates.java)
27. [获取剪切板的内容；UtilTemplates.java#getClipboardText](/app/src/main/java/template/UtilTemplates.java)
28. [获取指定文件夹大小；UtilTemplates.java#getDirSize](/app/src/main/java/template/UtilTemplates.java)
29. [获取存储可用空间大小；UtilTemplates.java#getAvailableSpace](/app/src/main/java/template/UtilTemplates.java)  
30. [各种自建证书加载；UtilTemplates.java#getSSLSocketFactory](/app/src/main/java/template/UtilTemplates.java)  
31. [自定义悬浮可拖动按钮；FloatDragBtnActivity.java](/app/src/main/java/template/FloatDragBtnActivity.java)  
32. [安卓自带图片选择器的正确打开方式；ImagePickActivity.java](/app/src/main/java/template/ImagePickActivity.java)


## 要了解学习的事物
1. 并发锁：ReentrantLock CountDownLatch CyclicBarrier Phaser ReadWriteLock StampedLock Semaphore Exchanger、LockSupport Condition