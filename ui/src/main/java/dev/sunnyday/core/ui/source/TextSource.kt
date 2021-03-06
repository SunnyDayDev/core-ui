package dev.sunnyday.core.ui.source

import android.content.Context
import androidx.annotation.StringRes

sealed class TextSource: Source<CharSequence> {

    data class Res(@StringRes val resId: Int): TextSource() {

        override fun get(context: Context): String = context.getString(resId)

    }

    data class ResWithFormat(@StringRes val resId: Int, val args: Array<out Any>): TextSource() {

        companion object {

            fun create(@StringRes resId: Int, vararg args: Any) =
                ResWithFormat(resId, args)

        }

        override fun get(context: Context): String = context.getString(resId, *args)

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ResWithFormat

            if (resId != other.resId) return false
            if (!args.contentEquals(other.args)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = resId
            result = 31 * result + args.contentHashCode()
            return result
        }

    }

    data class Raw(val value: String): TextSource() {

        override fun get(context: Context): String = value
    }

    companion object {

        @Suppress("NOTHING_TO_INLINE")
        inline operator fun invoke(raw: String): TextSource = Raw(raw)

        @Suppress("NOTHING_TO_INLINE")
        inline operator fun invoke(@StringRes res: Int): TextSource = Res(res)

        @Suppress("NOTHING_TO_INLINE")
        inline operator fun invoke(@StringRes res: Int, vararg args: Any): TextSource = ResWithFormat(res, args)

    }

}