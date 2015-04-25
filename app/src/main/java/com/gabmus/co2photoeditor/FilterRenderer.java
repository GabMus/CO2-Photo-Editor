package com.gabmus.co2photoeditor;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.security.Timestamp;
import java.sql.Time;
import java.util.Calendar;
import java.util.Random;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.egl.EGLConfig;


public class FilterRenderer implements GLSurfaceView.Renderer
{

    private int hShaderProgramBase;
    private int hShaderProgramFinalPass;
    private int hShaderProgramBlackAndWhite;
    private int hShaderProgramSepia;
    private int hShaderProgramToneMapping;
    private int hShaderProgramCathodeRayTube;
    private int hShaderProgramFilmGrain;
    private int hShaderProgramProperFilmGrain;
    private int hShaderProgramNegative;


    public boolean PARAMS_EnableProperFilmGrain = false;
    public float PARAMS_ProperFilmGrainStrength = 0.8f;
    public float PARAMS_ProperFilmGrainAccentuateDarkNoisePower = 4f;
    public float PARAMS_ProperFilmGrainRandomNoiseStrength = 1.2f;
    public float PARAMS_ProperFilmGrainRandomValue = 0.1f;

    public boolean PARAMS_EnableNegative = false;
    public boolean PARAMS_EnableBlackAndWhite = false;
    public boolean PARAMS_EnableSepia = false;

    public boolean PARAMS_EnableToneMapping = false;
    public float PARAMS_ToneMappingExposure = 2.0f;
    public float PARAMS_ToneMappingVignetting = 1.0f;

    public boolean PARAMS_EnableCathodeRayTube = false;
    public int PARAMS_CathodeRayTubeLineWidth = 1;
    public boolean PARAMS_CathodeRayTubeIsHorizontal = false;

    public boolean PARAMS_EnableFilmGrain = false;
    public float PARAMS_FilmGrainAmount = 0.35f;
    public float PARAMS_FilmGrainParticleSize = 2.6f;
    public float PARAMS_FilmGrainLuminance = 1f;
    public float PARAMS_FilmGrainColorAmount = 0.0f;
    public float PARAMS_FilmGrainSeed = 0.0f;

    public boolean BOOL_LoadTexture = false;
    public RenderTarget2D target1, target2, saveTarget;
    private FilterSurfaceView fsv;

    public int ImageWidth = 0;
    public int ImageHeigth = 0;

    public boolean SaveImage = false;
    public String SavePath = "";

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
        if (MainActivity.gotSharedPic) {
            fsv.LoadBitmap(MainActivity.sharedPicBmp);
        }
        else {
            fsv.LoadBitmap(fsv.generateTestBitmap());
        }
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

        //CRT
        String crt_FS =
                "precision mediump float;" +
                        "uniform sampler2D filteredPhoto;" +
                        "uniform int horizontal;\n" +
                        "uniform int linewidth;\n" +
                        "uniform float pixheigth;\n" +
                        "uniform float pixwidth;\n" +
                        "varying vec2 UV;" +
                        "" +
                        "" +
                        "void main() {\n" +
                        "vec4 c;\n" +
                        "    float fv;\n" +
                "    float val;\n" +
                "    c = texture2D( filteredPhoto, UV);\n" +
                "    if ( (UV.x  > 0.000000) ){\n" +
                "        fv = (UV.x  / pixwidth);\n" +
                "        if ( horizontal == 1 ){\n" +
                "            fv = (UV.y  / pixheigth);\n" +
                "        }\n" +
                "        val = mod(float(fv), float(3 * linewidth));\n" +
                        "val = val -0.008f;" +
                "        if ( ((val >= 0.0) && (val < float(linewidth))) ){\n" +
                "            c = vec4( c.x , 0.000000, 0.000000, 1.00000);\n" +
                "        }\n" +
                "        else{\n" +
                "            if ( ((val >= float(linewidth)) && (val < float(linewidth * 2))) ){\n" +
                "                c = vec4( 0.000000, c.y , 0.000000, 1.00000);\n" +
                "            }\n" +
                "            else{\n" +

