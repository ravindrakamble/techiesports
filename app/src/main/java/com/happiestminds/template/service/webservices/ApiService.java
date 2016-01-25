package com.happiestminds.template.service.webservices;

import com.happiestminds.template.model.response.ApiResponse;
import com.happiestminds.template.model.request.LoginData;
import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;

/**
 * Created by ravindra.kambale on 8/28/2015.
 */
public interface ApiService {
    @GET("/test/v1/dashboardData")
    void getDashboardData(Callback<ApiResponse> cb);

    @POST("/test/v1/login")
    void login(@Body LoginData loginData, Callback<ApiResponse> cb);

    @POST("/test/v1/getUsername")
    void retrivePassword(@Body LoginData loginData, Callback<ApiResponse> cb);
}
