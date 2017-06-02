package com.login;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;

import com.consts.Constants;
import com.data.Config;
import com.data.TempData;
import com.demo.v3.R;
import com.hikvision.vmsnetsdk.LineInfo;
import com.hikvision.vmsnetsdk.ServInfo;
import com.hikvision.vmsnetsdk.VMSNetSDK;
import com.resource.ResourceListActivity;
import com.resource.ResourceListAdapter;
import com.util.UIUtil;

/**
 * 该类主要用于登录平台使用
 * 
 * @author zhoudaihui
 * @Data 2014-7-9
 */
public class LoginActivity extends Activity {

    @SuppressLint("HandlerLeak")
    private final class MsgHandler extends Handler {

        @SuppressWarnings("unchecked")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Constants.Login.GET_LINE_IN_PROCESS:
                    showGetLineProgress();
                break;
                case Constants.Login.GET_LINE_SUCCESS:
                    onGetLineSuccess((List<Object>) msg.obj);
                break;
                case Constants.Login.GET_LINE_FAILED:
                    onGetLineFailed();
                break;
                case Constants.Login.SHOW_LOGIN_PROGRESS:
                    showLoginProgress();
                break;
                case Constants.Login.CANCEL_LOGIN_PROGRESS:
                    cancelProgress();
                break;
                case Constants.Login.LOGIN_SUCCESS:
                    // 登录成功
                    onLoginSuccess();
                break;
                case Constants.Login.LOGIN_FAILED:
                    // 登录失败
                    onLoginFailed();
                break;

