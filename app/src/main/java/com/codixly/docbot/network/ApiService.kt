package com.codixly.docbot.network

import com.codixly.docbot.model.LoginRequest
import com.codixly.docbot.model.LoginResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiService {
    @Headers("Content-Type: application/json")
    @POST("login_customer")
    fun loginUser(@Body loginRequest: LoginRequest): Call<LoginResponse>
}
