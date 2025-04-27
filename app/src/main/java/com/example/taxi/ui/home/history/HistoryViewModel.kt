package com.example.taxi.ui.home.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.taxi.domain.exception.traceErrorException
import com.example.taxi.domain.model.history.HistoryDataResponse
import com.example.taxi.domain.model.history.Meta
import com.example.taxi.domain.usecase.main.GetMainResponseUseCase
import com.example.taxi.utils.Resource
import com.example.taxi.utils.ResourceState
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class HistoryViewModel(private val getMainResponseUseCase: GetMainResponseUseCase) : ViewModel() {


    val compositeDisposable = CompositeDisposable()

    private val _historyResponse = MutableLiveData<Resource<HistoryDataResponse<Meta>>>()

    private val _isActiveFilter = MutableLiveData<Boolean>().apply {
        value = false
    }
    val isActiveFilter: LiveData<Boolean> get() = _isActiveFilter
    val historyResponse: LiveData<Resource<HistoryDataResponse<Meta>>> get() = _historyResponse

    fun getHistory(
        page: Int,
        from: String? = null,
        to: String? = null,
        type: Int? = null,
        status: Int? = null
    ) {
        _historyResponse.postValue(Resource(ResourceState.LOADING))
        compositeDisposable.add(
            getMainResponseUseCase.getHistory(
                page = page,
                from = from,
                to = to,
                type = type,
                status = status
            )
                .subscribeOn(Schedulers.io())
                .doOnSubscribe {
                    // Perform any setup tasks before the subscription starts
                }
                .doOnTerminate {
                    // Perform any cleanup tasks after the subscription ends
                }
                .subscribe(
                    { response ->

                        _historyResponse.postValue(Resource(ResourceState.SUCCESS, response))


                    },
                    { error ->
                        _historyResponse.postValue(
                            Resource(
                                ResourceState.ERROR,
                                message = traceErrorException(error).getErrorMessage()
                            )
                        )
                    }
                )
        )
    }

    public override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }

    fun activatedFilter() {
        _isActiveFilter.value = true
    }

    fun isNotActivatedFilter() {
        _isActiveFilter.value = false
    }

}