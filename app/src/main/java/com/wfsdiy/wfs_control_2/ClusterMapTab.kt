package com.wfsdiy.wfs_control_2

import android.annotation.SuppressLint
import android.graphics.Paint
import android.graphics.Typeface

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.collections.set
import kotlin.math.atan2
import kotlin.math.sqrt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ClusterMapTab(
    clusterMarkers: List<ClusterMarker>,
    onClusterMarkersChanged: (List<ClusterMarker>) -> Unit,
    onCanvasSizeChanged: (width: Float, height: Float) -> Unit,
    initialLayoutDone: Boolean,
    onInitialLayoutDone: () -> Unit,
    stageWidth: Float,
    stageDepth: Float,
    stageOriginX: Float,
    stageOriginY: Float,
    clusterSecondaryTouchEnabled: Boolean = true,
    viewModel: MainActivityViewModel? = null
) {
    val context = LocalContext.current
    val draggingMarkers = remember { mutableStateMapOf<Long, Int>() } // <Pointer.id.value, ClusterMarker.id>
    val currentOnMarkersChanged by rememberUpdatedState(onClusterMarkersChanged)
    val currentMarkersState by rememberUpdatedState(clusterMarkers)
    
    // Vector control state for secondary touches
    data class VectorControl(
        val markerId: Int,
        val initialMarkerPosition: Offset,
        val initialTouchPosition: Offset,
        val currentTouchPosition: Offset
    )
    val vectorControls = remember { mutableStateMapOf<Long, VectorControl>() }
    var vectorControlsUpdateTrigger: Int by remember { mutableStateOf(0) }
    // Calculate responsive marker radius
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthDp = configuration.screenWidthDp.dp
    val screenHeightDp = configuration.screenHeightDp.dp
    val screenDensity = density.density
    
    val baseMarkerRadius = (screenWidthDp.value / 40f).coerceIn(7.5f, 17.5f) // 7.5-17.5dp range (half size)
    val responsiveMarkerRadius = (baseMarkerRadius * screenDensity).coerceIn(7.5f, 17.5f)
    val markerRadius = responsiveMarkerRadius.dpToPx()

    val textPaint = remember {
        Paint().apply {
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
    }
    
    // Clear vector controls when cluster secondary touch is disabled
    LaunchedEffect(clusterSecondaryTouchEnabled) {
        if (!clusterSecondaryTouchEnabled) {
            vectorControls.clear()
            vectorControlsUpdateTrigger++
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val canvasWidth = constraints.maxWidth.toFloat()
        val canvasHeight = constraints.maxHeight.toFloat()
        val pickupRadiusMultiplier = 1.25f

        // Update shared canvas dimensions and call onCanvasSizeChanged
        LaunchedEffect(canvasWidth, canvasHeight) {
            if (canvasWidth > 0f && canvasHeight > 0f) {
                CanvasDimensions.updateDimensions(canvasWidth, canvasHeight)
                CanvasDimensions.updateMarkerRadius(markerRadius)
                onCanvasSizeChanged(canvasWidth, canvasHeight)
            }
        }

        LaunchedEffect(canvasWidth, canvasHeight, initialLayoutDone) {
            if (canvasWidth > 0f && canvasHeight > 0f && !initialLayoutDone) {
                // Check if markers are still in their default grid positions (from MainActivity initialization)
                // If they are, we don't need to reposition them
                val markersInDefaultPositions = clusterMarkers.all { marker ->
                    val index = marker.id - 1
                    val numCols = 5
                    val numRows = 2
                    val spacingFactor = (screenWidthDp.value / 3.5f).coerceIn(80f, 120f)
                    
                    val contentWidthOfCenters = (numCols - 1) * spacingFactor
                    val contentHeightOfCenters = (numRows - 1) * spacingFactor
                    val totalVisualWidth = contentWidthOfCenters + markerRadius * 2f
                    val totalVisualHeight = contentHeightOfCenters + markerRadius * 2f
                    
                    val screenWidth = screenWidthDp.value * screenDensity
                    val screenHeight = screenHeightDp.value * screenDensity
                    
                    val centeredStartX = ((screenWidth - totalVisualWidth) / 2f) + markerRadius
                    val centeredStartY = ((screenHeight - totalVisualHeight) / 2f) + markerRadius
                    
                    val logicalCol = index % numCols
                    var logicalRow = index / numCols
                    if (numRows > 1) {
                        logicalRow = (numRows - 1) - logicalRow
                    }
                    val expectedX = centeredStartX + logicalCol * spacingFactor
                    val expectedY = centeredStartY + logicalRow * spacingFactor
                    
                    // Check if marker is close to expected position (within 10 pixels tolerance)
                    val tolerance = 10f
                    kotlin.math.abs(marker.positionX - expectedX) < tolerance && 
                    kotlin.math.abs(marker.positionY - expectedY) < tolerance
                }
                
                if (!markersInDefaultPositions) {
                    // Markers have been moved (likely by OSC), don't reposition them
                } else {
                    // Markers are still in default positions, apply canvas-based positioning
                    val numCols = 5
                    val numRows = 2
                    
                    // Calculate responsive spacing factor based on screen size
                    val baseSpacingFactor = (screenWidthDp.value / 3.5f).coerceIn(80f, 120f) // 80-120dp range (more compact)
                    val spacingFactor = baseSpacingFactor

                    val contentWidthOfCenters = (numCols - 1) * spacingFactor
                    val contentHeightOfCenters = (numRows - 1) * spacingFactor
                    val totalVisualWidth = contentWidthOfCenters + markerRadius * 2f
                    val totalVisualHeight = contentHeightOfCenters + markerRadius * 2f

                    val centeredStartX = ((canvasWidth - totalVisualWidth) / 2f) + markerRadius
                    val centeredStartY = ((canvasHeight - totalVisualHeight) / 2f) + markerRadius

                    val updatedMarkers = clusterMarkers.map { clusterMarker ->
                        val index = clusterMarker.id - 1
                        val logicalCol = index % numCols
                        var logicalRow = index / numCols
                        if (numRows > 1) {
                            logicalRow = (numRows -1) - logicalRow
                        }
                        val xPos = centeredStartX + logicalCol * spacingFactor
                        val yPos = centeredStartY + logicalRow * spacingFactor

                        clusterMarker.copy(
                            positionX = xPos.coerceIn(markerRadius, canvasWidth - markerRadius),
                            positionY = yPos.coerceIn(markerRadius, canvasHeight - markerRadius)
                        )
                    }
                    currentOnMarkersChanged(updatedMarkers)
                }
                onInitialLayoutDone()
            }
        }

        Canvas(modifier = Modifier
            .fillMaxSize()
            .pointerInput(stageWidth, stageDepth) {
                awaitEachGesture {
                    val pointerIdToCurrentLogicalPosition = mutableMapOf<PointerId, Offset>()
                    val pointersThatAttemptedGrab = mutableSetOf<PointerId>()
                    
                    while (true) {
                        val event = awaitPointerEvent()
                        var positionsChangedInThisEvent = false
                        val nextMarkersList = currentMarkersState.toMutableList()
                        event.changes.forEach { change ->
                            val pointerId = change.id
                            val pointerValue = change.id.value
                            if (change.pressed) {
                                if (!draggingMarkers.containsKey(pointerValue)) {
                                    // Check if this pointer has a vector control first
                                    if (vectorControls.containsKey(pointerValue)) {
                                        // Handle secondary finger movement
                                        val vectorControl = vectorControls[pointerValue]
                                        if (vectorControl != null) {
                                            // Update the current touch position
                                            val updatedVectorControl = vectorControl.copy(currentTouchPosition = change.position)
                                            vectorControls[pointerValue] = updatedVectorControl
                                            vectorControlsUpdateTrigger++ // Trigger recomposition
                                            
                                            // Calculate angle and distance changes
                                            val currentMarker = currentMarkersState.find { it.id == vectorControl.markerId }
                                            if (currentMarker != null && initialLayoutDone && clusterSecondaryTouchEnabled) {
                                                val initialAngle = calculateAngle(vectorControl.initialMarkerPosition, vectorControl.initialTouchPosition)
                                                val currentAngle = calculateAngle(currentMarker.position, change.position)
                                                val angleChange = currentAngle - initialAngle
                                                
                                                // Send OSC messages asynchronously
                                                CoroutineScope(Dispatchers.IO).launch {
                                                    val initialDistance = calculateDistance(vectorControl.initialMarkerPosition, vectorControl.initialTouchPosition)
                                                    val currentDistance = calculateDistance(currentMarker.position, change.position)
                                                    val distanceRatio = if (initialDistance > 0f) currentDistance / initialDistance else 1.0f
                                                    
                                                    sendOscClusterRotation(context, vectorControl.markerId, (angleChange + 360f) % 360f)
                                                    sendOscClusterScale(context, vectorControl.markerId, distanceRatio)
                                                }
                                            }
                                            change.consume()
                                        }
                                    } else if (!pointersThatAttemptedGrab.contains(pointerId)) {
                                        pointersThatAttemptedGrab.add(pointerId)
                                        val touchPosition = change.position
                                        val candidateMarkers =
                                            currentMarkersState.filter { clusterMarker ->
                                                !draggingMarkers.containsValue(clusterMarker.id) &&
                                                        distance(
                                                            touchPosition,
                                                            clusterMarker.position
                                                        ) <= clusterMarker.radius * pickupRadiusMultiplier
                                            }
                                        if (candidateMarkers.isNotEmpty()) {
                                            val markerToDrag = candidateMarkers.minWithOrNull(
                                                compareBy<ClusterMarker> {
                                                    distance(
                                                        touchPosition,
                                                        it.position
                                                    )
                                                }
                                                    .thenBy { it.id }
                                            )
                                            markerToDrag?.let {
                                                if (draggingMarkers.size < 10) {
                                                    draggingMarkers[pointerValue] = it.id
                                                    pointerIdToCurrentLogicalPosition[pointerId] =
                                                        it.position
                                                }
                                            }
                                        } else {
                                            // No marker in pickup range - check for vector control only if cluster secondary touch is enabled
                                            if (clusterSecondaryTouchEnabled) {
                                                val draggedMarkers = draggingMarkers.values.toSet()
                                                val markersWithVectorControl = vectorControls.values.map { it.markerId }.toSet()
                                                val availableMarkers = draggedMarkers - markersWithVectorControl
                                                
                                                if (availableMarkers.isNotEmpty()) {
                                                    // Find the closest dragged marker without vector control
                                                    val closestMarkerId = availableMarkers.minByOrNull { markerId ->
                                                        val marker = currentMarkersState.find { it.id == markerId }
                                                        marker?.let { distance(touchPosition, it.position) } ?: Float.MAX_VALUE
                                                    }
                                                    
                                                    closestMarkerId?.let { markerId ->
                                                        val marker = currentMarkersState.find { it.id == markerId }
                                                        marker?.let {
                                                            vectorControls[pointerValue] = VectorControl(
                                                                markerId = markerId,
                                                                initialMarkerPosition = it.position,
                                                                initialTouchPosition = touchPosition,
                                                                currentTouchPosition = touchPosition
                                                            )
                                                            vectorControlsUpdateTrigger++ // Trigger recomposition
                                                            
                                                            // Send initial OSC messages for secondary touch asynchronously
                                                            CoroutineScope(Dispatchers.IO).launch {
                                                                sendOscClusterRotation(context, markerId, 0.0f)
                                                                sendOscClusterScale(context, markerId, 1.0f)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        change.consume()
                                    }
                                } else {
                                    val markerIdBeingDragged = draggingMarkers[pointerValue]
                                    if (markerIdBeingDragged != null) {
                                        val markerIndex =
                                            nextMarkersList.indexOfFirst { it.id == markerIdBeingDragged }
                                        if (markerIndex != -1) {
                                            val markerToMove = nextMarkersList[markerIndex]
                                            val oldLogicalPosition =
                                                pointerIdToCurrentLogicalPosition[pointerId]
                                            if (oldLogicalPosition != null && change.positionChanged()) {
                                                val dragDelta =
                                                    change.position - change.previousPosition
                                                val newLogicalPosition = Offset(
                                                    x = (oldLogicalPosition.x + dragDelta.x).coerceIn(
                                                        markerRadius,
                                                        canvasWidth - markerRadius
                                                    ),
                                                    y = (oldLogicalPosition.y + dragDelta.y).coerceIn(
                                                        markerRadius,
                                                        canvasHeight - markerRadius
                                                    )
                                                )
                                                pointerIdToCurrentLogicalPosition[pointerId] =
                                                    newLogicalPosition
                                                val updatedMarker =
                                                    markerToMove.copy(positionX = newLogicalPosition.x, positionY = newLogicalPosition.y)
                                                nextMarkersList[markerIndex] = updatedMarker
                                                positionsChangedInThisEvent = true
                                                
                                                // Send OSC messages asynchronously to avoid blocking UI
                                                if (initialLayoutDone) {
                                                    CoroutineScope(Dispatchers.IO).launch {
                                                        // Send position OSC
                                                        sendOscPosition(
                                                            context,
                                                            updatedMarker.id,
                                                            updatedMarker.position.x,
                                                            updatedMarker.position.y,
                                                            true
                                                        )
                                                        
                                                        // Check if this marker has vector control and send rotation/scale OSC
                                                        vectorControls.values.forEach { vectorControl ->
                                                            if (vectorControl.markerId == updatedMarker.id) {
                                                                val initialAngle = calculateAngle(vectorControl.initialMarkerPosition, vectorControl.initialTouchPosition)
                                                                val currentAngle = calculateAngle(updatedMarker.position, vectorControl.currentTouchPosition)
                                                                val angleChange = currentAngle - initialAngle
                                                                
                                                                val initialDistance = calculateDistance(vectorControl.initialMarkerPosition, vectorControl.initialTouchPosition)
                                                                val currentDistance = calculateDistance(updatedMarker.position, vectorControl.currentTouchPosition)
                                                                val distanceRatio = if (initialDistance > 0f) currentDistance / initialDistance else 1.0f
                                                                
                                                                sendOscClusterRotation(context, vectorControl.markerId, (angleChange + 360f) % 360f)
                                                                sendOscClusterScale(context, vectorControl.markerId, distanceRatio)
                                                            }
                                                        }
                                                    }
                                                }
                                                change.consume()
                                            }
                                        }
                                    }
                                }
                            } else {
                                if (draggingMarkers.containsKey(pointerValue)) {
                                    val releasedMarkerId = draggingMarkers.remove(pointerValue)!!
                                    pointerIdToCurrentLogicalPosition.remove(pointerId)
                                    
                                    // Clean up any vector controls associated with this marker
                                    vectorControls.entries.removeAll { (_, vectorControl) ->
                                        vectorControl.markerId == releasedMarkerId
                                    }
                                    vectorControlsUpdateTrigger++ // Trigger recomposition
                                    
                                    val markerForOsc =
                                        nextMarkersList.find { it.id == releasedMarkerId }
                                    if (markerForOsc != null) {
                                        if (initialLayoutDone) {
                                            sendOscPosition(
                                                context,
                                                markerForOsc.id,
                                                markerForOsc.position.x,
                                                markerForOsc.position.y,
                                                true
                                            )
                                        }
                                    }
                                } else if (vectorControls.containsKey(pointerValue)) {
                                    // Remove vector control when secondary touch is released
                                    vectorControls.remove(pointerValue)
                                    vectorControlsUpdateTrigger++ // Trigger recomposition
                                }
                                pointersThatAttemptedGrab.remove(pointerId)
                                change.consume()
                            }
                        }
                        if (positionsChangedInThisEvent) {
                            currentOnMarkersChanged(nextMarkersList.toList())
                        }
                        if (event.changes.all { !it.pressed } && draggingMarkers.isEmpty()) {
                            break
                        }
                    }
                }
            }
        ) {
            drawRect(Color.Black)
            drawStageCoordinates(stageWidth, stageDepth, canvasWidth, canvasHeight, markerRadius)
            drawStageCornerLabels(stageWidth, stageDepth, stageOriginX, stageOriginY, canvasWidth, canvasHeight, markerRadius)
            drawOriginMarker(stageWidth, stageDepth, stageOriginX, stageOriginY, canvasWidth, canvasHeight, markerRadius)
            
            // Draw vector control lines only if cluster secondary touch is enabled
            if (clusterSecondaryTouchEnabled) {
                vectorControls.values.forEach { vectorControl ->
                    val currentMarker = currentMarkersState.find { it.id == vectorControl.markerId }
                    if (currentMarker != null) {
                        // Calculate initial vector (from initial marker position to initial touch position)
                        val initialVector = vectorControl.initialTouchPosition - vectorControl.initialMarkerPosition
                        
                        // Draw grey reference line: same length and direction as initial vector, translated to current marker position
                        val greyLineEnd = currentMarker.position + initialVector
                        drawLine(
                            color = Color.Gray,
                            start = currentMarker.position,
                            end = greyLineEnd,
                            strokeWidth = 2f
                        )
                        
                        // Draw white active line (current marker position to current touch position)
                        drawLine(
                            color = Color.White,
                            start = currentMarker.position,
                            end = vectorControl.currentTouchPosition,
                            strokeWidth = 2f
                        )
                    }
                }
            }
            
            clusterMarkers.sortedByDescending { it.id }.forEach { clusterMarker ->
                drawMarker(clusterMarker, draggingMarkers.containsValue(clusterMarker.id), textPaint, true, stageWidth, stageDepth, stageOriginX, stageOriginY, canvasWidth, canvasHeight)
            }
        }
    }
}
