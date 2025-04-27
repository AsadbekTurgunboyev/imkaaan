package com.example.taxi.domain.usecase.main

import com.example.taxi.domain.model.CheckResponse
import com.example.taxi.domain.model.MainResponse
import com.example.taxi.domain.model.checkAccess.AccessModel
import com.example.taxi.domain.model.finish.FinishCostRequest
import com.example.taxi.domain.model.order.OrderCompleteRequest
import com.example.taxi.domain.model.tarif.ModeRequest
import com.example.taxi.domain.model.transfer.TransferRequest
import com.example.taxi.domain.repository.MainRepository
import io.reactivex.Observable
import okhttp3.MultipartBody
import retrofit2.http.Query

class GetMainResponseUseCase(private val mainRepository: MainRepository) {

    fun getModes() = mainRepository.getModes()

    fun getDriverData() = mainRepository.getDriverAllData()

    fun setModes(request: ModeRequest) = mainRepository.setModes(request = request)

    fun getService() = mainRepository.getService()

    fun setService(request: ModeRequest) = mainRepository.setService(request = request)

    fun getBalance() = mainRepository.getBalance()

    fun getSettings() = mainRepository.getSettings()

    fun getOrders() = mainRepository.getOrders()

    fun acceptOrder(id: Int) = mainRepository.orderAccept(id = id)

    fun acceptWithTaximeter() = mainRepository.orderWithTaximeter()

    fun arrivedOrder() = mainRepository.arrivedOrder()

    fun startOrder() = mainRepository.startOrder()

    fun checkAccess(request: AccessModel) = mainRepository.checkAccess(request = request)

    fun completeOrder(request: OrderCompleteRequest) =
        mainRepository.competeOrder(request = request)


    fun completeOrderNoNetwork(request: OrderCompleteRequest) =
        mainRepository.completeOrderNoNetwork(request = request)


    fun sendLocation(request: com.example.taxi.domain.model.location.LocationRequest) =
        mainRepository.sendLocation(
            request = request
        )

    fun getHistory(
        page: Int,
        from: String? = null,
        to: String? = null,
        type: Int? = null,
        status: Int? = null
    ) = mainRepository.getHistory(page = page, from = from, to = to, type = type, status = status)

    fun getDriverNameById(driver_id: Int) = mainRepository.getDriverById(driver_id = driver_id)

    fun transferMoney(request: TransferRequest) = mainRepository.transferMoney(request = request)

    fun getTransferHistory(page: Int, from: String? = null, to: String? = null, type: Int? = null) =
        mainRepository.getTransferHistory(page = page, from, to, type)

    fun getAbout() = mainRepository.getAbout()
    fun getFAQ() = mainRepository.getFAQ()

    fun getOrderCurrent() = mainRepository.getCurrentOrder()

    fun confirmBonusPassword(orderHistoryId: Int, code: Int) =
        mainRepository.confirmBonusPassword(orderHistoryId, code)

    fun transferWithBonus(order_id: Int, money: Int) =
        mainRepository.transferWithBonus(order_id = order_id, money = money)

    fun getMessage() = mainRepository.getMessage()

    fun paymentClick(amount: Int) = mainRepository.paymentClick(amount)
    fun paymentPayme(amount: Int) = mainRepository.paymentPayme(amount)
    fun paymentUzum(amount: Int) = mainRepository.paymentUzum(amount)

    fun photoControl(
        img_front: MultipartBody.Part,
        img_back: MultipartBody.Part,
        img_left: MultipartBody.Part,
        img_right: MultipartBody.Part,
        img_front_chair: MultipartBody.Part,
        img_back_chair: MultipartBody.Part,
        img_number: MultipartBody.Part,
        img_license: MultipartBody.Part
    ) = mainRepository.photoControl(
        img_front,
        img_back,
        img_left,
        img_right,
        img_front_chair,
        img_back_chair,
        img_number,
        img_license
    )

    fun checkPhotoControl() = mainRepository.checkPhotoControl()

    fun getStatisticsData(
         type: Int
    ) = mainRepository.getStatisticsData(type)

    fun getFinishCost(request: FinishCostRequest) = mainRepository.getFinishCost(request = request)
}