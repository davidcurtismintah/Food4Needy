package com.allow.food4needy.domain

enum class Status {
    LOADING,
    SUCCESS,
    ERROR
}

class Response<D, E> private constructor(
        val status: Status,
        val data: D? = null,
        val error: E? = null
) {
    companion object {

        fun <D, E> loading(): Response<D, E> {
            return Response(status = Status.LOADING)
        }

        fun <D, E> success(data: D): Response<D, E> {
            return Response(status = Status.SUCCESS, data = data)
        }

        fun <D, E> error(error: E): Response<D, E> {
            return Response(status = Status.ERROR, error = error)
        }
    }
}