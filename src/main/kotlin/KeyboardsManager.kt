import dev.inmo.tgbotapi.extensions.utils.types.buttons.dataButton
import dev.inmo.tgbotapi.extensions.utils.types.buttons.inlineKeyboard
import dev.inmo.tgbotapi.utils.row

class KeyboardsManager {
    fun getPageOne() =
        inlineKeyboard {
            row {
                dataButton(text = "Translate!", data = "GoToTranslation")
            }
            row {
                dataButton(text = "go to 2nd page", data = "GoToPage2")
            }
        }
    fun getPageTwo() =
        inlineKeyboard {
        row {
            dataButton(text = "Translate!", data = "GoToTranslation")
        }
        row {
            dataButton(text = "go to 1st page", data = "GoToPage1")
        }
    }
}