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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.BuildingEntity
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberPending
import com.example.ui.theme.BlueOnPrimaryContainer
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.BluePrimaryContainer
import com.example.ui.theme.LightBorder
import com.example.ui.theme.LightSurface
import com.example.ui.theme.SlateSecondaryContainer
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ScheduleUpgradeDialog(
    building: BuildingEntity,
    onSchedule: (fromLevel: Int, toLevel: Int, durationMillis: Long) -> Unit,
    onDismiss: () -> Unit
) {
    val distributions = remember(building) { building.parseLevelDistributions() }
    val upgradableLevels = remember(distributions, building.maxLevel) {
        distributions.filter { it.level < building.maxLevel && it.count > 0 }
    }

    var selectedFromLevel by remember {
        mutableIntStateOf(upgradableLevels.firstOrNull()?.level ?: building.currentLevel)
    }

    val selectedToLevel = selectedFromLevel + 1

    var days by remember { mutableIntStateOf(1) }
    var hours by remember { mutableIntStateOf(0) }
    var minutes by remember { mutableIntStateOf(0) }

    val totalDurationMillis by remember {
        derivedStateOf {
            ((days * 24L * 60L + hours * 60L + minutes) * 60L * 1000L).coerceAtLeast(0L)
        }
    }

    val finishDateString by remember(totalDurationMillis) {
        derivedStateOf {
            if (totalDurationMillis > 0) {
                val target = System.currentTimeMillis() + totalDurationMillis
                val sdf = SimpleDateFormat("EEE, MMM d, h:mm a", Locale.getDefault())
                sdf.format(Date(target))
            } else {
                "Instant"
            }
        }
    }

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
                        imageVector = Icons.Default.HourglassTop,
                        contentDescription = null,
                        tint = BluePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Schedule Upgrade Timer",
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
                    .testTag("schedule_upgrade_dialog")
            ) {
                if (upgradableLevels.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(AmberContainer)
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "All instances of ${building.name} are already at Max Level (${building.maxLevel})! To upgrade further, raise the Max Level in Town Hall or Manage Counts.",
                            color = AmberPending,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    // Level selector: Choose which instance to upgrade
                    Text(
                        text = "SELECT LEVEL TO UPGRADE",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(upgradableLevels) { item ->
                            val isSelected = item.level == selectedFromLevel
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) BluePrimaryContainer else LightSurface)
                                    .border(
                                        1.dp,
                                        if (isSelected) BluePrimary else LightBorder,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedFromLevel = item.level }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("select_level_${item.level}"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Lv ${item.level}",
                                    color = if (isSelected) BluePrimary else TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    tint = if (isSelected) BluePrimary else TextMuted,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Lv ${item.level + 1}",
                                    color = if (isSelected) BluePrimary else TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "(${item.count} avail)",
                                    color = if (isSelected) BluePrimary else TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = LightBorder)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Duration Picker: Days, Hours, Minutes
                    Text(
                        text = "UPGRADE FINISH DURATION",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Days Stepper
                        DurationColumn(
                            label = "DAYS",
                            value = days,
                            onValueChange = { days = it },
                            onDec = { if (days > 0) days-- },
                            onInc = { if (days < 30) days++ },
                            minValue = 0,
                            maxValue = 30,
                            testTag = "days"
                        )

                        // Hours Stepper
                        DurationColumn(
                            label = "HOURS",
                            value = hours,
                            onValueChange = { hours = it },
                            onDec = { if (hours > 0) hours-- },
                            onInc = { if (hours < 23) hours++ },
                            minValue = 0,
                            maxValue = 23,
                            testTag = "hours"
                        )

                        // Minutes Stepper
                        DurationColumn(
                            label = "MINUTES",
                            value = minutes,
                            onValueChange = { minutes = it },
                            onDec = { if (minutes > 0) minutes-- },
                            onInc = { if (minutes < 59) minutes++ },
                            minValue = 0,
                            maxValue = 59,
                            testTag = "minutes"
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Preset Chips
                    Text(
                        text = "Quick Presets",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val presets = listOf(
                            "15m" to (0 to (0 to 15)),
                            "1h" to (0 to (1 to 0)),
                            "4h" to (0 to (4 to 0)),
                            "12h" to (0 to (12 to 0)),
                            "1d" to (1 to (0 to 0)),
                            "3d" to (3 to (0 to 0)),
                            "7d" to (7 to (0 to 0)),
                            "14d" to (14 to (0 to 0))
                        )
                        items(presets) { (label, duration) ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SlateSecondaryContainer)
                                    .border(1.dp, LightBorder, RoundedCornerShape(6.dp))
                                    .clickable {
                                        days = duration.first
                                        hours = duration.second.first
                                        minutes = duration.second.second
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .testTag("preset_$label")
                            ) {
                                Text(
                                    text = label,
                                    color = BluePrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Completion Summary Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(BluePrimaryContainer)
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Estimated Completion",
                                    color = BlueOnPrimaryContainer,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${days}d ${hours}h ${minutes}m",
                                    color = BluePrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = finishDateString,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Notification Alert explanation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = BluePrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "You will be alerted: \"Building might be upgraded please check\" upon timer completion.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (upgradableLevels.isNotEmpty()) {
                Button(
                    onClick = {
                        val duration = if (totalDurationMillis <= 0) 60_000L else totalDurationMillis
                        onSchedule(selectedFromLevel, selectedToLevel, duration)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BluePrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("start_upgrade_button")
                ) {
                    Text("Start Upgrade Timer", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("cancel_upgrade_button")
            ) {
                Text("Cancel")
            }
        },
        containerColor = LightSurface
    )
}

@Composable
private fun DurationColumn(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    onDec: () -> Unit,
    onInc: () -> Unit,
    minValue: Int = 0,
    maxValue: Int = 59,
    testTag: String
) {
    var textValue by remember(value) { mutableStateOf(value.toString()) }
    var isFocused by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(SlateSecondaryContainer)
            .border(
                1.dp,
                if (isFocused) BluePrimary else LightBorder,
                RoundedCornerShape(8.dp)
            )
            .padding(vertical = 6.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(
                onClick = onDec,
                enabled = value > minValue,
                modifier = Modifier
                    .size(24.dp)
                    .testTag("dec_$testTag")
            ) {
                Icon(
                    Icons.Default.Remove,
                    contentDescription = "Decrease $label",
                    tint = if (value > minValue) TextPrimary else TextMuted,
                    modifier = Modifier.size(12.dp)
                )
            }

            Box(
                modifier = Modifier
                    .width(32.dp)
                    .padding(horizontal = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                BasicTextField(
                    value = textValue,
                    onValueChange = { input: String ->
                        val digits = input.filter { it.isDigit() }.take(2)
                        textValue = digits
                        val parsed = digits.toIntOrNull()
                        if (parsed != null) {
                            onValueChange(parsed.coerceIn(minValue, maxValue))
                        }
                    },
                    textStyle = TextStyle(
                        color = BluePrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(BluePrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            isFocused = focusState.isFocused
                            if (!focusState.isFocused) {
                                val parsed = textValue.toIntOrNull()
                                if (parsed == null) {
                                    textValue = value.toString()
                                } else {
                                    val clamped = parsed.coerceIn(minValue, maxValue)
                                    textValue = clamped.toString()
                                    onValueChange(clamped)
                                }
                            }
                        }
                        .testTag("input_$testTag")
                )
            }

            IconButton(
                onClick = onInc,
                enabled = value < maxValue,
                modifier = Modifier
                    .size(24.dp)
                    .testTag("inc_$testTag")
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Increase $label",
                    tint = if (value < maxValue) TextPrimary else TextMuted,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}
