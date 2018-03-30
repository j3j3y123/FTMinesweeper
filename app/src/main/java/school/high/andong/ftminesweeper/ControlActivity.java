package school.high.andong.ftminesweeper;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.support.design.widget.Snackbar;

public class ControlActivity extends AppCompatActivity {

    String send_blue; //블루투스에 전송될 문자열

    float left_oldXvalue;
    float left_oldYvalue;
    float right_oldXvalue;
    float right_oldYvalue;

    int rx; //BL모터의 조종 값
    int ly; //주 날개 서보의 조종 값
    int lx; //수평 꼬리 날개 서보의 조종 값
    int ry; //수직 꼬리 날개 서보의 조종 값
    int ao; //오토파일럿 On, Off 값
    int ad; //오토파일럿 각도 값

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_control);

        final ImageView left_btn = findViewById(R.id.left_btn);
        final ImageView right_btn  = findViewById(R.id.right_btn);

        final Button connect_btn = findViewById(R.id.btn_connect);
        final Button stop_btn = findViewById(R.id.btn_stop);
        final Button setting_btn = findViewById(R.id.btn_setting);
        final Button exit_btn = findViewById(R.id.btn_exit);

        final SeekBar auto_d = findViewById(R.id.auto_d);

        final Switch auto_s = findViewById(R.id.auto_s);

        final TextView auto_i = findViewById(R.id.auto_i);

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

        setting_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SettingActivity.class), true);

            }
        });

        stop_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),MainActivity.class), true);
            }
        });
        /*
          설정 버튼 눌렀을때 설정으로 이동
         */

        stop_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class), true);
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


        auto_d.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                ao = auto_d.getProgress();
                if (ao < 10) {
                    auto_i.setText("0" + ao + "°");
                } else {
                    auto_i.setText("" + ao + "°");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                ao = auto_d.getProgress();
                if (ao < 10) {
                    auto_i.setText("0" + ao + "°");
                } else {
                    auto_i.setText("" + ao + "°");
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                ao = auto_d.getProgress();
                if (ao < 10) {
                    auto_i.setText("0" + ao + "°");
                } else {
                    auto_i.setText("" + ao + "°");
                }
            }
        });
        /*
          상단 자동 시크바를 움직일 때 옆에 있는 텍스트 값 변경
         */

        left_btn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN :
                        left_oldXvalue = event.getRawX();
                        left_oldYvalue = event.getRawY();
                    case MotionEvent.ACTION_MOVE :
                        if (event.getRawX() - ((device.getWidth() / 2) - (button_layout.getWidth() / 2) - left_layout.getWidth()) + left_btn.getWidth() / 2 > left_layout.getWidth() &&
                                event.getRawY() - ((stick_layout.getHeight() / 2) + auto_layout.getHeight() - (left_layout.getHeight() / 2)) > left_layout.getHeight()){
                            left_btn.setX(left_layout.getWidth() - left_btn.getWidth());
                            left_btn.setY(left_layout.getHeight() - left_btn.getHeight());
                            return true;
                        } else if (event.getRawX() - ((device.getWidth() / 2) - (button_layout.getWidth() / 2) - left_layout.getWidth()) - left_btn.getWidth() / 2 < 0 &&
                                event.getRawY() - ((stick_layout.getHeight() / 2) + auto_layout.getHeight() - (left_layout.getHeight() / 2)) > left_layout.getHeight()){
                            left_btn.setX(0);
                            left_btn.setY(left_layout.getHeight() - left_btn.getHeight());
                            return true;
                        } else if (event.getRawX() - ((device.getWidth() / 2) - (button_layout.getWidth() / 2) - left_layout.getWidth()) + left_btn.getWidth() / 2 > left_layout.getWidth() &&
                                event.getRawY() - ((stick_layout.getHeight() / 2) + auto_layout.getHeight() - (left_layout.getHeight() / 2)) - left_btn.getHeight() < 0){
                            left_btn.setX(left_layout.getWidth() - left_btn.getWidth());
                            left_btn.setY(0);
                            return true;
                        } else if (event.getRawX() - ((device.getWidth() / 2) - (button_layout.getWidth() / 2) - left_layout.getWidth()) - left_btn.getWidth() / 2 < 0 &&
                                event.getRawY() - ((stick_layout.getHeight() / 2) + auto_layout.getHeight() - (left_layout.getHeight() / 2)) - left_btn.getHeight() < 0){
                            left_btn.setX(0);
                            left_btn.setY(0);
                            return true;
                        } else if (event.getRawX() - ((device.getWidth() / 2) - (button_layout.getWidth() / 2) - left_layout.getWidth()) - left_btn.getWidth() / 2 < 0 ||
                                event.getRawX() - ((device.getWidth() / 2) - (button_layout.getWidth() / 2) - left_layout.getWidth()) + left_btn.getWidth() / 2 > left_layout.getWidth()){
                            if (event.getRawX() - ((device.getWidth() / 2) - (button_layout.getWidth() / 2) - left_layout.getWidth()) - left_btn.getWidth() / 2 < 0){
                                left_btn.setX(0);
                                left_btn.setY(event.getRawY() - ((stick_layout.getHeight() / 2) + auto_layout.getHeight() - (left_layout.getHeight() / 2)) - left_btn.getHeight());
                                return true;
                            } else {
                                left_btn.setX(left_layout.getWidth() - left_btn.getWidth());
                                left_btn.setY(event.getRawY() - ((stick_layout.getHeight() / 2) + auto_layout.getHeight() - (left_layout.getHeight() / 2)) - left_btn.getHeight());
                                return true;
                            }
                        } else if (event.getRawY() - ((stick_layout.getHeight() / 2) + auto_layout.getHeight() - (left_layout.getHeight() / 2)) - left_btn.getHeight() < 0 ||
                                event.getRawY() - ((stick_layout.getHeight() / 2) + auto_layout.getHeight() - (left_layout.getHeight() / 2)) > left_layout.getHeight()){
                            if (event.getRawY() - ((stick_layout.getHeight() / 2) + auto_layout.getHeight() - (left_layout.getHeight() / 2)) - left_btn.getHeight() < 0){
                                left_btn.setX(event.getRawX() - ((device.getWidth() / 2) - (button_layout.getWidth() / 2) - left_layout.getWidth()) - left_btn.getWidth() / 2);
                                left_btn.setY(0);
                                return true;
                            } else {
                                left_btn.setX(event.getRawX() - ((device.getWidth() / 2) - (button_layout.getWidth() / 2) - left_layout.getWidth()) - left_btn.getWidth() / 2);
                                left_btn.setY(left_layout.getHeight() - left_btn.getHeight());
                                return true;
                            }
                        } else {
                            left_btn.setX(event.getRawX() - ((device.getWidth() / 2) - (button_layout.getWidth() / 2) - left_layout.getWidth()) - left_btn.getWidth() / 2);
                            left_btn.setY(event.getRawY() - ((stick_layout.getHeight() / 2) + auto_layout.getHeight() - (left_layout.getHeight() / 2)) - left_btn.getHeight());
                            return true;
                        }
                    case MotionEvent.ACTION_UP :
                        left_btn.setX(left_layout.getWidth() / 2 - left_btn.getWidth() / 2);
                        left_btn.setY(left_layout.getHeight() / 2 - left_btn.getHeight() / 2);
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
                    case MotionEvent.ACTION_DOWN :
                        right_oldXvalue = event.getRawX();
                        right_oldYvalue = event.getRawY();
                    case MotionEvent.ACTION_MOVE :
                        if (event.getRawX() - ((device.getWidth() / 2) + (button_layout.getWidth() / 2)) + right_btn.getWidth() / 2 > right_layout.getWidth() &&
                                event.getRawY() - ((stick_layout.getHeight() / 2) + auto_layout.getHeight() - (right_layout.getHeight() / 2)) > right_layout.getHeight()){
                            right_btn.setX(right_layout.getWidth() - right_btn.getWidth());
                            right_btn.setY(right_layout.getHeight() - right_btn.getHeight());
                            return true;
                        } else if (event.getRawX() - ((device.getWidth() / 2) + (button_layout.getWidth() / 2)) - right_btn.getWidth() / 2 < 0 &&
                                event.getRawY() - ((stick_layout.getHeight() / 2) + auto_layout.getHeight() - (right_layout.getHeight() / 2)) > right_layout.getHeight()){
                            right_btn.setX(0);
                            right_btn.setY(right_layout.getHeight() - right_btn.getHeight());
                            return true;
                        } else if (event.getRawX() - ((device.getWidth() / 2) + (button_layout.getWidth() / 2)) + right_btn.getWidth() / 2 > right_layout.getWidth() &&
                                event.getRawY() - ((stick_layout.getHeight() / 2) + auto_layout.getHeight() - (right_layout.getHeight() / 2)) - right_btn.getHeight() < 0){
                            right_btn.setX(right_layout.getWidth() - right_btn.getWidth());
                            right_btn.setY(0);
                            return true;
                        } else if (event.getRawX() - ((device.getWidth() / 2) + (button_layout.getWidth() / 2)) - right_btn.getWidth() / 2 < 0 &&
                                event.getRawY() - ((stick_layout.getHeight() / 2) + auto_layout.getHeight() - (right_layout.getHeight() / 2)) - right_btn.getHeight() < 0){
                            right_btn.setX(0);
                            right_btn.setY(0);
                            return true;
                        } else if (event.getRawX() - ((device.getWidth() / 2) + (button_layout.getWidth() / 2)) - right_btn.getWidth() / 2 < 0 ||
                                event.getRawX() - ((device.getWidth() / 2) + (button_layout.getWidth() / 2)) + right_btn.getWidth() / 2 > right_layout.getWidth()){
                            if (event.getRawX() - ((device.getWidth() / 2) + (button_layout.getWidth() / 2)) - right_btn.getWidth() / 2 < 0){
                                right_btn.setX(0);
                                right_btn.setY(event.getRawY() - ((stick_layout.getHeight() / 2) + auto_layout.getHeight() - (right_layout.getHeight() / 2)) - right_btn.getHeight());
                                return true;
                            } else {
                                right_btn.setX(right_layout.getWidth() - right_btn.getWidth());
                                right_btn.setY(event.getRawY() - ((stick_layout.getHeight() / 2) + auto_layout.getHeight() - (right_layout.getHeight() / 2)) - right_btn.getHeight());
                                return true;
                            }
                        } else if (event.getRawY() - ((stick_layout.getHeight() / 2) + auto_layout.getHeight() - (right_layout.getHeight() / 2)) - right_btn.getHeight() < 0 ||
                                event.getRawY() - ((stick_layout.getHeight() / 2) + auto_layout.getHeight() - (right_layout.getHeight() / 2)) > right_layout.getHeight()){
                            if (event.getRawY() - ((stick_layout.getHeight() / 2) + auto_layout.getHeight() - (right_layout.getHeight() / 2)) - right_btn.getHeight() < 0){
                                right_btn.setX(event.getRawX() - ((device.getWidth() / 2) + (button_layout.getWidth() / 2)) - right_btn.getWidth() / 2);
                                right_btn.setY(0);
                                return true;
                            } else {
                                right_btn.setX(event.getRawX() - ((device.getWidth() / 2) + (button_layout.getWidth() / 2)) - right_btn.getWidth() / 2);
                                right_btn.setY(right_layout.getHeight() - right_btn.getHeight());
                                return true;
                            }
                        } else {
                            right_btn.setX(event.getRawX() - ((device.getWidth() / 2) + (button_layout.getWidth() / 2)) - right_btn.getWidth() / 2);
                            right_btn.setY(event.getRawY() - ((stick_layout.getHeight() / 2) + auto_layout.getHeight() - (right_layout.getHeight() / 2)) - right_btn.getHeight());
                            return true;
                        }
                    case MotionEvent.ACTION_UP :
                        right_btn.setX(right_layout.getWidth() / 2 - right_btn.getWidth() / 2);
                }
                return true;
            }
        });
        /*
          우측 조이스틱 움직임
         */
    }

    public void startActivity(Intent intent, boolean deleteThis) {
        super.startActivity(intent);
    }
    /*
      다른 창을 열었을 때의 이 스크린의 명령
     */

    @Override public boolean onKeyDown(int keyCode, KeyEvent event) { switch (keyCode) { case KeyEvent.KEYCODE_BACK: return true; } return super.onKeyDown(keyCode, event); }

}
//