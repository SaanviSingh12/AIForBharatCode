package com.sahayak.android.ui.audio

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A horizontal waveform bar visualiser.
 *
 * @param amplitudes  List of normalised amplitudes (0 f … 1 f).  The most
 *                    recent value is drawn on the right edge.
 * @param barColor    Colour of each bar.
 * @param barWidth    Width of one bar.
 * @param barSpacing  Gap between bars.
 * @param minBarHeight Minimum height so silent bars are still visible.
 * @param modifier    Standard Compose modifier.
 */
@Composable
fun WaveformVisualizer(
    amplitudes: List<Float>,
    modifier: Modifier = Modifier,
    barColor: Color = Color(0xFFEF5350),   // matches EmergencyRed
    barWidth: Dp = 4.dp,
    barSpacing: Dp = 2.dp,
    minBarHeight: Dp = 3.dp,
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp),
    ) {
        val barWidthPx = barWidth.toPx()
        val barSpacingPx = barSpacing.toPx()
        val minBarHeightPx = minBarHeight.toPx()
        val totalBarWidth = barWidthPx + barSpacingPx
        val maxBars = (size.width / totalBarWidth).toInt()
        val cornerRadius = CornerRadius(barWidthPx / 2f, barWidthPx / 2f)

        // Take only the last `maxBars` amplitudes so the waveform scrolls
        val visibleAmplitudes = if (amplitudes.size > maxBars)
            amplitudes.takeLast(maxBars)
        else
            amplitudes

        visibleAmplitudes.forEachIndexed { index, amplitude ->
            val barHeight = (amplitude * size.height)
                .coerceAtLeast(minBarHeightPx)
                .coerceAtMost(size.height)
            val x = index * totalBarWidth
            val y = (size.height - barHeight) / 2f   // centre vertically

            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, y),
                size = Size(barWidthPx, barHeight),
                cornerRadius = cornerRadius,
            )
        }
    }
}
