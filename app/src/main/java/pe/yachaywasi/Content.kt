package pe.yachaywasi

/** Tipos de pregunta. Hay tres: elegir, tocar todas las correctas y ordenar. */
sealed class Q { abstract val say: String; abstract val hint: String }
class Choice(
    override val say: String, val show: String, val options: List<String>, val answer: Int,
    override val hint: String, val en: String? = null
) : Q()
class TapAll(override val say: String, val items: List<String>, val targets: Set<Int>, override val hint: String) : Q()
class Order(override val say: String, val items: List<String>, override val hint: String) : Q()

class Activity(
    val id: String, val title: String, val emoji: String, val area: String,
    val objective: String, val qs: List<Q>
)
class Place(val id: String, val name: String, val emoji: String, val needStars: Int, val activity: Activity?)

object Content {
    private fun count(e: String, n: Int, name: String): Choice {
        val others = mutableListOf(n + 1, if (n > 1) n - 1 else n + 2)
        others.add(n % 3, n)
        return Choice("¿Cuántos $name hay?", e.repeat(n), others.map { it.toString() }, n % 3,
            "Toca cada uno con el dedo y cuenta en voz alta.")
    }

    // ---------- AVENTURAS ----------
    val mercado = Activity("adv.mercado", "El mercado", "🧺", "Matemática y Comunicación",
        "Contar, reconocer colores, clasificar alimentos y relacionar objetos con palabras.", listOf(
            Choice("¿Cuántas manzanas tenemos?", "🍎🍎🍎", listOf("2", "3", "5"), 1, "Toca cada manzana y cuenta: uno, dos, tres."),
            Choice("¿De qué color es el plátano?", "🍌", listOf("🟥 Rojo", "🟨 Amarillo", "🟦 Azul"), 1, "Mira bien el plátano. Es del color del sol."),
            TapAll("Toca todas las frutas.", listOf("🍎", "🥕", "🍌", "🥛", "🍇", "🍞"), setOf(0, 2, 4), "Las frutas son dulces y crecen en árboles o plantas."),
            Choice("¿Cuál es el pan?", "PAN", listOf("🍞", "🥛", "🥚"), 0, "Lee con calma: P, A, N. Pan se come con mantequilla.")
        ))
    val hogar = Activity("adv.hogar", "Mi hogar", "🏠", "Comunicación y Convivencia",
        "Reconocer objetos cotidianos, ordenar secuencias y practicar hábitos de higiene.", listOf(
            Choice("¿Dónde dormimos?", "🌙", listOf("🛏️ Cama", "🍽️ Plato", "⚽ Pelota"), 0, "Dormimos de noche, cuando sale la luna."),
            Order("Ordena lo que hacemos en la mañana.", listOf("🌅 Despertar", "🦷 Lavar dientes", "🍲 Desayunar", "🎒 Ir a la escuela"), "Primero abrimos los ojos. Al final salimos de casa."),
            Choice("¿Qué usamos para lavarnos las manos?", "👐", listOf("🚿 Jabón y agua", "🧱 Piedra", "🍞 Pan"), 0, "Para quedar limpios necesitamos agua y jabón."),
            TapAll("Toca los objetos de la cocina.", listOf("🍳", "🛏️", "🥄", "🎈", "🍽️", "🛋️"), setOf(0, 2, 4), "En la cocina preparamos y servimos la comida.")
        ))
    val chacra = Activity("adv.chacra", "La chacra", "🌽", "Ciencia y Matemática",
        "Identificar animales y plantas, observar patrones y entender causa y efecto.", listOf(
            Choice("¿Qué animal nos da lana?", "🧵", listOf("🐑 Oveja", "🐔 Gallina", "🐄 Vaca"), 0, "Su lana es suave y abriga."),
            Order("Ordena cómo crece el maíz.", listOf("🌰 Semilla", "🌱 Brote", "🌿 Planta", "🌽 Choclo"), "Todo empieza con una semilla pequeña."),
            Choice("¿Qué necesita la planta para crecer?", "🌱", listOf("💧 Agua y sol", "🧱 Solo piedras", "🍬 Dulces"), 0, "Piensa: ¿qué le damos a las plantas todos los días?"),
            Choice("¿Qué sigue en el patrón?", "🌽🥔🌽🥔🌽❓", listOf("🌽", "🥔"), 1, "Se repite: maíz, papa, maíz, papa…")
        ))
    val rio = Activity("adv.rio", "El río", "🏞️", "Ciencia y Convivencia",
        "Descubrir el recorrido del agua, reconocer seres vivos y cuidar el ambiente.", listOf(
            Order("Ordena el viaje del agua.", listOf("☁️ Nube", "🌧️ Lluvia", "🏞️ Río", "🌊 Mar"), "El agua cae del cielo y baja hasta el mar."),
            Choice("¿Quién vive en el río?", "🏞️", listOf("🐟 Pez", "🐄 Vaca", "🐓 Gallo"), 0, "Vive dentro del agua y nada."),
            Choice("Hay basura en el río. ¿Qué hacemos?", "🗑️", listOf("♻️ La recogemos", "🌊 La tiramos más", "🙈 No miramos"), 0, "Cuidar el río es cuidar a todos los que viven allí.")
        ))

