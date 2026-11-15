package com.carolash.app

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun plural(n: Int, one: String, many: String): String = if (n == 1) "$n $one" else "$n $many"

/** Número que "rueda" cuando cambia. */
@Composable
fun Rolling(value: Int, size: Int, w: androidx.compose.ui.text.font.FontWeight, color: Color, text: (Int) -> String) {
    AnimatedContent(
        targetState = value,
        transitionSpec = {
            val up = targetState > initialState
            (slideInVertically(tween(420, easing = EaseOut)) { if (up) it else -it } + fadeIn(tween(300))) togetherWith
                (slideOutVertically(tween(320, easing = EaseOut)) { if (up) -it else it } + fadeOut(tween(200)))
        },
        label = "rolling",
    ) { v -> Txt(text(v), size, w, color) }
}

/** Marca una cita como hecha y celebra si se desbloquea una insignia. */
fun completeAppt(app: App, a: Appointment) {
    val c = app.data.client(a.clientId)
    val badge = app.finishAppointment(a.id)
    app.toast("Cita de ${firstName(c?.name ?: "la clienta")} lista · +${money(a.price)}")
    if (badge != null) {
        app.scope?.launch {
            delay(650)
            app.pushFade(UnlockedS(badge))
        }
    }
}

// ------------------------------------------------------------------ Hoy

@Composable
fun HoyScreen(app: App) {
    val d = app.data
    val today = app.today
    val st = stats(d, today)
    val tomorrow = d.apptsOn(today.plusDays(1)).filter { isOpen(it) }
    val todayList = d.apptsOn(today)
    val nextOpen = todayList.firstOrNull { isOpen(it) }
    val due = if (d.settings.maintenanceAlerts) suggestions(d, today).filter { !it.date.isAfter(today) } else emptyList()

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(top = 18.dp, bottom = navPad() + 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(Modifier.fillMaxWidth().height(52.dp).rise(0), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Txt("Hola, Caro", 34, W7, C.Teal, script = true)
                Txt(cap(dayName(today)), 14, W6, C.Muted, Modifier.padding(top = 2.dp))
            }
            Image(painterResource(R.drawable.logo), "Caro Lash", Modifier.size(width = 40.dp, height = 48.dp))
        }

        WeekCard(app, st, Modifier.rise(1))

        // Por recordar
        Row(Modifier.fillMaxWidth().padding(top = 4.dp).rise(2), verticalAlignment = Alignment.Bottom) {
            Txt("Por recordar", 18, W8, modifier = Modifier.weight(1f))
            Txt("Citas de mañana", 13, W6, C.Muted)
        }
        if (tomorrow.isEmpty()) {
            Box(Modifier.rise(3)) { EmptyNote("Mañana no tienes citas por recordar.") }
        } else {
            tomorrow.forEachIndexed { i, a ->
                val c = d.client(a.clientId)
                if (c != null) Box(Modifier.rise(3 + i)) { ReminderRow(app, c, a) }
            }
        }

        // Hoy
        Row(Modifier.fillMaxWidth().padding(top = 6.dp).rise(5), verticalAlignment = Alignment.CenterVertically) {
            Txt("Hoy", 18, W8, modifier = Modifier.weight(1f))
            if (d.clients.isNotEmpty()) SoftButton("Nueva cita", { app.openSheet(ReservarSheet(null, today, null)) }, icon = Ic.Plus)
        }
        if (todayList.isEmpty()) {
            Box(Modifier.rise(6)) {
                if (d.clients.isEmpty()) {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Txt("Empieza agregando a tu primera clienta", 16, W8)
                            Txt("Con su número y la forma de sus ojos, la app te sugiere el diseño y cuándo le toca mantenimiento.", 14, W4, C.Muted, lh = 20)
                            BigButton("Agregar clienta", { app.openSheet(NuevaClientaSheet) }, height = 48, icon = Ic.Plus, size = 16)
                        }
                    }
                } else EmptyNote("Hoy no tienes citas. ¡Día libre!")
            }
        } else {
            todayList.forEachIndexed { i, a ->
                val c = d.client(a.clientId)
                Box(Modifier.rise(6 + i)) { TodayRow(app, c, a, a.id == nextOpen?.id) }
            }
        }

        // Mantenimientos pasados
        due.take(2).forEachIndexed { i, sg ->
            Box(Modifier.rise(8 + i)) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(C.Amber100, RoundedCornerShape(14.dp))
                        .border(1.5.dp, C.AmberLine.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                        .padding(start = 14.dp, top = 8.dp, bottom = 8.dp, end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Txt("${sg.client.name} · mantenimiento", 14, W8, C.Amber950, maxLines = 1)
                        Txt("Pasaron ${plural(sg.weeksSince(today), "semana", "semanas")}", 13, W6, C.Amber900)
                    }
                    SoftButton("Agendar", { app.openSheet(ReservarSheet(sg.client.id, null, null)) }, bg = C.Teal, fg = Color.White)
                }
            }
        }
        if (due.size > 2) {
            Txt("y ${due.size - 2} más en Clientas", 13, W6, C.Muted, Modifier.padding(start = 4.dp))
        }
    }
}

