package com.example.orankarl.puzzle;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;

import java.io.IOException;
import java.io.InputStream;

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

    final public static int PRESET_OFFSET = 100;

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
                            activity.onSingleBeginButtonPressed();
                            break;
                        case 1:    //MainSurface 登录
                            activity.onLogButtonPressed();
                            break;
                        case 2:    //MainSurface 排行榜 & MainSurface2 排行榜
                            activity.onRankButtonPressed();
                            break;
                        case 3:    //Login 登录
                            activity.onLoginButtonPressed();
                            break;
                        case 4:    //Login 注册
                            activity.onOpenRegisterViewButtonPressed();
                            break;
                        case 5:    //MainSurface2 开始（多人）
                            activity.onMultiButtonPressed();
                            break;
                        case 6:    //MainSurface2 注销
                            activity.onLogoutButtonPressed();
                            break;
                        case 7:    //ChoosePicture 选择图片
                            activity.onChoosePictureButtonPressed();
                            break;
                        case 8:    //ChoosePicture 确定，下一步
                            activity.onChoosePictureConfirmButtonPressed();
                            break;
                        case 9:    //Room 创建房间
                            activity.onCreateRoomButtonPressed();
                            break;
                        case 10:    //Room 加入房间
                            activity.onJoinRoomButtonPressed();
                            break;
                        case 11:    //Room 返回
                            activity.onBackPressed();
                            break;
                        case 12:    //ChoosePattern 样式1
                            activity.onChoosePatternButton1Pressed();
                            break;
                        case 13:    //ChoosePattern 样式2
                            activity.onChoosePatternButton2Pressed();
                            break;
                        case 14:    //ChoosePattern 返回
                            activity.onBackPressed();
                            break;
                        case 15:    //ChooseSplit 分割1
                            activity.onChooseSplitButton1Pressed();
                            break;
                        case 16:    //ChooseSplit 分割2
                            activity.onChooseSplitButton2Pressed();
                            break;
                        case 17:    //ChooseSplit 返回
                            activity.onBackPressed();
                            break;
                        case 18:    //Rank 返回
                            activity.onBackPressed();
                            break;
                        case 19:    // RoomList 返回
                            activity.onBackPressed();
                            break;
                        case 20:    // MemberList 退出
                            activity.onBackPressed();
                            break;
                        case 21:    // MemberList 解散
                            activity.onDeleteRoomButtonPressed();
                            break;
                        case 22:    // MemberList 开始
                            activity.gameStart();
                            break;
                        case 23:    // Register 注册
                            activity.onRegisterButtonPressed();
                            break;
                        case 24:    // ChoosePicture 重新选择预设图片
                            activity.onPictureRechoosePressed();
                            break;
                    }
                    // preset image clicked
                    for (int i = 0; i < ChoosePictureView.PRESET_IMAGES; ++i) {
                        if (whichClick == PRESET_OFFSET + i) {
                            activity.onChoosePreSetPicturePressed(i);
                        }
                    }
                }
            }
        }

    }

    public void onTouchEventPuzzle(MotionEvent event, Context context) {
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
                    PuzzleActivity activity = (PuzzleActivity) context;
                    //activity.onButtonPressed();
                    activity.returnToMainActivity();
                }
            }
        }
    }
}
