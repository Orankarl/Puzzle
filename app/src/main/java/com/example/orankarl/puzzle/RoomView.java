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

public class RoomView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private Context context;
    private SurfaceHolder holder;
    private Paint paint;
    private Thread thread;
    private Canvas canvas;

    public static int screenW, screenH;
    private Resources resources = this.getResources();
    MenuButton buttonCreateRoom;
    MenuButton buttonJoinRoom;
    MenuButton buttonBack;
    boolean flag = true;

    public RoomView(Context context) {
        super(context);
        this.context = context;
        holder = this.getHolder();
        holder.addCallback(this);
        paint = new Paint();
        paint.setAntiAlias(true);
        setFocusable(true);
    }

    private void init() {
        Bitmap bmpButtonCreateRoom = BitmapFactory.decodeResource(resources, R.drawable.button_createroom);
        Bitmap bmpButtonCreateRoomPressed = BitmapFactory.decodeResource(resources, R.drawable.button_createroom_pressed);
        Bitmap bmpButtonJoinRoom = BitmapFactory.decodeResource(resources, R.drawable.button_joinroom);
        Bitmap bmpButtonJoinRoomPressed = BitmapFactory.decodeResource(resources, R.drawable.button_joinroom_pressed);
        Bitmap bmpButtonBack = BitmapFactory.decodeResource(resources, R.drawable.button_back);
        Bitmap bmpButtonBackPressed = BitmapFactory.decodeResource(resources, R.drawable.button_back_pressed);
        int posX = MainSurfaceView.screenW / 2 - bmpButtonCreateRoom.getWidth() / 2;
        int posY = MainSurfaceView.screenH / 3 - bmpButtonCreateRoom.getHeight() / 2;
        buttonCreateRoom = new MenuButton(context, bmpButtonCreateRoom, bmpButtonCreateRoomPressed, posX, posY);
        posX = MainSurfaceView.screenW / 2 - bmpButtonJoinRoom.getWidth() / 2;
        posY = MainSurfaceView.screenH * 2 / 3 - bmpButtonJoinRoom.getHeight() / 2;
        buttonJoinRoom = new MenuButton(context, bmpButtonJoinRoom, bmpButtonJoinRoomPressed, posX, posY);
        posX = bmpButtonBack.getWidth() / 4;
        posY = MainSurfaceView.screenH - bmpButtonBack.getHeight() * 5 / 4;
        buttonBack = new MenuButton(context, bmpButtonBack, bmpButtonBackPressed, posX, posY);
    }

    public void draw() {
        try {
            canvas = holder.lockCanvas();
            if (canvas != null) {
                canvas.drawColor(Color.WHITE);

                buttonCreateRoom.draw(canvas, paint);
                buttonJoinRoom.draw(canvas, paint);
                buttonBack.draw(canvas, paint);
            }
        } catch (Exception e) {

        } finally {
            if (canvas != null) holder.unlockCanvasAndPost(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        buttonCreateRoom.onTouchEvent(event, 9);
        buttonJoinRoom.onTouchEvent(event, 10);
        buttonBack.onTouchEvent(event, 11);
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
