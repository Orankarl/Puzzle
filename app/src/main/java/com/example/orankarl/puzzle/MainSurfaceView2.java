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

public class MainSurfaceView2 extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private Context context;
    private SurfaceHolder holder;
    private Paint paint;
    private Thread thread;
    private Canvas canvas;

    public static int screenW, screenH;
    private Resources resources = this.getResources();
    MenuButton buttonSingle;
    MenuButton buttonMulti;
    MenuButton buttonRank;
    MenuButton buttonLogout;
    boolean flag = true;
    public MainSurfaceView2(Context context) {
        super(context);
        this.context = context;
        holder = this.getHolder();
        holder.addCallback(this);
        paint = new Paint();
        paint.setAntiAlias(true);
        setFocusable(true);
    }

    private void init() {
        Bitmap bmpButtonSingle = BitmapFactory.decodeResource(resources, R.drawable.button_start);
        Bitmap bmpButtonSinglePressed = BitmapFactory.decodeResource(resources, R.drawable.button_start_pressed);
        Bitmap bmpButtonMulti = BitmapFactory.decodeResource(resources, R.drawable.button_start2);
        Bitmap bmpButtonMultiPressed = BitmapFactory.decodeResource(resources, R.drawable.button_start2_pressed);
        Bitmap bmpButtonRank = BitmapFactory.decodeResource(resources, R.drawable.button_start);
        Bitmap bmpButtonRankPressed = BitmapFactory.decodeResource(resources, R.drawable.button_start_pressed);
        Bitmap bmpButtonLogout = BitmapFactory.decodeResource(resources, R.drawable.button_start);
        Bitmap bmpButtonLogoutPressed = BitmapFactory.decodeResource(resources, R.drawable.button_start_pressed);
        int posX = MainSurfaceView.screenW / 2 - bmpButtonSingle.getWidth() / 2;
        int posY = MainSurfaceView.screenH / 5 - bmpButtonSingle.getHeight() / 2;
        buttonSingle = new MenuButton(context, bmpButtonSingle, bmpButtonSinglePressed, posX, posY);
        posX = MainSurfaceView.screenW / 2 - bmpButtonMulti.getWidth() / 2;
        posY = MainSurfaceView.screenH * 2 / 5 - bmpButtonSingle.getHeight() / 2;
        buttonMulti = new MenuButton(context, bmpButtonMulti, bmpButtonMultiPressed, posX, posY);
        posX = MainSurfaceView.screenW / 2 - bmpButtonMulti.getWidth() / 2;
        posY = MainSurfaceView.screenH * 3 / 5 - bmpButtonSingle.getHeight() / 2;
        buttonRank = new MenuButton(context, bmpButtonRank, bmpButtonRankPressed, posX, posY);
        posX = MainSurfaceView.screenW / 2 - bmpButtonMulti.getWidth() / 2;
        posY = MainSurfaceView.screenH * 4 / 5 - bmpButtonSingle.getHeight() / 2;
        buttonLogout = new MenuButton(context, bmpButtonLogout, bmpButtonLogoutPressed, posX, posY);
    }

    public void draw() {
        try {
            canvas = holder.lockCanvas();
            if (canvas != null) {
                canvas.drawColor(Color.WHITE);

                buttonSingle.draw(canvas, paint);
                buttonMulti.draw(canvas, paint);
                buttonRank.draw(canvas, paint);
                buttonLogout.draw(canvas, paint);
            }
        } catch (Exception e) {

        } finally {
            if (canvas != null) holder.unlockCanvasAndPost(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        buttonSingle.onTouchEvent(event, 0);
        buttonMulti.onTouchEvent(event, 5);
        buttonRank.onTouchEvent(event, 2);
        buttonLogout.onTouchEvent(event, 6);
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
