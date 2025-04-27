package com.example.taxi.domain.usecase.base

import com.example.taxi.domain.model.ApiError

interface UseCaseResponse<Type> {

    fun onSuccess(result: Type)

    fun onError(apiError: ApiError?)
}