                "                c =  vec4(0.000000, 0.000000, c.z , 1.00000);\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                        "    gl_FragColor = c;\n" +
                        "}";
        hShaderProgramCathodeRayTube = createprogram(generalreverseVS, crt_FS);

        //NEGATIVE
        String neg_FS =
                "precision mediump float;" +
                        "uniform sampler2D filteredPhoto;" +
                        "varying vec2 UV;" +
                        "void main() {" +
                        "  vec4 col = texture2D(filteredPhoto, UV);" +
                        "  gl_FragColor = vec4(1.0 - col.r, 1.0 - col.g, 1.0 - col.b, 1);" +
                        "}";
        hShaderProgramNegative = createprogram(generalreverseVS, neg_FS);

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

        //ToneMapping+
        String tonemapping_FS =
                "precision mediump float;" +
                        "uniform sampler2D filteredPhoto;" +
                        "uniform float exposure;" +
                        "uniform float vign;" +
                        "varying vec2 UV;" +
                        "void main() {" +
                        "vec4 color;\n" +
                        "float vignette;\n" +
                        "vec2 vtc = vec2( (UV - 0.500000));" +
                        "color = texture2D( filteredPhoto, UV);\n" +
                        "vignette = pow( (1.00000 - (dot( vtc, vtc) * vign)), 2.00000);\n" +
                        "gl_FragColor = vec4(color.r * vignette, color.g * vignette, color.b * vignette, 1);" +
                        "}";

        hShaderProgramToneMapping = createprogram(generalreverseVS, tonemapping_FS);


