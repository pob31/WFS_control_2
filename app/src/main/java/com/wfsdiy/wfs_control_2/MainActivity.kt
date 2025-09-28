package com.wfsdiy.wfs_control_2

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.min
import kotlin.math.max
import kotlinx.parcelize.Parcelize


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
    var positionX: Float,
    var positionY: Float,
    val radius: Float,
    var isLocked: Boolean = false,
    var isVisible: Boolean = true,
    var name: String = ""
) : Parcelable {
    // Helper property to get Offset
    var position: Offset
        get() = Offset(positionX, positionY)
        set(value) {
            positionX = value.x
            positionY = value.y
        }
}

// Define the ClusterMarker data class (Simplified)
@Parcelize
data class ClusterMarker(
    val id: Int,
    var positionX: Float,
    var positionY: Float,
    val radius: Float
) : Parcelable {
    // Helper property to get Offset
    var position: Offset
        get() = Offset(positionX, positionY)
        set(value) {
            positionX = value.x
            positionY = value.y
        }
}

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

        // Handle notification intent
        handleNotificationIntent(intent)

        setContent {
            WFS_control_2Theme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    WFSControlApp()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        if (intent?.action == "com.wfsdiy.wfs_control_2.NOTIFICATION_TAP") {
            Log.i("MainActivity", "Notification tapped - bringing app to foreground")
            // The app is already in foreground, no additional action needed
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
    var oscService by remember { mutableStateOf<OscService?>(null) }
    var isBound by remember { mutableStateOf(false) }
    val viewModel: MainActivityViewModel? = if (isBound) {
        viewModel(factory = MainActivityViewModel.Factory(oscService!!))
    } else {
        null
    }

    // Keep all existing local state for UI interactions (unchanged)
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthDp = configuration.screenWidthDp.dp
    val screenDensity = density.density
    
    // Calculate responsive marker radius based on screen size and density
    val baseMarkerRadius = (screenWidthDp.value / 40f).coerceIn(7.5f, 17.5f) // 7.5-17.5dp range (half size)
    val responsiveMarkerRadius = (baseMarkerRadius * screenDensity).coerceIn(7.5f, 17.5f)
    val markerRadiusPx = responsiveMarkerRadius.dpToPx()
    var numberOfInputs by rememberSaveable { mutableStateOf(MAX_INPUTS) }
    
    // Screen dimensions for OSC operations
    var screenWidthPx by remember { mutableStateOf(0f) }
    var screenHeightPx by remember { mutableStateOf(0f) }
    var oscServiceStarted by remember { mutableStateOf(false) }
    
    var markers by remember {
        mutableStateOf(
            List(MAX_INPUTS) { index ->
                Marker(
                    id = index + 1,
                    positionX = screenWidthDp.value * 0.1f, // 10% from left edge
                    positionY = screenWidthDp.value * 0.1f, // 10% from top edge
                    radius = markerRadiusPx,
                    name = ""
                )
            }
        )
    }
    var initialInputLayoutDone by remember { mutableStateOf(false) }

    var clusterMarkers by rememberSaveable {
        mutableStateOf(
            List(10) { index ->
                ClusterMarker(
                    id = index + 1,
                    positionX = screenWidthDp.value * 0.1f, // 10% from left edge
                    positionY = screenWidthDp.value * 0.1f, // 10% from top edge
                    radius = markerRadiusPx
                )
            }
        )
    }
    var clusterNormalizedHeights by rememberSaveable { mutableStateOf(List(10) { 0.2f }) }
    var initialClusterLayoutDone by remember { mutableStateOf(false) }

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Input Map", "Lock Input Markers", "View Input Markers", "Input Parameters", "Cluster Map", "Cluster Height", "Array Adjust", "Settings")

    val dynamicTabFontSize: TextUnit = remember(screenWidthDp) {
        val baseSize = screenWidthDp.value / 35f
        val fontSize = max(10f, min(20f, baseSize))
        fontSize.sp
    }

    var currentCanvasPixelWidth by remember { mutableStateOf(0f) }
    var currentCanvasPixelHeight by remember { mutableStateOf(0f) }

    var stageWidth by remember { mutableStateOf(16.0f) }
    var stageDepth by remember { mutableStateOf(10.0f) }
    var stageHeight by remember { mutableStateOf(7.0f) }

    val serviceConnection = remember {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                Log.i("WFSControlApp", "onServiceConnected called with service: $service")
                val binder = service as OscService.OscBinder
                oscService = binder.getService()
                isBound = true
                Log.i("WFSControlApp", "OscService connected. Service: $oscService")
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                oscService = null
                isBound = false
                Log.i("WFSControlApp", "OscService disconnected.")
            }
        }
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Intent(context, OscService::class.java).also { intent ->
                ContextCompat.startForegroundService(context, intent)
                context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            }
        } else {
            Log.w("WFSControlApp", "Notification permission denied.")
        }
    }

    // Load settings and start service
    LaunchedEffect(Unit) {
        // Get screen dimensions first
        val displayMetrics = context.resources.displayMetrics
        screenWidthPx = displayMetrics.widthPixels.toFloat()
        screenHeightPx = displayMetrics.heightPixels.toFloat()
        Log.i("WFSControlApp", "Initialized screen dimensions: ${screenWidthPx}x${screenHeightPx}")
        
        // Initialize shared canvas dimensions immediately with screen dimensions
        CanvasDimensions.updateDimensions(screenWidthPx, screenHeightPx)
        Log.i("WFSControlApp", "Initialized shared canvas dimensions: ${screenWidthPx}x${screenHeightPx}")
        
        val (loadedInputs, loadedLockStates, loadedVisibilityStates) = loadAppSettings(context)
        numberOfInputs = loadedInputs
        markers = markers.mapIndexed { index, marker ->
            marker.copy(
                isLocked = loadedLockStates.getOrElse(index) { false },
                isVisible = loadedVisibilityStates.getOrElse(index) { true }
            )
        }
        initialInputLayoutDone = false
        
        // Start OSC service with screen dimensions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED -> {
                    Log.i("WFSControlApp", "Starting OscService with notification permission")
                    Intent(context, OscService::class.java).also { intent ->
                        ContextCompat.startForegroundService(context, intent)
                        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
                        Log.i("WFSControlApp", "OscService startForegroundService and bindService called")
                    }
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            Log.i("WFSControlApp", "Starting OscService (no notification permission needed)")
            Intent(context, OscService::class.java).also { intent ->
                ContextCompat.startForegroundService(context, intent)
                context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
                Log.i("WFSControlApp", "OscService startForegroundService and bindService called")
            }
        }
    }

    // Start OSC server when service is bound (canvas dimensions already initialized)
    LaunchedEffect(isBound, oscService) {
        if (isBound && viewModel != null && !oscServiceStarted) {
            Log.i("WFSControlApp", "Starting OSC server (canvas dimensions already initialized)")
            viewModel.startOscServer()
            oscServiceStarted = true
        }
    }

    // Process buffered OSC data when service is available
    LaunchedEffect(isBound, oscService) {
        if (isBound && viewModel != null) {
            Log.i("WFSControlApp", "Service is bound, checking OSC server status...")
            val serverRunning = oscService?.isOscServerRunning() ?: false
            Log.i("WFSControlApp", "OSC server running: $serverRunning")
            while (true) {
                kotlinx.coroutines.delay(100) // Check every 100ms for buffered data
                
                // Process buffered marker updates
                val markerUpdates = viewModel.getBufferedMarkerUpdates()
                if (markerUpdates.isNotEmpty()) {
                    Log.d("BUFFERED_MARKERS", "Processing ${markerUpdates.size} buffered marker updates")
                }
                markerUpdates.forEach { update ->
                    if (update.isCluster) {
                        Log.d("CLUSTER_UPDATE", "Processing cluster marker update for ID ${update.id}")
                        val updatedClusterMarkers = clusterMarkers.map { clusterMarker ->
                            if (clusterMarker.id == update.id) {
                                var updatedMarker = clusterMarker
                                update.position?.let {
                                    val (canvasWidth, canvasHeight) = CanvasDimensions.getCurrentDimensions()
                                    updatedMarker = updatedMarker.copy(
                                        positionX = it.x.coerceIn(clusterMarker.radius, canvasWidth - clusterMarker.radius),
                                        positionY = it.y.coerceIn(clusterMarker.radius, canvasHeight - clusterMarker.radius)
                                    )
                                    Log.d("CLUSTER_UPDATE", "Updated cluster marker ${update.id} to position (${it.x}, ${it.y}) using canvas dimensions ${canvasWidth}x${canvasHeight}")
                                }
                                updatedMarker
                            } else {
                                clusterMarker
                            }
                        }
                        clusterMarkers = updatedClusterMarkers
                        Log.d("CLUSTER_UPDATE", "Cluster markers state updated, new count: ${clusterMarkers.size}")
                    } else {
                        markers = markers.map { marker ->
                            if (marker.id == update.id) {
                                var updatedMarker = marker
                                update.name?.let { updatedMarker = updatedMarker.copy(name = it) }
                                update.position?.let {
                                    val (canvasWidth, canvasHeight) = CanvasDimensions.getCurrentDimensions()
                                    updatedMarker = updatedMarker.copy(
                                        positionX = it.x.coerceIn(marker.radius, canvasWidth - marker.radius),
                                        positionY = it.y.coerceIn(marker.radius, canvasHeight - marker.radius)
                                    )
                                    Log.d("MARKER_UPDATE", "Updated marker ${update.id} to position (${it.x}, ${it.y}) using canvas dimensions ${canvasWidth}x${canvasHeight}")
                                }
                                updatedMarker
                            } else {
                                marker
                            }
                        }
                    }
                }
                
                // Process buffered stage updates
                val stageUpdates = viewModel.getBufferedStageUpdates()
                stageUpdates.forEach { update ->
                    when (update.type) {
                        "width" -> stageWidth = update.value
                        "depth" -> stageDepth = update.value
                        "height" -> stageHeight = update.value
                    }
                }
                
                // Process buffered inputs updates
                val inputsUpdates = viewModel.getBufferedInputsUpdates()
                inputsUpdates.forEach { update ->
                    numberOfInputs = update.count
                    initialInputLayoutDone = false
                    Log.i("WFSControlApp", "Number of inputs updated to: ${update.count} via OSC")
                }
                
                // Process buffered cluster Z updates
                val clusterZUpdates = viewModel.getBufferedClusterZUpdates()
                clusterZUpdates.forEach { update ->
                    val index = update.clusterId - 1
                    if (index >= 0 && index < clusterNormalizedHeights.size) {
                        val updatedHeights = clusterNormalizedHeights.toMutableList()
                        updatedHeights[index] = update.normalizedZ.coerceIn(0f, 1f)
                        clusterNormalizedHeights = updatedHeights
                        Log.d("OSC_CLUSTER_Z", "Updated cluster ${update.clusterId} (index $index) height to ${update.normalizedZ} via OSC.")
                    } else {
                        Log.w("OSC_CLUSTER_Z", "Received Z for out-of-bounds cluster ID: ${update.clusterId}. Max clusters: ${clusterNormalizedHeights.size}")
                    }
                }
            }
        }
    }

    // Sync local state with service when it changes
    LaunchedEffect(markers, clusterMarkers, clusterNormalizedHeights, stageWidth, stageDepth, stageHeight, numberOfInputs) {
        if (isBound && viewModel != null) {
            viewModel.syncMarkers(markers)
            viewModel.syncClusterMarkers(clusterMarkers)
            viewModel.syncClusterHeights(clusterNormalizedHeights)
            viewModel.syncStageDimensions(stageWidth, stageDepth, stageHeight)
            viewModel.syncNumberOfInputs(numberOfInputs)
        }
    }

    // Save settings when they change
    LaunchedEffect(markers, numberOfInputs) {
        saveAppSettings(context, numberOfInputs, markers)
    }

    DisposableEffect(Unit) {
        onDispose {
            if (isBound) {
                context.unbindService(serviceConnection)
            }
        }
    }

    val resetToDefaults = {
        numberOfInputs = MAX_INPUTS
        markers = markers.map { marker ->
            marker.copy(
                positionX = screenWidthDp.value * 0.1f, // 10% from left edge
                positionY = screenWidthDp.value * 0.1f, // 10% from top edge
                isLocked = false, 
                isVisible = true
            )
        }
        initialInputLayoutDone = false
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
                    // Send OSC data via service if available
                    val firstMarker = newMarkerList.firstOrNull()
                    if (firstMarker != null) {
                        viewModel?.sendMarkerPosition(
                            firstMarker.id, 
                            firstMarker.positionX, 
                            firstMarker.positionY, 
                            false
                        )
                    }
                },
                onCanvasSizeChanged = { width, height ->
                    currentCanvasPixelWidth = width
                    currentCanvasPixelHeight = height
                    // Update shared canvas dimensions
                    CanvasDimensions.updateDimensions(width, height)
                },
                initialLayoutDone = initialInputLayoutDone,
                onInitialLayoutDone = { initialInputLayoutDone = true },
                stageWidth = stageWidth,
                stageDepth = stageDepth
            )
            1 -> LockingTab(
                numberOfInputs = numberOfInputs,
                markers = markers,
                onMarkersChanged = { updatedMarkers -> 
                    markers = updatedMarkers
                    // Update service with new marker states
                    viewModel?.syncMarkers(updatedMarkers)
                }
            )
            2 -> VisibilityTab(
                numberOfInputs = numberOfInputs,
                markers = markers,
                onMarkersChanged = { updatedMarkers -> 
                    markers = updatedMarkers
                    // Update service with new marker states
                    viewModel?.syncMarkers(updatedMarkers)
                }
            )
            3 -> InputParametersTab()
            4 -> ClusterMapTab(
                clusterMarkers = clusterMarkers,
                onClusterMarkersChanged = { updatedClusterMarkers ->
                    Log.d("CLUSTER_TAB", "ClusterMapTab received ${updatedClusterMarkers.size} markers")
                    updatedClusterMarkers.forEach { marker ->
                        Log.d("CLUSTER_TAB", "Marker ${marker.id}: position (${marker.positionX}, ${marker.positionY})")
                    }
                    
                    // Only update state if there are actual changes (not just initial layout)
                    val hasChanges = updatedClusterMarkers.any { updatedMarker ->
                        val currentMarker = clusterMarkers.find { it.id == updatedMarker.id }
                        currentMarker == null || 
                        currentMarker.positionX != updatedMarker.positionX || 
                        currentMarker.positionY != updatedMarker.positionY
                    }
                    
                    if (hasChanges) {
                        Log.d("CLUSTER_TAB", "Updating cluster markers state due to changes")
                        clusterMarkers = updatedClusterMarkers
                        // Update service with new cluster marker states
                        viewModel?.syncClusterMarkers(updatedClusterMarkers)
                    } else {
                        Log.d("CLUSTER_TAB", "No changes detected, skipping state update")
                    }
                },
                onCanvasSizeChanged = { width, height ->
                    currentCanvasPixelWidth = width
                    currentCanvasPixelHeight = height
                    // Update shared canvas dimensions
                    CanvasDimensions.updateDimensions(width, height)
                },
                initialLayoutDone = initialClusterLayoutDone,
                onInitialLayoutDone = { initialClusterLayoutDone = true },
                stageWidth = stageWidth,
                stageDepth = stageDepth,
                viewModel = viewModel
            )
            5 -> ClusterHeightTab(
                clusterNormalizedHeights = clusterNormalizedHeights,
                stageHeight = stageHeight,
                onNormalizedHeightChanged = { index, newNormalizedHeight ->
                    val updatedHeights = clusterNormalizedHeights.toMutableList()
                    updatedHeights[index] = newNormalizedHeight.coerceIn(0f, 1f)
                    clusterNormalizedHeights = updatedHeights
                    // Send OSC data via service if available
                    viewModel?.sendClusterZ(index + 1, updatedHeights[index])
                }
            )
            6 -> ArrayAdjustTab()
            7 -> SettingsTab(
                onResetToDefaults = resetToDefaults,
                onNetworkParametersChanged = {
                    // Update service network parameters when settings change
                    oscService?.updateNetworkParameters()
                }
            )
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
