//package com.allow.crazydots;
//
//import android.content.Context;
//import android.opengl.GLSurfaceView;
//import android.util.Log;
//
//import com.allow.crazydots.objects.Dot;
//import com.allow.crazydots.objects.Paper;
//import com.allow.crazydots.objects.RectLine;
//import com.allow.crazydots.objects.Table;
//import com.allow.crazydots.programs.ColorShaderProgram;
//import com.allow.crazydots.programs.TextureShaderProgram;
//import com.allow.crazydots.util.DialogHelper;
//import com.allow.crazydots.util.Geometry;
//import com.allow.crazydots.util.Geometry.Plane;
//import com.allow.crazydots.util.Geometry.Vector;
//import com.allow.crazydots.util.TextureHelper;
//
//import java.util.ArrayList;
//
//import javax.microedition.khronos.egl.EGLConfig;
//import javax.microedition.khronos.opengles.GL10;
//
//import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
//import static android.opengl.GLES20.glClear;
//import static android.opengl.GLES20.glClearColor;
//import static android.opengl.GLES20.glViewport;
//import static android.opengl.Matrix.invertM;
//import static android.opengl.Matrix.multiplyMM;
//import static android.opengl.Matrix.multiplyMV;
//import static android.opengl.Matrix.orthoM;
//import static android.opengl.Matrix.rotateM;
//import static android.opengl.Matrix.scaleM;
//import static android.opengl.Matrix.setIdentityM;
//import static android.opengl.Matrix.translateM;
//import static com.allow.crazydots.util.Geometry.DotPoint;
//import static com.allow.crazydots.util.Geometry.LineSegment;
//import static com.allow.crazydots.util.Geometry.Point;
//import static com.allow.crazydots.util.Geometry.Ray;
//import static com.allow.crazydots.util.Geometry.Sphere;
//import static com.allow.crazydots.util.Geometry.intersects;
//import static com.allow.crazydots.util.Geometry.vectorBetween;
//
//public class CrazyDotsRenderer implements GLSurfaceView.Renderer {
//
//    private static final String TAG = "CrazyDotsRenderer";
//    private final float[] projectionMatrix = new float[16];
//    private final float[] viewMatrix = new float[16];
//    private final float[] viewProjectionMatrix = new float[16];
//    private final float[] modelMatrix = new float[16];
//    private final float[] modelViewProjectionMatrix = new float[16];
//    private final float[] invertedViewProjectionMatrix = new float[16];
//
//    public static final int DIRECTION_NONE = 0;
//    public static final int DIRECTION_LEFT = 1;
//    public static final int DIRECTION_RIGHT = 2;
//    public static final int DIRECTION_UP = 3;
//    public static final int DIRECTION_DOWN = 4;
//
//    public static final int DEFAULT_NUM_ROWS = 6;
//    public static final int DEFAULT_NUM_COLS = 6;
//
//    public static final int PLAY_MODE_SINGLE = 1;
//    public static final int PLAY_MODE_MULTI = 2;
//    private static final int DEFAULT_NUM_PLAYERS = 2;
//
//    public static final int MESSAGE_READ = 0x400 + 1;
//    public static final int MY_HANDLE = 0x400 + 2;
//
//    private static final float leftPaperMargin = 0.2f;
//    private static final float rightPaperMargin = 0.2f;
//    private static final float topPaperMargin = 0.2f;
//    private static final float bottomPaperMargin = 0.2f;
//
//    private static final float leftBound = -1.0f;
//    private static final float rightBound = 1.0f;
//    private static final float topBound = 1.0f;
//    private static final float bottomBound = -1.0f;
//
//    private final Context context;
//    private Thread computerHandler;
//
//    private Table table;
//    private Paper paper;
//    private Dot dot;
//    private Dot playerDot;
//    private Dot selectedDot;
//    private Sphere dotBoundingSphere;
//    private RectLine rectLine;
//    private RectLine dragLine;
//
//    private TextureShaderProgram textureProgram;
//    private int tableTexture;
//    private int paperTexture;
//    private int lineBlackTexture;
//    private int lineRedTexture;
//    private int lineBlueTexture;
//    private int playerTexture;
//    private int player1Texture;
//    private int player2Texture;
//    private int computerTexture;
//    private int defaultDotTexture;
//    private int dotShadowTexture;
//    private int pressedDotTexture;
//    private ColorShaderProgram colorProgram;
//    private DialogHelper dialog;
//
//    private int rows;
//    private int cols;
//    private int previousRow;
//    private int previousCol;
//    private boolean dotPressed;
//    private boolean startingPlayerLine;
//    private boolean draggingPlayerLine;
//    private LineData draggingLineData;
//    private float draggingLineX1;
//    private float draggingLineY1;
//    private float draggingLineX2;
//    private float draggingLineY2;
//    private DotPoint[][] dotsPointsArray;
//    private ArrayList<LineData> linesArray;
//    //    private Line[] linesArray = new Line[2*rows*cols - rows - cols];
//    private int[][] gameBoard;
//    private float rowHeight;
//    private float colWidth;
//    private float boundingSphereRadius;
//    private int totalPlayers;
//    private int currentPlayer;
//    private int[] scores;
//    private int currentBoxesOnBoard;
//    private int totalBoxesOnBoard;
//    //    private Player[] players = new Player[totalPlayers];
////    private Player currentPlayer;
//    private int playMode;
//    private boolean gameOver;
//    private boolean useP2p;
//
//    public CrazyDotsRenderer(final Context context, final int playMode, int rows, int cols, int numPlayers) {
//        this.context = context;
//        dialog = DialogHelper.getInstance(context, this);
//
//        table = new Table();
//        paper = new Paper();
//
//        playAgain(playMode, rows, cols, numPlayers);
//    }
//
//    public void playAgain() {
//        resetGame();
//        startGame();
//    }
//
//    public void playAgain(int mode, int numRows, int numCols, int numPlayers) {
//        resetGame(mode, numRows, numCols, numPlayers);
//        startGame();
//    }
//
//    public void resetGame() {
//        resetGame(playMode, rows, cols, totalPlayers);
//    }
//
//    public void resetGame(int mode, int numRows, int numCols, int numPlayers) {
//        this.playMode = mode;
//        this.rows = (numRows <= 0) ? DEFAULT_NUM_ROWS : numRows;
//        this.cols = (numCols <= 0) ? DEFAULT_NUM_COLS : numCols;
//        this.totalPlayers = (numPlayers <= 0) ? DEFAULT_NUM_PLAYERS : numPlayers;
//
//        if (useP2p) {
//            this.playMode = PLAY_MODE_MULTI;
//            this.rows = DEFAULT_NUM_ROWS;
//            this.cols = DEFAULT_NUM_COLS;
//            this.totalPlayers = DEFAULT_NUM_PLAYERS;
//        }
//
//        rowHeight = (float) ((2.0 - ((topPaperMargin + bottomPaperMargin) / 2.0)) / (rows - 1));
//        colWidth = (float) ((2.0 - ((leftPaperMargin + rightPaperMargin)) / 2.0) / (cols - 1));
//
//        final float dotRadius = Math.min(colWidth, rowHeight) / 10;
//        dot = new Dot(dotRadius);
//
//        rectLine = new RectLine(dotRadius);
//        dragLine = new RectLine(dotRadius / 2);
//
//        final float playerDotRadius = Math.min(colWidth, rowHeight) / 3;
//        playerDot = new Dot(playerDotRadius);
//
//        final float selectedDotRadius = Math.min(colWidth, rowHeight) / 8;
//        selectedDot = new Dot(selectedDotRadius);
//
//        boundingSphereRadius = Math.min(colWidth, rowHeight) / 1.5f;
//
//        previousRow = -1;
//        previousCol = -1;
//        dotPressed = false;
//        startingPlayerLine = false;
//        draggingPlayerLine = false;
//        draggingLineData = new LineData(new LineSegment(new Point(0f, 0f, 0f), new Point(0f, 0, 0f)), 1);
//        draggingLineX1 = 0f;
//        draggingLineY1 = 0f;
//        draggingLineX2 = 0f;
//        draggingLineY2 = 0f;
//        dotsPointsArray = new DotPoint[rows][cols];
//        for (int row = 0; row < rows; row++) {
//            for (int col = 0; col < cols; col++) {
//                float x = convertColToPoint(col, cols);
//                float y = convertRowToPoint(row, rows);
//                dotsPointsArray[row][col] = new DotPoint(x, y, 0f, false);
//            }
//        }
//        linesArray = new ArrayList<>(2 * rows * cols - rows - cols);
//        gameBoard = new int[rows - 1][cols - 1];
//        currentPlayer = 0;
//        scores = new int[totalPlayers];
//        currentBoxesOnBoard = 0;
//        totalBoxesOnBoard = (rows - 1) * (cols - 1);
//        gameOver = false;
//    }
//
//    private void startGame() {
//        nextPlayer();
//    }
//
//
//    private void nextPlayer() {
//        if (currentBoxesOnBoard == totalBoxesOnBoard) {
//            gameOver = true;
//            stopGame();
//            return;
//        }
//
//        currentPlayer++;
//        if (currentPlayer > totalPlayers)
//            currentPlayer = 1;
//        if (playMode == PLAY_MODE_SINGLE && currentPlayer == 1) {
//            computerHandler = new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        Thread.sleep(500);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    makeCalculatedMove();
//                }
//            });
//            computerHandler.start();
//        } else if (playMode == PLAY_MODE_MULTI && useP2p) {
//            StringBuilder builder = new StringBuilder();
//            /*previousRow;
//            previousCol;
//            dotPressed;
//            startingPlayerLine;
//            draggingPlayerLine;
//            draggingLineData;
//            draggingLineX1;
//            draggingLineY1;
//            draggingLineX2;
//            draggingLineY2;
//            currentPlayer;
//            currentBoxesOnBoard;
//            totalBoxesOnBoard;
//            gameOver;
//            builder.append();
//            chatManager.write(builder.toString().getBytes());*/
//        }
//    }
//
//    private void stopGame() {
//        // Toast the score
//        postScore();
//    }
//
//    private void postScore() {
//        dialog.showDialog(playMode, scores, gameOver);
//    }
//
//    @Override
//    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
//        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
//        textureProgram = new TextureShaderProgram(context);
//        tableTexture = TextureHelper.loadTexture(context, R.drawable.bg_table);
//        paperTexture = TextureHelper.loadTexture(context, R.drawable.bg_paper2);
//        lineBlackTexture = TextureHelper.loadTexture(context, R.drawable.line_black);
//        lineRedTexture = TextureHelper.loadTexture(context, R.drawable.line_red);
//        lineBlueTexture = TextureHelper.loadTexture(context, R.drawable.line_blue);
//        playerTexture = TextureHelper.loadTexture(context, R.drawable.player);
//        player1Texture = TextureHelper.loadTexture(context, R.drawable.player1);
//        player2Texture = TextureHelper.loadTexture(context, R.drawable.player2);
//        computerTexture = TextureHelper.loadTexture(context, R.drawable.player_computer);
//        defaultDotTexture = TextureHelper.loadTexture(context, R.drawable.whiteball);
//        pressedDotTexture = TextureHelper.loadTexture(context, R.drawable.whitepool);
//        dotShadowTexture = TextureHelper.loadTexture(context, R.drawable.shadow);
//        colorProgram = new ColorShaderProgram(context);
//    }
//
//    @Override
//    public void onSurfaceChanged(GL10 gl, int width, int height) {
//        glViewport(0, 0, width, height);
//        final float aspectRatio = width > height ?
//                (float) width / (float) height :
//                (float) height / (float) width;
//        if (width > height) {
//            orthoM(projectionMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, -1f, 1f);
//        } else {
//            orthoM(projectionMatrix, 0, -1f, 1f, -aspectRatio, aspectRatio, -1f, 1f);
//        }
//        setIdentityM(viewMatrix, 0);
//    }
//
//    @Override
//    public void onDrawFrame(GL10 gl) {
//        glClear(GL_COLOR_BUFFER_BIT);
//        multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
//        invertM(invertedViewProjectionMatrix, 0, viewProjectionMatrix, 0);
//
//        // draw table
//        positionTableInScene();
//        textureProgram.useProgram();
//        textureProgram.setUniforms(modelViewProjectionMatrix, tableTexture);
//        table.bindData(textureProgram);
//        table.draw();
//
//        // draw paper
//        positionPaperInScene();
//        textureProgram.useProgram();
//        textureProgram.setUniforms(modelViewProjectionMatrix, paperTexture);
//        paper.bindData(textureProgram);
//        paper.draw();
//
//        LineData lineData;
//        // draw lines
//        for (int i = 0; i < linesArray.size(); i++) {
//            positionRectLineInScene(0, 0, 0);
//            textureProgram.useProgram();
//            lineData = linesArray.get(i);
//            rectLine.bindData(lineData.line.point1.x, lineData.line.point1.y,
//                    lineData.line.point2.x, lineData.line.point2.y);
//            switch (playMode) {
//                case PLAY_MODE_SINGLE:
//                    if (lineData.player == 1) {
//                        textureProgram.setUniforms(modelViewProjectionMatrix, lineBlackTexture);
//                    } else {
//                        textureProgram.setUniforms(modelViewProjectionMatrix, lineRedTexture);
//                    }
//                    break;
//                case PLAY_MODE_MULTI:
//                    if (lineData.player == 1) {
//                        textureProgram.setUniforms(modelViewProjectionMatrix, lineRedTexture);
//                    } else if (lineData.player == 2) {
//                        textureProgram.setUniforms(modelViewProjectionMatrix, lineBlueTexture);
//                    } else {
//                        textureProgram.setUniforms(modelViewProjectionMatrix, lineBlackTexture);
//                    }
//                    break;
//            }
//            textureProgram.setAttributes(rectLine.vertexBuffer);
//            rectLine.draw();
//        }
//
//        // draw draggingPlayerLine line_black
//        positionRectLineInScene(0, 0, 0);
//        textureProgram.useProgram();
//        dragLine.bindData(draggingLineX1, draggingLineY1, draggingLineX2, draggingLineY2);
//        switch (playMode) {
//            case PLAY_MODE_SINGLE:
//                if (draggingLineData.player == 1) {
//                    textureProgram.setUniforms(modelViewProjectionMatrix, lineBlackTexture);
//                } else {
//                    textureProgram.setUniforms(modelViewProjectionMatrix, lineRedTexture);
//                }
//                break;
//            case PLAY_MODE_MULTI:
//                if (draggingLineData.player == 1) {
//                    textureProgram.setUniforms(modelViewProjectionMatrix, lineRedTexture);
//                } else if (draggingLineData.player == 2) {
//                    textureProgram.setUniforms(modelViewProjectionMatrix, lineBlueTexture);
//                } else {
//                    textureProgram.setUniforms(modelViewProjectionMatrix, lineBlackTexture);
//                }
//                break;
//        }
//        textureProgram.setAttributes(dragLine.vertexBuffer);
//        dragLine.draw();
//
//        // draw dot shadows
//        textureProgram.useProgram();
//        for (int row = 0; row < rows; row++) {
//            for (int col = 0; col < cols; col++) {
//                float x = convertColToPoint(col, cols);
//                float y = convertRowToPoint(row, rows);
//                positionDotInScene(x, y, 0f);
//                if (dotsPointsArray[row][col].dotPressed) {
//                    selectedDot.bindData(textureProgram);
//                    textureProgram.setUniforms(modelViewProjectionMatrix, dotShadowTexture);
//                    selectedDot.draw();
//                } else {
//                    dot.bindData(textureProgram);
//                    textureProgram.setUniforms(modelViewProjectionMatrix, dotShadowTexture);
//                    dot.draw();
//                }
//            }
//
//        }
//
//        // draw dots
//        textureProgram.useProgram();
//        for (int row = 0; row < rows; row++) {
//            for (int col = 0; col < cols; col++) {
//                float x = convertColToPoint(col, cols);
//                float y = convertRowToPoint(row, rows);
//                positionDotInScene(x, y, 0f);
//                if (dotsPointsArray[row][col].dotPressed) {
//                    selectedDot.bindData(textureProgram);
//                    textureProgram.setUniforms(modelViewProjectionMatrix, pressedDotTexture);
//                    selectedDot.draw();
//                } else {
//                    dot.bindData(textureProgram);
//                    textureProgram.setUniforms(modelViewProjectionMatrix, defaultDotTexture);
//                    dot.draw();
//                }
//            }
//        }
//
//        // draw players
//        textureProgram.useProgram();
//        playerDot.bindData(textureProgram);
//        for (int row = 0; row < rows - 1; row++) {
//            for (int col = 0; col < cols - 1; col++) {
//                float x = (float) (convertColToPoint(col, cols) + colWidth / 2.0);
//                float y = (float) (convertRowToPoint(row, rows) - rowHeight / 2.0);
//                positionPlayerInScene(x, y, 0f);
//                if (gameBoard[row][col] != 0) {
//                    switch (playMode) {
//                        case PLAY_MODE_SINGLE:
//                            if (gameBoard[row][col] == 1) {
//                                textureProgram.setUniforms(modelViewProjectionMatrix, computerTexture);
//                            } else {
//                                textureProgram.setUniforms(modelViewProjectionMatrix, playerTexture);
//                            }
//                            break;
//                        case PLAY_MODE_MULTI:
//                            if (gameBoard[row][col] == 1) {
//                                textureProgram.setUniforms(modelViewProjectionMatrix, player1Texture);
//                            } else if (gameBoard[row][col] == 2) {
//                                textureProgram.setUniforms(modelViewProjectionMatrix, player2Texture);
//                            } else {
//                                textureProgram.setUniforms(modelViewProjectionMatrix, playerTexture);
//                            }
//                            break;
//                    }
//                    playerDot.draw();
//                }
//            }
//        }
//
//    }
//
//    private void positionTableInScene() {
//        setIdentityM(modelMatrix, 0);
//        rotateM(modelMatrix, 0, 90, 0f, 0f, 1.0f);
//        scaleM(modelMatrix, 0, 1.5f, 1.5f, 1f);
//        multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix, 0, modelMatrix, 0);
//    }
//
//    private void positionPaperInScene() {
//        setIdentityM(modelMatrix, 0);
//        multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix,
//                0, modelMatrix, 0);
//    }
//
//    private void positionDotInScene(float x, float y, float z) {
//        setIdentityM(modelMatrix, 0);
//        translateM(modelMatrix, 0, x, y, z);
//        multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix,
//                0, modelMatrix, 0);
//    }
//
//    private void positionRectLineInScene(float x, float y, float z) {
//        setIdentityM(modelMatrix, 0);
//        translateM(modelMatrix, 0, x, y, z);
//        multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix,
//                0, modelMatrix, 0);
//    }
//
//    private void positionPlayerInScene(float x, float y, float z) {
//        setIdentityM(modelMatrix, 0);
//        translateM(modelMatrix, 0, x, y, z);
//        multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix,
//                0, modelMatrix, 0);
//    }
//
//    public void handleTouchPress(float normalizedX, float normalizedY) {
//        if (playMode == PLAY_MODE_SINGLE && currentPlayer == 1)
//            return;
//        Ray ray = convertNormalized2DPointToRay(normalizedX, normalizedY);
//        Plane plane = new Plane(new Point(0, 0, 0), new Vector(0, 0, 1));
//        Point touchedPoint = Geometry.intersectionPoint(ray, plane);
//        int row = findNearestRow(clamp(touchedPoint.y, bottomBound, topBound), rows);
//        int col = findNearestCol(clamp(touchedPoint.x, leftBound, rightBound), cols);
//        float x = convertColToPoint(col, cols);
//        float y = convertRowToPoint(row, rows);
//        dotBoundingSphere = new Sphere(new Point(x, y, 0f), boundingSphereRadius);
//        dotPressed = intersects(dotBoundingSphere, ray);
//        if (dotPressed) {
//            makePlayerMove(row, col);
//            draggingLineX1 = draggingLineX2 = convertColToPoint(col, cols);
//            draggingLineY1 = draggingLineY2 = convertRowToPoint(row, rows);
//            draggingPlayerLine = false;
//        }
//    }
//
//    public void handleTouchDrag(float normalizedX, float normalizedY) {
//        if (playMode == PLAY_MODE_SINGLE && currentPlayer == 1)
//            return;
//        if (dotPressed) {
////            Ray ray = convertNormalized2DPointToRay(normalizedX, normalizedY);
////            Plane plane = new Plane(new Point(0, 0, 0), new Vector(0, 0, 1));
////            Point touchedPoint = Geometry.intersectionPoint(ray, plane);
////            draggingLineX2 = clamp(touchedPoint.x, bottomBound, topBound);
////            draggingLineY2 = clamp(touchedPoint.y, bottomBound, topBound);
//            if (!draggingPlayerLine) {
//                draggingPlayerLine = true;
//            }
//        }
//    }
//
//    public void handleTouchLift(float normalizedX, float normalizedY) {
//        if (playMode == PLAY_MODE_SINGLE && currentPlayer == 1)
//            return;
//        if (dotPressed) {
//            if (!draggingPlayerLine) {
//                return;
//            }
//            Ray ray = convertNormalized2DPointToRay(normalizedX, normalizedY);
//            Plane plane = new Plane(new Point(0, 0, 0), new Vector(0, 0, 1));
//            Point touchedPoint = Geometry.intersectionPoint(ray, plane);
//
//            float x1 = convertColToPoint(previousCol, cols);
//            float y1 = convertRowToPoint(previousRow, rows);
//            Ray lineRay = new Ray(new Point(x1, y1, 0f), vectorBetween(new Point(x1, y1, 0f), touchedPoint));
//
//            int row = findNearestRow(clamp(touchedPoint.y, bottomBound, topBound), rows);
//            int col = findNearestCol(clamp(touchedPoint.x, leftBound, rightBound), cols);
//            // dragging left
//            if (col < previousCol && col >= 0) {
//                row = previousRow;
//                col = previousCol - 1;
//            }
//            // dragging right
//            else if (col > previousCol && col <= cols - 1) {
//                row = previousRow;
//                col = previousCol + 1;
//            }
//            // dragging up
//            else if (row < previousRow && row >= 0) {
//                row = previousRow - 1;
//                col = previousCol;
//            }
//            // dragging down
//            else if (row > previousRow && row <= rows - 1) {
//                row = previousRow + 1;
//                col = previousCol;
//            }
//            float x2 = convertColToPoint(col, cols);
//            float y2 = convertRowToPoint(row, rows);
//            Sphere dotBoundingSphere = new Sphere(new Point(x2, y2, 0f), boundingSphereRadius);
//            boolean dotPressed = intersects(dotBoundingSphere, lineRay);
//            if (dotPressed) {
//                makePlayerMove(row, col);
//            }
//            this.dotPressed = false;
//            draggingLineX1 = draggingLineX2;
//            draggingLineY1 = draggingLineY2;
//            draggingPlayerLine = false;
//        }
//    }
//
//    public void handlePause() {
//        dialog.showDialog(playMode, scores, gameOver);
//    }
//
//    private float clamp(float value, float min, float max) {
//        return Math.min(max, Math.max(value, min));
//    }
//
//    // returns the closest dot point to this point which is a multiple of distanceApart.
//    // numRows is the number of rows.
//    // point is the point being rounded.
//    private float findRoundedRowPoint(int numRows, double point) {
//        double nearest = 2.0 / (numRows - 1);
//        return (float) (nearest * (Math.round(point / nearest)));
//    }
//
//    // returns the closest dot point to this point which is a multiple of distanceApart.
//    // numCols is the number of cols.
//    // point is the point being rounded.
//    private float findRoundedColPoint(int numCols, double point) {
//        double nearest = 2.0 / (numCols - 1);
//        return (float) (nearest * (Math.round(point / nearest)));
//    }
//
//
//    // returns the dot point closest to this point.
//    // point is the touchedPoint y.
//    // numRows is the number of rows
//    private float findNearestRowPoint(float point, int numRows) {
//        return convertRowToPoint(findNearestRow(point, numRows), numRows);
//    }
//
//    // returns the dot point closest to this point.
//    // point is the touchedPoint x.
//    // numRows is the number of cols.
//    private float findNearestColPoint(float point, int numCols) {
//        return convertColToPoint(findNearestCol(point, numCols), numCols);
//    }
//
//    // returns the touchedPoint point.
//    // row is the nth row.
//    // numRows is the number of rows.
//    private float convertRowToPoint(int row, int numRows) {
//        return (float) ((((numRows + 1) / 2.0) - (row + 1)) * rowHeight);
//    }
//
//
//    // returns the touchedPoint point.
//    // col is the nth col.
//    // numCols is the number of cols.
//    private float convertColToPoint(int col, int numCols) {
//        return (float) (((col + 1) - ((numCols + 1) / 2.0)) * colWidth);
//    }
//
//    // returns the row.
//    // point is the touchedPoint y.
//    // numRows is the number of rows.
//    private int findNearestRow(float point, int numRows) {
//        return (int) Math.round(((-point * (numRows - 1)) + (numRows - 1)) / 2.0);
//    }
//
//    // returns the col.
//    // point is the touchedPoint x.
//    // numCols is the number of cols.
//    private int findNearestCol(float point, int numCols) {
//        return (int) Math.round(((point * (numCols - 1)) + (numCols - 1)) / 2.0);
//    }
//
//    private void makePlayerMove(int row, int col) {
//        // if single player and current player is the computer, return
//        if (playMode == PLAY_MODE_SINGLE && currentPlayer == 1)
//            return;
//
//        if (!draggingPlayerLine) {
//            if (!startingPlayerLine) {
//                dotsPointsArray[row][col].dotPressed = true;
//                previousRow = row;
//                previousCol = col;
//                startingPlayerLine = true;
//            } else {
//                dotsPointsArray[previousRow][previousCol].dotPressed = false;
//                startingPlayerLine = false;
//                move(row, col);
//            }
//        } else {
//            dotsPointsArray[previousRow][previousCol].dotPressed = false;
//            startingPlayerLine = false;
//            move(row, col);
//        }
//    }
//
//    private void move(int row, int col) {
//        if (canMakeLine(row, col)) {
//            // add line_black to lines array
//            makeLine(row, col);
//            // check if square is closed
//            checkSquares(row, col);
//            // set next player
//            nextPlayer();
//        }
//    }
//
//
//    private boolean canMakeLine(int row, int col) {
//        switch (lineDirection(row, col)) {
//            case DIRECTION_LEFT:
//                if (dotsPointsArray[previousRow][previousCol].leftDrawn && dotsPointsArray[row][col].rightDrawn) {
//                    return false;
//                }
//                dotsPointsArray[previousRow][previousCol].leftDrawn = true;
//                dotsPointsArray[row][col].rightDrawn = true;
//                return true;
//            case DIRECTION_RIGHT:
//                if (dotsPointsArray[previousRow][previousCol].rightDrawn && dotsPointsArray[row][col].leftDrawn) {
//                    return false;
//                }
//                dotsPointsArray[previousRow][previousCol].rightDrawn = true;
//                dotsPointsArray[row][col].leftDrawn = true;
//                return true;
//            case DIRECTION_UP:
//                if (dotsPointsArray[previousRow][previousCol].upDrawn && dotsPointsArray[row][col].downDrawn) {
//                    return false;
//                }
//                dotsPointsArray[previousRow][previousCol].upDrawn = true;
//                dotsPointsArray[row][col].downDrawn = true;
//                return true;
//            case DIRECTION_DOWN:
//                if (dotsPointsArray[previousRow][previousCol].downDrawn && dotsPointsArray[row][col].upDrawn) {
//                    return false;
//                }
//                dotsPointsArray[previousRow][previousCol].downDrawn = true;
//                dotsPointsArray[row][col].upDrawn = true;
//                return true;
//            default:
//                return false;
//        }
//    }
//
//    private int lineDirection(int row, int col) {
//        if (col == previousCol - 1 && row == previousRow)
//            return DIRECTION_LEFT;
//        if (col == previousCol + 1 && row == previousRow)
//            return DIRECTION_RIGHT;
//        if (row == previousRow - 1 && col == previousCol)
//            return DIRECTION_UP;
//        if (row == previousRow + 1 && col == previousCol)
//            return DIRECTION_DOWN;
//        return DIRECTION_NONE;
//    }
//
//    private class LineData {
//        public LineSegment line;
//        public int player;
//
//        public LineData(LineSegment line, int player) {
//            this.line = line;
//            this.player = player;
//        }
//    }
//
//    private void makeLine(int row, int col) {
//        linesArray.add(new LineData(new LineSegment(
//                new Point(dotsPointsArray[previousRow][previousCol].x,
//                        dotsPointsArray[previousRow][previousCol].y,
//                        dotsPointsArray[previousRow][previousCol].z),
//                new Point(dotsPointsArray[row][col].x,
//                        dotsPointsArray[row][col].y,
//                        dotsPointsArray[row][col].z)), currentPlayer));
//    }
//
//    private void checkSquares(int row, int col) {
//        switch (lineDirection(row, col)) {
//            case DIRECTION_LEFT:
//                // check up
//                if (row != 0) {
//                    // check if square is empty
//                    if (gameBoard[row - 1][col] == 0) {
//                        if (dotsPointsArray[row][col].upDrawn && dotsPointsArray[row - 1][col].rightDrawn &&
//                                dotsPointsArray[row - 1][col + 1].downDrawn && dotsPointsArray[row][col + 1].leftDrawn) {
//                            gameBoard[row - 1][col] = currentPlayer;
//                            scores[currentPlayer - 1]++;
//                            currentBoxesOnBoard++;
//                        }
//                    }
//                }
//                // check down
//                if (row != rows - 1) {
//                    if (gameBoard[row][col] == 0) {
//                        if (dotsPointsArray[row][col].downDrawn && dotsPointsArray[row + 1][col].rightDrawn &&
//                                dotsPointsArray[row + 1][col + 1].upDrawn && dotsPointsArray[row][col + 1].leftDrawn) {
//                            gameBoard[row][col] = currentPlayer;
//                            scores[currentPlayer - 1]++;
//                            currentBoxesOnBoard++;
//                        }
//                    }
//                }
//                break;
//            case DIRECTION_RIGHT:
//                // check up
//                if (row != 0) {
//                    if (gameBoard[row - 1][col - 1] == 0) {
//                        if (dotsPointsArray[row][col].upDrawn && dotsPointsArray[row - 1][col].leftDrawn &&
//                                dotsPointsArray[row - 1][col - 1].downDrawn && dotsPointsArray[row][col - 1].rightDrawn) {
//                            gameBoard[row - 1][col - 1] = currentPlayer;
//                            scores[currentPlayer - 1]++;
//                            currentBoxesOnBoard++;
//                        }
//                    }
//                }
//                // check down
//                if (row != rows - 1) {
//                    if (gameBoard[row][col - 1] == 0) {
//                        if (dotsPointsArray[row][col].downDrawn && dotsPointsArray[row + 1][col].leftDrawn &&
//                                dotsPointsArray[row + 1][col - 1].upDrawn && dotsPointsArray[row][col - 1].rightDrawn) {
//                            gameBoard[row][col - 1] = currentPlayer;
//                            scores[currentPlayer - 1]++;
//                            currentBoxesOnBoard++;
//                        }
//                    }
//                }
//                break;
//            case DIRECTION_UP:
//                // check left
//                if (col != 0) {
//                    if (gameBoard[row][col - 1] == 0) {
//                        if (dotsPointsArray[row][col].leftDrawn && dotsPointsArray[row][col - 1].downDrawn &&
//                                dotsPointsArray[row + 1][col - 1].rightDrawn && dotsPointsArray[row + 1][col].upDrawn) {
//                            gameBoard[row][col - 1] = currentPlayer;
//                            scores[currentPlayer - 1]++;
//                            currentBoxesOnBoard++;
//                        }
//                    }
//                }
//                // check right
//                if (col != cols - 1) {
//                    if (gameBoard[row][col] == 0) {
//                        if (dotsPointsArray[row][col].rightDrawn && dotsPointsArray[row][col + 1].downDrawn &&
//                                dotsPointsArray[row + 1][col + 1].leftDrawn && dotsPointsArray[row + 1][col].upDrawn) {
//                            gameBoard[row][col] = currentPlayer;
//                            scores[currentPlayer - 1]++;
//                            currentBoxesOnBoard++;
//                        }
//                    }
//                }
//                break;
//            case DIRECTION_DOWN:
//                // check left
//                if (col != 0) {
//                    if (gameBoard[row - 1][col - 1] == 0) {
//                        if (dotsPointsArray[row][col].leftDrawn && dotsPointsArray[row][col - 1].upDrawn &&
//                                dotsPointsArray[row - 1][col - 1].rightDrawn && dotsPointsArray[row - 1][col].downDrawn) {
//                            gameBoard[row - 1][col - 1] = currentPlayer;
//                            scores[currentPlayer - 1]++;
//                            currentBoxesOnBoard++;
//                        }
//                    }
//                }
//                // check right
//                if (col != cols - 1) {
//                    if (gameBoard[row - 1][col] == 0) {
//                        if (dotsPointsArray[row][col].rightDrawn && dotsPointsArray[row][col + 1].upDrawn &&
//                                dotsPointsArray[row - 1][col + 1].leftDrawn && dotsPointsArray[row - 1][col].downDrawn) {
//                            gameBoard[row - 1][col] = currentPlayer;
//                            scores[currentPlayer - 1]++;
//                            currentBoxesOnBoard++;
//                        }
//                    }
//                }
//                break;
//            default:
//                break;
//        }
//    }
//
////    private void makeCalculatedMove() {
////        if (((playMode == PLAY_MODE_SINGLE) && (currentPlayer != 1)) || (playMode == PLAY_MODE_MULTI))
////            return;
////
////        ArrayList<PossibleWin> pws = possibleWins();
////        /*int winIndex = (int)(Math.random()*(pws.size()+1)); // make random move
////        if (pws.isEmpty() || winIndex >= pws.size()) {         // if possible wins is
////                                                                // empty or random number
////                                                                 // is greater than size
////                                                                  //of possible wins
////            makeRandomMove();
////            return;
////        }*/
////
////        if (pws.isEmpty()) { // make random move
////            makeRandomMove(); // only if possible wins is empty
////            return;
////        }
////
////        int winIndex = (int) (Math.random() * (pws.size()));
////        previousRow = pws.get(winIndex).fromRow;
////        previousCol = pws.get(winIndex).fromCol;
////
////        int row = pws.get(winIndex).toRow;
////        int col = pws.get(winIndex).toCol;
////        move(row, col);
////    }
//
//    private void makeCalculatedMove() {
//        if (((playMode == PLAY_MODE_SINGLE) && (currentPlayer != 1)) || (playMode == PLAY_MODE_MULTI))
//            return;
//
//        ArrayList<SquareInfo>[] squareInfos = getSquareInfos();
//        if (!(squareInfos[1].isEmpty())) {
//            makeRandomMove(squareInfos, 1);
//        } else if (!squareInfos[3].isEmpty() || !squareInfos[4].isEmpty()) {
//            int rand;
//            if (!squareInfos[3].isEmpty() && !squareInfos[4].isEmpty())
//                rand = (int) (Math.random() * 1);
//            else if (!squareInfos[3].isEmpty())
//                rand = 0;
//            else
//                rand = 1;
//            switch (rand) {
//                case 0:
//                    makeRandomMove(squareInfos, 3);
//                    break;
//                case 1:
//                    makeRandomMove(squareInfos, 4);
//                    break;
//            }
//        } else if (!squareInfos[2].isEmpty()) {
//            makeRandomMove(squareInfos, 2);
//        }
//    }
//
//    private void makeRandomMove(ArrayList<SquareInfo>[] squareInfos, int numNotDrawn) {
//        ArrayList<SquareInfo> notDrawnInfos = squareInfos[numNotDrawn];
//        int randInfoIndex = (int) (Math.random() * (notDrawnInfos.size()));
//        SquareInfo info = notDrawnInfos.get(randInfoIndex);
//        ArrayList<SquareInfo.LegalMove> legalMoves = info.getLegalMovesArray();
//        int randMoveIndex = (int) (Math.random() * (legalMoves.size()));
//        SquareInfo.LegalMove legalMove = legalMoves.get(randMoveIndex);
//        previousRow = legalMove.fromRow;
//        previousCol = legalMove.fromCol;
//        int row = legalMove.toRow;
//        int col = legalMove.toCol;
//        move(row, col);
//    }
//
//    public static class SquareInfo {
//
//        public static class LegalMove {
//            int fromRow, fromCol;
//            int toRow, toCol;
//        }
//
//        private ArrayList<LegalMove> legalMovesArray = new ArrayList<>();
//
//        public ArrayList<LegalMove> getLegalMovesArray() {
//            return legalMovesArray;
//        }
//
//        public int getNumNotDrawn() {
//            return legalMovesArray.size();
//        }
//
//        void add(LegalMove legalMove) {
//            legalMovesArray.add(legalMove);
//        }
//    }
//
//    private ArrayList<SquareInfo>[] getSquareInfos() {
//        ArrayList<SquareInfo>[] squareInfosArray = new ArrayList[5];
//        ArrayList<SquareInfo> zeroNotDrawnInfoArray = new ArrayList<>();
//        ArrayList<SquareInfo> oneNotDrawnInfoArray = new ArrayList<>();
//        ArrayList<SquareInfo> twoNotDrawnInfoArray = new ArrayList<>();
//        ArrayList<SquareInfo> threeNotDrawnInfoArray = new ArrayList<>();
//        ArrayList<SquareInfo> fourNotDrawnInfoArray = new ArrayList<>();
//        for (int row = 0; row < rows - 1; row++) {
//            for (int col = 0; col < cols - 1; col++) {
//                SquareInfo squareInfo = getSquareInfo(row, col);
//                switch (squareInfo.getNumNotDrawn()) {
//                    case 0:
//                        zeroNotDrawnInfoArray.add(squareInfo);
//                        break;
//                    case 1:
//                        oneNotDrawnInfoArray.add(squareInfo);
//                        break;
//                    case 2:
//                        twoNotDrawnInfoArray.add(squareInfo);
//                        break;
//                    case 3:
//                        threeNotDrawnInfoArray.add(squareInfo);
//                        break;
//                    case 4:
//                        fourNotDrawnInfoArray.add(squareInfo);
//                }
//            }
//        }
//
//        squareInfosArray[0] = zeroNotDrawnInfoArray; // all have been drawn so its already full
//        squareInfosArray[1] = oneNotDrawnInfoArray; // only one is not drawn so its possible win
//        squareInfosArray[2] = twoNotDrawnInfoArray; // half drawn
//        squareInfosArray[3] = threeNotDrawnInfoArray; // only one line drawn
//        squareInfosArray[4] = fourNotDrawnInfoArray; // no lines drawn around this square
//
//        return squareInfosArray;
//    }
//
//    private SquareInfo getSquareInfo(int row, int col) {
//        SquareInfo squareInfo = new SquareInfo();
//        SquareInfo.LegalMove legalMove;
//        Log.d("TAG", "row " + row + " col " + col);
//        if (gameBoard[row][col] == 0) {
//            if (!dotsPointsArray[row][col].rightDrawn) {
//                legalMove = new SquareInfo.LegalMove();
//                legalMove.fromRow = row;
//                legalMove.fromCol = col;
//                legalMove.toRow = row;
//                legalMove.toCol = col + 1;
//                squareInfo.add(legalMove);
//                Log.d("TAG", "rightDrawn ");
//            }
//            if (!dotsPointsArray[row][col + 1].downDrawn) {
//                legalMove = new SquareInfo.LegalMove();
//                legalMove.fromRow = row;
//                legalMove.fromCol = col + 1;
//                legalMove.toRow = row + 1;
//                legalMove.toCol = col + 1;
//                squareInfo.add(legalMove);
//                Log.d("TAG", "downDrawn ");
//            }
//            if (!dotsPointsArray[row + 1][col + 1].leftDrawn) {
//                legalMove = new SquareInfo.LegalMove();
//                legalMove.fromRow = row + 1;
//                legalMove.fromCol = col + 1;
//                legalMove.toRow = row + 1;
//                legalMove.toCol = col;
//                squareInfo.add(legalMove);
//                Log.d("TAG", "leftDrawn ");
//            }
//            if (!dotsPointsArray[row + 1][col].upDrawn) {
//                legalMove = new SquareInfo.LegalMove();
//                legalMove.fromRow = row + 1;
//                legalMove.fromCol = col;
//                legalMove.toRow = row;
//                legalMove.toCol = col;
//                squareInfo.add(legalMove);
//                Log.d("TAG", "upDrawn ");
//            }
//
//        }
//        return squareInfo;
//    }
//
//    public int findNumNotDrawn(int row, int col) {
//        int numNotDrawn = 0;
//        if (!dotsPointsArray[row][col].rightDrawn) {
//            numNotDrawn++;
//        }
//        if (!dotsPointsArray[row][col + 1].downDrawn) {
//            numNotDrawn++;
//        }
//        if (!dotsPointsArray[row + 1][col + 1].leftDrawn) {
//            numNotDrawn++;
//        }
//        if (!dotsPointsArray[row + 1][col].upDrawn) {
//            numNotDrawn++;
//        }
//
//        return numNotDrawn;
//    }
//
//    // holder class
//    private class PossibleWin {
//        int fromRow, fromCol;
//        int toRow, toCol;
//    }
//
//    private ArrayList<PossibleWin> possibleWins() {
//        ArrayList<PossibleWin> possibleWins = new ArrayList<>();
//        for (int row = 0; row < rows - 1; row++) {
//            for (int col = 0; col < cols - 1; col++) {
//                // check if square is empty
//                if (gameBoard[row][col] == 0) {
//                    if (!dotsPointsArray[row][col].rightDrawn && dotsPointsArray[row][col + 1].downDrawn &&
//                            dotsPointsArray[row + 1][col + 1].leftDrawn && dotsPointsArray[row + 1][col].upDrawn) {
//                        PossibleWin pw = new PossibleWin();
//                        pw.fromRow = row;
//                        pw.fromCol = col;
//                        pw.toRow = row;
//                        pw.toCol = col + 1;
//                        possibleWins.add(pw);
//                    } else if (dotsPointsArray[row][col].rightDrawn && !dotsPointsArray[row][col + 1].downDrawn &&
//                            dotsPointsArray[row + 1][col + 1].leftDrawn && dotsPointsArray[row + 1][col].upDrawn) {
//                        PossibleWin pw = new PossibleWin();
//                        pw.fromRow = row;
//                        pw.fromCol = col + 1;
//                        pw.toRow = row + 1;
//                        pw.toCol = col + 1;
//                        possibleWins.add(pw);
//                    } else if (dotsPointsArray[row][col].rightDrawn && dotsPointsArray[row][col + 1].downDrawn &&
//                            !dotsPointsArray[row + 1][col + 1].leftDrawn && dotsPointsArray[row + 1][col].upDrawn) {
//                        PossibleWin pw = new PossibleWin();
//                        pw.fromRow = row + 1;
//                        pw.fromCol = col + 1;
//                        pw.toRow = row + 1;
//                        pw.toCol = col;
//                        possibleWins.add(pw);
//                    } else if (dotsPointsArray[row][col].rightDrawn && dotsPointsArray[row][col + 1].downDrawn &&
//                            dotsPointsArray[row + 1][col + 1].leftDrawn && !dotsPointsArray[row + 1][col].upDrawn) {
//                        PossibleWin pw = new PossibleWin();
//                        pw.fromRow = row + 1;
//                        pw.fromCol = col;
//                        pw.toRow = row;
//                        pw.toCol = col;
//                        possibleWins.add(pw);
//                    }
//                }
//            }
//        }
//        return possibleWins;
//    }
//
////    private void makeRandomMove() {
////        if (((playMode == PLAY_MODE_SINGLE) && (currentPlayer != 1)) || (playMode == PLAY_MODE_MULTI))
////            return;
////
////        int row;
////        int col;
////
////        int[] rowcol;
////        do {
////            do {// find first point
////                row = (int) (Math.random() * rows);
////                col = (int) (Math.random() * cols);
////            }
////            while ((row < rows - 1 && row > 0 && col < cols - 1 && col > 0) && dotsPointsArray[row][col].upDrawn && dotsPointsArray[row][col].downDrawn &&
////                    dotsPointsArray[row][col].leftDrawn && dotsPointsArray[row][col].rightDrawn ||
////                    (row == 0 && (col != 0 || col != cols - 1) && dotsPointsArray[row][col].leftDrawn && dotsPointsArray[row][col].rightDrawn && dotsPointsArray[row][col].downDrawn) ||
////                    (row == 0 && col == 0 && dotsPointsArray[row][col].rightDrawn && dotsPointsArray[row][col].downDrawn) ||
////                    (row == 0 && col == cols - 1 && dotsPointsArray[row][col].leftDrawn && dotsPointsArray[row][col].downDrawn) ||
////                    (row == rows - 1 && (col != 0 || col != cols - 1) && dotsPointsArray[row][col].leftDrawn && dotsPointsArray[row][col].upDrawn && dotsPointsArray[row][col].rightDrawn) ||
////                    (row == rows - 1 && col == 0 && dotsPointsArray[row][col].rightDrawn && dotsPointsArray[row][col].upDrawn) ||
////                    (row == rows - 1 && col == cols - 1 && dotsPointsArray[row][col].leftDrawn && dotsPointsArray[row][col].upDrawn) ||
////                    (col == 0 && (row != 0 || row != rows - 1) && dotsPointsArray[row][col].upDrawn && dotsPointsArray[row][col].rightDrawn && dotsPointsArray[row][col].downDrawn) ||
////                    (col == 0 && row == 0 && dotsPointsArray[row][col].rightDrawn && dotsPointsArray[row][col].downDrawn) ||
////                    (col == 0 && row == rows - 1 && dotsPointsArray[row][col].upDrawn && dotsPointsArray[row][col].rightDrawn) ||
////                    (col == cols - 1 && (row != 0 || row != rows - 1) && dotsPointsArray[row][col].upDrawn && dotsPointsArray[row][col].leftDrawn && dotsPointsArray[row][col].downDrawn) ||
////                    (col == cols - 1 && row == 0 && dotsPointsArray[row][col].leftDrawn && dotsPointsArray[row][col].downDrawn) ||
////                    (col == cols - 1 && row == rows - 1 && dotsPointsArray[row][col].upDrawn && dotsPointsArray[row][col].leftDrawn));
////
////            previousRow = row;
////            previousCol = col;
////
////            // find second point
////            rowcol = nextRandomRowCol();
////
////        } while (rowcol[0] == -1 || rowcol[1] == -1);
////
////        // add line_black to lines array
////        makeLine(rowcol[0], rowcol[1]);
////        // check if square is closed
////        checkSquares(rowcol[0], rowcol[1]);
////        // set next player
////        nextPlayer();
////    }
//
//    private int[] nextRandomRowCol() {
//        int row = -1, col = -1;
//        int[] rowcol = {row, col};
//        int direction;
//
//        direction = (int) (Math.random() * 4);
//        switch (direction) {
//            // up
//            case 0:
//                if (previousRow > 0) {
//                    if (!dotsPointsArray[previousRow][previousCol].upDrawn) {
//                        row = previousRow - 1;
//                        col = previousCol;
//                        dotsPointsArray[previousRow][previousCol].upDrawn = true;
//                        dotsPointsArray[row][col].downDrawn = true;
//                        rowcol[0] = row;
//                        rowcol[1] = col;
//                        return rowcol;
//                    }
//                } else {
//                    if (!dotsPointsArray[previousRow][previousCol].downDrawn) {
//                        row = previousRow + 1;
//                        col = previousCol;
//                        dotsPointsArray[previousRow][previousCol].downDrawn = true;
//                        dotsPointsArray[row][col].upDrawn = true;
//                        rowcol[0] = row;
//                        rowcol[1] = col;
//                        return rowcol;
//                    }
//                }
//                // down
//            case 1:
//                if (previousRow < rows - 1) {
//                    if (!dotsPointsArray[previousRow][previousCol].downDrawn) {
//                        row = previousRow + 1;
//                        col = previousCol;
//                        dotsPointsArray[previousRow][previousCol].downDrawn = true;
//                        dotsPointsArray[row][col].upDrawn = true;
//                        rowcol[0] = row;
//                        rowcol[1] = col;
//                        return rowcol;
//                    }
//                } else {
//                    if (!dotsPointsArray[previousRow][previousCol].upDrawn) {
//                        row = previousRow - 1;
//                        col = previousCol;
//                        dotsPointsArray[previousRow][previousCol].upDrawn = true;
//                        dotsPointsArray[row][col].downDrawn = true;
//                        rowcol[0] = row;
//                        rowcol[1] = col;
//                        return rowcol;
//                    }
//                }
//                // left
//            case 2:
//                if (previousCol > 0) {
//                    if (!dotsPointsArray[previousRow][previousCol].leftDrawn) {
//                        row = previousRow;
//                        col = previousCol - 1;
//                        dotsPointsArray[previousRow][previousCol].leftDrawn = true;
//                        dotsPointsArray[row][col].rightDrawn = true;
//                        rowcol[0] = row;
//                        rowcol[1] = col;
//                        return rowcol;
//                    }
//                } else {
//                    if (!dotsPointsArray[previousRow][previousCol].rightDrawn) {
//                        row = previousRow;
//                        col = previousCol + 1;
//                        dotsPointsArray[previousRow][previousCol].rightDrawn = true;
//                        dotsPointsArray[row][col].leftDrawn = true;
//                        rowcol[0] = row;
//                        rowcol[1] = col;
//                        return rowcol;
//                    }
//                }
//                // right
//            case 3:
//                if (previousCol < cols - 1) {
//                    if (!dotsPointsArray[previousRow][previousCol].rightDrawn) {
//                        row = previousRow;
//                        col = previousCol + 1;
//                        dotsPointsArray[previousRow][previousCol].rightDrawn = true;
//                        dotsPointsArray[row][col].leftDrawn = true;
//                        rowcol[0] = row;
//                        rowcol[1] = col;
//                        return rowcol;
//                    }
//                } else {
//                    if (!dotsPointsArray[previousRow][previousCol].leftDrawn) {
//                        row = previousRow;
//                        col = previousCol - 1;
//                        dotsPointsArray[previousRow][previousCol].leftDrawn = true;
//                        dotsPointsArray[row][col].rightDrawn = true;
//                        rowcol[0] = row;
//                        rowcol[1] = col;
//                        return rowcol;
//                    }
//                }
//
//            default:
//                break;
//        }
//
//        return rowcol;
//    }
//
//    private Ray convertNormalized2DPointToRay(float normalizedX, float normalizedY) {
//        final float[] nearPointNdc = {normalizedX, normalizedY, -1, 1};
//        final float[] farPointNdc = {normalizedX, normalizedY, 1, 1};
//        final float[] nearPointWorld = new float[4];
//        final float[] farPointWorld = new float[4];
//        multiplyMV(
//                nearPointWorld, 0, invertedViewProjectionMatrix, 0, nearPointNdc, 0);
//        multiplyMV(
//                farPointWorld, 0, invertedViewProjectionMatrix, 0, farPointNdc, 0);
//        divideByW(nearPointWorld);
//        divideByW(farPointWorld);
//        Point nearPointRay =
//                new Point(nearPointWorld[0], nearPointWorld[1], nearPointWorld[2]);
//        Point farPointRay =
//                new Point(farPointWorld[0], farPointWorld[1], farPointWorld[2]);
//        return new Ray(nearPointRay,
//                vectorBetween(nearPointRay, farPointRay));
//    }
//
//    private void divideByW(float[] vector) {
//        vector[0] /= vector[3];
//        vector[1] /= vector[3];
//        vector[2] /= vector[3];
//    }
//
//    private void readAndPlay(String readMessage) {
//
//    }
//
//}
