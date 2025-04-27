package com.example.taxi.data.source

import com.example.taxi.domain.model.BonusResponse
import com.example.taxi.domain.model.CheckResponse
import com.example.taxi.domain.model.IsCompletedModel
import com.example.taxi.domain.model.MainResponse
import com.example.taxi.domain.model.PaymentUrl
import com.example.taxi.domain.model.about.ResponseAbout
import com.example.taxi.domain.model.balance.BalanceData
import com.example.taxi.domain.model.checkAccess.AccessModel
import com.example.taxi.domain.model.finish.FinishCostRequest
import com.example.taxi.domain.model.finish.FinishCostResponse
import com.example.taxi.domain.model.history.*
import com.example.taxi.domain.model.location.LocationRequest
import com.example.taxi.domain.model.message.MessageResponse
import com.example.taxi.domain.model.order.*
import com.example.taxi.domain.model.register.RegisterData
import com.example.taxi.domain.model.register.RegisterRequest
import com.example.taxi.domain.model.register.car_data.*
import com.example.taxi.domain.model.register.confirm_password.ConfirmationRequest
import com.example.taxi.domain.model.register.confirm_password.ResendSmsRequest
import com.example.taxi.domain.model.register.confirm_password.UserData
import com.example.taxi.domain.model.register.person_data.PersonDataRequest
import com.example.taxi.domain.model.selfie.SelfieAllData
import com.example.taxi.domain.model.selfie.StatusModel
import com.example.taxi.domain.model.settings.SettingsData
import com.example.taxi.domain.model.statistics.StatisticsResponse
import com.example.taxi.domain.model.statistics.StatisticsResponseValue
import com.example.taxi.domain.model.tarif.ModeRequest
import com.example.taxi.domain.model.tarif.ModeResponse
import com.example.taxi.domain.model.transfer.DriverNameByIdResponse
import com.example.taxi.domain.model.transfer.HistoryMeta
import com.example.taxi.domain.model.transfer.ResponseTransferHistory
import com.example.taxi.domain.model.transfer.TransferRequest
import io.reactivex.Observable
import okhttp3.MultipartBody
import retrofit2.http.*

interface ApiService {

    @POST("driver/login-via-phone")
    fun register(@Body phone: RegisterRequest): Observable<MainResponse<RegisterData>>

    @POST("driver/confirm-phone")
    fun confirmPhone(
        @Query("id") parentId: Int? = null,
        @Body request: ConfirmationRequest
    ): Observable<MainResponse<UserData<IsCompletedModel>>>

    @POST("driver/resend-sms")
    fun resendSms(@Body request: ResendSmsRequest): Observable<MainResponse<UserData<IsCompletedModel>>>

    @POST("driver/fill-user-account")
    fun fillPersonData(
        @Body request: PersonDataRequest
    ): Observable<MainResponse<UserData<IsCompletedModel>>>

    @GET("open-data/cars")
    fun getCars(): Observable<CarDataResponse>

    @GET("open-data/colors")
    fun getColors(): Observable<CarColorResponse>

    @POST("driver/fill-info")
    fun fillCarInfo(@Body request: CarInfoRequest): Observable<CarInfoResponse>

    @Multipart
    @POST("driver/fill-selfie")
    fun fillSelfie(
        @Part selfie: MultipartBody.Part, @Part licensePhoto: MultipartBody.Part
    ): Observable<MainResponse<SelfieAllData<IsCompletedModel, StatusModel>>>

    @GET("mode/index")
    fun getModes(): Observable<ModeResponse>

    @POST("settings/check-access")
    fun checkAccess(@Body request: AccessModel): Observable<MainResponse<Any>>

    @GET("driver/me")
    fun getDriverAllData(): Observable<MainResponse<SelfieAllData<IsCompletedModel, StatusModel>>>

    @GET("balance/total")
    fun getBalance(): Observable<MainResponse<BalanceData>>

    @GET("service/index")
    fun getService(): Observable<ModeResponse>

    @GET("settings/index")
    fun getSettings(): Observable<MainResponse<List<SettingsData>>>

