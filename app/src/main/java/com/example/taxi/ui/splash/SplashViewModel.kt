package com.example.taxi.ui.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.taxi.domain.exception.traceErrorException
import com.example.taxi.domain.model.MainResponse
import com.example.taxi.domain.model.checkAccess.AccessModel
import com.example.taxi.domain.usecase.main.GetMainResponseUseCase
import com.example.taxi.utils.Resource
import com.example.taxi.utils.ResourceState
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class SplashViewModel(private val getMainResponseUseCase: GetMainResponseUseCase) : ViewModel() {

    private val _checkAccess = MutableLiveData<Resource<MainResponse<Any>>>()
    val checkAccess : LiveData<Resource<MainResponse<Any>>> get() = _checkAccess
    private val compositeDisposable = CompositeDisposable()



    fun checkAccess(request: AccessModel) {
        _checkAccess.postValue(Resource(ResourceState.LOADING))
        compositeDisposable.add(
            getMainResponseUseCase.checkAccess(request = request)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe {
                    // Perform any setup tasks before the subscription starts
                }
                .doOnTerminate {
                    // Perform any cleanup tasks after the subscription ends
                }
                .subscribe(
                    { response ->
                        _checkAccess.postValue(Resource(ResourceState.SUCCESS, response))
                    },
                    { error ->
                        _checkAccess.postValue(
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