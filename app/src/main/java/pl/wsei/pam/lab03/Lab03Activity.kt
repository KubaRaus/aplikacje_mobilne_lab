package pl.wsei.pam.lab03

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.media.MediaPlayer
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.animation.DecelerateInterpolator
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import pl.wsei.pam.lab01.R
import java.util.Random

class Lab03Activity : AppCompatActivity() {
    private lateinit var mBoard: GridLayout
    private lateinit var mBoardModel: MemoryBoardView
    private var isBoardLocked: Boolean = false
    private var isSound: Boolean = true
    private lateinit var completionPlayer: MediaPlayer
    private lateinit var negativePlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_lab03)

        mBoard = findViewById(R.id.main)

        ViewCompat.setOnApplyWindowInsetsListener(mBoard) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val size = intent.getIntArrayExtra("size")
        val rows: Int
        val columns: Int

        if (size != null && size.size >= 2) {
            rows = size[0]
            columns = size[1]
        } else {
            rows = intent.getIntExtra("rows", 3)
            columns = intent.getIntExtra("columns", 3)
        }

        if (savedInstanceState != null) {
            val state = savedInstanceState.getIntArray("game_state")
            val iconMapping = savedInstanceState.getIntArray("icon_mapping")
            mBoardModel = MemoryBoardView(mBoard, columns, rows)
            if (iconMapping != null) {
                mBoardModel.setIconMapping(iconMapping)
            }
            if (state != null) {
                mBoardModel.setState(state)
            }
        } else {
            mBoardModel = MemoryBoardView(mBoard, columns, rows)
        }
        mBoardModel.setOnGameChangeListener { e ->
            if (isBoardLocked) return@setOnGameChangeListener
            when (e.state) {
                GameStates.Matching -> {
                    e.tiles.forEach { it.revealed = true }
                }

                GameStates.Match -> {
                    e.tiles.forEach { it.revealed = true }
                    if (isSound && ::completionPlayer.isInitialized) {
                        completionPlayer.start()
                    }
                    isBoardLocked = true
                    setBoardEnabled(false)
                    val pending = intArrayOf(e.tiles.size)
                    e.tiles.forEach { tile ->
                        animatePairedButton(tile.button) {
                            tile.removeOnClickListener()
                            pending[0] -= 1
                            if (pending[0] == 0) {
                                setBoardEnabled(true)
                                isBoardLocked = false
                            }
                        }
                    }
                }

                GameStates.NoMatch -> {
                    e.tiles.forEach { it.revealed = true }
                    if (isSound && ::negativePlayer.isInitialized) {
                        negativePlayer.start()
                    }
                    isBoardLocked = true
                    setBoardEnabled(false)
                    animateWrongPair(e.tiles.map { it.button }) {
                        e.tiles.forEach { it.revealed = false }
                        setBoardEnabled(true)
                        isBoardLocked = false
                    }
                }

                GameStates.Finished -> {
                    e.tiles.forEach { it.revealed = true }
                    if (isSound && ::completionPlayer.isInitialized) {
                        completionPlayer.start()
                    }
                    isBoardLocked = true
                    setBoardEnabled(false)
                    val pending = intArrayOf(e.tiles.size)
                    e.tiles.forEach { tile ->
                        animatePairedButton(tile.button) {
                            tile.removeOnClickListener()
                            pending[0] -= 1
                            if (pending[0] == 0) {
                                setBoardEnabled(true)
                                isBoardLocked = false
                                Toast.makeText(this, "Game finished", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setBoardEnabled(enabled: Boolean) {
        mBoard.children.forEach { it.isEnabled = enabled }
    }

    private fun animatePairedButton(button: ImageButton, action: () -> Unit) {
        val set = AnimatorSet()
        val random = Random()
        button.pivotX = random.nextFloat() * button.width.coerceAtLeast(1)
        button.pivotY = random.nextFloat() * button.height.coerceAtLeast(1)

        val rotation = ObjectAnimator.ofFloat(button, "rotation", 0f, 1080f)
        val scalingX = ObjectAnimator.ofFloat(button, "scaleX", 1f, 1.25f, 1f)
        val scalingY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 1.25f, 1f)
        val fade = ObjectAnimator.ofFloat(button, "alpha", 1f, 0.35f)

        set.duration = 650
        set.interpolator = DecelerateInterpolator()
        set.playTogether(rotation, scalingX, scalingY, fade)
        set.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                button.rotation = 0f
                button.scaleX = 1f
                button.scaleY = 1f
                button.alpha = 0.35f
                action()
            }
        })
        set.start()
    }

    private fun animateWrongPair(buttons: List<ImageButton>, action: () -> Unit) {
        val set = AnimatorSet()
        val animators = buttons.map { button ->
            ObjectAnimator.ofFloat(button, "rotation", 0f, -12f, 12f, -8f, 8f, 0f)
        }
        set.duration = 450
        set.interpolator = DecelerateInterpolator()
        set.playTogether(animators)
        set.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                buttons.forEach { it.rotation = 0f }
                action()
            }
        })
        set.start()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putIntArray("game_state", mBoardModel.getState())
        outState.putIntArray("icon_mapping", mBoardModel.getIconMapping())
    }

    override fun onResume() {
        super.onResume()
        completionPlayer = MediaPlayer.create(applicationContext, R.raw.completion)
        negativePlayer = MediaPlayer.create(applicationContext, R.raw.negative_guitar)
    }

    override fun onPause() {
        super.onPause()
        if (::completionPlayer.isInitialized) {
            completionPlayer.release()
        }
        if (::negativePlayer.isInitialized) {
            negativePlayer.release()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.board_activity_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.board_activity_sound -> {
                if (isSound) {
                    Toast.makeText(this, "Sound turn off", Toast.LENGTH_SHORT).show()
                    item.setIcon(R.drawable.baseline_volume_off_24)
                    isSound = false
                } else {
                    Toast.makeText(this, "Sound turn on", Toast.LENGTH_SHORT).show()
                    item.setIcon(R.drawable.baseline_volume_up_24)
                    isSound = true
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}