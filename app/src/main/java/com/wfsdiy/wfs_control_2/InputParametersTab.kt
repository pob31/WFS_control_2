package com.wfsdiy.wfs_control_2

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

@Composable
fun InputParametersTab(
    viewModel: MainActivityViewModel
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    // Observe the input parameters state
    val inputParametersState by viewModel.inputParametersState.collectAsState()
    val numberOfInputs by viewModel.numberOfInputs.collectAsState()

    val selectedChannel = inputParametersState.getSelectedChannel()
    val inputId by rememberUpdatedState(selectedChannel.inputId)

    // Calculate responsive dimensions
    val screenWidthDp = configuration.screenWidthDp.dp
    val screenHeightDp = configuration.screenHeightDp.dp
    val screenDensity = density.density

    // Get responsive text sizes and spacing
    val textSizes = getResponsiveTextSizes()
    val spacing = getResponsiveSpacing()

    // Responsive slider dimensions
    val horizontalSliderWidth = (screenWidthDp * 0.8f).coerceAtLeast(200.dp)
    val horizontalSliderHeight = (40.dp * screenDensity).coerceIn(30.dp, 60.dp)
    val verticalSliderWidth = (40.dp * screenDensity).coerceIn(30.dp, 60.dp)
    val verticalSliderHeight = (150.dp * screenDensity).coerceIn(120.dp, 250.dp)

    // State for showing grid overlay
    var showGridOverlay by remember { mutableStateOf(false) }

    // Input Name state
    val inputName = selectedChannel.getParameter("inputName")
    var inputNameValue by remember { mutableStateOf(inputName.stringValue) }

    LaunchedEffect(inputId, inputName.stringValue) {
        inputNameValue = inputName.stringValue
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(spacing.padding)
        ) {
            // Fixed header with Input Channel selector and Input Name
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = screenWidthDp * 0.1f,
                        end = screenWidthDp * 0.1f,
                        bottom = spacing.largeSpacing
                    ),
                horizontalArrangement = Arrangement.spacedBy(spacing.smallSpacing)
            ) {
                // Input Channel Selector
                InputChannelSelector(
                    selectedInputId = inputParametersState.selectedInputId,
                    maxInputs = numberOfInputs,
                    onInputSelected = { inputId ->
                        viewModel.setSelectedInput(inputId)
                        viewModel.requestInputParameters(inputId)
                    },
                    onOpenSelector = { showGridOverlay = true },
                    modifier = Modifier.weight(1f)
                )

                // Input Name
                ParameterTextBox(
                    label = "Input Name",
                    value = inputNameValue,
                    onValueChange = { newValue ->
                        inputNameValue = newValue
                    },
                    onValueCommit = { committedValue ->
                        selectedChannel.setParameter("inputName", InputParameterValue(
                            normalizedValue = 0f,
                            stringValue = committedValue,
                            displayValue = committedValue
                        ))
                        viewModel.sendInputParameterString("/remoteInput/inputName", inputId, committedValue)
                    },
                    height = 56.dp,
                    modifier = Modifier.weight(1f)
                )
            }

            // Scrollable content
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(spacing.smallSpacing)
            ) {
        
        // Input Group
        ParameterSectionHeader(title = "Input")
        
        RenderInputSection(
            selectedChannel = selectedChannel,
            viewModel = viewModel,
            horizontalSliderWidth = horizontalSliderWidth,
            horizontalSliderHeight = horizontalSliderHeight,
            verticalSliderWidth = verticalSliderWidth,
            verticalSliderHeight = verticalSliderHeight,
            spacing = spacing
        )
        
        // Directivity Group (now has its own collapsible header)
        RenderDirectivitySection(
            selectedChannel = selectedChannel,
            viewModel = viewModel,
            horizontalSliderWidth = horizontalSliderWidth,
            horizontalSliderHeight = horizontalSliderHeight,
            verticalSliderWidth = verticalSliderWidth,
            verticalSliderHeight = verticalSliderHeight,
            spacing = spacing,
            screenWidthDp = screenWidthDp
        )
        
        // Live Source Attenuation Group (now has its own collapsible header)
        RenderLiveSourceSection(
            selectedChannel = selectedChannel,
            viewModel = viewModel,
            horizontalSliderWidth = horizontalSliderWidth,
            horizontalSliderHeight = horizontalSliderHeight,
            verticalSliderWidth = verticalSliderWidth,
            verticalSliderHeight = verticalSliderHeight,
            spacing = spacing,
            screenWidthDp = screenWidthDp
        )
        
        // Floor Reflections Group (now has its own collapsible header)
        RenderFloorReflectionsSection(
            selectedChannel = selectedChannel,
            viewModel = viewModel,
            horizontalSliderWidth = horizontalSliderWidth,
            horizontalSliderHeight = horizontalSliderHeight,
            verticalSliderWidth = verticalSliderWidth,
            verticalSliderHeight = verticalSliderHeight,
            spacing = spacing,
            screenWidthDp = screenWidthDp
        )
        
        // Jitter Group (now has its own collapsible header)
        RenderJitterSection(
            selectedChannel = selectedChannel,
            viewModel = viewModel,
            horizontalSliderWidth = horizontalSliderWidth,
            horizontalSliderHeight = horizontalSliderHeight,
            spacing = spacing,
            screenWidthDp = screenWidthDp
        )
        
        // LFO Group (now has its own collapsible header)
        RenderLFOSection(
            selectedChannel = selectedChannel,
            viewModel = viewModel,
            horizontalSliderWidth = horizontalSliderWidth,
            horizontalSliderHeight = horizontalSliderHeight,
            verticalSliderWidth = verticalSliderWidth,
            verticalSliderHeight = verticalSliderHeight,
            spacing = spacing,
            screenWidthDp = screenWidthDp
        )
            }
        }

        // Grid overlay at Box level so it appears on top of everything
        if (showGridOverlay) {
            InputChannelGridOverlay(
                selectedInputId = inputParametersState.selectedInputId,
                maxInputs = numberOfInputs,
                onInputSelected = { inputId ->
                    viewModel.setSelectedInput(inputId)
                    viewModel.requestInputParameters(inputId)
                    showGridOverlay = false
                },
                onDismiss = { showGridOverlay = false }
            )
        }
    }
}

