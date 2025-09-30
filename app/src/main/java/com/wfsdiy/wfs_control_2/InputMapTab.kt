package com.wfsdiy.wfs_control_2

import android.graphics.Paint
import android.graphics.Typeface
import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import kotlin.math.pow
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.floor
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Helper functions for vector control calculations
fun calculateAngle(from: Offset, to: Offset): Float {
    return Math.toDegrees(atan2((to.y - from.y).toDouble(), (to.x - from.x).toDouble())).toFloat()
}

fun calculateDistance(from: Offset, to: Offset): Float {
    val dx = to.x - from.x
    val dy = to.y - from.y
    return sqrt(dx * dx + dy * dy)
}

fun calculateRelativeDistanceChange(initialDistance: Float, currentDistance: Float): Float {
    return if (initialDistance > 0f) (currentDistance - initialDistance) / initialDistance else 0f
}

// Assuming drawMarker is in MapElements.kt or accessible
// internal fun DrawScope.drawMarker( ... )

fun DrawScope.drawStageCoordinates(
    stageWidth: Float,
    stageDepth: Float,
    canvasWidthPx: Float,
    canvasHeightPx: Float,
    markerRadius: Float = 0f
) {
    if (stageWidth <= 0f || stageDepth <= 0f) return

    // Adjust canvas boundaries to account for marker radius
    val effectiveCanvasWidth = canvasWidthPx - (markerRadius * 2f)
    val effectiveCanvasHeight = canvasHeightPx - (markerRadius * 2f)
    
    val pixelsPerMeterX = effectiveCanvasWidth / stageWidth
    val pixelsPerMeterY = effectiveCanvasHeight / stageDepth

    val originXPx = canvasWidthPx / 2f
    val originYPx = canvasHeightPx - markerRadius // Bottom of effective canvas

    val lineColor = Color.DarkGray
    val lineStrokeWidth = 1f // Use 1 pixel for thin grid lines

    // Horizontal lines for depth (from bottom up)
    for (depthStep in 1..floor(stageDepth).toInt()) {
        val yPx = originYPx - (depthStep * pixelsPerMeterY)
        if (yPx >= markerRadius && yPx <= canvasHeightPx - markerRadius) { // Draw only if within effective canvas bounds
            drawLine(
                color = lineColor,
                start = Offset(markerRadius, yPx),
                end = Offset(canvasWidthPx - markerRadius, yPx),
                strokeWidth = lineStrokeWidth
            )
        }
    }

    // Vertical lines for width (from center out)
    // Center line (0m)
    drawLine(
        color = lineColor,
        start = Offset(originXPx, markerRadius),
        end = Offset(originXPx, canvasHeightPx - markerRadius),
        strokeWidth = lineStrokeWidth
    )
    // Lines to the right and left of center
    for (widthStep in 1..floor(stageWidth / 2f).toInt()) {
        // Right side
        val xPxPositive = originXPx + (widthStep * pixelsPerMeterX)
        if (xPxPositive >= markerRadius && xPxPositive <= canvasWidthPx - markerRadius) {
            drawLine(
                color = lineColor,
                start = Offset(xPxPositive, markerRadius),
                end = Offset(xPxPositive, canvasHeightPx - markerRadius),
                strokeWidth = lineStrokeWidth
            )
        }
        // Left side
        val xPxNegative = originXPx - (widthStep * pixelsPerMeterX)
        if (xPxNegative >= markerRadius && xPxNegative <= canvasWidthPx - markerRadius) {
            drawLine(
                color = lineColor,
                start = Offset(xPxNegative, markerRadius),
                end = Offset(xPxNegative, canvasHeightPx - markerRadius),
                strokeWidth = lineStrokeWidth
            )
        }
    }
}


