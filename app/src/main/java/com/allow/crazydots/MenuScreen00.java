//package com.allow.crazydots;
//
//import android.app.Activity;
//import android.app.Dialog;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.support.v4.app.DialogFragment;
//import android.support.v7.app.AlertDialog;
//import android.support.v7.app.AppCompatActivity;
//import android.os.Bundle;
//import android.view.KeyEvent;
//import android.view.View;
//import android.widget.EditText;
//import android.widget.RadioButton;
//
//import com.allow.crazydots.p2p.WiFiServiceDiscoveryActivity;
//
//import static android.view.ViewGroup.*;
//
//public class MenuScreen extends AppCompatActivity {
//
//    public static final String PLAY_MODE = "playMode";
//    public static final String NUM_DOT_ROWS = "number of dots";
//    public static final String NUM_DOT_COLS = "number of dots";
//    public static final String NUM_PLAYERS = "number of players";
//
//    private EditText dotRowsEdit;
//    private EditText dotColsEdit;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_menu_screen);
//        dotRowsEdit = (EditText) findViewById(R.id.dotRowsEdit);
//        dotColsEdit = (EditText) findViewById(R.id.dotColsEdit);
//    }
//
//    public void singleButtonClick(View view) {
//        startSingle();
//    }
//
//    public void startSingle(){
//        Intent intent = new Intent(this, CrazyDotsActivity.class);
//        intent.putExtra(PLAY_MODE, CrazyDotsRenderer.PLAY_MODE_SINGLE);
//        try {
//            intent.putExtra(NUM_DOT_ROWS, Integer.parseInt(dotRowsEdit.getText().toString()));
//            intent.putExtra(NUM_DOT_COLS, Integer.parseInt(dotColsEdit.getText().toString()));
//        } catch (NumberFormatException e) {
//            e.printStackTrace();
//        }
//        startActivity(intent);
//    }
//
//    public void multiButtonClick(View view) {
//        DialogFragment dialog = new NumPlayersDialogFragment();
//        dialog.show(getSupportFragmentManager(), "NumPlayersDialogFragment");
//    }
//
//    public void exitButtonClick(View view) {
//        int pid= android.os.Process.myPid();
//        android.os.Process.killProcess(pid);
//    }
//
//    public void startMulti(String numPlayersStr, boolean useP2p){
//        Intent intent;
//        if (useP2p){
//            intent = new Intent(this, WiFiServiceDiscoveryActivity.class);
//        } else {
//            intent = new Intent(this, CrazyDotsActivity.class);
//        }
//        Bundle bundle = new Bundle();
//        bundle.putInt(PLAY_MODE, CrazyDotsRenderer.PLAY_MODE_MULTI);
//        try {
//            bundle.putInt(NUM_DOT_ROWS, Integer.parseInt(dotRowsEdit.getText().toString()));
//        } catch (NumberFormatException e) {
//            e.printStackTrace();
//        }
//        try {
//            bundle.putInt(NUM_DOT_COLS, Integer.parseInt(dotColsEdit.getText().toString()));
//        } catch (NumberFormatException e) {
//            e.printStackTrace();
//        }
//        try {
//            bundle.putInt(NUM_PLAYERS, Integer.parseInt(numPlayersStr));
//        } catch (NumberFormatException e) {
//            e.printStackTrace();
//        }
//        intent.putExtras(bundle);
//        startActivity(intent);
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        dotRowsEdit.setText("");
//        dotColsEdit.setText("");
//    }
//
//    //------------------------------------------
//    public static class NumPlayersDialogFragment extends DialogFragment {
//
//        private MenuScreen menuScreen;
//
//        @Override
//        public void onAttach(Activity activity) {
//            super.onAttach(activity);
//            menuScreen = (MenuScreen) activity;
//        }
//
//        @Override
//        public Dialog onCreateDialog(Bundle savedInstanceState) {
//            final boolean[] useP2p = new boolean[1];
//            View view = menuScreen.getLayoutInflater().inflate(R.layout.multiplayer_dialog_view, null);
//            final EditText numPlayersEditText = (EditText) view.findViewById(R.id.numPlayersEditText);
//            numPlayersEditText.setOnKeyListener(new View.OnKeyListener() {
//                public boolean onKey(View v, int keyCode, KeyEvent event) {
//                    if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
//                        menuScreen.startMulti(numPlayersEditText.getText().toString(), true);
//                        return true;
//                    } else {
//                        return false;
//                    }
//                }
//            });
//            final RadioButton numPlayersRadioButtonYes = (RadioButton) view.findViewById(R.id.numPlayersRadioButtonYes);
//            numPlayersRadioButtonYes.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    useP2p[0] = true;
//                }
//            });
//            final RadioButton numPlayersRadioButtonNo = (RadioButton) view.findViewById(R.id.numPlayersRadioButtonNo);
//            numPlayersRadioButtonNo.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    useP2p[0] = false;
//                }
//            });
//            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//            builder.setTitle("Multi Player")
//                    .setView(view)
//                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            if (numPlayersRadioButtonYes.isChecked()) {
//                                menuScreen.startMulti(numPlayersEditText.getText().toString(), true);
//                            } else {
//                                menuScreen.startMulti(numPlayersEditText.getText().toString(), false);
//                            }
//                            NumPlayersDialogFragment.this.dismiss();
//                        }
//                    })
//            .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    NumPlayersDialogFragment.this.dismiss();
//                }
//            });
//            final AlertDialog dialog = builder.create();
//            numPlayersEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//                @Override
//                public void onFocusChange(View v, boolean hasFocus) {
//                    /*if (hasFocus) {
//                        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
//                    }*/
//                }
//            });
//            return dialog;
//        }
//    }
//}
