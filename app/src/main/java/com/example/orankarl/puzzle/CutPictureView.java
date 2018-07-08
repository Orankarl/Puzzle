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

public class CutPictureView extends SurfaceView implements SurfaceHolder.Callback, Runnable{
    private Context context;
    private SurfaceHolder holder;
    private Paint paint;
    private Thread thread;
    private Canvas canvas;
    private Resources resources = this.getResources();
    public static int screenW, screenH;

    boolean flag = true;
    MenuButton buttonBack;
    MenuButton buttonConfirm;

    public  CutPictureView(Context context){
        super(context);
        this.context = context;
        holder = this.getHolder();
        holder.addCallback(this);
        paint = new Paint();
        paint.setAntiAlias(true);
        setFocusable(true);
    }
    private void init() {
        Bitmap bmpButtonconfirm = BitmapFactory.decodeResource(resources, R.drawable.button_confirm);
        Bitmap bmpButtonconfirmpressed = BitmapFactory.decodeResource(resources, R.drawable.button_confirm_pressed);
        int posX = bmpButtonconfirm.getWidth() * 5 / 2;
        int posY = MainSurfaceView.screenH - bmpButtonconfirm.getHeight() * 5 / 4;
        buttonConfirm = new MenuButton(context, bmpButtonconfirm, bmpButtonconfirmpressed, posX, posY);

        Bitmap bmpButtonBack = BitmapFactory.decodeResource(resources, R.drawable.button_back);
        Bitmap bmpButtonBackPressed = BitmapFactory.decodeResource(resources, R.drawable.button_back_pressed);
        posX = bmpButtonBack.getWidth() / 4;
        posY = MainSurfaceView.screenH - bmpButtonBack.getHeight() * 5 / 4;
        buttonBack = new MenuButton(context, bmpButtonBack, bmpButtonBackPressed, posX, posY);
    }
    public void draw(){
        try {
            canvas = holder.lockCanvas();
            if (canvas != null) {
                canvas.drawColor(Color.WHITE);
                buttonBack.draw(canvas, paint);
                buttonConfirm.draw(canvas,paint);
                int TEXT_SIZE = (int)Math.round(100 * RATIO);
                Paint textPaint = new Paint();
                textPaint.setColor(Color.BLACK);
                textPaint.setTextSize(TEXT_SIZE);

                String title = "选取正方形截图";
                canvas.drawText(title, screenW / 2 - getTextWidth(textPaint, title) / 2, screenH * 2 / 3 + textPaint.getTextSize() , textPaint);
            }
        } catch (Exception e) {

        } finally {
            if (canvas != null) holder.unlockCanvasAndPost(canvas);
        }
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        buttonBack.onTouchEvent(event, 17);
        buttonConfirm.onTouchEvent(event,24);
        return true;
    }
    @Override
    public void run() {
        while (flag) {
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
