package viettel.cyberspace.assitant.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.cloud.android.speech.R;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import viettel.cyberspace.assitant.model.User;
import viettel.cyberspace.assitant.rest.ApiClient;
import viettel.cyberspace.assitant.rest.ApiInterface;
import viettel.cyberspace.assitant.storage.StorageManager;


/**
 * Created by brwsr on 08/05/2018.
 */

public class LoginActivity extends AppCompatActivity {
    //    RelativeLayout layoutSkip;
    Button signin;
    EditText account, password;
    ProgressDialog progressDialog;
    Context context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Intent intent = new Intent(this, ChatBotActivity.class);
        startActivity(intent);
        finish();
    }


    private void Login(String account, String password) {
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);

        HashMap<String, String> map = new HashMap<>();
        map.put("username", account);
        map.put("password", password);

        Call<User> call = apiService.login(map);

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, retrofit2.Response<User> response) {
                int statusCode = response.code();
                if (statusCode == 200) {
                    onLoginSuccess(response.body());
                } else {
                    onLoginFailed(response.message());
                    Log.v("trungbd", statusCode + "");
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                onLoginFailed(t.toString());
                Log.v("trungbd", t.toString());
            }
        });
    }

    private void onLoginSuccess(User user) {
        if (progressDialog != null) progressDialog.dismiss();
        if (user.getUser_type() == null) {
            Toast.makeText(getBaseContext(), "Lỗi kết nối mạng hoặc tài khoản đăng nhập không chính xác, vui lòng kiểm tra lại!", Toast.LENGTH_LONG).show();
            return;
        }
        StorageManager.setStringValue(getApplicationContext(), "account", account.getText().toString());
        StorageManager.saveUser(getApplicationContext(), user);
        Intent intent = new Intent(this, ChatBotActivity.class);
        startActivity(intent);
        finish();
        //dang nhap thanh cong
    }

    public void onLoginFailed(String statusCode) {
        if (progressDialog != null) progressDialog.dismiss();
        Toast.makeText(getBaseContext(), "Lỗi kết nối mạng hoặc tài khoản đăng nhập không chính xác, vui lòng kiểm tra lại!", Toast.LENGTH_LONG).show();
    }
}
