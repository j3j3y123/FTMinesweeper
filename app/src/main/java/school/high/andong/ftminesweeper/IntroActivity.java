package school.high.andong.ftminesweeper;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by j3j3y on 2018-03-01.
 */

public class IntroActivity extends AppCompatActivity{

    String MW;
    String VW;
    String HW;
    SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        MW = pref.getString("Main_Wing", "0");
        VW = pref.getString("Vertical_Wing","0");
        HW = pref.getString("Horizontal_Wing", "0");

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(IntroActivity.this, ControlActivity.class);
                intent.putExtra("Main_Wing", MW);
                intent.putExtra("Vertical_Wing", VW);
                intent.putExtra("Horizontal_Wing", HW);
                startActivity(intent);
                finish();
            }
        }, 2000);
    }
}
