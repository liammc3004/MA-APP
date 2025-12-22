package com.example.maapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.maapp.network.Player
import com.example.maapp.network.PlayerQueue

@Composable
fun PlayerScreen(
    players: List<Player>,
    selectedPlayerId: String?,
    queues: List<PlayerQueue>,
    onPlayerSelected: (Player) -> Unit
) {
    val queue = queues.firstOrNull { it.playerId == selectedPlayerId }
    val selectedPlayer = players.firstOrNull { it.playerId == selectedPlayerId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "Players",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(12.dp))
        PlayerCarousel(players = players, selectedPlayerId = selectedPlayerId, onPlayerSelected = onPlayerSelected)
        Spacer(modifier = Modifier.height(24.dp))
        NowPlayingCard(player = selectedPlayer)
        Spacer(modifier = Modifier.height(16.dp))
        QueueList(queue)
    }
}

@Composable
private fun PlayerCarousel(
    players: List<Player>,
    selectedPlayerId: String?,
    onPlayerSelected: (Player) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(players) { player ->
            val isActive = player.playerId == selectedPlayerId
            Card(
                onClick = { onPlayerSelected(player) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(player.name, color = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
                    Text(player.state.name.lowercase().replaceFirstChar { it.titlecase() }, color = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@Composable
private fun NowPlayingCard(player: Player?) {
    Card(modifier = Modifier.fillMaxWidth()) {
        if (player == null) {
            Text(
                text = "No player selected",
                modifier = Modifier.padding(16.dp)
            )
            return
        }
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Now playing", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                val artwork = player.currentItem?.artwork
                Box(
                    modifier = Modifier
                        .height(96.dp)
                        .weight(1f)
                ) {
                    if (artwork != null) {
                        Image(
                            painter = rememberAsyncImagePainter(artwork),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                }
                Column(modifier = Modifier.weight(2f)) {
                    Text(player.currentItem?.title ?: "Nothing playing", fontWeight = FontWeight.Bold)
                    Text(player.currentItem?.artist ?: "")
                    Spacer(modifier = Modifier.height(8.dp))
                    ControlRow(isPlaying = player.state.name.equals("playing", ignoreCase = true))
                }
            }
        }
    }
}

@Composable
private fun ControlRow(isPlaying: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        IconButton(onClick = { /* TODO send play/pause */ }) {
            Icon(imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null)
        }
        LinearProgressIndicator(progress = 0.3f, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun QueueList(queue: PlayerQueue?) {
    Card(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Queue", style = MaterialTheme.typography.titleMedium)
            if (queue == null || queue.items.isEmpty()) {
                Text(text = "No items queued", modifier = Modifier.padding(top = 8.dp))
                return
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                items(queue.items) { item ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { }
                            .padding(vertical = 4.dp)
                    ) {
                        Text(text = item.title, fontWeight = FontWeight.Medium)
                        item.artist?.let { Text(text = it, color = Color.Gray) }
                    }
                }
            }
        }
    }
}