@Composable
private fun RenderInputSection(
    selectedChannel: InputChannelState,
    viewModel: MainActivityViewModel,
    horizontalSliderWidth: androidx.compose.ui.unit.Dp,
    horizontalSliderHeight: androidx.compose.ui.unit.Dp,
    verticalSliderWidth: androidx.compose.ui.unit.Dp,
    verticalSliderHeight: androidx.compose.ui.unit.Dp,
    spacing: ResponsiveSpacing
) {
    val inputId by rememberUpdatedState(selectedChannel.inputId)

    // Attenuation
    val attenuation = selectedChannel.getParameter("attenuation")
    var attenuationValue by remember { mutableStateOf(attenuation.normalizedValue) }
    var attenuationDisplayValue by remember { mutableStateOf("0.00") }

    LaunchedEffect(inputId, attenuation.normalizedValue) {
        attenuationValue = attenuation.normalizedValue
        val definition = InputParameterDefinitions.parametersByVariableName["attenuation"]!!
        val actualValue = InputParameterDefinitions.applyFormula(definition, attenuationValue)
        attenuationDisplayValue = String.format(Locale.US, "%.2f", actualValue)
    }
    
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Attenuation", fontSize = 12.sp, color = Color.White)
            StandardSlider(
                value = attenuationValue,
                onValueChange = { newValue ->
                    attenuationValue = newValue
                    val definition = InputParameterDefinitions.parametersByVariableName["attenuation"]!!
                    val actualValue = InputParameterDefinitions.applyFormula(definition, newValue)
                    attenuationDisplayValue = String.format(Locale.US, "%.2f", actualValue)
                    selectedChannel.setParameter("attenuation", InputParameterValue(
                        normalizedValue = newValue,
                        stringValue = "",
                        displayValue = "${String.format(Locale.US, "%.2f", actualValue)}dB"
                    ))
                    viewModel.sendInputParameterFloat("/remoteInput/attenuation", inputId, actualValue)
                },
                modifier = Modifier.height(verticalSliderHeight), // Only constrain height, let width adapt to number box
                sliderColor = Color(0xFFFF5722),
                trackBackgroundColor = Color.DarkGray,
                orientation = SliderOrientation.VERTICAL,
                displayedValue = attenuationDisplayValue,
                isValueEditable = true,
                onDisplayedValueChange = { /* Typing handled internally */ },
                onValueCommit = { committedValue ->
                    committedValue.toFloatOrNull()?.let { value ->
                        val definition = InputParameterDefinitions.parametersByVariableName["attenuation"]!!
                        val coercedValue = value.coerceIn(definition.minValue, definition.maxValue)
                        val normalized = InputParameterDefinitions.reverseFormula(definition, coercedValue)
                        attenuationValue = normalized
                        attenuationDisplayValue = String.format(Locale.US, "%.2f", coercedValue)
                        selectedChannel.setParameter("attenuation", InputParameterValue(
                            normalizedValue = normalized,
                            stringValue = "",
                            displayValue = "${String.format(Locale.US, "%.2f", coercedValue)}dB"
                        ))
                        viewModel.sendInputParameterFloat("/remoteInput/attenuation", inputId, coercedValue)
                    }
                },
                valueUnit = "dB",
                valueTextColor = Color.White
            )
        }
    }
    
    Spacer(modifier = Modifier.height(spacing.smallSpacing))
    
    // Delay/Latency
    val delayLatency = selectedChannel.getParameter("delayLatency")
    var delayLatencyValue by remember { mutableFloatStateOf(0f) } // -100 to 100 range directly
    var delayLatencyDisplayValue by remember { mutableStateOf("0.00") }

    LaunchedEffect(inputId, delayLatency.normalizedValue) {
        val definition = InputParameterDefinitions.parametersByVariableName["delayLatency"]!!
        val actualValue = InputParameterDefinitions.applyFormula(definition, delayLatency.normalizedValue)
        delayLatencyValue = actualValue
        delayLatencyDisplayValue = String.format(Locale.US, "%.2f", actualValue)
    }
    
    Column {
        Text("Latency compensation / Delay", fontSize = 12.sp, color = Color.White)
        BidirectionalSlider(
            value = delayLatencyValue,
            onValueChange = { newValue ->
                delayLatencyValue = newValue
                delayLatencyDisplayValue = String.format(Locale.US, "%.2f", newValue)
                val normalized = (newValue + 100f) / 200f
                selectedChannel.setParameter("delayLatency", InputParameterValue(
                    normalizedValue = normalized,
                    stringValue = "",
                    displayValue = "${String.format(Locale.US, "%.2f", newValue)}ms"
                ))
                viewModel.sendInputParameterFloat("/remoteInput/delayLatency", inputId, newValue)
            },
            modifier = Modifier.width(horizontalSliderWidth).height(horizontalSliderHeight),
            sliderColor = Color(0xFF4CAF50),
            trackBackgroundColor = Color.DarkGray,
            orientation = SliderOrientation.HORIZONTAL,
            valueRange = -100f..100f,
            displayedValue = delayLatencyDisplayValue,
            isValueEditable = true,
            onDisplayedValueChange = { /* Typing handled internally */ },
            onValueCommit = { committedValue ->
                committedValue.toFloatOrNull()?.let { value ->
                    val coercedValue = value.coerceIn(-100f, 100f)
                    delayLatencyValue = coercedValue
                    delayLatencyDisplayValue = String.format(Locale.US, "%.2f", coercedValue)
                    val normalized = (coercedValue + 100f) / 200f
                    selectedChannel.setParameter("delayLatency", InputParameterValue(
                        normalizedValue = normalized,
                        stringValue = "",
                        displayValue = "${String.format(Locale.US, "%.2f", coercedValue)}ms"
                    ))
                    viewModel.sendInputParameterFloat("/remoteInput/delayLatency", inputId, coercedValue)
                }
            },
            valueUnit = "ms",
            valueTextColor = Color.White
        )
    }
    
    Spacer(modifier = Modifier.height(spacing.smallSpacing))
    
    // Minimal Latency
    val minimalLatency = selectedChannel.getParameter("minimalLatency")
    var minLatencyIndex by remember {
        mutableIntStateOf(minimalLatency.normalizedValue.toInt().coerceIn(0, 1))
    }

    LaunchedEffect(inputId, minimalLatency.normalizedValue) {
        minLatencyIndex = minimalLatency.normalizedValue.toInt().coerceIn(0, 1)
    }
    
    ParameterTextButton(
        label = "Minimal Latency",
        selectedIndex = minLatencyIndex,
        options = listOf("Acoustic Precedence", "Minimal Latency"),
        onSelectionChange = { index ->
            minLatencyIndex = index
            selectedChannel.setParameter("minimalLatency", InputParameterValue(
                normalizedValue = index.toFloat(),
                stringValue = "",
                displayValue = listOf("Acoustic Precedence", "Minimal Latency")[index]
            ))
            viewModel.sendInputParameterInt("/remoteInput/minimalLatency", inputId, index)
        },
        modifier = Modifier.fillMaxWidth()
    )
    
    Spacer(modifier = Modifier.height(spacing.smallSpacing))
    
    // Position X, Y, Z with Joystick and Slider controls
    val positionX = selectedChannel.getParameter("positionX")
    var positionXValue by remember {
        mutableStateOf(positionX.displayValue.replace("m", "").trim().ifEmpty { "0.00" })
    }

    val positionY = selectedChannel.getParameter("positionY")
    var positionYValue by remember {
        mutableStateOf(positionY.displayValue.replace("m", "").trim().ifEmpty { "0.00" })
    }

    val positionZ = selectedChannel.getParameter("positionZ")
    var positionZValue by remember {
        mutableStateOf(positionZ.displayValue.replace("m", "").trim().ifEmpty { "0.00" })
    }

    LaunchedEffect(inputId, positionX.normalizedValue, positionY.normalizedValue, positionZ.normalizedValue) {
        positionXValue = positionX.displayValue.replace("m", "").trim().ifEmpty { "0.00" }
        positionYValue = positionY.displayValue.replace("m", "").trim().ifEmpty { "0.00" }
        positionZValue = positionZ.displayValue.replace("m", "").trim().ifEmpty { "0.00" }
    }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Left column: Number boxes
        Column(
            modifier = Modifier.weight(0.5f),
            verticalArrangement = Arrangement.spacedBy(spacing.smallSpacing)
        ) {
            ParameterNumberBox(
                label = "Position X",
                value = positionXValue,
                onValueChange = { newValue ->
                    positionXValue = newValue
                },
                onValueCommit = { committedValue ->
                    committedValue.toFloatOrNull()?.let { value ->
                        val coerced = value.coerceIn(-50f, 50f)
                        positionXValue = String.format(Locale.US, "%.2f", coerced)
                        selectedChannel.setParameter("positionX", InputParameterValue(
                            normalizedValue = (coerced + 50f) / 100f,
                            stringValue = "",
                            displayValue = "${String.format(Locale.US, "%.2f", coerced)}m"
                        ))
                        viewModel.sendInputParameterFloat("/remoteInput/positionX", inputId, coerced)
                    }
                },
                unit = "m",
                modifier = Modifier.fillMaxWidth()
            )
            
            ParameterNumberBox(
                label = "Position Y",
                value = positionYValue,
                onValueChange = { newValue ->
                    positionYValue = newValue
                },
                onValueCommit = { committedValue ->
                    committedValue.toFloatOrNull()?.let { value ->
                        val coerced = value.coerceIn(-50f, 50f)
                        positionYValue = String.format(Locale.US, "%.2f", coerced)
                        selectedChannel.setParameter("positionY", InputParameterValue(
                            normalizedValue = (coerced + 50f) / 100f,
                            stringValue = "",
                            displayValue = "${String.format(Locale.US, "%.2f", coerced)}m"
                        ))
                        viewModel.sendInputParameterFloat("/remoteInput/positionY", inputId, coerced)
                    }
                },
                unit = "m",
                modifier = Modifier.fillMaxWidth()
            )
            
            ParameterNumberBox(
                label = "Position Z",
                value = positionZValue,
                onValueChange = { newValue ->
                    positionZValue = newValue
                },
                onValueCommit = { committedValue ->
                    committedValue.toFloatOrNull()?.let { value ->
                        val coerced = value.coerceIn(-50f, 50f)
                        positionZValue = String.format(Locale.US, "%.2f", coerced)
                        selectedChannel.setParameter("positionZ", InputParameterValue(
                            normalizedValue = (coerced + 50f) / 100f,
                            stringValue = "",
                            displayValue = "${String.format(Locale.US, "%.2f", coerced)}m"
                        ))
                        viewModel.sendInputParameterFloat("/remoteInput/positionZ", inputId, coerced)
                    }
                },
                unit = "m",
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // Right column: Joystick and Z slider side by side
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(0.5f)
        ) {
            // Joystick for X and Y position control
            Joystick(
                modifier = Modifier
                    .size(150.dp)
                    .padding(start = verticalSliderWidth * 2),
                onPositionChanged = { x, y ->
                    // x and y are in range -1 to 1
                    // Speed: 10 units per second, joystick reports every 100ms
                    // So increment = joystickValue * 10 * 0.1 = joystickValue * 1.0
                    val xIncrement = x * 1.0f
                    val yIncrement = y * 1.0f
                    
                    // Only update if joystick is deflected
                    if (xIncrement != 0f) {
                        positionXValue.toFloatOrNull()?.let { currentX ->
                            val newX = (currentX + xIncrement).coerceIn(-50f, 50f)
                            positionXValue = String.format(Locale.US, "%.2f", newX)
                            selectedChannel.setParameter("positionX", InputParameterValue(
                                normalizedValue = (newX + 50f) / 100f,
                                stringValue = "",
                                displayValue = "${String.format(Locale.US, "%.2f", newX)}m"
                            ))
                            viewModel.sendInputParameterFloat("/remoteInput/positionX", inputId, newX)
                        }
                    }
                    
                    if (yIncrement != 0f) {
                        positionYValue.toFloatOrNull()?.let { currentY ->
                            val newY = (currentY + yIncrement).coerceIn(-50f, 50f)
                            positionYValue = String.format(Locale.US, "%.2f", newY)
                            selectedChannel.setParameter("positionY", InputParameterValue(
                                normalizedValue = (newY + 50f) / 100f,
                                stringValue = "",
                                displayValue = "${String.format(Locale.US, "%.2f", newY)}m"
                            ))
                            viewModel.sendInputParameterFloat("/remoteInput/positionY", inputId, newY)
                        }
                    }
                }
            )
            
            // Auto-return vertical slider for Z position control
            var zSliderValue by remember { mutableFloatStateOf(0f) }
            
            LaunchedEffect(zSliderValue) {
                // Continuously update position Z while slider is deflected
                while (zSliderValue != 0f) {
                    val zIncrement = zSliderValue * 1.0f
                    
                    positionZValue.toFloatOrNull()?.let { currentZ ->
                        val newZ = (currentZ + zIncrement).coerceIn(-50f, 50f)
                        positionZValue = String.format(Locale.US, "%.2f", newZ)
                        selectedChannel.setParameter("positionZ", InputParameterValue(
                            normalizedValue = (newZ + 50f) / 100f,
                            stringValue = "",
                            displayValue = "${String.format(Locale.US, "%.2f", newZ)}m"
                        ))
                        viewModel.sendInputParameterFloat("/remoteInput/positionZ", inputId, newZ)
                    }
                    
                    kotlinx.coroutines.delay(100) // Update every 100ms
                }
            }
            
            AutoCenterBidirectionalSlider(
                value = zSliderValue,
                onValueChange = { newValue ->
                    zSliderValue = newValue
                },
                modifier = Modifier
                    .height(verticalSliderHeight)
                    .width(verticalSliderWidth),
                sliderColor = Color(0xFF4CAF50),
                trackBackgroundColor = Color.DarkGray,
                orientation = SliderOrientation.VERTICAL,
                valueRange = -1f..1f,
                centerValue = 0f
            )
        }
    }
    
    Spacer(modifier = Modifier.height(spacing.smallSpacing))
    
    // Cluster
    val cluster = selectedChannel.getParameter("cluster")
    var clusterIndex by remember {
        mutableIntStateOf(cluster.normalizedValue.roundToInt().coerceIn(0, 10))
    }

    LaunchedEffect(inputId, cluster.normalizedValue) {
        clusterIndex = cluster.normalizedValue.roundToInt().coerceIn(0, 10)
    }
    
    ParameterDropdown(
        label = "Cluster",
        selectedIndex = clusterIndex,
        options = listOf("none", "Cluster 1", "Cluster 2", "Cluster 3", "Cluster 4", "Cluster 5", "Cluster 6", "Cluster 7", "Cluster 8", "Cluster 9", "Cluster 10"),
        onSelectionChange = { index ->
            clusterIndex = index
            val options = listOf("none", "Cluster 1", "Cluster 2", "Cluster 3", "Cluster 4", "Cluster 5", "Cluster 6", "Cluster 7", "Cluster 8", "Cluster 9", "Cluster 10")
            selectedChannel.setParameter("cluster", InputParameterValue(
                normalizedValue = index.toFloat(),
                stringValue = "",
                displayValue = options[index]
            ))
            viewModel.sendInputParameterInt("/remoteInput/cluster", inputId, index)
        },
        modifier = Modifier.fillMaxWidth()
    )
    
    Spacer(modifier = Modifier.height(spacing.smallSpacing))
    
    // Max Speed Active
    val maxSpeedActive = selectedChannel.getParameter("maxSpeedActive")
    var maxSpeedActiveIndex by remember {
        mutableIntStateOf(maxSpeedActive.normalizedValue.toInt().coerceIn(0, 1))
    }

    LaunchedEffect(inputId, maxSpeedActive.normalizedValue) {
        maxSpeedActiveIndex = maxSpeedActive.normalizedValue.toInt().coerceIn(0, 1)
    }
    
    ParameterTextButton(
        label = "Max Speed Active",
        selectedIndex = maxSpeedActiveIndex,
        options = listOf("ON", "OFF"),
        onSelectionChange = { index ->
            maxSpeedActiveIndex = index
            selectedChannel.setParameter("maxSpeedActive", InputParameterValue(
                normalizedValue = index.toFloat(),
                stringValue = "",
                displayValue = listOf("ON", "OFF")[index]
            ))
            // Invert for OSC: UI index 0 (ON) -> OSC 1, UI index 1 (OFF) -> OSC 0
            viewModel.sendInputParameterInt("/remoteInput/maxSpeedActive", inputId, 1 - index)
        },
        modifier = Modifier.fillMaxWidth()
    )
    
    Spacer(modifier = Modifier.height(spacing.smallSpacing))
    
    // Max Speed
    val maxSpeed = selectedChannel.getParameter("maxSpeed")
    val isMaxSpeedEnabled = maxSpeedActiveIndex == 0 // 0 = ON, 1 = OFF
    var maxSpeedValue by remember { mutableStateOf(maxSpeed.normalizedValue) }
    var maxSpeedDisplayValue by remember {
        mutableStateOf(maxSpeed.displayValue.replace("m/s", "").trim().ifEmpty { "0.01" })
    }

    // Reset state when input channel changes
    LaunchedEffect(inputId, maxSpeed.normalizedValue) {
        maxSpeedValue = maxSpeed.normalizedValue
        maxSpeedDisplayValue = maxSpeed.displayValue.replace("m/s", "").trim().ifEmpty { "0.01" }
    }
    
    Column {
        Text(
            "Max Speed", 
            fontSize = 12.sp, 
            color = if (isMaxSpeedEnabled) Color.White else Color.Gray
        )
        BasicDial(
            value = maxSpeedValue,
            onValueChange = { newValue ->
                maxSpeedValue = newValue
                val definition = InputParameterDefinitions.parametersByVariableName["maxSpeed"]!!
                val actualValue = InputParameterDefinitions.applyFormula(definition, newValue)
                maxSpeedDisplayValue = String.format(Locale.US, "%.2f", actualValue)
                
                // Update the state in the channel
                val updatedValue = InputParameterValue(
                    normalizedValue = newValue,
                    stringValue = "",
                    displayValue = "${String.format(Locale.US, "%.2f", actualValue)}m/s"
                )
                selectedChannel.setParameter("maxSpeed", updatedValue)
                
                viewModel.sendInputParameterFloat("/remoteInput/maxSpeed", inputId, actualValue)
            },
            dialColor = if (isMaxSpeedEnabled) Color.DarkGray else Color(0xFF2A2A2A),
            indicatorColor = if (isMaxSpeedEnabled) Color.White else Color.Gray,
            trackColor = if (isMaxSpeedEnabled) Color(0xFF00BCD4) else Color.DarkGray,
            displayedValue = maxSpeedDisplayValue,
            valueUnit = "m/s",
            isValueEditable = true,
            onDisplayedValueChange = { /* Not needed - typing is handled internally */ },
            onValueCommit = { committedValue ->
                committedValue.toFloatOrNull()?.let { value ->
                    val definition = InputParameterDefinitions.parametersByVariableName["maxSpeed"]!!
                    val coercedValue = value.coerceIn(definition.minValue, definition.maxValue)
                    val normalized = InputParameterDefinitions.reverseFormula(definition, coercedValue)
                    maxSpeedValue = normalized
                    maxSpeedDisplayValue = String.format(Locale.US, "%.2f", coercedValue)
                    
                    // Update the state in the channel
                    val updatedValue = InputParameterValue(
                        normalizedValue = normalized,
                        stringValue = "",
                        displayValue = "${String.format(Locale.US, "%.2f", coercedValue)}m/s"
                    )
                    selectedChannel.setParameter("maxSpeed", updatedValue)
                    
                    viewModel.sendInputParameterFloat("/remoteInput/maxSpeed", inputId, coercedValue)
                }
                // Note: Invalid input is automatically reverted by BasicDial's LaunchedEffect
            },
            valueTextColor = if (isMaxSpeedEnabled) Color.White else Color.Gray,
            enabled = true // Always enabled, just greyed out visually
        )
    }
    
    Spacer(modifier = Modifier.height(spacing.smallSpacing))
    
    // Height Factor
    val heightFactor = selectedChannel.getParameter("heightFactor")
    var heightFactorValue by remember { mutableStateOf(heightFactor.normalizedValue) }
    var heightFactorDisplayValue by remember {
        mutableStateOf(heightFactor.displayValue.replace("%", "").trim().ifEmpty { "0" })
    }

    LaunchedEffect(inputId, heightFactor.normalizedValue) {
        heightFactorValue = heightFactor.normalizedValue
        val definition = InputParameterDefinitions.parametersByVariableName["heightFactor"]!!
        val actualValue = InputParameterDefinitions.applyFormula(definition, heightFactor.normalizedValue)
        heightFactorDisplayValue = actualValue.toInt().toString()
    }
    
    Column {
        Text("Height Factor", fontSize = 12.sp, color = Color.White)
        BasicDial(
            value = heightFactorValue,
            onValueChange = { newValue ->
                heightFactorValue = newValue
                val definition = InputParameterDefinitions.parametersByVariableName["heightFactor"]!!
                val actualValue = InputParameterDefinitions.applyFormula(definition, newValue)
                heightFactorDisplayValue = actualValue.toInt().toString()
                selectedChannel.setParameter("heightFactor", InputParameterValue(
                    normalizedValue = newValue,
                    stringValue = "",
                    displayValue = "${actualValue.toInt()}%"
                ))
                viewModel.sendInputParameterInt("/remoteInput/heightFactor", inputId, actualValue.toInt())
            },
            dialColor = Color.DarkGray,
            indicatorColor = Color.White,
            trackColor = Color(0xFFE91E63),
            displayedValue = heightFactorDisplayValue,
            valueUnit = "%",
            isValueEditable = true,
            onDisplayedValueChange = {},
            onValueCommit = { committedValue ->
                committedValue.toFloatOrNull()?.let { value ->
                    // Round to nearest integer
                    val roundedValue = value.roundToInt()
                    val coercedValue = roundedValue.coerceIn(0, 100)
                    val normalized = coercedValue / 100f
                    heightFactorValue = normalized
                    heightFactorDisplayValue = coercedValue.toString()
                    selectedChannel.setParameter("heightFactor", InputParameterValue(
                        normalizedValue = normalized,
                        stringValue = "",
                        displayValue = "${coercedValue}%"
                    ))
                    viewModel.sendInputParameterInt("/remoteInput/heightFactor", inputId, coercedValue)
                }
            },
            valueTextColor = Color.White,
            enabled = true
        )
    }
    
    Spacer(modifier = Modifier.height(spacing.smallSpacing))
    
    // Attenuation Law
    val attenuationLaw = selectedChannel.getParameter("attenuationLaw")
    var attenuationLawIndex by remember {
        mutableIntStateOf(attenuationLaw.normalizedValue.toInt().coerceIn(0, 1))
    }

    LaunchedEffect(inputId, attenuationLaw.normalizedValue) {
        attenuationLawIndex = attenuationLaw.normalizedValue.toInt().coerceIn(0, 1)
    }
    
    ParameterTextButton(
        label = "Attenuation Law",
        selectedIndex = attenuationLawIndex,
        options = listOf("Log", "1/d²"),
        onSelectionChange = { index ->
            attenuationLawIndex = index
            selectedChannel.setParameter("attenuationLaw", InputParameterValue(
                normalizedValue = index.toFloat(),
                stringValue = "",
                displayValue = listOf("Log", "1/d²")[index]
            ))
            viewModel.sendInputParameterInt("/remoteInput/attenuationLaw", inputId, index)
        },
        modifier = Modifier.fillMaxWidth()
    )
    
    Spacer(modifier = Modifier.height(spacing.smallSpacing))
    
    // Distance Attenuation (visible if attenuationLawIndex == 0)
    if (attenuationLawIndex == 0) {
        val distanceAttenuation = selectedChannel.getParameter("distanceAttenuation")
        var distanceAttenuationValue by remember { mutableStateOf(distanceAttenuation.normalizedValue) }
        var distanceAttenuationDisplayValue by remember {
            mutableStateOf(distanceAttenuation.displayValue.replace("dB/m", "").trim().ifEmpty { "-6.00" })
        }

        LaunchedEffect(inputId, distanceAttenuation.normalizedValue) {
            distanceAttenuationValue = distanceAttenuation.normalizedValue
            val definition = InputParameterDefinitions.parametersByVariableName["distanceAttenuation"]!!
            val actualValue = InputParameterDefinitions.applyFormula(definition, distanceAttenuation.normalizedValue)
            distanceAttenuationDisplayValue = String.format(Locale.US, "%.2f", actualValue)
        }
        
        Column {
            Text("Distance Attenuation", fontSize = 12.sp, color = Color.White)
            BasicDial(
                value = distanceAttenuationValue,
                onValueChange = { newValue ->
                    distanceAttenuationValue = newValue
                    val definition = InputParameterDefinitions.parametersByVariableName["distanceAttenuation"]!!
                    val actualValue = InputParameterDefinitions.applyFormula(definition, newValue)
                    distanceAttenuationDisplayValue = String.format(Locale.US, "%.2f", actualValue)
                    selectedChannel.setParameter("distanceAttenuation", InputParameterValue(
                        normalizedValue = newValue,
                        stringValue = "",
                        displayValue = "${String.format(Locale.US, "%.2f", actualValue)}dB/m"
                    ))
                    viewModel.sendInputParameterFloat("/remoteInput/distanceAttenuation", inputId, actualValue)
                },
                dialColor = Color.DarkGray,
                indicatorColor = Color.White,
                trackColor = Color(0xFFFFC107),
                displayedValue = distanceAttenuationDisplayValue,
                valueUnit = "dB/m",
                isValueEditable = true,
                onDisplayedValueChange = {},
                onValueCommit = { committedValue ->
                    committedValue.toFloatOrNull()?.let { value ->
                        val coercedValue = value.coerceIn(-6f, 0f)
                        val definition = InputParameterDefinitions.parametersByVariableName["distanceAttenuation"]!!
                        val normalized = InputParameterDefinitions.reverseFormula(definition, coercedValue)
                        distanceAttenuationValue = normalized
                        distanceAttenuationDisplayValue = String.format(Locale.US, "%.2f", coercedValue)
                        selectedChannel.setParameter("distanceAttenuation", InputParameterValue(
                            normalizedValue = normalized,
                            stringValue = "",
                            displayValue = "${String.format(Locale.US, "%.2f", coercedValue)}dB/m"
                        ))
                        viewModel.sendInputParameterFloat("/remoteInput/distanceAttenuation", inputId, coercedValue)
                    }
                },
                valueTextColor = Color.White,
                enabled = true
            )
        }
        
        Spacer(modifier = Modifier.height(spacing.smallSpacing))
    }
    
    // Distance Ratio (visible if attenuationLawIndex == 1)
    if (attenuationLawIndex == 1) {
        val distanceRatio = selectedChannel.getParameter("distanceRatio")
        var distanceRatioValue by remember { mutableStateOf(distanceRatio.normalizedValue) }
        var distanceRatioDisplayValue by remember {
            mutableStateOf(distanceRatio.displayValue.replace("x", "").trim().ifEmpty { "0.1" })
        }

        LaunchedEffect(inputId, distanceRatio.normalizedValue) {
            distanceRatioValue = distanceRatio.normalizedValue
            val definition = InputParameterDefinitions.parametersByVariableName["distanceRatio"]!!
            val actualValue = InputParameterDefinitions.applyFormula(definition, distanceRatio.normalizedValue)
            distanceRatioDisplayValue = String.format(Locale.US, "%.2f", actualValue)
        }
        
        Column {
            Text("Distance Ratio", fontSize = 12.sp, color = Color.White)
            BasicDial(
                value = distanceRatioValue,
                onValueChange = { newValue ->
                    distanceRatioValue = newValue
                    val definition = InputParameterDefinitions.parametersByVariableName["distanceRatio"]!!
                    val actualValue = InputParameterDefinitions.applyFormula(definition, newValue)
                    distanceRatioDisplayValue = String.format(Locale.US, "%.2f", actualValue)
                    selectedChannel.setParameter("distanceRatio", InputParameterValue(
                        normalizedValue = newValue,
                        stringValue = "",
                        displayValue = "${String.format(Locale.US, "%.2f", actualValue)}x"
                    ))
                    viewModel.sendInputParameterFloat("/remoteInput/distanceRatio", inputId, actualValue)
                },
                dialColor = Color.DarkGray,
                indicatorColor = Color.White,
                trackColor = Color(0xFFFFC107),
                displayedValue = distanceRatioDisplayValue,
                valueUnit = "x",
                isValueEditable = true,
                onDisplayedValueChange = {},
                onValueCommit = { committedValue ->
                    committedValue.toFloatOrNull()?.let { value ->
                        val coercedValue = value.coerceIn(0.1f, 10f)
                        val definition = InputParameterDefinitions.parametersByVariableName["distanceRatio"]!!
                        val normalized = InputParameterDefinitions.reverseFormula(definition, coercedValue)
                        distanceRatioValue = normalized
                        distanceRatioDisplayValue = String.format(Locale.US, "%.2f", coercedValue)
                        selectedChannel.setParameter("distanceRatio", InputParameterValue(
                            normalizedValue = normalized,
                            stringValue = "",
                            displayValue = "${String.format(Locale.US, "%.2f", coercedValue)}x"
                        ))
                        viewModel.sendInputParameterFloat("/remoteInput/distanceRatio", inputId, coercedValue)
                    }
                },
                valueTextColor = Color.White,
                enabled = true
            )
        }
        
        Spacer(modifier = Modifier.height(spacing.smallSpacing))
    }
    
    // Common Attenuation
    val commonAtten = selectedChannel.getParameter("commonAtten")
    var commonAttenValue by remember { mutableStateOf(commonAtten.normalizedValue) }
    var commonAttenDisplayValue by remember {
        mutableStateOf(commonAtten.displayValue.replace("%", "").trim().ifEmpty { "0" })
    }

    LaunchedEffect(inputId, commonAtten.normalizedValue) {
        commonAttenValue = commonAtten.normalizedValue
        val definition = InputParameterDefinitions.parametersByVariableName["commonAtten"]!!
        val actualValue = InputParameterDefinitions.applyFormula(definition, commonAtten.normalizedValue)
        commonAttenDisplayValue = actualValue.toInt().toString()
    }
    
    Column {
        Text("Common Attenuation", fontSize = 12.sp, color = Color.White)
        BasicDial(
            value = commonAttenValue,
            onValueChange = { newValue ->
                commonAttenValue = newValue
                val definition = InputParameterDefinitions.parametersByVariableName["commonAtten"]!!
                val actualValue = InputParameterDefinitions.applyFormula(definition, newValue)
                commonAttenDisplayValue = actualValue.toInt().toString()
                selectedChannel.setParameter("commonAtten", InputParameterValue(
                    normalizedValue = newValue,
                    stringValue = "",
                    displayValue = "${actualValue.toInt()}%"
                ))
                viewModel.sendInputParameterInt("/remoteInput/commonAtten", inputId, actualValue.toInt())
            },
            dialColor = Color.DarkGray,
            indicatorColor = Color.White,
            trackColor = Color(0xFF3F51B5),
            displayedValue = commonAttenDisplayValue,
            valueUnit = "%",
            isValueEditable = true,
            onDisplayedValueChange = {},
            onValueCommit = { committedValue ->
                committedValue.toFloatOrNull()?.let { value ->
                    // Round to nearest integer
                    val roundedValue = value.roundToInt()
                    val coercedValue = roundedValue.coerceIn(0, 100)
                    val normalized = coercedValue / 100f
                    commonAttenValue = normalized
                    commonAttenDisplayValue = coercedValue.toString()
                    selectedChannel.setParameter("commonAtten", InputParameterValue(
                        normalizedValue = normalized,
                        stringValue = "",
                        displayValue = "${coercedValue}%"
                    ))
                    viewModel.sendInputParameterInt("/remoteInput/commonAtten", inputId, coercedValue)
                }
            },
            valueTextColor = Color.White,
            enabled = true
        )
    }
}

