package com.carolash.app

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private val STYLES = listOf("Clásicas", "Híbridas", "Volumen ruso")
private val CURVES = listOf("J", "B", "C", "CC", "D", "L")
private val THICKS = listOf("0.05 mm", "0.07 mm", "0.10 mm", "0.12 mm", "0.15 mm", "0.18 mm", "0.20 mm")

private fun AppData.editClient(id: String, f: (Client) -> Client): AppData =
    copy(clients = clients.map { if (it.id == id) f(it) else it })

private fun nextIn(list: List<String>, cur: String): String {
    val i = list.indexOf(cur)
    return list[(i + 1) % list.size]
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FichaScreen(app: App, clientId: String) {
    val d = app.data
    val c = d.client(clientId)
    if (c == null) {
        LaunchedEffect(Unit) { app.pop() }
        return
    }
    val today = app.today
    val ctx = LocalContext.current
    var tab by remember { mutableStateOf(0) }
    var editEyes by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    val rec = recommend(c.eyes)
    fun edit(f: (Client) -> Client) = app.update { it.editClient(c.id, f) }

    Column(Modifier.fillMaxSize()) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            TopBar("Ficha de clienta", { app.pop() })
            Row(Modifier.fillMaxWidth().rise(0), verticalAlignment = Alignment.CenterVertically) {
                Avatar(c.name, 56)
                Column(Modifier.padding(start = 14.dp)) {
                    Txt(c.name, 22, W8, maxLines = 1)
                    Txt(if (c.phone.isBlank()) "Sin número" else c.phone, 14, W6, C.Muted)
                }
            }
            AnimatedVisibility(
                visible = c.allergy.isNotBlank(),
                enter = fadeIn(tween(300)) + expandVertically(tween(400, easing = EaseOut)),
                exit = fadeOut(tween(200)) + shrinkVertically(tween(300)),
            ) {
                Row(
                    Modifier.fillMaxWidth().background(C.DangerBg, RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Ico(Ic.Warning, C.Danger, 20.dp, 2f)
                    Txt(c.allergy, 14, W8, C.Danger, Modifier.padding(start = 10.dp))
                }
            }

            // Pestañas internas con píldora deslizante
            BoxWithConstraints(Modifier.fillMaxWidth().height(44.dp).rise(1).background(C.Seg, RoundedCornerShape(14.dp)).padding(4.dp)) {
                val segW = maxWidth / 2
                val x by animateDpAsState(segW * tab, spring(dampingRatio = 0.75f, stiffness = 420f), label = "fichaSeg")
                Box(Modifier.offset(x = x).size(width = segW, height = 36.dp).background(Color.White, RoundedCornerShape(11.dp)))
                Row(Modifier.fillMaxSize()) {
                    listOf("Pestañas", "Cita y datos").forEachIndexed { i, n ->
                        Box(Modifier.weight(1f).fillMaxHeight().bounceClick(onClick = { tab = i }), contentAlignment = Alignment.Center) {
                            Txt(n, 15, if (tab == i) W8 else W7, if (tab == i) C.Ink else C.Muted)
                        }
                    }
                }
            }

            AnimatedContent(
                targetState = tab,
                transitionSpec = {
                    (fadeIn(tween(320)) + slideInVertically(tween(380, easing = EaseOut)) { it / 14 }) togetherWith
                        (fadeOut(tween(140)) + slideOutVertically(tween(200)) { -it / 20 })
                },
                label = "fichaTab",
            ) { t ->
                if (t == 0) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Forma de ojos
                        Card(Modifier.fillMaxWidth().animateContentSize(tween(380, easing = EaseOut))) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Txt("Forma de ojos", 12, W7, C.Muted)
                                        Txt(if (c.eyes.isEmpty()) "Sin definir" else c.eyes.joinToString(", ") { EYES[it] }, 15, W8, lh = 20)
                                    }
                                    SoftButton(if (editEyes) "Listo" else "Editar", { editEyes = !editEyes })
                                }
                                if (editEyes) {
                                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        EYES.forEachIndexed { i, n ->
                                            Chip(n, i in c.eyes, {
                                                edit { cl -> cl.copy(eyes = (if (i in cl.eyes) cl.eyes - i else cl.eyes + i).sorted()) }
                                            })
                                        }
                                    }
                                }
                            }
                        }
                        // Diseño
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Txt("Diseño", 12, W7, C.Muted)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    DESIGNS.forEachIndexed { i, n ->
                                        Box(Modifier.weight(1f)) {
                                            Chip(n, c.design == i, { edit { cl -> cl.copy(design = i, map = PRESETS[i]) } }, Modifier.fillMaxWidth())
                                            if (c.eyes.isNotEmpty() && rec == i) {
                                                Box(
                                                    Modifier.align(Alignment.TopEnd).offset(x = 4.dp, y = (-4).dp).size(18.dp).background(C.Amber, androidx.compose.foundation.shape.CircleShape),
                                                    contentAlignment = Alignment.Center,
                                                ) { Ico(Ic.Star, C.Amber950, 11.dp, fill = true) }
                                            }
                                        }
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        if (c.eyes.isEmpty()) {
                                            Txt("Elige la forma de sus ojos para ver la recomendación.", 13, W6, C.Muted, lh = 18)
                                        } else if (rec == c.design) {
                                            Txt("${DESIGNS[rec]} recomendada para sus ojos", 13, W8, C.TealText)
                                            Txt(c.eyes.joinToString(", ") { EYES[it] }, 12, W4, C.Muted)
                                        } else {
                                            Txt("Para sus ojos va mejor ${DESIGNS[rec].lowercase()}", 13, W8, C.Amber900)
                                            Txt("Toca ${DESIGNS[rec]} arriba para aplicarla.", 12, W4, C.Muted)
                                        }
                                    }
                                    Txt("Ver guía", 14, W8, C.Teal, Modifier.bounceClick(onClick = { app.push(GuiaS) }).padding(vertical = 12.dp, horizontal = 4.dp))
                                }
                            }
                        }
                        // Mapping
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Txt("Mapping (mm)", 12, W7, C.Muted)
                                        Txt("Toca un cuadrito para subirlo 1 mm", 12, W4, C.Muted)
                                    }
                                    SquareBtn(Ic.Minus, "Quitar un cuadrito", { if (c.map.size > 3) edit { cl -> cl.copy(map = cl.map.dropLast(1)) } })
                                    Spacer(Modifier.width(6.dp))
                                    SquareBtn(Ic.Plus, "Agregar un cuadrito", { if (c.map.size < 8) edit { cl -> cl.copy(map = cl.map + cl.map.last()) } })
                                }
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    val maxV = c.map.maxOrNull() ?: 0
                                    c.map.forEachIndexed { i, v ->
                                        val top = v == maxV
                                        val bg by animateColorAsState(if (top) C.Teal else C.Teal50, tween(300), label = "mapBg")
                                        Box(
                                            Modifier
                                                .weight(1f)
                                                .height(48.dp)
                                                .bounceClick(label = "Zona ${i + 1}, $v milímetros", onClick = {
                                                    edit { cl -> cl.copy(map = cl.map.mapIndexed { j, x -> if (j == i) (if (x >= 15) 6 else x + 1) else x }) }
                                                })
                                                .background(bg, RoundedCornerShape(12.dp))
                                                .border(1.dp, if (top) C.Teal else C.Input, RoundedCornerShape(12.dp)),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            AnimatedContent(
                                                targetState = v,
                                                transitionSpec = { fadeIn(tween(160)) togetherWith fadeOut(tween(100)) },
                                                label = "mapNum",
                                            ) { n -> Txt(n.toString(), 17, W8, if (top) Color.White else C.TealText) }
                                        }
                                    }
                                }
                                Row(Modifier.fillMaxWidth()) {
                                    Txt("Esquina interna", 12, W6, C.Muted, Modifier.weight(1f))
                                    Txt("Esquina externa", 12, W6, C.Muted)
                                }
                            }
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CycleCell("Estilo", c.style.ifBlank { "—" }, Modifier.weight(1f)) {
                                edit { cl -> cl.copy(style = nextIn(STYLES, cl.style)) }
                            }
                            CycleCell("Curvatura", c.curve.ifBlank { "—" }, Modifier.weight(1f)) {
                                edit { cl -> cl.copy(curve = nextIn(CURVES, cl.curve)) }
                            }
                            CycleCell("Grosor", c.thickness.ifBlank { "—" }, Modifier.weight(1f)) {
                                edit { cl -> cl.copy(thickness = nextIn(THICKS, cl.thickness)) }
                            }
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        val next = nextAppt(d, c, today)
                        val sg = suggestions(d, today).firstOrNull { it.client.id == c.id }
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Txt("Próxima cita", 12, W7, C.Muted)
                                if (next != null) {
                                    Txt("${cap(dayName(next.date))} · ${timeText(next.startMin)}", 16, W8)
                                    Txt("${next.service} · ${money(next.price)}", 13, W6, C.Muted)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        SoftButton("Recordar", { app.push(RecordatorioS(next.id)) }, Modifier.weight(1f), icon = Ic.Chat)
                                        SoftButton("Ver cita", { app.openSheet(CitaSheet(next.id)) }, Modifier.weight(1f), icon = Ic.Calendar)
                                    }
                                } else if (sg != null) {
                                    Txt("Mantenimiento sugerido el ${dayName(sg.date)}", 15, W8, lh = 20)
                                    Txt("Cada ${plural(c.weeks, "semana", "semanas")} desde su última visita", 13, W6, C.Muted)
                                } else {
                                    Txt("Sin citas por ahora", 15, W7, C.Muted)
                                }
                            }
                        }
                        Card(Modifier.fillMaxWidth()) {
                            Row(Modifier.padding(start = 14.dp, end = 8.dp, top = 6.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Txt("Mantenimiento cada", 15, W8, modifier = Modifier.weight(1f))
                                Stepper(c.weeks, "semanas", { edit { cl -> cl.copy(weeks = (cl.weeks - 1).coerceAtLeast(1)) } },
                                    { edit { cl -> cl.copy(weeks = (cl.weeks + 1).coerceAtMost(12)) } }, text = "${c.weeks} sem.", size = 16, minWidth = 62)
                            }
                        }
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Txt("Datos", 12, W7, C.Muted)
                                Field(c.name, { v -> edit { cl -> cl.copy(name = v.take(40)) } }, Modifier.fillMaxWidth(), hint = "Nombre", height = 46)
                                Field(c.phone, { v -> edit { cl -> cl.copy(phone = v.filter { ch -> ch.isDigit() || ch == ' ' || ch == '+' }.take(16)) } },
                                    Modifier.fillMaxWidth(), hint = "+506 8000 1234", height = 46, keyboard = KeyboardType.Phone)
                                Field(c.allergy, { v -> edit { cl -> cl.copy(allergy = v.take(60)) } }, Modifier.fillMaxWidth(), hint = "Alergias o sensibilidad (opcional)", height = 46, w = W6)
                            }
                        }
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Txt("Historial", 12, W7, C.Muted, Modifier.weight(1f))
                                    Txt(plural(visits(d, c), "visita", "visitas"), 13, W8, C.TealText)
                                }
                                val hist = d.appointments.filter { it.clientId == c.id && it.status == Status.DONE }.sortedByDescending { it.date }.take(5)
                                if (hist.isEmpty()) Txt("Todavía no hay visitas registradas.", 14, W4, C.Muted)
                                for (h in hist) {
                                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                        Txt(shortDate(h.date), 14, W8, modifier = Modifier.width(64.dp))
                                        Txt(h.service, 14, W6, C.Ink2, Modifier.weight(1f), maxLines = 1)
                                        Txt(money(h.price), 14, W7, C.Muted)
                                    }
                                }
                            }
                        }
                        SoftButton(
                            if (confirmDelete) "Toca otra vez para eliminar" else "Eliminar clienta",
                            {
                                if (confirmDelete) {
                                    app.update { dd -> dd.copy(clients = dd.clients.filter { it.id != c.id }, appointments = dd.appointments.filter { it.clientId != c.id }) }
                                    app.toast("Clienta eliminada")
                                    app.pop()
                                } else confirmDelete = true
                            },
                            Modifier.fillMaxWidth(), icon = Ic.Trash, bg = C.DangerBg, fg = C.Danger,
                        )
                    }
                }
            }
        }
        FooterBar {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(56.dp).bounceClick(label = "Abrir WhatsApp", onClick = {
                        val digits = whatsappDigits(c.phone)
                        if (digits.isEmpty()) app.toast("Esta clienta no tiene número") else {
                            try {
                                ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$digits")))
                            } catch (e: ActivityNotFoundException) {
                                app.toast("No se pudo abrir WhatsApp")
                            }
                        }
                    }).background(C.Teal50, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center,
                ) { Ico(Ic.Chat, C.TealText, 24.dp, 2f) }
                BigButton("Agendar cita", { app.openSheet(ReservarSheet(c.id, null, null)) }, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun CycleCell(label: String, value: String, modifier: Modifier, onClick: () -> Unit) {
    Card(modifier, onClick = onClick, label = "$label: $value. Toca para cambiar") {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp).fillMaxWidth()) {
            Txt(label, 12, W7, C.Muted)
            AnimatedContent(
                targetState = value,
                transitionSpec = { (fadeIn(tween(220)) + slideInVertically(tween(260, easing = EaseOut)) { it / 2 }) togetherWith (fadeOut(tween(100))) },
                label = "cycle",
            ) { v -> Txt(v, 15, W8, lh = 20) }
        }
    }
}

