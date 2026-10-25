package pe.yachaywasi

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.min
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val GREETING = "¡Hola! Soy Tito. ¿Listo para descubrir algo nuevo?"

@Composable
fun HomeScreen(go: (String) -> Unit) {
    Store.version.intValue
    var askGrade by remember { mutableStateOf(Store.grade == 0) }
    LaunchedEffect(Unit) { delay(900); Speaker.speak(GREETING) }
    Column(Modifier.fillMaxSize().background(C.Sand).padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.weight(1f))
            IconBtn(if (Store.sound) "🔊" else "🔇") { Store.sound = !Store.sound; if (!Store.sound) Speaker.stop() }
            IconBtn("🔁") { Speaker.speak(GREETING) }
            IconBtn("❓") { go("tutorial") }
        }
        Text("YACHAY WASI", fontSize = 34.sp, fontWeight = FontWeight.ExtraBold, color = C.Orange)
        Text("Colegio Virtual", fontSize = 20.sp, color = C.Ink)
        Text("Aprender descubriendo, crecer pensando", fontSize = 13.sp, color = C.Ink)
        Spacer(Modifier.height(8.dp))
        TitoBubble(GREETING)
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                BigButton("Aventuras", "🗺️", C.Green, Modifier.weight(1f).fillMaxWidth()) { go("adventures") }
                BigButton("Laboratorio de la Mente", "🧠", C.Purple, Modifier.weight(1f).fillMaxWidth()) { go("lab") }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                BigButton("Aprender", "📚", C.Sky, Modifier.weight(1f).fillMaxWidth()) { go("learn") }
                BigButton("Mi Progreso", "⭐", C.Orange, Modifier.weight(1f).fillMaxWidth()) { go("progress") }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text("Yachay Wasi es una plataforma de apoyo al aprendizaje. No reemplaza la matrícula escolar.", fontSize = 11.sp, textAlign = TextAlign.Center, color = C.Ink)
    }
    if (askGrade) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Un adulto elige el grado") },
            text = { Text("¿Qué grado va a trabajar el niño? Se puede cambiar después en el panel para adultos.") },
            confirmButton = { TextButton({ Store.grade = 1; askGrade = false }) { Text("1.º de primaria", fontSize = 18.sp) } },
            dismissButton = { TextButton({ Store.grade = 2; askGrade = false }) { Text("2.º de primaria", fontSize = 18.sp) } }
        )
    }
}

// ---------- TUTORIAL ----------
private class Step(val say: String, val emoji: String, val label: String)

@Composable
fun TutorialScreen(onExit: () -> Unit, onHome: () -> Unit) {
    val steps = listOf(
        Step("Toca este botón grande.", "👆", "Tócame"),
        Step("Toca el altavoz para escuchar otra vez.", "🔊", "Escuchar"),
        Step("Elige la respuesta. ¿Cuál es la manzana?", "🍎", ""),
        Step("Para volver atrás, toca la flecha.", "⬅️", "Volver"),
        Step("Para repetir una actividad, toca este botón.", "🔁", "Repetir"),
        Step("Para ir a otra aventura, toca la flecha grande.", "➡️", "Seguir"),
        Step("Inclina el celular a un lado y mira cómo se mueve Tito.", "🐹", "Ya probé")
    )
    var i by remember { mutableIntStateOf(0) }
    val tilt = rememberTilt(true)
    var tick by remember { mutableIntStateOf(0) }
    var px by remember { mutableFloatStateOf(0f) }
    var py by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(i) { Speaker.speak(steps[i].say) }
    LaunchedEffect(i == steps.size - 1) {
        if (i == steps.size - 1) {
            tilt.calibrate()
            while (true) {
                withFrameNanos { it }
                val v = tilt.vector()
                px = (px + v.first * 4f).coerceIn(-120f, 120f)
                py = (py + v.second * 4f).coerceIn(-120f, 120f)
                tick++
            }
        }
    }
    fun next() { if (i < steps.size - 1) i++ else { Store.seenTutorial = true; onExit() } }
    val s = steps[i]
    Column(Modifier.fillMaxSize().background(C.Sand)) {
        TopBar("Tutorial  ${i + 1} de ${steps.size}", onExit, onHome) { Speaker.speak(s.say) }
        Column(Modifier.weight(1f).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            TitoBubble(s.say)
            Spacer(Modifier.height(30.dp))
            when (i) {
                2 -> Row(Modifier.fillMaxWidth().height(120.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(20.dp)).background(Color.White).clickable { next() }, contentAlignment = Alignment.Center) { Text("🍎", fontSize = 56.sp) }
                    Box(Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(20.dp)).background(Color.White).clickable { Speaker.speak("Casi. Mira otra vez, la manzana es roja y redonda.") }, contentAlignment = Alignment.Center) { Text("🐟", fontSize = 56.sp) }
                }
                6 -> {
                    Box(Modifier.size(300.dp).clip(RoundedCornerShape(24.dp)).background(Color(0xFFF3DFA8)), contentAlignment = Alignment.Center) {
                        tick
                        Text("🐹", Modifier.offset(px.dp, py.dp), fontSize = 52.sp)
                    }
                    Text(if (tilt.available) "Inclina: ⬅️ ➡️ ⬆️ ⬇️" else "Este celular no tiene sensor: en los laberintos usarás botones.", fontSize = 15.sp, color = C.Ink, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(10.dp))
                    BigButton(s.label, "✅", C.Green, Modifier.fillMaxWidth().height(90.dp)) { next() }
                }
                else -> BigButton(s.label, s.emoji, C.Green, Modifier.fillMaxWidth().height(170.dp)) {
                    if (i == 1) Speaker.speak("¡Muy bien! Así se escucha otra vez.")
                    next()
                }
            }
        }
    }
}

