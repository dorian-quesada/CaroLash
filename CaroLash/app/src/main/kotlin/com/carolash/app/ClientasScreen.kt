package com.carolash.app

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate

private val FILTERS = listOf("Todas", "Mantenimiento", "Inactivas")

private data class Tag(val text: String, val bg: Color, val fg: Color)

private fun tagFor(d: AppData, c: Client, today: LocalDate, sg: List<Suggestion>): Tag? {
    val next = nextAppt(d, c, today)
    if (next != null) {
        val t = when (next.date) {
            today -> "Hoy ${timeText(next.startMin)}"
            today.plusDays(1) -> "Mañana ${timeOnly(next.startMin)}"
            else -> shortDate(next.date)
        }
        return Tag(t, C.Teal100, C.TealText)
    }
    if (sg.any { it.client.id == c.id && !it.date.isAfter(today.plusDays(7)) }) return Tag("Mantenimiento", C.Amber100, C.Amber900)
    val inactive = inactiveWeeks(d, c, today)
    if (inactive != null) return Tag("$inactive sem. sin venir", C.DangerBg, C.Danger)
    if (visits(d, c) == 0 && c.createdOn == today) return Tag("Nueva", C.Teal100, C.TealText)
    return null
}

@Composable
fun ClientasScreen(app: App) {
    val d = app.data
    val today = app.today
    var filter by remember { mutableStateOf(0) }
    var query by remember { mutableStateOf("") }
    val sg = suggestions(d, today)
    val list = d.clients
        .filter { query.isBlank() || it.name.contains(query.trim(), ignoreCase = true) }
        .filter {
            when (filter) {
                1 -> sg.any { s -> s.client.id == it.id && !s.date.isAfter(today.plusDays(7)) } && nextAppt(d, it, today) == null
                2 -> inactiveWeeks(d, it, today) != null
                else -> true
            }
        }
        .sortedBy { it.name.lowercase() }

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(top = 18.dp, bottom = navPad() + 84.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(Modifier.fillMaxWidth().height(44.dp).rise(0), verticalAlignment = Alignment.CenterVertically) {
                Txt("Clientas", 28, W8, modifier = Modifier.weight(1f))
                Txt("${d.clients.size} en total", 14, W6, C.Muted)
            }
            Row(
                Modifier.fillMaxWidth().rise(1).height(48.dp).background(Color.White, RoundedCornerShape(14.dp)).border(1.5.dp, C.Input, RoundedCornerShape(14.dp)).padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Ico(Ic.Search, C.Muted, 20.dp, 2f)
                Spacer(Modifier.width(10.dp))
                Field(query, { query = it }, Modifier.weight(1f), hint = "Buscar clienta", bordered = false, height = 44, size = 16, w = W6)
            }
            Row(Modifier.fillMaxWidth().rise(2), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FILTERS.forEachIndexed { i, n -> Chip(n, filter == i, { filter = i }) }
            }
            key(filter, query.isBlank()) {
                if (d.clients.isEmpty()) {
                    Box(Modifier.rise(3)) {
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Ico(Ic.People, C.Soft, 44.dp, 1.6f)
                                Txt("Aún no tienes clientas", 16, W8)
                                Txt("Agrégalas con su número y la forma de sus ojos para que la app te sugiera el diseño.", 14, W4, C.Muted, align = TextAlign.Center, lh = 20)
                            }
                        }
                    }
                } else if (list.isEmpty()) {
                    Box(Modifier.rise(3)) { EmptyNote("No hay clientas en este filtro.") }
                } else {
                    list.forEachIndexed { i, c ->
                        Box(Modifier.rise(3 + minOf(i, 8))) { ClientRow(app, c, tagFor(d, c, today, sg)) }
                    }
                }
            }
        }
        // Botón "Nueva"
        Row(
            Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = navPad() + 14.dp)
                .height(52.dp)
                .bounceClick(label = "Nueva clienta", onClick = { app.openSheet(NuevaClientaSheet) })
                .background(C.Teal, RoundedCornerShape(26.dp))
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Ico(Ic.Plus, Color.White, 22.dp, 2.4f)
            Spacer(Modifier.width(8.dp))
            Txt("Nueva", 16, W8, Color.White)
        }
    }
}

