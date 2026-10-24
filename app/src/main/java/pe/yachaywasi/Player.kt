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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Motor de actividades: elegir, tocar todas las correctas y ordenar. Sin castigos, con pistas graduadas. */
@Composable
fun ActivityPlayer(act: Activity, onExit: () -> Unit, onHome: () -> Unit) =
    ActivityPlayer(act.id, act.title, act.qs, onExit, onHome)

@Composable
fun ActivityPlayer(id: String, title: String, qs: List<Q>, onExit: () -> Unit, onHome: () -> Unit) {
    var round by remember { mutableIntStateOf(0) }
    var finished by remember { mutableStateOf<Int?>(null) }
    key(round) {
        val f = finished
        if (f != null) FinishScreen(f, onAgain = { finished = null; round++ }, onExit = onExit, onHome = onHome)
        else QuestionRunner(id, title, qs, onExit, onHome) { stars -> Store.finish(id, stars, title); finished = stars }
    }
}

fun starsFor(errors: Int): Int = if (errors == 0) 3 else if (errors <= 2) 2 else 1

@Composable
fun QuestionRunner(id: String, title: String, qs: List<Q>, onExit: () -> Unit, onHome: () -> Unit, onDone: (Int) -> Unit) {
    var i by remember { mutableIntStateOf(0) }
    var errs by remember { mutableIntStateOf(0) }
    var again by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    val q = qs[i]
    var wrong by remember(i) { mutableIntStateOf(0) }
    var msg by remember(i) { mutableStateOf(q.say) }
    var solved by remember(i) { mutableStateOf(false) }

    LaunchedEffect(i, again) {
        Speaker.speak(q.say)
        if (q is Choice && q.en != null) { delay(1800); Speaker.speak(q.en, "en") }
    }

    fun result(ok: Boolean) {
        if (solved) return
        if (ok) {
            solved = true
            Store.record(id, true)
            msg = "¡Muy bien! ¡Lo descubriste!"
            Speaker.speak(msg)
            scope.launch {
                delay(1400)
                if (i < qs.size - 1) i++ else onDone(starsFor(errs))
            }
        } else {
            wrong++; errs++
            Store.record(id, false); Store.hint(id)
            msg = when (wrong) {
                1 -> "¡Casi! Mira la pista: ${q.hint}"
                2 -> "¡Tú puedes! ${q.hint}"
                else -> "Tito te ayuda: mira la respuesta marcada."
            }
            Speaker.speak(if (wrong >= 3) "Tito te ayuda. Mira la respuesta marcada." else q.hint)
        }
    }

    Column(Modifier.fillMaxSize().background(C.Sand)) {
        TopBar(title, onExit, onHome) { again++ }
        Text(
            "●".repeat(i) + "◉" + "○".repeat(qs.size - i - 1), Modifier.fillMaxWidth().padding(top = 6.dp),
            textAlign = TextAlign.Center, fontSize = 18.sp, color = C.Orange
        )
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(14.dp)) {
            TitoBubble(msg)
            Spacer(Modifier.height(14.dp))
            when (q) {
                is Choice -> ChoiceView(q, wrong, solved, ::result)
                is TapAll -> TapAllView(q, wrong, solved, ::result)
                is Order -> OrderView(q, wrong, solved, ::result)
            }
        }
    }
}

@Composable
fun OptionBox(text: String, modifier: Modifier, dim: Boolean, good: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier.height(112.dp).alpha(if (dim) 0.35f else 1f).clip(shape)
            .background(if (good) Color(0xFFD7F2DA) else Color.White)
            .border(if (good) 6.dp else 2.dp, if (good) C.Green else C.Orange, shape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            if (good) "✔ $text" else text,
            fontSize = if (text.length <= 3) 44.sp else if (text.length <= 8) 24.sp else 17.sp,
            textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = C.Ink, modifier = Modifier.padding(4.dp)
        )
    }
}

