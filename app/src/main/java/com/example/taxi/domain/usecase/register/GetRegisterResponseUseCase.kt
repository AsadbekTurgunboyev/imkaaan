package com.example.taxi.domain.usecase.register

import com.example.taxi.domain.model.register.RegisterRequest
import com.example.taxi.domain.model.register.car_data.CarInfoRequest
import com.example.taxi.domain.model.register.confirm_password.ConfirmationRequest
import com.example.taxi.domain.model.register.confirm_password.ResendSmsRequest
import com.example.taxi.domain.model.register.person_data.PersonDataRequest
import com.example.taxi.domain.repository.RegisterRepository
import okhttp3.MultipartBody

class GetRegisterResponseUseCase(private val registerRepository: RegisterRepository) {

    fun register(phone: RegisterRequest) =
        registerRepository.register(phone = phone)

    fun resendSMS(request: ResendSmsRequest) =
        registerRepository.resendSMS(request = request)

    fun confirmPassword(request: ConfirmationRequest, parentId : Int? = null) =
        registerRepository.confirmPassword(request = request, parentId = parentId)

    fun fillPersonData(request: PersonDataRequest) =
        registerRepository.fillPersonData(request = request)

    fun fillCarInfo(request: CarInfoRequest) =
        registerRepository.fillCarInfo(request = request)

    fun fillSelfie(selfieUri: MultipartBody.Part, licensePhotoUri: MultipartBody.Part) =
        registerRepository.fillSelfie(selfieUri = selfieUri, licensePhotoUri = licensePhotoUri)

    fun getCarColor() = registerRepository.getCarColor()

    fun getCarData() = registerRepository.getCarData()


}