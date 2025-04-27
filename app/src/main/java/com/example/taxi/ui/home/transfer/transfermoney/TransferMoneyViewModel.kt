package com.example.taxi.ui.home.transfer.transfermoney

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.taxi.domain.model.MainResponse
import com.example.taxi.domain.model.transfer.DriverNameByIdResponse
import com.example.taxi.domain.model.transfer.TransferRequest
import com.example.taxi.domain.usecase.main.GetMainResponseUseCase
import com.example.taxi.utils.Resource
import com.example.taxi.utils.ResourceState
import com.example.taxi.utils.getMainErrorMessage
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class TransferMoneyViewModel(private val getMainResponseUseCase: GetMainResponseUseCase) :
    ViewModel() {
    private val compositeDisposable = CompositeDisposable()

    private val _driverName = MutableLiveData<Resource<MainResponse<DriverNameByIdResponse>>>()
    val driverName: LiveData<Resource<MainResponse<DriverNameByIdResponse>>> get() = _driverName

    private val _transferMoney = MutableLiveData<Resource<MainResponse<Any>>>()
    val transferMoney: LiveData<Resource<MainResponse<Any>>> get() = _transferMoney


    fun getDriverNameById(driver_id: Int) {
        _driverName.value = Resource(ResourceState.LOADING)
        compositeDisposable.add(
            getMainResponseUseCase.getDriverNameById(driver_id = driver_id)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe {
                    // Perform any setup tasks before the subscription starts
                }
                .doOnTerminate {
                    // Perform any cleanup tasks after the subscription ends
                }
                .subscribe(
                    { response ->
                        _driverName.postValue(
                            Resource(
                                ResourceState.SUCCESS,
                                data = response
                            )
                        )
                    },
                    { error ->
                        _driverName.postValue(
                            Resource(
                                ResourceState.ERROR,
                                message = getMainErrorMessage(error)
                            )
                        )
                    }
                )
        )
    }

    fun transferMoney(request: TransferRequest){
        _transferMoney.value = Resource(ResourceState.LOADING)
        compositeDisposable.add(
            getMainResponseUseCase.transferMoney(request = request)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe {
                    // Perform any setup tasks before the subscription starts
                }
                .doOnTerminate {
                    // Perform any cleanup tasks after the subscription ends
                }
                .subscribe(
                    { response ->
                        _transferMoney.postValue(
                            Resource(
                                ResourceState.SUCCESS,
                                data = response
                            )
                        )
                    },
                    { error ->
                        _transferMoney.postValue(
                            Resource(
                                ResourceState.ERROR,
                                message = getMainErrorMessage(error)
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