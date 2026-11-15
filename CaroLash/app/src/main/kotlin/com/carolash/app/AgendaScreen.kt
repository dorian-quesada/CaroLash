package com.carolash.app

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth

private val VIEWS = listOf("Día", "Semana", "Mes")

private fun apptChip(a: Appointment, today: LocalDate): Triple<String, Color, Color> = when {
    a.status == Status.DONE -> Triple("Hecha", C.Teal50, C.TealText)
    a.date.isBefore(today) -> Triple("Sin marcar", C.DangerBg, C.Danger)
    a.status == Status.CONFIRMED -> Triple("Confirmada", C.Teal100, C.TealText)
    else -> Triple("Pendiente", C.Amber100, C.Amber900)
}

@Composable
fun AgendaScreen(app: App, focus: LocalDate?) {
    val d = app.data
    val today = app.today
    var view by remember { mutableStateOf(2) }
    var sel by remember { mutableStateOf(focus ?: today) }
    var dir by remember { mutableStateOf(1) }

    fun move(step: Int) {
        dir = step
        sel = when (view) {
            2 -> sel.plusMonths(step.toLong())
            1 -> sel.plusDays(7L * step)
            else -> sel.plusDays(step.toLong())
        }
    }

    val sugg = suggestions(d, today)
    val title = when (view) {
        2 -> cap(MONTHS[sel.monthValue - 1]) + " " + sel.year
        1 -> {
            val ws = weekStart(sel)
            val we = ws.plusDays(6)
            if (ws.monthValue == we.monthValue) "${ws.dayOfMonth}–${we.dayOfMonth} ${MONTHS[ws.monthValue - 1].take(3)}"
            else "${ws.dayOfMonth} ${MONTHS[ws.monthValue - 1].take(3)} – ${we.dayOfMonth} ${MONTHS[we.monthValue - 1].take(3)}"
        }
        else -> "${sel.dayOfMonth} de ${MONTHS[sel.monthValue - 1]}"
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(top = 18.dp, bottom = navPad() + 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(Modifier.fillMaxWidth().height(44.dp).rise(0), verticalAlignment = Alignment.CenterVertically) {
            Txt("Agenda", 28, W8, modifier = Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.offset(x = 10.dp)) {
                IconBtn(Ic.Back, "Anterior", { move(-1) })
                AnimatedContent(
                    targetState = title,
                    transitionSpec = { fadeIn(tween(260)) togetherWith fadeOut(tween(120)) },
                    label = "agendaTitle",
                ) { t -> Txt(t, 15, W8, align = TextAlign.Center, modifier = Modifier.width(120.dp)) }
                IconBtn(Ic.Next, "Siguiente", { move(1) })
            }
        }

        // Selector Día / Semana / Mes con píldora deslizante
        BoxWithConstraints(Modifier.fillMaxWidth().height(44.dp).rise(1).background(C.Seg, RoundedCornerShape(14.dp)).padding(4.dp)) {
            val segW = maxWidth / 3
            val x by animateDpAsState(segW * view, spring(dampingRatio = 0.75f, stiffness = 420f), label = "seg")
            Box(Modifier.offset(x = x).size(width = segW, height = 36.dp).background(Color.White, RoundedCornerShape(11.dp)))
            Row(Modifier.fillMaxSize()) {
                VIEWS.forEachIndexed { i, n ->
                    Box(
                        Modifier.weight(1f).fillMaxHeight().bounceClick(onClick = { view = i }),
                        contentAlignment = Alignment.Center,
                    ) { Txt(n, 15, if (view == i) W8 else W7, if (view == i) C.Ink else C.Muted) }
                }
            }
        }

        Column(Modifier.rise(2).animateContentSize(tween(420, easing = EaseOut))) {
            AnimatedContent(
                targetState = view,
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(160)) using SizeTransform(clip = true) },
                label = "calendarView",
            ) { v ->
                when (v) {
                    2 -> MonthCard(app, sel, dir, sugg) { sel = it }
                    1 -> WeekStrip(app, sel, sugg) { sel = it }
                    else -> Spacer(Modifier.height(0.dp))
                }
            }
            if (view != 0) {
                Row(Modifier.padding(top = 10.dp, start = 2.dp), horizontalArrangement = Arrangement.spacedBy(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(8.dp).background(C.Teal, CircleShape))
                        Txt("Cita agendada", 13, W6, C.Muted, Modifier.padding(start = 6.dp))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(8.dp).border(1.5.dp, C.Teal, CircleShape))
                        Txt("Mantenimiento sugerido", 13, W6, C.Muted, Modifier.padding(start = 6.dp))
                    }
                }
            }
        }

        AnimatedContent(
            targetState = sel,
            transitionSpec = {
                val fwd = targetState.isAfter(initialState)
                (fadeIn(tween(320)) + slideInHorizontally(tween(380, easing = EaseOut)) { if (fwd) it / 8 else -it / 8 }) togetherWith
                    (fadeOut(tween(140)) + slideOutHorizontally(tween(200)) { if (fwd) -it / 10 else it / 10 })
            },
            label = "dayList",
        ) { day -> DayList(app, day, sugg) }
    }
}

