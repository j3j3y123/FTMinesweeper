
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
import android.util.Log;
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
import android.widget.ToggleButton;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import static android.view.KeyEvent.KEYCODE_BACK;

public class ControlActivity extends AppCompatActivity {

    private final int REQUEST_BLUETOOTH_ENABLE = 100;

    private Button connect_btn, stop_btn, setting_btn, exit_btn;
    private ToggleButton land_btn;
    LinearLayout device, all_lay, control_lay, button_lay, auto_lay, seek_lay;
    View left_blank, right_blank;
    RelativeLayout left_lay, right_lay, dash_lay;
    ImageView left_btn, right_btn, dashboard_stick;
    Switch auto_switch;
    SeekBar auto_seekbar, lx_seekbar, ly_seekbar, rx_seekbar, ry_seekbar;
    TextView auto_text;

    ConnectedTask mConnectedTask = null;
    static BluetoothAdapter mBluetoothAdapter;
    private String mConnectedDeviceName = null;
    static boolean isConnectionError = false;
    private static final String TAG = "BluetoothClient";

    String send_blue; //블루투스에 전송될 문자열
    String before_send;
    String receiveMessage;
    String MW;
    String VW;
    String HW;

    float left_oldX_value, left_oldY_value, right_oldX_value, right_oldY_value;

    int ry = 0; //모터의 조종 값
    int ly = 3; //수평 꼬리 날개 서보의 조종 값
    int lx = 3; //주 날개 서보의 조종 값
    int rx = 3; //수직 꼬리 날개 서보의 조종 값
    int ao = 0; //오토파일럿 On, Off 값
    int ad = 70; //오토파일럿 각도 값
    int ld = 0; //이륙 모드 여부
    int s_ry = ry * 18; //실제로 보내질 모터의 조종 값
    int s_ly = ly * 12; //실제로 보내질 수평 꼬리 날개 서보의 조종 값
    int s_lx = lx * 12; //실제로 보내질 주 날개 서보의 조종 값
    int s_rx = rx * 12; //실제로 보내질 수직 꼬리 날개 서보의 조종
    int first = 0;
    int m_w, v_w, h_w;

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

        final Intent intent = new Intent(this.getIntent());
        MW = intent.getStringExtra("Main_Wing");
        VW = intent.getStringExtra("Vertical_Wing");
        HW = intent.getStringExtra("Horizontal_Wing");

        m_w = Integer.parseInt(MW);
        v_w = Integer.parseInt(VW);
        h_w = Integer.parseInt(HW);

