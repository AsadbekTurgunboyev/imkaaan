package com.example.taxi.ui.login.cardata

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.taxi.domain.exception.traceErrorException
import com.example.taxi.domain.model.register.car_data.CarColorResponse
import com.example.taxi.domain.model.register.car_data.CarDataResponse
import com.example.taxi.domain.model.register.car_data.CarInfoRequest
import com.example.taxi.domain.model.register.car_data.CarInfoResponse
import com.example.taxi.domain.usecase.register.GetRegisterResponseUseCase
import com.example.taxi.utils.Resource
import com.example.taxi.utils.setError
import com.example.taxi.utils.setLoading
import com.example.taxi.utils.setSuccess
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class CarDataViewModel(private val getRegisterResponseUseCase: GetRegisterResponseUseCase): ViewModel() {
    private val compositeDisposable = CompositeDisposable()
    val carDataResponse = MutableLiveData<Resource<CarDataResponse>>()
    val carColorResponse = MutableLiveData<Resource<CarColorResponse>>()
    val carInfo = MutableLiveData<Resource<CarInfoResponse>>()

    fun getCarData(){
        carDataResponse.setLoading()
        compositeDisposable.add(getRegisterResponseUseCase.getCarData()
            .subscribeOn(Schedulers.io())
            .doOnSubscribe{}
            .doOnTerminate{}
            .subscribe({
                carDataResponse.setSuccess(it,null)
            },{
                carDataResponse.setError(traceErrorException(it).getErrorMessage())
            }))

    }

    fun fillCarInfo(request: CarInfoRequest){
        carInfo.setLoading()
        compositeDisposable.add(getRegisterResponseUseCase.fillCarInfo(request)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe{}
            .doOnTerminate{}
            .subscribe({
                carInfo.setSuccess(it,null)
            },{
                carInfo.setError(traceErrorException(it).getErrorMessage())
            }))

    }

    fun getCarColor(){
        carColorResponse.setLoading()
        compositeDisposable.add(getRegisterResponseUseCase.getCarColor()
            .subscribeOn(Schedulers.io())
            .doOnSubscribe{}
            .doOnTerminate{}
            .subscribe({
                carColorResponse.setSuccess(it,null)
            },{
                carColorResponse.setError(traceErrorException(it).getErrorMessage())
            }))


    }



    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }
}