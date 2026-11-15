package com.carolash.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ------------------------------------------------------------------ Bienvenida

@Composable
fun WelcomeScreen(app: App) {
    val t = rememberInfiniteTransition(label = "float")
    val dy by t.animateFloat(0f, 1f, infiniteRepeatable(tween(3200), RepeatMode.Reverse), label = "floatY")
    BoxWithConstraints(Modifier.fillMaxSize().background(Color.White)) {
        val small = maxHeight < 700.dp
        val logoH = if (small) 180.dp else 260.dp
        Column(
            Modifier.fillMaxSize().padding(horizontal = 28.dp).padding(top = if (small) 20.dp else 40.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = "Caro Lash",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(width = logoH * 0.92f, height = logoH)
                    .rise(0)
                    .graphicsLayer { translationY = (dy - 0.5f) * 12.dp.toPx() },
            )
            Txt("Hola, Caro", if (small) 46 else 56, W7, C.Teal, Modifier.padding(top = 20.dp).rise(1), script = true)
            Txt(
                "Vamos a empezar a customizar tu app según tus servicios y tus precios.",
                18, W6, C.Ink, Modifier.padding(top = 12.dp).rise(2), align = TextAlign.Center, lh = 26,
            )
            Txt(
                "Son unos minutos. Todo se guarda en tu celular y funciona sin internet.",
                15, W4, C.Muted, Modifier.padding(top = 10.dp).rise(3), align = TextAlign.Center, lh = 22,
            )
            Spacer(Modifier.weight(1f))
            Box(Modifier.rise(4)) { BigButton("Empezar", { app.push(ServiciosS(false)) }) }
            Txt("Después puedes cambiar todo en Ajustes", 13, W6, C.Muted, Modifier.padding(top = 12.dp).rise(5))
        }
    }
}

// ------------------------------------------------------------------ Piezas de los pasos

@Composable
fun StepHeader(app: App, step: Int, settings: Boolean) {
    Row(Modifier.fillMaxWidth().height(44.dp), verticalAlignment = Alignment.CenterVertically) {
        IconBtn(Ic.Back, "Volver", { app.pop() }, size = 24.dp, modifier = Modifier.offset(x = (-10).dp))
        Spacer(Modifier.weight(1f))
        if (!settings) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                for (i in 1..3) {
                    val w by animateDpAsState(if (i == step) 22.dp else 6.dp, tween(400), label = "dot")
                    Box(Modifier.width(w).height(6.dp).background(if (i <= step) C.Teal else C.Input, RoundedCornerShape(3.dp)))
                }
            }
        }
    }
}

@Composable
fun StepTitle(title: String, sub: String) {
    Column(Modifier.rise(0)) {
        Txt(title, 28, W8, lh = 32)
        Txt(sub, 15, W4, C.Muted, Modifier.padding(top = 6.dp), lh = 21)
    }
}

// ------------------------------------------------------------------ Servicios

@Composable
fun ServiciosScreen(app: App, settings: Boolean) {
    val items = remember { mutableStateListOf<Service>().apply { addAll(app.data.services) } }
    val scroll = rememberScrollState()
    val scope = rememberCoroutineScope()
    var added by remember { mutableStateOf(0) }
    LaunchedEffect(added) {
        if (added > 0) {
            delay(120)
            scroll.animateScrollTo(scroll.maxValue)
        }
    }
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(horizontal = 20.dp).padding(top = 8.dp)) {
            StepHeader(app, 1, settings)
            StepTitle("Servicios y precios", "Son ejemplos: cámbialos, quítalos o agrega los tuyos.")
        }
        Column(
            Modifier.weight(1f).verticalScroll(scroll).padding(horizontal = 20.dp).padding(top = 12.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            for (s in items.toList()) {
                key(s.id) {
                    val vs = remember { MutableTransitionState(false).apply { targetState = true } }
                    AnimatedVisibility(
                        visibleState = vs,
                        enter = fadeIn(tween(420)) + expandVertically(tween(460, easing = EaseOut)),
                        exit = fadeOut(tween(240)) + shrinkVertically(tween(400, easing = EaseOut)),
                    ) {
                        Box(Modifier.padding(bottom = 0.dp)) {
                            ServiceCard(
                                s = s,
                                onChange = { n ->
                                    val i = items.indexOfFirst { it.id == s.id }
                                    if (i >= 0) items[i] = n
                                },
                                onRemove = {
                                    vs.targetState = false
                                    scope.launch {
                                        delay(430)
                                        items.removeAll { it.id == s.id }
                                    }
                                },
                            )
                        }
                    }
                }
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .bounceClick(onClick = {
                        items.add(Service(newId(), "", 0, 60))
                        added += 1
                    })
                    .border(1.5.dp, C.Teal, RoundedCornerShape(14.dp)),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Ico(Ic.Plus, C.Teal, 20.dp, 2.2f)
                Spacer(Modifier.width(8.dp))
                Txt("Agregar servicio", 16, W8, C.Teal)
            }
        }
        FooterBar {
            BigButton(if (settings) "Guardar cambios" else "Guardar y continuar", {
                val ok = items.filter { it.name.isNotBlank() }
                if (ok.isEmpty()) {
                    app.toast("Agrega al menos un servicio")
                } else {
                    app.update { it.copy(services = ok) }
                    if (settings) {
                        app.toast("Servicios guardados")
                        app.pop()
                    } else app.push(HorarioS(false))
                }
            })
        }
    }
}

