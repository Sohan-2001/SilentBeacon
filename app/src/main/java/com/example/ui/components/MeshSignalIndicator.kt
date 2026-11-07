package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.DashboardUiState
import com.example.ui.theme.ActionBlue
import com.example.ui.theme.ActionBlueLight
import com.example.ui.theme.CalmGreen
import com.example.ui.theme.CalmGreenLight
import com.example.ui.theme.DayBorder
import com.example.ui.theme.DaySurface
import com.example.ui.theme.DaySurfaceVariant
import com.example.ui.theme.DayTextPrimary
import com.example.ui.theme.DayTextSecondary
import com.example.ui.theme.DayTextTertiary
import com.example.ui.theme.SafetyAmber
import com.example.ui.theme.SafetyAmberLight
import com.example.ui.theme.White

data class MeshSignalInfo(
    val activeBars: Int,
    val title: String,
    val detail: String,
    val color: Color,
    val bgColor: Color,
    val isScanning: Boolean
)

fun calculateMeshSignalInfo(nodeCount: Int, isMeshActive: Boolean): MeshSignalInfo {
    if (!isMeshActive) {
        return MeshSignalInfo(
            activeBars = 0,
            title = "Radios Disabled",
            detail = "Turn on mesh switch to enable peer-to-peer radio discovery.",
            color = DayTextTertiary,
            bgColor = DaySurfaceVariant,
            isScanning = false
        )
    }

    return when (nodeCount) {
        0 -> MeshSignalInfo(
            activeBars = 0,
            title = "Scanning Vicinity",
            detail = "Radios are scanning 30m–100m for other phones running SilentBeacon.",
            color = SafetyAmber,
            bgColor = SafetyAmberLight,
            isScanning = true
        )
        1 -> MeshSignalInfo(
            activeBars = 1,
            title = "Direct Link (1 Node)",
            detail = "Direct connection with 1 neighbor. Messages hop through this node.",
            color = SafetyAmber,
            bgColor = SafetyAmberLight,
            isScanning = false
        )
        2 -> MeshSignalInfo(
            activeBars = 2,
            title = "Moderate Mesh (2 Nodes)",
            detail = "Cluster active with 2 nodes. Redundant gossip message delivery.",
            color = CalmGreen,
            bgColor = CalmGreenLight,
            isScanning = false
        )
        3 -> MeshSignalInfo(
            activeBars = 3,
            title = "Strong Mesh (3 Nodes)",
            detail = "Multi-peer disaster mesh established. High relay resilience.",
            color = CalmGreen,
            bgColor = CalmGreenLight,
            isScanning = false
        )
        else -> MeshSignalInfo(
            activeBars = 4,
            title = "Dense Mesh ($nodeCount Nodes)",
            detail = "High-density mesh in range. Instant multi-hop broadcast propagation.",
            color = CalmGreen,
            bgColor = CalmGreenLight,
            isScanning = false
        )
    }
}

/**
 * Visual cellular/mesh stepped signal strength bars (4 bars with increasing heights).
 */
@Composable
fun SignalBarsWidget(
    connectedCount: Int,
    isMeshActive: Boolean,
    modifier: Modifier = Modifier,
    barWidth: Dp = 4.dp,
    maxHeight: Dp = 16.dp,
    barSpacing: Dp = 2.dp
) {
    val signalInfo = calculateMeshSignalInfo(connectedCount, isMeshActive)

    // Animated scanning wave if 0 nodes are connected while mesh is active
    val transition = rememberInfiniteTransition(label = "signal_scan")
    val waveStep by transition.animateFloat(
        initialValue = 0f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_step"
    )

    val heights = listOf(0.35f, 0.55f, 0.75f, 1.0f)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(barSpacing),
        verticalAlignment = Alignment.Bottom
    ) {
        for (i in 0 until 4) {
            val barHeight = maxHeight * heights[i]
            val isBarActive = when {
                !isMeshActive -> false
                signalInfo.isScanning -> (waveStep.toInt() == i)
                else -> i < signalInfo.activeBars
            }

            val barColor = when {
                !isMeshActive -> DayBorder
                signalInfo.isScanning -> if (isBarActive) ActionBlue else DayBorder
                isBarActive -> signalInfo.color
                else -> DayBorder
            }

            Box(
                modifier = Modifier
                    .width(barWidth)
                    .height(barHeight)
                    .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                    .background(barColor)
            )
        }
    }
}

/**
 * Compact Signal Strength Pill for the TopAppBar.
 */
@Composable
fun TopBarSignalStrengthPill(
    connectedCount: Int,
    isMeshActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val signalInfo = calculateMeshSignalInfo(connectedCount, isMeshActive)

    Surface(
        modifier = modifier
            .testTag("topbar_mesh_signal_indicator")
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = if (isMeshActive && connectedCount > 0) CalmGreenLight else DaySurfaceVariant,
        border = BorderStroke(
            1.dp,
            if (isMeshActive && connectedCount > 0) CalmGreen.copy(alpha = 0.4f) else DayBorder
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SignalBarsWidget(
                connectedCount = connectedCount,
                isMeshActive = isMeshActive,
                barWidth = 3.dp,
                maxHeight = 12.dp,
                barSpacing = 1.5.dp
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = when {
                    !isMeshActive -> "Offline"
                    connectedCount == 0 -> "Scanning"
                    connectedCount == 1 -> "1 Node"
                    else -> "$connectedCount Nodes"
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isMeshActive && connectedCount > 0) CalmGreen else DayTextPrimary,
                maxLines = 1
            )
        }
    }
}

