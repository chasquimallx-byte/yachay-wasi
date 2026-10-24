package pe.yachaywasi

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * Voz de Tito con el motor de texto-a-voz de Android (funciona sin internet si el celular
 * tiene instalada la voz en español / inglés). No usa IA ni servidores.
 */
object Speaker : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var ready = false
    var lastError: String = ""
        private set

    fun init(ctx: Context) {
        if (tts == null) tts = TextToSpeech(ctx.applicationContext, this)
    }

    override fun onInit(status: Int) {
        ready = status == TextToSpeech.SUCCESS
        if (ready) tts?.setSpeechRate(0.85f) else lastError = "Sin motor de voz"
    }

    fun speak(text: String, lang: String = "es") {
        if (!Store.sound || !ready) return
        val t = tts ?: return
        val options = if (lang == "en") listOf(Locale.US, Locale.ENGLISH)
        else listOf(Locale("es", "PE"), Locale("es", "US"), Locale("es", "ES"), Locale("es"))
        for (loc in options) {
            val r = t.setLanguage(loc)
            if (r != TextToSpeech.LANG_MISSING_DATA && r != TextToSpeech.LANG_NOT_SUPPORTED) {
                t.speak(text, TextToSpeech.QUEUE_FLUSH, null, "yw")
                return
            }
        }
        lastError = "Falta la voz ($lang)"
    }

    fun stop() { tts?.stop() }
}
