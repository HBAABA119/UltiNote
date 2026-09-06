package com.ultinote.app.ink

/**
 * Handwriting convert-to-text languages. Tags are BCP-47 and resolve to ML Kit
 * on-device models at convert time — an unsupported tag simply reports
 * "unavailable" instead of crashing, so this list is safe to grow.
 */
data class ConvertLanguage(
    val tag: String,
    val englishName: String,
    val nativeName: String,
    val rtl: Boolean = false
)

object ConvertLanguages {
    val all: List<ConvertLanguage> = listOf(
        ConvertLanguage("en", "English", "English"),
        ConvertLanguage("de", "German", "Deutsch"),
        ConvertLanguage("ur", "Urdu", "اردو", rtl = true),
        ConvertLanguage("ar", "Arabic", "العربية", rtl = true),
        ConvertLanguage("az", "Azerbaijani", "Azərbaycanca"),
        ConvertLanguage("fr", "French", "Français"),
        ConvertLanguage("es", "Spanish", "Español"),
        ConvertLanguage("tr", "Turkish", "Türkçe"),
        ConvertLanguage("fa", "Persian", "فارسی", rtl = true),
        ConvertLanguage("hi", "Hindi", "हिन्दी"),
        ConvertLanguage("bn", "Bengali", "বাংলা"),
        ConvertLanguage("pa", "Punjabi", "ਪੰਜਾਬੀ"),
        ConvertLanguage("pt", "Portuguese", "Português"),
        ConvertLanguage("it", "Italian", "Italiano"),
        ConvertLanguage("nl", "Dutch", "Nederlands"),
        ConvertLanguage("ru", "Russian", "Русский"),
        ConvertLanguage("uk", "Ukrainian", "Українська"),
        ConvertLanguage("pl", "Polish", "Polski"),
        ConvertLanguage("sv", "Swedish", "Svenska"),
        ConvertLanguage("id", "Indonesian", "Bahasa Indonesia"),
        ConvertLanguage("ms", "Malay", "Bahasa Melayu"),
        ConvertLanguage("vi", "Vietnamese", "Tiếng Việt"),
        ConvertLanguage("th", "Thai", "ไทย"),
        ConvertLanguage("zh", "Chinese (Simplified)", "简体中文"),
        ConvertLanguage("ja", "Japanese", "日本語"),
        ConvertLanguage("ko", "Korean", "한국어"),
        ConvertLanguage("he", "Hebrew", "עברית", rtl = true)
    )

    fun forTag(tag: String): ConvertLanguage = all.firstOrNull { it.tag == tag } ?: all[0]
}
