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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.libraryrental.data.Book
import com.example.libraryrental.logic.InputValidator
import com.example.libraryrental.logic.RentalPolicy
import com.example.libraryrental.ui.formatDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReserveSheet(
    book: Book,
    onDismiss: () -> Unit,
    onReserve: (userName: String, rentalDays: Int) -> Unit
) {
    var userName by rememberSaveable { mutableStateOf("") }
    var selectedDays by rememberSaveable { mutableIntStateOf(RentalPolicy.RENTAL_DAY_OPTIONS.first()) }
    val sheetState = rememberModalBottomSheetState()

    val cleanName = InputValidator.sanitizeName(userName)
    val nameIsValid = InputValidator.isValidName(cleanName)
    val dueDate = RentalPolicy.deadlineFrom(System.currentTimeMillis(), selectedDays)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "Reserve \"${book.title}\"",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "by ${book.author}", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = userName,
                onValueChange = { userName = it.take(InputValidator.MAX_NAME_LENGTH) },
                label = { Text("Your name") },
                singleLine = true,
                isError = userName.isNotEmpty() && !nameIsValid,
                supportingText = {
                    if (userName.isNotEmpty() && !nameIsValid) {
                        Text("Enter at least ${InputValidator.MIN_NAME_LENGTH} characters")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Rental duration", style = MaterialTheme.typography.labelLarge)

            Column(modifier = Modifier.selectableGroup()) {
                RentalPolicy.RENTAL_DAY_OPTIONS.forEach { days ->
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
                        Text(text = "$days days")
                    }
                }
            }

            Text(
                text = "Due back by ${formatDate(dueDate)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onReserve(cleanName, selectedDays) },
                enabled = nameIsValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reserve")
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
