package com.allow.crazydots.objects;

import com.allow.crazydots.Constants;
import com.allow.crazydots.programs.TextureShaderProgram;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glVertexAttribPointer;
import static com.allow.crazydots.Constants.BYTES_PER_FLOAT;

public class Table {

    private static final int POSITION_COMPONENT_COUNT = 3;
    private static final int TEXTURE_COORDINATES_COMPONENT_COUNT = 2;
    private static final int STRIDE = (POSITION_COMPONENT_COUNT
            + TEXTURE_COORDINATES_COMPONENT_COUNT) * BYTES_PER_FLOAT;

    public FloatBuffer vertexBuffer;

    public Table() {
        float[] rectangleVertices = {
                -1, 0.7f, 0f, 0f + (1 - (1f / (0.7f - (-0.7f)))) / 2f, 0f,
                1, 0.7f, 0f, 1 - (1 - (1f / (0.7f - (-0.7f)))) / 2f, 0f,
                -1, -0.7f, 0f, 0f + (1 - (1f / (0.7f - (-0.7f)))) / 2f, 1f,
                1, -0.7f, 0f, 1 - (1 - (1f / (0.7f - (-0.7f)))) / 2f, 1f
        };
        ByteBuffer buffer = ByteBuffer.allocateDirect(rectangleVertices.length * Constants.BYTES_PER_FLOAT);
        buffer.order(ByteOrder.nativeOrder());
        vertexBuffer = buffer.asFloatBuffer();
        vertexBuffer.put(rectangleVertices);
        vertexBuffer.position(0);
    }

    public void draw() {
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
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
        vertexBuffer.position(dataOffset);
        glVertexAttribPointer(attributeLocation, componentCount, GL_FLOAT,
                false, stride, vertexBuffer);
        glEnableVertexAttribArray(attributeLocation);
        vertexBuffer.position(0);
    }
}
