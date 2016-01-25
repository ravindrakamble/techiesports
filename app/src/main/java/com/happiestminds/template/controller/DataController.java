package com.happiestminds.template.controller;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.happiestminds.template.database.DatabaseManager;
import com.happiestminds.template.service.webservices.ApiService;
import com.happiestminds.template.service.webservices.RestAdapterRequestInterceptor;
import com.happiestminds.template.util.Constants;


import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

/**
 * Created by ravindra.kambale on 11/30/2015.
 */
public class DataController {
    private static DataController dcInstance = new DataController();
    private DatabaseManager dbManager;
    private ApiService apiManager;
    public static DataController getInstance() {
        return dcInstance;
    }

    private DataController() {
    }

    public DatabaseManager getDatabaseManager(Context context){
        if(dbManager == null){
            dbManager = DatabaseManager.getInstance(context);
        }

        return dbManager;
    }

    public ApiService getAPIManager(){
        if(apiManager == null){
            RestAdapter adapter = provideRestAdapter(provideRestAdapterRequestInterceptor(), provideGson());
            apiManager = adapter.create(ApiService.class);
        }

        return apiManager;
    }

    Gson provideGson() {
        return new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
    }

    RestAdapterRequestInterceptor provideRestAdapterRequestInterceptor() {
        return new RestAdapterRequestInterceptor();
    }

    RestAdapter provideRestAdapter( RestAdapterRequestInterceptor restRequestInterceptor, Gson gson) {

        return new RestAdapter.Builder()
                .setEndpoint(Constants.Http.URL_BASE)
                .setRequestInterceptor(restRequestInterceptor)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setConverter(new GsonConverter(gson))
                .build();
    }
}
