package com.allow.crazydots.objects;

import com.allow.crazydots.Constants;
import com.allow.crazydots.data.VertexArray;
import com.allow.crazydots.objects.ObjectBuilder.DrawCommand;
import com.allow.crazydots.objects.ObjectBuilder.GeneratedData;
import com.allow.crazydots.programs.ColorShaderProgram;
import com.allow.crazydots.programs.TextureShaderProgram;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.List;

import static android.opengl.GLES20.GL_BLEND;
import static android.opengl.GLES20.GL_DEPTH_TEST;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_ONE_MINUS_SRC_ALPHA;
import static android.opengl.GLES20.GL_SRC_ALPHA;
import static android.opengl.GLES20.GL_TRIANGLE_FAN;
import static android.opengl.GLES20.glBlendFunc;
import static android.opengl.GLES20.glDepthMask;
import static android.opengl.GLES20.glDisable;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glVertexAttribPointer;
import static com.allow.crazydots.Constants.BYTES_PER_FLOAT;
import static com.allow.crazydots.util.Geometry.*;

public class Dot {
    private static final int POSITION_COMPONENT_COUNT = 3;
    private static final int TEXTURE_COORDINATES_COMPONENT_COUNT = 2;
    private static final int STRIDE = (POSITION_COMPONENT_COUNT
            + TEXTURE_COORDINATES_COMPONENT_COUNT) * BYTES_PER_FLOAT;
    private final FloatBuffer textureCoordBuffer;
    public final float size;
    public Dot(float size) {
        this.size = size;
        float[] textureCordinates = {
                0f, 0f, 0f, 0.5f, 0.5f,
                -size, -size, 0f, 0f, 1f,
                size, -size, 0f, 1f, 1f,
                size, size, 0f, 1f, 0f,
                -size, size, 0f, 0f, 0f,
                -size, -size, 0f, 0f, 1f
        };
        ByteBuffer buffer = ByteBuffer.allocateDirect(textureCordinates.length * Constants.BYTES_PER_FLOAT);
        buffer.order(ByteOrder.nativeOrder());
        textureCoordBuffer = buffer.asFloatBuffer();
        textureCoordBuffer.put(textureCordinates);
        textureCoordBuffer.position(0);
    }

    public void bindData(TextureShaderProgram textureProgram) {
        setVertexAttribPointer(
                0,
                textureProgram.getPositionAttributeLocation(),
                POSITION_COMPONENT_COUNT,
                STRIDE);
        setVertexAttribPointer(
                POSITION_COMPONENT_COUNT,
                textureProgram.getTextureCoordinatesAttributeLocation(),
                TEXTURE_COORDINATES_COMPONENT_COUNT,
                STRIDE);
    }

    public void setVertexAttribPointer(int dataOffset, int attributeLocation,
                                       int componentCount, int stride) {
        textureCoordBuffer.position(dataOffset);
        glVertexAttribPointer(attributeLocation, componentCount, GL_FLOAT,
                false, stride, textureCoordBuffer);
        glEnableVertexAttribArray(attributeLocation);
        textureCoordBuffer.position(0);
    }

    public void draw() {
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDepthMask(false);
        glDrawArrays(GL_TRIANGLE_FAN, 0, 6);
        glDisable(GL_BLEND);
    }
}
