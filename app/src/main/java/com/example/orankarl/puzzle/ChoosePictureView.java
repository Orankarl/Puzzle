package com.example.orankarl.puzzle;

import android.content.Context;
import android.content.res.AssetManager;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static android.graphics.BitmapFactory.decodeStream;

public class ChoosePictureView extends SurfaceView implements SurfaceHolder.Callback, Runnable{
    private Context context;
    private SurfaceHolder holder;
    private Paint paint;
    private Canvas canvas;

    final public static int PRESET_IMAGES = 5;

    MainActivity activity = (MainActivity)getContext();

    public Bitmap origin_bitmap = null;
    public Bitmap bitmap = null;

    public static int screenW, screenH;
    private Resources resources = this.getResources();
    MenuButton buttonStart;
    MenuButton buttonStart2;
    MenuButton buttonRechoose;
    MenuButton[] buttonPreSetPicture;

    boolean flag = true;
    public ChoosePictureView(Context context) {
        super(context);
        this.context = context;
        holder = this.getHolder();
        holder.addCallback(this);
        paint = new Paint();
        paint.setAntiAlias(true);
        setFocusable(true);
    }

    private Bitmap decodeFile(File f) {
        Bitmap b = null;
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;

            FileInputStream fis = new FileInputStream(f);
            decodeStream(fis, null, o);
            fis.close();
            int IMAGE_MAX_SIZE = 1000;
            int scale = 1;
            if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
                scale = (int) Math.pow(
                        2,
                        (int) Math.round(Math.log(IMAGE_MAX_SIZE
                                / (double) Math.max(o.outHeight, o.outWidth))
                                / Math.log(0.5)));
            }

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            fis = new FileInputStream(f);
            b = decodeStream(fis, null, o2);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return b;
    }

    private void init() {
        origin_bitmap = activity.puzzleBitmap;
        Bitmap bmpButton = BitmapFactory.decodeResource(resources, R.drawable.button_choose);
        Bitmap bmpButtonPressed = BitmapFactory.decodeResource(resources, R.drawable.button_choose_pressed);
        Bitmap bmpButton2 = BitmapFactory.decodeResource(resources, R.drawable.button_confirm);
        Bitmap bmpButton2Pressed = BitmapFactory.decodeResource(resources, R.drawable.button_confirm_pressed);
        Bitmap bmpButtonRechoose = BitmapFactory.decodeResource(resources, R.drawable.button_choose);
        Bitmap bmpButtonRechoosePressed = BitmapFactory.decodeResource(resources, R.drawable.button_choose);

        Bitmap[] bmpButtonPreSetPicture = new Bitmap[PRESET_IMAGES];
        buttonPreSetPicture = new MenuButton[PRESET_IMAGES];
        AssetManager assetManager = activity.getAssets();
        InputStream is = null;
        for (int i = 0; i < PRESET_IMAGES; ++i) {
            try {
                is = assetManager.open("PreSetImage/image" + Integer.toString(i + 1) + ".jpg");
            } catch (IOException e) {
                e.printStackTrace();
            }
            bmpButtonPreSetPicture[i] = BitmapFactory.decodeStream(is);
        }
        //assetManager.close();

        int origin_width = bmpButtonPreSetPicture[0].getWidth();
        int origin_height = bmpButtonPreSetPicture[0].getHeight();
        float scaleWidth = ((float) MainSurfaceView.screenW / 4) / origin_width;
        float scaleHeight = ((float) MainSurfaceView.screenH / 4) / origin_height;
        Matrix matrix = new Matrix();
        float scale = scaleWidth > scaleHeight ? scaleHeight : scaleWidth;
        matrix.postScale(scale, scale);

        for (int i = 0; i < PRESET_IMAGES; ++i)
            bmpButtonPreSetPicture[i] = Bitmap.createBitmap(bmpButtonPreSetPicture[i], 0, 0, origin_width, origin_height, matrix, true);

        int posX = MainSurfaceView.screenW / 2 - bmpButton.getWidth() / 2;
        int posY = MainSurfaceView.screenH * 3 / 4 - bmpButton.getHeight()/2;
        buttonStart = new MenuButton(context, bmpButton, bmpButtonPressed, posX, posY);
        buttonRechoose = new MenuButton(context, bmpButtonRechoose, bmpButtonRechoosePressed, posX, posY - bmpButton.getHeight() * 3 / 2);
        buttonStart2 = new MenuButton(context, bmpButton2, bmpButton2Pressed, posX, posY + bmpButton.getHeight() * 3 / 2);
        posX = MainSurfaceView.screenW / 6 - bmpButtonPreSetPicture[0].getWidth() / 2;
        posY = MainSurfaceView.screenH / 5 - bmpButtonPreSetPicture[0].getHeight() / 2;
        buttonPreSetPicture[0] = new MenuButton(context, bmpButtonPreSetPicture[0], bmpButtonPreSetPicture[0], posX, posY);
        posX = MainSurfaceView.screenW * 3 / 6 - bmpButtonPreSetPicture[0].getWidth() / 2;
        buttonPreSetPicture[1] = new MenuButton(context, bmpButtonPreSetPicture[1], bmpButtonPreSetPicture[1], posX, posY);
        posX = MainSurfaceView.screenW * 5 / 6 - bmpButtonPreSetPicture[0].getWidth() / 2;
        buttonPreSetPicture[2] = new MenuButton(context, bmpButtonPreSetPicture[2], bmpButtonPreSetPicture[2], posX, posY);
        posX = MainSurfaceView.screenW / 3 - bmpButtonPreSetPicture[0].getWidth() / 2;
        posY += bmpButtonPreSetPicture[0].getHeight() * 5 / 4;
        buttonPreSetPicture[3] = new MenuButton(context, bmpButtonPreSetPicture[3], bmpButtonPreSetPicture[3], posX, posY);
        posX = MainSurfaceView.screenW * 2 / 3 - bmpButtonPreSetPicture[0].getWidth() / 2;
        buttonPreSetPicture[4] = new MenuButton(context, bmpButtonPreSetPicture[4], bmpButtonPreSetPicture[4], posX, posY);

        if (origin_bitmap != null) {
            origin_width = origin_bitmap.getWidth();
            origin_height = origin_bitmap.getHeight();
            scaleWidth = ((float) MainSurfaceView.screenW * 3 / 4) / origin_width;
            scaleHeight = ((float) MainSurfaceView.screenH / 2) / origin_height;
            matrix = new Matrix();
            scale = scaleWidth > scaleHeight ? scaleHeight : scaleWidth;
            matrix.postScale(scale, scale);
            bitmap = Bitmap.createBitmap(origin_bitmap, 0, 0, origin_width, origin_height, matrix, true);
            activity.puzzleBitmap = bitmap;
        }
    }

    public void draw() {
        try {
            canvas = holder.lockCanvas();
            if (canvas != null) {
                canvas.drawBitmap(activity.background, 0, 0, paint);
                if (activity.isSelected) {
                    canvas.drawBitmap(bitmap, MainSurfaceView.screenW / 2 - bitmap.getWidth() / 2, MainSurfaceView.screenH / 3 - bitmap.getHeight() / 2, paint);
                    buttonStart2.draw(canvas, paint);
                    buttonRechoose.draw(canvas, paint);
                }
                else {
                    for (int i = 0; i < PRESET_IMAGES; ++i)
                        buttonPreSetPicture[i].draw(canvas, paint);
                }
                buttonStart.draw(canvas, paint);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (canvas != null) holder.unlockCanvasAndPost(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        buttonStart.onTouchEvent(event, 7);
        buttonStart2.onTouchEvent(event, 8);
        buttonRechoose.onTouchEvent(event, 24);
        for (int i = 0; i < PRESET_IMAGES; ++i)
            buttonPreSetPicture[i].onTouchEvent(event, MenuButton.PRESET_OFFSET+i);
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