@Composable
fun ChoiceView(q: Choice, wrong: Int, solved: Boolean, onResult: (Boolean) -> Unit) {
    val dis = remember(q) { mutableStateListOf<Int>() }
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(q.show, fontSize = if (q.show.length <= 8) 56.sp else 38.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = C.Ink)
        Spacer(Modifier.height(18.dp))
        val rows = if (q.options.size <= 3) listOf(q.options.indices.toList()) else q.options.indices.toList().chunked(2)
        for (row in rows) {
            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                for (idx in row) {
                    OptionBox(
                        q.options[idx], Modifier.weight(1f), dim = idx in dis,
                        good = (solved || wrong >= 3) && idx == q.answer,
                        enabled = !solved && idx !in dis
                    ) {
                        if (idx == q.answer) onResult(true) else { dis.add(idx); onResult(false) }
                    }
                }
            }
        }
    }
}

@Composable
fun TapAllView(q: TapAll, wrong: Int, solved: Boolean, onResult: (Boolean) -> Unit) {
    val sel = remember(q) { mutableStateListOf<Int>() }
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        for (row in q.items.indices.toList().chunked(3)) {
            Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                for (idx in row) {
                    val picked = idx in sel
                    val help = (wrong >= 3 || solved) && idx in q.targets
                    val shape = RoundedCornerShape(20.dp)
                    Box(
                        Modifier.weight(1f).height(96.dp).clip(shape)
                            .background(if (help) Color(0xFFD7F2DA) else Color.White)
                            .border(if (picked || help) 6.dp else 2.dp, if (help) C.Green else if (picked) C.Sky else C.Orange, shape)
                            .clickable(enabled = !solved) { if (picked) sel.remove(idx) else sel.add(idx) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(q.items[idx], fontSize = 44.sp)
                        if (picked) Text("✔", Modifier.align(Alignment.TopEnd).padding(6.dp), fontSize = 20.sp, color = C.Sky, fontWeight = FontWeight.Bold)
                    }
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
        Spacer(Modifier.height(14.dp))
        Box(
            Modifier.fillMaxWidth().height(64.dp).clip(RoundedCornerShape(20.dp)).background(C.Green)
                .clickable(enabled = !solved) { onResult(sel.toSet() == q.targets) },
            contentAlignment = Alignment.Center
        ) { Text("✅ Listo", fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Bold) }
    }
}

@Composable
fun OrderView(q: Order, wrong: Int, solved: Boolean, onResult: (Boolean) -> Unit) {
    val n = q.items.size
    val ordered = remember(q) { q.items.indices.toList() }
    val shuffled = remember(q) { var p = ordered.shuffled(); while (p == ordered) p = ordered.shuffled(); p }
    val chosen = remember(q) { mutableStateListOf<Int>() }
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            for (k in 0 until n) {
                Box(
                    Modifier.weight(1f).height(84.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFFFFE9B8))
                        .border(2.dp, C.Orange, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (k < chosen.size) q.items[chosen[k]] else "${k + 1}", fontSize = if (k < chosen.size) 15.sp else 28.sp,
                        textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = C.Ink, modifier = Modifier.padding(2.dp))
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        for (row in shuffled.chunked(2)) {
            Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                for (idx in row) {
                    val used = idx in chosen
                    val help = wrong >= 3 && chosen.size < n && idx == ordered[chosen.size]
                    val shape = RoundedCornerShape(18.dp)
                    Box(
                        Modifier.weight(1f).height(68.dp).alpha(if (used) 0.3f else 1f).clip(shape)
                            .background(if (help) Color(0xFFD7F2DA) else Color.White)
                            .border(if (help) 6.dp else 2.dp, if (help) C.Green else C.Orange, shape)
                            .clickable(enabled = !used && !solved) {
                                chosen.add(idx)
                                if (chosen.size == n) {
                                    if (chosen.toList() == ordered) onResult(true) else { chosen.clear(); onResult(false) }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) { Text(q.items[idx], fontSize = if (q.items[idx].length <= 3) 30.sp else 17.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = C.Ink, modifier = Modifier.padding(4.dp)) }
                }
                if (row.size < 2) Spacer(Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(10.dp))
        Box(
            Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(18.dp)).background(C.Sky)
                .clickable(enabled = !solved && chosen.isNotEmpty()) { chosen.removeAt(chosen.lastIndex) },
            contentAlignment = Alignment.Center
        ) { Text("↩️ Borrar el último", fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold) }
    }
}
