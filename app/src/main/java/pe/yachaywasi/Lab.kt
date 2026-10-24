package pe.yachaywasi

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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

/** Contenedor común: muestra el juego y luego la celebración; ajusta el nivel según el desempeño real. */
@Composable
fun GameHost(game: String, title: String, onExit: () -> Unit, onHome: () -> Unit, board: @Composable (onFinish: (Int) -> Unit) -> Unit) {
    var round by remember { mutableIntStateOf(0) }
    var fin by remember { mutableStateOf<Int?>(null) }
    key(round) {
        val f = fin
        if (f != null) FinishScreen(f, { fin = null; round++ }, onExit, onHome)
        else board { s ->
            Store.finish("lab.$game", s, title)
            val lv = Store.levelOf(game)
            Store.setLevel(game, if (s == 3) lv + 1 else if (s == 1) lv - 1 else lv)
            fin = s
        }
    }
}

@Composable
fun LabScreen(go: (String) -> Unit, onHome: () -> Unit) {
    Store.version.intValue
    Column(Modifier.fillMaxSize().background(C.Sand)) {
        TopBar("🧠 Laboratorio de la Mente", onHome, onHome)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            TitoBubble("Aquí exploramos e inventamos. ¡Elige un juego!")
            val games = listOf(
                Triple("🃏 Memoria visual", "lab/memory", "memory"), Triple("🔴🔵 Patrones", "lab/patterns", "patterns"),
                Triple("🧩 Rompecabezas", "lab/puzzle", "puzzle"), Triple("🐾🍎 Clasificación", "lab/classify", "classify"))
            for ((name, route, key) in games) {
                Card(Modifier.fillMaxWidth(), onClick = { go(route) }, border = C.Purple) {
                    Text(name, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = C.Ink)
                    Text("Nivel ${Store.levelOf(key) + 1} de 3  ·  ${starsText(Store.stars("lab.$key"))}", fontSize = 15.sp, color = C.Ink)
                }
            }
            Card(Modifier.fillMaxWidth(), onClick = { go("mazes") }, border = C.Green) {
                Text("🌀 Los laberintos de Tito", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = C.Ink)
                Text("Inclina el celular para guiar a Tito", fontSize = 15.sp, color = C.Ink)
            }
        }
    }
}

