import dev.inmo.micro_utils.fsm.common.State
import dev.inmo.tgbotapi.extensions.api.edit.reply_markup.editMessageReplyMarkup
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.send.send
import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitAnyContentMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitDataCallbackQuery
import dev.inmo.tgbotapi.extensions.behaviour_builder.telegramBotWithBehaviourAndFSMAndStartLongPolling
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.command
import dev.inmo.tgbotapi.extensions.utils.extensions.parseCommandsWithParams
import dev.inmo.tgbotapi.types.IdChatIdentifier
import dev.inmo.tgbotapi.types.message.abstracts.ContentMessage
import dev.inmo.tgbotapi.extensions.utils.extensions.sameThread
import dev.inmo.tgbotapi.extensions.utils.types.buttons.InlineKeyboardMarkup
import dev.inmo.tgbotapi.types.buttons.InlineKeyboardButtons.CallbackDataInlineKeyboardButton
import dev.inmo.tgbotapi.types.message.HTMLParseMode
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.utils.RiskFeature
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import reverso.LanguageCode


fun detectLanguage(text: String): LanguageCode {
    // Detect text language. Returns language code (en, ru, fr etc.). Use symbol search
    return if (text.contains(Regex("[а-яА-Я]"))) {
        LanguageCode("ru")
    }
    else {
        LanguageCode("en")
    }
}


@OptIn(RiskFeature::class)
suspend fun main(args: Array<String>) {
    val translator = ReversoTranslatorAPI()
    val botToken = args.first()
    val menumanager = MenuManager()
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
            val msg = it.menuMessage?:sendMessage(it.context, text="welcome to the main menu", replyMarkup = menumanager.getPageOne())
            val callback = waitDataCallbackQuery().first()
            val content = callback.data

            when(content){
                "GoToTranslation" -> {
                    println("translation")
                    ExpectTranslationRequest(it.context)
                }
                "GoToPage2" -> {
                    editMessageReplyMarkup(msg.chat.id, msg.messageId, replyMarkup = menumanager.getPageTwo())
                    MainMenu(it.context, msg)
                }
                "GoToPage1" -> {
                    editMessageReplyMarkup(msg.chat.id, msg.messageId, replyMarkup = menumanager.getPageOne())
                    MainMenu(it.context, msg)
                }
                else -> {it}
            }
        }

        strictlyOn<ExpectTranslationRequest> {
            val mm = MessagesManager(it.sourceMessage.chat)
            send(
                it.context,
            ) {
                +mm.getTranslationRequestMessage()
            }
            val contentMessage = waitAnyContentMessage().first()
            val content = contentMessage.content

            when {
                content is TextContent && content.parseCommandsWithParams().keys.contains("stop") -> StopState(it.context)
                content is TextContent -> {
                    if (content.text.length > 100) {
                        reply(contentMessage, mm.getTextToLongMessage())
                    }
                    try {
                        val language = detectLanguage(content.text)
                        val targetLang = if (language.code == "en") LanguageCode("ru") else LanguageCode("en")
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
                    StopState(it.context, it.sourceMessage)
                }
            }
        }

        strictlyOn<StopState> {
            send(it.context) { + "Edmund McMillen, You litte F**ker You made a shit of piece with your trash Issac it’s f**King Bad this trash game I will become back my money I hope you will in your next time a cow on a trash farm you sucker" }
            null
        }

        command("start") {
            startChain(MainMenu(it.chat.id, null))
        }

    }.second.join()
}
