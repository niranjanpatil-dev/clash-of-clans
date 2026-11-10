# Clash of Clans Building Management App

A modern, high-fidelity Clash of Clans Building Management Android application built with Jetpack Compose, Room persistence, and Google Search Grounding via Gemini for real-time automatic synchronization of maximum building upgrade levels across Home Village and Builder Base.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following product decisions were confirmed during the planning phase:
> - **Village Scope**: Supports both **Home Village** (TH1 to TH18) and **Builder Base** (BH1 to BH10) structures with seamless category toggling.
> - **Initial Data**: Pre-loaded with standard Building Types (Defence, Resource, Army, Traps, Other) and iconic Clash of Clans buildings (Cannons, Archer Towers, Wizard Towers, Infernos, Monolith, Storages, etc.).
> - **Search Grounding Sync**: Automatically prompts a **Diff Preview & Confirmation Dialog** whenever the active Town Hall level changes, alongside an explicit **Manual Sync** button.

- **Confirmed Decision 1**: Room SQLite database with Repository pattern for resilient, offline-first local data storage.
- **Confirmed Decision 2**: Google Search Grounding using `gemini-3.8-flash` with the `googleSearch` tool to retrieve verified building maximum levels and source citation links directly from web data.
- **Confirmed Decision 3**: Strict safety constraint where existing `Current Level` values are never overwritten during sync; only `Maximum Level` is updated after user confirmation.

---

## 1. Overview & Core Concept

- **What It Does**: Enables Clash of Clans players and clan leaders to track, manage, and optimize their village upgrades. Users can record current and maximum building levels, inspect category-wise completion rates, and keep maximum levels up to date with the latest Supercell updates via Google Search Grounding.
- **Target Audience**: Clash of Clans players managing active village progressions across Town Hall 1 through 18 and Builder Hall 1 through 10.
- **Key Value**: Eliminates manual wiki lookups for building level caps after Town Hall upgrades, presenting clear visual upgrade trackers, level diffs, and real-time grounding citations.

---

## 2. User Experience & Visual Design

### Key User Flows
1. **Dashboard & Progression Overview**:
   - The user opens the app to a rich gaming dashboard displaying the active Town Hall badge, overall upgrade progress percentage, completed vs pending counters, and category progress bars.
   - A quick-switch Town Hall banner displays the current village tier and last synchronized status.
2. **Town Hall Level Change & Grounding Sync**:
   - The user upgrades or changes their Town Hall level.
   - The app initiates Google Search Grounding (`gemini-3.8-flash` + `googleSearch`) to query current maximum levels for the new Town Hall.
   - A **Diff Preview Modal** displays:
     - Unchanged buildings
     - Upgraded max levels (e.g., Archer Tower: Max 21 ➔ Max 22)
     - Preserved current user levels
     - Web grounding source URLs & titles
   - User reviews and confirms to apply updates into the database.
3. **Building Master CRUD**:
   - View buildings with search, village filter (Home Village / Builder Base), and category chips (Defence, Resource, Army, Traps).
   - Tap "+ Add Building" to open a modal with type dropdown, name, current level stepper, and max level.
   - Edit, delete with confirmation, and one-tap "+1" / "-1" level adjustments directly on building cards.
4. **Building Type Master CRUD**:
   - Manage categories (Defence, Resource, Army, Traps, Other) with custom icons and color tags.
5. **Town Hall Master CRUD**:
   - Add custom Town Hall or Builder Hall entries, customize names/themes, and set the active village level.

### Visual Identity & Theme
- **Aesthetic Direction**: Clash of Clans-inspired dark gaming aesthetic featuring stone textures, glowing mana/elixir accents, polished gold trims, and high-contrast typography.
- **Color Palette**:
  - `Dark Obsidian Background`: `#0D1117` and `#161B22`
  - `Surface / Card Elevaton`: `#1F2937` with subtle glowing borders
  - `Clan Gold (Primary Accent)`: `#F59E0B` / `#FBBF24` (Maxed progress, badges)
  - `Elixir Violet / Magenta`: `#D946EF` / `#A855F7` (In-progress indicators, army)
  - `Dark Elixir Indigo`: `#6366F1` (Special structures & heroes)
  - `Gem Emerald`: `#10B981` (Completed state, positive diffs)
  - `Town Hall Flame`: `#EF4444` (Town Hall headers, warning diffs)
- **Component Styling & Layout**:
  - Adaptive layout: Side Navigation Rail on tablets and landscape screens; Bottom Navigation Bar on compact portrait mobile devices.
  - Multi-tier progress bars displaying current level vs maximum level with percentage completion.
  - Diff chips showing green indicators `[+1 Level Cap]` on preview items.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Persistence Layer (Room Database)**
  - *Chosen Approach*: Android Room SQLite database with typed DAOs and reactive `Flow` emissions.
  - *Why*: Instant offline access, zero sign-in barrier for village tracking, and reactive UI updates across screens when syncing or editing.
