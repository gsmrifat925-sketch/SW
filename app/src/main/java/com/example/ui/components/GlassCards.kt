package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.ChartPoint
import com.example.i18n.LocalStrings
import com.example.i18n.formatTaka
import com.example.ui.theme.BrightViolet
import com.example.ui.theme.CardDarkElevated
import com.example.ui.theme.CardDarkSurface
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonRed
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.PureBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun NeonGlassCard(
    modifier: Modifier = Modifier,
    glowColor: Color = NeonPurple,
    containerBrush: Brush = Brush.linearGradient(
        colors = listOf(CardDarkElevated.copy(alpha = 0.92f), CardDarkSurface.copy(alpha = 0.96f))
    ),
    cornerRadius: Dp = 20.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val baseModifier = modifier
        .shadow(
            elevation = 10.dp,
            shape = shape,
            ambientColor = glowColor.copy(alpha = 0.35f),
            spotColor = glowColor.copy(alpha = 0.55f)
        )
        .clip(shape)
        .background(containerBrush)
        .border(
            width = 1.15.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    glowColor.copy(alpha = 0.85f),
                    glowColor.copy(alpha = 0.28f),
                    glowColor.copy(alpha = 0.65f)
                )
            ),
            shape = shape
        )
        .let { m ->
            if (onClick != null) m.clickable(onClick = onClick) else m
        }

    Box(modifier = baseModifier, content = content)
}

