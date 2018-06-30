package com.example.orankarl.puzzle;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;

import java.util.ArrayList;

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
        mainBiasY = (mainID % rowCount) * pieceHeight;
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
        PuzzlePiece mainPiece = pieceGroup.getMainPiece();
        mainPiece.setPosX(mainPiece.getPosX() + biasX);
        mainPiece.setPosY(mainPiece.getPosY() + biasY);
        attachedPiece.add(mainPiece);
        attachedID.add(id);
        biasListX.add(biasX);
        biasListY.add(biasY);
        for (int i = 0; i < pieceGroup.attachedPiece.size(); i++) {
            PuzzlePiece tempPiece = pieceGroup.attachedPiece.get(i);
            int attachedBiasX = biasX +  pieceGroup.biasListX.get(i);
            int attachedBiasY = biasY + pieceGroup.biasListY.get(i);
            tempPiece.setPosX(mainPiece.getPosX() + attachedBiasX);
            tempPiece.setPosY(mainPiece.getPosY() + attachedBiasY);
            attachedPiece.add(tempPiece);
            attachedID.add(pieceGroup.attachedID.get(i));
            biasListX.add(attachedBiasX);
            biasListY.add(attachedBiasY);
        }
    }

    Canvas draw(Canvas canvas, Paint paint) {
        canvas.drawBitmap(mainPiece.getBitmap(), mainPiece.getPosX(), mainPiece.getPosY(), paint);
        for (int i = 0; i < attachedPiece.size(); i++) {
            canvas.drawBitmap(attachedPiece.get(i).getBitmap(), mainPiece.getPosX()+biasListX.get(i), mainPiece.getPosY()+biasListY.get(i), paint);
        }
        return canvas;
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
        return attachedID;
    }
}
