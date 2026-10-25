package pe.yachaywasi

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.min

class MapNode(val name: String, val emoji: String, val stars: Int, val available: Boolean)

/**
 * Mapa tipo Mario Bros: un camino con nodos. Solo se abre el siguiente nivel al completar el anterior
 * (stars > 0). Tito camina de nodo en nodo y, si se desbloquea uno nuevo, camina solo hasta él.
 */
@Composable
fun MarioMap(
    title: String, intro: String, posKey: String, nodes: List<MapNode>,
    onOpen: (Int) -> Unit, onBack: () -> Unit, onHome: () -> Unit
) {
    val n = nodes.size
    val done = nodes.map { it.stars > 0 }
    var f = 0
    while (f + 1 < n && done[f] && nodes[f + 1].available) f++

    val fx = listOf(0.2f, 0.62f, 0.28f, 0.74f, 0.25f, 0.66f, 0.3f, 0.72f, 0.5f)
    val rowH = 118f
    val scope = rememberCoroutineScope()
    val tito = remember { Animatable(Store.intOf("$posKey.pos").coerceIn(0, f).toFloat()) }
    var walking by remember { mutableStateOf(false) }
    var msg by remember { mutableStateOf(intro) }

    LaunchedEffect(f) {
        val front = Store.intOf("$posKey.front")
        if (f > front) {
            walking = true
            msg = "¡Nuevo nivel desbloqueado: ${nodes[f].name}!"
            Speaker.speak("¡Nuevo nivel desbloqueado!")
            tito.animateTo(f.toFloat(), tween(((f - tito.value) * 900f).toInt().coerceAtLeast(400)))
            Store.putInt("$posKey.pos", f); Store.putInt("$posKey.front", f)
            walking = false
        } else if (f < front) {                       // p. ej. tras borrar el progreso
            tito.snapTo(f.toFloat()); Store.putInt("$posKey.pos", f); Store.putInt("$posKey.front", f)
        }
    }

    fun tap(i: Int) {
        if (walking) return
        if (i > f) {
            val m = if (!nodes[i].available) "Este lugar llegará pronto." else "Completa el nivel anterior para abrir este."
            msg = m; Speaker.speak(m); return
        }
        scope.launch {
            walking = true
            tito.animateTo(i.toFloat(), tween((abs(tito.value - i) * 700f).toInt().coerceAtLeast(250)))
            Store.putInt("$posKey.pos", i)
            walking = false
            onOpen(i)
        }
    }

    Column(Modifier.fillMaxSize().background(C.Sand)) {
        TopBar(title, onBack, onHome)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 8.dp)) {
            TitoBubble(msg, Modifier.padding(vertical = 8.dp))
            BoxWithConstraints(Modifier.fillMaxWidth().height((n * rowH + 40f).dp)) {
                val w = maxWidth.value
                val d = LocalDensity.current.density
                fun cx(i: Int) = w * fx[i % fx.size]
                fun cy(i: Int) = 60f + i * rowH

                Canvas(Modifier.fillMaxSize()) {
                    val all = Path().apply { moveTo(cx(0) * d, cy(0) * d); for (i in 1 until n) lineTo(cx(i) * d, cy(i) * d) }
                    drawPath(all, Color(0xFFD9C9A3), style = Stroke(width = 18f * d, cap = StrokeCap.Round, join = StrokeJoin.Round))
                    if (f > 0) {
                        val open = Path().apply { moveTo(cx(0) * d, cy(0) * d); for (i in 1..f) lineTo(cx(i) * d, cy(i) * d) }
                        drawPath(open, C.Yellow, style = Stroke(width = 18f * d, cap = StrokeCap.Round, join = StrokeJoin.Round))
                    }
                    drawPath(all, Color.White.copy(alpha = 0.6f), style = Stroke(width = 3f * d, pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f * d, 14f * d))))
                }

                for (i in 0 until n) {
                    val nd = nodes[i]
                    val open = i <= f
                    Column(Modifier.offset((cx(i) - 50f).dp, (cy(i) - 36f).dp).width(100.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            Modifier.size(72.dp).clip(CircleShape)
                                .background(if (done[i]) C.Green else if (open) C.Orange else Color(0xFFB8AE98))
                                .border(4.dp, Color.White, CircleShape)
                                .clickable { tap(i) },
                            contentAlignment = Alignment.Center
                        ) { Text(if (open) nd.emoji else "🔒", fontSize = 34.sp) }
                        Text(nd.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = C.Ink, textAlign = TextAlign.Center)
                        if (done[i]) Text(starsText(nd.stars), fontSize = 11.sp)
                        else if (!open) Text(if (!nd.available) "Pronto" else "Bloqueado", fontSize = 11.sp, color = C.Ink)
                        else Text("¡Abierto!", fontSize = 11.sp, color = C.Green, fontWeight = FontWeight.Bold)
                    }
                }

                val t = tito.value
                val a = floor(t).toInt().coerceIn(0, n - 1)
                val b = min(a + 1, n - 1)
                val fr = t - a
                val tx = cx(a) + (cx(b) - cx(a)) * fr
                val ty = cy(a) + (cy(b) - cy(a)) * fr
                Text("🐹", Modifier.offset((tx - 26f).dp, (ty - 90f).dp), fontSize = 48.sp)
            }
        }
    }
}
