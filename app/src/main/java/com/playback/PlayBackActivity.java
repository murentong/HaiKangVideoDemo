package com.playback;

import java.util.Calendar;
import java.util.Random;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;

import com.consts.Constants;
import com.data.Config;
import com.data.TempData;
import com.demo.v3.R;
import com.hik.mcrsdk.rtsp.ABS_TIME;
import com.hik.mcrsdk.rtsp.RtspClient;
import com.hikvision.vmsnetsdk.DeviceInfo;
import com.hikvision.vmsnetsdk.RecordInfo;
import com.hikvision.vmsnetsdk.VMSNetSDK;
import com.util.DebugLog;
import com.util.UIUtil;
import com.util.UtilAudioPlay;
import com.util.UtilFilePath;

/**
 * 回放UI类
 * 
 * @author huangweifeng
 * @Data 2013-10-29
 */
public class PlayBackActivity extends Activity implements OnClickListener, PlayBackCallBack {

    /**
     * 日志
     */
    private static final String TAG             = "PlayBackActivity";
    /**
     * 播放视图控件
     */
    private SurfaceView         mSurfaceView;
    /**
     * 开始按钮
     */
    private Button              mStartButton;
    /**
     * 停止按钮
     */
    private Button              mStopButton;
    /**
     * 暂停按钮
     */
    private Button              mPauseButton;
    /**
     * 抓拍按钮
     */
    private Button              mCaptureButton;
    /**
     * 录像按钮
     */
    private Button              mRecordButton;
    /**
     * 音频按钮
     */
    private Button              mAudioButton;
    /** 等待框 */
    private ProgressBar         mProgressBar;
    /**
     * 控制层对象
     */
    private PlayBackControl     mControl;
    /**
     * 创建消息对象
     */
    private Handler             mMessageHandler = new MyHandler();
    /**
     * 回放时的参数对象
     */
    private PlayBackParams      mParamsObj;
    /**
     * 是否暂停标签
     */
    private boolean             mIsPause;

    /**
     * 音频是否开启
     */
    private boolean             mIsAudioOpen;
    /**
     * 是否正在录像
     */
    private boolean             mIsRecord;

