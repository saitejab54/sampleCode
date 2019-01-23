package com.example.infovision;

import java.util.List;

import model.RetroPhoto;
import retrofit2.Call;
import retrofit2.http.GET;

public interface GetDataService {

    @GET("/photos")
    Call<List<RetroPhoto>> getAllPhotos();

}
