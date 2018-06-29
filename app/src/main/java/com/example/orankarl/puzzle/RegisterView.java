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

public class RegisterView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private Context context;
    private SurfaceHolder holder;
    private Paint paint;
    private Thread thread;
    private Canvas canvas;

    public int TextSize;

    public static int screenW, screenH;
    private Resources resources = this.getResources();
    MenuButton buttonLogin;
    MenuButton buttonRegister;
    boolean flag = true;
    public RegisterView(Context context) {
        super(context);
        this.context = context;
        holder = this.getHolder();
        holder.addCallback(this);
        paint = new Paint();
        paint.setAntiAlias(true);
        setFocusable(true);
    }

    private void init() {
        Bitmap registerButton = BitmapFactory.decodeResource(resources, R.drawable.button_register);
        Bitmap registerButtonPressed = BitmapFactory.decodeResource(resources, R.drawable.button_register_pressed);

        int posX = RegisterView.screenW / 2 - registerButton.getWidth() / 2;
        int posY = RegisterView.screenH * 3 / 4 - registerButton.getHeight() / 2;
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

                String title = "注册";
                canvas.drawText(title, screenW / 2 - getTextWidth(textPaint, title) / 2, screenH / 10 + textPaint.getTextSize() , textPaint);

                //TEXT_SIZE = (int)Math.round(80 * RATIO);
                //textPaint.setTextSize(TEXT_SIZE);
                textPaint.setTextSize(TextSize);

                String text1 = "Username: ";
                String text2 = "Password: ";
                String text3 = "Nickname: ";
                canvas.drawText(text1, screenW / 2 - getTextWidth(textPaint, text1), screenH  * 2 / 7 + textPaint.getTextSize() , textPaint);
                canvas.drawText(text2, screenW / 2 - getTextWidth(textPaint, text2), screenH * 3 / 7 + textPaint.getTextSize() , textPaint);
                canvas.drawText(text3, screenW / 2 - getTextWidth(textPaint, text3), screenH * 4 / 7 + textPaint.getTextSize() , textPaint);

                buttonRegister.draw(canvas, paint);
            }
        } catch (Exception e) {

        } finally {
            if (canvas != null) holder.unlockCanvasAndPost(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        buttonRegister.onTouchEvent(event, 23);
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
