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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import com.example.data.entity.BuildingEntity
import com.example.data.entity.LevelCount
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberPending
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.BluePrimaryContainer
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.LightBorder
import com.example.ui.theme.LightSurface
import com.example.ui.theme.RedDanger
import com.example.ui.theme.RedDangerContainer
import com.example.ui.theme.SlateSecondaryContainer
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ManageBuildingCountDialog(
    building: BuildingEntity,
    onSave: (totalCount: Int, maxLevel: Int, distributions: List<LevelCount>) -> Unit,
    onDismiss: () -> Unit
) {
    var totalCount by remember { mutableIntStateOf(maxOf(1, building.totalCount)) }
    var maxLevel by remember { mutableIntStateOf(maxOf(1, building.maxLevel)) }

    val initialDist = remember(building) {
        val parsed = building.parseLevelDistributions()
        if (parsed.isNotEmpty()) parsed else listOf(LevelCount(level = building.currentLevel, count = totalCount))
    }

    val distributionRows = remember {
        mutableStateListOf<LevelCount>().apply {
            addAll(initialDist)
        }
    }

    val allocatedSum = distributionRows.sumOf { it.count }
    val isBalanced = allocatedSum == totalCount

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(BluePrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = null,
                        tint = BluePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Manage Building Counts",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Text(
                        text = building.name,
                        color = BluePrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("manage_building_count_dialog")
            ) {
                // Section 1: Total Count & Max Level Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Total Count Stepper Card
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SlateSecondaryContainer)
                            .border(1.dp, LightBorder, RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "TOTAL COUNT",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            IconButton(
                                onClick = { if (totalCount > 1) totalCount-- },
                                enabled = totalCount > 1,
                                modifier = Modifier
                                    .size(28.dp)
                                    .testTag("total_count_dec")
                            ) {
                                Icon(
                                    Icons.Default.Remove,
                                    contentDescription = "Decrease count",
                                    tint = if (totalCount > 1) TextPrimary else TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = "$totalCount",
                                color = BluePrimary,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            IconButton(
                                onClick = { totalCount++ },
                                modifier = Modifier
                                    .size(28.dp)
                                    .testTag("total_count_inc")
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Increase count",
                                    tint = TextPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Max Level Stepper Card
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SlateSecondaryContainer)
                            .border(1.dp, LightBorder, RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "MAX LEVEL",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            IconButton(
                                onClick = { if (maxLevel > 1) maxLevel-- },
                                enabled = maxLevel > 1,
                                modifier = Modifier
                                    .size(28.dp)
                                    .testTag("max_level_dec")
                            ) {
                                Icon(
                                    Icons.Default.Remove,
                                    contentDescription = "Decrease max level",
                                    tint = if (maxLevel > 1) TextPrimary else TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = "$maxLevel",
                                color = TextPrimary,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            IconButton(
                                onClick = { maxLevel++ },
                                modifier = Modifier
                                    .size(28.dp)
                                    .testTag("max_level_inc")
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Increase max level",
                                    tint = TextPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Section 2: Allocation Status Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when {
                                isBalanced -> EmeraldContainer
                                allocatedSum < totalCount -> AmberContainer
                                else -> RedDangerContainer
                            }
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when {
                                isBalanced -> Icons.Default.Check
                                else -> Icons.Default.Warning
                            },
                            contentDescription = null,
                            tint = when {
                                isBalanced -> EmeraldSuccess
                                allocatedSum < totalCount -> AmberPending
                                else -> RedDanger
                            },
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when {
                                isBalanced -> "Allocated: $allocatedSum / $totalCount (Balanced)"
                                allocatedSum < totalCount -> "Allocated: $allocatedSum / $totalCount (${totalCount - allocatedSum} unassigned)"
                                else -> "Allocated: $allocatedSum / $totalCount (${allocatedSum - totalCount} over limit)"
                            },
                            color = when {
                                isBalanced -> EmeraldSuccess
                                allocatedSum < totalCount -> AmberPending
                                else -> RedDanger
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (!isBalanced) {
                        Text(
                            text = "Auto-Fit",
                            color = BluePrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier
                                .clickable {
                                    totalCount = maxOf(1, allocatedSum)
                                }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = LightBorder)
                Spacer(modifier = Modifier.height(10.dp))

                // Section 3: Level Breakdown Header + Add Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Level Breakdown",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    OutlinedButton(
                        onClick = {
                            // Find next available level not in list, or default to level 1
                            val existingLevels = distributionRows.map { it.level }.toSet()
                            val nextLv = (1..maxLevel).firstOrNull { it !in existingLevels } ?: 1
                            distributionRows.add(LevelCount(level = nextLv, count = 1))
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BluePrimary),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("add_level_row_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Level", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Section 4: Scrollable list of Level Rows
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 220.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(distributionRows) { index, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(LightSurface)
                                .border(1.dp, LightBorder, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Level Picker Controls
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Level",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(SlateSecondaryContainer)
                                        .padding(horizontal = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = {
                                            if (item.level > 1) {
                                                distributionRows[index] = item.copy(level = item.level - 1)
                                            }
                                        },
                                        enabled = item.level > 1,
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(12.dp))
                                    }
                                    Text(
                                        text = "${item.level}",
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )
                                    IconButton(
                                        onClick = {
                                            if (item.level < maxLevel) {
                                                distributionRows[index] = item.copy(level = item.level + 1)
                                            }
                                        },
                                        enabled = item.level < maxLevel,
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                                    }
                                }
                            }

                            // Instance Count Stepper Controls
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Qty",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(SlateSecondaryContainer)
                                        .padding(horizontal = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = {
                                            if (item.count > 1) {
                                                distributionRows[index] = item.copy(count = item.count - 1)
                                            }
                                        },
                                        enabled = item.count > 1,
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(12.dp))
                                    }
                                    Text(
                                        text = "${item.count}",
                                        color = BluePrimary,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )
                                    IconButton(
                                        onClick = {
                                            distributionRows[index] = item.copy(count = item.count + 1)
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                // Remove Row Icon
                                IconButton(
                                    onClick = {
                                        distributionRows.removeAt(index)
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Remove row",
                                        tint = RedDanger,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalDist = distributionRows.filter { it.count > 0 }
                    val finalTotal = if (isBalanced) totalCount else finalDist.sumOf { it.count }
                    onSave(maxOf(1, finalTotal), maxLevel, finalDist)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = BluePrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("save_distribution_button")
            ) {
                Text("Save Breakdown", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("cancel_distribution_button")
            ) {
                Text("Cancel")
            }
        },
        containerColor = LightSurface
    )
}
