package com.github.liebharc.cachetable;

import org.h2.command.ddl.CreateTableData;
import org.h2.engine.Session;
import org.h2.message.DbException;
import org.h2.table.Column;
import org.h2.value.DataType;
import org.h2.value.Value;
import org.h2.value.ValueLong;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CombinedCacheMetaData {
    private final static String REF_NAME = "REF";
    private final ICacheMetaInfo cacheMetaInfo;
    private final List<Column> indexColumns;
    private final List<Function<? super Object, ?>> columnToMethod;
    private final List<Column> allColumns;
    private final boolean expectListResults;
    private final ValueLong one = ValueLong.get(1);

    public CombinedCacheMetaData(CreateTableData tableData, ICacheMetaInfo cacheMetaInfo) {
        this.cacheMetaInfo = cacheMetaInfo;
        expectListResults = cacheMetaInfo.getNumberOfIndexColumns() > 1;
        allColumns = new ArrayList<>(tableData.columns);
        final int numberOfCols = cacheMetaInfo.getNumberOfIndexColumns();
        indexColumns = allColumns.stream().limit(numberOfCols).collect(Collectors.toList());
        List<Column> objectColumns = new ArrayList<>(allColumns);
        for (int i = 0; i < numberOfCols; i++) {
            objectColumns.remove(0);
        }

        if (objectColumns.size() > 1) {
            Map<String, Method> nameToMethod = Arrays.stream(cacheMetaInfo.getValue().getMethods())
                    .filter(method -> method.getName().startsWith("get") && method.getParameterCount() == 0)
                    .collect(Collectors.toMap(method -> method.getName().toUpperCase().substring(3), method -> method));
            columnToMethod = new ArrayList<>(allColumns.size());
            for (int i = 0; i < allColumns.size(); i++) {
                Column col = allColumns.get(i);
                if (col.getName().equals(REF_NAME)) {
                    continue;
                }

                final String colName = col.getName().toUpperCase();
                Function<? super Object, ?> getter = cacheMetaInfo.createFieldAccessor(colName);
                if (getter == null) {
                    Method method = nameToMethod.get(colName);
                    if (method == null) {
                        throw new IllegalArgumentException("Unknown column: " + colName);
                    }

                    getter = obj -> {
                        try {
                            return method.invoke(obj);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            DbException.throwInternalError(e.getMessage());
                        }

                        return null;
                    };
                }

                columnToMethod.add(getter);
            }
        }
        else {
            columnToMethod = null;
        }
    }

    private Object getFieldValue(Column column, Object entry) {
        if (column.getName().equals(REF_NAME)) {
            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream out = new ObjectOutputStream(bos);
                out.writeObject(entry);
                out.close();
                return bos.toByteArray();
            } catch (IOException e) {
                DbException.throwInternalError(e.getMessage());
            }
        }

        if (columnToMethod == null) {
            if (column == indexColumns) {
                return entry;
            }

            return entry;
        }

        Function<? super Object, ?> getter = columnToMethod.get(column.getColumnId());
        if (getter == null) {
            DbException.throwInternalError("Unknown column " + column);
        }
        return getter.apply(entry);
    }

    private Value getAndConvertFieldValue(Session session, Column column, Object entry) {
        Object columnValue = this.getFieldValue(column, entry);
        return convertValue(session, column, columnValue);
    }

    private Value convertValue(Session session, Column column, Object columnValue) {
        return DataType.convertToValue(session, columnValue, column.getType().getValueType());
    }

    public List<Value[]> getRowOrNull(Value[] indexValue, Session session) {
        final Object[] indexRawValue = new Object[cacheMetaInfo.getNumberOfIndexColumns()];
        for (int i = 0; i < cacheMetaInfo.getNumberOfIndexColumns(); i++) {
            final Value idxVal = indexValue[i];
            indexRawValue[i] = idxVal != null ? idxVal.getObject() : null;
        }

        final Object result = cacheMetaInfo.getValueOrNull(indexRawValue);

        if (expectListResults) {
            return returnListValue(indexValue, session, indexRawValue, (List<Object>)result);
        }

        Value[] singleResult = returnSingleValue(indexValue, session, indexRawValue, result);
        return singleResult == null ? Collections.emptyList() : Collections.singletonList(singleResult);
    }

    private List<Value[]> returnListValue(Value[] indexValue, Session session, Object[] indexRawValue, List<Object> entries) {
        final List<Value[]> results = new ArrayList<>();
        for (Object entry : entries) {
            Value[] values = returnSingleValue(indexValue, session, indexRawValue, entry);
            if (values != null) {
                results.add(values);
            }
        }

        return results;
    }

    private Value[] returnSingleValue(Value[] indexValue, Session session, Object[] indexRawValue, Object entry) {
        if (entry == null) {
            return null;
        }

        Value[] values = new Value[allColumns.size()];
        for (int i = 0; i < indexRawValue.length; i++) {
            values[i] = indexValue[i];
            if (values[i] == null) {
                values[i] = this.getAndConvertFieldValue(session, indexColumns.get(i), entry);
            }
        }
        for (int i = cacheMetaInfo.getNumberOfIndexColumns(); i < values.length; i++) {
            Column column = allColumns.get(i);
            values[i] = this.getAndConvertFieldValue(session, column, entry);
        }
        return values;
    }

    Iterator<Value[]> getAllRows(Session session) {
        List<Column> columns = allColumns;
        int columnsSize = allColumns.size();
        final int indexColumnsCnt = cacheMetaInfo.getNumberOfIndexColumns();
        final boolean simpleKey = indexColumnsCnt == 1;
        return cacheMetaInfo.getAllValues().map(entry -> {
            Value[] values = new Value[columnsSize];
            for (int i = 0; i < indexColumnsCnt; i++) {
                values[i] =
                        simpleKey
                            ? this.convertValue(session, indexColumns.get(i), entry.getKey())
                            : this.getAndConvertFieldValue(session, indexColumns.get(i), entry.getValue());
            }

            for (int i = indexColumnsCnt; i < values.length; i++) {
                Column column = columns.get(i);
                values[i] = this.getAndConvertFieldValue(session, column, entry.getValue());
            }

            return values;
        }).iterator();
    }

    List<Value[]> getRowsInRange(Session session, Value[] firstValue, Value[] lastValue) {
        final List<Value[]> result = new ArrayList<>();
        getRowsInRangeRec(
                session,
                result,
                firstValue.clone(),
                firstValue,
                lastValue,
                0);
        return result;
    }

    /**
     * Implements a nested loop which for each level iterates from lower bound to upper bound (inclusive). If
     * lower or upper bound is null then this is unbounded (to one or both sides) and we instead return all values
     * on that cache level.
     */
    void getRowsInRangeRec(Session session, List<Value[]> result, Value[] counters, Value[] lowerBound, Value[] upperBound, int level) {
        if(level == counters.length) {
            List<Value[]> row = this.getRowOrNull(counters, session);
            if (row != null) {
                result.addAll(row);
            }
        }
        else {
            if (lowerBound[level]== null || upperBound[level] == null) {
                counters[level] = null; // Unbounded query
                getRowsInRangeRec(session, result, counters, lowerBound, upperBound, level + 1);
                return;
            }

            counters[level] = lowerBound[level];
            while (!counters[level].equals(upperBound[level])) {
                getRowsInRangeRec(session, result, counters, lowerBound, upperBound, level + 1);
                counters[level] = counters[level].add(one);
            }

                // One more recursive call as the upper bound should be included for all but the lowest level
                getRowsInRangeRec(session, result, counters, lowerBound, upperBound, level + 1);
        }
    }

    public long getRowSize() {
        return cacheMetaInfo.size();
    }

    public int getNumberOfIndexColumns() {
        return cacheMetaInfo.getNumberOfIndexColumns();
    }
}
