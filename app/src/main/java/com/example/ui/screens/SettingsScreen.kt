package com.example.ui.screens

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.graphics.pdf.PdfDocument
import android.graphics.Paint
import android.graphics.Canvas
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppCurrency
import com.example.ui.AppLanguage
import com.example.ui.FinancialViewModel
import com.example.ui.getTranslation
import com.example.ui.theme.ExpenseRose
import com.example.ui.theme.SuccessGreen
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: FinancialViewModel,
    onBack: () -> Unit
) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    val currentCurrency by viewModel.currentCurrency.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val isLockEnabled by viewModel.isLockEnabled.collectAsState()
    val securityPin by viewModel.securityPin.collectAsState()

    var tempName by remember(userProfile) { mutableStateOf(userProfile.name) }
    var tempEmail by remember(userProfile) { mutableStateOf(userProfile.email) }
    var tempPin by remember(securityPin) { mutableStateOf(securityPin) }

    var showLanguageDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    val transactions by viewModel.transactions.collectAsState()
    val context = LocalContext.current

    val csvString = remember(transactions) {
        val headers = "ID,Amount,Category,Date,Note,Type\n"
        val rows = transactions.map { t ->
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
                        textSize = 20f
                        isFakeBoldText = true
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
                    
                    canvas.drawText("FINANCIAL TRACKER REPORT", 40f, y, titlePaint)
                    y += 25f
                    
                    val userText = "Backup Owner: ${userProfile.name} (${userProfile.email})"
                    canvas.drawText(userText, 40f, y, bodyPaint)
                    y += 15f
                    
                    val dateText = "Export Date: 2026-06-20 (Local Time)"
                    canvas.drawText(dateText, 40f, y, bodyPaint)
                    canvas.drawLine(40f, y + 10f, 555f, y + 10f, shadowPaint)
                    y += 30f
                    
                    val totalIncome = transactions.filter { it.isIncome }.sumOf { it.amount }
                    val totalExpense = transactions.filter { !it.isIncome }.sumOf { it.amount }
                    val balance = totalIncome - totalExpense
                    
                    val rectPaint = Paint().apply {
                        color = android.graphics.Color.argb(30, 0, 150, 136)
                        style = android.graphics.Paint.Style.FILL
                    }
                    canvas.drawRect(40f, y, 555f, y + 55f, rectPaint)
                    
                    canvas.drawText("Summary statistics:", 50f, y + 18f, headerPaint)
                    canvas.drawText("Total Income: ${currentCurrency.symbol} ${String.format(Locale.US, "%,.2f", totalIncome)}", 50f, y + 34f, bodyPaint)
                    canvas.drawText("Total Expenses: ${currentCurrency.symbol} ${String.format(Locale.US, "%,.2f", totalExpense)}", 220f, y + 34f, bodyPaint)
                    canvas.drawText("Net Balance: ${currentCurrency.symbol} ${String.format(Locale.US, "%,.2f", balance)}", 390f, y + 34f, bodyPaint)
                    
                    y += 80f
                    
                    canvas.drawText("Date", 40f, y, headerPaint)
                    canvas.drawText("Type", 120f, y, headerPaint)
                    canvas.drawText("Category", 185f, y, headerPaint)
                    canvas.drawText("Note", 285f, y, headerPaint)
                    canvas.drawText("Amount", 485f, y, headerPaint)
                    canvas.drawLine(40f, y + 5f, 555f, y + 5f, shadowPaint)
                    y += 20f
                    
                    val greenPaint = Paint().apply {
                        color = android.graphics.Color.rgb(0, 150, 0)
                        textSize = 10f
                        isFakeBoldText = true
                    }
                    
                    val redPaint = Paint().apply {
                        color = android.graphics.Color.rgb(200, 0, 0)
                        textSize = 10f
                        isFakeBoldText = true
                    }
                    
                    transactions.forEach { t ->
                        if (y > 780f) {
                            pdfDocument.finishPage(currentPage)
                            pageNumber++
                            currentInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                            currentPage = pdfDocument.startPage(currentInfo)
                            canvas = currentPage.canvas
                            
                            y = 50f
                            canvas.drawText("FINANCIAL TRACKER REPORT (Cont.)", 40f, y, titlePaint)
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = getTranslation("settings", currentLang),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Section 1: User Profile Card Information Block
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_profile_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userProfile.initials,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = getTranslation("profile", currentLang),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = getTranslation("edit_profile_desc", currentLang),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        label = { Text(getTranslation("username", currentLang)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("setting_input_username"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = tempEmail,
                        onValueChange = { tempEmail = it },
                        label = { Text(getTranslation("email", currentLang)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("setting_input_email"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (tempName.isNotBlank() && tempEmail.isNotBlank()) {
                                viewModel.updateProfile(tempName, tempEmail)
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        getTranslation("profile_updated", currentLang)
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("profile_save_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Save")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(getTranslation("save", currentLang), fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Section 2: Preferences Configurations Block
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_prefs_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = getTranslation("app_preferences", currentLang),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Language Selector Selector Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showLanguageDialog = true }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Language Option",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = getTranslation("language", currentLang),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Text(
                            text = currentLang.displayName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Currency Selection Trigger Block
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showCurrencyDialog = true }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = "Currency Choice",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = getTranslation("currency", currentLang),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Text(
                            text = currentCurrency.displayName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Section 3: App Security Block
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_security_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = getTranslation("security", currentLang),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Toggle Security Switch Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PrivacyTip,
                                contentDescription = "Security Status",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = getTranslation("enable_pin_lock", currentLang),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Switch(
                            checked = isLockEnabled,
                            onCheckedChange = { viewModel.updateLockStatus(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.testTag("pin_lock_toggle")
                        )
                    }

                    if (isLockEnabled) {
                        Spacer(modifier = Modifier.height(12.dp))

                        // Custom Numeric Pin field entry
                        OutlinedTextField(
                            value = tempPin,
                            onValueChange = { input ->
                                if (input.length <= 4 && input.all { it.isDigit() }) {
                                    tempPin = input
                                    if (input.length == 4) {
                                        viewModel.updateSecurityPin(input)
                                    }
                                }
                            },
                            label = { Text(getTranslation("new_pin", currentLang)) },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("setting_input_pin"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            ),
                            singleLine = true
                        )
                    }
                }
            }

            // Section 3.5: Backup & Export Block
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_backup_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = getTranslation("backup_and_export", currentLang),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = getTranslation("backup_desc", currentLang),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                csvLauncher.launch("financial_backup_${System.currentTimeMillis()}.csv")
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("export_csv_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "CSV",
                                modifier = Modifier.size(18.dp)
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
                                pdfLauncher.launch("financial_backup_${System.currentTimeMillis()}.pdf")
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("export_pdf_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = "PDF",
                                modifier = Modifier.size(18.dp)
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

            // Section 4: Risk / Danger action center
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_danger_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Danger Zone",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ExpenseRose,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Button(
                        onClick = { showResetDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("danger_reset_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ExpenseRose.copy(alpha = 0.15f),
                            contentColor = ExpenseRose
                        )
                    ) {
                        Icon(imageVector = Icons.Default.DeleteForever, contentDescription = "Reset Forever")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(getTranslation("reset_all_data", currentLang), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Modal dialogue 1: Language selection sheet list
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = {
                Text(
                    text = getTranslation("language", currentLang),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Column {
                    AppLanguage.entries.forEach { lang ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.updateLanguage(lang)
                                    showLanguageDialog = false
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = lang.displayName,
                                fontSize = 16.sp,
                                fontWeight = if (lang == currentLang) FontWeight.Bold else FontWeight.Normal,
                                color = if (lang == currentLang) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(getTranslation("cancel", currentLang))
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // Modal dialogue 2: Currency selector selection sheet list
    if (showCurrencyDialog) {
        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            title = {
                Text(
                    text = getTranslation("currency", currentLang),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    AppCurrency.entries.forEach { currency ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.updateCurrency(currency)
                                    showCurrencyDialog = false
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = currency.displayName,
                                fontSize = 16.sp,
                                fontWeight = if (currency == currentCurrency) FontWeight.Bold else FontWeight.Normal,
                                color = if (currency == currentCurrency) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showCurrencyDialog = false }) {
                    Text(getTranslation("cancel", currentLang))
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // Modal dialogue 3: Danger Zone Data Purge validation
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text(
                    text = getTranslation("reset_all_data", currentLang),
                    fontWeight = FontWeight.Bold,
                    color = ExpenseRose
                )
            },
            text = {
                Text(
                    text = getTranslation("reset_confirm", currentLang),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllTransactions()
                        showResetDialog = false
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                getTranslation("data_cleared", currentLang)
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRose)
                ) {
                    Text(getTranslation("reset_all_data", currentLang), color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(getTranslation("cancel", currentLang))
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}