@Composable
fun FinancialMetricCard(
    title: String,
    amount: Double,
    icon: ImageVector,
    accentColor: Color,
    secondaryGlow: Color,
    cardGradientColors: List<Color>,
    useChevronArrow: Boolean = false,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    NeonGlassCard(
        modifier = modifier
            .testTag(testTag)
            .minimumInteractiveComponentSize(),
        glowColor = accentColor,
        containerBrush = Brush.linearGradient(colors = cardGradientColors),
        cornerRadius = 18.dp,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(accentColor.copy(alpha = 0.22f))
                        .border(1.dp, accentColor.copy(alpha = 0.55f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Icon(
                    imageVector = if (useChevronArrow) Icons.AutoMirrored.Filled.KeyboardArrowRight else Icons.AutoMirrored.Filled.CallMade,
                    contentDescription = null,
                    tint = secondaryGlow,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = formatTaka(amount),
                fontFamily = OrbitronFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = secondaryGlow,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun NeonFilterPill(
    label: String,
    selected: Boolean,
    icon: ImageVector? = null,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(50)
    val bgBrush = if (selected) {
        Brush.horizontalGradient(listOf(ElectricViolet, NeonPurple))
    } else {
        Brush.horizontalGradient(listOf(CardDarkElevated.copy(alpha = 0.85f), CardDarkSurface))
    }
    val borderColor = if (selected) BrightViolet else NeonPurple.copy(alpha = 0.28f)

    Row(
        modifier = modifier
            .shadow(
                elevation = if (selected) 10.dp else 0.dp,
                shape = shape,
                ambientColor = NeonPurple,
                spotColor = NeonPurple
            )
            .clip(shape)
            .background(bgBrush)
            .border(1.dp, borderColor, shape)
            .clickable(onClick = onClick)
            .minimumInteractiveComponentSize()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) TextPrimary else TextSecondary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) TextPrimary else TextSecondary,
            maxLines = 1
        )
    }
}

@Composable
fun NeonPrimaryButton(
    text: String,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .shadow(
                elevation = 16.dp,
                shape = shape,
                ambientColor = NeonPurple,
                spotColor = NeonPurple
            )
            .clip(shape)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Color(0xFF7B2CBF), ElectricViolet, NeonPurple)
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.horizontalGradient(listOf(BrightViolet, TextPrimary.copy(alpha = 0.55f), BrightViolet)),
                shape = shape
            )
            .clickable(onClick = onClick)
            .minimumInteractiveComponentSize()
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontSize = 15.sp,
            color = TextPrimary,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun ProfitLossLineChart(
    points: List<ChartPoint>,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    var selectedIdx by remember(points) { mutableIntStateOf(-1) }

    if (points.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(170.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = strings.noChartData,
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
            )
        }
        return
    }

    val maxVal = remember(points) {
        val peak = points.maxOfOrNull { max(it.profit, it.loss) } ?: 1000.0
        max(peak * 1.18, 500.0)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        if (selectedIdx in points.indices) {
            val pt = points[selectedIdx]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(PureBlack.copy(alpha = 0.65f))
                    .border(1.dp, NeonPurple.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = pt.fullDateLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${strings.profitLabel}: ${formatTaka(pt.profit)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonGreen
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${strings.lossLabel}: ${formatTaka(pt.loss)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonRed
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(175.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Y-axis scale labels
            Column(
                modifier = Modifier
                    .height(145.dp)
                    .padding(end = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                listOf(1.0, 0.66, 0.33, 0.0).forEach { ratio ->
                    val valAtTick = maxVal * ratio
                    val label = when {
                        valAtTick >= 1000 -> "${(valAtTick / 1000.0).let { if (it % 1.0 < 0.1) it.toInt().toString() else String.format("%.1f", it) }}k"
                        else -> valAtTick.roundToInt().toString()
                    }
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(145.dp)
                        .pointerInput(points) {
                            detectTapGestures { offset ->
                                if (points.size == 1) {
                                    selectedIdx = 0
                                } else {
                                    val stepX = size.width.toFloat() / (points.size - 1).coerceAtLeast(1)
                                    val idx = (offset.x / stepX).roundToInt().coerceIn(0, points.lastIndex)
                                    selectedIdx = if (selectedIdx == idx) -1 else idx
                                }
                            }
                        }
                ) {
                    val w = size.width
                    val h = size.height
                    val padV = 10.dp.toPx()
                    val usableH = (h - padV * 2f).coerceAtLeast(10f)

                    // Horizontal grid lines
                    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                    for (i in 0..3) {
                        val y = padV + usableH * (i / 3f)
                        drawLine(
                            color = TextMuted.copy(alpha = 0.25f),
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = dashEffect
                        )
                    }

                    fun buildOffsets(values: List<Double>): List<Offset> {
                        if (values.size == 1) {
                            val y = padV + usableH * (1f - (values[0] / maxVal).toFloat().coerceIn(0f, 1f))
                            return listOf(Offset(w * 0.15f, y), Offset(w * 0.85f, y))
                        }
                        val stepX = w / (values.size - 1).coerceAtLeast(1)
                        return values.mapIndexed { idx, v ->
                            val x = idx * stepX
                            val y = padV + usableH * (1f - (v / maxVal).toFloat().coerceIn(0f, 1f))
                            Offset(x, y)
                        }
                    }

                    fun buildSmoothPath(pts: List<Offset>): Path {
                        val path = Path()
                        if (pts.isEmpty()) return path
                        path.moveTo(pts.first().x, pts.first().y)
                        for (i in 0 until pts.size - 1) {
                            val p0 = pts[i]
                            val p1 = pts[i + 1]
                            val midX = (p0.x + p1.x) / 2f
                            path.cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
                        }
                        return path
                    }

                    fun drawSeries(values: List<Double>, lineColor: Color) {
                        val pts = buildOffsets(values)
                        val linePath = buildSmoothPath(pts)
                        val fillPath = Path().apply {
                            addPath(linePath)
                            lineTo(pts.last().x, h - padV)
                            lineTo(pts.first().x, h - padV)
                            close()
                        }

                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(lineColor.copy(alpha = 0.26f), Color.Transparent),
                                startY = padV,
                                endY = h - padV
                            )
                        )
                        // Outer soft glow stroke
                        drawPath(
                            path = linePath,
                            color = lineColor.copy(alpha = 0.28f),
                            style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
                        )
                        // Crisp foreground neon line
                        drawPath(
                            path = linePath,
                            color = lineColor,
                            style = Stroke(width = 2.6.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Glowing node dots
                        val displayPts = if (values.size == 1) listOf(pts.first()) else pts
                        displayPts.forEachIndexed { idx, offset ->
                            val isSelected = idx == selectedIdx
                            drawCircle(
                                color = lineColor.copy(alpha = 0.35f),
                                radius = if (isSelected) 9.dp.toPx() else 5.5.dp.toPx(),
                                center = offset
                            )
                            drawCircle(
                                color = TextPrimary,
                                radius = if (isSelected) 4.dp.toPx() else 2.6.dp.toPx(),
                                center = offset
                            )
                        }
                    }

                    // Draw Loss first, then Profit on top
                    drawSeries(points.map { it.loss }, NeonRed)
                    drawSeries(points.map { it.profit }, NeonGreen)
                }

                // X-axis tick labels (up to 6 evenly spaced labels)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val labels = if (points.size <= 6) {
                        points.map { it.label }
                    } else {
                        val step = (points.size - 1) / 5.0
                        (0..5).map { i -> points[(i * step).roundToInt().coerceIn(0, points.lastIndex)].label }
                    }
                    labels.forEach { lbl ->
                        Text(
                            text = lbl,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }
            }
        }
    }
}
