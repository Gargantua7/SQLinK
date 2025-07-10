package com.gargantua7.sqlink

import com.gargantua7.sqlink.builder.SQLBuildScope

abstract class SQLFunction: Expression {

    abstract val method: String
    abstract val args: List<Expression>

    override fun toString(): String = "$method(${args.joinToString(", ")})"
}

data class SQLFunctionReturnNumber(override val method: String, override val args: List<Expression>): SQLFunction(), INumberExpression {
    constructor(method: String, vararg args: Expression): this(method, args.toList())
}

data class SQLFunctionReturnText(override val method: String, override val args: List<Expression>): SQLFunction(), INumberExpression {
    constructor(method: String, vararg args: Expression): this(method, args.toList())

    val asNumber get() = SQLFunctionReturnNumber(method, args)
}


data class SQLFunctionReturnExpression(override val method: String, override val args: List<Expression>): SQLFunction(), Expression {
    constructor(method: String, vararg args: Expression): this(method, args.toList())
}

fun SQLBuildScope.max(column: INumberExpression) = SQLFunctionReturnNumber("MAX", listOf(column))

fun SQLBuildScope.min(column: INumberExpression) = SQLFunctionReturnNumber("MIN", listOf(column))

fun SQLBuildScope.sum(column: INumberExpression) = SQLFunctionReturnNumber("SUM", listOf(column))

fun SQLBuildScope.count(column: Expression = Table.all) = SQLFunctionReturnNumber("COUNT", listOf(column))

fun SQLBuildScope.strftime(format: String, expression: INumberExpression? = null) = expression?.let {
    SQLFunctionReturnText("STRFTIME", string(format), it / 1000, string("unixepoch"), string("localtime"))
} ?: run {
    SQLFunctionReturnText("STRFTIME", string(format), string("now"), string("localtime"))
}

fun SQLBuildScope.year(column: INumberExpression? = null) = strftime("%Y", column)

fun SQLBuildScope.month(column: INumberExpression? = null) = strftime("%m", column)

fun SQLBuildScope.quarter(column: INumberExpression? = null) = (strftime("%m", column).asNumber - 1) / 3 + 1

fun SQLBuildScope.week(column: INumberExpression? = null) = strftime("%w", column)

fun SQLBuildScope.day(column: INumberExpression? = null) = strftime("%d", column)

fun SQLBuildScope.rowNumber() = SQLFunctionReturnNumber("ROW_NUMBER")

fun SQLBuildScope.round(value: INumberExpression, decimal: INumberExpression = real(0)) = SQLFunctionReturnNumber("ROUND", value, decimal)

fun SQLBuildScope.coalesce(vararg expression: Expression) = SQLFunctionReturnExpression("COALESCE", *expression)

fun SQLBuildScope.nullIf(expression: Expression, value: Expression) = SQLFunctionReturnExpression("NULLIF", expression, value)