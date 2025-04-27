package com.example.taxi.ui.home.service

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.taxi.domain.exception.traceErrorException
import com.example.taxi.domain.model.tarif.ModeRequest
import com.example.taxi.domain.model.tarif.ModeResponse
import com.example.taxi.domain.usecase.main.GetMainResponseUseCase
import com.example.taxi.utils.Resource
import com.example.taxi.utils.ResourceState
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class ServiceViewModel(private val getMainResponseUseCase: GetMainResponseUseCase) : ViewModel() {
    private val _serviceResponse = MutableLiveData<Resource<ModeResponse>>()
    private val _serviceRequest = MutableLiveData<Resource<ModeResponse>>()
    private val compositeDisposable = CompositeDisposable()
    val serviceResponse: LiveData<Resource<ModeResponse>>
        get() = _serviceResponse

    val serviceRequest: LiveData<Resource<ModeResponse>>
        get() = _serviceRequest


    fun getService() {
        _serviceResponse.value = Resource(ResourceState.LOADING)
        compositeDisposable.add(
            getMainResponseUseCase.getService()
                .subscribeOn(Schedulers.io())
                .doOnSubscribe {
                    // Perform any setup tasks before the subscription starts
                }
                .doOnTerminate {
                    // Perform any cleanup tasks after the subscription ends
                }
                .subscribe(
                    { response ->
                        Log.d("xizmat", "getService: $response")
                        _serviceResponse.postValue(Resource(ResourceState.SUCCESS, response))
                    },
                    { error ->
                        _serviceResponse.postValue(
                            Resource(
                                ResourceState.ERROR,
                                message = traceErrorException(error).getErrorMessage()
                            )
                        )
                    }
                )
        )
    }

    fun setService(request: ModeRequest) {
        _serviceRequest.value = Resource(ResourceState.LOADING)
        compositeDisposable.add(
            getMainResponseUseCase.setService(request = request)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe {
                    // Perform any setup tasks before the subscription starts
                }
                .doOnTerminate {
                    // Perform any cleanup tasks after the subscription ends
                }
                .subscribe(
                    { response ->
                        _serviceRequest.postValue(
                            Resource(
                                ResourceState.SUCCESS,
                                data = response
                            )
                        )
                    },
                    { error ->
                        _serviceRequest.postValue(
                            Resource(
                                ResourceState.ERROR,
                                message = traceErrorException(error).getErrorMessage()
                            )
                        )
                    }
                )
        )
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }
}