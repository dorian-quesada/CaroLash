package com.carolash.app

import java.time.LocalDate
import java.time.temporal.ChronoUnit

// ---------------------------------------------------------------- Guía de ojos

val EYES = listOf(
    "Pequeños", "Grandes", "Juntos", "Separados", "Ascendentes",
    "Descendentes", "Redondos", "Almendrados", "Encapotados", "Saltones",
)

/** Una letra por diseño [muñeca, ardilla, gato]: I = ideal, B = funciona bien, C = con cuidado, E = evitar. */
val RATING = listOf("IIC", "EBI", "EBI", "IBE", "BBC", "IIE", "EBI", "BBB", "IIC", "EBI")

val DESIGNS = listOf("Muñeca", "Ardilla", "Ojo de gato")

val DESIGN_TAGLINE = listOf(
    "Abre y redondea el ojo · mirada dulce",
    "Levanta el ojo · efecto lifting",
    "Alarga y rasga el ojo · mirada seductora",
)

val DESIGN_LONGEST = listOf(
    "Las más largas van al centro del ojo.",
    "Las más largas van más o menos a ¾ hacia la esquina externa y bajan un poco al final.",
    "Las más largas quedan en la esquina externa: alarga y rasga el ojo, mirada seductora.",
)

/** Largos (mm) de las 5 zonas del mapping para cada diseño. */
val PRESETS = listOf(
    listOf(8, 10, 12, 10, 9),
    listOf(8, 9, 11, 12, 10),
    listOf(8, 9, 10, 11, 12),
)

/** Barras de ejemplo para dibujar cada diseño en la guía. */
val BARS = listOf(
    listOf(7, 8, 10, 11, 12, 11, 10, 8, 7),
    listOf(7, 8, 9, 10, 11, 11, 12, 10, 8),
    listOf(8, 8, 9, 10, 11, 12, 13, 14, 15),
)

private fun scoreOf(letter: Char): Int = when (letter) {
    'I' -> 3
    'B' -> 2
    'C' -> 1
    else -> -3
}

fun ratingLabel(letter: Char): String = when (letter) {
    'I' -> "Ideal"
    'B' -> "Funciona bien"
    'C' -> "Con cuidado"
    else -> "Evitar"
}

/** Diseño que mejor le queda según las formas de ojos. Si hay empate gana ardilla. */
fun recommend(eyes: List<Int>): Int {
    if (eyes.isEmpty()) return 1
    val total = IntArray(3)
    for (e in eyes) {
        if (e !in RATING.indices) continue
        for (d in 0..2) total[d] += scoreOf(RATING[e][d])
    }
    var best = 1
    for (d in listOf(1, 0, 2)) if (total[d] > total[best]) best = d
    return best
}

// ---------------------------------------------------------------- Formatos

val MONTHS = listOf(
    "enero", "febrero", "marzo", "abril", "mayo", "junio", "julio",
    "agosto", "septiembre", "octubre", "noviembre", "diciembre",
)
val DOWS_FULL = listOf("lunes", "martes", "miércoles", "jueves", "viernes", "sábado", "domingo")
val DOWS_SHORT = listOf("L", "M", "M", "J", "V", "S", "D")

fun money(n: Int): String {
    val neg = n < 0
    val digits = Math.abs(n).toString().reversed().chunked(3).joinToString(" ").reversed()
    return (if (neg) "-" else "") + "₡" + digits
}

/** "10:00 a. m." */
fun timeText(min: Int): String {
    val h = (min / 60) % 24
    val m = min % 60
    val h12 = if (h % 12 == 0) 12 else h % 12
    return "$h12:${m.toString().padStart(2, '0')} ${if (h < 12) "a. m." else "p. m."}"
}

fun timeOnly(min: Int): String {
    val h = (min / 60) % 24
    val m = min % 60
    val h12 = if (h % 12 == 0) 12 else h % 12
    return "$h12:${m.toString().padStart(2, '0')}"
}

fun ampm(min: Int): String = if ((min / 60) % 24 < 12) "a. m." else "p. m."

fun cap(s: String): String = if (s.isEmpty()) s else s.substring(0, 1).uppercase() + s.substring(1)

/** "jueves 5 de noviembre" */
fun dayName(d: LocalDate): String = DOWS_FULL[d.dayOfWeek.value - 1] + " " + d.dayOfMonth + " de " + MONTHS[d.monthValue - 1]

