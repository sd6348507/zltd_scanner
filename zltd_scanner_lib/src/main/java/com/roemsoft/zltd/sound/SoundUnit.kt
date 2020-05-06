package com.roemsoft.zltd.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.util.SparseIntArray
import com.roemsoft.zltd.R

object SoundUnit {

    const val SOUND_TYPE_SUCCESS = 0
    const val SOUND_TYPE_WARNING = 1
    const val SOUND_TYPE_QUERY = 2

    private var mSoundPool: SoundPool? = null
    private var mSoundMap = SparseIntArray()

    var enablePlay: Boolean = true

    fun init(context: Context, successId: Int = R.raw.success, warningId: Int = R.raw.warning, queryId: Int = R.raw.query) {
        if (mSoundPool == null) {
            loadSoundResource(
                context,
                successId,
                warningId,
                queryId
            )
        }
    }

    private fun playSound(type: Int) {
        if (!enablePlay) {
            return
        }
        mSoundPool?.play(mSoundMap[type], 1.0f, 1.0f, 1, 0, 1.0f)
    }

    fun success() {
        playSound(SOUND_TYPE_SUCCESS)
    }

    fun warning() {
        playSound(SOUND_TYPE_WARNING)
    }

    fun query() {
        playSound(SOUND_TYPE_QUERY)
    }

    fun release() {
        mSoundPool?.release()
        mSoundPool = null
        mSoundMap.clear()
    }

    private fun loadSoundResource(context: Context, successId: Int, warningId: Int, queryId: Int) {
        release()
        if (mSoundPool == null) {
            mSoundPool = SoundPool.Builder().apply {
                setMaxStreams(1)
                setAudioAttributes(AudioAttributes.Builder().apply { setLegacyStreamType(AudioManager.STREAM_RING) }.build())
            }.build()
        }
        mSoundPool?.let {
            mSoundMap.put(SOUND_TYPE_SUCCESS, it.load(context, successId, 1))
            mSoundMap.put(SOUND_TYPE_WARNING, it.load(context, warningId, 1))
            mSoundMap.put(SOUND_TYPE_QUERY, it.load(context, queryId, 1))
        }
    }



}