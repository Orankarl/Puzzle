package com.example.orankarl.puzzle;

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

import static com.example.orankarl.puzzle.MainActivity.RATIO;
import static com.example.orankarl.puzzle.MainActivity.getTextWidth;

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
        Bitmap loginButton = BitmapFactory.decodeResource(resources, R.drawable.button_mainlogin);
        Bitmap loginButtonPressed = BitmapFactory.decodeResource(resources, R.drawable.button_mainlogin_pressed);
        int posX = LoginView.screenW * 2 / 3 - loginButton.getWidth() / 2;
        int posY = LoginView.screenH * 3 / 4 - loginButton.getHeight() / 2;
        buttonLogin = new MenuButton(context, loginButton, loginButtonPressed, posX, posY);

        Bitmap registerButton = BitmapFactory.decodeResource(resources, R.drawable.button_register);
        Bitmap registerButtonPressed = BitmapFactory.decodeResource(resources, R.drawable.button_register_pressed);
        posX = LoginView.screenW / 3 - loginButton.getWidth() / 2;
        buttonRegister = new MenuButton(context, registerButton, registerButtonPressed, posX, posY);
    }

    public void draw() {
        try {
            canvas = holder.lockCanvas();
            if (canvas != null) {
                canvas.drawColor(Color.WHITE);

                int TEXT_SIZE = (int)Math.round(120 * RATIO);
                Paint textPaint = new Paint();
                textPaint.setColor(Color.BLACK);
                textPaint.setTextSize(TEXT_SIZE);

                String title = "登录";
                canvas.drawText(title, screenW / 2 - getTextWidth(textPaint, title) / 2, screenH / 10 + textPaint.getTextSize() , textPaint);

                TEXT_SIZE = (int)Math.round(80 * RATIO);
                textPaint.setTextSize(TEXT_SIZE);

                String text1 = "Username: ";
                String text2 = "Password: ";
                canvas.drawText(text1, 0, 9, screenW / 2 - getTextWidth(textPaint, text1), screenH / 3 + textPaint.getTextSize() , textPaint);
                canvas.drawText(text2, 0, 9, screenW / 2 - getTextWidth(textPaint, text2), screenH / 2 + textPaint.getTextSize() , textPaint);

                buttonLogin.draw(canvas, paint);
                buttonRegister.draw(canvas, paint);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (canvas != null) holder.unlockCanvasAndPost(canvas);
        }
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
