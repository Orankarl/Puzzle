package com.example.orankarl.puzzle;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.FileNotFoundException;

public class MainActivity extends AppCompatActivity {
    final Api api = new Api("192.168.1.202", 5000, new Handler(Looper.getMainLooper()));


    public static SurfaceViewEditText editText_username;
    SurfaceViewEditText editText_password;

    boolean login_flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(new LoginView(this));

        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);

        editText_username = new SurfaceViewEditText(this);
        FrameLayout.LayoutParams username_params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        editText_username.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        editText_username.setHint("3-20位用户名");
        username_params.leftMargin = size.x / 2;
        username_params.topMargin = size.y / 3;
        addContentView(editText_username, username_params);

        FrameLayout.LayoutParams password_params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        editText_password = new SurfaceViewEditText(this);
        editText_password.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        editText_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
        password_params.leftMargin = size.x / 2;
        password_params.topMargin = size.y / 2;
        addContentView(editText_password, password_params);
    }

    /*public void setEditTextVisibility(int isVisible) {
        username.setVisibility(isVisible);
    }*/

    public void onButtonPressed() {
        api.login("test", "test", loginRes -> {
            if (loginRes.status == -1)
                return;
            Toast.makeText(this, loginRes.token, Toast.LENGTH_SHORT).show();
            setContentView(new ChoosePictureView(this));
        });
    }

    public void onChoosePictureButtonPressed() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }

    public void onBeginButtonPressed() {
        Toast.makeText(this, "Game start", Toast.LENGTH_LONG).show();
    }

    public void onLoginButtonPressed() {
        String username = editText_username.getText().toString();
        String password = editText_password.getText().toString();
        //Toast.makeText(this, "Username is " + username + '\n' + "Password is " + password, Toast.LENGTH_LONG).show();

        final Toast t = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        api.login(username, password, loginRes -> {
            if (loginRes.status == -1) {
                t.setText("Username or Password Error!\nLogin Failed!");
                t.show();
                return;
            }
            api.userInfo(loginRes.token, res -> {
                login_flag = true;
                t.setText("Login Success!\nPlease Wait...");
                t.show();
            });
        });
        if (login_flag) {
            setContentView(new MainSurfaceView(this));
            login_flag = false;
        }
    }

    public void onRegistButtonPressed() {
        String username = editText_username.getText().toString();
        String password = editText_password.getText().toString();

        final Toast t = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        api.register(username, password, loginRes -> {
            if (loginRes.status == -1) {
                t.setText("Regist Failed!");
                t.show();
                return;
            }
            t.setText("Regist Success!" + '\n' + "Username is " + username + '\n' + "Password is " + password);
            t.show();
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bitmap bitmap = null;
        if (resultCode == RESULT_OK) {
            Uri uri = data.getData();
            ContentResolver cr = this.getContentResolver();
            try {
                bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
            } catch (FileNotFoundException e) {
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
        ChoosePictureView pictureView = new ChoosePictureView(this);
        pictureView.origin_bitmap = bitmap;
        setContentView(pictureView);
    }

}
