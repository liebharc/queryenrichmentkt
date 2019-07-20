package com.github.liebharc.cachetable;

import com.google.common.cache.Cache;
import kotlin.NotImplementedError;
import org.h2.command.dml.AllColumnsForPlan;
import org.h2.engine.Mode;
import org.h2.engine.Session;
import org.h2.index.*;
import org.h2.message.DbException;
import org.h2.result.Row;
import org.h2.result.RowImpl;
import org.h2.result.SearchRow;
import org.h2.result.SortOrder;
import org.h2.table.Column;
import org.h2.table.IndexColumn;
import org.h2.table.PageStoreTable;
import org.h2.table.TableFilter;
import org.h2.value.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class CacheTableHashIndex extends BaseIndex {

    /**
     * The index of the indexed column.
     */
    private final int indexColumn;
    private final CacheTable tableData;
    private final Cache<Long, String> cache;

    public CacheTableHashIndex(
            CacheTable table,
            int id,
            String indexName,
            IndexColumn[] columns,
            IndexType indexType,
            Cache<Long, String> cache) {
        super(table, id, indexName, columns, indexType);
        this.cache = cache;
        Column column = columns[0].column;
        indexColumn = column.getColumnId();
        this.tableData = table;
        reset();
    }

    private void reset() {
    }

    @Override
    public void truncate(Session session) {
        reset();
    }

    @Override
    public void add(Session session, Row row) {
        // Changes to the cache are not supported
    }

    @Override
    public void remove(Session session, Row row) {
        // Changes to the cache are not supported
    }

    @Override
    public Cursor find(Session session, SearchRow first, SearchRow last) {
        if (first == null || last == null) {
            return findAll(session);
        }
        Value v = first.getValue(indexColumn);
        if (v == ValueNull.INSTANCE
                && database.getMode().uniqueIndexNullsHandling != Mode.UniqueIndexNullsHandling.FORBID_ANY_DUPLICATES) {
            //return new NonUniqueHashCursor(session, tableData, nullRows);
            throw new NotImplementedError("");
        }
        /*
         * Sometimes the incoming search is a similar, but not the same type
         * e.g. the search value is INT, but the index column is LONG. In which
         * case we need to convert, otherwise the HashMap will not find the
         * result.
         */
        v = v.convertTo(tableData.getColumn(indexColumn).getType(), database.getMode(), null);
        final String value = cache.getIfPresent(v.getLong());
        return new SingleRowCursor(new RowImpl(new Value[] { v, ValueString.get(value) }, 1));
    }

    private Cursor findAll(Session session) {
        return new CacheCursor(cache.asMap().entrySet().iterator());
    }

    @Override
    public long getRowCount(Session session) {
        return getRowCountApproximation();
    }

    @Override
    public long getRowCountApproximation() {
        return cache.size();
    }

    @Override
    public long getDiskSpaceUsed() {
        return 0;
    }

    @Override
    public void close(Session session) {
        // nothing to do
    }

    @Override
    public void remove(Session session) {
        // nothing to do
    }

    @Override
    public double getCost(Session session, int[] masks,
                          TableFilter[] filters, int filter, SortOrder sortOrder,
                          AllColumnsForPlan allColumnsSet) {
        if (masks == null) {
            return 2;
        }

        for (Column column : columns) {
            int index = column.getColumnId();
            int mask = masks[index];
            if ((mask & IndexCondition.EQUALITY) != IndexCondition.EQUALITY) {
                return Long.MAX_VALUE;
            }
        }
        return 2;
    }

    @Override
    public void checkRename() {
        // ok
    }

    @Override
    public boolean needRebuild() {
        return false;
    }

    @Override
    public boolean canGetFirstOrLast() {
        return false;
    }

    @Override
    public Cursor findFirstOrLast(Session session, boolean first) {
        throw DbException.getUnsupportedException("HASH");
    }

    @Override
    public boolean canScan() {
        return true;
    }
}