// ------------------------------------------------------------------ Mes

@Composable
private fun MonthCard(app: App, sel: LocalDate, dir: Int, sugg: List<Suggestion>, onPick: (LocalDate) -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 6.dp)) {
            Row(Modifier.fillMaxWidth().height(22.dp), verticalAlignment = Alignment.CenterVertically) {
                for (l in DOWS_SHORT) Txt(l, 12, W8, C.Muted, Modifier.weight(1f), align = TextAlign.Center)
            }
            AnimatedContent(
                targetState = YearMonth.from(sel),
                transitionSpec = {
                    val fwd = targetState.isAfter(initialState)
                    (fadeIn(tween(320)) + slideInHorizontally(tween(420, easing = EaseOut)) { if (fwd) it / 3 else -it / 3 }) togetherWith
                        (fadeOut(tween(200)) + slideOutHorizontally(tween(420, easing = EaseOut)) { if (fwd) -it / 3 else it / 3 })
                },
                label = "month",
            ) { ym ->
                val first = ym.atDay(1)
                val gridStart = weekStart(first)
                Column {
                    for (r in 0..5) {
                        Row(Modifier.fillMaxWidth()) {
                            for (c in 0..6) {
                                val day = gridStart.plusDays((r * 7 + c).toLong())
                                Box(Modifier.weight(1f)) {
                                    DayCell(app, day, day.monthValue == ym.monthValue, day == sel, sugg, onPick)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(app: App, day: LocalDate, inMonth: Boolean, selected: Boolean, sugg: List<Suggestion>, onPick: (LocalDate) -> Unit) {
    val booked = app.data.apptsOn(day).size
    val ring = sugg.count { it.date == day }
    val isToday = day == app.today
    val bg by animateColorAsState(if (selected) C.Teal else Color.Transparent, tween(260), label = "cellBg")
    val fg = when {
        selected -> Color.White
        !inMonth -> C.Input
        else -> C.Ink
    }
    val shape = RoundedCornerShape(12.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(1.dp)
            .bounceClick(label = dayName(day), onClick = { onPick(day) })
            .background(bg, shape)
            .then(if (isToday && !selected) Modifier.border(1.5.dp, C.Teal, shape) else Modifier),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Txt(day.dayOfMonth.toString(), 15, W7, fg)
        Row(Modifier.height(8.dp).padding(top = 2.dp), horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
            val dotFill = if (selected) Color.White else C.Teal
            for (i in 0 until minOf(booked, 3)) Box(Modifier.size(5.dp).background(dotFill, CircleShape))
            if (ring > 0 && booked < 3) Box(Modifier.size(5.dp).border(1.3.dp, dotFill, CircleShape))
        }
    }
}

// ------------------------------------------------------------------ Semana

@Composable
private fun WeekStrip(app: App, sel: LocalDate, sugg: List<Suggestion>, onPick: (LocalDate) -> Unit) {
    val ws = weekStart(sel)
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            for (i in 0..6) {
                val day = ws.plusDays(i.toLong())
                val selected = day == sel
                val bg by animateColorAsState(if (selected) C.Teal else Color.Transparent, tween(260), label = "weekBg")
                val booked = app.data.apptsOn(day).size
                val ring = sugg.count { it.date == day }
                val shape = RoundedCornerShape(14.dp)
                Column(
                    Modifier
                        .weight(1f)
                        .height(72.dp)
                        .bounceClick(label = dayName(day), onClick = { onPick(day) })
                        .background(bg, shape)
                        .then(if (day == app.today && !selected) Modifier.border(1.5.dp, C.Teal, shape) else Modifier),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Txt(DOWS_SHORT[i], 12, W8, if (selected) Color.White else C.Muted)
                    Txt(day.dayOfMonth.toString(), 18, W8, if (selected) Color.White else C.Ink, Modifier.padding(top = 2.dp))
                    Row(Modifier.height(8.dp).padding(top = 3.dp), horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
                        val dotFill = if (selected) Color.White else C.Teal
                        for (k in 0 until minOf(booked, 3)) Box(Modifier.size(5.dp).background(dotFill, CircleShape))
                        if (ring > 0 && booked < 3) Box(Modifier.size(5.dp).border(1.3.dp, dotFill, CircleShape))
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------ Lista del día

@Composable
private fun DayList(app: App, day: LocalDate, sugg: List<Suggestion>) {
    val d = app.data
    val today = app.today
    val appts = d.apptsOn(day)
    val sg = sugg.filter { it.date == day }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Txt(cap(dayName(day)), 18, W8, modifier = Modifier.weight(1f, fill = false))
            if (day == today) {
                Spacer(Modifier.width(8.dp))
                Pill("Hoy", C.Teal100, C.TealText)
            }
        }
        for (a in appts) {
            val c = d.client(a.clientId)
            val (label, bg, fg) = apptChip(a, today)
            Row(
                Modifier
                    .fillMaxWidth()
                    .bounceClick(label = "Ver cita de ${c?.name ?: ""}", onClick = { app.openSheet(CitaSheet(a.id)) })
                    .background(Color.White, RoundedCornerShape(14.dp))
                    .border(1.dp, C.Border, RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.width(52.dp)) {
                    Txt(timeOnly(a.startMin), 15, W8)
                    Txt(ampm(a.startMin), 12, W6, C.Muted)
                }
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Txt(c?.name ?: "Clienta", 15, W8, modifier = Modifier.weight(1f, fill = false), maxLines = 1)
                        Spacer(Modifier.width(8.dp))
                        Pill(label, bg, fg)
                    }
                    Txt("${a.service} · ${money(a.price)}", 13, W6, C.Muted, maxLines = 1)
                }
            }
        }
        for (s in sg) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                    .border(1.5.dp, C.Teal.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
                    .padding(start = 12.dp, top = 8.dp, bottom = 8.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Txt(s.client.name, 15, W8, maxLines = 1)
                    Txt("Sugerido · cada ${plural(s.client.weeks, "semana", "semanas")}", 13, W6, C.Muted)
                }
                SoftButton("Agendar", { app.openSheet(ReservarSheet(s.client.id, if (day.isBefore(today)) today else day, null)) }, bg = C.Teal, fg = Color.White)
            }
        }
        if (appts.isEmpty() && sg.isEmpty()) {
            Column(Modifier.fillMaxWidth().padding(vertical = 18.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Ico(Ic.CalendarDay, C.Soft, 44.dp, 1.6f)
                Txt(if (isWorkDay(d, day)) "Día libre" else "No trabajas este día", 16, W8, C.Ink2)
                Txt("No hay citas para este día", 14, W4, C.Muted)
                if (d.clients.isNotEmpty() && !day.isBefore(today)) {
                    Spacer(Modifier.height(4.dp))
                    SoftButton("Agendar una cita", { app.openSheet(ReservarSheet(null, day, null)) }, icon = Ic.Plus)
                }
            }
        }
    }
}

// ------------------------------------------------------------------ Hojas: contenedor

@Composable
fun SheetPanel(fraction: Float?, content: @Composable ColumnScope.() -> Unit) {
    var m = Modifier.fillMaxWidth()
    if (fraction != null) m = m.fillMaxHeight(fraction)
    Column(
        m
            .clip(RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp))
            .background(Color.White)
            .swallowTaps()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)),
    ) {
        Box(Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 6.dp), contentAlignment = Alignment.Center) {
            Box(Modifier.size(width = 40.dp, height = 4.dp).background(C.Input, RoundedCornerShape(2.dp)))
        }
        content()
    }
}

// ------------------------------------------------------------------ Hoja: nueva cita

@Composable
fun ReservarContent(app: App, s: ReservarSheet) {
    val d = app.data
    val today = app.today
    val editing = s.editId?.let { id -> d.appointments.firstOrNull { it.id == id } }
    var clientId by remember(s) { mutableStateOf(editing?.clientId ?: s.clientId) }
    val services = d.services
    var serviceName by remember(s) {
        mutableStateOf(
            editing?.service ?: run {
                val maint = services.firstOrNull { it.name.contains("mantenimiento", ignoreCase = true) }
                val c = s.clientId?.let { d.client(it) }
                if (c != null && visits(d, c) > 0 && maint != null) maint.name else services.firstOrNull()?.name ?: ""
            },
        )
    }
    var day by remember(s) {
        mutableStateOf(
            run {
                var start = s.date ?: editing?.date ?: today
                if (start.isBefore(today)) start = today
                var guard = 0
                while (!isWorkDay(d, start) && guard < 7) {
                    start = start.plusDays(1)
                    guard++
                }
                start
            },
        )
    }
    var slot by remember(s) { mutableStateOf(editing?.startMin) }

    val service = services.firstOrNull { it.name == serviceName }
    val minutes = service?.minutes ?: editing?.minutes ?: 60
    val nowMin = LocalTime.now().let { it.hour * 60 + it.minute }
    val slots = slotsFor(d, day, minutes, s.editId, nowMin)
    val chosen = slot
    val valid = chosen != null && clientId != null && slots.any { it.startMin == chosen && it.free } && (service != null || editing != null)

    SheetPanel(0.9f) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            Txt(if (editing != null) "Mover cita" else "Nueva cita", 22, W8, modifier = Modifier.weight(1f))
            IconBtn(Ic.Close, "Cerrar", { app.closeSheet() }, modifier = Modifier.offset(x = 10.dp))
        }
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (d.clients.isEmpty()) {
                Txt("Primero agrega una clienta para poder agendarle una cita.", 15, W6, C.Muted, lh = 21)
                SoftButton("Agregar clienta", { app.openSheet(NuevaClientaSheet) }, icon = Ic.Plus)
            } else {
                Column {
                    Txt("Clienta", 13, W7, C.Muted, Modifier.padding(bottom = 8.dp))
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (c in d.clients) Chip(c.name, clientId == c.id, { if (editing == null) clientId = c.id })
                    }
                }
                Column {
                    Txt("Servicio", 13, W7, C.Muted, Modifier.padding(bottom = 8.dp))
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (sv in services) Chip("${sv.name} · ${money(sv.price)}", serviceName == sv.name, { serviceName = sv.name; slot = null })
                    }
                }
                Column {
                    Txt("Día", 13, W7, C.Muted, Modifier.padding(bottom = 8.dp))
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (i in 0..27) {
                            val dd = today.plusDays(i.toLong())
                            DayTile(dd, dd == day, isWorkDay(d, dd)) { day = dd; slot = null }
                        }
                    }
                }
                Column {
                    Txt("Hora · dura ${durationText(minutes)}", 13, W7, C.Muted, Modifier.padding(bottom = 8.dp))
                    if (slots.isEmpty()) Txt("Este servicio no cabe en tu horario.", 14, W6, C.Muted)
                    for (row in slots.chunked(4)) {
                        Row(Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            for (sl in row) {
                                Box(Modifier.weight(1f)) { SlotChip(sl, slot == sl.startMin) { slot = sl.startMin } }
                            }
                            for (k in 0 until 4 - row.size) Spacer(Modifier.weight(1f))
                        }
                    }
                    if (!isWorkDay(d, day)) Txt("Ese día no está en tu horario de trabajo.", 13, W6, C.Danger)
                    Txt("Los horarios en gris chocan con otra cita, el descanso o el almuerzo.", 12, W4, C.Muted, lh = 17)
                }
            }
        }
        FooterBar {
            val summary = if (chosen != null) "${cap(dayName(day))} · ${timeText(chosen)} – ${timeText(chosen + minutes)}" else "Elige un horario disponible"
            Txt(summary, 14, W7, C.Ink2, Modifier.padding(bottom = 8.dp).fillMaxWidth(), align = TextAlign.Center)
            BigButton(if (editing != null) "Guardar cambio" else "Agendar cita", {
                val cid = clientId
                val st = chosen
                if (cid != null && st != null) {
                    if (editing != null) {
                        app.update { dd ->
                            dd.copy(appointments = dd.appointments.map {
                                if (it.id == editing.id) it.copy(date = day, startMin = st, service = serviceName, minutes = minutes, price = service?.price ?: it.price, reminded = false) else it
                            })
                        }
                        app.toast("Cita movida al ${dayName(day)}")
                    } else if (service != null) {
                        app.update { dd ->
                            dd.copy(appointments = dd.appointments + Appointment(newId(), cid, service.name, service.price, day, st, service.minutes, Status.PENDING, false))
                        }
                        app.toast("Cita agendada: ${dayName(day)}, ${timeText(st)}")
                    }
                    app.closeSheet()
                }
            }, enabled = valid && d.clients.isNotEmpty())
        }
    }
}

