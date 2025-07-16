package com.gargantua7.sqlink

abstract class Table(val tableName: String) {

    companion object {
        val all = AllColumn
    }

    val all = Table.all

    override fun toString() = tableName
}

data class TableAlias<T: Table>(val table: T, val alias: String): Table(alias) {
    override fun toString() = super.toString()
}

fun Table.number(name: String) = NumberColumn(name)

fun Table.text(name: String) = TextColumn(name)

inline fun <reified E: Enum<E>> Table.enum(name: String) = EnumColumn<E>(name)

infix fun <T: Table> T.AS(alias: String) = TableAlias(this, alias)