/**
 * Dedicated, highly prominent Visual Signal Strength and Vicinity Node Density Card for the feed.
 */
@Composable
fun MeshSignalStrengthCard(
    state: DashboardUiState,
    onOpenNetworkDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    val signalInfo = calculateMeshSignalInfo(state.connectedPeers.size, state.isMeshActive)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DaySurface),
        border = BorderStroke(1.dp, DayBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("mesh_signal_strength_card")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row: Signal Bars & Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(signalInfo.bgColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        SignalBarsWidget(
                            connectedCount = state.connectedPeers.size,
                            isMeshActive = state.isMeshActive,
                            barWidth = 3.5.dp,
                            maxHeight = 15.dp,
                            barSpacing = 2.dp
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = "MESH VICINITY SIGNAL",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = DayTextSecondary,
                            letterSpacing = 0.5.sp,
                            maxLines = 1
                        )
                        Text(
                            text = if (state.connectedPeers.isEmpty()) "0 Nodes in Vicinity" else "${state.connectedPeers.size} ${if (state.connectedPeers.size == 1) "Node" else "Nodes"} Connected",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = DayTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Signal Rating Tag
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = signalInfo.bgColor,
                    border = BorderStroke(1.dp, signalInfo.color.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = signalInfo.title.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = signalInfo.color,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.5.dp),
                        maxLines = 1
                    )
                }
            }

            // Description of current signal and connectivity reach
            Text(
                text = signalInfo.detail,
                fontSize = 12.sp,
                color = DayTextSecondary,
                lineHeight = 16.sp
            )

            // Visual Node Constellation Radar Graphic
            NodeConstellationRadar(
                connectedPeers = state.connectedPeers,
                localNodeId = state.localNodeId,
                isMeshActive = state.isMeshActive,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            )

            // Footer info and action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = DayTextTertiary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Range ~30m-100m • BLE + Wi-Fi P2P",
                        fontSize = 11.sp,
                        color = DayTextTertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onOpenNetworkDetails,
                    colors = ButtonDefaults.buttonColors(containerColor = ActionBlueLight),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("view_mesh_radar_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Sensors,
                        contentDescription = null,
                        tint = ActionBlue,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "View Radar",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ActionBlue
                    )
                }
            }
        }
    }
}

/**
 * Clean Canvas-based Node Constellation Radar visualizer.
 * Renders your local device in the center with concentric radio rings,
 * and dynamically projects connected nodes in the immediate vicinity.
 */
@Composable
fun NodeConstellationRadar(
    connectedPeers: Set<String>,
    localNodeId: String,
    isMeshActive: Boolean,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "radar_sweep")
    val pulseRadius by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_radius"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(DaySurfaceVariant)
            .border(BorderStroke(1.dp, DayBorder), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        // Draw concentric radio rings and links
        Canvas(modifier = Modifier.fillMaxWidth().height(130.dp)) {
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val maxR = minOf(size.width, size.height) * 0.44f

            // 1. Concentric distance rings (e.g. 25m, 50m, 100m)
            drawCircle(
                color = DayBorder,
                radius = maxR * 0.35f,
                center = Offset(centerX, centerY),
                style = Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                color = DayBorder,
                radius = maxR * 0.7f,
                center = Offset(centerX, centerY),
                style = Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                color = DayBorder,
                radius = maxR,
                center = Offset(centerX, centerY),
                style = Stroke(width = 1.dp.toPx())
            )

            // 2. Animated scanning pulse ring if mesh is active
            if (isMeshActive) {
                drawCircle(
                    color = ActionBlue.copy(alpha = (1f - pulseRadius) * 0.35f),
                    radius = maxR * pulseRadius,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            // 3. Link lines to connected nodes
            val peerList = connectedPeers.toList()
            val totalPeers = peerList.size
            if (totalPeers > 0) {
                val angleStep = (2 * Math.PI) / totalPeers
                for (i in 0 until totalPeers) {
                    val angle = i * angleStep - (Math.PI / 2)
                    val nodeR = maxR * 0.75f
                    val targetX = centerX + (nodeR * Math.cos(angle)).toFloat()
                    val targetY = centerY + (nodeR * Math.sin(angle)).toFloat()

                    // Draw connection link line
                    drawLine(
                        color = CalmGreen.copy(alpha = 0.5f),
                        start = Offset(centerX, centerY),
                        end = Offset(targetX, targetY),
                        strokeWidth = 2.dp.toPx()
                    )

                    // Draw peer node point
                    drawCircle(
                        color = CalmGreen,
                        radius = 6.dp.toPx(),
                        center = Offset(targetX, targetY)
                    )
                    drawCircle(
                        color = White,
                        radius = 2.5.dp.toPx(),
                        center = Offset(targetX, targetY)
                    )
                }
            }
        }

        // Center Local Node Marker
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(if (isMeshActive) ActionBlue else DayTextTertiary, CircleShape)
                    .border(BorderStroke(2.dp, White), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Podcasts,
                    contentDescription = "You",
                    tint = White,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "You (${localNodeId.take(4)})",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = DayTextPrimary
            )
        }

        // Overlay text if no peers in vicinity
        if (connectedPeers.isEmpty() && isMeshActive) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
                    .background(DaySurface.copy(alpha = 0.9f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "No neighbors detected within 100m yet",
                    fontSize = 10.sp,
                    color = DayTextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
