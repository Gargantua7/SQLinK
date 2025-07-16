package com.gargantua7.sqlink.builder

import com.gargantua7.sqlink.*

abstract class AbstractSQLBuildScope {

    fun table(column: QueryResultSet): String = column.alias

    inline operator fun <T: Table> TableAlias<T>.invoke(operation: T.() -> TableColumn) = TableAliasColumn(this, table.operation())

    fun real(value: Number): NumberExpression = NumberExpression(value)

    fun string(it: Any) = StringExpression(it)

    fun <T> collection(collection: Collection<T>) = collection.joinToString(
        separator = ", ",
        prefix = "(",
        postfix = ")"
    ) { "'$it'" }

    fun <T> collection(vararg items: T) = items.joinToString(
        separator = ", ",
        prefix = "(",
        postfix = ")"
    ) { "'$it'" }

    fun result(name: String) = QueryResultSet(name)

    operator fun INumberExpression.plus(other: INumberExpression) =
        ArithmeticExpression(this, other, ArithmeticExpression.Algorithm.ADD)

    operator fun INumberExpression.minus(other: INumberExpression) =
        ArithmeticExpression(this, other, ArithmeticExpression.Algorithm.SUB)

    operator fun INumberExpression.times(other: INumberExpression) =
        ArithmeticExpression(this, other, ArithmeticExpression.Algorithm.MUL)

    operator fun INumberExpression.div(other: INumberExpression) =
        ArithmeticExpression(this, other, ArithmeticExpression.Algorithm.DIV)

    operator fun INumberExpression.plus(other: Number) =
        ArithmeticExpression(this, NumberExpression(other), ArithmeticExpression.Algorithm.ADD)

    operator fun INumberExpression.minus(other: Number) =
        ArithmeticExpression(this, NumberExpression(other), ArithmeticExpression.Algorithm.SUB)

    operator fun INumberExpression.times(other: Number) =
        ArithmeticExpression(this, NumberExpression(other), ArithmeticExpression.Algorithm.MUL)

    operator fun INumberExpression.div(other: Number) =
        ArithmeticExpression(this, NumberExpression(other), ArithmeticExpression.Algorithm.DIV)

    infix fun ITextExpression.concat(other: ITextExpression) =
        ConcatExpression(this, other)

    operator fun INumberExpression.unaryMinus() =
        MinusExpression(this)

    infix fun IBooleanExpression.and(other: IBooleanExpression) = AndExpression(this, other)

    infix fun IBooleanExpression.or(other: IBooleanExpression) = OrExpression(this, other)

    infix fun TableColumn.IN(sql: SQL) = BooleanExpression("$this IN (${sql.sql.trim()})")

    infix fun TableColumn.notIN(sql: SQL) = BooleanExpression("$this NOT IN (${sql.sql.trim()})")

    infix fun ITextExpression.IN(collection: Collection<String>) = BooleanExpression("$this IN ${collection(collection)}")

    infix fun ITextExpression.notIN(collection: Collection<String>) = BooleanExpression("$this NOT IN ${collection(collection)}")

    infix fun INumberExpression.IN(collection: Collection<Number>) = BooleanExpression("$this IN ${collection(collection)}")

    infix fun INumberExpression.notIN(collection: Collection<Number>) = BooleanExpression("$this NOT IN ${collection(collection)}")

    infix fun <E: Enum<E>> EnumColumn<E>.IN(collection: Collection<E>) = BooleanExpression("$this IN ${collection(collection)}")

    infix fun <E: Enum<E>> EnumColumn<E>.notIN(collection: Collection<E>) = BooleanExpression("$this NOT IN ${collection(collection)}")

    infix fun ITextExpression.eq(value: String) = BooleanExpression("$this = ${string(value)}")

    infix fun INumberExpression.eq(value: Number) = BooleanExpression("$this = $value")

    infix fun Expression.eq(other: Expression) = BooleanExpression("$this = $other")

    infix fun <E: Enum<E>> EnumColumn<E>.eq(other: E) = BooleanExpression("$this = '$other'")

    infix fun ITextExpression.notEq(value: String) = BooleanExpression("$this <> ${string(value)}")

    infix fun INumberExpression.notEq(value: Number) = BooleanExpression("$this <> $value")

    infix fun Expression.notEq(value: Expression) = BooleanExpression("$this <> $value")

    infix fun <E: Enum<E>> EnumColumn<E>.notEq(other: E) = BooleanExpression("$this = '$other'")

    infix fun INumberExpression.less(value: Number) = BooleanExpression("$this < $value")

    infix fun INumberExpression.less(value: INumberExpression) = BooleanExpression("$this < $value")

    infix fun INumberExpression.more(value: Number) = BooleanExpression("$this > $value")

    infix fun INumberExpression.more(value: INumberExpression) = BooleanExpression("$this > $value")

    infix fun INumberExpression.lessEq(value: Number) = BooleanExpression("$this <= $value")

    infix fun INumberExpression.lessEq(value: INumberExpression) = BooleanExpression("$this <= $value")

    infix fun INumberExpression.moreEq(value: Number) = BooleanExpression("$this >= $value")

    infix fun INumberExpression.moreEq(value: INumberExpression) = BooleanExpression("$this >= $value")

    infix fun ITextExpression.like(expression: ITextExpression) = BooleanExpression("$this LIKE $expression")

