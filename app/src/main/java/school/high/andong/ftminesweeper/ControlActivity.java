package school.high.andong.ftminesweeper;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class ControlActivity extends AppCompatActivity {

    float left_oldXvalue;
    float left_oldYvalue;
    float right_oldXvalue;
    float right_oldYvalue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_control);

        final ImageView left_btn = (ImageView) findViewById(R.id.left_btn);
        final ImageView right_btn  = (ImageView) findViewById(R.id.right_btn);

        final Button connect_btn = (Button) findViewById(R.id.btn_connect);
        final Button stop_btn = (Button) findViewById(R.id.btn_stop);

        RelativeLayout left_layout = (RelativeLayout) findViewById(R.id.layout_left);
        RelativeLayout right_layout = (RelativeLayout) findViewById(R.id.layout_right);

        left_btn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN :
                        left_oldXvalue = event.getRawX();
                        left_oldYvalue = event.getRawY();
                    case MotionEvent.ACTION_MOVE :
                        left_btn.setX(0);
                        left_btn.setY(0);
                    case MotionEvent.ACTION_UP :
                        left_btn.setX(event.getRawX()-left_oldXvalue);
                        left_btn.setY(event.getRawY()-left_oldYvalue);
                }
                return true;
            }
        });

        right_btn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View right_view, MotionEvent right_event) {
                switch (right_event.getAction()) {
                    case MotionEvent.ACTION_DOWN :
                        right_oldXvalue = right_event.getRawX();
                        right_oldYvalue = right_event.getRawY();
                    case MotionEvent.ACTION_MOVE :
                        right_btn.setX(right_event.getRawX()-right_oldXvalue);
                        right_btn.setY(right_event.getRawY()-right_oldYvalue);
                        return true;
                    case MotionEvent.ACTION_UP :
                        right_btn.setX(0);
                        right_btn.setY(0);
                }
                return true;
            }
        });
    }

    /*
    float left_oldXvalue;
    float left_oldYvalue;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int width = ((ViewGroup) v.getParent()).getWidth() - v.getWidth();
        int height = ((ViewGroup) v.getParent()).getHeight() - v.getHeight();

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            left_oldXvalue = event.getX();
            left_oldYvalue = event.getY();
            //Log.i("Tag1", "Action Down X" + event.getX() + "," + event.getY());
            Log.i("Tag1", "Action Down rX " + event.getRawX() + "," + event.getRawY());
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            v.setX(event.getRawX() - left_oldXvalue - 50);
            v.setY(event.getRawY() - (left_oldYvalue + v.getHeight() + 38));
            //  Log.i("Tag2", "Action Down " + me.getRawX() + "," + me.getRawY());
        } else if (event.getAction() == MotionEvent.ACTION_UP) {

            if (v.getX() > width && v.getY() > height) {
                v.setX(width);
                v.setY(height);
            } else if (v.getX() < 0 && v.getY() > height) {
                v.setX(0);
                v.setY(height);
            } else if (v.getX() > width && v.getY() < 0) {
                v.setX(width);
                v.setY(0);
            } else if (v.getX() < 0 && v.getY() < 0) {
                v.setX(0);
                v.setY(0);
            } else if (v.getX() < 0 || v.getX() > width) {
                if (v.getX() < 0) {
                    v.setX(0);
                    v.setY(event.getRawY() - left_oldYvalue - v.getHeight());
                } else {
                    v.setX(width);
                    v.setY(event.getRawY() - left_oldYvalue - v.getHeight());
                }
            } else if (v.getY() < 0 || v.getY() > height) {
                if (v.getY() < 0) {
                    v.setX(event.getRawX() - left_oldXvalue);
                    v.setY(0);
                } else {
                    v.setX(event.getRawX() - left_oldXvalue);
                    v.setY(height);
                }
            }
        }
        return true;
    }
    */
}