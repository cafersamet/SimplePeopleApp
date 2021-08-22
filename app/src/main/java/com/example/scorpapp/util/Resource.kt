package com.example.scorpapp.util

sealed class Resource<T>(val data: T? = null, val message: String? = null, val nextKey: String? = null) {
    class Success<T>(data: T, nextKey : String?) : Resource<T>(data, nextKey = nextKey)
    class Error<T>(message: String) : Resource<T>(message = message)
    class Loading<T> :Resource<T>()
}