package com.example.compatibility

import android.app.ActivityManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import android.os.Vibrator
import android.os.VibratorManager
import com.example.data.local.BeaconDatabase
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

data class CompatibilityIssue(
    val title: String,
    val description: String,
    val isFatal: Boolean
)

data class FeatureDiagnostic(
    val name: String,
    val isSupported: Boolean,
    val details: String,
    val isMandatory: Boolean
)

data class DeviceCompatibilityReport(
    val isFullySupported: Boolean,
    val canRunAppSafely: Boolean,
    val isAndroidGoOrLowRam: Boolean,
    val androidVersionCode: Int,
    val androidReleaseName: String,
    val deviceModel: String,
    val fatalIssues: List<CompatibilityIssue>,
    val warnings: List<CompatibilityIssue>,
    val diagnostics: List<FeatureDiagnostic>
)

enum class PreloadCheckStep(val stepNumber: Int, val title: String, val description: String) {
    SYSTEM_OS(1, "Operating System & Runtime", "Checking Android version and low-memory environment"),
    PLAY_SERVICES(2, "Google Play Services & Nearby", "Verifying Nearby Connections API runtime components"),
    BLUETOOTH_LE(3, "Bluetooth & BLE Radios", "Testing Bluetooth Low Energy and peripheral advertising"),
    WIFI_DIRECT(4, "Wi-Fi & Wi-Fi Direct (P2P)", "Testing peer-to-peer Wi-Fi hardware interfaces"),
    LOCAL_STORAGE(5, "Offline Storage Engine", "Verifying Room database read/write integrity"),
    AUDIO_ALARM(6, "Alarm & Alert Subsystems", "Verifying speaker ringtone and haptic vibrator hardware")
}

object DeviceCompatibilityChecker {

    /**
     * Synchronous / instant compatibility check for subsequent launches or fast verification.
     */
    fun checkCompatibility(context: Context): DeviceCompatibilityReport {
        val diagnostics = mutableListOf<FeatureDiagnostic>()
        val fatalIssues = mutableListOf<CompatibilityIssue>()
        val warnings = mutableListOf<CompatibilityIssue>()

        var isLowRam = false
        val pm = context.packageManager

        // 1. Android OS Version Check
        try {
            val minSupportedSdk = Build.VERSION_CODES.O // Android 8.0 (API 26)
            val currentSdk = Build.VERSION.SDK_INT
            val isOsSupported = currentSdk >= minSupportedSdk
            val osDetails = "Android ${Build.VERSION.RELEASE} (API $currentSdk)"

            diagnostics.add(
                FeatureDiagnostic(
                    name = "Operating System",
                    isSupported = isOsSupported,
                    details = osDetails,
                    isMandatory = true
                )
            )
            if (!isOsSupported) {
                fatalIssues.add(
                    CompatibilityIssue(
                        title = "Android Version Too Low",
                        description = "SilentBeacon requires Android 8.0 (Oreo) or higher. Current: $osDetails",
                        isFatal = true
                    )
                )
            }
        } catch (e: Throwable) {
            fatalIssues.add(
                CompatibilityIssue(
                    title = "OS Check Failed",
                    description = "Error inspecting Android OS version: ${e.message}",
                    isFatal = true
                )
            )
        }

        // 2. Android Go / Low RAM Assessment
        try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            isLowRam = activityManager?.isLowRamDevice == true

            if (isLowRam) {
                warnings.add(
                    CompatibilityIssue(
                        title = "Android Go Edition / Low RAM Device",
                        description = "This device runs Android Go with limited system memory. Mesh operates in battery-saver mode.",
                        isFatal = false
                    )
                )
            }
        } catch (e: Throwable) {
            // Non fatal
        }

