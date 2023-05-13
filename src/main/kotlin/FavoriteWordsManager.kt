import models.Word
import models.Words
import org.ktorm.database.Database
import org.ktorm.dsl.*
import java.time.LocalDateTime

object FavoriteWordsManager {
    private val database = DatabaseManager.database
    fun addWordToFavorite(chatId: String, word: String, wordTranslation: String, sourceLanguage: String, targetLanguage: String) {
        database.insert(Words) { it->
            set(it.word, word)
            set(it.wordTranslation, wordTranslation)
            set(it.chatId, chatId)
            set(it.sourceLanguage, sourceLanguage)
            set(it.targetLanguage, targetLanguage)
            set(it.wordLastAsked, LocalDateTime.now())
            set(it.wordCorrectAnswers, 0)
            set(it.wordIncorrectAnswers, 0)
        }
    }

    fun removeWordFromFavorite(chatId: String, word: String) {
        database.delete(Words) { it->
            it.id eq chatId + word
        }
    }

    fun getUserFavoriteWords(chatId: String): List<Word> {
        return database.from(Words).select().where { Words.chatId eq chatId }.map { Words.createEntity(it) }
    }

    fun isWordInFavorite(chatId: String, word: String): Boolean {
        return database.from(Words).select().where { Words.chatId eq chatId }.map { Words.createEntity(it) }.any { it.word == word }
    }

    fun getUserTrainWords(chatId: String): List<Word> {
        throw NotImplementedError()
        //TODO("Create trainings logics")
    }
}