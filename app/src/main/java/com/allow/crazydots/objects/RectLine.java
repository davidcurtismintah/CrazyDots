package com.allow.crazydots.objects;

import com.allow.crazydots.Constants;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.GL_BLEND;
import static android.opengl.GLES20.GL_DEPTH_TEST;
import static android.opengl.GLES20.GL_ONE_MINUS_SRC_ALPHA;
import static android.opengl.GLES20.GL_SRC_ALPHA;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.glBlendFunc;
import static android.opengl.GLES20.glDepthMask;
import static android.opengl.GLES20.glDisable;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnable;

public class RectLine {

    private float[] powerVertices = new float[20];
    public FloatBuffer vertexBuffer;
    public float width;

    public RectLine(float width){
        ByteBuffer buffer = ByteBuffer.allocateDirect(powerVertices.length * Constants.BYTES_PER_FLOAT);
        buffer.order(ByteOrder.nativeOrder());
        vertexBuffer = buffer.asFloatBuffer();
        this.width = width;
    }

    public void bindData(float powerX1, float powerY1, float powerX2, float powerY2){
        float vx = powerX2 - powerX1;
        float vy = powerY2 - powerY1;

        float vLen = (float) Math.sqrt(vx*vx + vy*vy);

        float v1x = vx / vLen;
        float v1y = vy / vLen;

        float u1x = -v1y;

        float p1x = powerX1 - u1x * width / 2;
        float p1y = powerY1 - v1x * width / 2;
        float p2x = p1x + u1x * width;
        float p2y = p1y + v1x * width;
        float p3x = p1x + v1x * vLen;
        float p3y = p1y + v1y * vLen;
        float p4x = p3x + u1x * width;
        float p4y = p3y + v1x * width;

        powerVertices = new float[]{
                p1x, p1y, 0f, 0f, 0f,
                p2x, p2y, 0f, 1, 0f,
                p3x, p3y, 0f, 0f, vLen,
                p4x, p4y, 0f, 1, vLen
        };

        vertexBuffer.position(0);
        vertexBuffer.put(powerVertices);
        vertexBuffer.position(0);
    }

    public void draw() {
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDepthMask(false);

        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

        glDisable(GL_BLEND);
    }

}
