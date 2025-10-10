package com.wfsdiy.wfs_control_2

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.pow
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
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(spacing.padding)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.smallSpacing)
    ) {
        // Input Channel Selector
        InputChannelSelector(
            selectedInputId = inputParametersState.selectedInputId,
            maxInputs = numberOfInputs,
            onInputSelected = { inputId ->
                viewModel.setSelectedInput(inputId)
                viewModel.requestInputParameters(inputId)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = spacing.largeSpacing)
        )
        
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
        
        // Directivity Group
        ParameterSectionHeader(title = "Directivity")
        
        RenderDirectivitySection(
            selectedChannel = selectedChannel,
            viewModel = viewModel,
            horizontalSliderWidth = horizontalSliderWidth,
            horizontalSliderHeight = horizontalSliderHeight,
            verticalSliderWidth = verticalSliderWidth,
            verticalSliderHeight = verticalSliderHeight,
            spacing = spacing
        )
        
        // Live Source Attenuation Group
        ParameterSectionHeader(title = "Live Source Attenuation")
        
        RenderLiveSourceSection(
            selectedChannel = selectedChannel,
            viewModel = viewModel,
            horizontalSliderWidth = horizontalSliderWidth,
            horizontalSliderHeight = horizontalSliderHeight,
            verticalSliderWidth = verticalSliderWidth,
            verticalSliderHeight = verticalSliderHeight,
            spacing = spacing
        )
        
        // Floor Reflections Group
        ParameterSectionHeader(title = "Floor Reflections")
        
        RenderFloorReflectionsSection(
            selectedChannel = selectedChannel,
            viewModel = viewModel,
            horizontalSliderWidth = horizontalSliderWidth,
            horizontalSliderHeight = horizontalSliderHeight,
            verticalSliderWidth = verticalSliderWidth,
            verticalSliderHeight = verticalSliderHeight,
            spacing = spacing
        )
        
        // Jitter Group
        ParameterSectionHeader(title = "Jitter")
        
        RenderJitterSection(
            selectedChannel = selectedChannel,
            viewModel = viewModel,
            horizontalSliderWidth = horizontalSliderWidth,
            horizontalSliderHeight = horizontalSliderHeight,
            spacing = spacing
        )
        
        // LFO Group
        ParameterSectionHeader(title = "LFO")
        
        RenderLFOSection(
            selectedChannel = selectedChannel,
            viewModel = viewModel,
            horizontalSliderWidth = horizontalSliderWidth,
            horizontalSliderHeight = horizontalSliderHeight,
            verticalSliderWidth = verticalSliderWidth,
            verticalSliderHeight = verticalSliderHeight,
            spacing = spacing
        )
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
    val inputId = selectedChannel.inputId
    
    // Input Name
    val inputName = selectedChannel.getParameter("inputName")
    var inputNameValue by remember { mutableStateOf(inputName.stringValue) }
    
    LaunchedEffect(inputId) {
        inputNameValue = selectedChannel.getParameter("inputName").stringValue
    }
    
    ParameterTextBox(
        label = "Input Name",
        value = inputNameValue,
        onValueChange = { newValue ->
            inputNameValue = newValue
            selectedChannel.setParameter("inputName", InputParameterValue(
                normalizedValue = 0f,
                stringValue = newValue,
                displayValue = newValue
            ))
            viewModel.sendInputParameterString("/remoteInput/inputName", inputId, newValue)
        },
        modifier = Modifier.fillMaxWidth()
    )
    
    Spacer(modifier = Modifier.height(spacing.smallSpacing))
    
    // Attenuation
    val attenuation = selectedChannel.getParameter("attenuation")
    var attenuationValue by remember { mutableStateOf(attenuation.normalizedValue) }
    var attenuationDisplayValue by remember { mutableStateOf("0.00") }
    
    LaunchedEffect(inputId) {
        attenuationValue = selectedChannel.getParameter("attenuation").normalizedValue
        val definition = InputParameterDefinitions.parametersByVariableName["attenuation"]!!
        val actualValue = InputParameterDefinitions.applyFormula(definition, attenuationValue)
        attenuationDisplayValue = String.format("%.2f", actualValue)
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
                    attenuationDisplayValue = String.format("%.2f", actualValue)
                    selectedChannel.setParameter("attenuation", InputParameterValue(
                        normalizedValue = newValue,
                        stringValue = "",
                        displayValue = "${String.format("%.2f", actualValue)}dB"
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
                        attenuationDisplayValue = String.format("%.2f", coercedValue)
                        selectedChannel.setParameter("attenuation", InputParameterValue(
                            normalizedValue = normalized,
                            stringValue = "",
                            displayValue = "${String.format("%.2f", coercedValue)}dB"
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
    var delayLatencyValue by remember { mutableStateOf(0f) } // -100 to 100 range directly
    var delayLatencyDisplayValue by remember { mutableStateOf("0.00") }
    
    LaunchedEffect(inputId) {
        val definition = InputParameterDefinitions.parametersByVariableName["delayLatency"]!!
        val currentParam = selectedChannel.getParameter("delayLatency")
        val actualValue = InputParameterDefinitions.applyFormula(definition, currentParam.normalizedValue)
        delayLatencyValue = actualValue
        delayLatencyDisplayValue = String.format("%.2f", actualValue)
    }
    
    Column {
        Text("Delay/Latency comp.", fontSize = 12.sp, color = Color.White)
        BidirectionalSlider(
            value = delayLatencyValue,
            onValueChange = { newValue ->
                delayLatencyValue = newValue
                delayLatencyDisplayValue = String.format("%.2f", newValue)
                val normalized = (newValue + 100f) / 200f
                selectedChannel.setParameter("delayLatency", InputParameterValue(
                    normalizedValue = normalized,
                    stringValue = "",
                    displayValue = "${String.format("%.2f", newValue)}ms"
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
                    delayLatencyDisplayValue = String.format("%.2f", coercedValue)
                    val normalized = (coercedValue + 100f) / 200f
                    selectedChannel.setParameter("delayLatency", InputParameterValue(
                        normalizedValue = normalized,
                        stringValue = "",
                        displayValue = "${String.format("%.2f", coercedValue)}ms"
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
        mutableStateOf(minimalLatency.normalizedValue.toInt().coerceIn(0, 1)) 
    }
    
    LaunchedEffect(inputId) {
        minLatencyIndex = selectedChannel.getParameter("minimalLatency").normalizedValue.toInt().coerceIn(0, 1)
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
    
    // Position X, Y, Z
    val positionX = selectedChannel.getParameter("positionX")
    var positionXValue by remember { 
        mutableStateOf(positionX.displayValue.replace("m", "").trim()) 
    }
    
    LaunchedEffect(inputId) {
        positionXValue = selectedChannel.getParameter("positionX").displayValue.replace("m", "").trim()
    }
    
    ParameterNumberBox(
        label = "Position X",
        value = positionXValue,
        onValueChange = { newValue ->
            positionXValue = newValue
            newValue.toFloatOrNull()?.let { value ->
                val coerced = value.coerceIn(0f, 50f)
                selectedChannel.setParameter("positionX", InputParameterValue(
                    normalizedValue = coerced / 50f,
                    stringValue = "",
                    displayValue = "${String.format("%.2f", coerced)}m"
                ))
                viewModel.sendInputParameterFloat("/remoteInput/positionX", inputId, coerced)
            }
        },
        unit = "m",
        modifier = Modifier.fillMaxWidth()
    )
    
    Spacer(modifier = Modifier.height(spacing.smallSpacing))
    
    val positionY = selectedChannel.getParameter("positionY")
    var positionYValue by remember { 
        mutableStateOf(positionY.displayValue.replace("m", "").trim()) 
    }
    
    LaunchedEffect(inputId) {
        positionYValue = selectedChannel.getParameter("positionY").displayValue.replace("m", "").trim()
    }
    
    ParameterNumberBox(
        label = "Position Y",
        value = positionYValue,
        onValueChange = { newValue ->
            positionYValue = newValue
            newValue.toFloatOrNull()?.let { value ->
                val coerced = value.coerceIn(0f, 50f)
                selectedChannel.setParameter("positionY", InputParameterValue(
                    normalizedValue = coerced / 50f,
                    stringValue = "",
                    displayValue = "${String.format("%.2f", coerced)}m"
                ))
                viewModel.sendInputParameterFloat("/remoteInput/positionY", inputId, coerced)
            }
        },
        unit = "m",
        modifier = Modifier.fillMaxWidth()
    )
    
    Spacer(modifier = Modifier.height(spacing.smallSpacing))
    
    val positionZ = selectedChannel.getParameter("positionZ")
    var positionZValue by remember { 
        mutableStateOf(positionZ.displayValue.replace("m", "").trim()) 
    }
    
    LaunchedEffect(inputId) {
        positionZValue = selectedChannel.getParameter("positionZ").displayValue.replace("m", "").trim()
    }
    
    ParameterNumberBox(
        label = "Position Z",
        value = positionZValue,
        onValueChange = { newValue ->
            positionZValue = newValue
            newValue.toFloatOrNull()?.let { value ->
                val coerced = value.coerceIn(0f, 50f)
                selectedChannel.setParameter("positionZ", InputParameterValue(
                    normalizedValue = coerced / 50f,
                    stringValue = "",
                    displayValue = "${String.format("%.2f", coerced)}m"
                ))
                viewModel.sendInputParameterFloat("/remoteInput/positionZ", inputId, coerced)
            }
        },
        unit = "m",
        modifier = Modifier.fillMaxWidth()
    )
    
    Spacer(modifier = Modifier.height(spacing.smallSpacing))
    
    // Cluster
    val cluster = selectedChannel.getParameter("cluster")
    var clusterIndex by remember { 
        mutableStateOf(cluster.normalizedValue.toInt().coerceIn(0, 10)) 
    }
    
    LaunchedEffect(inputId) {
        clusterIndex = selectedChannel.getParameter("cluster").normalizedValue.toInt().coerceIn(0, 10)
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
        mutableStateOf(maxSpeedActive.normalizedValue.toInt().coerceIn(0, 1)) 
    }
    
    LaunchedEffect(inputId) {
        maxSpeedActiveIndex = selectedChannel.getParameter("maxSpeedActive").normalizedValue.toInt().coerceIn(0, 1)
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
            viewModel.sendInputParameterInt("/remoteInput/maxSpeedActive", inputId, index)
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
    LaunchedEffect(inputId) {
        val currentMaxSpeed = selectedChannel.getParameter("maxSpeed")
        maxSpeedValue = currentMaxSpeed.normalizedValue
        maxSpeedDisplayValue = currentMaxSpeed.displayValue.replace("m/s", "").trim().ifEmpty { "0.01" }
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
                maxSpeedDisplayValue = String.format("%.2f", actualValue)
                
                // Update the state in the channel
                val updatedValue = InputParameterValue(
                    normalizedValue = newValue,
                    stringValue = "",
                    displayValue = "${String.format("%.2f", actualValue)}m/s"
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
                    maxSpeedDisplayValue = String.format("%.2f", coercedValue)
                    
                    // Update the state in the channel
                    val updatedValue = InputParameterValue(
                        normalizedValue = normalized,
                        stringValue = "",
                        displayValue = "${String.format("%.2f", coercedValue)}m/s"
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
    
    // Add more Input group parameters here...
    // Height Factor, Attenuation Law, Distance Attenuation/Ratio, Common Attenuation
}

@Composable
private fun RenderDirectivitySection(
    selectedChannel: InputChannelState,
    viewModel: MainActivityViewModel,
    horizontalSliderWidth: androidx.compose.ui.unit.Dp,
    horizontalSliderHeight: androidx.compose.ui.unit.Dp,
    verticalSliderWidth: androidx.compose.ui.unit.Dp,
    verticalSliderHeight: androidx.compose.ui.unit.Dp,
    spacing: ResponsiveSpacing
) {
    val inputId = selectedChannel.inputId
    
    // Directivity (Width Expansion Slider - grows from center)
    val directivity = selectedChannel.getParameter("directivity")
    var directivityValue by remember { mutableStateOf(0f) } // 0-1 where it expands from center
    var directivityDisplayValue by remember { mutableStateOf("2") }
    
    LaunchedEffect(inputId) {
        val definition = InputParameterDefinitions.parametersByVariableName["directivity"]!!
        val currentParam = selectedChannel.getParameter("directivity")
        val actualValue = InputParameterDefinitions.applyFormula(definition, currentParam.normalizedValue)
        // Map 2-360 to 0-1 expansion value (2 = 0, 360 = 1)
        directivityValue = (actualValue - 2f) / 358f
        directivityDisplayValue = actualValue.toInt().toString()
    }
    
    Column {
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
                committedValue.toIntOrNull()?.let { value ->
                    val coercedValue = value.toFloat().coerceIn(2f, 360f)
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
    
    Spacer(modifier = Modifier.height(spacing.smallSpacing))
    
    // Rotation
    val rotation = selectedChannel.getParameter("rotation")
    var rotationValue by remember { mutableStateOf((rotation.normalizedValue * 360f) - 180f) }
    
    LaunchedEffect(inputId) {
        val currentRotation = selectedChannel.getParameter("rotation")
        rotationValue = (currentRotation.normalizedValue * 360f) - 180f
    }
    
    Column {
        Text("Rotation", fontSize = 12.sp, color = Color.White)
        AngleDial(
            value = rotationValue,
            onValueChange = { newValue ->
                rotationValue = newValue
                selectedChannel.setParameter("rotation", InputParameterValue(
                    normalizedValue = (newValue + 180f) / 360f,
                    stringValue = "",
                    displayValue = "${newValue.toInt()}°"
                ))
                viewModel.sendInputParameterInt("/remoteInput/rotation", inputId, newValue.toInt())
            },
            dialColor = Color.DarkGray,
            indicatorColor = Color.White,
            trackColor = Color(0xFF4CAF50),
            isValueEditable = true,
            onDisplayedValueChange = {},
            valueTextColor = Color.White,
            enabled = true
        )
    }
    
    // Add Tilt and HF Shelf here...
}

@Composable
private fun RenderLiveSourceSection(
    selectedChannel: InputChannelState,
    viewModel: MainActivityViewModel,
    horizontalSliderWidth: androidx.compose.ui.unit.Dp,
    horizontalSliderHeight: androidx.compose.ui.unit.Dp,
    verticalSliderWidth: androidx.compose.ui.unit.Dp,
    verticalSliderHeight: androidx.compose.ui.unit.Dp,
    spacing: ResponsiveSpacing
) {
    val inputId = selectedChannel.inputId
    
    // Active
    val liveSourceActive = selectedChannel.getParameter("liveSourceActive")
    var liveSourceActiveIndex by remember { 
        mutableStateOf(liveSourceActive.normalizedValue.toInt().coerceIn(0, 1)) 
    }
    
    LaunchedEffect(inputId) {
        liveSourceActiveIndex = selectedChannel.getParameter("liveSourceActive").normalizedValue.toInt().coerceIn(0, 1)
    }
    
    val isLiveSourceEnabled = liveSourceActiveIndex == 0 // 0 = ON, 1 = OFF
    
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
            viewModel.sendInputParameterInt("/remoteInput/liveSourceActive", inputId, index)
        },
        modifier = Modifier.fillMaxWidth()
    )
    
    Spacer(modifier = Modifier.height(spacing.smallSpacing))
    
    // Radius (greyed out when inactive) - Width Expansion Slider
    val radius = selectedChannel.getParameter("liveSourceRadius")
    var radiusValue by remember { mutableStateOf(0f) } // 0-1 expansion value
    var radiusDisplayValue by remember { mutableStateOf("0.00") }
    
    LaunchedEffect(inputId) {
        val definition = InputParameterDefinitions.parametersByVariableName["liveSourceRadius"]!!
        val currentParam = selectedChannel.getParameter("liveSourceRadius")
        val actualValue = InputParameterDefinitions.applyFormula(definition, currentParam.normalizedValue)
        // Map 0-50 to 0-1 expansion value
        radiusValue = actualValue / 50f
        radiusDisplayValue = String.format("%.2f", actualValue)
    }
    
    Column {
        Text("Radius", fontSize = 12.sp, color = if (isLiveSourceEnabled) Color.White else Color.Gray)
        WidthExpansionSlider(
            value = radiusValue,
            onValueChange = { newValue ->
                radiusValue = newValue
                // Map 0-1 expansion to 0-50 meters
                val actualValue = newValue * 50f
                radiusDisplayValue = String.format("%.2f", actualValue)
                val normalized = actualValue / 50f
                selectedChannel.setParameter("liveSourceRadius", InputParameterValue(
                    normalizedValue = normalized,
                    stringValue = "",
                    displayValue = "${String.format("%.2f", actualValue)}m"
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
                    radiusDisplayValue = String.format("%.2f", coercedValue)
                    selectedChannel.setParameter("liveSourceRadius", InputParameterValue(
                        normalizedValue = expansionValue,
                        stringValue = "",
                        displayValue = "${String.format("%.2f", coercedValue)}m"
                    ))
                    viewModel.sendInputParameterFloat("/remoteInput/liveSourceRadius", inputId, coercedValue)
                }
            },
            valueUnit = "m",
            valueTextColor = if (isLiveSourceEnabled) Color.White else Color.Gray,
            enabled = true // Always enabled, just greyed out visually
        )
    }
    
    // Add remaining Live Source Attenuation parameters here...
    // Shape, Attenuation, Peak Threshold, Peak Ratio, Slow Threshold, Slow Ratio
}

@Composable
private fun RenderFloorReflectionsSection(
    selectedChannel: InputChannelState,
    viewModel: MainActivityViewModel,
    horizontalSliderWidth: androidx.compose.ui.unit.Dp,
    horizontalSliderHeight: androidx.compose.ui.unit.Dp,
    verticalSliderWidth: androidx.compose.ui.unit.Dp,
    verticalSliderHeight: androidx.compose.ui.unit.Dp,
    spacing: ResponsiveSpacing
) {
    val inputId = selectedChannel.inputId
    
    // Active
    val FRactive = selectedChannel.getParameter("FRactive")
    var FRactiveIndex by remember { 
        mutableStateOf(FRactive.normalizedValue.toInt().coerceIn(0, 1)) 
    }
    
    LaunchedEffect(inputId) {
        FRactiveIndex = selectedChannel.getParameter("FRactive").normalizedValue.toInt().coerceIn(0, 1)
    }
    
    val isFREnabled = FRactiveIndex == 0 // 0 = ON, 1 = OFF
    
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
            viewModel.sendInputParameterInt("/remoteInput/FRactive", inputId, index)
        },
        modifier = Modifier.fillMaxWidth()
    )
    
    // Add remaining Floor Reflections parameters here...
    // Attenuation, Low Cut Active, Low Cut Freq, High Shelf Active, High Shelf Freq, High Shelf Gain, High Shelf Slope, Diffusion
}

@Composable
private fun RenderJitterSection(
    selectedChannel: InputChannelState,
    viewModel: MainActivityViewModel,
    horizontalSliderWidth: androidx.compose.ui.unit.Dp,
    horizontalSliderHeight: androidx.compose.ui.unit.Dp,
    spacing: ResponsiveSpacing
) {
    val inputId = selectedChannel.inputId
    
    // Jitter - Width Expansion Slider
    val jitter = selectedChannel.getParameter("jitter")
    var jitterValue by remember { mutableStateOf(0f) } // 0-1 expansion value
    var jitterDisplayValue by remember { mutableStateOf("0.00") }
    
    LaunchedEffect(inputId) {
        val definition = InputParameterDefinitions.parametersByVariableName["jitter"]!!
        val currentParam = selectedChannel.getParameter("jitter")
        val actualValue = InputParameterDefinitions.applyFormula(definition, currentParam.normalizedValue)
        // Map 0-10 to 0-1 expansion value (formula uses pow(x,2), so reverse it)
        // Since formula is 10*pow(x,2), we have actualValue = 10*x^2, so x = sqrt(actualValue/10)
        jitterValue = if (actualValue > 0) kotlin.math.sqrt(actualValue / 10f) else 0f
        jitterDisplayValue = String.format("%.2f", actualValue)
    }
    
    Column {
        Text("Jitter", fontSize = 12.sp, color = Color.White)
        WidthExpansionSlider(
            value = jitterValue,
            onValueChange = { newValue ->
                jitterValue = newValue
                // Map 0-1 expansion using the formula: 10*pow(x,2)
                val actualValue = 10f * newValue.pow(2)
                jitterDisplayValue = String.format("%.2f", actualValue)
                // For normalized storage, we use the sqrt of the normalized actual value
                val normalized = newValue
                selectedChannel.setParameter("jitter", InputParameterValue(
                    normalizedValue = normalized,
                    stringValue = "",
                    displayValue = "${String.format("%.2f", actualValue)}m"
                ))
                viewModel.sendInputParameterFloat("/remoteInput/jitter", inputId, actualValue)
            },
            modifier = Modifier.width(horizontalSliderWidth).height(horizontalSliderHeight),
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
                    jitterDisplayValue = String.format("%.2f", coercedValue)
                    selectedChannel.setParameter("jitter", InputParameterValue(
                        normalizedValue = expansionValue,
                        stringValue = "",
                        displayValue = "${String.format("%.2f", coercedValue)}m"
                    ))
                    viewModel.sendInputParameterFloat("/remoteInput/jitter", inputId, coercedValue)
                }
            },
            valueUnit = "m",
            valueTextColor = Color.White
        )
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
    spacing: ResponsiveSpacing
) {
    val inputId = selectedChannel.inputId
    
    // Active
    val LFOactive = selectedChannel.getParameter("LFOactive")
    var LFOactiveIndex by remember { 
        mutableStateOf(LFOactive.normalizedValue.toInt().coerceIn(0, 1)) 
    }
    
    LaunchedEffect(inputId) {
        LFOactiveIndex = selectedChannel.getParameter("LFOactive").normalizedValue.toInt().coerceIn(0, 1)
    }
    
    val isLFOEnabled = LFOactiveIndex == 0 // 0 = ON, 1 = OFF
    
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
            viewModel.sendInputParameterInt("/remoteInput/LFOactive", inputId, index)
        },
        modifier = Modifier.fillMaxWidth()
    )
    
    // Add remaining LFO parameters here...
    // Period, Phase, Shape X/Y/Z, Rate X/Y/Z, Amplitude X/Y/Z, Phase X/Y/Z, Gyrophone
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
