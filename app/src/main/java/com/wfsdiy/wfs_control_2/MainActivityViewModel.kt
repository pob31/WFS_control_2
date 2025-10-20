package com.wfsdiy.wfs_control_2

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainActivityViewModel(private val oscService: OscService) : ViewModel() {

    val markers: StateFlow<List<Marker>> = oscService.markers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val clusterMarkers: StateFlow<List<ClusterMarker>> = oscService.clusterMarkers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val clusterNormalizedHeights: StateFlow<List<Float>> = oscService.clusterNormalizedHeights
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val stageWidth: StateFlow<Float> = oscService.stageWidth
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 16.0f)
    
    val stageDepth: StateFlow<Float> = oscService.stageDepth
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 10.0f)
    
    val stageHeight: StateFlow<Float> = oscService.stageHeight
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 7.0f)
    
    val stageOriginX: StateFlow<Float> = oscService.stageOriginX
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 8.0f) // 0.5 * stageWidth (16.0f)
    
    val stageOriginY: StateFlow<Float> = oscService.stageOriginY
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0f)
    
    val stageOriginZ: StateFlow<Float> = oscService.stageOriginZ
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0f)
    
    val numberOfInputs: StateFlow<Int> = oscService.numberOfInputs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 64)
    
    val inputParametersState: StateFlow<InputParametersState> = oscService.inputParametersState
        .stateIn(viewModelScope, SharingStarted.Eagerly, InputParametersState())

    fun sendMarkerPosition(markerId: Int, x: Float, y: Float, isCluster: Boolean) {
        oscService.sendMarkerPosition(markerId, x, y, isCluster)
    }

    fun sendClusterZ(clusterId: Int, normalizedZ: Float) {
        oscService.sendClusterZ(clusterId, normalizedZ)
    }
    
    fun sendArrayAdjustCommand(oscAddress: String, arrayId: Int, value: Float) {
        oscService.sendArrayAdjustCommand(oscAddress, arrayId, value)
    }
    
    fun startOscServerWithCanvasDimensions(canvasWidth: Float, canvasHeight: Float) {
        oscService.startOscServerWithCanvasDimensions(canvasWidth, canvasHeight)
    }
    
    fun startOscServer() {
        oscService.startOscServer()
    }
    
    fun isOscServerRunning(): Boolean = oscService.isOscServerRunning()
    
    fun restartOscServer() {
        oscService.restartOscServer()
    }
    
    fun updateNetworkParameters() {
        oscService.updateNetworkParameters()
    }
    
    // Methods to get buffered data from service
    fun getBufferedMarkerUpdates(): List<OscService.OscMarkerUpdate> {
        return oscService.getBufferedMarkerUpdates()
    }
    
    fun getBufferedStageUpdates(): List<OscService.OscStageUpdate> {
        return oscService.getBufferedStageUpdates()
    }
    
    fun getBufferedInputsUpdates(): List<OscService.OscInputsUpdate> {
        return oscService.getBufferedInputsUpdates()
    }
    
    fun getBufferedClusterZUpdates(): List<OscService.OscClusterZUpdate> {
        return oscService.getBufferedClusterZUpdates()
    }
    
    // Methods to sync current state with service
    fun syncMarkers(markers: List<Marker>) {
        oscService.syncMarkers(markers)
    }
    
    fun syncClusterMarkers(clusterMarkers: List<ClusterMarker>) {
        oscService.syncClusterMarkers(clusterMarkers)
    }
    
    fun syncClusterHeights(heights: List<Float>) {
        oscService.syncClusterHeights(heights)
    }
    
    fun syncStageDimensions(width: Float, depth: Float, height: Float, originX: Float = -1f, originY: Float = 0f, originZ: Float = 0f) {
        oscService.syncStageDimensions(width, depth, height, originX, originY, originZ)
    }
    
    fun syncNumberOfInputs(count: Int) {
        oscService.syncNumberOfInputs(count)
    }
    
    // Input parameter methods
    fun sendInputParameterInt(oscPath: String, inputId: Int, value: Int) {
        oscService.sendInputParameterInt(oscPath, inputId, value)
    }
    
    fun sendInputParameterFloat(oscPath: String, inputId: Int, value: Float) {
        oscService.sendInputParameterFloat(oscPath, inputId, value)
    }
    
    fun sendInputParameterString(oscPath: String, inputId: Int, value: String) {
        oscService.sendInputParameterString(oscPath, inputId, value)
    }
    
    fun requestInputParameters(inputId: Int) {
        oscService.requestInputParameters(inputId)
    }
    
    fun setSelectedInput(inputId: Int) {
        oscService.setSelectedInput(inputId)
    }
    
    fun getBufferedInputParameterUpdates(): List<OscService.OscInputParameterUpdate> {
        return oscService.getBufferedInputParameterUpdates()
    }
    
    fun syncInputParametersState(state: InputParametersState) {
        oscService.syncInputParametersState(state)
    }

    // Factory for creating the ViewModel with the OscService dependency
    class Factory(private val oscService: OscService) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainActivityViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainActivityViewModel(oscService) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

