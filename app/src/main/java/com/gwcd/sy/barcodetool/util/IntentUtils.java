package com.gwcd.sy.barcodetool.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.gwcd.sy.barcodetool.BarcodeApp;

/**
 * @项目名称：TwoCodeTools    
 * @类名称：IntentUtils    
 * @类描述：    
 * @创建人：Administrator    
 * @创建时间：2015-5-30 下午9:00:48    
 * @修改人：Administrator    
 * @修改时间：2015-5-30 下午9:00:48    
 * @修改备注：    
 * @version     
 */
public final class IntentUtils {
	
	/** 拨打电话  */
	public static void call(Context context , String phoneNum) {
		Uri uri = Uri.parse("tel:"+phoneNum);
		Intent intent = new Intent(Intent.ACTION_DIAL, uri);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}
	
	/** 进入发短信界面 */
	public static void sendSMS(Context context , String content) {
		Uri uri = Uri.parse("smsto:");
		Intent intent = new Intent(Intent.ACTION_SENDTO,uri);
		intent.putExtra("sms_body", content);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}
	
	/** 跳转到网络设置 */
	public static void settingNetwork() {
		Intent intent = new Intent("android.settings.WIRELESS_SETTINGS");
		BarcodeApp.getInstance().startActivity(intent);
	}
}
