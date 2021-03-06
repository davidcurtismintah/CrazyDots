//package com.allow.crazydots;
//
//import android.content.Intent;
//import android.net.wifi.p2p.WifiP2pInfo;
//import android.opengl.GLSurfaceView;
//import android.support.v7.app.AppCompatActivity;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.MotionEvent;
//import android.view.View;
//import android.view.WindowManager;
//
//public class CrazyDotsActivity extends AppCompatActivity {
//
//    private GLSurfaceView glSurfaceView;
//    private CrazyDotsRenderer crazyDotsRenderer;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        super.onCreate(savedInstanceState);
//        glSurfaceView = new GLSurfaceView(this);
//        glSurfaceView.setEGLContextClientVersion(2);
//        glSurfaceView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (event != null) {
//                    final float normalizedX =
//                            (event.getX() / ( float) v.getWidth()) * 2 - 1;
//                    final float normalizedY =
//                            -((event.getY() / ( float) v.getHeight()) * 2 - 1);
//                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                        glSurfaceView.queueEvent( new Runnable() {
//                            @Override
//                            public void run() {
//                                crazyDotsRenderer.handleTouchPress(
//                                        normalizedX, normalizedY);
//                            }
//                        });
//                    } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
//                        glSurfaceView.queueEvent( new Runnable() {
//                            @Override
//                            public void run() {
//                                crazyDotsRenderer.handleTouchDrag(
//                                        normalizedX, normalizedY);
//                            }
//                        });
//                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
//                        glSurfaceView.queueEvent( new Runnable() {
//                            @Override
//                            public void run() {
//                                crazyDotsRenderer.handleTouchLift(
//                                        normalizedX, normalizedY);
//                            }
//                        });
//                    }
//
//                    return true;
//                } else {
//                    return false;
//                }
//            }
//        });
//        int playMode = 0;
//        int rows = 0;
//        int cols = 0;
//        int numPlayers = 0;
//        Intent intent = getIntent();
//        if (intent != null){
//            Bundle bundle = intent.getExtras();
//            playMode = bundle.getInt(MenuScreen.PLAY_MODE, CrazyDotsRenderer.PLAY_MODE_SINGLE);
//            rows = bundle.getInt(MenuScreen.NUM_DOT_ROWS, 0);
//            cols = bundle.getInt(MenuScreen.NUM_DOT_COLS, 0);
//            numPlayers = bundle.getInt(MenuScreen.NUM_PLAYERS, 0);
//        }
//
//        crazyDotsRenderer = new CrazyDotsRenderer(this, playMode, rows, cols, numPlayers);
//        glSurfaceView.setRenderer(crazyDotsRenderer);
//
//        setContentView(glSurfaceView);
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        glSurfaceView.onPause();
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        glSurfaceView.onResume();
//    }
//
//    @Override
//    public void onBackPressed() {
//        crazyDotsRenderer.handlePause();
//    }
//}
