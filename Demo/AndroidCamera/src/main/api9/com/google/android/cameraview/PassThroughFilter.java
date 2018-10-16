package com.google.android.cameraview;

/**
 * Created by drakedan on 2018/5/29.
 */

public class PassThroughFilter extends BaseImageFilter {

    private static final String KPassThroughVertexShader =
            "attribute vec4 vPosition;\n" +
                    "attribute vec4 inputTextureCoordinate;\n" +
                    "varying vec2 textureCoordinate;\n" +
                    " \n" +
                    "void main()\n" +
                    "{\n" +
                    "    gl_Position = vPosition;\n" +
                    "    textureCoordinate = inputTextureCoordinate.xy;\n" +
                    "}";

    private static final String KPassThroughFragmentShader =
            "varying mediump vec2 textureCoordinate;\n" +
                    " \n" +
                    "uniform sampler2D inputImageTexture;\n" +
                    " \n" +
                    "void main()\n" +
                    "{\n" +
                    "     gl_FragColor = texture2D(inputImageTexture, textureCoordinate);\n" +
                    "}";

    public PassThroughFilter() {
        super(KPassThroughVertexShader, KPassThroughFragmentShader);
    }
}
