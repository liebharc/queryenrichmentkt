package com.github.liebharc.cachetable


import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import org.h2.command.ddl.CreateTableData
import org.h2.engine.Session
import org.h2.index.Index
import org.h2.index.IndexType
import org.h2.result.Row
import org.h2.table.IndexColumn
import org.h2.table.Table
import org.h2.table.TableType
import java.util.*

class CacheTable(data: CreateTableData) : Table(data.schema, data.id, data.tableName, false, false) {

    private val cache: Cache<Long, String> = CacheBuilder.newBuilder().build<Long, String>()

    init {
        setColumns(data.columns.toTypedArray())
        cache.put(1, "Test")
        cache.put(2, "Test2")
    }

    private val index = CacheTableIndex(cache, this)

    private val indexes = ArrayList<Index>(Arrays.asList(index))

    override fun lock(session: Session?, exclusive: Boolean, forceLockEvenInMvcc: Boolean): Boolean {
        // Can't lock
        return false
    }

    override fun close(session: Session?) {
        // Nothing to close
    }

    override fun checkRename() {
    }

    override fun unlock(s: Session?) {
        // Can't lock
    }

    override fun addRow(session: Session?, row: Row?) {
        // Can't add row
    }

    override fun getTableType(): TableType {
        // Linked table sounds the closest to what we try to mimic
        return TableType.TABLE_LINK
    }

    override fun getDiskSpaceUsed(): Long {
        return cache.size()
    }

    override fun isDeterministic(): Boolean {
        return false
    }

    override fun removeRow(session: Session?, row: Row?) {
        // Can't remove row
    }

    override fun checkSupportAlter() {
    }

    override fun truncate(session: Session?) {
    }

    override fun getRowCountApproximation(): Long {
        return cache.size()
    }

    override fun isLockedExclusively(): Boolean {
        return false
    }

    override fun getMaxDataModificationId(): Long {
        return 0
    }

    override fun getRowCount(session: Session?): Long {
        return cache.size()
    }

    override fun canGetRowCount(): Boolean {
        return true
    }

    override fun getCreateSQL(): String {
        return "Select from cache"
    }

    override fun getDropSQL(): String {
        return ""
    }

    override fun canDrop(): Boolean {
        return false
    }

    override fun getIndexes(): ArrayList<Index> {
        return indexes
    }

    override fun getUniqueIndex(): Index {
        return index
    }

    override fun getScanIndex(session: Session?): Index {
        return index
    }

    override fun addIndex(session: Session?, indexName: String?, indexId: Int, cols: Array<out IndexColumn>?, indexType: IndexType?, create: Boolean, indexComment: String?): Index {
        return index
    }
}