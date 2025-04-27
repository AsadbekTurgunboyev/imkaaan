package com.example.taxi.ui.check

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taxi.domain.exception.traceErrorException
import com.example.taxi.domain.model.CheckResponse
import com.example.taxi.domain.model.MainResponse
import com.example.taxi.domain.usecase.main.GetMainResponseUseCase
import com.example.taxi.utils.Resource
import com.example.taxi.utils.ResourceState
import com.google.gson.Gson
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.adapter.rxjava2.HttpException
import java.io.File

class CheckViewModel(private val getRegisterResponseUseCase: GetMainResponseUseCase) : ViewModel() {
    private val compositeDisposable = CompositeDisposable()

    private var _selfieResponse = MutableLiveData<Resource<MainResponse<Any>>>();
    val selfieResponse: LiveData<Resource<MainResponse<Any>>> get() = _selfieResponse

    private var _checkResponse = MutableLiveData<Resource<MainResponse<CheckResponse>>>()
    val checkResponse: LiveData<Resource<MainResponse<CheckResponse>>> get() = _checkResponse

    fun clearSelfie(){
        _checkResponse = MutableLiveData<Resource<MainResponse<CheckResponse>>>()
    }

    fun driverCheck() {
        _checkResponse.postValue(Resource(ResourceState.LOADING))
        compositeDisposable.add(
            getRegisterResponseUseCase.checkPhotoControl()
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
                            _checkResponse.postValue(Resource(ResourceState.SUCCESS, response))

                        }


                    },
                    { error ->
                        _checkResponse.postValue(
                            Resource(
                                ResourceState.ERROR,
                                message = traceErrorException(error).getErrorMessage()
                            )
                        )
                    }
                )
        )
    }

    fun fillChecks(
        imageUris: MutableMap<String, Uri>,

        ) {
        _selfieResponse.postValue(Resource(ResourceState.LOADING))

        val imgFrontFile = imageUris["img_front"]?.let { getFileFromUri(it) }
        val imgBack = imageUris["img_back"]?.let { getFileFromUri(it) }
        val imgLeft = imageUris["img_left"]?.let { getFileFromUri(it) }
        val imgRight = imageUris["img_right"]?.let { getFileFromUri(it) }
        val imgFrontChair = imageUris["img_front_chair"]?.let { getFileFromUri(it) }
        val imgBackChair = imageUris["img_back_chair"]?.let { getFileFromUri(it) }
        val imgNumber = imageUris["img_number"]?.let { getFileFromUri(it) }
        val imgLicense = imageUris["img_license"]?.let { getFileFromUri(it) }

        val imgFrontPhotoRequestBody = imgFrontFile?.asRequestBody("image/*".toMediaTypeOrNull())
        val imgBackRequestBody = imgBack?.asRequestBody("image/*".toMediaTypeOrNull())
        val imgLeftRequestBody = imgLeft?.asRequestBody("image/*".toMediaTypeOrNull())
        val imgRightRequestBody = imgRight?.asRequestBody("image/*".toMediaTypeOrNull())
        val imgFrontChairRequestBody = imgFrontChair?.asRequestBody("image/*".toMediaTypeOrNull())
        val imgBackChairRequestBody = imgBackChair?.asRequestBody("image/*".toMediaTypeOrNull())
        val imgNumberRequestBody = imgNumber?.asRequestBody("image/*".toMediaTypeOrNull())
        val imgLicenseRequestBody = imgLicense?.asRequestBody("image/*".toMediaTypeOrNull())


        val imgFrontPart = imgFrontPhotoRequestBody?.let {
            MultipartBody.Part.createFormData(
                "img_front",
                imgFrontFile.name,
                it
            )
        }
        val imgBackPart = imgBackRequestBody?.let {
            MultipartBody.Part.createFormData(
                "img_back",
                imgBack.name,
                it
            )
        }
        val imgLeftPart = imgLeftRequestBody?.let {
            MultipartBody.Part.createFormData(
                "img_left",
                imgLeft.name,
                it
            )
        }
        val imgRightPart = imgRightRequestBody?.let {
            MultipartBody.Part.createFormData(
                "img_right",
                imgRight.name,
                it
            )
        }
        val imgFrontChairPart = imgFrontChairRequestBody?.let {
            MultipartBody.Part.createFormData(
                "img_front_chair",
                imgFrontChair.name,
                it
            )
        }
        val imgBackChairPart = imgBackChairRequestBody?.let {
            MultipartBody.Part.createFormData(
                "img_back_chair",
                imgBackChair.name,
                it
            )
        }
        val imgNumberPart = imgNumberRequestBody?.let {
            MultipartBody.Part.createFormData(
                "img_number",
                imgNumber.name,
                it
            )
        }
        val imgLicensePart = imgLicenseRequestBody?.let {
            MultipartBody.Part.createFormData(
                "img_license",
                imgLicense.name,
                it
            )
        }

        if (imgFrontPart != null && imgLicensePart != null && imgBackPart != null && imgLeftPart != null &&
            imgRightPart != null && imgFrontChairPart != null && imgBackChairPart != null && imgNumberPart != null
        ) {
            getRegisterResponseUseCase.photoControl(
                imgFrontPart,
                imgBackPart,
                imgLeftPart,
                imgRightPart,
                imgFrontChairPart,
                imgBackChairPart,
                imgNumberPart,
                imgLicensePart
            )
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { /* Optionally handle subscription start */ }
                .doOnTerminate { /* Optionally handle termination */ }
                .subscribe({ response ->
                    Log.e("tekshirish", "fillSelfie: $response")
                    viewModelScope.launch {
                        _selfieResponse.postValue(Resource(ResourceState.SUCCESS, response))
                    }
                }, { error ->
                    val errorMessage = when (error) {
                        is HttpException -> {
                            val errorBody = error.response()?.errorBody()?.string()
                            val errorResponse = Gson().fromJson(
                                errorBody,
                                MainResponse::class.java
                            ) as MainResponse<*>
                            errorResponse.message
                        }

                        else -> error.localizedMessage ?: "An unknown error occurred"
                    }
                    Log.e("tekshirish", "Error in fillSelfie: $errorMessage")
                    _selfieResponse.postValue(Resource(ResourceState.ERROR, message = errorMessage))
                }).also { compositeDisposable.add(it) }
        } else {
            _selfieResponse.postValue(
                Resource(
                    ResourceState.ERROR,
                    message = "Suratlarni yuklashda xatolik"
                )
            )
        }


    }


    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
        compositeDisposable.clear()
    }

    private fun getFileFromUri(uri: Uri): File? {
        val filePath = uri.path
        return filePath?.let { File(it) }
    }

}