@Composable
private fun RenderDirectivitySection(
    selectedChannel: InputChannelState,
    viewModel: MainActivityViewModel,
    horizontalSliderWidth: androidx.compose.ui.unit.Dp,
    horizontalSliderHeight: androidx.compose.ui.unit.Dp,
    verticalSliderWidth: androidx.compose.ui.unit.Dp,
    verticalSliderHeight: androidx.compose.ui.unit.Dp,
    spacing: ResponsiveSpacing,
    screenWidthDp: androidx.compose.ui.unit.Dp
) {
    val inputId by rememberUpdatedState(selectedChannel.inputId)
    var isExpanded by remember { mutableStateOf(false) }

    // Directivity (Width Expansion Slider - grows from center)
    val directivity = selectedChannel.getParameter("directivity")
    var directivityValue by remember { mutableFloatStateOf(0f) } // 0-1 where it expands from center
    var directivityDisplayValue by remember { mutableStateOf("2") }

    LaunchedEffect(inputId, directivity.normalizedValue) {
        val definition = InputParameterDefinitions.parametersByVariableName["directivity"]!!
        val actualValue = InputParameterDefinitions.applyFormula(definition, directivity.normalizedValue)
        // Map 2-360 to 0-1 expansion value (2 = 0, 360 = 1)
        directivityValue = (actualValue - 2f) / 358f
        directivityDisplayValue = actualValue.toInt().toString()
    }

    // Rotation
    val rotation = selectedChannel.getParameter("rotation")
    var rotationValue by remember { mutableStateOf((rotation.normalizedValue * 360f) - 180f) }

    LaunchedEffect(inputId, rotation.normalizedValue) {
        rotationValue = (rotation.normalizedValue * 360f) - 180f
    }

    // Tilt
    val tilt = selectedChannel.getParameter("tilt")
    var tiltValue by remember { mutableFloatStateOf(0f) } // -90 to 90 range directly
    var tiltDisplayValue by remember { mutableStateOf("0") }

    LaunchedEffect(inputId, tilt.normalizedValue) {
        val definition = InputParameterDefinitions.parametersByVariableName["tilt"]!!
        val actualValue = InputParameterDefinitions.applyFormula(definition, tilt.normalizedValue)
        tiltValue = actualValue
        tiltDisplayValue = actualValue.toInt().toString()
    }

    // HF Shelf
    val HFshelf = selectedChannel.getParameter("HFshelf")
    var HFshelfValue by remember { mutableStateOf(HFshelf.normalizedValue) }
    var HFshelfDisplayValue by remember { mutableStateOf("0.00") }

    LaunchedEffect(inputId, HFshelf.normalizedValue) {
        HFshelfValue = HFshelf.normalizedValue
        val definition = InputParameterDefinitions.parametersByVariableName["HFshelf"]!!
        val actualValue = InputParameterDefinitions.applyFormula(definition, HFshelf.normalizedValue)
        HFshelfDisplayValue = String.format(Locale.US, "%.2f", actualValue)
    }

    // Collapsible Header
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
            .padding(
                start = screenWidthDp * 0.1f,
                end = screenWidthDp * 0.1f,
                top = spacing.smallSpacing,
                bottom = spacing.smallSpacing
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Directivity",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00BCD4)
        )
        Text(
            text = if (isExpanded) "▼" else "▶",
            fontSize = 16.sp,
            color = Color(0xFF00BCD4)
        )
    }

    // Collapsible content
    if (isExpanded) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = screenWidthDp * 0.1f, end = screenWidthDp * 0.1f)
        ) {
            // Single Row: Directivity | Rotation | Tilt | HF Shelf
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.smallSpacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Directivity
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Directivity", fontSize = 12.sp, color = Color.White)
                    WidthExpansionSlider(
                        value = directivityValue,
                        onValueChange = { newValue ->
                            directivityValue = newValue
                            // Map 0-1 expansion to 2-360 degrees
                            val actualValue = 2f + (newValue * 358f)
                            directivityDisplayValue = actualValue.toInt().toString()
                            val normalized = (actualValue - 2f) / 358f
                            selectedChannel.setParameter("directivity", InputParameterValue(
                                normalizedValue = normalized,
                                stringValue = "",
                                displayValue = "${actualValue.toInt()}°"
                            ))
                            viewModel.sendInputParameterInt("/remoteInput/directivity", inputId, actualValue.toInt())
                        },
                        modifier = Modifier.width(horizontalSliderWidth).height(horizontalSliderHeight),
                        sliderColor = Color(0xFF9C27B0),
                        trackBackgroundColor = Color.DarkGray,
                        orientation = SliderOrientation.HORIZONTAL,
                        displayedValue = directivityDisplayValue,
                        isValueEditable = true,
                        onValueCommit = { committedValue ->
                            committedValue.toFloatOrNull()?.let { value ->
                                // Round to nearest integer
                                val roundedValue = value.roundToInt().toFloat()
                                val coercedValue = roundedValue.coerceIn(2f, 360f)
                                val expansionValue = (coercedValue - 2f) / 358f
                                directivityValue = expansionValue
                                directivityDisplayValue = coercedValue.toInt().toString()
                                selectedChannel.setParameter("directivity", InputParameterValue(
                                    normalizedValue = expansionValue,
                                    stringValue = "",
                                    displayValue = "${coercedValue.toInt()}°"
                                ))
                                viewModel.sendInputParameterInt("/remoteInput/directivity", inputId, coercedValue.toInt())
                            }
                        },
                        valueUnit = "°",
                        valueTextColor = Color.White
                    )
                }

                // Rotation
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Rotation", fontSize = 12.sp, color = Color.White)
                    AngleDial(
                        value = rotationValue,
                        onValueChange = { newValue ->
                            // Clamp to -180 to 180 using ((x+540)%360)-180
                            val clamped = ((newValue + 540f) % 360f) - 180f
                            rotationValue = clamped
                            selectedChannel.setParameter("rotation", InputParameterValue(
                                normalizedValue = (clamped + 180f) / 360f,
                                stringValue = "",
                                displayValue = "${clamped.toInt()}°"
                            ))
                            viewModel.sendInputParameterInt("/remoteInput/rotation", inputId, clamped.toInt())
                        },
                        dialColor = Color.DarkGray,
                        indicatorColor = Color.White,
                        trackColor = Color(0xFF4CAF50),
                        isValueEditable = true,
                        onDisplayedValueChange = {},
                        valueTextColor = Color.White,
                        enabled = true,
                        sizeMultiplier = 0.7f
                    )
                }

                // Tilt
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Tilt", fontSize = 12.sp, color = Color.White)
                    BidirectionalSlider(
                        value = tiltValue,
                        onValueChange = { newValue ->
                            tiltValue = newValue
                            tiltDisplayValue = newValue.toInt().toString()
                            val normalized = (newValue + 90f) / 180f
                            selectedChannel.setParameter("tilt", InputParameterValue(
                                normalizedValue = normalized,
                                stringValue = "",
                                displayValue = "${newValue.toInt()}°"
                            ))
                            viewModel.sendInputParameterInt("/remoteInput/tilt", inputId, newValue.toInt())
                        },
                        modifier = Modifier.height(verticalSliderHeight),
                        sliderColor = Color(0xFFFF5722),
                        trackBackgroundColor = Color.DarkGray,
                        orientation = SliderOrientation.VERTICAL,
                        valueRange = -90f..90f,
                        displayedValue = tiltDisplayValue,
                        isValueEditable = true,
                        onDisplayedValueChange = { /* Typing handled internally */ },
                        onValueCommit = { committedValue ->
                            committedValue.toFloatOrNull()?.let { value ->
                                // Round to nearest integer
                                val roundedValue = value.roundToInt().toFloat()
                                val coercedValue = roundedValue.coerceIn(-90f, 90f)
                                tiltValue = coercedValue
                                tiltDisplayValue = coercedValue.toInt().toString()
                                val normalized = (coercedValue + 90f) / 180f
                                selectedChannel.setParameter("tilt", InputParameterValue(
                                    normalizedValue = normalized,
                                    stringValue = "",
                                    displayValue = "${coercedValue.toInt()}°"
                                ))
                                viewModel.sendInputParameterInt("/remoteInput/tilt", inputId, coercedValue.toInt())
                            }
                        },
                        valueUnit = "°",
                        valueTextColor = Color.White
                    )
                }

                // HF Shelf
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("HF Shelf", fontSize = 12.sp, color = Color.White)
                    StandardSlider(
                        value = HFshelfValue,
                        onValueChange = { newValue ->
                            HFshelfValue = newValue
                            val definition = InputParameterDefinitions.parametersByVariableName["HFshelf"]!!
                            val actualValue = InputParameterDefinitions.applyFormula(definition, newValue)
                            HFshelfDisplayValue = String.format(Locale.US, "%.2f", actualValue)
                            selectedChannel.setParameter("HFshelf", InputParameterValue(
                                normalizedValue = newValue,
                                stringValue = "",
                                displayValue = "${String.format(Locale.US, "%.2f", actualValue)}dB"
                            ))
                            viewModel.sendInputParameterFloat("/remoteInput/HFshelf", inputId, actualValue)
                        },
                        modifier = Modifier.width(horizontalSliderWidth).height(horizontalSliderHeight),
                        sliderColor = Color(0xFF00BCD4),
                        trackBackgroundColor = Color.DarkGray,
                        orientation = SliderOrientation.HORIZONTAL,
                        displayedValue = HFshelfDisplayValue,
                        isValueEditable = true,
                        onDisplayedValueChange = { /* Typing handled internally */ },
                        onValueCommit = { committedValue ->
                            committedValue.toFloatOrNull()?.let { value ->
                                val definition = InputParameterDefinitions.parametersByVariableName["HFshelf"]!!
                                val coercedValue = value.coerceIn(definition.minValue, definition.maxValue)
                                val normalized = InputParameterDefinitions.reverseFormula(definition, coercedValue)
                                HFshelfValue = normalized
                                HFshelfDisplayValue = String.format(Locale.US, "%.2f", coercedValue)
                                selectedChannel.setParameter("HFshelf", InputParameterValue(
                                    normalizedValue = normalized,
                                    stringValue = "",
                                    displayValue = "${String.format(Locale.US, "%.2f", coercedValue)}dB"
                                ))
                                viewModel.sendInputParameterFloat("/remoteInput/HFshelf", inputId, coercedValue)
                            }
                        },
                        valueUnit = "dB",
                        valueTextColor = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun RenderLiveSourceSection(
    selectedChannel: InputChannelState,
    viewModel: MainActivityViewModel,
    horizontalSliderWidth: androidx.compose.ui.unit.Dp,
    horizontalSliderHeight: androidx.compose.ui.unit.Dp,
    verticalSliderWidth: androidx.compose.ui.unit.Dp,
    verticalSliderHeight: androidx.compose.ui.unit.Dp,
    spacing: ResponsiveSpacing,
    screenWidthDp: androidx.compose.ui.unit.Dp
) {
    val inputId by rememberUpdatedState(selectedChannel.inputId)
    var isExpanded by remember { mutableStateOf(false) }

    // Active
    val liveSourceActive = selectedChannel.getParameter("liveSourceActive")
    var liveSourceActiveIndex by remember {
        mutableIntStateOf(liveSourceActive.normalizedValue.toInt().coerceIn(0, 1))
    }

    LaunchedEffect(inputId, liveSourceActive.normalizedValue) {
        liveSourceActiveIndex = liveSourceActive.normalizedValue.toInt().coerceIn(0, 1)
    }

    val isLiveSourceEnabled = liveSourceActiveIndex == 0 // 0 = ON, 1 = OFF

    // Radius (greyed out when inactive) - Width Expansion Slider
    val radius = selectedChannel.getParameter("liveSourceRadius")
    var radiusValue by remember { mutableFloatStateOf(0f) } // 0-1 expansion value
    var radiusDisplayValue by remember { mutableStateOf("0.00") }

    LaunchedEffect(inputId, radius.normalizedValue) {
        val definition = InputParameterDefinitions.parametersByVariableName["liveSourceRadius"]!!
        val actualValue = InputParameterDefinitions.applyFormula(definition, radius.normalizedValue)
        // Map 0-50 to 0-1 expansion value
        radiusValue = actualValue / 50f
        radiusDisplayValue = String.format(Locale.US, "%.2f", actualValue)
    }

    // Shape
    val liveSourceShape = selectedChannel.getParameter("liveSourceShape")
    var liveSourceShapeIndex by remember {
        mutableIntStateOf(liveSourceShape.normalizedValue.roundToInt().coerceIn(0, 3))
    }

    LaunchedEffect(inputId, liveSourceShape.normalizedValue) {
        liveSourceShapeIndex = liveSourceShape.normalizedValue.roundToInt().coerceIn(0, 3)
    }

    // Attenuation
    val liveSourceAttenuation = selectedChannel.getParameter("liveSourceAttenuation")
    var liveSourceAttenuationValue by remember { mutableStateOf(liveSourceAttenuation.normalizedValue) }
    var liveSourceAttenuationDisplayValue by remember { mutableStateOf("0.00") }

    LaunchedEffect(inputId, liveSourceAttenuation.normalizedValue) {
        liveSourceAttenuationValue = liveSourceAttenuation.normalizedValue
        val definition = InputParameterDefinitions.parametersByVariableName["liveSourceAttenuation"]!!
        val actualValue = InputParameterDefinitions.applyFormula(definition, liveSourceAttenuation.normalizedValue)
        liveSourceAttenuationDisplayValue = String.format(Locale.US, "%.2f", actualValue)
    }

    // Peak Threshold
    val liveSourcePeakThreshold = selectedChannel.getParameter("liveSourcePeakThreshold")
    var liveSourcePeakThresholdValue by remember { mutableStateOf(liveSourcePeakThreshold.normalizedValue) }
    var liveSourcePeakThresholdDisplayValue by remember { mutableStateOf("0.00") }

    LaunchedEffect(inputId, liveSourcePeakThreshold.normalizedValue) {
        liveSourcePeakThresholdValue = liveSourcePeakThreshold.normalizedValue
        val definition = InputParameterDefinitions.parametersByVariableName["liveSourcePeakThreshold"]!!
        val actualValue = InputParameterDefinitions.applyFormula(definition, liveSourcePeakThreshold.normalizedValue)
        liveSourcePeakThresholdDisplayValue = String.format(Locale.US, "%.2f", actualValue)
    }

    // Peak Ratio
    val liveSourcePeakRatio = selectedChannel.getParameter("liveSourcePeakRatio")
    var liveSourcePeakRatioValue by remember { mutableStateOf(liveSourcePeakRatio.normalizedValue) }
    var liveSourcePeakRatioDisplayValue by remember {
        mutableStateOf(liveSourcePeakRatio.displayValue.replace("", "").trim().ifEmpty { "1.00" })
    }

    LaunchedEffect(inputId, liveSourcePeakRatio.normalizedValue) {
        liveSourcePeakRatioValue = liveSourcePeakRatio.normalizedValue
        val definition = InputParameterDefinitions.parametersByVariableName["liveSourcePeakRatio"]!!
        val actualValue = InputParameterDefinitions.applyFormula(definition, liveSourcePeakRatio.normalizedValue)
        liveSourcePeakRatioDisplayValue = String.format(Locale.US, "%.2f", actualValue)
    }

    // Slow Threshold
    val liveSourceSlowThreshold = selectedChannel.getParameter("liveSourceSlowThreshold")
    var liveSourceSlowThresholdValue by remember { mutableStateOf(liveSourceSlowThreshold.normalizedValue) }
    var liveSourceSlowThresholdDisplayValue by remember { mutableStateOf("0.00") }

    LaunchedEffect(inputId, liveSourceSlowThreshold.normalizedValue) {
        liveSourceSlowThresholdValue = liveSourceSlowThreshold.normalizedValue
        val definition = InputParameterDefinitions.parametersByVariableName["liveSourceSlowThreshold"]!!
        val actualValue = InputParameterDefinitions.applyFormula(definition, liveSourceSlowThreshold.normalizedValue)
        liveSourceSlowThresholdDisplayValue = String.format(Locale.US, "%.2f", actualValue)
    }

    // Slow Ratio
    val liveSourceSlowRatio = selectedChannel.getParameter("liveSourceSlowRatio")
    var liveSourceSlowRatioValue by remember { mutableStateOf(liveSourceSlowRatio.normalizedValue) }
    var liveSourceSlowRatioDisplayValue by remember {
        mutableStateOf(liveSourceSlowRatio.displayValue.replace("", "").trim().ifEmpty { "1.00" })
    }

    LaunchedEffect(inputId, liveSourceSlowRatio.normalizedValue) {
        liveSourceSlowRatioValue = liveSourceSlowRatio.normalizedValue
        val definition = InputParameterDefinitions.parametersByVariableName["liveSourceSlowRatio"]!!
        val actualValue = InputParameterDefinitions.applyFormula(definition, liveSourceSlowRatio.normalizedValue)
        liveSourceSlowRatioDisplayValue = String.format(Locale.US, "%.2f", actualValue)
    }

    // Collapsible Header
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
            .padding(
                start = screenWidthDp * 0.1f,
                end = screenWidthDp * 0.1f,
                top = spacing.smallSpacing,
                bottom = spacing.smallSpacing
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Live Source Attenuation",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00BCD4)
        )
        Text(
            text = if (isExpanded) "▼" else "▶",
            fontSize = 16.sp,
            color = Color(0xFF00BCD4)
        )
    }

    // Collapsible content
    if (isExpanded) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = screenWidthDp * 0.1f, end = screenWidthDp * 0.1f)
        ) {
            // Row 1: Active | Radius | Shape | Attenuation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.smallSpacing)
            ) {
                // Active
                Column(modifier = Modifier.weight(1f)) {
                    ParameterTextButton(
                        label = "Active",
                        selectedIndex = liveSourceActiveIndex,
                        options = listOf("ON", "OFF"),
                        onSelectionChange = { index ->
                            liveSourceActiveIndex = index
                            selectedChannel.setParameter("liveSourceActive", InputParameterValue(
                                normalizedValue = index.toFloat(),
                                stringValue = "",
                                displayValue = listOf("ON", "OFF")[index]
                            ))
                            viewModel.sendInputParameterInt("/remoteInput/liveSourceActive", inputId, 1 - index)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Radius
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Radius", fontSize = 12.sp, color = if (isLiveSourceEnabled) Color.White else Color.Gray)
                    WidthExpansionSlider(
                        value = radiusValue,
                        onValueChange = { newValue ->
                            radiusValue = newValue
                            // Map 0-1 expansion to 0-50 meters
                            val actualValue = newValue * 50f
                            radiusDisplayValue = String.format(Locale.US, "%.2f", actualValue)
                            val normalized = actualValue / 50f
                            selectedChannel.setParameter("liveSourceRadius", InputParameterValue(
                                normalizedValue = normalized,
                                stringValue = "",
                                displayValue = "${String.format(Locale.US, "%.2f", actualValue)}m"
                            ))
                            viewModel.sendInputParameterFloat("/remoteInput/liveSourceRadius", inputId, actualValue)
                        },
                        modifier = Modifier.width(horizontalSliderWidth).height(horizontalSliderHeight),
                        sliderColor = if (isLiveSourceEnabled) Color(0xFF2196F3) else Color.Gray,
                        trackBackgroundColor = Color.DarkGray,
                        orientation = SliderOrientation.HORIZONTAL,
                        displayedValue = radiusDisplayValue,
                        isValueEditable = true,
                        onDisplayedValueChange = { /* Typing handled internally */ },
                        onValueCommit = { committedValue ->
                            committedValue.toFloatOrNull()?.let { value ->
                                val coercedValue = value.coerceIn(0f, 50f)
                                val expansionValue = coercedValue / 50f
                                radiusValue = expansionValue
                                radiusDisplayValue = String.format(Locale.US, "%.2f", coercedValue)
                                selectedChannel.setParameter("liveSourceRadius", InputParameterValue(
                                    normalizedValue = expansionValue,
                                    stringValue = "",
                                    displayValue = "${String.format(Locale.US, "%.2f", coercedValue)}m"
                                ))
                                viewModel.sendInputParameterFloat("/remoteInput/liveSourceRadius", inputId, coercedValue)
                            }
                        },
                        valueUnit = "m",
                        valueTextColor = if (isLiveSourceEnabled) Color.White else Color.Gray,
                        enabled = true
                    )
                }

                // Shape
                Column(modifier = Modifier.weight(1f)) {
                    ParameterDropdown(
                        label = "Shape",
                        selectedIndex = liveSourceShapeIndex,
                        options = listOf("linear", "log", "square d²", "sine"),
                        onSelectionChange = { index ->
                            liveSourceShapeIndex = index
                            selectedChannel.setParameter("liveSourceShape", InputParameterValue(
                                normalizedValue = index.toFloat(),
                                stringValue = "",
                                displayValue = listOf("linear", "log", "square d²", "sine")[index]
                            ))
                            viewModel.sendInputParameterInt("/remoteInput/liveSourceShape", inputId, index)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = isLiveSourceEnabled
                    )
                }

                // Attenuation
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Attenuation", fontSize = 12.sp, color = if (isLiveSourceEnabled) Color.White else Color.Gray)
                    StandardSlider(
                        value = liveSourceAttenuationValue,
                        onValueChange = { newValue ->
                            liveSourceAttenuationValue = newValue
                            val definition = InputParameterDefinitions.parametersByVariableName["liveSourceAttenuation"]!!
                            val actualValue = InputParameterDefinitions.applyFormula(definition, newValue)
                            liveSourceAttenuationDisplayValue = String.format(Locale.US, "%.2f", actualValue)
                            selectedChannel.setParameter("liveSourceAttenuation", InputParameterValue(
                                normalizedValue = newValue,
                                stringValue = "",
                                displayValue = "${String.format(Locale.US, "%.2f", actualValue)}dB"
                            ))
                            viewModel.sendInputParameterFloat("/remoteInput/liveSourceAttenuation", inputId, actualValue)
                        },
                        modifier = Modifier.width(horizontalSliderWidth).height(horizontalSliderHeight),
                        sliderColor = if (isLiveSourceEnabled) Color(0xFF9C27B0) else Color.Gray,
                        trackBackgroundColor = Color.DarkGray,
                        orientation = SliderOrientation.HORIZONTAL,
                        displayedValue = liveSourceAttenuationDisplayValue,
                        isValueEditable = true,
                        onDisplayedValueChange = { /* Typing handled internally */ },
                        onValueCommit = { committedValue ->
                            committedValue.toFloatOrNull()?.let { value ->
                                val definition = InputParameterDefinitions.parametersByVariableName["liveSourceAttenuation"]!!
                                val coercedValue = value.coerceIn(definition.minValue, definition.maxValue)
                                val normalized = InputParameterDefinitions.reverseFormula(definition, coercedValue)
                                liveSourceAttenuationValue = normalized
                                liveSourceAttenuationDisplayValue = String.format(Locale.US, "%.2f", coercedValue)
                                selectedChannel.setParameter("liveSourceAttenuation", InputParameterValue(
                                    normalizedValue = normalized,
                                    stringValue = "",
                                    displayValue = "${String.format(Locale.US, "%.2f", coercedValue)}dB"
                                ))
                                viewModel.sendInputParameterFloat("/remoteInput/liveSourceAttenuation", inputId, coercedValue)
                            }
                        },
                        valueUnit = "dB",
                        valueTextColor = if (isLiveSourceEnabled) Color.White else Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacing.largeSpacing))

            // Row 2: Peak Threshold | Peak Ratio | Slow Threshold | Slow Ratio
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.smallSpacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Peak Threshold
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Peak Threshold", fontSize = 12.sp, color = if (isLiveSourceEnabled) Color.White else Color.Gray)
                    StandardSlider(
                        value = liveSourcePeakThresholdValue,
                        onValueChange = { newValue ->
                            liveSourcePeakThresholdValue = newValue
                            val definition = InputParameterDefinitions.parametersByVariableName["liveSourcePeakThreshold"]!!
                            val actualValue = InputParameterDefinitions.applyFormula(definition, newValue)
                            liveSourcePeakThresholdDisplayValue = String.format(Locale.US, "%.2f", actualValue)
                            selectedChannel.setParameter("liveSourcePeakThreshold", InputParameterValue(
                                normalizedValue = newValue,
                                stringValue = "",
                                displayValue = "${String.format(Locale.US, "%.2f", actualValue)}dB"
                            ))
                            viewModel.sendInputParameterFloat("/remoteInput/liveSourcePeakThreshold", inputId, actualValue)
                        },
                        modifier = Modifier.width(horizontalSliderWidth).height(horizontalSliderHeight),
                        sliderColor = if (isLiveSourceEnabled) Color(0xFFFF9800) else Color.Gray,
                        trackBackgroundColor = Color.DarkGray,
                        orientation = SliderOrientation.HORIZONTAL,
                        displayedValue = liveSourcePeakThresholdDisplayValue,
                        isValueEditable = true,
                        onDisplayedValueChange = { /* Typing handled internally */ },
                        onValueCommit = { committedValue ->
                            committedValue.toFloatOrNull()?.let { value ->
                                val definition = InputParameterDefinitions.parametersByVariableName["liveSourcePeakThreshold"]!!
                                val coercedValue = value.coerceIn(definition.minValue, definition.maxValue)
                                val normalized = InputParameterDefinitions.reverseFormula(definition, coercedValue)
                                liveSourcePeakThresholdValue = normalized
                                liveSourcePeakThresholdDisplayValue = String.format(Locale.US, "%.2f", coercedValue)
                                selectedChannel.setParameter("liveSourcePeakThreshold", InputParameterValue(
                                    normalizedValue = normalized,
                                    stringValue = "",
                                    displayValue = "${String.format(Locale.US, "%.2f", coercedValue)}dB"
                                ))
                                viewModel.sendInputParameterFloat("/remoteInput/liveSourcePeakThreshold", inputId, coercedValue)
                            }
                        },
                        valueUnit = "dB",
                        valueTextColor = if (isLiveSourceEnabled) Color.White else Color.Gray
                    )
                }

                // Peak Ratio
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Peak Ratio", fontSize = 12.sp, color = if (isLiveSourceEnabled) Color.White else Color.Gray)
                    BasicDial(
                        value = liveSourcePeakRatioValue,
                        onValueChange = { newValue ->
                            liveSourcePeakRatioValue = newValue
                            val definition = InputParameterDefinitions.parametersByVariableName["liveSourcePeakRatio"]!!
                            val actualValue = InputParameterDefinitions.applyFormula(definition, newValue)
                            liveSourcePeakRatioDisplayValue = String.format(Locale.US, "%.2f", actualValue)
                            selectedChannel.setParameter("liveSourcePeakRatio", InputParameterValue(
                                normalizedValue = newValue,
                                stringValue = "",
                                displayValue = String.format(Locale.US, "%.2f", actualValue)
                            ))
                            viewModel.sendInputParameterFloat("/remoteInput/liveSourcePeakRatio", inputId, actualValue)
                        },
                        dialColor = if (isLiveSourceEnabled) Color.DarkGray else Color(0xFF2A2A2A),
                        indicatorColor = if (isLiveSourceEnabled) Color.White else Color.Gray,
                        trackColor = if (isLiveSourceEnabled) Color(0xFF673AB7) else Color.DarkGray,
                        displayedValue = liveSourcePeakRatioDisplayValue,
                        valueUnit = "",
                        isValueEditable = true,
                        onDisplayedValueChange = {},
                        onValueCommit = { committedValue ->
                            committedValue.toFloatOrNull()?.let { value ->
                                val definition = InputParameterDefinitions.parametersByVariableName["liveSourcePeakRatio"]!!
                                val coercedValue = value.coerceIn(definition.minValue, definition.maxValue)
                                val normalized = InputParameterDefinitions.reverseFormula(definition, coercedValue)
                                liveSourcePeakRatioValue = normalized
                                liveSourcePeakRatioDisplayValue = String.format(Locale.US, "%.2f", coercedValue)
                                selectedChannel.setParameter("liveSourcePeakRatio", InputParameterValue(
                                    normalizedValue = normalized,
                                    stringValue = "",
                                    displayValue = String.format(Locale.US, "%.2f", coercedValue)
                                ))
                                viewModel.sendInputParameterFloat("/remoteInput/liveSourcePeakRatio", inputId, coercedValue)
                            }
                        },
                        valueTextColor = if (isLiveSourceEnabled) Color.White else Color.Gray,
                        enabled = true,
                        sizeMultiplier = 0.7f
                    )
                }

                // Slow Threshold
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Slow Threshold", fontSize = 12.sp, color = if (isLiveSourceEnabled) Color.White else Color.Gray)
                    StandardSlider(
                        value = liveSourceSlowThresholdValue,
                        onValueChange = { newValue ->
                            liveSourceSlowThresholdValue = newValue
                            val definition = InputParameterDefinitions.parametersByVariableName["liveSourceSlowThreshold"]!!
                            val actualValue = InputParameterDefinitions.applyFormula(definition, newValue)
                            liveSourceSlowThresholdDisplayValue = String.format(Locale.US, "%.2f", actualValue)
                            selectedChannel.setParameter("liveSourceSlowThreshold", InputParameterValue(
                                normalizedValue = newValue,
                                stringValue = "",
                                displayValue = "${String.format(Locale.US, "%.2f", actualValue)}dB"
                            ))
                            viewModel.sendInputParameterFloat("/remoteInput/liveSourceSlowThreshold", inputId, actualValue)
                        },
                        modifier = Modifier.width(horizontalSliderWidth).height(horizontalSliderHeight),
                        sliderColor = if (isLiveSourceEnabled) Color(0xFF4CAF50) else Color.Gray,
                        trackBackgroundColor = Color.DarkGray,
                        orientation = SliderOrientation.HORIZONTAL,
                        displayedValue = liveSourceSlowThresholdDisplayValue,
                        isValueEditable = true,
                        onDisplayedValueChange = { /* Typing handled internally */ },
                        onValueCommit = { committedValue ->
                            committedValue.toFloatOrNull()?.let { value ->
                                val definition = InputParameterDefinitions.parametersByVariableName["liveSourceSlowThreshold"]!!
                                val coercedValue = value.coerceIn(definition.minValue, definition.maxValue)
                                val normalized = InputParameterDefinitions.reverseFormula(definition, coercedValue)
                                liveSourceSlowThresholdValue = normalized
                                liveSourceSlowThresholdDisplayValue = String.format(Locale.US, "%.2f", coercedValue)
                                selectedChannel.setParameter("liveSourceSlowThreshold", InputParameterValue(
                                    normalizedValue = normalized,
                                    stringValue = "",
                                    displayValue = "${String.format(Locale.US, "%.2f", coercedValue)}dB"
                                ))
                                viewModel.sendInputParameterFloat("/remoteInput/liveSourceSlowThreshold", inputId, coercedValue)
                            }
                        },
                        valueUnit = "dB",
                        valueTextColor = if (isLiveSourceEnabled) Color.White else Color.Gray
                    )
                }

                // Slow Ratio
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Slow Ratio", fontSize = 12.sp, color = if (isLiveSourceEnabled) Color.White else Color.Gray)
                    BasicDial(
                        value = liveSourceSlowRatioValue,
                        onValueChange = { newValue ->
                            liveSourceSlowRatioValue = newValue
                            val definition = InputParameterDefinitions.parametersByVariableName["liveSourceSlowRatio"]!!
                            val actualValue = InputParameterDefinitions.applyFormula(definition, newValue)
                            liveSourceSlowRatioDisplayValue = String.format(Locale.US, "%.2f", actualValue)
                            selectedChannel.setParameter("liveSourceSlowRatio", InputParameterValue(
                                normalizedValue = newValue,
                                stringValue = "",
                                displayValue = String.format(Locale.US, "%.2f", actualValue)
                            ))
                            viewModel.sendInputParameterFloat("/remoteInput/liveSourceSlowRatio", inputId, actualValue)
                        },
                        dialColor = if (isLiveSourceEnabled) Color.DarkGray else Color(0xFF2A2A2A),
                        indicatorColor = if (isLiveSourceEnabled) Color.White else Color.Gray,
                        trackColor = if (isLiveSourceEnabled) Color(0xFFE91E63) else Color.DarkGray,
                        displayedValue = liveSourceSlowRatioDisplayValue,
                        valueUnit = "",
                        isValueEditable = true,
                        onDisplayedValueChange = {},
                        onValueCommit = { committedValue ->
                            committedValue.toFloatOrNull()?.let { value ->
                                val definition = InputParameterDefinitions.parametersByVariableName["liveSourceSlowRatio"]!!
                                val coercedValue = value.coerceIn(definition.minValue, definition.maxValue)
                                val normalized = InputParameterDefinitions.reverseFormula(definition, coercedValue)
                                liveSourceSlowRatioValue = normalized
                                liveSourceSlowRatioDisplayValue = String.format(Locale.US, "%.2f", coercedValue)
                                selectedChannel.setParameter("liveSourceSlowRatio", InputParameterValue(
                                    normalizedValue = normalized,
                                    stringValue = "",
                                    displayValue = String.format(Locale.US, "%.2f", coercedValue)
                                ))
                                viewModel.sendInputParameterFloat("/remoteInput/liveSourceSlowRatio", inputId, coercedValue)
                            }
                        },
                        valueTextColor = if (isLiveSourceEnabled) Color.White else Color.Gray,
                        enabled = true,
                        sizeMultiplier = 0.7f
                    )
                }
            }
        }
    }
}

