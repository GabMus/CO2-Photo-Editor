package com.gabmus.co2photoeditor;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.egl.EGLConfig;

class FilterSurfaceView extends GLSurfaceView
{

    public final FilterRenderer renderer;
    public MainActivity activity;

    public FilterSurfaceView(Context context, MainActivity act)
    {

        super(context);
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
        renderer.target1 = new RenderTarget2D(bmp.getWidth(), bmp.getHeight());
        renderer.target2 = new RenderTarget2D(bmp.getWidth(), bmp.getHeight());
        renderer.saveTarget = new RenderTarget2D(bmp.getWidth(), bmp.getHeight());
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
        bitmap.recycle();


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
                bitmap.setPixel(x, y, 0x9FF9F9FF);
            }
        }
        p.setColor(0xFFFFFFFF);
        c.drawLine(2, 52, 152, 350, p);
        p.setColor(0xFF00FFFF);
        p.setTextSize(15);
        c.drawText("CASHMADAFFACCA", 300, 300, p);

        //Bitmap bemp = BitmapFactory.decodeResource(getResources(), R.drawable.abc_list_selector_disabled_holo_light);
        return bitmap;
    }

    public Bitmap generateTestBitmap2()
    {
        int width = 600;
        int height = 800;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixel(10, 10, 0xFFFFFFFF);
        Canvas c = new Canvas(bitmap);
        Paint p = new Paint();
        p.setColor(0xFFFFFFFF);
        c.drawLine(2, 52, 152, 350, p);
        p.setColor(0xFF0FFFFF);
        p.setTextSize(15);
        c.drawText("CASHMADAFFACCA2", 300, 300, p);

        //Bitmap bemp = BitmapFactory.decodeResource(getResources(), R.drawable.abc_list_selector_disabled_holo_light);
        return bitmap;
    }
}
