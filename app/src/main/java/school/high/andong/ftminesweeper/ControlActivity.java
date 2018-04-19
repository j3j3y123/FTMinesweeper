
/*
 * 비행기 조종에 관련하여 모든 작업이 이루어질 액티비티
 */

package school.high.andong.ftminesweeper;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import static android.view.KeyEvent.KEYCODE_BACK;

public class ControlActivity extends AppCompatActivity {

    private final int REQUEST_BLUETOOTH_ENABLE = 100;

    private Button connect_btn;

    ConnectedTask mConnectedTask = null;
    static BluetoothAdapter mBluetoothAdapter;
    private String mConnectedDeviceName = null;
    static boolean isConnectionError = false;
    private static final String TAG = "BluetoothClient";

    String send_blue; //블루투스에 전송될 문자열
    String before_send;
    String receiveMessage;

    float left_oldX_value;
    float left_oldY_value;
    float right_oldX_value;
    float right_oldY_value;

    int ry = 0; //모터의 조종 값
    int ly = 3; //수평 꼬리 날개 서보의 조종 값
    int lx = 3; //주 날개 서보의 조종 값
    int rx = 3; //수직 꼬리 날개 서보의 조종 값
    int ao = 0; //오토파일럿 On, Off 값
    int ad = 70; //오토파일럿 각도 값
    int ld = 0; //이륙 모드 여부
    int s_ry; //실제로 보내질 모터의 조종 값
    int s_ly; //실제로 보내질 수평 꼬리 날개 서보의 조종 값
    int s_lx; //실제로 보내질 주 날개 서보의 조종 값
    int s_rx; //실제로 보내질 수직 꼬리 날개 서보의 조종 값
    int finish;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KEYCODE_BACK:
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    //뒤로가기 키 막기

