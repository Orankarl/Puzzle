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

import java.util.ArrayList;

public class RankView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private Context context;
    private SurfaceHolder holder;
    private Paint paint;
    private Thread thread;
    private Canvas canvas;

    public static int screenW, screenH;
    private Resources resources = this.getResources();
    MenuButton buttonBack;
    boolean flag = true;

    public RankView(Context context) {
        super(context);
        this.context = context;
        holder = this.getHolder();
        holder.addCallback(this);
        paint = new Paint();
        paint.setAntiAlias(true);
        setFocusable(true);
    }

    private void init() {
        Bitmap bmpButtonBack = BitmapFactory.decodeResource(resources, R.drawable.button_start);
        Bitmap bmpButtonBackPressed = BitmapFactory.decodeResource(resources, R.drawable.button_start_pressed);

        int posX = bmpButtonBack.getWidth() / 4;
        int posY = MainSurfaceView.screenH - bmpButtonBack.getHeight() * 5 / 4;
        buttonBack = new MenuButton(context, bmpButtonBack, bmpButtonBackPressed, posX, posY);
    }

    public void draw() {
        try {
            canvas = holder.lockCanvas();
            if (canvas != null) {
                canvas.drawColor(Color.WHITE);

                float ratioWidth = (float)screenW / 1080;
                float ratioHeight = (float)screenH / 1812;
                double RATIO = Math.min(ratioWidth, ratioHeight);
                if (ratioWidth != ratioHeight) {
                    if (RATIO == ratioWidth) {
                        double OFFSET_LEFT = 0;
                        double OFFSET_TOP = Math.round((screenH - 1812 * RATIO) / 2);
                    }else {
                        double OFFSET_LEFT = Math.round((screenW - 1080 * RATIO) / 2);
                        double OFFSET_TOP = 0;
                    }
                }
                int TEXT_SIZE = (int)Math.round(100 * RATIO);
                Paint textPaint = new Paint();
                textPaint.setColor(Color.BLACK);
                textPaint.setTextSize(TEXT_SIZE);

                String title = "排行榜";
                canvas.drawText(title, screenW / 2 - getTextWidth(textPaint, title) / 2, screenH / 15 + textPaint.getTextSize() , textPaint);

                TEXT_SIZE = (int)Math.round(60 * RATIO);
                textPaint.setTextSize(TEXT_SIZE);

                String[] rank_id = new String[10];

                int[] time = new int[10];
                int[] minute = new int[10];
                int[] second = new int[10];

                for (int i = 0; i < 10; i++) {
                    time[i] = 100;
                    minute[i] = time[i] / 60;
                    second[i] = time[i] % 60;
                }

                String[] final_time = new String[10];
                for (int i = 0; i < 10; i++) {
                    final_time[i] = "" + minute[i] + ":" + second[i];
                }

                for (int i = 0; i < 10; i++) {
                    rank_id[i] = "诚神李冠诚";
                }
                for (int i = 0; i < 10; i++) {
                    String num = "" + (i + 1);
                    canvas.drawText(num, screenW / 6 - getTextWidth(textPaint, num) / 2, screenH * (i + 3) / 15 + textPaint.getTextSize() , textPaint);
                    canvas.drawText(rank_id[i], screenW / 3, screenH * (i + 3) / 15 + textPaint.getTextSize() , textPaint);
                    canvas.drawText(final_time[i], screenW * 3 / 4, screenH * (i + 3) / 15 + textPaint.getTextSize() , textPaint);
                }

                buttonBack.draw(canvas, paint);
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
        buttonBack.onTouchEvent(event, 18);
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
