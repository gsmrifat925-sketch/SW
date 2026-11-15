package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.i18n.LocalStrings
import com.example.ui.theme.BrightViolet
import com.example.ui.theme.CardDarkElevated
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.MagentaGlow
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonRed
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.PureBlack
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

/**
 * Custom Canvas S-shaped lightning bolt logo with a 3-point esports crown and neon violet aura.
 */
@Composable
fun ShockwaveSLightningLogo(
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    showCrown: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "logo_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Ambient neon radial glow behind S-bolt
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    NeonPurple.copy(alpha = glowAlpha * 0.65f),
                    ElectricViolet.copy(alpha = glowAlpha * 0.25f),
                    Color.Transparent
                ),
                center = Offset(w * 0.5f, h * 0.55f),
                radius = w * 0.55f
            )
        )

        if (showCrown) {
            val crownPath = Path().apply {
                moveTo(w * 0.34f, h * 0.23f)
                lineTo(w * 0.30f, h * 0.11f)
                lineTo(w * 0.43f, h * 0.17f)
                lineTo(w * 0.52f, h * 0.06f)
                lineTo(w * 0.61f, h * 0.17f)
                lineTo(w * 0.74f, h * 0.11f)
                lineTo(w * 0.69f, h * 0.23f)
                close()
            }
            drawPath(
                path = crownPath,
                brush = Brush.linearGradient(listOf(TextPrimary, BrightViolet))
            )
            drawPath(
                path = crownPath,
                color = NeonPurple,
                style = Stroke(width = w * 0.025f, join = StrokeJoin.Round)
            )
        }

        val topOffset = if (showCrown) 0.26f else 0.12f
        // Sharp S-shaped lightning bolt path
        val boltPath = Path().apply {
            moveTo(w * 0.72f, h * topOffset)
            lineTo(w * 0.36f, h * topOffset)
            lineTo(w * 0.21f, h * 0.57f)
            lineTo(w * 0.49f, h * 0.57f)
            lineTo(w * 0.33f, h * 0.94f)
            lineTo(w * 0.79f, h * 0.47f)
            lineTo(w * 0.52f, h * 0.47f)
            lineTo(w * 0.64f, h * (topOffset + 0.09f))
            close()
        }

        // Outer neon purple stroke
        drawPath(
            path = boltPath,
            color = MagentaGlow.copy(alpha = glowAlpha),
            style = Stroke(width = w * 0.09f, cap = StrokeCap.Round, join = StrokeJoin.Miter)
        )
        // Metallic white-silver lightning fill
        drawPath(
            path = boltPath,
            brush = Brush.linearGradient(
                colors = listOf(TextPrimary, Color(0xFFE9D5FF), BrightViolet),
                start = Offset(w * 0.2f, h * topOffset),
                end = Offset(w * 0.8f, h * 0.94f)
            )
        )
        // Sharp neon rim
        drawPath(
            path = boltPath,
            color = NeonPurple,
            style = Stroke(width = w * 0.028f, join = StrokeJoin.Miter)
        )
    }
}

@Composable
fun ShockwaveWordmark(
    fontSize: TextUnit = 24.sp,
    modifier: Modifier = Modifier
) {
    val text = buildAnnotatedString {
        withStyle(SpanStyle(color = TextPrimary)) {
            append("SHOCK")
        }
        withStyle(
            SpanStyle(
                brush = Brush.linearGradient(
                    colors = listOf(BrightViolet, NeonPurple, MagentaGlow)
                )
            )
        ) {
            append("WAVE")
        }
    }
    Text(
        text = text,
        fontFamily = OrbitronFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = fontSize,
        letterSpacing = 1.5.sp,
        modifier = modifier
    )
}

