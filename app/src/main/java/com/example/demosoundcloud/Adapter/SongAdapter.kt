package com.example.demosoundcloud.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.demosoundcloud.Adapter.SongAdapter.SongViewHolder
import com.example.demosoundcloud.Model.Song
import com.example.demosoundcloud.R
import com.example.demosoundcloud.Utility.Utility
import com.squareup.picasso.Picasso
import java.util.*
import kotlin.collections.ArrayList

class SongAdapter(
    private val context: Context,
    private val songList: ArrayList<Song>,
    private val listener: RecyclerItemClickListener
) : RecyclerView.Adapter<SongViewHolder>() {
    var selectedPosition = 0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.song_row, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songList[position]
        if (selectedPosition == position) {
            holder.itemView.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.colorPrimary
                )
            )
            holder.iv_play_active.visibility = View.VISIBLE
        } else {
            holder.itemView.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    android.R.color.transparent
                )
            )
            holder.iv_play_active.visibility = View.INVISIBLE
        }
        holder.tv_title.text = song.title
        holder.tv_artist.text = song.artist
        val duration: String = Utility.convertDuration(song.duration)
        holder.tv_duration.text = duration
        Picasso.get().load(song.artworkUrl).placeholder(R.drawable.music_placeholder)
            .into(holder.iv_artwork)
        holder.bind(song, listener)
    }

    override fun getItemCount(): Int {
        return songList.size
    }

    class SongViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
         val tv_title: TextView = itemView.findViewById<View>(R.id.tv_title) as TextView
        val tv_artist: TextView = itemView.findViewById<View>(R.id.tv_artist) as TextView
        val tv_duration: TextView = itemView.findViewById<View>(R.id.tv_duration) as TextView
        val iv_artwork: ImageView = itemView.findViewById<View>(R.id.iv_artwork) as ImageView
        val iv_play_active: ImageView = itemView.findViewById<View>(R.id.iv_play_active) as ImageView
        fun bind(song: Song?, listener: RecyclerItemClickListener) {
            itemView.setOnClickListener { listener.onClickListener(song, layoutPosition) }
        }

    }

    interface RecyclerItemClickListener {
        fun onClickListener(song: Song?, position: Int)
    }

}