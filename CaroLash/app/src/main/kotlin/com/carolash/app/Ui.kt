package com.carolash.app

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// ------------------------------------------------------------------ Colores y letras

object C {
    val Teal = Color(0xFF0E7C7A)
    val TealDark = Color(0xFF0B6967)
    val TealText = Color(0xFF0A5F5D)
    val TealOnDark = Color(0xFF2C8582)
    val Bg = Color(0xFFF4F9F8)
    val Border = Color(0xFFDDEBEA)
    val Input = Color(0xFFBFDCDA)
    val Teal50 = Color(0xFFE8F7F6)
    val Teal100 = Color(0xFFD5F1EF)
    val Seg = Color(0xFFE3EFEE)
    val Muted = Color(0xFF5E6666)
    val Ink = Color(0xFF1F2A2A)
    val Ink2 = Color(0xFF3E4848)
    val Amber = Color(0xFFF2C14E)
    val Amber100 = Color(0xFFFBEFC9)
    val Amber900 = Color(0xFF5C3D00)
    val Amber950 = Color(0xFF4A3200)
    val AmberLine = Color(0xFFD9A93A)
    val DangerBg = Color(0xFFFCEAE5)
    val Danger = Color(0xFF8F2E1B)
    val Soft = Color(0xFF7FB9B6)
}

val W4 = FontWeight.Normal
val W6 = FontWeight.SemiBold
val W7 = FontWeight.Bold
val W8 = FontWeight.ExtraBold

val Nunito = FontFamily(
    Font(R.font.nunito_regular, FontWeight.Normal),
    Font(R.font.nunito_semibold, FontWeight.SemiBold),
    Font(R.font.nunito_bold, FontWeight.Bold),
    Font(R.font.nunito_extrabold, FontWeight.ExtraBold),
)
val Script = FontFamily(Font(R.font.dancing_bold, FontWeight.Bold))

val NAV_H = 76.dp
val EaseOut = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)
val EaseSpring = CubicBezierEasing(0.34f, 1.35f, 0.5f, 1f)

@Composable
fun Txt(
    text: String,
    size: Int,
    w: FontWeight = W6,
    color: Color = C.Ink,
    modifier: Modifier = Modifier,
    align: TextAlign = TextAlign.Start,
    lh: Int = 0,
    maxLines: Int = Int.MAX_VALUE,
    script: Boolean = false,
) {
    BasicText(
        text = text,
        modifier = modifier,
        maxLines = maxLines,
        overflow = if (maxLines == Int.MAX_VALUE) TextOverflow.Clip else TextOverflow.Ellipsis,
        style = TextStyle(
            color = color,
            fontSize = size.sp,
            fontWeight = w,
            fontFamily = if (script) Script else Nunito,
            textAlign = align,
            lineHeight = if (lh > 0) lh.sp else TextUnit.Unspecified,
        ),
    )
}

// ------------------------------------------------------------------ Íconos (trazos de 24 x 24)

private fun circle(cx: Int, cy: Int, r: Int): String =
    "M${cx - r} ${cy}a$r $r 0 1 0 ${2 * r} 0a$r $r 0 1 0 ${-2 * r} 0"

private fun rrect(x: Int, y: Int, w: Int, h: Int, r: Int): String =
    "M${x + r} ${y}h${w - 2 * r}a$r $r 0 0 1 $r ${r}v${h - 2 * r}a$r $r 0 0 1 ${-r} ${r}h${-(w - 2 * r)}a$r $r 0 0 1 ${-r} ${-r}v${-(h - 2 * r)}a$r $r 0 0 1 $r ${-r}z"

