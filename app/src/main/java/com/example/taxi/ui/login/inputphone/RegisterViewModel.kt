package com.example.taxi.ui.login.inputphone

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taxi.domain.exception.traceErrorException
import com.example.taxi.domain.model.MainResponse
import com.example.taxi.domain.model.register.RegisterData
import com.example.taxi.domain.model.register.RegisterRequest
import com.example.taxi.domain.usecase.register.GetRegisterResponseUseCase
import com.example.taxi.utils.Resource
import com.example.taxi.utils.ResourceState
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch

class RegisterViewModel(private val getRegisterResponseUseCase: GetRegisterResponseUseCase) :
    ViewModel() {
    private val compositeDisposable = CompositeDisposable()

    private var _registerResponse = MutableLiveData<Resource<MainResponse<RegisterData>>>()

    val registerResponse: LiveData<Resource<MainResponse<RegisterData>>>
        get() = _registerResponse

    fun register(phone: RegisterRequest) {

        _registerResponse.postValue(Resource(ResourceState.LOADING))
        compositeDisposable.add(
            getRegisterResponseUseCase.register(phone = phone)
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
                            _registerResponse.postValue(Resource(ResourceState.SUCCESS, response))

                        }


                    },
                    { error ->

                        _registerResponse.postValue(
                            Resource(
                                ResourceState.ERROR,
                                message = traceErrorException(error).getErrorMessage()
                            )
                        )
                    }
                )
        )

    }

    fun clear() {
        _registerResponse = MutableLiveData()
    }

    companion object {
        private val TAG = RegisterViewModel::class.java.name
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }
}