        //Film Grain
        String filmGrain_FS =
                "precision mediump float;" +
                        "uniform sampler2D filteredPhoto; //rendered scene sampler\n" +
                        "uniform float bgl_RenderedTextureWidth; //scene sampler width\n" +
                        "uniform float bgl_RenderedTextureHeight; //scene sampler height\n" +
                        "uniform float timer;\n" +
                        "uniform float grainamount; //grain amount\n" +
                        "uniform float coloramount;\n" +
                        "uniform float grainsize;\n" +
                        "uniform float lumamount;\n" +
                        "\n" +
                        "float permTexUnit = 1.0/ 1024.0;\t\t// Perm texture texel-size\n" +
                        "float permTexUnitHalf = 0.5/1024.0;\t// Half perm texture texel-size\n" +
                        "\n" +
                        "float width = bgl_RenderedTextureWidth;\n" +
                        "float height = bgl_RenderedTextureHeight;\n" +
                        "\n" +
                        "varying vec2 UV;" +
                        "    \n" +
                        "//a random texture generator, but you can also use a pre-computed perturbation texture\n" +
                        "vec4 rnm(in vec2 tc) \n" +
                        "{\n" +
                        "    float noise =  sin(dot(tc + vec2(timer,timer),vec2(12.9898,78.233))) * 43758.5453;\n" +
                        "\n" +
                        "\tfloat noiseR =  fract(noise)*2.0-1.0;\n" +
                        "\tfloat noiseG =  fract(noise*1.2154)*2.0-1.0; \n" +
                        "\tfloat noiseB =  fract(noise*1.3453)*2.0-1.0;\n" +
                        "\tfloat noiseA =  fract(noise*1.3647)*2.0-1.0;\n" +
                        "\t\n" +
                        "\treturn vec4(noiseR,noiseG,noiseB,noiseA);\n" +
                        "}\n" +
                        "\n" +
                        "float fade(in float t) {\n" +
                        "\treturn t*t*t*(t*(t*6.0-15.0)+10.0);\n" +
                        "}\n" +
                        "\n" +
                        "float pnoise3D(in vec3 p)\n" +
                        "{\n" +
                        "\tvec3 pi = permTexUnit*floor(p)+permTexUnitHalf; // Integer part, scaled so +1 moves permTexUnit texel\n" +
                        "\t// and offset 1/2 texel to sample texel centers\n" +
                        "\tvec3 pf = fract(p);     // Fractional part for interpolation\n" +
                        "\n" +
                        "\t// Noise contributions from (x=0, y=0), z=0 and z=1\n" +
                        "\tfloat perm00 = rnm(pi.xy).a ;\n" +
                        "\tvec3  grad000 = rnm(vec2(perm00, pi.z)).rgb * 4.0 - 1.0;\n" +
                        "\tfloat n000 = dot(grad000, pf);\n" +
                        "\tvec3  grad001 = rnm(vec2(perm00, pi.z + permTexUnit)).rgb * 4.0 - 1.0;\n" +
                        "\tfloat n001 = dot(grad001, pf - vec3(0.0, 0.0, 1.0));\n" +
                        "\n" +
                        "\t// Noise contributions from (x=0, y=1), z=0 and z=1\n" +
                        "\tfloat perm01 = rnm(pi.xy + vec2(0.0, permTexUnit)).a ;\n" +
                        "\tvec3  grad010 = rnm(vec2(perm01, pi.z)).rgb * 4.0 - 1.0;\n" +
                        "\tfloat n010 = dot(grad010, pf - vec3(0.0, 1.0, 0.0));\n" +
                        "\tvec3  grad011 = rnm(vec2(perm01, pi.z + permTexUnit)).rgb * 4.0 - 1.0;\n" +
                        "\tfloat n011 = dot(grad011, pf - vec3(0.0, 1.0, 1.0));\n" +
                        "\n" +
                        "\t// Noise contributions from (x=1, y=0), z=0 and z=1\n" +
                        "\tfloat perm10 = rnm(pi.xy + vec2(permTexUnit, 0.0)).a ;\n" +
                        "\tvec3  grad100 = rnm(vec2(perm10, pi.z)).rgb * 4.0 - 1.0;\n" +
                        "\tfloat n100 = dot(grad100, pf - vec3(1.0, 0.0, 0.0));\n" +
                        "\tvec3  grad101 = rnm(vec2(perm10, pi.z + permTexUnit)).rgb * 4.0 - 1.0;\n" +
                        "\tfloat n101 = dot(grad101, pf - vec3(1.0, 0.0, 1.0));\n" +
                        "\n" +
                        "\t// Noise contributions from (x=1, y=1), z=0 and z=1\n" +
                        "\tfloat perm11 = rnm(pi.xy + vec2(permTexUnit, permTexUnit)).a ;\n" +
                        "\tvec3  grad110 = rnm(vec2(perm11, pi.z)).rgb * 4.0 - 1.0;\n" +
                        "\tfloat n110 = dot(grad110, pf - vec3(1.0, 1.0, 0.0));\n" +
                        "\tvec3  grad111 = rnm(vec2(perm11, pi.z + permTexUnit)).rgb * 4.0 - 1.0;\n" +
                        "\tfloat n111 = dot(grad111, pf - vec3(1.0, 1.0, 1.0));\n" +
                        "\n" +
                        "\t// Blend contributions along x\n" +
                        "\tvec4 n_x = mix(vec4(n000, n001, n010, n011), vec4(n100, n101, n110, n111), fade(pf.x));\n" +
                        "\n" +
                        "\t// Blend contributions along y\n" +
                        "\tvec2 n_xy = mix(n_x.xy, n_x.zw, fade(pf.y));\n" +
                        "\n" +
                        "\t// Blend contributions along z\n" +
                        "\tfloat n_xyz = mix(n_xy.x, n_xy.y, fade(pf.z));\n" +
                        "\n" +
                        "\t// We're done, return the final noise value.\n" +
                        "\treturn n_xyz;\n" +
                        "}\n" +
                        "\n" +
                        "//2d coordinate orientation thing\n" +
                        "vec2 coordRot(in vec2 tc, in float angle)\n" +
                        "{\n" +
                        "\tfloat aspect = width/height;\n" +
                        "\tfloat rotX = ((tc.x*2.0-1.0)*aspect*cos(angle)) - ((tc.y*2.0-1.0)*sin(angle));\n" +
                        "\tfloat rotY = ((tc.y*2.0-1.0)*cos(angle)) + ((tc.x*2.0-1.0)*aspect*sin(angle));\n" +
                        "\trotX = ((rotX/aspect)*0.5+0.5);\n" +
                        "\trotY = rotY*0.5+0.5;\n" +
                        "\treturn vec2(rotX,rotY);\n" +
                        "}\n" +
                        "\n" +
                        "void main() \n" +
                        "{\n" +
                        "\tvec2 texCoord = UV;\n" +
                        "\t\n" +
                        "\tvec3 rotOffset = vec3(1.425,3.892,5.835); //rotation offset values\t\n" +
                        "\tvec2 rotCoordsR = coordRot(texCoord, timer + rotOffset.x);\n" +
                        "\tvec3 noise = vec3(pnoise3D(vec3(rotCoordsR*vec2(width/grainsize,height/grainsize),0.0)));\n" +
                        "  \n" +
                        "\tif (coloramount > 0.0)\n" +
                        "\t{\n" +
                        "\t\tvec2 rotCoordsG = coordRot(texCoord, timer + rotOffset.y);\n" +
                        "\t\tvec2 rotCoordsB = coordRot(texCoord, timer + rotOffset.z);\n" +
                        "\t\tnoise.g = mix(noise.r,pnoise3D(vec3(rotCoordsG*vec2(width/grainsize,height/grainsize),1.0)),coloramount);\n" +
                        "\t\tnoise.b = mix(noise.r,pnoise3D(vec3(rotCoordsB*vec2(width/grainsize,height/grainsize),2.0)),coloramount);\n" +
                        "\t}\n" +
                        "\n" +
                        "\tvec3 col = texture2D(filteredPhoto, texCoord).rgb;\n" +
                        "\n" +
                        "\t//noisiness response curve based on scene luminance\n" +
                        "\tvec3 lumcoeff = vec3(0.299,0.587,0.114);\n" +
                        "\tfloat luminance = mix(0.0,dot(col, lumcoeff),lumamount);\n" +
                        "\tfloat lum = smoothstep(0.2,0.0,luminance);\n" +
                        "\tlum += luminance;\n" +
                        "\t\n" +
                        "\t\n" +
                        "\tnoise = mix(noise,vec3(0.0),pow(lum,4.0));\n" +
                        "\tcol = col+noise*grainamount;\n" +
                        "   \n" +
                        "\tgl_FragColor =  vec4(col,1.0);\n" +
                        "}";
        hShaderProgramFilmGrain = createprogram(generalreverseVS, filmGrain_FS);

