/*
package com.allow.crazydots;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.opengl.GLSurfaceView;
import android.util.DisplayMetrics;
import android.widget.Toast;

import com.allow.crazydots.objects.Dot;
import com.allow.crazydots.objects.Paper;
import com.allow.crazydots.objects.RectLine;
import com.allow.crazydots.programs.ColorShaderProgram;
import com.allow.crazydots.programs.TextureShaderProgram;
import com.allow.crazydots.util.TextureHelper;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glViewport;
import static android.opengl.Matrix.invertM;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.multiplyMV;
import static android.opengl.Matrix.orthoM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.translateM;
import static com.allow.crazydots.util.Geometry.DotPoint;
import static com.allow.crazydots.util.Geometry.LineSegment;
import static com.allow.crazydots.util.Geometry.Point;
import static com.allow.crazydots.util.Geometry.Ray;
import static com.allow.crazydots.util.Geometry.Sphere;
import static com.allow.crazydots.util.Geometry.intersects;
import static com.allow.crazydots.util.Geometry.vectorBetween;

public class CrazyDotsRenderer0 implements GLSurfaceView.Renderer {

    private final float[] projectionMatrix = new float[16];
    private final float[] viewMatrix = new float[16];
    private final float[] viewProjectionMatrix = new float[16];
    private final float[] modelMatrix = new float[16];
    private final float[] modelViewProjectionMatrix = new float[16];
    private final float[] invertedViewProjectionMatrix = new float[16];

    private final Context context;

    private Paper paper;
    private Dot dot;
    private Dot playerDot;
    private Dot selectedDot;
    private Sphere dotBoundingSphere;
    private RectLine rectLine;

    private TextureShaderProgram textureProgram;
    private int rectLineTexture;
    private ColorShaderProgram colorProgram;

    private int rows = 10;
    private int cols = 10;
    private int previousRow = -1;
    private int previousCol = -1;
    private boolean dotPressed;
    private boolean startingPlayerLine;
    private boolean draggingPlayerLine;
    private LineData draggingLineData;
    private float draggingLineX1;
    private float draggingLineY1;
    private float draggingLineX2;
    private float draggingLineY2;
    private DotPoint[][] dotsPointsArray = new DotPoint[rows][cols];
    private ArrayList<LineData> linesArray = new ArrayList<>(2 * rows * cols - rows - cols);
    //  private Line[] linesArray = new Line[2*rows*cols - rows - cols];
    private int[][] gameBoard = new int[rows - 1][cols - 1];

    public static final int NUM_POINTS_AROUND_DOT = 32;
    public static final DisplayMetrics display = Resources.getSystem().getDisplayMetrics();
    private int totalWidth = display.widthPixels;
    private int totalHeight = display.heightPixels;
    private int distApartRows = totalHeight / rows;
    private int distApartCols = totalWidth / cols;
    private final float boundingSphereRadius;

    private int totalPlayers = 2;
    private int currentPlayer = 1;
    private int[] scores = new int[totalPlayers];
    //    private Player[] players = new Player[totalPlayers];
//    private Player currentPlayer;
    public static final int PLAY_MODE_SINGLE = 1;
    public static final int PLAY_MODE_MULTI = 2;
    private int playMode;
    private int boxesOnBoard;
    private int totalBoxesOnBoard = (rows - 1) * (cols - 1);

    public CrazyDotsRenderer0(final Context context, int playMode) {
        this.context = context;
        this.playMode = playMode;

        int x1 = distApartCols / 2;
        float normalizedX1 = (x1 / (float) totalWidth) * 2 - 1;
        int x2 = distApartCols + distApartCols / 2;
        float normalizedX2 = (x2 / (float) totalWidth) * 2 - 1;
        float normalizedDistApartCols = Math.abs(normalizedX2 - normalizedX1);

        int y1 = distApartRows / 2;
        float normalizedY1 = -((y1 / (float) totalHeight) * 2 - 1);
        int y2 = distApartRows + distApartRows / 2;
        float normalizedY2 = -((y2 / (float) totalHeight) * 2 - 1);
        float normalizedDistApartRows = Math.abs(normalizedY2 - normalizedY1);

        paper = new Paper();

        final float dotRadius = Math.min(normalizedDistApartCols, normalizedDistApartRows) / 10;
        dot = new Dot(dotRadius, NUM_POINTS_AROUND_DOT);
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int x = col * distApartCols + distApartCols / 2;
                int y = row * distApartRows + distApartRows / 2;
                float normalizedX = (x / (float) totalWidth) * 2 - 1;
                float normalizedY = -((y / (float) totalHeight) * 2 - 1);
                dotsPointsArray[row][col] = new DotPoint(normalizedX, normalizedY, 0f, false);
            }
        }

        final float playerDotRadius = Math.min(normalizedDistApartCols, normalizedDistApartRows) / 3;
        playerDot = new Dot(playerDotRadius, NUM_POINTS_AROUND_DOT);

        final float selectedDotRadius = Math.min(normalizedDistApartCols, normalizedDistApartRows) / 5;
        selectedDot = new Dot(selectedDotRadius, NUM_POINTS_AROUND_DOT);

        boundingSphereRadius = Math.min(normalizedDistApartCols, normalizedDistApartRows) / 2;

        rectLine = new RectLine();
        draggingLineData = new LineData(new LineSegment(
                new Point(0f, 0f, 0f),
                new Point(0f, 0, 0f)), 1);

        startGame();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        textureProgram = new TextureShaderProgram(context);
        rectLineTexture = TextureHelper.loadTexture(context, R.drawable.line);
        colorProgram = new ColorShaderProgram(context);
    }

    private void startGame() {
        if (playMode == PLAY_MODE_SINGLE) {
            currentPlayer = 1;
            makeRandomMove();
        } else if (playMode == PLAY_MODE_MULTI) {
            currentPlayer = 1;
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        glViewport(0, 0, width, height);
        final float aspectRatio = width > height ?
                (float) width / (float) height :
                (float) height / (float) width;
        if (width > height) {
            orthoM(projectionMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, -1f, 1f);
        } else {
            orthoM(projectionMatrix, 0, -1f, 1f, -aspectRatio, aspectRatio, -1f, 1f);
        }
        setIdentityM(viewMatrix, 0);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        glClear(GL_COLOR_BUFFER_BIT);
        multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
        invertM(invertedViewProjectionMatrix, 0, viewProjectionMatrix, 0);

        // draw paper
        positionPaperInScene();
        colorProgram.useProgram();
        colorProgram.setUniforms(modelViewProjectionMatrix, 1f, 1f, 1f);
//        paper.bindData(colorProgram);
        paper.draw();

        // draw lines
        for (LineData lineData : linesArray) {
            positionRectLineInScene(0, 0, 0);
            textureProgram.useProgram();
            rectLine.bindData(lineData.line.point1.x, lineData.line.point1.y,
                    lineData.line.point2.x, lineData.line.point2.y, dot.radius);
            if (lineData.player == 1) {
                textureProgram.setUniforms(modelViewProjectionMatrix, rectLineTexture);
            } else {
                textureProgram.setUniforms(modelViewProjectionMatrix, rectLineTexture);
            }
            textureProgram.setAttributes(rectLine.vertexBuffer);
            rectLine.draw();
        }

        // draw draggingPlayerLine line
        positionRectLineInScene(0, 0, 0);
        textureProgram.useProgram();
        rectLine.bindData(draggingLineX1, draggingLineY1, draggingLineX2, draggingLineY2, dot.radius);
        if (draggingLineData.player == 1) {
            textureProgram.setUniforms(modelViewProjectionMatrix, rectLineTexture);
        } else {
            textureProgram.setUniforms(modelViewProjectionMatrix, rectLineTexture);
        }
        textureProgram.setAttributes(rectLine.vertexBuffer);
        rectLine.draw();

        // draw dots
        colorProgram.useProgram();
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                positionDotInScene(dotsPointsArray[row][col].x, dotsPointsArray[row][col].y, dotsPointsArray[row][col].z);
                if (dotsPointsArray[row][col].dotPressed) {
                    selectedDot.bindData(colorProgram);
                    colorProgram.setUniforms(modelViewProjectionMatrix, 0f, 1f, 0f);
                    selectedDot.draw();
                } else {
                    dot.bindData(colorProgram);
                    colorProgram.setUniforms(modelViewProjectionMatrix, 0f, 0f, 0f);
                    dot.draw();
                }
            }
        }

        // draw players
        colorProgram.useProgram();
        playerDot.bindData(colorProgram);
        for (int row = 0; row < rows - 1; row++) {
            for (int col = 0; col < cols - 1; col++) {
                int x = col * distApartCols + distApartCols;
                int y = row * distApartRows + distApartRows;
                float normalizedX = (x / (float) totalWidth) * 2 - 1;
                float normalizedY = -((y / (float) totalHeight) * 2 - 1);
                positionPlayerInScene(normalizedX, normalizedY, 0f);
                if (gameBoard[row][col] != 0) {
                    if (gameBoard[row][col] == 1) {
                        colorProgram.setUniforms(modelViewProjectionMatrix, 1f, 0f, 0f);
                    } else {
                        colorProgram.setUniforms(modelViewProjectionMatrix, 0f, 0f, 1f);
                    }
                    playerDot.draw();
                }
            }
        }

    }

    private void positionPaperInScene() {
        setIdentityM(modelMatrix, 0);
        multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix,
                0, modelMatrix, 0);
    }

    private void positionDotInScene(float x, float y, float z) {
        setIdentityM(modelMatrix, 0);
        translateM(modelMatrix, 0, x, y, z);
        multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix,
                0, modelMatrix, 0);
    }

    private void positionRectLineInScene(float x, float y, float z) {
        setIdentityM(modelMatrix, 0);
        translateM(modelMatrix, 0, x, y, z);
        multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix,
                0, modelMatrix, 0);
    }

    private void positionPlayerInScene(float x, float y, float z) {
        setIdentityM(modelMatrix, 0);
        translateM(modelMatrix, 0, x, y, z);
        multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix,
                0, modelMatrix, 0);
    }

    public void handleTouchPress(float normalizedX, float normalizedY) {
        Ray ray = convertNormalized2DPointToRay(normalizedX, normalizedY);
        outerLoop:
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                dotBoundingSphere = new Sphere(new Point(
                        dotsPointsArray[row][col].x,
                        dotsPointsArray[row][col].y,
                        dotsPointsArray[row][col].z),
                        boundingSphereRadius);
                dotPressed = intersects(dotBoundingSphere, ray);
                if (dotPressed) {
                    draggingLineX1 = draggingLineX2 = dotsPointsArray[row][col].x;
                    draggingLineY1 = draggingLineY2 = dotsPointsArray[row][col].y;
                    draggingPlayerLine = false;
                    makePlayerMove(row, col);
                    break outerLoop;
                }
            }
        }
    }

    public void handleTouchDrag(float normalizedX, float normalizedY) {
        if (dotPressed) {
            */