@Composable
fun ShockwaveTeamAvatar(
    size: Dp = 44.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(PureBlack)
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(listOf(BrightViolet, ElectricBlue)),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_shockwave_emblem),
            contentDescription = "SHOCKWAVE emblem",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun ShockwaveSplashScreen(
    onFinished: () -> Unit
) {
    val strings = LocalStrings.current
    val logoScale = remember { Animatable(0.55f) }
    val contentAlpha = remember { Animatable(0f) }
    val pulse = rememberInfiniteTransition(label = "splash_ring")
    val ringScale by pulse.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring_scale"
    )
    val ringAlpha by pulse.animateFloat(
        initialValue = 0.65f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring_alpha"
    )

    LaunchedEffect(Unit) {
        logoScale.animateTo(1f, animationSpec = spring(dampingRatio = 0.58f, stiffness = 260f))
    }
    LaunchedEffect(Unit) {
        contentAlpha.animateTo(1f, animationSpec = tween(650))
        delay(1050)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF1D093B), DeepObsidian, PureBlack),
                    radius = 950f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Expanding shockwave ring
        Box(
            modifier = Modifier
                .size(160.dp)
                .scale(ringScale)
                .alpha(ringAlpha)
                .border(2.dp, NeonPurple, CircleShape)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .alpha(contentAlpha.value)
                .padding(24.dp)
        ) {
            ShockwaveSLightningLogo(
                size = 124.dp,
                modifier = Modifier.scale(logoScale.value)
            )
            Spacer(modifier = Modifier.height(18.dp))
            ShockwaveWordmark(fontSize = 32.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = strings.subtitle,
                style = MaterialTheme.typography.labelLarge,
                color = BrightViolet,
                letterSpacing = 2.4.sp
            )
        }
    }
}

@Composable
fun PinLockScreen(
    title: String,
    subtitle: String,
    errorMessage: String? = null,
    showCancel: Boolean = false,
    onCancel: () -> Unit = {},
    onPinComplete: (String) -> Unit
) {
    var digits by remember { mutableStateOf("") }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            digits = ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF13072A), DeepObsidian, PureBlack)
                )
            )
            .statusBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(NeonPurple.copy(alpha = 0.16f))
                    .border(1.5.dp, NeonPurple.copy(alpha = 0.65f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "PIN Lock",
                    tint = BrightViolet,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            ShockwaveWordmark(fontSize = 22.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                repeat(4) { index ->
                    val filled = index < digits.length
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(if (filled) NeonPurple else Color.Transparent)
                            .border(
                                width = 2.dp,
                                color = if (filled) BrightViolet else TextSecondary.copy(alpha = 0.5f),
                                shape = CircleShape
                            )
                    )
                }
            }
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = NeonRed,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            val rows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9")
            )
            rows.forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(22.dp)) {
                    row.forEach { digit ->
                        PinKeyButton(label = digit) {
                            if (digits.length < 4) {
                                val updated = digits + digit
                                digits = updated
                                if (updated.length == 4) {
                                    onPinComplete(updated)
                                }
                            }
                        }
                    }
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(22.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showCancel) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onCancel)
                            .minimumInteractiveComponentSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = LocalStrings.current.cancel,
                            style = MaterialTheme.typography.labelLarge,
                            color = TextSecondary
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(72.dp))
                }
                PinKeyButton(label = "0") {
                    if (digits.length < 4) {
                        val updated = digits + "0"
                        digits = updated
                        if (updated.length == 4) {
                            onPinComplete(updated)
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(CardDarkElevated.copy(alpha = 0.7f))
                        .border(1.dp, NeonPurple.copy(alpha = 0.35f), CircleShape)
                        .clickable {
                            if (digits.isNotEmpty()) digits = digits.dropLast(1)
                        }
                        .minimumInteractiveComponentSize()
                        .testTag("pin_backspace_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                        contentDescription = "Backspace",
                        tint = TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun PinKeyButton(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(CardDarkElevated, Color(0xFF120C24))
                )
            )
            .border(1.dp, NeonPurple.copy(alpha = 0.45f), CircleShape)
            .clickable(onClick = onClick)
            .minimumInteractiveComponentSize()
            .testTag("pin_key_$label"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = OrbitronFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = TextPrimary
        )
    }
}
