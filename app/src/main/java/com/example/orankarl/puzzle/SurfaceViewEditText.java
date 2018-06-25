package com.example.orankarl.puzzle;

import android.content.Context;
import android.graphics.Canvas;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.widget.EditText;

public class SurfaceViewEditText extends AppCompatEditText {
    public SurfaceViewEditText(Context context) {
        super(context);
//        setMaxLines(1);
        setMinWidth(360);
        setSingleLine(true);
    }
}
