import models.User
import dev.inmo.micro_utils.fsm.common.State
import dev.inmo.tgbotapi.types.chat.Chat
import dev.inmo.tgbotapi.types.message.abstracts.ContentMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import models.response.TranslationResponse

sealed interface BotState : State
data class TranslationState(override val context: Chat, val user:User, val tracedMessage: ContentMessage<TextContent>? = null, var tracedTranslation: TranslationResponse? = null) : BotState
data class MainMenuState(override val context: Chat, val user:User, val tracedMessage: ContentMessage<TextContent>? = null) : BotState
data class LanguageChoiceState(override val context: Chat, val user:User, val tracedMessage: ContentMessage<TextContent>) : BotState
data class StopState(override val context: Chat, val user:User) : BotState
data class FavouriteState(override val context: Chat, val user:User, val tracedMessage: ContentMessage<TextContent>) : BotState