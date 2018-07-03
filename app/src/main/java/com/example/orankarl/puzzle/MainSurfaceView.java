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

public class MainSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private Context context;
    private SurfaceHolder holder;
    private Paint paint;
    private Thread thread;
    private Canvas canvas;

    public static int screenW, screenH;
    private Resources resources = this.getResources();
    MenuButton buttonSingle;
    MenuButton buttonLog;
    MenuButton buttonRank;
    private Bitmap background;
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
        background = BitmapFactory.decodeResource(resources, R.drawable.background);
        Bitmap bmpButtonSingle = BitmapFactory.decodeResource(resources, R.drawable.button_singlestart);
        Bitmap bmpButtonSinglePressed = BitmapFactory.decodeResource(resources, R.drawable.button_singlestart_pressed);
        Bitmap bmpButtonMulti = BitmapFactory.decodeResource(resources, R.drawable.button_multistart);
        Bitmap bmpButtonMultiPressed = BitmapFactory.decodeResource(resources, R.drawable.button_multistart_pressed);
        Bitmap bmpButtonRank = BitmapFactory.decodeResource(resources, R.drawable.button_rank);
        Bitmap bmpButtonRankPressed = BitmapFactory.decodeResource(resources, R.drawable.button_rank_pressed);
        int posX = MainSurfaceView.screenW / 2 - bmpButtonSingle.getWidth() / 2;
        int posY = MainSurfaceView.screenH / 2 - bmpButtonSingle.getHeight() / 2;
        buttonSingle = new MenuButton(context, bmpButtonSingle, bmpButtonSinglePressed, posX, posY);
        posX = MainSurfaceView.screenW / 2 - bmpButtonMulti.getWidth() / 2;
        posY += bmpButtonMulti.getHeight() * 3 / 2;
        buttonLog = new MenuButton(context, bmpButtonMulti, bmpButtonMultiPressed, posX, posY);
        posX = MainSurfaceView.screenW / 2 - bmpButtonMulti.getWidth() / 2;
        posY += bmpButtonMulti.getHeight() * 3 / 2;
        buttonRank = new MenuButton(context, bmpButtonRank, bmpButtonRankPressed, posX, posY);
    }

    public void draw() {
        try {
            canvas = holder.lockCanvas();
            if (canvas != null) {
                canvas.drawColor(Color.WHITE);

                canvas.drawBitmap(background, 0, 0, paint);

                Bitmap  origin_bitmap = BitmapFactory.decodeResource(this.getContext().getResources(), R.drawable.logo);
                int origin_width = origin_bitmap.getWidth();
                int origin_height = origin_bitmap.getHeight();
                float scaleWidth = ((float) MainSurfaceView.screenW * 3 / 4) / origin_width;
                float scaleHeight = ((float) MainSurfaceView.screenH / 2) / origin_height;
                Matrix matrix = new Matrix();
                float scale = scaleWidth > scaleHeight ? scaleHeight : scaleWidth;
                matrix.postScale(scale, scale);
                Bitmap bitmap = Bitmap.createBitmap(origin_bitmap, 0, 0, origin_width, origin_height, matrix, true);
                canvas.drawBitmap(bitmap, MainSurfaceView.screenW / 2 - bitmap.getWidth() / 2, MainSurfaceView.screenH / 4 - bitmap.getHeight() / 2, paint);

                buttonSingle.draw(canvas, paint);
                buttonLog.draw(canvas, paint);
                buttonRank.draw(canvas, paint);
            }
        } catch (Exception e) {

        } finally {
            if (canvas != null) holder.unlockCanvasAndPost(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        buttonSingle.onTouchEvent(event, 0);
        buttonLog.onTouchEvent(event, 1);
        buttonRank.onTouchEvent(event, 2);
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
