package com.example.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.entity.TownHallEntity
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.LightBorder
import com.example.ui.theme.LightSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AddEditTownHallDialog(
    townHallToEdit: TownHallEntity? = null,
    village: String,
    onSave: (level: Int, name: String, makeActive: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val maxAllowedLevel = if (village == "HOME") 18 else 10
    var level by remember { mutableIntStateOf(townHallToEdit?.level ?: 1) }
    var name by remember {
        mutableStateOf(
            townHallToEdit?.name ?: if (village == "HOME") "Town Hall $level" else "Builder Hall $level"
        )
    }
    var makeActive by remember { mutableStateOf(townHallToEdit?.isActive ?: false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp)),
            color = LightSurface,
            tonalElevation = 6.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, LightBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (townHallToEdit == null) "Add Town Hall" else "Edit Town Hall",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_th_dialog")) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Level Stepper
                Text(
                    text = if (village == "HOME") "Town Hall Level (1 to $maxAllowedLevel)" else "Builder Hall Level (1 to $maxAllowedLevel)",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                LevelStepper(
                    value = level,
                    minValue = 1,
                    maxValue = maxAllowedLevel,
                    onValueChange = {
                        level = it
                        if (name.startsWith("Town Hall") || name.startsWith("Builder Hall")) {
                            name = if (village == "HOME") "Town Hall $level" else "Builder Hall $level"
                        }
                    },
                    testTagPrefix = "th_level"
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Custom Name / Nickname
                Text("Display Name / Title", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("e.g. Town Hall 16", color = TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BluePrimary,
                        unfocusedBorderColor = LightBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = LightSurface,
                        unfocusedContainerColor = LightSurface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("th_name_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Make Active checkbox
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = makeActive,
                        onCheckedChange = { makeActive = it },
                        colors = CheckboxDefaults.colors(checkedColor = BluePrimary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Set as current active village level",
                        color = TextPrimary,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LightBorder)
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            val finalName = if (name.isBlank()) {
                                if (village == "HOME") "Town Hall $level" else "Builder Hall $level"
                            } else name.trim()
                            onSave(level, finalName, makeActive)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary, contentColor = Color.White),
                        modifier = Modifier.testTag("save_th_button")
                    ) {
                        Text(if (townHallToEdit == null) "Add" else "Save", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
