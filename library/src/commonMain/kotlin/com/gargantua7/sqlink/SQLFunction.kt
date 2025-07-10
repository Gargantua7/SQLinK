package com.gargantua7.sqlink

data class SQLFunction(val method: String, val args: List<Expression>): Expression {
    override fun toString(): String = "$method(${args.joinToString(", ")})"

    constructor(method: String, vararg args: Expression): this(method, args.toList())

}

inline fun SQLBuilder.max(column: Expression) = SQLFunction("MAX", listOf(column))

inline fun SQLBuilder.min(column: Expression) = SQLFunction("MIN", listOf(column))

inline fun SQLBuilder.sum(column: Expression) = SQLFunction("SUM", listOf(column))

inline fun SQLBuilder.count(column: Expression = Table.all) = SQLFunction("COUNT", listOf(column))

inline fun SQLBuilder.strftime(format: String, expression: Expression? = null) = expression?.let {
    SQLFunction("STRFTIME", string(format), it / 1000, string("unixepoch"), string("localtime"))
} ?: run {
    SQLFunction("STRFTIME", string(format), string("now"), string("localtime"))
}

inline fun SQLBuilder.year(column: Expression? = null) = strftime("%Y", column)

inline fun SQLBuilder.month(column: Expression? = null) = strftime("%m", column)

inline fun SQLBuilder.quarter(column: Expression? = null) = (strftime("%m", column) - 1) / 3 + 1

inline fun SQLBuilder.week(column: Expression? = null) = strftime("%w", column)

inline fun SQLBuilder.day(column: Expression? = null) = strftime("%d", column)

inline fun SQLBuilder.rowNumber() = SQLFunction("ROW_NUMBER")

inline fun SQLBuilder.round(value: INumberExpression, decimal: INumberExpression = real(0)) = SQLFunction("ROUND", value, decimal)

inline fun SQLBuilder.coalesce(vararg expression: Expression) = SQLFunction("COALESCE", *expression)

inline fun SQLBuilder.nullIf(expression: Expression, value: Expression) = SQLFunction("NULLIF", expression, value)