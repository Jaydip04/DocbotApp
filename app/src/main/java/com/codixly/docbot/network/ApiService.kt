package com.codixly.docbot.network

import com.codixly.docbot.model.LoginRequest
import com.codixly.docbot.model.LoginResponse
import com.codixly.docbot.model.LogoutRequest
import com.codixly.docbot.model.LogoutResponse
import com.codixly.docbot.model.VerifyKeyRequest
import com.codixly.docbot.model.VerifyKeyResponse
import com.codixly.docbot.model.VerifyTokenRequest
import com.codixly.docbot.model.VerifyTokenResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiService {
    @Headers("Content-Type: application/json")
    @POST("login_customer")
    fun loginUser(@Body loginRequest: LoginRequest): Call<LoginResponse>

    @POST("verifyToken")
    fun verifyToken(@Body request: VerifyTokenRequest): Call<VerifyTokenResponse>

    @POST("logout")
    fun logout(@Body request: LogoutRequest): Call<LogoutResponse>

    @POST("get_verify_key")
    fun getVerifyKey(@Body request: VerifyKeyRequest): Call<VerifyKeyResponse>
}
