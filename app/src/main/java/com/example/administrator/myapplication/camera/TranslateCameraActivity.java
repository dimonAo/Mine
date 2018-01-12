package com.example.administrator.myapplication.camera;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.example.administrator.myapplication.R;

import java.io.File;
import java.io.IOException;

public class TranslateCameraActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    private SurfaceView surfaceView;
    private Button camera_start;

    private SurfaceHolder mHolder;
    private Camera mCamera;
    private int mCameraId = 0;

    private boolean isview = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translate_camera);
        surfaceView = findViewById(R.id.surface_view);
        camera_start = findViewById(R.id.camera_start);

        mHolder = surfaceView.getHolder();
        mHolder.addCallback(this);

        camera_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captrue();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (this.mCamera == null) {
            mCamera = getCameraId(mCameraId);
            if (mHolder != null) {
                startPreview(mCamera, mHolder);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }

    private Camera getCameraId(int id) {
        Camera mCamera = null;
        try {
            mCamera = Camera.open(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mCamera;
    }

    /**
     * 预览相机
     */
    private void startPreview(Camera camera, SurfaceHolder holder) {
        try {
            setupCamera(camera);
            camera.setPreviewDisplay(holder);
            //亲测的一个方法 基本覆盖所有手机 将预览矫正
            CameraUtil.getInstance().setCameraDisplayOrientation(this, mCameraId, camera);
//            camera.setDisplayOrientation(90);
            camera.startPreview();
            isview = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置
     */
    private void setupCamera(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();

        if (parameters.getSupportedFocusModes().contains(
                Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }

        //这里第三个参数为最小尺寸 getPropPreviewSize方法会对从最小尺寸开始升序排列 取出所有支持尺寸的最小尺寸
        Camera.Size previewSize = CameraUtil.getInstance().getPropSizeForHeight(parameters.getSupportedPreviewSizes(), 800);
        parameters.setPreviewSize(previewSize.width, previewSize.height);

        Camera.Size pictrueSize = CameraUtil.getInstance().getPropSizeForHeight(parameters.getSupportedPictureSizes(), 800);
        parameters.setPictureSize(pictrueSize.width, pictrueSize.height);

        camera.setParameters(parameters);

        /**
         * 设置surfaceView的尺寸 因为camera默认是横屏，所以取得支持尺寸也都是横屏的尺寸
         * 我们在startPreview方法里面把它矫正了过来，但是这里我们设置设置surfaceView的尺寸的时候要注意
         * previewSize.height<previewSize.width
         * previewSize.width才是surfaceView的高度
         * 一般相机都是屏幕的宽度 这里设置为屏幕宽度 高度自适应 你也可以设置自己想要的大小
         *
         */

//        picHeight = (screenWidth * pictrueSize.width) / pictrueSize.height;
//
//        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(screenWidth, (screenWidth * pictrueSize.width) / pictrueSize.height);
//        //这里当然可以设置拍照位置 比如居中 我这里就置顶了
//        //params.gravity = Gravity.CENTER;
//        surfaceView.setLayoutParams(params);
    }


    /**
     * 释放相机资源
     */
    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    private void captrue() {
        mCamera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                isview = false;
                //将data 转换为位图 或者你也可以直接保存为文件使用 FileOutputStream
                //这里我相信大部分都有其他用处把 比如加个水印 后续再讲解
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                Bitmap saveBitmap = CameraUtil.getInstance().setTakePicktrueOrientation(mCameraId, bitmap);




//                saveBitmap = Bitmap.createScaledBitmap(saveBitmap, screenWidth, picHeight, true);

//                if (index == 1) {
//                    //正方形 animHeight(动画高度)
//                    saveBitmap = Bitmap.createBitmap(saveBitmap, 0, animHeight + SystemUtils.dp2px(context, 44), screenWidth, screenWidth);
//                } else {
//                    //正方形 animHeight(动画高度)
//                    saveBitmap = Bitmap.createBitmap(saveBitmap, 0, 0, screenWidth, screenWidth * 4/3);
//                }

                String img_path = getExternalFilesDir(Environment.DIRECTORY_DCIM).getPath() +
                        File.separator + System.currentTimeMillis() + ".jpeg";

                BitmapUtils.saveJPGE_After(TranslateCameraActivity.this, saveBitmap, img_path, 100);

                if(!bitmap.isRecycled()){
                    bitmap.recycle();
                }

                if(!saveBitmap.isRecycled()){
                    saveBitmap.recycle();
                }

                Intent intent = new Intent();
                intent.putExtra(AppConstant.KEY.IMG_PATH, img_path);
                intent.setClass(TranslateCameraActivity.this,DealPictureActivity.class);
//                intent.putExtra(AppConstant.KEY.PIC_WIDTH, screenWidth);
//                intent.putExtra(AppConstant.KEY.PIC_HEIGHT, picHeight);
//                setResult(AppConstant.RESULT_CODE.RESULT_OK, intent);
                startActivity(intent);
                finish();


                //这里打印宽高 就能看到 CameraUtil.getInstance().getPropPictureSize(parameters.getSupportedPictureSizes(), 200);
                // 这设置的最小宽度影响返回图片的大小 所以这里一般这是1000左右把我觉得
//                Log.d("bitmapWidth==", bitmap.getWidth() + "");
//                Log.d("bitmapHeight==", bitmap.getHeight() + "");
            }
        });
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        startPreview(this.mCamera, holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        this.mCamera.stopPreview();
        startPreview(this.mCamera, holder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseCamera();
    }
}
