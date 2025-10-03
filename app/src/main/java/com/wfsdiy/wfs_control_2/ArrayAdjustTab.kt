package com.wfsdiy.wfs_control_2

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme // Added for Preview
import androidx.compose.material3.Surface // Added for Preview
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext // Added for OSC
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import kotlin.math.sqrt

// Theme Colors (Placeholders)
val timeColumnColor = Color(0xFF4A90E2) // Blueish
val levelColumnColor = Color(0xFF50E3C2) // Greenish
val horizontalParallaxColor = Color(0xFFF5A623) // Orangeish
val verticalParallaxColor = Color(0xFFBD10E0) // Purpleish
val defaultColumnColor = Color.DarkGray

@Composable
fun ArrayAdjustTab() {
    // Device detection for responsive sizing
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthDp = configuration.screenWidthDp.dp
    val screenHeightDp = configuration.screenHeightDp.dp
    val screenDensity = density.density
    
    // Use physical screen size to detect phone vs tablet
    val physicalWidthInches = screenWidthDp.value / 160f
    val physicalHeightInches = screenHeightDp.value / 160f
    val diagonalInches = sqrt(physicalWidthInches * physicalWidthInches + physicalHeightInches * physicalHeightInches)
    val isPhone = diagonalInches < 6.0f
    
    // Responsive sizing
    val sideColumnWeight = if (isPhone) 0.7f else 0.4f // 30% smaller on phone, 60% smaller on tablet
    val headerFontSize = if (isPhone) 8.sp else 16.sp // 50% smaller on phone
    val row2FontSize = if (isPhone) 6.sp else 12.sp // 50% smaller on phone
    val arrayLabelFontSize = if (isPhone) 7.sp else 14.sp // 50% smaller on phone
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // Overall background for the tab container
    ) {
        Spacer(modifier = Modifier.weight(1f)) // Empty row above, proportional height

        Row(
            modifier = Modifier
                .weight(9f) // Main content area, proportional height for 9 conceptual rows
                .fillMaxWidth()
                .padding(horizontal = 4.dp), // Padding for the group of columns
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Column 1
            SideColumn(
                modifier = Modifier
                    .weight(sideColumnWeight)
                    .fillMaxHeight(),
                arrayLabels = List(5) { "Array ${it + 1}" },
                arrayLabelFontSize = arrayLabelFontSize
            )

            VerticalDivider()

            // Column 2 - TIME
            ControlColumn(
                modifier = Modifier
                    .weight(1.5f)
                    .fillMaxHeight(),
                columnTitle = "TIME",
                row2Labels = Pair("Increase Delay", "Compensate Latency"),
                row3AndLastLabels = listOf("-1.0s", "-0.1s", "+0.1s", "+1.0s"),
                themeColor = timeColumnColor,
                columnIdentifier = "time",
                headerFontSize = headerFontSize,
                row2FontSize = row2FontSize
            )

            VerticalDivider()

            // Column 3 - LEVEL
            ControlColumn(
                modifier = Modifier
                    .weight(1.5f)
                    .fillMaxHeight(),
                columnTitle = "LEVEL",
                row2Labels = Pair("Quieter", "Louder"),
                row3AndLastLabels = listOf("-1.0dB", "-0.1dB", "+0.1dB", "+1.0dB"),
                themeColor = levelColumnColor,
                columnIdentifier = "level",
                headerFontSize = headerFontSize,
                row2FontSize = row2FontSize
            )

            VerticalDivider()

            // Column 4 - HORIZONTAL PARALLAX
            ControlColumn(
                modifier = Modifier
                    .weight(1.5f)
                    .fillMaxHeight(),
                columnTitle = "HORIZONTAL PARALLAX",
                row2Labels = Pair("Bring Closer", "Send Farther"),
                row3AndLastLabels = listOf("-1.0m", "-0.1m", "+0.1m", "+1.0m"),
                themeColor = horizontalParallaxColor,
                columnIdentifier = "h_parallax",
                headerFontSize = headerFontSize,
                row2FontSize = row2FontSize
            )

            VerticalDivider()

            // Column 5 - VERTICAL PARALLAX
            ControlColumn(
                modifier = Modifier
                    .weight(1.5f)
                    .fillMaxHeight(),
                columnTitle = "VERTICAL PARALLAX",
                row2Labels = Pair("Lower Speaker", "Raise Speaker"),
                row3AndLastLabels = listOf("-1.0m", "-0.1m", "+0.1m", "+1.0m"),
                themeColor = verticalParallaxColor,
                columnIdentifier = "v_parallax",
                headerFontSize = headerFontSize,
                row2FontSize = row2FontSize
            )

            VerticalDivider()

            // Column 6
            SideColumn(
                modifier = Modifier
                    .weight(sideColumnWeight)
                    .fillMaxHeight(),
                arrayLabels = List(5) { "Array ${it + 1}" },
                arrayLabelFontSize = arrayLabelFontSize
            )
        }

        Spacer(modifier = Modifier.weight(1f)) // Empty row below, proportional height
    }
}

@Composable
fun VerticalDivider() {
    Divider(
        color = Color.Gray,
        modifier = Modifier
            .fillMaxHeight()
            .width(1.dp)
    )
}

@Composable
fun ColumnScope.HeaderCell(text: String, themeColor: Color, fontSize: TextUnit = 16.sp) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f) // For the first conceptual row
            .background(themeColor.copy(alpha = 0.5f))
            .padding(vertical = 4.dp, horizontal = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = fontSize, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center, lineHeight = fontSize * 1.125f)
    }
}

