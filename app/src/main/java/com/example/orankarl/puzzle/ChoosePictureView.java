package com.example.orankarl.puzzle;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class ChoosePictureView extends SurfaceView implements SurfaceHolder.Callback, Runnable{
    private Context context;
    private SurfaceHolder holder;
    private Paint paint;
    private Thread thread;
    private Canvas canvas;

    public Bitmap origin_bitmap = null;
    public Bitmap bitmap = null;

    public static int screenW, screenH;
    private Resources resources = this.getResources();
    MenuButton buttonStart;
    MenuButton buttonStart2;
    boolean flag = true;
    public ChoosePictureView(Context context) {
        super(context);
        this.context = context;
        holder = this.getHolder();
        holder.addCallback(this);
        paint = new Paint();
        paint.setAntiAlias(true);
        setFocusable(true);
    }

    private void init() {
        origin_bitmap = MainActivity.puzzleBitmap;
        Bitmap bmpButton = BitmapFactory.decodeResource(resources, R.drawable.button_choose);
        Bitmap bmpButtonPressed = BitmapFactory.decodeResource(resources, R.drawable.button_choose_pressed);
        Bitmap bmpButton2 = BitmapFactory.decodeResource(resources, R.drawable.button_confirm);
        Bitmap bmpButton2Pressed = BitmapFactory.decodeResource(resources, R.drawable.button_confirm_pressed);
        int posX = MainSurfaceView.screenW / 2 - bmpButton.getWidth() / 2;
        int posY = MainSurfaceView.screenH * 3 / 4 - bmpButton.getHeight()/2;
        buttonStart = new MenuButton(context, bmpButton, bmpButtonPressed, posX, posY);
        buttonStart2 = new MenuButton(context, bmpButton2, bmpButton2Pressed, posX, posY + bmpButton.getHeight() * 3 / 2);

        if (origin_bitmap != null) {
            int origin_width = origin_bitmap.getWidth();
            int origin_height = origin_bitmap.getHeight();
            float scaleWidth = ((float) MainSurfaceView.screenW * 3 / 4) / origin_width;
            float scaleHeight = ((float) MainSurfaceView.screenH / 2) / origin_height;
            Matrix matrix = new Matrix();
            float scale = scaleWidth > scaleHeight ? scaleHeight : scaleWidth;
            matrix.postScale(scale, scale);
            bitmap = Bitmap.createBitmap(origin_bitmap, 0, 0, origin_width, origin_height, matrix, true);
        }
    }

    public void draw() {
        try {
            canvas = holder.lockCanvas();
            if (canvas != null) {
                canvas.drawColor(Color.WHITE);
                if (bitmap != null) {
                    canvas.drawBitmap(bitmap, MainSurfaceView.screenW / 2 - bitmap.getWidth() / 2, MainSurfaceView.screenH / 3 - bitmap.getHeight() / 2, paint);
                    buttonStart2.draw(canvas, paint);
                }
                buttonStart.draw(canvas, paint);
            }
        } catch (Exception e) {

        } finally {
            if (canvas != null) holder.unlockCanvasAndPost(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        buttonStart.onTouchEvent(event, 7);
        buttonStart2.onTouchEvent(event, 8);
        return true;
    }

    @Override
    public void run() {
        while(flag) {
            long start = System.currentTimeMillis();
            draw();
            long end = System.currentTimeMillis();
            try {
                if (end - start < 50) {
                    Thread.sleep(50 - (end - start));
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        screenW = this.getWidth();
        screenH = this.getHeight();
        init();
        flag = true;
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }
}
