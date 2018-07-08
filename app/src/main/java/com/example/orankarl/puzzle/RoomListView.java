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

public class RoomListView extends SurfaceView implements SurfaceHolder.Callback, Runnable{
    private Context context;
    private SurfaceHolder holder;
    private Paint paint;
    private Thread thread;
    private Canvas canvas;

    MainActivity activity = (MainActivity)getContext();

    public Bitmap origin_bitmap = null;
    public Bitmap bitmap = null;

    public static int screenW, screenH;
    private Resources resources = this.getResources();
    MenuButton buttonBack;
    boolean flag = true;
    public RoomListView(Context context) {
        super(context);
        this.context = context;
        holder = this.getHolder();
        holder.addCallback(this);
        paint = new Paint();
        paint.setAntiAlias(true);
        setFocusable(true);
    }

    private void init() {
        Bitmap bmpBackButton = BitmapFactory.decodeResource(resources, R.drawable.button_back);
        Bitmap bmpBackButtonPressed = BitmapFactory.decodeResource(resources, R.drawable.button_back_pressed);
        int posX = bmpBackButton.getWidth() / 4;
        int posY = MainSurfaceView.screenH - bmpBackButton.getHeight() * 5 / 4;
        buttonBack = new MenuButton(context, bmpBackButton, bmpBackButtonPressed, posX, posY);
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

                String title = "Room list";
                canvas.drawText(title, screenW / 2 - activity.getTextWidth(textPaint, title) / 2, screenH / 15 + textPaint.getTextSize() , textPaint);

                buttonBack.draw(canvas, paint);
            }
        } catch (Exception e) {

        } finally {
            if (canvas != null) holder.unlockCanvasAndPost(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        buttonBack.onTouchEvent(event, 19);
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
