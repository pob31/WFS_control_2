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
    
    Spacer(modifier = Modifier.height(spacing.smallSpacing))
    
    // Height Factor
    val heightFactor = selectedChannel.getParameter("heightFactor")
    var heightFactorValue by remember { mutableStateOf(heightFactor.normalizedValue) }
    var heightFactorDisplayValue by remember { 
        mutableStateOf(heightFactor.displayValue.replace("%", "").trim().ifEmpty { "0" }) 
    }
    
    LaunchedEffect(inputId) {
        val current = selectedChannel.getParameter("heightFactor")
        heightFactorValue = current.normalizedValue
        val definition = InputParameterDefinitions.parametersByVariableName["heightFactor"]!!
        val actualValue = InputParameterDefinitions.applyFormula(definition, current.normalizedValue)
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
                committedValue.toIntOrNull()?.let { value ->
                    val coercedValue = value.coerceIn(0, 100)
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
        mutableStateOf(attenuationLaw.normalizedValue.toInt().coerceIn(0, 1)) 
    }
    
    LaunchedEffect(inputId) {
        attenuationLawIndex = selectedChannel.getParameter("attenuationLaw").normalizedValue.toInt().coerceIn(0, 1)
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
        
        LaunchedEffect(inputId) {
            val current = selectedChannel.getParameter("distanceAttenuation")
            distanceAttenuationValue = current.normalizedValue
            val definition = InputParameterDefinitions.parametersByVariableName["distanceAttenuation"]!!
            val actualValue = InputParameterDefinitions.applyFormula(definition, current.normalizedValue)
            distanceAttenuationDisplayValue = String.format("%.2f", actualValue)
        }
        
        Column {
            Text("Distance Attenuation", fontSize = 12.sp, color = Color.White)
            BasicDial(
                value = distanceAttenuationValue,
                onValueChange = { newValue ->
                    distanceAttenuationValue = newValue
                    val definition = InputParameterDefinitions.parametersByVariableName["distanceAttenuation"]!!
                    val actualValue = InputParameterDefinitions.applyFormula(definition, newValue)
                    distanceAttenuationDisplayValue = String.format("%.2f", actualValue)
                    selectedChannel.setParameter("distanceAttenuation", InputParameterValue(
                        normalizedValue = newValue,
                        stringValue = "",
                        displayValue = "${String.format("%.2f", actualValue)}dB/m"
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
                        distanceAttenuationDisplayValue = String.format("%.2f", coercedValue)
                        selectedChannel.setParameter("distanceAttenuation", InputParameterValue(
                            normalizedValue = normalized,
                            stringValue = "",
                            displayValue = "${String.format("%.2f", coercedValue)}dB/m"
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
        
        LaunchedEffect(inputId) {
            val current = selectedChannel.getParameter("distanceRatio")
            distanceRatioValue = current.normalizedValue
            val definition = InputParameterDefinitions.parametersByVariableName["distanceRatio"]!!
            val actualValue = InputParameterDefinitions.applyFormula(definition, current.normalizedValue)
            distanceRatioDisplayValue = String.format("%.2f", actualValue)
        }
        
        Column {
            Text("Distance Ratio", fontSize = 12.sp, color = Color.White)
            BasicDial(
                value = distanceRatioValue,
                onValueChange = { newValue ->
                    distanceRatioValue = newValue
                    val definition = InputParameterDefinitions.parametersByVariableName["distanceRatio"]!!
                    val actualValue = InputParameterDefinitions.applyFormula(definition, newValue)
                    distanceRatioDisplayValue = String.format("%.2f", actualValue)
                    selectedChannel.setParameter("distanceRatio", InputParameterValue(
                        normalizedValue = newValue,
                        stringValue = "",
                        displayValue = "${String.format("%.2f", actualValue)}x"
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
                        distanceRatioDisplayValue = String.format("%.2f", coercedValue)
                        selectedChannel.setParameter("distanceRatio", InputParameterValue(
                            normalizedValue = normalized,
                            stringValue = "",
                            displayValue = "${String.format("%.2f", coercedValue)}x"
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
    
    LaunchedEffect(inputId) {
        val current = selectedChannel.getParameter("commonAtten")
        commonAttenValue = current.normalizedValue
        val definition = InputParameterDefinitions.parametersByVariableName["commonAtten"]!!
        val actualValue = InputParameterDefinitions.applyFormula(definition, current.normalizedValue)
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
                committedValue.toIntOrNull()?.let { value ->
                    val coercedValue = value.coerceIn(0, 100)
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
    
    Spacer(modifier = Modifier.height(spacing.smallSpacing))
    
    // Tilt
    val tilt = selectedChannel.getParameter("tilt")
    var tiltValue by remember { mutableStateOf(0f) } // -90 to 90 range directly
    var tiltDisplayValue by remember { mutableStateOf("0") }
    
    LaunchedEffect(inputId) {
        val definition = InputParameterDefinitions.parametersByVariableName["tilt"]!!
        val currentParam = selectedChannel.getParameter("tilt")
        val actualValue = InputParameterDefinitions.applyFormula(definition, currentParam.normalizedValue)
        tiltValue = actualValue
        tiltDisplayValue = actualValue.toInt().toString()
    }
    
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                    committedValue.toIntOrNull()?.let { value ->
                        val coercedValue = value.toFloat().coerceIn(-90f, 90f)
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
    }
    
    Spacer(modifier = Modifier.height(spacing.smallSpacing))
    
    // HF Shelf
    val HFshelf = selectedChannel.getParameter("HFshelf")
    var HFshelfValue by remember { mutableStateOf(HFshelf.normalizedValue) }
    var HFshelfDisplayValue by remember { mutableStateOf("0.00") }
    
    LaunchedEffect(inputId) {
        HFshelfValue = selectedChannel.getParameter("HFshelf").normalizedValue
        val definition = InputParameterDefinitions.parametersByVariableName["HFshelf"]!!
        val actualValue = InputParameterDefinitions.applyFormula(definition, HFshelfValue)
        HFshelfDisplayValue = String.format("%.2f", actualValue)
    }
    
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("HF Shelf", fontSize = 12.sp, color = Color.White)
            StandardSlider(
                value = HFshelfValue,
                onValueChange = { newValue ->
                    HFshelfValue = newValue
                    val definition = InputParameterDefinitions.parametersByVariableName["HFshelf"]!!
                    val actualValue = InputParameterDefinitions.applyFormula(definition, newValue)
                    HFshelfDisplayValue = String.format("%.2f", actualValue)
                    selectedChannel.setParameter("HFshelf", InputParameterValue(
                        normalizedValue = newValue,
                        stringValue = "",
                        displayValue = "${String.format("%.2f", actualValue)}dB"
                    ))
                    viewModel.sendInputParameterFloat("/remoteInput/HFshelf", inputId, actualValue)
                },
                modifier = Modifier.height(verticalSliderHeight),
                sliderColor = Color(0xFF00BCD4),
                trackBackgroundColor = Color.DarkGray,
                orientation = SliderOrientation.VERTICAL,
                displayedValue = HFshelfDisplayValue,
                isValueEditable = true,
                onDisplayedValueChange = { /* Typing handled internally */ },
                onValueCommit = { committedValue ->
                    committedValue.toFloatOrNull()?.let { value ->
                        val definition = InputParameterDefinitions.parametersByVariableName["HFshelf"]!!
                        val coercedValue = value.coerceIn(definition.minValue, definition.maxValue)
                        val normalized = InputParameterDefinitions.reverseFormula(definition, coercedValue)
                        HFshelfValue = normalized
                        HFshelfDisplayValue = String.format("%.2f", coercedValue)
                        selectedChannel.setParameter("HFshelf", InputParameterValue(
                            normalizedValue = normalized,
                            stringValue = "",
                            displayValue = "${String.format("%.2f", coercedValue)}dB"
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
    
    Spacer(modifier = Modifier.height(spacing.smallSpacing))
    
    // Shape
    val liveSourceShape = selectedChannel.getParameter("liveSourceShape")
    var liveSourceShapeIndex by remember { 
        mutableStateOf(liveSourceShape.normalizedValue.toInt().coerceIn(0, 3)) 
    }
    
    LaunchedEffect(inputId) {
        liveSourceShapeIndex = selectedChannel.getParameter("liveSourceShape").normalizedValue.toInt().coerceIn(0, 3)
    }
    
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
    
    Spacer(modifier = Modifier.height(spacing.smallSpacing))
    
    // Attenuation
    val liveSourceAttenuation = selectedChannel.getParameter("liveSourceAttenuation")
    var liveSourceAttenuationValue by remember { mutableStateOf(liveSourceAttenuation.normalizedValue) }
    var liveSourceAttenuationDisplayValue by remember { mutableStateOf("0.00") }
    
    LaunchedEffect(inputId) {
        liveSourceAttenuationValue = selectedChannel.getParameter("liveSourceAttenuation").normalizedValue
        val definition = InputParameterDefinitions.parametersByVariableName["liveSourceAttenuation"]!!
        val actualValue = InputParameterDefinitions.applyFormula(definition, liveSourceAttenuationValue)
        liveSourceAttenuationDisplayValue = String.format("%.2f", actualValue)
    }
    
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Attenuation", fontSize = 12.sp, color = if (isLiveSourceEnabled) Color.White else Color.Gray)
            StandardSlider(
                value = liveSourceAttenuationValue,
                onValueChange = { newValue ->
                    liveSourceAttenuationValue = newValue
                    val definition = InputParameterDefinitions.parametersByVariableName["liveSourceAttenuation"]!!
                    val actualValue = InputParameterDefinitions.applyFormula(definition, newValue)
                    liveSourceAttenuationDisplayValue = String.format("%.2f", actualValue)
                    selectedChannel.setParameter("liveSourceAttenuation", InputParameterValue(
                        normalizedValue = newValue,
                        stringValue = "",
                        displayValue = "${String.format("%.2f", actualValue)}dB"
                    ))
                    viewModel.sendInputParameterFloat("/remoteInput/liveSourceAttenuation", inputId, actualValue)
                },
                modifier = Modifier.height(verticalSliderHeight),
                sliderColor = if (isLiveSourceEnabled) Color(0xFF9C27B0) else Color.Gray,
                trackBackgroundColor = Color.DarkGray,
                orientation = SliderOrientation.VERTICAL,
                displayedValue = liveSourceAttenuationDisplayValue,
                isValueEditable = true,
                onDisplayedValueChange = { /* Typing handled internally */ },
                onValueCommit = { committedValue ->
                    committedValue.toFloatOrNull()?.let { value ->
                        val definition = InputParameterDefinitions.parametersByVariableName["liveSourceAttenuation"]!!
                        val coercedValue = value.coerceIn(definition.minValue, definition.maxValue)
                        val normalized = InputParameterDefinitions.reverseFormula(definition, coercedValue)
                        liveSourceAttenuationValue = normalized
                        liveSourceAttenuationDisplayValue = String.format("%.2f", coercedValue)
                        selectedChannel.setParameter("liveSourceAttenuation", InputParameterValue(
                            normalizedValue = normalized,
                            stringValue = "",
                            displayValue = "${String.format("%.2f", coercedValue)}dB"
                        ))
                        viewModel.sendInputParameterFloat("/remoteInput/liveSourceAttenuation", inputId, coercedValue)
                    }
                },
                valueUnit = "dB",
                valueTextColor = if (isLiveSourceEnabled) Color.White else Color.Gray
            )
        }
    }
    
    Spacer(modifier = Modifier.height(spacing.smallSpacing))
    
    // Peak Threshold
    val liveSourcePeakThreshold = selectedChannel.getParameter("liveSourcePeakThreshold")
    var liveSourcePeakThresholdValue by remember { mutableStateOf(liveSourcePeakThreshold.normalizedValue) }
    var liveSourcePeakThresholdDisplayValue by remember { mutableStateOf("0.00") }
    
    LaunchedEffect(inputId) {
        liveSourcePeakThresholdValue = selectedChannel.getParameter("liveSourcePeakThreshold").normalizedValue
        val definition = InputParameterDefinitions.parametersByVariableName["liveSourcePeakThreshold"]!!
        val actualValue = InputParameterDefinitions.applyFormula(definition, liveSourcePeakThresholdValue)
        liveSourcePeakThresholdDisplayValue = String.format("%.2f", actualValue)
    }
    
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Peak Threshold", fontSize = 12.sp, color = if (isLiveSourceEnabled) Color.White else Color.Gray)
            StandardSlider(
                value = liveSourcePeakThresholdValue,
                onValueChange = { newValue ->
                    liveSourcePeakThresholdValue = newValue
                    val definition = InputParameterDefinitions.parametersByVariableName["liveSourcePeakThreshold"]!!
                    val actualValue = InputParameterDefinitions.applyFormula(definition, newValue)
                    liveSourcePeakThresholdDisplayValue = String.format("%.2f", actualValue)
                    selectedChannel.setParameter("liveSourcePeakThreshold", InputParameterValue(
                        normalizedValue = newValue,
                        stringValue = "",
                        displayValue = "${String.format("%.2f", actualValue)}dB"
                    ))
                    viewModel.sendInputParameterFloat("/remoteInput/liveSourcePeakThreshold", inputId, actualValue)
                },
                modifier = Modifier.height(verticalSliderHeight),
                sliderColor = if (isLiveSourceEnabled) Color(0xFFFF9800) else Color.Gray,
                trackBackgroundColor = Color.DarkGray,
                orientation = SliderOrientation.VERTICAL,
                displayedValue = liveSourcePeakThresholdDisplayValue,
                isValueEditable = true,
                onDisplayedValueChange = { /* Typing handled internally */ },
                onValueCommit = { committedValue ->
                    committedValue.toFloatOrNull()?.let { value ->
                        val definition = InputParameterDefinitions.parametersByVariableName["liveSourcePeakThreshold"]!!
                        val coercedValue = value.coerceIn(definition.minValue, definition.maxValue)
                        val normalized = InputParameterDefinitions.reverseFormula(definition, coercedValue)
                        liveSourcePeakThresholdValue = normalized
                        liveSourcePeakThresholdDisplayValue = String.format("%.2f", coercedValue)
                        selectedChannel.setParameter("liveSourcePeakThreshold", InputParameterValue(
                            normalizedValue = normalized,
                            stringValue = "",
                            displayValue = "${String.format("%.2f", coercedValue)}dB"
                        ))
                        viewModel.sendInputParameterFloat("/remoteInput/liveSourcePeakThreshold", inputId, coercedValue)
                    }
                },
                valueUnit = "dB",
                valueTextColor = if (isLiveSourceEnabled) Color.White else Color.Gray
            )
        }
    }
    
    Spacer(modifier = Modifier.height(spacing.smallSpacing))
    
    // Peak Ratio
    val liveSourcePeakRatio = selectedChannel.getParameter("liveSourcePeakRatio")
    var liveSourcePeakRatioValue by remember { mutableStateOf(liveSourcePeakRatio.normalizedValue) }
    var liveSourcePeakRatioDisplayValue by remember { 
        mutableStateOf(liveSourcePeakRatio.displayValue.replace("", "").trim().ifEmpty { "1.00" }) 
    }
    
    LaunchedEffect(inputId) {
        val current = selectedChannel.getParameter("liveSourcePeakRatio")
        liveSourcePeakRatioValue = current.normalizedValue
        val definition = InputParameterDefinitions.parametersByVariableName["liveSourcePeakRatio"]!!
        val actualValue = InputParameterDefinitions.applyFormula(definition, current.normalizedValue)
        liveSourcePeakRatioDisplayValue = String.format("%.2f", actualValue)
    }
    
    Column {
        Text("Peak Ratio", fontSize = 12.sp, color = if (isLiveSourceEnabled) Color.White else Color.Gray)
        BasicDial(
            value = liveSourcePeakRatioValue,
            onValueChange = { newValue ->
                liveSourcePeakRatioValue = newValue
                val definition = InputParameterDefinitions.parametersByVariableName["liveSourcePeakRatio"]!!
                val actualValue = InputParameterDefinitions.applyFormula(definition, newValue)
                liveSourcePeakRatioDisplayValue = String.format("%.2f", actualValue)
                selectedChannel.setParameter("liveSourcePeakRatio", InputParameterValue(
                    normalizedValue = newValue,
                    stringValue = "",
                    displayValue = String.format("%.2f", actualValue)
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
                    liveSourcePeakRatioDisplayValue = String.format("%.2f", coercedValue)
                    selectedChannel.setParameter("liveSourcePeakRatio", InputParameterValue(
                        normalizedValue = normalized,
                        stringValue = "",
                        displayValue = String.format("%.2f", coercedValue)
                    ))
                    viewModel.sendInputParameterFloat("/remoteInput/liveSourcePeakRatio", inputId, coercedValue)
                }
            },
            valueTextColor = if (isLiveSourceEnabled) Color.White else Color.Gray,
            enabled = true
        )
    }
    
    Spacer(modifier = Modifier.height(spacing.smallSpacing))
    
    // Slow Threshold
    val liveSourceSlowThreshold = selectedChannel.getParameter("liveSourceSlowThreshold")
    var liveSourceSlowThresholdValue by remember { mutableStateOf(liveSourceSlowThreshold.normalizedValue) }
    var liveSourceSlowThresholdDisplayValue by remember { mutableStateOf("0.00") }
    
    LaunchedEffect(inputId) {
        liveSourceSlowThresholdValue = selectedChannel.getParameter("liveSourceSlowThreshold").normalizedValue
        val definition = InputParameterDefinitions.parametersByVariableName["liveSourceSlowThreshold"]!!
        val actualValue = InputParameterDefinitions.applyFormula(definition, liveSourceSlowThresholdValue)
        liveSourceSlowThresholdDisplayValue = String.format("%.2f", actualValue)
    }
    
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Slow Threshold", fontSize = 12.sp, color = if (isLiveSourceEnabled) Color.White else Color.Gray)
            StandardSlider(
                value = liveSourceSlowThresholdValue,
                onValueChange = { newValue ->
                    liveSourceSlowThresholdValue = newValue
                    val definition = InputParameterDefinitions.parametersByVariableName["liveSourceSlowThreshold"]!!
                    val actualValue = InputParameterDefinitions.applyFormula(definition, newValue)
                    liveSourceSlowThresholdDisplayValue = String.format("%.2f", actualValue)
                    selectedChannel.setParameter("liveSourceSlowThreshold", InputParameterValue(
                        normalizedValue = newValue,
                        stringValue = "",
                        displayValue = "${String.format("%.2f", actualValue)}dB"
                    ))
                    viewModel.sendInputParameterFloat("/remoteInput/liveSourceSlowThreshold", inputId, actualValue)
                },
                modifier = Modifier.height(verticalSliderHeight),
                sliderColor = if (isLiveSourceEnabled) Color(0xFF4CAF50) else Color.Gray,
                trackBackgroundColor = Color.DarkGray,
                orientation = SliderOrientation.VERTICAL,
                displayedValue = liveSourceSlowThresholdDisplayValue,
                isValueEditable = true,
                onDisplayedValueChange = { /* Typing handled internally */ },
                onValueCommit = { committedValue ->
                    committedValue.toFloatOrNull()?.let { value ->
                        val definition = InputParameterDefinitions.parametersByVariableName["liveSourceSlowThreshold"]!!
                        val coercedValue = value.coerceIn(definition.minValue, definition.maxValue)
                        val normalized = InputParameterDefinitions.reverseFormula(definition, coercedValue)
                        liveSourceSlowThresholdValue = normalized
                        liveSourceSlowThresholdDisplayValue = String.format("%.2f", coercedValue)
                        selectedChannel.setParameter("liveSourceSlowThreshold", InputParameterValue(
                            normalizedValue = normalized,
                            stringValue = "",
                            displayValue = "${String.format("%.2f", coercedValue)}dB"
                        ))
                        viewModel.sendInputParameterFloat("/remoteInput/liveSourceSlowThreshold", inputId, coercedValue)
                    }
                },
                valueUnit = "dB",
                valueTextColor = if (isLiveSourceEnabled) Color.White else Color.Gray
            )
        }
    }
    
    Spacer(modifier = Modifier.height(spacing.smallSpacing))
    
    // Slow Ratio
    val liveSourceSlowRatio = selectedChannel.getParameter("liveSourceSlowRatio")
    var liveSourceSlowRatioValue by remember { mutableStateOf(liveSourceSlowRatio.normalizedValue) }
    var liveSourceSlowRatioDisplayValue by remember { 
        mutableStateOf(liveSourceSlowRatio.displayValue.replace("", "").trim().ifEmpty { "1.00" }) 
    }
    
    LaunchedEffect(inputId) {
        val current = selectedChannel.getParameter("liveSourceSlowRatio")
        liveSourceSlowRatioValue = current.normalizedValue
        val definition = InputParameterDefinitions.parametersByVariableName["liveSourceSlowRatio"]!!
        val actualValue = InputParameterDefinitions.applyFormula(definition, current.normalizedValue)
        liveSourceSlowRatioDisplayValue = String.format("%.2f", actualValue)
    }
    
    Column {
        Text("Slow Ratio", fontSize = 12.sp, color = if (isLiveSourceEnabled) Color.White else Color.Gray)
        BasicDial(
            value = liveSourceSlowRatioValue,
            onValueChange = { newValue ->
                liveSourceSlowRatioValue = newValue
                val definition = InputParameterDefinitions.parametersByVariableName["liveSourceSlowRatio"]!!
                val actualValue = InputParameterDefinitions.applyFormula(definition, newValue)
                liveSourceSlowRatioDisplayValue = String.format("%.2f", actualValue)
                selectedChannel.setParameter("liveSourceSlowRatio", InputParameterValue(
                    normalizedValue = newValue,
                    stringValue = "",
                    displayValue = String.format("%.2f", actualValue)
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
                    liveSourceSlowRatioDisplayValue = String.format("%.2f", coercedValue)
                    selectedChannel.setParameter("liveSourceSlowRatio", InputParameterValue(
                        normalizedValue = normalized,
                        stringValue = "",
                        displayValue = String.format("%.2f", coercedValue)
                    ))
                    viewModel.sendInputParameterFloat("/remoteInput/liveSourceSlowRatio", inputId, coercedValue)
                }
            },
            valueTextColor = if (isLiveSourceEnabled) Color.White else Color.Gray,
            enabled = true
        )
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
    
    Spacer(modifier = Modifier.height(spacing.smallSpacing))
    
    // FRattenuation
    val FRattenuation = selectedChannel.getParameter("FRattentuation")
    var FRattenuationValue by remember { mutableStateOf(FRattenuation.normalizedValue) }
    var FRattenuationDisplayValue by remember { mutableStateOf("0.00") }
    
    LaunchedEffect(inputId) {
        FRattenuationValue = selectedChannel.getParameter("FRattentuation").normalizedValue
        val definition = InputParameterDefinitions.parametersByVariableName["FRattentuation"]!!
        val actualValue = InputParameterDefinitions.applyFormula(definition, FRattenuationValue)
        FRattenuationDisplayValue = String.format("%.2f", actualValue)
    }
    
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Attenuation", fontSize = 12.sp, color = if (isFREnabled) Color.White else Color.Gray)
            StandardSlider(
                value = FRattenuationValue,
                onValueChange = { newValue ->
                    FRattenuationValue = newValue
                    val definition = InputParameterDefinitions.parametersByVariableName["FRattentuation"]!!
                    val actualValue = InputParameterDefinitions.applyFormula(definition, newValue)
                    FRattenuationDisplayValue = String.format("%.2f", actualValue)
                    selectedChannel.setParameter("FRattentuation", InputParameterValue(
                        normalizedValue = newValue,
                        stringValue = "",
                        displayValue = "${String.format("%.2f", actualValue)}dB"
                    ))
                    viewModel.sendInputParameterFloat("/remoteInput/FRattentuation", inputId, actualValue)
                },
                modifier = Modifier.height(verticalSliderHeight),
                sliderColor = if (isFREnabled) Color(0xFF2196F3) else Color.Gray,
                trackBackgroundColor = Color.DarkGray,
                orientation = SliderOrientation.VERTICAL,
                displayedValue = FRattenuationDisplayValue,
                isValueEditable = true,
                onDisplayedValueChange = { /* Typing handled internally */ },
                onValueCommit = { committedValue ->
                    committedValue.toFloatOrNull()?.let { value ->
                        val definition = InputParameterDefinitions.parametersByVariableName["FRattentuation"]!!
                        val coercedValue = value.coerceIn(definition.minValue, definition.maxValue)
                        val normalized = InputParameterDefinitions.reverseFormula(definition, coercedValue)
                        FRattenuationValue = normalized
                        FRattenuationDisplayValue = String.format("%.2f", coercedValue)
                        selectedChannel.setParameter("FRattentuation", InputParameterValue(
                            normalizedValue = normalized,
                            stringValue = "",
                            displayValue = "${String.format("%.2f", coercedValue)}dB"
                        ))
                        viewModel.sendInputParameterFloat("/remoteInput/FRattentuation", inputId, coercedValue)
                    }
                },
                valueUnit = "dB",
                valueTextColor = if (isFREnabled) Color.White else Color.Gray
            )
        }
    }
    
    Spacer(modifier = Modifier.height(spacing.smallSpacing))
    
    // Low Cut Active
    val FRlowCutActive = selectedChannel.getParameter("FRlowCutActive")
    var FRlowCutActiveIndex by remember { 
        mutableStateOf(FRlowCutActive.normalizedValue.toInt().coerceIn(0, 1)) 
    }
    
    LaunchedEffect(inputId) {
        FRlowCutActiveIndex = selectedChannel.getParameter("FRlowCutActive").normalizedValue.toInt().coerceIn(0, 1)
    }
    
    val isFRLowCutEnabled = isFREnabled && FRlowCutActiveIndex == 0
    
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
            viewModel.sendInputParameterInt("/remoteInput/FRlowCutActive", inputId, index)
        },
        modifier = Modifier.fillMaxWidth()
    )
    
    Spacer(modifier = Modifier.height(spacing.smallSpacing))
    
    // Low Cut Freq
    val FRlowCutFreq = selectedChannel.getParameter("FRlowCutFreq")
    var FRlowCutFreqValue by remember { mutableStateOf(FRlowCutFreq.normalizedValue) }
    var FRlowCutFreqDisplayValue by remember { mutableStateOf("20") }
    
    LaunchedEffect(inputId) {
        FRlowCutFreqValue = selectedChannel.getParameter("FRlowCutFreq").normalizedValue
        val definition = InputParameterDefinitions.parametersByVariableName["FRlowCutFreq"]!!
        val actualValue = InputParameterDefinitions.applyFormula(definition, FRlowCutFreqValue)
        FRlowCutFreqDisplayValue = actualValue.toInt().toString()
    }
    
    Column {
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
                committedValue.toIntOrNull()?.let { value ->
                    val coercedValue = value.coerceIn(20, 20000)
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
    
    Spacer(modifier = Modifier.height(spacing.smallSpacing))
    
    // High Shelf Active
    val FRhighShelfActive = selectedChannel.getParameter("FRhighShelfActive")
    var FRhighShelfActiveIndex by remember { 
        mutableStateOf(FRhighShelfActive.normalizedValue.toInt().coerceIn(0, 1)) 
    }
    
    LaunchedEffect(inputId) {
        FRhighShelfActiveIndex = selectedChannel.getParameter("FRhighShelfActive").normalizedValue.toInt().coerceIn(0, 1)
    }
    
    val isFRHighShelfEnabled = isFREnabled && FRhighShelfActiveIndex == 0
    
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
            viewModel.sendInputParameterInt("/remoteInput/FRhighShelfActive", inputId, index)
        },
        modifier = Modifier.fillMaxWidth()
    )
    
    Spacer(modifier = Modifier.height(spacing.smallSpacing))
    
    // High Shelf Freq
    val FRhighShelfFreq = selectedChannel.getParameter("FRhighShelfFreq")
    var FRhighShelfFreqValue by remember { mutableStateOf(FRhighShelfFreq.normalizedValue) }
    var FRhighShelfFreqDisplayValue by remember { mutableStateOf("20") }
    
    LaunchedEffect(inputId) {
        FRhighShelfFreqValue = selectedChannel.getParameter("FRhighShelfFreq").normalizedValue
        val definition = InputParameterDefinitions.parametersByVariableName["FRhighShelfFreq"]!!
        val actualValue = InputParameterDefinitions.applyFormula(definition, FRhighShelfFreqValue)
        FRhighShelfFreqDisplayValue = actualValue.toInt().toString()
    }
    
    Column {
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
                committedValue.toIntOrNull()?.let { value ->
                    val coercedValue = value.coerceIn(20, 20000)
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
    
    Spacer(modifier = Modifier.height(spacing.smallSpacing))
    
    // High Shelf Gain
    val FRhighShelfGain = selectedChannel.getParameter("FRhighShelfGain")
    var FRhighShelfGainValue by remember { mutableStateOf(FRhighShelfGain.normalizedValue) }
    var FRhighShelfGainDisplayValue by remember { mutableStateOf("0.00") }
    
    LaunchedEffect(inputId) {
        FRhighShelfGainValue = selectedChannel.getParameter("FRhighShelfGain").normalizedValue
        val definition = InputParameterDefinitions.parametersByVariableName["FRhighShelfGain"]!!
        val actualValue = InputParameterDefinitions.applyFormula(definition, FRhighShelfGainValue)
        FRhighShelfGainDisplayValue = String.format("%.2f", actualValue)
    }
    
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("High Shelf Gain", fontSize = 12.sp, color = if (isFRHighShelfEnabled) Color.White else Color.Gray)
            StandardSlider(
                value = FRhighShelfGainValue,
                onValueChange = { newValue ->
                    FRhighShelfGainValue = newValue
                    val definition = InputParameterDefinitions.parametersByVariableName["FRhighShelfGain"]!!
                    val actualValue = InputParameterDefinitions.applyFormula(definition, newValue)
                    FRhighShelfGainDisplayValue = String.format("%.2f", actualValue)
                    selectedChannel.setParameter("FRhighShelfGain", InputParameterValue(
                        normalizedValue = newValue,
                        stringValue = "",
                        displayValue = "${String.format("%.2f", actualValue)}dB"
                    ))
                    viewModel.sendInputParameterFloat("/remoteInput/FRhighShelfGain", inputId, actualValue)
                },
                modifier = Modifier.height(verticalSliderHeight),
                sliderColor = if (isFRHighShelfEnabled) Color(0xFFFF5722) else Color.Gray,
                trackBackgroundColor = Color.DarkGray,
                orientation = SliderOrientation.VERTICAL,
                displayedValue = FRhighShelfGainDisplayValue,
                isValueEditable = true,
                onDisplayedValueChange = { /* Typing handled internally */ },
                onValueCommit = { committedValue ->
                    committedValue.toFloatOrNull()?.let { value ->
                        val definition = InputParameterDefinitions.parametersByVariableName["FRhighShelfGain"]!!
                        val coercedValue = value.coerceIn(definition.minValue, definition.maxValue)
                        val normalized = InputParameterDefinitions.reverseFormula(definition, coercedValue)
                        FRhighShelfGainValue = normalized
                        FRhighShelfGainDisplayValue = String.format("%.2f", coercedValue)
                        selectedChannel.setParameter("FRhighShelfGain", InputParameterValue(
                            normalizedValue = normalized,
                            stringValue = "",
                            displayValue = "${String.format("%.2f", coercedValue)}dB"
                        ))
                        viewModel.sendInputParameterFloat("/remoteInput/FRhighShelfGain", inputId, coercedValue)
                    }
                },
                valueUnit = "dB",
                valueTextColor = if (isFRHighShelfEnabled) Color.White else Color.Gray
            )
        }
    }
    
    Spacer(modifier = Modifier.height(spacing.smallSpacing))
    
    // High Shelf Slope
    val FRhighShelfSlope = selectedChannel.getParameter("FRhighShelfSlope")
    var FRhighShelfSlopeValue by remember { mutableStateOf(FRhighShelfSlope.normalizedValue) }
    var FRhighShelfSlopeDisplayValue by remember { mutableStateOf("0.10") }
    
    LaunchedEffect(inputId) {
        FRhighShelfSlopeValue = selectedChannel.getParameter("FRhighShelfSlope").normalizedValue
        val definition = InputParameterDefinitions.parametersByVariableName["FRhighShelfSlope"]!!
        val actualValue = InputParameterDefinitions.applyFormula(definition, FRhighShelfSlopeValue)
        FRhighShelfSlopeDisplayValue = String.format("%.2f", actualValue)
    }
    
    Column {
        Text("High Shelf Slope", fontSize = 12.sp, color = if (isFRHighShelfEnabled) Color.White else Color.Gray)
        StandardSlider(
            value = FRhighShelfSlopeValue,
            onValueChange = { newValue ->
                FRhighShelfSlopeValue = newValue
                val definition = InputParameterDefinitions.parametersByVariableName["FRhighShelfSlope"]!!
                val actualValue = InputParameterDefinitions.applyFormula(definition, newValue)
                FRhighShelfSlopeDisplayValue = String.format("%.2f", actualValue)
                selectedChannel.setParameter("FRhighShelfSlope", InputParameterValue(
                    normalizedValue = newValue,
                    stringValue = "",
                    displayValue = String.format("%.2f", actualValue)
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
                    FRhighShelfSlopeDisplayValue = String.format("%.2f", coercedValue)
                    selectedChannel.setParameter("FRhighShelfSlope", InputParameterValue(
                        normalizedValue = normalized,
                        stringValue = "",
                        displayValue = String.format("%.2f", coercedValue)
                    ))
                    viewModel.sendInputParameterFloat("/remoteInput/FRhighShelfSlope", inputId, coercedValue)
                }
            },
            valueUnit = "",
            valueTextColor = if (isFRHighShelfEnabled) Color.White else Color.Gray
        )
    }
    
    Spacer(modifier = Modifier.height(spacing.smallSpacing))
    
    // Diffusion
    val FRdiffusion = selectedChannel.getParameter("FRdiffusion")
    var FRdiffusionValue by remember { mutableStateOf(FRdiffusion.normalizedValue) }
    var FRdiffusionDisplayValue by remember { 
        mutableStateOf(FRdiffusion.displayValue.replace("%", "").trim().ifEmpty { "0" }) 
    }
    
    LaunchedEffect(inputId) {
        val current = selectedChannel.getParameter("FRdiffusion")
        FRdiffusionValue = current.normalizedValue
        val definition = InputParameterDefinitions.parametersByVariableName["FRdiffusion"]!!
        val actualValue = InputParameterDefinitions.applyFormula(definition, current.normalizedValue)
        FRdiffusionDisplayValue = actualValue.toInt().toString()
    }
    
    Column {
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
                committedValue.toIntOrNull()?.let { value ->
                    val coercedValue = value.coerceIn(0, 100)
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
            enabled = true
        )
    }
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
    
    Spacer(modifier = Modifier.height(spacing.smallSpacing))
    
    // Period
    val LFOperiod = selectedChannel.getParameter("LFOperiod")
    var LFOperiodValue by remember { mutableStateOf(LFOperiod.normalizedValue) }
    var LFOperiodDisplayValue by remember { 
        mutableStateOf(LFOperiod.displayValue.replace("s", "").trim().ifEmpty { "0.01" }) 
    }
    
    LaunchedEffect(inputId) {
        val current = selectedChannel.getParameter("LFOperiod")
        LFOperiodValue = current.normalizedValue
        val definition = InputParameterDefinitions.parametersByVariableName["LFOperiod"]!!
        val actualValue = InputParameterDefinitions.applyFormula(definition, current.normalizedValue)
        LFOperiodDisplayValue = String.format("%.2f", actualValue)
    }
    
    Column {
        Text("Period", fontSize = 12.sp, color = if (isLFOEnabled) Color.White else Color.Gray)
        BasicDial(
            value = LFOperiodValue,
            onValueChange = { newValue ->
                LFOperiodValue = newValue
                val definition = InputParameterDefinitions.parametersByVariableName["LFOperiod"]!!
                val actualValue = InputParameterDefinitions.applyFormula(definition, newValue)
                LFOperiodDisplayValue = String.format("%.2f", actualValue)
                selectedChannel.setParameter("LFOperiod", InputParameterValue(
                    normalizedValue = newValue,
                    stringValue = "",
                    displayValue = "${String.format("%.2f", actualValue)}s"
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
                    LFOperiodDisplayValue = String.format("%.2f", coercedValue)
                    selectedChannel.setParameter("LFOperiod", InputParameterValue(
                        normalizedValue = normalized,
                        stringValue = "",
                        displayValue = "${String.format("%.2f", coercedValue)}s"
                    ))
                    viewModel.sendInputParameterFloat("/remoteInput/LFOperiod", inputId, coercedValue)
                }
            },
            valueTextColor = if (isLFOEnabled) Color.White else Color.Gray,
            enabled = true
        )
    }
    
    Spacer(modifier = Modifier.height(spacing.smallSpacing))
    
    // Phase
    val LFOphase = selectedChannel.getParameter("LFOphase")
    var LFOphaseValue by remember { mutableStateOf(0f) } // 0 to 360 range directly
    
    LaunchedEffect(inputId) {
        val definition = InputParameterDefinitions.parametersByVariableName["LFOphase"]!!
        val currentParam = selectedChannel.getParameter("LFOphase")
        val actualValue = InputParameterDefinitions.applyFormula(definition, currentParam.normalizedValue)
        LFOphaseValue = actualValue
    }
    
    Column {
        Text("Phase", fontSize = 12.sp, color = if (isLFOEnabled) Color.White else Color.Gray)
        AngleDial(
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
            enabled = true
        )
    }
    
    Spacer(modifier = Modifier.height(spacing.smallSpacing))
    
    // Shape X
    val LFOshapeX = selectedChannel.getParameter("LFOshapeX")
    var LFOshapeXIndex by remember { 
        mutableStateOf(LFOshapeX.normalizedValue.toInt().coerceIn(0, 8)) 
    }
    
    LaunchedEffect(inputId) {
        LFOshapeXIndex = selectedChannel.getParameter("LFOshapeX").normalizedValue.toInt().coerceIn(0, 8)
    }
    
    val isShapeXEnabled = isLFOEnabled
    
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
    
    Spacer(modifier = Modifier.height(spacing.smallSpacing))
    
    // Shape Y
    val LFOshapeY = selectedChannel.getParameter("LFOshapeY")
    var LFOshapeYIndex by remember { 
        mutableStateOf(LFOshapeY.normalizedValue.toInt().coerceIn(0, 8)) 
    }
    
    LaunchedEffect(inputId) {
        LFOshapeYIndex = selectedChannel.getParameter("LFOshapeY").normalizedValue.toInt().coerceIn(0, 8)
    }
    
    val isShapeYEnabled = isLFOEnabled
    
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
    
    Spacer(modifier = Modifier.height(spacing.smallSpacing))
    
    // Shape Z
    val LFOshapeZ = selectedChannel.getParameter("LFOshapeZ")
    var LFOshapeZIndex by remember { 
        mutableStateOf(LFOshapeZ.normalizedValue.toInt().coerceIn(0, 8)) 
    }
    
    LaunchedEffect(inputId) {
        LFOshapeZIndex = selectedChannel.getParameter("LFOshapeZ").normalizedValue.toInt().coerceIn(0, 8)
    }
    
    val isShapeZEnabled = isLFOEnabled
    
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
    
    Spacer(modifier = Modifier.height(spacing.smallSpacing))
    
    // Rate X
    val LFOrateX = selectedChannel.getParameter("LFOrateX")
    var LFOrateXValue by remember { mutableStateOf(LFOrateX.normalizedValue) }
    var LFOrateXDisplayValue by remember { 
        mutableStateOf(LFOrateX.displayValue.replace("x", "").trim().ifEmpty { "0.01" }) 
    }
    
    LaunchedEffect(inputId) {
        val current = selectedChannel.getParameter("LFOrateX")
        LFOrateXValue = current.normalizedValue
        val definition = InputParameterDefinitions.parametersByVariableName["LFOrateX"]!!
        val actualValue = InputParameterDefinitions.applyFormula(definition, current.normalizedValue)
        LFOrateXDisplayValue = String.format("%.2f", actualValue)
    }
    
    val isRateXEnabled = isLFOEnabled && LFOshapeXIndex != 0
    
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Rate X", fontSize = 12.sp, color = if (isRateXEnabled) Color.White else Color.Gray, modifier = Modifier.width(80.dp))
        StandardSlider(
            value = LFOrateXValue,
            onValueChange = { newValue ->
                LFOrateXValue = newValue
                val definition = InputParameterDefinitions.parametersByVariableName["LFOrateX"]!!
                val actualValue = InputParameterDefinitions.applyFormula(definition, newValue)
                LFOrateXDisplayValue = String.format("%.2f", actualValue)
                selectedChannel.setParameter("LFOrateX", InputParameterValue(
                    normalizedValue = newValue,
                    stringValue = "",
                    displayValue = "${String.format("%.2f", actualValue)}x"
                ))
                viewModel.sendInputParameterFloat("/remoteInput/LFOrateX", inputId, actualValue)
            },
            modifier = Modifier.width(horizontalSliderWidth).height(horizontalSliderHeight),
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
                    LFOrateXDisplayValue = String.format("%.2f", coercedValue)
                    selectedChannel.setParameter("LFOrateX", InputParameterValue(
                        normalizedValue = normalized,
                        stringValue = "",
                        displayValue = "${String.format("%.2f", coercedValue)}x"
                    ))
                    viewModel.sendInputParameterFloat("/remoteInput/LFOrateX", inputId, coercedValue)
                }
            },
            valueUnit = "x",
            valueTextColor = if (isRateXEnabled) Color.White else Color.Gray
        )
    }
    
    Spacer(modifier = Modifier.height(spacing.smallSpacing))
    
    // Rate Y
    val LFOrateY = selectedChannel.getParameter("LFOrateY")
    var LFOrateYValue by remember { mutableStateOf(LFOrateY.normalizedValue) }
    var LFOrateYDisplayValue by remember { 
        mutableStateOf(LFOrateY.displayValue.replace("x", "").trim().ifEmpty { "0.01" }) 
    }
    
    LaunchedEffect(inputId) {
        val current = selectedChannel.getParameter("LFOrateY")
        LFOrateYValue = current.normalizedValue
        val definition = InputParameterDefinitions.parametersByVariableName["LFOrateY"]!!
        val actualValue = InputParameterDefinitions.applyFormula(definition, current.normalizedValue)
        LFOrateYDisplayValue = String.format("%.2f", actualValue)
    }
    
    val isRateYEnabled = isLFOEnabled && LFOshapeYIndex != 0
    
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Rate Y", fontSize = 12.sp, color = if (isRateYEnabled) Color.White else Color.Gray, modifier = Modifier.width(80.dp))
        StandardSlider(
            value = LFOrateYValue,
            onValueChange = { newValue ->
                LFOrateYValue = newValue
                val definition = InputParameterDefinitions.parametersByVariableName["LFOrateY"]!!
                val actualValue = InputParameterDefinitions.applyFormula(definition, newValue)
                LFOrateYDisplayValue = String.format("%.2f", actualValue)
                selectedChannel.setParameter("LFOrateY", InputParameterValue(
                    normalizedValue = newValue,
                    stringValue = "",
                    displayValue = "${String.format("%.2f", actualValue)}x"
                ))
                viewModel.sendInputParameterFloat("/remoteInput/LFOrateY", inputId, actualValue)
            },
            modifier = Modifier.width(horizontalSliderWidth).height(horizontalSliderHeight),
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
                    LFOrateYDisplayValue = String.format("%.2f", coercedValue)
                    selectedChannel.setParameter("LFOrateY", InputParameterValue(
                        normalizedValue = normalized,
                        stringValue = "",
                        displayValue = "${String.format("%.2f", coercedValue)}x"
                    ))
                    viewModel.sendInputParameterFloat("/remoteInput/LFOrateY", inputId, coercedValue)
                }
            },
            valueUnit = "x",
            valueTextColor = if (isRateYEnabled) Color.White else Color.Gray
        )
    }
    
    Spacer(modifier = Modifier.height(spacing.smallSpacing))
    
    // Rate Z
    val LFOrateZ = selectedChannel.getParameter("LFOrateZ")
    var LFOrateZValue by remember { mutableStateOf(LFOrateZ.normalizedValue) }
    var LFOrateZDisplayValue by remember { 
        mutableStateOf(LFOrateZ.displayValue.replace("x", "").trim().ifEmpty { "0.01" }) 
    }
    
    LaunchedEffect(inputId) {
        val current = selectedChannel.getParameter("LFOrateZ")
        LFOrateZValue = current.normalizedValue
        val definition = InputParameterDefinitions.parametersByVariableName["LFOrateZ"]!!
        val actualValue = InputParameterDefinitions.applyFormula(definition, current.normalizedValue)
        LFOrateZDisplayValue = String.format("%.2f", actualValue)
    }
    
    val isRateZEnabled = isLFOEnabled && LFOshapeZIndex != 0
    
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Rate Z", fontSize = 12.sp, color = if (isRateZEnabled) Color.White else Color.Gray, modifier = Modifier.width(80.dp))
        StandardSlider(
            value = LFOrateZValue,
            onValueChange = { newValue ->
                LFOrateZValue = newValue
                val definition = InputParameterDefinitions.parametersByVariableName["LFOrateZ"]!!
                val actualValue = InputParameterDefinitions.applyFormula(definition, newValue)
                LFOrateZDisplayValue = String.format("%.2f", actualValue)
                selectedChannel.setParameter("LFOrateZ", InputParameterValue(
                    normalizedValue = newValue,
                    stringValue = "",
                    displayValue = "${String.format("%.2f", actualValue)}x"
                ))
                viewModel.sendInputParameterFloat("/remoteInput/LFOrateZ", inputId, actualValue)
            },
            modifier = Modifier.width(horizontalSliderWidth).height(horizontalSliderHeight),
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
                    LFOrateZDisplayValue = String.format("%.2f", coercedValue)
                    selectedChannel.setParameter("LFOrateZ", InputParameterValue(
                        normalizedValue = normalized,
                        stringValue = "",
                        displayValue = "${String.format("%.2f", coercedValue)}x"
                    ))
                    viewModel.sendInputParameterFloat("/remoteInput/LFOrateZ", inputId, coercedValue)
                }
            },
            valueUnit = "x",
            valueTextColor = if (isRateZEnabled) Color.White else Color.Gray
        )
    }
    
    Spacer(modifier = Modifier.height(spacing.smallSpacing))
    
    // Amplitude X
    val LFOamplitudeX = selectedChannel.getParameter("LFOamplitudeX")
    var LFOamplitudeXValue by remember { mutableStateOf(0f) }
    var LFOamplitudeXDisplayValue by remember { mutableStateOf("0.00") }
    
    LaunchedEffect(inputId) {
        val definition = InputParameterDefinitions.parametersByVariableName["LFOamplitudeX"]!!
        val currentParam = selectedChannel.getParameter("LFOamplitudeX")
        val actualValue = InputParameterDefinitions.applyFormula(definition, currentParam.normalizedValue)
        LFOamplitudeXValue = actualValue
        LFOamplitudeXDisplayValue = String.format("%.2f", actualValue)
    }
    
    val isAmplitudeXEnabled = isLFOEnabled && LFOshapeXIndex != 0
    
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Amplitude X", fontSize = 12.sp, color = if (isAmplitudeXEnabled) Color.White else Color.Gray, modifier = Modifier.width(80.dp))
        BidirectionalSlider(
            value = LFOamplitudeXValue,
            onValueChange = { newValue ->
                LFOamplitudeXValue = newValue
                LFOamplitudeXDisplayValue = String.format("%.2f", newValue)
                val normalized = newValue / 50f
                selectedChannel.setParameter("LFOamplitudeX", InputParameterValue(
                    normalizedValue = normalized,
                    stringValue = "",
                    displayValue = "${String.format("%.2f", newValue)}m"
                ))
                viewModel.sendInputParameterFloat("/remoteInput/LFOamplitudeX", inputId, newValue)
            },
            modifier = Modifier.width(horizontalSliderWidth).height(horizontalSliderHeight),
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
                    LFOamplitudeXDisplayValue = String.format("%.2f", coercedValue)
                    val normalized = coercedValue / 50f
                    selectedChannel.setParameter("LFOamplitudeX", InputParameterValue(
                        normalizedValue = normalized,
                        stringValue = "",
                        displayValue = "${String.format("%.2f", coercedValue)}m"
                    ))
                    viewModel.sendInputParameterFloat("/remoteInput/LFOamplitudeX", inputId, coercedValue)
                }
            },
            valueUnit = "m",
            valueTextColor = if (isAmplitudeXEnabled) Color.White else Color.Gray
        )
    }
    
    Spacer(modifier = Modifier.height(spacing.smallSpacing))
    
    // Amplitude Y
    val LFOamplitudeY = selectedChannel.getParameter("LFOamplitudeY")
    var LFOamplitudeYValue by remember { mutableStateOf(0f) }
    var LFOamplitudeYDisplayValue by remember { mutableStateOf("0.00") }
    
    LaunchedEffect(inputId) {
        val definition = InputParameterDefinitions.parametersByVariableName["LFOamplitudeY"]!!
        val currentParam = selectedChannel.getParameter("LFOamplitudeY")
        val actualValue = InputParameterDefinitions.applyFormula(definition, currentParam.normalizedValue)
        LFOamplitudeYValue = actualValue
        LFOamplitudeYDisplayValue = String.format("%.2f", actualValue)
    }
    
    val isAmplitudeYEnabled = isLFOEnabled && LFOshapeYIndex != 0
    
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Amplitude Y", fontSize = 12.sp, color = if (isAmplitudeYEnabled) Color.White else Color.Gray, modifier = Modifier.width(80.dp))
        BidirectionalSlider(
            value = LFOamplitudeYValue,
            onValueChange = { newValue ->
                LFOamplitudeYValue = newValue
                LFOamplitudeYDisplayValue = String.format("%.2f", newValue)
                val normalized = newValue / 50f
                selectedChannel.setParameter("LFOamplitudeY", InputParameterValue(
                    normalizedValue = normalized,
                    stringValue = "",
                    displayValue = "${String.format("%.2f", newValue)}m"
                ))
                viewModel.sendInputParameterFloat("/remoteInput/LFOamplitudeY", inputId, newValue)
            },
            modifier = Modifier.width(verticalSliderWidth).height(verticalSliderHeight),
            sliderColor = if (isAmplitudeYEnabled) Color(0xFF4CAF50) else Color.Gray,
            trackBackgroundColor = Color.DarkGray,
            orientation = SliderOrientation.VERTICAL,
            valueRange = 0f..50f,
            displayedValue = LFOamplitudeYDisplayValue,
            isValueEditable = true,
            onDisplayedValueChange = { /* Typing handled internally */ },
            onValueCommit = { committedValue ->
                committedValue.toFloatOrNull()?.let { value ->
                    val coercedValue = value.coerceIn(0f, 50f)
                    LFOamplitudeYValue = coercedValue
                    LFOamplitudeYDisplayValue = String.format("%.2f", coercedValue)
                    val normalized = coercedValue / 50f
                    selectedChannel.setParameter("LFOamplitudeY", InputParameterValue(
                        normalizedValue = normalized,
                        stringValue = "",
                        displayValue = "${String.format("%.2f", coercedValue)}m"
                    ))
                    viewModel.sendInputParameterFloat("/remoteInput/LFOamplitudeY", inputId, coercedValue)
                }
            },
            valueUnit = "m",
            valueTextColor = if (isAmplitudeYEnabled) Color.White else Color.Gray
        )
    }
    
    Spacer(modifier = Modifier.height(spacing.smallSpacing))
    
    // Amplitude Z
    val LFOamplitudeZ = selectedChannel.getParameter("LFOamplitudeZ")
    var LFOamplitudeZValue by remember { mutableStateOf(0f) }
    var LFOamplitudeZDisplayValue by remember { mutableStateOf("0.00") }
    
    LaunchedEffect(inputId) {
        val definition = InputParameterDefinitions.parametersByVariableName["LFOamplitudeZ"]!!
        val currentParam = selectedChannel.getParameter("LFOamplitudeZ")
        val actualValue = InputParameterDefinitions.applyFormula(definition, currentParam.normalizedValue)
        LFOamplitudeZValue = actualValue
        LFOamplitudeZDisplayValue = String.format("%.2f", actualValue)
    }
    
    val isAmplitudeZEnabled = isLFOEnabled && LFOshapeZIndex != 0
    
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Amplitude Z", fontSize = 12.sp, color = if (isAmplitudeZEnabled) Color.White else Color.Gray, modifier = Modifier.width(80.dp))
        BidirectionalSlider(
            value = LFOamplitudeZValue,
            onValueChange = { newValue ->
                LFOamplitudeZValue = newValue
                LFOamplitudeZDisplayValue = String.format("%.2f", newValue)
                val normalized = newValue / 50f
                selectedChannel.setParameter("LFOamplitudeZ", InputParameterValue(
                    normalizedValue = normalized,
                    stringValue = "",
                    displayValue = "${String.format("%.2f", newValue)}m"
                ))
                viewModel.sendInputParameterFloat("/remoteInput/LFOamplitudeZ", inputId, newValue)
            },
            modifier = Modifier.width(verticalSliderWidth).height(verticalSliderHeight),
            sliderColor = if (isAmplitudeZEnabled) Color(0xFF4CAF50) else Color.Gray,
            trackBackgroundColor = Color.DarkGray,
            orientation = SliderOrientation.VERTICAL,
            valueRange = 0f..50f,
            displayedValue = LFOamplitudeZDisplayValue,
            isValueEditable = true,
            onDisplayedValueChange = { /* Typing handled internally */ },
            onValueCommit = { committedValue ->
                committedValue.toFloatOrNull()?.let { value ->
                    val coercedValue = value.coerceIn(0f, 50f)
                    LFOamplitudeZValue = coercedValue
                    LFOamplitudeZDisplayValue = String.format("%.2f", coercedValue)
                    val normalized = coercedValue / 50f
                    selectedChannel.setParameter("LFOamplitudeZ", InputParameterValue(
                        normalizedValue = normalized,
                        stringValue = "",
                        displayValue = "${String.format("%.2f", coercedValue)}m"
                    ))
                    viewModel.sendInputParameterFloat("/remoteInput/LFOamplitudeZ", inputId, coercedValue)
                }
            },
            valueUnit = "m",
            valueTextColor = if (isAmplitudeZEnabled) Color.White else Color.Gray
        )
    }
    
    Spacer(modifier = Modifier.height(spacing.smallSpacing))
    
    // Phase X
    val LFOphaseX = selectedChannel.getParameter("LFOphaseX")
    var LFOphaseXValue by remember { mutableStateOf(0f) }
    
    LaunchedEffect(inputId) {
        val definition = InputParameterDefinitions.parametersByVariableName["LFOphaseX"]!!
        val currentParam = selectedChannel.getParameter("LFOphaseX")
        val actualValue = InputParameterDefinitions.applyFormula(definition, currentParam.normalizedValue)
        LFOphaseXValue = actualValue
    }
    
    val isPhaseXEnabled = isLFOEnabled && LFOshapeXIndex != 0
    
    Column {
        Text("Phase X", fontSize = 12.sp, color = if (isPhaseXEnabled) Color.White else Color.Gray)
        AngleDial(
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
            enabled = true
        )
    }
    
    Spacer(modifier = Modifier.height(spacing.smallSpacing))
    
    // Phase Y
    val LFOphaseY = selectedChannel.getParameter("LFOphaseY")
    var LFOphaseYValue by remember { mutableStateOf(0f) }
    
    LaunchedEffect(inputId) {
        val definition = InputParameterDefinitions.parametersByVariableName["LFOphaseY"]!!
        val currentParam = selectedChannel.getParameter("LFOphaseY")
        val actualValue = InputParameterDefinitions.applyFormula(definition, currentParam.normalizedValue)
        LFOphaseYValue = actualValue
    }
    
    val isPhaseYEnabled = isLFOEnabled && LFOshapeYIndex != 0
    
    Column {
        Text("Phase Y", fontSize = 12.sp, color = if (isPhaseYEnabled) Color.White else Color.Gray)
        AngleDial(
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
            enabled = true
        )
    }
    
    Spacer(modifier = Modifier.height(spacing.smallSpacing))
    
    // Phase Z
    val LFOphaseZ = selectedChannel.getParameter("LFOphaseZ")
    var LFOphaseZValue by remember { mutableStateOf(0f) }
    
    LaunchedEffect(inputId) {
        val definition = InputParameterDefinitions.parametersByVariableName["LFOphaseZ"]!!
        val currentParam = selectedChannel.getParameter("LFOphaseZ")
        val actualValue = InputParameterDefinitions.applyFormula(definition, currentParam.normalizedValue)
        LFOphaseZValue = actualValue
    }
    
    val isPhaseZEnabled = isLFOEnabled && LFOshapeZIndex != 0
    
    Column {
        Text("Phase Z", fontSize = 12.sp, color = if (isPhaseZEnabled) Color.White else Color.Gray)
        AngleDial(
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
            enabled = true
        )
    }
    
    Spacer(modifier = Modifier.height(spacing.smallSpacing))
    
    // Gyrophone (at the end as per CSV)
    val LFOgyrophone = selectedChannel.getParameter("LFOgyrophone")
    var LFOgyrophoneIndex by remember { 
        mutableStateOf((LFOgyrophone.normalizedValue.toInt() + 1).coerceIn(0, 2)) 
    }
    
    LaunchedEffect(inputId) {
        LFOgyrophoneIndex = (selectedChannel.getParameter("LFOgyrophone").normalizedValue.toInt() + 1).coerceIn(0, 2)
    }
    
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
