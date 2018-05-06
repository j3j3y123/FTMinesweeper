package school.high.andong.ftminesweeper;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ResourceBundle;

import static android.view.KeyEvent.KEYCODE_BACK;

public class SettingActivity extends AppCompatActivity {

    String MW;
    String VW;
    String HW;

    SharedPreferences pref;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KEYCODE_BACK:
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_setting);

        pref = getSharedPreferences("Setting", MODE_PRIVATE);

        final Button btn_apply = findViewById(R.id.btn_apply);
        final Button btn_default = findViewById(R.id.btn_default);

        final EditText main_wing = findViewById(R.id.mainwing);
        final EditText vertical_wing = findViewById(R.id.verticalwing);
        final EditText horizontal_wing = findViewById(R.id.horizontalwing);

        Intent intent1 = getIntent();

        MW = intent1.getStringExtra("Main_Wing");
        VW = intent1.getStringExtra("Vertical_Wing");
        HW = intent1.getStringExtra("Horizontal_Wing");

        main_wing.setText(MW);
        vertical_wing.setText(VW);
        horizontal_wing.setText(HW);

        btn_apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("Main_Wing", main_wing.getText().toString());
                editor.putString("Vertical_Wing", vertical_wing.getText().toString());
                editor.putString("Horizontal_Wing", horizontal_wing.getText().toString());
                editor.commit();

                Intent intent1 = new Intent(SettingActivity.this, ControlActivity.class);
                intent1.putExtra("Main_Wing", main_wing.getText().toString());
                intent1.putExtra("Vertical_Wing", vertical_wing.getText().toString());
                intent1.putExtra("Horizontal_Wing", horizontal_wing.getText().toString());
                startActivity(intent1);
                finish();
            }
        });

        btn_default.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                main_wing.setText("0");
                vertical_wing.setText("0");
                horizontal_wing.setText("0");
            }
        });
    }
}
