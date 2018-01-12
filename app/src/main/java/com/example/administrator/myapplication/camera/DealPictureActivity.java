package com.example.administrator.myapplication.camera;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.administrator.myapplication.MainActivity;
import com.example.administrator.myapplication.R;

import java.io.File;

public class DealPictureActivity extends AppCompatActivity {

    private static final String TAG = "DealPictureActivity";

    //    private ImageView cut_img;
    private ClipImageView cut_img;
    private Button area_tran;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal_picture);
//        TextView textView = findViewById(R.id.pic_path);
//        textView.setText(getStringPicPath());
        area_tran = findViewById(R.id.area_tran);
        cut_img = findViewById(R.id.cut_img);
        try {
            File file = new File(getStringPicPath());
            cut_img.setImageBitmap(BitmapUtils.getBitemapFromFile(file));
//            cut_img.setBitmap(BitmapUtils.getBitemapFromFile(file));
        } catch (Exception e) {
            e.printStackTrace();
        }

        area_tran.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bitmap = cut_img.getClippedBitmap();
                String img_path = getExternalFilesDir(Environment.DIRECTORY_DCIM).getPath() +
                        File.separator + System.currentTimeMillis() + ".jpeg";
                BitmapUtils.saveJPGE_After(DealPictureActivity.this, bitmap, img_path, 100);

                if (!bitmap.isRecycled()) {
                    bitmap.recycle();
                }

                Intent intent = new Intent(DealPictureActivity.this, MainActivity.class);
                intent.putExtra(AppConstant.KEY.IMG_PATH, img_path);
                startActivity(intent);
                finish();

            }
        });

    }

    private String getStringPicPath() {
        if (getIntent() == null) {
            Log.e(TAG, "get intent is null");
            return null;
        }
        String tempPath = getIntent().getStringExtra(AppConstant.KEY.IMG_PATH);
        Log.e(TAG, "tempPath : " + tempPath);
        return tempPath;
    }

}
