package com.example.geonote.ui.detail

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.geonote.R
import com.example.geonote.data.database.AppDatabase
import com.example.geonote.data.repository.TravelRepository
import com.example.geonote.data.api.RetrofitClient
import com.example.geonote.databinding.ActivityDetailBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private val viewModel: DetailViewModel by viewModels {
        val db = AppDatabase.getDatabase(application)
        val repo = TravelRepository(db.travelEntryDao(), RetrofitClient.openMeteoApi, RetrofitClient.nominatimApi)
        object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
                DetailViewModel(repo) as T
        }
    }

    private var entryId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        entryId = intent.getIntExtra("ENTRY_ID", -1)
        if (entryId == -1) {
            finish()
            return
        }

        setupToolbar()
        observeState()
        viewModel.loadEntry(entryId)
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finishAfterTransition() }
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is DetailUiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.tvError.visibility = View.GONE
                    }
                    is DetailUiState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        bindData(state)
                    }
                    is DetailUiState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.tvError.visibility = View.VISIBLE
                        binding.tvError.text = "❌ ${state.message}"
                    }
                }
            }
        }
    }

    private fun bindData(state: DetailUiState.Success) {
        val entry = state.entry
        binding.tvTitle.text = entry.title
        binding.tvDate.text = SimpleDateFormat("dd MMMM yyyy", Locale.FRENCH).format(Date(entry.date))
        binding.tvDescription.text = entry.description
        binding.chipLocation.text = state.locationText
        binding.chipWeather.text = state.weatherText

        // Chargement image optimisé
        binding.ivHero.load(entry.photoUri) {
            placeholder(R.drawable.ic_placeholder)
            error(R.drawable.ic_placeholder)
            crossfade(true)
        }
    }
}