package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.FinancialViewModel
import com.example.ui.getTranslation
import com.example.ui.AppLanguage
import com.example.ui.theme.ExpenseRose
import com.example.ui.theme.SuccessGreen
import java.text.NumberFormat
import java.util.Locale
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import kotlinx.coroutines.launch

@Composable
fun ReportsScreen(
    viewModel: FinancialViewModel
) {
    val transactions by viewModel.transactions.collectAsState()
    val budgets by viewModel.budgets.collectAsState()
    val currentLang by viewModel.currentLanguage.collectAsState()
    val currentCurrency by viewModel.currentCurrency.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    val currencyFormatter = NumberFormat.getCurrencyInstance(currentCurrency.locale)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // 1. FILTER STATES (Period, Type, Category)
    var selectedPeriod by remember { mutableStateOf("All Time") } // "All Time", "This Week", "This Month"
    var selectedType by remember { mutableStateOf("All") }     // "All", "Expense", "Income"
    var selectedCategory by remember { mutableStateOf("All Categories") }

    // 2. DYNAMICALLY FILTER TRANSACTIONS
    val filteredTransactions = remember(transactions, selectedPeriod, selectedType, selectedCategory) {
        transactions.filter { t ->
            // Date parsing & matching
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            val matchesPeriod = when (selectedPeriod) {
                "This Week" -> {
                    try {
                        val tDate = sdf.parse(t.date)
                        if (tDate != null) {
                            val diff = System.currentTimeMillis() - tDate.time
                            diff in 0..(7L * 24 * 60 * 60 * 1000)
                        } else true
                    } catch (e: Exception) { true }
                }
                "This Month" -> {
                    try {
                        val tDate = sdf.parse(t.date)
                        if (tDate != null) {
                            val diff = System.currentTimeMillis() - tDate.time
                            diff in 0..(30L * 24 * 60 * 60 * 1000)
                        } else true
                    } catch (e: Exception) { true }
                }
                else -> true
            }

            // Type filtration
            val matchesType = when (selectedType) {
                "Expense" -> !t.isIncome
                "Income" -> t.isIncome
                else -> true
            }

            // Category filtration
            val matchesCategory = if (selectedCategory == "All Categories" || selectedCategory == "Semua Kategori") {
                true
            } else {
                t.category.equals(selectedCategory, ignoreCase = true)
            }

            matchesPeriod && matchesType && matchesCategory
        }
    }

    // 3. AGGREGATES FOR CHARTS
    val totalFilteredAmount = filteredTransactions.sumOf { it.amount }
    val categorySumMap = filteredTransactions.groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amount } }

    // 4. EXPORT HANDLERS CONFIGURATION
    val csvString = remember(filteredTransactions) {
        val headers = "ID,Amount,Category,Date,Note,Type\n"
        val rows = filteredTransactions.map { t ->
            val safeNote = t.note.replace("\"", "\"\"")
            val escapedNote = if (safeNote.contains(",") || safeNote.contains("\n") || safeNote.contains("\"")) {
                "\"$safeNote\""
            } else {
                safeNote
            }
            val type = if (t.isIncome) "Income" else "Expense"
            "${t.id},${t.amount},${t.category},${t.date},$escapedNote,$type"
        }.joinToString("\n")
        headers + rows
    }

    val csvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { output ->
                    output.write(csvString.toByteArray(Charsets.UTF_8))
                    output.flush()
                }
                scope.launch {
                    snackbarHostState.showSnackbar(getTranslation("export_success", currentLang))
                }
            } catch (e: Exception) {
                scope.launch {
                    snackbarHostState.showSnackbar(getTranslation("export_failed", currentLang) + ": ${e.localizedMessage}")
                }
            }
        }
    }

    val pdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    val pdfDocument = PdfDocument()
                    val pageWidth = 595
                    val pageHeight = 842
                    var pageNumber = 1
                    
                    var currentInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    var currentPage = pdfDocument.startPage(currentInfo)
                    var canvas = currentPage.canvas
                    
                    val titlePaint = Paint().apply {
                        color = android.graphics.Color.DKGRAY
                        textSize = 18f
                        isFakeBoldText = true
                    }
                    
                    val filterPaint = Paint().apply {
                        color = android.graphics.Color.rgb(100, 110, 120)
                        textSize = 10f
                        textSkewX = -0.25f
                    }
                    
                    val headerPaint = Paint().apply {
                        color = android.graphics.Color.BLACK
                        textSize = 12f
                        isFakeBoldText = true
                    }
                    
                    val bodyPaint = Paint().apply {
                        color = android.graphics.Color.BLACK
                        textSize = 10f
                    }
                    
                    val shadowPaint = Paint().apply {
                        color = android.graphics.Color.LTGRAY
                        strokeWidth = 1f
                    }

                    val linePaint = Paint().apply {
                        color = android.graphics.Color.GRAY
                        strokeWidth = 0.5f
                    }
                    
                    var y = 50f
                    
                    canvas.drawText("FINANCIAL DATA FILTERED REPORT", 40f, y, titlePaint)
                    y += 18f
                    
                    val activeFiltersText = "Selected Filter: Period=$selectedPeriod | Type=$selectedType | Category=$selectedCategory"
                    canvas.drawText(activeFiltersText, 40f, y, filterPaint)
                    y += 20f
                    
                    val userText = "Owner: ${userProfile.name} (${userProfile.email})"
                    canvas.drawText(userText, 40f, y, bodyPaint)
                    y += 15f
                    
                    val dateText = "Export Date: 2026-06-20 (Local Time)"
                    canvas.drawText(dateText, 40f, y, bodyPaint)
                    canvas.drawLine(40f, y + 10f, 555f, y + 10f, shadowPaint)
                    y += 30f
                    
                    val totalIncome = filteredTransactions.filter { it.isIncome }.sumOf { it.amount }
                    val totalExpense = filteredTransactions.filter { !it.isIncome }.sumOf { it.amount }
                    val balance = totalIncome - totalExpense
                    
                    val rectPaint = Paint().apply {
                        color = android.graphics.Color.argb(25, 0, 121, 107) // Light teal
                        style = android.graphics.Paint.Style.FILL
                    }
                    canvas.drawRect(40f, y, 555f, y + 55f, rectPaint)
                    
                    canvas.drawText("Filtered Summary Metrics:", 50f, y + 18f, headerPaint)
                    canvas.drawText("Total Income: ${currentCurrency.symbol} ${String.format(Locale.US, "%,.2f", totalIncome)}", 50f, y + 36f, bodyPaint)
                    canvas.drawText("Total Expense: ${currentCurrency.symbol} ${String.format(Locale.US, "%,.2f", totalExpense)}", 210f, y + 36f, bodyPaint)
                    canvas.drawText("Net Balance: ${currentCurrency.symbol} ${String.format(Locale.US, "%,.2f", balance)}", 380f, y + 36f, bodyPaint)
                    
                    y += 85f
                    
                    canvas.drawText("Date", 40f, y, headerPaint)
                    canvas.drawText("Type", 120f, y, headerPaint)
                    canvas.drawText("Category", 185f, y, headerPaint)
                    canvas.drawText("Note", 285f, y, headerPaint)
                    canvas.drawText("Amount", 485f, y, headerPaint)
                    canvas.drawLine(40f, y + 5f, 555f, y + 5f, shadowPaint)
                    y += 20f
                    
                    val greenPaint = Paint().apply {
                        color = android.graphics.Color.rgb(0, 140, 60)
                        textSize = 10f
                        isFakeBoldText = true
                    }
                    
                    val redPaint = Paint().apply {
                        color = android.graphics.Color.rgb(210, 30, 30)
                        textSize = 10f
                        isFakeBoldText = true
                    }
                    
                    filteredTransactions.forEach { t ->
                        if (y > 780f) {
                            canvas.drawText("Page $pageNumber", 490f, 810f, bodyPaint)
                            pdfDocument.finishPage(currentPage)
                            pageNumber++
                            currentInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                            currentPage = pdfDocument.startPage(currentInfo)
                            canvas = currentPage.canvas
                            
                            y = 50f
                            canvas.drawText("FINANCIAL DATA FILTERED REPORT (Cont.)", 40f, y, titlePaint)
                            canvas.drawLine(40f, y + 10f, 555f, y + 10f, shadowPaint)
                            y += 30f
                            
                            canvas.drawText("Date", 40f, y, headerPaint)
                            canvas.drawText("Type", 120f, y, headerPaint)
                            canvas.drawText("Category", 185f, y, headerPaint)
                            canvas.drawText("Note", 285f, y, headerPaint)
                            canvas.drawText("Amount", 485f, y, headerPaint)
                            canvas.drawLine(40f, y + 5f, 555f, y + 5f, shadowPaint)
                            y += 20f
                        }
                        
                        canvas.drawText(t.date, 40f, y, bodyPaint)
                        
                        val typeText = if (t.isIncome) "Income" else "Expense"
                        canvas.drawText(typeText, 120f, y, bodyPaint)
                        canvas.drawText(t.category, 185f, y, bodyPaint)
                        
                        val displayNote = if (t.note.length > 30) t.note.take(28) + ".." else t.note
                        canvas.drawText(displayNote, 285f, y, bodyPaint)
                        
                        val amountStr = "${currentCurrency.symbol} ${String.format(Locale.US, "%,.2f", t.amount)}"
                        val textPaint = if (t.isIncome) greenPaint else redPaint
                        canvas.drawText(amountStr, 485f, y, textPaint)
                        
                        canvas.drawLine(40f, y + 5f, 555f, y + 5f, linePaint)
                        y += 20f
                    }
                    
                    canvas.drawText("Page $pageNumber", 490f, 810f, bodyPaint)
                    
                    pdfDocument.finishPage(currentPage)
                    pdfDocument.writeTo(outputStream)
                    pdfDocument.close()
                }
                scope.launch {
                    snackbarHostState.showSnackbar(getTranslation("export_success", currentLang))
                }
            } catch (e: Exception) {
                scope.launch {
                    snackbarHostState.showSnackbar(getTranslation("export_failed", currentLang) + ": ${e.localizedMessage}")
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                    .verticalScroll(rememberScrollState())
            ) {
            // Header Info Title
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Text(
                    text = getTranslation("reports", currentLang),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (currentLang == AppLanguage.INDONESIAN) "Analisis visual pengeluaran Anda langsung dari database offline" else "Visual spending analyses generated directly from Room cache",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // FILTER CONTROLS PANEL CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .testTag("filter_options_card"),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (currentLang == AppLanguage.INDONESIAN) "Saring Data Laporan" else "Filter Report Data",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Row of Period selection Chips
                    Column {
                        Text(
                            text = if (currentLang == AppLanguage.INDONESIAN) "Jangka Waktu" else "Time Period",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("All Time", "This Week", "This Month").forEach { period ->
                                val isSelected = selectedPeriod == period
                                val label = when (period) {
                                    "This Week" -> if (currentLang == AppLanguage.INDONESIAN) "Minggu Ini" else "This Week"
                                    "This Month" -> if (currentLang == AppLanguage.INDONESIAN) "Bulan Ini" else "This Month"
                                    else -> if (currentLang == AppLanguage.INDONESIAN) "Semua" else "All Time"
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(100.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                                            shape = RoundedCornerShape(100.dp)
                                        )
                                        .clickable { selectedPeriod = period }
                                        .padding(vertical = 8.dp)
                                        .testTag("filter_period_chip_${period.replace(" ", "_")}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Row of Type selection Chips (All, Expense, Income)
                    Column {
                        Text(
                            text = if (currentLang == AppLanguage.INDONESIAN) "Tipe Transaksi" else "Transaction Type",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("All", "Expense", "Income").forEach { type ->
                                val isSelected = selectedType == type
                                val label = when (type) {
                                    "Expense" -> if (currentLang == AppLanguage.INDONESIAN) "Pengeluaran" else "Expenses"
                                    "Income" -> if (currentLang == AppLanguage.INDONESIAN) "Pemasukan" else "Income"
                                    else -> if (currentLang == AppLanguage.INDONESIAN) "Semua Jenis" else "All Types"
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(100.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                                            shape = RoundedCornerShape(100.dp)
                                        )
                                        .clickable { selectedType = type }
                                        .padding(vertical = 8.dp)
                                        .testTag("filter_type_chip_$type"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Scrollable Category selector
                    val categories = remember(transactions) {
                        val list = transactions.map { it.category }.distinct().filter { it.isNotBlank() }.sorted()
                        listOf("All Categories") + list
                    }
                    
                    Column {
                        Text(
                            text = if (currentLang == AppLanguage.INDONESIAN) "Pilih Kategori" else "Category Selector",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        androidx.compose.foundation.lazy.LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(categories.size) { index ->
                                val cat = categories[index]
                                val isSelected = selectedCategory == cat
                                val label = if (cat == "All Categories") {
                                    if (currentLang == AppLanguage.INDONESIAN) "Semua Kategori" else "All Categories"
                                } else cat
                                
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.secondary
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { selectedCategory = cat }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                        .testTag("filter_category_chip_${cat.replace(" ", "_")}")
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // EXPORT ACTIONS CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .testTag("export_options_card"),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (currentLang == AppLanguage.INDONESIAN) "Ekspor & Cetak Laporan Terpilih" else "Export & Build Filtered Report",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = if (currentLang == AppLanguage.INDONESIAN) {
                            "Unduh riwayat transaksi yang tersaring saat ini (${filteredTransactions.size} Transaksi) dalam bentuk file dokumen."
                        } else {
                            "Download the currently filtered list (${filteredTransactions.size} transactions) as a clean offline file."
                        },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                csvLauncher.launch("financial_report_${System.currentTimeMillis()}.csv")
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("export_csv_button"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "CSV",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "CSV",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = {
                                pdfLauncher.launch("financial_report_${System.currentTimeMillis()}.pdf")
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("export_pdf_button"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = "PDF",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "PDF",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // VISUAL RENDERING SECTION
            if (filteredTransactions.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PieChart,
                            contentDescription = "Analysis Empty",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (currentLang == AppLanguage.INDONESIAN) "Saringan Tidak Menemukan Data" else "No Matching Data",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (currentLang == AppLanguage.INDONESIAN) {
                                "Pilih filter saringan atau jenis transaksi lain untuk memunculkan visual grafik grafik laporan."
                            } else {
                                "Adjust your filters or register offline transactions matching the chosen criteria to see analytics."
                            },
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                // Map out colors from current category budgets
                val categoryColorMap = budgets.associate { it.category to Color(it.colorHex) }
                val fallbackColors = listOf(Color(0xFF00796B), Color(0xFF303F9F), Color(0xFFE64A19), Color(0xFFD32F2F), Color(0xFF7B1FA2), Color(0xFF388E3C), Color(0xFFFBC02D), Color(0xFFC2185B))

                // DOUGHNUT CHART CARD
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .testTag("doughnut_chart_card"),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (selectedType == "Income") {
                                if (currentLang == AppLanguage.INDONESIAN) "Pemasukan berdasarkan Kategori" else "Income by Category"
                            } else {
                                if (currentLang == AppLanguage.INDONESIAN) "Pengeluaran berdasarkan Kategori" else "Expenses by Category"
                            },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                        )

                        // Compose custom Canvas drawing (Doughnut Chart)
                        Box(
                            modifier = Modifier
                                .size(180.dp)
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                var startAngle = -90f
                                categorySumMap.entries.forEachIndexed { index, (cat, valSum) ->
                                    val sweepAngle = ((valSum / totalFilteredAmount) * 360f).toFloat()
                                    val col = categoryColorMap[cat] ?: fallbackColors[index % fallbackColors.size]

                                    drawArc(
                                        color = col,
                                        startAngle = startAngle,
                                        sweepAngle = sweepAngle,
                                        useCenter = false,
                                        style = Stroke(width = 30.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                    startAngle += sweepAngle
                                }
                            }

                            // Center label sum
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (selectedType == "Income") {
                                        if (currentLang == AppLanguage.INDONESIAN) "Total Masuk" else "Total Income"
                                    } else {
                                        if (currentLang == AppLanguage.INDONESIAN) "Total Keluar" else "Total Spent"
                                    },
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = currencyFormatter.format(totalFilteredAmount),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Chart Color Legends Row/Column Grid
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            categorySumMap.entries.forEachIndexed { i, (catName, sumVal) ->
                                val pct = (sumVal / totalFilteredAmount * 100).toInt()
                                val col = categoryColorMap[catName] ?: fallbackColors[i % fallbackColors.size]

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
                                                .background(col)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = catName,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = currencyFormatter.format(sumVal),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "$pct%",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = col,
                                            modifier = Modifier.width(36.dp),
                                            textAlign = TextAlign.End
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // BAR CHART LAYOUT CARD
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .testTag("bar_chart_card"),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = if (currentLang == AppLanguage.INDONESIAN) "Kepadatan Nilai Kategori" else "Category Value Density",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 20.dp)
                        )

                        // Simple scalable vertical columns represent different amounts
                        val maxSpending = categorySumMap.values.maxOrNull() ?: 1.0

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            categorySumMap.entries.forEachIndexed { i, (catName, sumVal) ->
                                val normalizedHeightFraction = (sumVal / maxSpending).toFloat().coerceIn(0.1f, 1f)
                                val col = categoryColorMap[catName] ?: fallbackColors[i % fallbackColors.size]

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Numeric label on top of column bar
                                    Text(
                                        text = if (sumVal >= 1000) "${(sumVal / 1000).toInt()}k" else sumVal.toInt().toString(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )

                                    // Dynamic vertical bar block
                                    Box(
                                        modifier = Modifier
                                            .width(22.dp)
                                            .fillMaxHeight(normalizedHeightFraction)
                                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                            .background(col)
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Category name bottom flag
                                    Text(
                                        text = catName.take(4),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
}

// Utility extension to make fillMaxHeight work in Row blocks
@Composable
fun Modifier.fillMaxHeight(fraction: Float) = this.then(
    Modifier.padding(top = (160 * (1f - fraction)).dp)
)
