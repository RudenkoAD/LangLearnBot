import dev.inmo.micro_utils.fsm.common.State
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.send.send
import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitAnyContentMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitDataCallbackQuery
import dev.inmo.tgbotapi.extensions.behaviour_builder.telegramBotWithBehaviourAndFSMAndStartLongPolling
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.command
import dev.inmo.tgbotapi.extensions.utils.extensions.parseCommandsWithParams
import dev.inmo.tgbotapi.extensions.utils.extensions.sameThread
import dev.inmo.tgbotapi.extensions.utils.types.buttons.InlineKeyboardMarkup
import dev.inmo.tgbotapi.types.IdChatIdentifier
import dev.inmo.tgbotapi.types.buttons.InlineKeyboardButtons.CallbackDataInlineKeyboardButton
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.utils.botCommand
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first


fun detectLanguage(text: String): String {
    // Detect text language. Returns language code (en, ru, fr etc.). Use symbol search
    if (text.contains(Regex("[а-яА-Я]"))) {
        return "ru"
    }
    else {
        return "en"
    }
}


sealed interface BotState : State
data class MainMenu(override val context: IdChatIdentifier, val sourceMessage: CommonMessage<TextContent>) : BotState
data class ExpectTranslationRequest(override val context: IdChatIdentifier, val sourceMessage: CommonMessage<TextContent>) : BotState
data class StopState(override val context: IdChatIdentifier, val sourceMessage: CommonMessage<TextContent>) : BotState

suspend fun main(args: Array<String>) {
    val translator = ReversoTranslatorAPI()
    val botToken = args.first()

    telegramBotWithBehaviourAndFSMAndStartLongPolling<BotState>(
        botToken,
        CoroutineScope(Dispatchers.IO),
        onStateHandlingErrorHandler = { state, e ->
            when (state) {
                is ExpectTranslationRequest -> {
                    println("Thrown error on ExpectContentOrStopState")
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
            val keyboard2 = InlineKeyboardMarkup(CallbackDataInlineKeyboardButton(text = "Translate!", callbackData = "GoToTranslation"))
            sendMessage(it.context, text="welcome to the main menu", replyMarkup =  keyboard2 )
            //val callback = waitDataCallbackQuery().filter {callback -> callback.chatInstance == it.context.toString()}.first()
            val callback = waitDataCallbackQuery().first()
            val content = callback.data
            println(content)
            when(content){
                "GoToTranslation" -> {
                    println("yes")
                    ExpectTranslationRequest(it.context, it.sourceMessage)
                }
                else -> {
                    println("no")
                    it
                }
            }
        }

        strictlyOn<ExpectTranslationRequest> {
            send(
                it.context,
            ) {
                +"Send me some garbage you want translated, you piece of shit,  or send " + botCommand("stop") + " if you want to stop me, you fucker"
            }
            val contentMessage = waitAnyContentMessage().filter { message ->
                message.sameThread(it.sourceMessage)
            }.first()
            val content = contentMessage.content

            when {
                content is TextContent && content.parseCommandsWithParams().keys.contains("stop") -> StopState(it.context, it.sourceMessage)
                content is TextContent -> {
                    if (content.text.length > 100) {
                        reply(contentMessage, "Text is too long")
                    }
                    try {
                        val language = detectLanguage(content.text)
                        val targetLang = if (language == "en") "ru" else "en"
                        val translated = translator.translate(content.text, language, targetLang)
                        reply(contentMessage, translated.dictionary_entry_list[0].term)
                    } catch (e: Exception) {
                        reply(contentMessage, "К сожалению перевода данного слова не найдено. Попробуйте еще раз")
                    }
                    it
                }
                else -> {
                    reply(contentMessage, "fuck you")
                    StopState(it.context, it.sourceMessage)
                }
            }
        }

        strictlyOn<StopState> {
            send(it.context) { + "Edmund McMillen, You litte F**ker You made a shit of piece with your trash Issac it’s f**King Bad this trash game I will become back my money I hope you will in your next time a cow on a trash farm you sucker" }

            null
        }

        command("start") {
            startChain(MainMenu(it.chat.id, it))
        }

    }.second.join()
}
