package com.example.taxi.ui.home.transfer.transferhistory

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.taxi.domain.model.transfer.HistoryMeta
import com.example.taxi.domain.model.transfer.ResponseTransferHistory
import com.example.taxi.domain.usecase.main.GetMainResponseUseCase
import com.example.taxi.utils.Resource
import com.example.taxi.utils.ResourceState
import com.example.taxi.utils.getMainErrorMessage
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class TransferHistoryViewModel(private val getMainResponseUseCase: GetMainResponseUseCase) :
    ViewModel() {
    private val compositeDisposable = CompositeDisposable()

    private val _historyTransfer = MutableLiveData<Resource<ResponseTransferHistory<HistoryMeta>>>()
    val historyTransfer: LiveData<Resource<ResponseTransferHistory<HistoryMeta>>> get() = _historyTransfer

    private val _isActiveFilter = MutableLiveData<Boolean>().apply {
        value = false
    }
    val isActiveFilter: LiveData<Boolean> get() = _isActiveFilter

    fun getHistoryFromPage(page: Int, from: String? = null, to: String? = null, type: Int? = null) {
        _historyTransfer.value = Resource(ResourceState.LOADING)
        compositeDisposable.add(
            getMainResponseUseCase.getTransferHistory(page = page, from = from, to = to,type = type)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe {
                    // Perform any setup tasks before the subscription starts
                }
                .doOnTerminate {
                    // Perform any cleanup tasks after the subscription ends
                }
                .subscribe(
                    { response ->



                        _historyTransfer.postValue(
                            Resource(
                                ResourceState.SUCCESS,
                                data = response
                            )
                        )
                    },
                    { error ->
                        Log.d("apiresponse", "getHistoryFromPage: $error ", )

                        _historyTransfer.postValue(
                            Resource(
                                ResourceState.ERROR,
                                message = getMainErrorMessage(error)
                            )
                        )
                    }
                )
        )
    }

    fun activatedFilter(){
        _isActiveFilter.value = true
    }

    fun isNotActivatedFilter(){
        _isActiveFilter.value = false
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }
}