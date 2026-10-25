package pe.yachaywasi

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * EL CAMINO DEL QUIPU — idea tomada de «Sansu» (matemática de 1.º en Japón): aprender cantidades,
 * sumas y restas manipulando material concreto, no memorizando. Adaptación al Perú: el material
 * son cuerdas con nudos (inspiradas en los quipus andinos). Niveles tipo Mario: se abre uno al completar el anterior.
 */
class QLevel(val title: String, val kind: String, val lo: Int, val hi: Int, val emoji: String)
class QRound(val kind: String, val a: Int, val b: Int)

object Quipu {
    val levels = listOf(
        QLevel("Mis primeros nudos", "build", 1, 3, "🎀"),
        QLevel("Más nudos", "build", 3, 6, "🎀"),
        QLevel("¿Cuál es más larga?", "compare", 1, 6, "⚖️"),
        QLevel("Unir cuerdas", "join", 1, 5, "🔗"),
        QLevel("Unir cuerdas grandes", "join", 1, 8, "🔗"),
        QLevel("Quitar nudos", "take", 3, 5, "✂️"),
        QLevel("Quitar más nudos", "take", 5, 8, "✂️"),
        QLevel("El gran quipu", "mix", 3, 8, "🏆")
    )

    fun makeRound(kind: String, l: QLevel): QRound = when (kind) {
        "build" -> QRound("build", (l.lo..l.hi).random(), 0)
        "compare" -> {
            val x = (l.lo..l.hi).random()
            var y = (l.lo..l.hi).random()
            while (y == x) y = (l.lo..l.hi).random()
            QRound("compare", x, y)
        }
        "join" -> { val x = (1..(l.hi - 1)).random(); QRound("join", x, (1..(l.hi - x)).random()) }
        else -> { val x = (l.lo..l.hi).random(); QRound("take", x, (1..(x - 1)).random()) }
    }

    fun rounds(l: QLevel): List<QRound> =
        List(3) { makeRound(if (l.kind == "mix") listOf("build", "join", "take", "compare").random() else l.kind, l) }

    fun say(r: QRound) = when (r.kind) {
        "build" -> "Pon ${r.a} nudos en la cuerda."
        "compare" -> "¿Qué cuerda tiene más nudos?"
        "join" -> "Una cuerda tiene ${r.a} nudos y la otra ${r.b}. Únelas."
        else -> "La cuerda tiene ${r.a} nudos. Quita ${r.b}. ¿Cuántos quedan?"
    }

    fun hint(r: QRound) = when (r.kind) {
        "build" -> "Cuenta cada nudo con el dedo: uno, dos, tres…"
        "compare" -> "Cuenta los nudos de cada cuerda. La que tiene más nudos es más larga."
        "join" -> "Cuenta todos los nudos juntos: los de la primera cuerda y los de la segunda."
        else -> "Quita los nudos de uno en uno y cuenta los que se quedan."
    }
}

@Composable
fun QuipuMap(go: (String) -> Unit, onBack: () -> Unit, onHome: () -> Unit) {
    Store.version.intValue
    val nodes = Quipu.levels.mapIndexed { i, l -> MapNode(l.title, l.emoji, Store.stars("quipu.${i + 1}"), true) }
    MarioMap(
        "🎀 El camino del Quipu",
        "Los quipus eran cuerdas con nudos para llevar cuentas en los Andes. ¡Hagamos uno! Toca el primer nivel.",
        "quipu", nodes, { go("quipu/${it + 1}") }, onBack, onHome
    )
}

@Composable
fun Cord(k: Int) {
    Box(Modifier.fillMaxWidth().height(44.dp), contentAlignment = Alignment.CenterStart) {
        Box(Modifier.fillMaxWidth().height(6.dp).background(Color(0xFFB98A4E)))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(k) { Box(Modifier.size(30.dp).clip(CircleShape).background(Color(0xFF7A4A21)).border(3.dp, Color(0xFFE0B97A), CircleShape)) }
        }
    }
}

@Composable
private fun QBtn(text: String, color: Color, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    Box(
        modifier.height(60.dp).clip(RoundedCornerShape(18.dp)).background(if (enabled) color else Color.Gray).clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) { Text(text, fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) }
}

@Composable
private fun NumberOptions(ans: Int, solved: Boolean, onResult: (Boolean) -> Unit) {
    val opts = remember(ans) { listOf(ans, ans + 1, if (ans > 1) ans - 1 else ans + 2).shuffled() }
    val dis = remember(ans) { mutableStateListOf<Int>() }
    Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        for (o in opts) OptionBox("$o", Modifier.weight(1f), dim = o in dis, good = solved && o == ans, enabled = !solved && o !in dis) {
            if (o == ans) onResult(true) else { dis.add(o); onResult(false) }
        }
    }
}

