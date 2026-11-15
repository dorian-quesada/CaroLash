package com.carolash.app

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import kotlin.random.Random

private fun badgeIcon(id: String): List<String> = when (id) {
    "first" -> Ic.Check
    "five" -> Ic.People
    "streak" -> Ic.Flame
    "week6" -> Ic.Bolt
    "goal" -> Ic.Target
    else -> Ic.Star
}

// ------------------------------------------------------------------ Logros

@Composable
fun LogrosScreen(app: App) {
    val d = app.data
    val today = app.today
    val st = stats(d, today)
    val goal = d.settings.goalIncome
    val pct = if (goal > 0) (st.monthIncome.toFloat() / goal).coerceIn(0f, 1f) else 0f
    var go by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(350)
        go = true
    }
    val frac by animateFloatAsState(if (go) pct else 0f, tween(1300, easing = EaseOut), label = "goal")
    val prev = today.withDayOfMonth(1).minusMonths(1)
    val bl = badges(d, st)

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(top = 18.dp, bottom = navPad() + 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(Modifier.fillMaxWidth().height(44.dp).rise(0), verticalAlignment = Alignment.CenterVertically) {
            Txt("Tus logros", 28, W8, modifier = Modifier.weight(1f))
            Txt(cap(MONTHS[today.monthValue - 1]) + " " + today.year, 14, W6, C.Muted)
        }

        Column(
            Modifier.fillMaxWidth().rise(1).background(C.TealDark, RoundedCornerShape(18.dp)).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Txt("Este mes ganaste", 13, W7, C.Teal100)
            Rolling(st.monthIncome, 40, W8, Color.White) { money(it) }
            Box(Modifier.fillMaxWidth().height(10.dp).background(C.TealOnDark, RoundedCornerShape(5.dp))) {
                Box(Modifier.fillMaxWidth(frac).fillMaxHeight().background(C.Amber, RoundedCornerShape(5.dp)))
            }
            Row(Modifier.fillMaxWidth()) {
                Txt("Meta ${money(goal)}", 13, W7, Color.White, Modifier.weight(1f))
                Txt("${if (goal > 0) st.monthIncome * 100 / goal else 0} %", 13, W8, C.Amber)
            }
            val missing = goal - st.monthIncome
            val prevText = if (st.lastMonthIncome > 0) " ${cap(MONTHS[prev.monthValue - 1])} cerró en ${money(st.lastMonthIncome)}." else ""
            Txt(
                if (goal <= 0) "Pon una meta en Ajustes para ver tu avance."
                else if (missing > 0) "Te faltan ${money(missing)}.$prevText"
                else "¡Meta cumplida! Pasaste tu meta por ${money(-missing)}.$prevText",
                14, W4, C.Teal100, lh = 20,
            )
        }

        Row(Modifier.fillMaxWidth().rise(2), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile("Esta semana", plural(st.weekCount, "clienta", "clientas"), "Tu récord: ${st.record}", Modifier.weight(1f))
            StatTile("Este mes", plural(st.monthCount, "clienta", "clientas"),
                if (st.lastMonthCount > 0) "${cap(MONTHS[prev.monthValue - 1])}: ${st.lastMonthCount}" else "Meta: ${d.settings.goalMonth}", Modifier.weight(1f))
        }

        Row(
            Modifier.fillMaxWidth().rise(3).background(C.Amber100, RoundedCornerShape(14.dp)).padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Ico(Ic.Star, C.Amber900, 22.dp, fill = true)
            Txt(
                if (st.streak > 0) "Llevas ${plural(st.streak, "semana seguida", "semanas seguidas")} con ${d.settings.goalWeek} o más clientas"
                else "Atiende ${d.settings.goalWeek} clientas esta semana para empezar una racha",
                15, W8, C.Amber950, Modifier.padding(start = 10.dp), lh = 20,
            )
        }

        Txt("Insignias", 18, W8, modifier = Modifier.padding(top = 4.dp).rise(4))
        for ((r, row) in bl.chunked(3).withIndex()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for ((k, b) in row.withIndex()) {
                    Box(Modifier.weight(1f).rise(5 + r * 3 + k)) { BadgeTile(app, b) }
                }
            }
        }
    }
}

@Composable
private fun StatTile(label: String, value: String, foot: String, modifier: Modifier) {
    Card(modifier) {
        Column(Modifier.padding(12.dp).fillMaxWidth()) {
            Txt(label, 13, W7, C.Muted)
            Txt(value, 22, W8, modifier = Modifier.padding(top = 2.dp))
            Txt(foot, 13, W6, C.Muted, Modifier.padding(top = 2.dp))
        }
    }
}

