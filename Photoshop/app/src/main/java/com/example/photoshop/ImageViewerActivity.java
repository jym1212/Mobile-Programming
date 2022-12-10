package com.example.photoshop;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.EventLog;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import java.io.File;

public class ImageViewerActivity extends Activity {
    Button btnPrev, btnNext, photoshopBtn, ratingBtn, checkBtn;
    TextView ratingTextView;
    RatingBar ratingBar;
    Dialog dialog;
    LinearLayout imageLayout;
    MyPictureView myPictureView;
    GridView gridView;
    int curNum = 1;
    float[] ratingScore = new float[50];
    File[] imageFiles;
    String imageFname;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.imageviewer);
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, MODE_PRIVATE);

        btnPrev = (Button) findViewById(R.id.btnPrev);
        btnNext = (Button) findViewById(R.id.btnNext);
        ratingBtn = (Button) findViewById(R.id.ratingBtn);
        photoshopBtn = (Button) findViewById(R.id.photoshopBtn);
        ratingTextView = (TextView) findViewById(R.id.ratingTextView);

        imageLayout = (LinearLayout) findViewById(R.id.imageLayout);
        myPictureView = (MyPictureView) new MyPictureView(this);
        imageLayout.addView(myPictureView);

        imageFiles = new File(Environment.getExternalStorageDirectory().
                getAbsolutePath()+"/Pictures").listFiles();
        imageFname = imageFiles[curNum].toString();
        myPictureView.imagePath = imageFname;

        gridView = (GridView) findViewById(R.id.gridView);
        MyGridAdapter gridAdapter = new MyGridAdapter(this);
        gridView.setAdapter(gridAdapter);

        for(int i = 0; i < 50; i++){
            ratingScore[i] = 0;
        }

        ratingTextView.setText("Rating : " + ratingScore[1]);

        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(curNum <= 1){
                    Toast.makeText(getApplicationContext(), "First Picture", Toast.LENGTH_SHORT).show();
                }
                else {
                    curNum--;
                    ratingTextView.setText("Rating : " + ratingScore[curNum]);
                    imageFname = imageFiles[curNum].toString();
                    myPictureView.imagePath = imageFname;
                    myPictureView.invalidate();
                }
            }
        });
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(curNum >= imageFiles.length-1){
                    Toast.makeText(getApplicationContext(), "Last Picture", Toast.LENGTH_SHORT).show();
                }
                else {
                    curNum++;
                    ratingTextView.setText("Rating : " + ratingScore[curNum]);
                    imageFname = imageFiles[curNum].toString();
                    myPictureView.imagePath = imageFname;
                    myPictureView.invalidate();
                }
            }
        });
        ratingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = new Dialog(ImageViewerActivity.this);
                dialog.setContentView(R.layout.dialog);
                ratingBar = (RatingBar) dialog.findViewById(R.id.ratingBar);
                checkBtn = (Button) dialog.findViewById(R.id.checkBtn);
                checkBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                ratingBar.setOnRatingBarChangeListener(new rating());
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
            }
        });
        photoshopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent2);
                finish();
            }
        });
    }

    public class MyGridAdapter extends BaseAdapter {
        Context mContext;
        ImageView imageView;
        File root = new File(Environment.getExternalStorageDirectory().toString()+"/Pictures");
        private File[] fileName = root.listFiles();

        public MyGridAdapter(Context c) {
            mContext = c;
        }
        @Override
        public int getCount() {
            return fileName.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            Uri uri = Uri.fromFile(fileName[position]);
            if (convertView == null) {
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(300, 400));
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageView.setPadding(5, 5, 5, 5);

                int pos = position;
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        View dialogView = (View) View.inflate(ImageViewerActivity.this,
                                R.layout.picture_dialog, null);
                        AlertDialog.Builder dlg = new AlertDialog.Builder(ImageViewerActivity.this);
                        ImageView ivPoster = (ImageView) dialogView.findViewById(R.id.ivPoster);
                        ivPoster.setImageBitmap(BitmapFactory.decodeFile(fileName[pos].toString()));
                        dlg.setTitle("Picture");
                        dlg.setIcon(R.drawable.ic_baseline_add_to_photos_24);
                        dlg.setView(dialogView);
                        dlg.setNegativeButton("닫기", null);
                        dlg.show();
                    }
                });

            } else {
                imageView = (ImageView) convertView;
            }

            imageView.setImageURI(uri);
            return imageView;
        }
    }

    public class MyPictureView extends View {
        String imagePath = null;
        public MyPictureView(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas){
            super.onDraw(canvas);
            if(imagePath != null){
                Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                int picX = (this.getWidth() - bitmap.getWidth()) / 2;
                int picY = (this.getHeight() - bitmap.getHeight()) / 2;
                canvas.drawBitmap(bitmap, picX, picY, null);
                bitmap.recycle();
            }
        }
    }
    public class rating implements RatingBar.OnRatingBarChangeListener{
        public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
            ratingTextView = (TextView) findViewById(R.id.ratingTextView);
            ratingScore[curNum] = rating;
            ratingTextView.setText("Rating : " + ratingScore[curNum]);
        }
    }
}