// ------------------------------------------------------------------ Guía de diseños

@Composable
fun GuiaScreen(app: App) {
    var design by remember { mutableStateOf(1) }
    Column(Modifier.fillMaxSize()) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TopBar("Guía de diseños", { app.pop() })
            BoxWithConstraints(Modifier.fillMaxWidth().height(44.dp).rise(0).background(C.Seg, RoundedCornerShape(14.dp)).padding(4.dp)) {
                val segW = maxWidth / 3
                val x by animateDpAsState(segW * design, spring(dampingRatio = 0.75f, stiffness = 420f), label = "guiaSeg")
                Box(Modifier.offset(x = x).size(width = segW, height = 36.dp).background(Color.White, RoundedCornerShape(11.dp)))
                Row(Modifier.fillMaxSize()) {
                    DESIGNS.forEachIndexed { i, n ->
                        Box(Modifier.weight(1f).fillMaxHeight().bounceClick(onClick = { design = i }), contentAlignment = Alignment.Center) {
                            Txt(n, 14, if (design == i) W8 else W7, if (design == i) C.Ink else C.Muted)
                        }
                    }
                }
            }
            Box(Modifier.rise(1)) {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val bars = BARS[design]
                        val maxV = bars.maxOrNull() ?: 1
                        Row(Modifier.fillMaxWidth().height(132.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Bottom) {
                            bars.forEachIndexed { i, v ->
                                val h by animateDpAsState((v * 8.4f).dp, tween(520, delayMillis = i * 28, easing = EaseOut), label = "bar")
                                Box(Modifier.weight(1f).height(h).background(if (v == maxV) C.Teal else Color(0xFF9FD8D4), RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)))
                            }
                        }
                        Row(Modifier.fillMaxWidth()) {
                            Txt("Esquina interna", 12, W6, C.Muted, Modifier.weight(1f))
                            Txt("Esquina externa", 12, W6, C.Muted)
                        }
                        AnimatedContent(
                            targetState = design,
                            transitionSpec = { fadeIn(tween(280)) togetherWith fadeOut(tween(120)) },
                            label = "guiaText",
                        ) { dsg ->
                            Column(Modifier.fillMaxWidth()) {
                                Txt("${DESIGNS[dsg]}: ${DESIGN_TAGLINE[dsg]}", 16, W8, C.TealText, lh = 21)
                                Txt(DESIGN_LONGEST[dsg], 13, W4, C.Muted, Modifier.padding(top = 4.dp), lh = 19)
                            }
                        }
                    }
                }
            }
            Txt("Cómo le queda a cada ojo", 16, W8, modifier = Modifier.padding(top = 4.dp).rise(2))
            for (row in EYES.indices.chunked(2)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (i in row) {
                        Box(Modifier.weight(1f)) { EyeCard(i, design) }
                    }
                }
            }
        }
    }
}

@Composable
private fun EyeCard(eye: Int, design: Int) {
    val letter = RATING[eye][design]
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Txt(EYES[eye], 15, W8)
            AnimatedContent(
                targetState = letter,
                transitionSpec = { fadeIn(tween(260)) togetherWith fadeOut(tween(120)) },
                label = "rate",
            ) { l ->
                val (b2, f2) = when (l) {
                    'I' -> Pair(C.Teal100, C.TealText)
                    'B' -> Pair(C.Seg, C.Ink2)
                    'C' -> Pair(C.Amber100, C.Amber900)
                    else -> Pair(C.DangerBg, C.Danger)
                }
                Pill(ratingLabel(l), b2, f2)
            }
        }
    }
}
