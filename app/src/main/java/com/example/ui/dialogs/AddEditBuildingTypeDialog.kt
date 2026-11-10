package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.data.entity.BuildingTypeEntity
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.BluePrimaryContainer
import com.example.ui.theme.LightBorder
import com.example.ui.theme.LightSurface
import com.example.ui.theme.RedDanger
import com.example.ui.theme.SlateSecondaryContainer
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

val AvailableCategoryIcons = listOf(
    "DEFENCE" to Icons.Default.Shield,
    "RESOURCE" to Icons.Default.Storage,
    "ARMY" to Icons.Default.LocalFireDepartment,
    "TRAPS" to Icons.Default.Dangerous,
    "OTHER" to Icons.Default.Build
)

@Composable
fun AddEditBuildingTypeDialog(
    typeToEdit: BuildingTypeEntity? = null,
    onSave: (name: String, iconKey: String, priority: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(typeToEdit?.name ?: "") }
    var selectedIconKey by remember { mutableStateOf(typeToEdit?.iconKey ?: "DEFENCE") }
    var priority by remember { mutableIntStateOf(typeToEdit?.priority ?: 1) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
                        text = if (typeToEdit == null) "Add Category" else "Edit Category",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_type_dialog")) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Name
                Text("Category Name", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMessage = null
                    },
                    placeholder = { Text("e.g. Defence, Resource, Army...", color = TextMuted) },
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
                        .testTag("type_name_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Priority Stepper
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Category Priority", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Text(
                            text = "1 is highest priority (1 before 2)",
                            color = BluePrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SlateSecondaryContainer)
                            .border(1.dp, LightBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { if (priority > 1) priority-- },
                            enabled = priority > 1,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("priority_dec")
                        ) {
                            Icon(
                                Icons.Default.Remove,
                                contentDescription = "Decrease Priority",
                                tint = if (priority > 1) TextPrimary else TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Text(
                            text = "$priority",
                            color = BluePrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        IconButton(
                            onClick = { priority++ },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("priority_inc")
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Increase Priority",
                                tint = TextPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Icon selection
                Text("Icon Symbol", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AvailableCategoryIcons.forEach { (key, icon) ->
                        val isSelected = selectedIconKey == key
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) BluePrimaryContainer else SlateSecondaryContainer)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) BluePrimary else LightBorder,
                                    shape = CircleShape
                                )
                                .clickable { selectedIconKey = key },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = key,
                                tint = if (isSelected) BluePrimary else TextSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorMessage ?: "", color = RedDanger, fontSize = 12.sp)
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
                            if (name.isBlank()) {
                                errorMessage = "Please enter a category name."
                                return@Button
                            }
                            onSave(name.trim(), selectedIconKey, priority)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary, contentColor = Color.White),
                        modifier = Modifier.testTag("save_type_button")
                    ) {
                        Text(if (typeToEdit == null) "Add" else "Save", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
