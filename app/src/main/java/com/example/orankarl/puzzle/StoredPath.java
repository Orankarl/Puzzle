package com.example.orankarl.puzzle;

import android.content.Context;
import android.content.res.Resources;
import android.util.SparseArray;

import java.util.ArrayList;

public class StoredPath {
    /*
        Class for loading paths from array resources.
     */
    public static SparseArray<ArrayList<Float>> paths = new SparseArray<>();
    public static String[] pathNames = {"classic_horizontal_x", "classic_vertical_y", "flat_horizontal_x", "flat_horizontal_y", "flat_vertical_x", "flat_vertical_y"};
    public static final int flat_horizontal_x = 0, flat_horizontal_y = 1, classic_horizontal_x = 2, classic_horizontal_y = 3, zsc_horizontal_x = 4, zsc_horizontal_y = 5;
    public static void init(Context context) {
        readArray(flat_horizontal_x, R.array.horizontal_flat_x, context);
        readArray(flat_horizontal_y, R.array.horizontal_flat_y, context);
//        readArray(flat_vertical_x, R.array.vertical_flat_x, context);
//        readArray(flat_vertical_y, R.array.vertical_flat_y, context);
        readArray(classic_horizontal_x, R.array.horizontal_classic_x, context);
        readArray(classic_horizontal_y, R.array.horizontal_classic_y, context);
//        readArray(classic_vertical_x, R.array.horizontal_classic_y, context);
//        readArray(classic_vertical_y, R.array.horizontal_classic_x, context);
        readArray(zsc_horizontal_x, R.array.horizontal_zsc_x, context);
        readArray(zsc_horizontal_y, R.array.horizontal_zsc_y, context);
//        readArray(zsc_vertical_x, R.array.horizontal_zsc_y, context);
//        readArray(zsc_vertical_y, R.array.horizontal_zsc_x, context);
    }
    private static void readArray(int id, int resID, Context context) {
        ArrayList<Float> list = new ArrayList<>();
        Resources resources = context.getResources();
        String[] temp = resources.getStringArray(resID);
        for (String str:temp) {
            list.add(Float.parseFloat(str));
//            if (id == classic_horizontal_x) {
//                Log.d("chx float:", String.valueOf(list.get(list.size()-1)));
//                Log.d("chx double:", String.valueOf(Double.parseDouble(str)));
//            }
        }
        paths.put(id, list);
    }

    public static SparseArray<ArrayList<Float>> getPaths() {
        return paths;
    }

    public static String[] getPathNames() {
        return pathNames;
    }
}
