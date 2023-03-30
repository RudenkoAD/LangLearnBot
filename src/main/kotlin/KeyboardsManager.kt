import dev.inmo.tgbotapi.extensions.utils.types.buttons.dataButton
import dev.inmo.tgbotapi.extensions.utils.types.buttons.inlineKeyboard
import dev.inmo.tgbotapi.utils.row

class KeyboardsManager {
    fun getPageOneKeyboard() =
        inlineKeyboard {
            row { dataButton(text = "Translate!", data = "GoToTranslation") }
            row { dataButton(text = "Go to 2nd page", data = "GoToPage2") }
            row{ dataButton(text = "Choose a language to translate to", data = "GoToLanguageChoice") }
        }
    fun getPageTwoKeyboard() =
        inlineKeyboard {
        row { dataButton(text = "Translate!", data = "GoToTranslation") }
        row { dataButton(text = "Go to 1st page", data = "GoToPage1") }
    }
    fun getLanguageChoiceKeyboard() =
        inlineKeyboard {
            row{dataButton(text = "Russian", data = "ChangelangTo_ru")}
            row{dataButton(text = "English", data = "ChangelangTo_en")}
            row{dataButton(text = "French", data = "ChangelangTo_fr")}
            row{dataButton(text = "Chinese", data = "ChangelangTo_ch")}
        }
}