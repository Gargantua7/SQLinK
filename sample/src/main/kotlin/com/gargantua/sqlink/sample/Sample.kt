package com.gargantua.sqlink.sample

import com.gargantua7.sqlink.*
import com.gargantua7.sqlink.builder.QueryResultSet
import com.gargantua7.sqlink.builder.sql

enum class Gender {
    MALE, FEMALE
}

object Person: Table("Person") {

    val id = text("id")
    val name = text("name")
    val age = number("age")
    val gender = enum<Gender>("gender")

    val spouse = text("spouse")
    val remark = text("remark")
}

fun main() {

    sql {

        update(Person) {
            it.age set (it.age + 1)
            it.remark set null
        }
        where {
            and {
                exp(Person.age more 18)
                or {
                    and {
                        exp(Person.age less 60)
                        exp(Person.gender eq Gender.MALE)
                    }

                    and {
                        exp(Person.age less 55)
                        exp(Person.gender eq Gender.FEMALE)
                    }
                }
            }
        }

    }.let(::println)

    sql {

        select(Person.all)
        from(Person)
        where { (Person.gender eq Gender.FEMALE) or ((Person.age less 18) and (Person.name like "Michel")) }
        orderBy(Person.id.asc, Person.age.desc)

    }.let(::println)

    sql {
        sql {

            select(Person.id AS "uid")
            from(Person)
            where { (Person.age more 60) and (Person.gender eq Gender.MALE) }

        }
        union
        sql {

            select(Person.id AS "uid")
            from(Person)
            where { (Person.age more 55) and (Person.gender eq Gender.FEMALE) }
        }

        orderBy(Person.id.asc, Person.age.desc)
    }.let(::println)

    sql {

        val deadBook = listOf("001")

        delete(Person)
        where { Person.id IN deadBook }

    }.let(::println)

    sql {
        select(count(Person.gender eq Gender.FEMALE))
        from(Person)
    }.let(::println)

    sql {

        val queryResultSet = QueryResultSet("res")

        with(queryResultSet) {
            select(Person.all)
            from(Person)
            where { Person.age more 18 }
        }
        select(Person.id AS "uid")
        from(queryResultSet)
        where { Person.gender eq Gender.MALE }

    }.let(::println)

    sql {
        val selfTable = Person AS "self"
        val spouseTable = Person AS "spouse"

        select(selfTable { name } AS "name", spouseTable { name } AS "spouse_name")
        from(selfTable)
        leftJoin(spouseTable) {
            selfTable { spouse } eq spouseTable { id }
        }
        where {
            spouseTable { id }.isNotNull
        }
    }.let(::println)

}