package models

import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.Table
import org.ktorm.schema.varchar
import reverso.LanguageCode

object Users : Table<User>("t_users") {
    val name = varchar("name").bindTo { it.name }
    val chatId = varchar("chat_id").primaryKey().bindTo { it.chatId }
    val targetLanguage = varchar("target_language").bindTo { it.targetLanguage }
}

interface User : Entity<User> {
    val name: String
    var targetLanguage: String
    val targetLanguageCode get() = LanguageCode(targetLanguage)
    val chatId: String
}

val Database.users get() = this.sequenceOf(Users)
