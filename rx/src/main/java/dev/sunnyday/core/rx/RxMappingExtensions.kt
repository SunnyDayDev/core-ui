package dev.sunnyday.core.rx

import dev.sunnyday.core.util.Optional
import io.reactivex.*
import io.reactivex.functions.Function
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by Aleksandr Tcikin (SunnyDay.Dev) on 02.08.2018.
 * mail: mail@sunnyday.dev
 */

// region: Map list

fun <T, R> Flowable<out Iterable<T>>.mapList(mapper: (T) -> R): Flowable<List<R>> = map { it.map(mapper) }

fun <T, R> Observable<out Iterable<T>>.mapList(mapper: (T) -> R): Observable<List<R>> = map { it.map(mapper) }

fun <T, R> Single<out Iterable<T>>.mapList(mapper: (T) -> R): Single<List<R>> = map { it.map(mapper) }

fun <T, R> Maybe<out Iterable<T>>.mapList(mapper: (T) -> R): Maybe<List<R>> = map { it.map(mapper) }

fun <T, C: MutableCollection<in R>, R> Flowable<out Iterable<T>>.mapListTo(collection: C, mapper: (T) -> R): Flowable<C> = map { it.mapTo(collection, mapper) }

fun <T, C: MutableCollection<in R>, R> Observable<out Iterable<T>>.mapListTo(collection: C, mapper: (T) -> R): Observable<C> = map { it.mapTo(collection, mapper) }

fun <T, C: MutableCollection<in R>, R> Single<out Iterable<T>>.mapListTo(collection: C, mapper: (T) -> R): Single<C> = map { it.mapTo(collection, mapper) }

fun <T, C: MutableCollection<in R>, R> Maybe<out Iterable<T>>.mapListTo(collection: C, mapper: (T) -> R): Maybe<C> = map { it.mapTo(collection, mapper) }

// endregion

// region: Map optional

fun <T, R> Flowable<Optional<T>>.map(mapper: (T) -> R): Flowable<Optional<R>> =
    map { (source) -> Optional(source?.let(mapper)) }

fun <T, R> Observable<Optional<T>>.map(mapper: (T) -> R): Observable<Optional<R>> =
    map { (source) -> Optional(source?.let(mapper)) }

fun <T, R> Single<Optional<T>>.map(mapper: (T) -> R): Single<Optional<R>> =
    map { (source) -> Optional(source?.let(mapper)) }

fun <T, R> Maybe<Optional<T>>.map(mapper: (T) -> R): Maybe<Optional<R>> =
    map { (source) -> Optional(source?.let(mapper)) }

// endregion

// region: Cast

inline fun <reified T> Flowable<out T>.cast(): Flowable<T> = cast(T::class.java)

inline fun <reified T> Observable<out T>.cast(): Observable<T> = cast(T::class.java)

inline fun <reified T> Single<out T>.cast(): Single<T> = cast(T::class.java)

inline fun <reified T> Maybe<out T>.cast(): Maybe<T> = cast(T::class.java)

// endregion

// region: Map to signal

fun <T> Flowable<T>.mapToSignal(predicate: (T) -> Boolean): Flowable<Unit> =
        filter(predicate).map { Unit }

fun <T> Flowable<T>.mapToSignal(): Flowable<Unit> = map { Unit }

fun Flowable<Boolean>.mapToSignal(filterValue: Boolean): Flowable<Unit> =
        filter{ it == filterValue } .map { Unit }

fun <T> Observable<T>.mapToSignal(predicate: (T) -> Boolean): Observable<Unit> =
        filter(predicate).map { Unit }

fun <T> Observable<T>.mapToSignal(): Observable<Unit> = map { Unit }

fun Observable<Boolean>.mapToSignal(acceptableValue: Boolean): Observable<Unit> =
        filter{ it == acceptableValue } .map { Unit }

fun <T> Single<T>.mapToSignal(predicate: (T) -> Boolean): Maybe<Unit> =
        filter(predicate).map { Unit }

fun <T> Single<T>.mapToSignal(): Single<Unit> = map { Unit }

fun Single<Boolean>.mapToSignal(filterValue: Boolean): Maybe<Unit> =
        filter{ it == filterValue } .map { Unit }

fun <T> Maybe<T>.mapToSignal(predicate: (T) -> Boolean): Maybe<Unit> =
        filter(predicate).map { Unit }

