package school.high.andong.ftminesweeper;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SettingActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_setting);

        final Button accept_btn = findViewById(R.id.btn_accept);
        final Button default_btn = findViewById(R.id.btn_default);
        final EditText mainwing_text = findViewById(R.id.text_mainwing);

        accept_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        default_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainwing_text.setText("0");
            }
        });
    }

}
