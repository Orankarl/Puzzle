package com.example.orankarl.puzzle;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

public class PuzzleSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private Context context;
    private SurfaceHolder holder;
    private Paint paint;
    private Thread thread;
    private Canvas canvas;
    private Bitmap bitmap, cutBitmap, bitmapCache;
    private ArrayList<PuzzlePiece> pieces = new ArrayList<>();
    PuzzlePiece piece;
    private boolean isChosen = false;
    private int chosenPieceIndex, updateOrderIndex = -1;
    float touchX = 0, touchY = 0;
    int biasX = 0, biasY = 0;
    LinkedList<Integer> drawOrder = new LinkedList<>();
    int upmostPieceIndex = 0;

    public static int screenW, screenH;
    private Resources resources = this.getResources();
    MenuButton buttonStart;
    MenuButton buttonChangeAccount;
    boolean flag = true;
    public PuzzleSurfaceView(Context context) {
        super(context);
        this.context = context;
        holder = this.getHolder();
        holder.addCallback(this);
        paint = new Paint();
        paint.setAntiAlias(true);
        setFocusable(true);
    }

    private void init() {
        int fixedWidth = DensityUtil.densityUtil.px2dip(600);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        bitmap = BitmapFactory.decodeResource(resources, R.drawable.sample, options);
        Log.d("width:", String.valueOf(options.outWidth));
        Log.d("height", String.valueOf(options.outHeight));
        options.inJustDecodeBounds = false;
        options.inSampleSize = options.outWidth / fixedWidth;
        int height = options.outHeight * fixedWidth / options.outWidth;
        options.outWidth = fixedWidth;
        options.outHeight = height;
        bitmap = BitmapFactory.decodeResource(resources, R.drawable.sample, options);
        Log.d("width:", String.valueOf(options.outWidth));
        Log.d("height", String.valueOf(options.outHeight));

        piece = new PuzzlePiece(bitmap, screenW / 2 - bitmap.getWidth() / 2, screenH / 2 - bitmap.getHeight() / 2);

        GraphicPath path = new GraphicPath();
        path.addPath(0, 0);
        path.addPath(bitmap.getWidth(), bitmap.getHeight()/2);
        path.addPath(bitmap.getWidth() / 2, bitmap.getHeight());
        path.addPath(0, 0);

        Rect rect = new Rect(path.getLeft(), path.getTop(), path.getRight(), path.getBottom());
        if (rect.left < 0) rect.left = 0;
        if (rect.right < 0) rect.right = 0;
        if (rect.top < 0) rect.top = 0;
        if (rect.bottom < 0) rect.bottom = 0;
        int cut_width = Math.abs(rect.left - rect.right);
        int cut_height = Math.abs(rect.top - rect.bottom);

        if (cut_width > 0 && cut_height > 0) {
            Bitmap tempCutBitmap = Bitmap.createBitmap(bitmap, rect.left, rect.top, cut_width, cut_height);
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            paint.setColor(Color.BLACK);
            Bitmap temp = Bitmap.createBitmap(cut_width, cut_height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(temp);

            Path path1 = new Path();
            if (path.size() > 1) {
                path1.moveTo((float) (path.pathX.get(0) - rect.left), (float)(path.pathY.get(0) - rect.top));
                for (int i = 1; i < path.size(); i++) {
                    path1.lineTo((float)(path.pathX.get(i)-rect.left), (float)(path.pathY.get(i) - rect.top));
                }
            }
            canvas.drawPath(path1, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

            canvas.drawBitmap(tempCutBitmap, 0, 0, paint);
            cutBitmap = temp;
        }

        int spacing = screenW / 10;

        ArrayList<Bitmap> cutImages = CutUtil.cutImage(bitmap, CutUtil.type2, 4);
        pieces.add(new PuzzlePiece(cutImages.get(0), (screenW - spacing) / 2 - cutImages.get(0).getWidth(),
                (screenH - spacing) / 2 - cutImages.get(0).getHeight()));
        pieces.add(new PuzzlePiece(cutImages.get(1), (screenW + spacing) / 2,
                (screenH - spacing) / 2 - cutImages.get(1).getHeight()));
        pieces.add(new PuzzlePiece(cutImages.get(2), (screenW - spacing) / 2 - cutImages.get(0).getWidth(),
                (screenH + spacing) / 2));
        pieces.add(new PuzzlePiece(cutImages.get(3), (screenW + spacing) / 2,
                (screenH + spacing) / 2));

        for (int i = 0; i < 4; i++) {
            drawOrder.offer(i);
        }
    }

    public void draw() {
        try {
            canvas = holder.lockCanvas();
            if (canvas != null) {
                canvas.drawColor(Color.WHITE);

//                    canvas.drawBitmap(bitmap, MainSurfaceView.screenW / 2 - bitmap.getWidth() / 2, MainSurfaceView.screenH * 2 / 3 - bitmap.getHeight() / 2, paint);
//                    canvas.drawBitmap(cutBitmap, MainSurfaceView.screenW / 2 - cutBitmap.getWidth() / 2, MainSurfaceView.screenH / 3 - cutBitmap.getHeight() / 2, paint);
//                    canvas.drawBitmap(piece.getBitmap(), piece.getPosX(), piece.getPosY(), paint);
//                    for (PuzzlePiece piece:pieces) {
//                        canvas.drawBitmap(piece.getBitmap(), piece.getPosX(), piece.getPosY(), paint);
//                    }
                bitmapCache = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);

                Canvas canvas1 = new Canvas(bitmapCache);
                if (updateOrderIndex > -1) {
                    Iterator<Integer> iterator = drawOrder.iterator();
                    while(iterator.hasNext()) {
                        int temp = iterator.next();
                        if (temp == updateOrderIndex) {
                            iterator.remove();
                        }
                    }
                    drawOrder.offer(updateOrderIndex);
                    updateOrderIndex = -1;
                }
                for (int index : drawOrder) {
                    canvas1.drawBitmap(pieces.get(index).getBitmap(), pieces.get(index).getPosX(), pieces.get(index).getPosY(), paint);
                }
                if (isChosen) {
                    canvas1.drawBitmap(pieces.get(chosenPieceIndex).getBitmap(), pieces.get(chosenPieceIndex).getPosX(), pieces.get(chosenPieceIndex).getPosY(), paint);
                }
                canvas.drawBitmap(bitmapCache, 0, 0, paint);
//                for (int index : drawOrder) {
//                    canvas.drawBitmap(pieces.get(index).getBitmap(), pieces.get(index).getPosX(), pieces.get(index).getPosY(), paint);
//                }
//                if (isChosen) {
//                    canvas.drawBitmap(pieces.get(chosenPieceIndex).getBitmap(), pieces.get(chosenPieceIndex).getPosX(), pieces.get(chosenPieceIndex).getPosY(), paint);
//                }

            }
        } catch (Exception e) {

        } finally {
            if (canvas != null) holder.unlockCanvasAndPost(canvas);
        }
    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        buttonStart.onTouchEvent(event, 0);
//        buttonChangeAccount.onTouchEvent(event, 5);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (piece.isInPiece(event.getX(), event.getY())) {
                    isChosen = true;
                    touchX = event.getX();
                    touchY = event.getY();
                    biasX = (int) touchX - piece.getPosX();
                    biasY = (int) touchY - piece.getPosY();
//                    Log.d("touchX", String.valueOf(piece.getPosX()));
//                    Log.d("touchY", String.valueOf(piece.getPosY()));
                }
                ListIterator<Integer> listIterator = drawOrder.listIterator(drawOrder.size());
                while (listIterator.hasPrevious()) {
                    int index = listIterator.previous();
                    PuzzlePiece piece = pieces.get(index);
                    if (piece.isInPiece(event.getX(), event.getY())) {
                        isChosen = true;
                        chosenPieceIndex = index;
                        touchX = event.getX();
                        touchY = event.getY();
                        biasX = (int) touchX - piece.getPosX();
                        biasY = (int) touchY - piece.getPosY();
                        break;
                    }
                }

                break;
            case MotionEvent.ACTION_MOVE:
                if (isChosen) {
//                    piece.addDiff(event.getX() - touchX, event.getY() - touchY);
                    pieces.get(chosenPieceIndex).setPosX((int)event.getX() - biasX);
                    pieces.get(chosenPieceIndex).setPosY((int)event.getY() - biasY);
//                    Log.d("dx:", String.valueOf(event.getX() - touchX));
//                    Log.d("dy:", String.valueOf(event.getY() - touchY));
                }
                break;
            case MotionEvent.ACTION_UP:
                if (isChosen) {
                    updateOrderIndex = chosenPieceIndex;
//                    Iterator<Integer> iterator = drawOrder.iterator();
//                    while(iterator.hasNext()) {
//                        int temp = iterator.next();
//                        if (temp == chosenPieceIndex) {
//                            iterator.remove();
//                        }
//                    }
//                    drawOrder.offer(chosenPieceIndex);
                }
                isChosen = false;
                break;
            default:
                break;
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
                if (end - start < 15) {
                    Thread.sleep(15 - (end - start));
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
        Log.d("view width:", String.valueOf(screenW));
        Log.d("view height", String.valueOf(screenH));
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
