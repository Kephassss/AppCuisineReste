package com.repasdelaflemme.app.data.remote;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MealDbApi {
    // https://www.themealdb.com/api/json/v1/1/search.php?s=Arrabiata
    @GET("/api/json/v1/1/search.php")
    Call<MealDbResponse> search(@Query("s") String query);
}

