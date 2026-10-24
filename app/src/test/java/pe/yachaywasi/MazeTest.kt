package pe.yachaywasi

import org.junit.Assert.*
import org.junit.Test

class MazeTest {
    private fun engine(i: Int) = MazeEngine(MazeGrid(Mazes.all[i].rows))

    @Test fun mapsAreRectangularAndHaveStartAndGoal() {
        for (m in Mazes.all) {
            assertTrue(m.rows.all { it.length == m.rows[0].length })
            val g = MazeGrid(m.rows)
            assertEquals(1, g.find('S').size)
            assertEquals(1, g.find('G').size)
            assertTrue(g.path(g.find('S')[0], g.find('G')[0]).isNotEmpty())
        }
    }

    @Test fun mazeTwoHasThreeReachableFruits() {
        val g = MazeGrid(Mazes.all[1].rows)
        assertEquals(3, g.find('F').size)
        g.find('F').forEach { assertTrue(g.path(g.find('S')[0], it).isNotEmpty()) }
    }

    @Test fun mazeThreeRequiresTheBridge() {
        val g = MazeGrid(Mazes.all[2].rows)
        assertTrue(g.find('B').isNotEmpty())
        val noBridge = MazeGrid(Mazes.all[2].rows.map { it.replace('B', '~') })
        assertTrue(noBridge.path(g.find('S')[0], g.find('G')[0]).isEmpty())
    }

    @Test fun wallsAreNeverCrossed() {
        for (i in 0..2) {
            val e = engine(i)
            repeat(600) { e.step(-1f, -1f, 3.4f, 0.05f) }
            assertTrue(e.x >= 1f + e.radius - 1e-3f && e.y >= 1f + e.radius - 1e-3f)
            repeat(600) { e.step(1f, 1f, 3.4f, 0.05f) }
            assertFalse(e.grid.blocked(e.x, e.y, e.radius))
        }
    }

    @Test fun botFollowingHintsCompletesEveryMaze() {
        for (i in 0..2) {
            val e = engine(i)
            var guard = 0
            while (!e.won && guard++ < 30000) {
                val p = e.hintPath()
                assertTrue(p.isNotEmpty())
                val n = if (p.size > 1) p[1] else p[0]
                val dx = n.first + 0.5f - e.x
                val dy = n.second + 0.5f - e.y
                val len = kotlin.math.sqrt(dx * dx + dy * dy).coerceAtLeast(1e-4f)
                e.step(dx / len, dy / len, 2.4f, 0.016f)
            }
            assertTrue("laberinto ${i + 1} no terminó", e.won)
            assertEquals(e.fruits.size, e.collectedCount)
        }
    }

    @Test fun goalDoesNotCountBeforeAllFruits() {
        val e = engine(1)
        val g = e.goal
        e.x = g.first + 0.5f; e.y = g.second + 0.5f
        e.step(0f, 0f, 2f, 0.016f)
        assertFalse(e.won)
    }

    @Test fun tiltDeadZoneAndSaturation() {
        assertEquals(0f, Tilt.shape(0.5f, 0.9f, 5f), 0f)
        assertEquals(1f, Tilt.shape(9f, 0.9f, 5f), 0f)
        assertEquals(-1f, Tilt.shape(-9f, 0.9f, 5f), 0f)
        assertTrue(Tilt.shape(2f, 0.9f, 5f) in 0.1f..0.9f)
    }
}
