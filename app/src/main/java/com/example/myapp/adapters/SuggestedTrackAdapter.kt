package com.example.myapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.R

data class SuggestedTrackDisplay(val title: String, val artist: String)

class SuggestedTrackAdapter(
    private var tracks: List<SuggestedTrackDisplay>
) : RecyclerView.Adapter<SuggestedTrackAdapter.SuggestedTrackViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestedTrackViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_suggested_track, parent, false)
        return SuggestedTrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: SuggestedTrackViewHolder, position: Int) {
        val track = tracks[position]
        holder.bind(track)
    }

    override fun getItemCount(): Int = tracks.size

    fun updateData(newTracks: List<SuggestedTrackDisplay>) {
        tracks = newTracks
        notifyDataSetChanged()
    }

    class SuggestedTrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val trackNameTextView: TextView = itemView.findViewById(R.id.suggestedTrackNameTextView)
        private val trackArtistTextView: TextView = itemView.findViewById(R.id.suggestedTrackArtistTextView)

        fun bind(track: SuggestedTrackDisplay) {
            trackNameTextView.text = track.title
            trackArtistTextView.text = track.artist
            // itemView.setOnClickListener { onItemClicked(track) } // Can add click listener if needed
        }
    }
}
