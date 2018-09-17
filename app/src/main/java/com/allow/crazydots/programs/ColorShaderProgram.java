package com.allow.crazydots.programs;


import android.content.Context;

import com.allow.crazydots.R;

import static android.opengl.GLES20.*;

public class ColorShaderProgram extends ShaderProgram{
    private final int uMatrixLocation;

    private final int aPositionLocation;
    private final int uColorLocation;

    public ColorShaderProgram(Context context) {
        super(context, R.raw.simple_vertex_shader,
                R.raw.simple_fragment_shader);

        uMatrixLocation = glGetUniformLocation(program, U_MATRIX);

        aPositionLocation = glGetAttribLocation(program, A_POSITION);
        uColorLocation = glGetUniformLocation(program, U_COLOR);
    }

    public void setUniforms( float[] matrix, float r, float g, float b) {
        glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0);
        glUniform4f(uColorLocation, r, g, b, 1f);
    }

    public int getPositionAttributeLocation() {
        return aPositionLocation;
    }
}
