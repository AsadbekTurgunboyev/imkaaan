package com.example.taxi.network

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.taxi.domain.model.MainResponse
import com.example.taxi.domain.model.order.MileageData
import com.example.taxi.domain.model.order.OrderAccept
import com.example.taxi.domain.model.order.UserModel
import com.example.taxi.domain.preference.UserPreferenceManager
import com.example.taxi.domain.usecase.main.GetMainResponseUseCase
import com.example.taxi.utils.Resource
import com.example.taxi.utils.ResourceState
import com.google.gson.Gson
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException

const val IS_CONNECT = "isConnect"
const val NO_CONNECT = "noConnect"

class NetworkViewModel(
    private val getMainResponseUseCase: GetMainResponseUseCase,
    private val userPreferenceManager: UserPreferenceManager
) : ViewModel() {
    private val compositeDisposable = CompositeDisposable()

    private val _response = MutableLiveData<Resource<String>?>()
    val response: LiveData<Resource<String>?> get() = _response
    private val _driverStatus = MutableLiveData<Resource<MainResponse<OrderAccept<UserModel, MileageData>>?>?>()
    val driverStatus: LiveData<Resource<MainResponse<OrderAccept<UserModel,MileageData>>?>?> get() = _driverStatus

    fun resetData() {
        _driverStatus.value = null
    }

    fun clearNetworkData(){
        _response.postValue(null)
    }

    fun getOrderCurrent() {
        _response.postValue(Resource(ResourceState.LOADING))
        _driverStatus.postValue(Resource(ResourceState.LOADING))

        compositeDisposable.add(getMainResponseUseCase.getOrderCurrent()
            .subscribeOn(Schedulers.io())
            .doOnSubscribe {
                // Perform any setup tasks before the subscription starts
            }.doOnTerminate {}.subscribe({ response ->

                userPreferenceManager.savePriceSettings(response.data)
                 userPreferenceManager.saveStartCostUpdate(response.data.start_cost)
                _driverStatus.postValue(Resource(ResourceState.SUCCESS, response))
                _response.postValue(Resource(ResourceState.SUCCESS, IS_CONNECT))
            }, { error ->


                val errorMessage = if (error is HttpException) {
                    try {
                        val errorBody = error.response()?.errorBody()?.string()
                        val mainResponse = Gson().fromJson(errorBody, MainResponse::class.java)
                        if (mainResponse.status == 400 && mainResponse.success.not()) {
                            NO_CONNECT
                        } else {
                            mainResponse.message
                        }
                    } catch (e: Exception) {
                        "An error occurred"
                    }
                } else {
                    "An error occurred"
                }

                _response.postValue(
                    Resource(
                        ResourceState.ERROR,
                        message = errorMessage
                    )
                )
                _driverStatus.postValue(Resource(ResourceState.ERROR, message = errorMessage))

            })
        )
    }

}