package com.example.administrator.myapplication.camera;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.administrator.myapplication.R;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;
import com.yanzhenjie.permission.PermissionListener;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RationaleListener;

import java.util.List;

public class CameraActivity extends AppCompatActivity {
    private static final String TAG = "CameraActivity";
    private Button camera;
    private Button pic;
//    private ImageView imageView;
    private static final int EXTRA_PERMISSION = 100;
    private static final int CAMERA_PERMISSION = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        camera = findViewById(R.id.camera);
        pic = findViewById(R.id.pic);
//        imageView = findViewById(R.id.imageView);

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "click the camera btn");


                //摄像头取得图片
                AndPermission.with(CameraActivity.this)
                        .requestCode(CAMERA_PERMISSION)
                        .permission(Permission.CAMERA)
                        .callback(new PermissionListener() {
                            @Override
                            public void onSucceed(int requestCode, @NonNull List<String> grantPermissions) {
                                if (CAMERA_PERMISSION == requestCode) {
                                    Log.e(TAG, "apply camera permission success");
                                    startActivity(new Intent(CameraActivity.this, TranslateCameraActivity.class));
                                }
                            }

                            @Override
                            public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {
                                if (CAMERA_PERMISSION == requestCode) {
                                    Log.e(TAG, "apply camera permission failed");
                                }
                            }
                        })
                        .rationale(new RationaleListener() {
                            @Override
                            public void showRequestPermissionRationale(int requestCode, Rationale rationale) {
                                AndPermission.rationaleDialog(CameraActivity.this, rationale).show();
                            }
                        })
                        .start();
            }
        });

        pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //相册取得图片
                checkStoragepermission(VALUE_PICK_PICTURE);
            }
        });

    }

    private void checkStoragepermission(final int type) {
        AndPermission.with(this)
                .requestCode(EXTRA_PERMISSION)
                .permission(Permission.STORAGE)
                .callback(new PermissionListener() {
                    @Override
                    public void onSucceed(int requestCode, @NonNull List<String> grantPermissions) {
                        if (EXTRA_PERMISSION == requestCode) {
                            if (type == VALUE_PICK_PICTURE) {
                                selectPicFromLocal();
                            } else {

                            }
                        }
                    }

                    @Override
                    public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {

                    }
                })
                .rationale(new RationaleListener() {
                    @Override
                    public void showRequestPermissionRationale(int requestCode, Rationale rationale) {
                        AndPermission.rationaleDialog(CameraActivity.this, rationale).show();
                    }
                })

                .start();
    }


    private static final int VALUE_PICK_PICTURE = 2;
    private static final int VALUE_PICK_CAMERA = 3;
    private static final int CROP_PIC = 4;

    private void selectPicFromLocal() {
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, VALUE_PICK_PICTURE);
    }


    private String imageFileLocation = "file:///" + Environment.getExternalStorageDirectory().getPath() + "/Mine/images" + System.currentTimeMillis() + ".png";

    /**
     * 裁剪图片
     *
     * @param uri 目标图片uri
     */
    private void zoomPhoto(Uri uri) {
        Uri imageUri = Uri.parse(imageFileLocation);

        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");

        intent.putExtra("crop", "true");
        intent.putExtra("scanle", true);

        intent.putExtra("return-data", false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
        intent.putExtra("noFaceDetection", true);
        startActivityForResult(intent, CROP_PIC);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case VALUE_PICK_PICTURE:
                    //直接从相册获取图片
                    Log.e(TAG, "pic uri : " + data.getData());
                    zoomPhoto(data.getData());
                    break;

                case VALUE_PICK_CAMERA:
                    //从摄像头获取图片

                    break;

                case CROP_PIC:
                    Log.e(TAG, "get crop pic uri : " + data.getData());
//                    try {
//                        imageView.setImageBitmap(BitmapFactory.decodeStream(getContentResolver().openInputStream(data.getData())));
//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                    }
                    break;
            }
        }
    }

    @SuppressWarnings("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

        }
    };


}
