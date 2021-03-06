package dev.sunnyday.core.mvvm.observable

import androidx.databinding.ListChangeRegistry
import androidx.databinding.ObservableList
import dev.sunnyday.core.mvvm.util.notSupportedOperation

/**
 * Created by sunny on 07.06.2018.
 * mail: mail@sunnyday.dev
 */

class MergedMVVMList<T>(vararg lists: MVVMList<out T>): ImmutableMVVMList<T> {

    @Suppress("UNCHECKED_CAST")
    private val lists: List<MVVMList<T>> = lists.map { it as MVVMList<T> }

    @Transient
    private var listeners: ListChangeRegistry = ListChangeRegistry()

    private val iteratorDelegate get() = IteratorDelegate()

    private val childOnListChangedCallback = ChildOnListChangedCallback()

    init {
        this.lists.forEach {
            it.addOnListChangedCallback(childOnListChangedCallback)
        }
    }

    override val size: Int get() = lists.sumBy { it.size }

    override fun contains(element: T): Boolean = lists.any { it.contains(element) }

    override fun containsAll(elements: Collection<T>): Boolean = elements.all { contains(it) }

    override fun get(index: Int): T {
        return lists.find {
            val offset = getIndexOffset(it)
            index >= offset && index < offset + it.size
        } ?.let {
            val checkedIndex = index - getIndexOffset(it)
            it[checkedIndex]
        } ?: throw IndexOutOfBoundsException("Index: $index, size: $size")
    }

    override fun indexOf(element: T): Int = lists.find {  it.contains(element) }
            ?.let { it.indexOf(element) + getIndexOffset(it) } ?: -1

    override fun lastIndexOf(element: T): Int = lists.findLast {  it.contains(element) }
            ?.let { it.indexOf(element) + getIndexOffset(it) } ?: -1

    override fun isEmpty(): Boolean = lists.all { it.isEmpty() }

    override fun addOnListChangedCallback(listener: ObservableList.OnListChangedCallback<out ObservableList<T>>) {
        listeners.add(listener)
    }

    override fun removeOnListChangedCallback(listener: ObservableList.OnListChangedCallback<out ObservableList<T>>) {
        listeners.remove(listener)
    }

    override fun iterator() = MVVMIterator(iteratorDelegate)

    override fun listIterator() = MVVMListIterator(iteratorDelegate, -1)

    override fun listIterator(index: Int) = MVVMListIterator(iteratorDelegate, index)

    private fun getIndexOffset(list: MVVMList<T>): Int {
        val listIndex = lists.indexOfFirst { it === list }
        return (0 until listIndex).sumBy { lists[it].size }
    }

    class Builder<T> {

        private val lists = mutableListOf<MVVMList<out T>>()

        fun add(list: MVVMList<out T>): Builder<T> = this.also {
            lists.add(list)
        }

        fun add(optionalItem: OptionalMVVMListItem<out T>): Builder<T> = this.also {
            lists.add(optionalItem.list)
        }

        fun add(item: T): Builder<T> = this.also {
            lists.add(MVVMArrayList(item))
        }

        fun build(): MergedMVVMList<T> = MergedMVVMList(*lists.toTypedArray())

    }

    private inner class ChildOnListChangedCallback
        : ObservableList.OnListChangedCallback<MVVMList<T>>() {

        override fun onChanged(sender: MVVMList<T>) {
            if(sender === this@MergedMVVMList) return

            listeners.notifyChanged(this@MergedMVVMList)
        }

        override fun onItemRangeRemoved(sender: MVVMList<T>, positionStart: Int, itemCount: Int) {
            if(sender === this@MergedMVVMList) return

            listeners.notifyRemoved(
                this@MergedMVVMList,
                getFixedIndex(sender, positionStart),
                itemCount
            )
        }

        override fun onItemRangeMoved(sender: MVVMList<T>, fromPosition: Int, toPosition: Int, itemCount: Int) {
            if(sender === this@MergedMVVMList) return

            listeners.notifyMoved(
                this@MergedMVVMList,
                getFixedIndex(sender, fromPosition),
                getFixedIndex(sender, toPosition),
                itemCount
            )
        }

        override fun onItemRangeInserted(sender: MVVMList<T>, positionStart: Int, itemCount: Int) {
            if(sender === this@MergedMVVMList) return

            listeners.notifyInserted(
                this@MergedMVVMList,
                getFixedIndex(sender, positionStart),
                itemCount
            )
        }

        override fun onItemRangeChanged(sender: MVVMList<T>, positionStart: Int, itemCount: Int) {
            if(sender === this@MergedMVVMList) return

            listeners.notifyChanged(
                this@MergedMVVMList,
                getFixedIndex(sender, positionStart),
                itemCount
            )
        }

        private fun getFixedIndex(list: MVVMList<T>, index: Int): Int = getIndexOffset(list) + index

    }

    inner class IteratorDelegate: MVVMListIterator.Delegate<T> {

        override val size = this@MergedMVVMList.size

        override fun get(index: Int) = this@MergedMVVMList[index]

        override fun remove(index: Int) = notSupportedOperation()

        override fun set(index: Int, value: T) = notSupportedOperation()

        override fun add(index: Int, element: T) = notSupportedOperation()

    }

}