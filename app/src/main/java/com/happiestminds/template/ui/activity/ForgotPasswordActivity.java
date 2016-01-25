package com.happiestminds.template.ui.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.happiestminds.template.R;
import com.happiestminds.template.model.request.LoginData;
import com.happiestminds.template.model.response.ApiResponse;
import com.happiestminds.template.util.UIUtils;

import butterknife.Bind;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import timber.log.Timber;

/**
 * Copyright 2016 (C) Happiest Minds Pvt Ltd..
 *
 * <P> Forgot password functionality
 *
 * <P>Notes:
 * <P>Dependency:
 *
 * @authors Ravindra Kamble (ravindra.kambale@happiestminds.com)
 *
 * @created on: 1-Jan-2016
 */
public class ForgotPasswordActivity extends BootstrapActivity {

    @Bind(R.id.et_forgot_password)
    EditText etForgotPassword;

    @Bind(R.id.btn_forgotPassword)
    Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
    }

    @OnClick(R.id.btn_forgotPassword)
    public void handleButtonClick(){
        LoginData loginData = new LoginData();
        loginData.setUsername(etForgotPassword.getText().toString().trim());
        apiService.retrivePassword(loginData, new Callback<ApiResponse>() {
            @Override
            public void success(ApiResponse apiResponse, Response response) {
                Timber.i("Retrieving username successful");
            }

            @Override
            public void failure(RetrofitError error) {
                Timber.i("Retrieving username failed");
            }
        });
    }

    @OnClick(R.id.btn_login)
    public void showLoginActivity(){
        UIUtils.startActivity(this, LoginActivity.class);
        this.finish();
    }
}
