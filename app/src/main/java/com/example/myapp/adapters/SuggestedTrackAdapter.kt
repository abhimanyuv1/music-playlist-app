package com.example.myapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.R
import com.example.myapp.models.spotify_api.SpotifyTrackFull // Import the correct model

class SuggestedTrackAdapter(
    private var tracks: List<SpotifyTrackFull>, // Use SpotifyTrackFull
    private val onItemClicked: (SpotifyTrackFull) -> Unit // Click listener lambda
) : RecyclerView.Adapter<SuggestedTrackAdapter.SuggestedTrackViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestedTrackViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_suggested_track, parent, false)
        return SuggestedTrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: SuggestedTrackViewHolder, position: Int) {
        val track = tracks[position]
        holder.bind(track, onItemClicked) // Pass the click listener to bind
    }

    override fun getItemCount(): Int = tracks.size

    fun updateData(newTracks: List<SpotifyTrackFull>) {
        tracks = newTracks
        notifyDataSetChanged()
    }

    class SuggestedTrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val trackNameTextView: TextView = itemView.findViewById(R.id.suggestedTrackNameTextView)
        private val trackArtistTextView: TextView = itemView.findViewById(R.id.suggestedTrackArtistTextView)

        fun bind(track: SpotifyTrackFull, onItemClicked: (SpotifyTrackFull) -> Unit) { // Bind SpotifyTrackFull
            trackNameTextView.text = track.name
            trackArtistTextView.text = track.artists.joinToString(", ") { it.name }
            itemView.setOnClickListener { onItemClicked(track) } // Set the click listener on the item view
        }
    }
}