    @SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_control);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            showErrorDialog("This device is not implement Bluetooth.");
            return;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_BLUETOOTH_ENABLE);
        } else {
            showPairedDevicesListDialog();
        }


        connect_btn = findViewById(R.id.btn_connect);
        final ImageView left_btn = findViewById(R.id.left_btn);
        final ImageView right_btn = findViewById(R.id.right_btn);

        final Button stop_btn = findViewById(R.id.btn_stop);
        final Button setting_btn = findViewById(R.id.btn_setting);
        final Button exit_btn = findViewById(R.id.btn_exit);

        final SeekBar auto_d = findViewById(R.id.auto_d);
        final SeekBar seek_lx = findViewById(R.id.LX);
        final SeekBar seek_ly = findViewById(R.id.LY);
        final SeekBar seek_rx = findViewById(R.id.RX);
        final SeekBar seek_ry = findViewById(R.id.RY);

        final Switch auto_s = findViewById(R.id.auto_s);

        final TextView auto_i = findViewById(R.id.auto_i);
        final TextView asdf = findViewById(R.id.asdf);

        final RelativeLayout left_layout = findViewById(R.id.layout_left);
        final RelativeLayout right_layout = findViewById(R.id.layout_right);

        final LinearLayout device = findViewById(R.id.layout_all);
        final LinearLayout auto_layout = findViewById(R.id.layout_auto);
        final LinearLayout stick_layout = findViewById(R.id.layout_stick);

        final LinearLayout button_layout = findViewById(R.id.layout_button);

        auto_i.setText("00°");

        left_btn.setX(left_layout.getWidth() / 2 - left_btn.getWidth() / 2);
        left_btn.setY(left_layout.getHeight() / 2 - left_btn.getHeight() / 2);
        right_btn.setX(right_layout.getWidth() / 2 - right_btn.getWidth() / 2);
        right_btn.setY(right_layout.getHeight() - right_btn.getHeight());

        connect_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPairedDevicesListDialog();
                sendMessage(before_send);
            }
        });

        //연결 버튼 눌렀을 때의 명령

        setting_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SettingActivity.class), true);

            }
        });

        //설정 버튼 눌렀을때 설정으로 이동

        stop_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                right_btn.setY(right_layout.getHeight() - right_btn.getHeight());
                seek_ry.setProgress(0);
                ry = seek_ry.getProgress();
                s_ry = ry * 18;
                before_send = "<" + s_ry + " " + s_ly + " " + s_lx + " " + s_rx + " " + ao + " " + ad + " " + ld + ">";
                asdf.setText(send_blue);
                sendMessage(before_send);
            }
        });

        //출력정지 버튼을 눌럿을때 RX 0으로

        auto_s.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    ao = 1;
                    auto_i.setTextColor(Color.parseColor("#00a000"));
                    before_send = "<" + s_ry + " " + s_ly + " " + s_lx + " " + s_rx + " " + ao + " " + ad + " " + ld + ">";
                    asdf.setText(send_blue);
                    sendMessage(before_send);
                } else {
                    ao = 0;
                    auto_i.setTextColor(-1979711488);
                    auto_d.setProgress(70);
                    before_send = "<" + s_ry + " " + s_ly + " " + s_lx + " " + s_rx + " " + ao + " " + ad + " " + ld + ">";
                    asdf.setText(send_blue);
                    sendMessage(before_send);
                }
            }
        });

        //자동 주행 온/오프 설정

        exit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, R.string.snackbarmaintext, Snackbar.LENGTH_LONG).setAction(R.string.snackbarok, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                }).show();
            }
        });

        //나가기 버튼 설정

        auto_d.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                ad = auto_d.getProgress();
                if ((ad - 70) > 0 && (ad - 70) < 10) {
                    auto_i.setText("0" + (ad - 70) + "°");
                } else if ((ad - 70) < 0) {
                    if ((70 - ad) < 10) {
                        auto_i.setText("-0" + (70 - ad) + "°");
                    } else {
                        auto_i.setText("-" + (70 - ad) + "°");
                    }
                } else {
                    auto_i.setText("" + (ad - 70) + "°");
                }
                before_send = "<" + s_ry + " " + s_ly + " " + s_lx + " " + s_rx + " " + ao + " " + ad + " " + ld + ">";
                asdf.setText(send_blue);
                sendMessage(before_send);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                ad = auto_d.getProgress();
                if ((ad - 70) > 0 && (ad - 70) < 10) {
                    auto_i.setText("0" + (ad - 70) + "°");
                } else if ((ad - 70) < 0) {
                    if ((70 - ad) < 10) {
                        auto_i.setText("-0" + (70 - ad) + "°");
                    } else {
                        auto_i.setText("-" + (70 - ad) + "°");
                    }
                } else {
                    auto_i.setText("" + (ad - 70) + "°");
                }
                before_send = "<" + s_ry + " " + s_ly + " " + s_lx + " " + s_rx + " " + ao + " " + ad + " " + ld + ">";
                asdf.setText(send_blue);
                sendMessage(before_send);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                ad = auto_d.getProgress();
                if ((ad - 70) > 0 && (ad - 70) < 10) {
                    auto_i.setText("0" + (ad - 70) + "°");
                } else if ((ad - 70) < 0) {
                    if ((70 - ad) < 10) {
                        auto_i.setText("-0" + (70 - ad) + "°");
                    } else {
                        auto_i.setText("-" + (70 - ad) + "°");
                    }
                } else {
                    auto_i.setText("" + (ad - 70) + "°");
                }
                before_send = "<" + s_ry + " " + s_ly + " " + s_lx + " " + s_rx + " " + ao + " " + ad + " " + ld + ">";
                asdf.setText(send_blue);
                sendMessage(before_send);
            }
        });

        /*
          상단 자동 시크바를 움직일 때 옆에 있는 텍스트 값 변경
        */

        left_btn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        left_oldX_value = event.getRawX();
                        left_oldY_value = event.getRawY();
                    case MotionEvent.ACTION_MOVE:
                        if (event.getRawX() - ((device.getWidth() / 2) - (button_layout.getWidth() / 2) - left_layout.getWidth()) + left_btn.getWidth() / 2 > left_layout.getWidth() &&
                                event.getRawY() - ((stick_layout.getHeight() / 2) + auto_layout.getHeight() - (left_layout.getHeight() / 2)) > left_layout.getHeight()) {
                            left_btn.setX(left_layout.getWidth() - left_btn.getWidth());
                            seek_lx.setProgress(Math.round((left_layout.getWidth() - left_btn.getWidth()) / (left_layout.getWidth() - left_btn.getWidth()) * 6));
                            left_btn.setY(left_layout.getHeight() - left_btn.getHeight());
                            seek_ly.setProgress(Math.abs(6 - Math.round((left_layout.getHeight() - left_btn.getHeight()) / (left_layout.getHeight() - left_btn.getHeight()) * 6)));
                            lx = seek_lx.getProgress();
                            ly = seek_ly.getProgress();
                            s_lx = rx * 12;
                            s_ly = ly * 12;
                            before_send = "<" + s_ry + " " + s_ly + " " + s_lx + " " + s_rx + " " + ao + " " + ad + " " + ld + ">";
                            asdf.setText(send_blue);
                            sendMessage(before_send);
                            return true;
                        } else if (event.getRawX() - ((device.getWidth() / 2) - (button_layout.getWidth() / 2) - left_layout.getWidth()) - left_btn.getWidth() / 2 < 0 &&
                                event.getRawY() - ((stick_layout.getHeight() / 2) + auto_layout.getHeight() - (left_layout.getHeight() / 2)) > left_layout.getHeight()) {
                            left_btn.setX(0);
                            seek_lx.setProgress(0);
                            left_btn.setY(left_layout.getHeight() - left_btn.getHeight());
                            seek_ly.setProgress(Math.abs(6 - Math.round((left_layout.getHeight() - left_btn.getHeight()) / (left_layout.getHeight() - left_btn.getHeight()) * 6)));
                            lx = seek_lx.getProgress();
                            ly = seek_ly.getProgress();
                            s_lx = rx * 12;
                            s_ly = ly * 12;
                            before_send = "<" + s_ry + " " + s_ly + " " + s_lx + " " + s_rx + " " + ao + " " + ad + " " + ld + ">";
                            asdf.setText(send_blue);
                            sendMessage(before_send);
                            return true;
                        } else if (event.getRawX() - ((device.getWidth() / 2) - (button_layout.getWidth() / 2) - left_layout.getWidth()) + left_btn.getWidth() / 2 > left_layout.getWidth() &&
                                event.getRawY() - ((stick_layout.getHeight() / 2) + auto_layout.getHeight() - (left_layout.getHeight() / 2)) - left_btn.getHeight() < 0) {
                            left_btn.setX(left_layout.getWidth() - left_btn.getWidth());
                            seek_lx.setProgress(Math.round((left_layout.getWidth() - left_btn.getWidth()) / (left_layout.getWidth() - left_btn.getWidth()) * 6));
                            left_btn.setY(0);
                            seek_ly.setProgress(6);
                            lx = seek_lx.getProgress();
                            ly = seek_ly.getProgress();
                            s_lx = rx * 12;
                            s_ly = ly * 12;
                            before_send = "<" + s_ry + " " + s_ly + " " + s_lx + " " + s_rx + " " + ao + " " + ad + " " + ld + ">";
                            asdf.setText(send_blue);
                            sendMessage(before_send);
                            return true;
                        } else if (event.getRawX() - ((device.getWidth() / 2) - (button_layout.getWidth() / 2) - left_layout.getWidth()) - left_btn.getWidth() / 2 < 0 &&
                                event.getRawY() - ((stick_layout.getHeight() / 2) + auto_layout.getHeight() - (left_layout.getHeight() / 2)) - left_btn.getHeight() < 0) {
                            left_btn.setX(0);
                            seek_lx.setProgress(0);
                            left_btn.setY(0);
                            seek_ly.setProgress(6);
                            lx = seek_lx.getProgress();
                            ly = seek_ly.getProgress();
                            s_lx = rx * 12;
                            s_ly = ly * 12;
                            before_send = "<" + s_ry + " " + s_ly + " " + s_lx + " " + s_rx + " " + ao + " " + ad + " " + ld + ">";
                            asdf.setText(send_blue);
                            sendMessage(before_send);
                            return true;
                        } else if (event.getRawX() - ((device.getWidth() / 2) - (button_layout.getWidth() / 2) - left_layout.getWidth()) - left_btn.getWidth() / 2 < 0 ||
                                event.getRawX() - ((device.getWidth() / 2) - (button_layout.getWidth() / 2) - left_layout.getWidth()) + left_btn.getWidth() / 2 > left_layout.getWidth()) {
                            if (event.getRawX() - ((device.getWidth() / 2) - (button_layout.getWidth() / 2) - left_layout.getWidth()) - left_btn.getWidth() / 2 < 0) {
                                left_btn.setX(0);
                                seek_lx.setProgress(0);
                                left_btn.setY(event.getRawY() - ((stick_layout.getHeight() / 2) + auto_layout.getHeight() - (left_layout.getHeight() / 2)) - left_btn.getHeight());
                                seek_ly.setProgress(Math.abs(6 - Math.round((event.getRawY() - ((stick_layout.getHeight() / 2) + auto_layout.getHeight() - (left_layout.getHeight() / 2)) - left_btn.getHeight()) / (left_layout.getHeight() - left_btn.getHeight()) * 6)));
                                lx = seek_lx.getProgress();
                                ly = seek_ly.getProgress();
                                s_lx = rx * 12;
                                s_ly = ly * 12;
                                before_send = "<" + s_ry + " " + s_ly + " " + s_lx + " " + s_rx + " " + ao + " " + ad + " " + ld + ">";
                                asdf.setText(send_blue);
                                sendMessage(before_send);
                                return true;
                            } else {
                                left_btn.setX(left_layout.getWidth() - left_btn.getWidth());
                                seek_lx.setProgress(Math.round((left_layout.getWidth() - left_btn.getWidth()) / (left_layout.getWidth() - left_btn.getWidth()) * 6));
                                left_btn.setY(event.getRawY() - ((stick_layout.getHeight() / 2) + auto_layout.getHeight() - (left_layout.getHeight() / 2)) - left_btn.getHeight());
                                seek_ly.setProgress(Math.abs(6 - Math.round((event.getRawY() - ((stick_layout.getHeight() / 2) + auto_layout.getHeight() - (left_layout.getHeight() / 2)) - left_btn.getHeight()) / (left_layout.getHeight() - left_btn.getHeight()) * 6)));
                                lx = seek_lx.getProgress();
                                ly = seek_ly.getProgress();
                                s_lx = rx * 12;
                                s_ly = ly * 12;
                                before_send = "<" + s_ry + " " + s_ly + " " + s_lx + " " + s_rx + " " + ao + " " + ad + " " + ld + ">";
                                asdf.setText(send_blue);
                                sendMessage(before_send);
                                return true;
                            }
                        } else if (event.getRawY() - ((stick_layout.getHeight() / 2) + auto_layout.getHeight() - (left_layout.getHeight() / 2)) - left_btn.getHeight() < 0 ||
                                event.getRawY() - ((stick_layout.getHeight() / 2) + auto_layout.getHeight() - (left_layout.getHeight() / 2)) > left_layout.getHeight()) {
                            if (event.getRawY() - ((stick_layout.getHeight() / 2) + auto_layout.getHeight() - (left_layout.getHeight() / 2)) - left_btn.getHeight() < 0) {
                                left_btn.setX(event.getRawX() - ((device.getWidth() / 2) - (button_layout.getWidth() / 2) - left_layout.getWidth()) - left_btn.getWidth() / 2);
                                seek_lx.setProgress(Math.round((event.getRawX() - ((device.getWidth() / 2) - (button_layout.getWidth() / 2) - left_layout.getWidth()) - left_btn.getWidth() / 2) / (left_layout.getWidth() - left_btn.getWidth()) * 6));
                                left_btn.setY(0);
                                seek_ly.setProgress(6);
                                lx = seek_lx.getProgress();
                                ly = seek_ly.getProgress();
                                s_lx = rx * 12;
                                s_ly = ly * 12;
                                before_send = "<" + s_ry + " " + s_ly + " " + s_lx + " " + s_rx + " " + ao + " " + ad + " " + ld + ">";
                                asdf.setText(send_blue);
                                sendMessage(before_send);
                                return true;
                            } else {
                                left_btn.setX(event.getRawX() - ((device.getWidth() / 2) - (button_layout.getWidth() / 2) - left_layout.getWidth()) - left_btn.getWidth() / 2);
                                seek_lx.setProgress(Math.round((event.getRawX() - ((device.getWidth() / 2) - (button_layout.getWidth() / 2) - left_layout.getWidth()) - left_btn.getWidth() / 2) / (left_layout.getWidth() - left_btn.getWidth()) * 6));
                                left_btn.setY(left_layout.getHeight() - left_btn.getHeight());
                                seek_ly.setProgress(Math.abs(6 - Math.round((left_layout.getHeight() - left_btn.getHeight()) / (left_layout.getHeight() - left_btn.getHeight()) * 6)));
                                lx = seek_lx.getProgress();
                                ly = seek_ly.getProgress();
                                s_lx = rx * 12;
                                s_ly = ly * 12;
                                before_send = "<" + s_ry + " " + s_ly + " " + s_lx + " " + s_rx + " " + ao + " " + ad + " " + ld + ">";
                                asdf.setText(send_blue);
                                sendMessage(before_send);
                                return true;
                            }
                        } else {
                            left_btn.setX(event.getRawX() - ((device.getWidth() / 2) - (button_layout.getWidth() / 2) - left_layout.getWidth()) - left_btn.getWidth() / 2);
                            seek_lx.setProgress(Math.round((((event.getRawX() - ((device.getWidth() / 2) - (button_layout.getWidth() / 2) - left_layout.getWidth()) - left_btn.getWidth() / 2)) / (left_layout.getWidth() - left_btn.getWidth())) * 6));
                            left_btn.setY(event.getRawY() - ((stick_layout.getHeight() / 2) + auto_layout.getHeight() - (left_layout.getHeight() / 2)) - left_btn.getHeight());
                            seek_ly.setProgress(Math.abs(6 - Math.round(((event.getRawY() - ((stick_layout.getHeight() / 2) + auto_layout.getHeight() - (left_layout.getHeight() / 2)) - left_btn.getHeight()) / (left_layout.getHeight() - left_btn.getHeight())) * 6)));
                            lx = seek_lx.getProgress();
                            ly = seek_ly.getProgress();
                            s_lx = rx * 12;
                            s_ly = ly * 12;
                            before_send = "<" + s_ry + " " + s_ly + " " + s_lx + " " + s_rx + " " + ao + " " + ad + " " + ld + ">";
                            asdf.setText(send_blue);
                            sendMessage(before_send);
                            return true;
                        }
                    case MotionEvent.ACTION_UP:
                        left_btn.setX(left_layout.getWidth() / 2 - left_btn.getWidth() / 2);
                        left_btn.setY(left_layout.getHeight() / 2 - left_btn.getHeight() / 2);
                        seek_lx.setProgress(3);
                        seek_ly.setProgress(3);
                        lx = seek_lx.getProgress();
                        ly = seek_ly.getProgress();
                        s_lx = rx * 12;
                        s_ly = ly * 12;
                        before_send = "<" + s_ry + " " + s_ly + " " + s_lx + " " + s_rx + " " + ao + " " + ad + " " + ld + ">";
                        asdf.setText(send_blue);
                        sendMessage(before_send);
                }
                return true;
            }
        });
        /*
          좌측 조이스틱 움직임
         */

        right_btn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View right_view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        right_oldX_value = event.getRawX();
                        right_oldY_value = event.getRawY();
                    case MotionEvent.ACTION_MOVE:
                        if (event.getRawX() - ((device.getWidth() / 2) + (button_layout.getWidth() / 2)) + right_btn.getWidth() / 2 > right_layout.getWidth() &&
                                event.getRawY() - ((stick_layout.getHeight() / 2) + auto_layout.getHeight() - (right_layout.getHeight() / 2)) > right_layout.getHeight()) {
                            right_btn.setX(right_layout.getWidth() - right_btn.getWidth());
                            seek_rx.setProgress(Math.round((right_layout.getWidth() - right_btn.getWidth()) / (right_layout.getWidth() - right_btn.getWidth()) * 6));
                            right_btn.setY(right_layout.getHeight() - right_btn.getHeight());
                            seek_ry.setProgress(Math.abs(10 - Math.round((right_layout.getHeight() - right_btn.getHeight()) / (right_layout.getHeight() - right_btn.getHeight()) * 10)));
                            rx = seek_rx.getProgress();
                            ry = seek_ry.getProgress();
                            s_rx = rx * 12;
                            s_ry = ry * 18;
                            before_send = "<" + s_ry + " " + s_ly + " " + s_lx + " " + s_rx + " " + ao + " " + ad + " " + ld + ">";
                            asdf.setText(send_blue);
                            sendMessage(before_send);
                            return true;
                        } else if (event.getRawX() - ((device.getWidth() / 2) + (button_layout.getWidth() / 2)) - right_btn.getWidth() / 2 < 0 &&
                                event.getRawY() - ((stick_layout.getHeight() / 2) + auto_layout.getHeight() - (right_layout.getHeight() / 2)) > right_layout.getHeight()) {
                            right_btn.setX(0);
                            seek_rx.setProgress(0);
                            right_btn.setY(right_layout.getHeight() - right_btn.getHeight());
                            seek_ry.setProgress(Math.abs(10 - Math.round((right_layout.getHeight() - right_btn.getHeight()) / (right_layout.getHeight() - right_btn.getHeight()) * 10)));
                            rx = seek_rx.getProgress();
                            ry = seek_ry.getProgress();
                            s_rx = rx * 12;
                            s_ry = ry * 18;
                            before_send = "<" + s_ry + " " + s_ly + " " + s_lx + " " + s_rx + " " + ao + " " + ad + " " + ld + ">";
                            asdf.setText(send_blue);
                            sendMessage(before_send);
                            return true;
                        } else if (event.getRawX() - ((device.getWidth() / 2) + (button_layout.getWidth() / 2)) + right_btn.getWidth() / 2 > right_layout.getWidth() &&
                                event.getRawY() - ((stick_layout.getHeight() / 2) + auto_layout.getHeight() - (right_layout.getHeight() / 2)) - right_btn.getHeight() < 0) {
                            right_btn.setX(right_layout.getWidth() - right_btn.getWidth());
                            seek_rx.setProgress(Math.round((right_layout.getWidth() - right_btn.getWidth()) / (right_layout.getWidth() - right_btn.getWidth()) * 6));
                            right_btn.setY(0);
                            seek_ry.setProgress(10);
                            rx = seek_rx.getProgress();
                            ry = seek_ry.getProgress();
                            s_rx = rx * 12;
                            s_ry = ry * 18;
                            before_send = "<" + s_ry + " " + s_ly + " " + s_lx + " " + s_rx + " " + ao + " " + ad + " " + ld + ">";
                            asdf.setText(send_blue);
                            sendMessage(before_send);
                            return true;
                        } else if (event.getRawX() - ((device.getWidth() / 2) + (button_layout.getWidth() / 2)) - right_btn.getWidth() / 2 < 0 &&
                                event.getRawY() - ((stick_layout.getHeight() / 2) + auto_layout.getHeight() - (right_layout.getHeight() / 2)) - right_btn.getHeight() < 0) {
                            right_btn.setX(0);
                            seek_rx.setProgress(0);
                            right_btn.setY(0);
                            seek_ry.setProgress(10);
                            rx = seek_rx.getProgress();
                            ry = seek_ry.getProgress();
                            s_rx = rx * 12;
                            s_ry = ry * 18;
                            before_send = "<" + s_ry + " " + s_ly + " " + s_lx + " " + s_rx + " " + ao + " " + ad + " " + ld + ">";
                            asdf.setText(send_blue);
                            sendMessage(before_send);
                            return true;
                        } else if (event.getRawX() - ((device.getWidth() / 2) + (button_layout.getWidth() / 2)) - right_btn.getWidth() / 2 < 0 ||
                                event.getRawX() - ((device.getWidth() / 2) + (button_layout.getWidth() / 2)) + right_btn.getWidth() / 2 > right_layout.getWidth()) {
                            if (event.getRawX() - ((device.getWidth() / 2) + (button_layout.getWidth() / 2)) - right_btn.getWidth() / 2 < 0) {
                                right_btn.setX(0);
                                seek_rx.setProgress(0);
                                right_btn.setY(event.getRawY() - ((stick_layout.getHeight() / 2) + auto_layout.getHeight() - (right_layout.getHeight() / 2)) - right_btn.getHeight());
                                seek_ry.setProgress(Math.abs(10 - Math.round((event.getRawY() - ((stick_layout.getHeight() / 2) + auto_layout.getHeight() - (right_layout.getHeight() / 2)) - right_btn.getHeight()) / (right_layout.getHeight() - right_btn.getHeight()) * 10)));
                                rx = seek_rx.getProgress();
                                ry = seek_ry.getProgress();
                                s_rx = rx * 12;
                                s_ry = ry * 18;
                                before_send = "<" + s_ry + " " + s_ly + " " + s_lx + " " + s_rx + " " + ao + " " + ad + " " + ld + ">";
                                asdf.setText(send_blue);
                                sendMessage(before_send);
                                return true;
                            } else {
                                right_btn.setX(right_layout.getWidth() - right_btn.getWidth());
                                seek_rx.setProgress(Math.round((right_layout.getWidth() - right_btn.getWidth()) / (right_layout.getWidth() - right_btn.getWidth()) * 6));
                                right_btn.setY(event.getRawY() - ((stick_layout.getHeight() / 2) + auto_layout.getHeight() - (right_layout.getHeight() / 2)) - right_btn.getHeight());
                                seek_ry.setProgress(Math.abs(10 - Math.round((event.getRawY() - ((stick_layout.getHeight() / 2) + auto_layout.getHeight() - (right_layout.getHeight() / 2)) - right_btn.getHeight()) / (right_layout.getHeight() - right_btn.getHeight()) * 10)));
                                rx = seek_rx.getProgress();
                                ry = seek_ry.getProgress();
                                s_rx = rx * 12;
                                s_ry = ry * 18;
                                before_send = "<" + s_ry + " " + s_ly + " " + s_lx + " " + s_rx + " " + ao + " " + ad + " " + ld + ">";
                                asdf.setText(send_blue);
                                sendMessage(before_send);
                                return true;
                            }
                        } else if (event.getRawY() - ((stick_layout.getHeight() / 2) + auto_layout.getHeight() - (right_layout.getHeight() / 2)) - right_btn.getHeight() < 0 ||
                                event.getRawY() - ((stick_layout.getHeight() / 2) + auto_layout.getHeight() - (right_layout.getHeight() / 2)) > right_layout.getHeight()) {
                            if (event.getRawY() - ((stick_layout.getHeight() / 2) + auto_layout.getHeight() - (right_layout.getHeight() / 2)) - right_btn.getHeight() < 0) {
                                right_btn.setX(event.getRawX() - ((device.getWidth() / 2) + (button_layout.getWidth() / 2)) - right_btn.getWidth() / 2);
                                seek_rx.setProgress(Math.round((event.getRawX() - ((device.getWidth() / 2) + (button_layout.getWidth() / 2)) - right_btn.getWidth() / 2) / (right_layout.getWidth() - right_btn.getWidth()) * 6));
                                right_btn.setY(0);
                                seek_ry.setProgress(10);
                                rx = seek_rx.getProgress();
                                ry = seek_ry.getProgress();
                                s_rx = rx * 12;
                                s_ry = ry * 18;
                                before_send = "<" + s_ry + " " + s_ly + " " + s_lx + " " + s_rx + " " + ao + " " + ad + " " + ld + ">";
                                asdf.setText(send_blue);
                                sendMessage(before_send);
                                return true;
                            } else {
                                right_btn.setX(event.getRawX() - ((device.getWidth() / 2) + (button_layout.getWidth() / 2)) - right_btn.getWidth() / 2);
                                seek_rx.setProgress(Math.round((event.getRawX() - ((device.getWidth() / 2) + (button_layout.getWidth() / 2)) - right_btn.getWidth() / 2) / (right_layout.getWidth() - right_btn.getWidth()) * 6));
                                right_btn.setY(right_layout.getHeight() - right_btn.getHeight());
                                seek_ry.setProgress(Math.abs(10 - Math.round((right_layout.getHeight() - right_btn.getHeight()) / (right_layout.getHeight() - right_btn.getHeight()) * 10)));
                                rx = seek_rx.getProgress();
                                ry = seek_ry.getProgress();
                                s_rx = rx * 12;
                                s_ry = ry * 18;
                                before_send = "<" + s_ry + " " + s_ly + " " + s_lx + " " + s_rx + " " + ao + " " + ad + " " + ld + ">";
                                asdf.setText(send_blue);
                                sendMessage(before_send);
                                return true;
                            }
                        } else {
                            right_btn.setX(event.getRawX() - ((device.getWidth() / 2) + (button_layout.getWidth() / 2)) - right_btn.getWidth() / 2);
                            seek_rx.setProgress(Math.round((event.getRawX() - ((device.getWidth() / 2) + (button_layout.getWidth() / 2)) - right_btn.getWidth() / 2) / (right_layout.getWidth() - right_btn.getWidth()) * 6));
                            right_btn.setY(event.getRawY() - ((stick_layout.getHeight() / 2) + auto_layout.getHeight() - (right_layout.getHeight() / 2)) - right_btn.getHeight());
                            seek_ry.setProgress(Math.abs(10 - Math.round((event.getRawY() - ((stick_layout.getHeight() / 2) + auto_layout.getHeight() - (right_layout.getHeight() / 2)) - right_btn.getHeight()) / (right_layout.getHeight() - right_btn.getHeight()) * 10)));
                            rx = seek_rx.getProgress();
                            ry = seek_ry.getProgress();
                            s_rx = rx * 12;
                            s_ry = ry * 18;
                            before_send = "<" + s_ry + " " + s_ly + " " + s_lx + " " + s_rx + " " + ao + " " + ad + " " + ld + ">";
                            asdf.setText(send_blue);
                            sendMessage(before_send);
                            return true;
                        }
                    case MotionEvent.ACTION_UP:
                        right_btn.setX(right_layout.getWidth() / 2 - right_btn.getWidth() / 2);
                        seek_rx.setProgress(3);
                        rx = seek_rx.getProgress();
                        s_rx = rx * 12;
                        before_send = "<" + s_ry + " " + s_ly + " " + s_lx + " " + s_rx + " " + ao + " " + ad + " " + ld + ">";
                        asdf.setText(send_blue);
                        sendMessage(before_send);
                }
                return true;
            }
        });
        /*
          우측 조이스틱 움직임
         */
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mConnectedTask != null) {
            mConnectedTask.cancel(true);
        }
    }

    private class ConnectTask extends AsyncTask<Void, Void, Boolean> {
        private BluetoothSocket mBluetoothSocket = null;
        private BluetoothDevice mBluetoothDevice = null;

        ConnectTask(BluetoothDevice bluetoothDevice) {
            mBluetoothDevice = bluetoothDevice;
            mConnectedDeviceName = bluetoothDevice.getName();

            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

            try {
                mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(uuid);
            } catch (IOException e) {

            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            mBluetoothAdapter.cancelDiscovery();
            try {
                mBluetoothSocket.connect();
            } catch (IOException e) {
                try {
                    mBluetoothSocket.close();
                } catch (IOException e2) {

                }
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean isSucess) {
            if (isSucess) {
                connected(mBluetoothSocket);
            } else {
                isConnectionError = true;
                showErrorDialog("Unable to connect device");
            }
        }
    }

    public void connected(BluetoothSocket socket) {
        mConnectedTask = new ConnectedTask(socket);
        mConnectedTask.execute();
    }

    class ConnectedTask extends AsyncTask<Void, String, Boolean> {
        private InputStream mInputStream = null;
        private OutputStream mOutputStream = null;
        private BluetoothSocket mBluetoothSocket = null;

        ConnectedTask(BluetoothSocket socket) {
            mBluetoothSocket = socket;
            try {
                mInputStream = mBluetoothSocket.getInputStream();
                mOutputStream = mBluetoothSocket.getOutputStream();
            } catch (IOException e) {

            }
            connect_btn.setTextColor(Color.parseColor("#00a000"));
            sendMessage(before_send);
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            byte[] readBuffer = new byte[1024];
            int readBufferPosition = 0;

            while (true) {
                if (isCancelled()) return false;

                try {
                    int bytesAvailable = mInputStream.available();

                    if (bytesAvailable > 0) {
                        byte[] packetBytes = new byte[bytesAvailable];

                        mInputStream.read(packetBytes);

                        for (int i = 0; i < bytesAvailable; i++) {

                            byte b = packetBytes[i];
                            if (b == '\n') {
                                byte[] encodedBytes = new byte[readBufferPosition];
                                System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                String recvMessate = new String(encodedBytes, "UTF-8");

                                readBufferPosition = 0;

                                publishProgress(recvMessate);
                            } else {
                                readBuffer[readBufferPosition++] = b;
                            }
                        }
                    }
                } catch (IOException e) {
                    return false;
                }
            }
        }

        @Override
        protected void onProgressUpdate(String... recvMessage) {
            receiveMessage = recvMessage[0];
        }

        @Override
        protected void onPostExecute(Boolean isSuccess) {
            super.onPostExecute(isSuccess);

            if (!isSuccess) {
                closeSocket();
                isConnectionError = true;
                showErrorDialog("Device connection was lost");
            }
        }

        @Override
        protected void onCancelled(Boolean aBollean) {
            super.onCancelled(aBollean);
            closeSocket();
        }

        void closeSocket() {
            try {
                mBluetoothSocket.close();
            } catch (IOException e2) {

            }
        }

        void write(String msg) {
            msg += "\n";

            try {
                mOutputStream.write(msg.getBytes());
                mOutputStream.flush();
            } catch (IOException e) {

            }
        }
    }

    public void showPairedDevicesListDialog() {
        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        final BluetoothDevice[] pairedDevices = devices.toArray(new BluetoothDevice[0]);

        if (pairedDevices.length == 0) {
            showQuitDialog("No devices have been paired.\n" + "You must pair it with another devices.");
            return;
        }

        String[] items;
        items = new String[pairedDevices.length];
        for (int i = 0; i < pairedDevices.length; i++) {
            items[i] = pairedDevices[i].getName();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select device");
        builder.setCancelable(true);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                ConnectTask task = new ConnectTask(pairedDevices[which]);
                task.execute();
            }
        });
        builder.create().show();
    }

    public void showErrorDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Quit");
        builder.setCancelable(false);
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (isConnectionError) {
                    isConnectionError = false;
                    finish();
                }
            }
        });
        builder.create().show();
    }

    public void showQuitDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Quit");
        builder.setCancelable(false);
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        builder.create().show();
    }

    void sendMessage(String msg) {
        if (mConnectedTask != null && !before_send.equals(send_blue)) {
            mConnectedTask.write(msg);
            send_blue = msg;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_BLUETOOTH_ENABLE) {
            if (resultCode == RESULT_OK) {
                showPairedDevicesListDialog();
            }
            if (resultCode == RESULT_CANCELED) {
                showQuitDialog("You need to enable bluetooth");
            }
        }
    }

    public void startActivity(Intent intent, boolean deleteThis) {
        super.startActivity(intent);
        if (deleteThis) {
            finish = 0;
        }
    }
    /*
      다른 창을 열었을 때의 이 스크린의 명령
     */
}