package com.codixly.docbot.network

import com.codixly.docbot.model.CustomerDataRequest
import com.codixly.docbot.model.CustomerDataResponse
import com.codixly.docbot.model.DeleteAccountRequest
import com.codixly.docbot.model.DeleteAccountResponse
import com.codixly.docbot.model.FaqResponse
import com.codixly.docbot.model.LoginRequest
import com.codixly.docbot.model.LoginResponse
import com.codixly.docbot.model.LogoutRequest
import com.codixly.docbot.model.LogoutResponse
import com.codixly.docbot.model.PatientRegistrationRequest
import com.codixly.docbot.model.PatientRegistrationResponse
import com.codixly.docbot.model.SendOtpRequest
import com.codixly.docbot.model.SendOtpResponse
import com.codixly.docbot.model.VerifyKeyRequest
import com.codixly.docbot.model.VerifyKeyResponse
import com.codixly.docbot.model.VerifyOtpRequest
import com.codixly.docbot.model.VerifyOtpResponse
import com.codixly.docbot.model.VerifyTokenRequest
import com.codixly.docbot.model.VerifyTokenResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST


interface ApiService {
    @Headers("Content-Type: application/json")
    @POST("login_customer")
    fun loginUser(@Body loginRequest: LoginRequest): Call<LoginResponse>

    @Headers("Content-Type: application/json")
    @POST("verifyToken")
    fun verifyToken(@Body request: VerifyTokenRequest): Call<VerifyTokenResponse>

    @Headers("Content-Type: application/json")
    @POST("logout")
    fun logout(@Body request: LogoutRequest): Call<LogoutResponse>

    @Headers("Content-Type: application/json")
    @POST("get_verify_key")
    fun getVerifyKey(@Body request: VerifyKeyRequest): Call<VerifyKeyResponse>

    @Headers("Content-Type: application/json")
    @POST("delete_account")
    fun deleteAccount(
        @Body request: DeleteAccountRequest
    ): Call<DeleteAccountResponse>

    @POST("customer_data")
    fun getCustomerData(
        @Body request: CustomerDataRequest
    ): Call<CustomerDataResponse>

    @GET("getFaq")
    fun getFaqs(): Call<FaqResponse>

    @POST("send_otp")
    fun sendOtp(@Body request: SendOtpRequest): Call<SendOtpResponse>

    @POST("verify_otp")
    fun verifyOtp(@Body request: VerifyOtpRequest): Call<VerifyOtpResponse>

    @POST("register_paitent")
    fun registerPatient(@Body request: PatientRegistrationRequest): Call<PatientRegistrationResponse>

}
