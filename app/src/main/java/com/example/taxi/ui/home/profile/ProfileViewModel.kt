package com.example.taxi.ui.home.profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.taxi.domain.exception.traceErrorException
import com.example.taxi.domain.model.MainResponse
import com.example.taxi.domain.model.statistics.StatisticsResponse
import com.example.taxi.domain.model.statistics.StatisticsResponseValue
import com.example.taxi.domain.usecase.main.GetMainResponseUseCase
import com.example.taxi.utils.Resource
import com.example.taxi.utils.ResourceState
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class ProfileViewModel(private val getMainResponseUseCase: GetMainResponseUseCase) : ViewModel() {
    private val compositeDisposable = CompositeDisposable()
    private val _statisticType = MutableLiveData<Int>().apply {
        value = 2
    }

    private val _statisticsValue = MutableLiveData<Resource<MainResponse<List<StatisticsResponse<StatisticsResponseValue>>>>>()
    val statisticsValue :LiveData<Resource<MainResponse<List<StatisticsResponse<StatisticsResponseValue>>>>> get() = _statisticsValue

    val statisticsType: LiveData<Int> get() = _statisticType


    fun getStatistics(type: Int){
        _statisticsValue.postValue(Resource(ResourceState.LOADING))
        compositeDisposable.add(
            getMainResponseUseCase.getStatisticsData(type)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe {
                    // Perform any setup tasks before the subscription starts
                }
                .doOnTerminate {
                    // Perform any cleanup tasks after the subscription ends
                }
                .subscribe(
                    { response ->

                        _statisticsValue.postValue(Resource(ResourceState.SUCCESS, response))
                        Log.d("xizmat ", "getService: $response")
//                        _serviceResponse.postValue(Resource(ResourceState.SUCCESS, response))
                    },
                    { error ->
                        _statisticsValue.postValue(Resource(ResourceState.ERROR, message = traceErrorException(error).getErrorMessage()))

                        Log.e("xizmat ", "getService: $error")

//                        _serviceResponse.postValue(
//                            Resource(
//                                ResourceState.ERROR,
//                                message = traceErrorException(error).getErrorMessage()
//                            )
//                        )
                    }
                )
        )
    }


    fun setWeeks() {
        _statisticType.postValue(1)
    }

    fun setDays() {
        _statisticType.postValue(2)
    }

    fun setMonths() {
        _statisticType.postValue(0)
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }
}