object Ic {
    val Back = listOf("M15 5l-7 7 7 7")
    val Next = listOf("M9 5l7 7-7 7")
    val Home = listOf("M3 11l9-7 9 7v9a1 1 0 0 1-1 1h-5v-6H9v6H4a1 1 0 0 1-1-1z")
    val Calendar = listOf(rrect(3, 5, 18, 16, 3), "M8 3v4M16 3v4M3 10h18")
    val CalendarDay = listOf(rrect(3, 5, 18, 16, 3), "M8 3v4M16 3v4M3 10h18M9 15h6")
    val People = listOf(circle(9, 8, 4), "M2.5 20c.5-3.5 3.2-5.5 6.5-5.5s6 2 6.5 5.5", "M16 4.8a3.2 3.2 0 0 1 0 6.4M18.5 14.8c1.6.8 2.8 2.6 3 5")
    val Star = listOf("M12 3l2.7 5.6 6.1.9-4.4 4.3 1 6.1L12 17l-5.4 2.9 1-6.1L3.2 9.5l6.1-.9z")
    val Sliders = listOf("M4 7h10M18 7h2M4 17h2M10 17h10", circle(16, 7, 2), circle(8, 17, 2))
    val Chat = listOf("M21 12a8 8 0 0 1-11.7 7.1L4 20l1-4.6A8 8 0 1 1 21 12z")
    val Check = listOf("M5 12.5l4.5 4.5L19 7.5")
    val Plus = listOf("M12 5v14M5 12h14")
    val Minus = listOf("M5 12h14")
    val Trash = listOf("M4 7h16M9 7V4h6v3M6 7l1 13h10l1-13M10 11v6M14 11v6")
    val Close = listOf("M6 6l12 12M18 6L6 18")
    val Warning = listOf("M12 4l9.5 16.5h-19z", "M12 10v4.5M12 17.5v.5")
    val Search = listOf(circle(11, 11, 6), "M20 20l-4.2-4.2")
    val Edit = listOf("M4 20h4L19 9l-4-4L4 16z")
    val Clock = listOf(circle(12, 12, 8), "M12 8v4l3 2")
    val Copy = listOf(rrect(9, 9, 11, 11, 2), "M5 15V6a2 2 0 0 1 2-2h9")
    val Bolt = listOf("M13 3L5 14h6l-1 7 8-11h-6z")
    val Target = listOf(circle(12, 12, 9), circle(12, 12, 5), circle(12, 12, 1))
    val Repeat = listOf("M4 12a8 8 0 0 1 14-5.3L20 9M20 4v5h-5M20 12a8 8 0 0 1-14 5.3L4 15M4 20v-5h5")
    val Download = listOf("M12 4v11M7 10l5 5 5-5M5 20h14")
    val Eye = listOf("M2 12s4-7 10-7 10 7 10 7-4 7-10 7S2 12 2 12z", circle(12, 12, 3))
    val List = listOf("M4 6h16M4 12h16M4 18h10")
    val Flame = listOf("M12 3c1 4 5 5 5 10a5 5 0 0 1-10 0c0-2 1-3 2-4 0 2 1 3 2 3 0-3-1-5 1-9z")
    val Gift = listOf(rrect(3, 9, 18, 12, 2), "M12 9v12M3 13h18M12 9c-3 0-4-4-1.5-4S12 9 12 9zM12 9c3 0 4-4 1.5-4S12 9 12 9z")
}

@Composable
fun Ico(paths: List<String>, tint: Color, size: Dp = 22.dp, stroke: Float = 1.8f, fill: Boolean = false, modifier: Modifier = Modifier) {
    val parsed = remember(paths) { paths.map { PathParser().parsePathString(it).toPath() } }
    Canvas(modifier.size(size)) {
        val k = this.size.minDimension / 24f
        scale(k, k, pivot = Offset.Zero) {
            for (p in parsed) {
                if (fill) drawPath(p, tint, style = Fill)
                else drawPath(p, tint, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
        }
    }
}

// ------------------------------------------------------------------ Animaciones comunes

/** Rebote suave al apretar. */
fun Modifier.bounceClick(enabled: Boolean = true, label: String? = null, onClick: () -> Unit): Modifier = composed {
    val src = remember { MutableInteractionSource() }
    val pressed by src.collectIsPressedAsState()
    val s by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 520f),
        label = "press",
    )
    this
        .graphicsLayer { scaleX = s; scaleY = s }
        .then(if (label != null) Modifier.semantics { contentDescription = label } else Modifier)
        .clickable(interactionSource = src, indication = null, enabled = enabled, role = Role.Button, onClick = onClick)
}

