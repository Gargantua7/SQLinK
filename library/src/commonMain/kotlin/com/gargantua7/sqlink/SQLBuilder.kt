package com.gargantua7.sqlink

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlin.math.exp

@SQLMaker
open class SQLBuilder(private val sb: StringBuilder) {

    operator fun String.unaryPlus() {
        sb.appendLine(this.trim())
    }

    operator fun SQL.unaryPlus() {
        sb.appendLine(sql.trim())
    }

    inline fun select(vararg column: IColumn) = "SELECT " + column.joinToString {
        when(it) {
            is Column -> it.name
            is ColumnAlias -> when(it.column) {
                is Column -> "${it.column.name} AS ${it.alias.name}"
                is TableAliasColumn -> "${it.column.alias}.${it.column.column.name} AS ${it.alias.name}"
                else -> throw UnsupportedOperationException()
            }
            is ExpressionColumnAlias -> "${it.expression} AS ${it.alias.name}"
            is TableAliasColumn -> "${it.alias}.${it.column.name}"
        }
    }

    inline fun select(columns: List<IColumn>) = "SELECT " + columns.joinToString {
        when(it) {
            is Column -> it.name
            is ColumnAlias -> when(it.column) {
                is Column -> "${it.column.name} AS ${it.alias.name}"
                is TableAliasColumn -> "${it.column.alias}.${it.column.column.name} AS ${it.alias.name}"
                else -> throw UnsupportedOperationException()
            }
            is ExpressionColumnAlias -> "${it.expression} AS ${it.alias.name}"
            is TableAliasColumn -> "${it.alias}.${it.column.name}"
        }
    }

    inline fun select(block: SelectScope.() -> Unit) = select(buildList { SelectScope(this).block() })

    inline fun update(table: Table) = "UPDATE ${table(table)}"

    inline fun from(table: Table) = "FROM ${table(table)}"

    inline fun from(table: QueryResultSet) = "FROM ${table(table)}"

    inline fun from(crossinline block: SQLBuilder.() -> Unit) = buildSQL {
        + "FROM ("
        this.block()
        + ")"
    }

    inline fun <T: Table> from(table: TableAlias<T>) = "FROM ${table(table)}"

    inline fun <T: Table> leftJoin(table: TableAlias<T>, crossinline on: SQLBuilder.() -> Unit) = buildSQL {
        + "LEFT JOIN ${table(table)} ON ("
        on()
        + ")"
    }

    inline fun set(crossinline block: UpdateSetScope.() -> Unit) = buildSQL {

        + ("SET " + (
                buildList {
                UpdateSetScope(this).block()
            }.joinToString(separator = ", \n") { (column, expression) ->
                "$column = $expression"
            }
        ))

    }

    inline fun where(crossinline block: SQLBuilder.() -> Unit) = buildSQL {
        + "WHERE ("
        block(this)
        + ")"
    }

    inline fun orderBy(vararg order: OrderExpression) = "ORDER BY " + order.joinToString { it.toString() }

    inline fun groupBy(vararg column: ExpressionColumn) = "GROUP BY " + column.joinToString {
        when(it) {
            is Column -> it.name
            is TableAliasColumn -> "${it.alias}.${it.column.name}"
        }
    }

    inline infix fun TableAliasColumn.link(other: TableAliasColumn) = "$this = $other"

    inline fun table(table: Table): String = table.tableName

    inline fun <T: Table> table(alias: TableAlias<T>): String = "${alias.table.tableName} ${alias.alias}"

    inline fun table(column: QueryResultSet): String = column.alias

    inline fun column(column: Column): String = column.name

    inline fun <T: Table> column(table: T, block: T.() -> Column) = table.block().name

    inline fun <T: Table> column(alias: TableAlias<T>, block: T.() -> Column) = TableAliasColumn(alias.alias, alias.table.block())

    inline fun real(value: Number): NumberExpression = NumberExpression(value)