/*Ray ray = convertNormalized2DPointToRay(normalizedX, normalizedY);
            Plane plane = new Plane( new Point(0, 0, 0), new Vector(0, 0, 1));
            Point touchedPoint = Geometry.intersectionPoint(ray, plane);
            draggingLineX2 = touchedPoint.x;
            draggingLineY2 = touchedPoint.y;*//*

            if (!draggingPlayerLine) {
                draggingPlayerLine = true;
            }
        }
    }

    public void handleTouchLift(float normalizedX, float normalizedY) {
        if (dotPressed) {
            if (!draggingPlayerLine) {
                return;
            }
            Ray ray = convertNormalized2DPointToRay(normalizedX, normalizedY);
            outerLoop:
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    dotBoundingSphere = new Sphere(new Point(
                            dotsPointsArray[row][col].x,
                            dotsPointsArray[row][col].y,
                            dotsPointsArray[row][col].z),
                            boundingSphereRadius);
                    boolean dotPressed = intersects(dotBoundingSphere, ray);
                    if (dotPressed) {
                        makePlayerMove(row, col);
                        break outerLoop;
                    }
                }
            }
            dotPressed = false;
            draggingLineX1 = draggingLineX2;
            draggingLineY1 = draggingLineY2;
            draggingPlayerLine = false;
        }
    }

    private void makePlayerMove(int row, int col) {
        // if single player and current player is the computer, return
        if (playMode == PLAY_MODE_SINGLE && currentPlayer == 1)
            return;

        if (!draggingPlayerLine) {
            if (!startingPlayerLine) {
                dotsPointsArray[row][col].dotPressed = true;
                previousRow = row;
                previousCol = col;
                startingPlayerLine = true;
            } else {
                dotsPointsArray[previousRow][previousCol].dotPressed = false;
                startingPlayerLine = false;
                move(row, col);
            }
        } else {
            dotsPointsArray[previousRow][previousCol].dotPressed = false;
            startingPlayerLine = false;
            move(row, col);
        }
    }

    private void move(int row, int col) {
        if (canMakeLine(row, col)) {
            // add line_black to lines array
            makeLine(row, col);
            // check if square is closed
            checkSquares(row, col);
            // set next player
            nextPlayer();
        }
    }


    private boolean canMakeLine(int row, int col) {
        // same point touched
        if (dotsPointsArray[previousRow][previousCol].distance(dotsPointsArray[row][col]) == 0) {
            return false;
        }
        // up movement
        else if (previousRow > row && previousCol == col) {
            if (dotsPointsArray[previousRow][previousCol].distance(dotsPointsArray[row][col]) >
                    dotsPointsArray[previousRow][previousCol].distance(dotsPointsArray[previousRow - 1][previousCol])) {
                return false;
            } else if (dotsPointsArray[previousRow][previousCol].upDrawn && dotsPointsArray[row][col].downDrawn) {
                return false;
            }
            dotsPointsArray[previousRow][previousCol].upDrawn = true;
            dotsPointsArray[row][col].downDrawn = true;
            return true;
        }
        // down movement
        else if (previousRow < row && previousCol == col) {
            if (dotsPointsArray[previousRow][previousCol].distance(dotsPointsArray[row][col]) >
                    dotsPointsArray[previousRow][previousCol].distance(dotsPointsArray[previousRow + 1][previousCol])) {
                return false;
            } else if (dotsPointsArray[previousRow][previousCol].downDrawn && dotsPointsArray[row][col].upDrawn) {
                return false;
            }
            dotsPointsArray[previousRow][previousCol].downDrawn = true;
            dotsPointsArray[row][col].upDrawn = true;
            return true;
        }
        // left movement
        else if (previousCol > col && previousRow == row) {
            if (dotsPointsArray[previousRow][previousCol].distance(dotsPointsArray[row][col]) >
                    dotsPointsArray[previousRow][previousCol].distance(dotsPointsArray[previousRow][previousCol - 1])) {
                return false;
            } else if (dotsPointsArray[previousRow][previousCol].leftDrawn && dotsPointsArray[row][col].rightDrawn) {
                return false;
            }
            dotsPointsArray[previousRow][previousCol].leftDrawn = true;
            dotsPointsArray[row][col].rightDrawn = true;
            return true;
        }
        // right movement
        else if (previousCol < col && previousRow == row) {
            if (dotsPointsArray[previousRow][previousCol].distance(dotsPointsArray[row][col]) >
                    dotsPointsArray[previousRow][previousCol].distance(dotsPointsArray[previousRow][previousCol + 1])) {
                return false;
            } else if (dotsPointsArray[previousRow][previousCol].rightDrawn && dotsPointsArray[row][col].leftDrawn) {
                return false;
            }
            dotsPointsArray[previousRow][previousCol].rightDrawn = true;
            dotsPointsArray[row][col].leftDrawn = true;
            return true;
        }

        return false;
    }

    private class LineData {
        public LineSegment line_black;
        public int player;

        public LineData(LineSegment line_black, int player) {
            this.line_black = line_black;
            this.player = player;
        }
    }

    private void makeLine(int row, int col) {
        linesArray.add(new LineData(new LineSegment(
                new Point(dotsPointsArray[previousRow][previousCol].x,
                        dotsPointsArray[previousRow][previousCol].y,
                        dotsPointsArray[previousRow][previousCol].z),
                new Point(dotsPointsArray[row][col].x,
                        dotsPointsArray[row][col].y,
                        dotsPointsArray[row][col].z)), currentPlayer));
    }

    private void checkSquares(int row, int col) {
        // if moving up or if moving left check right-down
        if (previousRow > row && previousCol == col || previousRow == row && previousCol > col) {
            if (row != rows - 1 && col != cols - 1) {
                // check if square is empty
                if (gameBoard[row][col] != 0) {
                    return;
                }
                if (dotsPointsArray[row][col].rightDrawn && dotsPointsArray[row][col + 1].downDrawn &&
                        dotsPointsArray[row + 1][col + 1].leftDrawn && dotsPointsArray[row + 1][col].upDrawn) {
                    gameBoard[row][col] = currentPlayer;
                    scores[currentPlayer - 1]++;
                    boxesOnBoard++;
                }
            }
        }
        // if moving up or if moving right check left-down
        if (previousRow > row && previousCol == col || previousRow == row && previousCol < col) {
            if (row != rows - 1 && col != 0) {
                // check if square is empty
                if (gameBoard[row][col - 1] != 0) {
                    return;
                }
                if (dotsPointsArray[row][col].leftDrawn && dotsPointsArray[row][col - 1].downDrawn &&
                        dotsPointsArray[row + 1][col - 1].rightDrawn && dotsPointsArray[row + 1][col].upDrawn) {
                    gameBoard[row][col - 1] = currentPlayer;
                    scores[currentPlayer - 1]++;
                    boxesOnBoard++;
                }
            }
        }
        // if moving down or if moving left check right-up
        if (previousRow < row && previousCol == col || previousRow == row && previousCol > col) {
            if (row != 0 && col != cols - 1) {
                // check if square is empty
                if (gameBoard[row - 1][col] != 0) {
                    return;
                }
                if (dotsPointsArray[row][col].upDrawn && dotsPointsArray[row - 1][col].rightDrawn &&
                        dotsPointsArray[row - 1][col + 1].downDrawn && dotsPointsArray[row][col + 1].leftDrawn) {
                    gameBoard[row - 1][col] = currentPlayer;
                    scores[currentPlayer - 1]++;
                    boxesOnBoard++;
                }
            }
        }
        // if moving down or if moving right check left-up
        if (previousRow < row && previousCol == col || previousRow == row && previousCol < col) {
            if (row != 0 && col != 0) {
                // check if square is empty
                if (gameBoard[row - 1][col - 1] != 0) {
                    return;
                }
                if (dotsPointsArray[row][col].upDrawn && dotsPointsArray[row - 1][col].leftDrawn &&
                        dotsPointsArray[row - 1][col - 1].downDrawn && dotsPointsArray[row][col - 1].rightDrawn) {
                    gameBoard[row - 1][col - 1] = currentPlayer;
                    scores[currentPlayer - 1]++;
                    boxesOnBoard++;
                }
            }
        }
    }

    private void nextPlayer() {
        if (boxesOnBoard == totalBoxesOnBoard) {
            stopGame();
            return;
        }

        currentPlayer++;
        if (currentPlayer > totalPlayers)
            currentPlayer = 1;

        if (playMode == PLAY_MODE_SINGLE && currentPlayer == 1)
            makeCalculatedMove();
    }

    private void stopGame() {
        // Toast the score
        postScore();
    }

    private void postScore() {
        ((Activity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int highScore = 0;
                int highPlayer = 1;
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < scores.length; i++) {
                    builder.append("Player ");
                    builder.append(i + 1);
                    builder.append(" : ");
                    builder.append(scores[i]);
                    builder.append(" ");
                    if (scores[i] > highScore) {
                        highScore = scores[i];
                        highPlayer = i;
                    }
                }
                builder.append("Player ");
                builder.append(highPlayer);
                builder.append(" Won");
                Toast.makeText(context, builder.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void makeCalculatedMove() {
        if (((playMode == PLAY_MODE_SINGLE) && (currentPlayer != 1)) || (playMode == PLAY_MODE_MULTI))
            return;

        ArrayList<PossibleWin> pws = possibleWins();
        */
