package com.wfsdiy.wfs_control_2

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClusterHeightTab(
    clusterNormalizedHeights: List<Float>,
    stageHeight: Float,
    onNormalizedHeightChanged: (index: Int, newNormalizedHeight: Float) -> Unit
) {
    var draggingSliderIndex by remember { mutableStateOf<Int?>(null) }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        clusterNormalizedHeights.forEachIndexed { index, normalizedHeight ->
            val clusterId = index + 1 // Cluster IDs are 1-based
            val clusterBaseColor = getMarkerColor(clusterId, isClusterMarker = true)

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .padding(horizontal = 2.dp)
            ) {Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = "C $clusterId",
                    color = Color.White, // Ensure text is visible on dark background
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    val interactionSource = remember { MutableInteractionSource() }

                    // This LaunchedEffect is crucial for updating draggingSliderIndex
                    LaunchedEffect(interactionSource) {

                        interactionSource.interactions.collect { interaction ->
                            when (interaction) {
                                is androidx.compose.foundation.interaction.DragInteraction.Start -> {

                                    draggingSliderIndex = index
                                }
                                is androidx.compose.foundation.interaction.DragInteraction.Stop -> {

                                    if (draggingSliderIndex == index) {

                                        draggingSliderIndex = null
                                    }
                                }
                                is androidx.compose.foundation.interaction.PressInteraction.Cancel -> { // Also handle cancel

                                    if (draggingSliderIndex == index) {

                                        draggingSliderIndex = null
                                    }
                                }
                                // Other interaction states can be logged for debugging if needed but might not need to change draggingSliderIndex
                            }
                        }
                    }

                    val dynamicTrackThickness = (this@BoxWithConstraints.maxWidth / 2f).coerceAtLeast(4.dp)
                    // Define track colors based on the clusterBaseColor
                    val activeTrackCustomColor = clusterBaseColor.copy(alpha = 0.75f) // Slightly more vibrant active part
                    val inactiveTrackCustomColor = clusterBaseColor.copy(alpha = 0.3f) // Lighter/dimmer inactive part

                    Slider(
                        value = normalizedHeight,
                        onValueChange = { newValue ->
                            // If not already set by DragInteraction.Start, set it now.
                            // This helps if the drag starts very quickly or if onValueChange is triggered before DragInteraction.Start.
                            if (draggingSliderIndex != index) {
                                draggingSliderIndex = index
                            }
                            onNormalizedHeightChanged(index, newValue)
                        },
                        valueRange = 0f..1f,
                        onValueChangeFinished = {

                            if (draggingSliderIndex == index) { // Ensure it's the correct slider

                                draggingSliderIndex = null
                            }
                        },
                        interactionSource = interactionSource,
                        colors = SliderDefaults.colors(
                            thumbColor = clusterBaseColor, // Use cluster color for thumb
                            activeTrackColor = Color.Transparent, // Custom track handles this
                            inactiveTrackColor = Color.Transparent // Custom track handles this
                        ),
                        thumb = {
                            SliderDefaults.Thumb(
                                interactionSource = interactionSource,
                                colors = SliderDefaults.colors(thumbColor = clusterBaseColor) // Ensure thumb is colored
                            )
                        },
                        track = { sliderState ->
                            val activeTrackFraction = (sliderState.value - sliderState.valueRange.start) /
                                    (sliderState.valueRange.endInclusive - sliderState.valueRange.start)
                            Box(modifier = Modifier.fillMaxSize()) {
                                Box( // Inactive Track
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(dynamicTrackThickness)
                                        .background(color = inactiveTrackCustomColor) // Use custom inactive color
                                        .align(Alignment.CenterStart)
                                )
                                Box( // Active Track
                                    modifier = Modifier
                                        .fillMaxWidth(fraction = activeTrackFraction)
                                        .height(dynamicTrackThickness)
                                        .background(color = activeTrackCustomColor) // Use custom active color
                                        .align(Alignment.CenterStart)
                                )
                            }
                        },
                        modifier = Modifier
                            .graphicsLayer { rotationZ = -90f }
                            .layout { measurable, constraints ->
                                val placeable = measurable.measure(
                                    constraints.copy(
                                        minWidth = constraints.minHeight,
                                        maxWidth = constraints.maxHeight,
                                        minHeight = constraints.minWidth,
                                        maxHeight = constraints.maxWidth,
                                    )
                                )
                                layout(placeable.height, placeable.width) {
                                    placeable.placeRelative(
                                        x = (placeable.height - placeable.width) / 2,
                                        y = (placeable.width - placeable.height) / 2
                                    )
                                }
                            }
                            .fillMaxWidth()
                    )
                }

                val denormalizedHeight = normalizedHeight * stageHeight
                val heightText = if (draggingSliderIndex == index) { // Check against the correct index
                    String.format("%.1fm", denormalizedHeight)
                } else {
                    ""
                }


                val dynamicFontSize = (this@BoxWithConstraints.maxWidth.value / 8f).sp
                Text(
                    text = heightText,
                    color = Color.Yellow,
                    fontSize = dynamicFontSize,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .heightIn(min = (this@BoxWithConstraints.maxWidth.value / 6f).dp)
                )
            }
            }
        }
    }
}