@Composable
private fun RenderFloorReflectionsSection(
    selectedChannel: InputChannelState,
    viewModel: MainActivityViewModel,
    horizontalSliderWidth: androidx.compose.ui.unit.Dp,
    horizontalSliderHeight: androidx.compose.ui.unit.Dp,
    verticalSliderWidth: androidx.compose.ui.unit.Dp,
    verticalSliderHeight: androidx.compose.ui.unit.Dp,
    spacing: ResponsiveSpacing,
    screenWidthDp: androidx.compose.ui.unit.Dp
) {
    val inputId by rememberUpdatedState(selectedChannel.inputId)
    var isExpanded by remember { mutableStateOf(false) }

    // Active
    val FRactive = selectedChannel.getParameter("FRactive")
    var FRactiveIndex by remember {
        mutableIntStateOf(FRactive.normalizedValue.toInt().coerceIn(0, 1))
    }

    LaunchedEffect(inputId, FRactive.normalizedValue) {
        FRactiveIndex = FRactive.normalizedValue.toInt().coerceIn(0, 1)
    }

    val isFREnabled = FRactiveIndex == 0 // 0 = ON, 1 = OFF

    // FRattenuation
    val FRattenuation = selectedChannel.getParameter("FRattentuation")
    var FRattenuationValue by remember { mutableStateOf(FRattenuation.normalizedValue) }
    var FRattenuationDisplayValue by remember { mutableStateOf("0.00") }

    LaunchedEffect(inputId, FRattenuation.normalizedValue) {
        FRattenuationValue = FRattenuation.normalizedValue
        val definition = InputParameterDefinitions.parametersByVariableName["FRattentuation"]!!
        val actualValue = InputParameterDefinitions.applyFormula(definition, FRattenuation.normalizedValue)
        FRattenuationDisplayValue = String.format(Locale.US, "%.2f", actualValue)
    }

    // Low Cut Active
    val FRlowCutActive = selectedChannel.getParameter("FRlowCutActive")
    var FRlowCutActiveIndex by remember {
        mutableIntStateOf(FRlowCutActive.normalizedValue.toInt().coerceIn(0, 1))
    }

    LaunchedEffect(inputId, FRlowCutActive.normalizedValue) {
        FRlowCutActiveIndex = FRlowCutActive.normalizedValue.toInt().coerceIn(0, 1)
    }

    val isFRLowCutEnabled = isFREnabled && FRlowCutActiveIndex == 0

    // Low Cut Freq
    val FRlowCutFreq = selectedChannel.getParameter("FRlowCutFreq")
    var FRlowCutFreqValue by remember { mutableStateOf(FRlowCutFreq.normalizedValue) }
    var FRlowCutFreqDisplayValue by remember { mutableStateOf("20") }

    LaunchedEffect(inputId, FRlowCutFreq.normalizedValue) {
        FRlowCutFreqValue = FRlowCutFreq.normalizedValue
        val definition = InputParameterDefinitions.parametersByVariableName["FRlowCutFreq"]!!
        val actualValue = InputParameterDefinitions.applyFormula(definition, FRlowCutFreq.normalizedValue)
        FRlowCutFreqDisplayValue = actualValue.toInt().toString()
    }

    // High Shelf Active
    val FRhighShelfActive = selectedChannel.getParameter("FRhighShelfActive")
    var FRhighShelfActiveIndex by remember {
        mutableIntStateOf(FRhighShelfActive.normalizedValue.toInt().coerceIn(0, 1))
    }

    LaunchedEffect(inputId, FRhighShelfActive.normalizedValue) {
        FRhighShelfActiveIndex = FRhighShelfActive.normalizedValue.toInt().coerceIn(0, 1)
    }

    val isFRHighShelfEnabled = isFREnabled && FRhighShelfActiveIndex == 0

    // High Shelf Freq
    val FRhighShelfFreq = selectedChannel.getParameter("FRhighShelfFreq")
    var FRhighShelfFreqValue by remember { mutableStateOf(FRhighShelfFreq.normalizedValue) }
    var FRhighShelfFreqDisplayValue by remember { mutableStateOf("20") }

    LaunchedEffect(inputId, FRhighShelfFreq.normalizedValue) {
        FRhighShelfFreqValue = FRhighShelfFreq.normalizedValue
        val definition = InputParameterDefinitions.parametersByVariableName["FRhighShelfFreq"]!!
        val actualValue = InputParameterDefinitions.applyFormula(definition, FRhighShelfFreq.normalizedValue)
        FRhighShelfFreqDisplayValue = actualValue.toInt().toString()
    }

    // High Shelf Gain
    val FRhighShelfGain = selectedChannel.getParameter("FRhighShelfGain")
    var FRhighShelfGainValue by remember { mutableStateOf(FRhighShelfGain.normalizedValue) }
    var FRhighShelfGainDisplayValue by remember { mutableStateOf("0.00") }

    LaunchedEffect(inputId, FRhighShelfGain.normalizedValue) {
        FRhighShelfGainValue = FRhighShelfGain.normalizedValue
        val definition = InputParameterDefinitions.parametersByVariableName["FRhighShelfGain"]!!
        val actualValue = InputParameterDefinitions.applyFormula(definition, FRhighShelfGain.normalizedValue)
        FRhighShelfGainDisplayValue = String.format(Locale.US, "%.2f", actualValue)
    }

    // High Shelf Slope
    val FRhighShelfSlope = selectedChannel.getParameter("FRhighShelfSlope")
    var FRhighShelfSlopeValue by remember { mutableStateOf(FRhighShelfSlope.normalizedValue) }
    var FRhighShelfSlopeDisplayValue by remember { mutableStateOf("0.10") }

    LaunchedEffect(inputId, FRhighShelfSlope.normalizedValue) {
        FRhighShelfSlopeValue = FRhighShelfSlope.normalizedValue
        val definition = InputParameterDefinitions.parametersByVariableName["FRhighShelfSlope"]!!
        val actualValue = InputParameterDefinitions.applyFormula(definition, FRhighShelfSlope.normalizedValue)
        FRhighShelfSlopeDisplayValue = String.format(Locale.US, "%.2f", actualValue)
    }

    // Diffusion
    val FRdiffusion = selectedChannel.getParameter("FRdiffusion")
    var FRdiffusionValue by remember { mutableStateOf(FRdiffusion.normalizedValue) }
    var FRdiffusionDisplayValue by remember {
        mutableStateOf(FRdiffusion.displayValue.replace("%", "").trim().ifEmpty { "0" })
    }

    LaunchedEffect(inputId, FRdiffusion.normalizedValue) {
        FRdiffusionValue = FRdiffusion.normalizedValue
        val definition = InputParameterDefinitions.parametersByVariableName["FRdiffusion"]!!
        val actualValue = InputParameterDefinitions.applyFormula(definition, FRdiffusion.normalizedValue)
        FRdiffusionDisplayValue = actualValue.toInt().toString()
    }

    // Collapsible Header
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
            .padding(
                start = screenWidthDp * 0.1f,
                end = screenWidthDp * 0.1f,
                top = spacing.smallSpacing,
                bottom = spacing.smallSpacing
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Floor Reflections",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00BCD4)
        )
        Text(
            text = if (isExpanded) "▼" else "▶",
            fontSize = 16.sp,
            color = Color(0xFF00BCD4)
        )
    }

    // Collapsible content
    if (isExpanded) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = screenWidthDp * 0.1f, end = screenWidthDp * 0.1f)
        ) {
            // Row 1: Active | Attenuation | Low Cut Active | Low Cut Freq
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.smallSpacing)
            ) {
                // Active
                Column(modifier = Modifier.weight(1f)) {
                    ParameterTextButton(
                        label = "Active",
                        selectedIndex = FRactiveIndex,
                        options = listOf("ON", "OFF"),
                        onSelectionChange = { index ->
                            FRactiveIndex = index
                            selectedChannel.setParameter("FRactive", InputParameterValue(
                                normalizedValue = index.toFloat(),
                                stringValue = "",
                                displayValue = listOf("ON", "OFF")[index]
                            ))
                            viewModel.sendInputParameterInt("/remoteInput/FRactive", inputId, 1 - index)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Attenuation
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Attenuation", fontSize = 12.sp, color = if (isFREnabled) Color.White else Color.Gray)
                    StandardSlider(
                        value = FRattenuationValue,
                        onValueChange = { newValue ->
                            FRattenuationValue = newValue
                            val definition = InputParameterDefinitions.parametersByVariableName["FRattentuation"]!!
                            val actualValue = InputParameterDefinitions.applyFormula(definition, newValue)
                            FRattenuationDisplayValue = String.format(Locale.US, "%.2f", actualValue)
                            selectedChannel.setParameter("FRattentuation", InputParameterValue(
                                normalizedValue = newValue,
                                stringValue = "",
                                displayValue = "${String.format(Locale.US, "%.2f", actualValue)}dB"
                            ))
                            viewModel.sendInputParameterFloat("/remoteInput/FRattentuation", inputId, actualValue)
                        },
                        modifier = Modifier.width(horizontalSliderWidth).height(horizontalSliderHeight),
                        sliderColor = if (isFREnabled) Color(0xFF2196F3) else Color.Gray,
                        trackBackgroundColor = Color.DarkGray,
                        orientation = SliderOrientation.HORIZONTAL,
                        displayedValue = FRattenuationDisplayValue,
                        isValueEditable = true,
                        onDisplayedValueChange = { /* Typing handled internally */ },
                        onValueCommit = { committedValue ->
                            committedValue.toFloatOrNull()?.let { value ->
                                val definition = InputParameterDefinitions.parametersByVariableName["FRattentuation"]!!
                                val coercedValue = value.coerceIn(definition.minValue, definition.maxValue)
                                val normalized = InputParameterDefinitions.reverseFormula(definition, coercedValue)
                                FRattenuationValue = normalized
                                FRattenuationDisplayValue = String.format(Locale.US, "%.2f", coercedValue)
                                selectedChannel.setParameter("FRattentuation", InputParameterValue(
                                    normalizedValue = normalized,
                                    stringValue = "",
                                    displayValue = "${String.format(Locale.US, "%.2f", coercedValue)}dB"
                                ))
                                viewModel.sendInputParameterFloat("/remoteInput/FRattentuation", inputId, coercedValue)
                            }
                        },
                        valueUnit = "dB",
                        valueTextColor = if (isFREnabled) Color.White else Color.Gray
                    )
                }

                // Low Cut Active
                Column(modifier = Modifier.weight(1f)) {
                    ParameterTextButton(
                        label = "Low Cut Active",
                        selectedIndex = FRlowCutActiveIndex,
                        options = listOf("ON", "OFF"),
                        onSelectionChange = { index ->
                            FRlowCutActiveIndex = index
                            selectedChannel.setParameter("FRlowCutActive", InputParameterValue(
                                normalizedValue = index.toFloat(),
                                stringValue = "",
                                displayValue = listOf("ON", "OFF")[index]
                            ))
                            viewModel.sendInputParameterInt("/remoteInput/FRlowCutActive", inputId, 1 - index)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Low Cut Freq
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Low Cut Freq", fontSize = 12.sp, color = if (isFRLowCutEnabled) Color.White else Color.Gray)
                    StandardSlider(
                        value = FRlowCutFreqValue,
                        onValueChange = { newValue ->
                            FRlowCutFreqValue = newValue
                            val definition = InputParameterDefinitions.parametersByVariableName["FRlowCutFreq"]!!
                            val actualValue = InputParameterDefinitions.applyFormula(definition, newValue)
                            FRlowCutFreqDisplayValue = actualValue.toInt().toString()
                            selectedChannel.setParameter("FRlowCutFreq", InputParameterValue(
                                normalizedValue = newValue,
                                stringValue = "",
                                displayValue = "${actualValue.toInt()}Hz"
                            ))
                            viewModel.sendInputParameterInt("/remoteInput/FrlowCutFreq", inputId, actualValue.toInt())
                        },
                        modifier = Modifier.width(horizontalSliderWidth).height(horizontalSliderHeight),
                        sliderColor = if (isFRLowCutEnabled) Color(0xFFCDDC39) else Color.Gray,
                        trackBackgroundColor = Color.DarkGray,
                        orientation = SliderOrientation.HORIZONTAL,
                        displayedValue = FRlowCutFreqDisplayValue,
                        isValueEditable = true,
                        onDisplayedValueChange = { /* Typing handled internally */ },
                        onValueCommit = { committedValue ->
                            committedValue.toFloatOrNull()?.let { value ->
                                val roundedValue = value.roundToInt()
                                val coercedValue = roundedValue.coerceIn(20, 20000)
                                val definition = InputParameterDefinitions.parametersByVariableName["FRlowCutFreq"]!!
                                val normalized = InputParameterDefinitions.reverseFormula(definition, coercedValue.toFloat())
                                FRlowCutFreqValue = normalized
                                FRlowCutFreqDisplayValue = coercedValue.toString()
                                selectedChannel.setParameter("FRlowCutFreq", InputParameterValue(
                                    normalizedValue = normalized,
                                    stringValue = "",
                                    displayValue = "${coercedValue}Hz"
                                ))
                                viewModel.sendInputParameterInt("/remoteInput/FrlowCutFreq", inputId, coercedValue)
                            }
                        },
                        valueUnit = "Hz",
                        valueTextColor = if (isFRLowCutEnabled) Color.White else Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacing.largeSpacing))

            // Row 2: High Shelf Active | High Shelf Freq | High Shelf Gain | High Shelf Slope
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.smallSpacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // High Shelf Active
                Column(modifier = Modifier.weight(1f)) {
                    ParameterTextButton(
                        label = "High Shelf Active",
                        selectedIndex = FRhighShelfActiveIndex,
                        options = listOf("ON", "OFF"),
                        onSelectionChange = { index ->
                            FRhighShelfActiveIndex = index
                            selectedChannel.setParameter("FRhighShelfActive", InputParameterValue(
                                normalizedValue = index.toFloat(),
                                stringValue = "",
                                displayValue = listOf("ON", "OFF")[index]
                            ))
                            viewModel.sendInputParameterInt("/remoteInput/FRhighShelfActive", inputId, 1 - index)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // High Shelf Freq
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("High Shelf Freq", fontSize = 12.sp, color = if (isFRHighShelfEnabled) Color.White else Color.Gray)
                    StandardSlider(
                        value = FRhighShelfFreqValue,
                        onValueChange = { newValue ->
                            FRhighShelfFreqValue = newValue
                            val definition = InputParameterDefinitions.parametersByVariableName["FRhighShelfFreq"]!!
                            val actualValue = InputParameterDefinitions.applyFormula(definition, newValue)
                            FRhighShelfFreqDisplayValue = actualValue.toInt().toString()
                            selectedChannel.setParameter("FRhighShelfFreq", InputParameterValue(
                                normalizedValue = newValue,
                                stringValue = "",
                                displayValue = "${actualValue.toInt()}Hz"
                            ))
                            viewModel.sendInputParameterInt("/remoteInput/FRhighShelfFreq", inputId, actualValue.toInt())
                        },
                        modifier = Modifier.width(horizontalSliderWidth).height(horizontalSliderHeight),
                        sliderColor = if (isFRHighShelfEnabled) Color(0xFF009688) else Color.Gray,
                        trackBackgroundColor = Color.DarkGray,
                        orientation = SliderOrientation.HORIZONTAL,
                        displayedValue = FRhighShelfFreqDisplayValue,
                        isValueEditable = true,
                        onDisplayedValueChange = { /* Typing handled internally */ },
                        onValueCommit = { committedValue ->
                            committedValue.toFloatOrNull()?.let { value ->
                                val roundedValue = value.roundToInt()
                                val coercedValue = roundedValue.coerceIn(20, 20000)
                                val definition = InputParameterDefinitions.parametersByVariableName["FRhighShelfFreq"]!!
                                val normalized = InputParameterDefinitions.reverseFormula(definition, coercedValue.toFloat())
                                FRhighShelfFreqValue = normalized
                                FRhighShelfFreqDisplayValue = coercedValue.toString()
                                selectedChannel.setParameter("FRhighShelfFreq", InputParameterValue(
                                    normalizedValue = normalized,
                                    stringValue = "",
                                    displayValue = "${coercedValue}Hz"
                                ))
                                viewModel.sendInputParameterInt("/remoteInput/FRhighShelfFreq", inputId, coercedValue)
                            }
                        },
                        valueUnit = "Hz",
                        valueTextColor = if (isFRHighShelfEnabled) Color.White else Color.Gray
                    )
                }

                // High Shelf Gain
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("High Shelf Gain", fontSize = 12.sp, color = if (isFRHighShelfEnabled) Color.White else Color.Gray)
                    StandardSlider(
                        value = FRhighShelfGainValue,
                        onValueChange = { newValue ->
                            FRhighShelfGainValue = newValue
                            val definition = InputParameterDefinitions.parametersByVariableName["FRhighShelfGain"]!!
                            val actualValue = InputParameterDefinitions.applyFormula(definition, newValue)
                            FRhighShelfGainDisplayValue = String.format(Locale.US, "%.2f", actualValue)
                            selectedChannel.setParameter("FRhighShelfGain", InputParameterValue(
                                normalizedValue = newValue,
                                stringValue = "",
                                displayValue = "${String.format(Locale.US, "%.2f", actualValue)}dB"
                            ))
                            viewModel.sendInputParameterFloat("/remoteInput/FRhighShelfGain", inputId, actualValue)
                        },
                        modifier = Modifier.width(horizontalSliderWidth).height(horizontalSliderHeight),
                        sliderColor = if (isFRHighShelfEnabled) Color(0xFFFF5722) else Color.Gray,
                        trackBackgroundColor = Color.DarkGray,
                        orientation = SliderOrientation.HORIZONTAL,
                        displayedValue = FRhighShelfGainDisplayValue,
                        isValueEditable = true,
                        onDisplayedValueChange = { /* Typing handled internally */ },
                        onValueCommit = { committedValue ->
                            committedValue.toFloatOrNull()?.let { value ->
                                val definition = InputParameterDefinitions.parametersByVariableName["FRhighShelfGain"]!!
                                val coercedValue = value.coerceIn(definition.minValue, definition.maxValue)
                                val normalized = InputParameterDefinitions.reverseFormula(definition, coercedValue)
                                FRhighShelfGainValue = normalized
                                FRhighShelfGainDisplayValue = String.format(Locale.US, "%.2f", coercedValue)
                                selectedChannel.setParameter("FRhighShelfGain", InputParameterValue(
                                    normalizedValue = normalized,
                                    stringValue = "",
                                    displayValue = "${String.format(Locale.US, "%.2f", coercedValue)}dB"
                                ))
                                viewModel.sendInputParameterFloat("/remoteInput/FRhighShelfGain", inputId, coercedValue)
                            }
                        },
                        valueUnit = "dB",
                        valueTextColor = if (isFRHighShelfEnabled) Color.White else Color.Gray
                    )
                }

                // High Shelf Slope
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("High Shelf Slope", fontSize = 12.sp, color = if (isFRHighShelfEnabled) Color.White else Color.Gray)
                    StandardSlider(
                        value = FRhighShelfSlopeValue,
                        onValueChange = { newValue ->
                            FRhighShelfSlopeValue = newValue
                            val definition = InputParameterDefinitions.parametersByVariableName["FRhighShelfSlope"]!!
                            val actualValue = InputParameterDefinitions.applyFormula(definition, newValue)
                            FRhighShelfSlopeDisplayValue = String.format(Locale.US, "%.2f", actualValue)
                            selectedChannel.setParameter("FRhighShelfSlope", InputParameterValue(
                                normalizedValue = newValue,
                                stringValue = "",
                                displayValue = String.format(Locale.US, "%.2f", actualValue)
                            ))
                            viewModel.sendInputParameterFloat("/remoteInput/FRhighShelfSlope", inputId, actualValue)
                        },
                        modifier = Modifier.width(horizontalSliderWidth).height(horizontalSliderHeight),
                        sliderColor = if (isFRHighShelfEnabled) Color(0xFFFF9800) else Color.Gray,
                        trackBackgroundColor = Color.DarkGray,
                        orientation = SliderOrientation.HORIZONTAL,
                        displayedValue = FRhighShelfSlopeDisplayValue,
                        isValueEditable = true,
                        onDisplayedValueChange = { /* Typing handled internally */ },
                        onValueCommit = { committedValue ->
                            committedValue.toFloatOrNull()?.let { value ->
                                val coercedValue = value.coerceIn(0.1f, 0.9f)
                                val definition = InputParameterDefinitions.parametersByVariableName["FRhighShelfSlope"]!!
                                val normalized = InputParameterDefinitions.reverseFormula(definition, coercedValue)
                                FRhighShelfSlopeValue = normalized
                                FRhighShelfSlopeDisplayValue = String.format(Locale.US, "%.2f", coercedValue)
                                selectedChannel.setParameter("FRhighShelfSlope", InputParameterValue(
                                    normalizedValue = normalized,
                                    stringValue = "",
                                    displayValue = String.format(Locale.US, "%.2f", coercedValue)
                                ))
                                viewModel.sendInputParameterFloat("/remoteInput/FRhighShelfSlope", inputId, coercedValue)
                            }
                        },
                        valueUnit = "",
                        valueTextColor = if (isFRHighShelfEnabled) Color.White else Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacing.largeSpacing))

            // Row 3: Diffusion (centered)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Diffusion", fontSize = 12.sp, color = if (isFREnabled) Color.White else Color.Gray)
                    BasicDial(
                        value = FRdiffusionValue,
                        onValueChange = { newValue ->
                            FRdiffusionValue = newValue
                            val definition = InputParameterDefinitions.parametersByVariableName["FRdiffusion"]!!
                            val actualValue = InputParameterDefinitions.applyFormula(definition, newValue)
                            FRdiffusionDisplayValue = actualValue.toInt().toString()
                            selectedChannel.setParameter("FRdiffusion", InputParameterValue(
                                normalizedValue = newValue,
                                stringValue = "",
                                displayValue = "${actualValue.toInt()}%"
                            ))
                            viewModel.sendInputParameterInt("/remoteInput/FRdiffusion", inputId, actualValue.toInt())
                        },
                        dialColor = if (isFREnabled) Color.DarkGray else Color(0xFF2A2A2A),
                        indicatorColor = if (isFREnabled) Color.White else Color.Gray,
                        trackColor = if (isFREnabled) Color(0xFF795548) else Color.DarkGray,
                        displayedValue = FRdiffusionDisplayValue,
                        valueUnit = "%",
                        isValueEditable = true,
                        onDisplayedValueChange = {},
                        onValueCommit = { committedValue ->
                            committedValue.toFloatOrNull()?.let { value ->
                                val roundedValue = value.roundToInt()
                                val coercedValue = roundedValue.coerceIn(0, 100)
                                val normalized = coercedValue / 100f
                                FRdiffusionValue = normalized
                                FRdiffusionDisplayValue = coercedValue.toString()
                                selectedChannel.setParameter("FRdiffusion", InputParameterValue(
                                    normalizedValue = normalized,
                                    stringValue = "",
                                    displayValue = "${coercedValue}%"
                                ))
                                viewModel.sendInputParameterInt("/remoteInput/FRdiffusion", inputId, coercedValue)
                            }
                        },
                        valueTextColor = if (isFREnabled) Color.White else Color.Gray,
                        enabled = true,
                        sizeMultiplier = 0.7f
                    )
                }
            }
        }
    }
}

