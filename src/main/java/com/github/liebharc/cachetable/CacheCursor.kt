package com.github.liebharc.cachetable

import org.h2.index.Cursor
import org.h2.result.Row
import org.h2.result.RowImpl
import org.h2.result.SearchRow
import org.h2.result.SimpleRow
import org.h2.value.Value
import org.h2.value.ValueLong
import org.h2.value.ValueString

class CacheCursor(val values: MutableIterator<Array<Value>>) : Cursor {

    private var current: Array<Value>? = null

    private var currentRow: Row? = null


    override fun next(): Boolean {
        current = if (values.hasNext()) { values.next() } else { null }
        if (current != null) {
            currentRow = RowImpl(current, 0)
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