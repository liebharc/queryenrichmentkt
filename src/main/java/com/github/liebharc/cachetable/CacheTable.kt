package com.github.liebharc.cachetable


import com.google.common.cache.Cache
import org.h2.command.ddl.CreateTableData
import org.h2.engine.Session
import org.h2.index.Index
import org.h2.index.IndexType
import org.h2.result.Row
import org.h2.table.IndexColumn
import org.h2.table.Table
import org.h2.table.TableType
import java.util.*
import kotlin.reflect.KClass

class CacheTable(data: CreateTableData) : Table(data.schema, data.id, data.tableName, false, false) {

    companion object {
        private val caches: MutableMap<Any, CacheMetaInfo<out Any, out Any>> = HashMap()

        fun<K: Any, V: Any> register(keyClass: KClass<K>, valueClass: KClass<V>, name: String, cache: Cache<K, V>) {
            caches.put(name, CacheMetaInfo(keyClass, valueClass, cache))
        }

        fun<K: Any, V: Any> find(name: String, keyClass: KClass<K>, valueClass: KClass<V>): Cache<K, V> {
            val metaInfo = caches.get(name)!!
            return metaInfo.cache as Cache<K, V>;
        }
    }

    private val cache: Cache<Long, String> = find(data.tableName, Long::class, String::class)

    init {
        setColumns(data.columns.toTypedArray())
    }

    private val index = CacheTableHashIndex(
            this,
            0,
            "Cache",
            IndexColumn.wrap(arrayOf(data.columns[0])),
            IndexType.createUnique(false, true),
            cache)

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