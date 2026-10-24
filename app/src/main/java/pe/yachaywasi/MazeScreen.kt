package pe.yachaywasi

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.delay

private val emojiPaint = android.graphics.Paint().apply {
    textAlign = android.graphics.Paint.Align.CENTER
    isAntiAlias = true
}

private fun DrawScope.emoji(s: String, cx: Float, cy: Float, size: Float) {
    emojiPaint.textSize = size
    drawIntoCanvas { it.nativeCanvas.drawText(s, cx, cy + size * 0.35f, emojiPaint) }
}

@Composable
fun MazeMenu(go: (String) -> Unit, onBack: () -> Unit, onHome: () -> Unit) {
    Store.version.intValue
    Column(Modifier.fillMaxSize().background(C.Sand)) {
        TopBar("🌀 Los laberintos de Tito", onBack, onHome)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            TitoBubble("Inclina el celular para ayudarme a llegar. ¡Sin prisa, no hay tiempo límite!")
            val level = listOf("Fácil", "Intermedio", "Un poco más difícil")
            for (m in Mazes.all) {
                Card(Modifier.fillMaxWidth(), border = C.Green, onClick = { go("maze/${m.id}") }) {
                    Text("${m.id}. ${m.name}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = C.Ink)
                    Text("${level[m.id - 1]}  ·  ${starsText(Store.stars("lab.maze${m.id}"), 1)}", fontSize = 15.sp, color = C.Ink)
                    Text(m.objective, fontSize = 14.sp, color = C.Ink)
                }
            }
        }
    }
}

@Composable
private fun SmallBtn(text: String, color: Color, modifier: Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    Box(
        modifier.height(46.dp).clip(RoundedCornerShape(14.dp)).background(if (enabled) color else Color.Gray)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) { Text(text, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) }
}

@Composable
private fun HoldBtn(label: String, modifier: Modifier, onDown: () -> Unit, onUp: () -> Unit) {
    Box(
        modifier.height(56.dp).clip(RoundedCornerShape(16.dp)).background(C.Purple).pointerInput(Unit) {
            detectTapGestures(onPress = { onDown(); try { tryAwaitRelease() } finally { onUp() } })
        },
        contentAlignment = Alignment.Center
    ) { Text(label, fontSize = 30.sp, color = Color.White) }
}

private val speeds = floatArrayOf(1.6f, 2.4f, 3.4f)
private val speedNames = listOf("🐢 Lento", "🚶 Normal", "🐇 Rápido")

