package com.example.orankarl.puzzle;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

public class PuzzleActivity extends AppCompatActivity {

    int pattern, split;
    boolean isOnline, isSingle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        DensityUtil.densityUtil = new DensityUtil(this);

        StoredPath.init(this);

        Intent intent = getIntent();
        if (intent != null) {
            //        String filename = intent.getStringExtra("picture");
            pattern = intent.getIntExtra("pattern", 1);
            split = intent.getIntExtra("split", 9);
            isOnline = intent.getBooleanExtra("isOnline", false);
            isSingle = intent.getBooleanExtra("isSingle", true);
//        byte[] bytes = intent.getByteArrayExtra("picture");
//        Log.d("bytes", String.valueOf(bytes));
//        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }




        setContentView(new PuzzleSurfaceView(this, MainActivity.puzzleBitmap, pattern, split, isSingle, isOnline));

    }
}
