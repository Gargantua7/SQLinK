package com.gargantua.sqlink.sample

import com.gargantua7.sqlink.Table
import com.gargantua7.sqlink.builder.sql
import com.gargantua7.sqlink.builder.union
import com.gargantua7.sqlink.enum
import com.gargantua7.sqlink.number
import com.gargantua7.sqlink.text

enum class Gender {
    MALE, FEMALE
}

object Person: Table("Person") {

    val id = text("id")
    val name = text("name")
    val age = number("age")
    val gender = enum<Gender>("gender")
}

fun main() {

    (sql {

        select(Person.id, Person.name, Person.age)
        from(Person)
        where { (Person.gender eq Gender.FEMALE) or ((Person.age less 18) and (Person.name like "Michel")) }
        orderBy(Person.id.asc, Person.age.desc)

    } union sql {

        select(Person.id, Person.name, Person.age)
        from(Person)
        where {
            or {
                and {
                    exp(Person.age moreEq 60)
                    exp(Person.gender eq Gender.MALE)
                }

                and {
                    exp(Person.age moreEq 55)
                    exp(Person.gender eq Gender.FEMALE)
                }
            }
        }
        orderBy(Person.id.asc, Person.age.desc)

    }).let(::println)

}