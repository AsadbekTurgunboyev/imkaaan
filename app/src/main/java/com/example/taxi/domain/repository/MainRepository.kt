package com.example.taxi.domain.repository

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
import com.example.taxi.domain.model.history.HistoryDataResponse
import com.example.taxi.domain.model.history.Meta
import com.example.taxi.domain.model.location.LocationRequest
import com.example.taxi.domain.model.message.MessageResponse
import com.example.taxi.domain.model.order.Address
import com.example.taxi.domain.model.order.MileageData
import com.example.taxi.domain.model.order.OrderAccept
import com.example.taxi.domain.model.order.OrderCompleteRequest
import com.example.taxi.domain.model.order.OrderData
import com.example.taxi.domain.model.order.UserModel
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
import retrofit2.http.Query

interface MainRepository {

    fun getModes(): Observable<ModeResponse>

    fun getService(): Observable<ModeResponse>

    fun getDriverAllData(): Observable<MainResponse<SelfieAllData<IsCompletedModel, StatusModel>>>

    fun setModes(request: ModeRequest): Observable<ModeResponse>

    fun setService(request: ModeRequest): Observable<ModeResponse>

    fun getBalance(): Observable<MainResponse<BalanceData>>

    fun getSettings(): Observable<MainResponse<List<SettingsData>>>

    fun getOrders(): Observable<MainResponse<List<OrderData<Address>>>>

    fun orderAccept(id: Int): Observable<MainResponse<OrderAccept<UserModel, MileageData>>>

    fun orderWithTaximeter(): Observable<MainResponse<OrderAccept<UserModel,MileageData>>>

    fun arrivedOrder(): Observable<MainResponse<Any>>

    fun startOrder(): Observable<MainResponse<Any>>

    fun competeOrder(request: OrderCompleteRequest): Observable<MainResponse<Any>>

    fun completeOrderNoNetwork(request: OrderCompleteRequest): Observable<MainResponse<Any>>

    fun sendLocation(request: LocationRequest): Observable<MainResponse<Any>>

    fun checkAccess(request: AccessModel): Observable<MainResponse<Any>>

    fun getDriverById(driver_id: Int): Observable<MainResponse<DriverNameByIdResponse>>

    fun getHistory(
        page: Int,
        from: String?,
        to: String?,
        type: Int?,
        status: Int?
    ): Observable<HistoryDataResponse<Meta>>

    fun transferMoney(request: TransferRequest): Observable<MainResponse<Any>>

    fun getTransferHistory(
        page: Int,
        from: String?,
        to: String?,
        type: Int?
    ): Observable<ResponseTransferHistory<HistoryMeta>>

    fun getAbout(): Observable<MainResponse<ResponseAbout>>
    fun getFAQ(): Observable<MainResponse<ResponseAbout>>
    fun getCurrentOrder(): Observable<MainResponse<OrderAccept<UserModel,MileageData>>>

    fun transferWithBonus(order_id: Int, money: Int): Observable<MainResponse<BonusResponse>>

    fun confirmBonusPassword(orderHistoryId: Int, code: Int): Observable<MainResponse<Any>>

    fun getMessage(): Observable<MessageResponse>

    fun paymentClick(amount: Int): Observable<MainResponse<PaymentUrl>>
    fun paymentPayme(amount: Int): Observable<MainResponse<PaymentUrl>>
    fun paymentUzum(amount: Int): Observable<MainResponse<PaymentUrl>>

    fun photoControl(
        img_front: MultipartBody.Part,
        img_back: MultipartBody.Part,
        img_left: MultipartBody.Part,
        img_right: MultipartBody.Part,
        img_front_chair: MultipartBody.Part,
        img_back_chair: MultipartBody.Part,
        img_number: MultipartBody.Part,
        img_license: MultipartBody.Part
    ): Observable<MainResponse<Any>>

    fun checkPhotoControl(): Observable<MainResponse<CheckResponse>>

    fun getStatisticsData(
        type: Int
    ): Observable<MainResponse<List<StatisticsResponse<StatisticsResponseValue>>>>

    fun getFinishCost(request: FinishCostRequest): Observable<MainResponse<FinishCostResponse>>
}
