package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.TransactionEntity
import com.example.data.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

enum class AppLanguage(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    INDONESIAN("id", "Bahasa Indonesia")
}

enum class AppCurrency(val code: String, val symbol: String, val displayName: String, val locale: java.util.Locale) {
    USD("USD", "$", "USD ($)", java.util.Locale.US),
    IDR("IDR", "Rp ", "Rupiah (Rp)", java.util.Locale("id", "ID")),
    EUR("EUR", "€", "Euro (€)", java.util.Locale.GERMANY),
    JPY("JPY", "¥", "Yen (¥)", java.util.Locale.JAPAN)
}

data class UserProfile(
    val name: String = "John Doe",
    val email: String = "john.doe@example.com",
    val initials: String = "JD"
)

fun getTranslation(key: String, lang: AppLanguage): String {
    return when (lang) {
        AppLanguage.INDONESIAN -> when (key) {
            "financial_tracker" -> "Pelacak Keuangan"
            "welcome" -> "Selamat datang kembali,"
            "total_balance" -> "Saldo Total"
            "income" -> "Pemasukan"
            "expense" -> "Pengeluaran"
            "monthly_budget" -> "Anggaran Bulanan"
            "recent_activity" -> "Aktivitas Terbaru"
            "view_all" -> "Semua"
            "add_transaction" -> "Tambah Transaksi"
            "save_transaction" -> "Simpan Transaksi"
            "bills" -> "Tagihan & Subs"
            "savings" -> "Tabungan & Target"
            "reports" -> "Laporan & Grafik"
            "budget" -> "Anggaran"
            "settings" -> "Pengaturan Aplikasi"
            "language" -> "Bahasa"
            "currency" -> "Mata Uang"
            "profile" -> "Profil Akun"
            "reset_data" -> "Hapus Semua Data"
            "reset_confirm" -> "Apakah Anda yakin ingin menghapus semua transaksi?"
            "app_lock" -> "Kunci Aplikasi PIN"
            "save" -> "Simpan"
            "username" -> "Nama Pengguna"
            "email" -> "Alamat Email"
            "paid" -> "Lunas"
            "unpaid" -> "Belum Bayar"
            "due_soon" -> "Jatuh Tempo"
            "over_limit" -> "Melebihi Batas!"
            "left" -> "sisa"
            "spent" -> "pakai"
            "saving_target" -> "Rencana Tabungan"
            "cancel" -> "Batal"
            "pin_enter" -> "Masukkan 4 angka kode PIN keamanan"
            "pin_tip" -> "Petunjuk: Gunakan PIN pengaman '"
            "pin_tip_suffix" -> "' untuk masuk."
            "pin_wrong" -> "PIN tidak cocok. (Gunakan: "
            "all_categories" -> "Semua Kategori"
            "no_transactions" -> "Belum ada transaksi."
            "add_new" -> "Klik tombol + untuk menambah data baru."
            "category" -> "Kategori"
            "date" -> "Tanggal"
            "note" -> "Keterangan / Catatan"
            "optional" -> "Opsional"
            "amount" -> "Nominal"
            "edit_profile_desc" -> "Ubah detail profil akun Anda di bawah ini."
            "app_preferences" -> "Preferensi Aplikasi"
            "security" -> "Keamanan & PIN"
            "reset_all_data" -> "Setel Ulang Data"
            "data_cleared" -> "Data Berhasil Dicuci!"
            "custom_pin" -> "Atur PIN Kunci Kustom"
            "enable_pin_lock" -> "Aktifkan Kunci PIN Aplikasi"
            "new_pin" -> "Pin Baru (4 angka)"
            "profile_updated" -> "Profil berhasil diperbarui!"
            "backup_and_export" -> "Cadangan & Ekspor Data"
            "backup_desc" -> "Unduh riwayat transaksi Anda dalam format CSV atau PDF sebagai salinan cadangan."
            "export_csv" -> "Ekspor format CSV"
            "export_pdf" -> "Ekspor format PDF"
            "export_success" -> "Data berhasil diekspor!"
            "export_failed" -> "Ekspor gagal!"
            else -> key
        }
        AppLanguage.ENGLISH -> when (key) {
            "financial_tracker" -> "Financial Tracker"
            "welcome" -> "Welcome back,"
            "total_balance" -> "Total Balance"
            "income" -> "Income"
            "expense" -> "Expenses"
            "monthly_budget" -> "Monthly Budget"
            "recent_activity" -> "Recent Activity"
            "view_all" -> "View All"
            "add_transaction" -> "Add Transaction"
            "save_transaction" -> "Save Transaction"
            "bills" -> "Bills & Subs"
            "savings" -> "Savings Goals"
            "reports" -> "Reports & Analytics"
            "budget" -> "Budget"
            "settings" -> "App Settings"
            "language" -> "Language"
            "currency" -> "Currency Unit"
            "profile" -> "User Profile"
            "reset_data" -> "Reset All Data"
            "reset_confirm" -> "Are you sure you want to reset all transaction data?"
            "app_lock" -> "Pin Lock Security"
            "save" -> "Save"
            "username" -> "Username"
            "email" -> "Email Address"
            "paid" -> "Paid"
            "unpaid" -> "Unpaid"
            "due_soon" -> "Due Soon"
            "over_limit" -> "Over Limit!"
            "left" -> "left"
            "spent" -> "spent"
            "saving_target" -> "Savings Target"
            "cancel" -> "Cancel"
            "pin_enter" -> "Enter your secure 4-digit security PIN"
            "pin_tip" -> "Quick Tip: Use security PIN '"
            "pin_tip_suffix" -> "' or tap fingerprint icon."
            "pin_wrong" -> "Incorrect PIN. (Tip: Use "
            "all_categories" -> "All Categories"
            "no_transactions" -> "No transactions registered yet."
            "add_new" -> "Click the floating button below to begin adding transactions."
            "category" -> "Category"
            "date" -> "Date"
            "note" -> "Keterangan / Note"
            "optional" -> "Optional"
            "amount" -> "Amount"
            "edit_profile_desc" -> "Modify your account credentials below."
            "app_preferences" -> "App Preferences"
            "security" -> "Security & Core PIN"
            "reset_all_data" -> "Reset App Data"
            "data_cleared" -> "All data reset successfully!"
            "custom_pin" -> "Change Security PIN"
            "enable_pin_lock" -> "Enable Launch Security Lock"
            "new_pin" -> "New PIN (4 digits)"
            "profile_updated" -> "Profile credentials updated!"
            "backup_and_export" -> "Backup & Export Data"
            "backup_desc" -> "Download your financial history in CSV or PDF format for offline backups or printouts."
            "export_csv" -> "Export to CSV"
            "export_pdf" -> "Export to PDF"
            "export_success" -> "Financial data exported successfully!"
            "export_failed" -> "Export failed!"
            else -> key
        }
    }
}