/*int winIndex = (int)(Math.random()*(pws.size()+1)); // make random move
        if (pws.isEmpty() || winIndex >= pws.size()) {         // if possible wins is
                                                                // empty or random number
                                                                 // is greater than size
                                                                  //of possible wins
            makeRandomMove();
            return;
        }*//*

        if (pws.isEmpty()) { // make random move
            makeRandomMove(); // only if possible wins is empty
            return;
        }

        int winIndex = (int) (Math.random() * (pws.size()));
        previousRow = pws.get(winIndex).fromRow;
        previousCol = pws.get(winIndex).fromCol;

        int row = pws.get(winIndex).toRow;
        int col = pws.get(winIndex).toCol;

        if (canMakeLine(row, col)) {
            // add line_black to lines array
            makeLine(row, col);
            // check if square is closed
            checkSquares(row, col);
            // set next player
            nextPlayer();
        }
    }

    // holder class
    private class PossibleWin {
        int fromRow, fromCol;
        int toRow, toCol;
    }

    private ArrayList<PossibleWin> possibleWins() {
        ArrayList<PossibleWin> possibleWins = new ArrayList<>();
        for (int row = 0; row < rows - 1; row++) {
            for (int col = 0; col < cols - 1; col++) {
                // check if square is empty
                if (gameBoard[row][col] == 0) {
                    if (!dotsPointsArray[row][col].rightDrawn && dotsPointsArray[row][col + 1].downDrawn &&
                            dotsPointsArray[row + 1][col + 1].leftDrawn && dotsPointsArray[row + 1][col].upDrawn) {
                        PossibleWin pw = new PossibleWin();
                        pw.fromRow = row;
                        pw.fromCol = col;
                        pw.toRow = row;
                        pw.toCol = col + 1;
                        possibleWins.add(pw);
                    } else if (dotsPointsArray[row][col].rightDrawn && !dotsPointsArray[row][col + 1].downDrawn &&
                            dotsPointsArray[row + 1][col + 1].leftDrawn && dotsPointsArray[row + 1][col].upDrawn) {
                        PossibleWin pw = new PossibleWin();
                        pw.fromRow = row;
                        pw.fromCol = col + 1;
                        pw.toRow = row + 1;
                        pw.toCol = col + 1;
                        possibleWins.add(pw);
                    } else if (dotsPointsArray[row][col].rightDrawn && dotsPointsArray[row][col + 1].downDrawn &&
                            !dotsPointsArray[row + 1][col + 1].leftDrawn && dotsPointsArray[row + 1][col].upDrawn) {
                        PossibleWin pw = new PossibleWin();
                        pw.fromRow = row + 1;
                        pw.fromCol = col + 1;
                        pw.toRow = row + 1;
                        pw.toCol = col;
                        possibleWins.add(pw);
                    } else if (dotsPointsArray[row][col].rightDrawn && dotsPointsArray[row][col + 1].downDrawn &&
                            dotsPointsArray[row + 1][col + 1].leftDrawn && !dotsPointsArray[row + 1][col].upDrawn) {
                        PossibleWin pw = new PossibleWin();
                        pw.fromRow = row + 1;
                        pw.fromCol = col;
                        pw.toRow = row;
                        pw.toCol = col;
                        possibleWins.add(pw);
                    }
                }
            }
        }
        return possibleWins;
    }

    private void makeRandomMove() {
        if (((playMode == PLAY_MODE_SINGLE) && (currentPlayer != 1)) || (playMode == PLAY_MODE_MULTI))
            return;

        int row;
        int col;

        int[] rowcol;
        do {
            do {// find first point
                row = (int) (Math.random() * rows);
                col = (int) (Math.random() * cols);
            }
            while ((row < rows - 1 && row > 0 && col < cols - 1 && col > 0) && dotsPointsArray[row][col].upDrawn && dotsPointsArray[row][col].downDrawn &&
                    dotsPointsArray[row][col].leftDrawn && dotsPointsArray[row][col].rightDrawn ||
                    (row == 0 && (col != 0 || col != cols - 1) && dotsPointsArray[row][col].leftDrawn && dotsPointsArray[row][col].rightDrawn && dotsPointsArray[row][col].downDrawn) ||
                    (row == 0 && col == 0 && dotsPointsArray[row][col].rightDrawn && dotsPointsArray[row][col].downDrawn) ||
                    (row == 0 && col == cols - 1 && dotsPointsArray[row][col].leftDrawn && dotsPointsArray[row][col].downDrawn) ||
                    (row == rows - 1 && (col != 0 || col != cols - 1) && dotsPointsArray[row][col].leftDrawn && dotsPointsArray[row][col].upDrawn && dotsPointsArray[row][col].rightDrawn) ||
                    (row == rows - 1 && col == 0 && dotsPointsArray[row][col].rightDrawn && dotsPointsArray[row][col].upDrawn) ||
                    (row == rows - 1 && col == cols - 1 && dotsPointsArray[row][col].leftDrawn && dotsPointsArray[row][col].upDrawn) ||
                    (col == 0 && (row != 0 || row != rows - 1) && dotsPointsArray[row][col].upDrawn && dotsPointsArray[row][col].rightDrawn && dotsPointsArray[row][col].downDrawn) ||
                    (col == 0 && row == 0 && dotsPointsArray[row][col].rightDrawn && dotsPointsArray[row][col].downDrawn) ||
                    (col == 0 && row == rows - 1 && dotsPointsArray[row][col].upDrawn && dotsPointsArray[row][col].rightDrawn) ||
                    (col == cols - 1 && (row != 0 || row != rows - 1) && dotsPointsArray[row][col].upDrawn && dotsPointsArray[row][col].leftDrawn && dotsPointsArray[row][col].downDrawn) ||
                    (col == cols - 1 && row == 0 && dotsPointsArray[row][col].leftDrawn && dotsPointsArray[row][col].downDrawn) ||
                    (col == cols - 1 && row == rows - 1 && dotsPointsArray[row][col].upDrawn && dotsPointsArray[row][col].leftDrawn));

            previousRow = row;
            previousCol = col;

            // find second point
            rowcol = nextRandomRowCol();

        } while (rowcol[0] == -1 || rowcol[1] == -1);

        // add line_black to lines array
        makeLine(rowcol[0], rowcol[1]);
        // check if square is closed
        checkSquares(rowcol[0], rowcol[1]);
        // set next player
        nextPlayer();
    }

    private int[] nextRandomRowCol() {
        int row = -1, col = -1;
        int[] rowcol = {row, col};
        int direction;

        direction = (int) (Math.random() * 4);
        switch (direction) {
            // up
            case 0:
                if (previousRow > 0) {
                    if (!dotsPointsArray[previousRow][previousCol].upDrawn) {
                        row = previousRow - 1;
                        col = previousCol;
                        dotsPointsArray[previousRow][previousCol].upDrawn = true;
                        dotsPointsArray[row][col].downDrawn = true;
                        rowcol[0] = row;
                        rowcol[1] = col;
                        return rowcol;
                    }
                } else {
                    if (!dotsPointsArray[previousRow][previousCol].downDrawn) {
                        row = previousRow + 1;
                        col = previousCol;
                        dotsPointsArray[previousRow][previousCol].downDrawn = true;
                        dotsPointsArray[row][col].upDrawn = true;
                        rowcol[0] = row;
                        rowcol[1] = col;
                        return rowcol;
                    }
                }
                // down
            case 1:
                if (previousRow < rows - 1) {
                    if (!dotsPointsArray[previousRow][previousCol].downDrawn) {
                        row = previousRow + 1;
                        col = previousCol;
                        dotsPointsArray[previousRow][previousCol].downDrawn = true;
                        dotsPointsArray[row][col].upDrawn = true;
                        rowcol[0] = row;
                        rowcol[1] = col;
                        return rowcol;
                    }
                } else {
                    if (!dotsPointsArray[previousRow][previousCol].upDrawn) {
                        row = previousRow - 1;
                        col = previousCol;
                        dotsPointsArray[previousRow][previousCol].upDrawn = true;
                        dotsPointsArray[row][col].downDrawn = true;
                        rowcol[0] = row;
                        rowcol[1] = col;
                        return rowcol;
                    }
                }
                // left
            case 2:
                if (previousCol > 0) {
                    if (!dotsPointsArray[previousRow][previousCol].leftDrawn) {
                        row = previousRow;
                        col = previousCol - 1;
                        dotsPointsArray[previousRow][previousCol].leftDrawn = true;
                        dotsPointsArray[row][col].rightDrawn = true;
                        rowcol[0] = row;
                        rowcol[1] = col;
                        return rowcol;
                    }
                } else {
                    if (!dotsPointsArray[previousRow][previousCol].rightDrawn) {
                        row = previousRow;
                        col = previousCol + 1;
                        dotsPointsArray[previousRow][previousCol].rightDrawn = true;
                        dotsPointsArray[row][col].leftDrawn = true;
                        rowcol[0] = row;
                        rowcol[1] = col;
                        return rowcol;
                    }
                }
                // right
            case 3:
                if (previousCol < cols - 1) {
                    if (!dotsPointsArray[previousRow][previousCol].rightDrawn) {
                        row = previousRow;
                        col = previousCol + 1;
                        dotsPointsArray[previousRow][previousCol].rightDrawn = true;
                        dotsPointsArray[row][col].leftDrawn = true;
                        rowcol[0] = row;
                        rowcol[1] = col;
                        return rowcol;
                    }
                } else {
                    if (!dotsPointsArray[previousRow][previousCol].leftDrawn) {
                        row = previousRow;
                        col = previousCol - 1;
                        dotsPointsArray[previousRow][previousCol].leftDrawn = true;
                        dotsPointsArray[row][col].rightDrawn = true;
                        rowcol[0] = row;
                        rowcol[1] = col;
                        return rowcol;
                    }
                }

            default:
                break;
        }

        return rowcol;
    }

    private Ray convertNormalized2DPointToRay(float normalizedX, float normalizedY) {
        final float[] nearPointNdc = {normalizedX, normalizedY, -1, 1};
        final float[] farPointNdc = {normalizedX, normalizedY, 1, 1};
        final float[] nearPointWorld = new float[4];
        final float[] farPointWorld = new float[4];
        multiplyMV(
                nearPointWorld, 0, invertedViewProjectionMatrix, 0, nearPointNdc, 0);
        multiplyMV(
                farPointWorld, 0, invertedViewProjectionMatrix, 0, farPointNdc, 0);
        divideByW(nearPointWorld);
        divideByW(farPointWorld);
        Point nearPointRay =
                new Point(nearPointWorld[0], nearPointWorld[1], nearPointWorld[2]);
        Point farPointRay =
                new Point(farPointWorld[0], farPointWorld[1], farPointWorld[2]);
        return new Ray(nearPointRay,
                vectorBetween(nearPointRay, farPointRay));
    }

    private void divideByW(float[] vector) {
        vector[0] /= vector[3];
        vector[1] /= vector[3];
        vector[2] /= vector[3];
    }


    public void setPlayMode(int playMode) {
        this.playMode = playMode;
    }

}*/
