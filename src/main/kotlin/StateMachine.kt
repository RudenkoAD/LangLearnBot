import database.User
import dev.inmo.micro_utils.fsm.common.State
import dev.inmo.tgbotapi.types.chat.Chat
import dev.inmo.tgbotapi.types.message.abstracts.ContentMessage
import dev.inmo.tgbotapi.types.message.content.TextContent

sealed interface BotState : State
data class ExpectTranslationRequest(override val context: Chat, val user:User) : BotState
data class MainMenu(override val context: Chat, val user:User, var menuMessage: ContentMessage<TextContent>? = null) : BotState
data class StopState(override val context: Chat, val user:User) : BotState