package com.allow.crazydots.util;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.allow.crazydots.CrazyDotsRenderer;

public class DialogHelper {

    private AlertDialog dialog;
    private StringBuilder stringBuilder;
    private static Context context;

    public static DialogHelper getInstance(final Context context, final CrazyDotsRenderer crazyDotsRenderer) {
        DialogHelper.context = context;
        AlertDialog.Builder dialogbuilder = new AlertDialog.Builder(context);
        dialogbuilder.setPositiveButton("PLAY AGAIN", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    crazyDotsRenderer.playAgain();
                }
            }
        });
        dialogbuilder.setNegativeButton("MENU", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_NEGATIVE) {
                    ((Activity) context).finish();
                    // int pid= android.os.Process.myPid();
                    // android.os.Process.killProcess(pid);
                }
            }
        });
        dialogbuilder.setNeutralButton("CONTINUE GAME", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        DialogHelper dialogHelper = new DialogHelper();
        dialogHelper.setDialog(dialogbuilder.create());

        return dialogHelper;
    }

    public void setDialog(AlertDialog dialog) {
        this.dialog = dialog;
    }

    public void showDialog(int playMode, int[] scores, boolean gameOver) {
        int highScore = 0;
        int highPlayer = 1;
        stringBuilder = new StringBuilder();
        for (int i = 0; i < scores.length; i++) {
            if (playMode == CrazyDotsRenderer.PLAY_MODE_SINGLE) {
                if (i == 0) {
                    stringBuilder.append("Computer");
                } else if (i == 1) {
                    stringBuilder.append("Your");
                }
            } else {
                stringBuilder.append("Player ");
                stringBuilder.append(i + 1);
            }

            stringBuilder.append(" Score");
            stringBuilder.append(" : ");
            stringBuilder.append(scores[i]);
            stringBuilder.append("\n\n");
            if (scores[i] > highScore) {
                highScore = scores[i];
                highPlayer = i + 1;
            }
        }
        if (gameOver) {
            stringBuilder.append("\n\n");
            if (playMode == CrazyDotsRenderer.PLAY_MODE_SINGLE) {
                if (highPlayer == 1) {
                    stringBuilder.append("Computer");
                } else if (highPlayer == 2) {
                    stringBuilder.append("You");
                }
            } else {
                stringBuilder.append("Player ");
                stringBuilder.append(highPlayer);
            }
            stringBuilder.append(" Won");
        }
        dialog.setTitle(gameOver ? "Game Over" : "Game Paused");
        dialog.setMessage(stringBuilder.toString());
        ((Activity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog.show();
            }
        });
    }
}
