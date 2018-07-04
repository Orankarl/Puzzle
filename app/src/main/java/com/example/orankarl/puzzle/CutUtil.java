package com.example.orankarl.puzzle;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.Log;
import android.util.SparseArray;

import java.util.ArrayList;

public class CutUtil {
    /*
        Class for cut image into pieces with paths from StoredPath.
     */
    public static final String type1 = "flat", type2 = "classic";
    public static ArrayList<Bitmap> cutImage(Bitmap bitmap, String pathType, int count) {
        ArrayList<Bitmap> pieces = new ArrayList<>();
        SparseArray<ArrayList<Float>> paths = StoredPath.getPaths();
        ArrayList<Float> horizontal_x, horizontal_y, vertical_x, vertical_y, border_horizontal_x, border_horizontal_y, border_vertical_x, border_vertical_y;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int rowCount = (int)Math.sqrt((double) count);
        Log.d("rowCount from cutImage:", String.valueOf(rowCount));
        border_horizontal_x = scalePath(paths.get(StoredPath.flat_horizontal_x), 1.0f * width / rowCount);
        border_horizontal_y = scalePath(paths.get(StoredPath.flat_horizontal_y), 1.0f * height / rowCount);
        border_vertical_x = scalePath(paths.get(StoredPath.flat_horizontal_y), 1.0f * width / rowCount);
        border_vertical_y = scalePath(paths.get(StoredPath.flat_horizontal_x), 1.0f * height / rowCount);
        if (pathType.equals(type1)) {
            horizontal_x = scalePath(paths.get(StoredPath.flat_horizontal_x), 1.0f * width / rowCount);
            horizontal_y = scalePath(paths.get(StoredPath.flat_horizontal_y), 1.0f * height / rowCount);
            vertical_x = scalePath(paths.get(StoredPath.flat_vertical_x), 1.0f * width / rowCount);
            vertical_y = scalePath(paths.get(StoredPath.flat_vertical_y), 1.0f * height / rowCount);
        } else if (pathType.equals(type2)){
            horizontal_x = scalePath(paths.get(StoredPath.classic_horizontal_x), 1.0f * width / rowCount);
            horizontal_y = scalePath(paths.get(StoredPath.classic_horizontal_y), 1.0f * height / rowCount);
            vertical_x = scalePath(paths.get(StoredPath.classic_horizontal_y), 1.0f * width / rowCount);
            vertical_y = scalePath(paths.get(StoredPath.classic_horizontal_x), 1.0f * height / rowCount);
        } else {
            horizontal_x = scalePath(paths.get(StoredPath.flat_horizontal_x), 1.0f * width / rowCount);
            horizontal_y = scalePath(paths.get(StoredPath.flat_horizontal_y), 1.0f * height / rowCount);
            vertical_x = scalePath(paths.get(StoredPath.flat_vertical_x), 1.0f * width / rowCount);
            vertical_y = scalePath(paths.get(StoredPath.flat_vertical_y), 1.0f * height / rowCount);
        }
        if (count == 4) {
            //顺序为从左到右，从上到下
            ArrayList<GraphicPath> pathForImages = new ArrayList<>();
//            Log.d("cutImage w & h:", String.valueOf(width) + " " + String.valueOf(height));
            GraphicPath pathForImage1 = graphicPathGenerate(border_horizontal_x, border_horizontal_y, vertical_x, vertical_y,
                    horizontal_x, horizontal_y, border_vertical_x, border_vertical_y, 0, 0, width/2, height/2);
            GraphicPath pathForImage2 = graphicPathGenerate(border_horizontal_x, border_horizontal_y, border_vertical_x, border_vertical_y,
                    horizontal_x, horizontal_y, vertical_x, vertical_y, width/2, 0, width/2, height/2);
            GraphicPath pathForImage3 = graphicPathGenerate(horizontal_x, horizontal_y, vertical_x, vertical_y,
                    border_horizontal_x, border_horizontal_y, border_vertical_x, border_vertical_y, 0, height/2, width/2, height/2);
            GraphicPath pathForImage4 = graphicPathGenerate(horizontal_x, horizontal_y, border_vertical_x, border_vertical_y,
                    border_horizontal_x, border_horizontal_y, vertical_x, vertical_y, width/2, height/2, width/2, height/2);
            pathForImages.add(pathForImage1);
            pathForImages.add(pathForImage2);
            pathForImages.add(pathForImage3);
            pathForImages.add(pathForImage4);

            for (GraphicPath path:pathForImages) {
                pieces.add(cutSinglePiece(bitmap, path));
            }
        }

        if (count  == 9) {
            ArrayList<GraphicPath> pathForImages = new ArrayList<>();
//            Log.d("cutImage w & h:", String.valueOf(width) + " " + String.valueOf(height));
            GraphicPath pathForImage1 = graphicPathGenerate(border_horizontal_x, border_horizontal_y, vertical_x, vertical_y,
                    horizontal_x, horizontal_y, border_vertical_x, border_vertical_y, 0, 0, width/3, height/3);
            GraphicPath pathForImage2 = graphicPathGenerate(border_horizontal_x, border_horizontal_y, vertical_x, vertical_y,
                    horizontal_x, horizontal_y, vertical_x, vertical_y, width/3, 0, width/3, height/3);
            GraphicPath pathForImage3 = graphicPathGenerate(border_horizontal_x, border_horizontal_y, border_vertical_x, border_vertical_y,
                    horizontal_x, horizontal_y, vertical_x, vertical_y, width/3*2, 0, width/3, height/3);
            GraphicPath pathForImage4 = graphicPathGenerate(horizontal_x, horizontal_y, vertical_x, vertical_y,
                    horizontal_x, horizontal_y, border_vertical_x, border_vertical_y, 0, height/3, width/3, height/3);
            GraphicPath pathForImage5 = graphicPathGenerate(horizontal_x, horizontal_y, vertical_x, vertical_y,
                    horizontal_x, horizontal_y, vertical_x, vertical_y, width/3, height/3, width/3, height/3);
            GraphicPath pathForImage6 = graphicPathGenerate(horizontal_x, horizontal_y, border_vertical_x, border_vertical_y,
                    horizontal_x, horizontal_y, vertical_x, vertical_y, width/3*2, height/3, width/3, height/3);
            GraphicPath pathForImage7 = graphicPathGenerate(horizontal_x, horizontal_y, vertical_x, vertical_y,
                    border_horizontal_x, border_horizontal_y, border_vertical_x, border_vertical_y, 0, height/3*2, width/3, height/3);
            GraphicPath pathForImage8 = graphicPathGenerate(horizontal_x, horizontal_y, vertical_x, vertical_y,
                    border_horizontal_x, border_horizontal_y, vertical_x, vertical_y, width/3, height/3*2, width/3, height/3);
            GraphicPath pathForImage9 = graphicPathGenerate(horizontal_x, horizontal_y, border_vertical_x, border_vertical_y,
                    border_horizontal_x, border_horizontal_y, vertical_x, vertical_y, width/3*2, height/3*2, width/3, height/3);
            pathForImages.add(pathForImage1);
            pathForImages.add(pathForImage2);
            pathForImages.add(pathForImage3);
            pathForImages.add(pathForImage4);
            pathForImages.add(pathForImage5);
            pathForImages.add(pathForImage6);
            pathForImages.add(pathForImage7);
            pathForImages.add(pathForImage8);
            pathForImages.add(pathForImage9);

            for (GraphicPath path:pathForImages) {
                pieces.add(cutSinglePiece(bitmap, path));
            }
        }
        if (count  == 16) {
            ArrayList<GraphicPath> pathForImages = new ArrayList<>();
            GraphicPath pathForImage1 = graphicPathGenerate(border_horizontal_x, border_horizontal_y, vertical_x, vertical_y,
                    horizontal_x, horizontal_y, border_vertical_x, border_vertical_y, 0, 0, width/4, height/4);
            GraphicPath pathForImage2 = graphicPathGenerate(border_horizontal_x, border_horizontal_y, vertical_x, vertical_y,
                    horizontal_x, horizontal_y, vertical_x, vertical_y, width/4, 0, width/4, height/4);
            GraphicPath pathForImage3 = graphicPathGenerate(border_horizontal_x, border_horizontal_y, vertical_x, vertical_y,
                    horizontal_x, horizontal_y, vertical_x, vertical_y, width/4*2, 0, width/4, height/4);
            GraphicPath pathForImage4 = graphicPathGenerate(border_horizontal_x, border_horizontal_y, border_vertical_x, border_vertical_y,
                    horizontal_x, horizontal_y, vertical_x, vertical_y, width/4*3, 0, width/4, height/4);
            GraphicPath pathForImage5 = graphicPathGenerate(horizontal_x, horizontal_y, vertical_x, vertical_y,
                    horizontal_x, horizontal_y, border_vertical_x, border_vertical_y, 0, height/4, width/4, height/4);
            GraphicPath pathForImage6 = graphicPathGenerate(horizontal_x, horizontal_y, vertical_x, vertical_y,
                    horizontal_x, horizontal_y, vertical_x, vertical_y, width/4, height/4, width/4, height/4);
            GraphicPath pathForImage7 = graphicPathGenerate(horizontal_x, horizontal_y, vertical_x, vertical_y,
                    horizontal_x, horizontal_y, vertical_x, vertical_y, width/4*2, height/4, width/4, height/4);
            GraphicPath pathForImage8 = graphicPathGenerate(horizontal_x, horizontal_y, border_vertical_x, border_vertical_y,
                    horizontal_x, horizontal_y, vertical_x, vertical_y, width/4*3, height/4, width/4, height/4);
            GraphicPath pathForImage9 = graphicPathGenerate(horizontal_x, horizontal_y, vertical_x, vertical_y,
                    horizontal_x, horizontal_y, border_vertical_x, border_vertical_y, 0, height/4*2, width/4, height/4);
            GraphicPath pathForImage10 = graphicPathGenerate(horizontal_x, horizontal_y, vertical_x, vertical_y,
                    horizontal_x, horizontal_y, vertical_x, vertical_y, width/4, height/4*2, width/4, height/4);
            GraphicPath pathForImage11 = graphicPathGenerate(horizontal_x, horizontal_y, vertical_x, vertical_y,
                    horizontal_x, horizontal_y, vertical_x, vertical_y, width/4*2, height/4*2, width/4, height/4);
            GraphicPath pathForImage12 = graphicPathGenerate(horizontal_x, horizontal_y, border_vertical_x, border_vertical_y,
                    horizontal_x, horizontal_y, vertical_x, vertical_y, width/4*3, height/4*2, width/4, height/4);
            GraphicPath pathForImage13 = graphicPathGenerate(horizontal_x, horizontal_y, vertical_x, vertical_y,
                    border_horizontal_x, border_horizontal_y, border_vertical_x, border_vertical_y, 0, height/4*3, width/4, height/4);
            GraphicPath pathForImage14 = graphicPathGenerate(horizontal_x, horizontal_y, vertical_x, vertical_y,
                    border_horizontal_x, border_horizontal_y, vertical_x, vertical_y, width/4, height/4*3, width/4, height/4);
            GraphicPath pathForImage15 = graphicPathGenerate(horizontal_x, horizontal_y, vertical_x, vertical_y,
                    border_horizontal_x, border_horizontal_y, vertical_x, vertical_y, width/4*2, height/4*3, width/4, height/4);
            GraphicPath pathForImage16 = graphicPathGenerate(horizontal_x, horizontal_y, border_vertical_x, border_vertical_y,
                    border_horizontal_x, border_horizontal_y, vertical_x, vertical_y, width/4*3, height/4*3, width/4, height/4);
            pathForImages.add(pathForImage1);
            pathForImages.add(pathForImage2);
            pathForImages.add(pathForImage3);
            pathForImages.add(pathForImage4);
            pathForImages.add(pathForImage5);
            pathForImages.add(pathForImage6);
            pathForImages.add(pathForImage7);
            pathForImages.add(pathForImage8);
            pathForImages.add(pathForImage9);
            pathForImages.add(pathForImage10);
            pathForImages.add(pathForImage11);
            pathForImages.add(pathForImage12);
            pathForImages.add(pathForImage13);
            pathForImages.add(pathForImage14);
            pathForImages.add(pathForImage15);
            pathForImages.add(pathForImage16);

            for (GraphicPath path:pathForImages) {
                pieces.add(cutSinglePiece(bitmap, path));
            }
        }

        return pieces;
    }

