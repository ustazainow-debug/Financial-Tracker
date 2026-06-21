package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalActivity
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.BudgetCategory
import com.example.ui.FinancialViewModel
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.ExpenseRose
import com.example.ui.getTranslation
import com.example.ui.AppLanguage
import java.text.NumberFormat
import java.util.Locale

@Composable
fun BudgetScreen(
    viewModel: FinancialViewModel
) {
    val budgets by viewModel.budgets.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val currentLang by viewModel.currentLanguage.collectAsState()
    val currentCurrency by viewModel.currentCurrency.collectAsState()

    val currencyFormatter = NumberFormat.getCurrencyInstance(currentCurrency.locale)

    // Modification dialog states
    var showBudgetDialog by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var inputCategoryName by remember { mutableStateOf("") }
    var inputLimitStr by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(0xFF4CAF50) }

    val colorPalette = listOf(
        0xFF4CAF50, // Green
        0xFF2196F3, // Blue
        0xFFFF9800, // Orange
        0xFF9C27B0, // Purple
        0xFFF44336, // Red
        0xFF00BCD4, // Teal
        0xFFE91E63, // Pink
        0xFF9E9E9E  // Grey
    )

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    inputCategoryName = ""
                    inputLimitStr = ""
                    selectedColor = 0xFF4CAF50
                    isEditing = false
                    showBudgetDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_budget_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Budget Category Limit")
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
                // Screen Header Header Title
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                ) {
                    Text(
                        text = getTranslation("monthly_budget", currentLang),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = if (currentLang == AppLanguage.INDONESIAN) "Pantau & sesuaikan batas pengeluaran Anda secara dinamis" else "Track & customize your monthly spending limits dynamically",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                items(
                    items = budgets,
                    key = { it.category }
                ) { budget ->
                    // Calculate real spent amount under this specific category
                    val spent = transactions
                        .filter { !it.isIncome && it.category.equals(budget.category, ignoreCase = true) }
                        .sumOf { it.amount }

                    val progressFraction = if (budget.limit > 0.0) (spent / budget.limit).coerceIn(0.0, 2.0) else 0.0
                    val isExceeded = spent > budget.limit
                    val isWarning = spent > budget.limit * 0.85 && spent <= budget.limit

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .clickable {
                                inputCategoryName = budget.category
                                inputLimitStr = budget.limit.toString()
                                selectedColor = budget.colorHex
                                isEditing = true
                                showBudgetDialog = true
                            }
                            .testTag("budget_card_${budget.category}"),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp)
                        ) {
                            // Row Title Block
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(Color(budget.colorHex))
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = budget.category,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Text(
                                    text = "${currencyFormatter.format(spent)} of ${currencyFormatter.format(budget.limit)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isExceeded) ExpenseRose else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Custom colored visual progress bar block
                            val barColor = when {
                                isExceeded -> ExpenseRose
                                isWarning -> Color(0xFFFBBF24) // Theme warm gold warning
                                else -> Color(budget.colorHex)
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(progressFraction.toFloat().coerceAtMost(1f))
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(barColor)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Helper flags warning alerts block
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${(progressFraction * 100).toInt()}% ${getTranslation("spent", currentLang)}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isExceeded) ExpenseRose else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )

                                if (isExceeded) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = "Budget Exceeded",
                                            tint = ExpenseRose,
                                            modifier = Modifier.size(14.dp)
                                         )
                                         Spacer(modifier = Modifier.width(4.dp))
                                         Text(
                                             text = getTranslation("over_limit", currentLang),
                                             fontSize = 11.sp,
                                             fontWeight = FontWeight.Bold,
                                             color = ExpenseRose
                                         )
                                    }
                                } else if (isWarning) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = "Budget critical warning",
                                            tint = Color(0xFFFBBF24),
                                            modifier = Modifier.size(14.dp)
                                         )
                                         Spacer(modifier = Modifier.width(4.dp))
                                         Text(
                                             text = if (currentLang == AppLanguage.INDONESIAN) "85%+ Terpakai" else "85%+ Explored",
                                             fontSize = 11.sp,
                                             fontWeight = FontWeight.Bold,
                                             color = Color(0xFFFBBF24)
                                         )
                                    }
                                } else {
                                    val remainingFraction = (1.0 - progressFraction).coerceIn(0.0, 1.0)
                                    Text(
                                        text = "${currencyFormatter.format(budget.limit - spent)} ${getTranslation("left", currentLang)}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = SuccessGreen
                                    )
                                }
                            }
                        }
                    }
                    }
                }
            }
        }
    }

    if (showBudgetDialog) {
        AlertDialog(
            onDismissRequest = { showBudgetDialog = false },
            title = {
                Text(
                    text = if (isEditing) {
                        if (currentLang == AppLanguage.INDONESIAN) "Edit Batas Anggaran" else "Edit Budget Limit"
                    } else {
                        if (currentLang == AppLanguage.INDONESIAN) "Tambah Batas Anggaran" else "Add Budget Limit"
                    }
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = inputCategoryName,
                        onValueChange = { inputCategoryName = it },
                        label = { Text(if (currentLang == AppLanguage.INDONESIAN) "Nama Kategori" else "Category Name") },
                        singleLine = true,
                        enabled = !isEditing, // Category names should be fixed once saved, or deleted and recreated.
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("budget_category_name_input")
                    )

                    OutlinedTextField(
                        value = inputLimitStr,
                        onValueChange = {
                            if (it.isEmpty() || it.toDoubleOrNull() != null || it.all { ch -> ch.isDigit() || ch == '.' }) {
                                inputLimitStr = it
                            }
                        },
                        label = { Text(if (currentLang == AppLanguage.INDONESIAN) "Batas Anggaran (USD)" else "Budget Limit Amount (USD)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("budget_limit_input")
                    )

                    Text(
                        text = if (currentLang == AppLanguage.INDONESIAN) "Pilih Warna Identitas" else "Select Color Accent",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    // Color palette circles picker
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        colorPalette.forEach { hexColor ->
                            val color = Color(hexColor)
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .clickable { selectedColor = hexColor }
                                    .run {
                                        if (selectedColor == hexColor) {
                                            border(
                                                width = 3.dp,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                shape = CircleShape
                                            )
                                        } else this
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedColor == hexColor) {
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
                        val limitVal = inputLimitStr.toDoubleOrNull() ?: 0.0
                        if (inputCategoryName.trim().isNotEmpty() && limitVal > 0.0) {
                            viewModel.addBudget(inputCategoryName.trim(), limitVal, selectedColor)
                        }
                        showBudgetDialog = false
                    },
                    modifier = Modifier.testTag("budget_dialog_confirm")
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
                    if (isEditing) {
                        TextButton(
                            onClick = {
                                viewModel.deleteBudget(inputCategoryName)
                                showBudgetDialog = false
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = ExpenseRose),
                            modifier = Modifier.testTag("budget_dialog_delete")
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (currentLang == AppLanguage.INDONESIAN) "Hapus" else "Delete")
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    TextButton(onClick = { showBudgetDialog = false }) {
                        Text(if (currentLang == AppLanguage.INDONESIAN) "Batal" else "Cancel")
                    }
                }
            }
        )
    }
}
