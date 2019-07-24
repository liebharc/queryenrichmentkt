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
import java.util.stream.Collectors;

public class CombinedCacheMetaData {
    private final static String REF_NAME = "REF";
    private final ICacheMetaInfo cacheMetaInfo;
    private final List<Column> indexColumns;
    private final List<Column> objectColumns;
    private final IdentityHashMap<Column, Method> columnToMethod;
    private final List<Column> allColumns;

    public CombinedCacheMetaData(CreateTableData tableData, ICacheMetaInfo cacheMetaInfo) {
        this.cacheMetaInfo = cacheMetaInfo;
        allColumns = new ArrayList<>(tableData.columns);
        final int numberofCols = cacheMetaInfo.getNumberOfIndexColumns();
        indexColumns = allColumns.stream().limit(numberofCols).collect(Collectors.toList());
        objectColumns = new ArrayList<>(allColumns);
        for (long i = 0; i < numberofCols; i++) {
            objectColumns.remove(0);
        }

        if (objectColumns.size() > 1) {
            Map<String, Method> nameToMethod = Arrays.stream(cacheMetaInfo.getValue().getMethods())
                    .filter(method -> method.getName().startsWith("get") && method.getParameterCount() == 0)
                    .collect(Collectors.toMap(method -> method.getName().toUpperCase().substring(3), method -> method));
            columnToMethod = new IdentityHashMap<>();
            allColumns.stream()
                    .filter(col -> !col.getName().equals(REF_NAME))
                    .forEach(col -> {
                        columnToMethod.put(col, nameToMethod.get(col.getName().toUpperCase()));
                    });
        }
        else {
            columnToMethod = null;
        }
    }

    public Object getFieldValue(Column column, Object entry) {
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

        try {
            Method getter = columnToMethod.get(column);
            if (getter == null) {
                DbException.throwInternalError("Unknown column " + column);
            }
            return getter.invoke(entry);
        } catch (IllegalAccessException e) {
            DbException.throwInternalError(e.getMessage());
        } catch (InvocationTargetException e) {
            DbException.throwInternalError(e.getMessage());
        }

        return null;
    }

    public Value getAndConvertFieldValue(Session session, Column column, Object entry) {
        Object columnValue = this.getFieldValue(column, entry);
        return convertValue(session, column, columnValue);
    }

    private Value convertValue(Session session, Column column, Object columnValue) {
        return DataType.convertToValue(session, columnValue, column.getType().getValueType());
    }

    public List<Value[]> getRowOrNull(Value[] indexValue, Session session) {
        final List<Object> indexRawValue = new ArrayList<>(cacheMetaInfo.getNumberOfIndexColumns());
        for (int i = 0; i < cacheMetaInfo.getNumberOfIndexColumns(); i++) {
            final Value idxVal = indexValue[i];
            indexRawValue.add(idxVal != null ? idxVal.getObject() : null);
        }

        final List<Object> entries = cacheMetaInfo.getOrNull(indexRawValue);
        final List<Value[]> results = new ArrayList<>();
        for (Object entry : entries) {
            if (entry == null) {
                return null;
            }

            Value[] values = new Value[allColumns.size()];
            for (int i = 0; i < indexRawValue.size(); i++) {
                values[i] = indexValue[i];
                if (values[i] == null) {
                    values[i] = this.getAndConvertFieldValue(session, indexColumns.get(i), entry);
                }
            }
            List<Column> columns = allColumns;
            for (int i = cacheMetaInfo.getNumberOfIndexColumns(); i < values.length; i++) {
                Column column = columns.get(i);
                values[i] = this.getAndConvertFieldValue(session, column, entry);
            }

            results.add(values);
        }

        return results;
    }

    public Iterator<Value[]> getAllRows(Session session) {
        return cacheMetaInfo.getAll().map(entry -> {
            List<Column> columns = allColumns;
            Value[] values = new Value[allColumns.size()];
            for (int i = 0; i < cacheMetaInfo.getNumberOfIndexColumns(); i++) {
                values[i] =
                        cacheMetaInfo.getNumberOfIndexColumns() == 1
                            ? this.convertValue(session, indexColumns.get(i), entry.getKey())
                            : this.getAndConvertFieldValue(session, indexColumns.get(i), entry.getValue());
            }

            for (int i = cacheMetaInfo.getNumberOfIndexColumns(); i < values.length; i++) {
                Column column = columns.get(i);
                values[i] = this.getAndConvertFieldValue(session, column, entry.getValue());
            }

            return values;
        }).iterator();
    }

    public Iterator<Value[]> getRowsInRange(Session session,  Value[] firstValue,  Value[] lastValue) {
        final List<Value[]> result = new ArrayList<>();
        getRowsInRangeRec(
                session,
                result,
                firstValue.clone(),
                firstValue,
                lastValue,
                0);
        return result.iterator();
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
                counters[level] = counters[level].add(ValueLong.get(1));
            }

            // One more recursive call as the upper bound should be included
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
