package com.tuputech.component.ui.visitor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;
import android.util.Log;

import com.tuputech.common.TupuTech;
import com.tuputech.component.ui.ScriptC_rotator;

/**
 * Created by RogerOu on 2018/5/22.
 */
public class Yuv2BitmapConverter {

    private RenderScript mRenderScript;
    private ScriptIntrinsicYuvToRGB mYUVtoRGBScript;
    private ScriptC_rotator mScriptC_rotator;

    public Yuv2BitmapConverter(Context context) {
        mRenderScript = RenderScript.create(context);
        mYUVtoRGBScript = ScriptIntrinsicYuvToRGB.create(mRenderScript, Element.U8_4(mRenderScript));
        mScriptC_rotator = new ScriptC_rotator(mRenderScript);
    }

    public Bitmap convertNv21ToRGBA(byte[] nv21, int srcWidth, int srcHeight, boolean mirror, TupuTech.Rect rect, int degree) {
        return convertNv21ToRGBA(nv21, srcWidth, srcHeight, mirror, rect, degree, 1f);
    }

    public Bitmap convertNv21ToRGBA(byte[] nv21, int srcWidth, int srcHeight, boolean mirror, TupuTech.Rect rect, int degree, float scale) {

        Rect cropRect = null;
        int targetHeight;
        int targetWidth;
        if (degree == 90 || degree == 270) {
            targetWidth = srcHeight;
            targetHeight = srcWidth;
        } else {
            targetWidth = srcWidth;
            targetHeight = srcHeight;
        }


        if (rect != null) {
            float cropL = rect.getLeft() < 0 ? 0 : rect.getLeft();
            float cropT = rect.getTop() < 0 ? 0 : rect.getTop();
            float cropR = rect.getRight() > targetWidth ? targetWidth : rect.getRight();
            float cropB = rect.getBottom() > targetHeight ? targetHeight : rect.getBottom();

            float cropW = cropR - cropL;
            float cropH = cropB - cropT;
            cropL = (cropL - cropW*(scale-1)/2) < 0 ? 0 : cropL - cropW*(scale-1)/2;
            cropR = (cropR + cropW*(scale-1)/2) > targetWidth ? targetWidth : cropR + cropW*(scale-1)/2;
            cropT = (cropT - cropH*(scale-1)/2) < 0 ? 0 : cropT - cropH*(scale-1)/2;
            cropB = (cropB + cropW*(scale-1)/2) > targetHeight ? targetHeight : cropB + cropW*(scale-1)/2;

            cropRect = new Rect((int) cropL, (int) cropT, (int) cropR, (int) cropB);

        }


        if (mYUVtoRGBScript == null) {
            mYUVtoRGBScript = ScriptIntrinsicYuvToRGB.create(mRenderScript, Element.U8_4(mRenderScript));
        }
        Type.Builder yuvBuilder = new Type.Builder(mRenderScript, Element.U8(mRenderScript))
                .setX(nv21.length);
        Allocation in = Allocation.createTyped(mRenderScript, yuvBuilder.create(), Allocation.USAGE_SCRIPT);


        Type.Builder rgba = new Type.Builder(mRenderScript, Element.RGBA_8888(mRenderScript)).setX(srcWidth).setY(srcHeight);
        Allocation out = Allocation.createTyped(mRenderScript, rgba.create(), Allocation.USAGE_SCRIPT);

        in.copyFrom(nv21);

        mYUVtoRGBScript.setInput(in);
        mYUVtoRGBScript.forEach(out);


        Bitmap bitmap1 = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888);
        out.copyTo(bitmap1);

        if (degree == 0 && cropRect == null && !mirror) {
            return bitmap1;
        }

        mScriptC_rotator.set_inWidth(bitmap1.getWidth());
        mScriptC_rotator.set_inHeight(bitmap1.getHeight());
        Allocation sourceAllocation = Allocation.createFromBitmap(mRenderScript, bitmap1,
                Allocation.MipmapControl.MIPMAP_NONE,
                Allocation.USAGE_SCRIPT);

        mScriptC_rotator.set_inImage(sourceAllocation);

        Bitmap.Config config = bitmap1.getConfig();
        bitmap1.recycle();
        Bitmap target = Bitmap.createBitmap(targetWidth, targetHeight, config);
        final Allocation targetAllocation = Allocation.createFromBitmap(mRenderScript, target,
                Allocation.MipmapControl.MIPMAP_NONE,
                Allocation.USAGE_SCRIPT);

        if (degree == 270) {
            mScriptC_rotator.forEach_rotate_90_clockwise(targetAllocation, targetAllocation);
        } else if (degree == 90) {
            mScriptC_rotator.forEach_rotate_270_clockwise(targetAllocation, targetAllocation);
        }

        targetAllocation.copyTo(target);


        if (cropRect != null && mirror) {

            Matrix matrix = new Matrix();
            matrix.setScale(-1, 1);
            Bitmap bitmap = Bitmap.createBitmap(target, 0, 0, targetWidth, targetHeight, matrix, true);
            target.recycle();
            Bitmap result = Bitmap.createBitmap(bitmap, cropRect.left, cropRect.top, cropRect.width(), cropRect.height());
            bitmap.recycle();
            return result;
        }

        if (cropRect != null) {
            Bitmap bitmap = Bitmap.createBitmap(target, cropRect.left, cropRect.top, cropRect.width(), cropRect.height());
            target.recycle();
            return bitmap;
        }

        return target;

    }

    public Bitmap convertNv21ToRGBA(byte[] nv21, int srcWidth, int srcHeight, int degree) {
        return convertNv21ToRGBA(nv21, srcWidth, srcHeight, false, null, degree);

    }


    public Bitmap convertNv21ToRGBA(byte[] nv21, int srcWidth, int srcHeight) {
        return convertNv21ToRGBA(nv21, srcWidth, srcHeight, 0);
    }

}
