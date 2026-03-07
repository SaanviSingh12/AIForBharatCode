package com.sahayak.android.i18n

/**
 * Language configuration matching the backend's supported locales.
 */
data class LanguageConfig(
    val code: String,        // short code: "en", "hi", …
    val awsCode: String,     // BCP-47 for AWS: "en-IN", "hi-IN", …
    val name: String,        // English name
    val nativeName: String,  // Name in native script
    val voiceInput: Boolean, // AWS Transcribe support
    val voiceOutput: Boolean, // AWS Polly support
)

val languageConfigs: List<LanguageConfig> = listOf(
    LanguageConfig("en", "en-IN", "English", "English",     voiceInput = true,  voiceOutput = true),
    LanguageConfig("hi", "hi-IN", "Hindi",   "हिंदी",        voiceInput = true,  voiceOutput = true),
    LanguageConfig("ta", "ta-IN", "Tamil",   "தமிழ்",        voiceInput = true,  voiceOutput = false),
    LanguageConfig("te", "te-IN", "Telugu",  "తెలుగు",       voiceInput = true,  voiceOutput = false),
    LanguageConfig("bn", "bn-IN", "Bengali", "বাংলা",        voiceInput = false, voiceOutput = false),
    LanguageConfig("mr", "mr-IN", "Marathi", "मराठी",        voiceInput = true,  voiceOutput = false),
    LanguageConfig("gu", "gu-IN", "Gujarati","ગુજરાતી",      voiceInput = false, voiceOutput = false),
    LanguageConfig("kn", "kn-IN", "Kannada", "ಕನ್ನಡ",        voiceInput = true,  voiceOutput = false),
    LanguageConfig("ml", "ml-IN", "Malayalam","മലയാളം",      voiceInput = false, voiceOutput = false),
    LanguageConfig("pa", "pa-IN", "Punjabi", "ਪੰਜਾਬੀ",       voiceInput = false, voiceOutput = false),
)

private val configByCode = languageConfigs.associateBy { it.code }

fun isVoiceInputSupported(code: String): Boolean =
    configByCode[code.substringBefore("-")]?.voiceInput == true

fun isVoiceOutputSupported(code: String): Boolean =
    configByCode[code.substringBefore("-")]?.voiceOutput == true

fun getAwsLanguageCode(code: String): String =
    configByCode[code.substringBefore("-")]?.awsCode ?: "hi-IN"
