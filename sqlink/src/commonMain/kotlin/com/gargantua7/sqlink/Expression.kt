package com.gargantua7.sqlink

import kotlin.jvm.JvmInline

sealed interface Expression: Selectable

sealed interface ITextExpression: Expression

sealed interface IBooleanExpression: Expression

sealed interface INumberExpression: IBooleanExpression

@JvmInline
value class StringExpression(val value: String): ITextExpression {

    constructor(value: Any): this(value.toString())
    constructor(value: Enum<*>): this(value.name)

    override fun toString(): String = if (value.startsWith("'") && value.endsWith("'")) value else "'$value'"
}

@JvmInline
value class BooleanExpression(val value: String): IBooleanExpression {
    override fun toString(): String = value
}

data class AndExpression(val left: IBooleanExpression, val right: IBooleanExpression): IBooleanExpression {
    override fun toString(): String = "($left) AND ($right)"
}

data class OrExpression(val left: IBooleanExpression, val right: IBooleanExpression): IBooleanExpression {
    override fun toString(): String = "($left) OR ($right)"
}

@JvmInline
value class NumberExpression(val value: Number): INumberExpression {
    override fun toString(): String = value.toString()
}

@JvmInline
value class MinusExpression(val express: INumberExpression): INumberExpression {
    override fun toString(): String = "(-${express})"

}

data class ArithmeticExpression(val left: INumberExpression, val right: INumberExpression, val algorithm: Algorithm): INumberExpression {
    override fun toString(): String = "($left ${algorithm.s} $right)"

    enum class Algorithm(val s: String) {

        ADD("+"), SUB("-"), MUL("*"), DIV("/")

    }
}

data class ConcatExpression(val left: ITextExpression, val right: ITextExpression): ITextExpression {
    override fun toString(): String = "$left || $right"
}

data object Null: Expression {
    override fun toString() = "NULL"
}

sealed interface Order {

    @JvmInline
    value class Asc(val column: Column): Order {
        override fun toString(): String = "$column ASC"
    }

    @JvmInline
    value class Desc(val column: Column): Order {
        override fun toString(): String = "$column DESC"
    }

}

data class OverExpression(
    val column: Expression,
    val partitionBy: List<TableColumn>? = null,
    val orderBy: List<Order>? = null,
    val rows: Pair<OverRowsType, OverRowsType?>? = null,
): Expression {
    override fun toString(): String = "$column OVER (" + buildString {
        if (partitionBy != null) {
            append("PARTITION BY ")
            append(partitionBy.joinToString())
        }
        if (orderBy != null) {
            append(" ORDER BY ")
            append(orderBy.joinToString())
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

sealed interface OverRowsType {

    data object UnboundedPreceding: OverRowsType {
        override fun toString(): String = "UNBOUNDED PRECEDING"
    }

    data object CurrentRow: OverRowsType {
        override fun toString(): String = "CURRENT ROW"
    }

    data object UnboundedFollowing: OverRowsType {
        override fun toString(): String = "UNBOUNDED FOLLOWING"
    }

    data class Preceding(val value: Int): OverRowsType {
        override fun toString(): String = "$value PRECEDING"
    }

    data class Following(val value: Int): OverRowsType {
        override fun toString(): String = "$value FOLLOWING"
    }
}

data class CaseExpression(
    val expression: Expression? = null,
    val branches: List<Branch> = emptyList(),
    val elseBranch: Expression? = null
): Expression {

    override fun toString() = "CASE $expression\n" +
            branches.joinToString(separator = "\n", postfix = "\n") { (expression, result) ->
                "WHEN $expression THEN $result"
            } +
            elseBranch?.let { "ELSE $it\n"}.orEmpty() +
            "END"

    data class Branch(val expression: Expression, val result: Expression)
}