// ---------- AVENTURAS (mapa tipo Mario Bros) ----------
@Composable
fun AdventuresScreen(go: (String) -> Unit, onHome: () -> Unit) {
    Store.version.intValue
    val places = Content.places
    val nodes = places.map { p -> MapNode(p.name, p.emoji, p.activity?.let { Store.stars(it.id) } ?: 0, p.activity != null) }
    MarioMap("🗺️ Mapa de aventuras", "Toca un lugar abierto y Tito caminará hasta allí.", "adv", nodes, { go("adv/${places[it].id}") }, onHome, onHome)
}

// ---------- APRENDER ----------
@Composable
fun LearnScreen(go: (String) -> Unit, onHome: () -> Unit) {
    Store.version.intValue
    val grade = Store.grade.coerceAtLeast(1)
    Column(Modifier.fillMaxSize().background(C.Sand)) {
        TopBar("📚 Aprender · $grade.º grado", onHome, onHome)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            TitoBubble("Elige qué quieres aprender.")
            Card(Modifier.fillMaxWidth(), border = C.Orange, onClick = { go("quipu") }) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("🎀", fontSize = 38.sp)
                    Column {
                        Text("El camino del Quipu", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = C.Ink)
                        Text("Niveles con cuerdas y nudos para contar, sumar y restar", fontSize = 13.sp, color = C.Ink)
                    }
                }
            }
            for (a in Content.learn(grade)) {
                Card(Modifier.fillMaxWidth(), border = C.Sky, onClick = { go("learn/${a.id}") }) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(a.emoji, fontSize = 38.sp)
                        Column {
                            Text(a.title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = C.Ink)
                            Text("${a.area}  ·  ${starsText(Store.stars(a.id))}", fontSize = 13.sp, color = C.Ink)
                        }
                    }
                }
            }
            Card(Modifier.fillMaxWidth(), border = C.Red, onClick = { go("english") }) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("🇬🇧", fontSize = 38.sp)
                    Column {
                        Text("Mis primeras 50 palabras en inglés", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = C.Ink)
                        Text("Escucha y mira. No necesitas saber leer.", fontSize = 13.sp, color = C.Ink)
                    }
                }
            }
        }
    }
}

