package com.wfsdiy.wfs_control_2

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Dropdown menu for selecting enum values
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParameterDropdown(
    label: String,
    selectedIndex: Int,
    options: List<String>,
    onSelectionChange: (Int) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (enabled) Color.White else Color.Gray,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded } // Always allow expansion
        ) {
            OutlinedTextField(
                value = options.getOrNull(selectedIndex) ?: "",
                onValueChange = {},
                readOnly = true,
                enabled = true, // Always enabled for interaction
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = if (enabled) Color.White else Color.Gray,
                    unfocusedTextColor = if (enabled) Color.White else Color.Gray,
                    disabledTextColor = Color.Gray,
                    focusedBorderColor = Color(0xFF00BCD4),
                    unfocusedBorderColor = if (enabled) Color.White else Color.Gray,
                    disabledBorderColor = Color.Gray
                ),
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true) // Always allow menu anchor
                    .fillMaxWidth()
                    .height(48.dp),
                textStyle = TextStyle(fontSize = 14.sp)
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEachIndexed { index, option ->
                    DropdownMenuItem(
                        text = { Text(option, color = Color.White) },
                        onClick = {
                            onSelectionChange(index)
                            expanded = false
                        },
                        modifier = Modifier.background(Color.DarkGray)
                    )
                }
            }
        }
    }
}

/**
 * Text button that toggles between enum values (e.g., ON/OFF, Log/1/d²)
 */
@Composable
fun ParameterTextButton(
    label: String,
    selectedIndex: Int,
    options: List<String>,
    onSelectionChange: (Int) -> Unit,
    enabled: Boolean = true,
    activeColor: Color = Color(0xFF2196F3),
    inactiveColor: Color = Color.DarkGray,
    modifier: Modifier = Modifier
) {
    // Determine the button color based on the selected option
    // For ON/OFF buttons: index 0 = ON (active color with higher opacity for better contrast),
    // index 1 = OFF (inactive color with lower opacity)
    val buttonColor = if (enabled) {
        if (selectedIndex == 0) activeColor.copy(alpha = 0.75f) else inactiveColor
    } else {
        Color.DarkGray
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (enabled) Color.White else Color.Gray,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Button(
            onClick = {
                if (enabled) {
                    val nextIndex = (selectedIndex + 1) % options.size
                    onSelectionChange(nextIndex)
                }
            },
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonColor,
                disabledContainerColor = Color.DarkGray,
                contentColor = Color.White,
                disabledContentColor = Color.Gray
            ),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier
                .width(210.dp)
                .height(48.dp)
        ) {
            Text(
                text = options.getOrNull(selectedIndex) ?: "",
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Editable number box with label and unit
 */
@Composable
fun ParameterNumberBox(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    unit: String = "",
    enabled: Boolean = true,
    isDecimal: Boolean = true,
    modifier: Modifier = Modifier,
    onValueCommit: ((String) -> Unit)? = null
) {
    val focusManager = LocalFocusManager.current
    var lastCommittedValue by remember { mutableStateOf(value) }
    
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (enabled) Color.White else Color.Gray,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .border(1.dp, if (enabled) Color.White else Color.Gray)
                .background(Color.DarkGray)
                .padding(horizontal = 8.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = { newValue ->
                    if (enabled) {
                        // Filter to allow only valid numbers
                        val filtered = if (isDecimal) {
                            newValue.filter { it.isDigit() || it == '.' || it == '-' }
                        } else {
                            newValue.filter { it.isDigit() || it == '-' }
                        }
                        onValueChange(filtered)
                    }
                },
                enabled = enabled,
                textStyle = TextStyle(
                    color = if (enabled) Color.White else Color.Gray,
                    fontSize = 14.sp,
                    textAlign = TextAlign.End
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (isDecimal) KeyboardType.Decimal else KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        if (value != lastCommittedValue) {
                            onValueCommit?.invoke(value)
                            lastCommittedValue = value
                        }
                    }
                ),
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged { focusState: FocusState ->
                        if (!focusState.isFocused && value != lastCommittedValue) {
                            onValueCommit?.invoke(value)
                            lastCommittedValue = value
                        }
                    }
            )
            
            if (unit.isNotEmpty()) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = unit,
                    fontSize = 14.sp,
                    color = if (enabled) Color.White else Color.Gray
                )
            }
        }
    }
}

