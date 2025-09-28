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
import androidx.compose.ui.platform.LocalConfiguration
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt

enum class SliderOrientation {
    HORIZONTAL,
    VERTICAL
}

@Composable
private fun getResponsiveSliderSizes(): ResponsiveSliderSizes {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    
    val screenWidthDp = configuration.screenWidthDp.dp
    val screenHeightDp = configuration.screenHeightDp.dp
    val screenDensity = density.density
    
    // Calculate responsive sizes based on screen dimensions and density
    val baseThumbSize = (16.dp * screenDensity).coerceIn(12.dp, 32.dp)
    val baseTrackThickness = (3.dp * screenDensity).coerceIn(2.dp, 8.dp)
    
    // Adjust for screen size
    val screenSizeFactor = min(screenWidthDp.value, screenHeightDp.value) / 400f // Normalize to 400dp baseline
    val adjustedThumbSize = (baseThumbSize.value * screenSizeFactor).coerceIn(12f, 32f).dp
    val adjustedTrackThickness = (baseTrackThickness.value * screenSizeFactor).coerceIn(2f, 8f).dp
    
    return ResponsiveSliderSizes(
        thumbSize = DpSize(adjustedThumbSize, adjustedThumbSize),
        trackThickness = adjustedTrackThickness
    )
}

private data class ResponsiveSliderSizes(
    val thumbSize: DpSize,
    val trackThickness: Dp
)

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
    thumbSize: DpSize? = null,
    trackThickness: Dp? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    onValueChangeFinished: (() -> Unit)? = null,
    enabled: Boolean = true
) {
    val responsiveSizes = getResponsiveSliderSizes()
    val finalThumbSize = thumbSize ?: responsiveSizes.thumbSize
    val finalTrackThickness = trackThickness ?: responsiveSizes.trackThickness
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
                thumbSize = finalThumbSize,
                enabled = enabled
            )
        },
        track = { sliderState ->
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize() // Fill the space Slider provides for the track
                    .height(4.dp) // Track is always drawn as a horizontal strip
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
    thumbSize: DpSize? = null,
    trackThickness: Dp? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    onActualValueChangeFinished: (() -> Unit)? = null,
    enabled: Boolean = true
) {
    val responsiveSizes = getResponsiveSliderSizes()
    val finalThumbSize = thumbSize ?: responsiveSizes.thumbSize
    val finalTrackThickness = trackThickness ?: responsiveSizes.trackThickness
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
                thumbSize = finalThumbSize,
                enabled = enabled
            )
        },
        track = { sliderState ->
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .height(finalTrackThickness)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidthExpansionSlider(
    value: Float, // Expected to be 0f to 1f, represents the proportion of expansion
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    orientation: SliderOrientation = SliderOrientation.HORIZONTAL,
    sliderColor: Color = Color.Cyan,
    trackBackgroundColor: Color = sliderColor.copy(alpha = 0.24f),
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    thumbSize: DpSize? = null,
    trackThickness: Dp? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    onValueChangeFinished: (() -> Unit)? = null,
    enabled: Boolean = true,
    hideThumbOnRelease: Boolean = false
) {
    val responsiveSizes = getResponsiveSliderSizes()
    val finalThumbSize = thumbSize ?: responsiveSizes.thumbSize
    val finalTrackThickness = trackThickness ?: responsiveSizes.trackThickness
    // Convert width expansion value (0-1) to raw slider position (0-1)
    // Inverse formula: if widthExpansion = 2 * abs(0.5 - raw), then raw = 0.5 ± (widthExpansion / 2)
    val rawSliderValue = if (value <= 0.5f) {
        0.5f - (value / 2f)
    } else {
        0.5f + (value / 2f)
    }

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
        value = rawSliderValue,
        onValueChange = { rawValue ->
            // Convert raw slider value (0-1) to width expansion value (0-1)
            // Formula: 2 * abs(0.5 - rawValue)
            val widthExpansionValue = 2f * abs(0.5f - rawValue)
            onValueChange(widthExpansionValue)
        },
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
                thumbSize = finalThumbSize,
                enabled = enabled
            )
        },
        track = { sliderState ->
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize() // Fill the space Slider provides for the track
                    .height(4.dp) // Track is always drawn as a horizontal strip
            ) {
                val valueRangeStart = sliderState.valueRange.start
                val valueRangeEnd = sliderState.valueRange.endInclusive
                val totalValueSpan = valueRangeEnd - valueRangeStart

                val normalizedCurrentValue = (sliderState.value - valueRangeStart) / totalValueSpan
                val normalizedCenter = 0.5f // Center of the slider (0.5 = middle)

                // Calculate the active track that grows from center
                val activeTrackWidthFraction = abs(normalizedCurrentValue - normalizedCenter) * 2f // *2 because we want full width at extremes
                val activeTrackStartFraction = normalizedCenter - (activeTrackWidthFraction / 2f)

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
fun StandardSlider(
    value: Float, // Expected to be 0f to 1f
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    orientation: SliderOrientation = SliderOrientation.HORIZONTAL,
    sliderColor: Color = Color.Red,
    trackBackgroundColor: Color = sliderColor.copy(alpha = 0.24f),
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    thumbSize: DpSize? = null,
    trackThickness: Dp? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    onValueChangeFinished: (() -> Unit)? = null,
    enabled: Boolean = true
) {
    val responsiveSizes = getResponsiveSliderSizes()
    val finalThumbSize = thumbSize ?: responsiveSizes.thumbSize
    val finalTrackThickness = trackThickness ?: responsiveSizes.trackThickness
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
                thumbSize = finalThumbSize,
                enabled = enabled
            )
        },
        track = { sliderState ->
    BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize() // Fill the space Slider provides for the track
                    .height(4.dp) // Track is always drawn as a horizontal strip
            ) {
                val valueRangeStart = sliderState.valueRange.start
                val valueRangeEnd = sliderState.valueRange.endInclusive
                val totalValueSpan = valueRangeEnd - valueRangeStart

                val normalizedCurrentValue = (sliderState.value - valueRangeStart) / totalValueSpan

                // Standard slider: active track goes from start to current position
                val activeTrackWidthFraction = normalizedCurrentValue
                val activeTrackStartFraction = 0f

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

private object ContentAlpha {
    const val disabled: Float = 0.38f
}
