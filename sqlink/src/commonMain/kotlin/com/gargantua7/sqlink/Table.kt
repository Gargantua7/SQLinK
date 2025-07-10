package com.gargantua7.sqlink

import com.gargantua7.sqlink.builder.TableAlias

abstract class Table(val tableName: String) {

    companion object {
        val all = Column("*")
    }

    val all = Table.all
}

fun Table.number(name: String) = NumberColumn(name)

fun Table.text(name: String) = TextColumn(name)

inline fun <reified E: Enum<E>> Table.enum(name: String) = EnumColumn<E>(name)

infix fun <T: Table> T.AS(alias: String) = TableAlias(this, alias)