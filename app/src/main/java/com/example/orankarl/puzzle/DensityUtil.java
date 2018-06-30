package com.example.orankarl.puzzle;

import android.content.Context;

public class DensityUtil {
    /*
        Apply conversions between dp and pixels.
     */

    Context context;
    static float scale;

    DensityUtil(Context context) {
        scale = context.getResources().getDisplayMetrics().density;
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public int dip2px(float dpValue) {
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public int px2dip(float pxValue) {
        return (int) (pxValue / scale + 0.5f);
    }

    public static DensityUtil densityUtil;
}
