package com.example.libraryrental.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.libraryrental.ui.components.BookingCard
import com.example.libraryrental.ui.components.RenewDialog
import com.example.libraryrental.viewmodel.DashboardViewModel

private val tabTitles = listOf("Current", "History")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onGoBack: () -> Unit
) {
    val activeBookings by viewModel.activeBookings.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var renewBookingId by rememberSaveable { mutableStateOf<Long?>(null) }

    LaunchedEffect(viewModel.message) {
        viewModel.message?.let { text ->
            snackbarHostState.showSnackbar(text)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Reservations") },
                navigationIcon = {
                    IconButton(onClick = onGoBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            val shownList = if (selectedTab == 0) activeBookings else history

            if (shownList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(if (selectedTab == 0) "No active reservations" else "No returned books yet")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(shownList, key = { it.booking.bookingId }) { item ->
                        BookingCard(
                            item = item,
                            onConfirmPickup = { viewModel.confirmPickup(item.booking.bookingId) },
                            onRenew = { renewBookingId = item.booking.bookingId },
                            onReturn = { viewModel.returnBook(item.booking.bookingId, item.book.bookId) },
                            onCancel = { viewModel.cancel(item.booking.bookingId, item.book.bookId) }
                        )
                    }
                }
            }
        }
    }

    val renewTarget = activeBookings.firstOrNull { it.booking.bookingId == renewBookingId }
    if (renewTarget != null) {
        RenewDialog(
            bookTitle = renewTarget.book.title,
            currentDeadline = renewTarget.booking.returnDeadline,
            onDismiss = { renewBookingId = null },
            onConfirm = { days ->
                viewModel.renew(renewTarget.booking.bookingId, days)
                renewBookingId = null
            }
        )
    }
}