/** Entrada escalonada: sube un poco y aparece. */
fun Modifier.rise(i: Int): Modifier = composed {
    val a = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(70L + i * 60L)
        a.animateTo(1f, tween(700, easing = EaseOut))
    }
    this.graphicsLayer {
        alpha = a.value
        translationY = (1f - a.value) * 18.dp.toPx()
    }
}

/** Entra con un pequeño rebote (para cuando se agrega algo). */
fun Modifier.popIn(): Modifier = composed {
    val a = remember { Animatable(0.85f) }
    LaunchedEffect(Unit) { a.animateTo(1f, spring(dampingRatio = 0.55f, stiffness = 380f)) }
    this.graphicsLayer {
        scaleX = a.value
        scaleY = a.value
        alpha = ((a.value - 0.85f) / 0.15f).coerceIn(0f, 1f)
    }
}

@Composable
fun Modifier.windowInsetsBottom(): Modifier = this.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))

@Composable
fun navPad(): Dp = NAV_H + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

// ------------------------------------------------------------------ Piezas

@Composable
fun Card(
    modifier: Modifier = Modifier,
    radius: Int = 16,
    bg: Color = Color.White,
    border: Color = C.Border,
    onClick: (() -> Unit)? = null,
    label: String? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val shape = RoundedCornerShape(radius.dp)
    var m = modifier
    if (onClick != null) m = m.bounceClick(label = label, onClick = onClick)
    Box(m.background(bg, shape).border(1.dp, border, shape).clip(shape), content = content)
}

@Composable
fun BigButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    height: Int = 56,
    bg: Color = C.Teal,
    fg: Color = Color.White,
    icon: List<String>? = null,
    size: Int = 18,
    radius: Int = 16,
) {
    val shape = RoundedCornerShape(radius.dp)
    val b by animateColorAsState(bg, tween(300), label = "btn")
    Row(
        modifier
            .fillMaxWidth()
            .height(height.dp)
            .alpha(if (enabled) 1f else 0.5f)
            .bounceClick(enabled = enabled, onClick = onClick)
            .background(b, shape),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Ico(icon, fg, 22.dp, 2f)
            Spacer(Modifier.width(8.dp))
        }
        Txt(text, size, W8, fg)
    }
}

/** Botón chico con fondo suave (turquesa claro). */
@Composable
fun SoftButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: List<String>? = null, bg: Color = C.Teal50, fg: Color = C.TealText, enabled: Boolean = true) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier
            .height(44.dp)
            .alpha(if (enabled) 1f else 0.5f)
            .bounceClick(enabled = enabled, onClick = onClick)
            .background(bg, shape)
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Ico(icon, fg, 18.dp, 2f)
            Spacer(Modifier.width(6.dp))
        }
        Txt(text, 14, W8, fg)
    }
}

@Composable
fun Chip(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(999.dp)
    val bg by animateColorAsState(if (selected) C.Teal else Color.White, tween(280), label = "chipBg")
    val fg by animateColorAsState(if (selected) Color.White else C.Ink, tween(280), label = "chipFg")
    val bd by animateColorAsState(if (selected) C.Teal else C.Input, tween(280), label = "chipBd")
    Box(
        modifier
            .height(44.dp)
            .bounceClick(onClick = onClick)
            .background(bg, shape)
            .border(1.5.dp, bd, shape)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Txt(text, 14, if (selected) W8 else W7, fg)
    }
}

@Composable
fun Pill(text: String, bg: Color, fg: Color, modifier: Modifier = Modifier, size: Int = 12) {
    Box(modifier.background(bg, RoundedCornerShape(999.dp)).padding(horizontal = 10.dp, vertical = 4.dp)) {
        Txt(text, size, W8, fg, maxLines = 1)
    }
}

