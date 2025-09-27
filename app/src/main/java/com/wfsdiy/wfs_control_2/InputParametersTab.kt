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

@Composable
fun InputParametersTab() {
    var joystickX by remember { mutableStateOf(0f) }
    var joystickY by remember { mutableStateOf(0f) }

    var sliderX by remember { mutableStateOf(0f) }
    var sliderY by remember { mutableStateOf(0f) }

    var autoCenterSliderXValue by remember { mutableStateOf(0f) }
    var autoCenterSliderYValue by remember { mutableStateOf(0f) }

    var widthExpansionSliderHValue by remember { mutableStateOf(0f) } // 0f to 1f
    var widthExpansionSliderVValue by remember { mutableStateOf(0f) } // 0f to 1f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Joystick Section
        Text("Joystick Output:", fontSize = 18.sp, color = Color.White)
        Text(
            text = "X: ${String.format("%.2f", joystickX)}, Y: ${String.format("%.2f", joystickY)}",
            fontSize = 16.sp,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Joystick(
            onPositionChanged = { x, y ->
                joystickX = x
                joystickY = y
            }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        // Bidirectional Sliders Section
        Text("Bidirectional Sliders Output:", fontSize = 18.sp, color = Color.White)
        Text(
            text = "Slider X: ${String.format("%.2f", sliderX)}, Slider Y: ${String.format("%.2f", sliderY)}",
            fontSize = 16.sp,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        BidirectionalSlider(
            value = sliderX,
            onValueChange = { sliderX = it },
            modifier = Modifier.fillMaxWidth(0.8f).height(50.dp),
            sliderColor = Color(0xFF4CAF50), // Greenish
            trackBackgroundColor = Color.DarkGray,
            orientation = SliderOrientation.HORIZONTAL
        )
        Spacer(modifier = Modifier.height(16.dp))
        BidirectionalSlider(
            value = sliderY,
            onValueChange = { sliderY = it },
            modifier = Modifier.width(50.dp).height(200.dp),
            sliderColor = Color(0xFF2196F3), // Bluish
            trackBackgroundColor = Color.DarkGray,
            orientation = SliderOrientation.VERTICAL
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        // Auto-Center Bidirectional Sliders Section
        Text("Auto-Center Sliders Output:", fontSize = 18.sp, color = Color.White)
        Text(
            text = "X: ${String.format("%.2f", autoCenterSliderXValue)}, Y: ${String.format("%.2f", autoCenterSliderYValue)}",
            fontSize = 16.sp,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        AutoCenterBidirectionalSlider(
            value = autoCenterSliderXValue,
            onValueChange = { autoCenterSliderXValue = it },
            modifier = Modifier.fillMaxWidth(0.8f).height(50.dp),
            sliderColor = Color(0xFFFF9800), // Orangey
            trackBackgroundColor = Color.DarkGray,
            orientation = SliderOrientation.HORIZONTAL
        )
        Spacer(modifier = Modifier.height(16.dp))
        AutoCenterBidirectionalSlider(
            value = autoCenterSliderYValue,
            onValueChange = { autoCenterSliderYValue = it },
            modifier = Modifier.width(50.dp).height(200.dp),
            sliderColor = Color(0xFFE91E63), // Pinkish
            trackBackgroundColor = Color.DarkGray,
            orientation = SliderOrientation.VERTICAL
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        // Width Expansion Sliders Section
        Text("Width Expansion Sliders Output:", fontSize = 18.sp, color = Color.White)
        Text(
            text = "H-Width: ${String.format("%.2f", widthExpansionSliderHValue)}, V-Width: ${String.format("%.2f", widthExpansionSliderVValue)}",
            fontSize = 16.sp,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        WidthExpansionSlider(
            value = widthExpansionSliderHValue,
            onValueChange = { widthExpansionSliderHValue = it },
            modifier = Modifier.fillMaxWidth(0.8f).height(50.dp),
            sliderColor = Color(0xFF00BCD4), // Cyanish
            trackBackgroundColor = Color.DarkGray,
            orientation = SliderOrientation.HORIZONTAL,
            hideThumbOnRelease = true // Test hiding thumb
        )
        Spacer(modifier = Modifier.height(16.dp))
        WidthExpansionSlider(
            value = widthExpansionSliderVValue,
            onValueChange = { widthExpansionSliderVValue = it },
            modifier = Modifier.width(50.dp).height(200.dp),
            sliderColor = Color(0xFF7E57C2), // Purplish
            trackBackgroundColor = Color.DarkGray,
            orientation = SliderOrientation.VERTICAL,
            hideThumbOnRelease = false // Thumb always visible
        )
    }
}