@Composable
private fun ClientRow(app: App, c: Client, tag: Tag?) {
    val d = app.data
    val sub = if (visits(d, c) == 0) DESIGNS[c.design] else DESIGNS[c.design] + (if (c.style.isNotBlank()) " · " + c.style else "")
    Card(Modifier.fillMaxWidth(), onClick = { app.push(FichaS(c.id)) }, label = "Abrir ficha de ${c.name}") {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Avatar(c.name)
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Txt(c.name, 16, W8, maxLines = 1)
                Txt(sub, 13, W6, C.Muted, maxLines = 1)
            }
            if (tag != null) Pill(tag.text, tag.bg, tag.fg)
        }
    }
}

// ------------------------------------------------------------------ Hoja: nueva clienta

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NuevaClientaContent(app: App) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var eyes by remember { mutableStateOf(listOf<Int>()) }
    var weeks by remember { mutableStateOf(3) }
    var saved by remember { mutableStateOf(false) }
    val rec = recommend(eyes)

    SheetPanel(0.92f) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            Txt("Nueva clienta", 22, W8, modifier = Modifier.weight(1f))
            IconBtn(Ic.Close, "Cerrar", { app.closeSheet() }, modifier = Modifier.offset(x = 10.dp))
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(top = 6.dp, bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column {
                Txt("Nombre", 13, W7, C.Muted, Modifier.padding(bottom = 6.dp))
                Field(name, { name = it.take(40) }, Modifier.fillMaxWidth(), hint = "Ej. Sofía R.", height = 48)
            }
            Column {
                Txt("Teléfono (WhatsApp)", 13, W7, C.Muted, Modifier.padding(bottom = 6.dp))
                Field(phone, { phone = it.filter { ch -> ch.isDigit() || ch == ' ' }.take(12) }, Modifier.fillMaxWidth(), hint = "8000 1234", height = 48, keyboard = KeyboardType.Phone, prefix = "+506")
            }
            Column {
                Txt("Forma de sus ojos (puedes elegir varias)", 13, W7, C.Muted, Modifier.padding(bottom = 8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    EYES.forEachIndexed { i, n ->
                        Chip(n, i in eyes, { eyes = if (i in eyes) eyes - i else eyes + i })
                    }
                }
            }
            Row(
                Modifier.fillMaxWidth().background(C.Amber100, RoundedCornerShape(14.dp)).padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Ico(Ic.Star, C.Amber900, 20.dp, fill = true)
                AnimatedContent(
                    targetState = if (eyes.isEmpty()) -1 else rec,
                    transitionSpec = { fadeIn(tween(260)) togetherWith fadeOut(tween(120)) },
                    label = "rec",
                ) { r ->
                    Column {
                        if (r < 0) {
                            Txt("Diseño recomendado", 14, W8, C.Amber950)
                            Txt("Elige la forma de sus ojos y te digo cuál le queda mejor.", 13, W6, C.Amber900, lh = 18)
                        } else {
                            Txt("Diseño recomendado: ${DESIGNS[r].lowercase()}", 14, W8, C.Amber950)
                            Txt(DESIGN_TAGLINE[r], 13, W6, C.Amber900, lh = 18)
                        }
                    }
                }
            }
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(start = 14.dp, end = 8.dp, top = 6.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Txt("Mantenimiento cada", 15, W8, modifier = Modifier.weight(1f))
                    Stepper(weeks, "semanas", { weeks = (weeks - 1).coerceAtLeast(1) }, { weeks = (weeks + 1).coerceAtMost(12) }, text = "$weeks sem.", size = 16, minWidth = 62)
                }
            }
        }
        FooterBar {
            BigButton(
                text = if (saved) "Clienta guardada" else "Guardar clienta",
                onClick = {
                    if (!saved && name.isNotBlank()) {
                        val design = recommend(eyes)
                        val digits = phone.trim()
                        val client = Client(
                            id = newId(), name = name.trim(), phone = if (digits.isEmpty()) "" else "+506 $digits",
                            eyes = eyes.sorted(), design = design, style = "Híbridas", curve = "C", thickness = "0.10 mm",
                            map = PRESETS[design], weeks = weeks, allergy = "", createdOn = app.today,
                        )
                        app.update { it.copy(clients = it.clients + client) }
                        saved = true
                        app.toast("${client.name} agregada")
                        app.scope?.launch {
                            delay(800)
                            app.closeSheet()
                        }
                    }
                },
                enabled = name.isNotBlank(),
                icon = if (saved) Ic.Check else null,
                bg = if (saved) C.TealDark else C.Teal,
            )
        }
    }
}
