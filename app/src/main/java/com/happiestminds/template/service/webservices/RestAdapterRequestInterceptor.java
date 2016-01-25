package com.happiestminds.template.service.webservices;

import com.happiestminds.template.util.Constants;

import retrofit.RequestInterceptor;

/**
 * Created by ravindra.kambale on 8/28/2015.
 */
public class RestAdapterRequestInterceptor implements RequestInterceptor {

    public RestAdapterRequestInterceptor() {
    }

    @Override
    public void intercept(RequestFacade request) {

        // Add header to set content type of JSON
        request.addHeader(Constants.Http.CONTENT_TYPE, Constants.Http.CONTENT_TYPE_JSON);
    }
}
