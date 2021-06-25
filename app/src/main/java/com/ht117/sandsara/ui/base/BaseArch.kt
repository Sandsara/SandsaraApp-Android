package com.ht117.sandsara.ui.base

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface IAction

interface IState

sealed class Resource<T>(open val data: T? = null) {
    class Idle<T>: Resource<T>()
    data class Success<T>(override val data: T): Resource<T>(data)
    data class Stream<T>(val stream: Flow<T>): Resource<T>()
    class Loading<T>: Resource<T>()
    class Error<T>(val message: String): Resource<T>()
}

interface IModel<State: IState, Action: IAction> {
    val actions: SharedFlow<Action>
    val state: StateFlow<State>
}

interface IView<State: IState> {
    fun render(state: State)
}
