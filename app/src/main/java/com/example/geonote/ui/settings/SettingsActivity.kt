package com.example.geonote.ui.settings

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.geonote.R
import com.example.geonote.data.repository.AuthRepository
import com.example.geonote.databinding.ActivitySettingsBinding
import com.example.geonote.ui.auth.LoginActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var viewModel: SettingsViewModel

    // Demande permission notifications Android 13+
    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.setNotificationsEnabled(true)
            Toast.makeText(this, "Rappels activés", Toast.LENGTH_SHORT).show()
        } else {
            binding.switchNotifications.isChecked = false
            Toast.makeText(this, "Permission refusée", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = SettingsViewModel(applicationContext)
        setupUI()
        loadPreferences()
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.btnLogout.setOnClickListener {
            AuthRepository().signOut()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
        // Switch Notifications
        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Demande permission POST_NOTIFICATIONS si Android 13+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                        return@setOnCheckedChangeListener
                    }
                }
                // Vérifie permission SCHEDULE_EXACT_ALARM si Android 12+
                checkAndRequestExactAlarmPermission()

                viewModel.setNotificationsEnabled(true)
                Toast.makeText(this, "Rappels activés", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.setNotificationsEnabled(false)
                Toast.makeText(this, "Rappels désactivés", Toast.LENGTH_SHORT).show()
            }
        }

        // Switch Dark Mode (simulation)
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setDarkModeEnabled(isChecked)
            Toast.makeText(this, "Redémarrez l'app pour appliquer le thème", Toast.LENGTH_SHORT).show()
        }

        // Bouton effacer données
        binding.btnClearData.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Attention")
                .setMessage("Voulez-vous vraiment effacer TOUTES les données de l'application ?")
                .setPositiveButton("Oui") { _, _ ->
                    viewModel.clearAllData()
                    // Suppression Room via Repository (à implémenter si besoin)
                    Toast.makeText(this, "Données effacées", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Non", null)
                .show()
        }
    }

    private fun loadPreferences() {
        binding.switchNotifications.isChecked = viewModel.areNotificationsEnabled()
        binding.switchDarkMode.isChecked = viewModel.isDarkModeEnabled()
    }

    private fun checkAndRequestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                // Redirige vers les paramètres système
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = android.net.Uri.fromParts("package", packageName, null)
                }
                startActivity(intent)
            }
        }
    }
}