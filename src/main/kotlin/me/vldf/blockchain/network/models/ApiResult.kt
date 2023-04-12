package me.vldf.blockchain.network.models

import kotlinx.serialization.Serializable
import me.vldf.blockchain.network.models.bodies.AbstractBlockchainMessageBody

@Serializable
data class ApiResult<T : AbstractBlockchainMessageBody>(
    val result: T?,
    val errorMessage: String? = null
) {
    companion object {
        fun <T : AbstractBlockchainMessageBody> fromResult(result: T): ApiResult<T> {
            return ApiResult(result = result)
        }

        fun <T : AbstractBlockchainMessageBody> fromError(errorMessage: String): ApiResult<T> {
            return ApiResult(result = null, errorMessage = errorMessage)
        }
    }
}

//@OptIn(InternalSerializationApi::class)
//object ApiResultDeserializer : KSerializer<ApiResult<*>> {
//    override val descriptor: SerialDescriptor = ApiResult::class.serializer().descriptor
//
//    override fun deserialize(decoder: Decoder): ApiResult<*> {
//        val result = decoder.decodeSerializableValue()
//    }
//
//    override fun serialize(encoder: Encoder, value: ApiResult<*>) {
//        TODO("Not yet implemented")
//    }
//
//}
