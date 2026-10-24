package pe.yachaywasi

import org.junit.Assert.*
import org.junit.Test

class ContentTest {
    private fun check(a: Activity) {
        assertTrue("${a.id} sin preguntas", a.qs.isNotEmpty())
        for (q in a.qs) {
            assertTrue(q.say.isNotBlank() && q.hint.isNotBlank())
            when (q) {
                is Choice -> { assertTrue(q.options.size >= 2); assertTrue("${a.id}: ${q.say}", q.answer in q.options.indices) }
                is TapAll -> assertTrue(q.targets.isNotEmpty() && q.targets.all { it in q.items.indices })
                is Order -> assertTrue(q.items.size >= 2)
            }
        }
    }

    @Test fun everyActivityIsWellFormed() {
        Content.allActivities.forEach(::check)
        English.cats.forEach { check(English.listenGame(it)) }
    }

    @Test fun fiftyEnglishWords() {
        val words = English.cats.flatMap { it.words }
        assertEquals(10, English.cats.size)
        assertEquals(50, words.size)
        assertEquals(50, words.map { it.en }.toSet().size)
    }

    @Test fun listenGameAnswerMatchesSpokenWord() {
        for (c in English.cats) for ((i, q) in English.listenGame(c).qs.withIndex()) {
            q as Choice
            assertEquals(c.words[i].emoji, q.options[q.answer])
            assertEquals(c.words[i].en, q.en)
        }
    }

    @Test fun patternGeneratorAlwaysHasValidAnswer() {
        for (level in 0..2) repeat(50) { patternQs(level).forEach { q -> q as Choice; assertTrue(q.answer in q.options.indices) } }
    }

    @Test fun placesAndPaths() {
        assertEquals(9, Content.places.size)
        assertTrue(Content.learn(1).isNotEmpty() && Content.learn(2).isNotEmpty())
        assertEquals(3, starsFor(0)); assertEquals(2, starsFor(2)); assertEquals(1, starsFor(5))
    }
}