@Composable
private fun ServiceCard(s: Service, onChange: (Service) -> Unit, onRemove: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Field(
                    value = s.name, onChange = { onChange(s.copy(name = it.take(40))) },
                    modifier = Modifier.weight(1f), hint = "Nombre del servicio", size = 17, w = W8, bordered = false,
                )
                IconBtn(Ic.Trash, "Quitar servicio", onRemove, tint = C.Muted, size = 20.dp, stroke = 1.8f)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    MoneyField(s.price, { onChange(s.copy(price = it)) }, Modifier.weight(1f))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SquareBtn(Ic.Minus, "Menos tiempo", { onChange(s.copy(minutes = (s.minutes - 15).coerceAtLeast(15))) })
                    Box(Modifier.width(58.dp), contentAlignment = Alignment.Center) {
                        Txt(durationText(s.minutes), 15, W8, align = TextAlign.Center)
                    }
                    SquareBtn(Ic.Plus, "Más tiempo", { onChange(s.copy(minutes = (s.minutes + 15).coerceAtMost(480))) })
                }
            }
        }
    }
}

// ------------------------------------------------------------------ Horario

@Composable
fun HorarioScreen(app: App, settings: Boolean) {
    val s0 = app.data.settings
    var days by remember { mutableStateOf(s0.workDays) }
    var start by remember { mutableStateOf(s0.startMin) }
    var end by remember { mutableStateOf(s0.endMin) }
    var brk by remember { mutableStateOf(s0.breakMin) }
    var lunch by remember { mutableStateOf(s0.lunch) }
    Column(Modifier.fillMaxSize()) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(top = 8.dp, bottom = 16.dp),
        ) {
            StepHeader(app, 2, settings)
            StepTitle("Tu horario de trabajo", "Así la agenda no te deja agendar citas en tus horas libres. Lo cambias cuando quieras en Ajustes.")
            Txt("Días que trabajas", 14, W8, modifier = Modifier.padding(top = 22.dp, bottom = 8.dp).rise(1))
            Row(Modifier.fillMaxWidth().rise(1), horizontalArrangement = Arrangement.SpaceBetween) {
                for (i in 1..7) {
                    val on = i in days
                    val bg by animateColorAsState(if (on) C.Teal else Color.White, tween(260), label = "day")
                    val bd by animateColorAsState(if (on) C.Teal else C.Input, tween(260), label = "dayBd")
                    Box(
                        Modifier
                            .size(40.dp)
                            .bounceClick(label = DOWS_FULL[i - 1], onClick = {
                                days = if (on) { if (days.size > 1) days - i else days } else days + i
                            })
                            .background(bg, CircleShape)
                            .border(1.5.dp, bd, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) { Txt(DOWS_SHORT[i - 1], 15, if (on) W8 else W7, if (on) Color.White else C.Ink2) }
                }
            }
            Spacer(Modifier.height(18.dp))
            Box(Modifier.rise(2)) {
                SettingRow("Empiezo a las", null) {
                    Stepper(start, "hora de inicio", { start = (start - 30).coerceAtLeast(5 * 60) },
                        { start = (start + 30).coerceAtMost(end - 60) }, text = timeText(start), size = 17, minWidth = 84)
                }
            }
            Spacer(Modifier.height(10.dp))
            Box(Modifier.rise(3)) {
                SettingRow("Termino a las", null) {
                    Stepper(end, "hora de cierre", { end = (end - 30).coerceAtLeast(start + 60) },
                        { end = (end + 30).coerceAtMost(22 * 60) }, text = timeText(end), size = 17, minWidth = 84)
                }
            }
            Spacer(Modifier.height(10.dp))
            Box(Modifier.rise(4)) {
                SettingRow("Descanso entre citas", "Para limpiar y preparar") {
                    Stepper(brk, "minutos de descanso", { brk = (brk - 5).coerceAtLeast(0) },
                        { brk = (brk + 5).coerceAtMost(60) }, text = "$brk min", size = 16, minWidth = 56)
                }
            }
            Spacer(Modifier.height(10.dp))
            Box(Modifier.rise(5)) {
                SettingRow("Hora de almuerzo", "12:00 – 1:00 p. m. sin citas") {
                    SwitchBtn(lunch, { lunch = it }, "Bloquear la hora de almuerzo")
                }
            }
        }
        FooterBar {
            BigButton(if (settings) "Guardar cambios" else "Continuar", {
                app.update { it.copy(settings = it.settings.copy(workDays = days, startMin = start, endMin = end, breakMin = brk, lunch = lunch)) }
                if (settings) {
                    app.toast("Horario guardado")
                    app.pop()
                } else app.push(MetasS(false))
            })
        }
    }
}

