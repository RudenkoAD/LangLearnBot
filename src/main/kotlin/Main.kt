import database.User
import database.Users
import database.users
import dev.inmo.tgbotapi.extensions.api.edit.reply_markup.editMessageReplyMarkup
import dev.inmo.tgbotapi.extensions.api.edit.text.editMessageText
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitAnyContentMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitDataCallbackQuery
import dev.inmo.tgbotapi.extensions.behaviour_builder.telegramBotWithBehaviourAndFSMAndStartLongPolling
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.command
import dev.inmo.tgbotapi.extensions.utils.extensions.parseCommandsWithParams
import dev.inmo.tgbotapi.extensions.utils.usernameChatOrNull
import dev.inmo.tgbotapi.types.message.HTMLParseMode
import dev.inmo.tgbotapi.types.message.content.TextContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.dsl.insert
import org.ktorm.dsl.update
import org.ktorm.entity.find
import org.ktorm.support.postgresql.PostgreSqlDialect
import reverso.LanguageCode
import reverso.ReversoTranslatorAPI
import java.lang.System.getenv

fun detectLanguage(text: String): LanguageCode =
    // Detect text language. Returns language code (en, ru, fr etc.). Use symbol search
    //language codes are [ru, en, de, ar, es, fr, he, it, ja, ko, nl, pl, pt, ro, sv, tr, zh, uk]
    if (text.contains(Regex("[а-яА-Я]"))) {
        LanguageCode("ru")
    }
    else {
        LanguageCode("en")
    }



suspend fun main(args: Array<String>) {
    val translator = ReversoTranslatorAPI()
    // bot token = getenv("BOT_TOKEN") or args.first() if None
    val botToken = getenv("BOT_TOKEN") ?: args.first()
    val database = Database.connect("jdbc:postgresql://${getenv("DATABASE_IP")}:${getenv("DATABASE_PORT")}/${getenv("DATABASE_NAME")}",  user = getenv("DATABASE_USER"), password = getenv("DATABASE_PASSWORD"),  dialect = PostgreSqlDialect())


    telegramBotWithBehaviourAndFSMAndStartLongPolling<BotState>(
        botToken,
        CoroutineScope(Dispatchers.IO),
        onStateHandlingErrorHandler = { state, e ->
            when (state) {
                is ExpectTranslationRequest -> {
                    println("Thrown error on ExpectTranslationRequest")
                }
                is StopState -> {
                    println("Thrown error on StopState")
                }
                else -> { println("thrown error inside of a void")}
            }
            e.printStackTrace()
            state
        }
    )//sets up the bot, now the behaviour builder:
    {
        strictlyOn<MainMenu>{
            val mm = MessagesManager(it.context)
            val km= KeyboardsManager()
            val msg = it.menuMessage?:sendMessage(it.context, mm.getMainMenuMessage(), replyMarkup = km.getPageOneKeyboard())
            it.menuMessage = msg
            val callback = waitDataCallbackQuery().filter {query -> query.from.id == it.context.id}.first()
            val content = callback.data
            when{
                content=="GoToTranslation" -> {
                    println("translation")
                    ExpectTranslationRequest(it.context, it.user)
                }
                content=="GoToPage2" -> {
                    editMessageReplyMarkup(msg, replyMarkup = km.getPageTwoKeyboard())
                    it
                }
                content=="GoToPage1" -> {
                    editMessageReplyMarkup(msg, replyMarkup = km.getPageOneKeyboard())
                    it
                }
                content=="GoToLanguageChoice" -> {
                    editMessageText(msg, mm.getLanguageChoiceMessage())
                    editMessageReplyMarkup(msg, replyMarkup = km.getLanguageChoiceKeyboard())
                    it
                }
                content.startsWith("ChangelangTo_") -> {
                    editMessageText(msg, mm.getMainMenuMessage())
                    editMessageReplyMarkup(msg, replyMarkup = km.getPageOneKeyboard())
                    try {
                        database.update(Users) { user ->
                            set(user.targetLanguage, content.substring(13, 15))
                            where { user.chatId eq msg.chat.id.chatId.toString() }
                        }
                    } catch (e: Exception){
                        reply(msg, mm.getDatabaseErrorMessage())
                        print(e)
                    }
                    it.user.targetLanguage = content.substring(13, 15)
                    it
                }
                else -> {it}}
        }

        strictlyOn<ExpectTranslationRequest> {
            val mm = MessagesManager(it.context)
            sendMessage(it.context, mm.getTranslationRequestMessage())
            val contentMessage = waitAnyContentMessage().filter {message -> message.chat.id == it.context.id}.first()
            val content = contentMessage.content

            when {
                content is TextContent && content.parseCommandsWithParams().keys.contains("stop") -> StopState(it.context, it.user)
                content is TextContent -> {
                    if (content.text.length > 100) {
                        reply(contentMessage, mm.getTextToLongMessage())
                    }
                    try {
                        val language = detectLanguage(content.text)
                        val targetLang = it.user.targetLanguageCode //?:if (language.code == "en") LanguageCode("ru") else LanguageCode("en")
                        val translated = translator.translate(content.text, language, targetLang)
                        reply(contentMessage, mm.getTranslationResultMessage(from=language, to=targetLang, translation=translated), parseMode = HTMLParseMode)
                    } catch (e: Exception) {
                        reply(contentMessage, mm.getTranslationErrorMessage())
                        print(e)
                    }
                    it
                }
                else -> {
                    reply(contentMessage, mm.getSomeErrorMessage())
                    StopState(it.context, it.user)
                }
            }
        }

        strictlyOn<StopState> {
            MainMenu(it.context, it.user)
        }

        command("start") {message ->
            // insert user to database if not exists (with same chatId)
            var user: User? = database.users.find { it.chatId eq message.chat.id.chatId.toString() }
            if (user == null) {
                val username : String= message.chat.usernameChatOrNull()?.username?.usernameWithoutAt ?: "%username%"
                database.insert(Users) {
                    set(it.chatId, message.chat.id.chatId.toString())
                    set(it.name, username)
                    set(it.targetLanguage, "en")
                }
                user = database.users.find { it.chatId eq message.chat.id.chatId.toString() }!!
            }
            startChain(MainMenu(message.chat, user, null))

        }

    }.second.join()
}
