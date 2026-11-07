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