- **Decision 2: Google Search Grounding Execution**
  - *Chosen Approach*: Direct Google AI Studio Gemini API integration (`models/gemini-3.8-flash` with `googleSearch` tool) via Retrofit and OkHttp.
  - *Why*: Delivers verified live web search data from the Clash of Clans Wiki and official patch notes, including grounding metadata (source URLs and search query pills) as requested.
- **Decision 3: Diff-First Synchronization Workflow**
  - *Chosen Approach*: Present an explicit Diff Confirmation Dialog before any maximum level in the database is modified.
  - *Why*: Protects user data from unexpected updates, lets users inspect new level caps before saving, and adheres strictly to the rule: "Never overwrite current levels; ask confirmation before updating".

---

## 4. Technical Architecture & Data Strategy

### System Architecture & Data Flow

```
┌────────────────────────────────────────────────────────────────────────┐
│                          Jetpack Compose UI                            │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌───────────────┐  │
│  │  Dashboard   │ │  Buildings   │ │  Town Halls  │ │Building Types │  │
│  │    Screen    │ │ Master Screen│ │ Master Screen│ │ Master Screen │  │
│  └──────┬───────┘ └──────┬───────┘ └──────┬───────┘ └───────┬───────┘  │
│         │                │                │                 │          │
│         └────────────────┼────────────────┼─────────────────┘          │
│                          ▼                                             │
│                 ClashViewModel (StateFlow)                             │
│                  - villageFilter State                                 │
│                  - activeTownHall State                                │
│                  - syncDiffPreview State                               │
└──────────────────────────┬─────────────────────────────────────────────┘
                           │
       ┌───────────────────┴───────────────────┐
       ▼                                       ▼
┌─────────────────────────┐         ┌───────────────────────────────────┐
│     Room Repository     │         │   Gemini Grounding Service        │
│  ┌───────────────────┐  │         │  (gemini-3.8-flash + googleSearch)│
│  │ BuildingDao       │  │         │  - Retrofit HTTP Client           │
│  │ TownHallDao       │  │         │  - Dynamic Search Query Builder   │
│  │ BuildingTypeDao   │  │         │  - JSON Extraction & URL Parser   │
│  │ SyncHistoryDao    │  │         └─────────────────┬─────────────────┘
│  └───────────────────┘  │                           │
│     SQLite Database     │                           │ Diff Preview
│   (Local Persistence)   │◄──────────────────────────┘ (User Confirmed)
└─────────────────────────┘
```

### Data Entities & Schema

1. **`BuildingTypeEntity`**:
   - `id: Long` (Auto-generated Primary Key)
   - `name: String` ("Defence", "Resource", "Army", "Traps", "Other")
   - `village: String` ("HOME", "BUILDER")
   - `iconKey: String`

2. **`TownHallEntity`**:
   - `id: Long` (Auto-generated Primary Key)
   - `level: Int` (1 to 18 for Home Village; 1 to 10 for Builder Base)
   - `name: String` (e.g., "Town Hall 16 - Nature", "Builder Hall 10")
   - `village: String` ("HOME", "BUILDER")
   - `isActive: Boolean` (Active Town Hall driving max levels)

3. **`BuildingEntity`**:
   - `id: Long` (Auto-generated Primary Key)
   - `typeId: Long` (Foreign Key reference to `BuildingTypeEntity`)
   - `name: String` (e.g., "Cannon", "Archer Tower", "Eagle Artillery")
   - `currentLevel: Int` (User's current in-game level)
   - `maxLevel: Int` (Current maximum allowed level)
   - `village: String` ("HOME", "BUILDER")
   - `notes: String`

4. **`SyncRecordEntity`**:
   - `id: Long` (Auto-generated Primary Key)
   - `townHallLevel: Int`
   - `village: String`
   - `syncedAtTimestamp: Long`
   - `sourcesListJson: String` (URLs and titles from Search Grounding)
   - `updatedBuildingsCount: Int`

### Search Grounding Request & Response Flow
1. **Query Construction**:
   - Prompt sent to `gemini-3.8-flash`:
     `"Find the exact maximum building upgrade levels for each Clash of Clans building at Town Hall level [X]. Return JSON array with keys 'buildingName' and 'maxLevel'."`
   - Tools payload: `[ { "googleSearch": {} } ]`.
2. **Grounding Metadata Parsing**:
   - Extract `groundingMetadata.groundingChunks.web.uri` and `title` to display source citations.
3. **Diff Calculation**:
   - Match fetched building names against existing records in `BuildingEntity`.
   - Calculate changes: `BuildingDiff(building, oldMaxLevel, newMaxLevel)`.
   - Present modal with diff list and source URLs.
4. **Confirmed Application**:
   - Execute bulk update in Room database within a transaction, recording the sync timestamp and source citations.
