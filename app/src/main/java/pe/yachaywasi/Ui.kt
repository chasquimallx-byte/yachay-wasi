package pe.yachaywasi

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object C {
    val Orange = Color(0xFFD9822B)
    val Green = Color(0xFF3F9B4F)
    val Sky = Color(0xFF3A8FD1)
    val Purple = Color(0xFF7B4FA3)
    val Red = Color(0xFFC8553D)
    val Sand = Color(0xFFFFF6E0)
    val Ink = Color(0xFF3B2A1A)
    val Yellow = Color(0xFFF2B84B)
}

fun starsText(n: Int, of: Int = 3) = "⭐".repeat(n.coerceIn(0, of)) + "☆".repeat((of - n).coerceIn(0, of))

@Composable
fun IconBtn(emoji: String, onClick: () -> Unit) {
    Box(
        Modifier.size(48.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.3f)).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { Text(emoji, fontSize = 24.sp) }
}

@Composable
fun TopBar(title: String, onBack: (() -> Unit)?, onHome: (() -> Unit)?, onRepeat: (() -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth().background(C.Orange).padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (onBack != null) IconBtn("⬅️", onBack)
        Text(title, Modifier.weight(1f), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 2)
        if (onRepeat != null) IconBtn("🔁", onRepeat)
        IconBtn(if (Store.sound) "🔊" else "🔇") { Store.sound = !Store.sound; if (!Store.sound) Speaker.stop() }
        if (onHome != null) IconBtn("🏠", onHome)
    }
}

@Composable
fun TitoBubble(text: String, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("🐹", fontSize = 48.sp)
        Box(Modifier.weight(1f).clip(RoundedCornerShape(18.dp)).background(Color.White).padding(12.dp)) {
            Text(text, fontSize = 18.sp, color = C.Ink)
        }
    }
}

@Composable
fun BigButton(text: String, emoji: String, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier.clip(RoundedCornerShape(24.dp)).background(color).clickable(onClick = onClick).padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
    ) {
        Text(emoji, fontSize = 44.sp)
        Text(text, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    }
}

@Composable
fun Card(modifier: Modifier = Modifier, color: Color = Color.White, border: Color? = null, onClick: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    var m = modifier.clip(RoundedCornerShape(18.dp)).background(color)
    if (border != null) m = m.border(4.dp, border, RoundedCornerShape(18.dp))
    if (onClick != null) m = m.clickable(onClick = onClick)
    Column(m.padding(12.dp), content = content)
}

@Composable
fun FinishScreen(stars: Int, onAgain: () -> Unit, onExit: () -> Unit, onHome: () -> Unit) {
    LaunchedEffect(Unit) { Speaker.speak("¡Descubriste algo nuevo! Ganaste $stars estrellas.") }
    Column(
        Modifier.fillMaxSize().background(C.Sand).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
    ) {
        Text("🎉", fontSize = 72.sp)
        Text("¡Descubriste algo nuevo!", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = C.Ink, textAlign = TextAlign.Center)
        Text(starsText(stars), fontSize = 44.sp)
        Spacer(Modifier.height(8.dp))
        TitoBubble("¡Muy bien! Ganaste $stars ${if (stars == 1) "estrella" else "estrellas"}.")
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth().height(120.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            BigButton("Otra vez", "🔁", C.Sky, Modifier.weight(1f).fillMaxHeight(), onAgain)
            BigButton("Seguir", "➡️", C.Green, Modifier.weight(1f).fillMaxHeight(), onExit)
            BigButton("Inicio", "🏠", C.Orange, Modifier.weight(1f).fillMaxHeight(), onHome)
        }
    }
}
