package com.wfsdiy.wfs_control_2

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
    secondaryTouchMode: SecondaryTouchMode = SecondaryTouchMode.ATTENUATION_DELAY,
    onSecondaryTouchModeChanged: (SecondaryTouchMode) -> Unit = {},
    clusterSecondaryTouchEnabled: Boolean = true,
    onClusterSecondaryTouchEnabledChanged: (Boolean) -> Unit = {},
    clusterSecondaryAngularEnabled: Boolean = true,
    onClusterSecondaryAngularEnabledChanged: (Boolean) -> Unit = {},
    clusterSecondaryRadialEnabled: Boolean = true,
    onClusterSecondaryRadialEnabledChanged: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    var showResetDialog by remember { mutableStateOf(false) }
    var showShutdownDialog by remember { mutableStateOf(false) }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Secondary Touch Functions Section
        Text(
            "Secondary touch functions",
            style = MaterialTheme.typography.headlineSmall.copy(color = Color.White),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(0.95f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            var expanded by remember { mutableStateOf(false) }

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.width(280.dp)
            ) {
                OutlinedTextField(
                    value = secondaryTouchMode.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Input Map - Select Mode") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.LightGray,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.LightGray
                    ),
                    textStyle = TextStyle(color = Color.White, fontSize = 14.sp)
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(Color.DarkGray)
                ) {
                    SecondaryTouchMode.entries.forEach { mode ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    mode.displayName,
                                    color = Color.White,
                                    fontSize = 13.sp
                                )
                            },
                            onClick = {
                                onSecondaryTouchModeChanged(mode)
                                expanded = false
                            },
                        )
                    }
                }
            }

            // Cluster Map Secondary Touch Buttons
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    "Cluster Map Controls",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.White, fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Angular Control Button
                    Button(
                        onClick = { onClusterSecondaryAngularEnabledChanged(!clusterSecondaryAngularEnabled) },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = if (clusterSecondaryAngularEnabled) Color(0xFF2E7D32) else Color.Red,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            if (clusterSecondaryAngularEnabled) "Angular ON" else "Angular OFF",
                            fontSize = 14.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }

                    // Radial Control Button
                    Button(
                        onClick = { onClusterSecondaryRadialEnabledChanged(!clusterSecondaryRadialEnabled) },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = if (clusterSecondaryRadialEnabled) Color(0xFF2E7D32) else Color.Red,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            if (clusterSecondaryRadialEnabled) "Radial ON" else "Radial OFF",
                            fontSize = 14.sp,
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
            Button(
                onClick = { showResetDialog = true },
                modifier = Modifier.weight(1f),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1976D2),
                    contentColor = Color.White
                )
            ) {
                Text("Reset App Settings to Defaults")
            }
            
            Button(
                onClick = { showShutdownDialog = true },
                modifier = Modifier.weight(1f),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = Color.Red,
                    contentColor = Color.White
                )
            ) {
                Text("Shutdown Application")
            }
        }
    }
}
