# Building Count, Level Distribution & Upgrade Timer Plan

An architectural blueprint to enhance `BuildingEntity` with multi-instance quantity tracking, per-level distribution management, and upgrade completion scheduling with Android system notifications and in-app countdown timers.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The following product decisions were confirmed during the planning phase:
> - **Quantity & Level Distribution**: Each building tracks a total instance count (e.g., 5 Cannons) with a dedicated per-level distribution breakdown (e.g., 2 at Level 10, 3 at Level 11, with a maximum level cap of 15).
> - **Upgrade Time Input**: Players specify upgrade duration using a **Days, Hours, and Minutes** duration picker with quick-select Clash of Clans presets (e.g., 12h, 1d, 3d, 7d, 14d).
> - **Upgrade Alerts**: Upgrades are tracked via **both** an in-app active countdown timer on the Dashboard and an **Android System Notification** (`POST_NOTIFICATIONS`) when the builder finishes.

- **Confirmed Decision 1**: Update `BuildingEntity` to include `totalCount` and a structured `levelDistributionJson` representation, ensuring backward compatibility with existing Room databases.
- **Confirmed Decision 2**: Introduce an `ActiveUpgradeEntity` and `UpgradeNotificationWorker` using Android `WorkManager` for battery-efficient, reliable alarm delivery even if the app process is closed.
- **Confirmed Decision 3**: Upgrade dialog asks which specific level is being upgraded (e.g., 1x Level 10 ➔ Level 11) and automatically transitions that instance to the next level upon completion.

---

## 1. Overview & Core Concept

In Clash of Clans, a village rarely has only one instance of a building. A player typically owns 5–7 Cannons, 6 Archer Towers, and 4 Wizard Towers at varying upgrade levels. 

This enhancement delivers:
1. **Multi-Instance Distribution Form**:
   - Manage total count of each building (e.g., 5 Cannons).
   - Allocate count per level (e.g., 2 at Lv 10, 3 at Lv 11) ensuring the sum matches the total count.
   - Configure or view maximum level cap (e.g., Max Lv 15).
2. **Upgrade Scheduler & Alert System**:
   - When a user plans an upgrade, an upgrade prompt appears:
     - Select instance to upgrade (e.g., Level 10 ➔ Level 11).
     - Enter upgrade finish duration (Days, Hours, Minutes).
     - Calculates target completion datetime (e.g., "Finishes Tomorrow at 5:15 PM").
   - Schedules background system notification:
     - *"🔨 Builder Finished! Your Cannon upgrade to Level 11 is complete. Please check your village!"*
   - Displays real-time live countdown timer card on the Dashboard.

---

## 2. User Experience & Visual Design

### Key User Flows

```
┌────────────────────────┐      ┌───────────────────────────┐      ┌────────────────────────────┐
│   Building Card List   │ ───► │ Count & Distribution Form │ ───► │   Level Allocation Rows    │
│  - Total Count: 5      │      │  - Total Count: [ 5 ]     │      │   - 2x Level 10  [ - ] [ + ]│
│  - Levels: 2x10, 3x11  │      │  - Max Level: [ 15 ]      │      │   - 3x Level 11  [ - ] [ + ]│
└───────────┬────────────┘      └───────────────────────────┘      └────────────────────────────┘
            │
            │ Tap "Upgrade Level"
            ▼
┌────────────────────────┐      ┌───────────────────────────┐      ┌────────────────────────────┐
│ Upgrade Instance Modal │ ───► │  Duration Picker (D/H/M)  │ ───► │ Scheduled Alert & Timer    │
│ - Select: 1x Lv 10➔11  │      │  - [0d] [14h] [30m]       │      │ - Live countdown in app    │
│                        │      │  - Presets: 12h, 1d, 3d   │      │ - System push notification │
└────────────────────────┘      └───────────────────────────┘      └────────────────────────────┘
```

1. **Building Card Display**:
   - Displays total count badge: `Total: 5`
   - Level distribution chips: `2x Lv 10` • `3x Lv 11` (Max Lv 15)
   - Action buttons: "Manage Counts", "Start Upgrade"
2. **Manage Count & Distribution Dialog**:
   - **Total Count**: Stepper / input (1 to 20).
   - **Max Level**: Maximum upgrade level cap.
   - **Distribution Allocation**:
     - Dynamic rows: Level and Count.
     - Auto-validating status pill: *"Allocated 5 / 5 instances"* (Green) or *"3 / 5 allocated (2 unassigned)"* (Orange warning).
     - Quick "Distribute Evenly" helper button.
