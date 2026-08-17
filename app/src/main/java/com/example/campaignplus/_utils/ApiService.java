package com.example.campaignplus._utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

/**
 * Retrofit API interface for all HTTP calls.
 * This replaces the legacy AsyncHttpClient with modern Retrofit.
 */
public interface ApiService {
    
    @GET("api/{path}")
    Call<ResponseBody> get(@Path("path") String path);
    
    @GET("api/{path}")
    Call<ResponseBody> getWithParams(@Path("path") String path, @QueryMap Map<String, String> params);
    
    @POST("api/{path}")
    Call<ResponseBody> post(@Path("path") String path, @Body RequestBody body);
    
    @PUT("api/{path}")
    Call<ResponseBody> put(@Path("path") String path, @Body RequestBody body);
    
    @DELETE("api/{path}")
    Call<ResponseBody> delete(@Path("path") String path);
}