fun <T> Maybe<T>.mapToSignal(): Maybe<Unit> = map { Unit }

fun Maybe<Boolean>.mapToSignal(filterValue: Boolean): Maybe<Unit> =
        filter{ it == filterValue } .map { Unit }

// endregion

// region: throttleMap

fun <T: Any, R: Any> Observable<T>.throttleMap(map: (T) -> ObservableSource<out R>): Observable<R> {

    val mapping = AtomicBoolean(false)

    return flatMap {

        if (mapping.getAndSet(true)) {

            Observable.empty<R>()

        } else {

            Observable.just(it)
                .flatMap(map)
                .doFinally { mapping.set(false) }

        }

    }

}

fun <T: Any, R: Any> Observable<T>.throttleMapSingle(map: (T) -> SingleSource<out R>): Observable<R> {

    val mapping = AtomicBoolean(false)

    return flatMap {

        if (mapping.getAndSet(true)) {

            Observable.empty()

        } else {

            Single.defer { map(it) }
                .doFinally { mapping.set(false) }
                .toObservable()

        }

    }

}

fun <T: Any, R: Any> Observable<T>.throttleMapMaybe(map: (T) -> MaybeSource<out R>): Observable<R> {

    val mapping = AtomicBoolean(false)

    return flatMapMaybe {

        if (mapping.getAndSet(true)) {

            Maybe.empty()

        } else {

            Maybe.defer { map(it) }
                .doFinally { mapping.set(false) }

        }

    }

}

fun <T> Observable<T>.throttleMapCompletable(map: (T) -> CompletableSource): Completable {

    val mapping = AtomicBoolean(false)

    return flatMapCompletable {

        if (mapping.getAndSet(true)) {

            Completable.complete()

        } else {

            Completable.defer { map(it) }
                .doFinally { mapping.set(false) }

        }

    }

}

fun <T: Any, R: Any> Observable<T>.throttleMap(map: Function<in T, out ObservableSource<out R>>): Observable<R> =
    throttleMap(map::apply)

fun <T: Any, R: Any> Observable<T>.throttleMapSingle(map: Function<in T, out SingleSource<out R>>): Observable<R> =
    throttleMapSingle(map::apply)

fun <T: Any, R: Any> Observable<T>.throttleMapMaybe(map: Function<in T, out MaybeSource<out R>>): Observable<R> =
    throttleMapMaybe(map::apply)

fun <T: Any> Observable<T>.throttleMapCompletable(map: Function<in T, out CompletableSource>): Completable =
    throttleMapCompletable(map::apply)

// endregion

// region: Map error

fun Completable.mapError(mapper: (Throwable) -> Throwable): Completable = onErrorResumeNext {
    Completable.error(mapper(it))
}

fun <T> Maybe<T>.mapError(
        mapper: (Throwable) -> Throwable
): Maybe<T> = onErrorResumeNext { e: Throwable ->
    Maybe.error(mapper(e))
}

fun <T> Single<T>.mapError(mapper: (Throwable) -> Throwable): Single<T> = onErrorResumeNext {
    Single.error(mapper(it))
}

fun <T> Observable<T>.mapError(
        mapper: (Throwable) -> Throwable
): Observable<T> = onErrorResumeNext { e: Throwable ->
    Observable.error<T>(mapper(e))
}

fun <T> Flowable<T>.mapError(
        mapper: (Throwable) -> Throwable
): Flowable<T> = onErrorResumeNext { e: Throwable ->
    Flowable.error<T>(mapper(e))
}

// endregion

// region : Map not null

fun <T: Any, R: Any> Observable<T>.mapNotNull(mapper: (T) -> R?): Observable<R> =
        flatMap {  value ->
            mapper(value)?.let { Observable.just(it) } ?: Observable.empty()
        }

fun <T: Any, R: Any> Single<T>.mapNotNull(mapper: (T) -> R?): Maybe<R> =
        flatMapMaybe {  value ->
            mapper(value)?.let { Maybe.just(it) } ?: Maybe.empty()
        }

inline fun <reified R: Any> Observable<*>.filter(): Observable<R> =
        mapNotNull { it as? R }

// endregion

inline fun <T> acceptUnit(crossinline creator: () -> T): (Unit) -> T = { creator() }