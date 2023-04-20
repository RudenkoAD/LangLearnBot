import org.ktorm.database.Database
import org.ktorm.support.postgresql.PostgreSqlDialect


object DatabaseManager{
    val database = Database.connect("jdbc:postgresql://${System.getenv("DATABASE_IP")}:${System.getenv("DATABASE_PORT")}/${
        System.getenv(
            "DATABASE_NAME"
        )
    }",  user = System.getenv("DATABASE_USER"), password = System.getenv("DATABASE_PASSWORD"),  dialect = PostgreSqlDialect())
}