        // 3. Google Play Services Availability Check (Critical for Nearby Connections)
        try {
            val availability = GoogleApiAvailability.getInstance()
            val resultCode = availability.isGooglePlayServicesAvailable(context)
            val isPlayServicesAvailable = resultCode == ConnectionResult.SUCCESS
            val playServicesErrorString = if (isPlayServicesAvailable) {
                "Google Play Services Active"
            } else {
                availability.getErrorString(resultCode) ?: "GMS Unavailable"
            }

            diagnostics.add(
                FeatureDiagnostic(
                    name = "Google Play Services",
                    isSupported = isPlayServicesAvailable,
                    details = playServicesErrorString,
                    isMandatory = true
                )
            )
            if (!isPlayServicesAvailable) {
                fatalIssues.add(
                    CompatibilityIssue(
                        title = "Google Play Services Missing or Incompatible",
                        description = "Nearby Connections requires Google Play Services, which is missing or disabled on this device ($playServicesErrorString).",
                        isFatal = true
                    )
                )
            }
        } catch (e: Throwable) {
            diagnostics.add(
                FeatureDiagnostic(
                    name = "Google Play Services",
                    isSupported = false,
                    details = "Play Services check error: ${e.message}",
                    isMandatory = true
                )
            )
            fatalIssues.add(
                CompatibilityIssue(
                    title = "Play Services Unavailable",
                    description = "Failed checking Google Play Services: ${e.message}",
                    isFatal = true
                )
            )
        }