fun shortDate(d: LocalDate): String = d.dayOfMonth.toString() + " " + MONTHS[d.monthValue - 1].take(3)

fun initials(name: String): String {
    val parts = name.trim().split(" ").filter { it.isNotEmpty() }
    if (parts.isEmpty()) return "?"
    val a = parts[0].first().uppercaseChar().toString()
    val b = if (parts.size > 1) parts[1].first().uppercaseChar().toString() else ""
    return a + b
}

fun firstName(name: String): String = name.trim().split(" ").firstOrNull { it.isNotEmpty() } ?: name

fun weekStart(d: LocalDate): LocalDate = d.minusDays((d.dayOfWeek.value - 1).toLong())

fun durationText(min: Int): String {
    val h = min / 60
    val m = min % 60
    return when {
        h == 0 -> "$m min"
        m == 0 -> "$h h"
        else -> "$h h $m"
    }
}

// ---------------------------------------------------------------- Citas y sugerencias

fun AppData.client(id: String): Client? = clients.firstOrNull { it.id == id }

fun AppData.apptsOn(day: LocalDate): List<Appointment> =
    appointments.filter { it.date == day && it.status != Status.CANCELED }.sortedBy { it.startMin }

fun isOpen(a: Appointment): Boolean = a.status == Status.PENDING || a.status == Status.CONFIRMED

data class Suggestion(val client: Client, val date: LocalDate, val lastVisit: LocalDate) {
    fun weeksSince(today: LocalDate): Int = (ChronoUnit.DAYS.between(lastVisit, today) / 7).toInt()
}

/**
 * Mantenimientos sugeridos: a cada clienta con una visita hecha se le suma su frecuencia.
 * No se sugiere si ya tiene una cita pendiente a futuro.
 */
fun suggestions(data: AppData, today: LocalDate): List<Suggestion> {
    val out = ArrayList<Suggestion>()
    for (c in data.clients) {
        if (c.weeks <= 0) continue
        val mine = data.appointments.filter { it.clientId == c.id }
        if (mine.any { isOpen(it) && !it.date.isBefore(today) }) continue
        val last = mine.filter { it.status == Status.DONE }.maxByOrNull { it.date } ?: continue
        out.add(Suggestion(c, last.date.plusWeeks(c.weeks.toLong()), last.date))
    }
    return out.sortedBy { it.date }
}

/** Clientas que hace más del doble de su frecuencia no vienen. */
fun inactiveWeeks(data: AppData, c: Client, today: LocalDate): Int? {
    val mine = data.appointments.filter { it.clientId == c.id }
    if (mine.any { isOpen(it) && !it.date.isBefore(today) }) return null
    val last = mine.filter { it.status == Status.DONE }.maxByOrNull { it.date } ?: return null
    val weeks = (ChronoUnit.DAYS.between(last.date, today) / 7).toInt()
    return if (weeks >= maxOf(6, c.weeks * 2)) weeks else null
}

fun nextAppt(data: AppData, c: Client, today: LocalDate): Appointment? =
    data.appointments.filter { it.clientId == c.id && isOpen(it) && !it.date.isBefore(today) }
        .minWithOrNull(compareBy({ it.date }, { it.startMin }))

fun visits(data: AppData, c: Client): Int = data.appointments.count { it.clientId == c.id && it.status == Status.DONE }

fun lastVisit(data: AppData, c: Client): Appointment? =
    data.appointments.filter { it.clientId == c.id && it.status == Status.DONE }.maxByOrNull { it.date }

// ---------------------------------------------------------------- Horarios y choques

data class Slot(val startMin: Int, val free: Boolean)

fun overlaps(aStart: Int, aEnd: Int, bStart: Int, bEnd: Int): Boolean = aStart < bEnd && aEnd > bStart

/** Horarios de media en media hora; los que chocan con otra cita (o el almuerzo) quedan ocupados. */
fun slotsFor(data: AppData, day: LocalDate, minutes: Int, ignoreId: String?, now: Int): List<Slot> {
    val s = data.settings
    val others = data.apptsOn(day).filter { it.id != ignoreId }
    val out = ArrayList<Slot>()
    var t = s.startMin
    while (t + minutes <= s.endMin) {
        val end = t + minutes
        var free = true
        if (s.lunch && overlaps(t, end, 12 * 60, 13 * 60)) free = false
        for (o in others) {
            if (overlaps(t, end + s.breakMin, o.startMin - s.breakMin, o.startMin + o.minutes + s.breakMin)) {
                free = false
            }
        }
        if (day == LocalDate.now() && t < now) free = false
        out.add(Slot(t, free))
        t += 30
    }
    return out
}

