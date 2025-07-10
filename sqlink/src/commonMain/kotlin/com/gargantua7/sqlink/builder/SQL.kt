package com.gargantua7.sqlink.builder

import com.gargantua7.sqlink.Table
import kotlin.jvm.JvmInline

@JvmInline
value class SQL(val sql: String) {
    override fun toString() = sql
}

data class TableAlias<T: Table>(val table: T, val alias: String)

@JvmInline
value class QueryResultSet(val alias: String)