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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Locale;

public class PuzzleSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private Context context;
    private SurfaceHolder holder;
    private Paint paint;
    private Thread thread;
    private Canvas canvas;
    private Bitmap bitmap, cutBitmap, bitmapCache;
    private ArrayList<PuzzlePieceGroup> pieces = new ArrayList<>();
    private boolean isChosen = false, needUpdate = false, isSingle, isOnline, isFinished = false;
    private int chosenPieceIndex, pickedPieceIndex;
    float touchX = 0, touchY = 0;
    int biasX = 0, biasY = 0;
    LinkedList<Integer> drawOrder = new LinkedList<>();
    int pieceCount = 9, rowCount = 3;
    int pieceWidth, pieceHeight;
    int spacing;
    boolean[] isPieceNeedPaint, isPicked;
    ArrayList<Tuple> posList = new ArrayList<>();
    String pattern = CutUtil.type1, timeStr;
    long startTime;
    int minute, second;
    MenuButton buttonBack;
    double RATIO;

    PuzzleActivity activity = (PuzzleActivity) getContext();

    public static int screenW, screenH;
    private Resources resources = this.getResources();
    MenuButton buttonStart;
    MenuButton buttonChangeAccount;
    boolean flag = true;
    public PuzzleSurfaceView(Context context, Bitmap bitmap, int pattern, int split, boolean isSingle, boolean isOnline) {
        super(context);
        this.context = context;
        holder = this.getHolder();
        holder.addCallback(this);
        paint = new Paint();
        paint.setAntiAlias(true);
        setFocusable(true);

//        this.bitmap = BitmapFactory.decodeByteArray(bitmap, 0, bitmap.length);
//        this.bitmap = BitmapUtil.getBitmapFromFile(bitmap);
//        this.bitmap = MainActivity.puzzleBitmap;
        this.bitmap = bitmap;
//        Log.d("puzzleBitmap w&h:", String.valueOf(MainActivity.puzzleBitmap.getWidth()) + " " + String.valueOf(MainActivity.puzzleBitmap.getHeight()));
        if (pattern == 1) this.pattern = CutUtil.type1;
        else this.pattern = CutUtil.type2;
        pieceCount = split;
        rowCount = (int) Math.sqrt(pieceCount);
        this.isSingle = isSingle;
        this.isOnline = isOnline;
    }

    private void init() {
        this.bitmap = BitmapUtil.setImgSize(this.bitmap, (int) (0.8*screenW), 0);

        isPieceNeedPaint = new boolean[pieceCount];
        for (int i = 0; i < pieceCount; i++) isPieceNeedPaint[i] = true;

        isPicked = new boolean[pieceCount];
        for (int i = 0; i < pieceCount; i++) isPicked[i] = false;

        if (!isSingle && isOnline) {
            MainActivity.api.onPick(pickResponse -> {
                isPicked[pickResponse.pieceIndex] = true;
            });

            MainActivity.api.onMoveTo(response -> {
                pieces.get(pickedPieceIndex).setPosX((int)(screenW * response.X));
                pieces.get(pickedPieceIndex).setPosY((int)(screenH * response.Y));
            });

            MainActivity.api.onRelease(response -> {
                isPicked[pickedPieceIndex] = false;
            });
        }

        //1.获取当前设备的屏幕大小

        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        //2.计算与你开发时设定的屏幕大小的纵横比(这里假设你开发时定的屏幕大小是480*800)

        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;
        float ratioWidth = (float)screenWidth / 480;
        float ratioHeight = (float)screenHeight / 800;

        RATIO = Math.min(ratioWidth, ratioHeight);

        //3.根据上一步计算出来的最小纵横比来确定字体的大小(假定在480*800屏幕下字体大小设定为35)


//        int fixedWidth = DensityUtil.densityUtil.px2dip(500);
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inJustDecodeBounds = true;
//        bitmap = BitmapFactory.decodeResource(resources, R.drawable.sample, options);
//        Log.d("width:", String.valueOf(options.outWidth));
//        Log.d("height", String.valueOf(options.outHeight));
//        options.inJustDecodeBounds = false;
//        options.inSampleSize = options.outWidth / fixedWidth;
//        int height = options.outHeight * fixedWidth / options.outWidth;
//        options.outWidth = fixedWidth;
//        options.outHeight = height;
//        bitmap = BitmapFactory.decodeResource(resources, R.drawable.sample, options);
//        Log.d("width:", String.valueOf(options.outWidth));
//        Log.d("height", String.valueOf(options.outHeight));

        pieceWidth = bitmap.getWidth() / rowCount;
        pieceHeight = bitmap.getHeight() / rowCount;

        initPiecePos();

//        if (pieceCount == 4) {
//            spacing  = screenW / 10;
//
//            ArrayList<Bitmap> cutImages = CutUtil.cutImage(bitmap, CutUtil.type2, 4);
//            PuzzlePiece piece1 = new PuzzlePiece(cutImages.get(0), (screenW - spacing) / 2 - cutImages.get(0).getWidth(),
//                    (screenH - spacing) / 2 - cutImages.get(0).getHeight());
//            PuzzlePiece piece2 = new PuzzlePiece(cutImages.get(1), (screenW + spacing) / 2,
//                    (screenH - spacing) / 2 - cutImages.get(1).getHeight());
//            PuzzlePiece piece3 = new PuzzlePiece(cutImages.get(2), (screenW - spacing) / 2 - cutImages.get(0).getWidth(),
//                    (screenH + spacing) / 2);
//            PuzzlePiece piece4 = new PuzzlePiece(cutImages.get(3), (screenW + spacing) / 2,
//                    (screenH + spacing) / 2);
//
//            PuzzlePieceGroup pieceGroup1 = new PuzzlePieceGroup(piece1, 0, 2, bitmap.getWidth()/2, bitmap.getHeight()/2);
//            PuzzlePieceGroup pieceGroup2 = new PuzzlePieceGroup(piece2, 1, 2, bitmap.getWidth()/2, bitmap.getHeight()/2);
//            PuzzlePieceGroup pieceGroup3 = new PuzzlePieceGroup(piece3, 2, 2, bitmap.getWidth()/2, bitmap.getHeight()/2);
//            PuzzlePieceGroup pieceGroup4 = new PuzzlePieceGroup(piece4, 3, 2, bitmap.getWidth()/2, bitmap.getHeight()/2);
////        pieceGroup1.addPieceGroup(pieceGroup2);
////        pieceGroup1.addPieceGroup(pieceGroup3);
////        pieceGroup1.addPieceGroup(pieceGroup4);
//            pieces.add(pieceGroup1);
//            pieces.add(pieceGroup2);
//            pieces.add(pieceGroup3);
//            pieces.add(pieceGroup4);
//
//            for (int i = 0; i < 4; i++) {
//                drawOrder.offer(i);
//            }
//        } else if (pieceCount == 9) {
//            spacing = screenW / 20;
//            ArrayList<Bitmap> cutImages = CutUtil.cutImage(bitmap, pattern, 9);
//            PuzzlePiece piece1 = new PuzzlePiece(cutImages.get(0), posList.get(0).x, posList.get(0).y);
//            for (int i = 0; i < 9; i++) {
////                PuzzlePieceGroup pieceGroup = ;
//                pieces.add(new PuzzlePieceGroup(new PuzzlePiece(cutImages.get(i), posList.get(i).x, posList.get(i).y), i, rowCount, pieceWidth, pieceHeight));
//            }
//
//            for (int i = 0; i < 9; i++) {
//                drawOrder.offer(i);
//            }
//        } else if (pieceCount == 16) {
//            ArrayList<Bitmap> cutImages = CutUtil.cutImage(bitmap, pattern, 16);
//            for (int i = 0; i < 16; i++) {
////                PuzzlePieceGroup pieceGroup = ;
//                pieces.add(new PuzzlePieceGroup(new PuzzlePiece(cutImages.get(i), posList.get(i).x, posList.get(i).y), i, rowCount, pieceWidth, pieceHeight));
//            }
//
//            for (int i = 0; i < 16; i++) {
//                drawOrder.offer(i);
//            }
//        }
        ArrayList<Bitmap> cutImages = CutUtil.cutImage(bitmap, pattern, pieceCount);
        for (int i = 0; i < pieceCount; i++) {
//                PuzzlePieceGroup pieceGroup = ;
            pieces.add(new PuzzlePieceGroup(new PuzzlePiece(cutImages.get(i), posList.get(i).x, posList.get(i).y), i, rowCount, pieceWidth, pieceHeight));
        }

        for (int i = 0; i < pieceCount; i++) {
            drawOrder.offer(i);
        }

        Bitmap bmpButtonBack = BitmapFactory.decodeResource(resources, R.drawable.button_back);
        Bitmap bmpButtonBackPressed = BitmapFactory.decodeResource(resources, R.drawable.button_back_pressed);
        buttonBack = new MenuButton(context, bmpButtonBack, bmpButtonBackPressed, screenW/2-bmpButtonBack.getWidth()/2, screenH - bmpButtonBack.getHeight()*5/4);

        startTime = System.currentTimeMillis();
        isFinished = false;

        Log.d("pieces size:", String.valueOf(pieces.size()));
    }

    public void draw() {
        try {
            canvas = holder.lockCanvas();
            if (canvas != null) {
                canvas.drawBitmap(MainActivity.background, 0, 0, paint);

                //这里使用双缓冲，防止闪烁
                bitmapCache = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);

                Canvas canvas1 = new Canvas(bitmapCache);
                if (needUpdate) {
                    checkCombination();
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
                    if (isPieceNeedPaint[index] && !isFinished) {
                        canvas1 = pieces.get(index).draw(canvas1, paint);
                    }
                }
                if (isChosen && !isFinished) {
                    canvas1 = pieces.get(chosenPieceIndex).draw(canvas1, paint);
                }
                //draw time
                if (!isFinished) {
                    timeStr = "所用时间：";
                    long deltaMillis = System.currentTimeMillis() - startTime;
                    second = (int) (deltaMillis / 1000 % 60);
                    minute = (int) (deltaMillis / (60000) % 60);
                    if (minute < 10) timeStr += "0";
                    timeStr += String.valueOf(minute) + ":";
                    if (second < 10) timeStr += "0";
                    timeStr += String.valueOf(second);
                }

                Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                mPaint.setColor(Color.BLACK);
                mPaint.setTextSize((float)RATIO*40);
                mPaint.setTextAlign(Paint.Align.RIGHT);

                Rect rect = new Rect();
                mPaint.getTextBounds(timeStr, 0, 5, rect);
//                canvas.drawText("00:00", 0,rect.height() - rect.bottom ,mPaint);
                canvas1.drawText(timeStr, screenW - 0.03f*screenW, rect.height() - rect.bottom + 0.03f*screenH, mPaint);

                //draw finish view
                if (isFinished) {
                    //finish text
                    String finishStr = "完成";
                    Paint newPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                    newPaint.setColor(Color.BLACK);
                    newPaint.setTextSize((float)RATIO*50);
                    newPaint.setTextAlign(Paint.Align.CENTER);

                    Rect rect1 = new Rect();
                    newPaint.getTextBounds(finishStr, 0, finishStr.length(), rect1);
                    canvas1.drawText(finishStr, screenW / 2, screenH / 5, newPaint);

                    //finish bitmap
                    canvas1.drawBitmap(bitmap, MainSurfaceView.screenW / 2 - bitmap.getWidth() / 2, MainSurfaceView.screenH / 2 - bitmap.getHeight() / 2, paint);

                    //finish button
                    buttonBack.draw(canvas1, paint);
                }

                //draw bitmapCache on real canvas
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

    void checkCombination() {
        for (int i = 0; i < pieces.size(); i++) {
            if (i == chosenPieceIndex || !isPieceNeedPaint[i]) continue;
            if (pieces.get(chosenPieceIndex).isNeighbor(pieces.get(i)) && pieces.get(chosenPieceIndex).isCloseEnough(pieces.get(i))) {
                pieces.get(chosenPieceIndex).addPieceGroup(pieces.get(i));
                isPieceNeedPaint[i] = false;
                if (pieces.get(chosenPieceIndex).getAttachedPiece().size() == pieceCount - 1) {
                    isFinished = true;
                    if (this.pattern == CutUtil.type1) {
                        MainActivity.api.newResult(1, minute * 60 + second, response->{});
                    } else if (this.pattern == CutUtil.type2) {
                        MainActivity.api.newResult(2, minute * 60 + second, response->{});
                    }

                }
            }
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
        if (isFinished) buttonBack.onTouchEventPuzzle(event, activity);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                ListIterator<Integer> listIterator = drawOrder.listIterator(drawOrder.size());
                while (listIterator.hasPrevious()) {
                    int index = listIterator.previous();
                    if (!isPieceNeedPaint[index]) continue;
                    if (isPicked[index]) continue;
                    PuzzlePieceGroup piece = pieces.get(index);
                    if (piece.isInPiece(event.getX(), event.getY())) {
                        isChosen = true;
                        chosenPieceIndex = index;
                        touchX = event.getX();
                        touchY = event.getY();
                        biasX = (int) touchX - piece.getPosX();
                        biasY = (int) touchY - piece.getPosY();
                        if (!isSingle && isOnline) {
                            MainActivity.api.pick(index);
                            isPicked[index] = true;
                            pickedPieceIndex = index;
                        }
                        break;
                    }
                }

                break;
            case MotionEvent.ACTION_MOVE:
                if (isChosen) {
//                    piece.addDiff(event.getX() - touchX, event.getY() - touchY);
                    pieces.get(chosenPieceIndex).setPosX((int)event.getX() - biasX);
                    pieces.get(chosenPieceIndex).setPosY((int)event.getY() - biasY);
                    if (!isSingle && isOnline)
                        MainActivity.api.moveTo((event.getX() - biasX) / screenW, (event.getY() - biasY) / screenH);
//                    Log.d("dx:", String.valueOf(event.getX() - touchX));
//                    Log.d("dy:", String.valueOf(event.getY() - touchY));
                }
                break;
            case MotionEvent.ACTION_UP:
                if (isChosen) {
//                    updateOrderIndex = chosenPieceIndex;
                    needUpdate = true;
                    if (!isSingle && isOnline)
                        MainActivity.api.release();
                    isPicked[pickedPieceIndex] = false;
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
        spacing  = screenW / 10;
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

    public class Tuple {
        public int x, y;
        Tuple(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public void initPiecePos() {
        if (pieceCount == 9) {
            posList.add(new Tuple((screenW - 2*spacing - 3*pieceWidth)/2, (screenH - 2*spacing - 3*pieceHeight) / 2));
            posList.add(new Tuple((screenW - 2*spacing - 3*pieceWidth)/2 + pieceWidth + spacing, (screenH - 2*spacing - 3*pieceHeight) / 2));
            posList.add(new Tuple((screenW - 2*spacing - 3*pieceWidth)/2 + pieceWidth*2 + spacing*2, (screenH - 2*spacing - 3*pieceHeight) / 2));

            posList.add(new Tuple((screenW - 2*spacing - 3*pieceWidth)/2, (screenH - 2*spacing - 3*pieceHeight) / 2 + pieceHeight + spacing));
            posList.add(new Tuple((screenW - 2*spacing - 3*pieceWidth)/2 + pieceWidth + spacing, (screenH - 2*spacing - 3*pieceHeight) / 2 + pieceHeight + spacing));
            posList.add(new Tuple((screenW - 2*spacing - 3*pieceWidth)/2 + pieceWidth*2 + spacing*2, (screenH - 2*spacing - 3*pieceHeight) / 2 + pieceHeight + spacing));

            posList.add(new Tuple((screenW - 2*spacing - 3*pieceWidth)/2, (screenH - 2*spacing - 3*pieceHeight) / 2 + pieceHeight*2 + spacing*2));
            posList.add(new Tuple((screenW - 2*spacing - 3*pieceWidth)/2 + pieceWidth + spacing, (screenH - 2*spacing - 3*pieceHeight) / 2 + pieceHeight*2 + spacing*2));
            posList.add(new Tuple((screenW - 2*spacing - 3*pieceWidth)/2 + pieceWidth*2 + spacing*2, (screenH - 2*spacing - 3*pieceHeight) / 2 + pieceHeight*2 + spacing*2));
            Collections.shuffle(posList);
        } else if (pieceCount == 16) {
            posList.add(new Tuple((screenW - 3*spacing - 4*pieceWidth)/2, (screenH - 3*spacing - 4*pieceHeight) / 2));
            posList.add(new Tuple((screenW - 3*spacing - 4*pieceWidth)/2 + pieceWidth + spacing, (screenH - 3*spacing - 4*pieceHeight) / 2));
            posList.add(new Tuple((screenW - 3*spacing - 4*pieceWidth)/2 + pieceWidth*2 + spacing*2, (screenH - 3*spacing - 4*pieceHeight) / 2));
            posList.add(new Tuple((screenW - 3*spacing - 4*pieceWidth)/2 + pieceWidth*3 + spacing*3, (screenH - 3*spacing - 4*pieceHeight) / 2));

            posList.add(new Tuple((screenW - 3*spacing - 4*pieceWidth)/2, (screenH - 3*spacing - 4*pieceHeight) / 2  + pieceHeight + spacing));
            posList.add(new Tuple((screenW - 3*spacing - 4*pieceWidth)/2 + pieceWidth + spacing, (screenH - 3*spacing - 4*pieceHeight) / 2 + pieceHeight + spacing));
            posList.add(new Tuple((screenW - 3*spacing - 4*pieceWidth)/2 + pieceWidth*2 + spacing*2, (screenH - 3*spacing - 4*pieceHeight) / 2 + pieceHeight + spacing));
            posList.add(new Tuple((screenW - 3*spacing - 4*pieceWidth)/2 + pieceWidth*3 + spacing*3, (screenH - 3*spacing - 4*pieceHeight) / 2 + pieceHeight + spacing));

            posList.add(new Tuple((screenW - 3*spacing - 4*pieceWidth)/2, (screenH - 3*spacing - 4*pieceHeight) / 2  + pieceHeight*2 + spacing*2));
            posList.add(new Tuple((screenW - 3*spacing - 4*pieceWidth)/2 + pieceWidth + spacing, (screenH - 3*spacing - 4*pieceHeight) / 2 + pieceHeight*2 + spacing*2));
            posList.add(new Tuple((screenW - 3*spacing - 4*pieceWidth)/2 + pieceWidth*2 + spacing*2, (screenH - 3*spacing - 4*pieceHeight) / 2 + pieceHeight*2 + spacing*2));
            posList.add(new Tuple((screenW - 3*spacing - 4*pieceWidth)/2 + pieceWidth*3 + spacing*3, (screenH - 3*spacing - 4*pieceHeight) / 2 + pieceHeight*2 + spacing*2));

            posList.add(new Tuple((screenW - 3*spacing - 4*pieceWidth)/2, (screenH - 3*spacing - 4*pieceHeight) / 2  + pieceHeight*3 + spacing*3));
            posList.add(new Tuple((screenW - 3*spacing - 4*pieceWidth)/2 + pieceWidth + spacing, (screenH - 3*spacing - 4*pieceHeight) / 2 + pieceHeight*3 + spacing*3));
            posList.add(new Tuple((screenW - 3*spacing - 4*pieceWidth)/2 + pieceWidth*2 + spacing*2, (screenH - 3*spacing - 4*pieceHeight) / 2 + pieceHeight*3 + spacing*3));
            posList.add(new Tuple((screenW - 3*spacing - 4*pieceWidth)/2 + pieceWidth*3 + spacing*3, (screenH - 3*spacing - 4*pieceHeight) / 2 + pieceHeight*3 + spacing*3));

            Collections.shuffle(posList);
        }
    }
}