@Composable
private fun RenderJitterSection(
    selectedChannel: InputChannelState,
    viewModel: MainActivityViewModel,
    horizontalSliderWidth: androidx.compose.ui.unit.Dp,
    horizontalSliderHeight: androidx.compose.ui.unit.Dp,
    spacing: ResponsiveSpacing,
    screenWidthDp: androidx.compose.ui.unit.Dp
) {
    val inputId by rememberUpdatedState(selectedChannel.inputId)
    var isExpanded by remember { mutableStateOf(false) }

    // Calculate slider width to match Rate X in LFO section
    // LFO row has 10% padding on each side, 3 columns with weight(1), and 5% spacing between columns
    // Available width = 80% (after 10% padding on each side)
    // Column spacing = 10% (two 5% gaps)
    // Each column width = (80% - 10%) / 3 = 70% / 3 ≈ 23.33%
    val jitterSliderWidth = screenWidthDp * 0.7f / 3f

    // Jitter - Width Expansion Slider
    val jitter = selectedChannel.getParameter("jitter")
    var jitterValue by remember { mutableFloatStateOf(0f) } // 0-1 expansion value
    var jitterDisplayValue by remember { mutableStateOf("0.00") }

    LaunchedEffect(inputId, jitter.normalizedValue) {
        val definition = InputParameterDefinitions.parametersByVariableName["jitter"]!!
        val actualValue = InputParameterDefinitions.applyFormula(definition, jitter.normalizedValue)
        // Map 0-10 to 0-1 expansion value (formula uses pow(x,2), so reverse it)
        // Since formula is 10*pow(x,2), we have actualValue = 10*x^2, so x = sqrt(actualValue/10)
        jitterValue = if (actualValue > 0) kotlin.math.sqrt(actualValue / 10f) else 0f
        jitterDisplayValue = String.format(Locale.US, "%.2f", actualValue)
    }

    // Collapsible header
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = screenWidthDp * 0.1f, end = screenWidthDp * 0.1f)
            .clickable { isExpanded = !isExpanded }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Jitter",
            fontSize = 18.sp,
            color = Color(0xFF00BCD4),
            fontWeight = FontWeight.Bold
        )
        Text(
            text = if (isExpanded) "▼" else "▶",
            fontSize = 16.sp,
            color = Color(0xFF00BCD4)
        )
    }

    if (isExpanded) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.width(jitterSliderWidth),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Jitter", fontSize = 12.sp, color = Color.White)
                WidthExpansionSlider(
            value = jitterValue,
            onValueChange = { newValue ->
                jitterValue = newValue
                // Map 0-1 expansion using the formula: 10*pow(x,2)
                val actualValue = 10f * newValue.pow(2)
                jitterDisplayValue = String.format(Locale.US, "%.2f", actualValue)
                // For normalized storage, we use the sqrt of the normalized actual value
                val normalized = newValue
                selectedChannel.setParameter("jitter", InputParameterValue(
                    normalizedValue = normalized,
                    stringValue = "",
                    displayValue = "${String.format(Locale.US, "%.2f", actualValue)}m"
                ))
                viewModel.sendInputParameterFloat("/remoteInput/jitter", inputId, actualValue)
            },
            modifier = Modifier.fillMaxWidth().height(horizontalSliderHeight),
            sliderColor = Color(0xFFFF9800),
            trackBackgroundColor = Color.DarkGray,
            orientation = SliderOrientation.HORIZONTAL,
            displayedValue = jitterDisplayValue,
            isValueEditable = true,
            onDisplayedValueChange = { /* Typing handled internally */ },
            onValueCommit = { committedValue ->
                committedValue.toFloatOrNull()?.let { value ->
                    val coercedValue = value.coerceIn(0f, 10f)
                    // Reverse the formula: x = sqrt(actualValue/10)
                    val expansionValue = if (coercedValue > 0) kotlin.math.sqrt(coercedValue / 10f) else 0f
                    jitterValue = expansionValue
                    jitterDisplayValue = String.format(Locale.US, "%.2f", coercedValue)
                    selectedChannel.setParameter("jitter", InputParameterValue(
                        normalizedValue = expansionValue,
                        stringValue = "",
                        displayValue = "${String.format(Locale.US, "%.2f", coercedValue)}m"
                    ))
                    viewModel.sendInputParameterFloat("/remoteInput/jitter", inputId, coercedValue)
                }
            },
            valueUnit = "m",
            valueTextColor = Color.White
        )
            }
        }
    }
}

