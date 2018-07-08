package com.example.orankarl.puzzle;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.graphics.Region;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.jar.Attributes;

import static android.graphics.BitmapFactory.decodeStream;
import static com.example.orankarl.puzzle.MainActivity.RATIO;

public class CutPictureView extends SurfaceView implements SurfaceHolder.Callback, Runnable{
    private Context context;
    private AttributeSet attrs;
    private SurfaceHolder holder;
    private Paint paint;
    private Thread thread;
    private Canvas canvas;
    private Resources resources = this.getResources();
    public static int screenW, screenH;

    boolean flag = true;
    MenuButton buttonBack;
    MenuButton buttonConfirm;

    MainActivity activity = (MainActivity)getContext();


    private RectF mFloatRect;
    //截图框的变换
    private RectF mDefaultRect;
    //截图框初始数值

    private double mScaleX;
    private double mScaleY;
    //截图框可以放大的倍数

    private final int mDefaultColor = Color.parseColor("#a0000000");
    //截图框外默认颜色

    private float mTouchX;
    private float mTouchY;
    private float mMoveX;
    private float mMoveY;
    //点击和移动的点


    private static final int TOPLEFT = 0x0001;
    private static final int TOPRIGHT = 0x0002;
    private static final int BOTTOMLEFT = 0x0003;
    private static final int BOTTOMRIGHT = 0x0004;
    private static final int BOTHSIDE = 0x0005;
    private static final int MOVE = 0x0006;
    //常量


    private int mChangeType;//判断截图框的改变方式是移动还是缩放

    private int mTouch;//判断点击的四个角

    private int mFloatColor; //正方形框内的颜色



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

