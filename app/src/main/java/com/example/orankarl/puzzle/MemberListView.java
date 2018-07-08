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

public class MemberListView extends SurfaceView implements SurfaceHolder.Callback, Runnable{
    private Context context;
    private SurfaceHolder holder;
    private Paint paint;
    private Canvas canvas;

    MainActivity activity = (MainActivity)getContext();

    public Bitmap origin_bitmap = null;
    public Bitmap bitmap = null;

    public static int screenW, screenH;
    private Resources resources = this.getResources();
    MenuButton buttonExit;
    MenuButton buttonClose;
    MenuButton buttonStart;
    boolean flag = true;
    public MemberListView(Context context) {
        super(context);
        this.context = context;
        holder = this.getHolder();
        holder.addCallback(this);
        paint = new Paint();
        paint.setAntiAlias(true);
        setFocusable(true);
    }

    private void init() {
        Bitmap bmpExitButton = BitmapFactory.decodeResource(resources, R.drawable.button_exit);
        Bitmap bmpExitButtonPressed = BitmapFactory.decodeResource(resources, R.drawable.button_exit_pressed);
        Bitmap bmpCloseButton = BitmapFactory.decodeResource(resources, R.drawable.button_deleteroom);
        Bitmap bmpCloseButtonPressed = BitmapFactory.decodeResource(resources, R.drawable.button_deleteroom_pressed);
        Bitmap bmpStartButton = BitmapFactory.decodeResource(resources, R.drawable.button_start);
        Bitmap bmpStartButtonPressed = BitmapFactory.decodeResource(resources, R.drawable.button_start_pressed);
        int posX = bmpExitButton.getWidth() / 4;
        int posY = MainSurfaceView.screenH - bmpExitButton.getHeight() * 5 / 4;
        buttonExit = new MenuButton(context, bmpExitButton, bmpExitButtonPressed, posX, posY);
        buttonClose = new MenuButton(context, bmpCloseButton, bmpCloseButtonPressed, posX, posY);
        posX = MainSurfaceView.screenW / 2 - bmpStartButton.getWidth() / 2;
        posY = MainSurfaceView.screenH * 3 / 4 - bmpCloseButton.getHeight() / 2;
        buttonStart = new MenuButton(context, bmpStartButton, bmpStartButtonPressed, posX, posY);
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

                String title = activity.host + "'s Room";
                canvas.drawText(title, screenW / 2 - activity.getTextWidth(textPaint, title) / 2, screenH / 15 + textPaint.getTextSize() , textPaint);

                if (activity.isHost) {
                    buttonClose.draw(canvas, paint);
                    buttonStart.draw(canvas, paint);
                }
                else {
                    buttonExit.draw(canvas, paint);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (canvas != null) holder.unlockCanvasAndPost(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (activity.isHost) {
            buttonClose.onTouchEvent(event, 21);
            buttonStart.onTouchEvent(event, 22);
        }
        else {
            buttonExit.onTouchEvent(event, 20);
        }
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
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }
}
