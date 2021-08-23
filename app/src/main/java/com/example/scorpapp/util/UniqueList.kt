package com.example.scorpapp.util

class UniqueList<E : Unique<T>, T> : ArrayList<E>() {

    private var set = hashSetOf<T>()

    override fun addAll(elements: Collection<E>): Boolean {
        val filtered = filterElements(elements)
        trackElements(filtered)
        return super.addAll(filtered)
    }

    override fun addAll(index: Int, elements: Collection<E>): Boolean {
        val filtered = filterElements(elements)
        trackElements(filtered)
        return super.addAll(index, filtered)
    }

    override fun clear() {
        set.clear()
        super.clear()
    }

    private fun trackElements(filtered: List<E>) {
        val filteredSet = filtered.map { it.id }.toSet()

        require(filteredSet.size == filtered.size) {
            "Elements must be unique according to their id"
        }

        set.addAll(filteredSet)
    }
    private fun filterElements(elements: Collection<E>): List<E> {
        return elements.distinctBy { it.id }.filterNot { set.contains(it.id) }
    }
}