package com.gargantua7.sqlink.builder

import com.gargantua7.sqlink.*

abstract class AbstractSQLBuildScope {


    fun table(table: Table): String = table.tableName

    fun <T: Table> table(alias: TableAlias<T>): String = "${alias.table.tableName} ${alias.alias}"

    fun table(column: QueryResultSet): String = column.alias

    fun column(column: Column): String = column.name

    inline fun <T: Table> column(table: T, block: T.() -> Column) = table.block().name

    inline fun <T: Table> column(alias: TableAlias<T>, block: T.() -> Column) =
        TableAliasColumn(alias.alias, alias.table.block())

    fun real(value: Number): NumberExpression =
        NumberExpression(value)

    fun string(it: Any) = StringExpression(it.toString())

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

    fun tempColumn(name: String) = Column(name)

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
}

@SQLBuilder
class SQLBuildScope internal constructor(internal val sb: StringBuilder): AbstractSQLBuildScope() {

    @SQLBuilder
    fun sql(block: SQLBuildScope.() -> Unit) {
        sb.append(
            buildString {
                block(SQLBuildScope(this))
            }
        )
    }

    operator fun String.unaryPlus() {
        sb.appendLine(this.trim())
    }

    operator fun SQL.unaryPlus() {
        sb.appendLine(sql.trim())
    }

    fun select(vararg column: IColumn) = + ("SELECT " + column.joinToString())

    fun select(columns: List<IColumn>) = + ("SELECT " + columns.joinToString())

    inline fun select(block: SelectScope.() -> Unit) = select(buildList { SelectScope(this).block() })

    fun update(table: Table) = + "UPDATE ${table(table)}"

    fun from(table: Table) = + "FROM ${table(table)}"

    fun from(table: QueryResultSet) = + "FROM ${table(table)}"

    inline fun from(crossinline block: SQLBuildScope.() -> Unit) = sql {
        + "FROM ("
        this.block()
        + ")"
    }

    fun <T: Table> from(table: TableAlias<T>) = + "FROM ${table(table)}"

    inline fun <T: Table> leftJoin(table: TableAlias<T>, crossinline on: SQLBuildScope.() -> Unit) = sql {
        + "LEFT JOIN ${table(table)} ON ("
        on()
        + ")"
    }

    inline fun set(crossinline block: UpdateSetScope.() -> Unit) = sql {
        + ("SET " + (
                buildList {
                    UpdateSetScope(this).block()
                }.joinToString(separator = ", \n") { (column, expression) ->
                    "$column = $expression"
                }
                ))

    }

    fun where(operation: SingleConditionScope.() -> IBooleanExpression) = + ("WHERE " + SingleConditionScope().let(operation))

    fun orderBy(vararg order: OrderExpression) = + ("ORDER BY " + order.joinToString())

    fun groupBy(vararg column: ExpressionColumn) = + ("GROUP BY " + column.joinToString())

    inline fun with(res: QueryResultSet, crossinline block: SQLBuildScope.() -> Unit) =
        sql {
            +"WITH ${res.alias} AS ("
            block()
            +")"
        }

    infix fun TableAliasColumn.link(other: TableAliasColumn) = "$this = $other"

    infix fun Expression.AS(alias: Column) =
        ExpressionColumnAlias(this, alias)

    fun Expression.over(
        partitionBy: List<ExpressionColumn>? = null,
        orderBy: List<OrderExpression>? = null,
        rows: Pair<OverRowsType, OverRowsType?>? = null
    ) = OverExpression(this, partitionBy, orderBy, rows)

    val IColumn.asc get() = OrderExpression.Asc(this)

    val IColumn.desc get() = OrderExpression.Desc(this)
}

@SQLBuilder
open class ConditionScope internal constructor(): AbstractSQLBuildScope() {

    infix fun <T> ExpressionColumn.IN(collection: Collection<T>) = BooleanExpression("$this IN ${collection(collection)}")

    infix fun <T> ExpressionColumn.notIN(collection: Collection<T>) = BooleanExpression("$this NOT IN ${collection(collection)}")

    infix fun ExpressionColumn.IN(sql: SQL) = BooleanExpression("$this IN (${sql.sql.trim()})")

    infix fun ExpressionColumn.notIN(sql: SQL) = BooleanExpression("$this NOT IN (${sql.sql.trim()})")

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

    infix fun ExpressionColumn.like(expression: ITextExpression) = BooleanExpression("$this LIKE $expression")

    infix fun ExpressionColumn.like(expression: String) = BooleanExpression("$this LIKE $expression")

    val ExpressionColumn.isNull get() = BooleanExpression("$this IS NULL")

    val ExpressionColumn.isNotNull get() = BooleanExpression("$this IS NOT NULL")
}

@SQLBuilder
class SingleConditionScope internal constructor(): ConditionScope() {

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
class MultiConditionScope internal constructor(): ConditionScope() {
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

@SQLBuilder
class SelectScope(private val list: MutableList<IColumn>) {

    operator fun IColumn.unaryPlus() {
        list.add(this)
    }

    infix fun Expression.AS(alias: Column) =
        ExpressionColumnAlias(this, alias)

        fun Expression.over(
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

@SQLBuilder
fun sql(block: SQLBuildScope.() -> Unit): SQL {

    return SQL(buildString {
        block(SQLBuildScope(this))
    })
}

infix fun SQL.union(other: SQL): SQL {
    return SQL(sql + "UNION\n" + other.sql)
}