// ---------- INGLÉS ----------
@Composable
fun EnglishMenu(go: (String) -> Unit, onBack: () -> Unit, onHome: () -> Unit) {
    Store.version.intValue
    Column(Modifier.fillMaxSize().background(C.Sand)) {
        TopBar("🇬🇧 Inglés: 50 palabras", onBack, onHome)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            for (c in English.cats) {
                Card(Modifier.fillMaxWidth(), border = C.Red, onClick = { go("eng/${c.id}") }) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(c.emoji, fontSize = 36.sp)
                        Column {
                            Text(c.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = C.Ink)
                            Text(starsText(Store.stars("eng.${c.id}")), fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EnglishCategory(cat: WordCat, go: (String) -> Unit, onBack: () -> Unit, onHome: () -> Unit) {
    var shown by remember { mutableStateOf<Word?>(null) }
    Column(Modifier.fillMaxSize().background(C.Sand)) {
        TopBar("${cat.emoji} ${cat.name}", onBack, onHome) { shown?.let { Speaker.speak(it.en, "en") } }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            TitoBubble(shown?.let { "${it.en} = ${it.es}" } ?: "Toca cada tarjeta para escuchar la palabra.")
            for (w in cat.words) {
                Card(Modifier.fillMaxWidth(), border = if (shown == w) C.Green else null, onClick = { shown = w; Speaker.speak(w.en, "en") }) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(w.emoji, fontSize = 44.sp)
                        Text(w.en, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = C.Ink, modifier = Modifier.weight(1f))
                        Text("🔊", fontSize = 28.sp)
                    }
                }
            }
            BigButton("Jugar: escucha y toca", "🎧", C.Green, Modifier.fillMaxWidth().height(110.dp)) { go("eng/${cat.id}/play") }
        }
    }
}

// ---------- MI PROGRESO ----------
@Composable
fun ProgressScreen(go: (String) -> Unit, onHome: () -> Unit) {
    Store.version.intValue
    val total = Store.totalStars()
    Column(Modifier.fillMaxSize().background(C.Sand)) {
        TopBar("⭐ Mi Progreso", onHome, onHome)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            val who = Store.name.ifBlank { "explorador" }
            TitoBubble("¡Hola, $who! Tienes $total ⭐")
            Text("Mis insignias", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = C.Ink)
            Rewards.badges.forEach { b ->
                val on = total >= b.need
                Card(Modifier.fillMaxWidth(), color = if (on) Color.White else Color(0xFFE6DCC3)) {
                    Text("${if (on) b.emoji else "🔒"} ${b.name}  ${if (on) "" else "(${b.need} ⭐)"}", fontSize = 18.sp, color = C.Ink)
                }
            }
            Text("La casa de Tito", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = C.Ink)
            Card(Modifier.fillMaxWidth()) {
                Text("🏠 " + Rewards.decor.filter { total >= it.need }.joinToString(" ") { it.emoji }.ifBlank { "(vacía: ¡gana estrellas!)" }, fontSize = 30.sp)
                val nextD = Rewards.decor.firstOrNull { total < it.need }
                if (nextD != null) Text("Próximo objeto: ${nextD.emoji} ${nextD.name} con ${nextD.need} ⭐", fontSize = 14.sp, color = C.Ink)
            }
            BigButton("Para adultos", "🔒", C.Purple, Modifier.fillMaxWidth().height(90.dp)) { go("adults") }
        }
    }
}

// ---------- PANEL PARA ADULTOS ----------
@Composable
fun AdultsScreen(onBack: () -> Unit, onHome: () -> Unit) {
    val q = remember { val a = (6..9).random(); val b = (6..9).random(); Triple(a, b, a * b) }
    var open by remember { mutableStateOf(false) }
    var input by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().background(C.Sand)) {
        TopBar("🔒 Para adultos", onBack, onHome)
        if (!open) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Pregunta solo para adultos: ¿cuánto es ${q.first} × ${q.second}?", fontSize = 20.sp, color = C.Ink)
                OutlinedTextField(input, { input = it.filter { c -> c.isDigit() }.take(3) }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), label = { Text("Respuesta") })
                BigButton("Entrar", "✅", C.Green, Modifier.fillMaxWidth().height(80.dp)) { if (input == q.third.toString()) open = true else input = "" }
            }
        } else AdultsPanel()
    }
}