@Composable
fun ColumnScope.TwoSplitCell(labels: Pair<String, String>, themeColor: Color, labelFontSize: TextUnit = 12.sp) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f), // For this conceptual row's height in the parent Column
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f).fillMaxHeight().background(themeColor.copy(alpha = 0.3f)).padding(2.dp), contentAlignment = Alignment.Center) {
            Text(labels.first, textAlign = TextAlign.Center, fontSize = labelFontSize, color = Color.White, maxLines = 2)
        }
        Divider(color = themeColor.copy(alpha = 0.5f), modifier = Modifier.width(1.dp).fillMaxHeight())
        Box(modifier = Modifier.weight(1f).fillMaxHeight().background(themeColor.copy(alpha = 0.3f)).padding(2.dp), contentAlignment = Alignment.Center) {
            Text(labels.second, textAlign = TextAlign.Center, fontSize = labelFontSize, color = Color.White, maxLines = 2)
        }
    }
}

@Composable
fun ColumnScope.FourSplitCell(labels: List<String>, themeColor: Color, labelFontSize: TextUnit = 12.sp) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f), // For this conceptual row's height in the parent Column
        verticalAlignment = Alignment.CenterVertically
    ) {
        labels.forEachIndexed { index, label ->
            Box(modifier = Modifier.weight(1f).fillMaxHeight().background(themeColor.copy(alpha = 0.2f)).padding(1.dp), contentAlignment = Alignment.Center) {
                 Text(label, textAlign = TextAlign.Center, fontSize = labelFontSize, color = Color.White, maxLines = 1)
            }
            if (index < labels.size - 1) {
                Divider(color = themeColor.copy(alpha = 0.5f), modifier = Modifier.width(1.dp).fillMaxHeight())
            }
        }
    }
}

@Composable
fun ButtonCell(
    arrayNumber: Int, // 1 to 5
    buttonIndexInRow: Int, // 0 to 3
    columnIdentifier: String,
    themeColor: Color
) {
    val context = LocalContext.current

    Button(
        onClick = {
            val oscAddressPath: String = when (columnIdentifier) {
                "time" -> "/arrayAdjust/delayLatency"
                "level" -> "/arrayAdjust/attenuation"
                "h_parallax" -> "/arrayAdjust/Hparallax"
                "v_parallax" -> "/arrayAdjust/Vparallax"
                else -> {
                    return@Button // Don't send an OSC message
                }
            }

            val valueToSend: Float = when (buttonIndexInRow) {
                0 -> -1.0f
                1 -> -0.1f
                2 -> 0.1f
                3 -> 1.0f
                else -> {
                    return@Button // Don't send an OSC message
                }
            }

            // Call the function from MainActivity.kt (assuming it's accessible)
            sendOscArrayAdjustCommand(context, oscAddressPath, arrayNumber, valueToSend)
        },
        modifier = Modifier
            .fillMaxSize() // Fills the parent Box
            .padding(1.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = themeColor.copy(alpha = 0.6f),
            contentColor = Color.White
        ),
        contentPadding = PaddingValues(0.dp) // Minimal padding for small buttons
    ) {
        Text("") // Buttons are plain colored cells
    }
}

@Composable
fun SideColumn(modifier: Modifier = Modifier, arrayLabels: List<String>, arrayLabelFontSize: TextUnit = 14.sp) {
    Column(
        modifier = modifier
            .background(defaultColumnColor.copy(alpha = 0.1f))
            .padding(horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HeaderCell(text = " ", themeColor = defaultColumnColor)
        TwoSplitCell(labels = Pair(" ", " "), themeColor = defaultColumnColor, labelFontSize = 12.sp)
        FourSplitCell(labels = List(4) { " " }, themeColor = defaultColumnColor, labelFontSize = 12.sp)

        // Rows 4-8: Array Labels
        arrayLabels.forEach { label ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                 Text(label, fontSize = arrayLabelFontSize, color = Color.White, textAlign = TextAlign.Center)
            }
        }

        FourSplitCell(labels = List(4) { " " }, themeColor = defaultColumnColor, labelFontSize = 12.sp)
    }
}

@Composable
fun ControlColumn(
    modifier: Modifier = Modifier,
    columnTitle: String,
    row2Labels: Pair<String, String>,
    row3AndLastLabels: List<String>,
    themeColor: Color,
    columnIdentifier: String,
    headerFontSize: TextUnit = 16.sp,
    row2FontSize: TextUnit = 12.sp
) {
    Column(
        modifier = modifier
            .background(themeColor.copy(alpha = 0.1f))
            .padding(horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HeaderCell(text = columnTitle, themeColor = themeColor, fontSize = headerFontSize)
        TwoSplitCell(labels = row2Labels, themeColor = themeColor, labelFontSize = row2FontSize)
        FourSplitCell(labels = row3AndLastLabels, themeColor = themeColor)

        // Rows 4-8: Five rows of four temporary buttons
        repeat(5) { arrayIndex -> // arrayIndex will be 0 to 4
             Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f), // Weight for this conceptual button row's height
                horizontalArrangement = Arrangement.spacedBy(1.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(4) { buttonIndex -> // buttonIndex will be 0 to 3
                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) { // Each button container takes equal width and fills height of the row
                        ButtonCell(
                            arrayNumber = arrayIndex + 1, // arrayNumber is 1-indexed
                            buttonIndexInRow = buttonIndex, // buttonIndexInRow is 0-indexed
                            columnIdentifier = columnIdentifier,
                            themeColor = themeColor
                        )
                    }
                }
            }
        }
        FourSplitCell(labels = row3AndLastLabels, themeColor = themeColor)
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
fun ArrayAdjustTabPreview() {
     WFS_control_2Theme {
        Surface(color = Color.Black) { // Ensure preview has a dark background
            ArrayAdjustTab()
        }
    }
}
