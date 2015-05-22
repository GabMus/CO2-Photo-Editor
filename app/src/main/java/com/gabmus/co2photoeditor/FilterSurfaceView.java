package com.gabmus.co2photoeditor;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.MotionEvent;

import java.lang.Math;

class FilterSurfaceView extends GLSurfaceView
{

    public final FilterRenderer renderer;
    public MainActivity activity;

    public FilterSurfaceView(Context context, MainActivity act)
    {
        super(context);
        int[] tmpVal = new int[1];
        setEGLContextClientVersion(2);
        activity = act;
        renderer = new FilterRenderer(this);
        setRenderer(renderer);
    }

    public Bitmap toLoad = null;


    public boolean IsUsedBitmap()
    {
        return !default_b;
    }
    private boolean startup = true;
    private boolean default_b = true;
    public void LoadBitmap(Bitmap bmp)
    {
        if (!startup && default_b) default_b = false;
        startup = false;
        if ((bmp.getHeight()*bmp.getWidth())>=7900001) { //if pic bigger then 8Mpx resize to 8Mpx
            double h = bmp.getHeight();
            double b = bmp.getWidth();
            double y = b/h;
            double x = 7900000;
            h=Math.sqrt(x/y);
            b=x/(h);
            bmp= Bitmap.createScaledBitmap(bmp, (int)b, (int)h, true);
        }
        toLoad = bmp;
        renderer.BOOL_LoadTexture = true;
    }
    public void LoadTexture(Bitmap bmp)
    {
        renderer.ImageHeigth = bmp.getHeight();
        renderer.ImageWidth = bmp.getWidth();
        renderer.hToFilterTexture = loadTexture(bmp);
        if (renderer.target1 != null)
        renderer.target1.Release();
        if (renderer.target2 != null)
        renderer.target2.Release();
        if (renderer.saveTarget != null)
            renderer.saveTarget.Release();
        if (renderer.blur1 != null) renderer.blur1.Release();
        if (renderer.blur2 != null) renderer.blur2.Release();
        renderer.target1 = new RenderTarget2D(bmp.getWidth(), bmp.getHeight());
        renderer.target2 = new RenderTarget2D(bmp.getWidth(), bmp.getHeight());
        renderer.saveTarget = new RenderTarget2D(bmp.getWidth(), bmp.getHeight());
        renderer.blur1 = new RenderTarget2D(bmp.getWidth() /2, bmp.getHeight() / 2);
        renderer.blur2 = new RenderTarget2D(bmp.getWidth() / 2, bmp.getHeight() / 2);
        renderer.Render();
    }

    private int[] loadTexture(Bitmap bitmap)
    {
        final int[] textureHandle = new int[1];


        GLES20.glGenTextures(1, textureHandle, 0);
        if (textureHandle[0] == 0)throw(new RuntimeException("SHEEEEET"));

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        activity.helper.currentBitmap=bitmap;
        //bitmap.recycle();


        return textureHandle;
    }

    public void SaveImage(String location)
    {
        renderer.SaveImage = true;
        renderer.SavePath = location;
    }


    public Bitmap generateTestBitmap()
    {
        int width = getWidth();
        int height = getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixel(10, 10, 0xFFFFFFFF);
        Canvas c = new Canvas(bitmap);
        Paint p = new Paint();
        p.setColor(0x0000FFFF);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++)
            {
                bitmap.setPixel(x, y, 0xFF000000);
            }
        }

        //Bitmap bemp = BitmapFactory.decodeResource(getResources(), R.drawable.abc_list_selector_disabled_holo_light);
        return bitmap;
    }

}