fun isWorkDay(data: AppData, day: LocalDate): Boolean = day.dayOfWeek.value in data.settings.workDays

// ---------------------------------------------------------------- Logros

data class Stats(
    val weekCount: Int,
    val monthIncome: Int,
    val monthCount: Int,
    val lastMonthIncome: Int,
    val lastMonthCount: Int,
    val streak: Int,
    val record: Int,
    val clientsServed: Int,
)

private fun doneInWeek(data: AppData, ws: LocalDate): Int =
    data.appointments.count { it.status == Status.DONE && !it.date.isBefore(ws) && it.date.isBefore(ws.plusDays(7)) }

fun stats(data: AppData, today: LocalDate): Stats {
    val done = data.appointments.filter { it.status == Status.DONE }
    val ws = weekStart(today)
    val weekCount = doneInWeek(data, ws)
    val thisMonth = done.filter { it.date.year == today.year && it.date.monthValue == today.monthValue }
    val lm = today.withDayOfMonth(1).minusMonths(1)
    val lastMonth = done.filter { it.date.year == lm.year && it.date.monthValue == lm.monthValue }
    val goal = data.settings.goalWeek
    var streak = 0
    var w = if (weekCount >= goal) ws else ws.minusWeeks(1)
    var guard = 0
    while (guard < 200 && goal > 0 && doneInWeek(data, w) >= goal) {
        streak++
        w = w.minusWeeks(1)
        guard++
    }
    var record = 0
    for (a in done) record = maxOf(record, doneInWeek(data, weekStart(a.date)))
    return Stats(
        weekCount = weekCount,
        monthIncome = thisMonth.sumOf { it.price },
        monthCount = thisMonth.size,
        lastMonthIncome = lastMonth.sumOf { it.price },
        lastMonthCount = lastMonth.size,
        streak = streak,
        record = record,
        clientsServed = done.map { it.clientId }.toSet().size,
    )
}

data class Badge(val id: String, val title: String, val text: String, val unlocked: Boolean, val progress: String?)

fun badges(data: AppData, st: Stats): List<Badge> {
    val goal = data.settings.goalIncome
    return listOf(
        Badge("first", "Primera clienta del mes", "Ya atendiste a tu primera clienta de este mes", st.monthCount >= 1, null),
        Badge("five", "5 clientas en un mes", "Llegaste a 5 clientas atendidas en un mes", st.monthCount >= 5,
            if (st.monthCount >= 5) null else "${st.monthCount} de 5"),
        Badge("streak", "Racha de 4 semanas", "Cuatro semanas seguidas cumpliendo tu meta semanal", st.streak >= 4,
            if (st.streak >= 4) null else "${st.streak} de 4"),
        Badge("week6", "Semana de 6 clientas", "Hoy completaste tu sexta clienta de la semana. ¡Qué buen ritmo, Caro!", st.weekCount >= 6 || st.record >= 6,
            if (st.weekCount >= 6 || st.record >= 6) null else "${st.weekCount} de 6"),
        Badge("goal", "Meta del mes", "Cumpliste tu meta de ingresos del mes", goal > 0 && st.monthIncome >= goal,
            if (goal > 0) "${minOf(100, st.monthIncome * 100 / goal)} %" else null),
        Badge("c25", "25 clientas", "Ya atendiste a 25 clientas distintas", st.clientsServed >= 25,
            if (st.clientsServed >= 25) null else "${st.clientsServed} de 25"),
    )
}

// ---------------------------------------------------------------- Mensajes de WhatsApp

val TEMPLATE_NAMES = listOf("24 horas antes", "Mismo día", "Confirmar")

private fun whenText(d: LocalDate, today: LocalDate): String = when (d) {
    today -> "hoy"
    today.plusDays(1) -> "mañana, " + dayName(d)
    else -> "el " + dayName(d)
}

fun reminderMessage(template: Int, c: Client, a: Appointment, today: LocalDate, indications: Boolean): String {
    val n = firstName(c.name)
    val time = timeText(a.startMin)
    val svc = a.service.lowercase()
    val extra = if (indications) " Recuerda llegar sin maquillaje en los ojos." else ""
    return when (template) {
        0 -> "Hola $n, te escribe Caro de Caro Lash. Te recuerdo tu cita de ${whenText(a.date, today)} a las $time ($svc). Si necesitas cambiar la hora, avísame por aquí.$extra ¡Te espero!"
        1 -> "Hola $n, hoy te espero a las $time en Caro Lash para tu $svc. Si vas atrasada o te surge algo, avísame por aquí.$extra"
        else -> "Hola $n, ¿me confirmas tu cita ${if (a.date == today) "de hoy" else "del " + dayName(a.date)} a las $time ($svc)? Respóndeme con un sí y la dejo confirmada.$extra"
    }
}