// ---------- 1. MEMORIA VISUAL ----------
@Composable
fun MemoryGame(onExit: () -> Unit, onHome: () -> Unit) = GameHost("memory", "Memoria visual", onExit, onHome) { onFinish ->
    val pairs = listOf(3, 4, 6)[Store.levelOf("memory").coerceIn(0, 2)]
    val cards = remember {
        val pool = listOf("🐶", "🐱", "🐄", "🐟", "🍎", "🍌", "🌳", "☀️", "🌙", "⚽").shuffled().take(pairs)
        (pool + pool).shuffled()
    }
    val cols = if (pairs == 3) 3 else 4
    val up = remember { mutableStateListOf<Int>() }
    val done = remember { mutableStateListOf<Int>() }
    var tries by remember { mutableIntStateOf(0) }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) { Speaker.speak("Encuentra las imágenes iguales.") }
    Column(Modifier.fillMaxSize().background(C.Sand)) {
        TopBar("Memoria visual", onExit, onHome) { Speaker.speak("Encuentra las imágenes iguales.") }
        Column(Modifier.padding(14.dp)) {
            TitoBubble("Toca dos cartas. ¡Busca las parejas! Intentos: $tries")
            Spacer(Modifier.height(14.dp))
            for (row in cards.indices.toList().chunked(cols)) {
                Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (idx in row) {
                        val shown = idx in up || idx in done
                        val shape = RoundedCornerShape(16.dp)
                        Box(
                            Modifier.weight(1f).height(92.dp).clip(shape)
                                .background(if (shown) Color.White else C.Orange)
                                .border(if (idx in done) 5.dp else 2.dp, if (idx in done) C.Green else C.Ink.copy(alpha = 0.3f), shape)
                                .clickable(enabled = !busy && !shown) {
                                    up.add(idx)
                                    if (up.size == 2) {
                                        tries++
                                        if (cards[up[0]] == cards[up[1]]) {
                                            done.addAll(up); up.clear(); Speaker.speak("¡Pareja!")
                                            if (done.size == cards.size) scope.launch {
                                                delay(900)
                                                onFinish(if (tries <= pairs + 1) 3 else if (tries <= pairs * 2) 2 else 1)
                                            }
                                        } else {
                                            busy = true
                                            scope.launch { delay(900); up.clear(); busy = false }
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) { Text(if (shown) cards[idx] else "❓", fontSize = 40.sp) }
                    }
                    repeat(cols - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

// ---------- 2. PATRONES ----------
fun patternQs(level: Int): List<Q> {
    val pool = listOf("🔴", "🔵", "🟢", "🟡", "🍎", "🍌", "🐟", "🌳", "⭐", "🌙", "🔺", "🟦")
    return List(5) {
        val sym = pool.shuffled().take(3)
        val unit = when (level) { 0 -> listOf(0, 1); 1 -> listOf(0, 0, 1); else -> listOf(0, 1, 2) }
        val len = if (level == 0) 5 else 7
        val shown = List(len) { sym[unit[it % unit.size]] }
        val ans = sym[unit[len % unit.size]]
        val opts = unit.distinct().map { sym[it] }.shuffled()
        Choice("¿Qué figura sigue?", shown.joinToString("") + "❓", opts, opts.indexOf(ans),
            "Mira el grupo que se repite: ${unit.map { sym[it] }.joinToString(" ")}")
    }
}

@Composable
fun PatternsGame(onExit: () -> Unit, onHome: () -> Unit) = GameHost("patterns", "Patrones", onExit, onHome) { onFinish ->
    val qs = remember { patternQs(Store.levelOf("patterns").coerceIn(0, 2)) }
    QuestionRunner("lab.patterns", "Patrones", qs, onExit, onHome, onFinish)
}

// ---------- 3. ROMPECABEZAS (completar la imagen) ----------
@Composable
fun PuzzleGame(onExit: () -> Unit, onHome: () -> Unit) = GameHost("puzzle", "Rompecabezas", onExit, onHome) { onFinish ->
    val level = Store.levelOf("puzzle").coerceIn(0, 2)
    val ref = remember {
        when (level) {
            0 -> listOf("☁️", "☀️", "🌳", "🏠")
            1 -> listOf("☁️", "☀️", "☁️", "🌳", "🏠", "🌳")
            else -> listOf("☁️", "☀️", "🐦", "⛰️", "🌳", "🏠", "🌊", "🐟", "🌸")
        }
    }
    val cols = if (level == 0) 2 else 3
    val cur = remember { var p = ref.shuffled(); while (p == ref) p = ref.shuffled(); mutableStateListOf<String>().apply { addAll(p) } }
    var sel by remember { mutableIntStateOf(-1) }
    var moves by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) { Speaker.speak("Toca dos piezas para cambiarlas de lugar y armar la imagen.") }
    Column(Modifier.fillMaxSize().background(C.Sand)) {
        TopBar("Rompecabezas", onExit, onHome) { Speaker.speak("Toca dos piezas para cambiarlas de lugar y armar la imagen.") }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            TitoBubble("Arma la imagen como el modelo. Movimientos: $moves")
            Spacer(Modifier.height(8.dp))
            Text("Modelo:", fontSize = 16.sp, color = C.Ink)
            for (row in ref.indices.toList().chunked(cols)) Row { for (i in row) Text(ref[i], fontSize = 30.sp, modifier = Modifier.padding(2.dp)) }
            Spacer(Modifier.height(10.dp))
            Text("Tu imagen:", fontSize = 16.sp, color = C.Ink)
            for (row in cur.indices.toList().chunked(cols)) {
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (i in row) {
                        val ok = cur[i] == ref[i]
                        val shape = RoundedCornerShape(16.dp)
                        Box(
                            Modifier.weight(1f).height(84.dp).clip(shape).background(Color.White)
                                .border(if (sel == i) 6.dp else if (ok) 4.dp else 2.dp, if (sel == i) C.Sky else if (ok) C.Green else C.Orange, shape)
                                .clickable {
                                    if (sel == -1) sel = i
                                    else {
                                        if (sel != i) { val t = cur[sel]; cur[sel] = cur[i]; cur[i] = t; moves++ }
                                        sel = -1
                                        if (cur.toList() == ref) scope.launch {
                                            Speaker.speak("¡Completaste la imagen!"); delay(900)
                                            onFinish(if (moves <= ref.size + 2) 3 else if (moves <= ref.size * 2) 2 else 1)
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(cur[i], fontSize = 40.sp)
                            if (ok) Text("✔", Modifier.align(Alignment.TopEnd).padding(4.dp), fontSize = 16.sp, color = C.Green, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ---------- 4. CLASIFICACIÓN ----------
@Composable
fun ClassifyGame(onExit: () -> Unit, onHome: () -> Unit) = GameHost("classify", "Clasificación", onExit, onHome) { onFinish ->
    val level = Store.levelOf("classify").coerceIn(0, 2)
    val bins = listOf("🐾" to "Animales", "🍎" to "Alimentos", "🎒" to "Objetos").take(if (level == 0) 2 else 3)
    val groups = listOf(
        listOf("🐶", "🐱", "🐄", "🐟", "🐔", "🐑"), listOf("🍎", "🍌", "🥕", "🍞", "🥛", "🍇"), listOf("⚽", "📖", "🎒", "✏️", "💺", "🔑"))
    val per = if (level == 2) 4 else 3
    val items = remember { bins.indices.flatMap { b -> groups[b].shuffled().take(per).map { it to b } }.shuffled() }
    var i by remember { mutableIntStateOf(0) }
    var errs by remember { mutableIntStateOf(0) }
    var msg by remember { mutableStateOf("¿A qué grupo pertenece?") }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) { Speaker.speak("Toca el grupo al que pertenece cada imagen.") }
    Column(Modifier.fillMaxSize().background(C.Sand)) {
        TopBar("Clasificación", onExit, onHome) { Speaker.speak("Toca el grupo al que pertenece cada imagen.") }
        Column(Modifier.weight(1f).padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            TitoBubble(msg)
            Spacer(Modifier.height(16.dp))
            Text(items[i].first, fontSize = 96.sp)
            Text("${i + 1} de ${items.size}", fontSize = 16.sp, color = C.Ink)
            Spacer(Modifier.weight(1f))
            Row(Modifier.fillMaxWidth().height(120.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                bins.forEachIndexed { b, (e, name) ->
                    BigButton(name, e, if (b == 0) C.Green else if (b == 1) C.Red else C.Sky, Modifier.weight(1f).fillMaxHeight()) {
                        if (b == items[i].second) {
                            Store.record("lab.classify", true); Speaker.speak("¡Muy bien!")
                            if (i < items.size - 1) { i++; msg = "¡Muy bien! ¿Y esta?" } else scope.launch { delay(500); onFinish(starsFor(errs)) }
                        } else {
                            errs++; Store.record("lab.classify", false); Store.hint("lab.classify")
                            msg = "¡Casi! Mira bien la imagen y piensa: ¿es un animal, un alimento o un objeto?"
                            Speaker.speak("Casi. Mira bien la imagen e inténtalo otra vez.")
                        }
                    }
                }
            }
        }
    }
}
