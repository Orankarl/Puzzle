package com.example.orankarl.puzzle;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.TypedValue;
import android.util.Xml;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;

import static com.example.orankarl.puzzle.MainSurfaceView.screenH;
import static com.example.orankarl.puzzle.MainSurfaceView.screenW;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final Api api = new Api("39.108.99.67", 5000, new Handler(Looper.getMainLooper()));

    LocalDatabase mDbHelper;
    String token_tmp = "";
    String myToken = "";

    public static double RATIO;
    public static int viewState;    //MainSurfaceView: 0, LoginView: 1, MainSurfaceView2: 2, RoomView: 3, ChoosePictureView: 4, ChoosePatternView: 5, ChooseSplitView: 6,
                                      //RoomListView: 7, MemberListView: 8, RankView: 9, RegisterView: 10, CutView 11
    public static boolean isOnline;
    public static boolean isSingle;
    public static int split;
    public static int pattern;
    public static boolean isRank;
    public static boolean isHost;

    public String host;
    public boolean isSelected;
    public static Bitmap background;

    public Typeface font;

    SurfaceViewListView roomList;
    SurfaceViewListView memberList;
    List<String> memberData;

    SurfaceViewEditText editText_username;
    SurfaceViewEditText editText_password;
    SurfaceViewEditText editText_nickname;

    public Bitmap puzzleBitmap, smallBitmap, afterCutBitmap;

    ArrayList<Integer> posIndexList;

    @Override
    public void onBackPressed() {
        switch (viewState){
            case 1:
                setContentView(new MainSurfaceView(this));
                viewState = 0;
                break;
            case 2:
                //onLogoutButtonPressed();
                break;
            case 3:
                setContentView(new MainSurfaceView2(this));
                viewState = 2;
                break;
            case 4:
                if (!isOnline) {
                    setContentView(new MainSurfaceView(this));
                    viewState = 0;
                }
                else if (isSingle) {
                    setContentView(new MainSurfaceView2(this));
                    viewState = 2;
                }
                else {
                    setContentView(new RoomView(this));
                    viewState = 3;
                }
                break;
            case 5:
                if(!isRank) {
                    @SuppressLint("ResourceType") XmlPullParser parser = getResources().getXml(R.layout.activity_main);
                    setContentView(new CutPictureView(this, Xml.asAttributeSet(parser)));
                    viewState = 11;
                }
                else {
                    if (isOnline) {
                        setContentView(new MainSurfaceView2(this));
                        viewState = 2;
                    }
                    else {
                        setContentView(new MainSurfaceView(this));
                        viewState = 0;
                    }
                }
                break;
            case 6:
                setContentView(new ChoosePatternView(this));
                viewState = 5;
                break;
            case 7:
                setContentView(new RoomView(this));
                viewState = 3;
                break;
            case 8:
                if (!isHost) {
                    api.leaveRoom();
                    onJoinRoomButtonPressed();
                }
                break;
            case 9:
                setContentView(new ChooseSplitView(this));
                viewState = 6;
                break;
            case 10:
                onLogButtonPressed();
                break;
            case 11:
                if (!isOnline) {
                    if (isRank) {
                        setContentView(new MainSurfaceView(this));
                        viewState = 0;
                    }
                    else {
                        setContentView(new ChoosePictureView(this));
                        viewState = 4;
                    }
                }
                else if (isRank) {
                    setContentView(new MainSurfaceView2(this));
                    viewState = 2;
                }
                else {
                    setContentView(new ChoosePictureView(this));
                    viewState = 4;
                }
                break;

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(new MainSurfaceView(this));
        viewState = 0;
        isOnline = false;
        isSelected = false;

        puzzleBitmap = null;

        background = BitmapFactory.decodeResource(getResources(), R.drawable.background);
        font = Typeface.createFromAsset(getAssets(), "font/hipchick.ttf");

        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);

        roomList = new SurfaceViewListView(this);
        memberList = new SurfaceViewListView(this);

        float ratioWidth = (float)size.x / 1080;
        float ratioHeight = (float)size.y / 1812;
        RATIO = Math.min(ratioWidth, ratioHeight);
        mDbHelper = new LocalDatabase(this);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        try{
            db.execSQL("CREATE TABLE " + LocalDatabase.FeedEntry.TABLE_NAME + " (" + LocalDatabase.FeedEntry._ID + " INTEGER PRIMARY KEY," + LocalDatabase.FeedEntry.COLUMN_NAME_TITLE + " TEXT)");
        }catch (Exception e){
            e.printStackTrace();
        }

        List<String> data = new ArrayList<>();
        api.onRoomList(roomListResponse -> {
            if (viewState != 7)
                return;
            data.clear();
            for (Api.RoomListEntry i : roomListResponse.rooms) {
                data.add("[Pattern " + i.pattern + ", "
                        + (i.split == 1 ? "Easy" : "Hard") + "] "
                        + i.username + " ("
                        + i.size + " member" + (i.size == 1 ? "" : "s") + ")"
                );
            }

            FrameLayout.LayoutParams roomList_params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            roomList_params.leftMargin = size.x / 10;
            roomList_params.rightMargin = size.x / 10;
            roomList_params.topMargin = size.y / 6;
            roomList_params.height = size.y * 2 / 3;
            roomList_params.width = size.x * 4 / 5;
            ArrayAdapter<String> aa = new ArrayAdapter<String>(
                    this,
                    android.R.layout.simple_expandable_list_item_1,
                    data) {
                @Override
                public @NonNull View getView(int position, View convertView, @NonNull ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    AppCompatTextView tv = (AppCompatTextView) view.findViewById(android.R.id.text1);
                    tv.setTypeface(font);
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 22);
                    tv.setPadding(
                            tv.getPaddingLeft() / 2,
                            tv.getPaddingTop(),
                            tv.getPaddingRight() / 2,
                            tv.getPaddingBottom()
                    );
                    return view;
                }
            };
            roomList.setAdapter(aa);
            roomList.setOnItemClickListener((parent, view, position, id) -> {
                api.enterRoom(roomListResponse.rooms[position].username);
                pattern = roomListResponse.rooms[position].pattern;
                split = roomListResponse.rooms[position].split;
                comeInRoom();
            });

            ((ViewGroup)roomList.getParent()).removeView(roomList);
            addContentView(roomList, roomList_params);
        });

        memberData = new ArrayList<>();
        api.onRoomMember(roomMemberResponse -> {
            if (viewState != 8)
                return;
            memberData.clear();
            for (String i : roomMemberResponse.members) {
                memberData.add(i);
            }
            FrameLayout.LayoutParams memberList_params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            ArrayAdapter<String> aa = new ArrayAdapter<String>(
                    this,
                    android.R.layout.simple_expandable_list_item_1,
                    memberData) {
                @Override
                public @NonNull View getView(int position, View convertView, @NonNull ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    AppCompatTextView tv = (AppCompatTextView) view.findViewById(android.R.id.text1);
                    tv.setTypeface(font);
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 22);
                    tv.setPadding(
                            tv.getPaddingLeft() / 2,
                            tv.getPaddingTop() / 2,
                            tv.getPaddingRight() / 2,
                            tv.getPaddingBottom() / 2
                    );
                    return view;
                }
            };
            memberList.setAdapter(aa);
            memberList.setClickable(false);
            memberList_params.leftMargin = size.x / 10;
            memberList_params.rightMargin = size.x / 10;
            memberList_params.topMargin = size.y / 6;
            memberList_params.height = size.y / 2;
            memberList_params.width = size.x * 4 / 5;

            if(memberList.getParent()!=null)
                ((ViewGroup)memberList.getParent()).removeView(memberList);
            addContentView(memberList, memberList_params);
        });

        api.onEnterRoom(enterRoomResponse -> {
            if (viewState != 8)
                return;
            Toast.makeText(
                    this,
                    enterRoomResponse.username + " entered the room!",
                    Toast.LENGTH_SHORT).show();
        });

        api.onLeaveRoom(leaveRoomResponse -> {
            if (viewState != 8)
                return;
            Toast.makeText(
                    this,
                    leaveRoomResponse.username + " left the room.",
                    Toast.LENGTH_SHORT).show();
        });

        api.onCancelRoom(() -> {
            if (viewState != 8)
                return;
            memberData.clear();
            onJoinRoomButtonPressed();
        });

        api.onChangeRoom(changeRoomResponse -> {
            if (viewState != 7)
                return;
//            for (int i = 0; i < data.size(); i++) {
//                String tmp = data.get(i);
//                if (tmp.substring(0, tmp.indexOf(' ')).equals(changeRoomResponse.room)) {
//                    tmp = tmp.substring(0, tmp.lastIndexOf(':') + 1) + changeRoomResponse.size;
//                    data.set(i, tmp);
//                    break;
//                }
//            }
        });

        api.onGetGameParam(param -> {
            split = param.split;
            pattern = param.pattern;

            int pieceCount;
            if (param.split == 1) pieceCount = 9;
            else pieceCount = 16;
            posIndexList = new ArrayList<>();
            for (int i = 0; i < pieceCount; i++) {
                posIndexList.add(param.sequence[i]);
            }

            puzzleBitmap = param.image;
            TurnToGameView();
        });

        api.onStartGame(() -> {
            if (puzzleBitmap == null)
                return;
            if (viewState == 8) {
                if (isHost) return;
//                TurnToGameView();
            }
        });
    }

    public void onLogButtonPressed() {
        LoginView loginView = new LoginView(this);
        setContentView(loginView);
        viewState = 1;

        editText_username = new SurfaceViewEditText(this);
        FrameLayout.LayoutParams username_params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        editText_username.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        editText_username.setBackgroundColor(0);
        editText_username.setTypeface(font);
        editText_username.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 26);
        editText_username.setHint("username");
        editText_username.setMinWidth(screenW * 3 / 8);
        editText_username.setPadding(editText_username.getPaddingLeft(),0,editText_username.getPaddingRight(),editText_username.getPaddingBottom());
        username_params.leftMargin = screenW / 2;
        username_params.topMargin = screenH / 3;
        addContentView(editText_username, username_params);

        editText_password = new SurfaceViewEditText(this);
        FrameLayout.LayoutParams password_params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        editText_password.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        editText_password.setBackgroundColor(0);
        editText_password.setTypeface(font);
        editText_password.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 26);
        editText_password.setHint("your password");
        editText_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
        editText_password.setMinWidth(screenW * 3 / 8);
        editText_password.setPadding(editText_password.getPaddingLeft(),0,editText_password.getPaddingRight(),editText_password.getPaddingBottom());
        password_params.topMargin = screenH / 2;
        password_params.leftMargin = screenW / 2;
        addContentView(editText_password, password_params);

        loginView.TextSize = (int) editText_username.getTextSize();

        mDbHelper = new LocalDatabase(this);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String[] projection = {BaseColumns._ID, LocalDatabase.FeedEntry.COLUMN_NAME_TITLE};
        Cursor cursor = db.query(LocalDatabase.FeedEntry.TABLE_NAME, projection, null, null, null, null, null);
        while (cursor.moveToNext()) {
            myToken = cursor.getString(cursor.getColumnIndex(LocalDatabase.FeedEntry.COLUMN_NAME_TITLE));
        }
        cursor.close();

        if (!myToken.equals("")) {
            isOnline = true;
            api.socketAuth(myToken);
            Toast.makeText(this, "Welcome back!", Toast.LENGTH_LONG).show();
            setContentView(new MainSurfaceView2(this));
            viewState = 2;
        }
    }

    public void onLogoutButtonPressed() {
        mDbHelper = new LocalDatabase(this);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.execSQL("delete from " + LocalDatabase.FeedEntry.TABLE_NAME);
        myToken = "";

        Toast.makeText(this, "Logout Success!", Toast.LENGTH_LONG).show();

        setContentView(new MainSurfaceView(this));
        viewState = 0;
        isOnline = false;
    }

    public void onSingleBeginButtonPressed() {
        isRank = false;
        isSingle = true;
        setContentView(new ChoosePictureView(this));
        viewState = 4;
    }

    public void onChoosePictureButtonPressed() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);

    }

    public void onPictureRechoosePressed() {
        isSelected = false;
    }

    public void onChoosePreSetPicturePressed(int i) {
        AssetManager assetManager = getAssets();
        try {
            InputStream is = assetManager.open("PreSetImage/image" + Integer.toString(i + 1) + ".jpg");
            puzzleBitmap = BitmapFactory.decodeStream(is);

            @SuppressLint("ResourceType") XmlPullParser parser = getResources().getXml(R.layout.activity_main);
            setContentView(new CutPictureView(this,Xml.asAttributeSet(parser)));
            viewState = 11;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onLoginButtonPressed() {
        String username = editText_username.getText().toString();
        String password = editText_password.getText().toString();

        final Toast t = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        api.login(username, password, loginRes -> {
            if (loginRes.status == -1) {
                t.setText("Username or Password Error!\nLogin Failed!");
                t.show();
                return;
            }

            t.setText("Login Success!");
            t.show();
            setContentView(new MainSurfaceView2(this));
            isOnline = true;
            viewState = 2;

            token_tmp = loginRes.token;

            SQLiteDatabase db2 = mDbHelper.getWritableDatabase();
            db2.execSQL("delete from " + LocalDatabase.FeedEntry.TABLE_NAME);
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(LocalDatabase.FeedEntry.COLUMN_NAME_TITLE, token_tmp);
            db.insert(LocalDatabase.FeedEntry.TABLE_NAME, null, values);
        });
    }

    public void onOpenRegisterViewButtonPressed() {
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);

        RegisterView registerView = new RegisterView(this);
        setContentView(registerView);
        viewState = 10;

        editText_username = new SurfaceViewEditText(this);
        FrameLayout.LayoutParams username_params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        editText_username.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        editText_username.setBackgroundColor(0);
        editText_username.setTypeface(font);
        editText_username.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 26);
        editText_username.setHint("len 3 to 20");
        editText_username.setMinWidth(screenW * 3 / 8);
        editText_username.setPadding(editText_username.getPaddingLeft(),0,editText_username.getPaddingRight(),editText_username.getPaddingBottom());
        username_params.leftMargin = screenW / 2;
        username_params.topMargin = screenH * 2 / 7;
        addContentView(editText_username, username_params);

        editText_password = new SurfaceViewEditText(this);
        FrameLayout.LayoutParams password_params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        editText_password.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        editText_password.setBackgroundColor(0);
        editText_password.setTypeface(font);
        editText_password.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 26);
        editText_password.setHint("password");
        editText_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
        editText_password.setMinWidth(screenW * 3 / 8);
        editText_password.setPadding(editText_password.getPaddingLeft(),0,editText_password.getPaddingRight(),editText_password.getPaddingBottom());
        password_params.topMargin = screenH * 3 / 7;
        password_params.leftMargin = screenW / 2;
        addContentView(editText_password, password_params);

        editText_nickname = new SurfaceViewEditText(this);
        FrameLayout.LayoutParams nickname_params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        editText_nickname.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        editText_nickname.setBackgroundColor(0);
        editText_nickname.setTypeface(font);
        editText_nickname.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 26);
        editText_nickname.setHint("your nickname");
        editText_nickname.setMinWidth(screenW * 3 / 8);
        editText_nickname.setPadding(editText_password.getPaddingLeft(),0,editText_password.getPaddingRight(),editText_password.getPaddingBottom());
        nickname_params.topMargin = screenH * 4 / 7;
        nickname_params.leftMargin = screenW / 2;
        addContentView(editText_nickname, nickname_params);

        registerView.TextSize = (int)editText_username.getTextSize();
    }

    public void onRegisterButtonPressed() {
        String username = editText_username.getText().toString();
        String password = editText_password.getText().toString();
        String nickname = editText_nickname.getText().toString();

        final Toast t = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        api.register(username, nickname, password, regRes -> {
            if (regRes.status == -1) {
                t.setText("Register Failed!");
                t.show();
                return;
            }
            t.setText("Register Success!" + '\n' + "Username is " + username + '\n' + "Password is " + password);
            t.show();
            onBackPressed();
        });
    }

    public void onMultiButtonPressed() {
        isSingle = false;
        isRank = false;
        setContentView(new RoomView(this));
        viewState = 3;
    }

    public void onCreateRoomButtonPressed() {
        isHost = true;
        setContentView(new ChoosePictureView(this));
        viewState = 4;
    }

    public void onChoosePictureConfirmButtonPressed() {

        @SuppressLint("ResourceType") XmlPullParser parser = getResources().getXml(R.layout.activity_main);
        setContentView(new CutPictureView(this,Xml.asAttributeSet(parser)));
        viewState = 11;
    }
    public void onCutPictureConfirmButtonPressed(){
        puzzleBitmap = afterCutBitmap;
        setContentView(new ChoosePatternView(this));
        viewState = 5;

//        setContentView(new ChoosePictureView(this));
//        viewState = 4;
    }
    public void onRankButtonPressed() {
        isRank = true;
        setContentView(new ChoosePatternView(this));
        viewState = 5;
    }


    public void onChoosePatternButton1Pressed() {
        pattern = 1;
        setContentView(new ChooseSplitView(this));
        viewState = 6;
    }

    public void onChoosePatternButton2Pressed() {
        pattern = 2;
        setContentView(new ChooseSplitView(this));
        viewState = 6;
    }
    public void onChooseSplitButton1Pressed() {
        split = 1;
        if (isRank) {
            getRank();
        }
        else if (isSingle) {
            gameStart();
        }
        else {
            comeInRoom();
        }
    }

    public void onChooseSplitButton2Pressed() {
        split = 2;
        if (isRank) {
            getRank();
        }
        else if (isSingle) {
            gameStart();
        }
        else {
            comeInRoom();
        }
    }

    private void getRank() {
        RankView rankView = new RankView(this);
        final Toast t = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        api.rank(pattern, split, rankResponse -> {
            if (rankResponse.status == -1) {
                t.setText("Get Rank Board Failed!");
                t.show();
                return;
            }
            int count = 0;
            for (Api.RankResponseEntry i : rankResponse.rank) {
                rankView.rank_id[count] = i.nickname;
                rankView.time[count] = i.time;
                count++;
            }
            setContentView(rankView);
            viewState = 9;
        });
    }

    private void comeInRoom() {
        if (isHost) {
            api.newRoom(split, pattern);
            memberData.clear();
            api.userInfo(userInfoResponse -> {
                host = userInfoResponse.username;
                memberData.add(userInfoResponse.username);
                setContentView(new MemberListView(this));
                SurfaceViewListView memberList = new SurfaceViewListView(this);
                FrameLayout.LayoutParams memberList_params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                ArrayAdapter<String> aa = new ArrayAdapter<String>(
                        this,
                        android.R.layout.simple_expandable_list_item_1,
                        memberData) {
                    @Override
                    public @NonNull View getView(int position, View convertView, @NonNull ViewGroup parent) {
                        View view = super.getView(position, convertView, parent);
                        AppCompatTextView tv = (AppCompatTextView) view.findViewById(android.R.id.text1);
                        tv.setTypeface(font);
                        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 22);
                        tv.setPadding(
                                tv.getPaddingLeft() / 2,
                                tv.getPaddingTop() / 2,
                                tv.getPaddingRight() / 2,
                                tv.getPaddingBottom() / 2
                        );
                        return view;
                    }
                };
                memberList.setAdapter(aa);
                memberList.setClickable(false);
                Point size = new Point();
                getWindowManager().getDefaultDisplay().getSize(size);
                memberList_params.leftMargin = size.x / 10;
                memberList_params.rightMargin = size.x / 10;
                memberList_params.topMargin = size.y / 6;
                memberList_params.height = size.y / 2;
                memberList_params.width = size.x * 4 / 5;
                setContentView(new MemberListView(this));
                viewState = 8;
                addContentView(memberList, memberList_params);
            });
        }
        else {
            memberData.clear();
            setContentView(new MemberListView(this));
            SurfaceViewListView memberList = new SurfaceViewListView(this);
            FrameLayout.LayoutParams memberList_params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            ArrayAdapter<String> aa = new ArrayAdapter<String>(
                    this,
                    android.R.layout.simple_expandable_list_item_1,
                    memberData) {
                @Override
                public @NonNull View getView(int position, View convertView, @NonNull ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    AppCompatTextView tv = (AppCompatTextView) view.findViewById(android.R.id.text1);
                    tv.setTypeface(font);
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 22);
                    tv.setPadding(
                            tv.getPaddingLeft() / 2,
                            tv.getPaddingTop() / 2,
                            tv.getPaddingRight() / 2,
                            tv.getPaddingBottom() / 2
                    );
                    return view;
                }
            };
            memberList.setAdapter(aa);
            memberList.setClickable(false);
            Point size = new Point();
            getWindowManager().getDefaultDisplay().getSize(size);
            memberList_params.leftMargin = size.x / 10;
            memberList_params.rightMargin = size.x / 10;
            memberList_params.topMargin = size.y / 6;
            memberList_params.height = size.y / 2;
            memberList_params.width = size.x * 4 / 5;
            setContentView(new MemberListView(this));
            viewState = 8;
            addContentView(memberList, memberList_params);
        }
    }

    public void onJoinRoomButtonPressed() {
        List<String> data = new ArrayList<>();

        isHost = false;
        api.roomList();

        FrameLayout.LayoutParams roomList_params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        roomList_params.leftMargin = size.x / 10;
        roomList_params.rightMargin = size.x / 10;
        roomList_params.topMargin = size.y / 6;
        roomList_params.height = size.y * 2 / 3;
        roomList_params.width = size.x * 4 / 5;

        roomList.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_expandable_list_item_1, data));
        roomList.setOnItemClickListener((parent, view, position, id) -> {
            host = data.get(position);
            comeInRoom();
        });

        setContentView(new RoomListView(this));
        viewState = 7;
        addContentView(roomList, roomList_params);
    }

    public void onDeleteRoomButtonPressed() {
        api.deleteRoom();
        setContentView(new RoomView(this));
        viewState = 3;
    }


    public void gameStart() {
        api.startGame();
        Toast.makeText(this, "Transferring image...", Toast.LENGTH_LONG).show();
        if (isOnline) {
            int pieceCount;
            if (split == 1) pieceCount = 9;
            else pieceCount = 16;
            posIndexList = new ArrayList<>();
            for (int i = 0; i < pieceCount; i++) {
                posIndexList.add(i);
            }
            Collections.shuffle(posIndexList);
            int[] posArray = new int[pieceCount];
            for (int i = 0; i < pieceCount; i++) {
                posArray[i] = posIndexList.get(i);
            }
            api.gameParam(split, pattern, puzzleBitmap, posArray, response -> {
                TurnToGameView();
            });
        } else {
            TurnToGameView();
        }

    }

    private void TurnToGameView() {
        Toast.makeText(this, "Go!", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, PuzzleActivity.class);
        Bitmap bitmap = BitmapUtil.setImgSize(this.puzzleBitmap, (int) (0.8*screenW), 0);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//        if (puzzleBitmap != null) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] bitmapByte = stream.toByteArray();
            intent.putExtra("picture", bitmapByte);
//        }

        String filename = "picture";
//        BitmapUtil.saveBitmap2file(puzzleBitmap, filename);
//        intent.putExtra("picture", filename);
        intent.putExtra("isOnline", isOnline);
        intent.putExtra("isSingle", isSingle);
        intent.putExtra("pattern", pattern);
        if (split == 1) {
            intent.putExtra("split", 9);
        } else {
            intent.putExtra("split", 16);
        }
        if (isOnline && !isSingle) {
            intent.putIntegerArrayListExtra("posIndexList", posIndexList);
        }

        startActivity(intent);
    }

    public int getTextWidth(Paint paint, String str) {
        int iRet = 0;
        if (str != null && str.length() > 0) {
            int len = str.length();
            float[] widths = new float[len];
            paint.getTextWidths(str, widths);
            for (int j = 0; j < len; j++) {
                iRet += (int) Math.ceil(widths[j]);
            }
        }
        return iRet;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Uri uri = data.getData();
            ContentResolver cr = this.getContentResolver();
            try {
                if (uri != null)
                    puzzleBitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
        ChoosePictureView pictureView = new ChoosePictureView(this);
        pictureView.origin_bitmap = puzzleBitmap;
        isSelected = true;
        setContentView(pictureView);
    }

}
