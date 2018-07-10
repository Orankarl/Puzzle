package com.example.orankarl.puzzle;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class PuzzlePieceGroup {
    private PuzzlePiece mainPiece;
    private ArrayList<PuzzlePiece> attachedPiece;
    private int pieceWidth, pieceHeight;
    private int mainBiasX, mainBiasY;
    private ArrayList<Integer> biasListX, biasListY;
    private int mainID;
    private ArrayList<Integer> attachedID;
    private int rowCount;
    private int rotate = 0;
    private int horizontalLength, verticalLength, horizontalDelta, verticalDelta;
    private int deltaWidth, deltaHeight;
    PuzzlePieceGroup(PuzzlePiece piece, int mainID, int rowCount, int pieceWidth, int pieceHeight, int rotate) {
        mainPiece = piece;
        this.mainID = mainID;
        this.pieceWidth = pieceWidth;
        this.pieceHeight = pieceHeight;
        horizontalLength = pieceWidth;
        verticalLength = pieceHeight;
        attachedPiece = new ArrayList<>();
        biasListX = new ArrayList<>();
        biasListY = new ArrayList<>();
        attachedID = new ArrayList<>();
        this.rowCount = rowCount;
        mainBiasX = mainID % rowCount;
        mainBiasY = mainID / rowCount;
        this.rotate = rotate;

        setPosX(getPosX() - getHorizontalBias(mainID) * horizontalLength);
        setPosY(getPosY() - getVerticalBias(mainID) * verticalLength);

        deltaWidth = piece.getBitmap().getWidth() - pieceWidth;
        deltaHeight = piece.getBitmap().getHeight() - pieceHeight;
        horizontalDelta = deltaWidth;
        verticalDelta = deltaHeight;
    }
    boolean isInPiece(float x, float y) {
        if (mainPiece.isInPiece(x - getHorizontalBias(mainID) * horizontalLength, y - getVerticalBias(mainID) * verticalLength, rotate)) return true;
        for (int i = 0; i < attachedID.size(); i++) {
            PuzzlePiece piece = attachedPiece.get(i);
            if (piece.isInPiece(x - getHorizontalBias(attachedID.get(i)) * horizontalLength, y - getVerticalBias(attachedID.get(i)) * verticalLength, rotate)) return true;
        }
        return false;
    }

    void addPiece(PuzzlePiece piece, int id) {
        int biasX = id % rowCount - mainBiasX;
        int biasY = id / rowCount - mainBiasY;
        piece.setPosX(mainPiece.getPosX() + biasX);
        piece.setPosY(mainPiece.getPosY() + biasY);
        attachedPiece.add(piece);
        attachedID.add(id);
        biasListX.add(biasX);
        biasListY.add(biasY);
    }

    void addPieceGroup(PuzzlePieceGroup pieceGroup) {
        int id = pieceGroup.getMainID();
        int biasX = id % rowCount - mainBiasX;
        int biasY = id / rowCount - mainBiasY;
//        Log.d("mainID anotherMainID:", String.valueOf(mainID) + " " + String.valueOf(pieceGroup.mainID));
        PuzzlePiece mainPiece = pieceGroup.getMainPiece();
//        mainPiece.setPosX(mainPiece.getPosX() + biasX);
//        mainPiece.setPosY(mainPiece.getPosY() + biasY);
        if (pieceGroup.deltaHeight > deltaHeight) deltaHeight = pieceGroup.deltaHeight;
        if (deltaWidth < pieceGroup.deltaWidth) deltaWidth = pieceGroup.deltaWidth;
        updateLength();
        attachedPiece.add(mainPiece);
        attachedID.add(id);
        biasListX.add(biasX);
        biasListY.add(biasY);
        for (int i = 0; i < pieceGroup.attachedPiece.size(); i++) {
            PuzzlePiece tempPiece = pieceGroup.attachedPiece.get(i);
            int attachedBiasX = biasX +  pieceGroup.biasListX.get(i);
            int attachedBiasY = biasY + pieceGroup.biasListY.get(i);
//            tempPiece.setPosX(mainPiece.getPosX() + attachedBiasX);
//            tempPiece.setPosY(mainPiece.getPosY() + attachedBiasY);
            attachedPiece.add(tempPiece);
            attachedID.add(pieceGroup.attachedID.get(i));
            biasListX.add(attachedBiasX);
            biasListY.add(attachedBiasY);
//            Log.d("biasX biasY:", String.valueOf(attachedBiasX) + " " + String.valueOf(attachedBiasY));
        }
    }

    Canvas draw(Canvas canvas, Paint paint) {
//        if (rotate == 0) {
//            canvas.drawBitmap(mainPiece.getBitmap(), mainPiece.getPosX(), mainPiece.getPosY(), paint);
//            for (int i = 0; i < attachedPiece.size(); i++) {
//                canvas.drawBitmap(attachedPiece.get(i).getBitmap(), mainPiece.getPosX()+biasListX.get(i), mainPiece.getPosY()+biasListY.get(i), paint);
//            }
//        } else {
            Matrix matrix = new Matrix();
            matrix.setRotate(90*rotate);
            int biasX = getHorizontalBias(mainID) * horizontalLength;
            int biasY = getVerticalBias(mainID) * verticalLength;

//            Log.d("biasY ", String.valueOf(biasY));
//            Log.d("rotate", String.valueOf(rotate));

            if (getHorizontalBias(mainID) == 0 && (rotate == 1 || rotate == 2)) {
                biasX += horizontalDelta;
//                Log.d("biasX+", String.valueOf(biasX));
            }
            if (getVerticalBias(mainID) == 0 && (rotate == 2 || rotate == 3)) {
                biasY += verticalDelta;
//                Log.d("biasY+", String.valueOf(biasY));
            }
            Bitmap bitmap = Bitmap.createBitmap(mainPiece.getBitmap(), 0, 0, mainPiece.getBitmap().getWidth(), mainPiece.getBitmap().getHeight(), matrix, true);
            canvas.drawBitmap(bitmap, mainPiece.getPosX() + biasX, mainPiece.getPosY() + biasY, paint);
            for (int i = 0; i < attachedPiece.size(); i++) {
                biasX = getHorizontalBias(attachedID.get(i)) * horizontalLength;
                biasY = getVerticalBias(attachedID.get(i)) * verticalLength;
//                Log.d("biasY ", String.valueOf(biasY));
//                Log.d("rotate", String.valueOf(rotate));
                if (getHorizontalBias(attachedID.get(i)) == 0 && (rotate == 1 || rotate == 2)) {
                    biasX += horizontalDelta;
//                    Log.d("biasX+", String.valueOf(biasX));
                }
                if (getVerticalBias(attachedID.get(i)) == 0 && (rotate == 2 || rotate == 3)) {
                    biasY += verticalDelta;
//                    Log.d("biasY+", String.valueOf(biasY));
                }
                bitmap = Bitmap.createBitmap(attachedPiece.get(i).getBitmap(), 0, 0, attachedPiece.get(i).getBitmap().getWidth(), attachedPiece.get(i).getBitmap().getHeight(), matrix, true);
                canvas.drawBitmap(bitmap, mainPiece.getPosX() + biasX, mainPiece.getPosY() + biasY, paint);
            }
//        }

        return canvas;
    }

    public void setRotate(int rotate) {
        while(this.rotate != rotate) {
            this.rotate = (this.rotate+1) % 4;
            rotate90();
        }
    }

    void rotate90() {
        int oldHorizontalBias = getHorizontalBias(mainID) * horizontalLength;
        int oldVerticalBias = getVerticalBias(mainID) * verticalLength;
        rotate = (rotate + 1) % 4;
        updateLength();
        setPosX(getPosX() + oldHorizontalBias  - getHorizontalBias(mainID) * horizontalLength);
        setPosY(getPosY() + oldVerticalBias - getVerticalBias(mainID) * verticalLength);
        Log.d("biasX:", String.valueOf(getHorizontalBias(mainID) * horizontalLength));
        Log.d("biasY:", String.valueOf(getVerticalBias(mainID) * verticalLength));
    }

    void updateLength() {
        if (rotate % 2 == 0) {
            horizontalLength = pieceWidth;
            verticalLength = pieceHeight;
            horizontalDelta = deltaWidth;
            verticalDelta = deltaHeight;
        } else {
            horizontalLength = pieceHeight;
            verticalLength = pieceWidth;
            horizontalDelta = deltaHeight;
            verticalDelta = deltaWidth;
        }
        Log.d("h&v length", String.valueOf(horizontalLength) + " " + String.valueOf(verticalLength));
        Log.d("h&v delta", String.valueOf(horizontalDelta) + " " + String.valueOf(verticalDelta));
    }

    boolean isNeighbor(PuzzlePieceGroup pieceGroup) {
        if (rotate != pieceGroup.rotate) return false;
        ArrayList<Integer> selfAttachedID = getAttachedID();
        selfAttachedID.add(mainID);
        ArrayList<Integer> anotherAttachedID = pieceGroup.getAttachedID();
        anotherAttachedID.add(pieceGroup.mainID);

        for (int a:selfAttachedID) {
            for (int b:anotherAttachedID) {
                int i = a, j = b;
                if (i > j) {
                    int temp = i;
                    i = j;
                    j = temp;
                }

                if (j-i == rowCount) {
//                    Log.d("j i:", String.valueOf(j) + " " +String.valueOf(i));
//                    String str1 = "";
//                    for (int k:selfAttachedID) {
//                        str1 += String.valueOf(k) + " ";
//                    }
//                    String str2 = "";
//                    for (int k:anotherAttachedID) {
//                        str2 += String.valueOf(k) + " ";
//                    }
//                    Log.d("selfAttachedID:", str1);
//                    Log.d("anotherAttachedID:", str2);
//                    Log.d("mainID pG.mainID:", String.valueOf(mainID) + " " + String.valueOf(pieceGroup.mainID));
                    return true;
                }
                if (j-i == 1) {
                    if (j % rowCount != 0) {
//                        Log.d("j i:", String.valueOf(j) + " " +String.valueOf(i));
//                        Log.d("j i:", String.valueOf(j) + " " +String.valueOf(i));
//                        String str1 = "";
//                        for (int k:selfAttachedID) {
//                            str1 += String.valueOf(k) + " ";
//                        }
//                        String str2 = "";
//                        for (int k:anotherAttachedID) {
//                            str2 += String.valueOf(k) + " ";
//                        }
//                        Log.d("selfAttachedID:", str1);
//                        Log.d("anotherAttachedID:", str2);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    boolean isCloseEnough(PuzzlePieceGroup pieceGroup) {
        double closeRatio = 0.2;
        int selfBiasX = (mainID % rowCount) * pieceWidth;
        int selfBiasY = (mainID / rowCount) * pieceHeight;
        int biasX = (pieceGroup.getMainID() % rowCount) * pieceWidth;
        int biasY = (pieceGroup.getMainID() / rowCount) * pieceHeight;
        int realBiasX1, realBiasY1, realBiasX2, realBiasY2;
        switch (rotate) {
            case 0:
                realBiasX1 = mainBiasX;
                realBiasY1 = mainBiasY;
                realBiasX2 = pieceGroup.mainBiasX;
                realBiasY2 = pieceGroup.mainBiasY;
                break;
            case 1:
                realBiasX1 = rowCount - mainBiasY;
                realBiasY1 = mainBiasX;
                realBiasX2 = rowCount - pieceGroup.mainBiasY;
                realBiasY2 = pieceGroup.mainBiasX;
                break;
            case 2:
                realBiasX1 = rowCount - mainBiasX;
                realBiasY1 = rowCount - mainBiasY;
                realBiasX2 = rowCount - pieceGroup.mainBiasX;
                realBiasY2 = rowCount - pieceGroup.mainBiasY;
                break;
            case 3:
                realBiasX1 = mainBiasY;
                realBiasY1 = rowCount - mainBiasX;
                realBiasX2 = pieceGroup.mainBiasY;
                realBiasY2 = rowCount - pieceGroup.mainBiasX;
                break;
            default:
                realBiasX1 = mainBiasX;
                realBiasY1 = mainBiasY;
                realBiasX2 = pieceGroup.mainBiasX;
                realBiasY2 = pieceGroup.mainBiasY;
        }
//        if (Math.abs((getPosX() - realBiasX1 * horizontalLength) - (pieceGroup.getPosX() - realBiasX2 * horizontalLength)) < closeRatio * horizontalLength
//                && Math.abs((getPosY() - realBiasY1 * verticalLength) - (pieceGroup.getPosY() - realBiasY2 * verticalLength)) < closeRatio * verticalLength) {
////            Log.d("id of 2 pieces:", String.valueOf(mainID) + " " + String.valueOf(pieceGroup.mainID));
////            Log.d("posX,posY,biasX,biasY:", String.valueOf(getPosX()) + " " + String.valueOf(getPosY()) + " " + String.valueOf(mainBiasX) + " " + String.valueOf(mainBiasY));
////            Log.d("posX,posY,biasX,biasY:", String.valueOf(pieceGroup.getPosX()) + " " + String.valueOf(pieceGroup.getPosY())
////                    + " " + String.valueOf(pieceGroup.mainBiasX) + " " + String.valueOf(pieceGroup.mainBiasY));
////            Log.d("pieceWidth Height:", String.valueOf(pieceWidth) + " " + String.valueOf(pieceHeight));
////            Log.d("value1:", String.valueOf(Math.abs((getPosX() - mainBiasX) - (pieceGroup.getPosX() - pieceGroup.mainBiasX))));
////            Log.d("value2:", String.valueOf(Math.abs((getPosY() - mainBiasY) - (pieceGroup.getPosY() - pieceGroup.mainBiasY))));
//            return true;
//        }
        if (Math.abs((getPosX()) - (pieceGroup.getPosX())) < closeRatio * horizontalLength
                && Math.abs((getPosY()) - (pieceGroup.getPosY())) < closeRatio * verticalLength) {
//            Log.d("id of 2 pieces:", String.valueOf(mainID) + " " + String.valueOf(pieceGroup.mainID));
//            Log.d("posX,posY,biasX,biasY:", String.valueOf(getPosX()) + " " + String.valueOf(getPosY()) + " " + String.valueOf(mainBiasX) + " " + String.valueOf(mainBiasY));
//            Log.d("posX,posY,biasX,biasY:", String.valueOf(pieceGroup.getPosX()) + " " + String.valueOf(pieceGroup.getPosY())
//                    + " " + String.valueOf(pieceGroup.mainBiasX) + " " + String.valueOf(pieceGroup.mainBiasY));
//            Log.d("pieceWidth Height:", String.valueOf(pieceWidth) + " " + String.valueOf(pieceHeight));
//            Log.d("value1:", String.valueOf(Math.abs((getPosX() - mainBiasX) - (pieceGroup.getPosX() - pieceGroup.mainBiasX))));
//            Log.d("value2:", String.valueOf(Math.abs((getPosY() - mainBiasY) - (pieceGroup.getPosY() - pieceGroup.mainBiasY))));
            return true;
        }
        return false;
    }

    int getHorizontalBias(int id) {
        int biasX = id % rowCount;
        int biasY = id / rowCount;
        int realBiasX, realBiasY;
        switch (rotate) {
            case 0:
                realBiasX = biasX;
                realBiasY = biasY;
                break;
            case 1:
                realBiasX = rowCount - biasY - 1;
                break;
            case 2:
                realBiasX = rowCount - biasX - 1;
                break;
            case 3:
                realBiasX = biasY;
                break;
            default:
                realBiasX = biasX;
        }
        return realBiasX;
    }

    int getVerticalBias(int id) {
        int biasX = id % rowCount;
        int biasY = id / rowCount;
        int realBiasX, realBiasY;
        switch (rotate) {
            case 0:
                realBiasY = biasY;
                break;
            case 1:
                realBiasY = biasX;
                break;
            case 2:
                realBiasY = rowCount - biasY - 1;
                break;
            case 3:
                realBiasY = rowCount - biasX - 1;
                break;
            default:
                realBiasY = biasY;
        }
        return realBiasY;
    }

    public int getRowCount() {
        return rowCount;
    }

    void setPosX(int x) {
        mainPiece.setPosX(x);
        for (int i = 0; i < attachedPiece.size(); i++) {
            attachedPiece.get(i).setPosX(x);
        }
    }
    void setPosY(int y) {
        mainPiece.setPosY(y);
        for (int i = 0; i < attachedPiece.size(); i++) {
            attachedPiece.get(i).setPosY(y);
        }
    }
    int getPosX() {
        return mainPiece.getPosX();
    }
    int getPosY() {
        return mainPiece.getPosY();
    }

    public PuzzlePiece getMainPiece() {
        return mainPiece;
    }

    public ArrayList<PuzzlePiece> getAttachedPiece() {
        return attachedPiece;
    }

    public int getMainID() {
        return mainID;
    }

    public ArrayList<Integer> getAttachedID() {
        ArrayList<Integer> newList = new ArrayList<>(Arrays.asList(new Integer[attachedID.size()]));
        Collections.copy(newList, attachedID);
        return newList;
    }

    public int getRotate() {
        return rotate;
    }
}
