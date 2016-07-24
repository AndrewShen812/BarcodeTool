package com.gwcd.sy.barcodetool.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.gwcd.sy.barcodetool.BarcodeApp;
import com.gwcd.sy.barcodetool.util.IntentUtils;
import com.gwcd.sy.barcodetool.R;

public class ShareActivity extends Activity implements OnClickListener {
    
    private RelativeLayout mLayoutWX;
    
    private RelativeLayout mLayoutQQ;
    
    private RelativeLayout mLayoutWB;
    
    private RelativeLayout mLayoutDX;
    
    private String mScanString;
    
    private Context mContext;
    
    public static final String QQ_PACKAGE = "com.tencent.mobileqq";
    public static final String QQ_CLASS = "com.tencent.mobileqq.activity.JumpActivity";
    public static final String WEIXIN_PACKAGE = "com.tencent.mm";
    public static final String WEIXIN_CLASS = "com.tencent.mm.ui.tools.ShareImgUI";
    public static final int NOTIFY_QQ = 1;
    public static final int NOTIFY_WX = 2;  
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_share);
        
        mContext = this;
        
        initView();
        
        mScanString = getIntent().getStringExtra("ScanString");
    }
    
    private void initView() {
        
        mLayoutWX = (RelativeLayout) findViewById(R.id.ll_select_dialog_weixin);
        mLayoutQQ = (RelativeLayout) findViewById(R.id.ll_select_dialog_qq);
        mLayoutWB = (RelativeLayout) findViewById(R.id.ll_select_dialog_weibo);
        mLayoutDX = (RelativeLayout) findViewById(R.id.ll_select_dialog_sms);
        
        mLayoutWX.setOnClickListener(this);
        mLayoutQQ.setOnClickListener(this);
        mLayoutWB.setOnClickListener(this);
        mLayoutDX.setOnClickListener(this);
    }
    
    @Override
    public void onClick(View arg0) {
        switch (arg0.getId()) {
            case R.id.ll_select_dialog_weixin:
                startNotifySocial(mContext, NOTIFY_WX, mScanString);
                break;
            case R.id.ll_select_dialog_qq:
                startNotifySocial(mContext, NOTIFY_QQ, mScanString);
                break;
            case R.id.ll_select_dialog_weibo:
                Toast.makeText(mContext, "微博分享即将上线，敬请期待...", Toast.LENGTH_SHORT).show();
                break;
            case R.id.ll_select_dialog_sms:
                IntentUtils.sendSMS(mContext, mScanString);
                break;
            default:
                break;
        }
    }
    
    /**
     * 
     * @description 启动分享的应用
     * @date 2015-5-30
     * @param context
     * @param type
     * @param string
     */
    public static void startNotifySocial(Context context, int type, String string) {
        // 判断通知类型
        String packageName = null;
        String className = null;
        if (type == NOTIFY_QQ) {
            packageName = QQ_PACKAGE;
            className = QQ_CLASS;
        }
        else if (type == NOTIFY_WX) {
            packageName = WEIXIN_PACKAGE;
            className = WEIXIN_CLASS;
        }
        // 检查APK
        if (!isInstallNotifyAPP(packageName)) {
            if (type == NOTIFY_QQ) {
                Toast.makeText(context, "您的手机暂未安装QQ,请安装后使用。", Toast.LENGTH_SHORT).show();
            }
            else if (type == NOTIFY_WX) {
                Toast.makeText(context, "您的手机暂未安装微信,请安装后使用。", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.setClassName(packageName, className);
        intent.putExtra(Intent.EXTRA_TEXT, string);
        Intent.createChooser(intent, "Share");
        context.startActivity(intent);
    }
    
    /**
     * 
     * @description 检查应用是否安装
     * @date 2015-5-30
     * @param packageName
     * @return
     */
    public static boolean isInstallNotifyAPP(String packageName) {
        try {
            BarcodeApp.getInstance().getPackageManager().getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        }
        catch (NameNotFoundException e) {
            return false;
        }
    }
}
