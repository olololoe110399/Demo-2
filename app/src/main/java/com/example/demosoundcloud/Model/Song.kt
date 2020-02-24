package com.example.demosoundcloud.Model

import androidx.annotation.NonNull
import kotlin.Comparable

data class Song(
    var id: Long,
    var title: String,
    var artist: String,
    var artworkUrl: String,
    var duration: Long,
    var streamUrl: String,
    var playbackCount: Int
) : Comparable<Song> {
    override fun compareTo(@NonNull other: Song): Int {
        return other.playbackCount - playbackCount
    }


}



