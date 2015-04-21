package com.gabmus.co2photoeditor;

import android.app.AlertDialog;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.egl.EGLConfig;


public class FilterRenderer implements GLSurfaceView.Renderer
{
    private int hShaderProgramBase;
    private int hShaderProgramFinalPass;
    private int hShaderProgramBlackAndWhite;
    private int hShaderProgramSepia;

    public boolean PARAMS_EnableBlackAndWhite = false;
    public boolean PARAMS_EnableSepia = true;


    public boolean BOOL_LoadTexture = false;
    public RenderTarget2D target1, target2;
    private FilterSurfaceView fsv;

    public int ImageWidth = 0;
    public int ImageHeigth = 0;

    private FloatBuffer VB;
    private ShortBuffer IB;
    private FloatBuffer TC;

    public int[] hToFilterTexture;

    public FilterRenderer(FilterSurfaceView fsv_) { fsv = fsv_;}

    public void onSurfaceCreated(GL10 unused, EGLConfig config)
    {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        float[] vertices = new float[]
                {
                        -1f,  1f, 0.0f,
                        -1f, -1f, 0.0f,
                         1f, -1f, 0.0f,
                         1f,  1f, 0.0f,
                        -1f,  1f, 0.0f,
                         1f, -1f, 0.0f
                };
        short[] indices = new short[]
                {
                        0, 1, 2, 0, 2, 3
                };
        float[] texCoords =
                {
                        0.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 1.0f,
                        1.0f, 0.0f,
                        0.0f, 0.0f,
                        1.0f, 1.0f
                };

        ByteBuffer bVB = ByteBuffer.allocateDirect(vertices.length * 4); bVB.order(ByteOrder.nativeOrder());
        ByteBuffer bIB = ByteBuffer.allocateDirect(indices.length  * 2); bIB.order(ByteOrder.nativeOrder());
        ByteBuffer bTC = ByteBuffer.allocateDirect(texCoords.length * 4); bTC.order(ByteOrder.nativeOrder());

        VB = bVB.asFloatBuffer();
        IB = bIB.asShortBuffer();
        TC = bTC.asFloatBuffer();
        VB.put(vertices); VB.position(0);
        IB.put(indices); IB.position(0);
        TC.put(texCoords); TC.position(0);
        loadShaders();
        fsv.LoadBitmap(fsv.generateTestBitmap());
    }


    private String generalVS =
            "attribute vec4 vPosition;" +
                    "attribute vec2 texCoords;" +
                    "uniform sampler2D filteredPhoto;" +
                    "varying vec2 UV;" +
                    "void main() {" +
                    "  gl_Position = vPosition;" +
                    "  UV = texCoords;" +
                    "}";
    private String generalreverseVS =
            "attribute vec4 vPosition;" +
                    "attribute vec2 texCoords;" +
                    "uniform sampler2D filteredPhoto;" +
                    "varying vec2 UV;" +
                    "void main() {" +
                    "  gl_Position = vPosition;" +
                    "  vec2 tc = vec2(1, 1) - texCoords;" +
                    "  tc.x = texCoords.x;" +
                    "  UV = tc;" +
                    "}";
    private void loadShaders()
    {

        //BASE SHADER
        String baseshader_FS =
                "precision mediump float;" +
                "varying vec2 UV;" +
                "void main() {" +
                "  gl_FragColor = vec4(UV.x, UV.y, 0, 1);" +
                "}";
        hShaderProgramBase = createprogram(baseshader_FS);

        //Black & White
        String bandw_FS =
                "precision mediump float;" +
                "uniform sampler2D filteredPhoto;" +
                "varying vec2 UV;" +
                "void main() {" +
                "  vec4 col = texture2D(filteredPhoto, UV);" +
                "  float gravg = (col.r + col.g + col.b) / 3.0f;" +
                "  gl_FragColor = vec4(gravg, gravg, gravg, 1);" +
                "}";
        hShaderProgramBlackAndWhite = createprogram(generalreverseVS, bandw_FS);


        //Sepia
        String sepia_FS =
                "precision mediump float;" +
                        "uniform sampler2D filteredPhoto;" +
                        "varying vec2 UV;" +
                        "void main() {" +
                        "  vec4 color = texture2D(filteredPhoto, UV);" +
                        "  gl_FragColor = vec4(1, 1, 1, 1);" +
                        "  gl_FragColor.r = color.r * 0.393f + color.g * 0.769f + color.b * 0.189f;" +
                        "  gl_FragColor.g = color.r * 0.349f + color.g * 0.686f + color.b * 0.168f;" +
                        "  gl_FragColor.b = color.r * 0.272f + color.g * 0.534f + color.b * 0.131f;" +
                        "}";
        hShaderProgramSepia = createprogram(generalreverseVS, sepia_FS);

        //FINALPASS
        String finalPass_FS =
                "precision mediump float;" +
                        "uniform sampler2D filteredPhoto;" +
                        "varying vec2 UV;" +
                        "void main() {" +
                        "  gl_FragColor = texture2D(filteredPhoto, UV);" +
                        "}";
        hShaderProgramFinalPass = createprogram(finalPass_FS);
    }

