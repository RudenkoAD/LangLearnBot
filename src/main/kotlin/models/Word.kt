package models

import models.response.TranslationResponse
import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.Table
import org.ktorm.schema.varchar
import org.ktorm.schema.int
import org.ktorm.schema.datetime
import reverso.LanguageCode
import reverso.ReversoTranslatorAPI
import java.time.LocalDateTime

object Words : Table<Word>("t_words") {
    val id = varchar("id").primaryKey().bindTo { it.id }
    val word = varchar("word").primaryKey().bindTo { it.word }
    val wordTranslation = varchar("word_translation").bindTo { it.wordTranslation }
    val chatId = varchar("chat_id").bindTo { it.chatId }
    val sourceLanguage = varchar("source_language").bindTo { it.sourceLanguage }
    val targetLanguage = varchar("target_language").bindTo { it.targetLanguage }
    val wordLastAsked = datetime("word_last_asked").bindTo { it.wordLastAsked }
    val wordCorrectAnswers = int("word_correct_answers").bindTo { it.wordCorrectAnswers }
    val wordIncorrectAnswers = int("word_incorrect_answers").bindTo { it.wordIncorrectAnswers }
}

interface Word : Entity<Word> {
    val id: String
    val word: String
    val wordTranslation: String
    val chatId: String
    val sourceLanguage: String
    val targetLanguage: String
    val wordLastAsked: LocalDateTime
    val wordCorrectAnswers: Int
    val wordIncorrectAnswers: Int
    val sourceLanguageCode get() = LanguageCode(sourceLanguage)
    val targetLanguageCode get() = LanguageCode(targetLanguage)
}

fun Word.getTranslation(translator: ReversoTranslatorAPI): TranslationResponse {
    return translator.translate(this.word, this.sourceLanguageCode, this.targetLanguageCode)
}

val Database.words get() = this.sequenceOf(Words)
