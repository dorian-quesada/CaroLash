package com.carolash.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.time.LocalDate

// ------------------------------------------------------------------ Pantallas

sealed interface Screen

data object Welcome : Screen
data class ServiciosS(val settings: Boolean) : Screen
data class HorarioS(val settings: Boolean) : Screen
data class MetasS(val settings: Boolean) : Screen
data object Loading : Screen
data object HoyS : Screen
data class AgendaS(val focus: LocalDate? = null) : Screen
data object ClientasS : Screen
data object LogrosS : Screen
data object AjustesS : Screen
data class RecordatorioS(val apptId: String) : Screen
data class FichaS(val clientId: String) : Screen
data object GuiaS : Screen
data class UnlockedS(val badgeId: String) : Screen

/** Hojas que suben desde abajo encima de la pantalla actual. */
sealed interface Sheet
data object NuevaClientaSheet : Sheet
data class ReservarSheet(val clientId: String?, val date: LocalDate?, val editId: String?) : Sheet
data class CitaSheet(val apptId: String) : Sheet

enum class Kind { PUSH, POP, TAB, FADE }

val TAB_ORDER = listOf("Hoy", "Agenda", "Clientas", "Logros", "Ajustes")

fun tabIndex(s: Screen): Int = when (s) {
    is HoyS -> 0
    is AgendaS -> 1
    is ClientasS -> 2
    is LogrosS -> 3
    is AjustesS -> 4
    else -> -1
}

fun tabScreen(i: Int): Screen = when (i) {
    0 -> HoyS
    1 -> AgendaS()
    2 -> ClientasS
    3 -> LogrosS
    else -> AjustesS
}

// ------------------------------------------------------------------ Estado de la app

class App(val store: Store, startOnboarded: Boolean) {
    val stack = mutableStateListOf<Screen>(if (startOnboarded) Loading else Welcome)
    var kind by mutableStateOf(Kind.FADE)
    var tabDir by mutableIntStateOf(1)
    var sheet by mutableStateOf<Sheet?>(null)
    var toastText by mutableStateOf<String?>(null)
    var toastId by mutableIntStateOf(0)
    var today by mutableStateOf(LocalDate.now())
    /** Cuenta cada vez que se pide "volver a empezar" la animación de la pantalla de carga. */
    var loadingMs by mutableIntStateOf(if (startOnboarded) 900 else 1700)

    var scope: kotlinx.coroutines.CoroutineScope? = null

    val data: AppData get() = store.data
    val current: Screen get() = stack[stack.lastIndex]
    val isTab: Boolean get() = tabIndex(current) >= 0

    fun refreshToday() {
        val n = LocalDate.now()
        if (n != today) today = n
    }

    fun push(s: Screen) {
        kind = Kind.PUSH
        stack.add(s)
    }

    fun pop() {
        if (stack.size > 1) {
            kind = Kind.POP
            stack.removeAt(stack.lastIndex)
        }
    }

    fun pushFade(s: Screen) {
        kind = Kind.FADE
        stack.add(s)
    }

    fun popFade() {
        if (stack.size > 1) {
            kind = Kind.FADE
            stack.removeAt(stack.lastIndex)
        }
    }

    fun tab(i: Int) {
        val target = tabScreen(i)
        val cur = tabIndex(current)
        if (cur == i) return
        tabDir = if (i > cur) 1 else -1
        kind = Kind.TAB
        stack.clear()
        stack.add(target)
    }

    /** Abre la agenda en un día concreto. */
    fun openAgendaOn(day: LocalDate) {
        tabDir = 1
        kind = Kind.TAB
        stack.clear()
        stack.add(AgendaS(day))
    }

    fun root(s: Screen, k: Kind = Kind.FADE) {
        kind = k
        stack.clear()
        stack.add(s)
    }

    fun openSheet(s: Sheet) {
        sheet = s
    }

    fun closeSheet() {
        sheet = null
    }

    fun toast(text: String) {
        toastText = text
        toastId += 1
    }

    fun update(change: (AppData) -> AppData) = store.update(change)

    /** Marca la cita como hecha. Devuelve el id de una insignia nueva si se desbloqueó alguna. */
    fun finishAppointment(id: String): String? {
        val before = data
        update { d -> d.copy(appointments = d.appointments.map { if (it.id == id) it.copy(status = Status.DONE) else it }) }
        val had = badges(before, stats(before, today)).filter { it.unlocked }.map { it.id }.toSet()
        val st = stats(data, today)
        val fresh = badges(data, st).firstOrNull { it.unlocked && it.id !in had && it.id !in before.seenBadges }
        if (fresh != null) {
            update { d -> d.copy(seenBadges = d.seenBadges + fresh.id) }
            return fresh.id
        }
        return null
    }
}
