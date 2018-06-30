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
    private ArrayList<PuzzlePieceGroup> pieces = new ArrayList<>();
    private boolean isChosen = false, needUpdate = false;
    private int chosenPieceIndex, updateOrderIndex = -1;
    float touchX = 0, touchY = 0;
    int biasX = 0, biasY = 0;
    LinkedList<Integer> drawOrder = new LinkedList<>();
    int pieceCount = 0;
    int pieceWidth, pieceHeight;

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

        int spacing = screenW / 10;

        ArrayList<Bitmap> cutImages = CutUtil.cutImage(bitmap, CutUtil.type2, 4);
        PuzzlePiece piece1 = new PuzzlePiece(cutImages.get(0), (screenW - spacing) / 2 - cutImages.get(0).getWidth(),
                (screenH - spacing) / 2 - cutImages.get(0).getHeight());
        PuzzlePiece piece2 = new PuzzlePiece(cutImages.get(1), (screenW + spacing) / 2,
                (screenH - spacing) / 2 - cutImages.get(1).getHeight());
        PuzzlePiece piece3 = new PuzzlePiece(cutImages.get(2), (screenW - spacing) / 2 - cutImages.get(0).getWidth(),
                (screenH + spacing) / 2);
        PuzzlePiece piece4 = new PuzzlePiece(cutImages.get(3), (screenW + spacing) / 2,
                (screenH + spacing) / 2);

        PuzzlePieceGroup pieceGroup1 = new PuzzlePieceGroup(piece1, 0, 2, bitmap.getWidth()/2, bitmap.getHeight()/2);
        PuzzlePieceGroup pieceGroup2 = new PuzzlePieceGroup(piece2, 1, 2, bitmap.getWidth()/2, bitmap.getHeight()/2);
        PuzzlePieceGroup pieceGroup3 = new PuzzlePieceGroup(piece3, 2, 2, bitmap.getWidth()/2, bitmap.getHeight()/2);
        PuzzlePieceGroup pieceGroup4 = new PuzzlePieceGroup(piece4, 3, 2, bitmap.getWidth()/2, bitmap.getHeight()/2);
//        pieceGroup1.addPieceGroup(pieceGroup2);
//        pieceGroup1.addPieceGroup(pieceGroup3);
//        pieceGroup1.addPieceGroup(pieceGroup4);
        pieces.add(pieceGroup1);
        pieces.add(pieceGroup2);
        pieces.add(pieceGroup3);
        pieces.add(pieceGroup4);

        for (int i = 0; i < 4; i++) {
            drawOrder.offer(i);
        }
    }

    public void draw() {
        try {
            canvas = holder.lockCanvas();
            if (canvas != null) {
                canvas.drawColor(Color.WHITE);

                //这里使用双缓冲，防止闪烁
                bitmapCache = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);

                Canvas canvas1 = new Canvas(bitmapCache);
                if (needUpdate) {
                    Iterator<Integer> iterator = drawOrder.iterator();
                    while(iterator.hasNext()) {
                        int temp = iterator.next();
                        if (temp == chosenPieceIndex) {
                            iterator.remove();
                        }
                    }
                    drawOrder.offer(chosenPieceIndex);
                    needUpdate = false;
                }
                for (int index : drawOrder) {
                    canvas1 = pieces.get(index).draw(canvas1, paint);
                }
                if (isChosen) {
                    canvas1 = pieces.get(chosenPieceIndex).draw(canvas1, paint);
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

    void drawPieceGroup(Canvas canvas, PuzzlePieceGroup pieceGroup) {
        canvas.drawBitmap(pieceGroup.getMainPiece().getBitmap(), pieceGroup.getMainPiece().getPosX(), pieceGroup.getMainPiece().getPosY(), paint);
        ArrayList<PuzzlePiece> pieces = pieceGroup.getAttachedPiece();
//        ArrayList<Integer> biasX = pieceGroup.
    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        buttonStart.onTouchEvent(event, 0);
//        buttonChangeAccount.onTouchEvent(event, 5);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                ListIterator<Integer> listIterator = drawOrder.listIterator(drawOrder.size());
                while (listIterator.hasPrevious()) {
                    int index = listIterator.previous();
                    PuzzlePieceGroup piece = pieces.get(index);
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
                    needUpdate = true;
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
