# SQLinK

**SQLinK is a Kotlin Multiplatform, lightweight, type-safe DSL for building SQL query strings. Without any platform code or third-party dependency**

> Only support SQLite

It helps you construct complex SQL queries programmatically without worrying about string concatenation errors or SQL injection vulnerabilities that can arise from manual string building. SQLinK focuses purely on generating the SQL string; it **does not include any database drivers** or execute queries. You are responsible for using the generated SQL string with your preferred database connection library.

## Features

*   **Type-Safe Builders**: Define your tables and columns with Kotlin types, reducing errors.
*   **Expressive DSL**: Write SQL queries in a way that feels natural in Kotlin.
*   **Readability**: Generated SQL can be easily logged or inspected.
*   **Zero Platform-Specific Code**: The core library is pure Kotlin and does not contain any `actual`/`expect` implementations tied to specific platforms.
*   **No Third-Party Runtime Dependencies**: SQLinK is self-contained and adds no external library dependencies to your project, keeping it lean and minimizing potential conflicts.

## Getting Started

To use SQLinK in your Kotlin project, add the following dependency to your `build.gradle.kts` file:

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("com.gargantua.sqlink:sqlink:1.0.0")
        }
    }
}
```

## Usage

Below are examples demonstrating how to use SQLinK, based on the `Sample.kt` provided in the `Sample` module.

### 1. Defining a Table

First, define your table structure using the `Table` object and property delegates:

```kotlin

enum class Gender {
    MALE, FEMALE
}

object Person : Table("Person") {
    val id = text("id")
    val name = text("name")
    val age = number("age")
    val gender = enum<Gender>("gender") // Type-safe enum column
    val spouse = text("spouse") // Foreign key, an ID of another Person
    val remark = text("remark")
}
```

### 2. Building SQL Queries

Use the `sql { ... }` builder to construct your queries.

**a. SELECT Query**

```kotlin
val selectQuery = sql {
    select(Person.all) // Select all columns from Person
    from(Person)
    where { (Person.gender eq Gender.FEMALE) or ((Person.age less 18) and (Person.name like "Michel")) }
    orderBy(Person.id.asc, Person.age.desc)
}
println(selectQuery)
// Output (example):
// SELECT Person.id, Person.name, Person.age, Person.gender, Person.spouse, Person.remark FROM Person WHERE ((Person.gender = 'FEMALE') OR ((Person.age < 18) AND (Person.name LIKE 'Michel'))) ORDER BY Person.id ASC, Person.age DESC
```

**b. UPDATE Statement**

```kotlin
val updateQuery = sql {
    update(Person) {
        it.age set (it.age + 1) // Increment age
        it.remark set null      // Set remark to null
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
}
println(updateQuery)
// Output (example):
// UPDATE Person SET Person.age = (Person.age + 1), Person.remark = NULL WHERE ((Person.age > 18) AND (((Person.age < 60) AND (Person.gender = 'MALE')) OR ((Person.age < 55) AND (Person.gender = 'FEMALE'))))
```

**c. DELETE Statement**

```kotlin
val userIdsToDelete = listOf("001", "002")
val deleteQuery = sql {
    delete(Person)
    where { Person.id IN userIdsToDelete }
}
println(deleteQuery)
// Output (example):
// DELETE FROM Person WHERE Person.id IN ('001', '002')
```

**d. UNION of SELECT Statements**

```kotlin
val unionQuery = sql {
    sql { // First SELECT
        select(Person.id AS "uid")
        from(Person)
        where { (Person.age more 60) and (Person.gender eq Gender.MALE) }
    }
    union // UNION operator
    sql { // Second SELECT
        select(Person.id AS "uid")
        from(Person)
        where { (Person.age more 55) and (Person.gender eq Gender.FEMALE) }
    }
    orderBy(Person.id.asc) // Optional: Order the results of the UNION
    // Note: orderBy after UNION might need to refer to aliased columns or positional indexes
    // depending on the SQL dialect if column names are not identical or aliased consistently.
    // For simplicity, this example reuses Person.id which might not be valid if "uid" is the only column.
    // A more robust orderBy for the union would be orderBy("uid".asc) if "uid" is the common alias.
}
println(unionQuery)
// Output (example):
// (SELECT Person.id AS uid FROM Person WHERE ((Person.age > 60) AND (Person.gender = 'MALE'))) UNION (SELECT Person.id AS uid FROM Person WHERE ((Person.age > 55) AND (Person.gender = 'FEMALE'))) ORDER BY Person.id ASC
```

**e. Aggregate Functions (e.g., COUNT)**

```kotlin
val countQuery = sql {
    select(count(Person.gender eq Gender.FEMALE) AS "female_count") // Count females
    from(Person)
}
println(countQuery)
// Output (example):
// SELECT COUNT(Person.gender = 'FEMALE') AS female_count FROM Person
```

**f. Common Table Expressions (CTE / WITH clause)**

```kotlin
val cteQuery = sql {
    val adultPersons = QueryResultSet("adult_persons") // Define a CTE name

    with(adultPersons) { // Define the CTE body
        select(Person.all)
        from(Person)
        where { Person.age more 18 }
    }
    // Now use the CTE in the main query
    select(Person.id AS "uid", Person.name) // Referencing original Person columns, assuming adult_persons has them
    from(adultPersons) // Select from the CTE
    where { Person.gender eq Gender.MALE } // Filter within the CTE results
}
println(cteQuery)
// Output (example):
// WITH adult_persons AS (SELECT Person.id, Person.name, Person.age, Person.gender, Person.spouse, Person.remark FROM Person WHERE (Person.age > 18)) SELECT Person.id AS uid, Person.name FROM adult_persons WHERE (Person.gender = 'MALE')

```

**g. JOINs (e.g., LEFT JOIN with table aliases)**

```kotlin
val joinQuery = sql {
    val selfTable = Person AS "self"         // Alias for Person table
    val spouseTable = Person AS "spouse_alias" // Another alias for Person table (for the spouse)

    select(
        selfTable { name } AS "person_name",    // Select name from 'self'
        spouseTable { name } AS "spouse_name" // Select name from 'spouse_alias'
    )
    from(selfTable)
    leftJoin(spouseTable) { // Perform a LEFT JOIN
        selfTable { spouse } eq spouseTable { id } // Join condition
    }
    where {
        spouseTable { id }.isNotNull // Ensure there is a spouse
    }
}
println(joinQuery)
// Output (example):
// SELECT self.name AS person_name, spouse_alias.name AS spouse_name FROM Person self LEFT JOIN Person spouse_alias ON (self.spouse = spouse_alias.id) WHERE spouse_alias.id IS NOT NULL
```

**h. CASE Expressions**

```kotlin
val caseQuery = sql {
    select(
        Person.name,
        case(Person.gender) {                     // CASE expression on gender
            WHEN(Gender.MALE, string("M"))     // WHEN MALE THEN 'M'
            WHEN(Gender.FEMALE, string("F"))   // WHEN FEMALE THEN 'F'
            ELSE(string("O"))                  // ELSE 'O' (Optional)
        } AS "gender_code"                       // Alias for the case expression result
    )
    from(Person)
}
println(caseQuery)
// Output (example):
// SELECT Person.name, CASE Person.gender WHEN 'MALE' THEN 'M' WHEN 'FEMALE' THEN 'F' ELSE 'O' END AS gender_code FROM Person
```

These examples cover some of the core functionalities of SQLinK. Explore the library for more advanced features and combinations.

## Contributing

Contributions are welcome! Please feel free to submit issues or pull requests.

## License

This project is licensed under the Apache 2.0 License.
