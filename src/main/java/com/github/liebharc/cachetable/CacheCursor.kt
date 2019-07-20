package com.github.liebharc.cachetable

import org.h2.index.Cursor
import org.h2.result.Row
import org.h2.result.RowImpl
import org.h2.result.SearchRow
import org.h2.result.SimpleRow
import org.h2.value.ValueLong
import org.h2.value.ValueString

class CacheCursor(val values: MutableIterator<MutableMap.MutableEntry<Long, String>>) : Cursor {

    private var current: MutableMap.MutableEntry<Long, String>? = null

    private var currentRow: Row? = null


    override fun next(): Boolean {
        current = if (values.hasNext()) { values.next() } else { null }
        if (current != null) {
            currentRow = RowImpl(arrayOf(ValueLong.get(current!!.key), ValueString.get(current!!.value)), 0)
            return true
        }

        return false
    }

    override fun getSearchRow(): SearchRow {
        return currentRow!!
    }

    override fun get(): Row {
        return currentRow!!
    }

    override fun previous(): Boolean {
        return false
    }

}