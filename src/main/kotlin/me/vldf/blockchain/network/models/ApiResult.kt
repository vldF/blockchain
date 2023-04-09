package me.vldf.blockchain.network.models

import kotlinx.serialization.Serializable

@Serializable
@Suppress("DataClassPrivateConstructor")
data class ApiResult<T> private constructor(
    val result: T?,
    val errorMessage: String? = null
) {
    companion object {
        fun <T> fromResult(result: T): ApiResult<T> {
            return ApiResult(result = result)
        }

        fun <T> fromError(errorMessage: String): ApiResult<T> {
            return ApiResult(result = null, errorMessage = errorMessage)
        }
    }
}