@Composable
private fun BadgeTile(app: App, b: Badge) {
    val bg by androidx.compose.animation.animateColorAsState(if (b.unlocked) C.Amber else C.Seg, tween(400), label = "badge")
    Column(
        Modifier.fillMaxWidth().bounceClick(label = b.title, onClick = { app.toast(if (b.unlocked) b.text else b.title + (b.progress?.let { " · $it" } ?: "")) }),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(64.dp).background(bg, CircleShape), contentAlignment = Alignment.Center) {
            Ico(badgeIcon(b.id), if (b.unlocked) C.Amber950 else C.Soft, 28.dp, 2f, fill = false)
        }
        Txt(b.title, 12, W7, if (b.unlocked) C.Ink else C.Muted, Modifier.padding(top = 6.dp), align = TextAlign.Center, lh = 15)
        if (b.progress != null) Txt(b.progress, 12, W6, C.Muted, align = TextAlign.Center)
    }
}

// ------------------------------------------------------------------ Logro desbloqueado

private class Piece(val x: Float, val speed: Float, val size: Float, val rot: Float, val spin: Float, val color: Color, val sway: Float)

@Composable
fun UnlockedScreen(app: App, badgeId: String) {
    val d = app.data
    val st = stats(d, app.today)
    val b = badges(d, st).firstOrNull { it.id == badgeId }
    val pop = remember { Animatable(0f) }
    val fall = remember { Animatable(0f) }
    LaunchedEffect(Unit) { pop.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = 180f)) }
    LaunchedEffect(Unit) { fall.animateTo(1f, tween(4200, easing = LinearEasing)) }
    val pieces = remember {
        val rnd = Random(7)
        val cols = listOf(C.Amber, Color.White, C.Teal100, Color(0xFFF59AA8), Color(0xFF7FE0DA))
        List(44) { Piece(rnd.nextFloat(), 0.55f + rnd.nextFloat() * 0.8f, 6f + rnd.nextFloat() * 8f, rnd.nextFloat() * 360f, (rnd.nextFloat() - 0.5f) * 720f, cols[rnd.nextInt(cols.size)], rnd.nextFloat() * 6.28f) }
    }
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(C.TealDark, C.Teal)))) {
        Canvas(Modifier.fillMaxSize()) {
            val t = fall.value
            for (p in pieces) {
                val y = -40f + (size.height + 80f) * (t * p.speed)
                if (y > size.height + 40f) continue
                val x = p.x * size.width + Math.sin((t * 6.0 + p.sway)).toFloat() * 18f
                rotate(p.rot + p.spin * t, pivot = Offset(x, y)) {
                    drawRect(p.color, topLeft = Offset(x - p.size / 2, y - p.size / 2), size = Size(p.size, p.size * 0.6f))
                }
            }
        }
        Column(
            Modifier.fillMaxSize().padding(horizontal = 28.dp).padding(top = 72.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Txt("LOGRO DESBLOQUEADO", 12, W8, Color.White, Modifier.background(Color.White.copy(alpha = 0.16f), RoundedCornerShape(999.dp)).padding(horizontal = 14.dp, vertical = 6.dp).rise(1))
            Spacer(Modifier.height(34.dp))
            Box(
                Modifier.size(170.dp).graphicsLayer { scaleX = pop.value; scaleY = pop.value },
                contentAlignment = Alignment.Center,
            ) {
                Box(Modifier.fillMaxSize().border(2.dp, C.Amber.copy(alpha = 0.35f), CircleShape))
                Box(Modifier.size(136.dp).background(C.Amber, CircleShape), contentAlignment = Alignment.Center) {
                    Ico(badgeIcon(badgeId), C.Amber950, 64.dp, 2f)
                }
            }
            Txt(b?.title ?: "¡Nuevo logro!", 28, W8, Color.White, Modifier.padding(top = 30.dp).rise(2), align = TextAlign.Center)
            Txt(b?.text ?: "", 16, W4, C.Teal100, Modifier.padding(top = 10.dp).rise(3), align = TextAlign.Center, lh = 23)
            Row(Modifier.padding(top = 22.dp).rise(4), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MiniStat("Esta semana", plural(st.weekCount, "clienta", "clientas"))
                MiniStat("Tu récord", st.record.toString())
            }
            Spacer(Modifier.weight(1f))
            Box(Modifier.rise(5)) {
                BigButton("Ver mis logros", { app.root(LogrosS, Kind.FADE) }, bg = C.Amber, fg = C.Amber950)
            }
            Spacer(Modifier.height(10.dp))
            Box(
                Modifier.fillMaxWidth().height(52.dp).rise(6).bounceClick(onClick = { app.popFade() }).border(1.5.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center,
            ) { Txt("Seguir", 17, W8, Color.White) }
        }
    }
}

@Composable
private fun MiniStat(label: String, value: String) {
    Column(Modifier.background(Color.White.copy(alpha = 0.14f), RoundedCornerShape(14.dp)).padding(horizontal = 16.dp, vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Txt(label, 12, W7, C.Teal100)
        Txt(value, 18, W8, Color.White)
    }
}

// ------------------------------------------------------------------ Ajustes

private fun daysSummary(days: Set<Int>): String {
    val s = days.sorted()
    val names = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
    if (s.size == 1) return names[s[0] - 1]
    val contiguous = s.zipWithNext().all { (a, b) -> b == a + 1 }
    return if (contiguous) "${names[s.first() - 1]} a ${names[s.last() - 1]}" else s.joinToString(", ") { names[it - 1] }
}

@Composable
fun AjustesScreen(app: App) {
    val d = app.data
    val s = d.settings
    val ctx = LocalContext.current
    var confirmDemo by remember { mutableStateOf(false) }
    var confirmWipe by remember { mutableStateOf(false) }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(top = 18.dp, bottom = navPad() + 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Txt("Ajustes", 28, W8, modifier = Modifier.height(44.dp).rise(0))
        Box(Modifier.rise(1)) {
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Image(painterResource(R.drawable.logo), "Caro Lash", Modifier.size(width = 46.dp, height = 50.dp))
                    Column(Modifier.padding(start = 12.dp)) {
                        Txt("Caro Lash", 18, W8)
                        Txt("Funciona sin internet", 13, W6, C.Muted)
                    }
                }
            }
        }
        SettingsRow(1, Ic.List, "Servicios y precios", "${plural(d.services.size, "servicio", "servicios")} · se editan aquí") { app.push(ServiciosS(true)) }
        SettingsRow(2, Ic.Clock, "Horario de atención", "${daysSummary(s.workDays)} · ${timeText(s.startMin)} a ${timeText(s.endMin)}") { app.push(HorarioS(true)) }
        SettingsRow(3, Ic.Target, "Mis metas", "${money(s.goalIncome)} al mes · ${s.goalWeek} por semana") { app.push(MetasS(true)) }
        SettingsRow(4, Ic.Eye, "Guía de diseños", "Muñeca, ardilla y ojo de gato") { app.push(GuiaS) }
        SettingsRow(5, Ic.Repeat, "Repetir bienvenida", "Volver a la configuración inicial") { app.root(Welcome, Kind.FADE) }
        Box(Modifier.rise(8)) {
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(start = 12.dp, end = 6.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconTile(Ic.Bolt)
                    Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                        Txt("Avisos de mantenimiento", 15, W8)
                        Txt("Cuando una clienta ya toque volver", 13, W6, C.Muted)
                    }
                    SwitchBtn(s.maintenanceAlerts, { v -> app.update { it.copy(settings = it.settings.copy(maintenanceAlerts = v)) } }, "Avisos de mantenimiento")
                }
            }
        }
        SettingsRow(9, Ic.Download, "Guardar copia de mis datos", "Clientas, citas y logros en tu celular") {
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Copia de Caro Lash")
                putExtra(Intent.EXTRA_TEXT, app.store.backupText())
            }
            ctx.startActivity(Intent.createChooser(send, "Guardar copia"))
        }
        SettingsRow(10, Ic.Copy, "Restaurar copia", "Copia el texto de tu respaldo y toca aquí") {
            try {
                val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val text = cm.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
                val restored = Codec.decode(text)
                app.update { restored }
                app.toast("Copia restaurada")
            } catch (e: Exception) {
                app.toast("No encontré una copia válida en el portapapeles")
            }
        }
        Txt("Datos de prueba", 13, W7, C.Muted, Modifier.padding(top = 8.dp, start = 2.dp).rise(11))
        Box(Modifier.rise(12)) {
            SoftButton(
                if (confirmDemo) "Esto reemplaza tus datos. Toca otra vez" else "Cargar datos de ejemplo",
                {
                    if (confirmDemo) {
                        confirmDemo = false
                        app.update { cur ->
                            val demo = demoData(cur, app.today)
                            val unlocked = badges(demo, stats(demo, app.today)).filter { it.unlocked }.map { it.id }.toSet()
                            demo.copy(seenBadges = unlocked)
                        }
                        app.toast("Datos de ejemplo cargados")
                    } else confirmDemo = true
                },
                Modifier.fillMaxWidth(), icon = Ic.People,
            )
        }
        Box(Modifier.rise(13)) {
            SoftButton(
                if (confirmWipe) "Se borra todo. Toca otra vez" else "Borrar clientas y citas",
                {
                    if (confirmWipe) {
                        confirmWipe = false
                        app.update { it.copy(clients = emptyList(), appointments = emptyList(), seenBadges = emptySet()) }
                        app.toast("Datos borrados")
                    } else confirmWipe = true
                },
                Modifier.fillMaxWidth(), icon = Ic.Trash, bg = C.DangerBg, fg = C.Danger,
            )
        }
    }
}

@Composable
private fun IconTile(icon: List<String>) {
    Box(Modifier.size(40.dp).background(C.Teal50, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
        Ico(icon, C.TealText, 22.dp, 1.8f)
    }
}

@Composable
private fun SettingsRow(i: Int, icon: List<String>, title: String, sub: String, onClick: () -> Unit) {
    Box(Modifier.rise(1 + i)) {
        Card(Modifier.fillMaxWidth(), onClick = onClick, label = title) {
            Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                IconTile(icon)
                Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                    Txt(title, 15, W8, maxLines = 1)
                    Txt(sub, 13, W6, C.Muted, maxLines = 1)
                }
                Ico(Ic.Next, C.Muted, 20.dp, 2f)
            }
        }
    }
}
