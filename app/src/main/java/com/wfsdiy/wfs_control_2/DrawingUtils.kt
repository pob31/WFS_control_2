package com.wfsdiy.wfs_control_2

import android.graphics.Paint
import android.graphics.Typeface
import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

// Function to generate distinct colors for markers
fun getMarkerColor(id: Int, isClusterMarker: Boolean = false): Color {
    val totalMarkers = if (isClusterMarker) 10 else 32
    val hue = (id * 360f / totalMarkers) % 360f
    return Color.hsl(hue, if (isClusterMarker) 0.7f else 0.9f, if (isClusterMarker) 0.7f else 0.6f)
}

// Data class for Stage Coordinate drawing information
internal data class StagePointInfo(
    val stageX: Float,
    val stageY: Float,
    val align: Paint.Align,
    val isTopAnchor: Boolean // True if the label is for a top coordinate (TL, TC, TR)
)

// Function to draw stage coordinate labels on the canvas
fun DrawScope.drawStageCornerLabels(
    currentStageW: Float,
    currentStageD: Float,
    canvasPixelW: Float,
    canvasPixelH: Float
) {
    if (currentStageW <= 0f || currentStageD <= 0f || canvasPixelW <= 0f || canvasPixelH <= 0f) return

    val paint = Paint().apply {
        color = android.graphics.Color.GRAY
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        textSize = if (canvasPixelH > 0f) canvasPixelH / 45f else 20f // Default if canvasPixelH is 0
    }
    val padding = if (canvasPixelH > 0f) canvasPixelH / 60f else 10f

    val pointsToDraw = listOf(
        StagePointInfo(-currentStageW / 2f, 0f, Paint.Align.LEFT, false), // Bottom-Left
        StagePointInfo(0f, 0f, Paint.Align.CENTER, false),                  // Bottom-Center
        StagePointInfo(currentStageW / 2f, 0f, Paint.Align.RIGHT, false),  // Bottom-Right
        StagePointInfo(-currentStageW / 2f, currentStageD, Paint.Align.LEFT, true), // Top-Left
        StagePointInfo(0f, currentStageD, Paint.Align.CENTER, true),       // Top-Center
        StagePointInfo(currentStageW / 2f, currentStageD, Paint.Align.RIGHT, true) // Top-Right
    )

    pointsToDraw.forEach { point ->
        val labelText = "(${String.format("%.1f", point.stageX)}, ${String.format("%.1f", point.stageY)})"
        paint.textAlign = point.align

        val canvasX = ((point.stageX + currentStageW / 2f) / currentStageW) * canvasPixelW
        val canvasY = ((currentStageD - point.stageY) / currentStageD) * canvasPixelH // Y is inverted

        val finalDrawX = when (point.align) {
            Paint.Align.LEFT -> canvasX + padding
            Paint.Align.RIGHT -> canvasX - padding
            else -> canvasX // CENTER
        }

        val finalDrawY = if (point.isTopAnchor) {
            canvasY + padding + abs(paint.fontMetrics.ascent)
        } else {
            canvasY - padding
        }

        drawContext.canvas.nativeCanvas.drawText(labelText, finalDrawX, finalDrawY, paint)
    }
}

internal fun distance(p1: Offset, p2: Offset): Float {
    return sqrt((p1.x - p2.x).pow(2) + (p1.y - p2.y).pow(2))
}

