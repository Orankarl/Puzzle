package com.example.orankarl.puzzle;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class LoginView extends SurfaceView implements SurfaceHolder.Callback, Runnable{
    private Context context;
    private SurfaceHolder holder;
    private Paint paint;
    private Thread thread;
    private Canvas canvas;

    public static int screenW, screenH;
    private Resources resources = this.getResources();
    MenuButton buttonLogin;
    MenuButton buttonRegister;
    boolean flag = true;
    public LoginView(Context context) {
        super(context);
        this.context = context;
        holder = this.getHolder();
        holder.addCallback(this);
        paint = new Paint();
        paint.setAntiAlias(true);
        setFocusable(true);
    }

    private void init() {
//        Bitmap loginButton = BitmapFactory.decodeResource(resources, R.drawable.button_login);
        Bitmap loginButton = BitmapFactory.decodeResource(resources, R.drawable.start);
        Bitmap loginButtonPressed = BitmapFactory.decodeResource(resources, R.drawable.start_pressed);
        int posX = LoginView.screenW * 2 / 3 - loginButton.getWidth() / 2;
        int posY = LoginView.screenH * 3 / 4 - loginButton.getHeight() / 2;
//        buttonLogin = new MenuButton(context, loginButton, loginButton, posX, posY);
        buttonLogin = new MenuButton(context, loginButton, loginButtonPressed, posX, posY);

        Bitmap registerButton = BitmapFactory.decodeResource(resources, R.drawable.button_register);
        posX = LoginView.screenW / 3 - loginButton.getWidth() / 2;
        buttonRegister = new MenuButton(context, registerButton, registerButton, posX, posY);
    }

    public void draw() {
        try {
            canvas = holder.lockCanvas();
            if (canvas != null) {
                canvas.drawColor(Color.WHITE);
                buttonLogin.draw(canvas, paint);
                buttonRegister.draw(canvas, paint);
                Paint textPaint = new Paint();
                textPaint.setColor(Color.BLACK);
                textPaint.setTextSize(80);
                String text1 = "Username: ";
                String text2 = "Password: ";
                canvas.drawText(text1, screenW / 2 - getTextWidth(textPaint, text1), screenH / 3 + 100 , textPaint);
                canvas.drawText(text2, screenW / 2 - getTextWidth(textPaint, text2), screenH / 2 + 100 , textPaint);
            }
        } catch (Exception e) {

        } finally {
            if (canvas != null) holder.unlockCanvasAndPost(canvas);
        }
    }

    public static int getTextWidth(Paint paint, String str) {
        int iRet = 0;
        if (str != null && str.length() > 0) {
            int len = str.length();
            float[] widths = new float[len];
            paint.getTextWidths(str, widths);
            for (int j = 0; j < len; j++) {
                iRet += (int) Math.ceil(widths[j]);
            }
        }
        return iRet;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        buttonLogin.onTouchEvent(event, 3);
        buttonRegister.onTouchEvent(event, 4);
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
