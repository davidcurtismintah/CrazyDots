package com.allow.crazydots.programs;

import android.content.Context;

import com.allow.crazydots.Constants;
import com.allow.crazydots.R;

import java.nio.FloatBuffer;

import static android.opengl.GLES20.*;

public class TextureShaderProgram extends ShaderProgram{

    private final int uMatrixLocation;
    private final int uTextureUnitLocation;

    private final int aPositionLocation;
    private final int aTextureCoordinatesLocation;

    public TextureShaderProgram( Context context) {
        super(context, R.raw.texture_vertex_shader,
                R.raw.texture_fragment_shader);
        uMatrixLocation = glGetUniformLocation(program, U_MATRIX);
        uTextureUnitLocation = glGetUniformLocation(program, U_TEXTURE_UNIT);
        aPositionLocation = glGetAttribLocation(program, A_POSITION);
        aTextureCoordinatesLocation =
                glGetAttribLocation(program, A_TEXTURE_COORDINATES);
    }

    public void setUniforms(float[] matrix, int textureId) {
        glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureId);
        glUniform1i(uTextureUnitLocation, 0);
    }

    public int getPositionAttributeLocation() {
        return aPositionLocation;
    }
    public int getTextureCoordinatesAttributeLocation() {
        return aTextureCoordinatesLocation;
    }

    public void setAttributes(FloatBuffer vertexBuffer) {
        int offset;
        int stride;

        offset = 0;
        vertexBuffer.position(offset);
        stride = (Constants.POSITION_COMPONENT_COUNT + Constants.TEXTURE_COORDINATES_COMPONENT_COUNT) * Constants.BYTES_PER_FLOAT;
        glVertexAttribPointer(aPositionLocation, Constants.POSITION_COMPONENT_COUNT, GL_FLOAT, false, stride, vertexBuffer);
        glEnableVertexAttribArray(aPositionLocation);

        offset = Constants.POSITION_COMPONENT_COUNT;
        vertexBuffer.position(offset);
        stride = (Constants.TEXTURE_COORDINATES_COMPONENT_COUNT + Constants.POSITION_COMPONENT_COUNT) * Constants.BYTES_PER_FLOAT;
        glVertexAttribPointer(aTextureCoordinatesLocation, Constants.TEXTURE_COORDINATES_COMPONENT_COUNT, GL_FLOAT, false, stride, vertexBuffer);
        glEnableVertexAttribArray(aTextureCoordinatesLocation);
    }
}
