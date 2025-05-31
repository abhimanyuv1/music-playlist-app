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
import java.util.concurrent.TimeUnit

class PlaylistTrackAdapter(
    private var tracks: List<SpotifyTrackFull>,
    private val onItemClicked: (SpotifyTrackFull) -> Unit
) : RecyclerView.Adapter<PlaylistTrackAdapter.PlaylistTrackViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistTrackViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist_track, parent, false)
        return PlaylistTrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaylistTrackViewHolder, position: Int) {
        val track = tracks[position]
        holder.bind(track, onItemClicked)
    }

    override fun getItemCount(): Int = tracks.size

    fun updateData(newTracks: List<SpotifyTrackFull>) {
        tracks = newTracks
        notifyDataSetChanged()
    }

    class PlaylistTrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val albumArtImageView: ImageView = itemView.findViewById(R.id.playlistTrackAlbumArtImageView)
        private val trackNameTextView: TextView = itemView.findViewById(R.id.playlistTrackNameTextView)
        private val artistTextView: TextView = itemView.findViewById(R.id.playlistTrackArtistTextView)
        private val durationTextView: TextView = itemView.findViewById(R.id.playlistTrackDurationTextView)

        fun bind(track: SpotifyTrackFull, onItemClicked: (SpotifyTrackFull) -> Unit) {
            trackNameTextView.text = track.name
            val artistAlbumText = "${track.artists.joinToString { it.name }} - ${track.album.name}"
            artistTextView.text = artistAlbumText

            if (track.album.images.isNotEmpty()) {
                albumArtImageView.load(track.album.images[0].url) {
                    placeholder(R.drawable.ic_album_placeholder)
                    error(R.drawable.ic_album_placeholder)
                }
            } else {
                albumArtImageView.load(R.drawable.ic_album_placeholder)
            }

            // Format duration from ms to M:SS
            val minutes = TimeUnit.MILLISECONDS.toMinutes(track.duration_ms.toLong())
            val seconds = TimeUnit.MILLISECONDS.toSeconds(track.duration_ms.toLong()) % 60
            durationTextView.text = String.format("%d:%02d", minutes, seconds)

            itemView.setOnClickListener { onItemClicked(track) }
        }
    }
}