        connect_btn = findViewById(R.id.btn_connect);
        land_btn = findViewById(R.id.btn_land);
        stop_btn = findViewById(R.id.btn_stop);
        setting_btn = findViewById(R.id.btn_setting);
        exit_btn = findViewById(R.id.btn_exit);
        auto_switch = findViewById(R.id.switch_auto);
        auto_seekbar = findViewById(R.id.seekbar_auto);
        auto_text = findViewById(R.id.text_auto);
        device = findViewById(R.id.layout_device);
        all_lay = findViewById(R.id.layout_all);
        control_lay = findViewById(R.id.layout_control);
        auto_lay = findViewById(R.id.layout_auto);
        button_lay = findViewById(R.id.layout_button);
        left_lay = findViewById(R.id.control_left);
        right_lay = findViewById(R.id.control_right);
        dash_lay = findViewById(R.id.control_dashboard);
        left_blank = findViewById(R.id.left_blank);
        right_blank = findViewById(R.id.right_blank);
        left_btn = findViewById(R.id.control_left_btn);
        right_btn = findViewById(R.id.control_right_btn);
        dashboard_stick = findViewById(R.id.dashboard_stick);
        lx_seekbar = findViewById(R.id.seekbar_lx);
        ly_seekbar = findViewById(R.id.seekbar_ly);
        rx_seekbar = findViewById(R.id.seekbar_rx);
        ry_seekbar = findViewById(R.id.seekbar_ry);
        seek_lay = findViewById(R.id.layout_seek);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            showErrorDialog("This device is not implement Bluetooth.");
            return;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent intent1 = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent1, REQUEST_BLUETOOTH_ENABLE);
        } else {
            showPairedDevicesListDialog();
        }

        auto_text.setText("00°");

        left_btn.setX(left_lay.getWidth() / 2 - left_btn.getWidth() / 2);
        left_btn.setY(left_lay.getHeight() / 2 - left_btn.getHeight() / 2);
        right_btn.setX(right_lay.getWidth() / 2 - left_btn.getWidth() / 2);
        right_btn.setY(right_lay.getHeight() - right_btn.getHeight());

        connect_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mConnectedTask != null) {
                    mConnectedTask.closeSocket();
                } else {
                    showPairedDevicesListDialog();
                    sendMessage(before_send);
                }
            }
        });

        land_btn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    ld = 1;
                    beforesend();
                } else {
                    ld = 0;
                    beforesend();
                }
            }
        });

        stop_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                right_btn.setY(right_lay.getHeight() - right_btn.getHeight());
                ry_seekbar.setProgress(0);
                ry = ry_seekbar.getProgress();
                beforesend();
            }
        });

        setting_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ry == 0) {
                    Intent intent1 = new Intent(ControlActivity.this, SettingActivity.class);
                    intent1.putExtra("Main_Wing", MW);
                    intent1.putExtra("Vertical_Wing", VW);
                    intent1.putExtra("Horizontal_Wing", HW);
                    startActivity(intent1);
                    finish();
                }
            }
        });

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

        auto_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    ao = 1;
                    auto_text.setTextColor(Color.parseColor("#00a000"));
                    beforesend();
                } else {
                    ao = 0;
                    auto_text.setTextColor(Color.parseColor("#000000"));
                    beforesend();
                }
            }
        });

        auto_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ad = auto_seekbar.getProgress();
                if ((ad - 70) > 0 && (ad - 70) < 10) {
                    auto_text.setText("0" + (ad - 70) + "°");
                } else if ((ad - 70) < 0) {
                    if ((70 - ad) < 10) {
                        auto_text.setText("-0" + (70 - ad) + "°");
                    } else {
                        auto_text.setText("-" + (70 - ad) + "°");
                    }
                } else {
                    auto_text.setText("" + (ad - 70) + "°");
                }
                beforesend();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                ad = auto_seekbar.getProgress();
                if ((ad - 70) > 0 && (ad - 70) < 10) {
                    auto_text.setText("0" + (ad - 70) + "°");
                } else if ((ad - 70) < 0) {
                    if ((70 - ad) < 10) {
                        auto_text.setText("-0" + (70 - ad) + "°");
                    } else {
                        auto_text.setText("-" + (70 - ad) + "°");
                    }
                } else {
                    auto_text.setText("" + (ad - 70) + "°");
                }
                beforesend();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                ad = auto_seekbar.getProgress();
                if ((ad - 70) > 0 && (ad - 70) < 10) {
                    auto_text.setText("0" + (ad - 70) + "°");
                } else if ((ad - 70) < 0) {
                    if ((70 - ad) < 10) {
                        auto_text.setText("-0" + (70 - ad) + "°");
                    } else {
                        auto_text.setText("-" + (70 - ad) + "°");
                    }
                } else {
                    auto_text.setText("" + (ad - 70) + "°");
                }
                beforesend();
            }
        });

        left_btn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        left_oldX_value = event.getRawX();
                        left_oldY_value = event.getRawY();
                    case MotionEvent.ACTION_MOVE:
                        if (event.getRawX() - (device.getWidth() - all_lay.getWidth()) / 2 + left_btn.getWidth() / 2 > left_lay.getWidth() &&
                                event.getRawY() - (all_lay.getY() + auto_lay.getHeight() + left_lay.getY()) > left_lay.getHeight()) {
                            left_btn.setX(left_lay.getWidth() - left_btn.getWidth());
                            lx_seekbar.setProgress(Math.round((left_lay.getWidth() - left_btn.getWidth()) / (left_lay.getWidth() - left_btn.getWidth()) * 6));
                            left_btn.setY(left_lay.getWidth() - left_btn.getWidth());
                            ly_seekbar.setProgress(Math.abs(6 - Math.round((left_lay.getWidth() - left_btn.getWidth()) / (left_lay.getHeight() - left_btn.getHeight()) * 6)));
                            beforesend();
                            return true;
                        } else if (event.getRawX() - (device.getWidth() - all_lay.getWidth()) / 2 - left_btn.getWidth() / 2 < 0 &&
                                event.getRawY() - (all_lay.getY() + auto_lay.getHeight() + left_lay.getY()) > left_lay.getHeight()) {
                            left_btn.setX(0);
                            lx_seekbar.setProgress(0);
                            left_btn.setY(left_lay.getWidth() - left_btn.getWidth());
                            ly_seekbar.setProgress(Math.abs(6 - Math.round((left_lay.getWidth() - left_btn.getWidth()) / (left_lay.getHeight() - left_btn.getHeight()) * 6)));
                            beforesend();
                            return true;
                        } else if (event.getRawX() - (device.getWidth() - all_lay.getWidth()) / 2 + left_btn.getWidth() / 2 > left_lay.getWidth() &&
                                event.getRawY() - (all_lay.getY() + auto_lay.getHeight() + left_lay.getY()) - left_btn.getHeight() < 0) {
                            left_btn.setX(left_lay.getWidth() - left_btn.getWidth());
                            lx_seekbar.setProgress(Math.round((left_lay.getWidth() - left_btn.getWidth()) / (left_lay.getWidth() - left_btn.getWidth()) * 6));
                            left_btn.setY(0);
                            ly_seekbar.setProgress(6);
                            beforesend();
                            return true;
                        } else if (event.getRawX() - (device.getWidth() - all_lay.getWidth()) / 2 - left_btn.getWidth() / 2 < 0 &&
                                event.getRawY() - (all_lay.getY() + auto_lay.getHeight() + left_lay.getY()) - left_btn.getHeight() < 0) {
                            left_btn.setX(0);
                            lx_seekbar.setProgress(0);
                            left_btn.setY(0);
                            ly_seekbar.setProgress(6);
                            beforesend();
                            return true;
                        } else if (event.getRawX() - (device.getWidth() - all_lay.getWidth()) / 2 - left_btn.getWidth() / 2 < 0 ||
                                event.getRawX() - (device.getWidth() - all_lay.getWidth()) / 2 + left_btn.getWidth() / 2 > left_lay.getWidth()) {
                            if (event.getRawX() - (device.getWidth() - all_lay.getWidth()) / 2 - left_btn.getWidth() / 2 < 0) {
                                left_btn.setX(0);
                                lx_seekbar.setProgress(0);
                                left_btn.setY(event.getRawY() - (all_lay.getY() + auto_lay.getHeight() + left_lay.getY()) - left_btn.getHeight());
                                ly_seekbar.setProgress(Math.abs(6 - Math.round((event.getRawY() - (all_lay.getY() + auto_lay.getHeight() + left_lay.getY()) - left_btn.getHeight()) / (left_lay.getHeight() - left_btn.getHeight()) * 6)));
                                beforesend();
                                return true;
                            } else {
                                left_btn.setX(left_lay.getWidth() - left_btn.getWidth());
                                lx_seekbar.setProgress(Math.round((left_lay.getWidth() - left_btn.getWidth()) / (left_lay.getWidth() - left_btn.getWidth()) * 6));
                                left_btn.setY(event.getRawY() - (all_lay.getY() + auto_lay.getHeight() + left_lay.getY()) - left_btn.getHeight());
                                ly_seekbar.setProgress(Math.abs(6 - Math.round((event.getRawY() - (all_lay.getY() + auto_lay.getHeight() + left_lay.getY()) - left_btn.getHeight()) / (left_lay.getHeight() - left_btn.getHeight()) * 6)));
                                beforesend();
                                return true;
                            }
                        } else if (event.getRawY() - (all_lay.getY() + auto_lay.getHeight() + left_lay.getY()) - left_btn.getHeight() < 0 ||
                                event.getRawY() - (all_lay.getY() + auto_lay.getHeight() + left_lay.getY()) > left_lay.getHeight()) {
                            if (event.getRawY() - (all_lay.getY() + auto_lay.getHeight() + left_lay.getY()) - left_btn.getHeight() < 0) {
                                left_btn.setX(event.getRawX() - (device.getWidth() - all_lay.getWidth()) / 2 - left_btn.getWidth() / 2);
                                lx_seekbar.setProgress(Math.round((event.getRawX() - (device.getWidth() - all_lay.getWidth()) / 2 - left_btn.getWidth() / 2) / (left_lay.getWidth() - left_btn.getWidth()) * 6));
                                left_btn.setY(0);
                                ly_seekbar.setProgress(6);
                                beforesend();
                                return true;
                            } else {
                                left_btn.setX(event.getRawX() - (device.getWidth() - all_lay.getWidth()) / 2 - left_btn.getWidth() / 2);
                                lx_seekbar.setProgress(Math.round((event.getRawX() - (device.getWidth() - all_lay.getWidth()) / 2 - left_btn.getWidth() / 2) / (left_lay.getWidth() - left_btn.getWidth()) * 6));
                                left_btn.setY(left_lay.getWidth() - left_btn.getWidth());
                                ly_seekbar.setProgress(Math.abs(6 - Math.round((left_lay.getWidth() - left_btn.getWidth()) / (left_lay.getHeight() - left_btn.getHeight()) * 6)));
                                beforesend();
                                return true;
                            }
                        } else {
                            left_btn.setX(event.getRawX() - (device.getWidth() - all_lay.getWidth()) / 2 - left_btn.getWidth() / 2);
                            lx_seekbar.setProgress(Math.round((event.getRawX() - (device.getWidth() - all_lay.getWidth()) / 2 - left_btn.getWidth() / 2) / (left_lay.getWidth() - left_btn.getWidth()) * 6));
                            left_btn.setY(event.getRawY() - (all_lay.getY() + auto_lay.getHeight() + left_lay.getY()) - left_btn.getHeight());
                            ly_seekbar.setProgress(Math.abs(6 - Math.round((event.getRawY() - (all_lay.getY() + auto_lay.getHeight() + left_lay.getY()) - left_btn.getHeight()) / (left_lay.getHeight() - left_btn.getHeight()) * 6)));
                            beforesend();
                            return true;
                        }
                    case MotionEvent.ACTION_UP:
                        left_btn.setX(left_lay.getWidth() / 2 - left_btn.getWidth() / 2);
                        left_btn.setY(left_lay.getHeight() / 2 - left_btn.getHeight() / 2);
                        lx_seekbar.setProgress(3);
                        ly_seekbar.setProgress(3);
                        beforesend();
                        return true;
                }
                return true;
            }
        });

        right_btn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        right_oldX_value = event.getRawX();
                        right_oldY_value = event.getRawY();
                    case MotionEvent.ACTION_MOVE:
                        if (event.getRawX() - (device.getWidth() - all_lay.getWidth()) / 2 - left_blank.getWidth() - right_blank.getWidth() - dash_lay.getWidth() - left_lay.getWidth() + right_btn.getWidth() / 2 > right_lay.getWidth() &&
                                event.getRawY() - (all_lay.getY() + auto_lay.getHeight() + right_lay.getY()) > right_lay.getHeight()) {
                            right_btn.setX(right_lay.getWidth() - right_btn.getWidth());
                            rx_seekbar.setProgress(Math.round((right_lay.getWidth() - right_btn.getWidth()) / (right_lay.getWidth() - right_btn.getWidth()) * 6));
                            right_btn.setY(right_lay.getWidth() - right_btn.getWidth());
                            ry_seekbar.setProgress(Math.abs(10 - Math.round((right_lay.getWidth() - right_btn.getWidth()) / (right_lay.getHeight() - right_btn.getHeight()) * 10)));
                            beforesend();
                            return true;
                        } else if (event.getRawX() - (device.getWidth() - all_lay.getWidth()) / 2 - left_blank.getWidth() - right_blank.getWidth() - dash_lay.getWidth() - left_lay.getWidth() - right_btn.getWidth() / 2 < 0 &&
                                event.getRawY() - (all_lay.getY() + auto_lay.getHeight() + right_lay.getY()) > right_lay.getHeight()) {
                            right_btn.setX(0);
                            rx_seekbar.setProgress(0);
                            right_btn.setY(right_lay.getWidth() - right_btn.getWidth());
                            ry_seekbar.setProgress(Math.abs(10 - Math.round((right_lay.getWidth() - right_btn.getWidth()) / (right_lay.getHeight() - right_btn.getHeight()) * 10)));
                            beforesend();
                            return true;
                        } else if (event.getRawX() - (device.getWidth() - all_lay.getWidth()) / 2 - left_blank.getWidth() - right_blank.getWidth() - dash_lay.getWidth() - left_lay.getWidth() + right_btn.getWidth() / 2 > right_lay.getWidth() &&
                                event.getRawY() - (all_lay.getY() + auto_lay.getHeight() + right_lay.getY()) - right_btn.getHeight() < 0) {
                            right_btn.setX(right_lay.getWidth() - right_btn.getWidth());
                            rx_seekbar.setProgress(Math.round((right_lay.getWidth() - right_btn.getWidth()) / (right_lay.getWidth() - right_btn.getWidth()) * 6));
                            right_btn.setY(0);
                            ry_seekbar.setProgress(10);
                            beforesend();
                            return true;
                        } else if (event.getRawX() - (device.getWidth() - all_lay.getWidth()) / 2 - left_blank.getWidth() - right_blank.getWidth() - dash_lay.getWidth() - left_lay.getWidth() - right_btn.getWidth() / 2 < 0 &&
                                event.getRawY() - (all_lay.getY() + auto_lay.getHeight() + right_lay.getY()) - right_btn.getHeight() < 0) {
                            right_btn.setX(0);
                            rx_seekbar.setProgress(0);
                            right_btn.setY(0);
                            ry_seekbar.setProgress(10);
                            beforesend();
                            return true;
                        } else if (event.getRawX() - (device.getWidth() - all_lay.getWidth()) / 2 - left_blank.getWidth() - right_blank.getWidth() - dash_lay.getWidth() - left_lay.getWidth() - right_btn.getWidth() / 2 < 0 ||
                                event.getRawX() - (device.getWidth() - all_lay.getWidth()) / 2 - left_blank.getWidth() - right_blank.getWidth() - dash_lay.getWidth() - left_lay.getWidth() + right_btn.getWidth() / 2 > right_lay.getWidth()) {
                            if (event.getRawX() - (device.getWidth() - all_lay.getWidth()) / 2 - left_blank.getWidth() - right_blank.getWidth() - dash_lay.getWidth() - left_lay.getWidth() - right_btn.getWidth() / 2 < 0) {
                                right_btn.setX(0);
                                rx_seekbar.setProgress(0);
                                right_btn.setY(event.getRawY() - (all_lay.getY() + auto_lay.getHeight() + right_lay.getY()) - right_btn.getHeight());
                                ry_seekbar.setProgress(Math.abs(10 - Math.round((event.getRawY() - (all_lay.getY() + auto_lay.getHeight() + right_lay.getY()) - right_btn.getHeight()) / (right_lay.getHeight() - right_btn.getHeight()) * 10)));
                                beforesend();
                                return true;
                            } else {
                                right_btn.setX(right_lay.getWidth() - right_btn.getWidth());
                                rx_seekbar.setProgress(Math.round((right_lay.getWidth() - right_btn.getWidth()) / (right_lay.getWidth() - right_btn.getWidth()) * 6));
                                right_btn.setY(event.getRawY() - (all_lay.getY() + auto_lay.getHeight() + right_lay.getY()) - right_btn.getHeight());
                                ry_seekbar.setProgress(Math.abs(10 - Math.round((event.getRawY() - (all_lay.getY() + auto_lay.getHeight() + right_lay.getY()) - right_btn.getHeight()) / (right_lay.getHeight() - right_btn.getHeight()) * 10)));
                                beforesend();
                                return true;
                            }
                        } else if (event.getRawY() - (all_lay.getY() + auto_lay.getHeight() + right_lay.getY()) - right_btn.getHeight() < 0 ||
                                event.getRawY() - (all_lay.getY() + auto_lay.getHeight() + right_lay.getY()) > right_lay.getHeight()) {
                            if (event.getRawY() - (all_lay.getY() + auto_lay.getHeight() + right_lay.getY()) - right_btn.getHeight() < 0) {
                                right_btn.setX(event.getRawX() - (device.getWidth() - all_lay.getWidth()) / 2 - left_blank.getWidth() - right_blank.getWidth() - dash_lay.getWidth() - left_lay.getWidth() - right_btn.getWidth() / 2);
                                rx_seekbar.setProgress(Math.round((event.getRawX() - (device.getWidth() - all_lay.getWidth()) / 2 - left_blank.getWidth() - right_blank.getWidth() - dash_lay.getWidth() - left_lay.getWidth() - right_btn.getWidth() / 2) / (right_lay.getWidth() - right_btn.getWidth()) * 6));
                                right_btn.setY(0);
                                ry_seekbar.setProgress(10);
                                beforesend();
                                return true;
                            } else {
                                right_btn.setX(event.getRawX() - (device.getWidth() - all_lay.getWidth()) / 2 - left_blank.getWidth() - right_blank.getWidth() - dash_lay.getWidth() - left_lay.getWidth() - right_btn.getWidth() / 2);
                                rx_seekbar.setProgress(Math.round((event.getRawX() - (device.getWidth() - all_lay.getWidth()) / 2 - left_blank.getWidth() - right_blank.getWidth() - dash_lay.getWidth() - left_lay.getWidth() - right_btn.getWidth() / 2) / (right_lay.getWidth() - right_btn.getWidth()) * 6));
                                right_btn.setY(right_lay.getWidth() - right_btn.getWidth());
                                ry_seekbar.setProgress(Math.abs(10 - Math.round((right_lay.getWidth() - right_btn.getWidth()) / (right_lay.getHeight() - right_btn.getHeight()) * 10)));
                                beforesend();
                                return true;
                            }
                        } else {
                            right_btn.setX(event.getRawX() - (device.getWidth() - all_lay.getWidth()) / 2 - left_blank.getWidth() - right_blank.getWidth() - dash_lay.getWidth() - left_lay.getWidth() - right_btn.getWidth() / 2);
                            rx_seekbar.setProgress(Math.round((event.getRawX() - (device.getWidth() - all_lay.getWidth()) / 2 - left_blank.getWidth() - right_blank.getWidth() - dash_lay.getWidth() - left_lay.getWidth() - right_btn.getWidth() / 2) / (right_lay.getWidth() - right_btn.getWidth()) * 6));
                            right_btn.setY(event.getRawY() - (all_lay.getY() + auto_lay.getHeight() + right_lay.getY()) - right_btn.getHeight());
                            ry_seekbar.setProgress(Math.abs(10 - Math.round((event.getRawY() - (all_lay.getY() + auto_lay.getHeight() + right_lay.getY()) - right_btn.getHeight()) / (right_lay.getHeight() - right_btn.getHeight()) * 10)));
                            beforesend();
                            return true;
                        }
                    case MotionEvent.ACTION_UP:
                        right_btn.setX(right_lay.getWidth() / 2 - right_btn.getWidth() / 2);
                        rx_seekbar.setProgress(3);
                        beforesend();
                        return true;
                }
                return true;
            }
        });
    }

    private void beforesend() {
        rx = rx_seekbar.getProgress();
        ry = ry_seekbar.getProgress();
        lx = lx_seekbar.getProgress();
        ly = ly_seekbar.getProgress();
        s_ly = ly * 26 + h_w;
        s_lx = lx * 30 + m_w;
        s_rx = rx * 20 + v_w;
        s_ry = ry * 13;
        before_send = "M" + s_ry + "P" + s_ly + "R" + s_lx + "Y" + s_rx + "A" + ao + "D" + ad + "L" + ld ;
        auto_text.setText(before_send);
        sendMessage(before_send);
        dashboard_stick.setRotation(ry_seekbar.getProgress() * 24 - 120);
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
            if (first == 0) {
                s_ly = 3 * 26 + h_w;
                s_lx = 3 * 30 + m_w;
                s_rx = 3 * 20 + v_w;
                s_ry = 0;
                mConnectedTask.write("M" + s_ry + "P" + s_ly + "R" + s_lx + "Y" + s_rx + "A" + ao + "D" + ad + "L" + ld );
                first = 1;
            } else {
                first = 1;
                mConnectedTask.write(msg);
                send_blue = msg;
                auto_text.setText(send_blue);
            }
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
}