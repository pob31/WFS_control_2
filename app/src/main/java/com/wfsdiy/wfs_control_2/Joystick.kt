package com.wfsdiy.wfs_control_2

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
// import androidx.compose.foundation.layout.aspectRatio // Not strictly needed for this version
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun Joystick(
    modifier: Modifier = Modifier,
    joystickSize: Dp = 150.dp,
    outerCircleColor: Color = Color.LightGray,
    innerThumbColor: Color = Color.DarkGray,
    onPositionChanged: (x: Float, y: Float) -> Unit
) {
    var thumbOffset by remember { mutableStateOf(Offset.Zero) } // Offset of the thumb's center from the Joystick Box's center
    var joystickOutput by remember { mutableStateOf(Pair(0f, 0f)) }

    val density = LocalDensity.current
    val outerRadiusPx = remember(joystickSize, density) { with(density) { (joystickSize / 2).toPx() } }
    val thumbRadiusPx = remember(joystickSize, density) { with(density) { (joystickSize / 6).toPx() } } 
    // The maximum distance the center of the thumb can move from the center of the joystick
    val maxThumbDragRadius = remember(outerRadiusPx, thumbRadiusPx) { outerRadiusPx - thumbRadiusPx }


    // LaunchedEffect to periodically report the joystick position
    val currentOnPositionChanged by rememberUpdatedState(onPositionChanged)

    LaunchedEffect(Unit) { // Runs once and loops, ensuring it uses the latest onPositionChanged
        while (true) {
            currentOnPositionChanged(joystickOutput.first, joystickOutput.second)
            delay(100) // Report every 0.1 seconds
        }
    }

    Box(
        modifier = modifier
            .size(joystickSize)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { /* Can be used if needed, e.g., for haptic feedback */ },
                    onDragEnd = {
                        thumbOffset = Offset.Zero
                        joystickOutput = Pair(0f, 0f)
                    },
                    onDragCancel = {
                        thumbOffset = Offset.Zero
                        joystickOutput = Pair(0f, 0f)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume() // Consume the event

                        // Calculate the new raw offset from the center of the Box
                        val newThumbOffset = thumbOffset + dragAmount
                        val distanceFromCenter = sqrt(newThumbOffset.x.pow(2) + newThumbOffset.y.pow(2))

                        if (distanceFromCenter <= maxThumbDragRadius) {
                            // Thumb is within the draggable area
                            thumbOffset = newThumbOffset
                        } else {
                            // Thumb is trying to go outside, constrain it to the edge
                            val angle = atan2(newThumbOffset.y, newThumbOffset.x)
                            thumbOffset = Offset(
                                x = maxThumbDragRadius * cos(angle),
                                y = maxThumbDragRadius * sin(angle)
                            )
                        }

                        // Normalize and update joystickOutput
                        if (maxThumbDragRadius > 0f) { // Avoid division by zero if radii are equal
                            val normalizedX = thumbOffset.x / maxThumbDragRadius
                            val normalizedY = -thumbOffset.y / maxThumbDragRadius // Invert Y for standard joystick coordinates (up is positive Y)
                            joystickOutput = Pair(
                                normalizedX.coerceIn(-1f, 1f),
                                normalizedY.coerceIn(-1f, 1f)
                            )
                        } else {
                            joystickOutput = Pair(0f, 0f)
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasCenter = Offset(size.width / 2f, size.height / 2f)
            drawOuterCircle(this, outerCircleColor, canvasCenter, outerRadiusPx)
            drawInnerThumb(this, innerThumbColor, canvasCenter, thumbOffset, thumbRadiusPx)
        }
    }
}

private fun drawOuterCircle(drawScope: DrawScope, color: Color, center: Offset, radius: Float) {
    drawScope.drawCircle(
        color = color,
        radius = radius,
        center = center
    )
}

private fun drawInnerThumb(drawScope: DrawScope, color: Color, joystickCenter: Offset, thumbLogicalOffset: Offset, radius: Float) {
    drawScope.drawCircle(
        color = color,
        radius = radius,
        center = joystickCenter + thumbLogicalOffset
    )
}