    infix fun ITextExpression.like(expression: String) = BooleanExpression("$this LIKE $expression")

    val TableColumn.isNull get() = BooleanExpression("$this IS NULL")

    val TableColumn.isNotNull get() = BooleanExpression("$this IS NOT NULL")
}

@SQLBuilder
class SQLBuildScope internal constructor(): AbstractSQLBuildScope() {

    private val sb = StringBuilder()

    internal fun build(): SQL {
        return SQL(sb.toString())
    }

    @SQLBuilder
    fun sql(block: SQLBuildScope.() -> Unit) {
        sb.append(SQLBuildScope().apply(block).build())
    }

    operator fun String.unaryPlus() {
        if (isEmpty()) return
        sb.appendLine(this.trim())
    }

    operator fun SQL.unaryPlus() {
        if (sql.isEmpty()) return
        sb.appendLine(sql.trim())
    }

    fun select(vararg column: Selectable) = + ("SELECT " + column.joinToString())

    fun select(columns: List<Selectable>) = + ("SELECT " + columns.joinToString())

    fun from(table: Table) = + "FROM $table"

    fun from(table: QueryResultSet) = + "FROM ${table(table)}"

    inline fun from(crossinline block: SQLBuildScope.() -> Unit) = sql {
        + "FROM ("
        this.block()
        + ")"
    }

    fun <T: Table> from(table: TableAlias<T>) = + "FROM ${table.table} ${table.alias}"

    fun <T: Table> leftJoin(table: TableAlias<T>, on: SingleConditionScope.() -> IBooleanExpression) = sql {
        + "LEFT JOIN ${table.table} ${table.alias} ON ("
        + SingleConditionScope().let(on).toString()
        + ")"
    }

    fun <T: Table> update(table: T, set: UpdateSetScope.(T) -> Unit) {
        + "UPDATE $table"
        + UpdateSetScope().apply { set(table) }.build()
    }

    fun <T: Table> delete(table: T) = + "DELETE $table"

    fun where(operation: SingleConditionScope.() -> IBooleanExpression) = + ("WHERE " + SingleConditionScope().let(operation))

    val union: Unit get() = + "UNION"
    val unionAll: Unit get() = + "UNION ALL"

    fun orderBy(vararg order: OrderExpression) = + ("ORDER BY " + order.joinToString())

    fun groupBy(vararg column: TableColumn) = + ("GROUP BY " + column.joinToString())

    inline fun with(res: QueryResultSet, crossinline block: SQLBuildScope.() -> Unit) = sql {
            +"WITH ${res.alias} AS ("
            block()
            +")"
        }

    infix fun TableAliasColumn.link(other: TableAliasColumn) = "$this = $other"

    fun Expression.over(
        partitionBy: List<TableColumn>? = null,
        orderBy: List<OrderExpression>? = null,
        rows: Pair<OverRowsType, OverRowsType?>? = null
    ) = OverExpression(this, partitionBy, orderBy, rows)

    val Column.asc get() = OrderExpression.Asc(this)

    val Column.desc get() = OrderExpression.Desc(this)
}

@SQLBuilder
class SingleConditionScope internal constructor(): AbstractSQLBuildScope() {

    @SQLBuilder
    fun and(operation: MultiConditionScope.() -> Unit) = MultiConditionScope()
        .apply(operation)
        .build(::AndExpression)

    @SQLBuilder
    fun or(operation: MultiConditionScope.() -> Unit) = MultiConditionScope()
        .apply(operation)
        .build(::OrExpression)
}

@SQLBuilder
class MultiConditionScope internal constructor(): AbstractSQLBuildScope() {
    private val list: MutableList<IBooleanExpression> = ArrayList()

    @SQLBuilder
    fun and(operation: MultiConditionScope.() -> Unit) = MultiConditionScope()
        .apply(operation)
        .build(::AndExpression)
        .let(list::add)

    @SQLBuilder
    fun or(operation: MultiConditionScope.() -> Unit) = MultiConditionScope()
        .apply(operation)
        .build(::OrExpression)
        .let(list::add)

    fun exp(expression: IBooleanExpression) = list.add(expression)

    internal fun build(builder: (IBooleanExpression, IBooleanExpression) -> IBooleanExpression): IBooleanExpression =
        list.reduce(builder)
}

class UpdateSetScope internal constructor() {

    private val map = LinkedHashMap<Column, Expression>()

    infix fun TableColumn.set(expression: Nothing?) {
        map[this] = Null
    }

    infix fun NumberColumn.set(expression: INumberExpression) {
        map[this] = expression
    }

    infix fun NumberColumn.set(expression: Number) {
        map[this] = NumberExpression(expression)
    }

    infix fun TextColumn.set(expression: ITextExpression) {
        map[this] = expression
    }

    infix fun TextColumn.set(expression: String) {
        map[this] = StringExpression(expression)
    }

    infix fun <E: Enum<E>> EnumColumn<E>.set(value: E) {
        map[this] = StringExpression(value.name)
    }

    internal fun build(): SQL {
        if (map.isEmpty()) return SQL("")
        return SQL(
            "SET " + map.entries.joinToString { (column, expression) ->
                "$column = $expression"
            }
        )
    }

}

@SQLBuilder
fun sql(block: SQLBuildScope.() -> Unit): SQL {
    return SQLBuildScope().apply(block).build()
}