@Composable
fun MazeScreen(n: Int, onBack: () -> Unit, onHome: () -> Unit) {
    val def = Mazes.all[n - 1]
    val grid = remember(n) { MazeGrid(def.rows) }
    val eng = remember(n) { MazeEngine(grid) }
    val tilt = rememberTilt(true)
    var useTilt by remember { mutableStateOf(tilt.available) }
    var running by remember { mutableStateOf(false) }
    var paused by remember { mutableStateOf(false) }
    var won by remember { mutableStateOf(false) }
    var tick by remember { mutableIntStateOf(0) }
    var got by remember { mutableIntStateOf(0) }
    var crossed by remember { mutableStateOf(false) }
    var speedIdx by remember { mutableIntStateOf(1) }
    var hint by remember { mutableStateOf<List<Pair<Int, Int>>>(emptyList()) }
    var msg by remember { mutableStateOf(def.speech) }
    val dir = remember { floatArrayOf(0f, 0f) }

    // Pausa automática si la app pasa a segundo plano (el sensor se libera en rememberTilt).
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner) {
        val o = LifecycleEventObserver { _, e -> if (e == Lifecycle.Event.ON_PAUSE && running) paused = true }
        owner.lifecycle.addObserver(o)
        onDispose { owner.lifecycle.removeObserver(o) }
    }
    LaunchedEffect(n) { Speaker.speak(def.speech) }
    LaunchedEffect(hint) { if (hint.isNotEmpty()) { delay(6000); hint = emptyList() } }

    LaunchedEffect(running, paused, won, useTilt) {
        if (!running || paused || won) return@LaunchedEffect
        var last = 0L
        var fruits = eng.collectedCount
        while (true) {
            val t = withFrameNanos { it }
            if (last != 0L) {
                val dt = minOf(0.05f, (t - last) / 1_000_000_000f)
                val v = if (useTilt) tilt.vector() else Pair(dir[0], dir[1])
                eng.step(v.first, v.second, speeds[speedIdx], dt)
                tick++
                if (n == 3 && !crossed && eng.x > 7f) { crossed = true; msg = "¡Cruzaste el puente!"; Speaker.speak("¡Cruzaste el puente!") }
                if (eng.collectedCount > fruits) {
                    fruits = eng.collectedCount
                    got = fruits
                    msg = "¡Fruta! Van $fruits de ${eng.fruits.size}"
                    Speaker.speak("Van $fruits de ${eng.fruits.size}")
                }
                if (eng.won) {
                    won = true
                    Store.finish("lab.maze$n", 1, def.name)
                    break
                }
            }
            last = t
        }
    }

    fun restart() {
        eng.reset(); won = false; running = false; paused = false; hint = emptyList(); tilt.calibrate(); tick++; got = 0; crossed = false
        msg = def.speech
    }

    if (won) {
        FinishScreen(1, onAgain = { restart() }, onExit = onBack, onHome = onHome)
        return
    }

    Column(Modifier.fillMaxSize().background(C.Sand)) {
        TopBar("${def.id}. ${def.name}", onBack, onHome) { Speaker.speak(def.speech) }
        val progress = when {
            eng.fruits.isNotEmpty() -> "🍎 Frutas: $got de ${eng.fruits.size}" + if (got == eng.fruits.size) "  ➜ ¡Busca la salida!" else ""
            n == 3 -> if (crossed) "🌉 ¡Cruzaste el puente!" else "🌉 Busca el puente"
            else -> "🚩 Inicio  ➜  ${def.emojiGoal} Meta"
        }
        Text(progress, Modifier.fillMaxWidth().padding(top = 4.dp), textAlign = TextAlign.Center, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = C.Ink)

        Canvas(Modifier.weight(1f).fillMaxWidth().padding(6.dp)) {
            tick // suscribe el dibujo a cada cuadro del juego
            val cell = minOf(size.width / grid.w, size.height / grid.h)
            val ox = (size.width - cell * grid.w) / 2f
            val oy = (size.height - cell * grid.h) / 2f
            for (y in 0 until grid.h) for (x in 0 until grid.w) {
                val col = when (grid.at(x, y)) {
                    '#' -> Color(0xFF3F7A3A)
                    '~' -> Color(0xFF4FA3D9)
                    'B' -> Color(0xFF9A6B3A)
                    else -> Color(0xFFF3DFA8)
                }
                drawRect(col, Offset(ox + x * cell, oy + y * cell), Size(cell + 0.6f, cell + 0.6f))
                if (grid.at(x, y) == '~') drawRect(Color(0x33FFFFFF), Offset(ox + x * cell, oy + y * cell + cell * 0.4f), Size(cell, cell * 0.08f))
                if (grid.at(x, y) == 'B') for (k in 1..3) drawRect(Color(0xFF6B4423), Offset(ox + x * cell + cell * k / 4f - 2f, oy + y * cell), Size(4f, cell))
            }
            val s = grid.find('S').first()
            emoji("🚩", ox + (s.first + 0.5f) * cell, oy + (s.second + 0.5f) * cell, cell * 0.7f)
            emoji(def.emojiGoal, ox + (eng.goal.first + 0.5f) * cell, oy + (eng.goal.second + 0.5f) * cell, cell * 0.8f)
            val fr = listOf("🍎", "🍌", "🍇")
            eng.fruits.forEachIndexed { i, f ->
                if (!eng.collected[i]) emoji(fr[i % 3], ox + (f.first + 0.5f) * cell, oy + (f.second + 0.5f) * cell, cell * 0.7f)
            }
            for (p in hint.drop(1)) drawCircle(Color(0xFFFFC400), cell * 0.12f, Offset(ox + (p.first + 0.5f) * cell, oy + (p.second + 0.5f) * cell))
            emoji("🐹", ox + eng.x * cell, oy + eng.y * cell, cell * 0.8f)
        }

        Text(msg, Modifier.fillMaxWidth().padding(horizontal = 10.dp), fontSize = 13.sp, color = C.Ink, textAlign = TextAlign.Center, maxLines = 2)
        Column(Modifier.fillMaxWidth().padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (!useTilt) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    HoldBtn("⬅️", Modifier.weight(1f), { dir[0] = -1f }, { dir[0] = 0f })
                    HoldBtn("⬆️", Modifier.weight(1f), { dir[1] = -1f }, { dir[1] = 0f })
                    HoldBtn("⬇️", Modifier.weight(1f), { dir[1] = 1f }, { dir[1] = 0f })
                    HoldBtn("➡️", Modifier.weight(1f), { dir[0] = 1f }, { dir[0] = 0f })
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                SmallBtn(if (!running) "▶️ Comenzar" else if (paused) "▶️ Seguir" else "⏸️ Pausar", C.Green, Modifier.weight(1f)) {
                    if (!running) { tilt.calibrate(); running = true; paused = false; msg = "¡Vamos! Mueve el celular con suavidad." }
                    else paused = !paused
                }
                SmallBtn("🔄 Reiniciar", C.Orange, Modifier.weight(1f)) { restart() }
                SmallBtn("💡 Pista", C.Yellow, Modifier.weight(1f)) {
                    hint = eng.hintPath(); msg = "Sigue los puntos amarillos."; Speaker.speak("Sigue los puntos amarillos.")
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                SmallBtn("🎯 Calibrar celular", C.Sky, Modifier.weight(1f), enabled = tilt.available) {
                    tilt.calibrate(); msg = "Listo. Esta posición es la neutral."; Speaker.speak("Listo. Esta posición es la neutral.")
                }
                SmallBtn(if (useTilt) "🎮 Control: inclinar" else "🎮 Control: botones", C.Purple, Modifier.weight(1f), enabled = tilt.available || useTilt) {
                    useTilt = !useTilt
                    msg = if (useTilt) "Inclina el celular." else "Usa los botones grandes."
                }
                SmallBtn(speedNames[speedIdx], C.Red, Modifier.weight(1f)) { speedIdx = (speedIdx + 1) % 3 }
            }
            if (!tilt.available) Text("Este celular no tiene sensor de inclinación: usa los botones.", fontSize = 12.sp, color = C.Red)
        }
    }
}