    private int createprogram(String fssource) { return createprogram(generalVS, fssource);}
    private int createprogram(String vssource, String fssource)
    {
        int toRet;
        toRet = GLES20.glCreateProgram();
        GLES20.glAttachShader(toRet, compileshader(GLES20.GL_VERTEX_SHADER, vssource));
        GLES20.glAttachShader(toRet, compileshader(GLES20.GL_FRAGMENT_SHADER, fssource));
        GLES20.glLinkProgram(toRet);
        return toRet;
    }
    String ERROR = "";
    private int compileshader(int type, String shaderCode)
    {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        ERROR = GLES20.glGetShaderInfoLog(shader);
        if (ERROR.length() > 0)
        throw(new RuntimeException(ERROR));
        return shader;
    }

    private int hPos, hTex;
    private int cmp_W, cmp_H, cmp_X, cmp_Y;
    public void onDrawFrame(GL10 unused) {
        if (BOOL_LoadTexture) {

            if (fsv.toLoad != null)
            {
                fsv.LoadTexture(fsv.toLoad);
                int scrW = fsv.getWidth();
                int scrH = fsv.getHeight();
                float wRat = (float)ImageWidth/(float)scrW;
                float hRat = (float)ImageHeigth/(float)scrH;
                boolean majW = wRat > hRat ? true : false;
                //boolean stretchW = wRat > 1;
                //boolean stretchH = hRat > 1;
                //if (true)
                //    throw(new RuntimeException("IW: " + ImageWidth + "\nIH: " + ImageHeigth + "\nwRat = " + wRat + "\nhRat = " + hRat));
                if (majW)
                {
                    cmp_W = scrW; cmp_X = 0;
                    cmp_H = (int)((float)scrH / (float)wRat);
                    cmp_Y = (int)(((float)scrH-(float)cmp_H) / 2f);
                    //if (true)
                    //    throw(new RuntimeException("IW: " + ImageWidth + "\nIH: " + ImageHeigth + "\nwRat = " + wRat + "\nhRat = " + hRat + "\nX: " + cmp_X + "\nY: " + cmp_Y + "\nW: " + cmp_W + "\nH: " + cmp_H));
                }
                else
                {
                    cmp_H = scrH; cmp_Y = 0;
                    cmp_W = (int)((float)scrW / (float)hRat);
                    cmp_X = (int)(((float)scrW -(float)cmp_W) / 2f);
                    //if (true)
                    //    throw(new RuntimeException("IW: " + ImageWidth + "\nIH: " + ImageHeigth + "\nwRat = " + wRat + "\nhRat = " + hRat + "\nX: " + cmp_X + "\nY: " + cmp_Y + "\nW: " + cmp_W + "\nH: " + cmp_H));
                }


            }
            BOOL_LoadTexture =false;
        }
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        if (PARAMS_EnableBlackAndWhite)
        {
            SetRenderTarget();
            GLES20.glUseProgram(hShaderProgramBlackAndWhite);
            setVSParams(hShaderProgramBlackAndWhite);
            setShaderParamPhoto(hShaderProgramBlackAndWhite, GetCurTexture());
            drawquad();
        }
        if (PARAMS_EnableSepia)
        {
            SetRenderTarget();
            GLES20.glUseProgram(hShaderProgramSepia);
            setVSParams(hShaderProgramSepia);
            setShaderParamPhoto(hShaderProgramSepia, GetCurTexture());
            drawquad();
        }

        if (didshit)
        firstshit= false;
        RenderTarget2D.SetDefault(cmp_X, cmp_Y, cmp_W, cmp_H);
        GLES20.glUseProgram(hShaderProgramFinalPass);
        setVSParams(hShaderProgramFinalPass);
        int tx = GetCurTexture();
        setShaderParamPhoto(hShaderProgramFinalPass, tx);
        drawquad();

        didshit = false;
        firstshit = true;
    }

    private void setShaderParamPhoto(int program, int texID)
    {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texID);
        int loc = GLES20.glGetUniformLocation(program, "filteredPhoto");
        if (loc == -1) throw(new RuntimeException("SHEEEET"));
        GLES20.glUniform1i(loc, 0);
    }
    boolean first = true;
    boolean didshit = false;
    boolean firstshit = true;
    private void SetRenderTarget()
    {
        if (didshit) firstshit = false;
        didshit = true;
        if (first)
        {
            target1.Set();
            first = false;
        }
        else
        {
            target2.Set();
            first = true;
        }
    }
    private int GetCurTexture()
    {
        if (firstshit) return hToFilterTexture[0];
        if (first) return target2.GetTex();
        else return target1.GetTex();
    }

    private void drawquad(){        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
        GLES20.glDisableVertexAttribArray(hPos);
        GLES20.glDisableVertexAttribArray(hTex);}
    private void setVSParams(int program){setVSParamspos(program); setVSParamstc(program);}
    private void setVSParamspos(int program) {
        hPos = GLES20.glGetAttribLocation(program, "vPosition");
        GLES20.glEnableVertexAttribArray(hPos);
        GLES20.glVertexAttribPointer(hPos, 3,
                GLES20.GL_FLOAT, false,
                12, VB);
    }
    private void setVSParamstc(int program) {
        hTex = GLES20.glGetAttribLocation(program, "texCoords");
        GLES20.glEnableVertexAttribArray(hTex);
        GLES20.glVertexAttribPointer(hTex, 2,
                GLES20.GL_FLOAT, false,
                8, TC);
    }

        public void onSurfaceChanged(GL10 unused, int width, int height)
    {
        GLES20.glViewport(0, 0, width, height);
    }
}

