package com.wfsdiy.wfs_control_2

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt

enum class SliderOrientation {
    HORIZONTAL,
    VERTICAL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BidirectionalSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    orientation: SliderOrientation = SliderOrientation.HORIZONTAL,
    sliderColor: Color = Color.Blue,
    trackBackgroundColor: Color = sliderColor.copy(alpha = 0.24f),
    valueRange: ClosedFloatingPointRange<Float> = -1f..1f,
    thumbSize: DpSize = DpSize(20.dp, 20.dp),
    trackThickness: Dp = 4.dp,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    onValueChangeFinished: (() -> Unit)? = null,
    enabled: Boolean = true
) {
    val baseModifier = if (orientation == SliderOrientation.VERTICAL) {
        modifier
            .graphicsLayer { rotationZ = -90f }
            .layout { measurable, constraints ->
                val placeable = measurable.measure(
                    constraints.copy(
                        minWidth = constraints.minHeight,
                        maxWidth = constraints.maxHeight,
                        minHeight = constraints.minWidth,
                        maxHeight = constraints.maxWidth
                    )
                )
                layout(placeable.width, placeable.height) {
                    placeable.placeRelative(0, 0)
                }
            }
    } else {
        modifier
    }

    Slider(
        value = value,
        onValueChange = onValueChange,
        modifier = baseModifier,
        valueRange = valueRange,
        interactionSource = interactionSource,
        onValueChangeFinished = onValueChangeFinished,
        enabled = enabled,
        colors = SliderDefaults.colors(
            thumbColor = sliderColor,
            activeTrackColor = Color.Transparent,
            inactiveTrackColor = Color.Transparent,
            disabledThumbColor = sliderColor.copy(alpha = ContentAlpha.disabled),
            disabledActiveTrackColor = Color.Transparent,
            disabledInactiveTrackColor = Color.Transparent
        ),
        thumb = {
            SliderDefaults.Thumb(
                interactionSource = interactionSource,
                colors = SliderDefaults.colors(
                    thumbColor = sliderColor,
                    disabledThumbColor = sliderColor.copy(alpha = ContentAlpha.disabled)
                ),
                thumbSize = thumbSize,
                enabled = enabled
            )
        },
        track = { sliderState ->
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize() // Fill the space Slider provides for the track
                    .height(trackThickness) // Track is always drawn as a horizontal strip
            ) {
                val valueRangeStart = sliderState.valueRange.start
                val valueRangeEnd = sliderState.valueRange.endInclusive
                val totalValueSpan = valueRangeEnd - valueRangeStart

                val normalizedCurrentValue = (sliderState.value - valueRangeStart) / totalValueSpan
                val normalizedZeroPoint = (0f - valueRangeStart) / totalValueSpan

                val activeTrackStartFraction = min(normalizedCurrentValue, normalizedZeroPoint)
                val activeTrackWidthFraction = abs(normalizedCurrentValue - normalizedZeroPoint)

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(if (enabled) trackBackgroundColor else trackBackgroundColor.copy(alpha = ContentAlpha.disabled))
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(maxWidth * activeTrackWidthFraction)
                        .offset(x = maxWidth * activeTrackStartFraction)
                        .background(if (enabled) sliderColor else sliderColor.copy(alpha = ContentAlpha.disabled))
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoCenterBidirectionalSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    orientation: SliderOrientation = SliderOrientation.HORIZONTAL,
    sliderColor: Color = Color.Magenta,
    trackBackgroundColor: Color = sliderColor.copy(alpha = 0.24f),
    valueRange: ClosedFloatingPointRange<Float> = -1f..1f,
    centerValue: Float = 0f,
    thumbSize: DpSize = DpSize(20.dp, 20.dp),
    trackThickness: Dp = 4.dp,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    onActualValueChangeFinished: (() -> Unit)? = null,
    enabled: Boolean = true
) {
    val baseModifier = if (orientation == SliderOrientation.VERTICAL) {
        modifier
            .graphicsLayer { rotationZ = -90f }
            .layout { measurable, constraints ->
                val placeable = measurable.measure(
                    constraints.copy(
                        minWidth = constraints.minHeight,
                        maxWidth = constraints.maxHeight,
                        minHeight = constraints.minWidth,
                        maxHeight = constraints.maxWidth
                    )
                )
                layout(placeable.width, placeable.height) {
                    placeable.placeRelative(0, 0)
                }
            }
    } else {
        modifier
    }

    Slider(
        value = value,
        onValueChange = onValueChange,
        modifier = baseModifier,
        valueRange = valueRange,
        interactionSource = interactionSource,
        onValueChangeFinished = {
            onValueChange(centerValue)
            onActualValueChangeFinished?.invoke()
        },
        enabled = enabled,
        colors = SliderDefaults.colors(
            thumbColor = sliderColor,
            activeTrackColor = Color.Transparent,
            inactiveTrackColor = Color.Transparent,
            disabledThumbColor = sliderColor.copy(alpha = ContentAlpha.disabled),
            disabledActiveTrackColor = Color.Transparent,
            disabledInactiveTrackColor = Color.Transparent
        ),
        thumb = {
            SliderDefaults.Thumb(
                interactionSource = interactionSource,
                colors = SliderDefaults.colors(
                    thumbColor = sliderColor,
                    disabledThumbColor = sliderColor.copy(alpha = ContentAlpha.disabled)
                ),
                thumbSize = thumbSize,
                enabled = enabled
            )
        },
        track = { sliderState ->
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .height(trackThickness)
            ) {
                val valueRangeStart = sliderState.valueRange.start
                val valueRangeEnd = sliderState.valueRange.endInclusive
                val totalValueSpan = valueRangeEnd - valueRangeStart

                val normalizedCurrentValue = (sliderState.value - valueRangeStart) / totalValueSpan
                val normalizedVisualCenter = (centerValue.coerceIn(valueRangeStart, valueRangeEnd) - valueRangeStart) / totalValueSpan

                val activeTrackStartFraction = min(normalizedCurrentValue, normalizedVisualCenter)
                val activeTrackWidthFraction = abs(normalizedCurrentValue - normalizedVisualCenter)

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(if (enabled) trackBackgroundColor else trackBackgroundColor.copy(alpha = ContentAlpha.disabled))
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(maxWidth * activeTrackWidthFraction)
                        .offset(x = maxWidth * activeTrackStartFraction)
                        .background(if (enabled) sliderColor else sliderColor.copy(alpha = ContentAlpha.disabled))
                )
            }
        }
    )
}

