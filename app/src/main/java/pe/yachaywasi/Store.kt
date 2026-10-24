package pe.yachaywasi

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableIntStateOf

/** Todo el progreso se guarda SOLO en el celular (SharedPreferences). No hay cuentas ni servidores. */
object Store {
    private lateinit var sp: SharedPreferences
    val version = mutableIntStateOf(0)

    fun init(ctx: Context) { sp = ctx.getSharedPreferences("yachay", Context.MODE_PRIVATE) }
    private fun bump() { version.intValue = version.intValue + 1 }
    private fun touch() { version.intValue }

    var grade: Int
        get() { touch(); return sp.getInt("grade", 0) }          // 0 = aún no elegido
        set(v) { sp.edit().putInt("grade", v).apply(); bump() }
    var sound: Boolean
        get() { touch(); return sp.getBoolean("sound", true) }
        set(v) { sp.edit().putBoolean("sound", v).apply(); bump() }
    var name: String
        get() { touch(); return sp.getString("name", "") ?: "" }
        set(v) { sp.edit().putString("name", v.take(20)).apply(); bump() }
    var seenTutorial: Boolean
        get() { touch(); return sp.getBoolean("tut", false) }
        set(v) { sp.edit().putBoolean("tut", v).apply(); bump() }
    fun levelOf(game: String): Int { touch(); return sp.getInt("lv.$game", 0) }
    fun setLevel(game: String, v: Int) { sp.edit().putInt("lv.$game", v.coerceIn(0, 2)).apply(); bump() }

    fun ok(id: String): Int { touch(); return sp.getInt("ok.$id", 0) }
    fun err(id: String): Int { touch(); return sp.getInt("er.$id", 0) }
    fun hints(id: String): Int { touch(); return sp.getInt("hi.$id", 0) }
    fun done(id: String): Int { touch(); return sp.getInt("dn.$id", 0) }
    fun stars(id: String): Int { touch(); return sp.getInt("st.$id", 0) }

    fun record(id: String, correct: Boolean) {
        val k = if (correct) "ok.$id" else "er.$id"
        sp.edit().putInt(k, sp.getInt(k, 0) + 1).apply(); bump()
    }
    fun hint(id: String) { sp.edit().putInt("hi.$id", sp.getInt("hi.$id", 0) + 1).apply(); bump() }

    fun finish(id: String, stars: Int, title: String) {
        val best = maxOf(stars, sp.getInt("st.$id", 0))
        val hist = (sp.getString("hist", "") ?: "").lines().filter { it.isNotBlank() }.takeLast(59)
        val line = "${System.currentTimeMillis()}|$id|$stars|${title.replace("|", " ")}"
        sp.edit()
            .putInt("st.$id", best)
            .putInt("dn.$id", sp.getInt("dn.$id", 0) + 1)
            .putString("hist", (hist + line).joinToString("\n"))
            .apply()
        bump()
    }

    fun totalStars(): Int { touch(); return sp.all.entries.filter { it.key.startsWith("st.") }.sumOf { (it.value as? Int) ?: 0 } }
    fun completedIds(): List<String> { touch(); return sp.all.keys.filter { it.startsWith("dn.") }.map { it.removePrefix("dn.") }.sorted() }
    fun history(): List<String> { touch(); return (sp.getString("hist", "") ?: "").lines().filter { it.isNotBlank() }.reversed() }
    fun reset() { sp.edit().clear().apply(); bump() }
}

data class Badge(val name: String, val emoji: String, val need: Int)
data class Decor(val name: String, val emoji: String, val need: Int)

object Rewards {
    val badges = listOf(
        Badge("Primer paso", "🥾", 1), Badge("Explorador", "🧭", 5), Badge("Buscador de tesoros", "💎", 12),
        Badge("Sabio del cuy", "🦉", 25), Badge("Gran inventor", "💡", 40)
    )
    val decor = listOf(
        Decor("Brote", "🌱", 2), Decor("Moño andino", "🎀", 4), Decor("Flor", "🌼", 7),
        Decor("Lámpara", "🏮", 10), Decor("Mariposa", "🦋", 14), Decor("Arcoíris", "🌈", 20),
        Decor("Estrella", "🌟", 30)
    )
}