@Composable
private fun RenderLFOSection(
    selectedChannel: InputChannelState,
    viewModel: MainActivityViewModel,
    horizontalSliderWidth: androidx.compose.ui.unit.Dp,
    horizontalSliderHeight: androidx.compose.ui.unit.Dp,
    verticalSliderWidth: androidx.compose.ui.unit.Dp,
    verticalSliderHeight: androidx.compose.ui.unit.Dp,
    spacing: ResponsiveSpacing,
    screenWidthDp: androidx.compose.ui.unit.Dp
) {
    val inputId by rememberUpdatedState(selectedChannel.inputId)
    var isExpanded by remember { mutableStateOf(false) }

    // Active
    val LFOactive = selectedChannel.getParameter("LFOactive")
    var LFOactiveIndex by remember {
        mutableIntStateOf(LFOactive.normalizedValue.toInt().coerceIn(0, 1))
    }

    LaunchedEffect(inputId, LFOactive.normalizedValue) {
        LFOactiveIndex = LFOactive.normalizedValue.toInt().coerceIn(0, 1)
    }
    
    val isLFOEnabled = LFOactiveIndex == 0 // 0 = ON, 1 = OFF

    // Period
    val LFOperiod = selectedChannel.getParameter("LFOperiod")
    var LFOperiodValue by remember { mutableStateOf(LFOperiod.normalizedValue) }
    var LFOperiodDisplayValue by remember {
        mutableStateOf(LFOperiod.displayValue.replace("s", "").trim().ifEmpty { "0.01" })
    }

    LaunchedEffect(inputId, LFOperiod.normalizedValue) {
        LFOperiodValue = LFOperiod.normalizedValue
        val definition = InputParameterDefinitions.parametersByVariableName["LFOperiod"]!!
        val actualValue = InputParameterDefinitions.applyFormula(definition, LFOperiod.normalizedValue)
        LFOperiodDisplayValue = String.format(Locale.US, "%.2f", actualValue)
    }

    // Phase
    val LFOphase = selectedChannel.getParameter("LFOphase")
    var LFOphaseValue by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(inputId, LFOphase.normalizedValue) {
        // Phase values are stored directly (0-360), no formula application needed
        LFOphaseValue = LFOphase.normalizedValue
    }

    // Gyrophone
    val LFOgyrophone = selectedChannel.getParameter("LFOgyrophone")
    var LFOgyrophoneIndex by remember {
        mutableIntStateOf((LFOgyrophone.normalizedValue.roundToInt() + 1).coerceIn(0, 2))
    }

    LaunchedEffect(inputId, LFOgyrophone.normalizedValue) {
        LFOgyrophoneIndex = (LFOgyrophone.normalizedValue.roundToInt() + 1).coerceIn(0, 2)
    }

    // Shape X
    val LFOshapeX = selectedChannel.getParameter("LFOshapeX")
    var LFOshapeXIndex by remember {
        mutableIntStateOf(LFOshapeX.normalizedValue.roundToInt().coerceIn(0, 8))
    }

    LaunchedEffect(inputId, LFOshapeX.normalizedValue) {
        LFOshapeXIndex = LFOshapeX.normalizedValue.roundToInt().coerceIn(0, 8)
    }

    val isShapeXEnabled = isLFOEnabled

    // Shape Y
    val LFOshapeY = selectedChannel.getParameter("LFOshapeY")
    var LFOshapeYIndex by remember {
        mutableIntStateOf(LFOshapeY.normalizedValue.roundToInt().coerceIn(0, 8))
    }

    LaunchedEffect(inputId, LFOshapeY.normalizedValue) {
        LFOshapeYIndex = LFOshapeY.normalizedValue.roundToInt().coerceIn(0, 8)
    }

    val isShapeYEnabled = isLFOEnabled

    // Shape Z
    val LFOshapeZ = selectedChannel.getParameter("LFOshapeZ")
    var LFOshapeZIndex by remember {
        mutableIntStateOf(LFOshapeZ.normalizedValue.roundToInt().coerceIn(0, 8))
    }

    LaunchedEffect(inputId, LFOshapeZ.normalizedValue) {
        LFOshapeZIndex = LFOshapeZ.normalizedValue.roundToInt().coerceIn(0, 8)
    }

    val isShapeZEnabled = isLFOEnabled

    // Rate X
    val LFOrateX = selectedChannel.getParameter("LFOrateX")
    var LFOrateXValue by remember { mutableStateOf(LFOrateX.normalizedValue) }
    var LFOrateXDisplayValue by remember {
        mutableStateOf(LFOrateX.displayValue.replace("x", "").trim().ifEmpty { "0.01" })
    }

    LaunchedEffect(inputId, LFOrateX.normalizedValue) {
        LFOrateXValue = LFOrateX.normalizedValue
        val definition = InputParameterDefinitions.parametersByVariableName["LFOrateX"]!!
        val actualValue = InputParameterDefinitions.applyFormula(definition, LFOrateX.normalizedValue)
        LFOrateXDisplayValue = String.format(Locale.US, "%.2f", actualValue)
    }

    val isRateXEnabled = isLFOEnabled && LFOshapeXIndex != 0

    // Rate Y
    val LFOrateY = selectedChannel.getParameter("LFOrateY")
    var LFOrateYValue by remember { mutableStateOf(LFOrateY.normalizedValue) }
    var LFOrateYDisplayValue by remember {
        mutableStateOf(LFOrateY.displayValue.replace("x", "").trim().ifEmpty { "0.01" })
    }

    LaunchedEffect(inputId, LFOrateY.normalizedValue) {
        LFOrateYValue = LFOrateY.normalizedValue
        val definition = InputParameterDefinitions.parametersByVariableName["LFOrateY"]!!
        val actualValue = InputParameterDefinitions.applyFormula(definition, LFOrateY.normalizedValue)
        LFOrateYDisplayValue = String.format(Locale.US, "%.2f", actualValue)
    }

    val isRateYEnabled = isLFOEnabled && LFOshapeYIndex != 0

    // Rate Z
    val LFOrateZ = selectedChannel.getParameter("LFOrateZ")
    var LFOrateZValue by remember { mutableStateOf(LFOrateZ.normalizedValue) }
    var LFOrateZDisplayValue by remember {
        mutableStateOf(LFOrateZ.displayValue.replace("x", "").trim().ifEmpty { "0.01" })
    }

    LaunchedEffect(inputId, LFOrateZ.normalizedValue) {
        LFOrateZValue = LFOrateZ.normalizedValue
        val definition = InputParameterDefinitions.parametersByVariableName["LFOrateZ"]!!
        val actualValue = InputParameterDefinitions.applyFormula(definition, LFOrateZ.normalizedValue)
        LFOrateZDisplayValue = String.format(Locale.US, "%.2f", actualValue)
    }

    val isRateZEnabled = isLFOEnabled && LFOshapeZIndex != 0

    // Amplitude X
    val LFOamplitudeX = selectedChannel.getParameter("LFOamplitudeX")
    var LFOamplitudeXValue by remember { mutableFloatStateOf(0f) }
    var LFOamplitudeXDisplayValue by remember { mutableStateOf("0.00") }

    LaunchedEffect(inputId, LFOamplitudeX.normalizedValue) {
        val definition = InputParameterDefinitions.parametersByVariableName["LFOamplitudeX"]!!
        val actualValue = InputParameterDefinitions.applyFormula(definition, LFOamplitudeX.normalizedValue)
        LFOamplitudeXValue = actualValue
        LFOamplitudeXDisplayValue = String.format(Locale.US, "%.2f", actualValue)
    }

    val isAmplitudeXEnabled = isLFOEnabled && LFOshapeXIndex != 0

    // Amplitude Y
    val LFOamplitudeY = selectedChannel.getParameter("LFOamplitudeY")
    var LFOamplitudeYValue by remember { mutableFloatStateOf(0f) }
    var LFOamplitudeYDisplayValue by remember { mutableStateOf("0.00") }

    LaunchedEffect(inputId, LFOamplitudeY.normalizedValue) {
        val definition = InputParameterDefinitions.parametersByVariableName["LFOamplitudeY"]!!
        val actualValue = InputParameterDefinitions.applyFormula(definition, LFOamplitudeY.normalizedValue)
        LFOamplitudeYValue = actualValue
        LFOamplitudeYDisplayValue = String.format(Locale.US, "%.2f", actualValue)
    }

    val isAmplitudeYEnabled = isLFOEnabled && LFOshapeYIndex != 0

    // Amplitude Z
    val LFOamplitudeZ = selectedChannel.getParameter("LFOamplitudeZ")
    var LFOamplitudeZValue by remember { mutableFloatStateOf(0f) }
    var LFOamplitudeZDisplayValue by remember { mutableStateOf("0.00") }

    LaunchedEffect(inputId, LFOamplitudeZ.normalizedValue) {
        val definition = InputParameterDefinitions.parametersByVariableName["LFOamplitudeZ"]!!
        val actualValue = InputParameterDefinitions.applyFormula(definition, LFOamplitudeZ.normalizedValue)
        LFOamplitudeZValue = actualValue
        LFOamplitudeZDisplayValue = String.format(Locale.US, "%.2f", actualValue)
    }

    val isAmplitudeZEnabled = isLFOEnabled && LFOshapeZIndex != 0

    // Phase X
    val LFOphaseX = selectedChannel.getParameter("LFOphaseX")
    var LFOphaseXValue by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(inputId, LFOphaseX.normalizedValue) {
        // Phase values are stored directly (0-360), no formula application needed
        LFOphaseXValue = LFOphaseX.normalizedValue
    }

    val isPhaseXEnabled = isLFOEnabled && LFOshapeXIndex != 0

    // Phase Y
    val LFOphaseY = selectedChannel.getParameter("LFOphaseY")
    var LFOphaseYValue by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(inputId, LFOphaseY.normalizedValue) {
        // Phase values are stored directly (0-360), no formula application needed
        LFOphaseYValue = LFOphaseY.normalizedValue
    }

    val isPhaseYEnabled = isLFOEnabled && LFOshapeYIndex != 0

    // Phase Z
    val LFOphaseZ = selectedChannel.getParameter("LFOphaseZ")
    var LFOphaseZValue by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(inputId, LFOphaseZ.normalizedValue) {
        // Phase values are stored directly (0-360), no formula application needed
        LFOphaseZValue = LFOphaseZ.normalizedValue
    }

    val isPhaseZEnabled = isLFOEnabled && LFOshapeZIndex != 0

    // Collapsible Header
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
            .padding(
                start = screenWidthDp * 0.1f,
                end = screenWidthDp * 0.1f,
                top = spacing.smallSpacing,
                bottom = spacing.smallSpacing
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "LFO",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00BCD4)
        )
        Text(
            text = if (isExpanded) "▼" else "▶",
            fontSize = 16.sp,
            color = Color(0xFF00BCD4)
        )
    }

    // Collapsible content
    if (isExpanded) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = screenWidthDp * 0.1f, end = screenWidthDp * 0.1f)
        ) {
            // Row 1: Active | Period | Phase | Gyrophone
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.smallSpacing)
            ) {
                // Active
                Column(modifier = Modifier.weight(1f)) {
                    ParameterTextButton(
                        label = "Active",
                        selectedIndex = LFOactiveIndex,
                        options = listOf("ON", "OFF"),
                        onSelectionChange = { index ->
                            LFOactiveIndex = index
                            selectedChannel.setParameter("LFOactive", InputParameterValue(
                                normalizedValue = index.toFloat(),
                                stringValue = "",
                                displayValue = listOf("ON", "OFF")[index]
                            ))
                            // Invert for OSC: UI index 0 (ON) -> OSC 1, UI index 1 (OFF) -> OSC 0
                            viewModel.sendInputParameterInt("/remoteInput/LFOactive", inputId, 1 - index)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Period
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Period", fontSize = 12.sp, color = if (isLFOEnabled) Color.White else Color.Gray)
                    BasicDial(
                        value = LFOperiodValue,
                        onValueChange = { newValue ->
                            LFOperiodValue = newValue
                            val definition = InputParameterDefinitions.parametersByVariableName["LFOperiod"]!!
                            val actualValue = InputParameterDefinitions.applyFormula(definition, newValue)
                            LFOperiodDisplayValue = String.format(Locale.US, "%.2f", actualValue)
                            selectedChannel.setParameter("LFOperiod", InputParameterValue(
                                normalizedValue = newValue,
                                stringValue = "",
                                displayValue = "${String.format(Locale.US, "%.2f", actualValue)}s"
                            ))
                            viewModel.sendInputParameterFloat("/remoteInput/LFOperiod", inputId, actualValue)
                        },
                        dialColor = if (isLFOEnabled) Color.DarkGray else Color(0xFF2A2A2A),
                        indicatorColor = if (isLFOEnabled) Color.White else Color.Gray,
                        trackColor = if (isLFOEnabled) Color(0xFF00BCD4) else Color.DarkGray,
                        displayedValue = LFOperiodDisplayValue,
                        valueUnit = "s",
                        isValueEditable = true,
                        onDisplayedValueChange = {},
                        onValueCommit = { committedValue ->
                            committedValue.toFloatOrNull()?.let { value ->
                                val definition = InputParameterDefinitions.parametersByVariableName["LFOperiod"]!!
                                val coercedValue = value.coerceIn(definition.minValue, definition.maxValue)
                                val normalized = InputParameterDefinitions.reverseFormula(definition, coercedValue)
                                LFOperiodValue = normalized
                                LFOperiodDisplayValue = String.format(Locale.US, "%.2f", coercedValue)
                                selectedChannel.setParameter("LFOperiod", InputParameterValue(
                                    normalizedValue = normalized,
                                    stringValue = "",
                                    displayValue = "${String.format(Locale.US, "%.2f", coercedValue)}s"
                                ))
                                viewModel.sendInputParameterFloat("/remoteInput/LFOperiod", inputId, coercedValue)
                            }
                        },
                        valueTextColor = if (isLFOEnabled) Color.White else Color.Gray,
                        enabled = true,
                        sizeMultiplier = 0.7f
                    )
                }

                // Phase
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Phase", fontSize = 12.sp, color = if (isLFOEnabled) Color.White else Color.Gray)
                    PhaseDial(
                        value = LFOphaseValue,
                        onValueChange = { newValue ->
                            LFOphaseValue = newValue
                            val normalized = newValue / 360f
                            selectedChannel.setParameter("LFOphase", InputParameterValue(
                                normalizedValue = normalized,
                                stringValue = "",
                                displayValue = "${newValue.toInt()}°"
                            ))
                            viewModel.sendInputParameterInt("/remoteInput/LFOphase", inputId, newValue.toInt())
                        },
                        dialColor = if (isLFOEnabled) Color.DarkGray else Color(0xFF2A2A2A),
                        indicatorColor = if (isLFOEnabled) Color.White else Color.Gray,
                        trackColor = if (isLFOEnabled) Color(0xFF9C27B0) else Color.DarkGray,
                        isValueEditable = true,
                        onDisplayedValueChange = {},
                        valueTextColor = if (isLFOEnabled) Color.White else Color.Gray,
                        enabled = true,
                        sizeMultiplier = 0.7f
                    )
                }

                // Gyrophone
                Column(modifier = Modifier.weight(1f)) {
                    ParameterDropdown(
                        label = "Gyrophone",
                        selectedIndex = LFOgyrophoneIndex,
                        options = listOf("Anti-Clockwise", "OFF", "Clockwise"),
                        onSelectionChange = { index ->
                            LFOgyrophoneIndex = index
                            val oscValue = index - 1  // Convert 0,1,2 to -1,0,1
                            selectedChannel.setParameter("LFOgyrophone", InputParameterValue(
                                normalizedValue = oscValue.toFloat(),
                                stringValue = "",
                                displayValue = listOf("Anti-Clockwise", "OFF", "Clockwise")[index]
                            ))
                            viewModel.sendInputParameterInt("/remoteInput/LFOgyrophone", inputId, oscValue)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacing.smallSpacing))

            // Row 2: Shape X | (Rate X + Amplitude X) | Phase X
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(screenWidthDp * 0.05f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shape X (vertically centered)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    ParameterDropdown(
                        label = "Shape X",
                        selectedIndex = LFOshapeXIndex,
                        options = listOf("OFF", "sine", "square", "sawtooth", "triangle", "keystone", "log", "exp", "random"),
                        onSelectionChange = { index ->
                            LFOshapeXIndex = index
                            selectedChannel.setParameter("LFOshapeX", InputParameterValue(
                                normalizedValue = index.toFloat(),
                                stringValue = "",
                                displayValue = listOf("OFF", "sine", "square", "sawtooth", "triangle", "keystone", "log", "exp", "random")[index]
                            ))
                            viewModel.sendInputParameterInt("/remoteInput/LFOshapeX", inputId, index)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = isShapeXEnabled
                    )
                }

                // Rate X + Amplitude X (stacked vertically)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(spacing.smallSpacing)
                ) {
                    // Rate X
                    Column {
                        Text("Rate X", fontSize = 12.sp, color = if (isRateXEnabled) Color.White else Color.Gray)
                        StandardSlider(
                        value = LFOrateXValue,
                        onValueChange = { newValue ->
                            LFOrateXValue = newValue
                            val definition = InputParameterDefinitions.parametersByVariableName["LFOrateX"]!!
                            val actualValue = InputParameterDefinitions.applyFormula(definition, newValue)
                            LFOrateXDisplayValue = String.format(Locale.US, "%.2f", actualValue)
                            selectedChannel.setParameter("LFOrateX", InputParameterValue(
                                normalizedValue = newValue,
                                stringValue = "",
                                displayValue = "${String.format(Locale.US, "%.2f", actualValue)}x"
                            ))
                            viewModel.sendInputParameterFloat("/remoteInput/LFOrateX", inputId, actualValue)
                        },
                        modifier = Modifier.fillMaxWidth().height(horizontalSliderHeight),
                        sliderColor = if (isRateXEnabled) Color(0xFFFF9800) else Color.Gray,
                        trackBackgroundColor = Color.DarkGray,
                        orientation = SliderOrientation.HORIZONTAL,
                        displayedValue = LFOrateXDisplayValue,
                        isValueEditable = true,
                        onDisplayedValueChange = { /* Typing handled internally */ },
                        onValueCommit = { committedValue ->
                            committedValue.toFloatOrNull()?.let { value ->
                                val definition = InputParameterDefinitions.parametersByVariableName["LFOrateX"]!!
                                val coercedValue = value.coerceIn(definition.minValue, definition.maxValue)
                                val normalized = InputParameterDefinitions.reverseFormula(definition, coercedValue)
                                LFOrateXValue = normalized
                                LFOrateXDisplayValue = String.format(Locale.US, "%.2f", coercedValue)
                                selectedChannel.setParameter("LFOrateX", InputParameterValue(
                                    normalizedValue = normalized,
                                    stringValue = "",
                                    displayValue = "${String.format(Locale.US, "%.2f", coercedValue)}x"
                                ))
                                viewModel.sendInputParameterFloat("/remoteInput/LFOrateX", inputId, coercedValue)
                            }
                        },
                        valueUnit = "x",
                        valueTextColor = if (isRateXEnabled) Color.White else Color.Gray
                    )
                    }

                    // Amplitude X
                    Column {
                        Text("Amplitude X", fontSize = 12.sp, color = if (isAmplitudeXEnabled) Color.White else Color.Gray)
                        BidirectionalSlider(
                        value = LFOamplitudeXValue,
                        onValueChange = { newValue ->
                            LFOamplitudeXValue = newValue
                            LFOamplitudeXDisplayValue = String.format(Locale.US, "%.2f", newValue)
                            val normalized = newValue / 50f
                            selectedChannel.setParameter("LFOamplitudeX", InputParameterValue(
                                normalizedValue = normalized,
                                stringValue = "",
                                displayValue = "${String.format(Locale.US, "%.2f", newValue)}m"
                            ))
                            viewModel.sendInputParameterFloat("/remoteInput/LFOamplitudeX", inputId, newValue)
                        },
                        modifier = Modifier.fillMaxWidth().height(horizontalSliderHeight),
                        sliderColor = if (isAmplitudeXEnabled) Color(0xFF4CAF50) else Color.Gray,
                        trackBackgroundColor = Color.DarkGray,
                        orientation = SliderOrientation.HORIZONTAL,
                        valueRange = 0f..50f,
                        displayedValue = LFOamplitudeXDisplayValue,
                        isValueEditable = true,
                        onDisplayedValueChange = { /* Typing handled internally */ },
                        onValueCommit = { committedValue ->
                            committedValue.toFloatOrNull()?.let { value ->
                                val coercedValue = value.coerceIn(0f, 50f)
                                LFOamplitudeXValue = coercedValue
                                LFOamplitudeXDisplayValue = String.format(Locale.US, "%.2f", coercedValue)
                                val normalized = coercedValue / 50f
                                selectedChannel.setParameter("LFOamplitudeX", InputParameterValue(
                                    normalizedValue = normalized,
                                    stringValue = "",
                                    displayValue = "${String.format(Locale.US, "%.2f", coercedValue)}m"
                                ))
                                viewModel.sendInputParameterFloat("/remoteInput/LFOamplitudeX", inputId, coercedValue)
                            }
                        },
                        valueUnit = "m",
                        valueTextColor = if (isAmplitudeXEnabled) Color.White else Color.Gray
                    )
                    }
                }

                // Phase X
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Phase X", fontSize = 12.sp, color = if (isPhaseXEnabled) Color.White else Color.Gray)
                    PhaseDial(
                        value = LFOphaseXValue,
                        onValueChange = { newValue ->
                            LFOphaseXValue = newValue
                            val normalized = newValue / 360f
                            selectedChannel.setParameter("LFOphaseX", InputParameterValue(
                                normalizedValue = normalized,
                                stringValue = "",
                                displayValue = "${newValue.toInt()}°"
                            ))
                            viewModel.sendInputParameterInt("/remoteInput/LFOphaseX", inputId, newValue.toInt())
                        },
                        dialColor = if (isPhaseXEnabled) Color.DarkGray else Color(0xFF2A2A2A),
                        indicatorColor = if (isPhaseXEnabled) Color.White else Color.Gray,
                        trackColor = if (isPhaseXEnabled) Color(0xFF9C27B0) else Color.DarkGray,
                        isValueEditable = true,
                        onDisplayedValueChange = {},
                        valueTextColor = if (isPhaseXEnabled) Color.White else Color.Gray,
                        enabled = true,
                        sizeMultiplier = 0.7f
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacing.smallSpacing))

            // Row 3: Shape Y | (Rate Y + Amplitude Y) | Phase Y
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(screenWidthDp * 0.05f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shape Y (vertically centered)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    ParameterDropdown(
                        label = "Shape Y",
                        selectedIndex = LFOshapeYIndex,
                        options = listOf("OFF", "sine", "square", "sawtooth", "triangle", "keystone", "log", "exp", "random"),
                        onSelectionChange = { index ->
                            LFOshapeYIndex = index
                            selectedChannel.setParameter("LFOshapeY", InputParameterValue(
                                normalizedValue = index.toFloat(),
                                stringValue = "",
                                displayValue = listOf("OFF", "sine", "square", "sawtooth", "triangle", "keystone", "log", "exp", "random")[index]
                            ))
                            viewModel.sendInputParameterInt("/remoteInput/LFOshapeY", inputId, index)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = isShapeYEnabled
                    )
                }

                // Rate Y + Amplitude Y (stacked vertically)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(spacing.smallSpacing)
                ) {
                    // Rate Y
                    Column {
                        Text("Rate Y", fontSize = 12.sp, color = if (isRateYEnabled) Color.White else Color.Gray)
                        StandardSlider(
                        value = LFOrateYValue,
                        onValueChange = { newValue ->
                            LFOrateYValue = newValue
                            val definition = InputParameterDefinitions.parametersByVariableName["LFOrateY"]!!
                            val actualValue = InputParameterDefinitions.applyFormula(definition, newValue)
                            LFOrateYDisplayValue = String.format(Locale.US, "%.2f", actualValue)
                            selectedChannel.setParameter("LFOrateY", InputParameterValue(
                                normalizedValue = newValue,
                                stringValue = "",
                                displayValue = "${String.format(Locale.US, "%.2f", actualValue)}x"
                            ))
                            viewModel.sendInputParameterFloat("/remoteInput/LFOrateY", inputId, actualValue)
                        },
                        modifier = Modifier.fillMaxWidth().height(horizontalSliderHeight),
                        sliderColor = if (isRateYEnabled) Color(0xFFFF9800) else Color.Gray,
                        trackBackgroundColor = Color.DarkGray,
                        orientation = SliderOrientation.HORIZONTAL,
                        displayedValue = LFOrateYDisplayValue,
                        isValueEditable = true,
                        onDisplayedValueChange = { /* Typing handled internally */ },
                        onValueCommit = { committedValue ->
                            committedValue.toFloatOrNull()?.let { value ->
                                val definition = InputParameterDefinitions.parametersByVariableName["LFOrateY"]!!
                                val coercedValue = value.coerceIn(definition.minValue, definition.maxValue)
                                val normalized = InputParameterDefinitions.reverseFormula(definition, coercedValue)
                                LFOrateYValue = normalized
                                LFOrateYDisplayValue = String.format(Locale.US, "%.2f", coercedValue)
                                selectedChannel.setParameter("LFOrateY", InputParameterValue(
                                    normalizedValue = normalized,
                                    stringValue = "",
                                    displayValue = "${String.format(Locale.US, "%.2f", coercedValue)}x"
                                ))
                                viewModel.sendInputParameterFloat("/remoteInput/LFOrateY", inputId, coercedValue)
                            }
                        },
                        valueUnit = "x",
                        valueTextColor = if (isRateYEnabled) Color.White else Color.Gray
                    )
                    }

                    // Amplitude Y
                    Column {
                        Text("Amplitude Y", fontSize = 12.sp, color = if (isAmplitudeYEnabled) Color.White else Color.Gray)
                        BidirectionalSlider(
                        value = LFOamplitudeYValue,
                        onValueChange = { newValue ->
                            LFOamplitudeYValue = newValue
                            LFOamplitudeYDisplayValue = String.format(Locale.US, "%.2f", newValue)
                            val normalized = newValue / 50f
                            selectedChannel.setParameter("LFOamplitudeY", InputParameterValue(
                                normalizedValue = normalized,
                                stringValue = "",
                                displayValue = "${String.format(Locale.US, "%.2f", newValue)}m"
                            ))
                            viewModel.sendInputParameterFloat("/remoteInput/LFOamplitudeY", inputId, newValue)
                        },
                        modifier = Modifier.fillMaxWidth().height(horizontalSliderHeight),
                        sliderColor = if (isAmplitudeYEnabled) Color(0xFF4CAF50) else Color.Gray,
                        trackBackgroundColor = Color.DarkGray,
                        orientation = SliderOrientation.HORIZONTAL,
                        valueRange = 0f..50f,
                        displayedValue = LFOamplitudeYDisplayValue,
                        isValueEditable = true,
                        onDisplayedValueChange = { /* Typing handled internally */ },
                        onValueCommit = { committedValue ->
                            committedValue.toFloatOrNull()?.let { value ->
                                val coercedValue = value.coerceIn(0f, 50f)
                                LFOamplitudeYValue = coercedValue
                                LFOamplitudeYDisplayValue = String.format(Locale.US, "%.2f", coercedValue)
                                val normalized = coercedValue / 50f
                                selectedChannel.setParameter("LFOamplitudeY", InputParameterValue(
                                    normalizedValue = normalized,
                                    stringValue = "",
                                    displayValue = "${String.format(Locale.US, "%.2f", coercedValue)}m"
                                ))
                                viewModel.sendInputParameterFloat("/remoteInput/LFOamplitudeY", inputId, coercedValue)
                            }
                        },
                        valueUnit = "m",
                        valueTextColor = if (isAmplitudeYEnabled) Color.White else Color.Gray
                    )
                    }
                }

                // Phase Y
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Phase Y", fontSize = 12.sp, color = if (isPhaseYEnabled) Color.White else Color.Gray)
                    PhaseDial(
                        value = LFOphaseYValue,
                        onValueChange = { newValue ->
                            LFOphaseYValue = newValue
                            val normalized = newValue / 360f
                            selectedChannel.setParameter("LFOphaseY", InputParameterValue(
                                normalizedValue = normalized,
                                stringValue = "",
                                displayValue = "${newValue.toInt()}°"
                            ))
                            viewModel.sendInputParameterInt("/remoteInput/LFOphaseY", inputId, newValue.toInt())
                        },
                        dialColor = if (isPhaseYEnabled) Color.DarkGray else Color(0xFF2A2A2A),
                        indicatorColor = if (isPhaseYEnabled) Color.White else Color.Gray,
                        trackColor = if (isPhaseYEnabled) Color(0xFF9C27B0) else Color.DarkGray,
                        isValueEditable = true,
                        onDisplayedValueChange = {},
                        valueTextColor = if (isPhaseYEnabled) Color.White else Color.Gray,
                        enabled = true,
                        sizeMultiplier = 0.7f
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacing.smallSpacing))

            // Row 4: Shape Z | (Rate Z + Amplitude Z) | Phase Z
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(screenWidthDp * 0.05f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shape Z (vertically centered)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    ParameterDropdown(
                        label = "Shape Z",
                        selectedIndex = LFOshapeZIndex,
                        options = listOf("OFF", "sine", "square", "sawtooth", "triangle", "keystone", "log", "exp", "random"),
                        onSelectionChange = { index ->
                            LFOshapeZIndex = index
                            selectedChannel.setParameter("LFOshapeZ", InputParameterValue(
                                normalizedValue = index.toFloat(),
                                stringValue = "",
                                displayValue = listOf("OFF", "sine", "square", "sawtooth", "triangle", "keystone", "log", "exp", "random")[index]
                            ))
                            viewModel.sendInputParameterInt("/remoteInput/LFOshapeZ", inputId, index)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = isShapeZEnabled
                    )
                }

                // Rate Z + Amplitude Z (stacked vertically)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(spacing.smallSpacing)
                ) {
                    // Rate Z
                    Column {
                        Text("Rate Z", fontSize = 12.sp, color = if (isRateZEnabled) Color.White else Color.Gray)
                        StandardSlider(
                        value = LFOrateZValue,
                        onValueChange = { newValue ->
                            LFOrateZValue = newValue
                            val definition = InputParameterDefinitions.parametersByVariableName["LFOrateZ"]!!
                            val actualValue = InputParameterDefinitions.applyFormula(definition, newValue)
                            LFOrateZDisplayValue = String.format(Locale.US, "%.2f", actualValue)
                            selectedChannel.setParameter("LFOrateZ", InputParameterValue(
                                normalizedValue = newValue,
                                stringValue = "",
                                displayValue = "${String.format(Locale.US, "%.2f", actualValue)}x"
                            ))
                            viewModel.sendInputParameterFloat("/remoteInput/LFOrateZ", inputId, actualValue)
                        },
                        modifier = Modifier.fillMaxWidth().height(horizontalSliderHeight),
                        sliderColor = if (isRateZEnabled) Color(0xFFFF9800) else Color.Gray,
                        trackBackgroundColor = Color.DarkGray,
                        orientation = SliderOrientation.HORIZONTAL,
                        displayedValue = LFOrateZDisplayValue,
                        isValueEditable = true,
                        onDisplayedValueChange = { /* Typing handled internally */ },
                        onValueCommit = { committedValue ->
                            committedValue.toFloatOrNull()?.let { value ->
                                val definition = InputParameterDefinitions.parametersByVariableName["LFOrateZ"]!!
                                val coercedValue = value.coerceIn(definition.minValue, definition.maxValue)
                                val normalized = InputParameterDefinitions.reverseFormula(definition, coercedValue)
                                LFOrateZValue = normalized
                                LFOrateZDisplayValue = String.format(Locale.US, "%.2f", coercedValue)
                                selectedChannel.setParameter("LFOrateZ", InputParameterValue(
                                    normalizedValue = normalized,
                                    stringValue = "",
                                    displayValue = "${String.format(Locale.US, "%.2f", coercedValue)}x"
                                ))
                                viewModel.sendInputParameterFloat("/remoteInput/LFOrateZ", inputId, coercedValue)
                            }
                        },
                        valueUnit = "x",
                        valueTextColor = if (isRateZEnabled) Color.White else Color.Gray
                    )
                    }

                    // Amplitude Z
                    Column {
                        Text("Amplitude Z", fontSize = 12.sp, color = if (isAmplitudeZEnabled) Color.White else Color.Gray)
                        BidirectionalSlider(
                        value = LFOamplitudeZValue,
                        onValueChange = { newValue ->
                            LFOamplitudeZValue = newValue
                            LFOamplitudeZDisplayValue = String.format(Locale.US, "%.2f", newValue)
                            val normalized = newValue / 50f
                            selectedChannel.setParameter("LFOamplitudeZ", InputParameterValue(
                                normalizedValue = normalized,
                                stringValue = "",
                                displayValue = "${String.format(Locale.US, "%.2f", newValue)}m"
                            ))
                            viewModel.sendInputParameterFloat("/remoteInput/LFOamplitudeZ", inputId, newValue)
                        },
                        modifier = Modifier.fillMaxWidth().height(horizontalSliderHeight),
                        sliderColor = if (isAmplitudeZEnabled) Color(0xFF4CAF50) else Color.Gray,
                        trackBackgroundColor = Color.DarkGray,
                        orientation = SliderOrientation.HORIZONTAL,
                        valueRange = 0f..50f,
                        displayedValue = LFOamplitudeZDisplayValue,
                        isValueEditable = true,
                        onDisplayedValueChange = { /* Typing handled internally */ },
                        onValueCommit = { committedValue ->
                            committedValue.toFloatOrNull()?.let { value ->
                                val coercedValue = value.coerceIn(0f, 50f)
                                LFOamplitudeZValue = coercedValue
                                LFOamplitudeZDisplayValue = String.format(Locale.US, "%.2f", coercedValue)
                                val normalized = coercedValue / 50f
                                selectedChannel.setParameter("LFOamplitudeZ", InputParameterValue(
                                    normalizedValue = normalized,
                                    stringValue = "",
                                    displayValue = "${String.format(Locale.US, "%.2f", coercedValue)}m"
                                ))
                                viewModel.sendInputParameterFloat("/remoteInput/LFOamplitudeZ", inputId, coercedValue)
                            }
                        },
                        valueUnit = "m",
                        valueTextColor = if (isAmplitudeZEnabled) Color.White else Color.Gray
                    )
                    }
                }

                // Phase Z
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Phase Z", fontSize = 12.sp, color = if (isPhaseZEnabled) Color.White else Color.Gray)
                    PhaseDial(
                        value = LFOphaseZValue,
                        onValueChange = { newValue ->
                            LFOphaseZValue = newValue
                            val normalized = newValue / 360f
                            selectedChannel.setParameter("LFOphaseZ", InputParameterValue(
                                normalizedValue = normalized,
                                stringValue = "",
                                displayValue = "${newValue.toInt()}°"
                            ))
                            viewModel.sendInputParameterInt("/remoteInput/LFOphaseZ", inputId, newValue.toInt())
                        },
                        dialColor = if (isPhaseZEnabled) Color.DarkGray else Color(0xFF2A2A2A),
                        indicatorColor = if (isPhaseZEnabled) Color.White else Color.Gray,
                        trackColor = if (isPhaseZEnabled) Color(0xFF9C27B0) else Color.DarkGray,
                        isValueEditable = true,
                        onDisplayedValueChange = {},
                        valueTextColor = if (isPhaseZEnabled) Color.White else Color.Gray,
                        enabled = true,
                        sizeMultiplier = 0.7f
                    )
                }
            }
        }
    }
}

