package com.gwcd.sy.barcodetool.zxing.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.gwcd.sy.barcodetool.R;
import com.gwcd.sy.barcodetool.util.FileUtils;
import com.gwcd.sy.barcodetool.zxing.camera.CameraManager;
import com.gwcd.sy.barcodetool.zxing.decoding.CaptureActivityHandler;
import com.gwcd.sy.barcodetool.zxing.decoding.DecodeHandler;
import com.gwcd.sy.barcodetool.zxing.decoding.InactivityTimer;
import com.gwcd.sy.barcodetool.zxing.view.ViewfinderView;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

/**
 * Initial the camera
 *
 * @author Ryan.Tang
 */
public class CaptureActivity extends Activity implements Callback {

    public static final int REQUEST_CODE_SELECT_PHOTO = 100;

    public static final int REQUEST_CODE_CROP_PHOTO = 101;

    private static final int MSG_DECODE_QR_FINISH = 18001;

    private CaptureActivityHandler handler;

    private ViewfinderView viewfinderView;

    private boolean hasSurface;

    private Vector<BarcodeFormat> decodeFormats;

    private String characterSet;

    private InactivityTimer inactivityTimer;

    private MediaPlayer mediaPlayer;

    private boolean playBeep;

    private static final float BEEP_VOLUME = 0.10f;

    private boolean vibrate;

    private ImageView mIvFlash;

    private ImageView mIvAlbum;

    private boolean flashOn = false;