    inline fun string(it: Any) = StringExpression(it.toString())

    inline fun <T> collection(collection: Collection<T>) = collection.joinToString(
        separator = ", ",
        prefix = "(",
        postfix = ")"
    ) { "'$it'" }

    inline fun <T> collection(vararg items: T) = items.joinToString(
        separator = ", ",
        prefix = "(",
        postfix = ")"
    ) { "'$it'" }

    inline fun amount(value: BigDecimal) = (value * 100).toPlainString()

    inline fun tempColumn(name: String) = lazy { Column(name) }

    inline fun result(name: String) = lazy { QueryResultSet(name) }

    inline infix fun <T> ExpressionColumn.IN(collection: Collection<T>) = "$this IN ${collection(collection)}"

    inline infix fun <T> ExpressionColumn.notIN(collection: Collection<T>) = "$this NOT IN ${collection(collection)}"

    inline infix fun ExpressionColumn.IN(sql: SQL) = "$this IN (${sql.sql.trim()})"

    inline infix fun ExpressionColumn.notIN(sql: SQL) = "$this NOT IN (${sql.sql.trim()})"

    inline infix fun Expression.eq(value: String) = "$this = ${string(value)}"

    inline infix fun Expression.eq(value: Number) = "$this = $value"

    inline infix fun Expression.eq(other: Expression) = "$this = $other"

    inline infix fun Expression.notEq(value: String) = "$this <> ${string(value)}"

    inline infix fun Expression.notEq(value: Number) = "$this <> $value"

    inline infix fun Expression.notEq(value: Expression) = "$this <> $value"

    inline infix fun Expression.less(value: Number) = "$this < $value"

    inline infix fun Expression.less(value: Expression) = "$this < $value"

    inline infix fun Expression.more(value: Number) = "$this > $value"

    inline infix fun Expression.more(value: Expression) = "$this > $value"

    inline infix fun Expression.lessEq(value: Number) = "$this <= $value"

    inline infix fun Expression.lessEq(value: Expression) = "$this <= $value"

    inline infix fun Expression.moreEq(value: Number) = "$this >= $value"

    inline infix fun Expression.moreEq(value: Expression) = "$this >= $value"

    inline operator fun Expression.plus(other: Expression) = ArithmeticExpression(this, other, ArithmeticExpression.Algorithm.ADD)
    inline operator fun Expression.minus(other: Expression) = ArithmeticExpression(this, other, ArithmeticExpression.Algorithm.SUB)
    inline operator fun Expression.times(other: Expression) = ArithmeticExpression(this, other, ArithmeticExpression.Algorithm.MUL)
    inline operator fun Expression.div(other: Expression) = ArithmeticExpression(this, other, ArithmeticExpression.Algorithm.DIV)

    inline operator fun Expression.plus(other: Number) = ArithmeticExpression(this, NumberExpression(other), ArithmeticExpression.Algorithm.ADD)
    inline operator fun Expression.minus(other: Number) = ArithmeticExpression(this, NumberExpression(other), ArithmeticExpression.Algorithm.SUB)
    inline operator fun Expression.times(other: Number) = ArithmeticExpression(this, NumberExpression(other), ArithmeticExpression.Algorithm.MUL)
    inline operator fun Expression.div(other: Number) = ArithmeticExpression(this, NumberExpression(other), ArithmeticExpression.Algorithm.DIV)

    inline infix fun Expression.concat(other: Expression) = ConcatExpression(this, other)

    inline infix fun ExpressionColumn.like(expression: TextExpression) = "$this LIKE $expression"

    val ExpressionColumn.isNull inline get() = "$this IS NULL"

    val ExpressionColumn.isNotNull inline get() = "$this IS NOT NULL"

    inline operator fun Expression.unaryMinus() = MinusExpression(this)

    inline infix fun Expression.AS(alias: Column) = ExpressionColumnAlias(this, alias)

