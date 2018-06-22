package com.example.orankarl.puzzle;

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

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(new MainSurfaceView(this));
    }

    public void onButtonPressed() {
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
    }



}
