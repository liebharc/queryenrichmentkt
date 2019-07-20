package com.github.liebharc.cachetable

import com.google.common.cache.Cache
import org.h2.command.dml.AllColumnsForPlan
import org.h2.engine.Database
import org.h2.engine.DbObject
import org.h2.engine.Session
import org.h2.index.*
import org.h2.result.Row
import org.h2.result.RowImpl
import org.h2.result.SearchRow
import org.h2.result.SortOrder
import org.h2.schema.Schema
import org.h2.table.Column
import org.h2.table.IndexColumn
import org.h2.table.Table
import org.h2.table.TableFilter
import org.h2.value.ValueLong
import org.h2.value.ValueString
import java.lang.StringBuilder
import java.util.ArrayList

class CacheTableIndex(val cache: Cache<Long, String>, val table: CacheTable) : Index {
    override fun getComment(): String {
        return "Cache Index"
    }

    override fun getId(): Int {
        return 0
    }

    override fun isFindUsingFullTableScan(): Boolean {
        return false
    }

    override fun getColumns(): Array<Column> {
        return columns
    }

    override fun getPlanSQL(): String {
        return "Cache index"
    }

    override fun truncate(session: Session?) {
    }

    override fun getRowCountApproximation(): Long {
        return cache.size()
    }

    override fun getCreateSQLForCopy(table: Table?, quotedName: String?): String {
        return "Cache index"
    }

    override fun findNext(session: Session?, higherThan: SearchRow?, last: SearchRow?): Cursor {
        TODO("not implemented") //To change
    }

    override fun rename(newName: String?) {
    }

    override fun compareRows(rowData: SearchRow?, compare: SearchRow?): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun close(session: Session?) {
    }

    override fun setSortedInsertMode(sortedInsertMode: Boolean) {
    }

    override fun getRowCount(session: Session?): Long {
        return cache.size()
    }

    override fun checkRename() {
    }

    override fun createLookupBatch(filters: Array<out TableFilter>?, filter: Int): IndexLookupBatch {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setComment(comment: String?) {
    }

    override fun find(session: Session?, first: SearchRow?, last: SearchRow?): Cursor {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun find(filter: TableFilter?, first: SearchRow?, last: SearchRow?): Cursor {
        if (first != null && last != null && first.getValue(0).`object` == last.getValue(0).`object` ) {
            val key = last.getValue(0).`object` as Int
            val longKey = key.toLong()
            return SingleRowCursor(RowImpl(arrayOf(ValueLong.get(longKey), ValueString.get(cache.getIfPresent(longKey)!!)), 0))
        }

        return CacheCursor(cache.asMap().entries.iterator())
    }

    override fun isTemporary(): Boolean {
        return false
    }

    override fun isRowIdIndex(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getDiskSpaceUsed(): Long {
        return cache.size()
    }

    override fun getName(): String {
        return "CacheIndex"
    }

    override fun canGetFirstOrLast(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getDatabase(): Database {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun canFindNext(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun update(session: Session?, oldRow: Row?, newRow: Row?) {
        // Cache updates are not supported
    }

    override fun add(session: Session?, row: Row?) {
        // Cache updates are not supported
    }

    override fun getCreateSQL(): String {
        return "Column index"
    }

    override fun getRow(session: Session?, key: Long): Row {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getCost(session: Session?, masks: IntArray?, filters: Array<out TableFilter>?, filter: Int, sortOrder: SortOrder?, allColumnsSet: AllColumnsForPlan?): Double {
        return 1.0
    }

    override fun isHidden(): Boolean {
        return false
    }

    override fun getSchema(): Schema {
        return table.schema
    }

    override fun getChildren(): ArrayList<DbObject> {
        return ArrayList()
    }

    override fun getDropSQL(): String {
        return "Drop caches is not allowed"
    }

    override fun isFirstColumn(column: Column?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getTable(): Table {
        return table
    }

    override fun getType(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun remove(session: Session?, row: Row?) {
    }

    override fun remove(session: Session?) {
    }

    override fun getSQL(alwaysQuote: Boolean): String {
        return "Column index"
    }

    override fun getSQL(builder: StringBuilder?, alwaysQuote: Boolean): StringBuilder {
        return builder!!
    }

    override fun needRebuild(): Boolean {
        return false
    }

    override fun removeChildrenAndResources(session: Session?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findFirstOrLast(session: Session?, first: Boolean): Cursor {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setTemporary(temporary: Boolean) {
    }

    override fun canScan(): Boolean {
        return false
    }

    override fun getColumnIndex(col: Column?): Int {
        return table.columns.indexOf(col)
    }

    override fun getIndexColumns(): Array<IndexColumn> {
        return IndexColumn.wrap(table.columns)
    }

    override fun getIndexType(): IndexType {
        return IndexType.createUnique(true, true)
    }

}