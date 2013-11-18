package com.nickonline.android.nickapp.ui;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import org.andengine.opengl.texture.*;
import org.andengine.opengl.util.GLState;
import org.andengine.util.exception.NullBitmapException;
import org.andengine.util.math.MathUtils;

import java.io.IOException;

/**
 * Created By: Connor Fraser
 */
public class BitmapReferenceTexture extends Texture {
    private final Bitmap bitmap;

    public int getHeight() {
        return bitmap.getHeight();
    }

    public int getWidth() {
        return bitmap.getWidth();
    }


    public BitmapReferenceTexture(TextureManager textureManager, ITextureStateListener listener, Bitmap bitmap) {
        super(textureManager, PixelFormat.RGBA_8888, TextureOptions.NEAREST, listener);

        this.bitmap = bitmap;
    }

    @Override
    protected void writeTextureToHardware(GLState pGLState) throws IOException {
        if (bitmap == null) {
            throw new NullBitmapException("Caused by: '" + this.toString() + "'.");
        }

        final boolean useDefaultAlignment = MathUtils.isPowerOfTwo(bitmap.getWidth()) && MathUtils.isPowerOfTwo(bitmap.getHeight()) && (this.mPixelFormat == PixelFormat.RGBA_8888);
        if(!useDefaultAlignment) {
			/* Adjust unpack alignment. */
            GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, 1);
        }

        final boolean preMultipyAlpha = this.mTextureOptions.mPreMultiplyAlpha;
        if(preMultipyAlpha) {
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        } else {
            pGLState.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0, this.mPixelFormat);
        }

        if(!useDefaultAlignment) {
			/* Restore default unpack alignment. */
            GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, GLState.GL_UNPACK_ALIGNMENT_DEFAULT);
        }
    }
}
