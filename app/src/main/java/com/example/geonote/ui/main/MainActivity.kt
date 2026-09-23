package com.example.geonote.ui.main

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.geonote.R
import com.example.geonote.data.database.AppDatabase
import com.example.geonote.data.repository.TravelRepository
import com.example.geonote.data.api.RetrofitClient
import com.example.geonote.databinding.ActivityMainBinding
import com.example.geonote.ui.add.AddEntryActivity
import com.example.geonote.ui.detail.DetailActivity
import com.example.geonote.ui.settings.SettingsActivity
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: TravelAdapter

    // Injection manuelle du ViewModel
    private val viewModel: MainViewModel by viewModels {
        val db = AppDatabase.getDatabase(application)
        val repo = TravelRepository(
            dao = db.travelEntryDao(),
            weatherApi = RetrofitClient.openMeteoApi,
            locationApi = RetrofitClient.nominatimApi
        )
        MainViewModel.Factory(repo)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, R.anim.slide_in_right, R.anim.slide_out_left)
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupFab()
        observeViewModel()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.btnSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupRecyclerView() {
        adapter = TravelAdapter(
            onItemClick = { entry ->
                val intent = Intent(this, DetailActivity::class.java).apply {
                    putExtra("ENTRY_ID", entry.id)
                }
                startActivity(intent)
            },
            onLongClick = { entry ->
                // Suppression avec confirmation
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Supprimer ?")
                    .setMessage("Voulez-vous supprimer \"${entry.title}\" ?")
                    .setPositiveButton("Oui") { _, _ ->
                        viewModel.deleteEntry(entry)
                        Toast.makeText(this, "Supprimé", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Non", null)
                    .show()
                true
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
            // Animation d'apparition
            itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator().apply {
                addDuration = 300
                changeDuration = 300
            }
        }
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            val intent = Intent(this, AddEntryActivity::class.java)
            addEntryLauncher.launch(intent)
        }
    }

    private val addEntryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            // La liste se mettra à jour automatiquement via Flow/Room
            Toast.makeText(this, "🔄 Liste actualisée", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is UiState.Loading -> showLoading(true)
                    is UiState.Empty -> {
                        showLoading(false)
                        showEmpty(true)
                    }
                    is UiState.Success -> {
                        showLoading(false)
                        showEmpty(false)
                        adapter.submitList(state.entries)
                    }
                    is UiState.Error -> {
                        showLoading(false)
                        Toast.makeText(this@MainActivity, "Erreur: ${state.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
        binding.recyclerView.visibility = if (show) android.view.View.GONE else android.view.View.VISIBLE
    }

    private fun showEmpty(show: Boolean) {
        binding.emptyState.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
    }
}