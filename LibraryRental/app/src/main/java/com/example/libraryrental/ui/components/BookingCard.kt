package com.example.libraryrental.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.libraryrental.data.BookingStatus
import com.example.libraryrental.data.BookingWithBook
import com.example.libraryrental.logic.RentalPolicy
import com.example.libraryrental.ui.formatDate

@Composable
fun BookingCard(
    item: BookingWithBook,
    onConfirmPickup: () -> Unit,
    onRenew: () -> Unit,
    onReturn: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val booking = item.booking
    val daysRemaining = RentalPolicy.daysRemaining(booking.returnDeadline, System.currentTimeMillis())

    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = item.book.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(text = "by ${item.book.author}", style = MaterialTheme.typography.bodySmall)
            Text(
                text = item.book.category,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Reserved: ${formatDate(booking.bookingDate)}", style = MaterialTheme.typography.bodySmall)
            Text(text = "Due: ${formatDate(booking.returnDeadline)}", style = MaterialTheme.typography.bodySmall)

            when (booking.status) {
                BookingStatus.PENDING -> Text(
                    text = "Awaiting pickup",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
                BookingStatus.ACTIVE -> Text(
                    text = "On loan",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                BookingStatus.RETURNED -> Text(
                    text = "Returned",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            if (booking.status != BookingStatus.RETURNED) {
                Text(
                    text = remainingText(daysRemaining),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (daysRemaining < 0) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (booking.status == BookingStatus.PENDING) {
                        OutlinedButton(onClick = onCancel) { Text("Cancel") }
                        Button(onClick = onConfirmPickup) { Text("Confirm pickup") }
                    } else {
                        OutlinedButton(onClick = onRenew) { Text("Renew") }
                        Button(onClick = onReturn) { Text("Return") }
                    }
                }
            }
        }
    }
}

private fun remainingText(daysRemaining: Int): String {
    return when {
        daysRemaining > 1 -> "$daysRemaining days remaining"
        daysRemaining == 1 -> "1 day remaining"
        daysRemaining == 0 -> "Due today"
        daysRemaining == -1 -> "1 day overdue"
        else -> "${-daysRemaining} days overdue"
    }
}