fun <T> DrawScope.drawMarker(
    markerInstance: T,
    isBeingDragged: Boolean,
    textPaint: Paint,
    isClusterMarker: Boolean,
    currentStageW: Float,
    currentStageD: Float,
    canvasPixelW: Float,
    canvasPixelH: Float
) where T : Any {
    val id: Int
    val position: Offset
    val radius: Float
    var markerName: String = ""
    var markerIsLocked: Boolean = false
    var markerIsVisible: Boolean = true

    when (markerInstance) {
        is Marker -> {
            id = markerInstance.id
            position = markerInstance.position
            radius = markerInstance.radius
            markerName = markerInstance.name
            markerIsLocked = markerInstance.isLocked
            markerIsVisible = markerInstance.isVisible
        }
        is ClusterMarker -> {
            id = markerInstance.id
            position = markerInstance.position
            radius = markerInstance.radius
        }
        else -> {
            Log.e("DrawMarker", "Unsupported marker type: ${markerInstance::class.java.name}")
            return
        }
    }

    if (!isClusterMarker && !markerIsVisible) return

    val baseColor = getMarkerColor(id, isClusterMarker)
    val finalOuterColor: Color
    val labelColor: Int

    if (!isClusterMarker && markerIsLocked) {
        finalOuterColor = Color.LightGray
        labelColor = Color.Red.toArgb()
    } else {
        finalOuterColor = if (isBeingDragged) Color.White else baseColor
        labelColor = android.graphics.Color.WHITE
    }

    val innerRadius = radius * 0.6f
    drawCircle(color = finalOuterColor, radius = radius, center = position)
    drawCircle(color = Color.Black, radius = innerRadius, center = position)

    val referenceDimension = min(size.width, size.height)
    val dynamicBaseTextSizePx = referenceDimension / (if (isClusterMarker) 45f else 52.5f)

    textPaint.color = labelColor
    val idText = id.toString()

    if (!isClusterMarker && markerName.isNotBlank()) {
        textPaint.textSize = dynamicBaseTextSizePx * 0.9f
        val idTextMetrics = textPaint.fontMetrics
        val idTextY = position.y - (dynamicBaseTextSizePx * 0.35f) - (idTextMetrics.ascent + idTextMetrics.descent) / 2f
        drawContext.canvas.nativeCanvas.drawText(idText, position.x, idTextY, textPaint)

        textPaint.textSize = dynamicBaseTextSizePx * 0.7f
        val nameTextMetrics = textPaint.fontMetrics
        val nameTextY = position.y + (dynamicBaseTextSizePx * 0.45f) - (nameTextMetrics.ascent + nameTextMetrics.descent) / 2f
        drawContext.canvas.nativeCanvas.drawText(markerName, position.x, nameTextY, textPaint)
    } else {
        textPaint.textSize = dynamicBaseTextSizePx
        val idTextMetrics = textPaint.fontMetrics
        val idTextY = position.y - (idTextMetrics.ascent + idTextMetrics.descent) / 2f
        drawContext.canvas.nativeCanvas.drawText(idText, position.x, idTextY, textPaint)
    }

    if (isBeingDragged && canvasPixelW > 0f && canvasPixelH > 0f && currentStageW > 0f && currentStageD > 0f) {
        val currentPixelX = position.x
        val currentPixelY = position.y

        val stageX = ((currentPixelX / canvasPixelW) * currentStageW) - (currentStageW / 2f)
        val stageY = currentStageD - ((currentPixelY / canvasPixelH) * currentStageD)

        val coordText = " (${String.format("%.1f", stageX)}, ${String.format("%.1f", stageY)})"

        val originalTextSize = textPaint.textSize
        val originalTextAlign = textPaint.textAlign
        val originalTextColor = textPaint.color

        textPaint.textSize = dynamicBaseTextSizePx * 0.7f
        textPaint.color = android.graphics.Color.YELLOW

        val coordTextPadding = 10f
        val coordTextX: Float

        if (position.x < canvasPixelW / 2f) {
            textPaint.textAlign = Paint.Align.LEFT
            coordTextX = position.x + radius + coordTextPadding
        } else {
            textPaint.textAlign = Paint.Align.RIGHT
            coordTextX = position.x - radius - coordTextPadding
        }

        val coordTextMetrics = textPaint.fontMetrics
        val coordTextY = position.y - (coordTextMetrics.ascent + coordTextMetrics.descent) / 2f
        
        drawContext.canvas.nativeCanvas.drawText(coordText, coordTextX, coordTextY, textPaint)

        textPaint.textSize = originalTextSize
        textPaint.textAlign = originalTextAlign
        textPaint.color = originalTextColor
    }
}
