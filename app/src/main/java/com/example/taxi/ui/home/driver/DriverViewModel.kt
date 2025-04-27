package com.example.taxi.ui.home.driver

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.taxi.R
import com.example.taxi.domain.exception.traceErrorException
import com.example.taxi.domain.model.BonusResponse
import com.example.taxi.domain.model.MainResponse
import com.example.taxi.domain.model.finish.FinishCostRequest
import com.example.taxi.domain.model.finish.FinishCostResponse
import com.example.taxi.domain.model.order.MileageData
import com.example.taxi.domain.model.order.OrderAccept
import com.example.taxi.domain.model.order.OrderCompleteRequest
import com.example.taxi.domain.model.order.UserModel
import com.example.taxi.domain.usecase.main.GetMainResponseUseCase
import com.example.taxi.ui.home.DriveAction
import com.example.taxi.utils.Event
import com.example.taxi.utils.Resource
import com.example.taxi.utils.ResourceState
import com.google.gson.Gson
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import java.io.IOException
import java.util.concurrent.TimeUnit

class DriverViewModel(private val mainResponseUseCase: GetMainResponseUseCase) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    val orderStartCompleteLiveData = MutableLiveData<Int>().apply {
        value = DriveAction.ACCEPT
    }

    val orderSCTaximeter = MutableLiveData<Int>()


    private val _transferWithBonus = MutableLiveData<Resource<MainResponse<BonusResponse>>>()
    val transferWithBonus: LiveData<Resource<MainResponse<BonusResponse>>> get() = _transferWithBonus

    private var _acceptWithTaximeter =
        MutableLiveData<Event<Resource<MainResponse<OrderAccept<UserModel, MileageData>>>>>()
    val acceptWithTaximeter: LiveData<Event<Resource<MainResponse<OrderAccept<UserModel,MileageData>>>>> get() = _acceptWithTaximeter

    private var _startOrder = MutableLiveData<Resource<MainResponse<Any>>>()
    val startOrder: LiveData<Resource<MainResponse<Any>>> = _startOrder

    private var _arriveOrder = MutableLiveData<Resource<MainResponse<Any>>>()
    val arriveOrder: LiveData<Resource<MainResponse<Any>>> get() = _arriveOrder

    private var _completeOrder = MutableLiveData<Resource<MainResponse<Any>>>()
    val completeOrder: LiveData<Resource<MainResponse<Any>>> get() = _completeOrder

    private var _completeOrderLostNetwork = MutableLiveData<Resource<MainResponse<Any>>>()
    val completeOrderLostNetwork: LiveData<Resource<MainResponse<Any>>> get() = _completeOrderLostNetwork

    private var _confirmationCode = MutableLiveData<Resource<MainResponse<Any>>>()
    val confirmationCode: LiveData<Resource<MainResponse<Any>>> get() = _confirmationCode

    private var _finishCost = MutableLiveData<Resource<MainResponse<FinishCostResponse>>>()
    val finishCost: LiveData<Resource<MainResponse<FinishCostResponse>>> get() = _finishCost

    fun confirmBonusPassword(orderHistoryId: Int, code: Int) {
        _confirmationCode.postValue(Resource(ResourceState.LOADING))
        compositeDisposable.add(mainResponseUseCase.confirmBonusPassword(orderHistoryId, code)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { }
            .doOnTerminate {}
            .subscribe({ response ->
                _confirmationCode.postValue(Resource(ResourceState.SUCCESS, response))
            }, { error ->
                val errorMessage = if (error is HttpException) {
                    try {
                        val errorBody = error.response()?.errorBody()?.string()
                        val mainResponse = Gson().fromJson(errorBody, MainResponse::class.java)
                        mainResponse.message
                    } catch (e: Exception) {
                        "An error occurred"
                    }
                } else {
                    "An error occurred"
                }
                _confirmationCode.postValue(
                    Resource(
                        ResourceState.ERROR,
                        message = errorMessage
                    )
                )
            })
        )
    }

    fun getFinishCost(request: FinishCostRequest) {
        compositeDisposable.add(mainResponseUseCase.getFinishCost(request = request)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { }
            .doOnTerminate {}
            .subscribe({ response ->
                _finishCost.postValue(Resource(ResourceState.SUCCESS, response))
            },{ error ->
                _finishCost.postValue(
                    Resource(
                        ResourceState.ERROR,
                        message = traceErrorException(error).getErrorMessage()
                    )
                )
            }))
    }


    fun transferWithBonus(order_id: Int, money: Int) {
        _transferWithBonus.postValue(Resource(ResourceState.LOADING))
        compositeDisposable.add(mainResponseUseCase.transferWithBonus(order_id, money)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { }
            .doOnTerminate {}
            .subscribe({ response ->
                _transferWithBonus.postValue(Resource(ResourceState.SUCCESS, response))
            }, { error ->
                val errorMessage = if (error is HttpException) {
                    try {
                        val errorBody = error.response()?.errorBody()?.string()
                        val mainResponse = Gson().fromJson(errorBody, MainResponse::class.java)
                        mainResponse.message
                    } catch (e: Exception) {
                        "An error occurred"
                    }
                } else {
                    "An error occurred"
                }
                _transferWithBonus.postValue(
                    Resource(
                        ResourceState.ERROR,
                        message = errorMessage
                    )
                )
            })
        )

    }

    fun arriveOrder() {
        _arriveOrder.postValue(Resource(ResourceState.LOADING))
        compositeDisposable.add(mainResponseUseCase.arrivedOrder().subscribeOn(Schedulers.io())
            .doOnSubscribe {
                // Perform any setup tasks before the subscription starts
            }.doOnTerminate {}.subscribe({ response ->
                _arriveOrder.postValue(Resource(ResourceState.SUCCESS, response))


            }, { error ->
                _arriveOrder.postValue(
                    Resource(
                        ResourceState.ERROR,
                        message = traceErrorException(error).getErrorMessage()
                    )
                )

            })
        )
    }

    fun startOrder() {
        _startOrder.postValue(Resource(ResourceState.LOADING))
        compositeDisposable.add(mainResponseUseCase.startOrder().subscribeOn(Schedulers.io())
            .doOnSubscribe {
                // Perform any setup tasks before the subscription starts
            }.doOnTerminate {}.subscribe({ response ->
                _startOrder.postValue(Resource(ResourceState.SUCCESS, response))
            }, { error ->
                _startOrder.postValue(
                    Resource(
                        ResourceState.ERROR,
                        message = traceErrorException(error).getErrorMessage()
                    )
                )

            })
        )
    }

    fun completeOrderLostNetwork(orderCompleteRequest: OrderCompleteRequest) {
        _completeOrderLostNetwork.postValue(Resource(ResourceState.LOADING))
        compositeDisposable.add(mainResponseUseCase.completeOrderNoNetwork(orderCompleteRequest)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe {
                // Perform any setup tasks before the subscription starts
            }.doOnTerminate {}.subscribe({ response ->
                _completeOrderLostNetwork.postValue(Resource(ResourceState.SUCCESS, response))

            }, { error ->
                val errorMessage = if (error is HttpException) {
                    try {
                        val errorBody = error.response()?.errorBody()?.string()
                        val mainResponse = Gson().fromJson(errorBody, MainResponse::class.java)
                        mainResponse.status
                    } catch (e: Exception) {
                        "An error occurred"
                    }
                } else {
                    "An error occurred"
                }
                _completeOrderLostNetwork.postValue(
                    Resource(
                        ResourceState.ERROR,
                        message = errorMessage.toString()
                    )
                )

            })
        )
    }


    fun acceptWithTaximeter() {
        _acceptWithTaximeter.postValue(Event(Resource(ResourceState.LOADING)))
        compositeDisposable.add(
            mainResponseUseCase.acceptWithTaximeter()
                .timeout(15, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe {}
                .doOnTerminate {}
                .subscribe({ response ->
                    Log.d("zakaz", "acceptWithTaximeter:re  $response")
                    _acceptWithTaximeter.postValue(Event(Resource(ResourceState.SUCCESS, response)))
                }, { error ->
                    Log.d("zakaz", "acceptWithTaximeter:e  $error")

                    val errorMessage = when (error) {
                        is HttpException -> {
                            try {
                                val errorBody = error.response()?.errorBody()?.string()
                                val mainResponse =
                                    Gson().fromJson(errorBody, MainResponse::class.java)
                                mainResponse.message
                            } catch (e: Exception) {
                                R.string.cannot_connect_to_server
                            }
                        }

                        is IOException -> R.string.no_internet
                        else -> R.string.unknow_error
                    }
                    _acceptWithTaximeter.postValue(
                        Event(
                            Resource(
                                ResourceState.ERROR,
                                message = errorMessage.toString()
                            )
                        )
                    )

                })
        )

    }

    fun completeOrder(orderCompleteRequest: OrderCompleteRequest) {
        _completeOrder.postValue(Resource(ResourceState.LOADING))
        compositeDisposable.add(mainResponseUseCase.completeOrder(orderCompleteRequest)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe {
                // Perform any setup tasks before the subscription starts
            }.doOnTerminate {}.subscribe({ response ->
                _completeOrder.postValue(Resource(ResourceState.SUCCESS, response))

            }, { error ->
                _completeOrder.postValue(
                    Resource(
                        ResourceState.ERROR,
                        message = traceErrorException(error).getErrorMessage()
                    )
                )

            })
        )
    }


    fun completeOrderForBonus(orderCompleteRequest: OrderCompleteRequest) {
        compositeDisposable.add(mainResponseUseCase.completeOrder(orderCompleteRequest)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe {
                // Perform any setup tasks before the subscription starts
            }.doOnTerminate {}.subscribe({ response ->
                Log.d("driverxatolik", "completeOrderForBonus: ")
//                _completeOrder.postValue(Resource(ResourceState.SUCCESS, response))

            }, { error ->
                Log.d("driverxatolik", "completeOrderForBonus:x $error")

//                _completeOrder.postValue(
//                    Resource(
//                        ResourceState.ERROR,
//                        message = traceErrorException(error).getErrorMessage()
//                    )
//                )

            })
        )
    }

    fun acceptedOrder() {
        orderStartCompleteLiveData.postValue(DriveAction.ACCEPT)
    }

    fun arrivedOrder() {
        orderStartCompleteLiveData.postValue(DriveAction.ARRIVED)
    }

    fun startedOrder() {
        orderStartCompleteLiveData.postValue(DriveAction.STARTED)
    }

    fun completedOrder() {
        orderStartCompleteLiveData.postValue(DriveAction.COMPLETED)
    }

    fun acceptTaximeter() {
        orderSCTaximeter.postValue(DriveAction.TAX_STARTED)
    }

    fun completeTaximeter() {
        orderSCTaximeter.postValue(DriveAction.TAX_COMPLETED)
    }


    fun clearAllData() {
        _arriveOrder = MutableLiveData()
        _startOrder = MutableLiveData()
        _completeOrder = MutableLiveData()
        orderStartCompleteLiveData.value = DriveAction.ACCEPT
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }
}