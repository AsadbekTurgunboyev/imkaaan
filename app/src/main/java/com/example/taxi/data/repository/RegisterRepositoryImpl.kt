package com.example.taxi.data.repository

import com.example.taxi.data.source.ApiService
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
import com.example.taxi.domain.repository.RegisterRepository
import io.reactivex.Observable
import okhttp3.MultipartBody

class RegisterRepositoryImpl(private val apiService: ApiService) : RegisterRepository {

    override fun register(phone: RegisterRequest): Observable<MainResponse<RegisterData>> {
        return apiService.register(phone = phone)
    }

    override fun confirmPassword(
        parentId: Int?,
        request: ConfirmationRequest
    ): Observable<MainResponse<UserData<IsCompletedModel>>> {
        return apiService.confirmPhone(parentId = parentId, request = request)
    }



    override fun fillPersonData(
        request: PersonDataRequest
    ): Observable<MainResponse<UserData<IsCompletedModel>>> {
        return apiService.fillPersonData(request = request)
    }

    override fun getCarColor(): Observable<CarColorResponse> {
        return apiService.getColors()
    }

    override fun getCarData(): Observable<CarDataResponse> {
        return apiService.getCars()
    }

    override fun resendSMS(request: ResendSmsRequest): Observable<MainResponse<UserData<IsCompletedModel>>> {
        return apiService.resendSms(request = request)
    }


    override fun fillCarInfo(request: CarInfoRequest): Observable<CarInfoResponse> {
        return apiService.fillCarInfo(request = request)
    }


    override fun fillSelfie(
        selfieUri: MultipartBody.Part,
        licensePhotoUri: MultipartBody.Part
    ): Observable<MainResponse<SelfieAllData<IsCompletedModel,StatusModel>>> {
        return apiService.fillSelfie(selfie = selfieUri, licensePhoto = licensePhotoUri)
    }


}