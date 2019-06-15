package com.google.cloud.android.speech;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import viettel.cyberspace.speechrecognize.speedtotextcyberspace.MySharePreferenceVoice;
import viettel.cyberspace.speechrecognize.speedtotextcyberspace.UtilsVoice;

public class SettingActivity extends AppCompatActivity implements
        CompoundButton.OnCheckedChangeListener {

    EditText host, port;
    Button apdung, reset;
    CheckBox lientuc;
    RadioButton rb_8, rb_16;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        host = findViewById(R.id.host);
        port = findViewById(R.id.port);

        apdung = findViewById(R.id.apdung);
        reset = findViewById(R.id.reset);
        lientuc = findViewById(R.id.lientuc);

        rb_8 = findViewById(R.id.rb_8);
        rb_16 = findViewById(R.id.rb_16);

        rb_8.setOnCheckedChangeListener(this);
        rb_16.setOnCheckedChangeListener(this);


        apdung.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                String hostS = host.getText().toString();
                String portS = port.getText().toString();
                if (hostS.equals("")) {
                    Toast.makeText(getApplicationContext(), "Host khong duoc bo trong ", Toast.LENGTH_LONG).show();
                    return;
                }
                if (portS.equals("")) {
                    Toast.makeText(getApplicationContext(), "Port khong duoc bo trong ", Toast.LENGTH_LONG).show();
                    return;
                }

                int portI = Integer.parseInt(portS);

                boolean is_16Khz = rb_16.isChecked();
                //  Toast.makeText(getApplicationContext(), "tan so lay mau is_16Khz =  "+is_16Khz, Toast.LENGTH_LONG).show();
                MySharePreferenceVoice.setHostAndPort(getApplicationContext(), hostS, portI, lientuc.isChecked(), true, is_16Khz);
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });


        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MySharePreferenceVoice.setHostAndPort(getApplicationContext(), UtilsVoice.HOST_DEFAULT, UtilsVoice.PORT_DEFAULT, UtilsVoice.LIEN_TUC, UtilsVoice.PARSE_JSON, UtilsVoice.IS_16KHZ);
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });
        init();
    }

    private void init() {
        String hostString = MySharePreferenceVoice.getHost(getApplicationContext());
        int portString = MySharePreferenceVoice.getPort(getApplicationContext());

        boolean is16kHz= MySharePreferenceVoice.is16kHz(getApplicationContext());

        host.setText(hostString);
        port.setText(portString + "");
        lientuc.setChecked(MySharePreferenceVoice.isLienTuc(getApplicationContext()));
        rb_16.setChecked(is16kHz);

    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        // buttonView là biến thể hiện "View" vừa được thao tác
        // isChecked thể hiện giá trị của "View" vừa được thao tá
        // trong ví dụ này View ở đây chính là 2 RadioButton rbNam và rbNu
        if (isChecked) {
            if ((RadioButton) buttonView == rb_8) {
                //   Toast.makeText(this, "Bạn vừa chọn rb_8", Toast.LENGTH_SHORT).show();
                port.setText(UtilsVoice.PORT_8kHz+"");
            }
            if (buttonView == rb_16) {
                port.setText(UtilsVoice.PORT_DEFAULT+"");
                //   Toast.makeText(this, "Bạn vừa chọn rb_16", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
