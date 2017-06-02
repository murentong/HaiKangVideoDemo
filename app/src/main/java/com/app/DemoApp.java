package com.app;

import com.hik.mcrsdk.MCRSDK;
import com.hik.mcrsdk.rtsp.RtspClient;

import android.app.Application;

/**
 * Application 类
 * @author zhoudaihui
 *
 */
public class DemoApp extends Application {
    private static DemoApp ins;
	
	@Override
    public void onCreate() {
    	super.onCreate();
    	ins = this;
    	MCRSDK.init();
        RtspClient.initLib();
        MCRSDK.setPrint(1, null);
    }
	
	public static DemoApp getIns() {
		return ins;
	}
}