@Composable
fun Avatar(name: String, size: Int = 40) {
    Box(Modifier.size(size.dp).background(C.Teal100, CircleShape), contentAlignment = Alignment.Center) {
        Txt(initials(name), if (size >= 48) 17 else 15, W8, C.TealText)
    }
}

@Composable
fun IconBtn(icon: List<String>, desc: String, onClick: () -> Unit, tint: Color = C.Ink, modifier: Modifier = Modifier, size: Dp = 22.dp, stroke: Float = 2f) {
    Box(
        modifier.size(44.dp).bounceClick(label = desc, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Ico(icon, tint, size, stroke) }
}

@Composable
fun TopBar(title: String, onBack: (() -> Unit)?, modifier: Modifier = Modifier, trailing: @Composable () -> Unit = {}) {
    Row(modifier.fillMaxWidth().height(44.dp), verticalAlignment = Alignment.CenterVertically) {
        if (onBack != null) {
            IconBtn(Ic.Back, "Volver", onBack, size = 24.dp, modifier = Modifier.offset(x = (-10).dp))
        }
        Txt(title, 22, W8, modifier = Modifier.weight(1f), maxLines = 1)
        trailing()
    }
}

@Composable
fun FooterBar(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(
        modifier
            .fillMaxWidth()
            .background(Color.White)
            .border(1.dp, C.Border)
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .windowInsetsBottom(),
    ) { content() }
}

@Composable
fun SwitchBtn(on: Boolean, onChange: (Boolean) -> Unit, desc: String) {
    val x by animateDpAsState(if (on) 23.dp else 3.dp, spring(dampingRatio = 0.6f, stiffness = 500f), label = "thumb")
    val track by animateColorAsState(if (on) C.Teal else C.Input, tween(250), label = "track")
    Box(
        Modifier.width(56.dp).height(44.dp).bounceClick(label = desc, onClick = { onChange(!on) }),
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.width(48.dp).height(28.dp).background(track, RoundedCornerShape(14.dp))) {
            Box(Modifier.offset(x = x, y = 3.dp).size(22.dp).background(Color.White, CircleShape))
        }
    }
}

/** Número con − y + que cambia con una animación vertical. */
@Composable
fun Stepper(value: Int, label: String, onMinus: () -> Unit, onPlus: () -> Unit, text: String = value.toString(), size: Int = 22, minWidth: Int = 40) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        SquareBtn(Ic.Minus, "Menos $label", onMinus)
        Box(Modifier.width(minWidth.dp), contentAlignment = Alignment.Center) {
            AnimatedContent(
                targetState = value,
                transitionSpec = {
                    val up = targetState > initialState
                    (slideInVertically(tween(260, easing = EaseOut)) { if (up) it / 2 else -it / 2 } + fadeIn(tween(200))) togetherWith
                        (slideOutVertically(tween(200, easing = EaseOut)) { if (up) -it / 2 else it / 2 } + fadeOut(tween(120)))
                },
                label = "stepper",
            ) { _ -> Txt(text, size, W8, align = TextAlign.Center) }
        }
        SquareBtn(Ic.Plus, "Más $label", onPlus)
    }
}

@Composable
fun SquareBtn(icon: List<String>, desc: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier.size(44.dp).bounceClick(label = desc, onClick = onClick).background(C.Teal50, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center,
    ) { Ico(icon, C.TealText, 18.dp, 2.4f) }
}