    val places = listOf(
        Place("hogar", "Mi hogar", "🏠", 0, hogar),
        Place("pueblo", "El pueblo", "🏘️", 99, null),
        Place("mercado", "El mercado", "🧺", 0, mercado),
        Place("chacra", "La chacra", "🌽", 3, chacra),
        Place("bosque", "El bosque", "🌳", 99, null),
        Place("rio", "El río", "🏞️", 6, rio),
        Place("jardin", "El jardín", "🌸", 99, null),
        Place("montana", "La montaña", "⛰️", 99, null),
        Place("lab", "Laboratorio de inventos", "🔬", 99, null)
    )

    // ---------- APRENDER ----------
    private val vocales = Activity("l1.vocales", "Vocales", "🅰️", "Comunicación",
        "Relacionar el sonido inicial de una palabra con su vocal.", listOf(
            Choice("¿Con qué vocal empieza águila?", "🦅", listOf("A", "E", "I"), 0, "Escucha: á-gui-la. Empieza con la boca bien abierta."),
            Choice("¿Con qué vocal empieza elefante?", "🐘", listOf("O", "E", "U"), 1, "Escucha: e-le-fan-te."),
            Choice("¿Con qué vocal empieza isla?", "🏝️", listOf("I", "A", "O"), 0, "Escucha: is-la. Es un sonido delgado."),
            Choice("¿Con qué vocal empieza oso?", "🐻", listOf("U", "I", "O"), 2, "Escucha: o-so. Hacemos la boca redonda."),
            Choice("¿Con qué vocal empieza uva?", "🍇", listOf("E", "U", "A"), 1, "Escucha: u-va. Los labios hacen un tubito.")
        ))
    private val silabas = Activity("l1.silabas", "Sílabas", "🔤", "Comunicación",
        "Formar palabras uniendo sílabas.", listOf(
            Order("Forma la palabra MESA.", listOf("ME", "SA"), "Mesa suena: me… sa."),
            Order("Forma la palabra LUNA.", listOf("LU", "NA"), "Luna suena: lu… na."),
            Order("Forma la palabra PATO.", listOf("PA", "TO"), "Pato suena: pa… to."),
            Choice("M y A juntas suenan…", "M + A", listOf("MA", "AM", "AE"), 0, "Junta los sonidos: mmm-aaa."),
            Choice("¿Qué sílaba falta? LU _", "🌙  LU _", listOf("NA", "TO", "SA"), 0, "Luna: lu… na.")
        ))
    private val lectura = Activity("l1.lectura", "Leo palabras", "📖", "Comunicación",
        "Leer palabras cortas y asociarlas con su imagen.", listOf(
            Choice("¿Cuál es la imagen de la palabra GATO?", "GATO", listOf("🐱", "🐶", "🐟"), 0, "Lee despacio: GA… TO."),
            Choice("¿Cuál es la imagen de la palabra LUNA?", "LUNA", listOf("☀️", "🌙", "⭐"), 1, "Lee despacio: LU… NA."),
            Choice("¿Cuál es la imagen de la palabra SOL?", "SOL", listOf("🌙", "🌧️", "☀️"), 2, "Lee: S… O… L."),
            Choice("¿Cuál es la imagen de la palabra PAPA?", "PAPA", listOf("🥔", "🍎", "🥕"), 0, "Lee despacio: PA… PA.")
        ))
    private val conteo = Activity("l1.conteo", "Cuento y comparo", "🔢", "Matemática",
        "Relacionar cantidad con número y comparar grupos.", listOf(
            count("🍎", 3, "manzanas"), count("🐟", 5, "peces"), count("⭐", 2, "estrellas"), count("🌽", 4, "choclos"),
            Choice("¿Dónde hay más?", "🍎🍎🍎  y  🍎🍎", listOf("🍎🍎🍎", "🍎🍎"), 0, "Cuenta cada grupo. El número más grande tiene más."),
            Choice("¿Dónde hay menos?", "🐟🐟  y  🐟🐟🐟🐟", listOf("🐟🐟", "🐟🐟🐟🐟"), 0, "Cuenta los peces de cada grupo.")
        ))
    private val figuras = Activity("l1.figuras", "Figuras y patrones", "🔺", "Matemática",
        "Reconocer figuras geométricas y continuar patrones.", listOf(
            Choice("¿Cuál es el círculo?", "❓", listOf("🔵", "🟦", "🔺"), 0, "El círculo es redondo, sin esquinas."),
            Choice("¿Cuál es el cuadrado?", "❓", listOf("🔺", "🔵", "🟦"), 2, "El cuadrado tiene 4 esquinas iguales."),
            Choice("¿Cuál es el triángulo?", "❓", listOf("🔺", "🟦", "🔵"), 0, "El triángulo tiene 3 esquinas."),
            Choice("¿Qué sigue en el patrón?", "🔴🔵🔴🔵🔴❓", listOf("🔴", "🔵"), 1, "Se repite: rojo, azul, rojo, azul…")
        ))
    private val sumas = Activity("l1.sumas", "Sumas y restas", "➕", "Matemática",
        "Sumar y restar con objetos en problemas cotidianos.", listOf(
            Choice("Hay 2 manzanas y llega 1 más. ¿Cuántas hay?", "🍎🍎 + 🍎", listOf("2", "3", "4"), 1, "Cuenta todas juntas: uno, dos, tres."),
            Choice("Había 4 peces y se fue 1. ¿Cuántos quedan?", "🐟🐟🐟🐟 − 🐟", listOf("5", "3", "2"), 1, "Tacha un pez y cuenta los que quedan."),
            Choice("Tito tiene 3 frutas y encuentra 2. ¿Cuántas tiene?", "🍇🍇🍇 + 🍇🍇", listOf("5", "4", "6"), 0, "Cuenta desde 3: cuatro, cinco.")
        ))
    private val entorno = Activity("l1.entorno", "Mi cuerpo y mi comunidad", "🧒", "Entorno y convivencia",
        "Reconocer partes del cuerpo, hábitos saludables y convivencia respetuosa.", listOf(
            Choice("¿Con qué vemos?", "👀", listOf("👁️ Ojos", "👂 Orejas", "👃 Nariz"), 0, "Los usamos para mirar."),
            Choice("¿Con qué oímos?", "🎵", listOf("👃 Nariz", "👂 Orejas", "👄 Boca"), 1, "Los usamos para escuchar música y voces."),
            Choice("Un amigo se cae. ¿Qué hacemos?", "🤕", listOf("🤝 Lo ayudamos", "😝 Nos reímos", "🏃 Corremos"), 0, "Los buenos amigos se cuidan."),
            Choice("¿Qué alimento nos hace crecer fuertes?", "💪", listOf("🥕 Verduras", "🍬 Solo dulces", "🧂 Solo sal"), 0, "Las verduras tienen vitaminas.")
        ))
    private val oraciones = Activity("l2.oraciones", "Oraciones", "✍️", "Comunicación",
        "Construir oraciones y comprender textos breves.", listOf(
            Order("Ordena la oración.", listOf("El", "gato", "duerme"), "Empieza con «El»."),
            Order("Ordena la oración.", listOf("Mamá", "cocina", "sopa"), "Primero va quién hace la acción."),
            Choice("Tito corre al río. ¿Quién corre?", "🐹 Tito corre al río.", listOf("Tito", "El río", "Mamá"), 0, "Pregúntate: ¿quién hace la acción de correr?"),
            Choice("Ana riega las plantas. ¿Qué hace Ana?", "👧 Ana riega las plantas.", listOf("Riega", "Plantas", "Corre"), 0, "La acción es lo que hace Ana.")
        ))
    private val numeros2 = Activity("l2.numeros", "Números hasta 100", "💯", "Matemática",
        "Sumas y restas progresivas, secuencias y comparación de cantidades.", listOf(
            Choice("12 + 5 = ?", "12 + 5", listOf("17", "16", "18"), 0, "Suma primero 10 y luego 2 + 5."),
            Choice("20 − 8 = ?", "20 − 8", listOf("13", "12", "11"), 1, "Quita 10 y devuelve 2."),
            Choice("Completa: 10, 20, 30, ❓", "10, 20, 30, ❓", listOf("35", "50", "40"), 2, "Va de 10 en 10."),
            Choice("¿Cuál número es mayor?", "45  o  54", listOf("45", "54"), 1, "Compara primero las decenas."),
            Choice("¿Cuántos días tiene una semana?", "📅", listOf("5", "7", "10"), 1, "Lunes, martes, miércoles… cuéntalos.")
        ))
    private val ciclos2 = Activity("l2.ciclos", "Ciclos de vida y agua", "🐣", "Ciencia y convivencia",
        "Ordenar ciclos de vida y cuidar el agua.", listOf(
            Order("Ordena el ciclo de la gallina.", listOf("🥚 Huevo", "🐣 Pollito", "🐥 Polluelo", "🐔 Gallina"), "Todo empieza dentro de un huevo."),
            Choice("¿Cómo cuidamos el agua?", "💧", listOf("🚰 Cerramos el caño", "🌊 Dejamos correr", "🗑️ La ensuciamos"), 0, "El agua es de todos y se acaba si la gastamos."),
            Choice("Dos amigos quieren el mismo juguete. ¿Qué hacemos?", "🧩", listOf("🤝 Hablamos y turnamos", "😠 Gritamos", "💥 Lo quitamos"), 0, "Hablar y turnarse resuelve los problemas en paz.")
        ))