    private Handler mDecImgHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (MSG_DECODE_QR_FINISH == msg.what && msg.getData() != null) {
                String result = msg.getData().getString("result");
                if (!TextUtils.isEmpty(result)) {
                    Intent intent = new Intent();
                    intent.putExtra("result", result);
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    Toast.makeText(CaptureActivity.this, getString(R.string.qr_recognise_fail), Toast.LENGTH_SHORT).show();
                }
            }
            return true;
        }
    });

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zxing_camera);
        CameraManager.init(getApplication());
        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);

        mIvFlash = (ImageView) findViewById(R.id.iv_zxing_camera_flash);
        mIvFlash.setColorFilter(Color.WHITE);
        mIvFlash.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flashOn) {
                    mIvFlash.setColorFilter(Color.WHITE);
                    mIvFlash.setBackgroundResource(R.drawable.shape_round_bg);
                    CameraManager.get().disableFlashLight();
                } else {
                    mIvFlash.setColorFilter(Color.BLACK);
                    mIvFlash.setBackgroundResource(R.drawable.shape_round_bg_white);
                    CameraManager.get().enableFlashLight();
                }
                flashOn = !flashOn;
            }
        });
        mIvAlbum = (ImageView) findViewById(R.id.iv_zxing_camera_album);
        mIvAlbum.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent localIntent = new Intent();
                localIntent.setAction("android.intent.action.GET_CONTENT");
                localIntent.setType("image/*");
                startActivityForResult(localIntent, REQUEST_CODE_SELECT_PHOTO);
            }
        });
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onResume() {
        super.onResume();
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        decodeFormats = null;
        characterSet = null;

        playBeep = true;
        AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            playBeep = false;
        }
        initBeepSound();
        vibrate = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        CameraManager.get().closeDriver();
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
    }

    /**
     * Handler scan result
     *
     * @param result
     * @param barcode
     */
    public void handleDecode(Result result, Bitmap barcode) {
        inactivityTimer.onActivity();
        playBeepSoundAndVibrate();
        String resultString = result.getText();
        // FIXME
        if (resultString.equals("")) {
            Toast.makeText(CaptureActivity.this,
                    "Scan failed!",
                    Toast.LENGTH_SHORT).show();
        } else {
            Intent resultIntent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putString("result", resultString);
            resultIntent.putExtras(bundle);
            this.setResult(RESULT_OK, resultIntent);
        }
        CaptureActivity.this.finish();
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            CameraManager.get().openDriver(surfaceHolder);
        } catch (IOException ioe) {
            return;
        } catch (RuntimeException e) {
            return;
        }
        if (handler == null) {
            handler = new CaptureActivityHandler(this,
                    decodeFormats,
                    characterSet);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder,
                               int format,
                               int width,
                               int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;

    }

    public ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();

    }

    private void initBeepSound() {
        if (playBeep && mediaPlayer == null) {
            // The volume on STREAM_SYSTEM is not adjustable, and users found it
            // too loud,
            // so we now play on the music stream.
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(beepListener);

            AssetFileDescriptor file = getResources().openRawResourceFd(R.raw.beep);
            try {
                mediaPlayer.setDataSource(file.getFileDescriptor(),
                        file.getStartOffset(),
                        file.getLength());
                file.close();
                mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
                mediaPlayer.prepare();
            } catch (IOException e) {
                mediaPlayer = null;
            }
        }
    }

    private static final long VIBRATE_DURATION = 200L;

    private void playBeepSoundAndVibrate() {
        if (playBeep && mediaPlayer != null) {
            mediaPlayer.start();
        }
        if (vibrate) {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(VIBRATE_DURATION);
        }
    }

    /**
     * When the beep has finished playing, rewind to queue up another one.
     */
    private final OnCompletionListener beepListener = new OnCompletionListener() {
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.seekTo(0);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case REQUEST_CODE_SELECT_PHOTO:
//                startCropPicture(data.getData());
                Uri mImageCaptureUri = data.getData();
                if (mImageCaptureUri != null) {
                    String filePath = null;
                    if (Build.VERSION.SDK_INT >= 19) {
                        filePath = FileUtils.getAbsPathFromUri(CaptureActivity.this, mImageCaptureUri);
                    } else {
                        filePath = FileUtils.getRealFilePath(CaptureActivity.this, mImageCaptureUri);
                    }
                    if (!TextUtils.isEmpty(filePath)) {
                        Log.d("sy", "filePath:" + filePath);
                        startDecode(filePath);
                    } else {
                        Toast.makeText(CaptureActivity.this, getString(R.string.qr_recognising), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CaptureActivity.this, getString(R.string.qr_recognising), Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_CODE_CROP_PHOTO:
                startDecode(BACK_GROUND_SAVED_URI.getPath());
                break;
            default:
                break;
        }
    }

    private static final String BASE_PATH = Environment.getExternalStorageDirectory() + "/Barcode/";
    private static final String BACK_GROUND_SAVED_FILE_PATH = BASE_PATH + "cropResult.jpg";
    private static final String BACK_GROUND_SAVED_LOCATION = "file://" + BACK_GROUND_SAVED_FILE_PATH;
    private static final Uri BACK_GROUND_SAVED_URI;

    static {
        BACK_GROUND_SAVED_URI = Uri.parse(BACK_GROUND_SAVED_LOCATION);
    }

    public static boolean createFolder(File paramFile) {
        if ((paramFile.exists()) && (paramFile.isFile())) {
            boolean bool = paramFile.delete();
            if (!bool)
                return false;
        }
        paramFile.mkdirs();
        if (!paramFile.exists()) {
            return false;
        }

        return true;
    }

    private void checkFile() {
        createFolder(new File(BASE_PATH));
        File localFile = new File(BACK_GROUND_SAVED_FILE_PATH);
        if (!localFile.exists()) {
            try {
                localFile.createNewFile();
                return;
            } catch (IOException localIOException) {
                finish();
            }
        }
    }

    public void startCropPicture(Uri paramUri) {
        checkFile();
        Intent localIntent = new Intent("com.android.camera.action.CROP");
        localIntent.setDataAndType(paramUri, "image/*");
        localIntent.putExtra("crop", "true");
        localIntent.putExtra("scale", true);
        localIntent.putExtra("return-data", false);
        localIntent.putExtra("output", BACK_GROUND_SAVED_URI);
        localIntent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        localIntent.putExtra("aspectX", 1);
        localIntent.putExtra("aspectY", 1);
        localIntent.putExtra("tips", "随便裁剪一下吧");
        localIntent.putExtra("actionString", "裁剪完了");
        localIntent.putExtra("noFaceDetection", true);
        startActivityForResult(localIntent, REQUEST_CODE_CROP_PHOTO);
    }

    private void startDecode(final String path) {
        Toast.makeText(CaptureActivity.this, getString(R.string.qr_recognising), Toast.LENGTH_SHORT).show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                BitmapFactory.Options localOptions = new BitmapFactory.Options();
                localOptions.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(path, localOptions);
                localOptions.inJustDecodeBounds = false;
                int sampleSize = (int)(localOptions.outHeight / 200.0F);
                if (sampleSize <= 0)
                    sampleSize = 1;
                localOptions.inSampleSize = sampleSize;
                Bitmap img = BitmapFactory.decodeFile(path, localOptions);
                final String result = DecodeHandler.decode(img);
                if (img != null) {
                    img.recycle();
                    img = null;
                }
                if (!TextUtils.isEmpty(result)) {
                    Log.d("sy", "Found barcode:\n" + result);
                }
                Message msg = mDecImgHandler.obtainMessage(MSG_DECODE_QR_FINISH);
                Bundle data = new Bundle();
                data.putString("result", result);
                msg.setData(data);
                mDecImgHandler.sendMessage(msg);
            }
        }).start();
    }
}