    private static GraphicPath graphicPathGenerate(ArrayList<Float> top_x, ArrayList<Float> top_y,
                                                   ArrayList<Float> right_x, ArrayList<Float> right_y,
                                                   ArrayList<Float> bottom_x, ArrayList<Float> bottom_y,
                                                   ArrayList<Float> left_x, ArrayList<Float> left_y,
                                                   int biasX, int biasY, int width, int height) {
        GraphicPath path = new GraphicPath();
        //除最后一个外，每个for循环少加一个点，避免重叠，虽然重叠好像没影响
        //每个for循环对应一条边，顺序为上右下左
        //biasX和Y使坐标偏移以获得不同位置的块
        //width和height为单块的数据，加在底部和右边的边上
        for (int i = 0; i < top_x.size(); i++) {
            path.addPath(top_x.get(i).intValue() + biasX, top_y.get(i).intValue() + biasY);
        }
        for (int i = 0; i < right_x.size(); i++) {
            path.addPath(right_x.get(i).intValue() + biasX + width, right_y.get(i).intValue() + biasY);
        }
        for (int i = bottom_x.size() - 1; i >= 0; i--) {
            path.addPath(bottom_x.get(i).intValue() + biasX, bottom_y.get(i).intValue() + biasY + height);
        }
        for (int i = left_x.size() - 1; i >= 0; i--) {
            path.addPath(left_x.get(i).intValue() + biasX, left_y.get(i).intValue() + biasY);
        }
        Log.d("height:", String.valueOf(height));
//        path.print();
        return path;
    }

