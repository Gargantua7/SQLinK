package com.gargantua7.sqlink

import kotlin.jvm.JvmInline

@JvmInline
value class SQL(val sql: String) {
    override fun toString(): String {
        throw SQLBuilderShouldNotBeAccessedException
    }
}

data class TableAlias<T: Table>(val table: T, val alias: String) {
    override fun toString(): String {
        throw SQLBuilderShouldNotBeAccessedException
    }
}

@JvmInline
value class QueryResultSet(val alias: String) {
    override fun toString(): String {
        throw SQLBuilderShouldNotBeAccessedException
    }
}

object SQLBuilderShouldNotBeAccessedException : Exception("SQLBuilder should not be accessed")