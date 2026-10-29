package co.edu.uniquindio.pokeapi.domain.model

/** Los fallos que la UI sabe explicar. La capa de datos traduce a estos las excepciones de red y de HTTP. */
enum class AppError {
    /** Sin internet, tiempo de espera agotado o el servidor no respondió. */
    Network,

    /** La API respondió 404. */
    NotFound,

    /** La API respondió con otro código de error (5xx, 429…). */
    Server,

    /** La respuesta no se pudo leer u ocurrió algo inesperado. */
    Unknown,
}

class AppException(val error: AppError, cause: Throwable? = null) : Exception(error.name, cause)