/**
 * Text box for string values (like input name)
 */
@Composable
fun ParameterTextBox(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean = true,
    maxLength: Int = 24,
    modifier: Modifier = Modifier,
    onValueCommit: ((String) -> Unit)? = null,
    height: androidx.compose.ui.unit.Dp = 48.dp
) {
    val focusManager = LocalFocusManager.current
    var lastCommittedValue by remember { mutableStateOf(value) }

    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (enabled) Color.White else Color.Gray,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                if (enabled && newValue.length <= maxLength) {
                    onValueChange(newValue)
                }
            },
            enabled = enabled,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = if (enabled) Color.White else Color.Gray,
                unfocusedTextColor = if (enabled) Color.White else Color.Gray,
                disabledTextColor = Color.Gray,
                focusedBorderColor = Color(0xFF00BCD4),
                unfocusedBorderColor = if (enabled) Color.White else Color.Gray,
                disabledBorderColor = Color.Gray
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .onFocusChanged { focusState: FocusState ->
                    if (!focusState.isFocused && value != lastCommittedValue) {
                        onValueCommit?.invoke(value)
                        lastCommittedValue = value
                    }
                },
            textStyle = TextStyle(fontSize = 14.sp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    if (value != lastCommittedValue) {
                        onValueCommit?.invoke(value)
                        lastCommittedValue = value
                    }
                }
            )
        )
    }
}

/**
 * Input selector with grid overlay for selecting which input channel to edit (1-64)
 */
@Composable
fun InputChannelSelector(
    selectedInputId: Int,
    maxInputs: Int,
    onInputSelected: (Int) -> Unit,
    onOpenSelector: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Get the marker color for the selected input
    val markerColor = getMarkerColor(selectedInputId, isClusterMarker = false)

    Column(modifier = modifier) {
        Text(
            text = "Input Channel",
            fontSize = 14.sp,
            color = Color.White,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // Compact selector button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(markerColor, shape = RoundedCornerShape(4.dp))
                .clickable { onOpenSelector() }
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Input $selectedInputId",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "▼",
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Grid overlay for selecting input channel
 */
@Composable
fun InputChannelGridOverlay(
    selectedInputId: Int,
    maxInputs: Int,
    onInputSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    // Calculate grid columns based on maxInputs
    val columns = when {
        maxInputs <= 16 -> 4
        maxInputs <= 32 -> 6
        else -> 8
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight()
                .heightIn(max = 600.dp)
                .background(Color(0xFF1E1E1E), shape = RoundedCornerShape(16.dp))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Select Input Channel",
                    fontSize = 20.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("✕", fontSize = 24.sp, color = Color.White)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Grid of input numbers
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(count = maxInputs) { index ->
                    val inputId = index + 1
                    val isSelected = inputId == selectedInputId

                    // Get the marker color for this input
                    val markerColor = getMarkerColor(inputId, isClusterMarker = false)
                    val backgroundColor = if (isSelected) markerColor else markerColor.copy(alpha = 0.5f)

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .background(
                                backgroundColor,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onInputSelected(inputId) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$inputId",
                            fontSize = 18.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = Color.White
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Footer info
            Text(
                text = "Showing 1-$maxInputs of 64 inputs",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

/**
 * Section header for grouping parameters
 */
@Composable
fun ParameterSectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = Color.Gray,
            thickness = 1.dp
        )
        Text(
            text = title,
            fontSize = 18.sp,
            color = Color(0xFF00BCD4),
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}

