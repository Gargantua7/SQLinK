package com.gargantua7.sqlink.builder

import kotlin.jvm.JvmInline

@JvmInline
value class SQL(val sql: String) {
    override fun toString() = sql
}

@JvmInline
value class QueryResultSet(val alias: String)