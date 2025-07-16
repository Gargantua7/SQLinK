package com.gargantua7.sqlink

interface Selectable

data class ExpressionAlias(val expression: Expression, val alias: String): Selectable {
    override fun toString(): String = "$expression AS $alias"
}

infix fun Expression.AS(alias: String) = ExpressionAlias(this, alias)