data class BudgetCategory(
    val category: String,
    val limit: Double,
    val colorHex: Long
)

data class SavingGoal(
    val id: Int,
    val name: String,
    val targetAmount: Double,
    val currentSaved: Double,
    val colorHex: Long
)

data class BillSubscription(
    val id: Int,
    val name: String,
    val amount: Double,
    val dueDate: String,
    val category: String,
    val isPaid: Boolean,
    val isInstallment: Boolean = false,
    val durationMonths: Int = 1,
    val paidMonths: Int = 0,
    val frequency: String = "Monthly",
    val monthlyPayment: Double = amount,
    val customPayments: String = ""
)

class FinancialViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = TransactionRepository(db.transactionDao())
    private val prefs = application.getSharedPreferences("financial_app_prefs", Context.MODE_PRIVATE)

    // Current settings attributes persistent stream
    private val _currentLanguage = MutableStateFlow(
        AppLanguage.entries.find { it.code == prefs.getString("settings_language", "en") } ?: AppLanguage.ENGLISH
    )
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    private val _currentCurrency = MutableStateFlow(
        AppCurrency.entries.find { it.code == prefs.getString("settings_currency", "USD") } ?: AppCurrency.USD
    )
    val currentCurrency: StateFlow<AppCurrency> = _currentCurrency.asStateFlow()

    private val _userProfile = MutableStateFlow(
        UserProfile(
            name = prefs.getString("profile_name", "John Doe") ?: "John Doe",
            email = prefs.getString("profile_email", "john.doe@example.com") ?: "john.doe@example.com",
            initials = prefs.getString("profile_initials", "JD") ?: "JD"
        )
    )
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _isLockEnabled = MutableStateFlow(
        prefs.getBoolean("settings_lock_enabled", true)
    )
    val isLockEnabled: StateFlow<Boolean> = _isLockEnabled.asStateFlow()

    private val _securityPin = MutableStateFlow(
        prefs.getString("settings_security_pin", "1234") ?: "1234"
    )
    val securityPin: StateFlow<String> = _securityPin.asStateFlow()

    // App Unlocked State (Security barrier)
    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    private val _pinError = MutableStateFlow<String?>(null)
    val pinError: StateFlow<String?> = _pinError.asStateFlow()

    // Database Reactive Stream
    val transactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // State flows with customized lists
    private val _budgets = MutableStateFlow<List<BudgetCategory>>(emptyList())
    val budgets: StateFlow<List<BudgetCategory>> = _budgets.asStateFlow()

    private val _savingsGoals = MutableStateFlow<List<SavingGoal>>(emptyList())
    val savingsGoals: StateFlow<List<SavingGoal>> = _savingsGoals.asStateFlow()

    private val _bills = MutableStateFlow<List<BillSubscription>>(emptyList())
    val bills: StateFlow<List<BillSubscription>> = _bills.asStateFlow()

    private val _customCategories = MutableStateFlow<List<String>>(emptyList())
    val customCategories: StateFlow<List<String>> = _customCategories.asStateFlow()

    // Persistent storage helpers using robust built-in JSONObject and JSONArray
    private fun loadBudgets(): List<BudgetCategory> {
        val jsonStr = prefs.getString("settings_budgets_json", null)
        if (jsonStr != null) {
            try {
                val array = org.json.JSONArray(jsonStr)
                val list = mutableListOf<BudgetCategory>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        BudgetCategory(
                            category = obj.getString("category"),
                            limit = obj.getDouble("limit"),
                            colorHex = obj.getLong("colorHex")
                        )
                    )
                }
                return list
            } catch (e: Exception) {}
        }
        return listOf(
            BudgetCategory("Food", 1200.0, 0xFF4CAF50), // Green
            BudgetCategory("Transport", 300.0, 0xFF2196F3), // Blue
            BudgetCategory("Shopping", 800.0, 0xFFFF9800), // Orange
            BudgetCategory("Entertainment", 400.0, 0xFF9C27B0), // Purple
            BudgetCategory("Bills", 1500.0, 0xFFF44336), // Red
            BudgetCategory("Health", 500.0, 0xFF00BCD4) // Teal
        )
    }

    private fun saveBudgets(list: List<BudgetCategory>) {
        try {
            val array = org.json.JSONArray()
            for (item in list) {
                val obj = org.json.JSONObject()
                obj.put("category", item.category)
                obj.put("limit", item.limit)
                obj.put("colorHex", item.colorHex)
                array.put(obj)
            }
            prefs.edit().putString("settings_budgets_json", array.toString()).apply()
        } catch (e: Exception) {}
    }

    private fun loadSavingsGoals(): List<SavingGoal> {
        val jsonStr = prefs.getString("settings_goals_json", null)
        if (jsonStr != null) {
            try {
                val array = org.json.JSONArray(jsonStr)
                val list = mutableListOf<SavingGoal>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        SavingGoal(
                            id = obj.getInt("id"),
                            name = obj.getString("name"),
                            targetAmount = obj.getDouble("targetAmount"),
                            currentSaved = obj.getDouble("currentSaved"),
                            colorHex = obj.getLong("colorHex")
                        )
                    )
                }
                return list
            } catch (e: Exception) {}
        }
        return listOf(
            SavingGoal(1, "Emergency Fund", 10000.0, 4500.0, 0xFF00796B),
            SavingGoal(2, "New M4 MacBook Pro", 2500.0, 1200.0, 0xFF303F9F),
            SavingGoal(3, "Japan Trip 2027", 5000.0, 1800.0, 0xFFE64A19)
        )
    }

    private fun saveSavingsGoals(list: List<SavingGoal>) {
        try {
            val array = org.json.JSONArray()
            for (item in list) {
                val obj = org.json.JSONObject()
                obj.put("id", item.id)
                obj.put("name", item.name)
                obj.put("targetAmount", item.targetAmount)
                obj.put("currentSaved", item.currentSaved)
                obj.put("colorHex", item.colorHex)
                array.put(obj)
            }
            prefs.edit().putString("settings_goals_json", array.toString()).apply()
        } catch (e: Exception) {}
    }

    private fun loadBills(): List<BillSubscription> {
        val jsonStr = prefs.getString("settings_bills_json", null)
        if (jsonStr != null) {
            try {
                val array = org.json.JSONArray(jsonStr)
                val list = mutableListOf<BillSubscription>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        BillSubscription(
                            id = obj.getInt("id"),
                            name = obj.getString("name"),
                            amount = obj.getDouble("amount"),
                            dueDate = obj.getString("dueDate"),
                            category = obj.getString("category"),
                            isPaid = obj.getBoolean("isPaid"),
                            isInstallment = obj.optBoolean("isInstallment", false),
                            durationMonths = obj.optInt("durationMonths", 1),
                            paidMonths = obj.optInt("paidMonths", 0),
                            frequency = obj.optString("frequency", "Monthly"),
                            monthlyPayment = obj.optDouble("monthlyPayment", obj.getDouble("amount")),
                            customPayments = obj.optString("customPayments", "")
                        )
                    )
                }
                return list
            } catch (e: Exception) {}
        }
        return listOf(
            BillSubscription(1, "Netflix Premium", 15.99, "2026-06-25", "Entertainment", false),
            BillSubscription(2, "Electricity & Power", 145.50, "2026-06-28", "Bills", false),
            BillSubscription(3, "High-Speed Fiber Internet", 65.00, "2026-07-02", "Bills", true),
            BillSubscription(4, "Spotify Family", 19.99, "2026-07-05", "Entertainment", false),
            BillSubscription(5, "Gym Membership", 50.00, "2026-07-10", "Health", false)
        )
    }

    private fun saveBills(list: List<BillSubscription>) {
        try {
            val array = org.json.JSONArray()
            for (item in list) {
                val obj = org.json.JSONObject()
                obj.put("id", item.id)
                obj.put("name", item.name)
                obj.put("amount", item.amount)
                obj.put("dueDate", item.dueDate)
                obj.put("category", item.category)
                obj.put("isPaid", item.isPaid)
                obj.put("isInstallment", item.isInstallment)
                obj.put("durationMonths", item.durationMonths)
                obj.put("paidMonths", item.paidMonths)
                obj.put("frequency", item.frequency)
                obj.put("monthlyPayment", item.monthlyPayment)
                obj.put("customPayments", item.customPayments)
                array.put(obj)
            }
            prefs.edit().putString("settings_bills_json", array.toString()).apply()
        } catch (e: Exception) {}
    }

    private fun loadCustomCategories(): List<String> {
        val jsonStr = prefs.getString("settings_custom_categories_json", null)
        if (jsonStr != null) {
            try {
                val array = org.json.JSONArray(jsonStr)
                val list = mutableListOf<String>()
                for (i in 0 until array.length()) {
                    list.add(array.getString(i))
                }
                return list
            } catch (e: Exception) {}
        }
        return emptyList()
    }

    private fun saveCustomCategories(list: List<String>) {
        try {
            val array = org.json.JSONArray()
            for (item in list) {
                array.put(item)
            }
            prefs.edit().putString("settings_custom_categories_json", array.toString()).apply()
        } catch (e: Exception) {}
    }

    // Custom categories APIs
    fun addCustomCategory(category: String) {
        val trimmed = category.trim()
        if (trimmed.isNotEmpty() && trimmed !in _customCategories.value) {
            val updated = _customCategories.value + trimmed
            _customCategories.value = updated
            saveCustomCategories(updated)
        }
    }

    fun deleteCustomCategory(category: String) {
        val updated = _customCategories.value.filter { it != category }
        _customCategories.value = updated
        saveCustomCategories(updated)
    }

    // Budget customization APIs
    fun addBudget(category: String, limit: Double, colorHex: Long) {
        val current = _budgets.value.toMutableList()
        val index = current.indexOfFirst { it.category.equals(category, ignoreCase = true) }
        if (index != -1) {
            current[index] = BudgetCategory(category, limit, colorHex)
        } else {
            current.add(BudgetCategory(category, limit, colorHex))
        }
        _budgets.value = current
        saveBudgets(current)
    }

    fun deleteBudget(category: String) {
        val updated = _budgets.value.filter { !it.category.equals(category, ignoreCase = true) }
        _budgets.value = updated
        saveBudgets(updated)
    }

    // Savings customization APIs
    fun addSavingsGoal(name: String, targetAmount: Double, currentSaved: Double, colorHex: Long) {
        val current = _savingsGoals.value.toMutableList()
        val nextId = (current.maxOfOrNull { it.id } ?: 0) + 1
        current.add(SavingGoal(nextId, name, targetAmount, currentSaved, colorHex))
        _savingsGoals.value = current
        saveSavingsGoals(current)
    }

    fun editSavingsGoal(id: Int, name: String, targetAmount: Double, currentSaved: Double, colorHex: Long) {
        val current = _savingsGoals.value.map { goal ->
            if (goal.id == id) {
                SavingGoal(id, name, targetAmount, currentSaved, colorHex)
            } else {
                goal
            }
        }
        _savingsGoals.value = current
        saveSavingsGoals(current)
    }

    fun deleteSavingsGoal(id: Int) {
        val updated = _savingsGoals.value.filter { it.id != id }
        _savingsGoals.value = updated
        saveSavingsGoals(updated)
    }

    // Bills customization APIs
    fun addBill(
        name: String,
        amount: Double,
        dueDate: String,
        category: String,
        isInstallment: Boolean = false,
        durationMonths: Int = 1,
        paidMonths: Int = 0,
        frequency: String = "Monthly",
        monthlyPayment: Double = amount,
        customPayments: String = ""
    ) {
        val current = _bills.value.toMutableList()
        val nextId = (current.maxOfOrNull { it.id } ?: 0) + 1
        current.add(
            BillSubscription(
                id = nextId,
                name = name,
                amount = amount,
                dueDate = dueDate,
                category = category,
                isPaid = false,
                isInstallment = isInstallment,
                durationMonths = durationMonths,
                paidMonths = paidMonths,
                frequency = frequency,
                monthlyPayment = monthlyPayment,
                customPayments = customPayments
            )
        )
        _bills.value = current
        saveBills(current)
    }

    fun editBill(
        id: Int,
        name: String,
        amount: Double,
        dueDate: String,
        category: String,
        isPaid: Boolean,
        isInstallment: Boolean = false,
        durationMonths: Int = 1,
        paidMonths: Int = 0,
        frequency: String = "Monthly",
        monthlyPayment: Double = amount,
        customPayments: String = ""
    ) {
        val current = _bills.value.map { bill ->
            if (bill.id == id) {
                BillSubscription(
                    id = id,
                    name = name,
                    amount = amount,
                    dueDate = dueDate,
                    category = category,
                    isPaid = isPaid,
                    isInstallment = isInstallment,
                    durationMonths = durationMonths,
                    paidMonths = paidMonths,
                    frequency = frequency,
                    monthlyPayment = monthlyPayment,
                    customPayments = customPayments
                )
            } else {
                bill
            }
        }
        _bills.value = current
        saveBills(current)
    }

    fun deleteBill(id: Int) {
        val updated = _bills.value.filter { it.id != id }
        _bills.value = updated
        saveBills(updated)
    }

    init {
        // Load custom active lists
        _budgets.value = loadBudgets()
        _savingsGoals.value = loadSavingsGoals()
        _bills.value = loadBills()
        _customCategories.value = loadCustomCategories()

        // Unlock immediately if PIN lock is disabled
        val lockEnabled = prefs.getBoolean("settings_lock_enabled", true)
        _isUnlocked.value = !lockEnabled

        // Pre-populate data if the Room database is empty to demonstrate immediate visual quality
        viewModelScope.launch {
            val list = repository.allTransactions.first()
            if (list.isEmpty()) {
                seedDemoData()
            }
        }
    }

    private suspend fun seedDemoData() {
        val demoItems = listOf(
            TransactionEntity(
                amount = 4500.00,
                category = "Income",
                date = "2026-06-01",
                note = "Bi-weekly Professional Salary payout",
                isIncome = true,
                timestamp = System.currentTimeMillis() - 86400000L * 18 // 18 days ago
            ),
            TransactionEntity(
                amount = 120.50,
                category = "Food",
                date = "2026-06-15",
                note = "Gourmet grocery shopping haul",
                isIncome = false,
                timestamp = System.currentTimeMillis() - 86400000L * 4 // 4 days ago
            ),
            TransactionEntity(
                amount = 45.00,
                category = "Transport",
                date = "2026-06-17",
                note = "Weekly transit pass / train card reload",
                isIncome = false,
                timestamp = System.currentTimeMillis() - 86400000L * 2 // 2 days ago
            ),
            TransactionEntity(
                amount = 250.00,
                category = "Shopping",
                date = "2026-06-18",
                note = "Leather jacket and desk organizers",
                isIncome = false,
                timestamp = System.currentTimeMillis() - 86400000L * 1 // 1 day ago
            ),
            TransactionEntity(
                amount = 75.00,
                category = "Entertainment",
                date = "2026-06-19",
                note = "Concert tickets and movie event booking",
                isIncome = false,
                timestamp = System.currentTimeMillis() - 3600000L * 6 // 6 hours ago
            ),
            TransactionEntity(
                amount = 12.50,
                category = "Food",
                date = "2026-06-20",
                note = "V60 pour-over coffee and fresh sourdough pastry",
                isIncome = false,
                timestamp = System.currentTimeMillis() - 1800000L // 30 mins ago
            ),
            TransactionEntity(
                amount = 1500.00,
                category = "Income",
                date = "2026-06-15",
                note = "Consulting Gig - Contract UI Milestone",
                isIncome = true,
                timestamp = System.currentTimeMillis() - 86400000L * 5
            )
        )
        for (item in demoItems) {
            repository.insertTransaction(item)
        }
    }

    // Attempt PIN Unlock
    fun attemptUnlock(pin: String): Boolean {
        val storedPin = prefs.getString("settings_security_pin", "1234") ?: "1234"
        return if (pin == storedPin) {
            _isUnlocked.value = true
            _pinError.value = null
            true
        } else {
            val lang = _currentLanguage.value
            _pinError.value = getTranslation("pin_wrong", lang) + " '$storedPin')"
            false
        }
    }

    // Biometric Unlock Trigger
    fun biometricBypass() {
        _isUnlocked.value = true
        _pinError.value = null
    }

    // Add Transaction
    fun addTransaction(amount: Double, category: String, date: String, note: String, isIncome: Boolean) {
        viewModelScope.launch {
            val transaction = TransactionEntity(
                amount = amount,
                category = category,
                date = date,
                note = note,
                isIncome = isIncome,
                timestamp = System.currentTimeMillis()
            )
            repository.insertTransaction(transaction)
        }
    }

    // Delete Transaction
    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    // Update Savings Goal Goal saving target contribution
    fun contributeToGoal(goalId: Int, addAmount: Double) {
        _savingsGoals.value = _savingsGoals.value.map { goal ->
            if (goal.id == goalId) {
                goal.copy(currentSaved = (goal.currentSaved + addAmount).coerceIn(0.0, goal.targetAmount))
            } else {
                goal
            }
        }
    }

    // Toggle Bill paid/unpaid status
    fun toggleBillStatus(billId: Int) {
        val updated = _bills.value.map { bill ->
            if (bill.id == billId) {
                if (bill.isInstallment) {
                    val nextPaid = if (bill.isPaid) {
                        (bill.paidMonths - 1).coerceAtLeast(0)
                    } else {
                        (bill.paidMonths + 1).coerceAtMost(bill.durationMonths)
                    }
                    val nextPaidStatus = nextPaid >= bill.durationMonths
                    bill.copy(isPaid = nextPaidStatus, paidMonths = nextPaid)
                } else {
                    bill.copy(isPaid = !bill.isPaid)
                }
            } else {
                bill
            }
        }
        _bills.value = updated
        saveBills(updated)
    }

    // App Preferences & Settings setters
    fun updateLanguage(lang: AppLanguage) {
        _currentLanguage.value = lang
        prefs.edit().putString("settings_language", lang.code).apply()
    }

    fun updateCurrency(currency: AppCurrency) {
        _currentCurrency.value = currency
        prefs.edit().putString("settings_currency", currency.code).apply()
    }

    fun updateProfile(name: String, email: String) {
        val initials = name.split(" ")
            .filter { it.isNotEmpty() }
            .take(2)
            .map { it.first().uppercase() }
            .joinToString("")
        val finalInitials = if (initials.isEmpty()) "G" else initials

        val updated = UserProfile(name, email, finalInitials)
        _userProfile.value = updated
        prefs.edit()
            .putString("profile_name", name)
            .putString("profile_email", email)
            .putString("profile_initials", finalInitials)
            .apply()
    }

    fun updateLockStatus(enabled: Boolean) {
        _isLockEnabled.value = enabled
        prefs.edit().putBoolean("settings_lock_enabled", enabled).apply()
        if (!enabled) {
            _isUnlocked.value = true
        }
    }

    fun updateSecurityPin(newPin: String) {
        _securityPin.value = newPin
        prefs.edit().putString("settings_security_pin", newPin).apply()
    }

    fun clearAllTransactions() {
        viewModelScope.launch {
            val all = repository.allTransactions.first()
            for (tx in all) {
                repository.deleteTransaction(tx)
            }
        }
    }
}
