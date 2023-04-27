import models.User
import models.Users
import models.users
import dev.inmo.tgbotapi.extensions.api.deleteMessage
import dev.inmo.tgbotapi.extensions.api.edit.reply_markup.editMessageReplyMarkup
import dev.inmo.tgbotapi.extensions.api.edit.text.editMessageText
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitAnyContentMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitDataCallbackQuery
import dev.inmo.tgbotapi.extensions.behaviour_builder.telegramBotWithBehaviourAndFSMAndStartLongPolling
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.command
import dev.inmo.tgbotapi.extensions.utils.aggregateFlows
import dev.inmo.tgbotapi.extensions.utils.extensions.parseCommandsWithParams
import dev.inmo.tgbotapi.extensions.utils.usernameChatOrNull
import dev.inmo.tgbotapi.types.message.HTMLParseMode
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.MessageContent
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.types.queries.callback.DataCallbackQuery
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import org.ktorm.dsl.eq
import org.ktorm.dsl.insert
import org.ktorm.dsl.update
import org.ktorm.entity.find
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




suspend fun main() {
    val translator = ReversoTranslatorAPI()
    val botToken = getenv("BOT_TOKEN")
    val database = DatabaseManager.database


    telegramBotWithBehaviourAndFSMAndStartLongPolling(
        botToken,
        CoroutineScope(Dispatchers.IO),
        onStateHandlingErrorHandler = { state, e ->
            when (state) {
                is TranslationState -> {
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
        strictlyOn<MainMenuState>{
            val msg = it.tracedMessage?:sendMessage(it.context, MessagesManager.getMainMenuMessage(it.context), replyMarkup = KeyboardsManager.getMainMenuKeyboard())
            val dataflow = waitDataCallbackQuery().filter {query -> query.from.id == it.context.id}
            val msgflow = waitAnyContentMessage().filter {message -> message.chat.id == it.context.id}
            val flow = aggregateFlows(this, dataflow, msgflow)
            when (val callback = flow.first()){
                is DataCallbackQuery -> {
                    when (callback.data) {
                        MovementCallback.Translation -> {
                            deleteMessage(msg)
                            TranslationState(it.context, it.user)
                        }
                        MovementCallback.Favourite -> {
                            FavouriteState(it.context, it.user, msg)
                        }
                        MovementCallback.LanguageChoice -> {
                            LanguageChoiceState(it.context, it.user, msg)
                        }
                        else -> {
                            MainMenuState(it.context, it.user, msg)
                        }
                    }
                }
                is CommonMessage<*> -> {
                    sendMessage(it.context, "you are currently in the main menu. If you'd like to go to translate mode, please click the 'Translate!' button. If you can't see the main menu, please use the /start command")
                    it
                }
                else -> {
                    println("Got unexpectec callback in main menu: $callback")
                    it
                }
            }
        }

        strictlyOn<TranslationState> {
            val msg = it.tracedMessage?:sendMessage(it.context, MessagesManager.getTranslationRequestMessage())
            val dataflow = waitDataCallbackQuery().filter { query -> query.from.id == it.context.id }
            val msgflow = waitAnyContentMessage().filter { message -> message.chat.id == it.context.id }
            val flow = aggregateFlows(this, dataflow, msgflow)
            when (val callback = flow.first()) {
                is CommonMessage<MessageContent>->{
                    val content = callback.content
                    when {
                        content is TextContent && content.parseCommandsWithParams().keys.contains("stop") -> StopState(it.context, it.user)
                        content is TextContent -> {
                            if (content.text.length > 100) {
                                reply(callback, MessagesManager.getTextTooLongMessage())
                            }
                            val language = detectLanguage(content.text)
                            val targetLang = it.user.targetLanguageCode
                            if (language == targetLang){
                                print("detected same languages\n")
                                StopState(it.context, it.user)
                            }
                            try {
                                val translated = translator.translate(content.text, language, targetLang)
                                val newmsg = reply(callback,
                                    MessagesManager.getTranslationResultMessage(translation=translated),
                                    replyMarkup = KeyboardsManager.getTranslationResultKeyboard(),
                                    parseMode = HTMLParseMode
                                )
                                TranslationState(it.context, it.user, newmsg, translated)
                            }
                            catch (e: Exception) {
                                reply(callback, MessagesManager.getTranslationErrorMessage())
                                print("Error when translating from ${language.code} to ${targetLang.code}: ")
                                print(e)
                                StopState(it.context, it.user)
                            }
                        }
                        else -> {
                            reply(callback, MessagesManager.getSomeErrorMessage())
                            StopState(it.context, it.user)
                        }
                    }
                }
                is DataCallbackQuery->{
                    when (callback.data) {
                        //All of these callbacks must be coming from a translation message, which means tracedTranslation is not None
                        //TODO this way of handling it is bad lmao
                        TranslationKeyboardCallback.MoreExamples -> {
                            editMessageText(
                                message = msg,
                                text = MessagesManager.getTranslationResultMessage(it.tracedTranslation!!, 8),
                                replyMarkup = KeyboardsManager.getTranslationResultKeyboard(),
                                parseMode = HTMLParseMode
                            )
                            it
                        }
                        "AddToFavourite" -> {
                            FavoriteWordsManager.addWordToFavorite(
                                it.context.id.chatId.toString(),
                                it.tracedTranslation!!.dictionary_entry_list[0].term,//TODO this is dumb but idk how to choose a translation, probably another menu
                                it.tracedTranslation!!.request.source_text,
                                it.tracedTranslation!!.request.source_lang,
                                it.tracedTranslation!!.request.target_lang
                            )
                            it
                        }
                        else -> {
                            println("fuck")
                            it
                        }
                    }
                }
                else->it
            }
        }

        strictlyOn<LanguageChoiceState> {
            val msg = it.tracedMessage
            val dataflow = waitDataCallbackQuery().filter { query -> query.from.id == it.context.id }
            val msgflow = waitAnyContentMessage().filter { message -> message.chat.id == it.context.id }
            val flow = aggregateFlows(this, dataflow, msgflow)

            editMessageText(msg, MessagesManager.getLanguageChoiceMessage())
            editMessageReplyMarkup(msg, replyMarkup = KeyboardsManager.getLanguageChoiceKeyboard())

            when (val callback = flow.first()){
                is DataCallbackQuery -> {
                    val content = callback.data
                    when {
                        content.startsWith("ChangelangTo_") -> {
                            try {
                                database.update(Users) { user ->
                                    set(user.targetLanguage, content.substring(13, 15))
                                    where { user.chatId eq msg.chat.id.chatId.toString() }
                                }
                                it.user.targetLanguage = content.substring(13, 15)
                                editMessageText(msg, MessagesManager.getMainMenuMessage(it.context))
                                editMessageReplyMarkup(msg, replyMarkup = KeyboardsManager.getMainMenuKeyboard())
                                MainMenuState(it.context, it.user, it.tracedMessage)
                            } catch (e: Exception) {
                                reply(msg, MessagesManager.getDatabaseErrorMessage())
                                print(e)
                                it
                            }
                        }//TODO add reactions to other possible callbacks
                        else -> {
                            it
                        }
                    }
                }
                is CommonMessage<*> -> {it}//TODO add reactions to messages in language choice state
                else -> {it}
            }
        }

        strictlyOn<StopState> {
            MainMenuState(it.context, it.user)
        }

        strictlyOn<FavouriteState>{
            editMessageText(it.tracedMessage, text = MessagesManager.getFavouriteMessage(), replyMarkup = KeyboardsManager.getFavouriteKeyboard())
            val dataflow = waitDataCallbackQuery().filter { query -> query.from.id == it.context.id }
            val msgflow = waitAnyContentMessage().filter { message -> message.chat.id == it.context.id }
            val flow = aggregateFlows(this, dataflow, msgflow)

            MainMenuState(it.context, it.user, it.tracedMessage)

            when (val callback = flow.first()){//TODO add realisation to favouritestate
                is DataCallbackQuery -> {it}
                is CommonMessage<*> -> {it}
                else -> {it}
            }

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
            startChain(MainMenuState(message.chat, user, null))

        }

    }.second.join()
}