3. **Start Upgrade Dialog**:
   - Displays available levels with un-maxed instances (e.g., "2 instances at Lv 10").
   - Upgrade target: "Upgrading 1 instance: Level 10 ➔ Level 11".
   - **Duration Inputs**:
     - Three clean stepper/text inputs: **Days**, **Hours**, **Minutes**.
     - Quick preset chips: `12h`, `1d`, `2d`, `3d`, `5d`, `7d`, `12d`, `14d`.
     - Calculated finish timestamp banner: *"Finishes Friday, Nov 14 at 6:45 PM"*.
   - Push notification permission request (`POST_NOTIFICATIONS`) on Android 13+.
4. **Active Upgrades Dashboard Widget**:
   - Displays all active in-progress upgrades.
   - Real-time countdown timer: `13h 24m 12s remaining`.
   - "Finish Now" and "Cancel" buttons.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Storage of Level Distributions**
  - *Chosen Approach*: Store distribution as a type-safe list of `LevelCount(level: Int, count: Int)` serialized as JSON in `BuildingEntity.levelDistributionJson`, with helper getter/setter extensions.
  - *Why*: Avoids multi-table joins for simple count queries, keeps Room database operations atomic and fast, and preserves backward compatibility with existing databases.
- **Decision 2: Notification Execution with WorkManager**
  - *Chosen Approach*: `WorkManager` with `OneTimeWorkRequestBuilder` and `setInitialDelay()`, using `NotificationManagerCompat` and a dedicated `NotificationChannel("clash_upgrades", "Building Upgrades", IMPORTANCE_HIGH)`.
  - *Why*: Guarantees execution across Android Doze mode and device reboots without draining battery.
- **Decision 3: Automatic Level Transfer Upon Completion**
  - *Chosen Approach*: When an upgrade timer expires (or user clicks "Finish Now"), automatically move 1 instance from the old level to the new level in `levelDistributionJson`.
  - *Why*: Eliminates tedious manual adjustments; the player village stays synchronized with actual in-game completions.

---

## 4. Technical Architecture & Data Strategy

### Data Schema Changes

#### 1. `BuildingEntity.kt` Updates
```kotlin
@Entity(tableName = "buildings", ...)
data class BuildingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val typeId: Long,
    val name: String,
    val totalCount: Int = 1,                 // Total buildings owned
    val maxLevel: Int = 1,                   // Max level cap
    val levelDistributionJson: String = "[]",// JSON array of LevelCount
    val currentLevel: Int = 1,               // Average or highest level for sorting
    val village: String = "HOME",
    val notes: String = ""
)

data class LevelCount(
    val level: Int,
    val count: Int
)
```

#### 2. `ActiveUpgradeEntity.kt` (New Table)
```kotlin
@Entity(tableName = "active_upgrades")
data class ActiveUpgradeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val buildingId: Long,
    val buildingName: String,
    val fromLevel: Int,
    val toLevel: Int,
    val startTimestamp: Long,
    val finishTimestamp: Long,
    val workRequestId: String,
    val isCompleted: Boolean = false
)
```

### Components to Implement
1. **Entity & DAO Layer**:
   - Add `ActiveUpgradeEntity` and `ActiveUpgradeDao`.
   - Update `BuildingEntity` with `totalCount` and `levelDistributionJson`.
   - Bump Room database version from `2` to `3` with safe migration.
2. **Repository Layer**:
   - Add methods to schedule and complete upgrades.
   - Reactive flow of active upgrades for the Dashboard.
3. **Background Worker**:
   - `UpgradeNotificationWorker.kt` (extends `CoroutineWorker`):
     - Displays notification with sound, vibration, and title: *"🔨 Builder Finished: {Building Name}"*.
     - Body: *"Your {Building Name} upgrade to Level {ToLevel} is complete! Please check your village."*
     - Updates database record to marked completed and updates the building distribution.
4. **UI Components & Dialogs**:
   - `ManageBuildingCountDialog.kt`: Form for total count, max level, and level allocation.
   - `ScheduleUpgradeDialog.kt`: Duration picker with Days/Hours/Minutes and presets.
   - `ActiveUpgradesWidget.kt`: Live countdown card on the Dashboard.
5. **Permissions**:
   - Declare `POST_NOTIFICATIONS` in `AndroidManifest.xml`.
   - Request runtime permission in Jetpack Compose prior to scheduling notifications.
