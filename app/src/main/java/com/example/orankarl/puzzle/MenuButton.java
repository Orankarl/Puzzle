package com.example.orankarl.puzzle;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;

import static com.example.orankarl.puzzle.MainSurfaceView.*;

/*
    A menu button designed for being represented in Custom SurfaceView
    1. Change image when being pressed
    2. Able to react to touch events
    3. Save the context of Activity for function calls
 */

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
                        case 0:    //MainSurface 开始（单人） & MainSurface2 开始（单人）
                            isSingle = true;
                            activity.onBeginButtonPressed();
                            break;
                        case 1:    //MainSurface 登录
                            activity.onLogButtonPressed();
                            break;
                        case 2:    //MainSurface 排行榜 & MainSurface2 排行榜
                            isRank = true;
                            activity.onRankButtonPressed();
                            break;
                        case 3:    //Login 登录
                            activity.onLoginButtonPressed();
                            break;
                        case 4:    //Login 注册
                            activity.onRegisterButtonPressed();
                            break;
                        case 5:    //MainSurface2 开始（多人）
                            isSingle = false;
                            activity.onMultiButtonPressed();
                            break;
                        case 6:    //MainSurface2 注销
                            activity.onLogoutButtonPressed();
                            break;
                        case 7:    //ChoosePicture 选择图片
                            activity.onChoosePictureButtonPressed();
                            break;
                        case 8:    //ChoosePicture 确定，下一步
                            isRank = false;
                            activity.onChoosePatternPressed();
                            break;
                        case 9:    //Room 创建房间
                            activity.onCreateRoomPressed();
                            break;
                        case 10:    //Room 加入房间
                            //activity.onJoinRoomPressed();
                            break;
                        case 11:    //Room 返回
                            activity.backToMainSurfaceView2();
                            break;
                        case 12:    //ChoosePattern 样式1
                            pattern = 1;
                            activity.onChooseSplitPressed();
                            break;
                        case 13:    //ChoosePattern 样式2
                            pattern = 2;
                            activity.onChooseSplitPressed();
                            break;
                        case 14:    //ChoosePattern 返回
                            if (!isOnline){
                                activity.backToMainSurfaceView();
                                break;
                            }
                            else if (isRank || isSingle) {
                                activity.backToMainSurfaceView2();
                                break;
                            }
                            else {
                                activity.backToRoomView();
                                break;
                            }
                        case 15:    //ChooseSplit 分割1
                            split = 1;
                            if (isRank){
                                activity.showRank();
                                break;
                            }
                            else if (isSingle){
                                activity.gameStart();
                                break;
                            }
                            else {
                                //activity.hostInRoom();
                                break;
                            }
                        case 16:    //ChooseSplit 分割2
                            split = 2;
                            if (isRank){
                                activity.showRank();
                                break;
                            }
                            else if (isSingle){
                                activity.gameStart();
                                break;
                            }
                            else {
                                //activity.hostInRoom();
                                break;
                            }
                        case 17:    //ChooseSplit 返回
                            activity.backToChoosePatternView();
                            break;
                        case 18:    //Rank 返回
                            activity.backToChooseSplitView();
                            break;
                    }
                }
            }
        }

    }
}
