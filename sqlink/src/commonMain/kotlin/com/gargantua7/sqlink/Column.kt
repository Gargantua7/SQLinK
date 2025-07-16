package com.gargantua7.sqlink

import kotlin.jvm.JvmInline

sealed interface Column
sealed interface SelectableColumn: Column, Selectable
sealed interface TableColumn: SelectableColumn, Expression {
    val name: String
}

object AllColumn: Column, Expression {
    override fun toString(): String = "*"
}

@JvmInline
value class TextColumn(override val name: String): TableColumn, ITextExpression {
    override fun toString(): String = name
}

@JvmInline
value class NumberColumn(override val name: String): TableColumn, INumberExpression {
    override fun toString(): String = name
}

@JvmInline
value class EnumColumn<E: Enum<E>>(override val name: String): TableColumn, ITextExpression {
    override fun toString(): String = name
}

data class TableAliasColumn(val table: TableAlias<*>, val column: TableColumn): TableColumn {
    override val name get() = "${table.alias}.${column}"
    override fun toString(): String = name
}