@Composable
fun EmptyNote(text: String) {
    Card(Modifier.fillMaxWidth()) {
        Txt(text, 14, W6, C.Muted, Modifier.padding(16.dp))
    }
}

@Composable
private fun WeekCard(app: App, st: Stats, modifier: Modifier) {
    val s = app.data.settings
    val goal = s.goalIncome
    val pct = if (goal > 0) (st.monthIncome.toFloat() / goal).coerceIn(0f, 1f) else 0f
    var go by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(350)
        go = true
    }
    val frac by animateFloatAsState(if (go) pct else 0f, tween(1200, easing = EaseOut), label = "bar")
    Column(
        modifier
            .fillMaxWidth()
            .bounceClick(label = "Ver mis logros", onClick = { app.tab(3) })
            .background(C.TealDark, RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Column(Modifier.weight(1f)) {
                Txt("Esta semana", 13, W7, C.Teal100)
                Rolling(st.weekCount, 24, W8, Color.White) { plural(it, "clienta", "clientas") }
            }
            if (st.streak >= 2) {
                Row(
                    Modifier.background(C.Amber, RoundedCornerShape(999.dp)).padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Ico(Ic.Star, C.Amber950, 14.dp, fill = true)
                    Spacer(Modifier.width(6.dp))
                    Txt("Racha de ${st.streak} semanas", 12, W8, C.Amber950)
                }
            }
        }
        Column {
            Row(Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
                Txt("Meta de ${MONTHS[app.today.monthValue - 1]}", 13, W7, Color.White, Modifier.weight(1f))
                Txt("${money(st.monthIncome)} de ${money(goal)}", 13, W7, Color.White)
            }
            Box(Modifier.fillMaxWidth().height(10.dp).background(C.TealOnDark, RoundedCornerShape(5.dp))) {
                Box(Modifier.fillMaxWidth(frac).fillMaxHeight().background(C.Amber, RoundedCornerShape(5.dp)))
            }
        }
    }
}

@Composable
private fun ReminderRow(app: App, c: Client, a: Appointment) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(start = 12.dp, end = 10.dp, top = 10.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Avatar(c.name)
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Txt(c.name, 16, W8, maxLines = 1)
                Txt("${timeText(a.startMin)} · ${a.service}", 13, W6, C.Muted, maxLines = 1)
            }
            if (a.reminded) {
                Row(
                    Modifier
                        .height(44.dp)
                        .bounceClick(label = "Recordatorio enviado a ${firstName(c.name)}. Toca para enviar otro", onClick = { app.push(RecordatorioS(a.id)) })
                        .background(C.Teal100, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Ico(Ic.Check, C.TealText, 18.dp, 2.4f)
                    Spacer(Modifier.width(6.dp))
                    Txt("Enviado", 14, W8, C.TealText)
                }
            } else {
                SoftButton("Recordar", { app.push(RecordatorioS(a.id)) }, icon = Ic.Chat, bg = C.Teal, fg = Color.White)
            }
        }
    }
}

@Composable
private fun TodayRow(app: App, c: Client?, a: Appointment, isNext: Boolean) {
    val done = a.status == Status.DONE
    val bg = if (done) C.Teal50 else Color.White
    Row(
        Modifier
            .fillMaxWidth()
            .bounceClick(label = "Ver cita de ${c?.name ?: ""}", onClick = { app.openSheet(CitaSheet(a.id)) })
            .background(bg, RoundedCornerShape(14.dp))
            .border(1.dp, if (done) C.Input else C.Border, RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.width(52.dp)) {
            Txt(timeOnly(a.startMin), 15, W8)
            Txt(ampm(a.startMin), 12, W6, C.Muted)
        }
        Column(Modifier.weight(1f)) {
            Txt(c?.name ?: "Clienta", 15, W8, maxLines = 1)
            Txt(a.service, 13, W6, C.Muted, maxLines = 1)
        }
        when {
            done -> Row(verticalAlignment = Alignment.CenterVertically) {
                Ico(Ic.Check, C.TealText, 16.dp, 2.4f)
                Spacer(Modifier.width(4.dp))
                Txt("Hecha", 13, W8, C.TealText)
            }
            isNext -> Box(
                Modifier
                    .height(40.dp)
                    .bounceClick(label = "Marcar como hecha la cita de ${c?.name ?: ""}", onClick = { completeAppt(app, a) })
                    .background(C.Amber100, RoundedCornerShape(999.dp))
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center,
            ) { Txt("Marcar hecha", 12, W8, C.Amber900) }
            else -> Txt(if (a.status == Status.CONFIRMED) "Confirmada" else "Pendiente", 13, W7, C.Muted)
        }
    }
}

// ------------------------------------------------------------------ Recordatorio por WhatsApp

