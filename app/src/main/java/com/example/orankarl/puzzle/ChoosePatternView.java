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

public class ChoosePatternView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private Context context;
    private SurfaceHolder holder;
    private Paint paint;
    private Thread thread;
    private Canvas canvas;

    public static int screenW, screenH;
    private Resources resources = this.getResources();
    MenuButton buttonPattern1;
    MenuButton buttonPattern2;
    MenuButton buttonBack;
    boolean flag = true;

    MainActivity activity = (MainActivity)getContext();

    public ChoosePatternView(Context context) {
        super(context);
        this.context = context;
        holder = this.getHolder();
        holder.addCallback(this);
        paint = new Paint();
        paint.setAntiAlias(true);
        setFocusable(true);
    }

    private Bitmap Scale(Bitmap origin_bitmap) {
        int origin_width = origin_bitmap.getWidth();
        int origin_height = origin_bitmap.getHeight();
        float scaleWidth = ((float) MainSurfaceView.screenW / 2) / origin_width;
        float scaleHeight = ((float) MainSurfaceView.screenH / 2) / origin_height;
        Matrix matrix = new Matrix();
        float scale = scaleWidth > scaleHeight ? scaleHeight : scaleWidth;
        matrix.postScale(scale, scale);
        return Bitmap.createBitmap(origin_bitmap, 0, 0, origin_width, origin_height, matrix, true);
    }

    private void init() {
        Bitmap bmpButtonPattern1_origin = BitmapFactory.decodeResource(resources, R.drawable.pattern1);
        Bitmap bmpButtonPattern1Pressed_origin = BitmapFactory.decodeResource(resources, R.drawable.pattern1);
        Bitmap bmpButtonPattern2_origin = BitmapFactory.decodeResource(resources, R.drawable.pattern2);
        Bitmap bmpButtonPattern2Pressed_origin = BitmapFactory.decodeResource(resources, R.drawable.pattern2);

        Bitmap bmpButtonPattern1 = Scale(bmpButtonPattern1_origin);
        Bitmap bmpButtonPattern1Pressed = Scale(bmpButtonPattern1Pressed_origin);
        Bitmap bmpButtonPattern2 = Scale(bmpButtonPattern2_origin);
        Bitmap bmpButtonPattern2Pressed = Scale(bmpButtonPattern2Pressed_origin);

        Bitmap bmpButtonBack = BitmapFactory.decodeResource(resources, R.drawable.button_back);
        Bitmap bmpButtonBackPressed = BitmapFactory.decodeResource(resources, R.drawable.button_back_pressed);
        int posX = MainSurfaceView.screenW / 4 - bmpButtonPattern1.getWidth() / 2;
        int posY = MainSurfaceView.screenH / 3 - bmpButtonPattern1.getHeight() / 2;
        buttonPattern1 = new MenuButton(context, bmpButtonPattern1, bmpButtonPattern1Pressed, posX, posY);
        posX = MainSurfaceView.screenW  * 3 / 4 - bmpButtonPattern2.getWidth() / 2;
        posY = MainSurfaceView.screenH / 3 - bmpButtonPattern2.getHeight() / 2;
        buttonPattern2 = new MenuButton(context, bmpButtonPattern2, bmpButtonPattern2Pressed, posX, posY);
        posX = bmpButtonBack.getWidth() / 4;
        posY = MainSurfaceView.screenH - bmpButtonBack.getHeight() * 5 / 4;
        buttonBack = new MenuButton(context, bmpButtonBack, bmpButtonBackPressed, posX, posY);
    }

    public void draw() {
        try {
            canvas = holder.lockCanvas();
            if (canvas != null) {
                canvas.drawBitmap(activity.background, 0, 0, paint);

                buttonPattern1.draw(canvas, paint);
                buttonPattern2.draw(canvas, paint);
                buttonBack.draw(canvas, paint);

                int TEXT_SIZE = (int)Math.round(100 * activity.RATIO);
                Paint textPaint = new Paint();
                textPaint.setColor(Color.BLACK);
                textPaint.setTextSize(TEXT_SIZE);

                String title = "选择拼图样式";
                canvas.drawText(title, screenW / 2 - activity.getTextWidth(textPaint, title) / 2, screenH * 2 / 3 + textPaint.getTextSize() , textPaint);
            }
        } catch (Exception e) {

        } finally {
            if (canvas != null) holder.unlockCanvasAndPost(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        buttonPattern1.onTouchEvent(event, 12);
        buttonPattern2.onTouchEvent(event, 13);
        buttonBack.onTouchEvent(event, 14);
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
