package com.amitra.trustlines.client

class ApiException : RuntimeException {
    val status: Int
    override val message: String
    val errorMessage: ErrorMessage?

    constructor(status: Int, message: String, errorMessage: ErrorMessage? = null) {
        this.status = status
        this.message = message
        this.errorMessage = errorMessage
    }


}

