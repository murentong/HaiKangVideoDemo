package com.resource;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.consts.Constants;
import com.data.TempData;
import com.demo.v3.R;
import com.hikvision.vmsnetsdk.CameraInfo;
import com.hikvision.vmsnetsdk.ControlUnitInfo;
import com.hikvision.vmsnetsdk.LineInfo;
import com.hikvision.vmsnetsdk.RegionInfo;
import com.live.LiveActivity;
import com.playback.PlayBackActivity;

public class ResourceListAdapter extends BaseAdapter {

	private List<?> data;

	private Activity a;

	private Dialog mDialog;
	public ResourceListAdapter(Activity a) {
		this.a = a;
	}

	public void setData(List<?> data) {
		this.data = data;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		if (data != null) {
			return data.size();
		}
		return 0;
	}

	@Override
	public Object getItem(int position) {
		if (data != null) {
			return data.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = a.getLayoutInflater().inflate(
					R.layout.simple_item_layout, null);
			holder.itemtxt = (TextView) convertView.findViewById(R.id.item_txt);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		final Object itemData = getItem(position);
		String desc = getItemDesc(itemData);
		holder.itemtxt.setText(desc);
		if (!(itemData instanceof LineInfo))
			convertView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					gotoNextLevelList(itemData);
				}
			});
		return convertView;
	}

	private String getItemDesc(Object itemData) {
		if (itemData instanceof ControlUnitInfo) {
			ControlUnitInfo info = (ControlUnitInfo) itemData;
			return info.name/*"[controlUnitID:"
			+ info.controlUnitID+",name:" + info.name + ",parentID:" + info.parentID + "]"*/;
		}

		if (itemData instanceof RegionInfo) {
			RegionInfo info = (RegionInfo) itemData;
			return info.name/*"[regionID:" + info.regionID+",name:" + info.name + ",controlUnitID:"
					+ info.controlUnitID + ",parentID:" + info.parentID
					+ ",regionID:" + info.regionID + "]"*/;
		}

		if (itemData instanceof CameraInfo) {
			CameraInfo info = (CameraInfo) itemData;
			return info.name/*"[cameraID:" + info.cameraID + ",name:" + info.name+"]"*/;
		}
		
		if(itemData instanceof LineInfo)
		{
			LineInfo info = (LineInfo)itemData;
			return info.lineName;
		}

		return null;
	}

	/**
	 * 
	 * 
	 * @param info
	 */
	protected void gotoNextLevelList(final Object info) {
		//当前是监控点（摄像头）
		if (info instanceof CameraInfo) {
			gotoLiveOrPlayBack((CameraInfo)info);
			return;
		}

		// 当前是控制中心
		if (info instanceof ControlUnitInfo) {
			
            gotoNextLevelListFromCtrlUnit((ControlUnitInfo)info);
			return;
		}

		// 当前是区域
		if (info instanceof RegionInfo) {
			
			gotoNextLevelListFromRegion((RegionInfo)info);
			return ;
		}
	}


	
	private void gotoLiveOrPlayBack(final CameraInfo info) {
		String[] datas = new String[]{"预览","回放"};
		mDialog = new AlertDialog.Builder(a).setSingleChoiceItems(datas, 0, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mDialog.dismiss();
				switch (which) {

				case 0:
					gotoLive(info);
					break;
				case 1:
					gotoPlayback(info);
					break;
				default:
					break;
				}
			}
		}).create();
		mDialog.show();
		
	}

	/**
	  * 进入远程回放
	  * @param info
	  * @since V1.0
	  */
	protected void gotoPlayback(CameraInfo info) {
	    if(info == null){
	        Log.e(Constants.LOG_TAG,"gotoPlayback():: fail");
	        return;
	    }
		Intent it = new Intent(a, PlayBackActivity.class);
		it.putExtra(Constants.IntentKey.CAMERA_ID, info.cameraID);
		it.putExtra(Constants.IntentKey.DEVICE_ID, info.deviceID);
		a.startActivity(it);
		
	}

	/**
	  * 进入实时预览
	  * @param info
	  * @since V1.0
	  */
	protected void gotoLive(CameraInfo info) {
	    if(info == null){
            Log.e(Constants.LOG_TAG,"gotoLive():: fail");
            return;
        }
		Intent it = new Intent(a, LiveActivity.class);
		it.putExtra(Constants.IntentKey.CAMERA_ID, info.cameraID);
		TempData.getIns().setCameraInfo(info);
		a.startActivity(it);
	}

	/**
	 * 从控制中心获取下级列表
	 * @param info
	 */
	private void gotoNextLevelListFromCtrlUnit(ControlUnitInfo info) {
		Intent it = new Intent(a, ResourceListActivity.class);
		it.putExtra(Constants.IntentKey.CONTROL_UNIT_ID, info.controlUnitID);
		a.startActivity(it);
	}

	/**
	 * 从区域获取下级列表
	 * @param info
	 */
	private void gotoNextLevelListFromRegion(RegionInfo info) {
		Intent it = new Intent(a, ResourceListActivity.class);
		it.putExtra(Constants.IntentKey.REGION_ID, info.regionID);
		a.startActivity(it);
	}
	private static final class ViewHolder {
		TextView itemtxt;
	}
}