        //Proper Film Grain
        String properFilmGrain_FS =
                "precision mediump float;" +
                        "uniform sampler2D filteredPhoto;" +
                        "uniform float accentuateDarkNoisePower;\n" +
                        "uniform float filmGrainStrength;\n" +
                        "uniform float randomNoiseStrength;\n" +
                        "uniform float randomValue;\n" +
                        "varying vec2 UV;" +
                        "float xlat_lib_saturate( float x) {\n" +
                        "  return clamp( x, 0.0, 1.0);\n" +
                        "}\n" +
                        "vec3 FilmGrain( vec3 color, vec2 uv );\n" +
                        "\n" +
                        "\n" +
                        "vec3 FilmGrain( vec3 color, vec2 uv ) {\n" +
                        "    float x;\n" +
                        "    float dx;\n" +
                        "    float y;\n" +
                        "    float dy;\n" +
                        "    float noise;\n" +
                        "    float accentuateDarkNoise;\n" +
                        "\n" +
                        "    x = ((uv.x  * uv.y ) * 50000.0);\n" +
                        "    x = mod( x, 13.0000);\n" +
                        "    x = (x * x);\n" +
                        "    dx = mod( x, 0.0100000);\n" +
                        "    y = ((x * randomValue) + randomValue);\n" +
                        "    dy = mod( y, 0.0100000);\n" +
                        "    noise = (xlat_lib_saturate( (0.100000 + (dx * 100.000)) ) + (xlat_lib_saturate( (0.100000 + (dy * 100.000)) ) * randomNoiseStrength));\n" +
                        "    noise = ((noise * 2.00000) - 1.00000);\n" +
                        "    accentuateDarkNoise = pow( (1.00000 - ((color.x  + color.y  + color.z ) / 3.00000)), accentuateDarkNoisePower);\n" +
                        "    return (color + (((color * noise) * accentuateDarkNoise) * filmGrainStrength));\n" +
                        "}\n" +
                        "void main() {\n" +
                        "    vec3 xlat_retVal = vec3(texture2D(filteredPhoto, UV));\n" +
                        "   " +
                        "    xlat_retVal = FilmGrain( vec3(xlat_retVal), vec2(UV));\n" +
                        "\n" +
                        "    gl_FragColor = vec4( xlat_retVal, 1.0);" +
                        "}\n";
        hShaderProgramProperFilmGrain = createprogram(generalreverseVS, properFilmGrain_FS);

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