                default:
                break;
            }

        }
    }

    /** 发送消息的对象 */
    private MsgHandler          handler       = new MsgHandler();
    /** 用户名输入框 */
    private EditText            username;
    /** 密码输入框 */
    private EditText            passwd;
    /** 登录按钮 */
    private Button              loginBtn;
    /** 自动登录复选框 */
    private CheckBox            autologinChk;
    /** 登录平台地址 */
    private String              servAddr;
    /** 用户选中的线路 */
    private LineInfo            lineInfo;
    /** 登录返回的数据 */
    private ServInfo            servInfo;
    /** 是否是第一次执行onResume方法 */
    private boolean             isFirstResume = true;
    /** 线路选择下拉框 */
    private Spinner             lineSpinner;
    /** 获取线路按钮 */
    private Button              fetchLineBtn;
    /** 线路列表适配器 */
    private ResourceListAdapter lineAdapter;
    /** 服务器地址输入框 */
    private EditText            serverAddrEt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initUI();

        initData();
    }

    /**
     * 获取线路失败
     */
    public void onGetLineFailed() {
        cancelProgress();
        UIUtil.showToast(LoginActivity.this, getString(R.string.getline_fail_tip, UIUtil.getErrorDesc()));
    }

    /**
     * 获取线路成功，刷新线路列表
     * 
     * @param lineList
     */
    private void onGetLineSuccess(List<Object> lineList) {
        cancelProgress();
        UIUtil.showToast(LoginActivity.this, R.string.getline_suc_tip);
        if (lineList == null || lineList.isEmpty()) {
            return;
        }
        lineAdapter.setData(lineList);
    }

    /**
     * 登录失败
     */
    public void onLoginFailed() {
        cancelProgress();
        UIUtil.showToast(this, getString(R.string.login_failed, UIUtil.getErrorDesc()));
    }

    /**
     * 登录成功
     */
    public void onLoginSuccess() {
        cancelProgress();
        UIUtil.showToast(this, R.string.login_suc_tip);
        // 跳转到获取控制中心列表界面
        gotoResourceListActivity();
    }

    private void gotoResourceListActivity() {
        startActivity(new Intent(this, ResourceListActivity.class));
    }

    /**
     * 登录进度条
     */
    private void showGetLineProgress() {
        UIUtil.showProgressDialog(this, R.string.fetchline_process_tip);
    }

    /**
     * 登录进度条
     */
    private void showLoginProgress() {
        UIUtil.showProgressDialog(this, R.string.login_process_tip);
    }

    /**
     * 取消进度条
     */
    private void cancelProgress() {
        UIUtil.cancelProgressDialog();
    }

    /**
     * 初始化界面数据
     */
    private void initData() {
        servInfo = new ServInfo();

        // 为了方便测试，设置默认用户名密码
        username.setText("admin");
        passwd.setText("12345");
        // 登录平台地址
        servAddr = Config.getIns().getServerAddr();
        serverAddrEt.setText(servAddr);
    }

    /**
     * 初始化UI控件
     */
    private void initUI() {
        username = (EditText) findViewById(R.id.username);
        passwd = (EditText) findViewById(R.id.passwd);
        loginBtn = (Button) findViewById(R.id.loginbtn);
        autologinChk = (CheckBox) findViewById(R.id.autologin_chk);

        serverAddrEt = (EditText) findViewById(R.id.server_addr_et);
        lineSpinner = (Spinner) findViewById(R.id.lineSpinner);
        fetchLineBtn = (Button) findViewById(R.id.fetchLineBtn);

        lineAdapter = new ResourceListAdapter(this);
        lineSpinner.setAdapter(lineAdapter);
        lineSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                lineInfo = (LineInfo) parent.getAdapter().getItem(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        fetchLineBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                fetchLine();
            }
        });

        loginBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });

        autologinChk.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i(Constants.LOG_TAG, "autoLoginChk : " + isChecked);
                Config.getIns().setAutoLogin(isChecked);
            }
        });
        autologinChk.setChecked(Config.getIns().isAutoLogin());
    }

    /**
     * 获取线路
     */
    protected void fetchLine() {
        servAddr = serverAddrEt.getText().toString().trim();
        if (servAddr.length() <= 0) {
            UIUtil.showToast(this, R.string.serveraddr_empty_tip);
            return;
        }
        new Thread() {
            public void run() {
                handler.sendEmptyMessage(Constants.Login.GET_LINE_IN_PROCESS);
                List<LineInfo> lineInfoList = new ArrayList<LineInfo>();
                Log.i(Constants.LOG_TAG, "servAddr:" + servAddr);
                boolean ret = VMSNetSDK.getInstance().getLineList(servAddr, lineInfoList);
                if (ret) {
                    Message msg = new Message();
                    msg.what = Constants.Login.GET_LINE_SUCCESS;
                    msg.obj = lineInfoList;
                    handler.sendMessage(msg);
                } else {
                    handler.sendEmptyMessage(Constants.Login.GET_LINE_FAILED);
                }
            };
        }.start();
    }

    @Override
    protected void onResume() {
        servAddr = Config.getIns().getServerAddr();
        super.onResume();
        autoLogin();
    }

    /**
     * 自动登录
     */
    private void autoLogin() {
        if (!isFirstResume) {
            return;
        }
        isFirstResume = false;
        if (Config.getIns().isAutoLogin()) {
            login();
        }
    }

    /**
     * 登录
     */
    protected void login() {
        servAddr = serverAddrEt.getText().toString().trim();
        if (servAddr.length() <= 0) {
            UIUtil.showToast(this, R.string.serveraddr_empty_tip);
            return;
        }
        Config.getIns().setServerAddr(servAddr);
        if (lineInfo == null) {
            UIUtil.showToast(this, R.string.line_unavailable);
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                handler.sendEmptyMessage(Constants.Login.SHOW_LOGIN_PROGRESS);
                String userName = username.getText().toString().trim();
                String password = passwd.getText().toString().trim();

                String macAddress = getMac();

                // 登录请求
                boolean ret = VMSNetSDK.getInstance().login(servAddr, userName, password, lineInfo.lineID, macAddress,
                        servInfo);
                if (servInfo != null) {
                    // 打印出登录时返回的信息
                    Log.i(Constants.LOG_TAG, "login ret : " + ret);
                    Log.i(Constants.LOG_TAG, "login response info[" + "sessionID:" + servInfo.sessionID + ",userID:"
                            + servInfo.userID + ",magInfo:" + servInfo.magInfo + ",picServerInfo:"
                            + servInfo.picServerInfo + ",ptzProxyInfo:" + servInfo.ptzProxyInfo + ",userCapability:"
                            + servInfo.userCapability + ",vmsList:" + servInfo.vmsList + ",vtduInfo:"
                            + servInfo.vtduInfo + ",webAppList:" + servInfo.webAppList + "]");
                }

                if (ret) {
                    TempData.getIns().setLoginData(servInfo);
                    handler.sendEmptyMessage(Constants.Login.LOGIN_SUCCESS);
                } else {
                    handler.sendEmptyMessage(Constants.Login.LOGIN_FAILED);
                }

            }
        }).start();
    }

    /**
     * 获取登录设备mac地址
     * 
     * @return
     */
    protected String getMac() {
        WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        String mac = wm.getConnectionInfo().getMacAddress();
        return mac == null ? "" : mac;
    }
}
