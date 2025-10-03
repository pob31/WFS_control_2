package com.wfsdiy.wfs_control_2

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
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
    onNetworkParametersChanged: (() -> Unit)? = null,
    secondaryTouchMode: SecondaryTouchMode = SecondaryTouchMode.ATTENUATION_DELAY,
    onSecondaryTouchModeChanged: (SecondaryTouchMode) -> Unit = {},
    clusterSecondaryTouchEnabled: Boolean = true,
    onClusterSecondaryTouchEnabledChanged: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Confirm Reset") },
            text = { Text("Are you sure you want to reset the number of inputs, lock states, and visibility states to their defaults?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onResetToDefaults()
                        showDialog = false
                    }
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Secondary Touch Functions Section
        Text(
            "Secondary touch functions",
            style = MaterialTheme.typography.headlineSmall.copy(color = Color.White),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(0.8f),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            var expanded by remember { mutableStateOf(false) }
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = secondaryTouchMode.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Input Map - Select Mode") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.LightGray,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.LightGray
                    ),
                    textStyle = TextStyle(color = Color.White, fontSize = 16.sp)
                )
                
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(Color.DarkGray)
                ) {
                    SecondaryTouchMode.values().forEach { mode ->
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    mode.displayName,
                                    color = Color.White,
                                    fontSize = 14.sp
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
            
            // Cluster Secondary Touch Enable/Disable Button
            Button(
                onClick = { onClusterSecondaryTouchEnabledChanged(!clusterSecondaryTouchEnabled) },
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = if (clusterSecondaryTouchEnabled) Color(0xFF2E7D32) else Color.Red
                ),
                modifier = Modifier.width(120.dp)
            ) {
                Text(
                    if (clusterSecondaryTouchEnabled) "Cluster Enabled" else "Cluster Disabled",
                    color = Color.White,
                    fontSize = 16.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Existing NetworkTab content can be integrated here if you have it in a separate composable
        NetworkTab(onNetworkParametersChanged = onNetworkParametersChanged)

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = { showDialog = true }) {
            Text("Reset App Settings to Defaults")
        }
    }
}
