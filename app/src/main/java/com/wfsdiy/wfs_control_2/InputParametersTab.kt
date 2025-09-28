package com.wfsdiy.wfs_control_2

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
// import androidx.compose.foundation.layout.Row // Not explicitly used but good to have if needed for sub-layouts
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity

@Composable
private fun getResponsiveTextSizes(): ResponsiveTextSizes {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    
    val screenWidthDp = configuration.screenWidthDp.dp
    val screenDensity = density.density
    
    // Calculate responsive text sizes based on screen size and density
    val baseHeaderSize = (screenWidthDp.value / 25f).coerceIn(14f, 24f) // 14-24sp range
    val baseBodySize = (screenWidthDp.value / 30f).coerceIn(12f, 20f) // 12-20sp range
    val baseSmallSize = (screenWidthDp.value / 40f).coerceIn(10f, 16f) // 10-16sp range
    
    // Adjust for density
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
    
    // Calculate responsive spacing based on screen size and density
    val basePadding = (screenWidthDp.value / 25f).coerceIn(12f, 24f) // 12-24dp range
    val baseSmallSpacing = (screenWidthDp.value / 50f).coerceIn(6f, 12f) // 6-12dp range
    val baseLargeSpacing = (screenWidthDp.value / 20f).coerceIn(16f, 32f) // 16-32dp range
    
    // Adjust for density
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

@Composable
fun InputParametersTab() {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    
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
    
    var joystickX by remember { mutableStateOf(0f) }
    var joystickY by remember { mutableStateOf(0f) }

    var sliderX by remember { mutableStateOf(0f) }
    var sliderY by remember { mutableStateOf(0f) }

    var autoCenterSliderXValue by remember { mutableStateOf(0f) }
    var autoCenterSliderYValue by remember { mutableStateOf(0f) }

    var widthExpansionSliderHValue by remember { mutableStateOf(0f) } // 0f to 1f
    var widthExpansionSliderVValue by remember { mutableStateOf(0f) } // 0f to 1f

    var standardSliderHValue by remember { mutableStateOf(0f) } // 0f to 1f
    var standardSliderVValue by remember { mutableStateOf(0f) } // 0f to 1f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(spacing.padding)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.largeSpacing)
    ) {
        // Joystick Section
        Text("Joystick Output:", fontSize = textSizes.headerSize, color = Color.White)
        Text(
            text = "X: ${String.format("%.2f", joystickX)}, Y: ${String.format("%.2f", joystickY)}",
            fontSize = textSizes.bodySize,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(spacing.smallSpacing))
        Joystick(
            onPositionChanged = { x, y ->
                joystickX = x
                joystickY = y
            }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = spacing.largeSpacing))

        // Standard Sliders Section
        Text("Standard Sliders Output:", fontSize = textSizes.headerSize, color = Color.White)
        Text(
            text = "H-Standard: ${String.format("%.2f", standardSliderHValue)}, V-Standard: ${String.format("%.2f", standardSliderVValue)}",
            fontSize = textSizes.bodySize,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(spacing.smallSpacing))
        StandardSlider(
            value = standardSliderHValue,
            onValueChange = { standardSliderHValue = it },
            modifier = Modifier.width(horizontalSliderWidth).height(horizontalSliderHeight),
            sliderColor = Color(0xFFFF5722), // Deep Orange
            trackBackgroundColor = Color.DarkGray,
            orientation = SliderOrientation.HORIZONTAL
        )
        Spacer(modifier = Modifier.height(spacing.largeSpacing))
        StandardSlider(
            value = standardSliderVValue,
            onValueChange = { standardSliderVValue = it },
            modifier = Modifier.width(verticalSliderWidth).height(verticalSliderHeight),
            sliderColor = Color(0xFF9C27B0), // Purple
            trackBackgroundColor = Color.DarkGray,
            orientation = SliderOrientation.VERTICAL
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = spacing.largeSpacing))

        // Bidirectional Sliders Section
        Text("Bidirectional Sliders Output:", fontSize = textSizes.headerSize, color = Color.White)
        Text(
            text = "Slider X: ${String.format("%.2f", sliderX)}, Slider Y: ${String.format("%.2f", sliderY)}",
            fontSize = textSizes.bodySize,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(spacing.smallSpacing))
        BidirectionalSlider(
            value = sliderX,
            onValueChange = { sliderX = it },
            modifier = Modifier.width(horizontalSliderWidth).height(horizontalSliderHeight),
            sliderColor = Color(0xFF4CAF50), // Greenish
            trackBackgroundColor = Color.DarkGray,
            orientation = SliderOrientation.HORIZONTAL
        )
        Spacer(modifier = Modifier.height(spacing.largeSpacing))
        BidirectionalSlider(
            value = sliderY,
            onValueChange = { sliderY = it },
            modifier = Modifier.width(verticalSliderWidth).height(verticalSliderHeight),
            sliderColor = Color(0xFF2196F3), // Bluish
            trackBackgroundColor = Color.DarkGray,
            orientation = SliderOrientation.VERTICAL
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = spacing.largeSpacing))

        // Auto-Center Bidirectional Sliders Section
        Text("Auto-Center Sliders Output:", fontSize = textSizes.headerSize, color = Color.White)
        Text(
            text = "X: ${String.format("%.2f", autoCenterSliderXValue)}, Y: ${String.format("%.2f", autoCenterSliderYValue)}",
            fontSize = textSizes.bodySize,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        AutoCenterBidirectionalSlider(
            value = autoCenterSliderXValue,
            onValueChange = { autoCenterSliderXValue = it },
            modifier = Modifier.width(horizontalSliderWidth).height(horizontalSliderHeight),
            sliderColor = Color(0xFFFF9800), // Orangey
            trackBackgroundColor = Color.DarkGray,
            orientation = SliderOrientation.HORIZONTAL
        )
        Spacer(modifier = Modifier.height(spacing.largeSpacing))
        AutoCenterBidirectionalSlider(
            value = autoCenterSliderYValue,
            onValueChange = { autoCenterSliderYValue = it },
            modifier = Modifier.width(verticalSliderWidth).height(verticalSliderHeight),
            sliderColor = Color(0xFFE91E63), // Pinkish
            trackBackgroundColor = Color.DarkGray,
            orientation = SliderOrientation.VERTICAL
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = spacing.largeSpacing))

        // Width Expansion Sliders Section
        Text("Width Expansion Sliders Output:", fontSize = textSizes.headerSize, color = Color.White)
        Text(
            text = "H-Width: ${String.format("%.2f", widthExpansionSliderHValue)}, V-Width: ${String.format("%.2f", widthExpansionSliderVValue)}",
            fontSize = textSizes.bodySize,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        WidthExpansionSlider(
            value = widthExpansionSliderHValue,
            onValueChange = { widthExpansionSliderHValue = it },
            modifier = Modifier.width(horizontalSliderWidth).height(horizontalSliderHeight),
            sliderColor = Color(0xFF00BCD4), // Cyanish
            trackBackgroundColor = Color.DarkGray,
            orientation = SliderOrientation.HORIZONTAL,
            hideThumbOnRelease = true // Test hiding thumb
        )
        Spacer(modifier = Modifier.height(spacing.largeSpacing))
        WidthExpansionSlider(
            value = widthExpansionSliderVValue,
            onValueChange = { widthExpansionSliderVValue = it },
            modifier = Modifier.width(verticalSliderWidth).height(verticalSliderHeight),
            sliderColor = Color(0xFF7E57C2), // Purplish
            trackBackgroundColor = Color.DarkGray,
            orientation = SliderOrientation.VERTICAL,
            hideThumbOnRelease = false // Thumb always visible
        )
    }
}
