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
        final EditText edittext_mainwing = findViewById(R.id.edittext_mainwing);
        final EditText edittext_horizontalwing = findViewById(R.id.edittext_horizontalwing);
        final EditText edittext_verticalwing = findViewById(R.id.edittext_verticalwing);

        accept_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        default_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edittext_mainwing.setText("0");
                edittext_horizontalwing.setText("0");
                edittext_verticalwing.setText("0");
            }
        });
    }

}
