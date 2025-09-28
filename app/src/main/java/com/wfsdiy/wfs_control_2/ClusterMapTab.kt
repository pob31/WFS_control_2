package com.wfsdiy.wfs_control_2

import android.annotation.SuppressLint
import android.graphics.Paint
import android.graphics.Typeface
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
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
    viewModel: MainActivityViewModel? = null
) {
    Log.d("CLUSTER_TAB", "ClusterMapTab rendered with ${clusterMarkers.size} markers")
    clusterMarkers.forEach { marker ->
        Log.d("CLUSTER_TAB", "Rendered marker ${marker.id}: position (${marker.positionX}, ${marker.positionY})")
    }
    val context = LocalContext.current
    val draggingMarkers = remember { mutableStateMapOf<Long, Int>() } // <Pointer.id.value, ClusterMarker.id>
    val currentMarkersState by rememberUpdatedState(clusterMarkers)
    val currentOnMarkersChanged by rememberUpdatedState(onClusterMarkersChanged)
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
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val canvasWidth = constraints.maxWidth.toFloat()
        val canvasHeight = constraints.maxHeight.toFloat()
        val pickupRadiusMultiplier = 1.25f

        // Update shared canvas dimensions and call onCanvasSizeChanged
        LaunchedEffect(canvasWidth, canvasHeight) {
            if (canvasWidth > 0f && canvasHeight > 0f) {
                Log.d("CANVAS_DIMENSIONS", "ClusterMapTab updating shared canvas dimensions: ${canvasWidth}x${canvasHeight}")
                CanvasDimensions.updateDimensions(canvasWidth, canvasHeight)
                onCanvasSizeChanged(canvasWidth, canvasHeight)
            } else {
                Log.d("CANVAS_DIMENSIONS", "ClusterMapTab canvas dimensions still zero: ${canvasWidth}x${canvasHeight}")
            }
        }

        LaunchedEffect(canvasWidth, canvasHeight, initialLayoutDone) {
            if (canvasWidth > 0f && canvasHeight > 0f && !initialLayoutDone) {
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

                val updatedMarkers = currentMarkersState.map { clusterMarker ->
                    val index = clusterMarker.id - 1
                    val logicalCol = index % numCols
                    var logicalRow = index / numCols
                    if (numRows > 1) {
                        logicalRow = (numRows -1) - logicalRow
                    }
                    val xPos = centeredStartX + logicalCol * spacingFactor
                    val yPos = centeredStartY + logicalRow * spacingFactor

                    clusterMarker.copy(
                        positionX = xPos.coerceIn(clusterMarker.radius, canvasWidth - clusterMarker.radius),
                        positionY = yPos.coerceIn(clusterMarker.radius, canvasHeight - clusterMarker.radius)
                    )
                }
                currentOnMarkersChanged(updatedMarkers)
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
                                    if (!pointersThatAttemptedGrab.contains(pointerId)) {
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
                                                        markerToMove.radius,
                                                        canvasWidth - markerToMove.radius
                                                    ),
                                                    y = (oldLogicalPosition.y + dragDelta.y).coerceIn(
                                                        markerToMove.radius,
                                                        canvasHeight - markerToMove.radius
                                                    )
                                                )
                                                pointerIdToCurrentLogicalPosition[pointerId] =
                                                    newLogicalPosition
                                                val updatedMarker =
                                                    markerToMove.copy(positionX = newLogicalPosition.x, positionY = newLogicalPosition.y)
                                                nextMarkersList[markerIndex] = updatedMarker
                                                positionsChangedInThisEvent = true
                                                if (initialLayoutDone) {
                                                    sendOscPosition(
                                                        context,
                                                        updatedMarker.id,
                                                        updatedMarker.position.x,
                                                        updatedMarker.position.y,
                                                        true
                                                    )
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
            drawStageCoordinates(stageWidth, stageDepth, canvasWidth, canvasHeight)
            drawStageCornerLabels(stageWidth, stageDepth, canvasWidth, canvasHeight)
            currentMarkersState.sortedByDescending { it.id }.forEach { clusterMarker ->
                drawMarker(clusterMarker, draggingMarkers.containsValue(clusterMarker.id), textPaint, true, stageWidth, stageDepth, canvasWidth, canvasHeight)
            }
        }
    }
}