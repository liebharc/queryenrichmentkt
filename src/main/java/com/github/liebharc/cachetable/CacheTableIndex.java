package com.github.liebharc.cachetable;

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
import org.h2.table.TableFilter;
import org.h2.value.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CacheTableIndex extends BaseIndex {

    /**
     * The index of the indexed column.
     */
    private final List<Column> indexColumn;
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
        indexColumn =
                Arrays.stream(columns)
                        .limit(metaInfo.getNumberOfIndexColumns())
                        .map(column -> column.column)
                        .collect(Collectors.toList());
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
        // Changes to the metaInfo are not supported
    }

    @Override
    public void remove(Session session, Row row) {
        // Changes to the metaInfo are not supported
    }

    @Override
    public Cursor find(Session session, SearchRow first, SearchRow last) {
        if (first == null || last == null) {
            return findAll(session);
        }

        List<Value> firstValue = getValue(first);
        List<Value> lastValue = getValue(last);

        if (firstValue.equals(lastValue)) {
            return findSingleValue(session, firstValue);
        }

        return findRangeValue(session, firstValue, lastValue);
    }

    private List<Value> getValue(SearchRow row) {
        return indexColumn.stream()
                .map(col -> {
                    Value value = row.getValue(col.getColumnId());
                    return value != null ? value.convertTo(col.getType(), database.getMode(), null) : null;
                })
                .collect(Collectors.toList());
    }

    @NotNull
    private Cursor findSingleValue(Session session, List<Value> firstValue) {
        if (firstValue == ValueNull.INSTANCE
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
        List<Value[]> values = metaInfo.getRowOrNull(firstValue, session);
        if (values == null) {
            return new CacheCursor(emptyResult.iterator());
        }
        return new CacheCursor(values.iterator());
    }

    private Cursor findRangeValue(Session session, List<Value> firstValue, List<Value> lastValue) {
        return new CacheCursor(metaInfo.getRowsInRange(session, firstValue, lastValue));
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
