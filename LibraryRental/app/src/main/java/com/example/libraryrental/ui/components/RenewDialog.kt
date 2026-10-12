package com.example.libraryrental.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.libraryrental.logic.RentalPolicy
import com.example.libraryrental.ui.formatDate

@Composable
fun RenewDialog(
    bookTitle: String,
    currentDeadline: Long,
    onDismiss: () -> Unit,
    onConfirm: (extraDays: Int) -> Unit
) {
    var selectedDays by rememberSaveable { mutableIntStateOf(RentalPolicy.RENEWAL_DAY_OPTIONS.first()) }
    val start = maxOf(currentDeadline, System.currentTimeMillis())
    val newDeadline = RentalPolicy.deadlineFrom(start, selectedDays)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Renew \"$bookTitle\"") },
        text = {
            Column(modifier = Modifier.selectableGroup()) {
                Text("Extend the due date by:")
                RentalPolicy.RENEWAL_DAY_OPTIONS.forEach { days ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (days == selectedDays),
                                onClick = { selectedDays = days },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = (days == selectedDays), onClick = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("$days days")
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "New due date: ${formatDate(newDeadline)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedDays) }) { Text("Renew") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