fun whatsappDigits(phone: String): String {
    val d = phone.filter { it.isDigit() }
    return if (d.length == 8) "506$d" else d
}

// ---------------------------------------------------------------- Datos de ejemplo

/** Datos de ejemplo para probar la app (se pueden cargar desde Ajustes). */
fun demoData(base: AppData, today: LocalDate): AppData {
    fun cl(id: String, name: String, phone: String, eyes: List<Int>, style: String, curve: String, thick: String, weeks: Int, allergy: String): Client {
        val design = recommend(eyes)
        return Client(id, name, phone, eyes, design, style, curve, thick, PRESETS[design], weeks, allergy, today.minusDays(120))
    }
    val clients = listOf(
        cl("paola", "Paola C.", "+506 8000 1234", listOf(0, 3, 5), "Volumen ruso", "D", "0.07 mm", 3, "Sensible al pegamento fuerte"),
        cl("daniela", "Daniela R.", "+506 8111 2200", listOf(8, 5), "Híbridas", "C", "0.10 mm", 3, ""),
        cl("mariana", "Mariana J.", "+506 8222 3344", listOf(1, 2, 6), "Clásicas", "C", "0.15 mm", 3, ""),
        cl("andrea", "Andrea S.", "+506 8333 4455", listOf(7, 4), "Híbridas", "CC", "0.10 mm", 3, ""),
        cl("camila", "Camila V.", "+506 8444 5566", listOf(0, 3), "Volumen ruso", "D", "0.07 mm", 4, ""),
        cl("fernanda", "Fernanda L.", "+506 8555 6677", listOf(1, 9), "Clásicas", "C", "0.15 mm", 3, ""),
        cl("valeria", "Valeria M.", "+506 8666 7788", listOf(7), "Híbridas", "C", "0.10 mm", 2, ""),
        cl("lucia", "Lucía P.", "+506 8777 8899", listOf(2, 0), "Clásicas", "C", "0.15 mm", 3, ""),
    )
    val svc = base.services
    fun pick(i: Int): Service = svc[i % svc.size]
    val appts = ArrayList<Appointment>()
    var n = 0
    fun add(cid: String, day: LocalDate, start: Int, svcIdx: Int, st: Status) {
        val s = pick(svcIdx)
        n++
        appts.add(Appointment("demo$n", cid, s.name, s.price, day, start, s.minutes, st, false))
    }
    // Hoy y mañana
    add("valeria", today, 9 * 60, 1, Status.DONE)
    add("daniela", today, 13 * 60, 3, Status.CONFIRMED)
    add("camila", today, 16 * 60 + 30, 2, Status.PENDING)
    add("paola", today.plusDays(1), 10 * 60, 3, Status.CONFIRMED)
    add("andrea", today.plusDays(1), 14 * 60 + 30, 1, Status.PENDING)
    // Historial reciente: tres semanas anteriores con 4 clientas cada una
    val ws = weekStart(today)
    val weekClients = listOf("paola", "andrea", "camila", "valeria")
    for (w in 1..3) {
        for (k in 0..3) {
            add(weekClients[k], ws.minusWeeks(w.toLong()).plusDays(k.toLong()), 9 * 60 + k * 120, (w + k) % 4, Status.DONE)
        }
    }
    // Esta semana, ya hechas (antes de hoy) para que haya avance
    val monday = ws
    var extra = 0
    var d = monday
    while (d.isBefore(today) && extra < 4) {
        add(weekClients[extra % 4], d, 10 * 60, (extra + 1) % 4, Status.DONE)
        extra++
        d = d.plusDays(1)
    }
    // Clientas con mantenimiento vencido o que hace tiempo no vienen
    add("mariana", today.minusDays(24), 11 * 60, 3, Status.DONE)
    add("lucia", today.minusDays(22), 15 * 60, 0, Status.DONE)
    add("fernanda", today.minusDays(49), 10 * 60, 0, Status.DONE)
    return base.copy(clients = clients, appointments = appts)
}
