package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.FinancialViewModel
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.ExpenseRose
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.runtime.collectAsState
import com.example.ui.getTranslation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: FinancialViewModel,
    onBack: () -> Unit
) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    var isIncome by remember { mutableStateOf(false) }
    var amountStr by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Food") }
    var note by remember { mutableStateOf("") }

    // Prefill current date: YYYY-MM-DD
    val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    var dateStr by remember { mutableStateOf(todayDate) }

    // OCR states
    var isScanning by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val budgetsState by viewModel.budgets.collectAsState()
    val customCatsState by viewModel.customCategories.collectAsState()

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }

    val expenseCategories = remember(budgetsState, customCatsState) {
        val list = budgetsState.map { it.category }.toMutableList()
        customCatsState.forEach {
            if (it !in list) list.add(it)
        }
        if ("Food" !in list) list.add(0, "Food") // safety fallback so initial state is valid
        list.toList()
    }

    val incomeCategories = remember(customCatsState) {
        val list = mutableListOf("Income", "Investment", "Bonus", "Refund")
        customCatsState.forEach {
            if (it !in list) list.add(it)
        }
        list.toList()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(getTranslation("add_transaction", currentLang), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Income / Expense Stylized Toggle Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(4.dp)
            ) {
                // Expense switch option
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (!isIncome) ExpenseRose.copy(alpha = 0.2f)
                            else Color.Transparent
                        )
                        .clickable {
                            isIncome = false
                            if (category !in expenseCategories) {
                                category = "Food"
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Expense (Pengeluaran)",
                        color = if (!isIncome) ExpenseRose else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (!isIncome) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }

                // Income switch option
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isIncome) SuccessGreen.copy(alpha = 0.2f)
                            else Color.Transparent
                        )
                        .clickable {
                            isIncome = true
                            if (category !in incomeCategories) {
                                category = "Income"
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Income (Pemasukan)",
                        color = if (isIncome) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (isIncome) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }

            // AI OCR Receipt Scanner (Biomimetic Simulated Feature)
            Button(
                onClick = {
                    scope.launch {
                        isScanning = true
                        snackbarHostState.showSnackbar("AI Receipt OCR: Launching simulated scanner lens...")
                        delay(2200) // Simulate scanning latency
                        
                        // Select a random sample receipt to autofill inputs
                        val receiptIndex = (1..3).random()
                        when (receiptIndex) {
                            1 -> {
                                amountStr = "24.50"
                                category = "Food"
                                note = "Starbucks AI OCR: Venti Latte, Croissant"
                            }
                            2 -> {
                                amountStr = "115.00"
                                category = "Bills"
                                note = "Verizon OCR: June Internet Cellular Bill"
                            }
                            3 -> {
                                amountStr = "45.00"
                                category = "Transport"
                                note = "Shell OCR: Station petrol tank refilling"
                            }
                        }
                        isScanning = false
                        snackbarHostState.showSnackbar("AI Receipt OCR: Successfully auto-filled form inputs!")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_ocr_scan_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                shape = RoundedCornerShape(16.dp),
                enabled = !isScanning
            ) {
                if (isScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Deconstructing Receipt & Layout...", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                } else {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Simulated receipt OCR capability",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Auto-fill with AI Receipt OCR Scan", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Amount Input Field
            OutlinedTextField(
                value = amountStr,
                onValueChange = { value ->
                    // Filter numeric entry limits
                    if (value.isEmpty() || value.toDoubleOrNull() != null || value.all { it.isDigit() || it == '.' }) {
                        amountStr = value
                    }
                },
                label = { Text("Amount Value (USD / IDR)") },
                placeholder = { Text("0.00") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                leadingIcon = {
                    Icon(imageVector = Icons.Default.MonetizationOn, contentDescription = "Cash Amount")
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("amount_input")
            )

            // Category Selection Block
            Text(
                text = "Select Category",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val fullList = if (isIncome) incomeCategories else expenseCategories
                val activeList = fullList + "+ Custom"
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val firstRow = activeList.take((activeList.size + 1) / 2)
                    val secondRow = activeList.drop((activeList.size + 1) / 2)

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("categories_row_1")
                    ) {
                        items(firstRow) { cat ->
                            CategoryChip(
                                title = cat,
                                isSelected = category == cat,
                                onClick = {
                                    if (cat == "+ Custom") {
                                        newCategoryName = ""
                                        showAddCategoryDialog = true
                                    } else {
                                        category = cat
                                    }
                                }
                            )
                        }
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("categories_row_2")
                    ) {
                        items(secondRow) { cat ->
                            CategoryChip(
                                title = cat,
                                isSelected = category == cat,
                                onClick = {
                                    if (cat == "+ Custom") {
                                        newCategoryName = ""
                                        showAddCategoryDialog = true
                                    } else {
                                        category = cat
                                    }
                                }
                            )
                        }
                    }
                }
            }

            if (showAddCategoryDialog) {
                AlertDialog(
                    onDismissRequest = { showAddCategoryDialog = false },
                    title = { Text(if (currentLang == com.example.ui.AppLanguage.INDONESIAN) "Tambah Kategori Baru" else "Add New Category") },
                    text = {
                        Column {
                            Text(
                                text = if (currentLang == com.example.ui.AppLanguage.INDONESIAN) "Tentukan opsi kategori Anda sendiri secara khusus." else "Determine your own custom category option.",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = newCategoryName,
                                onValueChange = { newCategoryName = it },
                                label = { Text(if (currentLang == com.example.ui.AppLanguage.INDONESIAN) "Nama Kategori" else "Category Name") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("new_category_input")
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if (newCategoryName.trim().isNotEmpty()) {
                                    val catTrimmed = newCategoryName.trim()
                                    viewModel.addCustomCategory(catTrimmed)
                                    category = catTrimmed
                                }
                                showAddCategoryDialog = false
                            },
                            modifier = Modifier.testTag("confirm_add_category")
                        ) {
                            Text(if (currentLang == com.example.ui.AppLanguage.INDONESIAN) "Tambah" else "Add", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddCategoryDialog = false }) {
                            Text(if (currentLang == com.example.ui.AppLanguage.INDONESIAN) "Batal" else "Cancel")
                        }
                    }
                )
            }

            // Date Picker Format Field
            OutlinedTextField(
                value = dateStr,
                onValueChange = { dateStr = it },
                label = { Text("Date (YYYY-MM-DD)") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.CalendarToday, contentDescription = "Calendar Picker")
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("date_input")
            )

            // Memo / Description note input
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Memo Note / Description") },
                placeholder = { Text("Add transaction notes...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Description, contentDescription = "Receipt Memo Description")
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("note_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Primary SAVE Action Button
            Button(
                onClick = {
                    val finalAmount = amountStr.toDoubleOrNull() ?: 0.0
                    if (finalAmount <= 0.0) {
                        scope.launch {
                            snackbarHostState.showSnackbar("❌ Please enter a valid currency Amount.")
                        }
                    } else if (dateStr.isEmpty()) {
                        scope.launch {
                            snackbarHostState.showSnackbar("❌ Date field cannot be empty.")
                        }
                    } else {
                        viewModel.addTransaction(
                            amount = finalAmount,
                            category = category,
                            date = dateStr,
                            note = note,
                            isIncome = isIncome
                        )
                        scope.launch {
                            snackbarHostState.showSnackbar("✅ Transaction successfully added to offline Room storage.")
                            delay(800)
                            onBack()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("save_transaction_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isIncome) SuccessGreen else ExpenseRose,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = "Confirm submission")
                Spacer(modifier = Modifier.width(10.dp))
                Text("Save Transaction", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
fun CategoryChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .border(
                width = 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                shape = RoundedCornerShape(100.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected Indicator",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
