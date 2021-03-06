package com.example.orankarl.puzzle;

import android.app.job.JobInfo;
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
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.net.ResponseCache;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Random;

public class PuzzleSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private Context context;
    private SurfaceHolder holder;
    private Paint paint, alphaPaint;
    private Canvas canvas;
    private Bitmap bitmap;
    private ArrayList<PuzzlePieceGroup> pieces = new ArrayList<>();
    private boolean isChosen = false, needUpdate = false, isSingle, isOnline, isFinished = false, needOnMoveUpdate = false;
    private int chosenPieceIndex, pickedPieceIndex;
    float touchX = 0, touchY = 0;
    int biasX = 0, biasY = 0;
    LinkedList<Integer> drawOrder = new LinkedList<>();
    int pieceCount = 9, rowCount = 3;
    int pieceWidth, pieceHeight;
    int spacing;
    boolean[] isPieceNeedPaint, isPicked;
    ArrayList<Tuple> posList = new ArrayList<>();
    ArrayList<Integer> rotateList = new ArrayList<>();
    String pattern = CutUtil.type1, timeStr;
    long startTime;
    int minute, second;
    MenuButton buttonBack;
    double RATIO;
    ArrayList<Integer> posIndexList;
    int lastIndex;
    long lastDownTime;
    HashMap<String, Integer> pickedMap = new HashMap<>();

    PuzzleActivity activity = (PuzzleActivity) getContext();

    public static int screenW, screenH;
    private Resources resources = this.getResources();
    boolean flag = true;

    public PuzzleSurfaceView(Context context, Bitmap bitmap, int pattern, int split, boolean isSingle, boolean isOnline, ArrayList<Integer> posIndexList, ArrayList<Integer> rotateList) {
        super(context);
        holder = this.getHolder();
        holder.addCallback(this);
        paint = new Paint();
        paint.setAntiAlias(true);
        alphaPaint = new Paint();
        alphaPaint.setAntiAlias(true);
        alphaPaint.setAlpha(60);
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
        if (isOnline && !isSingle) {
            this.posIndexList = posIndexList;
            this.rotateList = rotateList;
        }
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
//                pickedPieceIndex = pickResponse.pieceIndex;
                Log.d("onPick", String.valueOf(pickedPieceIndex));
                pickedMap.put(pickResponse.username, pickResponse.pieceIndex);
            });

            MainActivity.api.onMoveTo(response -> {
                int index = pickedMap.get(response.username);
                pieces.get(index).setPosX((int)(screenW * response.X));
                pieces.get(index).setPosY((int)(screenH * response.Y));

//                Log.d("onMoveTo x&y", String.valueOf(response.X) + " " + String.valueOf(response.Y));

//                checkCombination();

            });

            MainActivity.api.onRelease(response -> {
                if (!pickedMap.containsKey(response.username)) {
                    return;
                }
                int index = pickedMap.get(response.username);
                isPicked[index] = false;
                pickedMap.remove(response.username);
                Log.d("onRelease", String.valueOf(pickedPieceIndex));
                checkOnMoveCombination(index);
//                needOnMoveUpdate = true;
//                pickedPieceIndex = -1;
            });

            MainActivity.api.onRotate(response ->{
//                pieces.get(response.pieceIndex).rotate90();
                Log.d("onRotate", String.valueOf(response.pieceIndex) + " " + String.valueOf(response.angle));
                pieces.get(response.pieceIndex).setRotate(response.angle);
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
//        Log.d("height", String.valueOf(options.outHeight))

        pieceWidth = bitmap.getWidth() / rowCount;
        pieceHeight = bitmap.getHeight() / rowCount;

        initPiecePos();

        ArrayList<Bitmap> cutImages = CutUtil.cutImage(bitmap, pattern, pieceCount);
        if (isSingle) Collections.shuffle(posList);
        if (isSingle) {
            Random random = new Random();
            rotateList.clear();
            for (int i = 0; i < pieceCount; i++) {
                rotateList.add(random.nextInt(4));
            }
        }
        for (int i = 0; i < pieceCount; i++) {
//                PuzzlePieceGroup pieceGroup = ;
            if (!isSingle && isOnline) {
                pieces.add(new PuzzlePieceGroup(new PuzzlePiece(cutImages.get(i), posList.get(posIndexList.get(i)).x, posList.get(posIndexList.get(i)).y), i, rowCount, pieceWidth, pieceHeight, rotateList.get(i)));
            } else {
                pieces.add(new PuzzlePieceGroup(new PuzzlePiece(cutImages.get(i), posList.get(i).x, posList.get(i).y), i, rowCount, pieceWidth, pieceHeight, rotateList.get(i)));
            }

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
                Bitmap bitmapCache = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);

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
//                if (needOnMoveUpdate) {
//                    checkCombination();
//                    Iterator<Integer> iterator = drawOrder.iterator();
//                    while(iterator.hasNext()) {
//                        int temp = iterator.next();
//                        if (temp == pickedPieceIndex) {
//                            iterator.remove();
//                        }
//                    }
//                    drawOrder.offer(pickedPieceIndex);
//                    needOnMoveUpdate = false;
//                }
                for (int index : drawOrder) {
                    if (isPieceNeedPaint[index] && !isFinished) {
                        if (isPicked[index]) {
                            canvas1 = pieces.get(index).draw(canvas1, alphaPaint);
                        } else {
                            canvas1 = pieces.get(index).draw(canvas1, paint);
                        }
                    }
                }
//                if (!isSingle && isOnline && isPicked[pickedPieceIndex] && !isFinished) {
//                    canvas1 = pieces.get(pickedPieceIndex).draw(canvas1, alphaPaint);
//                }
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
                    //finish bitmap
                    canvas1.drawBitmap(bitmap, MainSurfaceView.screenW / 2 - bitmap.getWidth() / 2, MainSurfaceView.screenH / 2 - bitmap.getHeight() / 2, paint);

                    //finish button
                    buttonBack.draw(canvas1, paint);

                    //finish text
                    String finishStr = "完成";
                    Paint newPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                    newPaint.setColor(Color.BLACK);
                    newPaint.setTextSize((float)RATIO*50);
                    newPaint.setTextAlign(Paint.Align.CENTER);

                    Rect rect1 = new Rect();
                    newPaint.getTextBounds(finishStr, 0, finishStr.length(), rect1);
                    canvas1.drawText(finishStr, screenW / 2, screenH / 5, newPaint);
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
                int mainPiece = Math.min(chosenPieceIndex, i);
                int attachedPiece = Math.max(chosenPieceIndex, i);
                pieces.get(mainPiece).addPieceGroup(pieces.get(attachedPiece));
                isPieceNeedPaint[attachedPiece] = false;
                logPieceCount();
//                    pieces.get(chosenPieceIndex).addPieceGroup(pieces.get(i));
//                    isPieceNeedPaint[i] = false;
                if (pieces.get(mainPiece).getAttachedPiece().size() == pieceCount - 1) {
                    isFinished = true;
//                        Log.d("needUpdate isFinished", String.valueOf(mainPiece) + " " + String.valueOf(pieces.get(mainPiece).getAttachedPiece().size()));
                    int pattern, split;
                    if (this.pattern.equals(CutUtil.type1)) {
                        pattern = 1;
                    } else if (this.pattern.equals(CutUtil.type2)) {
                        pattern = 2;
                    } else {
                        pattern = 1;
                    }
                    if (this.pieceCount == 9) split = 1;
                    else split = 2;
                    MainActivity.api.newResult(pattern, split, minute * 60 + second, response -> {
                    });
                }
                break;
            }
        }
    }

    void checkOnMoveCombination(int index) {
        for (int i = 0; i < pieces.size(); i++) {
            if (i == index || !isPieceNeedPaint[i]) continue;
            Log.d("checkOnMove", String.valueOf(index) + " " + String.valueOf(i));
            if (pieces.get(index).isNeighbor(pieces.get(i)) && pieces.get(index).isCloseEnough(pieces.get(i))) {
                int mainPiece = Math.min(index, i);
                int attachedPiece = Math.max(index, i);
                pieces.get(mainPiece).addPieceGroup(pieces.get(attachedPiece));
                isPieceNeedPaint[attachedPiece] = false;
                logPieceCount();
//                    pieces.get(chosenPieceIndex).addPieceGroup(pieces.get(i));
//                    isPieceNeedPaint[i] = false;
                if (pieces.get(mainPiece).getAttachedPiece().size() == pieceCount - 1) {
                    isFinished = true;
//                        Log.d("needUpdate isFinished", String.valueOf(mainPiece) + " " + String.valueOf(pieces.get(mainPiece).getAttachedPiece().size()));
                    int pattern, split;
                    if (this.pattern.equals(CutUtil.type1)) {
                        pattern = 1;
                    } else if (this.pattern.equals(CutUtil.type2)) {
                        pattern = 2;
                    } else {
                        pattern = 1;
                    }
                    if (this.pieceCount == 9) split = 1;
                    else split = 2;
                    MainActivity.api.newResult(pattern, split, minute * 60 + second, response -> {
                    });
                }
                break;
            }
        }
    }

    void logPieceCount() {
        String string = "";
        for (int i =0; i < pieceCount; i++) {
            if (isPieceNeedPaint[i]) {
                string += String.valueOf(i) + " " + String.valueOf(pieces.get(i).getAttachedPiece()) + ";";
            }
        }
        Log.d("pieceCount", string);
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
        if (isFinished) {
            buttonBack.onTouchEventPuzzle(event, activity);
            return true;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                long currentDownTime = event.getDownTime();
                ListIterator<Integer> listIterator = drawOrder.listIterator(drawOrder.size());
                while (listIterator.hasPrevious()) {
                    int index = listIterator.previous();
                    if (!isPieceNeedPaint[index]) continue;
                    if (isPicked[index]) continue;
                    PuzzlePieceGroup piece = pieces.get(index);
                    if (piece.isInPiece(event.getX(), event.getY())) {
                        if (lastIndex == index && currentDownTime - lastDownTime < 300) {
                            pieces.get(index).rotate90();
                            MainActivity.api.rotate(index, pieces.get(index).getRotate());

                            lastDownTime = currentDownTime;
                            break;
                        }
                        isChosen = true;
                        chosenPieceIndex = index;
                        touchX = event.getX();
                        touchY = event.getY();
                        biasX = (int) touchX - piece.getPosX();
                        biasY = (int) touchY - piece.getPosY();
                        if (!isSingle && isOnline && !isPicked[index]) {
                            MainActivity.api.pick(index);
                            isPicked[index] = true;
                            pickedPieceIndex = index;
                        }
                        lastDownTime = currentDownTime;
                        lastIndex = index;
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
                    if (!isSingle && isOnline) {
                        if (pickedPieceIndex == chosenPieceIndex)
                            MainActivity.api.release();
//                        if (isPicked[pickedPieceIndex])
//                            MainActivity.api.release();
                        isPicked[pickedPieceIndex] = false;
//                        pickedPieceIndex = -1;
                    }

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
                if (end - start < 16) {
                    Thread.sleep(16 - (end - start));
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
        Thread thread = new Thread(this);
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
//            Collections.shuffle(posList);
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

//            Collections.shuffle(posList);
        }
    }
}
