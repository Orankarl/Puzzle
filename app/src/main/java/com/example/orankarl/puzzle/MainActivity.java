package com.example.orankarl.puzzle;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    final Api api = new Api("192.168.1.202", 5000, new Handler(Looper.getMainLooper()));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(new MainSurfaceView(this));
    }

    public void onButtonPressed() {
        api.login("test", "test", loginRes -> {
            if (loginRes.status == -1)
                return;
            Toast.makeText(this, loginRes.token, Toast.LENGTH_SHORT).show();
        });
    }



}