@Composable
private fun AdultsPanel() {
    Store.version.intValue
    var confirm by remember { mutableStateOf(false) }
    val fmt = remember { SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Card(Modifier.fillMaxWidth()) {
            Text("Perfil local (solo en este celular)", fontWeight = FontWeight.Bold, color = C.Ink)
            OutlinedTextField(Store.name, { Store.name = it }, singleLine = true, label = { Text("Nombre o apodo (opcional)") })
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Grado:", color = C.Ink)
                listOf(1, 2).forEach { g ->
                    Box(Modifier.clip(RoundedCornerShape(12.dp)).background(if (Store.grade == g) C.Green else Color.LightGray).clickable { Store.grade = g }.padding(horizontal = 16.dp, vertical = 10.dp)) {
                        Text("$g.º", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Sonido y voz de Tito", Modifier.weight(1f), color = C.Ink)
                Switch(Store.sound, { Store.sound = it })
            }
            Text("Idioma disponible: español (quechua: pendiente de validación con hablantes).", fontSize = 12.sp, color = C.Ink)
            if (Speaker.lastError.isNotEmpty()) Text("⚠ ${Speaker.lastError}. Instala la voz en Ajustes > Texto a voz.", fontSize = 12.sp, color = C.Red)
        }
        val all = Content.allActivities + English.cats.map { English.listenGame(it) }
        val rows = (all.map { it.id to it.title } + listOf("lab.memory" to "Memoria visual", "lab.patterns" to "Patrones", "lab.puzzle" to "Rompecabezas",
            "lab.classify" to "Clasificación", "lab.maze1" to "Laberinto 1", "lab.maze2" to "Laberinto 2", "lab.maze3" to "Laberinto 3") + (1..8).map { "quipu.$it" to "Quipu: nivel $it" })
        Card(Modifier.fillMaxWidth()) {
            Text("Progreso por actividad", fontWeight = FontWeight.Bold, color = C.Ink)
            Text("Aciertos / errores / pistas · veces completada · estrellas", fontSize = 12.sp, color = C.Ink)
            var any = false
            for ((id, t) in rows) {
                val ok = Store.ok(id); val er = Store.err(id); val h = Store.hints(id)
                if (ok + er + Store.done(id) == 0) continue
                any = true
                Text("• $t: $ok / $er / $h · ${Store.done(id)}× · ${starsText(Store.stars(id))}", fontSize = 13.sp, color = C.Ink)
            }
            if (!any) Text("Aún no hay actividad registrada.", fontSize = 13.sp, color = C.Ink)
        }
        val reinforce = rows.filter { (id, _) -> Store.err(id) > Store.ok(id) && Store.err(id) > 0 }
        Card(Modifier.fillMaxWidth()) {
            Text("Posibles refuerzos", fontWeight = FontWeight.Bold, color = C.Ink)
            if (reinforce.isEmpty()) Text("Sin necesidades de refuerzo por ahora.", fontSize = 13.sp, color = C.Ink)
            reinforce.forEach { Text("• ${it.second}: conviene repetirla juntos.", fontSize = 13.sp, color = C.Ink) }
            Text("Estos datos son orientativos; no son un diagnóstico psicológico ni una evaluación oficial.", fontSize = 11.sp, color = C.Ink)
        }
        Card(Modifier.fillMaxWidth()) {
            Text("Historial reciente", fontWeight = FontWeight.Bold, color = C.Ink)
            Store.history().take(15).forEach {
                val p = it.split("|")
                if (p.size >= 4) Text("${fmt.format(Date(p[0].toLongOrNull() ?: 0L))}  ${p[3]}  ${starsText(p[2].toIntOrNull() ?: 0)}", fontSize = 12.sp, color = C.Ink)
            }
        }
        Card(Modifier.fillMaxWidth()) {
            Text("Privacidad: sin publicidad, sin cuentas, sin internet. Todo se guarda solo en este celular.", fontSize = 12.sp, color = C.Ink)
            Text("Yachay Wasi no está autorizado ni acreditado por el Ministerio de Educación; es apoyo al aprendizaje.", fontSize = 12.sp, color = C.Ink)
        }
        BigButton("Borrar todo el progreso", "🗑️", C.Red, Modifier.fillMaxWidth().height(80.dp)) { confirm = true }
    }
    if (confirm) AlertDialog(
        onDismissRequest = { confirm = false },
        title = { Text("¿Borrar todo el progreso?") }, text = { Text("Esta acción no se puede deshacer.") },
        confirmButton = { TextButton({ Store.reset(); confirm = false }) { Text("Borrar") } },
        dismissButton = { TextButton({ confirm = false }) { Text("Cancelar") } })
}
