package com.wfsdiy.wfs_control_2

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTab(
    onResetToDefaults: () -> Unit,
    onShutdownApp: () -> Unit,
    onNetworkParametersChanged: (() -> Unit)? = null,
    inputSecondaryAngularMode: SecondaryTouchFunction = SecondaryTouchFunction.OFF,
    onInputSecondaryAngularModeChanged: (SecondaryTouchFunction) -> Unit = {},
    inputSecondaryRadialMode: SecondaryTouchFunction = SecondaryTouchFunction.OFF,
    onInputSecondaryRadialModeChanged: (SecondaryTouchFunction) -> Unit = {},
    clusterSecondaryAngularEnabled: Boolean = false,
    onClusterSecondaryAngularEnabledChanged: (Boolean) -> Unit = {},
    clusterSecondaryRadialEnabled: Boolean = false,
    onClusterSecondaryRadialEnabledChanged: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    var showResetDialog by remember { mutableStateOf(false) }
    var showShutdownDialog by remember { mutableStateOf(false) }

    // State for showing mode selector overlays
    var showInputAngularSelector by remember { mutableStateOf(false) }
    var showInputRadialSelector by remember { mutableStateOf(false) }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Confirm Reset") },
            text = { Text("Are you sure you want to reset the number of inputs, lock states, and visibility states to their defaults?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onResetToDefaults()
                        showResetDialog = false
                    }
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showShutdownDialog) {
        AlertDialog(
            onDismissRequest = { showShutdownDialog = false },
            title = { Text("Confirm Shutdown") },
            text = { Text("Are you sure you want to shut down the application and OSC server?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onShutdownApp()
                        showShutdownDialog = false
                    }
                ) {
                    Text("Shutdown")
                }
            },
            dismissButton = {
                TextButton(onClick = { showShutdownDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Secondary Touch Functions Section
            Text(
                "Secondary Touch Functions",
                style = MaterialTheme.typography.headlineSmall.copy(color = Color.White),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Input Map Controls Row
            Column(
                modifier = Modifier.fillMaxWidth(0.8f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
            Text(
                "Input Map Controls",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Calculate Input Map marker 20 color
                val inputMapColor = run {
                    val hue = (20 * 360f / 32) % 360f
                    Color.hsl(hue, 0.9f, 0.6f)
                }

                // Input Map Angular Mode Selector
                ModeSelectorButton(
                    label = "Angular Change Function",
                    selectedMode = inputSecondaryAngularMode,
                    onOpenSelector = { showInputAngularSelector = true },
                    modifier = Modifier.weight(1f),
                    baseColor = inputMapColor
                )

                // Input Map Radial Mode Selector
                ModeSelectorButton(
                    label = "Radial Change Function",
                    selectedMode = inputSecondaryRadialMode,
                    onOpenSelector = { showInputRadialSelector = true },
                    modifier = Modifier.weight(1f),
                    baseColor = inputMapColor
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Cluster Map Controls Row
        Column(
            modifier = Modifier.fillMaxWidth(0.8f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Cluster Map Controls",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Cluster Angular Control Button
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Angular Change",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    // Calculate Input Map marker 10 color for Angular
                    val hue = (10 * 360f / 32) % 360f
                    val saturatedColor = Color.hsl(hue, 0.9f, 0.6f)
                    val buttonColor = if (clusterSecondaryAngularEnabled) {
                        saturatedColor.copy(alpha = 0.75f)
                    } else {
                        saturatedColor.copy(alpha = 0.3f)
                    }

                    Button(
                        onClick = { onClusterSecondaryAngularEnabledChanged(!clusterSecondaryAngularEnabled) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = buttonColor,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (clusterSecondaryAngularEnabled) "ON" else "OFF",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                // Cluster Radial Control Button
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Radial Change",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    // Calculate Input Map marker 10 color for Radial (same as Angular)
                    val hue = (10 * 360f / 32) % 360f
                    val saturatedColor = Color.hsl(hue, 0.9f, 0.6f)
                    val buttonColor = if (clusterSecondaryRadialEnabled) {
                        saturatedColor.copy(alpha = 0.75f)
                    } else {
                        saturatedColor.copy(alpha = 0.3f)
                    }

                    Button(
                        onClick = { onClusterSecondaryRadialEnabledChanged(!clusterSecondaryRadialEnabled) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = buttonColor,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (clusterSecondaryRadialEnabled) "ON" else "OFF",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Existing NetworkTab content can be integrated here if you have it in a separate composable
        NetworkTab(onNetworkParametersChanged = onNetworkParametersChanged)

        Spacer(modifier = Modifier.height(32.dp))

        // Reset and Shutdown buttons side by side
        Row(
            modifier = Modifier.fillMaxWidth(0.8f),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Calculate Input Map marker 19 color for Reset button
            val resetButtonColor = run {
                val hue = (19 * 360f / 32) % 360f
                Color.hsl(hue, 0.9f, 0.6f)
            }

            Button(
                onClick = { showResetDialog = true },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = resetButtonColor,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("Reset App Settings to Defaults")
            }

            Button(
                onClick = { showShutdownDialog = true },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("Shutdown Application")
            }
        }
    }

    // Mode selector overlays (rendered outside the scrollable column, at Box level)
    // Calculate Input Map marker 20 color for overlays
    val inputMapColor = run {
        val hue = (20 * 360f / 32) % 360f
        Color.hsl(hue, 0.9f, 0.6f)
    }

    if (showInputAngularSelector) {
        ModeGridOverlay(
            title = "Select Angular Change Function",
            selectedMode = inputSecondaryAngularMode,
            excludedMode = inputSecondaryRadialMode,
            onModeSelected = { mode ->
                onInputSecondaryAngularModeChanged(mode)
                showInputAngularSelector = false
            },
            onDismiss = { showInputAngularSelector = false },
            baseColor = inputMapColor
        )
    }

    if (showInputRadialSelector) {
        ModeGridOverlay(
            title = "Select Radial Change Function",
            selectedMode = inputSecondaryRadialMode,
            excludedMode = inputSecondaryAngularMode,
            onModeSelected = { mode ->
                onInputSecondaryRadialModeChanged(mode)
                showInputRadialSelector = false
            },
            onDismiss = { showInputRadialSelector = false },
            baseColor = inputMapColor
        )
    }
    }
}
