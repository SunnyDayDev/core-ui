package dev.sunnyday.core.mvi.spotify

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import io.reactivex.functions.Consumer

open class SubtypeEffectHandlerBuilder<F: Any, E: Any> {

    val builder: RxMobius.SubtypeEffectHandlerBuilder<F, E> = RxMobius.subtypeEffectHandler()

    inline fun <reified G : F> addTransformer(
            transformer: ObservableTransformer<G, E>) =
            apply { builder.addTransformer(G::class.java, transformer) }

    inline fun <reified G : F> addTransformer(
            noinline transformer: (Observable<G>) -> ObservableSource<E>) =
            addTransformer(effectsHandler(transformer))

    inline fun <reified G : F> addAction(
            crossinline block: () -> Unit,
            scheduler: Scheduler? = null) =
            apply {
                scheduler?.run { builder.addAction(G::class.java, { block() }, this) }
                        ?: builder.addAction(G::class.java) { block() }
            }

    inline fun <reified G : F> addConsumer(
            crossinline block: (G) -> Unit,
            scheduler: Scheduler? = null) =
            apply {
                scheduler?.run { builder.addConsumer(G::class.java, { block(it) }, this) }
                        ?: builder.addConsumer(G::class.java) { block(it) }
            }

    inline fun <reified G : F> addFunction(
            crossinline block: (G) -> E,
            scheduler: Scheduler? = null) =
            apply {
                scheduler?.run { builder.addFunction(G::class.java, { block(it) }, this) }
                        ?: builder.addFunction(G::class.java) { block(it) }
            }

    fun withFatalErrorHandler(block: (ObservableTransformer<out F, E>) -> Consumer<Throwable>) =
            apply { builder.withFatalErrorHandler { block(it) } }

    fun build(): ObservableTransformer<F, E> = builder.build()

}