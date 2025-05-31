package com.example.myapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.myapp.R
import com.example.myapp.models.spotify_api.SpotifyTrackFull

class SavedTrackAdapter(
    private var tracks: List<SpotifyTrackFull>,
    private val onItemClicked: (SpotifyTrackFull) -> Unit
) : RecyclerView.Adapter<SavedTrackAdapter.TrackViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_saved_track, parent, false)
        return TrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val track = tracks[position]
        holder.bind(track, onItemClicked)
    }

    override fun getItemCount(): Int = tracks.size

    fun updateData(newTracks: List<SpotifyTrackFull>) {
        tracks = newTracks
        notifyDataSetChanged()
    }

    class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val trackAlbumArtImageView: ImageView = itemView.findViewById(R.id.trackAlbumArtImageView)
        private val trackNameTextView: TextView = itemView.findViewById(R.id.trackNameTextView)
        private val trackArtistTextView: TextView = itemView.findViewById(R.id.trackArtistTextView)

        fun bind(track: SpotifyTrackFull, onItemClicked: (SpotifyTrackFull) -> Unit) {
            trackNameTextView.text = track.name
            trackArtistTextView.text = track.artists.joinToString(", ") { it.name }

            if (track.album.images.isNotEmpty()) {
                trackAlbumArtImageView.load(track.album.images[0].url) {
                    placeholder(R.drawable.ic_album_placeholder)
                    error(R.drawable.ic_album_placeholder)
                }
            } else {
                trackAlbumArtImageView.load(R.drawable.ic_album_placeholder)
            }
            itemView.setOnClickListener { onItemClicked(track) }
        }
    }
}
