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

public class ChooseSplitView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private Context context;
    private SurfaceHolder holder;
    private Paint paint;
    private Thread thread;
    private Canvas canvas;

    public static int screenW, screenH;
    private Resources resources = this.getResources();
    MenuButton buttonSplit1;
    MenuButton buttonSplit2;
    MenuButton buttonBack;
    boolean flag = true;

    MainActivity activity = (MainActivity)getContext();

    public ChooseSplitView(Context context) {
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
        Bitmap bmpButtonSplit1_origin = BitmapFactory.decodeResource(resources, R.drawable.split1);
        Bitmap bmpButtonSplit1Pressed_origin = BitmapFactory.decodeResource(resources, R.drawable.split1);
        Bitmap bmpButtonSplit2_origin = BitmapFactory.decodeResource(resources, R.drawable.split2);
        Bitmap bmpButtonSplit2Pressed_origin = BitmapFactory.decodeResource(resources, R.drawable.split2);

        Bitmap bmpButtonSplit1 = Scale(bmpButtonSplit1_origin);
        Bitmap bmpButtonSplit1Pressed = Scale(bmpButtonSplit1Pressed_origin);
        Bitmap bmpButtonSplit2 = Scale(bmpButtonSplit2_origin);
        Bitmap bmpButtonSplit2Pressed = Scale(bmpButtonSplit2Pressed_origin);

        Bitmap bmpButtonBack = BitmapFactory.decodeResource(resources, R.drawable.button_back);
        Bitmap bmpButtonBackPressed = BitmapFactory.decodeResource(resources, R.drawable.button_back_pressed);
        int posX = MainSurfaceView.screenW / 4 - bmpButtonSplit1.getWidth() / 2;
        int posY = MainSurfaceView.screenH / 3 - bmpButtonSplit1.getHeight() / 2;
        buttonSplit1 = new MenuButton(context, bmpButtonSplit1, bmpButtonSplit1Pressed, posX, posY);
        posX = MainSurfaceView.screenW  * 3 / 4 - bmpButtonSplit2.getWidth() / 2;
        posY = MainSurfaceView.screenH / 3 - bmpButtonSplit2.getHeight() / 2;
        buttonSplit2 = new MenuButton(context, bmpButtonSplit2, bmpButtonSplit2Pressed, posX, posY);
        posX = bmpButtonBack.getWidth() / 4;
        posY = MainSurfaceView.screenH - bmpButtonBack.getHeight() * 5 / 4;
        buttonBack = new MenuButton(context, bmpButtonBack, bmpButtonBackPressed, posX, posY);
    }

    public void draw() {
        try {
            canvas = holder.lockCanvas();
            if (canvas != null) {
                canvas.drawBitmap(activity.background, 0, 0, paint);

                buttonSplit1.draw(canvas, paint);
                buttonSplit2.draw(canvas, paint);
                buttonBack.draw(canvas, paint);

                int TEXT_SIZE = (int)Math.round(100 * activity.RATIO);
                Paint textPaint = new Paint();
                textPaint.setTypeface(activity.font);
                textPaint.setColor(Color.BLACK);
                textPaint.setTextSize(TEXT_SIZE);

                String title = "Choose split size";
                canvas.drawText(title, screenW / 2 - activity.getTextWidth(textPaint, title) / 2, screenH * 2 / 3 + textPaint.getTextSize() , textPaint);
            }
        } catch (Exception e) {

        } finally {
            if (canvas != null) holder.unlockCanvasAndPost(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        buttonSplit1.onTouchEvent(event, 15);
        buttonSplit2.onTouchEvent(event, 16);
        buttonBack.onTouchEvent(event, 17);
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
