package dev.sunnyday.core.util

/**
 * Created by Aleksandr Tcikin (SunnyDay.Dev) on 2019-04-05.
 * mail: mail@sunnydaydev.me
 */

infix fun Any?.equals(other: Any?): Boolean = this == other

infix fun Any?.equalsWithHashCode(other: Any?): Boolean = this.hashCode() == other.hashCode() && this == other

infix fun Any?.isSameObject(other: Any?): Boolean = this === other