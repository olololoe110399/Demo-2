package com.example.demosoundcloud.Request

import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.example.demosoundcloud.Config
import com.example.demosoundcloud.Model.Song
import org.json.JSONArray
import org.json.JSONException
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.*


class SoundcloudApiRequest(private val queue: RequestQueue) {
    interface SoundcloudInterface {
        fun onSuccess(songs: ArrayList<Song>?)
        fun onError(message: String?)
    }

    fun getSongList(query: String, callback: SoundcloudInterface) {
        var query = query
        var url = URL
        if (query.isNotEmpty()) {
            try {
                query = URLEncoder.encode(query, "UTF-8")
                url = "$URL&q=$query"
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }
        }
        Log.d(TAG, "getSongList: $url")

        val request = JsonArrayRequest(
            Request.Method.GET,
            url,
            null,
            Response.Listener { response ->
                Log.d(
                    TAG,
                    "onResponse: $response"
                )
                val songs = ArrayList<Song>()
                if (response.length() > 0) {
                    for (i in 0 until response.length()) {
                        try {
                            val songObject = response.getJSONObject(i)
                            val id = songObject.getLong("id")
                            val title = songObject.getString("title")
                            val artworkUrl = songObject.getString("artwork_url")
                            val streamUrl = songObject.getString("stream_url")
                            val duration = songObject.getLong("duration")
                            val playbackCount =
                                if (songObject.has("playback_count")) songObject.getInt("playback_count") else 0
                            val user = songObject.getJSONObject("user")
                            val artist = user.getString("username")
                            val song = Song(
                                id,
                                title,
                                artist,
                                artworkUrl,
                                duration,
                                streamUrl,
                                playbackCount
                            )
                            songs.add(song)

                        } catch (e: JSONException) {
                            Log.d(
                                TAG,
                                "onResponse1: " + e.message
                            )
                            callback.onError("Xảy ra lỗi")
                            e.printStackTrace()
                        }
                    }
                    callback.onSuccess(songs)
                } else {
                    callback.onError("Không tìm thấy bài hát")
                }
            },
            Response.ErrorListener { error ->
                Log.d(
                    TAG,
                    "onResponse2: " + error.message
                )
                callback.onError("Xảy ra lỗi")
            }
        )
        queue.add<JSONArray>(request)

    }

    companion object {
        private const val URL =
            "https://api.soundcloud.com/tracks?client_id=" + Config.CLIENT_ID
        private const val TAG = "APP"
    }


}