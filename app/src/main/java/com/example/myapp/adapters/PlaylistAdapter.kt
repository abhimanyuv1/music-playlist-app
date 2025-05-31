package com.example.myapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.myapp.R
import com.example.myapp.models.spotify_api.SpotifyPlaylistSimple

class PlaylistAdapter(
    private var playlists: List<SpotifyPlaylistSimple>,
    private val onItemClicked: (SpotifyPlaylistSimple) -> Unit
) : RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist, parent, false)
        return PlaylistViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val playlist = playlists[position]
        holder.bind(playlist, onItemClicked)
    }

    override fun getItemCount(): Int = playlists.size

    fun updateData(newPlaylists: List<SpotifyPlaylistSimple>) {
        playlists = newPlaylists
        notifyDataSetChanged()
    }

    class PlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val playlistImageView: ImageView = itemView.findViewById(R.id.playlistImageView)
        private val playlistNameTextView: TextView = itemView.findViewById(R.id.playlistNameTextView)

        fun bind(playlist: SpotifyPlaylistSimple, onItemClicked: (SpotifyPlaylistSimple) -> Unit) {
            playlistNameTextView.text = playlist.name
            if (playlist.images.isNotEmpty()) {
                playlistImageView.load(playlist.images[0].url) {
                    placeholder(R.drawable.ic_album_placeholder) // Placeholder while loading
                    error(R.drawable.ic_album_placeholder) // Placeholder on error
                }
            } else {
                playlistImageView.load(R.drawable.ic_album_placeholder)
            }
            itemView.setOnClickListener { onItemClicked(playlist) }
        }
    }
}
