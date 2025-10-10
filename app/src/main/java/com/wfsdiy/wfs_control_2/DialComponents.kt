package com.wfsdiy.wfs_control_2

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.TextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*
import kotlinx.coroutines.delay

// Helper function to convert degrees to radians
private fun Float.toRadians(): Float = this * PI.toFloat() / 180f

// Helper function to convert radians to degrees
private fun Float.toDegrees(): Float = this * 180f / PI.toFloat()

// Helper function to normalize angle to 0-360 range
private fun normalizeAngle(angle: Float): Float {
    return ((angle % 360f) + 360f) % 360f
}

// Helper function to calculate angle from center to point
private fun calculateDialAngle(center: Offset, point: Offset): Float {
    val dx = point.x - center.x
    val dy = point.y - center.y
    return normalizeAngle(atan2(dy, dx).toDegrees())
}

// Helper function to calculate distance between two points
private fun calculateDialDistance(point1: Offset, point2: Offset): Float {
    val dx = point1.x - point2.x
    val dy = point1.y - point2.y
    return sqrt(dx * dx + dy * dy)
}

@Composable
private fun getResponsiveDialSizes(): ResponsiveDialSizes {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    
    val screenWidthDp = configuration.screenWidthDp.dp
    val screenDensity = density.density
    
    // Calculate responsive dial size based on screen size and density
    val baseDialSize = (screenWidthDp.value / 4f).coerceIn(80f, 200f) // 80-200dp range
    val adjustedDialSize = (baseDialSize * screenDensity).coerceIn(80f, 200f).dp
    
    // Calculate responsive stroke width
    val baseStrokeWidth = (4.dp * screenDensity).coerceIn(2.dp, 8.dp)
    val adjustedStrokeWidth = (baseStrokeWidth.value * screenDensity).coerceIn(2f, 8f).dp
    
    return ResponsiveDialSizes(
        dialSize = adjustedDialSize,
        strokeWidth = adjustedStrokeWidth
    )
}

private data class ResponsiveDialSizes(
    val dialSize: Dp,
    val strokeWidth: Dp
)

/**
 * A basic dial component that ranges from 0.0 to 1.0
 * @param value Current value (0.0 to 1.0)
 * @param onValueChange Callback when value changes
 * @param modifier Modifier for the dial
 * @param dialColor Color of the dial background
 * @param indicatorColor Color of the value indicator
 * @param trackColor Color of the track/arc
 * @param displayedValue The value to display (independent from dial value)
 * @param valueUnit Unit to display with the value (e.g., "°", "%", "dB")
 * @param isValueEditable Whether the displayed value can be edited
 * @param onDisplayedValueChange Callback when displayed value changes
 * @param valueTextColor Color of the value text
 * @param enabled Whether the dial is interactive
 */
