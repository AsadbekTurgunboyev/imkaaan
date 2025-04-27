package com.example.taxi.domain.exception

import com.example.taxi.domain.model.ApiError
import com.example.taxi.domain.model.UNKNOWN_ERROR_MESSAGE
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

fun traceErrorException(throwable: Throwable?): ApiError {
    return when (throwable) {

        is HttpException -> {
            when (throwable.code()) {
                400 -> ApiError(
                    throwable.message(),
                    throwable.code(),
                    ApiError.ErrorStatus.BAD_REQUEST
                )
                401 -> ApiError(
                    throwable.message(),
                    throwable.code(),
                    ApiError.ErrorStatus.UNAUTHORIZED
                )
                403 -> ApiError(
                    throwable.message(),
                    throwable.code(),
                    ApiError.ErrorStatus.FORBIDDEN
                )
                404 -> ApiError(
                    throwable.message(),
                    throwable.code(),
                    ApiError.ErrorStatus.NOT_FOUND
                )
                405 -> ApiError(
                    throwable.message(),
                    throwable.code(),
                    ApiError.ErrorStatus.METHOD_NOT_ALLOWED
                )
                409 -> ApiError(
                    throwable.message(),
                    throwable.code(),
                    ApiError.ErrorStatus.CONFLICT
                )


                410 -> ApiError(
                    throwable.message(),
                    throwable.code(),// haydovchi faol emas
                    ApiError.ErrorStatus.CONFLICT
                )
                411 -> ApiError(
                    throwable.message(),
                    throwable.code(),// faol buyurtmasi mavjud
                    ApiError.ErrorStatus.CONFLICT
                )
                412 -> ApiError(
                    throwable.message(),
                    throwable.code(),// hisobda yetarli mablag' mavjud emas
                    ApiError.ErrorStatus.CONFLICT
                )
                413 -> ApiError(
                    throwable.message(),
                    throwable.code(),// Buyurtma qabul qilinib bo'ldi
                    ApiError.ErrorStatus.CONFLICT
                )

                414 -> ApiError(
                    throwable.message(),
                    throwable.code(),// boshqa
                    ApiError.ErrorStatus.CONFLICT
                )

                500 -> ApiError(
                    throwable.message(),
                    throwable.code(),
                    ApiError.ErrorStatus.INTERNAL_SERVER_ERROR
                )
                else -> ApiError(
                    UNKNOWN_ERROR_MESSAGE,
                    0,
                    ApiError.ErrorStatus.UNKNOWN_ERROR
                )
            }
        }

        is SocketTimeoutException -> {
            ApiError(throwable.message, ApiError.ErrorStatus.TIMEOUT)
        }

        is IOException -> {
            ApiError(throwable.message, ApiError.ErrorStatus.NO_CONNECTION)
        }

        else -> ApiError(UNKNOWN_ERROR_MESSAGE, 0, ApiError.ErrorStatus.UNKNOWN_ERROR)
    }
}