    private static ArrayList<Float> scalePath(ArrayList<Float> list, float scaleValue) {
        //scaleValue = width/height of single piece
        ArrayList<Float> result = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            result.add(list.get(i) * scaleValue);
//            Log.d("scale:", String.valueOf(list.get(i).floatValue()));
//            Log.d("scale multiply:", String.valueOf(list.get(i).floatValue() * scaleValue));
        }
        return result;
    }

    private static Bitmap cutSinglePiece(Bitmap bitmap, GraphicPath path) {
        Rect rect = new Rect(path.getLeft(), path.getTop(), path.getRight(), path.getBottom());
        if (rect.left < 0) rect.left = 0;
        if (rect.right < 0) rect.right = 0;
        if (rect.top < 0) rect.top = 0;
        if (rect.bottom < 0) rect.bottom = 0;
        int cut_width = Math.abs(rect.left - rect.right);
        int cut_height = Math.abs(rect.top - rect.bottom);

        Log.d("rect left&right:", String.valueOf(rect.left) + " " + String.valueOf(rect.right));
        Log.d("bitmap w&h:", String.valueOf(bitmap.getWidth()) + " " + String.valueOf(bitmap.getHeight()));
        Log.d("parameter:", String.valueOf(rect.left) + " " + String.valueOf(rect.top) + " " + String.valueOf(cut_width) + " " + String.valueOf(cut_height));

        Bitmap tempCutBitmap = Bitmap.createBitmap(bitmap, rect.left, rect.top, cut_width, cut_height);
        Log.d("cut finished", "");
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(Color.BLACK);
        Bitmap temp = Bitmap.createBitmap(cut_width, cut_height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(temp);

        Path path1 = new Path();
        if (path.size() > 1) {
            path1.moveTo((float) (path.pathX.get(0) - rect.left), (float) (path.pathY.get(0) - rect.top));
            for (int i = 1; i < path.size(); i++) {
                path1.lineTo((float) (path.pathX.get(i) - rect.left), (float) (path.pathY.get(i) - rect.top));
            }
        }
        canvas.drawPath(path1, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        canvas.drawBitmap(tempCutBitmap, 0, 0, paint);
        return temp;
    }
}
