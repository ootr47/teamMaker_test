package com.example.teammaker;


import com.example.teammaker.Notifications.MyResponse;
import com.example.teammaker.Notifications.Sender;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService
{
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=BOXxbs1YtAMTBCNtbJrf_Qs5hAAmuDdykkrAhbLz3zK3SI7liBhhuGbt-P_1tAGlUyB09CsGOVCozM_5_orkMK0"
    })

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
