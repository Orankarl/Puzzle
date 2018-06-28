package com.example.orankarl.puzzle;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.BaseColumns;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    final Api api = new Api("45.77.183.226", 5000, new Handler(Looper.getMainLooper()));

    LocalDatabase mDbHelper;
    String token_tmp = "";
    String myToken = "";

    public static double RATIO;
    public static int viewState;    //MainSurfaceView: 0, LoginView: 1, MainSurfaceView2: 2, RoomView: 3, ChoosePictureView: 4, ChoosePatternView: 5, ChooseSplitView: 6,
                                      //RoomListView: 7, MemberListView: 8, RankView: 9
    public static boolean isOnline;
    public static boolean isSingle;
    public static int split;
    public static int pattern;
    public static boolean isRank;
    public static boolean isHost;

    SurfaceViewEditText editText_username;
    SurfaceViewEditText editText_password;

    public static Bitmap puzzleBitmap;

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
                    onJoinRoomButtonPressed();
                }
                break;
            case 9:
                setContentView(new ChooseSplitView(this));
                viewState = 6;
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

        puzzleBitmap = null;

        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);

        float ratioWidth = (float)size.x / 1080;
        float ratioHeight = (float)size.y / 1812;
        RATIO = Math.min(ratioWidth, ratioHeight);
        mDbHelper = new LocalDatabase(this);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        try{
            db.execSQL("CREATE TABLE " + LocalDatabase.FeedEntry.TABLE_NAME + " (" + LocalDatabase.FeedEntry._ID + " INTEGER PRIMARY KEY," + LocalDatabase.FeedEntry.COLUMN_NAME_TITLE + " TEXT)");
        }catch (Exception e){}
    }

    public void onLogButtonPressed() {
        setContentView(new LoginView(this));
        viewState = 1;

        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);

        editText_username = new SurfaceViewEditText(this);
        FrameLayout.LayoutParams username_params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        editText_username.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        editText_username.setHint("3-20位用户名");
        editText_username.setPadding(editText_username.getPaddingLeft(),0,editText_username.getPaddingRight(),editText_username.getPaddingBottom());
        username_params.leftMargin = size.x / 2;
        username_params.topMargin = size.y / 3;
        addContentView(editText_username, username_params);

        editText_password = new SurfaceViewEditText(this);
        FrameLayout.LayoutParams password_params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        editText_password.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        editText_password.setHint("请输入密码");
        editText_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
        editText_password.setPadding(editText_password.getPaddingLeft(),0,editText_password.getPaddingRight(),editText_password.getPaddingBottom());
        password_params.topMargin = size.y / 2;
        password_params.leftMargin = size.x / 2;
        addContentView(editText_password, password_params);

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

            api.rank(loginRes.token, 1,rank -> {
                if (rank.status == 1)
                    rank.status = 2;
            });

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

    public void onRegisterButtonPressed() {
        String username = editText_username.getText().toString();
        String password = editText_password.getText().toString();

        final Toast t = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        api.register(username, password, loginRes -> {
            if (loginRes.status == -1) {
                t.setText("Register Failed!");
                t.show();
                return;
            }
            t.setText("Register Success!" + '\n' + "Username is " + username + '\n' + "Password is " + password);
            t.show();
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
        setContentView(new ChoosePatternView(this));
        viewState = 5;
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
            setContentView(new RankView(this));
            viewState = 9;
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
            setContentView(new RankView(this));
            viewState = 9;
        }
        else if (isSingle) {
            gameStart();
        }
        else {
            comeInRoom();
        }
    }

    private void comeInRoom() {
        setContentView(new MemberListView(this));
        SurfaceViewListView memberList = new SurfaceViewListView(this);
        FrameLayout.LayoutParams memberList_params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        memberList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, getMemberData()));
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

    public void onJoinRoomButtonPressed() {
        isHost = false;

        SurfaceViewListView roomList = new SurfaceViewListView(this);
        FrameLayout.LayoutParams roomList_params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        roomList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, getRoomData()));
        Toast t = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        roomList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                t.setText("进入" + (position + 1) + "号房间");
                t.show();
                comeInRoom();
            }
        });
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        roomList_params.leftMargin = size.x / 10;
        roomList_params.rightMargin = size.x / 10;
        roomList_params.topMargin = size.y / 6;
        roomList_params.height = size.y * 2 / 3;
        roomList_params.width = size.x * 4 / 5;
        setContentView(new RoomListView(this));
        viewState = 7;
        addContentView(roomList, roomList_params);
    }

    public void onDeleteRoomButtonPressed() {
        setContentView(new RoomView(this));
        viewState = 3;
    }

    private List<String> getRoomData() {
        List<String> data = new ArrayList<String>();
        data.add("测试数据1测试数据");
        data.add("测试数据2测试数据");
        data.add("测试数据3测试数据");
        data.add("测试数据4测试数据");
        data.add("测试数据5测试数据");
        data.add("测试数据6测试数据");
        data.add("测试数据7测试数据");
        data.add("测试数据8测试数据");
        data.add("测试数据9测试数据");
        data.add("测试数据10测试数据");
        return data;
    }

    private List<String> getMemberData() {
        List<String> data = new ArrayList<String>();
        data.add("房主id");
        data.add("");
        data.add("成员1");
        data.add("成员1");
        data.add("成员1");
        data.add("成员1");
        data.add("成员1");
        data.add("成员1");
        return data;
    }

    public void gameStart() {
        Toast.makeText(this, "游戏开始!", Toast.LENGTH_LONG).show();
    }

    public static int getTextWidth(Paint paint, String str) {
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
                puzzleBitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
            } catch (FileNotFoundException e) {
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
        ChoosePictureView pictureView = new ChoosePictureView(this);
        pictureView.origin_bitmap = puzzleBitmap;
        setContentView(pictureView);
    }

}
