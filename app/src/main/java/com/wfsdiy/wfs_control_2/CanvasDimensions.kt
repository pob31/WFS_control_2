package com.wfsdiy.wfs_control_2

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Shared canvas dimensions across the entire application.
 * This eliminates the need to pass canvas dimensions as parameters everywhere.
 */
object CanvasDimensions {
    private val _canvasWidth = MutableStateFlow(0f)
    private val _canvasHeight = MutableStateFlow(0f)
    private val _markerRadius = MutableStateFlow(20f) // Default marker radius
    
    val canvasWidth: StateFlow<Float> = _canvasWidth.asStateFlow()
    val canvasHeight: StateFlow<Float> = _canvasHeight.asStateFlow()
    val markerRadius: StateFlow<Float> = _markerRadius.asStateFlow()
    
    /**
     * Update the canvas dimensions.
     * This should be called when the canvas size changes.
     */
    fun updateDimensions(width: Float, height: Float) {
        _canvasWidth.value = width
        _canvasHeight.value = height
    }
    
    /**
     * Update the marker radius.
     * This should be called when the marker radius changes.
     */
    fun updateMarkerRadius(radius: Float) {
        _markerRadius.value = radius
    }
    
    /**
     * Get the current canvas dimensions as a Pair.
     * This is used by the OSC functions.
     */
    fun getCurrentDimensions(): Pair<Float, Float> {
        return Pair(_canvasWidth.value, _canvasHeight.value)
    }
    
    /**
     * Get the current marker radius.
     * This is used by the OSC functions.
     */
    fun getCurrentMarkerRadius(): Float {
        return _markerRadius.value
    }
    
    /**
     * Check if canvas dimensions are valid (non-zero).
     */
    fun areDimensionsValid(): Boolean {
        return _canvasWidth.value > 0f && _canvasHeight.value > 0f
    }
}

