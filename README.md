# HaiKangVideoDemo
海康视频监控Android端Demo

集成步骤请参考：（海康8800实时视频Android客户端集成总结）http://blog.csdn.net/Marvinhq/article/details/62482073

如下问题：
 java.lang.UnsatisfiedLinkError: dalvik.system.PathClassLoader[DexPathList[[zip file "/data/app/net.comet.example-1/base.apk"],nativeLibraryDirectories=[/data/app/net.comet.example-1/lib/arm64, /vendor/lib64, /system/lib64]]] couldn't find "libZBarDecoder.so"
      at java.lang.Runtime.loadLibrary(Runtime.java:366)
      at java.lang.System.loadLibrary(System.java:989)
      at com.dtr.zbar.build.ZBarDecoder.<clinit>(ZBarDecoder.java:6)
      at me.ele.hbdteam.widget.ScanView$1.onPreviewFrame(ScanView.java:263)
      at android.hardware.Camera$EventHandler.handleMessage(Camera.java:1565)
      at android.os.Handler.dispatchMessage(Handler.java:102)
      at android.os.Looper.loop(Looper.java:155)
      
   参考：http://blog.csdn.net/marvinhq/article/details/62490104
