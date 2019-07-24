package com.github.liebharc.cachetable;

import org.h2.command.dml.AllColumnsForPlan;
import org.h2.engine.Session;
import org.h2.index.*;
import org.h2.message.DbException;
import org.h2.result.Row;
import org.h2.result.RowImpl;
import org.h2.result.SearchRow;
import org.h2.result.SortOrder;
import org.h2.table.Column;
import org.h2.table.IndexColumn;
import org.h2.table.TableFilter;
import org.h2.value.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CacheTableIndex extends BaseIndex {

    /**
     * The index of the indexed column.
     */
    private final Column[] indexColumn;
    private final CombinedCacheMetaData metaInfo;
    private final static ArrayList<Value[]> emptyResult = new ArrayList<>();

    public CacheTableIndex(
            CacheTable table,
            int id,
            String indexName,
            IndexColumn[] columns,
            IndexType indexType,
            CombinedCacheMetaData metaInfo) {
        super(table, id, indexName, columns, indexType);
        this.metaInfo = metaInfo;
        indexColumn = new Column[metaInfo.getNumberOfIndexColumns()];
        for (int i = 0; i < indexColumn.length; i++) {
            indexColumn[i] = columns[i].column;
        }
    }

    @Override
    public void truncate(Session session) {
        // Changes aren't supported
    }

    @Override
    public void add(Session session, Row row) {
        // Changes aren't supported
    }

    @Override
    public void remove(Session session, Row row) {
        // Changes aren't supported
    }

    @Override
    public Cursor find(Session session, SearchRow first, SearchRow last) {
        if (first == null || last == null) {
            return findAll(session);
        }

        Value[] firstValue = getValue(first);
        Value[] lastValue = getValue(last);

        if (Arrays.equals(firstValue, lastValue)) {
            return findSingleValue(session, firstValue);
        }

        return findRangeValue(session, firstValue, lastValue);
    }

    private Value[] getValue(SearchRow row) {
        Value[] result = new Value[indexColumn.length];
        for (int i = 0; i < result.length; i++) {
            Column col = indexColumn[i];
            Value value = row.getValue(col.getColumnId());
            result[i] = value != null ? value.convertTo(col.getType(), database.getMode(), null) : null;
        }

        return result;
    }

    @NotNull
    private Cursor findSingleValue(Session session, Value[] firstValue) {
        /*
         * Sometimes the incoming search is a similar, but not the same type
         * e.g. the search value is INT, but the index column is LONG. In which
         * case we need to convert, otherwise the HashMap will not find the
         * result.
         */
        List<Value[]> values = metaInfo.getRowOrNull(firstValue, session);
        return createCursor(values);
    }

    private Cursor createCursor(List<Value[]> values) {
        if (values == null) {
            return new CacheCursor(emptyResult.iterator());
        }

        if (values.size() == 1) {
            return new SingleRowCursor(new RowImpl(values.get(0), 0));
        }

        return new CacheCursor(values.iterator());
    }

    private Cursor findRangeValue(Session session, Value[] firstValue, Value[] lastValue) {
        return new CacheCursor(metaInfo.getRowsInRange(session, firstValue, lastValue).iterator());
    }

    private Cursor findAll(Session session) {
        return new CacheCursor(metaInfo.getAllRows(session));
    }

    @Override
    public long getRowCount(Session session) {
        return getRowCountApproximation();
    }

    @Override
    public long getRowCountApproximation() {
        return metaInfo.getRowSize();
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
