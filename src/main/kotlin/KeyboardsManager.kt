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
            //language codes are [ru, en, de, ar, es, fr, he, it, ja, ko, nl, pl, pt, ro, sv, tr, zh, uk]
            row{dataButton(text = "Russian \uD83C\uDDF7\uD83C\uDDFA", data = "ChangelangTo_ru")}
            row{dataButton(text = "English \uD83C\uDDFA\uD83C\uDDF8", data = "ChangelangTo_en")}
            row{dataButton(text = "German \uD83C\uDDE9\uD83C\uDDEA", data = "ChangelangTo_de")}
            row{dataButton(text = "French \uD83C\uDDEB\uD83C\uDDF7", data = "ChangelangTo_fr")}
            row{dataButton(text = "Chinese \uD83C\uDDE8\uD83C\uDDF3", data = "ChangelangTo_zh")}
        }
}