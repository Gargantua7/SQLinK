package com.gargantua7.sqlink

import kotlin.jvm.JvmInline

sealed interface Expression

sealed interface TextExpression: Expression

sealed interface INumberExpression: Expression

@JvmInline
value class SQLExpression(val sql: SQL): TextExpression {
    override fun toString(): String = sql.sql.removeSuffix("\n")
}

@JvmInline
value class StringExpression(val value: String): TextExpression {
    override fun toString(): String = if (value.startsWith("'") && value.endsWith("'")) value else "'$value'"
}

@JvmInline
value class NumberExpression(val value: Number): INumberExpression {
    override fun toString(): String = value.toString()
}

@JvmInline
value class MinusExpression(val express: Expression): INumberExpression {
    override fun toString(): String = "(-${express})"

}

data class ArithmeticExpression(val left: Expression, val right: Expression, val algorithm: Algorithm): INumberExpression {
    override fun toString(): String = "($left ${algorithm.s} $right)"

    enum class Algorithm(val s: String) {

        ADD("+"), SUB("-"), MUL("*"), DIV("/")

    }
}

data class ConcatExpression(val left: Expression, val right: Expression): TextExpression {
    override fun toString(): String = "$left || $right"
}

data object Null: Expression {
    override fun toString() = "NULL"
}

data object Else: Expression {
    override fun toString() = "ELSE"
}

sealed interface OrderExpression: Expression {

    @JvmInline
    value class Asc(val column: Column): OrderExpression {
        override fun toString(): String = "$column ASC"
    }
    @JvmInline
    value class Desc(val column: Column): OrderExpression {
        override fun toString(): String = "$column DESC"
    }

}

data class OverExpression(
    val column: Expression,
    val partitionBy: List<ExpressionColumn>? = null,
    val orderBy: List<OrderExpression>? = null,
    val rows: Pair<OverRowsType, OverRowsType?>? = null,
): Expression {
    override fun toString(): String = "$column OVER (" + buildString {
        if (partitionBy != null) {
            append("PARTITION BY ")
            append(partitionBy.joinToString { it.toString() })
        }
        if (orderBy != null) {
            append(" ORDER BY ")
            append(orderBy.joinToString { it.toString() })
        }
        if (rows != null) {
            append(" ROWS BETWEEN ")
            append(rows.first.toString())
            rows.second?.let {
                append(" AND ")
                append(rows.second.toString())
            }
        }
    } + ")"
}

sealed class OverRowsType {
    data object UnboundedPreceding: OverRowsType() {
        override fun toString(): String = "UNBOUNDED PRECEDING"
    }
    data object CurrentRow: OverRowsType() {
        override fun toString(): String = "CURRENT ROW"
    }
    data object UnboundedFollowing: OverRowsType() {
        override fun toString(): String = "UNBOUNDED FOLLOWING"
    }
    data class Preceding(val value: Int): OverRowsType() {
        override fun toString(): String = "$value PRECEDING"
    }
    data class Following(val value: Int): OverRowsType() {
        override fun toString(): String = "$value FOLLOWING"
    }
}