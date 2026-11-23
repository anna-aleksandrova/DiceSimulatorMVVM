package com.example.broadcastflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

class MainActivity : ComponentActivity() {

    private val viewModel: BroadcastViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    BroadcastLogScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun BroadcastLogScreen(
    viewModel: BroadcastViewModel,
    modifier: Modifier = Modifier
) {
    val events by viewModel.events.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Журнал подій",
                style = MaterialTheme.typography.titleLarge
            )
            if (events.isNotEmpty()) {
                Button(onClick = { viewModel.clearAll() }) {
                    Text(text = "Очистити")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = events,
                key = { event -> event.id }
            ) { event ->
                ExpandableEventCard(
                    event = event,
                    onDelete = { viewModel.deleteEvent(event) },
                    modifier = Modifier.animateItem()
                )
            }
        }
    }
}

@Composable
fun ExpandableEventCard(
    event: BroadcastEvent,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    Card(
        onClick = { isExpanded = !isExpanded },
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = event.type,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = event.timestamp,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null
                    )
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Час: ${event.timestamp}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = event.details,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
