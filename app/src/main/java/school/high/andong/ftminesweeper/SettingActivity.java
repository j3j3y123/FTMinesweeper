package school.high.andong.ftminesweeper;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class SettingActivity extends AppCompatActivity{

    String s_value = "setting_value";
    String string;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_setting);

        final Button btn_apply = findViewById(R.id.btn_apply);
        final Button btn_default = findViewById(R.id.btn_default);

        final EditText main_wing = findViewById(R.id.mainwing);
        final EditText vertical_wing = findViewById(R.id.verticalwing);
        final EditText horizontal_wing = findViewById(R.id.horizontalwing);

        btn_apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                string = "MW_" + main_wing.getText() + "/VW_" + vertical_wing.getText() + "/HW_" + horizontal_wing.getText();
                FileOutputStream setting_value = null;
                try {
                    setting_value = openFileOutput(s_value, Context.MODE_PRIVATE);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                try {
                    setting_value.write(string.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    setting_value.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                finish();
            }
        });
//
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