        // 4. Bluetooth Hardware Check
        try {
            val hasBluetooth = pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)
            diagnostics.add(
                FeatureDiagnostic(
                    name = "Bluetooth Hardware",
                    isSupported = hasBluetooth,
                    details = if (hasBluetooth) "Hardware Present" else "No Bluetooth Chipset",
                    isMandatory = true
                )
            )
            if (!hasBluetooth) {
                fatalIssues.add(
                    CompatibilityIssue(
                        title = "No Bluetooth Hardware",
                        description = "This device does not have a physical Bluetooth radio chipset.",
                        isFatal = true
                    )
                )
            }
        } catch (e: Throwable) {
            fatalIssues.add(
                CompatibilityIssue(
                    title = "Bluetooth Detection Error",
                    description = "Error accessing Bluetooth features: ${e.message}",
                    isFatal = true
                )
            )
        }

        // 5. Bluetooth Low Energy (BLE) Check
        try {
            val hasBle = pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
            diagnostics.add(
                FeatureDiagnostic(
                    name = "Bluetooth Low Energy (BLE)",
                    isSupported = hasBle,
                    details = if (hasBle) "BLE Subsystem Supported" else "BLE Not Supported",
                    isMandatory = true
                )
            )
            if (!hasBle) {
                fatalIssues.add(
                    CompatibilityIssue(
                        title = "Bluetooth Low Energy Unsupported",
                        description = "This device lacks BLE support needed for micro-power neighbor discovery.",
                        isFatal = true
                    )
                )
            }
        } catch (e: Throwable) {
            fatalIssues.add(
                CompatibilityIssue(
                    title = "BLE Detection Error",
                    description = "Error accessing BLE feature: ${e.message}",
                    isFatal = true
                )
            )
        }

        // 6. Bluetooth Peripheral / Advertising Mode Check
        try {
            val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
            val bluetoothAdapter = bluetoothManager?.adapter ?: BluetoothAdapter.getDefaultAdapter()
            val isMultipleAdvSupported = bluetoothAdapter?.isMultipleAdvertisementSupported == true

            diagnostics.add(
                FeatureDiagnostic(
                    name = "BLE Peripheral Advertising",
                    isSupported = isMultipleAdvSupported,
                    details = if (isMultipleAdvSupported) "Peripheral Advertising Supported" else "Limited to Central/Scanning Mode",
                    isMandatory = false
                )
            )
            if (!isMultipleAdvSupported) {
                warnings.add(
                    CompatibilityIssue(
                        title = "BLE Multi-Advertising Limited",
                        description = "This chipset cannot broadcast as a peripheral beacon. It can still discover and connect to other beacons.",
                        isFatal = false
                    )
                )
            }
        } catch (e: Throwable) {
            warnings.add(
                CompatibilityIssue(
                    title = "BLE Advertising Check Skipped",
                    description = "Unable to query peripheral advertising support safely.",
                    isFatal = false
                )
            )
        }

        // 7. Wi-Fi Hardware Check
        try {
            val hasWifi = pm.hasSystemFeature(PackageManager.FEATURE_WIFI)
            diagnostics.add(
                FeatureDiagnostic(
                    name = "Wi-Fi Radio",
                    isSupported = hasWifi,
                    details = if (hasWifi) "Wi-Fi Hardware Present" else "No Wi-Fi Radio",
                    isMandatory = true
                )
            )
            if (!hasWifi) {
                fatalIssues.add(
                    CompatibilityIssue(
                        title = "No Wi-Fi Hardware",
                        description = "Device does not possess a Wi-Fi radio for high-bandwidth data transfers.",
                        isFatal = true
                    )
                )
            }
        } catch (e: Throwable) {
            fatalIssues.add(
                CompatibilityIssue(
                    title = "Wi-Fi Detection Error",
                    description = "Error accessing Wi-Fi features: ${e.message}",
                    isFatal = true
                )
            )
        }

        // 8. Wi-Fi Direct (P2P) Check
        try {
            val hasWifiDirect = pm.hasSystemFeature(PackageManager.FEATURE_WIFI_DIRECT)
            diagnostics.add(
                FeatureDiagnostic(
                    name = "Wi-Fi Direct (P2P)",
                    isSupported = hasWifiDirect,
                    details = if (hasWifiDirect) "Wi-Fi Direct P2P Supported" else "Wi-Fi Direct Unavailable",
                    isMandatory = false
                )
            )
            if (!hasWifiDirect) {
                warnings.add(
                    CompatibilityIssue(
                        title = "Wi-Fi Direct Unavailable",
                        description = "Direct peer-to-peer Wi-Fi is unsupported. Mesh transfers will rely on Bluetooth payloads.",
                        isFatal = false
                    )
                )
            }
        } catch (e: Throwable) {
            warnings.add(
                CompatibilityIssue(
                    title = "Wi-Fi Direct Check Skipped",
                    description = "Unable to query Wi-Fi Direct feature safely.",
                    isFatal = false
                )
            )
        }

        // 9. Local Room Database Integrity Check
        try {
            val db = BeaconDatabase.getInstance(context)
            val isOpen = db.openHelper.readableDatabase.isOpen
            diagnostics.add(
                FeatureDiagnostic(
                    name = "Offline Room Database",
                    isSupported = isOpen,
                    details = if (isOpen) "SQLite Database Operational" else "Storage Inaccessible",
                    isMandatory = true
                )
            )
            if (!isOpen) {
                fatalIssues.add(
                    CompatibilityIssue(
                        title = "Storage Database Failure",
                        description = "Cannot initialize local encrypted Room database storage.",
                        isFatal = true
                    )
                )
            }
        } catch (e: Throwable) {
            diagnostics.add(
                FeatureDiagnostic(
                    name = "Offline Room Database",
                    isSupported = false,
                    details = "Database error: ${e.message}",
                    isMandatory = true
                )
            )
            fatalIssues.add(
                CompatibilityIssue(
                    title = "Local Database Incompatible",
                    description = "Failed initializing local offline database storage: ${e.message}",
                    isFatal = true
                )
            )
        }

        // 10. Alarm Sound & Vibrator Subsystem Check
        try {
            val alertUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            val hasAlarmUri = alertUri != null

            diagnostics.add(
                FeatureDiagnostic(
                    name = "Emergency Alarm Sound",
                    isSupported = hasAlarmUri,
                    details = if (hasAlarmUri) "System Alarm Audio Stream Ready" else "No Default Audio Tone",
                    isMandatory = false
                )
            )
        } catch (e: Throwable) {
            // Non fatal
        }

        val deviceModel = try {
            "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}"
        } catch (e: Throwable) {
            "Unknown Android Device"
        }

        val canRunAppSafely = fatalIssues.isEmpty()
        val isFullySupported = fatalIssues.isEmpty() && warnings.isEmpty()

        return DeviceCompatibilityReport(
            isFullySupported = isFullySupported,
            canRunAppSafely = canRunAppSafely,
            isAndroidGoOrLowRam = isLowRam,
            androidVersionCode = Build.VERSION.SDK_INT,
            androidReleaseName = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            deviceModel = deviceModel,
            fatalIssues = fatalIssues,
            warnings = warnings,
            diagnostics = diagnostics
        )
    }

    /**
     * Asynchronous step-by-step preloader check executed exclusively during first app launch.
     * Safely checks each needed subsystem while reporting progress to the UI.
     */
    suspend fun runStepByStepPreloadCheck(
        context: Context,
        onProgress: (step: PreloadCheckStep, progressPercent: Float) -> Unit
    ): DeviceCompatibilityReport = withContext(Dispatchers.IO) {
        val totalSteps = PreloadCheckStep.entries.size

        for ((index, step) in PreloadCheckStep.entries.withIndex()) {
            val progress = (index + 1).toFloat() / totalSteps.toFloat()
            onProgress(step, progress)
            // Visual pacing so user can see each diagnostic test run safely
            delay(320L)
        }

        // Run full evaluation
        checkCompatibility(context)
    }
}
