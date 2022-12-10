package com.example.photoshop;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    Button ibZoomin, ibZoomout, ibRotate, ibBright, ibDark, ibGray, colorButton, imageViewButton, saveButton, prevButton, nextButton;
    LinearLayout pictureLayout;
    MyGraphicView graphicView;
    File[] imageFiles;
    String imageFname;
    Bitmap picture;
    Paint paint;
    int curNum = 1;
    float scaleX = 1, scaleY = 1, angle = 0, color = 1, satur = 1;
    int startX = -1, startY = -1, stopX = -1, stopY = -1;
    final static int LINE = 1, CIRCLE = 2, Rect = 3;
    final static int RED = 1, BLUE = 2, BLACK = 3;
    static int curShape = LINE, penColor = BLACK;
    boolean flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Mini Photoshop");

        ibZoomin = (Button) findViewById(R.id.ibZoomin);
        ibZoomout = (Button) findViewById(R.id.ibZoomout);
        ibRotate = (Button) findViewById(R.id.ibRotate);
        ibBright = (Button) findViewById(R.id.ibBright);
        ibDark = (Button) findViewById(R.id.ibDark);
        ibGray = (Button) findViewById(R.id.ibGray);
        imageViewButton = (Button) findViewById(R.id.imageViewButton);
        saveButton = (Button) findViewById(R.id.saveButton);
        prevButton = (Button) findViewById(R.id.prevButton);
        nextButton = (Button) findViewById(R.id.nextButton);
        colorButton = (Button) findViewById(R.id.colorButton);
        pictureLayout = (LinearLayout) findViewById(R.id.pictureLayout);

        graphicView = (MyGraphicView) new MyGraphicView(this);
        pictureLayout.addView(graphicView);

        imageFiles = new File(Environment.getExternalStorageDirectory().
                getAbsolutePath()+"/Pictures").listFiles();
        imageFname = imageFiles[curNum].toString();
        graphicView.imagePath = imageFname;

        ibZoomin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scaleX = scaleX + 0.2f;
                scaleY = scaleY + 0.2f;
                graphicView.invalidate();
            }
        });
        ibZoomout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scaleX = scaleX - 0.2f;
                scaleY = scaleY - 0.2f;
                graphicView.invalidate();
            }
        });
        ibRotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                angle = angle + 20;
                graphicView.invalidate();
            }
        });
        ibBright.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                color = color + 0.2f;
                graphicView.invalidate();
            }
        });
        ibDark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                color = color - 0.2f;
                graphicView.invalidate();
            }
        });
        ibGray.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (satur == 0)
                    satur = 1;
                else
                    satur = 0;
                graphicView.invalidate();
            }
        });
        imageViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(getApplicationContext(), ImageViewerActivity.class);
                startActivity(intent1);
                finish();
            }
        });
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(curNum <= 1){
                    Toast.makeText(getApplicationContext(), "First Picture", Toast.LENGTH_SHORT).show();
                }
                else {
                    curNum--;
                    flag = true;
                    imageFname = imageFiles[curNum].toString();
                    graphicView.imagePath = imageFname;
                    graphicView.invalidate();
                }
            }
        });
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(curNum >= imageFiles.length-1){
                    Toast.makeText(getApplicationContext(), "Last Picture", Toast.LENGTH_SHORT).show();
                }
                else {
                    curNum++;
                    flag = true;
                    imageFname = imageFiles[curNum].toString();
                    graphicView.imagePath = imageFname;
                    graphicView.invalidate();
                }
            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = 7;
                graphicView.setDrawingCacheEnabled(true);
                Bitmap bitmap = Bitmap.createBitmap(graphicView.getDrawingCache());
                graphicView.setDrawingCacheEnabled(true);
                File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                if(!dir.exists()){
                    dir.mkdirs();
                }
                try {
                    dir.createNewFile();
                    FileOutputStream fos = new FileOutputStream(new File(dir, "picture"+i+".png"));
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    Toast.makeText(getApplicationContext(), "save", Toast.LENGTH_SHORT).show();
                    i++;
                    fos.close();
                } catch (Exception e){
                    Toast.makeText(getApplicationContext(), "save fail", Toast.LENGTH_SHORT).show();
                    Log.e("photo", "save fail", e);
                }
                imageFiles = new File(Environment.getExternalStorageDirectory().
                        getAbsolutePath()+"/Pictures").listFiles();
                imageFname = imageFiles[curNum].toString();
                graphicView.imagePath = imageFname;
            }
        });

        registerForContextMenu(colorButton);
    }

    private class MyGraphicView extends View {
        String imagePath = null;
        public MyGraphicView(Context context) {
            super(context);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startX = (int) event.getX();
                    startY = (int) event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                case MotionEvent.ACTION_UP:
                    stopX = (int) event.getX();
                    stopY = (int) event.getY();
                    this.invalidate();
                    break;
            }
            return true;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            picture = BitmapFactory.decodeFile(imagePath);
            int picX = (this.getWidth() - picture.getWidth()) / 2;
            int picY = (this.getHeight() - picture.getHeight()) / 2;

            int cenX = this.getWidth() / 2;
            int cenY = this.getHeight() / 2;
            canvas.scale(scaleX, scaleY, cenX, cenY);

            canvas.rotate(angle, cenX, cenY);

            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStrokeWidth(5);
            paint.setStyle(Paint.Style.STROKE);

            switch (penColor) {
                case RED:
                    paint.setColor(Color.RED);
                    break;
                case BLUE:
                    paint.setColor(Color.BLUE);
                    break;
                case BLACK:
                    paint.setColor(Color.BLACK);
                    break;
            }

            float[] array = {
                    color, 0, 0, 0, 0,
                    0, color, 0, 0, 0,
                    0, 0, color, 0, 0,
                    0, 0, 0, 1, 0};
            ColorMatrix cm = new ColorMatrix(array);
            if (satur == 0) cm.setSaturation(satur);
            paint.setColorFilter(new ColorMatrixColorFilter(cm));
            canvas.drawBitmap(picture, picX, picY, paint);

            picture.recycle();
            if(flag){
                paint.reset();
                flag = false;
            }
            else {
                switch (curShape) {
                    case LINE:
                        canvas.drawLine(startX, startY, stopX, stopY, paint);
                        break;
                    case CIRCLE:
                        int radius = (int) Math.sqrt(Math.pow(stopX - startX, 2)
                                + Math.pow(stopY - startY, 2));
                        canvas.drawCircle(startX, startY, radius, paint);
                        break;
                    case Rect:
                        canvas.drawRect(startX, startY, stopX, stopY, paint);
                        break;
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, 1, 0, "Line");
        menu.add(0, 2, 0, "Circle");
        menu.add(0, 3, 0, "Rectangle");
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(1, 1, 0, "Red");
        menu.add(1, 2, 0, "Blue");
        menu.add(1, 3, 0, "Black");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                curShape = LINE;
                return true;
            case 2:
                curShape = CIRCLE;
                return true;
            case 3:
                curShape = Rect;
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                penColor = RED;
                return true;
            case 2:
                penColor = BLUE;
                return true;
            case 3:
                penColor = BLACK;
                return true;
        }
        return super.onContextItemSelected(item);
    }
}