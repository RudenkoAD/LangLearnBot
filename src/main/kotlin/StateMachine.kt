import dev.inmo.micro_utils.fsm.common.State
import dev.inmo.tgbotapi.types.IdChatIdentifier
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent

sealed interface BotState : State
data class ExpectTranslationRequest(override val context: IdChatIdentifier, val sourceMessage: CommonMessage<TextContent>) :
    BotState
data class MainMenu(override val context: IdChatIdentifier, val sourceMessage: CommonMessage<TextContent>) : BotState
data class StopState(override val context: IdChatIdentifier, val sourceMessage: CommonMessage<TextContent>) : BotState