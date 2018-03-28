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

        final Button btn_apply = findViewById(R.id.btn_apply);
        final Button btn_default = findViewById(R.id.btn_default);

        final EditText mainwing = findViewById(R.id.mainwing);
        final EditText verticalwing = findViewById(R.id.verticalwing);
        final EditText horizontalwing = findViewById(R.id.horizontalwing);

        btn_apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btn_default.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainwing.setText("0");
                verticalwing.setText("0");
                horizontalwing.setText("0");
            }
        });
    }
}