    public CutPictureView(Context context, AttributeSet attrs){
        super(context,attrs);
        this.context = context;
        this.attrs = attrs;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CutView);
        this.mFloatColor = typedArray.getColor(R.styleable.CutView_floatColor, mDefaultColor);
        holder = this.getHolder();
        holder.addCallback(this);
        paint = new Paint();
        paint.setAntiAlias(true);
        setFocusable(true);
    }

    private void init() {
        this.setFloatRect();
        Bitmap bmpButtonconfirm = BitmapFactory.decodeResource(resources, R.drawable.button_confirm);
        Bitmap bmpButtonconfirmpressed = BitmapFactory.decodeResource(resources, R.drawable.button_confirm_pressed);
        int posX = bmpButtonconfirm.getWidth() * 7 / 5;
        int posY = MainSurfaceView.screenH - bmpButtonconfirm.getHeight() * 5 / 4;
        buttonConfirm = new MenuButton(context, bmpButtonconfirm, bmpButtonconfirmpressed, posX, posY);

        Bitmap bmpButtonBack = BitmapFactory.decodeResource(resources, R.drawable.button_back);
        Bitmap bmpButtonBackPressed = BitmapFactory.decodeResource(resources, R.drawable.button_back_pressed);
        posX = bmpButtonBack.getWidth() / 4;
        posY = MainSurfaceView.screenH - bmpButtonBack.getHeight() * 5 / 4;
        buttonBack = new MenuButton(context, bmpButtonBack, bmpButtonBackPressed, posX, posY);


    }

    private void setFloatRect() {
        float width = activity.puzzleBitmap.getWidth();
        float height = activity.puzzleBitmap.getHeight();
        float left = MainSurfaceView.screenW / 2 - activity.puzzleBitmap.getWidth() / 2;
        float top = MainSurfaceView.screenH / 3 - activity.puzzleBitmap.getHeight() / 2;
        if (width < height) height =width;else width = height;
        float right = left + width;
        float bottom = top + height;
        mFloatRect = new RectF(left, top, right, bottom);
        mDefaultRect = new RectF(left, top, right, bottom);

    }
    private void drawFloatRect(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#ffffffff"));
        paint.setStrokeWidth(15);
        // 外边框
        float[] points = {mFloatRect.left, mFloatRect.top, mFloatRect.right, mFloatRect.top,
                mFloatRect.right, mFloatRect.top, mFloatRect.right, mFloatRect.bottom,
                mFloatRect.right, mFloatRect.bottom, mFloatRect.left, mFloatRect.bottom,
                mFloatRect.left, mFloatRect.bottom, mFloatRect.left, mFloatRect.top};
        canvas.drawLines(points, paint);
        PathEffect effects = new DashPathEffect(new float[]{5, 5}, 1);
        paint.setPathEffect(effects);
        paint.setStrokeWidth(1);
        float vx = (mFloatRect.right - mFloatRect.left) / 3;
        float vy = (mFloatRect.bottom - mFloatRect.top) / 3;
        // 四条虚线，要画出虚线需要执行setLayerType(LAYER_TYPE_SOFTWARE, null);方法
        float[] dashPoints = {mFloatRect.left + vx, mFloatRect.top, mFloatRect.left + vx, mFloatRect.bottom,
                mFloatRect.left + 2 * vx, mFloatRect.top, mFloatRect.left + 2 * vx, mFloatRect.bottom,
                mFloatRect.left, mFloatRect.top + vy, mFloatRect.right, mFloatRect.top + vy,
                mFloatRect.left, mFloatRect.top + 2 * vy, mFloatRect.right, mFloatRect.top + 2 * vy,};
        canvas.drawLines(dashPoints, paint);
    }

    //截图框的移动
    private void setFloatRectChange() {
        float x = mMoveX - mTouchX;
        float y = mMoveY - mTouchY;
        if (mDefaultRect.left <= mFloatRect.left + x && mDefaultRect.right >= mFloatRect.right + x) {
            mFloatRect.left += x;
            mFloatRect.right += x;
        } else if (mDefaultRect.left > mFloatRect.left + x && mDefaultRect.right >= mFloatRect.right + x) {
            float offx = mDefaultRect.left - mFloatRect.left;
            mFloatRect.left = mDefaultRect.left;
            mFloatRect.right += offx;
        } else if (mDefaultRect.left <= mFloatRect.left + x && mDefaultRect.right < mFloatRect.right + x) {
            float offx = mDefaultRect.right - mFloatRect.right;
            mFloatRect.right = mDefaultRect.right;
            mFloatRect.left += offx;
        }
        if (mDefaultRect.top <= mFloatRect.top + y && mDefaultRect.bottom >= mFloatRect.bottom + y) {
            mFloatRect.bottom += y;
            mFloatRect.top += y;
        } else if (mDefaultRect.top > mFloatRect.top + y && mDefaultRect.bottom >= mFloatRect.bottom + y) {
            float offy = mDefaultRect.top - mFloatRect.top;
            mFloatRect.top = mDefaultRect.top;
            mFloatRect.bottom += offy;
        } else if (mDefaultRect.top <= mFloatRect.top + y && mDefaultRect.bottom < mFloatRect.bottom + y) {
            float offy = mDefaultRect.bottom - mFloatRect.bottom;
            mFloatRect.bottom = mDefaultRect.bottom;
            mFloatRect.top += offy;
        }
        invalidate();
    }

    public Bitmap getCutBitmap() {
        // 计算截图框在原图片所在的位置大小，通过比例可以算出
        float left = (mFloatRect.left - mDefaultRect.left);
        float top = (mFloatRect.top - mDefaultRect.top);
        float width = mFloatRect.width() ;
        float height = mFloatRect.height();
        Bitmap dstBitmap = Bitmap.createBitmap(activity.puzzleBitmap, (int) left, (int) top, (int) width, (int) height);
        return dstBitmap;
    }
    public void draw(){
        try {
            canvas = holder.lockCanvas();
            if (canvas != null) {
                canvas.drawColor(Color.WHITE);
                canvas.drawBitmap(activity.puzzleBitmap, MainSurfaceView.screenW / 2 - activity.puzzleBitmap.getWidth() / 2, MainSurfaceView.screenH / 3 - activity.puzzleBitmap.getHeight() / 2, paint);

                Paint paint = new Paint();
                paint.setColor(mFloatColor);


                // 绘制截图框外部部分
                canvas.save();
                //Region.Op.XOR 是异并集，这里是获取截图框外部部分
                canvas.clipRect(mFloatRect, Region.Op.XOR);
                canvas.drawColor(mFloatColor);
                canvas.restore();
                canvas.save();
                //Region.Op.INTERSECT 是交集
                canvas.clipRect(mFloatRect, Region.Op.INTERSECT);
                // 绘制截图框部分
                drawFloatRect(canvas);
                canvas.restore();

                buttonBack.draw(canvas, paint);
                buttonConfirm.draw(canvas,paint);
                int TEXT_SIZE = (int)Math.round(100 * activity.RATIO);
                Paint textPaint = new Paint();
                textPaint.setTypeface(activity.font);
                textPaint.setColor(Color.BLACK);
                textPaint.setTextSize(TEXT_SIZE);

                String title = "Choose a square capture";
                canvas.drawText(title, screenW / 2 - activity.getTextWidth(textPaint, title) / 2, screenH * 2 / 3 + textPaint.getTextSize() , textPaint);
            }
        } catch (Exception e) {

        } finally {
            if (canvas != null) holder.unlockCanvasAndPost(canvas);
        }
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        buttonBack.onTouchEvent(event, 17);
        buttonConfirm.onTouchEvent(event,25);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchX = event.getX();
                mTouchY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                mMoveX = event.getX();
                mMoveY = event.getY();
                setFloatRectChange();
                mTouchX = mMoveX;
                mTouchY = mMoveY;
                break;
        }
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
