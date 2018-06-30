package com.example.orankarl.puzzle;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class GraphicPath {
    /*
        Storing coordinates of points.
     */
    public List<Integer> pathX;
    public List<Integer> pathY;
    public GraphicPath() {
        pathX = new ArrayList<>();
        pathY = new ArrayList<>();
    }
    private int[] getXArray() {
        int[] x = new int[pathX.size()];
        for (int i = 0; i < x.length; i++) {
            x[i] = pathX.get(i);
        }
        return x;
    }
    private int[] getYArray() {
        int[] y = new int[pathY.size()];
        for (int i = 0; i < y.length; i++) {
            y[i] = pathY.get(i);
        }
        return y;
    }
    public void addPath(int x, int y) {
        pathX.add(x);
        pathY.add(y);
    }
    public void clear() {
        pathX.clear();
        pathY.clear();
    }
    public int getTop() {
        int min = pathY.size()>0 ? pathY.get(0) : 0;
        for (int y:pathY) {
            if (y < min) min = y;
        }
        Log.d("top:", String.valueOf(min));
        return min;
    }
    public int getBottom() {
        int max = pathY.size()>0 ? pathY.get(0) : 0;
        for (int y:pathY) {
            if (y > max) max = y;
        }
        Log.d("bottom:", String.valueOf(max));
        return max;
    }
    public int getLeft() {
        int min = pathX.size() > 0 ? pathX.get(0) : 0;
        for (int x:pathX) {
            if (x < min) min = x;
        }
        return min;
    }
    public int getRight() {
        int max = pathX.size() > 0 ? pathX.get(0) : 0;
        for (int x:pathX) {
            if (x > max) max = x;
        }
        return max;
    }
    public int size() {
        return pathX.size();
    }
    public void print() {
        for (int i = 0; i < pathY.size(); i++) {
            Log.d("GraphicPath (x y):", String.valueOf(pathX.get(i)) + " " + String.valueOf(pathY.get(i)));
        }
    }
}
