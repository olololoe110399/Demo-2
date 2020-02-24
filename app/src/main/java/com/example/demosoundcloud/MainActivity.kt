package com.example.demosoundcloud

import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaPlayer.OnPreparedListener
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.demosoundcloud.Adapter.SongAdapter
import com.example.demosoundcloud.Adapter.SongAdapter.RecyclerItemClickListener
import com.example.demosoundcloud.Model.Song
import com.example.demosoundcloud.Request.SoundcloudApiRequest
import com.example.demosoundcloud.Request.SoundcloudApiRequest.SoundcloudInterface
import com.example.demosoundcloud.Utility.Utility.convertDuration
import com.example.demosoundcloud.VolleySingleton.Companion.getInstance
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar.*
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private val TAG = "APP"
    private var mAdapter: SongAdapter? = null
    private var songList: ArrayList<Song>? = null
    private var currentIndex = 0
    private var mediaPlayer: MediaPlayer? = null
    private var currentSongLength: Long = 0
    private var firstLaunch = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        getSongList("")

        songList = arrayListOf()

        recycler.layoutManager = LinearLayoutManager(applicationContext)
        mAdapter =
            SongAdapter(applicationContext, songList!!, object : RecyclerItemClickListener {
                override fun onClickListener(song: Song?, position: Int) {
                    firstLaunch = false
                    changeSelectedSong(position)
                    prepareSong(song!!)
                }
            })
        recycler.adapter = mAdapter

        //Initialisation du media player
        //Initialisation du media player
        mediaPlayer = MediaPlayer()
        mediaPlayer!!.run {
            setAudioStreamType(AudioManager.STREAM_MUSIC)
            setOnPreparedListener(OnPreparedListener { mp ->
                //Lancer la chanson
                togglePlay(mp)
            })
        }

        handleSeekbar()

        pushPlay()
        pushPrevious()
        pushNext()

        fab_search.setOnClickListener { createDialog() }
    }


    private fun togglePlay(mp: MediaPlayer) = if (mp.isPlaying) {
        mp.stop()
        mp.reset()
        mp.release()
    } else {
        pb_loader.visibility = View.GONE
        tb_title.visibility = View.VISIBLE
        mp.start()
        iv_play.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.selector_pause))

        Handler().postDelayed({
            this.runOnUiThread {
                kotlin.run {  seekbar.max = (currentSongLength / 1000).toInt()
                    val mCurrentPosition = mediaPlayer!!.currentPosition / 1000
                    seekbar.progress = mCurrentPosition
                    tv_time.text = convertDuration(mediaPlayer!!.currentPosition.toLong())
                    Log.i(TAG, "play ") }
            }
        }, 1000L)



    }

    private fun handleSeekbar() {
        seekbar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar,
                progress: Int,
                fromUser: Boolean
            ) {
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer?.seekTo(progress * 1000)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    private fun pushPlay() {
        iv_play.setOnClickListener {
            if (mediaPlayer!!.isPlaying && mediaPlayer != null) {
                iv_play.setImageDrawable(
                    ContextCompat.getDrawable(
                        this@MainActivity,
                        R.drawable.selector_play
                    )
                )
                mediaPlayer!!.pause()
            } else {
                if (firstLaunch) {
                    val song = songList!![0]
                    changeSelectedSong(0)
                    prepareSong(song)
                } else {
                    mediaPlayer!!.start()
                    firstLaunch = false
                }
                iv_play.setImageDrawable(
                    ContextCompat.getDrawable(
                        this@MainActivity,
                        R.drawable.selector_pause
                    )
                )
            }
        }
    }

    private fun pushPrevious() {
        iv_previous.setOnClickListener {
            firstLaunch = false
            if (mediaPlayer != null) {
                if (currentIndex - 1 >= 0) {
                    val previous = songList!![currentIndex - 1]
                    changeSelectedSong(currentIndex - 1)
                    prepareSong(previous)
                } else {
                    changeSelectedSong(songList!!.size - 1)
                    prepareSong(songList!![songList!!.size - 1])
                }
            }
        }
    }

    private fun pushNext() {
        iv_next.setOnClickListener {
            firstLaunch = false
            if (mediaPlayer != null) {
                if (currentIndex + 1 < songList!!.size) {
                    val next = songList!![currentIndex + 1]
                    changeSelectedSong(currentIndex + 1)
                    prepareSong(next)
                } else {
                    changeSelectedSong(0)
                    prepareSong(songList!![0])
                }
            }
        }
    }

    private fun prepareSong(song: Song) {
        currentSongLength = song.duration
        pb_loader.visibility = View.VISIBLE
        tb_title.visibility = View.GONE
        iv_play.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.selector_play))
        tb_title.text = song.title
        tv_time.text = convertDuration(song.duration)
        val stream =
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
//            "https://w.soundcloud.com/player/?url=" + editLink(song.streamUrl)
        mediaPlayer!!.reset()
        Log.i(TAG, stream)
        try {
            mediaPlayer?.apply {
                setDataSource(stream)
                prepareAsync()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun editLink(a: String): String {
        val str1 = a.replace(":", "%3A")
        val str2 = str1.replace(
            "/stream",
            "&amp;auto_play=true&amp;hide_related=true&amp;show_comments=false&amp;show_user=true&amp;show_reposts=false&amp;visual=false&amp;show_artwork=true&amp;allow_redirects=true"
        )
        return str2
    }

    fun createDialog() {
        val builder =
            AlertDialog.Builder(this@MainActivity)
        val view = layoutInflater.inflate(R.layout.dialog_search, null)
        val et_search = view.findViewById<EditText>(R.id.et_search)
        builder.setTitle(R.string.rechercher)
        builder.setView(view)
        builder.setPositiveButton(
            R.string.ok
        ) { _, _ ->
            val search = et_search.text.toString().trim { it <= ' ' }
            if (search.isNotEmpty()) {
                getSongList(search)
            } else {
                Toast.makeText(
                    this@MainActivity,
                    "Vui lòng điền vào trường",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        builder.create().show()
    }

    private fun changeSelectedSong(index: Int) {
        mAdapter!!.notifyItemChanged(mAdapter!!.selectedPosition)
        currentIndex = index
        mAdapter!!.selectedPosition = currentIndex
        mAdapter!!.notifyItemChanged(currentIndex)
    }


    fun getSongList(query: String?) {
        val queue = getInstance(this)!!.requestQueue
        val request = SoundcloudApiRequest(queue!!)
        pb_main_loader.visibility = View.VISIBLE
        request.getSongList(query!!, object : SoundcloudInterface {
            override fun onSuccess(songs: ArrayList<Song>?) {
                currentIndex = 0
                pb_main_loader.visibility = View.GONE
                songList!!.clear()
                songList!!.addAll(songs!!)
                mAdapter!!.notifyDataSetChanged()
                mAdapter!!.selectedPosition = 0
            }

            override fun onError(message: String?) {
                pb_main_loader.visibility = View.GONE
                Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()

    }


}