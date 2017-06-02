package com.resource;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ListView;

import com.callbacks.MsgCallback;
import com.callbacks.MsgIds;
import com.consts.Constants;
import com.demo.v3.R;
import com.util.UIUtil;

public class ResourceListActivity extends Activity implements MsgCallback {
	/**
	 * 资源列表
	 */
	private ListView resourceListView;
	/**
	 * 父节点资源类型，TYPE_UNKNOWN表示首次获取资源列表
	 */
	private int pResType = Constants.Resource.TYPE_UNKNOWN;
	/**
	 * 父控制中心的id，只有当parentResType为TYPE_CTRL_UNIT才有用
	 */
	private int pCtrlUnitId;
	/**
	 * 父区域的id，只有当parentResType为TYPE_REGION才有用
	 */
	private int pRegionId;
	/**
	 * 资源列表适配器
	 */
	private ResourceListAdapter adapter;
	/**
	 * 消息处理Handler
	 */
	private MsgHandler handler = new MsgHandler();
	/**
	 * 获取资源逻辑控制类
	 */
	private ResourceControl rc;

	@SuppressLint("HandlerLeak")
    private final class MsgHandler extends Handler {
		@SuppressWarnings("unchecked")
        @Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {

			// 获取控制中心列表成功
			case MsgIds.GET_C_F_NONE_SUC:
				// 从控制中心获取下级资源列表成功
			case MsgIds.GET_SUB_F_C_SUC:
				// 从区域获取下级列表成功
			case MsgIds.GET_SUB_F_R_SUC:
				refreshResList((List<Object>) msg.obj);
				break;
			// 获取控制中心列表失败
			case MsgIds.GET_C_F_NONE_FAIL:
				// 调用getControlUnitList失败
			case MsgIds.GET_CU_F_CU_FAIL:
				// 调用getRegionListFromCtrlUnit失败
			case MsgIds.GET_R_F_C_FAIL:
				// 调用getCameraListFromCtrlUnit失败
			case MsgIds.GET_C_F_C_FAIL:
				// 从控制中心获取下级资源列表成失败
			case MsgIds.GET_SUB_F_C_FAIL:
				// 调用getRegionListFromRegion失败
			case MsgIds.GET_R_F_R_FAIL:
				// 调用getCameraListFromRegion失败
			case MsgIds.GET_C_F_R_FAIL:
				// 从区域获取下级列表失败
			case MsgIds.GET_SUB_F_R_FAILED:
				onGetResListFailed();
			default:
				break;
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ctrl_unit_list);

		initUI();

		initData();

		reqResList();
	}

	/**
	 * 初始化数据
	 */
	private void initData() {
		Intent it = getIntent();
		if (it.hasExtra(Constants.IntentKey.CONTROL_UNIT_ID)) {
			pResType = Constants.Resource.TYPE_CTRL_UNIT;
			pCtrlUnitId = it
					.getIntExtra(Constants.IntentKey.CONTROL_UNIT_ID, 0);
			Log.i(Constants.LOG_TAG,
					"Getting resource from ctrlunit.parent id is "
							+ pCtrlUnitId);
		} else if (it.hasExtra(Constants.IntentKey.REGION_ID)) {
			pResType = Constants.Resource.TYPE_REGION;
			pRegionId = it.getIntExtra(Constants.IntentKey.REGION_ID, 0);
			Log.i(Constants.LOG_TAG,
					"Getting resource from region. parent id is " + pRegionId);
		} else {
			pResType = Constants.Resource.TYPE_UNKNOWN;
			Log.i(Constants.LOG_TAG, "Getting resource for the first time.");
		}
	}

	/**
	 * 调用接口失败时，界面弹出提示
	 */
	private void onGetResListFailed() {
		UIUtil.showToast(this,
				getString(R.string.fetch_reslist_failed, UIUtil.getErrorDesc()));
	}

	/**
	 * 获取数据成功后刷新列表
	 * 
	 * @param data
	 */
	private void refreshResList(List<Object> data) {
		if (data == null || data.isEmpty()) {
			UIUtil.showToast(this, R.string.no_data_tip);
			return;
		}
		UIUtil.showToast(this, R.string.fetch_resource_suc);
		adapter.setData(data);
	}

	/**
	 * 请求资源列表
	 */
	private void reqResList() {
		rc = new ResourceControl();
		rc.setCallback(this);
		new Thread(new Runnable() {
			@Override
			public void run() {
				int pId = 0;
				if (Constants.Resource.TYPE_CTRL_UNIT == pResType) {
					pId = pCtrlUnitId;
				} else if (Constants.Resource.TYPE_REGION == pResType) {
					pId = pRegionId;
				}
				rc.reqResList(pResType, pId);
			}
		}).start();
	}

	/**
	 * 初始化界面UI控件
	 */
	private void initUI() {
		resourceListView = (ListView) findViewById(R.id.ctrlunit_list);
		adapter = new ResourceListAdapter(this);
		resourceListView.setAdapter(adapter);
	}

	@Override
	public void onMsg(int msgId, Object data) {
		Message msg = new Message();
		msg.what = msgId;
		msg.obj = data;
		handler.sendMessage(msg);
	}

}
