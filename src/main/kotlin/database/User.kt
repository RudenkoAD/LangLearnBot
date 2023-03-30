package database

import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*
import reverso.LanguageCode

object Users : Table<User>("t_users") {
    val id = int("id").primaryKey().bindTo { it.id }
    val name = varchar("name").bindTo { it.name }
    val chatId = varchar("chat_id").bindTo { it.chatId }
    val targetLanguage = varchar("target_language").bindTo { it.targetLanguage }
}

interface User : Entity<User> {
    val id: Int
    val name: String
    val targetLanguage: String
    val targetLanguageCode get() = LanguageCode(targetLanguage)
    val chatId: String
}

val Database.users get() = this.sequenceOf(Users)
