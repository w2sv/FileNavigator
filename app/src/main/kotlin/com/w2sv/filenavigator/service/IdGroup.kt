package com.w2sv.filenavigator.service

import java.util.PriorityQueue

open class IdGroup(baseSeed: Int) : PriorityQueue<Int>() {

    private val idBase: Int = baseSeed * 1000

    fun addNewId(): Int =
        getNewId()
            .also {
                add(it)
            }

    fun addMultipleNewIds(n: Int): ArrayList<Int> =
        ArrayList((0 until n)
            .map { addNewId() })

    private fun getNewId(): Int =
        lastOrNull()?.let { it + 1 }
            ?: idBase
}