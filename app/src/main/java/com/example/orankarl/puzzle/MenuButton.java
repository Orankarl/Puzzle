package com.example.orankarl.puzzle;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.Toast;

import java.util.prefs.PreferenceChangeEvent;

public class MenuButton {
    private Context context;
    private Bitmap bmpBackground, bmpButton, bmpButtonPressed;
    private int posX, posY;
    private boolean isPressed;

    public MenuButton(Context context, Bitmap bmpButton, Bitmap bmpButtonPressed, int x, int y) {
        this.context = context;
        this.bmpButton = bmpButton;
        this.bmpButtonPressed = bmpButtonPressed;
        this.posX = x;
        this.posY = y;
        isPressed = false;
    }
    public MenuButton(Bitmap bmpBackground, Bitmap bmpButton, Bitmap bmpButtonPressed, int x, int y) {
        this.bmpBackground = bmpBackground;
        this.bmpButton = bmpButton;
        this.bmpButtonPressed = bmpButtonPressed;
        this.posX = x;
        this.posY = y;
        isPressed = false;
    }

    public void draw(Canvas canvas, Paint paint) {
//        canvas.drawBitmap(bmpBackground, 0, 0, paint);
        if (isPressed) {
            canvas.drawBitmap(bmpButtonPressed, posX, posY, paint);
        } else {
            canvas.drawBitmap(bmpButton, posX, posY, paint);
        }
    }

    public void onTouchEvent(MotionEvent event, int whichClick) {
        // 获取当前触控位置
        int pointX = (int) event.getX();
        int pointyY = (int) event.getY();

        // 当用户是按下和移动时
        if (event.getAction() == MotionEvent.ACTION_DOWN
                || event.getAction() == MotionEvent.ACTION_MOVE) {

            // 判定用户是否点击按钮
            if (pointX > posX && pointX < posX + bmpButton.getWidth()) {
                if (pointyY > posY && pointyY < posY + bmpButton.getHeight()) {
                    isPressed = true;
                    Log.d("StartButton", "DOWN");
                } else {
                    isPressed = false;
                }
            } else {
                isPressed = false;
            }

            // 当用于是抬起动作时
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            // 判断抬起时是否点击按钮,防止用户移动到别处
            if (pointX > posX && pointX < posX + bmpButton.getWidth()) {
                if (pointyY > posY && pointyY < posY + bmpButton.getHeight()) {
                    isPressed = false;//抬起后重置 还原Button状态为未按下状态
                    Log.d("StartButton", "UP");
                    MainActivity activity = (MainActivity) context;
                    //activity.onButtonPressed();
                    switch (whichClick) {
                        case 0:
                            activity.onButtonPressed();
                            break;
                        case 1:
                            activity.onChoosePictureButtonPressed();
                            break;
                        case 2:
                            activity.onBeginButtonPressed();
                            break;
                        case 3:
                            activity.onLoginButtonPressed();
                            break;
                        case 4:
                            activity.onRegistButtonPressed();
                            break;
                    }
                }
            }
        }

    }
}
