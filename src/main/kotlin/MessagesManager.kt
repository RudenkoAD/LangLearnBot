import dev.inmo.tgbotapi.extensions.utils.usernameChatOrNull
import dev.inmo.tgbotapi.types.chat.Chat
import dev.inmo.tgbotapi.types.message.textsources.botCommand
import models.response.TranslationResponse
import models.response.prettifyUsage
import reverso.LanguageCode
import reverso.languageEmoji

class MessagesManager(private val chat: Chat) {
    fun getMainMenuMessage(): String {
        return "Welcome to the main menu, ${chat.usernameChatOrNull()?.username?.usernameWithoutAt}! I am translator bot. I can help you to learn foreign languages. Use menu below to start."
    }
    fun getTranslationRequestMessage(): String {
        return "Please, send me a text to translate. Send ${botCommand("stop").asText} to cancel translation."
    }
    fun getTranslationResultMessage(translation: TranslationResponse, from: LanguageCode, to: LanguageCode): String {
        return """(${from.languageEmoji} -> ${to.languageEmoji}):
            |${translation.dictionary_entry_list.take(4).joinToString(separator = "\n* ", prefix = "* ") { it.term }}.
            | <b>Usages</b>
            | ${translation.usages.take(3).map { it.prettifyUsage("<i>", "</i>") }.joinToString(separator = "\n* ", prefix = "* ") {it.s_text}}
        """.trimMargin()
    }
    fun getTranslationErrorMessage(): String {
        return "Sorry, I can't translate this text. Please, try again."
    }
    fun getTextToLongMessage(): String {
        return "Sorry, your text is too long. Please, try again."
    }
}