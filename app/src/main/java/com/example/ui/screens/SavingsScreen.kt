package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Star
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.FinancialViewModel
import com.example.ui.SavingGoal
import com.example.ui.getTranslation
import com.example.ui.AppLanguage
import java.text.NumberFormat
import java.util.Locale

@Composable
fun SavingsScreen(
    viewModel: FinancialViewModel
) {
    val goals by viewModel.savingsGoals.collectAsState()
    val currentLang by viewModel.currentLanguage.collectAsState()
    val currentCurrency by viewModel.currentCurrency.collectAsState()

    val currencyFormatter = NumberFormat.getCurrencyInstance(currentCurrency.locale)

    // Modal contribution states
    var selectedGoalForContribution by remember { mutableStateOf<SavingGoal?>(null) }
    var inputContributionStr by remember { mutableStateOf("") }

    // Modification dialog states
    var showGoalDialog by remember { mutableStateOf(false) }
    var isEditingGoal by remember { mutableStateOf(false) }
    var editingGoalId by remember { mutableStateOf(0) }
    var inputGoalName by remember { mutableStateOf("") }
    var inputTargetAmountStr by remember { mutableStateOf("") }
    var inputCurrentSavedStr by remember { mutableStateOf("") }
    var selectedGoalColor by remember { mutableStateOf(0xFF00796B) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    inputGoalName = ""
                    inputTargetAmountStr = ""
                    inputCurrentSavedStr = "0"
                    selectedGoalColor = 0xFF00796B
                    isEditingGoal = false
                    showGoalDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_savings_goal_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Savings Goal")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 650.dp)
                    .fillMaxWidth()
            ) {
                // Header Info Title
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                ) {
                Text(
                    text = getTranslation("savings", currentLang),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (currentLang == AppLanguage.INDONESIAN) "Buat rencana, sesuaikan, dan simpan uang untuk masa depan Anda" else "Establish, customize, and fund security nets and high-milestone goals",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(
                    items = goals,
                    key = { it.id }
                ) { goal ->
                    val progressFraction = if (goal.targetAmount > 0.0) (goal.currentSaved / goal.targetAmount).coerceIn(0.0, 1.0) else 0.0
                    val progressPct = (progressFraction * 100).toInt()

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .clickable {
                                editingGoalId = goal.id
                                inputGoalName = goal.name
                                inputTargetAmountStr = goal.targetAmount.toString()
                                inputCurrentSavedStr = goal.currentSaved.toString()
                                selectedGoalColor = goal.colorHex
                                isEditingGoal = true
                                showGoalDialog = true
                            }
                            .testTag("savings_card_${goal.id}"),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            // Row meta
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(Color(goal.colorHex).copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Savings,
                                            contentDescription = "Target icon",
                                            tint = Color(goal.colorHex),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = goal.name,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Goal Target: ${currencyFormatter.format(goal.targetAmount)}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = "$progressPct%",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(goal.colorHex)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Compact Linear Progress Indicator bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(progressFraction.toFloat())
                                        .height(10.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(Color(goal.colorHex))
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Current Amount Metrics bottom row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Saved Amount",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                    Text(
                                        text = currencyFormatter.format(goal.currentSaved),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Button(
                                    onClick = {
                                        selectedGoalForContribution = goal
                                        inputContributionStr = "100" // Default helper starting recommendation
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(goal.colorHex),
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(100.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                                    modifier = Modifier.height(34.dp).testTag("contribute_button_${goal.id}")
                                ) {
                                    Text("Fund Goal", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Inline contrib details modal popup dialog
            selectedGoalForContribution?.let { goal ->
                AlertDialog(
                    onDismissRequest = { selectedGoalForContribution = null },
                    title = { Text("Contribute to ${goal.name}") },
                    text = {
                        Column {
                            Text(
                                text = "How much would you like to contribute from your balances to offline goals?",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = inputContributionStr,
                                onValueChange = { valStr ->
                                    if (valStr.isEmpty() || valStr.all { it.isDigit() || it == '.' }) {
                                        inputContributionStr = valStr
                                    }
                                },
                                label = { Text("Add Savings Amount") },
                                suffix = { Text("USD") },
                                maxLines = 1,
                                modifier = Modifier.fillMaxWidth().testTag("modal_contribution_input")
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Quick chips contribution accelerators row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                listOf("10", "50", "250", "500").forEach { valChip ->
                                    Button(
                                        onClick = { inputContributionStr = valChip },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        ),
                                        modifier = Modifier.height(30.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                    ) {
                                        Text("+$valChip", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val contributeDouble = inputContributionStr.toDoubleOrNull() ?: 0.0
                                if (contributeDouble > 0.0) {
                                    viewModel.contributeToGoal(goal.id, contributeDouble)
                                }
                                selectedGoalForContribution = null
                            },
                            modifier = Modifier.testTag("modal_confirm_contribute")
                        ) {
                            Text("Confirm Deposit", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { selectedGoalForContribution = null }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            if (showGoalDialog) {
                AlertDialog(
                    onDismissRequest = { showGoalDialog = false },
                    title = {
                        Text(
                            text = if (isEditingGoal) {
                                if (currentLang == AppLanguage.INDONESIAN) "Edit Target Tabungan" else "Edit Savings Goal"
                            } else {
                                if (currentLang == AppLanguage.INDONESIAN) "Tambah Target Tabungan" else "Add Savings Goal"
                            }
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = inputGoalName,
                                onValueChange = { inputGoalName = it },
                                label = { Text(if (currentLang == AppLanguage.INDONESIAN) "Nama Rencana" else "Goal Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("savings_goal_name_input")
                            )

                            OutlinedTextField(
                                value = inputTargetAmountStr,
                                onValueChange = {
                                    if (it.isEmpty() || it.toDoubleOrNull() != null || it.all { ch -> ch.isDigit() || ch == '.' }) {
                                        inputTargetAmountStr = it
                                    }
                                },
                                label = { Text(if (currentLang == AppLanguage.INDONESIAN) "Target Jumlah (USD)" else "Target Amount (USD)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("savings_goal_target_input")
                            )

                            OutlinedTextField(
                                value = inputCurrentSavedStr,
                                onValueChange = {
                                    if (it.isEmpty() || it.toDoubleOrNull() != null || it.all { ch -> ch.isDigit() || ch == '.' }) {
                                        inputCurrentSavedStr = it
                                    }
                                },
                                label = { Text(if (currentLang == AppLanguage.INDONESIAN) "Jumlah Tabungan Awal" else "Initial Saved Amount") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("savings_goal_current_input")
                            )

                            Text(
                                text = if (currentLang == AppLanguage.INDONESIAN) "Pilih Warna Aksen" else "Select Color Accent",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp)
                            )

                            // Color palette circular picker
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val colorPalette = listOf(
                                    0xFF00796B, // Teal
                                    0xFF303F9F, // Blue
                                    0xFFE64A19, // Orange
                                    0xFFD32F2F, // Red
                                    0xFF7B1FA2, // Purple
                                    0xFF388E3C, // Green
                                    0xFFFBC02D, // Yellow
                                    0xFFC2185B  // Pink
                                )
                                colorPalette.forEach { hexColor ->
                                    val color = Color(hexColor)
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                            .clickable { selectedGoalColor = hexColor }
                                            .run {
                                                if (selectedGoalColor == hexColor) {
                                                    border(
                                                        width = 3.dp,
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        shape = CircleShape
                                                    )
                                                } else this
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (selectedGoalColor == hexColor) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val target = inputTargetAmountStr.toDoubleOrNull() ?: 0.0
                                val current = inputCurrentSavedStr.toDoubleOrNull() ?: 0.0
                                if (inputGoalName.trim().isNotEmpty() && target > 0.0) {
                                    if (isEditingGoal) {
                                        viewModel.editSavingsGoal(editingGoalId, inputGoalName.trim(), target, current, selectedGoalColor)
                                    } else {
                                        viewModel.addSavingsGoal(inputGoalName.trim(), target, current, selectedGoalColor)
                                    }
                                    showGoalDialog = false
                                }
                            },
                            modifier = Modifier.testTag("savings_dialog_confirm")
                        ) {
                            Text(if (currentLang == AppLanguage.INDONESIAN) "Simpan" else "Save", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isEditingGoal) {
                                TextButton(
                                    onClick = {
                                        viewModel.deleteSavingsGoal(editingGoalId)
                                        showGoalDialog = false
                                    },
                                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFD32F2F)),
                                    modifier = Modifier.testTag("savings_dialog_delete")
                                ) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (currentLang == AppLanguage.INDONESIAN) "Hapus" else "Delete")
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }

                            TextButton(onClick = { showGoalDialog = false }) {
                                Text(if (currentLang == AppLanguage.INDONESIAN) "Batal" else "Cancel")
                            }
                        }
                    }
                )
            }
        }
    }
}
}