    private String              mCameraID;
    private VMSNetSDK           mVmsNetSDK      = null;
    private String              mDeviceID       = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_back);

        getPlayBackInfo();

        setUpView();

        init();

        queryPlaybackInfo();
    }

    /**
     * 该方法用来获取回放的信息，使用者自己实现 void
     * 
     * @since V1.0
     */
    private void getPlayBackInfo() {
        mCameraID = getIntent().getStringExtra(Constants.IntentKey.CAMERA_ID);
        mDeviceID = getIntent().getStringExtra(Constants.IntentKey.DEVICE_ID);
    }

    /**
     * 初始化
     * 
     * @since V1.0
     */
    private void init() {
        // 打开日志的开关
        DebugLog.setLogOption(true);
        // 创建和cms平台交互的对象
        mVmsNetSDK = VMSNetSDK.getInstance();
        // 初始化远程回放控制层对象
        mControl = new PlayBackControl();
        // 设置远程回放控制层回调
        mControl.setPlayBackCallBack(this);
        // 创建远程回放需要的参数
        mParamsObj = new PlayBackParams();
        // 播放控件
        mParamsObj.surfaceView = mSurfaceView;
    }

    /**
     * 进行远程回放录像查询
     * 
     * @since V1.0
     */
    public void queryPlaybackInfo() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                // cms平台地址
                String servAddr = Config.getIns().getServerAddr();
                // 登录成功后返回的回话ID
                String sessionID = TempData.getIns().getLoginData().sessionID;
                // 查询的录像类型，1-计划录像，2-移动录像，16-手动录像，4-报警录像
                String recordType = "1,2,4,16";
                // 查询的录像位置，也就是存储介质类型0-IPSAN、1-设备录像、2-PCNVR、3-ENVR、4-CISCO、5-DSNVR、7-CVR，目前支持单个查询
                String recordPos = "1";

                Calendar startCalendar = Calendar.getInstance();
                Calendar endCalendar = Calendar.getInstance();
                // 这里startCalendar 设置的时间是2014年6月10号00:00::00,其实是2014年7月10号00:00:00，具体原因参考Calendar类
                startCalendar.set(2014, 6, 10, 0, 0, 0);
                // 这里endCalendar 设置的时间是2014年6月10号23:59::59,其实是2014年7月10号23:59:59，具体原因参考Calendar类
                endCalendar.set(2014, 6, 10, 23, 59, 59);
                // 查询录像库中的时间对象，注意Calendar时间，使用前请先了解下Calendar
                com.hikvision.vmsnetsdk.ABS_TIME startTime = new com.hikvision.vmsnetsdk.ABS_TIME(startCalendar);
                com.hikvision.vmsnetsdk.ABS_TIME endTime = new com.hikvision.vmsnetsdk.ABS_TIME(endCalendar);

                setParamsObjTime(startTime, endTime);
                RecordInfo recordInfo = new RecordInfo();
                if (mVmsNetSDK == null) {
                    Log.e(Constants.LOG_TAG, "mVmsNetSDK is " + null);
                    return;
                }
                boolean ret = mVmsNetSDK.queryCameraRecord(servAddr, sessionID, mCameraID, recordType, recordPos,
                        startTime, endTime, recordInfo);
                Log.i(Constants.LOG_TAG, "ret : " + ret);
                if (recordInfo != null) {
                    Log.i(Constants.LOG_TAG, "segmentListPlayUrl : " + recordInfo.segmentListPlayUrl);
                }

                if (ret) {
                    mParamsObj.url = recordInfo.segmentListPlayUrl;
                }
                DeviceInfo deviceInfo = new DeviceInfo();
                ret = mVmsNetSDK.getDeviceInfo(servAddr, sessionID, mDeviceID, deviceInfo);
                if (ret && deviceInfo != null) {
                    mParamsObj.name = deviceInfo.userName;
                    mParamsObj.passwrod = deviceInfo.password;
                } else {
                    Log.e(Constants.LOG_TAG, "getDeviceInfo():: fail");
                }
            }
        }).start();
    }

    /**
     * 设置远程回放取流的开始时间和结束时间
     * 
     * @param startTime
     * @param endTime
     * @since V1.0
     */
    protected void setParamsObjTime(com.hikvision.vmsnetsdk.ABS_TIME startTime, com.hikvision.vmsnetsdk.ABS_TIME endTime) {
        if (startTime == null || endTime == null) {
            Log.e(Constants.LOG_TAG, "setParamsObjTime():: startTime is " + startTime + "endTime is " + endTime);
        }
        // 取流库中的时间对象
        ABS_TIME rtspEndTime = new ABS_TIME();
        ABS_TIME rtspStartTime = new ABS_TIME();

        // 设置播放结束时间
        rtspEndTime.setYear(endTime.dwYear);
        // 之所以要加1，是由于我们查询接口中的时间和取流中的时间采用的是两个自定义的时间类，这个地方开发者按照demo中实现就可以了。
        rtspEndTime.setMonth(endTime.dwMonth + 1);
        rtspEndTime.setDay(endTime.dwDay);
        rtspEndTime.setHour(endTime.dwHour);
        rtspEndTime.setMinute(endTime.dwMinute);
        rtspEndTime.setSecond(endTime.dwSecond);

        // 设置开始播放时间
        rtspStartTime.setYear(startTime.dwYear);
        // 之所以要加1，是由于我们查询接口中的时间和取流中的时间采用的是两个自定义的时间类，这个地方开发者按照demo中实现就可以了。
        rtspStartTime.setMonth(startTime.dwMonth + 1);
        rtspStartTime.setDay(startTime.dwDay);
        rtspStartTime.setHour(startTime.dwHour);
        rtspStartTime.setMinute(startTime.dwMinute);
        rtspStartTime.setSecond(startTime.dwSecond);

        if (mParamsObj != null) {
            // 设置开始远程回放的开始时间和结束时间。
            mParamsObj.endTime = rtspEndTime;
            mParamsObj.startTime = rtspStartTime;
        }
    }

    /**
     * 初始化控件 void
     * 
     * @since V1.0
     */
    private void setUpView() {
        mSurfaceView = (SurfaceView) findViewById(R.id.playbackSurfaceView);

        mStartButton = (Button) findViewById(R.id.playBackStart);
        mStartButton.setOnClickListener(this);

        mStopButton = (Button) findViewById(R.id.playBackStop);
        mStopButton.setOnClickListener(this);

        mPauseButton = (Button) findViewById(R.id.playBackPause);
        mPauseButton.setOnClickListener(this);

        mCaptureButton = (Button) findViewById(R.id.playBackCapture);
        mCaptureButton.setOnClickListener(this);

        mRecordButton = (Button) findViewById(R.id.playBackRecord);
        mRecordButton.setOnClickListener(this);

        mAudioButton = (Button) findViewById(R.id.playBackRadio);
        mAudioButton.setOnClickListener(this);

        mProgressBar = (ProgressBar) findViewById(R.id.playBackProgressBar);
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.playBackStart:
                startBtnOnClick();
            break;

            case R.id.playBackStop:
                stopBtnOnClick();
            break;

            case R.id.playBackPause:
                pauseBtnOnClick();
            break;

            case R.id.playBackCapture:
                captureBtnOnClick();
            break;

            case R.id.playBackRecord:
                recordBtnOnClick();
            break;

            case R.id.playBackRadio:
                audioBtnOnClick();
            break;

        }
    }

    /**
     * 音频按钮 void
     * 
     * @since V1.0
     */
    private void audioBtnOnClick() {
        if (null != mControl) {
            if (mIsAudioOpen) {
                mControl.stopAudio();
                mIsAudioOpen = false;
                UIUtil.showToast(PlayBackActivity.this, "关闭音频");
                mAudioButton.setText("开启音频");
            } else {
                boolean ret = mControl.startAudio();
                if (!ret) {
                    mIsAudioOpen = false;
                    UIUtil.showToast(PlayBackActivity.this, "开启音频失败");
                    mAudioButton.setText("开启音频");
                } else {
                    mIsAudioOpen = true;
                    // 开启音频成功，并不代表一定有声音，需要设备开启声音。
                    UIUtil.showToast(PlayBackActivity.this, "开启音频成功");
                    mAudioButton.setText("关闭音频");
                }
            }
        }
    }

    /**
     * 启动播放 void
     * 
     * @since V1.0
     */
    private void startBtnOnClick() {
        mProgressBar.setVisibility(View.VISIBLE);
        if (null != mProgressBar) {
            new Thread() {
                @Override
                public void run() {
                    if (null != mControl) {
                        mControl.startPlayBack(mParamsObj);
                    }
                    super.run();
                }
            }.start();
        }
    }

    /**
     * 停止播放 void
     * 
     * @since V1.0
     */
    private void stopBtnOnClick() {
        if (null != mControl) {
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    mControl.stopPlayBack();
                }
            }.start();
        }
    }

    /**
     * 暂停、回放播放 void
     * 
     * @since V1.0
     */
    private void pauseBtnOnClick() {
        if (null != mControl) {
            new Thread() {
                @Override
                public void run() {
                    if (!mIsPause) {
                        mControl.pausePlayBack();
                    } else {
                        mControl.resumePlayBack();
                    }
                    super.run();
                }
            }.start();
        }
    }

    /**
     * 抓拍 void
     * 
     * @since V1.0
     */
    private void captureBtnOnClick() {
        if (null != mControl) {
            // 随即生成一个1到10000的数字，用于抓拍图片名称的一部分，区分图片
            int recordIndex = new Random().nextInt(10000);
            boolean ret = mControl.capture(UtilFilePath.getPictureDirPath().getAbsolutePath(), "Picture" + recordIndex
                    + ".jpg");
            if (ret) {
                UIUtil.showToast(PlayBackActivity.this, "抓拍成功");
                UtilAudioPlay.playAudioFile(PlayBackActivity.this, R.raw.paizhao);
            } else {
                UIUtil.showToast(PlayBackActivity.this, "抓拍失败");
                DebugLog.error(TAG, "captureBtnOnClick():: 抓拍失败");
            }
        }
    }

    /**
     * 录像 void
     * 
     * @since V1.0
     */
    private void recordBtnOnClick() {
        if (null != mControl) {
            if (!mIsRecord) {
                int recordIndex = new Random().nextInt(10000);
                mControl.startRecord(UtilFilePath.getVideoDirPath().getAbsolutePath(), "Video" + recordIndex + ".mp4");
                mIsRecord = true;
                UIUtil.showToast(PlayBackActivity.this, "启动录像成功");
                mRecordButton.setText("停止录像");
            } else {
                mControl.stopRecord();
                mIsRecord = false;
                UIUtil.showToast(PlayBackActivity.this, "停止录像成功");
                mRecordButton.setText("开始录像");
            }
        }
    }

    @Override
    public void onMessageCallback(int message) {
        sendMessageCase(message);
    }

    /**
     * 发送消息
     * 
     * @param i void
     * @since V1.0
     */
    private void sendMessageCase(int i) {
        if (null != mMessageHandler) {
            Message msg = Message.obtain();
            msg.arg1 = i;
            mMessageHandler.sendMessage(msg);
        }
    }

    @SuppressLint("HandlerLeak")
    class MyHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.arg1) {
                case ConstantPlayBack.START_RTSP_SUCCESS:
                    UIUtil.showToast(PlayBackActivity.this, "启动取流库成功");
                break;

                case ConstantPlayBack.START_RTSP_FAIL:
                    UIUtil.showToast(PlayBackActivity.this, "启动取流库失败");
                    if (null != mProgressBar) {
                        mProgressBar.setVisibility(View.GONE);
                    }
                break;

                case ConstantPlayBack.PAUSE_SUCCESS:
                    UIUtil.showToast(PlayBackActivity.this, "暂停成功");
                    mPauseButton.setText("恢复");
                    mIsPause = true;
                break;

                case ConstantPlayBack.PAUSE_FAIL:
                    UIUtil.showToast(PlayBackActivity.this, "暂停失败");
                    mPauseButton.setText("暂停");
                    mIsPause = false;

                break;

                case ConstantPlayBack.RESUEM_FAIL:
                    UIUtil.showToast(PlayBackActivity.this, "恢复播放失败");
                    mPauseButton.setText("恢复");
                    mIsPause = true;
                break;

                case ConstantPlayBack.RESUEM_SUCCESS:
                    UIUtil.showToast(PlayBackActivity.this, "恢复播放成功");
                    mPauseButton.setText("暂停");
                    mIsPause = false;
                break;

                case ConstantPlayBack.START_OPEN_FAILED:
                    UIUtil.showToast(PlayBackActivity.this, "启动播放库失败");
                    if (null != mProgressBar) {
                        mProgressBar.setVisibility(View.GONE);
                    }
                break;

                case ConstantPlayBack.PLAY_DISPLAY_SUCCESS:
                    if (null != mProgressBar) {
                        mProgressBar.setVisibility(View.GONE);
                    }
                    DebugLog.info(TAG, "回放成功");
                break;
                case ConstantPlayBack.CAPTURE_FAILED_NPLAY_STATE:
                    UIUtil.showToast(PlayBackActivity.this, "非播状态不能抓怕");
                break;
                case ConstantPlayBack.PAUSE_FAIL_NPLAY_STATE:
                    UIUtil.showToast(PlayBackActivity.this, "非播放状态不能暂停");
                break;
                case ConstantPlayBack.RESUEM_FAIL_NPAUSE_STATE:
                    UIUtil.showToast(PlayBackActivity.this, "非播放状态");
                break;

                case RtspClient.RTSPCLIENT_MSG_CONNECTION_EXCEPTION:
                    if (null != mProgressBar) {
                        mProgressBar.setVisibility(View.GONE);
                    }
                    UIUtil.showToast(PlayBackActivity.this, "RTSP链接异常");
                break;

            }
        }
    }

}
