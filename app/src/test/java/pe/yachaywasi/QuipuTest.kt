package pe.yachaywasi

import org.junit.Assert.*
import org.junit.Test

class QuipuTest {
    @Test fun eightLevelsWithValidRounds() {
        assertEquals(8, Quipu.levels.size)
        repeat(200) {
            for (l in Quipu.levels) for (r in Quipu.rounds(l)) when (r.kind) {
                "build" -> assertTrue(r.a in 1..10)
                "compare" -> assertNotEquals(r.a, r.b)
                "join" -> assertTrue(r.a >= 1 && r.b >= 1 && r.a + r.b <= l.hi)
                "take" -> assertTrue(r.b >= 1 && r.b < r.a && r.a <= l.hi)
                else -> fail("tipo desconocido ${r.kind}")
            }
        }
    }
}