// Helper composables from the original file
@Composable
private fun getResponsiveTextSizes(): ResponsiveTextSizes {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    
    val screenWidthDp = configuration.screenWidthDp.dp
    val screenDensity = density.density
    
    val baseHeaderSize = (screenWidthDp.value / 25f).coerceIn(14f, 24f)
    val baseBodySize = (screenWidthDp.value / 30f).coerceIn(12f, 20f)
    val baseSmallSize = (screenWidthDp.value / 40f).coerceIn(10f, 16f)
    
    val densityFactor = screenDensity.coerceIn(1f, 3f)
    val headerSize = (baseHeaderSize * densityFactor).coerceIn(14f, 24f).sp
    val bodySize = (baseBodySize * densityFactor).coerceIn(12f, 20f).sp
    val smallSize = (baseSmallSize * densityFactor).coerceIn(10f, 16f).sp
    
    return ResponsiveTextSizes(
        headerSize = headerSize,
        bodySize = bodySize,
        smallSize = smallSize
    )
}

@Composable
private fun getResponsiveSpacing(): ResponsiveSpacing {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    
    val screenWidthDp = configuration.screenWidthDp.dp
    val screenDensity = density.density
    
    val basePadding = (screenWidthDp.value / 25f).coerceIn(12f, 24f)
    val baseSmallSpacing = (screenWidthDp.value / 50f).coerceIn(6f, 12f)
    val baseLargeSpacing = (screenWidthDp.value / 20f).coerceIn(16f, 32f)
    
    val densityFactor = screenDensity.coerceIn(1f, 2f)
    val padding = (basePadding * densityFactor).coerceIn(12f, 24f).dp
    val smallSpacing = (baseSmallSpacing * densityFactor).coerceIn(6f, 12f).dp
    val largeSpacing = (baseLargeSpacing * densityFactor).coerceIn(16f, 32f).dp
    
    return ResponsiveSpacing(
        padding = padding,
        smallSpacing = smallSpacing,
        largeSpacing = largeSpacing
    )
}

private data class ResponsiveTextSizes(
    val headerSize: androidx.compose.ui.unit.TextUnit,
    val bodySize: androidx.compose.ui.unit.TextUnit,
    val smallSize: androidx.compose.ui.unit.TextUnit
)

private data class ResponsiveSpacing(
    val padding: androidx.compose.ui.unit.Dp,
    val smallSpacing: androidx.compose.ui.unit.Dp,
    val largeSpacing: androidx.compose.ui.unit.Dp
)