@Composable
private fun DayTile(day: LocalDate, selected: Boolean, work: Boolean, onClick: () -> Unit) {
    val bg by animateColorAsState(if (selected) C.Teal else Color.White, tween(240), label = "tileBg")
    val shape = RoundedCornerShape(14.dp)
    Column(
        Modifier
            .size(width = 56.dp, height = 70.dp)
            .bounceClick(enabled = work, onClick = onClick)
            .background(bg, shape)
            .border(1.5.dp, if (selected) C.Teal else C.Input, shape),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        val fg = if (selected) Color.White else if (work) C.Ink else C.Input
        Txt(DOWS_SHORT[day.dayOfWeek.value - 1], 12, W8, if (selected) Color.White else if (work) C.Muted else C.Input)
        Txt(day.dayOfMonth.toString(), 19, W8, fg)
        Txt(MONTHS[day.monthValue - 1].take(3), 11, W6, if (selected) Color.White else if (work) C.Muted else C.Input)
    }
}

@Composable
private fun SlotChip(sl: Slot, selected: Boolean, onClick: () -> Unit) {
    val bg by animateColorAsState(if (selected) C.Teal else if (sl.free) Color.White else C.Bg, tween(220), label = "slotBg")
    val shape = RoundedCornerShape(12.dp)
    Box(
        Modifier
            .fillMaxWidth()
            .height(44.dp)
            .bounceClick(enabled = sl.free, onClick = onClick)
            .background(bg, shape)
            .border(1.5.dp, if (selected) C.Teal else if (sl.free) C.Input else C.Border, shape),
        contentAlignment = Alignment.Center,
    ) {
        Txt(timeOnly(sl.startMin) + " " + ampm(sl.startMin).replace(" ", "").replace(".", ""), 13, if (selected) W8 else W7,
            if (selected) Color.White else if (sl.free) C.Ink else C.Input)
    }
}

