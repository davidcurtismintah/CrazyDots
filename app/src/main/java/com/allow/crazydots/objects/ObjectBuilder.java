package com.allow.crazydots.objects;

import com.allow.crazydots.util.Geometry.Rectangle;

import java.util.ArrayList;
import java.util.List;

import static android.opengl.GLES20.GL_BLEND;
import static android.opengl.GLES20.GL_DEPTH_TEST;
import static android.opengl.GLES20.GL_ONE_MINUS_SRC_ALPHA;
import static android.opengl.GLES20.GL_SRC_ALPHA;
import static android.opengl.GLES20.GL_TRIANGLE_FAN;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.glBlendFunc;
import static android.opengl.GLES20.glDepthMask;
import static android.opengl.GLES20.glDisable;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnable;
import static com.allow.crazydots.util.Geometry.Circle;
import static com.allow.crazydots.util.Geometry.Point;

public class ObjectBuilder {


    interface DrawCommand {
        void draw();
    }

    static class GeneratedData {
        final float[] vertexData;
        final List<DrawCommand> drawList;
        GeneratedData( float[] vertexData, List<DrawCommand> drawList) {
            this.vertexData = vertexData;
            this.drawList = drawList;
        }
    }

    private final List<DrawCommand> drawList = new ArrayList<DrawCommand>();

    private static final int FLOATS_PER_VERTEX = 3;
    private final float[] vertexData;
    private int offset = 0;

    private ObjectBuilder(int sizeInVertices) {
        vertexData = new float[sizeInVertices * FLOATS_PER_VERTEX];
    }

    private static int sizeOfCircleInVertices(int numPoints) {
        return 1 + (numPoints + 1);
    }

    static GeneratedData createDot(Point center, float radius, int numPoints) {
        int size = sizeOfCircleInVertices(numPoints);
        ObjectBuilder builder = new ObjectBuilder(size);
        Circle dot = new Circle(center, radius);
        builder.appendDot(dot, numPoints);
        return builder.build();
    }

    private void appendDot(Circle circle, int numPoints) {
        final int startVertex = offset / FLOATS_PER_VERTEX;
        final int numVertices = sizeOfCircleInVertices(numPoints);

        vertexData[offset++] = circle.center.x;
        vertexData[offset++] = circle.center.y;
        vertexData[offset++] = circle.center.z;

        for (int i = 0; i <= numPoints; i++) {
            float angleInRadians =
                    ((float) i / (float) numPoints)
                            * ((float) Math.PI * 2f);
            vertexData[offset++] =
                    (float) (circle.center.x + circle.radius * Math.cos(angleInRadians));
            vertexData[offset++] =
                    (float) (circle.center.y + circle.radius * Math.sin(angleInRadians));
            vertexData[offset++] = circle.center.z;

        }

        drawList.add(new DrawCommand() {
            @Override
            public void draw() {
                glDisable(GL_DEPTH_TEST);
                glEnable(GL_BLEND);
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
                glDepthMask(false);
                glDrawArrays(GL_TRIANGLE_FAN, startVertex, numVertices);
                glDisable(GL_BLEND);
            }
        });
    }

    public static GeneratedData createLine(float width, float height, float[] vertex_data) {
        int size = 4;
        ObjectBuilder builder = new ObjectBuilder(size);
        Rectangle rectangle = new Rectangle(width, height);
        builder.appendRectangle(rectangle, vertex_data);
        return builder.build();
    }

    private void appendRectangle(Rectangle rectangle, float[] vertex_data) {
        final int startVertex = offset / FLOATS_PER_VERTEX;
        final int numVertices = 4;

        /*vertexData[offset++] = vertex_data[0]-rectangle.width/2;
        vertexData[offset++] = vertex_data[1];
        vertexData[offset++] = vertex_data[2];
        vertexData[offset++] = vertex_data[0]+rectangle.width/2;
        vertexData[offset++] = vertex_data[1];
        vertexData[offset++] = vertex_data[2];
        vertexData[offset++] = vertex_data[3]-rectangle.width/2;
        vertexData[offset++] = vertex_data[4];
        vertexData[offset++] = vertex_data[5];
        vertexData[offset++] = vertex_data[3]+rectangle.width/2;
        vertexData[offset++] = vertex_data[4];
        vertexData[offset++] = vertex_data[5];*/

        vertexData[offset++] = 0;
        vertexData[offset++] = 0;
        vertexData[offset++] = 0;
        vertexData[offset++] = 0.02f;
        vertexData[offset++] = 0;
        vertexData[offset++] = 0;
        vertexData[offset++] = 0;
        vertexData[offset++] = -0.25f;
        vertexData[offset++] = 0;
        vertexData[offset++] = 0.02f;
        vertexData[offset++] = -0.25f;
        vertexData[offset++] = 0;

        drawList.add( new DrawCommand() {
            @Override
            public void draw() {
                glDrawArrays(GL_TRIANGLE_STRIP, startVertex, numVertices);
            }
        });
    }

    private GeneratedData build() {
        return new GeneratedData(vertexData, drawList);
    }
}
