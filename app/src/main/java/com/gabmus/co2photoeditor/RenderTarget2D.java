package com.gabmus.co2photoeditor;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

public class RenderTarget2D
{
    private int[] fb;
    private int[] depthRb;
    private int[] renderTex;
    private IntBuffer texBuffer;
    public int Width;
    public int Height;

    public RenderTarget2D(int w, int h)
    {
        Width = w; Height = h; generateframebuffer();
    }

    public int GetTex() {return renderTex[0];}
    public boolean Set()
    {
        GLES20.glViewport(0, 0, Width, Height);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fb[0]);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, renderTex[0], 0);
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, depthRb[0]);
        int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        if (status != GLES20.GL_FRAMEBUFFER_COMPLETE)
            throw (new RuntimeException("SHEE"));
        GLES20.glClearColor(.0f, .0f, .0f, 1.0f);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        return true;
    }

    public void pfsave()
    {
        GLES20.glViewport(0, 0, Width, Height);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fb[0]);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, renderTex[0], 0);
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, depthRb[0]);
    }

    public void Release()
    {
        try
        {
            GLES20.glDeleteRenderbuffers(1, depthRb, 0);
            GLES20.glDeleteTextures(1, renderTex, 0);
            GLES20.glDeleteFramebuffers(1, fb, 0);
            GLES20.glFlush();
        }
        catch(Exception e){ }
    }

    public static void SetDefault(int x, int y, int w, int h)
    {
        GLES20.glViewport(x, y, w, h);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        GLES20.glClearColor(.0f, .0f, .0f, 1.0f);
        GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
    }

    private void generateframebuffer()
    {
        fb = new int[1];
        depthRb = new int[1];
        renderTex = new int[1];

        GLES20.glGenFramebuffers(1, fb, 0);
        GLES20.glGenRenderbuffers(1, depthRb, 0);
        GLES20.glGenTextures(1, renderTex, 0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, renderTex[0]);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR);

        int[] buf = new int[Width * Height];
        texBuffer = ByteBuffer.allocateDirect(buf.length
                * 4).order(ByteOrder.nativeOrder()).asIntBuffer();;

        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGB, Width, Height, 0, GLES20.GL_RGB, GLES20.GL_UNSIGNED_SHORT_5_6_5, texBuffer);

        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, depthRb[0]);
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, Width, Height);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fb[0]);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, renderTex[0], 0);
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, depthRb[0]);
    }
}