@Composable
fun BasicDial(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    dialColor: Color = Color.DarkGray,
    indicatorColor: Color = Color.White,
    trackColor: Color = Color.Blue,
    displayedValue: String = String.format("%.1f", value * 100f),
    valueUnit: String = "%",
    isValueEditable: Boolean = false,
    onDisplayedValueChange: (String) -> Unit = {},
    onValueCommit: (String) -> Unit = {},
    valueTextColor: Color = Color.White,
    enabled: Boolean = true
) {
    val responsiveSizes = getResponsiveDialSizes()
    val dialSize = responsiveSizes.dialSize
    val strokeWidth = responsiveSizes.strokeWidth
    
    // Convert value (0.0 to 1.0) to angle with dead zone at bottom
    val deadZoneAngle = 60f // Dead zone at bottom center
    val activeAngleRange = 360f - deadZoneAngle // 300° active range
    val startAngle = 120f // Start angle (after dead zone)
    val currentAngle = startAngle + (value * activeAngleRange)
    
    Box(
        modifier = modifier.size(dialSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(enabled) {
                    if (enabled) {
                        detectDragGestures(
                            onDragStart = { },
                            onDragEnd = { },
                            onDrag = { change, _ ->
                                val center = Offset(size.width / 2f, size.height / 2f)
                                val touchPosition = change.position
                                
                                // Calculate angle from center to touch position
                                val dx = touchPosition.x - center.x
                                val dy = touchPosition.y - center.y
                                val touchAngle = normalizeAngle(atan2(dy, dx).toDegrees())
                                
                                // Check if touch is in dead zone (60° to 120°)
                                val isInDeadZone = touchAngle >= 60f && touchAngle <= 120f
                                
                                if (!isInDeadZone) {
                                    // Convert touch angle to dial value
                                    // startAngle is 120°, range is 300°
                                    // We need to map touchAngle (0-360) to value (0-1)
                                    val relativeAngle = if (touchAngle >= startAngle) {
                                        touchAngle - startAngle
                                    } else {
                                        360f - startAngle + touchAngle
                                    }
                                    
                                    // Convert to value (0.0 to 1.0) within the active angle range
                                    val newValue = (relativeAngle / activeAngleRange).coerceIn(0f, 1f)
                                    onValueChange(newValue)
                                }
                                // If in dead zone, don't update the value (keep current value)
                            }
                        )
                    }
                }
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = min(size.width, size.height) / 2f - strokeWidth.value / 2f
            
            // Draw dial background circle
            drawCircle(
                color = dialColor,
                radius = radius,
                style = Stroke(width = strokeWidth.value)
            )
            
            // Draw track arc (active area only, avoiding dead zone at bottom)
            // Dead zone is at bottom center (60° total)
            // In Compose: 0° is 3 o'clock, 90° is 6 o'clock (bottom), 180° is 9 o'clock, 270° is 12 o'clock (top)
            // We want dead zone at bottom: 60° to 120° (centered at 90°)
            
            // Draw the full inactive track arc (dimmer)
            drawArc(
                color = trackColor.copy(alpha = 0.3f),
                startAngle = 120f, // Start after dead zone (bottom-right)
                sweepAngle = 300f,  // 300° active range (360° - 60° dead zone)
                useCenter = false,
                style = Stroke(width = strokeWidth.value * 4f, cap = StrokeCap.Round)
            )
            
            // Draw the active track arc (from start to current position)
            val activeArcSweep = (value * 300f).coerceIn(0f, 300f) // value is 0-1
            if (activeArcSweep > 0f) {
                drawArc(
                    color = trackColor.copy(alpha = 0.75f),
                    startAngle = 120f,
                    sweepAngle = activeArcSweep,
                    useCenter = false,
                    style = Stroke(width = strokeWidth.value * 4f, cap = StrokeCap.Round)
                )
            }
            
            // Draw value indicator line
            val indicatorEndX = center.x + cos(currentAngle.toRadians()) * radius
            val indicatorEndY = center.y + sin(currentAngle.toRadians()) * radius
            
            drawLine(
                color = indicatorColor,
                start = center,
                end = Offset(indicatorEndX, indicatorEndY),
                strokeWidth = strokeWidth.value * 1.5f,
                cap = StrokeCap.Round
            )
            
            // Draw center dot
            drawCircle(
                color = indicatorColor,
                radius = strokeWidth.value / 2f,
                center = center
            )
        }
        
        // Show value text at bottom in dead zone
        Box(
            modifier = Modifier.align(Alignment.BottomCenter).offset(y = (-dialSize * 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            if (isValueEditable) {
                var textFieldValue by remember { mutableStateOf(TextFieldValue(displayedValue)) }
                
                // Update text field when displayedValue changes (from dial rotation)
                LaunchedEffect(displayedValue) {
                    textFieldValue = TextFieldValue(displayedValue, selection = TextRange(displayedValue.length))
                }
                
                val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
                
                BasicTextField(
                    value = textFieldValue,
                    onValueChange = { newValue ->
                        textFieldValue = newValue
                        onDisplayedValueChange(newValue.text)
                    },
                    textStyle = TextStyle(
                        color = valueTextColor,
                        fontSize = (dialSize.value * 0.12f).sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    ),
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = androidx.compose.ui.text.input.ImeAction.Done
                    ),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                        onDone = {
                            onValueCommit(textFieldValue.text)
                            focusManager.clearFocus()
                        }
                    ),
                    modifier = Modifier.size(width = dialSize * 0.6f, height = dialSize * 0.15f)
                )
                
                // Show unit below the editable value
                Text(
                    text = valueUnit,
                    color = valueTextColor,
                    fontSize = (dialSize.value * 0.08f).sp,
                    modifier = Modifier.offset(y = dialSize * 0.1f)
                )
            } else {
                Text(
                    text = "$displayedValue$valueUnit",
                    color = valueTextColor,
                    fontSize = (dialSize.value * 0.12f).sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

/**
 * A rotary knob component with customizable range and appearance
 * @param value Current value
 * @param onValueChange Callback when value changes
 * @param modifier Modifier for the knob
 * @param valueRange Range of values (default 0f..1f)
 * @param knobColor Color of the knob background
 * @param indicatorColor Color of the value indicator
 * @param trackColor Color of the track/arc
 * @param displayedValue The value to display (independent from knob value)
 * @param valueUnit Unit to display with the value (e.g., "°", "%", "dB")
 * @param isValueEditable Whether the displayed value can be edited
 * @param onDisplayedValueChange Callback when displayed value changes
 * @param valueTextColor Color of the value text
 * @param enabled Whether the knob is interactive
 */
@Composable
fun RotaryKnob(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    knobColor: Color = Color.DarkGray,
    indicatorColor: Color = Color.White,
    trackColor: Color = Color.Blue,
    displayedValue: String = String.format("%.1f", value),
    valueUnit: String = "",
    isValueEditable: Boolean = false,
    onDisplayedValueChange: (String) -> Unit = {},
    valueTextColor: Color = Color.White,
    enabled: Boolean = true
) {
    val responsiveSizes = getResponsiveDialSizes()
    val knobSize = responsiveSizes.dialSize
    val strokeWidth = responsiveSizes.strokeWidth
    
    // Convert value to angle with dead zone at bottom
    val deadZoneAngle = 60f // Dead zone at bottom center
    val activeAngleRange = 360f - deadZoneAngle // 300° active range
    val startAngle = 120f // Start angle (after dead zone)
    val normalizedValue = (value - valueRange.start) / (valueRange.endInclusive - valueRange.start)
    val currentAngle = startAngle + (normalizedValue * activeAngleRange)
    
    Box(
        modifier = modifier.size(knobSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(enabled) {
                    if (enabled) {
                        detectDragGestures(
                            onDragStart = { },
                            onDragEnd = { },
                            onDrag = { change, _ ->
                                val center = Offset(size.width / 2f, size.height / 2f)
                                val touchPosition = change.position
                                
                                // Calculate angle from center to touch position
                                val dx = touchPosition.x - center.x
                                val dy = touchPosition.y - center.y
                                val touchAngle = normalizeAngle(atan2(dy, dx).toDegrees())
                                
                                // Check if touch is in dead zone (60° to 120°)
                                val isInDeadZone = touchAngle >= 60f && touchAngle <= 120f
                                
                                if (!isInDeadZone) {
                                    // Convert touch angle to dial value
                                    // startAngle is 120°, range is 300°
                                    // We need to map touchAngle (0-360) to value (0-1)
                                    val relativeAngle = if (touchAngle >= startAngle) {
                                        touchAngle - startAngle
                                    } else {
                                        360f - startAngle + touchAngle
                                    }
                                    
                                    // Convert to normalized value (0.0 to 1.0) within the active angle range
                                    val newNormalizedValue = (relativeAngle / activeAngleRange).coerceIn(0f, 1f)
                                    
                                    // Convert back to actual value range
                                    val newValue = valueRange.start + newNormalizedValue * (valueRange.endInclusive - valueRange.start)
                                    onValueChange(newValue)
                                }
                                // If in dead zone, don't update the value (keep current value)
                            }
                        )
                    }
                }
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = min(size.width, size.height) / 2f - strokeWidth.value / 2f
            
            // Draw knob background circle
            drawCircle(
                color = knobColor,
                radius = radius,
                style = Stroke(width = strokeWidth.value)
            )
            
            // Draw track arc (active area only, avoiding dead zone at bottom)
            // Dead zone is at bottom center (60° total)
            // In Compose: 0° is 3 o'clock, 90° is 6 o'clock (bottom), 180° is 9 o'clock, 270° is 12 o'clock (top)
            // We want dead zone at bottom: 60° to 120° (centered at 90°)
            
            // Draw the full inactive track arc (dimmer)
            drawArc(
                color = trackColor.copy(alpha = 0.3f),
                startAngle = 120f, // Start after dead zone (bottom-right)
                sweepAngle = 300f,  // 300° active range (360° - 60° dead zone)
                useCenter = false,
                style = Stroke(width = strokeWidth.value * 4f, cap = StrokeCap.Round)
            )
            
            // Draw the active track arc (from start to current position)
            val activeArcSweep = (normalizedValue * 300f).coerceIn(0f, 300f)
            if (activeArcSweep > 0f) {
                drawArc(
                    color = trackColor.copy(alpha = 0.75f),
                    startAngle = 120f,
                    sweepAngle = activeArcSweep,
                    useCenter = false,
                    style = Stroke(width = strokeWidth.value * 4f, cap = StrokeCap.Round)
                )
            }
            
            // Draw value indicator line
            val indicatorEndX = center.x + cos(currentAngle.toRadians()) * radius
            val indicatorEndY = center.y + sin(currentAngle.toRadians()) * radius
            
            drawLine(
                color = indicatorColor,
                start = center,
                end = Offset(indicatorEndX, indicatorEndY),
                strokeWidth = strokeWidth.value * 1.5f,
                cap = StrokeCap.Round
            )
            
            // Draw center dot
            drawCircle(
                color = indicatorColor,
                radius = strokeWidth.value / 2f,
                center = center
            )
        }
        
        // Show value text at bottom in dead zone
        Box(
            modifier = Modifier.align(Alignment.BottomCenter).offset(y = (-knobSize * 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            if (isValueEditable) {
                var textFieldValue by remember { mutableStateOf(TextFieldValue(displayedValue)) }
                
                BasicTextField(
                    value = textFieldValue,
                    onValueChange = { newValue ->
                        textFieldValue = newValue
                        onDisplayedValueChange(newValue.text)
                    },
                    textStyle = TextStyle(
                        color = valueTextColor,
                        fontSize = (knobSize.value * 0.12f).sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    ),
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    modifier = Modifier.size(width = knobSize * 0.6f, height = knobSize * 0.15f)
                )
                
                // Show unit below the editable value
                Text(
                    text = valueUnit,
                    color = valueTextColor,
                    fontSize = (knobSize.value * 0.08f).sp,
                    modifier = Modifier.offset(y = knobSize * 0.1f)
                )
            } else {
                Text(
                    text = "$displayedValue$valueUnit",
                    color = valueTextColor,
                    fontSize = (knobSize.value * 0.12f).sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

/**
 * A 360° dial component for angle values
 * @param value Current angle value (-180° to +180°)
 * @param onValueChange Callback when value changes
 * @param modifier Modifier for the dial
 * @param dialColor Color of the dial background
 * @param indicatorColor Color of the value indicator
 * @param trackColor Color of the track/arc
 * @param displayedValue The value to display (independent from dial value)
 * @param isValueEditable Whether the displayed value can be edited
 * @param onDisplayedValueChange Callback when displayed value changes
 * @param valueTextColor Color of the value text
 * @param enabled Whether the dial is interactive
 */
@Composable
fun AngleDial(
    value: Float, // -180° to +180°
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    dialColor: Color = Color.DarkGray,
    indicatorColor: Color = Color.White,
    trackColor: Color = Color.Blue,
    displayedValue: String = String.format("%.1f", value),
    isValueEditable: Boolean = true,
    onDisplayedValueChange: (String) -> Unit = {},
    valueTextColor: Color = Color.White,
    enabled: Boolean = true
) {
    val responsiveSizes = getResponsiveDialSizes()
    val dialSize = responsiveSizes.dialSize
    val strokeWidth = responsiveSizes.strokeWidth
    
    // Convert value (-180° to +180°) to angle (0° to 360°)
    // 0° is at bottom, 180° at top
    // Positive values go anticlockwise, negative values go clockwise
    val currentAngle = if (value >= 0f) {
        // Positive values: 0° to 180° anticlockwise (90° to 270° in Compose)
        90f + value
    } else {
        // Negative values: 0° to -180° clockwise (90° to -90° in Compose)
        90f + value
    }
    
    Box(
        modifier = modifier.size(dialSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(enabled) {
                    if (enabled) {
                        detectDragGestures(
                            onDragStart = { },
                            onDragEnd = { },
                            onDrag = { change, _ ->
                                val center = Offset(size.width / 2f, size.height / 2f)
                                val touchPosition = change.position
                                
                                // Calculate angle from center to touch position
                                val dx = touchPosition.x - center.x
                                val dy = touchPosition.y - center.y
                                val touchAngle = normalizeAngle(atan2(dy, dx).toDegrees())
                                
                                // Convert Compose angle to dial value
                                // Compose: 0° = right, 90° = bottom, 180° = left, 270° = top
                                // Dial: 0° = bottom, 90° = left, 180° = top, 270° = right
                                // Convert touch angle to dial coordinate system
                                val dialAngle = normalizeAngle(touchAngle + 90f)
                                
                                // Convert to value (-180° to +180°)
                                val newValue = dialAngle - 180f
                                
                                onValueChange(newValue.coerceIn(-180f, 180f))
                            }
                        )
                    }
                }
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = min(size.width, size.height) / 2f - strokeWidth.value / 2f
            
            // Draw dial background circle
            drawCircle(
                color = dialColor,
                radius = radius,
                style = Stroke(width = strokeWidth.value)
            )
            
            // Draw full 360° track (single color, no active/inactive)
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth.value * 4f, cap = StrokeCap.Round)
            )
            
            // Draw value indicator as a large dot on the track
            val indicatorAngle = currentAngle.toRadians()
            val indicatorRadius = radius - strokeWidth.value
            val indicatorX = center.x + cos(indicatorAngle) * indicatorRadius
            val indicatorY = center.y + sin(indicatorAngle) * indicatorRadius
            
            drawCircle(
                color = indicatorColor,
                radius = strokeWidth.value * 1.5f,
                center = Offset(indicatorX, indicatorY)
            )
            
            // Draw center dot
            drawCircle(
                color = indicatorColor,
                radius = strokeWidth.value / 2f,
                center = center
            )
        }
        
        // Show value text at center
        Box(
            modifier = Modifier.align(Alignment.Center),
            contentAlignment = Alignment.Center
        ) {
            if (isValueEditable) {
                var textFieldValue by remember { mutableStateOf(TextFieldValue(String.format("%.1f", value))) }
                val focusManager = LocalFocusManager.current
                
                // Update text when value changes
                LaunchedEffect(value) {
                    textFieldValue = TextFieldValue(String.format("%.1f", value))
                }
                
                BasicTextField(
                    value = textFieldValue,
                    onValueChange = { newValue ->
                        textFieldValue = newValue
                    },
                    textStyle = TextStyle(
                        color = valueTextColor,
                        fontSize = (dialSize.value * 0.12f).sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            // Apply modulo operation when Done is pressed
                            try {
                                val parsedValue = textFieldValue.text.toFloatOrNull()
                                if (parsedValue != null) {
                                    // Apply modulo operation: ((x+180)%360)-180
                                    val normalizedValue = ((parsedValue + 180f) % 360f) - 180f
                                    onValueChange(normalizedValue)
                                }
                            } catch (e: NumberFormatException) {
                                // Ignore invalid input
                            }
                            // Close the keyboard
                            focusManager.clearFocus()
                        }
                    ),
                    modifier = Modifier
                        .size(width = dialSize * 0.6f, height = dialSize * 0.15f)
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused) {
                                // Select all text when focused
                                val text = textFieldValue.text
                                textFieldValue = TextFieldValue(
                                    text = text,
                                    selection = androidx.compose.ui.text.TextRange(0, text.length)
                                )
                            }
                        }
                )
                
                // Show unit below the editable value
                Text(
                    text = "°",
                    color = valueTextColor,
                    fontSize = (dialSize.value * 0.08f).sp,
                    modifier = Modifier.offset(y = dialSize * 0.1f)
                )
            } else {
                Text(
                    text = "${String.format("%.1f", value)}°",
                    color = valueTextColor,
                    fontSize = (dialSize.value * 0.12f).sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}
