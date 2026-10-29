package co.edu.uniquindio.pokeapi.data.remote

import co.edu.uniquindio.pokeapi.domain.model.AppError
import co.edu.uniquindio.pokeapi.domain.model.AppException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException

/** Traduce lo que lanzan Retrofit y OkHttp a los errores que entiende la UI. */
internal fun Throwable.toAppException(): AppException = when (this) {
    is AppException -> this
    is IOException -> AppException(AppError.Network, this)
    is HttpException -> AppException(if (code() == 404) AppError.NotFound else AppError.Server, this)
    is SerializationException -> AppException(AppError.Unknown, this)
    else -> AppException(AppError.Unknown, this)
}

/** Ejecuta una petición y convierte su fallo en [AppException]. La cancelación de la corrutina se deja pasar. */
internal suspend inline fun <T> safeApiCall(block: () -> T): T =
    try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        throw e.toAppException()
    }
