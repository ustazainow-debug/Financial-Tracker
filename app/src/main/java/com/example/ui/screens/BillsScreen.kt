package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material3.Switch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.BillSubscription
import com.example.ui.FinancialViewModel
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.ExpenseRose
import com.example.ui.getTranslation
import com.example.ui.AppLanguage
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BillsScreen(
    viewModel: FinancialViewModel
) {
    val bills by viewModel.bills.collectAsState()
    val currentLang by viewModel.currentLanguage.collectAsState()
    val currentCurrency by viewModel.currentCurrency.collectAsState()

    val currencyFormatter = NumberFormat.getCurrencyInstance(currentCurrency.locale)

    // Parse helper to find if the bill is due within 7 days
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val currentMillis = System.currentTimeMillis()

    // Custom state variables for edit dialog
    var showBillDialog by remember { mutableStateOf(false) }
    var isEditingBill by remember { mutableStateOf(false) }
    var editingBillId by remember { mutableStateOf(0) }
    var inputBillName by remember { mutableStateOf("") }
    var inputBillAmountStr by remember { mutableStateOf("") }
    var inputBillDueDate by remember { mutableStateOf("") }
    var inputBillCategory by remember { mutableStateOf("Bills") }
    var inputBillIsPaid by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }

    // State variables for installment plan
    var inputBillIsInstallment by remember { mutableStateOf(false) }
    var inputBillDurationMonths by remember { mutableStateOf("3") }
    var inputBillPaidMonths by remember { mutableStateOf("0") }
    var inputBillFrequency by remember { mutableStateOf("Monthly") }
    var inputBillMonthlyPayment by remember { mutableStateOf("300000") }
    var inputBillUseDifferentPayments by remember { mutableStateOf(false) }
    var customPaymentsList by remember { mutableStateOf(listOf<String>()) }
    var selectedCustomMonthIdx by remember { mutableStateOf(0) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    inputBillName = ""
                    inputBillAmountStr = ""
                    inputBillDueDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    inputBillCategory = "Bills"
                    inputBillIsPaid = false
                    inputBillIsInstallment = false
                    inputBillDurationMonths = "3"
                    inputBillPaidMonths = "0"
                    inputBillFrequency = "Monthly"
                    inputBillMonthlyPayment = "300000"
                    inputBillUseDifferentPayments = false
                    customPaymentsList = List(3) { "300000" }
                    selectedCustomMonthIdx = 0
                    isEditingBill = false
                    showBillDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_bill_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add recurrence bill")
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
                // Header Description block
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Text(
                    text = getTranslation("bills", currentLang),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (currentLang == AppLanguage.INDONESIAN) "Pantau & sesuaikan tagihan rincian dan langganan Anda" else "Keep track & customize active memberships and recurring monthly bills",
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
                item {
                    val unpaidBills = bills.filter { !it.isPaid }
                    val urgentBill = unpaidBills.minByOrNull { it.dueDate }
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .testTag("reminders_card"),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = if (notificationsEnabled) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                                        contentDescription = "Alert Bell",
                                        tint = if (notificationsEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Text(
                                        text = if (currentLang == AppLanguage.INDONESIAN) "Pengingat Pembayaran" else "Payment Reminders",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                
                                Switch(
                                    checked = notificationsEnabled,
                                    onCheckedChange = { notificationsEnabled = it },
                                    modifier = Modifier.testTag("notifications_toggle")
                                )
                            }
                            
                            if (urgentBill != null) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Alarm,
                                            contentDescription = "Alarm Clock Icon",
                                            tint = ExpenseRose,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = if (currentLang == AppLanguage.INDONESIAN) "Tagihan Terdekat:" else "Next Due Soon:",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (currentLang == AppLanguage.INDONESIAN) {
                                            "\"${urgentBill.name}\" sebesar ${currencyFormatter.format(urgentBill.amount)} jatuh tempo pada tanggal ${urgentBill.dueDate}!"
                                        } else {
                                            "\"${urgentBill.name}\" of ${currencyFormatter.format(urgentBill.amount)} is due on ${urgentBill.dueDate}!"
                                        },
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (notificationsEnabled) {
                                        Text(
                                            text = if (currentLang == AppLanguage.INDONESIAN) {
                                                "🔔 Notifikasi aktif: Anda akan diingatkan tepat waktu."
                                            } else {
                                                "🔔 Push Alert active: You will be reminded on this due date."
                                            },
                                            fontSize = 11.sp,
                                            color = SuccessGreen,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(top = 6.dp)
                                        )
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f))
                                        .padding(12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (currentLang == AppLanguage.INDONESIAN) {
                                            "Bagus sekali! Semua tagihan Anda saat ini lunas."
                                        } else {
                                            "Awesome! All your current bills are completely settled."
                                        },
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = SuccessGreen
                                    )
                                }
                            }
                        }
                    }
                }

                // Sorting unpaid bills soonest-due first to optimize utility
                val sortedBills = bills.sortedWith(
                    compareBy<BillSubscription> { it.isPaid }.thenBy { it.dueDate }
                )

                items(
                    items = sortedBills,
                    key = { it.id }
                ) { bill ->
                    // Calculate if due soon (e.g., due date is within 7 days from now)
                    var isDueSoon = false
                    try {
                        val parsedDate = sdf.parse(bill.dueDate)
                        if (parsedDate != null) {
                            val diffDays = (parsedDate.time - currentMillis) / (1000 * 60 * 60 * 24L)
                            isDueSoon = diffDays in 0..7
                        }
                    } catch (e: Exception) {
                        // ignore parsing glitches
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .clickable {
                                editingBillId = bill.id
                                inputBillName = bill.name
                                inputBillAmountStr = bill.amount.toString()
                                inputBillDueDate = bill.dueDate
                                inputBillCategory = bill.category
                                inputBillIsPaid = bill.isPaid
                                inputBillIsInstallment = bill.isInstallment
                                inputBillDurationMonths = bill.durationMonths.toString()
                                inputBillPaidMonths = bill.paidMonths.toString()
                                inputBillFrequency = bill.frequency
                                inputBillMonthlyPayment = bill.monthlyPayment.toString()
                                inputBillUseDifferentPayments = bill.customPayments.isNotEmpty()
                                customPaymentsList = if (bill.customPayments.isNotEmpty()) {
                                    bill.customPayments.split(",")
                                } else {
                                    List(bill.durationMonths) { bill.monthlyPayment.toString() }
                                }
                                selectedCustomMonthIdx = 0
                                isEditingBill = true
                                showBillDialog = true
                            }
                            .testTag("bill_card_${bill.id}"),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                // Interactive Checkbox Circle for quick toggle
                                IconButtonWithFeedback(
                                    isChecked = bill.isPaid,
                                    onToggle = { viewModel.toggleBillStatus(bill.id) },
                                    modifier = Modifier.testTag("toggle_bill_${bill.id}")
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = bill.name,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (bill.isPaid) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                                        )
                                        if (bill.isInstallment) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                            ) {
                                                Text(
                                                    text = "INSTALLMENT",
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarMonth,
                                            contentDescription = "Due Date Flag",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Due on ${bill.dueDate}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                    }
                                    if (bill.isInstallment) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        val totalPeriodMonths = if (bill.frequency == "Semi-Annually" || bill.frequency == "Semi-Annual") {
                                            bill.durationMonths * 6
                                        } else if (bill.frequency == "Quarterly") {
                                            bill.durationMonths * 3
                                        } else {
                                            bill.durationMonths
                                        }
                                        
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            val langIndo = (currentLang == AppLanguage.INDONESIAN)
                                            val progressLabel = if (langIndo) {
                                                "Bayar: ${bill.paidMonths}/${bill.durationMonths} (${bill.frequency}) • Periode: $totalPeriodMonths bln"
                                            } else {
                                                "Paid: ${bill.paidMonths}/${bill.durationMonths} (${bill.frequency}) • Period: $totalPeriodMonths mos"
                                            }
                                            Text(
                                                text = progressLabel,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            // Progress bar
                                            val progress = if (bill.durationMonths > 0) bill.paidMonths.toFloat() / bill.durationMonths.toFloat() else 0f
                                            LinearProgressIndicator(
                                                progress = { progress },
                                                modifier = Modifier.width(140.dp).height(4.dp).clip(RoundedCornerShape(2.dp)),
                                                color = MaterialTheme.colorScheme.primary,
                                                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                            )
                                            
                                            if (bill.customPayments.isNotEmpty()) {
                                                val customList = bill.customPayments.split(",")
                                                val currentIdx = bill.paidMonths.coerceAtMost(customList.size - 1)
                                                val currentAmt = customList.getOrNull(currentIdx)?.toDoubleOrNull() ?: bill.monthlyPayment
                                                Text(
                                                    text = if (langIndo) {
                                                        "Bayar skrg: ${currencyFormatter.format(currentAmt)}"
                                                    } else {
                                                        "Pay now: ${currencyFormatter.format(currentAmt)}"
                                                    },
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Text(
                                                    text = customList.mapIndexed { idx, amtStr ->
                                                        val amt = amtStr.toDoubleOrNull() ?: 0.0
                                                        val formatted = if (amt >= 1000000) {
                                                            String.format("%.1fM", amt / 1000000.0).replace(".0", "")
                                                        } else if (amt >= 1000) {
                                                            "${(amt/1000).toInt()}k"
                                                        } else {
                                                            amt.toInt().toString()
                                                        }
                                                        if (idx == currentIdx) "[$formatted]" else formatted
                                                    }.joinToString(" → "),
                                                    fontSize = 8.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Amount and Badge Column block
                            Column(
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = currencyFormatter.format(bill.amount),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (bill.isPaid) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                // Dynamic badge layout
                                val (badgeBg, badgeText, statusLabel) = when {
                                    bill.isPaid -> Triple(SuccessGreen.copy(alpha = 0.2f), SuccessGreen, "PAID")
                                    isDueSoon -> Triple(Color(0xFFFBBF24).copy(alpha = 0.2f), Color(0xFFFBBF24), "DUE SOON")
                                    else -> Triple(ExpenseRose.copy(alpha = 0.2f), ExpenseRose, "UNPAID")
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(badgeBg)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = statusLabel,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = badgeText,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (showBillDialog) {
                AlertDialog(
                    onDismissRequest = { showBillDialog = false },
                    title = {
                        Text(
                            text = if (isEditingBill) {
                                if (currentLang == AppLanguage.INDONESIAN) "Edit Rincian Tagihan" else "Edit Bill Recurrence"
                            } else {
                                if (currentLang == AppLanguage.INDONESIAN) "Tambah Tagihan Baru" else "Add New Bill"
                            }
                        )
                    },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.verticalScroll(rememberScrollState())
                        ) {
                            OutlinedTextField(
                                value = inputBillName,
                                onValueChange = { inputBillName = it },
                                label = { Text(if (currentLang == AppLanguage.INDONESIAN) "Nama Tagihan / Layanan" else "Bill / Membership Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("bill_name_input")
                            )

                            OutlinedTextField(
                                value = inputBillAmountStr,
                                onValueChange = {
                                    if (it.isEmpty() || it.toDoubleOrNull() != null || it.all { ch -> ch.isDigit() || ch == '.' }) {
                                        inputBillAmountStr = it
                                    }
                                },
                                label = { Text(if (currentLang == AppLanguage.INDONESIAN) "Jumlah Tagihan (USD)" else "Billing Amount (USD)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("bill_amount_input")
                            )

                            OutlinedTextField(
                                value = inputBillDueDate,
                                onValueChange = { inputBillDueDate = it },
                                label = { Text(if (currentLang == AppLanguage.INDONESIAN) "Tanggal Jatuh Tempo (YYYY-MM-DD)" else "Due Date (YYYY-MM-DD)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("bill_due_date_input")
                            )

                            // Quick Date suggestions chips helper
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val sdfFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                val dateOptions = listOf(
                                    Pair(if (currentLang == AppLanguage.INDONESIAN) "Hari Ini" else "Today", 0),
                                    Pair(if (currentLang == AppLanguage.INDONESIAN) "+1 Ming" else "+1 Wk", 7),
                                    Pair(if (currentLang == AppLanguage.INDONESIAN) "+2 Ming" else "+2 Wks", 14),
                                    Pair(if (currentLang == AppLanguage.INDONESIAN) "+1 Bln" else "+1 Mo", 30)
                                )
                                dateOptions.forEach { (label, daysAhead) ->
                                    val calcCal = java.util.Calendar.getInstance()
                                    calcCal.add(java.util.Calendar.DAY_OF_YEAR, daysAhead)
                                    val targetDateStr = sdfFormat.format(calcCal.time)
                                    val isSelected = inputBillDueDate == targetDateStr
                                    
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable { inputBillDueDate = targetDateStr }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 11.sp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Checkbox(
                                    checked = inputBillIsInstallment,
                                    onCheckedChange = { inputBillIsInstallment = it },
                                    modifier = Modifier.testTag("bill_is_installment_checkbox")
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (currentLang == AppLanguage.INDONESIAN) "Skema Cicilan / PayLater" else "Installment / PayLater Plan",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            }

                            if (inputBillIsInstallment) {
                                val langIndo = (currentLang == AppLanguage.INDONESIAN)
                                val durationVal = inputBillDurationMonths.toIntOrNull() ?: 3

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = inputBillDurationMonths,
                                        onValueChange = {
                                            if (it.isEmpty() || it.toIntOrNull() != null) {
                                                inputBillDurationMonths = it
                                                val count = it.toIntOrNull() ?: 0
                                                if (count > 0) {
                                                    val safeDuration = count.coerceIn(1, 48)
                                                    if (customPaymentsList.size != safeDuration) {
                                                        customPaymentsList = List(safeDuration) { idx ->
                                                            customPaymentsList.getOrNull(idx) ?: inputBillMonthlyPayment
                                                        }
                                                    }
                                                }
                                            }
                                        },
                                        label = { Text(if (langIndo) "Durasi Cicilan (Bulan)" else "Installment Duration (Months)") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f).testTag("bill_installment_duration_input")
                                    )

                                    // Decrease Duration Button (-)
                                    OutlinedIconButton(
                                        onClick = {
                                            val currentDur = inputBillDurationMonths.toIntOrNull() ?: 3
                                            if (currentDur > 1) {
                                                val nextDur = currentDur - 1
                                                inputBillDurationMonths = nextDur.toString()
                                                if (customPaymentsList.size != nextDur) {
                                                    customPaymentsList = List(nextDur) { idx ->
                                                        customPaymentsList.getOrNull(idx) ?: inputBillMonthlyPayment
                                                    }
                                                }
                                            }
                                        },
                                        modifier = Modifier.size(48.dp).testTag("bill_duration_dec_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Remove,
                                            contentDescription = "Reduce Month",
                                            modifier = Modifier.size(18.dp)
                                         )
                                    }

                                    // Increase Duration Button (+)
                                    OutlinedIconButton(
                                        onClick = {
                                            val currentDur = inputBillDurationMonths.toIntOrNull() ?: 3
                                            val nextDur = (currentDur + 1).coerceAtMost(48)
                                            inputBillDurationMonths = nextDur.toString()
                                            if (customPaymentsList.size != nextDur) {
                                                customPaymentsList = List(nextDur) { idx ->
                                                    customPaymentsList.getOrNull(idx) ?: inputBillMonthlyPayment
                                                }
                                            }
                                        },
                                        modifier = Modifier.size(48.dp).testTag("bill_duration_inc_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Add Month",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    Checkbox(
                                        checked = inputBillUseDifferentPayments,
                                        onCheckedChange = { inputBillUseDifferentPayments = it },
                                        modifier = Modifier.testTag("bill_use_different_payments_checkbox")
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (langIndo) "Kustomisasi nominal berbeda tiap bulan" else "Customize dynamic monthly payments",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.sp
                                    )
                                }

                                if (inputBillUseDifferentPayments) {
                                    // Make sure customPaymentsList matches durationVal
                                    val safeDuration = durationVal.coerceIn(1, 48)
                                    if (customPaymentsList.size != safeDuration) {
                                        customPaymentsList = List(safeDuration) { idx ->
                                            customPaymentsList.getOrNull(idx) ?: inputBillMonthlyPayment
                                        }
                                    }
                                    if (selectedCustomMonthIdx >= safeDuration) {
                                        selectedCustomMonthIdx = safeDuration - 1
                                    }
                                    if (selectedCustomMonthIdx < 0) {
                                        selectedCustomMonthIdx = 0
                                    }

                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = if (langIndo) "Atur nominal cicilan per bulan:" else "Set monthly payment amounts:",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )

                                        // Beautiful Horizontal Slider representing Month Selection
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalScroll(rememberScrollState())
                                                .padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            customPaymentsList.forEachIndexed { idx, pVal ->
                                                val isSel = selectedCustomMonthIdx == idx
                                                val amtVal = pVal.toDoubleOrNull() ?: 0.0
                                                val formattedAmt = currencyFormatter.format(amtVal)

                                                Card(
                                                    modifier = Modifier
                                                        .width(110.dp)
                                                        .clickable { selectedCustomMonthIdx = idx }
                                                        .testTag("month_select_tab_$idx"),
                                                    shape = RoundedCornerShape(10.dp),
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = if (isSel) {
                                                            MaterialTheme.colorScheme.primaryContainer
                                                        } else {
                                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                                        }
                                                    ),
                                                    border = BorderStroke(
                                                        width = if (isSel) 2.dp else 1.dp,
                                                        color = if (isSel) {
                                                            MaterialTheme.colorScheme.primary
                                                        } else {
                                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                                                        }
                                                    )
                                                ) {
                                                    Column(
                                                        modifier = Modifier.padding(8.dp).fillMaxWidth(),
                                                        horizontalAlignment = Alignment.CenterHorizontally,
                                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                                    ) {
                                                        Text(
                                                            text = "${if (langIndo) "Bulan" else "Month"} ${idx + 1}",
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 11.sp,
                                                            color = if (isSel) {
                                                                MaterialTheme.colorScheme.onPrimaryContainer
                                                            } else {
                                                                MaterialTheme.colorScheme.onSurfaceVariant
                                                            }
                                                        )
                                                        Text(
                                                            text = formattedAmt,
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Medium,
                                                            color = if (isSel) {
                                                                MaterialTheme.colorScheme.primary
                                                            } else {
                                                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                                            },
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        // Active Month Editor Card
                                        val activeIndex = selectedCustomMonthIdx.coerceIn(0, customPaymentsList.size - 1)
                                        val paymentVal = customPaymentsList.getOrNull(activeIndex) ?: ""

                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                                            ),
                                            border = BorderStroke(
                                                width = 1.dp,
                                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                                            )
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(12.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    // Beautiful Month Label Badge
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(MaterialTheme.colorScheme.primaryContainer)
                                                            .padding(horizontal = 8.dp, vertical = 6.dp)
                                                    ) {
                                                        Text(
                                                            text = "${if (langIndo) "Bulan" else "Month"} ${activeIndex + 1}",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                                        )
                                                    }

                                                    OutlinedTextField(
                                                        value = paymentVal,
                                                        onValueChange = { newVal ->
                                                            if (newVal.isEmpty() || newVal.toDoubleOrNull() != null || newVal.all { it.isDigit() || it == '.' }) {
                                                                customPaymentsList = customPaymentsList.toMutableList().apply {
                                                                    this[activeIndex] = newVal
                                                                }
                                                            }
                                                        },
                                                        singleLine = true,
                                                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                                                        modifier = Modifier.weight(1f).testTag("bill_custom_payment_input_$activeIndex")
                                                    )

                                                    // Decrease Button (-)
                                                    OutlinedIconButton(
                                                        onClick = {
                                                            val currentAmt = paymentVal.toDoubleOrNull() ?: 0.0
                                                            val isIDR = currentCurrency.code == "IDR"
                                                            val step = if (isIDR) 50000.0 else 10.0
                                                            val nextAmt = (currentAmt - step).coerceAtLeast(0.0)
                                                            customPaymentsList = customPaymentsList.toMutableList().apply {
                                                                this[activeIndex] = if (nextAmt % 1.0 == 0.0) nextAmt.toLong().toString() else nextAmt.toString()
                                                            }
                                                        },
                                                        modifier = Modifier.size(40.dp).testTag("bill_custom_payment_dec_$activeIndex")
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Remove,
                                                            contentDescription = "Decrease",
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }

                                                    // Increase Button (+)
                                                    OutlinedIconButton(
                                                        onClick = {
                                                            val currentAmt = paymentVal.toDoubleOrNull() ?: 0.0
                                                            val isIDR = currentCurrency.code == "IDR"
                                                            val step = if (isIDR) 50000.0 else 10.0
                                                            val nextAmt = currentAmt + step
                                                            customPaymentsList = customPaymentsList.toMutableList().apply {
                                                                this[activeIndex] = if (nextAmt % 1.0 == 0.0) nextAmt.toLong().toString() else nextAmt.toString()
                                                            }
                                                        },
                                                        modifier = Modifier.size(40.dp).testTag("bill_custom_payment_inc_$activeIndex")
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Add,
                                                            contentDescription = "Increase",
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }

                                                // Quick setting adjustment chips
                                                Row(
                                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    val isIDR = currentCurrency.code == "IDR"
                                                    val stepsList = if (isIDR) {
                                                        listOf(-10000.0, -50000.0, 50000.0, 100000.0)
                                                    } else {
                                                        listOf(-5.0, -10.0, 10.0, 25.0)
                                                    }

                                                    stepsList.forEach { stepVal ->
                                                        val label = if (isIDR) {
                                                            val valueAbs = Math.abs(stepVal).toInt()
                                                            val suffix = if (valueAbs >= 1000) "${valueAbs / 1000}k" else "$valueAbs"
                                                            if (stepVal > 0) "+$suffix" else "-$suffix"
                                                        } else {
                                                            if (stepVal > 0) "+$${stepVal.toInt()}" else "-$${Math.abs(stepVal).toInt()}"
                                                        }

                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(4.dp))
                                                                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                                                                .clickable {
                                                                    val currentAmt = paymentVal.toDoubleOrNull() ?: 0.0
                                                                    val nextAmt = (currentAmt + stepVal).coerceAtLeast(0.0)
                                                                    customPaymentsList = customPaymentsList.toMutableList().apply {
                                                                        this[activeIndex] = if (nextAmt % 1.0 == 0.0) nextAmt.toLong().toString() else nextAmt.toString()
                                                                    }
                                                                }
                                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                                        ) {
                                                            Text(
                                                                text = label,
                                                                fontSize = 9.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            // Remove Last Month Button
                                            TextButton(
                                                onClick = {
                                                    val currentDur = inputBillDurationMonths.toIntOrNull() ?: 3
                                                    if (currentDur > 1) {
                                                        val nextDur = currentDur - 1
                                                        inputBillDurationMonths = nextDur.toString()
                                                        if (customPaymentsList.size != nextDur) {
                                                            customPaymentsList = List(nextDur) { idx ->
                                                                customPaymentsList.getOrNull(idx) ?: inputBillMonthlyPayment
                                                            }
                                                        }
                                                    }
                                                },
                                                colors = ButtonDefaults.textButtonColors(
                                                    contentColor = MaterialTheme.colorScheme.error
                                                ),
                                                modifier = Modifier.weight(1f).testTag("custom_payments_remove_last_btn")
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Remove,
                                                        contentDescription = "Remove Last Month",
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Text(
                                                        text = if (langIndo) "Hapus Bulan" else "Remove Month",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }

                                            // Add Month Button
                                            TextButton(
                                                onClick = {
                                                    val currentDur = inputBillDurationMonths.toIntOrNull() ?: 3
                                                    val nextDur = (currentDur + 1).coerceAtMost(48)
                                                    inputBillDurationMonths = nextDur.toString()
                                                    if (customPaymentsList.size != nextDur) {
                                                        customPaymentsList = List(nextDur) { idx ->
                                                            customPaymentsList.getOrNull(idx) ?: inputBillMonthlyPayment
                                                        }
                                                    }
                                                },
                                                modifier = Modifier.weight(1f).testTag("custom_payments_add_last_btn")
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Add,
                                                        contentDescription = "Add Month",
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Text(
                                                        text = if (langIndo) "Tambah Bulan" else "Add Month",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    OutlinedTextField(
                                        value = inputBillMonthlyPayment,
                                        onValueChange = {
                                            if (it.isEmpty() || it.toDoubleOrNull() != null || it.all { ch -> ch.isDigit() || ch == '.' }) {
                                                inputBillMonthlyPayment = it
                                            }
                                        },
                                        label = { Text(if (langIndo) "Besar Cicilan Per Bulan" else "Monthly Payment Amount") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().testTag("bill_installment_monthly_input")
                                    )
                                }

                                OutlinedTextField(
                                    value = inputBillPaidMonths,
                                    onValueChange = {
                                        if (it.isEmpty() || it.toIntOrNull() != null) {
                                            inputBillPaidMonths = it
                                        }
                                    },
                                    label = { Text(if (langIndo) "Sudah Dibayar (Jumlah Bulan/Kali)" else "Already Paid Month Count") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("bill_installment_paid_input")
                                )

                                Text(
                                    text = if (langIndo) "Frekuensi Pembayaran:" else "Payment Frequency:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                                val freqOptions = listOf("Monthly", "Quarterly", "Semi-Annually")
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    freqOptions.forEach { freq ->
                                        val isSelected = inputBillFrequency == freq
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                                )
                                                .border(
                                                    width = 1.dp,
                                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .clickable { inputBillFrequency = freq }
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = freq,
                                                fontSize = 11.sp,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                val durationNum = inputBillDurationMonths.toIntOrNull() ?: 3
                                val amountVal = inputBillMonthlyPayment.toDoubleOrNull() ?: 300000.0
                                val calculatedTotal = if (inputBillUseDifferentPayments) {
                                    customPaymentsList.sumOf { it.toDoubleOrNull() ?: 0.0 }
                                } else {
                                    amountVal * durationNum
                                }
                                
                                val periodMonths = if (inputBillFrequency == "Semi-Annually") {
                                    durationNum * 6
                                } else if (inputBillFrequency == "Quarterly") {
                                    durationNum * 3
                                } else {
                                    durationNum
                                }

                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = if (langIndo) "🧮 Kalkulator Cicilan Hasil:" else "🧮 Installment Period Calculator:",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = if (langIndo) {
                                                "• Total Periode Kontrak: $periodMonths Bulan\n" +
                                                "• Jumlah Pembayaran: $durationNum kali bayar ($inputBillFrequency)\n" +
                                                (if (inputBillUseDifferentPayments) {
                                                    "• Rincian Cicilan: ${customPaymentsList.mapIndexed { idx, s -> "Bln ${idx+1}: " + currencyFormatter.format(s.toDoubleOrNull() ?: 0.0) }.joinToString(", ")}\n"
                                                } else {
                                                    "• Nominal Cicilan: ${currencyFormatter.format(amountVal)} per kali\n"
                                                }) +
                                                "• Total Tagihan Pokok: ${currencyFormatter.format(calculatedTotal)}"
                                            } else {
                                                "• Total Installment Period: $periodMonths Months\n" +
                                                "• Payments Schedule: $durationNum installments ($inputBillFrequency)\n" +
                                                (if (inputBillUseDifferentPayments) {
                                                    "• Payments Breakdown: ${customPaymentsList.mapIndexed { idx, s -> "Mo ${idx+1}: " + currencyFormatter.format(s.toDoubleOrNull() ?: 0.0) }.joinToString(", ")}\n"
                                                } else {
                                                    "• Billing payment: ${currencyFormatter.format(amountVal)} per payment\n"
                                                }) +
                                                "• Total contract value: ${currencyFormatter.format(calculatedTotal)}"
                                            },
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = inputBillCategory,
                                onValueChange = { inputBillCategory = it },
                                label = { Text(if (currentLang == AppLanguage.INDONESIAN) "Kategori Tagihan" else "Billing Category") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("bill_category_input")
                            )

                            // Quick Categories suggestions chips
                            val quickBillCategories = listOf("Bills", "Entertainment", "Health", "Other")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                quickBillCategories.forEach { cat ->
                                    val isSelected = inputBillCategory.equals(cat, ignoreCase = true)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(100.dp))
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.surfaceVariant
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(100.dp)
                                            )
                                            .clickable { inputBillCategory = cat }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = cat,
                                            fontSize = 11.sp,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val durationVal = inputBillDurationMonths.toIntOrNull() ?: 1
                                val paidVal = inputBillPaidMonths.toIntOrNull() ?: 0
                                
                                val customPaymentsStr = if (inputBillIsInstallment && inputBillUseDifferentPayments) {
                                    customPaymentsList.joinToString(",")
                                } else {
                                    ""
                                }
                                
                                val billAmount = if (inputBillIsInstallment) {
                                    if (inputBillUseDifferentPayments) {
                                        customPaymentsList.sumOf { it.toDoubleOrNull() ?: 0.0 }
                                    } else {
                                        (inputBillMonthlyPayment.toDoubleOrNull() ?: 0.0) * durationVal
                                    }
                                } else {
                                    inputBillAmountStr.toDoubleOrNull() ?: 0.0
                                }
                                
                                val optMonthly = if (inputBillIsInstallment) {
                                    if (inputBillUseDifferentPayments) {
                                        customPaymentsList.firstOrNull()?.toDoubleOrNull() ?: 0.0
                                    } else {
                                        inputBillMonthlyPayment.toDoubleOrNull() ?: 0.0
                                    }
                                } else {
                                    billAmount
                                }
                                
                                if (inputBillName.trim().isNotEmpty() && billAmount > 0.0 && inputBillDueDate.trim().isNotEmpty()) {
                                    if (isEditingBill) {
                                        viewModel.editBill(
                                            id = editingBillId,
                                            name = inputBillName.trim(),
                                            amount = billAmount,
                                            dueDate = inputBillDueDate.trim(),
                                            category = inputBillCategory.trim(),
                                            isPaid = inputBillIsPaid || (paidVal >= durationVal),
                                            isInstallment = inputBillIsInstallment,
                                            durationMonths = durationVal,
                                            paidMonths = paidVal,
                                            frequency = inputBillFrequency,
                                            monthlyPayment = optMonthly,
                                            customPayments = customPaymentsStr
                                        )
                                    } else {
                                        viewModel.addBill(
                                            name = inputBillName.trim(),
                                            amount = billAmount,
                                            dueDate = inputBillDueDate.trim(),
                                            category = inputBillCategory.trim(),
                                            isInstallment = inputBillIsInstallment,
                                            durationMonths = durationVal,
                                            paidMonths = paidVal,
                                            frequency = inputBillFrequency,
                                            monthlyPayment = optMonthly,
                                            customPayments = customPaymentsStr
                                        )
                                    }
                                    showBillDialog = false
                                }
                            },
                            modifier = Modifier.testTag("bill_dialog_confirm")
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
                            if (isEditingBill) {
                                TextButton(
                                    onClick = {
                                        viewModel.deleteBill(editingBillId)
                                        showBillDialog = false
                                    },
                                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFD32F2F)),
                                    modifier = Modifier.testTag("bill_dialog_delete")
                                ) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (currentLang == AppLanguage.INDONESIAN) "Hapus" else "Delete")
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }

                            TextButton(onClick = { showBillDialog = false }) {
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

@Composable
fun IconButtonWithFeedback(
    isChecked: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(32.dp)
            .clip(CircleShape)
            .clickable { onToggle() },
        contentAlignment = Alignment.Center
    ) {
        if (isChecked) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Settled",
                tint = SuccessGreen,
                modifier = Modifier.size(28.dp)
            )
        } else {
            Icon(
                imageVector = Icons.Default.Circle,
                contentDescription = "Pending Unpaid",
                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