@Composable
fun QuipuRoundView(r: QRound, solved: Boolean, onResult: (Boolean) -> Unit) {
    when (r.kind) {
        "build" -> {
            var k by remember { mutableIntStateOf(0) }
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Cord(k)
                Text("$k", fontSize = 72.sp, fontWeight = FontWeight.Bold, color = C.Ink)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    QBtn("➖ Quitar", C.Sky, Modifier.weight(1f), enabled = !solved) { if (k > 0) k-- }
                    QBtn("➕ Nudo", C.Orange, Modifier.weight(1f), enabled = !solved) { if (k < 10) k++ }
                }
                Spacer(Modifier.height(10.dp))
                QBtn("✅ Listo", C.Green, Modifier.fillMaxWidth(), enabled = !solved) { onResult(k == r.a) }
            }
        }
        "compare" -> Column(Modifier.fillMaxWidth()) {
            listOf(r.a to r.b, r.b to r.a).forEachIndexed { idx, (mine, other) ->
                Card(Modifier.fillMaxWidth().padding(vertical = 8.dp), border = if (solved && mine > other) C.Green else C.Orange,
                    onClick = { if (!solved) onResult(mine > other) }) {
                    Text("Cuerda ${if (idx == 0) "A" else "B"}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = C.Ink)
                    Cord(mine)
                }
            }
        }
        "join" -> {
            var joined by remember { mutableStateOf(false) }
            Column(Modifier.fillMaxWidth()) {
                Text("Cuerda A", fontSize = 16.sp, color = C.Ink); Cord(r.a)
                Text("Cuerda B", fontSize = 16.sp, color = C.Ink); Cord(r.b)
                Spacer(Modifier.height(10.dp))
                if (!joined) QBtn("🔗 Unir las cuerdas", C.Purple, Modifier.fillMaxWidth()) { joined = true; Speaker.speak("Ahora hay una sola cuerda. ¿Cuántos nudos tiene?") }
                else {
                    Text("Cuerda unida: ¿cuántos nudos tiene?", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = C.Ink)
                    Cord(r.a + r.b)
                    NumberOptions(r.a + r.b, solved, onResult)
                }
            }
        }
        else -> {
            var removed by remember { mutableIntStateOf(0) }
            Column(Modifier.fillMaxWidth()) {
                Cord(r.a - removed)
                Text("Quitaste $removed de ${r.b}", fontSize = 16.sp, color = C.Ink)
                Spacer(Modifier.height(10.dp))
                if (removed < r.b) QBtn("✂️ Quitar un nudo", C.Red, Modifier.fillMaxWidth()) { removed++ }
                else {
                    Text("¿Cuántos nudos quedaron?", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = C.Ink)
                    NumberOptions(r.a - r.b, solved, onResult)
                }
            }
        }
    }
}

@Composable
fun QuipuLevel(n: Int, onExit: () -> Unit, onHome: () -> Unit) {
    val lv = Quipu.levels[n - 1]
    var round by remember { mutableIntStateOf(0) }
    var finished by remember { mutableStateOf<Int?>(null) }
    key(round) {
        val f = finished
        if (f != null) FinishScreen(f, { finished = null; round++ }, onExit, onHome)
        else QuipuRunner(n, lv, onExit, onHome) { s -> Store.finish("quipu.$n", s, "Quipu: ${lv.title}"); finished = s }
    }
}

@Composable
private fun QuipuRunner(n: Int, lv: QLevel, onExit: () -> Unit, onHome: () -> Unit, onDone: (Int) -> Unit) {
    val id = "quipu.$n"
    val rs = remember { Quipu.rounds(lv) }
    var i by remember { mutableIntStateOf(0) }
    var errs by remember { mutableIntStateOf(0) }
    var again by remember { mutableIntStateOf(0) }
    var wrong by remember(i) { mutableIntStateOf(0) }
    var solved by remember(i) { mutableStateOf(false) }
    var msg by remember(i) { mutableStateOf(Quipu.say(rs[i])) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(i, again) { Speaker.speak(Quipu.say(rs[i])) }

    fun result(ok: Boolean) {
        if (solved) return
        if (ok) {
            solved = true; Store.record(id, true)
            msg = "¡Muy bien! ¡Lo descubriste!"; Speaker.speak(msg)
            scope.launch { delay(1300); if (i < rs.size - 1) i++ else onDone(starsFor(errs)) }
        } else {
            wrong++; errs++; Store.record(id, false); Store.hint(id)
            msg = "¡Casi! Mira la pista: ${Quipu.hint(rs[i])}"
            Speaker.speak(Quipu.hint(rs[i]))
        }
    }

    Column(Modifier.fillMaxSize().background(C.Sand)) {
        TopBar("${n}. ${lv.title}", onExit, onHome) { again++ }
        Text("●".repeat(i) + "◉" + "○".repeat(rs.size - i - 1), Modifier.fillMaxWidth().padding(top = 6.dp), textAlign = TextAlign.Center, fontSize = 18.sp, color = C.Orange)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(14.dp)) {
            TitoBubble(msg)
            Spacer(Modifier.height(14.dp))
            key(i) { QuipuRoundView(rs[i], solved, ::result) }
        }
    }
}