@Composable
fun WidthExpansionSlider(
    value: Float, // Expected to be 0f to 1f, represents the proportion of expansion
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    orientation: SliderOrientation = SliderOrientation.HORIZONTAL,
    sliderColor: Color = Color.Cyan,
    trackBackgroundColor: Color = sliderColor.copy(alpha = 0.24f),
    thumbSizeDp: Dp = 20.dp, // Diameter of the thumb
    trackThickness: Dp = 4.dp,
    onValueChangeFinished: (() -> Unit)? = null,
    enabled: Boolean = true,
    hideThumbOnRelease: Boolean = false
) {
    var isBeingDragged by remember { mutableStateOf(false) }
    var thumbVisualOffsetRatio by remember { mutableStateOf(0f) } // -1f to 1f from center

    LaunchedEffect(value, isBeingDragged, hideThumbOnRelease) {
        if (!isBeingDragged && !hideThumbOnRelease) {
            thumbVisualOffsetRatio = value
        }
    }

    val density = LocalDensity.current

    val baseModifier = if (orientation == SliderOrientation.VERTICAL) {
        modifier // The external modifier already has the desired size (e.g., width 50dp, height 200dp)
            .graphicsLayer { rotationZ = -90f }
            .layout { measurable, constraints ->
                val placeable = measurable.measure(
                    constraints.copy(
                        minWidth = constraints.minHeight,   // e.g., 200dp becomes minWidth for measurement
                        maxWidth = constraints.maxHeight,  // e.g., 200dp becomes maxWidth for measurement
                        minHeight = constraints.minWidth,  // e.g., 50dp becomes minHeight for measurement
                        maxHeight = constraints.maxWidth   // e.g., 50dp becomes maxHeight for measurement
                    )
                )
                // Layout with unrotated dimensions: placeable.width is length, placeable.height is thickness
                layout(placeable.width, placeable.height) {
                    placeable.placeRelative(0, 0)
                }
            }
    } else {
        modifier // For horizontal, the modifier directly applies (e.g., fillMaxWidth, height 50dp)
    }

    // Apply pointerInput to the baseModifier before passing to BoxWithConstraints
    val finalModifierForBox = baseModifier
        .pointerInput(enabled, orientation, thumbSizeDp) { // Dependencies
            if (!enabled) return@pointerInput

            val viewLengthPx = if (orientation == SliderOrientation.HORIZONTAL) this.size.width.toFloat() else this.size.height.toFloat()

            detectDragGestures(
                onDragStart = { startOffset ->
                    isBeingDragged = true
                    val trackCenterPx = viewLengthPx / 2f
                    val halfTrackPx = trackCenterPx
                    if (halfTrackPx <= 0f) return@detectDragGestures

                    val currentPointerPos = if (orientation == SliderOrientation.HORIZONTAL) startOffset.x else startOffset.y
                    val displacementPx = currentPointerPos - trackCenterPx

                    thumbVisualOffsetRatio = (displacementPx / halfTrackPx).coerceIn(-1f, 1f)
                    onValueChange(abs(thumbVisualOffsetRatio))
                },
                onDrag = { change, _ ->
                    change.consume()
                    val trackCenterPx = viewLengthPx / 2f
                    val halfTrackPx = trackCenterPx
                    if (halfTrackPx <= 0f) return@detectDragGestures

                    val currentPointerPos = if (orientation == SliderOrientation.HORIZONTAL) change.position.x else change.position.y
                    val displacementPx = currentPointerPos - trackCenterPx

                    thumbVisualOffsetRatio = (displacementPx / halfTrackPx).coerceIn(-1f, 1f)
                    onValueChange(abs(thumbVisualOffsetRatio))
                },
                onDragEnd = {
                    isBeingDragged = false
                    if (!hideThumbOnRelease) thumbVisualOffsetRatio = value
                    onValueChangeFinished?.invoke()
                },
                onDragCancel = {
                    isBeingDragged = false
                    if (!hideThumbOnRelease) thumbVisualOffsetRatio = value
                    onValueChangeFinished?.invoke()
                }
            )
        }

    BoxWithConstraints(
        modifier = finalModifierForBox // This modifier now has the full size from the caller
    ) {
        // Inside BoxWithConstraints, maxWidth is always the length, maxHeight is always the thickness
        // (due to the custom layout modifier for VERTICAL correctly setting up unrotated dimensions)
        val actualTrackLengthPx = with(density) { maxWidth.toPx() }
        val actualThumbSizePx = with(density) { thumbSizeDp.toPx() }

        // 1. Background Track - Drawn along the length (maxWidth), with specified trackThickness (maxHeight)
        Box(
            modifier = Modifier
                .align(Alignment.Center) // Center the track within the BoxWithConstraints' full area
                .fillMaxWidth() // Fill the length
                .height(trackThickness) // Set track's thickness
                .background(if (enabled) trackBackgroundColor else trackBackgroundColor.copy(alpha = ContentAlpha.disabled))
        )

        // 2. Active Track (grows from center)
        val activeTrackVisualLengthPx = actualTrackLengthPx * value
        Box(
            modifier = Modifier
                .align(Alignment.Center) // Center the active part of the track
                .width(with(density) { activeTrackVisualLengthPx.toDp() }) // Width is based on value
                .height(trackThickness) // Thickness is fixed
                .background(if (enabled) sliderColor else sliderColor.copy(alpha = ContentAlpha.disabled))
        )

        // 3. Thumb - Positioned along the length (maxWidth), centered in the thickness (maxHeight)
        if (enabled && (isBeingDragged || !hideThumbOnRelease)) {
            val trackCenterPx = actualTrackLengthPx / 2f
            val halfTrackPxForThumb = trackCenterPx
            val thumbCenterAlongTrackPx = trackCenterPx + (thumbVisualOffsetRatio * halfTrackPxForThumb)

            // Calculate offset for the thumb's top-left corner to center it
            val thumbXOffset = thumbCenterAlongTrackPx - (actualThumbSizePx / 2f)
            // Center the thumb vertically within the full maxHeight of BoxWithConstraints
            val thumbYOffset = (this.constraints.maxHeight.toFloat() - actualThumbSizePx) / 2f

            Box(
                Modifier
                    .offset {
                        IntOffset(
                            x = thumbXOffset.roundToInt(),
                            y = thumbYOffset.roundToInt()
                        )
                    }
                    .size(thumbSizeDp)
                    .background(sliderColor, CircleShape)
            )
        }
    }
}

private object ContentAlpha {
    const val disabled: Float = 0.38f
}
