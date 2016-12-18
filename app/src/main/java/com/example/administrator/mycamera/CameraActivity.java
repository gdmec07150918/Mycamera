package com.example.administrator.mycamera;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class CameraActivity extends AppCompatActivity implements View.OnClickListener,
        SurfaceHolder.Callback{
    private SurfaceView mSurfaceView;   //相机视频浏览
    private ImageView mIamgeView;   //相片
    private SurfaceHolder mSurfaceHolder;
    private ImageView shutter;      //快照按钮
    private Camera mCamera = null;  //相机
    private boolean mPreviewRunning;    //运行相机浏览
    private static final int MENU_STARAT = 1;
    private static final int MENU_SENSOR = 2;
    private Bitmap bitmap;  //相片Bitmap
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //隐藏标题，设置全屏，
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camera);
        mSurfaceView = (SurfaceView) findViewById(R.id.camera);
        mIamgeView = (ImageView) findViewById(R.id.inmage);
        shutter = (ImageView) findViewById(R.id.shutter);

        shutter.setOnClickListener(this);
        mIamgeView.setVisibility(View.GONE);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        setCameraParams();
    }
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        try {
            if (mPreviewRunning){
                mCamera.stopPreview();
            }
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
            mPreviewRunning = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null){
            mCamera.stopPreview();
            mPreviewRunning =false;
            //回收相机
            mCamera.release();
            mCamera = null;
        }
    }
    @Override
    public void onClick(View v) {
        if (mPreviewRunning){
            shutter.setEnabled(false);
            //
            mCamera.autoFocus(new Camera.AutoFocusCallback() {

                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    mCamera.takePicture(mShutterCallbak,
                            null,mPictureCallback);
                }});
        }
    }
    Camera.PictureCallback mPictureCallback = new Camera.PictureCallback(){
        public void onPictureTaken(byte[] data, Camera camera) {
            if(data != null){
                saveAndShow(data);
            }
        }
    };
    Camera.ShutterCallback mShutterCallbak = new Camera.ShutterCallback(){
        public void onShutter() {
            System.out.println("快照回调函数...");
        }
    };
    public void setCameraParams() {
        if(mCamera != null){
            return;
        }
        mCamera = Camera.open();
        //设置参数
        Camera.Parameters params = mCamera.getParameters();
        //设置自动对焦
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        //设置预览帧速度
        params.setPreviewFrameRate(3);
        params.setPreviewFormat(PixelFormat.YCbCr_422_SP);
        params.set("jpeg-quality",85);
        List<Camera.Size> list = params.getSupportedPictureSizes();
        Camera.Size size = list.get(0);
        int w = size.width;
        int h = size.height;
        params.setPictureSize(w,h);

        params.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0,MENU_STARAT,0,"重拍");
        menu.add(0,MENU_SENSOR,0,"打开相册");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == MENU_STARAT){
            //
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            return true;
        }else if(item.getItemId() == MENU_STARAT){
            Intent intent = new Intent(this,AlbumActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    public void saveAndShow(byte[] data) {
        try {
            String imsgeId = System.currentTimeMillis() + "";
            String pathName = android.os.Environment.
                    getExternalStorageDirectory().getPath() + "/mycanera";
            File file = new File(pathName);
            if (!file.exists()) {
                file.mkdirs();
            }
            //
            pathName += "/" + imsgeId + ".jpeg";
            file = new File(pathName);
            if (!file.exists()) {
                file.createNewFile();//文件不存在时新建
            }
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data);
            fos.close();
            AlbumActivity album = new AlbumActivity();
            bitmap = album.loadImage(pathName);
            mIamgeView.setImageBitmap(bitmap);
            mIamgeView.setVisibility(View.GONE);
            if (mPreviewRunning){
                mCamera.stopPreview();
                mPreviewRunning = false;
            }
            shutter.setEnabled(true);

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
