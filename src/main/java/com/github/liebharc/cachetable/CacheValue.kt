package com.github.liebharc.cachetable

import org.h2.value.CompareMode
import org.h2.value.TypeInfo
import org.h2.value.Value
import java.lang.StringBuilder
import java.sql.PreparedStatement

data class CacheValue<T: Any>(val value: T) : Value() {
    override fun getObject(): Any {
        return value
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun compareTypeSafe(v: Value?, mode: CompareMode?): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getType(): TypeInfo {
        return TypeInfo.TYPE_JAVA_OBJECT
    }

    override fun set(prep: PreparedStatement?, parameterIndex: Int) {

    }

    override fun getSQL(builder: StringBuilder?): StringBuilder {
        return builder!!
    }

    override fun getValueType(): Int {
        return 0
    }

    override fun getString(): String {
        return value.toString()
    }
}