    @GET("order/index")
    fun getOrders(): Observable<MainResponse<List<OrderData<Address>>>>

    @POST("mode/toggle")
    fun setModes(@Body request: ModeRequest): Observable<ModeResponse>

    @POST("service/toggle")
    fun setService(@Body request: ModeRequest): Observable<ModeResponse>

    @POST("order/accept")
    fun orderAccept(@Query("id") id: Int): Observable<MainResponse<OrderAccept<UserModel,MileageData>>>

    @POST("order/taximeter")
    fun orderWithTaximeter(): Observable<MainResponse<OrderAccept<UserModel,MileageData>>>

    @POST("order/arrived")
    fun arrivedOrder(): Observable<MainResponse<Any>>

    @POST("order/started")
    fun startOrder(): Observable<MainResponse<Any>>

    @POST("order/completed")
    fun completeOrder(@Body request: OrderCompleteRequest): Observable<MainResponse<Any>>

    @POST("order/tugadi")
    fun completeOrderNoNetwork(@Body request: OrderCompleteRequest): Observable<MainResponse<Any>>

    @GET("order/history")
    fun getHistory(
        @Query("page") page: Int,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
        @Query("type") type: Int? = null,
        @Query("status") status: Int? = null
    ): Observable<HistoryDataResponse<Meta>>

    @POST("location/send")
    fun sendLocation(@Body request: LocationRequest): Observable<MainResponse<Any>>

    @GET("balance/get-driver")
    fun getDriverById(@Query("driver_id") driver_id: Int): Observable<MainResponse<DriverNameByIdResponse>>

    @POST("balance/transfer")
    fun transferMoney(@Body request: TransferRequest): Observable<MainResponse<Any>>

    @GET("balance/index")
    fun getTransferHistory(
        @Query("page") page: Int,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
        @Query("type") type: Int? = null
    ): Observable<ResponseTransferHistory<HistoryMeta>>

    @GET("data/about")
    fun getAbout(): Observable<MainResponse<ResponseAbout>>

    @GET("data/faq")
    fun getFAQ(): Observable<MainResponse<ResponseAbout>>

    @GET("order/current")
    fun getOrderCurrent(): Observable<MainResponse<OrderAccept<UserModel,MileageData>>>

    @GET("bonus/transfer")
    fun getTransferBonus(
        @Query("order_id") orderId: Int,
        @Query("money") money: Int
    ): Observable<MainResponse<BonusResponse>>

    @POST("bonus/confirm")
    fun confirmBonusPassword(
        @Query("order_history_id") orderHistoryId: Int,
        @Query("code") code: Int
    ): Observable<MainResponse<Any>>

    @POST("notification/index")
    fun getMessage(): Observable<MessageResponse>

    @GET("driver/get-click-url")
    fun paymentClick(
        @Query("amount") amount: Int
    ): Observable<MainResponse<PaymentUrl>>

    @GET("driver/get-payme-url")
    fun paymentPayme(
        @Query("amount") amount: Int
    ): Observable<MainResponse<PaymentUrl>>


    @GET("driver/get-uzum-url")
    fun paymentUzum(
        @Query("amount") amount: Int
    ): Observable<MainResponse<PaymentUrl>>


    @Multipart
    @POST("photo-control/create")
    fun photoControl(
        @Part img_front: MultipartBody.Part,
        @Part img_back: MultipartBody.Part,
        @Part img_left: MultipartBody.Part,
        @Part img_right: MultipartBody.Part,
        @Part img_front_chair: MultipartBody.Part,
        @Part img_back_chair: MultipartBody.Part,
        @Part img_number: MultipartBody.Part,
        @Part img_license: MultipartBody.Part
    ): Observable<MainResponse<Any>>

    @GET("photo-control/check")
    fun checkPhotoControl(): Observable<MainResponse<CheckResponse>>


    @GET("statistics/index")
    fun getStatisticsData(
        @Query("type_id") type: Int
    ): Observable<MainResponse<List<StatisticsResponse<StatisticsResponseValue>>>>

    @POST("order/finish-cost")
    fun finishCost(@Body request: FinishCostRequest): Observable<MainResponse<FinishCostResponse>>
}