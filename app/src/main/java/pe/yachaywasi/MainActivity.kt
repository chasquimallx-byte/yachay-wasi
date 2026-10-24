package pe.yachaywasi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Store.init(this)
        Speaker.init(this)
        setContent { App() }
    }

    override fun onPause() { super.onPause(); Speaker.stop() }
}

/** Padre de cada ruta: lo que hace el botón "atrás" (el de Android y el de la app). */
fun parentOf(route: String): String = when {
    route == "home" -> "home"
    route.startsWith("maze/") -> "mazes"
    route == "mazes" -> "lab"
    route.startsWith("lab/") -> "lab"
    route.startsWith("adv/") -> "adventures"
    route.startsWith("learn/") -> "learn"
    route.endsWith("/play") -> route.removeSuffix("/play")
    route.startsWith("eng/") -> "english"
    route == "english" -> "learn"
    route == "adults" -> "progress"
    else -> "home"
}

@Composable
fun App() {
    var route by rememberSaveable { mutableStateOf("home") }
    val go: (String) -> Unit = { route = it }
    val back = { route = parentOf(route) }
    val home = { route = "home" }
    BackHandler(enabled = route != "home") { back() }

    when {
        route == "home" -> HomeScreen(go)
        route == "tutorial" -> TutorialScreen(home, home)
        route == "adventures" -> AdventuresScreen(go, home)
        route.startsWith("adv/") -> {
            val p = Content.places.firstOrNull { it.id == route.removePrefix("adv/") }?.activity
            if (p != null) ActivityPlayer(p, back, home) else back()
        }
        route == "learn" -> LearnScreen(go, home)
        route.startsWith("learn/") -> {
            val a = Content.allActivities.firstOrNull { it.id == route.removePrefix("learn/") }
            if (a != null) ActivityPlayer(a, back, home) else back()
        }
        route == "english" -> EnglishMenu(go, back, home)
        route.startsWith("eng/") -> {
            val parts = route.split("/")
            val cat = English.cats.firstOrNull { it.id == parts.getOrNull(1) }
            if (cat == null) back()
            else if (parts.getOrNull(2) == "play") ActivityPlayer(English.listenGame(cat), back, home)
            else EnglishCategory(cat, go, back, home)
        }
        route == "lab" -> LabScreen(go, home)
        route == "lab/memory" -> MemoryGame(back, home)
        route == "lab/patterns" -> PatternsGame(back, home)
        route == "lab/puzzle" -> PuzzleGame(back, home)
        route == "lab/classify" -> ClassifyGame(back, home)
        route == "mazes" -> MazeMenu(go, back, home)
        route.startsWith("maze/") -> MazeScreen(route.removePrefix("maze/").toIntOrNull()?.coerceIn(1, 3) ?: 1, back, home)
        route == "progress" -> ProgressScreen(go, home)
        route == "adults" -> AdultsScreen(back, home)
        else -> HomeScreen(go)
    }
}
