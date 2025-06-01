package com.example.speakerrecoginitonfinal2.data.util

sealed class Resource<T>(open val data: T? = null, open val message: String? = null) { // val -> open val
    class Success<T>(override val data: T) : Resource<T>(data) // override val data: T
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message) // data: T? kalabilir
    // Loading sınıfına isInitialLoading eklendi
    class Loading<T>(data: T? = null, val isInitialLoading: Boolean = false) : Resource<T>(data) // data: T? kalabilir
}