    inline fun Expression.over(
        partitionBy: List<ExpressionColumn>? = null,
        orderBy: List<OrderExpression>? = null,
        rows: Pair<OverRowsType, OverRowsType?>? = null
    ) = OverExpression(this, partitionBy, orderBy, rows)

    inline val Column.asc get() = OrderExpression.Asc(this)

    inline val Column.desc get() = OrderExpression.Desc(this)

    @SQLMaker
    inline fun and(build: MultiSQLBuilder.() -> Unit) = MultiSQLBuilder().let { builder ->
        builder.build()
        if (builder.list.isNotEmpty()) {
            SQL(builder.list.joinToString(separator = "\nAND ") { "($it)" })
        } else SQL("1 = 1")
    }

    @SQLMaker
    inline fun or(build: MultiSQLBuilder.() -> Unit) = MultiSQLBuilder().let { builder ->
        builder.build()
        if (builder.list.isNotEmpty()) {
            SQL(builder.list.joinToString(separator = "\nOR ") { "($it)" })
        } else SQL("1 = 1")
    }

    inline fun union(
        default: String,
        build: MultiSQLBuilder.() -> Unit
    ) = MultiSQLBuilder().let { builder ->
        builder.build()
        if (builder.list.isNotEmpty()) {
            SQL(builder.list.joinToString(separator = "\nUNION\n") { it })
        } else SQL(default)
    }

    inline fun union(
        default: SQL,
        build: MultiSQLBuilder.() -> Unit
    ) = MultiSQLBuilder().let { builder ->
        builder.build()
        if (builder.list.isNotEmpty()) {
            SQL(builder.list.joinToString(separator = "\nUNION\n") { it })
        } else default
    }

    inline fun unionAll(
        default: String,
        build: MultiSQLBuilder.() -> Unit
    ) = MultiSQLBuilder().let { builder ->
        builder.build()
        if (builder.list.isNotEmpty()) {
            SQL(builder.list.joinToString(separator = "\nUNION ALL\n") { it })
        } else SQL(default)
    }

    inline fun unionAll(
        default: SQL,
        build: MultiSQLBuilder.() -> Unit
    ) = MultiSQLBuilder().let { builder ->
        builder.build()
        if (builder.list.isNotEmpty()) {
            SQL(builder.list.joinToString(separator = "\nUNION ALL\n") { it })
        } else default
    }

    inline fun with(res: QueryResultSet, crossinline block: SQLBuilder.() -> Unit) = buildSQL {
        + "WITH ${res.alias} AS ("
        + SQL(buildString { SQLBuilder(this).block() })
        + ")"
    }

}

@SQLMaker
class MultiSQLBuilder {

    val list = mutableListOf<String>()

    operator fun SQL.unaryPlus() {
        list.add(sql.trim())
    }
}

class SelectScope(private val list: MutableList<IColumn>) {

    operator fun IColumn.unaryPlus() {
        list.add(this)
    }

    inline infix fun Expression.AS(alias: Column) = ExpressionColumnAlias(this, alias)

        inline fun Expression.over(
            partitionBy: List<ExpressionColumn>? = null,
            orderBy: List<OrderExpression>? = null,
            rows: Pair<OverRowsType, OverRowsType?>? = null
        ) = OverExpression(this, partitionBy, orderBy, rows)

}

class UpdateSetScope(private val list: MutableList<Pair<Column, Expression>>) {

    operator fun set(column: Column, expression: Any?) {
        when(expression) {
            null -> list.add(column to Null)
            is SQL -> list.add(column to SQLExpression(expression))
            is Number -> list.add(column to NumberExpression(expression))
            is String -> list.add(column to StringExpression(expression))
            is Expression -> list.add(column to expression)
            else -> list.add(column to StringExpression(expression.toString()))
        }
    }

}

@SQLMaker
fun buildSQL(block: SQLBuilder.() -> Unit): SQL {

    return SQL(buildString {
        block(SQLBuilder(this))
    })
}
