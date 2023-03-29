package database

import org.ktorm.entity.Entity
import org.ktorm.schema.*

object Users : Table<Nothing>("t_users") {
    val id = int("id").primaryKey()
    val name = varchar("name")
    val chat_id = varchar("chat_id")
}

interface User : Entity<User> {
    val id: Int
    val name: String
    val chat_id: Int
}
