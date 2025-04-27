package com.example.taxi.ui.login.personaldata

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.taxi.domain.model.IsCompletedModel
import com.example.taxi.domain.model.MainResponse
import com.example.taxi.domain.model.register.confirm_password.UserData
import com.example.taxi.domain.model.register.person_data.PersonDataRequest
import com.example.taxi.domain.usecase.register.GetRegisterResponseUseCase
import com.example.taxi.utils.Resource
import com.example.taxi.utils.ResourceState
import com.example.taxi.utils.getMainErrorMessage
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class PersonalDataViewModel(private val getRegisterResponseUseCase: GetRegisterResponseUseCase) :
    ViewModel() {
    private val compositeDisposable = CompositeDisposable()

    private val _personDataResponse = MutableLiveData<Resource<MainResponse<UserData<IsCompletedModel>>>>()
    val personDataResponse: LiveData<Resource<MainResponse<UserData<IsCompletedModel>>>> get() = _personDataResponse


    fun fillPersonData(request: PersonDataRequest) {
        _personDataResponse.postValue(Resource(ResourceState.LOADING))

        Log.d("tekshirish", "malumot: $request")
        compositeDisposable.add(getRegisterResponseUseCase.fillPersonData(request = request)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe {}
            .doOnTerminate {}
            .subscribe({
                _personDataResponse.postValue(Resource(ResourceState.SUCCESS, it))
            }, { error ->
                Log.d("tekshirish", "fillPersonData: $error")
                _personDataResponse.postValue(
                    Resource(
                        ResourceState.ERROR,
                        message = getMainErrorMessage(error)
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