            if (fsv.toLoad != null) {
                fsv.LoadTexture(fsv.toLoad);
                int scrW = fsv.getWidth();
                int scrH = fsv.getHeight();
                float wRat = (float) ImageWidth / (float) scrW;
                float hRat = (float) ImageHeigth / (float) scrH;
                boolean majW = wRat > hRat ? true : false;
                float aspect = (float) (majW ? (float) ImageWidth / (float) ImageHeigth : (float) ImageHeigth / (float) ImageWidth);
                if (majW) {
                    cmp_W = scrW;
                    cmp_X = 0;
                    cmp_H = (int) ((float) scrW / aspect);
                    cmp_Y = (int) (((float) scrH - (float) cmp_H) / 2f);
                    //throw(new RuntimeException("IW: " + ImageWidth + "\nIH: " + ImageHeigth + "\nwRat = " + wRat + "\nhRat = " + hRat + "\nX: " + cmp_X + "\nY: " + cmp_Y + "\nW: " + cmp_W + "\nH: " + cmp_H + "\nscW: " + scrW + "\nscH: " + scrH));
                } else {
                    cmp_H = scrH;
                    cmp_Y = 0;
                    cmp_W = (int) ((float) scrH / (float) aspect);
                    cmp_X = (int) (((float) scrW - (float) cmp_W) / 2f);
                }
            }
            BOOL_LoadTexture = false;
        }
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        if (PARAMS_EnableCathodeRayTube) {
            SetRenderTarget();
            GLES20.glUseProgram(hShaderProgramCathodeRayTube);
            setVSParams(hShaderProgramCathodeRayTube);
            setShaderParamPhoto(hShaderProgramCathodeRayTube, GetCurTexture());
            int pxw = GLES20.glGetUniformLocation(hShaderProgramCathodeRayTube, "pixwidth");
            int pxh = GLES20.glGetUniformLocation(hShaderProgramCathodeRayTube, "pixheigth");
            int hrz = GLES20.glGetUniformLocation(hShaderProgramCathodeRayTube, "horizontal");
            int lnw = GLES20.glGetUniformLocation(hShaderProgramCathodeRayTube, "linewidth");
            if (pxw < 0 || pxh < 0 || hrz < 0 || lnw < 0) throw (new RuntimeException("ff"));
            //if (true) throw new RuntimeException("pxw = " + (float)(1f / (float)ImageWidth));
            GLES20.glUniform1f(pxw, (float) (1f / (float) ImageWidth));
            GLES20.glUniform1f(pxh, (float) (1f / (float) ImageHeigth));
            GLES20.glUniform1i(lnw, PARAMS_CathodeRayTubeLineWidth);
            GLES20.glUniform1i(hrz, PARAMS_CathodeRayTubeIsHorizontal ? 1 : 0);
            drawquad();
        }
        if (PARAMS_EnableNegative) {
            SetRenderTarget();
            GLES20.glUseProgram(hShaderProgramNegative);
            setVSParams(hShaderProgramNegative);
            setShaderParamPhoto(hShaderProgramNegative, GetCurTexture());
            drawquad();
        }
        if (PARAMS_EnableBlackAndWhite) {
            SetRenderTarget();
            GLES20.glUseProgram(hShaderProgramBlackAndWhite);
            setVSParams(hShaderProgramBlackAndWhite);
            setShaderParamPhoto(hShaderProgramBlackAndWhite, GetCurTexture());
            drawquad();
        }
        if (PARAMS_EnableSepia) {
            SetRenderTarget();
            GLES20.glUseProgram(hShaderProgramSepia);
            setVSParams(hShaderProgramSepia);
            setShaderParamPhoto(hShaderProgramSepia, GetCurTexture());
            drawquad();
        }
        if (PARAMS_EnableToneMapping && (PARAMS_ToneMappingExposure != 0 || PARAMS_ToneMappingVignetting != 0)) {
            SetRenderTarget();
            GLES20.glUseProgram(hShaderProgramToneMapping);
            setVSParams(hShaderProgramToneMapping);
            setShaderParamPhoto(hShaderProgramToneMapping, GetCurTexture());
            int exposure = GLES20.glGetUniformLocation(hShaderProgramToneMapping, "exposure");
            int vign = GLES20.glGetUniformLocation(hShaderProgramToneMapping, "vign");
            GLES20.glUniform1f(exposure, PARAMS_ToneMappingExposure);
            GLES20.glUniform1f(vign, PARAMS_ToneMappingVignetting);
            drawquad();
        }
        if (PARAMS_EnableFilmGrain) {
            SetRenderTarget();
            GLES20.glUseProgram(hShaderProgramFilmGrain);
            setVSParams(hShaderProgramFilmGrain);
            setShaderParamPhoto(hShaderProgramFilmGrain, GetCurTexture());
            int w = GLES20.glGetUniformLocation(hShaderProgramFilmGrain, "bgl_RenderedTextureWidth");
            int h = GLES20.glGetUniformLocation(hShaderProgramFilmGrain, "bgl_RenderedTextureHeight");
            int t = GLES20.glGetUniformLocation(hShaderProgramFilmGrain, "timer");

            int ga = GLES20.glGetUniformLocation(hShaderProgramFilmGrain, "grainamount");
            int ca = GLES20.glGetUniformLocation(hShaderProgramFilmGrain, "coloramount");
            int gs = GLES20.glGetUniformLocation(hShaderProgramFilmGrain, "grainsize");
            int la = GLES20.glGetUniformLocation(hShaderProgramFilmGrain, "lumamount");

            GLES20.glUniform1f(w, ImageWidth);
            GLES20.glUniform1f(h, ImageHeigth);
            GLES20.glUniform1f(ga, PARAMS_FilmGrainAmount);
            GLES20.glUniform1f(gs, PARAMS_FilmGrainParticleSize);
            GLES20.glUniform1f(la, PARAMS_FilmGrainLuminance);
            GLES20.glUniform1f(ca, PARAMS_FilmGrainColorAmount);
            GLES20.glUniform1f(t, PARAMS_FilmGrainSeed);
            drawquad();
        }
        if (PARAMS_EnableProperFilmGrain) {
            SetRenderTarget();
            GLES20.glUseProgram(hShaderProgramProperFilmGrain);
            setVSParams(hShaderProgramProperFilmGrain);
            setShaderParamPhoto(hShaderProgramProperFilmGrain, GetCurTexture());

            int adnp = GLES20.glGetUniformLocation(hShaderProgramProperFilmGrain, "accentuateDarkNoisePower");
            int fgs = GLES20.glGetUniformLocation(hShaderProgramProperFilmGrain, "filmGrainStrength");
            int rns = GLES20.glGetUniformLocation(hShaderProgramProperFilmGrain, "randomNoiseStrength");
            int rv = GLES20.glGetUniformLocation(hShaderProgramProperFilmGrain, "randomValue");


            //int v = 1;
            //if (adnp < v || fgs < v || rns < v || rv < v)
            //    throw new RuntimeException("" + adnp + " " + fgs + " " + rns + " " + rv );

            GLES20.glUniform1f(adnp, PARAMS_ProperFilmGrainAccentuateDarkNoisePower);
            GLES20.glUniform1f(fgs, PARAMS_ProperFilmGrainStrength);
            GLES20.glUniform1f(rns, PARAMS_ProperFilmGrainRandomNoiseStrength);
            GLES20.glUniform1f(rv, PARAMS_ProperFilmGrainRandomValue);

            drawquad();
        }

        if (didshit)
            firstshit = false;
        first = !first;

        int tx = GetCurTexture();



        if (SaveImage) {
            SaveImage = false;

            saveTarget.Set();
            GLES20.glUseProgram(hShaderProgramFinalPass);
            setVSParams(hShaderProgramFinalPass);
            setShaderParamPhoto(hShaderProgramFinalPass, tx);
            drawquad();

            saveTarget.pfsave();
            int wd = saveTarget.Width;
            int hg = saveTarget.Height;

            int screenshotSize = wd * hg;
            ByteBuffer bb = ByteBuffer.allocateDirect(screenshotSize * 4);
            bb.order(ByteOrder.nativeOrder());
            GLES20.glReadPixels(0, 0, wd, hg, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, bb);
            int pixelsBuffer[] = new int[screenshotSize];
            bb.asIntBuffer().get(pixelsBuffer);
            bb = null;
            Bitmap bitmap = Bitmap.createBitmap(wd, hg, Bitmap.Config.RGB_565);
            bitmap.setPixels(pixelsBuffer, screenshotSize - wd, -wd, 0, 0, wd, hg);
            pixelsBuffer = null;

            short sBuffer[] = new short[screenshotSize];
            ShortBuffer sb = ShortBuffer.wrap(sBuffer);
            bitmap.copyPixelsToBuffer(sb);

            for (int i = 0; i < screenshotSize; ++i) {
                short v = sBuffer[i];
                sBuffer[i] = (short) (((v & 0x1f) << 11) | (v & 0x7e0) | ((v & 0xf800) >> 11));
            }
            sb.rewind();
            bitmap.copyPixelsFromBuffer(sb);


            File file = new File(SavePath);

            FileOutputStream fOut = null;
            try {
                fOut = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fOut);

            //call media scanner to show the new picture in the gallery

            MediaScannerConnection.scanFile(
                    fsv.activity,
                    new String[]{SavePath},
                    null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        @Override
                        public void onScanCompleted(String path, Uri uri) {
                            Log.v("CO2 Photo Editor ",
                                    "file " + path + " was scanned seccessfully: " + uri);
                        }
                    });
                try {
                    fOut.flush();
                    fOut.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

        }
        RenderTarget2D.SetDefault(cmp_X, cmp_Y, cmp_W, cmp_H);
        GLES20.glUseProgram(hShaderProgramFinalPass);
        setVSParams(hShaderProgramFinalPass);
        setShaderParamPhoto(hShaderProgramFinalPass, tx);
        drawquad();
        didshit = false;
        firstshit = true;
    }

    public void setPARAMS_FilmGrainSeed(float v)
    {
        Random r = new Random();
        v*= 10;
        float a = (float)(r.nextInt(10) - 1);
        v+=a;
        PARAMS_FilmGrainSeed = v;
    }

    public int rtid;
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
        if (first) {rtid = 1;return target1.GetTex();}
        else { rtid = 2;return target2.GetTex();}
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