    fun learn(grade: Int): List<Activity> =
        if (grade == 2) listOf(oraciones, numeros2, ciclos2, lectura, sumas)
        else listOf(vocales, silabas, lectura, conteo, figuras, sumas, entorno)

    val allActivities: List<Activity> get() = places.mapNotNull { it.activity } + listOf(
        vocales, silabas, lectura, conteo, figuras, sumas, entorno, oraciones, numeros2, ciclos2)
}

// ---------- INGLÉS: primeras 50 palabras ----------
class Word(val en: String, val es: String, val emoji: String)
class WordCat(val id: String, val name: String, val emoji: String, val words: List<Word>)

object English {
    val cats = listOf(
        WordCat("animals", "Animales", "🐱", listOf(Word("cat", "gato", "🐱"), Word("dog", "perro", "🐶"), Word("bird", "pájaro", "🐦"), Word("fish", "pez", "🐟"), Word("cow", "vaca", "🐄"))),
        WordCat("colors", "Colores", "🎨", listOf(Word("red", "rojo", "🔴"), Word("blue", "azul", "🔵"), Word("yellow", "amarillo", "🟡"), Word("green", "verde", "🟢"), Word("black", "negro", "⚫"))),
        WordCat("numbers", "Números", "🔢", listOf(Word("one", "uno", "1"), Word("two", "dos", "2"), Word("three", "tres", "3"), Word("four", "cuatro", "4"), Word("five", "cinco", "5"))),
        WordCat("family", "Familia", "👪", listOf(Word("mom", "mamá", "👩"), Word("dad", "papá", "👨"), Word("baby", "bebé", "👶"), Word("sister", "hermana", "👧"), Word("brother", "hermano", "👦"))),
        WordCat("body", "Cuerpo", "🖐️", listOf(Word("head", "cabeza", "🙂"), Word("eyes", "ojos", "👀"), Word("nose", "nariz", "👃"), Word("mouth", "boca", "👄"), Word("hands", "manos", "🖐️"))),
        WordCat("school", "Escuela", "🎒", listOf(Word("book", "libro", "📖"), Word("pen", "lapicero", "🖊️"), Word("bag", "mochila", "🎒"), Word("desk", "carpeta / pupitre", "💺"), Word("pencil", "lápiz", "✏️"))),
        WordCat("food", "Alimentos", "🍎", listOf(Word("apple", "manzana", "🍎"), Word("banana", "plátano", "🍌"), Word("bread", "pan", "🍞"), Word("milk", "leche", "🥛"), Word("egg", "huevo", "🥚"))),
        WordCat("nature", "Naturaleza", "🌳", listOf(Word("sun", "sol", "☀️"), Word("moon", "luna", "🌙"), Word("tree", "árbol", "🌳"), Word("flower", "flor", "🌸"), Word("water", "agua", "💧"))),
        WordCat("actions", "Acciones", "🏃", listOf(Word("run", "correr", "🏃"), Word("jump", "saltar", "🤸"), Word("eat", "comer", "🍽️"), Word("play", "jugar", "⚽"), Word("sleep", "dormir", "😴"))),
        WordCat("phrases", "Expresiones", "👋", listOf(Word("hello", "hola", "🙋"), Word("goodbye", "adiós", "👋"), Word("please", "por favor", "🙏"), Word("yes", "sí", "✅"), Word("no", "no", "❌")))
    )

    /** "Escucha y toca": se dice la palabra en inglés y el niño elige la imagen. No hace falta saber leer. */
    fun listenGame(cat: WordCat): Activity {
        val ws = cat.words
        val qs = ws.mapIndexed { i, w ->
            val others = ws.filter { it !== w }.take(2).toMutableList()
            others.add(i % 3, w)
            Choice("Escucha y toca la imagen.", "🔊", others.map { it.emoji }, i % 3,
                "Escucha otra vez. ${w.en} significa ${w.es}.", en = w.en)
        }
        return Activity("eng.${cat.id}", "Inglés: ${cat.name}", cat.emoji, "Inglés",
            "Asociar la pronunciación de 5 palabras en inglés con su imagen.", qs)
    }
}