@Composable
fun SettingRow(title: String, sub: String?, trailing: @Composable () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(start = 14.dp, end = 8.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Txt(title, 16, W8)
                if (sub != null) Txt(sub, 13, W6, C.Muted)
            }
            trailing()
        }
    }
}

// ------------------------------------------------------------------ Metas

@Composable
fun MetasScreen(app: App, settings: Boolean) {
    val s0 = app.data.settings
    var income by remember { mutableStateOf(s0.goalIncome) }
    var week by remember { mutableStateOf(s0.goalWeek) }
    var month by remember { mutableStateOf(s0.goalMonth) }
    Column(Modifier.fillMaxSize()) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(top = 8.dp, bottom = 16.dp),
        ) {
            StepHeader(app, 3, settings)
            StepTitle("Pon tus metas", "Con esto armamos tus logros y te celebramos cuando las cumplas. Las cambias cuando quieras.")
            Box(Modifier.padding(top = 18.dp).rise(1)) {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Txt("Meta de ingresos del mes", 14, W8)
                        MoneyField(income, { income = it }, Modifier.fillMaxWidth(), height = 52, size = 28, align = TextAlign.Start)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            for (v in listOf(150_000, 250_000, 400_000)) {
                                Box(Modifier.weight(1f)) {
                                    Chip(money(v), income == v, { income = v }, Modifier.fillMaxWidth())
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Box(Modifier.rise(2)) {
                SettingRow("Clientas por semana", "Tu meta semanal") {
                    Stepper(week, "clientas por semana", { week = (week - 1).coerceAtLeast(1) }, { week = (week + 1).coerceAtMost(30) })
                }
            }
            Spacer(Modifier.height(12.dp))
            Box(Modifier.rise(3)) {
                SettingRow("Clientas al mes", "Tu meta mensual") {
                    Stepper(month, "clientas al mes", { month = (month - 1).coerceAtLeast(1) }, { month = (month + 1).coerceAtMost(120) })
                }
            }
            Row(
                Modifier.padding(top = 14.dp).fillMaxWidth().rise(4).background(C.Amber100, RoundedCornerShape(14.dp)).padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Ico(Ic.Star, C.Amber900, 20.dp, fill = true)
                Txt("Las rachas, los récords y las insignias se activan solos. No tienes que configurar nada más.", 14, W7, C.Amber900, lh = 19)
            }
        }
        FooterBar {
            BigButton(if (settings) "Guardar cambios" else "Listo, entrar a mi agenda", {
                app.update { it.copy(settings = it.settings.copy(goalIncome = income, goalWeek = week, goalMonth = month, onboarded = true)) }
                if (settings) {
                    app.toast("Metas guardadas")
                    app.pop()
                } else {
                    app.loadingMs = 1700
                    app.root(Loading, Kind.FADE)
                }
            })
        }
    }
}

// ------------------------------------------------------------------ Cargando (skeleton)

@Composable
fun LoadingScreen(app: App) {
    LaunchedEffect(Unit) {
        delay(app.loadingMs.toLong())
        app.root(HoyS, Kind.FADE)
    }
    val t = rememberInfiniteTransition(label = "breath")
    val k by t.animateFloat(0.94f, 1.04f, infiniteRepeatable(tween(1200), RepeatMode.Reverse), label = "breathK")
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp).padding(top = 32.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Shimmer(Modifier.width(170.dp).height(28.dp), 10)
                Shimmer(Modifier.width(120.dp).height(14.dp), 7)
            }
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = "Caro Lash",
                modifier = Modifier.size(width = 44.dp, height = 48.dp).graphicsLayer { scaleX = k; scaleY = k },
            )
        }
        Shimmer(Modifier.fillMaxWidth().height(104.dp), 16)
        Shimmer(Modifier.width(130.dp).height(20.dp).padding(top = 4.dp), 8)
        for (i in 1..2) Shimmer(Modifier.fillMaxWidth().height(64.dp), 16)
        Shimmer(Modifier.width(60.dp).height(20.dp), 8)
        for (i in 1..3) Shimmer(Modifier.fillMaxWidth().height(52.dp), 14)
    }
}
