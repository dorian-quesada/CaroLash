package com.carolash.app

import android.graphics.Color as AColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import java.io.File

class MainActivity : ComponentActivity() {
    private var app: App? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(AColor.TRANSPARENT, AColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(AColor.TRANSPARENT, AColor.TRANSPARENT),
        )
        val store = Store(File(filesDir, "caro_lash.json"))
        val a = App(store, store.data.settings.onboarded)
        app = a
        setContent { AppRoot(a) }
    }

    override fun onResume() {
        super.onResume()
        app?.refreshToday()
    }
}

// ------------------------------------------------------------------ Raíz con transiciones

private fun transitionFor(app: App): ContentTransform {
    val ease = EaseOut
    return when (app.kind) {
        Kind.PUSH -> (slideInHorizontally(tween(520, easing = ease)) { it } + fadeIn(tween(300))) togetherWith
            (slideOutHorizontally(tween(520, easing = ease)) { -it / 4 } + fadeOut(tween(420)))
        Kind.POP -> (slideInHorizontally(tween(520, easing = ease)) { -it / 4 } + fadeIn(tween(300))) togetherWith
            (slideOutHorizontally(tween(520, easing = ease)) { it } + fadeOut(tween(420)))
        Kind.TAB -> {
            val d = app.tabDir
            (slideInHorizontally(tween(460, easing = ease)) { d * 120 } + fadeIn(tween(360))) togetherWith
                (slideOutHorizontally(tween(360, easing = ease)) { -d * 120 } + fadeOut(tween(220)))
        }
        Kind.FADE -> fadeIn(tween(560, easing = ease)) togetherWith fadeOut(tween(420))
    }
}

@Composable
fun AppRoot(app: App) {
    app.scope = androidx.compose.runtime.rememberCoroutineScope()
    Box(Modifier.fillMaxSize().background(C.Bg)) {
        Box(
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)),
        ) {
            val sheetOpen = app.sheet != null
            val behind by animateFloatAsState(if (sheetOpen) 0.94f else 1f, tween(480, easing = EaseOut), label = "behind")
            Box(Modifier.fillMaxSize().graphicsLayer { scaleX = behind; scaleY = behind }) {
                AnimatedContent(
                    targetState = app.current,
                    transitionSpec = {
                        val t = transitionFor(app)
                        // En "volver" la pantalla que sale debe quedar encima.
                        t.targetContentZIndex = if (app.kind == Kind.POP) 0f else 1f
                        t
                    },
                    modifier = Modifier.fillMaxSize(),
                    label = "screens",
                ) { screen ->
                    Box(Modifier.fillMaxSize().background(C.Bg)) { ScreenHost(app, screen) }
                }
            }

            // Barra inferior (solo en las 5 pestañas): se desliza hacia abajo en las demás pantallas.
            androidx.compose.animation.AnimatedVisibility(
                visible = app.isTab && !sheetOpen,
                modifier = Modifier.align(Alignment.BottomCenter),
                enter = slideInVertically(tween(480, easing = EaseOut)) { it } + fadeIn(tween(300)),
                exit = slideOutVertically(tween(380, easing = EaseOut)) { it } + fadeOut(tween(250)),
            ) {
                NavBar(app)
            }

            // Fondo oscuro detrás de las hojas.
            val scrim by animateFloatAsState(if (sheetOpen) 0.42f else 0f, tween(420), label = "scrim")
            if (scrim > 0.001f) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0B2B2A).copy(alpha = scrim))
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { app.closeSheet() },
                )
            }
            SheetHost(app)

            Toast(
                app.toastText,
                Modifier.align(Alignment.BottomCenter).padding(bottom = 92.dp).navigationBarsPadding().padding(horizontal = 24.dp),
            )
        }
    }

    LaunchedEffect(app.toastId) {
        if (app.toastText != null) {
            delay(2300)
            app.toastText = null
        }
    }

    BackHandler(enabled = app.sheet != null || app.stack.size > 1 || (app.isTab && app.current != HoyS)) {
        when {
            app.sheet != null -> app.closeSheet()
            app.stack.size > 1 -> app.pop()
            else -> app.tab(0)
        }
    }
}

// ------------------------------------------------------------------ Barra inferior con píldora deslizante

private val NAV_ICONS = listOf(Ic.Home, Ic.Calendar, Ic.People, Ic.Star, Ic.Sliders)

@Composable
fun NavBar(app: App) {
    val sel = tabIndex(app.current).coerceAtLeast(0)
    Column(Modifier.fillMaxWidth().background(Color.White)) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(C.Border))
        BoxWithConstraints(Modifier.fillMaxWidth().height(NAV_H - 1.dp)) {
            val itemW = maxWidth / 5
            val pillX by animateDpAsState(itemW * sel + (itemW - 52.dp) / 2, spring(dampingRatio = 0.72f, stiffness = 380f), label = "pill")
            Box(
                Modifier
                    .offset(x = pillX, y = 9.dp)
                    .size(width = 52.dp, height = 30.dp)
                    .background(C.Teal100, RoundedCornerShape(15.dp)),
            )
            Row(Modifier.fillMaxSize()) {
                for (i in 0..4) {
                    val on = i == sel
                    Column(
                        Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .bounceClick(label = TAB_ORDER[i], onClick = { app.tab(i) })
                            .padding(top = 9.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(Modifier.size(width = 52.dp, height = 30.dp), contentAlignment = Alignment.Center) {
                            Ico(NAV_ICONS[i], if (on) C.TealText else C.Muted, 22.dp, 1.8f)
                        }
                        Txt(TAB_ORDER[i], 12, if (on) W8 else W7, if (on) C.TealText else C.Muted, modifier = Modifier.padding(top = 2.dp))
                    }
                }
            }
        }
        Box(Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom)))
    }
}

// ------------------------------------------------------------------ Hojas inferiores

@Composable
fun SheetHost(app: App) {
    val sheet = app.sheet
    var shown by remember { androidx.compose.runtime.mutableStateOf<Sheet?>(null) }
    if (sheet != null) shown = sheet
    androidx.compose.animation.AnimatedVisibility(
        visible = sheet != null,
        modifier = Modifier.fillMaxSize(),
        enter = slideInVertically(tween(520, easing = EaseOut)) { it },
        exit = slideOutVertically(tween(420, easing = EaseOut)) { it },
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            val s = shown
            when (s) {
                is NuevaClientaSheet -> NuevaClientaContent(app)
                is ReservarSheet -> ReservarContent(app, s)
                is CitaSheet -> CitaContent(app, s)
                null -> {}
            }
        }
    }
}

// ------------------------------------------------------------------ Selector de pantallas

@Composable
fun ScreenHost(app: App, screen: Screen) {
    when (screen) {
        is Welcome -> WelcomeScreen(app)
        is ServiciosS -> ServiciosScreen(app, screen.settings)
        is HorarioS -> HorarioScreen(app, screen.settings)
        is MetasS -> MetasScreen(app, screen.settings)
        is Loading -> LoadingScreen(app)
        is HoyS -> HoyScreen(app)
        is AgendaS -> AgendaScreen(app, screen.focus)
        is ClientasS -> ClientasScreen(app)
        is LogrosS -> LogrosScreen(app)
        is AjustesS -> AjustesScreen(app)
        is RecordatorioS -> RecordatorioScreen(app, screen.apptId)
        is FichaS -> FichaScreen(app, screen.clientId)
        is GuiaS -> GuiaScreen(app)
        is UnlockedS -> UnlockedScreen(app, screen.badgeId)
    }
}
