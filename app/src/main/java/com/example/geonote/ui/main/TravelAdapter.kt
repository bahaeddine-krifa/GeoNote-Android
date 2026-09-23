package com.example.geonote.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.geonote.R
import com.example.geonote.data.entity.TravelEntry
import com.example.geonote.databinding.ItemTravelEntryBinding
import java.text.SimpleDateFormat
import java.util.*

class TravelAdapter(
    private val onItemClick: (TravelEntry) -> Unit,
    private val onLongClick: (TravelEntry) -> Boolean
) : ListAdapter<TravelEntry, TravelAdapter.TravelViewHolder>(EntryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TravelViewHolder {
        val binding = ItemTravelEntryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TravelViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TravelViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TravelViewHolder(
        private val binding: ItemTravelEntryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.FRENCH)

        fun bind(entry: TravelEntry) {
            // Titre & description
            binding.tvTitle.text = entry.title
            binding.tvDescription.text = entry.description.take(80) + if (entry.description.length > 80) "..." else ""

            // Date formatée
            binding.tvDate.text = SimpleDateFormat("dd MMM yyyy", Locale.FRENCH).format(Date(entry.date))

            //  Ville : afficher cityName si disponible, sinon fallback sur coordonnées
            val locationText = entry.cityName ?:
            if (entry.latitude != null && entry.longitude != null) {
                "📍 ${String.format("%.2f", entry.latitude)}, ${String.format("%.2f", entry.longitude)}"
            } else "📍 Non localisé"
            binding.chipLocation.text = locationText

            //  Météo : afficher weatherInfo si disponible
            binding.tvWeather.text = entry.weatherInfo ?: "⏳ --°C"

            // Image avec Coil
            binding.ivPhoto.load(entry.photoUri) {
                placeholder(R.drawable.ic_placeholder)
                error(R.drawable.ic_placeholder)
                crossfade(true)
            }

            // Clicks
            binding.root.setOnClickListener { onItemClick(entry) }
            binding.root.setOnLongClickListener { onLongClick(entry) }
        }
    }
}

// DiffUtil pour animations fluides
private class EntryDiffCallback : DiffUtil.ItemCallback<TravelEntry>() {
    override fun areItemsTheSame(oldItem: TravelEntry, newItem: TravelEntry) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: TravelEntry, newItem: TravelEntry) = oldItem == newItem
}