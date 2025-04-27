package com.example.taxi.ui.home.order

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taxi.R
import com.example.taxi.domain.exception.traceErrorException
import com.example.taxi.domain.model.MainResponse
import com.example.taxi.domain.model.location.LocationRequest
import com.example.taxi.domain.model.order.Address
import com.example.taxi.domain.model.order.MileageData
import com.example.taxi.domain.model.order.OrderAccept
import com.example.taxi.domain.model.order.OrderData
import com.example.taxi.domain.model.order.UserModel
import com.example.taxi.domain.usecase.main.GetMainResponseUseCase
import com.example.taxi.utils.Event
import com.example.taxi.utils.Resource
import com.example.taxi.utils.ResourceState
import com.google.gson.Gson
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.util.concurrent.TimeUnit

class OrderViewModel(private val getMainResponseUseCase: GetMainResponseUseCase) : ViewModel() {

    val compositeDisposable = CompositeDisposable()
    private val ackSubject = PublishSubject.create<Boolean>()

    private var _orderItems = MutableLiveData<OrderData<Address>>()

    public var isNewOrder = MutableLiveData<Boolean>().apply {
        value = false
    }

    public fun clearNewOrder() {
        isNewOrder.value = false
    }

    private var _acceptedOrder =
        MutableLiveData<Event<Resource<MainResponse<OrderAccept<UserModel, MileageData>>>>>()
    val acceptOrder: LiveData<Event<Resource<MainResponse<OrderAccept<UserModel,MileageData>>>>> get() = _acceptedOrder

    private var _ordersCount = MutableLiveData<Int>().apply {
        value = 0
    }

    val ordersCount: LiveData<Int> = _ordersCount

    val orderResponse: LiveData<Resource<MainResponse<List<OrderData<Address>>>>>
        get() = _orderResponse
    private var _serverOrderResponse =
        MutableLiveData<Resource<MainResponse<List<OrderData<Address>>>>>()

    fun addItem(orderItem: OrderData<Address>) {
        _orderItems.postValue(orderItem)
//        _orderItems.value = orderItem
    }


    fun removeItem(orderId: Int) {
        val existingResponse = _serverOrderResponse.value
        if (existingResponse != null && existingResponse.state == ResourceState.SUCCESS) {
            val currentData = existingResponse.data?.data?.toMutableList()
            if (currentData != null) {
                val orderToRemove = currentData.firstOrNull { it.id == orderId }
                if (orderToRemove != null) {
                    currentData.remove(orderToRemove)
                    val newData = existingResponse.data
                    newData.data = currentData
                    _orderResponse.postValue(Resource(ResourceState.SUCCESS, newData))
                }
            }
        }
    }

    private var _orderResponse =
        MediatorLiveData<Resource<MainResponse<List<OrderData<Address>>>>>().apply {
            addSource(_orderItems) { newItem ->
                val existingResponse = _serverOrderResponse.value
                if (existingResponse != null && existingResponse.state == ResourceState.SUCCESS) {
                    val currentData = existingResponse.data?.data?.toMutableList()
                    if (currentData != null) {
                        isNewOrder.value = true
                        currentData.add(0, newItem)
                        val newData = existingResponse.data
                        newData.data = currentData
                        this.value = Resource(ResourceState.SUCCESS, newData)
                    }
                }
            }
            addSource(_serverOrderResponse) { newResponse ->
                this.value = newResponse
                isNewOrder.value = false
            }
        }


    fun waitForAck(): Single<Boolean> {
        return ackSubject.firstOrError().timeout(7, TimeUnit.SECONDS)
    }

    fun sendLocation(request: LocationRequest) {
        compositeDisposable.add(
            getMainResponseUseCase.sendLocation(request = request)
                .timeout(10, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe {}
                .doOnTerminate {}
                .subscribe({
                    ackSubject.onNext(true)
                }, { error ->
                    ackSubject.onNext(false)
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

                })
        )
    }

    fun acceptOrder(id: Int) {
        _acceptedOrder.postValue(Event(Resource(ResourceState.LOADING)))
        compositeDisposable.add(
            getMainResponseUseCase.acceptOrder(id = id)
                .timeout(10, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe {}
                .doOnTerminate {}
                .subscribe({ response ->
                    _acceptedOrder.postValue(Event(Resource(ResourceState.SUCCESS, response)))
                }, { error ->
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
                    _acceptedOrder.postValue(
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

    fun clearOrder() {
        _acceptedOrder.value?.getContentIfNotHandled()
    }

    fun getOrders() {
        _serverOrderResponse.postValue(Resource(ResourceState.LOADING))
        compositeDisposable.add(
            getMainResponseUseCase.getOrders()
                .subscribeOn(Schedulers.io())
                .doOnSubscribe {
                    // Perform any setup tasks before the subscription starts
                }
                .doOnTerminate {
                    // Perform any cleanup tasks after the subscription ends
                }
                .subscribe(
                    { response ->
                        viewModelScope.launch {
                            _ordersCount.postValue(response.data.size)
                            _serverOrderResponse.postValue(
                                Resource(
                                    ResourceState.SUCCESS,
                                    response
                                )
                            )
                        }

                    },
                    { error ->
                        _serverOrderResponse.postValue(
                            Resource(
                                ResourceState.ERROR,
                                message = traceErrorException(error).getErrorMessage()
                            )
                        )
                    }
                )
        )
    }

    fun clearAcceptOrderData() {

    }

    fun clearViewModelData() {
        _acceptedOrder = MutableLiveData()
        _orderResponse = MediatorLiveData()
        _ordersCount = MutableLiveData()
        _ordersCount.value = 0
        _orderItems = MutableLiveData()
    }

    public override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }

    fun updateItem(updateItem: OrderData<Address>) {
        val existingResponse = _orderResponse.value
        if (existingResponse != null && existingResponse.state == ResourceState.SUCCESS) {
            val currentData = existingResponse.data?.data?.toMutableList()
            if (currentData != null) {
                var indexToUpdate = -1
                for (i in 0 until currentData.size) {
                    if (currentData[i].id == updateItem.id) {
                        indexToUpdate = i
                        break
                    }
                }
                if (indexToUpdate != -1) {
                    currentData[indexToUpdate] = updateItem
                    val newData = existingResponse.data
                    newData.data = currentData
                    _orderResponse.postValue(Resource(ResourceState.SUCCESS, newData))
                }
            }
        }
    }
}