@Composable
fun Field(
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    hint: String = "",
    height: Int = 44,
    size: Int = 16,
    w: FontWeight = W7,
    align: TextAlign = TextAlign.Start,
    keyboard: KeyboardType = KeyboardType.Text,
    bg: Color = C.Bg,
    bordered: Boolean = true,
    prefix: String? = null,
) {
    var focused by remember { mutableStateOf(false) }
    var raw by remember { mutableStateOf(TextFieldValue(value)) }
    // Si el texto cambió desde afuera (formato, límites), el cursor se va al final.
    val tfv = if (raw.text != value) TextFieldValue(value, TextRange(value.length)) else raw
    val bd by animateColorAsState(if (focused) C.Teal else C.Input, tween(200), label = "fieldBd")
    val shape = RoundedCornerShape(12.dp)
    var m = modifier.height(height.dp).onFocusChanged { focused = it.hasFocus }
    if (bordered) m = m.background(bg, shape).border(1.5.dp, bd, shape)
    Row(m.padding(horizontal = if (bordered) 12.dp else 4.dp), verticalAlignment = Alignment.CenterVertically) {
        if (prefix != null) {
            Txt(prefix, 16, W7, C.Muted)
            Spacer(Modifier.width(8.dp))
        }
        BasicTextField(
            value = tfv,
            onValueChange = { raw = it; onChange(it.text) },
            singleLine = true,
            textStyle = TextStyle(color = C.Ink, fontSize = size.sp, fontWeight = w, fontFamily = Nunito, textAlign = align),
            keyboardOptions = KeyboardOptions(keyboardType = keyboard),
            cursorBrush = SolidColor(C.Teal),
            modifier = Modifier.weight(1f),
            decorationBox = { inner ->
                Box(Modifier.fillMaxWidth(), contentAlignment = when (align) {
                    TextAlign.End -> Alignment.CenterEnd
                    TextAlign.Center -> Alignment.Center
                    else -> Alignment.CenterStart
                }) {
                    if (value.isEmpty() && hint.isNotEmpty()) Txt(hint, size, W6, C.Muted.copy(alpha = 0.7f))
                    inner()
                }
            },
        )
    }
}

/** Precio en colones: se ve como ₡25 000 y solo acepta números. */
@Composable
fun MoneyField(value: Int, onChange: (Int) -> Unit, modifier: Modifier = Modifier, height: Int = 44, size: Int = 16, align: TextAlign = TextAlign.End) {
    Field(
        value = if (value == 0) "" else money(value),
        onChange = { t ->
            val d = t.filter { it.isDigit() }.take(7)
            onChange(if (d.isEmpty()) 0 else d.toInt())
        },
        modifier = modifier,
        hint = "₡0",
        height = height,
        size = size,
        align = align,
        keyboard = KeyboardType.Number,
    )
}

@Composable
fun Toast(text: String?, modifier: Modifier = Modifier) {
    androidx.compose.animation.AnimatedVisibility(
        visible = text != null,
        modifier = modifier,
        enter = slideInVertically(tween(380, easing = EaseOut)) { it } + fadeIn(tween(250)),
        exit = slideOutVertically(tween(300)) { it } + fadeOut(tween(220)),
    ) {
        var last by remember { mutableStateOf("") }
        if (text != null) last = text
        Box(
            Modifier.background(C.Ink, RoundedCornerShape(14.dp)).padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) { Txt(last, 14, W7, Color.White, align = TextAlign.Center) }
    }
}

// ------------------------------------------------------------------ Skeleton

@Composable
fun Shimmer(modifier: Modifier, radius: Int = 12) {
    val t = rememberInfiniteTransition(label = "shimmer")
    val x by t.animateFloat(
        initialValue = -300f,
        targetValue = 900f,
        animationSpec = infiniteRepeatable(tween(1300, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmerX",
    )
    val brush = Brush.linearGradient(
        colors = listOf(Color(0xFFE3EFEE), Color(0xFFF4F9F8), Color(0xFFE3EFEE)),
        start = Offset(x, 0f),
        end = Offset(x + 300f, 120f),
    )
    Box(modifier.background(brush, RoundedCornerShape(radius.dp)))
}

/** Evita que un toque sobre un panel llegue a lo que está detrás. */
fun Modifier.swallowTaps(): Modifier = composed {
    clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = {})
}
