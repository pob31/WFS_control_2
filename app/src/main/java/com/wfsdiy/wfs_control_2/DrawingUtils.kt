package com.wfsdiy.wfs_control_2

import android.graphics.Paint
import android.graphics.Typeface

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import java.util.Locale
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
    currentStageOriginX: Float,
    currentStageOriginY: Float,
    canvasPixelW: Float,
    canvasPixelH: Float,
    markerRadius: Float = 0f
) {
    if (currentStageW <= 0f || currentStageD <= 0f || canvasPixelW <= 0f || canvasPixelH <= 0f) return

    val paint = Paint().apply {
        color = android.graphics.Color.GRAY
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        textSize = if (canvasPixelH > 0f) canvasPixelH / 45f else 20f // Default if canvasPixelH is 0
    }
    val padding = if (canvasPixelH > 0f) canvasPixelH / 60f else 10f

    // Adjust canvas boundaries to account for marker radius
    val effectiveCanvasWidth = canvasPixelW - (markerRadius * 2f)
    val effectiveCanvasHeight = canvasPixelH - (markerRadius * 2f)

    // Calculate corner coordinates based on stage origin
    val bottomLeftX = if (currentStageOriginX == 0f) 0f else -1*currentStageOriginX
    val bottomLeftY = if (currentStageOriginY == 0f) 0f else -1*currentStageOriginY
    val bottomRightX = currentStageW - currentStageOriginX
    val bottomRightY = if (currentStageOriginY == 0f) 0f else -1*currentStageOriginY
    val topLeftX = if (currentStageOriginX == 0f) 0f else -1*currentStageOriginX
    val topLeftY = currentStageD - currentStageOriginY
    val topRightX = currentStageW - currentStageOriginX
    val topRightY = currentStageD - currentStageOriginY

    val pointsToDraw = listOf(
        StagePointInfo(bottomLeftX, bottomLeftY, Paint.Align.LEFT, false), // Bottom-Left
        StagePointInfo(0f, bottomLeftY, Paint.Align.CENTER, false),        // Bottom-Center
        StagePointInfo(bottomRightX, bottomRightY, Paint.Align.RIGHT, false), // Bottom-Right
        StagePointInfo(topLeftX, topLeftY, Paint.Align.LEFT, true),         // Top-Left
        StagePointInfo(0f, topLeftY, Paint.Align.CENTER, true),            // Top-Center
        StagePointInfo(topRightX, topRightY, Paint.Align.RIGHT, true)      // Top-Right
    )

    pointsToDraw.forEach { point ->
        // Display the stage coordinates based on origin
        val labelText = "(${String.format(Locale.US, "%.1f", point.stageX)}, ${String.format(Locale.US, "%.1f", point.stageY)})"
        paint.textAlign = point.align

        // Fixed canvas positions - corners and center of top/bottom edges
        val canvasX = when (point.align) {
            Paint.Align.LEFT -> markerRadius + padding
            Paint.Align.RIGHT -> effectiveCanvasWidth + markerRadius - padding
            else -> (effectiveCanvasWidth / 2f) + markerRadius // CENTER
        }
        
        val canvasY = if (point.isTopAnchor) {
            markerRadius + padding + abs(paint.fontMetrics.ascent)
        } else {
            effectiveCanvasHeight + markerRadius - padding
        }

        drawContext.canvas.nativeCanvas.drawText(labelText, canvasX, canvasY, paint)
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
    currentStageOriginX: Float,
    currentStageOriginY: Float,
    canvasPixelW: Float,
    canvasPixelH: Float,
    isTablet: Boolean = false
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
    val baseTextSize = referenceDimension / (if (isClusterMarker) 40f else 45f) // Larger text (was 45f/52.5f)
    val dynamicBaseTextSizePx = if (isTablet) baseTextSize * 0.9f else baseTextSize // 10% smaller on tablets

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

    if (isBeingDragged && canvasPixelW > 0f && canvasPixelH > 0f) {
        val currentPixelX = position.x
        val currentPixelY = position.y

        // Convert to 0.0-1.0 normalized coordinates accounting for marker radius
        val markerRadius = CanvasDimensions.getCurrentMarkerRadius()
        val effectiveWidth = canvasPixelW - (markerRadius * 2f)
        val effectiveHeight = canvasPixelH - (markerRadius * 2f)
        val normalizedX = if (effectiveWidth > 0f) (currentPixelX - markerRadius) / effectiveWidth else 0f
        val normalizedY = if (effectiveHeight > 0f) 1f - ((currentPixelY - markerRadius) / effectiveHeight) else 0f

        // Convert to stage coordinates for display
        val displayX = normalizedX * currentStageW
        val displayY = normalizedY * currentStageD

        // Adjust coordinates by subtracting stage origin
        val adjustedX = displayX - currentStageOriginX
        val adjustedY = displayY - currentStageOriginY

        val coordText = " (${String.format(Locale.US, "%.1f", adjustedX)}, ${String.format(Locale.US, "%.1f", adjustedY)})"

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

fun DrawScope.drawOriginMarker(
    currentStageW: Float,
    currentStageD: Float,
    currentStageOriginX: Float,
    currentStageOriginY: Float,
    canvasPixelW: Float,
    canvasPixelH: Float,
    markerRadius: Float = 0f
) {
    if (currentStageW <= 0f || currentStageD <= 0f || canvasPixelW <= 0f || canvasPixelH <= 0f) return

    // Adjust canvas boundaries to account for marker radius
    val effectiveCanvasWidth = canvasPixelW - (markerRadius * 2f)
    val effectiveCanvasHeight = canvasPixelH - (markerRadius * 2f)

    // Find where a marker would be positioned to have displayed coordinates (0.0, 0.0)
    // displayedX = displayX - stageOriginX = 0.0 → displayX = stageOriginX
    // displayedY = displayY - stageOriginY = 0.0 → displayY = stageOriginY
    
    // displayX = normalizedX * currentStageW = currentStageOriginX
    // displayY = normalizedY * currentStageD = currentStageOriginY
    
    val normalizedX = currentStageOriginX / currentStageW
    val normalizedY = currentStageOriginY / currentStageD
    
    val canvasX = normalizedX * effectiveCanvasWidth + markerRadius
    val canvasY = (1f - normalizedY) * effectiveCanvasHeight + markerRadius

    // Draw origin marker: circle with crosshairs
    val originRadius = 15f
    val crosshairLength = 20f
    
    // Draw circle
    drawCircle(
        color = Color.White,
        radius = originRadius,
        center = Offset(canvasX, canvasY)
    )
    
    // Draw crosshairs
    drawLine(
        color = Color.White,
        start = Offset(canvasX - crosshairLength, canvasY),
        end = Offset(canvasX + crosshairLength, canvasY),
        strokeWidth = 2f
    )
    drawLine(
        color = Color.White,
        start = Offset(canvasX, canvasY - crosshairLength),
        end = Offset(canvasX, canvasY + crosshairLength),
        strokeWidth = 2f
    )
}
