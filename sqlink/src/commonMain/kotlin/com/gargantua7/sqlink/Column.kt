package com.gargantua7.sqlink

import kotlin.jvm.JvmInline

sealed interface IColumn
sealed interface StatementColumn: IColumn
sealed interface ExpressionColumn: IColumn, Expression

@JvmInline
value class Column(val name: String): StatementColumn, ExpressionColumn {
    override fun toString(): String = name
}

@JvmInline
value class TextColumn(val name: String): StatementColumn, ExpressionColumn, ITextExpression {
    override fun toString(): String = name
}

@JvmInline
value class NumberColumn(val name: String): StatementColumn, ExpressionColumn, INumberExpression {
    override fun toString(): String = name
}

@JvmInline
value class EnumColumn<E: Enum<E>>(val name: String): StatementColumn, ExpressionColumn, ITextExpression {
    override fun toString(): String = name
}

data class ColumnAlias(val column: IColumn, val alias: Column): StatementColumn {
    override fun toString(): String = "$column AS $alias"
}

data class ExpressionColumnAlias(val expression: Expression, val alias: Column): StatementColumn {
    override fun toString(): String = "$expression AS $alias"
}

data class TableAliasColumn(val alias: String, val column: Column): ExpressionColumn {
    override fun toString(): String = "$alias.${column.name}"
}


infix fun Column.AS(alias: Column) = ColumnAlias(this, alias)

infix fun TableAliasColumn.AS(alias: Column) = ColumnAlias(this, alias)