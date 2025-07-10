package com.gargantua7.sqlink

abstract class Table(val tableName: String) {

    companion object {
        val all by lazy { Column("*") }
    }

    val all by column("*")

    protected inline fun column(name: String): Lazy<Column> {
        return lazy { Column(name) }
    }

}

inline infix fun <T: Table> T.AS(alias: String) = TableAlias(this, alias)