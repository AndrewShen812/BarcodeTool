package com.gwcd.sy.barcodetool.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.WriterException;
import com.gwcd.sy.barcodetool.util.StringUtils;
import com.gwcd.sy.barcodetool.R;
import com.gwcd.sy.barcodetool.zxing.activity.CaptureActivity;
import com.gwcd.sy.barcodetool.zxing.encoding.EncodingHandler;

/**
 * @项目名称：TwoCodeTools
 * @类名称：MainActivity
 * @类描述：二维码工具主页面
 * @创建人：Administrator
 * @创建时间：2015-5-30 下午4:23:47
 * @修改人：Administrator
 * @修改时间：2015-5-30 下午4:23:47
 * @修改备注：
 * @version
 */
public class MainActivity extends Activity implements OnClickListener {
    
    private Button mBtnScan;
    
    private Button mBtnClear;
    
    private Button mBtnCreate;
    
    private TextView mTvResult;
    
    private EditText mEtInput;
    
    private ImageView mImgBtnQRCode;
    
    private static final int DEF_W_H = 270;
    
    private static int wAndH = DEF_W_H;
    
    private TextView mTvUrlTip;
    
    private Context mContext;
    
    private long mFirstClick = 0;
    
    private TextView mTvShare;
    
    private Bitmap mQrCode;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        
        mContext = this;
        
        initView();
    }
    
    /**
     * @description 初始化UI控件
     * @date 2015-5-30
     */
    private void initView() {
        mBtnScan = (Button) findViewById(R.id.btn_scanning);
        mBtnCreate = (Button) findViewById(R.id.btn_creat_qrcode);
        mEtInput = (EditText) findViewById(R.id.et_input);
        mImgBtnQRCode = (ImageView) findViewById(R.id.imgbtn_qrcode);
        mImgBtnQRCode.setVisibility(View.GONE);
        mTvResult = (TextView) findViewById(R.id.result);
        mTvUrlTip = (TextView) findViewById(R.id.tv_main_goto_url);
        mTvShare = (TextView) findViewById(R.id.tv_main_share);
        mBtnClear = (Button) findViewById(R.id.btn_clear);
        mBtnScan.setOnClickListener(this);
        mBtnCreate.setOnClickListener(this);
        mTvUrlTip.setOnClickListener(this);
        mTvShare.setOnClickListener(this);
        mBtnClear.setOnClickListener(this);
    }
    
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_scanning:
                scanning();
                break;
            case R.id.btn_clear:
                mTvShare.setVisibility(View.GONE);
                mTvUrlTip.setVisibility(View.GONE);
                mTvResult.setText("");
                break;
            case R.id.btn_creat_qrcode:
                try {
                    if (mQrCode != null) {
                        mQrCode.recycle();
                        mQrCode = null;
                    }
                    mQrCode = CreatQRCode();
                    if (mQrCode == null) {
                        return;
                    }
                    mImgBtnQRCode.setImageBitmap(mQrCode);
                    mImgBtnQRCode.setVisibility(View.VISIBLE);
                }
                catch (WriterException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.tv_main_goto_url:
                accessUrl();
                break;
            case R.id.tv_main_share:
                // Intent i = new Intent(mContext, ShareActivity.class);
                // i.putExtra("ScanString", mTvResult.getText().toString());
                // startActivity(i);
                Intent intent = new Intent(Intent.ACTION_SEND); // 启动分享发送的属性
                intent.setType("text/plain"); // 分享发送的数据类型
                intent.putExtra(Intent.EXTRA_SUBJECT, "二维码扫描分享"); // 分享的主题
                intent.putExtra(Intent.EXTRA_TEXT, mTvResult.getText().toString()); // 分享的内容
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(Intent.createChooser(intent, "二维码扫描分享"));// 目标应用选择对
                break;
            default:
                break;
        }
    }
    
    /**
     * 扫描二维码
     */
    private void scanning() {
        mTvUrlTip.setVisibility(View.GONE);
        mTvShare.setVisibility(View.GONE);
        Intent intent = new Intent(mContext, CaptureActivity.class);
        startActivityForResult(intent, 0);
    }
    
    /**
     * 生成二维码
     * 
     * @return qrcode 返回生成的Bitmap格式的二维码图片
     * @throws WriterException
     */
    private Bitmap CreatQRCode() throws WriterException {
        String str = mEtInput.getText().toString();
        if (null == str || "".equals(str) || str.length() < 1) {
            Toast.makeText(mContext, "输入内容为空", Toast.LENGTH_SHORT).show();
            return null;
        }
        // 生成二维码
        float density = mContext.getResources().getDisplayMetrics().density;
        Log.i("SY", "density:" + density);
        wAndH = (int) (density * DEF_W_H + 0.5f);
        Log.i("SY", "final wAndH:" + wAndH);
        Bitmap qrcode = EncodingHandler.createQRCode(str, wAndH);
        // //生成底部显示内容的位图
        // SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // Bitmap bmContent = BarcodeCreater.creatCodeBitmap(time.format(new
        // Date()), wAndH, 0, MainActivity.this);
        // //生成竖式内容
        // String cloumn = "放心粮油追溯";
        // int qrWidth = qrcode.getWidth();
        // if(qrWidth <= 0)
        // qrWidth = 400;
        // Bitmap bmColumn = BarcodeCreater.creatColumnBitmap(cloumn,
        // (wAndH-qrWidth)/2, wAndH, MainActivity.this);
        // //组合二维码和内容
        // // Bitmap mixedBitmap = BarcodeCreater.mixtureBitmap(qrcode,
        // bmContent, new PointF(0, qrcode.getHeight()-50));
        // Bitmap mixedBitmap = BarcodeCreater.mixtureBitmap(qrcode, bmContent,
        // new PointF(0, qrcode.getHeight()-bmContent.getHeight()),
        // bmColumn, new PointF(0, 0));
        //
        // return mixedBitmap;
        return qrcode;
    }
    
    private void accessUrl() {
        new AlertDialog.Builder(mContext).setTitle("提示")
                                         .setMessage("该链接可能存在木马或病毒等风险，确定要访问吗？")
                                         .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                             @Override
                                             public void onClick(DialogInterface arg0, int arg1) {
                                                 Intent intent = new Intent();
                                                 intent.setAction("android.intent.action.VIEW");
                                                 Uri content_url = Uri.parse(mTvResult.getText().toString().trim());
                                                 intent.setData(content_url);
                                                 startActivity(intent);
                                             }
                                         })
                                         .setNegativeButton("取消", null)
                                         .create()
                                         .show();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            String scanResult = bundle.getString("result");
            mTvResult.setText(scanResult);
            mBtnClear.setVisibility(View.VISIBLE);
            mTvShare.setVisibility(View.VISIBLE);
            if (StringUtils.isUrl(scanResult)) {
                mTvUrlTip.setVisibility(View.VISIBLE);
            }
        }
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                long secondClick = System.currentTimeMillis();
                if (secondClick - mFirstClick > 2000) {
                    mFirstClick = secondClick;
                    Toast.makeText(mContext, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                }
                else {
                    finish();
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(0);
                }
                break;
            default:
                break;
        }
        return true;
    }
}
