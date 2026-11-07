# SilentBeacon 🚨📡

> **Zero-Bandwidth, Local-First Emergency Coordination Mesh for Android**  
> *Built for the Code for Change Hackathon | Offline P2P Disaster Response*

[![Android](https://img.shields.io/badge/Platform-Android_8.0+_(API_26+)-3DDC84?logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin_2.0-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack_Compose-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Protocol](https://img.shields.io/badge/Networking-BLE_%2B_Wi--Fi_Direct-FF6F00)](#how-it-works-under-the-hood)
[![Storage](https://img.shields.io/badge/Local_Storage-Room_(SQLite)-009688)](#data-layer--conflict-resolution)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)

---

## 📌 Problem & Impact

When natural disasters (earthquakes, cyclones, floods) or major infrastructure failures strike, **cellular towers and internet service providers collapse first**. Victims are stranded without means to signal for urgent medical assistance, clean drinking water, rescue, or shelter. First responders and local neighborhood volunteers cannot discover who needs help or coordinate resources.

**SilentBeacon** turns everyday Android devices into resilient, autonomous peer-to-peer communication nodes. It bypasses cellular infrastructure, internet backbones, and GPS satellites entirely, enabling communities to build an ad-hoc epidemic gossip mesh using raw hardware radio transceivers (Bluetooth Low Energy and Wi-Fi Direct).

---

## ⚡ Core Features

- **100% Offline by Design:** Functions completely in Airplane Mode (with Bluetooth & Wi-Fi radios active). Zero cloud endpoints, zero external server dependencies.
- **Autonomous Epidemic Gossip Mesh:** Uses an opportunistic store-and-forward relay. When two devices running SilentBeacon come within radio proximity (30–100m), they automatically discover each other, handshake, exchange missing emergency tickets, and route them forward.
- **Deterministic Conflict-Free Replication (LWW-CRDT):** Resolves out-of-order ticket status updates (e.g., transitioning an SOS from `OPEN` to `RESOLVED`) across distributed nodes using logical **Lamport Timestamps** paired with unique node identifiers.
- **Zero-Friction Emergency Pairing:** Unattended background discovery powered by Google Nearby Connections (`Strategy.P2P_CLUSTER`). No manual PIN entry, pairing codes, or router setup required.
- **Controlled Packet Lifecycles (TTL):** Built-in hop counter (`maxHops = 7`) and payload deduplication prevent infinite broadcast loops and radio congestion across dense crowds.
- **Hardware GPS Independence:** Leverages manual landmark and indoor location tagging so requests are never blocked if satellite GPS is unavailable or blocked by structural debris.
- **Foreground Mesh Relay Service:** Runs an ongoing low-power radio discovery loop in the background, continuously relaying distress signals while the phone is locked in a pocket or backpack.

---

## 🏗️ Architecture
┌────────────────────────────────────────────────────────┐
│                   Jetpack Compose UI                   │
│   (SOS Dispatch Sheet | Emergency Feed | Peer Radar)   │
└───────────────────────────┬────────────────────────────┘
│
┌───────────────────────────▼────────────────────────────┐
│                    ViewModel Layer                     │
│    (StateFlow, Lamport Clock Tick, UI State Machine)   │
└─────────────┬────────────────────────────┬─────────────┘
│                            │
┌─────────────▼────────────┐ ┌─────────────▼─────────────┐
│   Offline Sync Engine    │ │   P2P Mesh Network Layer  │
│  - Merkle / Clock Vector │ │  - Nearby Connections API │
│  - Gossip Deduplication  │ │  - BLE + Wi-Fi Direct     │
│  - Conflict Resolution   │ │  - Epidemic Routing (TTL) │
└─────────────┬────────────┘ └─────────────┬─────────────┘
│                            │
┌─────────────▼────────────────────────────▼─────────────┐
│                 Room Database (SQLite)                 │
│               Local Storage & Persistence              │
└────────────────────────────────────────────────────────┘


---

## 🔬 How It Works Under the Hood

### 1. Zero-Bandwidth Dual-Radio Tunneling
1. **Discovery (Bluetooth Low Energy):** Every active node continuously advertises and scans for the unique service identifier `com.silentbeacon.mesh.v1`. BLE provides micro-power consumption while constantly seeking neighboring devices.
2. **Autonomous Handshake:** When Device A spots Device B, both negotiate a secure cryptographic handshake and auto-accept the connection via `client.acceptConnection()`.
3. **High-Speed Socket Upgrade (Wi-Fi Direct / SoftAP):** For payload transmission, the radio stack dynamically switches to an ad-hoc Wi-Fi Direct cluster (forming a high-bandwidth local TCP/IP socket between devices) without any internet connection.

### 2. State Synchronization via Vector Clocks
When two nodes connect:
1. Node A sends a `Handshake` packet containing its current `maxClockObserved` and a list of known `ticketId` hashes.
2. Node B compares Node A's vector against its local SQLite database.
3. Node B transmits only missing tickets inside a `DeltaBundle`.
4. Node A applies the delta locally, advances its Lamport clock to `max(localClock, remoteClock) + 1`, increments `hopCount`, and forwards the bundle to any other peers connected in its radio cluster.

---

## 🛠️ Tech Stack

| Layer | Technology |
| :--- | :--- |
| **Language** | Kotlin 2.0 (Coroutines, Flow, Serialization) |
| **UI Framework** | Jetpack Compose + Material 3 |
| **Mesh Networking** | Google Play Services Nearby Connections API (`Strategy.P2P_CLUSTER`) |
| **Physical Radios** | Bluetooth Low Energy (BLE) + Wi-Fi Direct / Local Hotspot |
| **Local Database** | Room (SQLite) with reactive Flow query streams |
| **Concurreny & Sync**| Custom CRDT Layer with Monotonic Lamport Timestamps |

---

## 📂 Project Structure

app/src/main/java/com/silentbeacon/
├── data/
│   ├── local/
│   │   ├── BeaconDatabase.kt           # Room database definition
│   │   └── TicketDao.kt                # SQLite reactive queries & CRDT write rules
│   ├── model/
│   │   └── TicketModels.kt             # Tickets, SyncVectorClock & MeshPayload models
│   └── sync/
│       └── MeshSyncEngine.kt           # Vector clock sync, delta calculation & Lamport ordering
├── network/
│   └── NearbyConnectionsMeshManager.kt # P2P Cluster discovery, connection lifecycle & radio I/O
├── service/
│   └── MeshRelayService.kt             # Foreground background relay service
├── ui/
│   ├── EmergencyDashboardScreen.kt     # Radar bar, emergency feed & SOS creation dialog
│   └── theme/                          # Material 3 colors, typography & shapes
└── MainActivity.kt                     # Permission orchestration & lifecycle binding


---

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug (2024.2.1) or newer
- JDK 17+
- **Two physical Android devices** running Android 8.0 (API 26) or higher *(Nearby Connections radio mesh cannot be fully simulated in an emulator)*

### Permissions
SilentBeacon requires access to low-level radio hardware. In accordance with Android 12+ privacy guidelines, it sets `android:usesPermissionFlags="neverForLocation"` to operate without accessing device GPS:
- `android.permission.BLUETOOTH_SCAN`
- `android.permission.BLUETOOTH_ADVERTISE`
- `android.permission.BLUETOOTH_CONNECT`
- `android.permission.NEARBY_WIFI_DEVICES`
- `android.permission.ACCESS_FINE_LOCATION` (Required for radio discovery on Android 11 and lower)
- `android.permission.FOREGROUND_SERVICE` & `FOREGROUND_SERVICE_CONNECTED_DEVICE`


