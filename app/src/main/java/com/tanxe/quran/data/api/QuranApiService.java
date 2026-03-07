package com.tanxe.quran.data.api;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface QuranApiService {
    @GET("v1/edition")
    Call<JsonObject> getEditions(@Query("type") String type, @Query("format") String format);

    @GET("v1/quran/{edition}")
    Call<JsonObject> getQuranEdition(@Path("edition") String edition);
}
