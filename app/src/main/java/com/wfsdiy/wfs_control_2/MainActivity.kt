package com.wfsdiy.wfs_control_2

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlin.math.min
import kotlin.math.max
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue


// SharedPreferences Constants
internal const val PREFS_NAME = "network_prefs"
internal const val KEY_INCOMING_PORT = "incoming_port"
internal const val KEY_OUTGOING_PORT = "outgoing_port"
internal const val KEY_IP_ADDRESS = "ip_address"
internal const val KEY_NUMBER_OF_INPUTS = "number_of_inputs"
internal const val KEY_LOCK_STATES = "lock_states"
internal const val KEY_VISIBILITY_STATES = "visibility_states"

// Maximum number of inputs the system can handle
internal const val MAX_INPUTS = 64

// Define the Marker data class
@Parcelize
data class Marker(
    val id: Int,
    var position: @RawValue Offset,
    val radius: Float,
    var isLocked: Boolean = false,
    var isVisible: Boolean = true,
    var name: String = ""
) : Parcelable

// Define the ClusterMarker data class (Simplified)
@Parcelize
data class ClusterMarker(
    val id: Int,
    var position: @RawValue Offset,
    val radius: Float
) : Parcelable

// Helper to convert Dp to Px for marker radius, as Canvas works with Px
@Composable
fun Float.dpToPx(): Float {
    return with(LocalDensity.current) { this@dpToPx.dp.toPx() }
}

// Helper to convert Sp to Px for text size on Canvas
@Composable
fun Float.spToPx(): Float {
    return with(LocalDensity.current) { this@spToPx.sp.toPx() }
}

// SharedPreferences Helper Functions
fun saveNetworkParameters(context: Context, incomingPort: String, outgoingPort: String, ipAddress: String) {
    val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    with(sharedPrefs.edit()) {
        putString(KEY_INCOMING_PORT, incomingPort)
        putString(KEY_OUTGOING_PORT, outgoingPort)
        putString(KEY_IP_ADDRESS, ipAddress)
        apply()
    }
}

fun loadNetworkParameters(context: Context): Triple<String, String, String> {
    val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val incomingPort = sharedPrefs.getString(KEY_INCOMING_PORT, "8000") ?: "8000"
    val outgoingPort = sharedPrefs.getString(KEY_OUTGOING_PORT, "8001") ?: "8001"
    val ipAddress = sharedPrefs.getString(KEY_IP_ADDRESS, "192.168.1.100") ?: "192.168.1.100"
    return Triple(incomingPort, outgoingPort, ipAddress)
}

fun saveAppSettings(context: Context, numberOfInputs: Int, markers: List<Marker>) {
    val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    with(sharedPrefs.edit()) {
        putInt(KEY_NUMBER_OF_INPUTS, numberOfInputs)
        // Storing boolean arrays as comma-separated strings
        val lockStatesString = markers.joinToString(",") { it.isLocked.toString() }
        val visibilityStatesString = markers.joinToString(",") { it.isVisible.toString() }
        putString(KEY_LOCK_STATES, lockStatesString)
        putString(KEY_VISIBILITY_STATES, visibilityStatesString)
        apply()
    }
}

