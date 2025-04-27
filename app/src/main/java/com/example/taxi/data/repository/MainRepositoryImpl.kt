package com.example.taxi.data.repository

import com.example.taxi.data.source.ApiService
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
import com.example.taxi.domain.repository.MainRepository
import io.reactivex.Observable
import okhttp3.MultipartBody

class MainRepositoryImpl(private val apiService: ApiService) : MainRepository {

    override fun getModes(): Observable<ModeResponse> {
        return apiService.getModes()
    }

    override fun getService(): Observable<ModeResponse> {
        return apiService.getService()
    }

    override fun getDriverAllData(): Observable<MainResponse<SelfieAllData<IsCompletedModel, StatusModel>>> {
        return apiService.getDriverAllData()
    }

    override fun setModes(request: ModeRequest): Observable<ModeResponse> {
        return apiService.setModes(request = request)
    }

    override fun setService(request: ModeRequest): Observable<ModeResponse> {
        return apiService.setService(request = request)
    }

    override fun getBalance(): Observable<MainResponse<BalanceData>> {
        return apiService.getBalance()
    }

    override fun getSettings(): Observable<MainResponse<List<SettingsData>>> {
        return apiService.getSettings()
    }

    override fun getOrders(): Observable<MainResponse<List<OrderData<Address>>>> {
        return apiService.getOrders()
    }

    override fun orderAccept(id: Int): Observable<MainResponse<OrderAccept<UserModel, MileageData>>> {
        return apiService.orderAccept(id = id)
    }

    override fun orderWithTaximeter(): Observable<MainResponse<OrderAccept<UserModel,MileageData>>> {
        return apiService.orderWithTaximeter()
    }

    override fun arrivedOrder(): Observable<MainResponse<Any>> {
        return apiService.arrivedOrder()
    }

    override fun startOrder(): Observable<MainResponse<Any>> {
        return apiService.startOrder()
    }

    override fun competeOrder(request: OrderCompleteRequest): Observable<MainResponse<Any>> {
        return apiService.completeOrder(request = request)
    }

    override fun completeOrderNoNetwork(request: OrderCompleteRequest): Observable<MainResponse<Any>> {
        return apiService.completeOrderNoNetwork(request = request)
    }

    override fun sendLocation(request: LocationRequest): Observable<MainResponse<Any>> {
        return apiService.sendLocation(request = request)
    }

    override fun checkAccess(request: AccessModel): Observable<MainResponse<Any>> {
        return apiService.checkAccess(request = request)
    }

    override fun getDriverById(driver_id: Int): Observable<MainResponse<DriverNameByIdResponse>> {
        return apiService.getDriverById(driver_id = driver_id)
    }

    override fun getHistory(
        page: Int,
        from: String?,
        to: String?,
        type: Int?,
        status: Int?
    ): Observable<HistoryDataResponse<Meta>> {
        return apiService.getHistory(
            page = page,
            from = from,
            to = to,
            type = type,
            status = status
        )
    }

    override fun transferMoney(request: TransferRequest): Observable<MainResponse<Any>> {
        return apiService.transferMoney(request = request)
    }

    override fun getTransferHistory(
        page: Int,
        from: String?,
        to: String?,
        type: Int?
    ): Observable<ResponseTransferHistory<HistoryMeta>> {
        return apiService.getTransferHistory(page = page, from = from, to = to, type = type)
    }

    override fun getAbout(): Observable<MainResponse<ResponseAbout>> {
        return apiService.getAbout()
    }

    override fun getFAQ(): Observable<MainResponse<ResponseAbout>> {
        return apiService.getFAQ()
    }

    override fun getCurrentOrder(): Observable<MainResponse<OrderAccept<UserModel,MileageData>>> {
        return apiService.getOrderCurrent()
    }

    override fun transferWithBonus(
        order_id: Int,
        money: Int
    ): Observable<MainResponse<BonusResponse>> {
        return apiService.getTransferBonus(order_id, money)
    }

    override fun confirmBonusPassword(
        orderHistoryId: Int,
        code: Int
    ): Observable<MainResponse<Any>> {
        return apiService.confirmBonusPassword(orderHistoryId, code)
    }

    override fun getMessage(): Observable<MessageResponse> {
        return apiService.getMessage()
    }

    override fun paymentClick(amount: Int): Observable<MainResponse<PaymentUrl>> {
        return apiService.paymentClick(amount)
    }

    override fun paymentPayme(amount: Int): Observable<MainResponse<PaymentUrl>> {
        return apiService.paymentPayme(amount)
    }

    override fun paymentUzum(amount: Int): Observable<MainResponse<PaymentUrl>> {
        return apiService.paymentUzum(amount)
    }

    override fun photoControl(
        img_front: MultipartBody.Part,
        img_back: MultipartBody.Part,
        img_left: MultipartBody.Part,
        img_right: MultipartBody.Part,
        img_front_chair: MultipartBody.Part,
        img_back_chair: MultipartBody.Part,
        img_number: MultipartBody.Part,
        img_license: MultipartBody.Part
    ): Observable<MainResponse<Any>> {
        return apiService.photoControl(
            img_front,
            img_back,
            img_left,
            img_right,
            img_front_chair,
            img_back_chair,
            img_number,
            img_license
        )
    }

    override fun checkPhotoControl(): Observable<MainResponse<CheckResponse>> {
        return apiService.checkPhotoControl()
    }

    override fun getStatisticsData(type: Int): Observable<MainResponse<List<StatisticsResponse<StatisticsResponseValue>>>> {
        return apiService.getStatisticsData(type)
    }

    override fun getFinishCost(request: FinishCostRequest): Observable<MainResponse<FinishCostResponse>> {
        return apiService.finishCost(request = request)
    }


}