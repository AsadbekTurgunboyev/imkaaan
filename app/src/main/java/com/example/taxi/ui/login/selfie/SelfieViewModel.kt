package com.example.taxi.ui.login.selfie

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taxi.domain.exception.traceErrorException
import com.example.taxi.domain.model.IsCompletedModel
import com.example.taxi.domain.model.MainResponse
import com.example.taxi.domain.model.selfie.SelfieAllData
import com.example.taxi.domain.model.selfie.StatusModel
import com.example.taxi.domain.usecase.register.GetRegisterResponseUseCase
import com.example.taxi.utils.Resource
import com.example.taxi.utils.ResourceState
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class SelfieViewModel(private val getRegisterResponseUseCase: GetRegisterResponseUseCase) :
    ViewModel() {
    private val compositeDisposable = CompositeDisposable()

    private var _selfieResponse = MutableLiveData<Resource<MainResponse<SelfieAllData<IsCompletedModel,StatusModel>>>>();
    val selfieResponse get() = _selfieResponse


    fun fillSelfie(
        selfieUri: Uri, licensePhotoUri: Uri, contentResolver: ContentResolver
    ) {
        _selfieResponse.postValue(Resource(ResourceState.LOADING))

        val selfieFile = getFileFromUri(selfieUri, contentResolver)
        val licensePhotoFile = getFileFromUri(licensePhotoUri, contentResolver)

        val selfieRequestBody = selfieFile?.asRequestBody("image/*".toMediaTypeOrNull())
        val licensePhotoRequestBody = licensePhotoFile?.asRequestBody("image/*".toMediaTypeOrNull())

        val selfiePart = selfieRequestBody?.let {
            MultipartBody.Part.createFormData("selfie", selfieFile.name, it)
        }

        val licensePhotoPart = licensePhotoRequestBody?.let {
            MultipartBody.Part.createFormData("licensePhoto", licensePhotoFile.name, it)
        }

        if (selfiePart != null && licensePhotoPart != null) {
            getRegisterResponseUseCase.fillSelfie(selfiePart, licensePhotoPart)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { /* Optionally handle subscription start */ }
                .doOnTerminate { /* Optionally handle termination */ }
                .subscribe({ response ->
                    Log.e("tekshirish", "fillSelfie: $response")
                    viewModelScope.launch {
                        _selfieResponse.postValue(Resource(ResourceState.SUCCESS, response))
                    }
                }, { error ->
                    val apiError = traceErrorException(error)
                    val errorMessage = apiError.message ?: "An unknown error occurred"

                    Log.e("tekshirish", "Error in fillSelfie: $errorMessage", error)
                    _selfieResponse.postValue(Resource(ResourceState.ERROR, message = errorMessage))
                }).also { compositeDisposable.add(it) }
        }else{
            _selfieResponse.postValue(
                Resource(
                    ResourceState.ERROR,
                    message = "Suratlarni yuklashda xatolik"
                )
            )
        }

    }

    fun clearFile(){
        _selfieResponse = MutableLiveData<Resource<MainResponse<SelfieAllData<IsCompletedModel,StatusModel>>>>()
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
        compositeDisposable.clear()
    }

    private fun getFileFromUri(uri: Uri, contentResolver: ContentResolver): File? {
        val filePath = uri.path
        return filePath?.let { File(it) }
    }
}