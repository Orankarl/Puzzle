package com.example.orankarl.puzzle;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.FileNotFoundException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //setContentView(new MainSurfaceView(this));
        setContentView(new LoginView(this));
    }

    public void onButtonPressed() {
        Toast.makeText(this, "Button Pressed", Toast.LENGTH_LONG).show();
        setContentView(new ChoosePictureView(this));
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
        final Toast t = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        final Api api = new Api("192.168.1.202", 5000);
        api.login("test", "test", loginRes -> {
            if (loginRes.status == -1)
                return;
            api.userInfo(loginRes.token, res -> {
                t.setText(res._id);
                t.show();
            });
        });
        setContentView(new MainSurfaceView(this));
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
