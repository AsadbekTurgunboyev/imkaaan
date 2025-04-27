package com.example.taxi.ui.home.menu.about

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.taxi.domain.exception.traceErrorException
import com.example.taxi.domain.model.MainResponse
import com.example.taxi.domain.model.about.ResponseAbout
import com.example.taxi.domain.usecase.main.GetMainResponseUseCase
import com.example.taxi.utils.Resource
import com.example.taxi.utils.ResourceState
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class AboutViewModel(private val mainResponseUseCase: GetMainResponseUseCase): ViewModel() {
    private val compositeDisposable = CompositeDisposable()

    private val _about = MutableLiveData<Resource<MainResponse<ResponseAbout>>>()
    val about : LiveData<Resource<MainResponse<ResponseAbout>>> get() = _about


    private val _faq = MutableLiveData<Resource<MainResponse<ResponseAbout>>>()
    val faq : LiveData<Resource<MainResponse<ResponseAbout>>> get() = _faq



    fun getAbout(){
        _about.postValue(Resource(ResourceState.LOADING))
        compositeDisposable.add(mainResponseUseCase.getAbout().subscribeOn(Schedulers.io())
            .doOnSubscribe {
                // Perform any setup tasks before the subscription starts
            }.doOnTerminate {}.subscribe({ response ->
                _about.postValue(Resource(ResourceState.SUCCESS, response))
            }, { error ->
                _about.postValue(
                    Resource(
                        ResourceState.ERROR,
                        message = traceErrorException(error).getErrorMessage()
                    )
                )

            })
        )
    }

    fun getFAQ(){
        _faq.postValue(Resource(ResourceState.LOADING))
        compositeDisposable.add(mainResponseUseCase.getFAQ().subscribeOn(Schedulers.io())
            .doOnSubscribe {
                // Perform any setup tasks before the subscription starts
            }.doOnTerminate {}.subscribe({ response ->
                _faq.postValue(Resource(ResourceState.SUCCESS, response))
            }, { error ->
                _faq.postValue(
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