// ------------------------------------------------------------------ Hoja: detalle de la cita

@Composable
fun CitaContent(app: App, s: CitaSheet) {
    val d = app.data
    val a = d.appointments.firstOrNull { it.id == s.apptId }
    val c = a?.let { d.client(it.clientId) }
    var confirmCancel by remember(s) { mutableStateOf(false) }
    SheetPanel(null) {
        if (a == null || c == null) {
            Txt("Esta cita ya no existe.", 15, W6, C.Muted, Modifier.padding(20.dp))
            return@SheetPanel
        }
        val today = app.today
        val (label, bg, fg) = apptChip(a, today)
        Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(c.name, 48)
                Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                    Txt(c.name, 19, W8, maxLines = 1)
                    Txt(if (c.phone.isBlank()) "Sin número" else c.phone, 14, W6, C.Muted)
                }
                Pill(label, bg, fg)
            }
            Row(
                Modifier.fillMaxWidth().background(C.Bg, RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Ico(Ic.Calendar, C.TealText, 20.dp)
                Column(Modifier.padding(start = 10.dp)) {
                    Txt("${cap(dayName(a.date))} · ${timeText(a.startMin)}", 15, W7)
                    Txt("${a.service} · ${money(a.price)} · ${durationText(a.minutes)}", 13, W6, C.Muted)
                }
            }
            if (isOpen(a) && !a.date.isAfter(today)) {
                BigButton("Marcar como hecha", {
                    app.closeSheet()
                    completeAppt(app, a)
                }, height = 52, icon = Ic.Check, size = 17)
            }
            if (a.status == Status.PENDING) {
                SoftButton("Confirmar cita", {
                    app.update { dd -> dd.copy(appointments = dd.appointments.map { if (it.id == a.id) it.copy(status = Status.CONFIRMED) else it }) }
                    app.toast("Cita confirmada")
                    app.closeSheet()
                }, Modifier.fillMaxWidth(), icon = Ic.Check)
            }
            if (isOpen(a)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SoftButton("Recordar", { app.closeSheet(); app.push(RecordatorioS(a.id)) }, Modifier.weight(1f), icon = Ic.Chat)
                    SoftButton("Mover", { app.openSheet(ReservarSheet(c.id, a.date, a.id)) }, Modifier.weight(1f), icon = Ic.Clock)
                }
            }
            SoftButton("Ver ficha de ${firstName(c.name)}", { app.closeSheet(); app.push(FichaS(c.id)) }, Modifier.fillMaxWidth(), icon = Ic.Eye)
            if (isOpen(a)) {
                SoftButton(
                    if (confirmCancel) "Toca otra vez para cancelar" else "Cancelar cita",
                    {
                        if (confirmCancel) {
                            app.update { dd -> dd.copy(appointments = dd.appointments.map { if (it.id == a.id) it.copy(status = Status.CANCELED) else it }) }
                            app.toast("Cita cancelada")
                            app.closeSheet()
                        } else confirmCancel = true
                    },
                    Modifier.fillMaxWidth(), icon = Ic.Trash, bg = C.DangerBg, fg = C.Danger,
                )
            }
        }
    }
}
