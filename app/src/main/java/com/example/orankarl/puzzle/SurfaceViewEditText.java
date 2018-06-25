package com.example.orankarl.puzzle;

import android.content.Context;
import android.graphics.Canvas;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.widget.EditText;

/*
    An Custom EditText designed for SurfaceView
    1. Can be used as an independent ContentView in Activity
    2. Able to appear or hide when needed
 */

public class SurfaceViewEditText extends AppCompatEditText {
    public SurfaceViewEditText(Context context) {
        super(context);
//        setMaxLines(1);
        setMinWidth(400);
        setSingleLine(true);
    }
}
