package com.example.orankarl.puzzle;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.os.Handler;
import android.view.Window;

public class MainSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private Context context;
    private SurfaceHolder holder;
    private Paint paint;
    private Thread thread;
    private Canvas canvas;

    public static int screenW, screenH;
    private Resources resources = this.getResources();
    MenuButton buttonStart;
    MenuButton buttonChangeAccount;
    boolean flag = true;
    public MainSurfaceView(Context context) {
        super(context);
        this.context = context;
        holder = this.getHolder();
        holder.addCallback(this);
        paint = new Paint();
        paint.setAntiAlias(true);
        setFocusable(true);
    }

    private void init() {
//        Bitmap bmpButton = BitmapFactory.decodeResource(resources, R.drawable.button_start);
//        Bitmap bmpButtonPressed = BitmapFactory.decodeResource(resources, R.drawable.button_restart);
        Bitmap bmpButton = BitmapFactory.decodeResource(resources, R.drawable.start);
        Bitmap bmpButtonPressed = BitmapFactory.decodeResource(resources, R.drawable.start_pressed);
        Bitmap bmpButtonChangeAccount = BitmapFactory.decodeResource(resources, R.drawable.button_change_account);
        int posX = MainSurfaceView.screenW / 2 - bmpButton.getWidth() / 2;
        int posY = MainSurfaceView.screenH / 3 - bmpButton.getHeight() / 2;
        buttonStart = new MenuButton(context, bmpButton, bmpButtonPressed, posX, posY);
        posX = MainSurfaceView.screenW / 2 - bmpButtonChangeAccount.getWidth() / 2;
        posY = MainSurfaceView.screenH * 2 / 3 - bmpButtonChangeAccount.getHeight() / 2;
        buttonChangeAccount = new MenuButton(context, bmpButtonChangeAccount, bmpButtonChangeAccount, posX, posY);
    }

    public void draw() {
        try {
            canvas = holder.lockCanvas();
            if (canvas != null) {
                canvas.drawColor(Color.WHITE);

                buttonStart.draw(canvas, paint);
                buttonChangeAccount.draw(canvas, paint);
            }
        } catch (Exception e) {

        } finally {
            if (canvas != null) holder.unlockCanvasAndPost(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        buttonStart.onTouchEvent(event, 0);
        buttonChangeAccount.onTouchEvent(event, 5);
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
