package com.example.orankarl.puzzle;

import android.graphics.Bitmap;
import android.view.MotionEvent;

public class PuzzlePiece {
    private Bitmap bitmap;
    private int posX, posY;
    private int width, height;
    PuzzlePiece(Bitmap bitmap, int posX, int posY) {
        this.bitmap = bitmap;
        this.posX = posX;
        this.posY = posY;
        this.width = bitmap.getWidth();
        this.height = bitmap.getHeight();
    }
    boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();
        if (touchX < posX || touchX > posX + width || touchY < posY || touchY > posY + height) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:

        }
        return true;
    }
    boolean isInPiece(float x, float y) {
        return !(x < posX) && !(x > posX + width) && !(y < posY) && !(y > posY + height);
    }
    void addDiff(float x, float y) {
        posX += x;
        posY += y;
    }
    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        this.width = bitmap.getWidth();
        this.height = bitmap.getHeight();
    }

    public int getPosX() {
        return posX;
    }

    public void setPosX(int posX) {
        this.posX = posX;
    }

    public int getPosY() {
        return posY;
    }

    public void setPosY(int posY) {
        this.posY = posY;
    }
}
