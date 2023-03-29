package database

import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*

object Users : Table<User>("t_users") {
    val id = int("id").primaryKey().bindTo { it.id }
    val name = varchar("name").bindTo { it.name }
    val chatId = varchar("chat_id").bindTo { it.chatId }
}

interface User : Entity<User> {
    val id: Int
    val name: String
    val chatId: String
}

val Database.users get() = this.sequenceOf(Users)