fun loadAppSettings(context: Context): Triple<Int, List<Boolean>, List<Boolean>> {
    val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val numberOfInputs = sharedPrefs.getInt(KEY_NUMBER_OF_INPUTS, MAX_INPUTS)

    val lockStatesString = sharedPrefs.getString(KEY_LOCK_STATES, null)
    val lockStates = lockStatesString?.split(",")?.map { it.toBoolean() } ?: List(MAX_INPUTS) { false }

    val visibilityStatesString = sharedPrefs.getString(KEY_VISIBILITY_STATES, null)
    val visibilityStates = visibilityStatesString?.split(",")?.map { it.toBoolean() } ?: List(MAX_INPUTS) { true }

    return Triple(numberOfInputs, lockStates, visibilityStates)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        hideSystemUI()

        setContent {
            WFS_control_2Theme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    WFSControlApp()
                }
            }
        }
    }

    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            controller?.apply {
                hide(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                    )
            @Suppress("DEPRECATION")
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WFSControlApp() {
    val context = LocalContext.current
    val markerRadiusPx = 20f.dpToPx()
    var numberOfInputs by rememberSaveable { mutableStateOf(MAX_INPUTS) }
    var markers by remember {
        mutableStateOf(
            List(MAX_INPUTS) { index ->
                Marker(
                    id = index + 1,
                    position = Offset(0f, 0f),
                    radius = markerRadiusPx,
                    name = ""
                )
            }
        )
    }
    var initialInputLayoutDone by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val (loadedInputs, loadedLockStates, loadedVisibilityStates) = loadAppSettings(context)
        numberOfInputs = loadedInputs
        markers = markers.mapIndexed { index, marker ->
            marker.copy(
                isLocked = loadedLockStates.getOrElse(index) { false },
                isVisible = loadedVisibilityStates.getOrElse(index) { true }
            )
        }
        // After loading, the initial layout has not been done for the loaded state
        initialInputLayoutDone = false
    }

    LaunchedEffect(markers, numberOfInputs) {
        saveAppSettings(context, numberOfInputs, markers)
    }

    val resetToDefaults = {
        numberOfInputs = MAX_INPUTS
        markers = markers.map { marker ->
            marker.copy(
                position = Offset(0f, 0f), // Reset position
                isLocked = false, 
                isVisible = true
            )
        }
        initialInputLayoutDone = false // This will trigger the repositioning in InputMapTab
    }

    var clusterMarkers by rememberSaveable {
        mutableStateOf(
            List(10) { index ->
                ClusterMarker(
                    id = index + 1,
                    position = Offset(0f, 0f),
                    radius = markerRadiusPx
                )
            }
        )
    }
    var clusterNormalizedHeights by rememberSaveable { mutableStateOf(List(10) { 0.2f }) }

    var initialClusterLayoutDone by remember { mutableStateOf(false) }

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Input Map", "Lock Input Markers", "View Input Markers", "Input Parameters", "Cluster Map", "Cluster Height", "Array Adjust", "Settings")

    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp

    val dynamicTabFontSize: TextUnit = remember(screenWidthDp) {
        val baseSize = screenWidthDp.value / 35f
        max(10f, min(20f, baseSize)).sp
    }

    var currentCanvasPixelWidth by remember { mutableStateOf(0f) }
    var currentCanvasPixelHeight by remember { mutableStateOf(0f) }

    var stageWidth by remember { mutableStateOf(16.0f) }
    var stageDepth by remember { mutableStateOf(10.0f) }
    var stageHeight by remember { mutableStateOf(7.0f) }

    LaunchedEffect(Unit) {
        startOscServer(
            context = context,
            getCanvasDimensions = {
                Pair(currentCanvasPixelWidth, currentCanvasPixelHeight)
            },
            onOscDataReceived = { id, name, position, isCluster ->
                if (isCluster) {
                    clusterMarkers = clusterMarkers.map { clusterMarker ->
                        if (clusterMarker.id == id) {
                            var updatedMarker = clusterMarker
                            position?.let {
                                updatedMarker = updatedMarker.copy(
                                    position = Offset(
                                        it.x.coerceIn(clusterMarker.radius, currentCanvasPixelWidth - clusterMarker.radius),
                                        it.y.coerceIn(clusterMarker.radius, currentCanvasPixelHeight - clusterMarker.radius)
                                    )
                                )
                            }
                            updatedMarker
                        } else {
                            clusterMarker
                        }
                    }
                } else {
                    markers = markers.map { marker ->
                        if (marker.id == id) {
                            var updatedMarker = marker
                            name?.let { updatedMarker = updatedMarker.copy(name = it) }
                            position?.let {
                                updatedMarker = updatedMarker.copy(
                                    position = Offset(
                                        it.x.coerceIn(marker.radius, currentCanvasPixelWidth - marker.radius),
                                        it.y.coerceIn(marker.radius, currentCanvasPixelHeight - marker.radius)
                                    )
                                )
                            }
                            updatedMarker
                        } else {
                            marker
                        }
                    }
                }
            },
            onStageWidthChanged = { newWidth ->
                stageWidth = newWidth
            },
            onStageDepthChanged = { newDepth ->
                stageDepth = newDepth
            },
            onStageHeightChanged = { newHeight ->
                stageHeight = newHeight
            },
            onNumberOfInputsChanged = { newCount ->
                numberOfInputs = newCount
                initialInputLayoutDone = false
                Log.i("WFSControlApp", "Number of inputs updated to: $newCount via OSC")
            },
            onClusterZChanged = { clusterId, newNormalizedZ ->
                val index = clusterId - 1
                if (index >= 0 && index < clusterNormalizedHeights.size) {
                    val updatedHeights = clusterNormalizedHeights.toMutableList()
                    updatedHeights[index] = newNormalizedZ.coerceIn(0f, 1f)
                    clusterNormalizedHeights = updatedHeights
                    Log.d("OSC_CLUSTER_Z", "Updated cluster $clusterId (index $index) height to $newNormalizedZ via OSC.")
                } else {
                    Log.w("OSC_CLUSTER_Z", "Received Z for out-of-bounds cluster ID: $clusterId. Max clusters: ${clusterNormalizedHeights.size}")
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.height(56.dp),
            containerColor = Color.DarkGray // Set a base color for the TabRow if needed, otherwise it might be transparent or themed
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedTab == index
                Tab(
                    selected = isSelected,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            title,
                            fontSize = dynamicTabFontSize,
                            color = if (isSelected) Color.White else Color.LightGray
                        )
                    },
                    modifier = Modifier
                        .fillMaxHeight()
                        .background(if (isSelected) Color.Black else Color.DarkGray)
                )
            }
        }

        when (selectedTab) {
            0 -> InputMapTab(
                numberOfInputs = numberOfInputs,
                markers = markers,
                onMarkersInitiallyPositioned = { newMarkerList ->
                    markers = newMarkerList
                },
                onCanvasSizeChanged = { width, height ->
                    currentCanvasPixelWidth = width
                    currentCanvasPixelHeight = height
                },
                initialLayoutDone = initialInputLayoutDone,
                onInitialLayoutDone = { initialInputLayoutDone = true },
                stageWidth = stageWidth,
                stageDepth = stageDepth
            )
            1 -> LockingTab(
                numberOfInputs = numberOfInputs,
                markers = markers,
                onMarkersChanged = { updatedMarkers -> markers = updatedMarkers }
            )
            2 -> VisibilityTab(
                numberOfInputs = numberOfInputs,
                markers = markers,
                onMarkersChanged = { updatedMarkers -> markers = updatedMarkers }
            )
            3 -> InputParametersTab()
            4 -> ClusterMapTab(
                clusterMarkers = clusterMarkers,
                onClusterMarkersChanged = { clusterMarkers = it },
                onCanvasSizeChanged = { width, height ->
                    currentCanvasPixelWidth = width
                    currentCanvasPixelHeight = height
                },
                initialLayoutDone = initialClusterLayoutDone,
                onInitialLayoutDone = { initialClusterLayoutDone = true },
                stageWidth = stageWidth,
                stageDepth = stageDepth
            )
            5 -> ClusterHeightTab(
                clusterNormalizedHeights = clusterNormalizedHeights,
                stageHeight = stageHeight,
                onNormalizedHeightChanged = { index, newNormalizedHeight ->
                    val updatedHeights = clusterNormalizedHeights.toMutableList()
                    updatedHeights[index] = newNormalizedHeight.coerceIn(0f, 1f)
                    clusterNormalizedHeights = updatedHeights
                    sendOscClusterZ(context, index + 1, updatedHeights[index])
                }
            )
            6 -> ArrayAdjustTab()
            7 -> SettingsTab(onResetToDefaults = resetToDefaults)
        }
    }
}

@Composable
fun WFS_control_2Theme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(background = Color.Black),
        content = content
    )
}
