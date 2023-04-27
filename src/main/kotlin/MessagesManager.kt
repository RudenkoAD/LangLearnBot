import dev.inmo.tgbotapi.extensions.utils.usernameChatOrNull
import dev.inmo.tgbotapi.types.chat.Chat
import dev.inmo.tgbotapi.types.chat.User
import dev.inmo.tgbotapi.types.message.textsources.botCommand
import models.response.TranslationResponse
import models.response.prettifyUsage
import reverso.LanguageCode
import reverso.languageEmoji

object MessagesManager{
    fun getMainMenuMessage(chat: Chat): String {
        return "Welcome to the main menu, ${chat.usernameChatOrNull()?.username?.usernameWithoutAt}! I am translator bot. I can help you to learn foreign languages. Use menu below to start."
    }
    fun getTranslationRequestMessage(): String {
        return "Please, send me a text to translate. Send ${botCommand("stop").asText} to cancel translation."
    }

    fun getLanguageChoiceMessage(): String = "Please Choose a language to translate to"

    fun getTranslationResultMessage(translation: TranslationResponse, num_examples:Int = 3): String {
        val from = LanguageCode(translation.request.source_lang)
        val to = LanguageCode(translation.request.target_lang)
        return """(${from.languageEmoji} -> ${to.languageEmoji}):${translation.request.source_text}
            |${translation.dictionary_entry_list.take(4).joinToString(separator = "\n* ", prefix = "* ") { it.term }}.
            | <b>Usages</b>
            | ${translation.usages.take(num_examples).map { it.prettifyUsage("<i>", "</i>") }.joinToString(separator = "\n* ", prefix = "* ") {it.t_text}}
        """.trimMargin()
    }
    fun getTranslationErrorMessage(): String = "Sorry, I can't translate this text. Please, try again."

    fun getTextTooLongMessage(): String = "Sorry, your text is too long."

    fun getSomeErrorMessage(): String {
        return "Something went wrong. Please, try again. If you see this message again use ${botCommand("start").command} to restart the bot."
    }

    fun getFavouriteMessage(): String = "Welcome to the favourite words menu!\nHere you can see the list of your favourite words, and learn them!"
    fun getDatabaseErrorMessage(): String = "Error trying to store something in the database"

}