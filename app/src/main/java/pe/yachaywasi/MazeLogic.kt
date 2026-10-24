package pe.yachaywasi

import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

/** Lógica pura (sin Android) de los laberintos: mapas, colisiones, frutas, meta y pistas. */
data class MazeDef(
    val id: Int,
    val name: String,
    val emojiGoal: String,
    val speech: String,
    val objective: String,
    val rows: List<String>
)

/**
 * Leyenda: '#' pared/seto · '.' camino · 'S' inicio · 'G' meta · 'F' fruta · '~' agua (no se puede pisar) · 'B' puente.
 */
object Mazes {
    val all = listOf(
        MazeDef(
            1, "La casita de Tito", "🏠",
            "Ayuda a Tito a llegar a su casita. Inclina el celular con suavidad.",
            "Reconocer direcciones y practicar el control del celular.",
            listOf(
                "#######",
                "#S....#",
                "#####.#",
                "#.....#",
                "#.#####",
                "#.....#",
                "#####.#",
                "#....G#",
                "#######"
            )
        ),
        MazeDef(
            2, "La chacra de las frutas", "🚪",
            "Tito debe recoger tres frutas y después buscar la salida.",
            "Contar hasta tres, planificar el recorrido y orientarse.",
            listOf(
                "#########",
                "#S..#...#",
                "#.#.#.#.#",
                "#.#...#F#",
                "#.#####.#",
                "#F....#.#",
                "###.###.#",
                "#...#...#",
                "#.###.###",
                "#...F..G#",
                "#########"
            )
        ),
        MazeDef(
            3, "El puente del río", "🏁",
            "Encuentra el puente, cruza el río y llega al otro lado.",
            "Resolver problemas, reconocer rutas y planificar movimientos.",
            listOf(
                "##########",
                "#S.#.~~.G#",
                "#..#.~~#.#",
                "#.##.~~#.#",
                "#....~~#.#",
                "#.###~~#.#",
                "#.###~~#.#",
                "#....BB..#",
                "#.###~~#.#",
                "#....~~#.#",
                "##########"
            )
        )
    )
}

class MazeGrid(val rows: List<String>) {
    val h = rows.size
    val w = rows[0].length

    fun at(x: Int, y: Int): Char = if (x in 0 until w && y in 0 until h) rows[y][x] else '#'
    fun isBlockedCell(x: Int, y: Int): Boolean = at(x, y).let { it == '#' || it == '~' }

    /** Un círculo de radio r centrado en (px,py) choca si alguna de sus 4 esquinas cae en una celda bloqueada. */
    fun blocked(px: Float, py: Float, r: Float): Boolean {
        val xs = floatArrayOf(px - r, px + r)
        val ys = floatArrayOf(py - r, py + r)
        for (x in xs) for (y in ys) {
            if (isBlockedCell(floor(x).toInt(), floor(y).toInt())) return true
        }
        return false
    }

    fun find(c: Char): List<Pair<Int, Int>> {
        val out = ArrayList<Pair<Int, Int>>()
        for (y in 0 until h) for (x in 0 until w) if (rows[y][x] == c) out.add(Pair(x, y))
        return out
    }

    /** Camino más corto (BFS) entre dos celdas; lista vacía si no hay camino. */
    fun path(from: Pair<Int, Int>, to: Pair<Int, Int>): List<Pair<Int, Int>> {
        val prev = HashMap<Pair<Int, Int>, Pair<Int, Int>?>()
        val q = ArrayDeque<Pair<Int, Int>>()
        prev[from] = null
        q.add(from)
        val dirs = listOf(Pair(1, 0), Pair(-1, 0), Pair(0, 1), Pair(0, -1))
        while (q.isNotEmpty()) {
            val c = q.removeFirst()
            if (c == to) break
            for (d in dirs) {
                val n = Pair(c.first + d.first, c.second + d.second)
                if (!isBlockedCell(n.first, n.second) && !prev.containsKey(n)) {
                    prev[n] = c
                    q.add(n)
                }
            }
        }
        if (!prev.containsKey(to)) return emptyList()
        val out = ArrayList<Pair<Int, Int>>()
        var c: Pair<Int, Int>? = to
        while (c != null) {
            out.add(c)
            c = prev[c]
        }
        out.reverse()
        return out
    }
}

class MazeEngine(val grid: MazeGrid) {
    val radius = 0.28f
    var x = 0f
    var y = 0f
    val fruits: List<Pair<Int, Int>> = grid.find('F')
    val collected = BooleanArray(fruits.size)
    val goal: Pair<Int, Int> = grid.find('G').first()
    var won = false
        private set

    val collectedCount: Int get() = collected.count { it }
    val allFruits: Boolean get() = collectedCount == fruits.size

    init { reset() }

    fun reset() {
        val s = grid.find('S').first()
        x = s.first + 0.5f
        y = s.second + 0.5f
        collected.fill(false)
        won = false
    }

    /** vx, vy en [-1,1]; speed en celdas/segundo; dt en segundos. Avanza en pasos cortos: no atraviesa paredes. */
    fun step(vx: Float, vy: Float, speed: Float, dt: Float) {
        if (won) return
        val mx = vx * speed * dt
        val my = vy * speed * dt
        val n = max(1, ceil(max(abs(mx), abs(my)) / 0.1f).toInt())
        val sx = mx / n
        val sy = my / n
        repeat(n) {
            if (!grid.blocked(x + sx, y, radius)) x += sx
            if (!grid.blocked(x, y + sy, radius)) y += sy
        }
        for (i in fruits.indices) {
            if (!collected[i] && dist(fruits[i]) < 0.55f) collected[i] = true
        }
        if (dist(goal) < 0.5f && allFruits) won = true
    }

    private fun dist(c: Pair<Int, Int>): Float {
        val dx = x - (c.first + 0.5f)
        val dy = y - (c.second + 0.5f)
        return kotlin.math.sqrt(dx * dx + dy * dy)
    }

    /** Pista: ruta hacia la fruta pendiente más cercana o, si no quedan, hacia la meta. */
    fun hintPath(): List<Pair<Int, Int>> {
        val from = Pair(floor(x).toInt(), floor(y).toInt())
        var best: List<Pair<Int, Int>> = emptyList()
        for (i in fruits.indices) {
            if (collected[i]) continue
            val p = grid.path(from, fruits[i])
            if (p.isNotEmpty() && (best.isEmpty() || p.size < best.size)) best = p
        }
        return if (best.isNotEmpty()) best else grid.path(from, goal)
    }
}
