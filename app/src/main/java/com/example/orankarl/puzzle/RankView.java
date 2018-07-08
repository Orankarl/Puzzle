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

    MainActivity activity = (MainActivity)getContext();

    public String[] rank_id;
    public int[] time;

    public RankView(Context context) {
        super(context);
        this.context = context;
        holder = this.getHolder();
        holder.addCallback(this);
        paint = new Paint();
        paint.setAntiAlias(true);
        setFocusable(true);
        time = new int[10];
        rank_id = new String[10];
        for (int i = 0; i < 10; i++) {
            time[i] = 0;
            rank_id[i] = "";
        }
    }

    private void init() {
        Bitmap bmpButtonBack = BitmapFactory.decodeResource(resources, R.drawable.button_back);
        Bitmap bmpButtonBackPressed = BitmapFactory.decodeResource(resources, R.drawable.button_back_pressed);

        int posX = bmpButtonBack.getWidth() / 4;
        int posY = MainSurfaceView.screenH - bmpButtonBack.getHeight() * 5 / 4;
        buttonBack = new MenuButton(context, bmpButtonBack, bmpButtonBackPressed, posX, posY);
    }

    public void draw() {
        try {
            canvas = holder.lockCanvas();
            if (canvas != null) {
                canvas.drawBitmap(activity.background, 0, 0, paint);

                int TEXT_SIZE = (int)Math.round(100 * activity.RATIO);
                Paint textPaint = new Paint();
                textPaint.setTypeface(activity.font);
                textPaint.setColor(Color.BLACK);
                textPaint.setTextSize(TEXT_SIZE);

                String title = "Rank";
                canvas.drawText(title, screenW / 2 - activity.getTextWidth(textPaint, title) / 2, screenH / 15 + textPaint.getTextSize() , textPaint);

                TEXT_SIZE = (int)Math.round(60 * activity.RATIO);
                textPaint.setTextSize(TEXT_SIZE);

                int[] minute = new int[10];
                int[] second = new int[10];

                for (int i = 0; i < 10; i++) {
                    minute[i] = time[i] / 60;
                    second[i] = time[i] % 60;
                }

                String[] final_time = new String[10];
                for (int i = 0; i < 10; i++) {
                    final_time[i] = "" + minute[i] + ":" + second[i];
                    if (second[i] == 0)
                        final_time[i] += 0;
                }

                for (int i = 0; i < 10; i++) {
                    String num = "" + (i + 1);
                    canvas.drawText(num, screenW / 6 - activity.getTextWidth(textPaint, num) / 2, screenH * (i + 3) / 15 + textPaint.getTextSize() , textPaint);
                    canvas.drawText(rank_id[i], screenW / 3, screenH * (i + 3) / 15 + textPaint.getTextSize() , textPaint);
                    if (minute[i] == 0 && second[i] == 0)
                        canvas.drawText("-:--", screenW * 3 / 4, screenH * (i + 3) / 15 + textPaint.getTextSize() , textPaint);
                    else
                        canvas.drawText(final_time[i], screenW * 3 / 4, screenH * (i + 3) / 15 + textPaint.getTextSize() , textPaint);
                }

                buttonBack.draw(canvas, paint);
            }
        } catch (Exception e) {

        } finally {
            if (canvas != null) holder.unlockCanvasAndPost(canvas);
        }
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