@Composable
fun RecordatorioScreen(app: App, apptId: String) {
    val d = app.data
    val a = d.appointments.firstOrNull { it.id == apptId }
    val c = a?.let { d.client(it.clientId) }
    if (a == null || c == null) {
        LaunchedEffect(Unit) { app.pop() }
        return
    }
    val ctx = LocalContext.current
    var tpl by remember { mutableStateOf(if (a.date == app.today) 1 else 0) }
    var indic by remember { mutableStateOf(true) }
    var custom by remember { mutableStateOf<String?>(null) }
    var editing by remember { mutableStateOf(false) }
    val auto = reminderMessage(tpl, c, a, app.today, indic)
    val msg = custom ?: auto

    Column(Modifier.fillMaxSize()) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            TopBar("Enviar recordatorio", { app.pop() })
            Box(Modifier.rise(0)) {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Avatar(c.name, 48)
                            Column(Modifier.padding(start = 12.dp)) {
                                Txt(c.name, 18, W8)
                                Txt(if (c.phone.isBlank()) "Sin número" else c.phone, 14, W6, C.Muted)
                            }
                        }
                        Row(
                            Modifier.fillMaxWidth().background(C.Bg, RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Ico(Ic.Calendar, C.TealText, 20.dp)
                            Column(Modifier.padding(start = 10.dp)) {
                                Txt("${cap(dayName(a.date))} · ${timeText(a.startMin)}", 15, W7)
                                Txt(a.service, 13, W6, C.Muted)
                            }
                        }
                    }
                }
            }
            Column(Modifier.rise(1)) {
                Txt("Plantilla", 13, W7, C.Muted, Modifier.padding(bottom = 8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TEMPLATE_NAMES.forEachIndexed { i, n ->
                        Chip(n, tpl == i, { tpl = i; custom = null; editing = false })
                    }
                }
            }
            Column(Modifier.rise(2)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Txt("Así le llegará", 13, W7, C.Muted, Modifier.weight(1f))
                    Row(
                        Modifier.height(44.dp).bounceClick(onClick = {
                            if (editing) editing = false else {
                                if (custom == null) custom = auto
                                editing = true
                            }
                        }),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Ico(if (editing) Ic.Check else Ic.Edit, C.Teal, 16.dp, 2f)
                        Spacer(Modifier.width(6.dp))
                        Txt(if (editing) "Listo" else "Editar mensaje", 14, W8, C.Teal)
                    }
                }
                val shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 6.dp)
                if (editing) {
                    BasicTextField(
                        value = custom ?: auto,
                        onValueChange = { custom = it },
                        textStyle = TextStyle(color = C.Ink, fontSize = 15.sp, lineHeight = 22.sp, fontFamily = Nunito),
                        cursorBrush = SolidColor(C.Teal),
                        modifier = Modifier.fillMaxWidth().background(Color.White, shape).border(1.5.dp, C.Teal, shape).padding(14.dp),
                    )
                } else {
                    Box(Modifier.fillMaxWidth().background(C.Teal50, shape).border(1.dp, Color(0xFFBFE0DE), shape).padding(14.dp)) {
                        AnimatedContent(
                            targetState = msg,
                            transitionSpec = { fadeIn(tween(320)) togetherWith fadeOut(tween(160)) },
                            label = "msg",
                        ) { m -> Txt(m, 15, W4, lh = 22) }
                    }
                }
            }
            Box(Modifier.rise(3)) {
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(start = 14.dp, end = 6.dp, top = 6.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Txt("Agregar indicaciones previas", 14, W7)
                            Txt("Llegar sin maquillaje en los ojos", 13, W6, C.Muted)
                        }
                        SwitchBtn(indic, { indic = it; custom = null }, "Agregar indicaciones previas")
                    }
                }
            }
        }
        FooterBar {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BigButton("Abrir WhatsApp con el mensaje", {
                    val digits = whatsappDigits(c.phone)
                    if (digits.isEmpty()) {
                        app.toast("Esta clienta no tiene número")
                    } else {
                        try {
                            val uri = Uri.parse("https://wa.me/$digits?text=" + Uri.encode(msg))
                            ctx.startActivity(Intent(Intent.ACTION_VIEW, uri))
                        } catch (e: ActivityNotFoundException) {
                            app.toast("No se pudo abrir WhatsApp")
                        }
                    }
                }, height = 52, icon = Ic.Chat, size = 17)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SoftButton("Copiar mensaje", {
                        val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText("Mensaje", msg))
                        app.toast("Mensaje copiado")
                    }, Modifier.weight(1f))
                    SoftButton("Marcar como enviado", {
                        app.update { dd -> dd.copy(appointments = dd.appointments.map { if (it.id == a.id) it.copy(reminded = true) else it }) }
                        app.toast("Recordatorio marcado como enviado")
                        app.pop()
                    }, Modifier.weight(1f))
                }
                Txt("La app no usa internet. WhatsApp sí necesita conexión para enviar.", 12, W6, C.Muted, Modifier.fillMaxWidth(), align = TextAlign.Center, lh = 17)
            }
        }
    }
}
