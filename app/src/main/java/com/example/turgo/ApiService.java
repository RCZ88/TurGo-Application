package com.example.turgo;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ApiService {
    @GET("/getUserById/{uid}")
    Call<User> getUserById(@Path("uid") String userId);
}
