package dev.sunnyday.core.mvvm.observable

import androidx.databinding.BaseObservable
import dev.sunnyday.core.util.Wrapped
import timber.log.Timber

/**
 * Created by sunny on 31.05.2018.
 * mail: mail@sunnyday.dev
 */

open class BaseCommand<T>: BaseObservable() {

    protected var event: Wrapped<T>? = null

    protected fun internalFire(event: T) {
        synchronized(this) {
            this.event = Wrapped(event)
        }
        notifyChange()
    }

    protected inline fun internalHandle(handleAction: (T) -> Boolean) {
        synchronized(this) {
            val event = this.event
            if (event != null && handleAction(event.value)) {
                this.event = null
            }
        }
    }

}

class Command<T>: BaseCommand<T>() {

    fun fire(event: T) = internalFire(event)

    fun handle(action: (T) -> Unit) = internalHandle {
        action(it)
        true
    }

    companion object {

        fun pure() = Command<Unit>()

    }

}

class TargetedCommand<E, T: Any>: BaseCommand<Pair<E, T>>() {

    fun fire(event: E, target: T) = internalFire(event to target)

    fun handle(target: T, action: (E) -> Unit) = internalHandle {
        if (target == it.second) {
            action(it.first)
            true
        } else {
            false
        }
    }

    companion object {

        fun <T: Any> pure() = TargetedCommand<Unit, T>()

    }

}

class CommandForResult<T, R>(private val defaultValue: R) {

    private var resolver: ((T) -> R)? = null

    fun fire(value: T): R {
        val resolve = this.resolver ?: { defaultValue }
        return resolve(value)
    }

    fun handle(action: (T) -> R) {
        if (resolver != null) {
            Timber.d("Already have command resolver. It will be overriden.")
        }
        resolver = action
    }

    companion object {

        fun <R> pure(defaultValue: R) = CommandForResult<Unit, R>(defaultValue)

    }

}

operator fun <T> Command<T>.invoke(event: T) = fire(event)
operator fun Command<Unit>.invoke() = fire(Unit)

operator fun <E, T: Any> TargetedCommand<E, T>.invoke(event: E, target: T) = fire(event, target)
operator fun <T: Any> TargetedCommand<Unit, T>.invoke(target: T) = fire(Unit, target)

operator fun <T, R> CommandForResult<T, R>.invoke(value: T) = fire(value)
operator fun <R> CommandForResult<Unit, R>.invoke() = fire(Unit)