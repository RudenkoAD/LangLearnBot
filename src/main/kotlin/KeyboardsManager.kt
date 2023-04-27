import dev.inmo.tgbotapi.extensions.utils.types.buttons.dataButton
import dev.inmo.tgbotapi.extensions.utils.types.buttons.inlineKeyboard
import dev.inmo.tgbotapi.utils.row

object KeyboardsManager {
    fun getEmptyKeyboard() = inlineKeyboard {}

    fun getMainMenuKeyboard() =
        inlineKeyboard {
            row { dataButton(text = "Translate!", data = MovementCallback.Translation) }
            row { dataButton(text = "Go to favourite words", data = MovementCallback.Favourite)}
            row{ dataButton(text = "Choose a language to translate to", data = MovementCallback.LanguageChoice) }
        }
    fun getFavouriteKeyboard() =
        inlineKeyboard {
        row { dataButton(text = "Show favourite words!", data = FavouriteKeyboardCallback.ShowFavouriteWords) }
        row { dataButton(text = "Back", data = MovementCallback.Menu) }
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
    fun getTranslationResultKeyboard() =
        inlineKeyboard {
            row{
                dataButton(text = "More Examples", data = TranslationKeyboardCallback.MoreExamples)
                dataButton(text = "Add to favourite", data = TranslationKeyboardCallback.AddToFavourite)
            }
            row{
                dataButton(text="Back to Menu", data = MovementCallback.Menu)
            }
        }
}

object MovementCallback{
    const val Menu = "GoToMainMenu"
    const val Translation = "GoToTranslation"
    const val Favourite = "GoToFavourite"
    const val LanguageChoice = "GoToLanguageChoice"
}

object TranslationKeyboardCallback{
    const val MoreExamples = "MoreExamples"
    const val AddToFavourite = "AddToFavourite"
}

object FavouriteKeyboardCallback{
    const val ShowFavouriteWords = "ShowFavouriteWords"
}