@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun InputMapTab(
    numberOfInputs: Int,
    markers: List<Marker>,
    onMarkersInitiallyPositioned: (List<Marker>) -> Unit,
    onCanvasSizeChanged: (width: Float, height: Float) -> Unit,
    initialLayoutDone: Boolean,
    onInitialLayoutDone: () -> Unit,
    stageWidth: Float,
    stageDepth: Float,
    stageOriginX: Float,
    stageOriginY: Float
) {
    val context = LocalContext.current
    val draggingMarkers = remember { mutableStateMapOf<Long, Int>() }
    val currentMarkersState by rememberUpdatedState(markers)
    
    // Local state for smooth dragging without blocking global updates
    val localMarkerPositions = remember { mutableStateMapOf<Int, Offset>() }
    
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
    val screenDensity = density.density
    
    val baseMarkerRadius = (screenWidthDp.value / 40f).coerceIn(7.5f, 17.5f) // 7.5-17.5dp range (half size)
    val responsiveMarkerRadius = (baseMarkerRadius * screenDensity).coerceIn(7.5f, 17.5f)
    val markerRadius = responsiveMarkerRadius.dpToPx()

    val textPaint = remember {
        Paint().apply {
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            // textSize will be set in drawMarker based on marker.isVisible and zoom
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
            } else {

            }
        }

        LaunchedEffect(canvasWidth, canvasHeight, initialLayoutDone, numberOfInputs) {
            if (canvasWidth > 0f && canvasHeight > 0f && !initialLayoutDone && numberOfInputs > 0) {
                val numCols = 8
                val numRows = (numberOfInputs + numCols - 1) / numCols
                
                // Calculate responsive spacing factor based on screen size
                val baseSpacingFactor = (screenWidthDp.value / 4f).coerceIn(60f, 100f) // 60-100dp range (more compact)
                val spacingFactor = baseSpacingFactor

                val contentWidthOfCenters = (numCols - 1) * spacingFactor
                val contentHeightOfCenters = (numRows - 1) * spacingFactor
                val totalVisualWidth = contentWidthOfCenters + markerRadius * 2f
                val totalVisualHeight = contentHeightOfCenters + markerRadius * 2f

                val centeredStartX = ((canvasWidth - totalVisualWidth) / 2f) + markerRadius
                val centeredStartY = ((canvasHeight - totalVisualHeight) / 2f) + markerRadius

                val newFullMarkersList = currentMarkersState.mapIndexed { originalIndex, marker ->
                    if (originalIndex < numberOfInputs) { // Only update positions for *active* markers
                        val indexForCalc = originalIndex // Use originalIndex for grid calculation
                        val logicalCol = indexForCalc % numCols
                        var logicalRow = indexForCalc / numCols
                        if (numRows > 1) { // Reverse row order for Y if multi-row
                            logicalRow = (numRows - 1) - logicalRow
                        }
                        val xPos = centeredStartX + logicalCol * spacingFactor
                        val yPos = centeredStartY + logicalRow * spacingFactor

                        marker.copy(
                            positionX = xPos.coerceIn(markerRadius, canvasWidth - markerRadius),
                            positionY = yPos.coerceIn(markerRadius, canvasHeight - markerRadius)
                        )
                    } else {
                        // For markers beyond numberOfInputs, return them unchanged
                        marker
                    }
                }
                onMarkersInitiallyPositioned(newFullMarkersList)
                onInitialLayoutDone()
            } else if (numberOfInputs == 0 && !initialLayoutDone) {
                // If numberOfInputs is 0, reset positions
                val resetMarkersList = currentMarkersState.map { it.copy(positionX = 0f, positionY = 0f) } 
                onMarkersInitiallyPositioned(resetMarkersList)
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
                        val activeMarkersSnapshot = currentMarkersState.take(numberOfInputs).toMutableList()

                        event.changes.forEach { change ->
                            val pointerId = change.id
                            val pointerValue = change.id.value // Using Long as key for draggingMarkers
                            
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
                                            
                                            // Calculate and send OSC messages asynchronously
                                            if (initialLayoutDone) {
                                                CoroutineScope(Dispatchers.IO).launch {
                                                    // Use local position if available, otherwise use global position
                                                    val currentMarkerPosition = if (localMarkerPositions.containsKey(vectorControl.markerId)) {
                                                        localMarkerPositions[vectorControl.markerId]!!
                                                    } else {
                                                        currentMarkersState.find { it.id == vectorControl.markerId }?.position
                                                    }
                                                    
                                                    if (currentMarkerPosition != null) {
                                                        val initialAngle = calculateAngle(vectorControl.initialMarkerPosition, vectorControl.initialTouchPosition)
                                                        val currentAngle = calculateAngle(currentMarkerPosition, change.position)
                                                        val angleChange = currentAngle - initialAngle
                                                        
                                                        val initialDistance = calculateDistance(vectorControl.initialMarkerPosition, vectorControl.initialTouchPosition)
                                                        val currentDistance = calculateDistance(currentMarkerPosition, change.position)
                                                        val distanceChange = calculateRelativeDistanceChange(initialDistance, currentDistance)
                                                        
                                                        sendOscMarkerOrientation(context, vectorControl.markerId, ((angleChange.toInt() + 360) % 360))
                                                        sendOscMarkerDirectivity(context, vectorControl.markerId, distanceChange)
                                                    }
                                                }
                                            }
                                            change.consume()
                                        }
                                    } else if (!pointersThatAttemptedGrab.contains(pointerId)) {
                                        pointersThatAttemptedGrab.add(pointerId)
                                        val touchPosition = change.position
                                        val candidateMarkers =
                                            activeMarkersSnapshot.filterIndexed { _, m -> // m is from activeMarkersSnapshot
                                                // Get original marker from currentMarkersState to check lock/visibility
                                                val originalMarker = currentMarkersState.getOrNull(m.id -1)
                                                originalMarker != null && originalMarker.isVisible &&
                                                        !originalMarker.isLocked &&
                                                        !draggingMarkers.containsValue(m.id) && // Check against marker ID
                                                        distance(
                                                            touchPosition,
                                                            m.position // m.position is from activeMarkersSnapshot
                                                        ) <= m.radius * pickupRadiusMultiplier
                                            }

                                        if (candidateMarkers.isNotEmpty()) {
                                            val markerToDrag = candidateMarkers.minWithOrNull(
                                                compareBy<Marker> { marker -> distance(touchPosition, marker.position) }
                                                    .thenBy { marker -> marker.id }
                                            )
                                            markerToDrag?.let {
                                                if (draggingMarkers.size < 10) { // Limit concurrent drags
                                                    draggingMarkers[pointerValue] = it.id // Store marker ID
                                                    pointerIdToCurrentLogicalPosition[pointerId] = it.position
                                                }
                                            }
                                        } else {
                                            // No marker in pickup range - check for vector control
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
                                                            sendOscMarkerOrientation(context, markerId, 0)
                                                            sendOscMarkerDirectivity(context, markerId, 0.0f)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        change.consume()
                                    }
                                } else { // Pointer is pressed, but already dragging (associated with this pointerValue)
                                    val markerIdBeingDragged = draggingMarkers[pointerValue]
                                    if (markerIdBeingDragged != null) {
                                        val originalGlobalIndex = currentMarkersState.indexOfFirst { it.id == markerIdBeingDragged }

                                        if (originalGlobalIndex != -1) {
                                             // Check lock state from the main currentMarkersState using originalGlobalIndex
                                            if (!currentMarkersState[originalGlobalIndex].isLocked) {
                                                val oldLogicalPosition = pointerIdToCurrentLogicalPosition[pointerId]
                                                if (oldLogicalPosition != null && change.positionChanged()) {
                                                    val dragDelta = change.position - change.previousPosition
                                                    val markerToMove = currentMarkersState[originalGlobalIndex] // Get the most up-to-date marker state for radius

                                                    val newLogicalPosition = Offset(
                                                        x = (oldLogicalPosition.x + dragDelta.x).coerceIn(markerRadius, canvasWidth - markerRadius),
                                                        y = (oldLogicalPosition.y + dragDelta.y).coerceIn(markerRadius, canvasHeight - markerRadius)
                                                    )
                                                    pointerIdToCurrentLogicalPosition[pointerId] = newLogicalPosition
                                                    
                                                    val updatedMarker = markerToMove.copy(positionX = newLogicalPosition.x, positionY = newLogicalPosition.y)
                                                    
                                                    // Update local state immediately for smooth visual feedback
                                                    localMarkerPositions[updatedMarker.id] = newLogicalPosition
                                                    
                                                    // Update global state asynchronously to avoid blocking
                                                    CoroutineScope(Dispatchers.Default).launch {
                                                        val newFullListForWFS = currentMarkersState.toMutableList()
                                                        newFullListForWFS[originalGlobalIndex] = updatedMarker
                                                        
                                                        withContext(Dispatchers.Main) {
                                                            onMarkersInitiallyPositioned(newFullListForWFS.toList())
                                                        }
                                                    }

                                                    // Send OSC messages asynchronously to avoid blocking
                                                    if (initialLayoutDone) {
                                                        CoroutineScope(Dispatchers.IO).launch {
                                                            sendOscPosition(context, updatedMarker.id, updatedMarker.position.x, updatedMarker.position.y, false)
                                                            
                                                            // Check if this marker has vector control and send orientation/directivity OSC
                                                            vectorControls.values.forEach { vectorControl ->
                                                                if (vectorControl.markerId == updatedMarker.id) {
                                                                    // Use local position for consistent calculations
                                                                    val currentMarkerPosition = localMarkerPositions[updatedMarker.id] ?: updatedMarker.position
                                                                    
                                                                    val initialAngle = calculateAngle(vectorControl.initialMarkerPosition, vectorControl.initialTouchPosition)
                                                                    val currentAngle = calculateAngle(currentMarkerPosition, vectorControl.currentTouchPosition)
                                                                    val angleChange = currentAngle - initialAngle
                                                                    
                                                                    val initialDistance = calculateDistance(vectorControl.initialMarkerPosition, vectorControl.initialTouchPosition)
                                                                    val currentDistance = calculateDistance(currentMarkerPosition, vectorControl.currentTouchPosition)
                                                                    val distanceChange = calculateRelativeDistanceChange(initialDistance, currentDistance)
                                                                    
                                                                    sendOscMarkerOrientation(context, vectorControl.markerId, ((angleChange.toInt() + 360) % 360))
                                                                    sendOscMarkerDirectivity(context, vectorControl.markerId, distanceChange)
                                                                }
                                                            }
                                                        }
                                                    }
                                                    change.consume()
                                                }
                                            }
                                        }
                                    }
                                }
                            } else { // Pointer released
                                if (draggingMarkers.containsKey(pointerValue)) {
                                    val releasedMarkerId = draggingMarkers.remove(pointerValue)!!
                                    pointerIdToCurrentLogicalPosition.remove(pointerId)
                                    
                                    // Clean up local position
                                    localMarkerPositions.remove(releasedMarkerId)
                                    
                                    // Clean up any vector controls associated with this marker
                                    vectorControls.entries.removeAll { (_, vectorControl) ->
                                        vectorControl.markerId == releasedMarkerId
                                    }
                                    vectorControlsUpdateTrigger++ // Trigger recomposition
                                    
                                    // OSC for released marker (use its final position from currentMarkersState)
                                    val finalMarkerState = currentMarkersState.find { it.id == releasedMarkerId }
                                    if (finalMarkerState != null && !finalMarkerState.isLocked && initialLayoutDone) {
                                        sendOscPosition(context, finalMarkerState.id, finalMarkerState.position.x, finalMarkerState.position.y, false)
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
                        if (event.changes.all { !it.pressed } && draggingMarkers.isEmpty()) {
                            break 
                        }
                    }
                }
            }
        ) { // DrawScope
            drawRect(Color.Black) // Background for the canvas
            
            // Draw the stage grid lines (bottom-center origin)
            drawStageCoordinates(stageWidth, stageDepth, canvasWidth, canvasHeight, markerRadius)
            
            // Draw the stage corner/center labels (top-left origin assumed by this function)
            drawStageCornerLabels(stageWidth, stageDepth, stageOriginX, stageOriginY, canvasWidth, canvasHeight, markerRadius)

            // Draw origin marker at position where displayed coordinates would be (0.0, 0.0)
            drawOriginMarker(stageWidth, stageDepth, stageOriginX, stageOriginY, canvasWidth, canvasHeight, markerRadius)

            // Draw vector control lines
            vectorControls.values.forEach { vectorControl ->
                // Use local position if available, otherwise use global position
                val currentMarkerPosition = if (localMarkerPositions.containsKey(vectorControl.markerId)) {
                    localMarkerPositions[vectorControl.markerId]!!
                } else {
                    currentMarkersState.find { it.id == vectorControl.markerId }?.position
                }
                
                if (currentMarkerPosition != null) {
                    // Calculate initial vector (from initial marker position to initial touch position)
                    val initialVector = vectorControl.initialTouchPosition - vectorControl.initialMarkerPosition
                    
                    // Draw grey reference line: same length and direction as initial vector, translated to current marker position
                    val greyLineEnd = currentMarkerPosition + initialVector
                    drawLine(
                        color = Color.Gray,
                        start = currentMarkerPosition,
                        end = greyLineEnd,
                        strokeWidth = 2f
                    )
                    
                    // Draw white active line (current marker position to current touch position)
                    drawLine(
                        color = Color.White,
                        start = currentMarkerPosition,
                        end = vectorControl.currentTouchPosition,
                        strokeWidth = 2f
                    )
                }
            }

            // Draw markers on top of the grid and labels
            currentMarkersState.take(numberOfInputs).sortedByDescending { it.id }.forEach { marker ->
                // Use local position if available for smooth dragging, otherwise use global position
                val displayMarker = if (localMarkerPositions.containsKey(marker.id)) {
                    marker.copy(
                        positionX = localMarkerPositions[marker.id]!!.x,
                        positionY = localMarkerPositions[marker.id]!!.y
                    )
                } else {
                    marker
                }
                
                // Assuming drawMarker is defined elsewhere and handles its own textPaint settings for visibility/zoom
                drawMarker(displayMarker, draggingMarkers.containsValue(marker.id), textPaint, false, stageWidth, stageDepth, stageOriginX, stageOriginY, canvasWidth, canvasHeight)
            }
        }
    }
}
