package com.example.taxi.ui.home.dashboard

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taxi.domain.exception.traceErrorException
import com.example.taxi.domain.model.IsCompletedModel
import com.example.taxi.domain.model.MainResponse
import com.example.taxi.domain.model.PaymentUrl
import com.example.taxi.domain.model.balance.BalanceData
import com.example.taxi.domain.model.message.MessageResponse
import com.example.taxi.domain.model.selfie.SelfieAllData
import com.example.taxi.domain.model.selfie.StatusModel
import com.example.taxi.domain.model.settings.DataNames
import com.example.taxi.domain.model.settings.SettingsData
import com.example.taxi.domain.model.settings.getItemValueByName
import com.example.taxi.domain.preference.UserPreferenceManager
import com.example.taxi.domain.usecase.main.GetMainResponseUseCase
import com.example.taxi.translate.TranslateApi
import com.example.taxi.utils.Resource
import com.example.taxi.utils.ResourceState
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class DashboardViewModel(
    private val getMainResponseUseCase: GetMainResponseUseCase,
    private val userPreferenceManager: UserPreferenceManager,
    private val translateApi: TranslateApi
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val _balanceResponse = MutableLiveData<Resource<MainResponse<BalanceData>>>()

    private val _settingsResponse = MutableLiveData<Resource<MainResponse<List<SettingsData>>>>()

    val balanceResponse: LiveData<Resource<MainResponse<BalanceData>>>
        get() = _balanceResponse

    private val _driverDataResponse =
        MutableLiveData<Resource<MainResponse<SelfieAllData<IsCompletedModel, StatusModel>>>>()
    val driverDataResponse get() = _driverDataResponse

    val settingsResponse: LiveData<Resource<MainResponse<List<SettingsData>>>>
        get() = _settingsResponse

    private val _messageResponse = MutableLiveData<Resource<MessageResponse>>()

    val messageResponse: LiveData<Resource<MessageResponse>> get() = _messageResponse

    private val _paymentType = MutableLiveData<Int>().apply {
        value = 1
    }

    private var _paymentProgress = MutableLiveData<Resource<MainResponse<PaymentUrl>>>()
    val paymentProgress: LiveData<Resource<MainResponse<PaymentUrl>>> get() = _paymentProgress
    val paymentType: LiveData<Int> get() = _paymentType

    fun clearPaymentProgress() {
        _paymentProgress = MutableLiveData<Resource<MainResponse<PaymentUrl>>>()
    }

    fun getMessage() {
        _messageResponse.postValue(Resource(ResourceState.LOADING))
        compositeDisposable.add(
            getMainResponseUseCase.getMessage()
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
                            // Check if the user's preferred language is Russian
                            if (userPreferenceManager.getLanguage().code == "ru") {
                                // Launch all translation jobs
                                val jobs = response.data.map { message ->
                                    async {
                                        val translatedText = translateApi.translateSuspend(
                                            message.message,
                                            "uz",
                                            "ru"
                                        )
                                        message.message = translatedText ?: message.message
                                    }
                                }
                                jobs.forEach { it.await() }
                            }
                            _messageResponse.postValue(Resource(ResourceState.SUCCESS, response))
                            updateMessagePreferences(response.data.size)
                        }


                    },
                    { error ->
                        _messageResponse.postValue(
                            Resource(
                                ResourceState.ERROR,
                                message = traceErrorException(error).getErrorMessage()
                            )
                        )
                    }
                )
        )
    }

    private suspend fun TranslateApi.translateSuspend(
        text: String,
        sourceLang: String,
        targetLang: String
    ): String? {
        return suspendCancellableCoroutine { continuation ->
            translate(text, sourceLang, targetLang).whenComplete { result, throwable ->
                if (throwable != null) {
                    continuation.resumeWithException(throwable)
                } else {
                    continuation.resume(result)
                }
            }
        }
    }

    private fun updateMessagePreferences(currentMessageCount: Int) {
        if (currentMessageCount > userPreferenceManager.getMessageValue()) {
            userPreferenceManager.saveMessageCount(currentMessageCount - userPreferenceManager.getMessageValue())
            userPreferenceManager.saveMessageValue(currentMessageCount)
        }
    }

    fun paymentPayme() {
        _paymentType.postValue(2)
    }

    fun paymentUzum() {
        _paymentType.postValue(3)
    }

    fun paymentClick() {
        _paymentType.postValue(1)
    }

    fun getSettings() {
        _settingsResponse.postValue(Resource(ResourceState.LOADING))
        compositeDisposable.add(
            getMainResponseUseCase.getSettings()
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
                            _settingsResponse.postValue(Resource(ResourceState.SUCCESS, response))
                            setPhoneNumber(response?.getItemValueByName(DataNames.PHONE_NUMBER))
                            setAllSettings(response)
                        }


                    },
                    { error ->
                        _settingsResponse.postValue(
                            Resource(
                                ResourceState.ERROR,
                                message = traceErrorException(error).getErrorMessage()
                            )
                        )
                    }
                )
        )
    }

    private fun setAllSettings(response: MainResponse<List<SettingsData>>?) {
        val centerLat = response?.getItemValueByName(DataNames.CENTER_LATITUDE)
        val centerLong = response?.getItemValueByName(DataNames.CENTER_LONGITUDE)
        val centerRadius = response?.getItemValueByName(DataNames.CENTER_RADIUS)
        userPreferenceManager.setSettings(centerLat, centerLong, centerRadius)
    }

    private fun setPhoneNumber(itemValueByName: String?) {
        itemValueByName?.let {
            userPreferenceManager.saveCallPhoneNumber(it)
        }
    }

    fun getDriverData() {
        _driverDataResponse.postValue(Resource(ResourceState.LOADING))
        compositeDisposable.add(
            getMainResponseUseCase.getDriverData()
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
                            // Check if the user's preferred language is Russian
                            if (userPreferenceManager.getLanguage().code == "ru") {
                                // Launch all translation jobs
                                try {
                                    if (response.data.status.string.lowercase() == "tasdiqlanmagan" || response.data.status.string.lowercase() == "tasdiqlangan") {
                                        response.data.status.string =
                                            if (response.data.status.string.lowercase() == "tasdiqlanmagan") "Не подтверждено" else "Подтверждено"
                                        _driverDataResponse.postValue(
                                            Resource(
                                                ResourceState.SUCCESS,
                                                response
                                            )
                                        )

                                    } else {
                                        val m = translateApi.translateSuspend(
                                            response.data.status.string,
                                            "uz",
                                            "ru"
                                        )
                                        Log.d("tekshirish", "getDriverData: $m")
                                        response.data.status.string = m.toString()
                                        _driverDataResponse.postValue(
                                            Resource(
                                                ResourceState.SUCCESS,
                                                response
                                            )
                                        )
                                    }

                                } catch (e: Exception) {
                                    _driverDataResponse.postValue(
                                        Resource(
                                            ResourceState.SUCCESS,
                                            response
                                        )
                                    )

                                }
                            } else {
                                _driverDataResponse.postValue(
                                    Resource(
                                        ResourceState.SUCCESS,
                                        response
                                    )
                                )

                            }
                        }

                    },
                    { error ->
                        _driverDataResponse.postValue(
                            Resource(
                                ResourceState.ERROR,
                                message = traceErrorException(error).code.toString()
                            )
                        )
                    }
                )
        )
    }

    fun getBalance() {
        _balanceResponse.postValue(Resource(ResourceState.LOADING))
        compositeDisposable.add(
            getMainResponseUseCase.getBalance()
                .subscribeOn(Schedulers.io())
                .doOnSubscribe {
                    // Perform any setup tasks before the subscription starts
                }
                .doOnTerminate {
                    // Perform any cleanup tasks after the subscription ends
                }
                .subscribe(
                    { response ->
                        _balanceResponse.postValue(Resource(ResourceState.SUCCESS, response))
                    },
                    { error ->
                        _balanceResponse.postValue(
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

    fun payment(toInt: Int) {
        _paymentProgress.postValue(Resource(ResourceState.LOADING))
        val a = when (paymentType.value) {
            1 -> getMainResponseUseCase.paymentClick(toInt)
            2 -> getMainResponseUseCase.paymentPayme(toInt)
            3 -> getMainResponseUseCase.paymentUzum(toInt)
            else -> getMainResponseUseCase.paymentClick(toInt)
        }

        compositeDisposable.add(a
            .subscribeOn(Schedulers.io())
            .doOnSubscribe {
                // Perform any setup tasks before the subscription starts
            }
            .doOnTerminate {
                // Perform any cleanup tasks after the subscription ends
            }
            .subscribe(
                { response ->

                    Log.d("tekshirish", "payment: ${response.data.url}")
                    _paymentProgress.postValue(Resource(ResourceState.SUCCESS, response))
                },
                { error ->
                    Log.d("tekshirish", "payment: ${error.message}")
                    _paymentProgress.postValue(
                        Resource(
                            ResourceState.ERROR,
                            message = traceErrorException(error).getErrorMessage()
                        )
                    )
                }
            )
        )
    }
}