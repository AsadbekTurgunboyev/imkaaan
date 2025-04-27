package com.example.taxi.domain.repository

import com.example.taxi.domain.model.IsCompletedModel
import com.example.taxi.domain.model.MainResponse
import com.example.taxi.domain.model.register.RegisterData
import com.example.taxi.domain.model.register.RegisterRequest
import com.example.taxi.domain.model.register.car_data.CarColorResponse
import com.example.taxi.domain.model.register.car_data.CarDataResponse
import com.example.taxi.domain.model.register.car_data.CarInfoRequest
import com.example.taxi.domain.model.register.car_data.CarInfoResponse
import com.example.taxi.domain.model.register.confirm_password.ConfirmationRequest
import com.example.taxi.domain.model.register.confirm_password.ResendSmsRequest
import com.example.taxi.domain.model.register.confirm_password.UserData
import com.example.taxi.domain.model.register.person_data.PersonDataRequest
import com.example.taxi.domain.model.selfie.SelfieAllData
import com.example.taxi.domain.model.selfie.StatusModel
import io.reactivex.Observable
import okhttp3.MultipartBody

interface RegisterRepository {
    fun register(phone : RegisterRequest): Observable<MainResponse<RegisterData>>

    fun confirmPassword(parentId: Int? = null,request: ConfirmationRequest): Observable<MainResponse<UserData<IsCompletedModel>>>

    fun fillPersonData( request: PersonDataRequest): Observable<MainResponse<UserData<IsCompletedModel>>>

    fun getCarColor() : Observable<CarColorResponse>

    fun getCarData() : Observable<CarDataResponse>

    fun resendSMS(request: ResendSmsRequest) : Observable<MainResponse<UserData<IsCompletedModel>>>

    fun fillCarInfo(request: CarInfoRequest): Observable<CarInfoResponse>

    fun fillSelfie(selfieUri: MultipartBody.Part, licensePhotoUri: MultipartBody.Part): Observable<MainResponse<SelfieAllData<IsCompletedModel,StatusModel>>>
}