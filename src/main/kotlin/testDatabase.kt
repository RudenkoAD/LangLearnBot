import models.Users
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.support.postgresql.PostgreSqlDialect

// This function is created to check correct database settings and connection

fun main() {
    val database = Database.connect("jdbc:postgresql://${System.getenv("DATABASE_IP")}:${System.getenv("DATABASE_PORT")}/${
        System.getenv(
            "DATABASE_NAME"
        )
    }",  user = System.getenv("DATABASE_USER"), password = System.getenv("DATABASE_PASSWORD"),  dialect = PostgreSqlDialect())

    for (row in database.from(Users).select()) {
        println(row[Users.name])
    }
    database.insert(Users) {it->
        set(it.chatId, "123_test_test")
        set(it.name, "test")
    }
    database.delete(Users) {it->
        it.chatId eq "123_test_test"
    }
}