package com.example.orankarl.puzzle;

import android.graphics.Bitmap;
import android.graphics.Canvas;
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
    PuzzlePieceGroup(PuzzlePiece piece, int mainID, int rowCount, int pieceWidth, int pieceHeight) {
        mainPiece = piece;
        this.mainID = mainID;
        this.pieceWidth = pieceWidth;
        this.pieceHeight = pieceHeight;
        attachedPiece = new ArrayList<>();
        biasListX = new ArrayList<>();
        biasListY = new ArrayList<>();
        attachedID = new ArrayList<>();
        this.rowCount = rowCount;
        mainBiasX = (mainID % rowCount) * pieceWidth;
        mainBiasY = (mainID / rowCount) * pieceHeight;
    }
    boolean isInPiece(float x, float y) {
        if (mainPiece.isInPiece(x, y)) return true;
        for (PuzzlePiece piece:attachedPiece) {
            if (piece.isInPiece(x, y)) return true;
        }
        return false;
    }

    void addPiece(PuzzlePiece piece, int id) {
        int biasX = (id % rowCount) * pieceWidth - mainBiasX;
        int biasY = (id / rowCount) * pieceHeight - mainBiasY;
        piece.setPosX(mainPiece.getPosX() + biasX);
        piece.setPosY(mainPiece.getPosY() + biasY);
        attachedPiece.add(piece);
        attachedID.add(id);
        biasListX.add(biasX);
        biasListY.add(biasY);
    }

    void addPieceGroup(PuzzlePieceGroup pieceGroup) {
        int id = pieceGroup.getMainID();
        int biasX = (id % rowCount) * pieceWidth - mainBiasX;
        int biasY = (id / rowCount) * pieceHeight - mainBiasY;
//        Log.d("mainID anotherMainID:", String.valueOf(mainID) + " " + String.valueOf(pieceGroup.mainID));
        PuzzlePiece mainPiece = pieceGroup.getMainPiece();
//        mainPiece.setPosX(mainPiece.getPosX() + biasX);
//        mainPiece.setPosY(mainPiece.getPosY() + biasY);
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
        canvas.drawBitmap(mainPiece.getBitmap(), mainPiece.getPosX(), mainPiece.getPosY(), paint);
        for (int i = 0; i < attachedPiece.size(); i++) {
            canvas.drawBitmap(attachedPiece.get(i).getBitmap(), mainPiece.getPosX()+biasListX.get(i), mainPiece.getPosY()+biasListY.get(i), paint);
        }
        return canvas;
    }

    boolean isNeighbor(PuzzlePieceGroup pieceGroup) {
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
        double closeRatio = 0.1;
        int selfBiasX = (mainID % rowCount) * pieceWidth;
        int selfBiasY = (mainID / rowCount) * pieceHeight;
        int biasX = (pieceGroup.getMainID() % rowCount) * pieceWidth;
        int biasY = (pieceGroup.getMainID() / rowCount) * pieceHeight;
        if (Math.abs((getPosX() - mainBiasX) - (pieceGroup.getPosX() - pieceGroup.mainBiasX)) < closeRatio * pieceWidth
                && Math.abs((getPosY() - mainBiasY) - (pieceGroup.getPosY() - pieceGroup.mainBiasY)) < closeRatio * pieceHeight) {
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

    public int getRowCount() {
        return rowCount;
    }

    void setPosX(int x) {
        mainPiece.setPosX(x);
        for (int i = 0; i < attachedPiece.size(); i++) {
            attachedPiece.get(i).setPosX(x + biasListX.get(i));
        }
    }
    void setPosY(int y) {
        mainPiece.setPosY(y);
        for (int i = 0; i < attachedPiece.size(); i++) {
            attachedPiece.get(i).setPosY(y + biasListY.get(i));
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
}
