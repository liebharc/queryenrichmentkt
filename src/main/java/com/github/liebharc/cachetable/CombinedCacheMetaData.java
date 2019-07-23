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
        final long numberofCols = cacheMetaInfo.getNumberOfIndexColumns();
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

    public List<Value[]> getRowOrNull(List<Value> indexValue, Session session) {
        final List<Object> indexRawValue = new ArrayList<>((int) cacheMetaInfo.getNumberOfIndexColumns());
        for (int i = 0; i < (int) cacheMetaInfo.getNumberOfIndexColumns(); i++) {
            final Value idxVal = indexValue.get(i);
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
                values[i] = indexValue.get(i);
                if (values[i] == null) {
                    values[i] = this.getAndConvertFieldValue(session, indexColumns.get(i), entry);
                }
            }
            List<Column> columns = allColumns;
            for (int i = (int) cacheMetaInfo.getNumberOfIndexColumns(); i < values.length; i++) {
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
                values[i] = this.convertValue(session, indexColumns.get(i), entry.getKey());
            }

            for (int i = (int)cacheMetaInfo.getNumberOfIndexColumns(); i < values.length; i++) {
                Column column = columns.get(i);
                values[i] = this.getAndConvertFieldValue(session, column, entry.getValue());
            }

            return values;
        }).iterator();
    }

    public Iterator<Value[]> getRowsInRange(Session session,  List<Value> firstValue,  List<Value> lastValue) {
        return getRowsInRange(session, 0, firstValue, lastValue.get(0));
    }

    private Iterator<Value[]> getRowsInRange(Session session, int indexColumn, List<Value> currentValue, Value lastValue) {
        ValueLong one = ValueLong.get(1);
        Value[] value = currentValue.toArray(new Value[0]);
        final List<Value[]> result = new ArrayList<>();
        while (!value[indexColumn].equals(lastValue)) {
            List<Value[]> row = this.getRowOrNull(Arrays.asList(value), session);
            if (row != null) {
                result.addAll(row);
            }

            value[indexColumn] = value[indexColumn].add(one);
        }

        return result.iterator();
    }

    public long getRowSize() {
        return cacheMetaInfo.size();
    }

    public long getNumberOfIndexColumns() {
        return cacheMetaInfo.getNumberOfIndexColumns();
    }
}
