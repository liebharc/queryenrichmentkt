package com.github.liebharc.cachetable;

import com.google.common.cache.Cache;
import org.h2.command.ddl.CreateTableData;
import org.h2.engine.Session;
import org.h2.message.DbException;
import org.h2.table.Column;
import org.h2.value.DataType;
import org.h2.value.Value;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class CombinedCacheMetaData {
    private final static String REF_NAME = "REF";
    private final CreateTableData tableData;
    private final CacheMetaInfo cacheMetaInfo;
    private final Column indexColumn;
    private final ArrayList<Column> objectColumns;
    private final IdentityHashMap<Column, Method> columnToMethod;
    private final int rowSize;
    private final ArrayList<Column> allColumns;

    public CombinedCacheMetaData(CreateTableData tableData, CacheMetaInfo cacheMetaInfo) {
        this.tableData = tableData;
        this.cacheMetaInfo = cacheMetaInfo;
        allColumns = new ArrayList<>(tableData.columns);
        indexColumn = allColumns.get(0);
        objectColumns = new ArrayList<>(allColumns);
        objectColumns.remove(0);
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

        rowSize = allColumns.size();
    }
    public Object getRawValue(Column column, Object entry) {
        if (column.getName().equals(REF_NAME)) {
            return entry;
        }

        if (columnToMethod == null) {
            if (column == indexColumn) {
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

    public Value getValue(Session session, Column column, Object entry) {
        Object columnValue = this.getRawValue(column, entry);
        return convertValue(session, column, columnValue);
    }

    private Value convertValue(Session session, Column column, Object columnValue) {
        return DataType.convertToValue(session, columnValue, column.getType().getValueType());
    }


    public Value[] getRow(Value indexValue, Session session) {
        final Object value = this.getCache().getIfPresent(
                indexColumn.convert(indexValue).getObject());
        Value[] values = new Value[this.getRowSize()];
        values[0] = indexValue;
        ArrayList<Column> columns = this.getAllColumns();
        for (int i = 1; i < values.length; i++) {
            Column column = columns.get(i);
            values[i] = this.getValue(session, column, value);
        }

        return values;
    }

    public Iterator<Value[]> getAllRows(Session session) {
        return this.getCache().asMap().entrySet().stream().map(entry -> {
            ArrayList<Column> columns = this.getAllColumns();
            Value[] values = new Value[this.getRowSize()];
            values[0] = this.convertValue(session, indexColumn, entry.getKey());
            for (int i = 1; i < values.length; i++) {
                Column column = columns.get(i);
                values[i] = this.getValue(session, column, entry.getValue());
            }

            return values;
        }).iterator();
    }

    public CreateTableData getTableData() {
        return tableData;
    }

    public CacheMetaInfo getCacheMetaInfo() {
        return cacheMetaInfo;
    }

    public Cache<? extends  Object, ? extends  Object> getCache() {
        return cacheMetaInfo.getCache();
    }

    public Column getIndexColumn() {
        return indexColumn;
    }

    public int getRowSize() {
        return rowSize;
    }

    public ArrayList<Column> getObjectColumns() {
        return objectColumns;
    }

    public ArrayList<Column> getAllColumns() {
        return allColumns;
    }
}
