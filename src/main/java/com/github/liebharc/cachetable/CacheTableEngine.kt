package com.github.liebharc.cachetable

import org.h2.api.TableEngine
import org.h2.command.ddl.CreateTableData
import org.h2.table.Table

class CacheTableEngine : TableEngine {
        override fun createTable(tableData: CreateTableData?): Table {
                return CacheTable(tableData!!)
        }
}