package com.example.taxi.ui.login.inputpassword

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taxi.domain.exception.traceErrorException
import com.example.taxi.domain.model.IsCompletedModel
import com.example.taxi.domain.model.MainResponse
import com.example.taxi.domain.model.register.confirm_password.ConfirmationRequest
import com.example.taxi.domain.model.register.confirm_password.ResendSmsRequest
import com.example.taxi.domain.model.register.confirm_password.UserData
import com.example.taxi.domain.usecase.register.GetRegisterResponseUseCase
import com.example.taxi.utils.Resource
import com.example.taxi.utils.ResourceState
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch

class InputPasswordViewModel(private val getRegisterResponseUseCase: GetRegisterResponseUseCase) :
    ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val _confirmResponse = MutableLiveData<Resource<MainResponse<UserData<IsCompletedModel>>>>()
    val confirmResponse: LiveData<Resource<MainResponse<UserData<IsCompletedModel>>>> get() = _confirmResponse

    fun confirmPassword(request: ConfirmationRequest, refererId: Int?) {
        _confirmResponse.postValue(Resource(ResourceState.LOADING))
        compositeDisposable
            .add(getRegisterResponseUseCase.confirmPassword(request = request, parentId = refererId)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe {}
                .doOnTerminate {}
                .subscribe({ response ->
                    viewModelScope.launch {
                        _confirmResponse.postValue(
                            Resource(
                                ResourceState.SUCCESS,
                                response
                            )
                        )

                    }
                },
                    { error ->
                        _confirmResponse.postValue(
                            Resource(
                                ResourceState.ERROR,
                                message = traceErrorException(error).getErrorMessage()
                            )
                        )
                    })
            )

    }

    fun resendSMS(request: ResendSmsRequest) {
        _confirmResponse.postValue(Resource(ResourceState.LOADING))
        compositeDisposable
            .add(getRegisterResponseUseCase.resendSMS(request = request)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe {}
                .doOnTerminate {}
                .subscribe({ response ->
                    viewModelScope.launch {
                        _confirmResponse.postValue(
                            Resource(
                                ResourceState.SUCCESS,
                                response
                            )
                        )

                    }
                },
                    { error ->
                        _confirmResponse.postValue(
                            Resource(
                                ResourceState.ERROR,
                                message = traceErrorException(error).getErrorMessage()
                            )
                        )
                    })
            )

    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }
}