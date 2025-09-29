package com.wfsdiy.wfs_control_2

import android.annotation.SuppressLint
import android.graphics.Paint
import android.graphics.Typeface

import androidx.compose.foundation.Canvas
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
    stageDepth: Float
) {
    val context = LocalContext.current
    val draggingMarkers = remember { mutableStateMapOf<Long, Int>() }
    val currentMarkersState by rememberUpdatedState(markers)
    
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
                                    if (!pointersThatAttemptedGrab.contains(pointerId)) {
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
                                                    
                                                    // Update WFSControlApp by creating a new full list
                                                    val newFullListForWFS = currentMarkersState.toMutableList()
                                                    newFullListForWFS[originalGlobalIndex] = updatedMarker
                                                    onMarkersInitiallyPositioned(newFullListForWFS.toList()) // Update WFSControlApp

                                                    if (initialLayoutDone) {
                                                        sendOscPosition(context, updatedMarker.id, updatedMarker.position.x, updatedMarker.position.y, false)
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
                                    // OSC for released marker (use its final position from currentMarkersState)
                                    val finalMarkerState = currentMarkersState.find { it.id == releasedMarkerId }
                                    if (finalMarkerState != null && !finalMarkerState.isLocked && initialLayoutDone) {
                                        sendOscPosition(context, finalMarkerState.id, finalMarkerState.position.x, finalMarkerState.position.y, false)
                                    }
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
            drawStageCornerLabels(stageWidth, stageDepth, canvasWidth, canvasHeight, markerRadius)

            // Draw markers on top of the grid and labels
            currentMarkersState.take(numberOfInputs).sortedByDescending { it.id }.forEach { marker ->
                // Assuming drawMarker is defined elsewhere and handles its own textPaint settings for visibility/zoom
                drawMarker(marker, draggingMarkers.containsValue(marker.id), textPaint, false, stageWidth, stageDepth, canvasWidth, canvasHeight)
            }
        }
    }
}
