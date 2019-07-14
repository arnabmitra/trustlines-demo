package com.amitra.trustlines.client

import feign.Response
import feign.Util
import feign.codec.ErrorDecoder
import org.slf4j.LoggerFactory
import java.io.IOException
import java.lang.Exception

class ExceptionDecoder : ErrorDecoder {

    val logger = LoggerFactory.getLogger(this::class.java)

    override fun decode(methodKey: String?, response: Response): ApiException {
        var message = String.format("status %s reading %s", response.status(), methodKey)
        var body: String? = null
        try {
            if (response.body() != null) {
                body = Util.toString(response.body().asReader())
                message += "; content:\n$body"
            }
        } catch (ignored: IOException) {
            //what to log?? imo nothing
        }

        val errorMessage = getErrorMessage(body)
        return ApiException(status = response.status(), message = message, errorMessage = errorMessage)
    }


    fun getErrorMessage(errorMessage: String?): ErrorMessage? {
        return errorMessage?.let {
            try {
                //customize irl
                ErrorMessage(errorKey = "TRUST_LINE_ERROR", errorDetails = errorMessage)
            } catch (e: Exception) {
                logger.warn("An exception occurred while getting the chain code exception args", e)
                null
            }
        }
    }
}


data class ErrorMessage(val errorKey: String? = null, val errorDetails: String? = null)
