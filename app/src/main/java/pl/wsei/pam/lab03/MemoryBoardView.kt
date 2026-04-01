package pl.wsei.pam.lab03

import android.view.Gravity
import android.view.View
import android.widget.GridLayout
import android.widget.ImageButton
import pl.wsei.pam.lab01.R
import java.util.Stack

class MemoryBoardView(
    private val gridLayout: GridLayout,
    private val cols: Int,
    private val rows: Int
) {
    private val tiles: MutableMap<String, Tile> = mutableMapOf()

    // Ikony odkrywanych kart — czyste ikony bez kolorów
    private val icons: List<Int> = listOf(
        R.drawable.baseline_rocket_launch_24,
        R.drawable.ic_rocket,
        R.drawable.baseline_star_24,
        R.drawable.baseline_emoji_emotions_24,
        R.drawable.baseline_info_24,
        R.drawable.baseline_circle_24,
        R.drawable.baseline_favorite_24
    )

    init {
        gridLayout.columnCount = cols
        gridLayout.rowCount = rows

        val pairCount = cols * rows / 2
        val sourceIcons: List<Int> = MutableList(pairCount) { idx ->
            icons[idx % icons.size]
        }

        val shuffledIcons: MutableList<Int> = mutableListOf<Int>().also {
            it.addAll(sourceIcons)
            it.addAll(sourceIcons)
            it.shuffle()
        }

        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val btn = ImageButton(gridLayout.context).also {
                    it.tag = "${row}x${col}"
                    val layoutParams = GridLayout.LayoutParams()
                    layoutParams.width = 0
                    layoutParams.height = 0
                    layoutParams.setGravity(Gravity.CENTER)
                    layoutParams.columnSpec = GridLayout.spec(col, 1, 1f)
                    layoutParams.rowSpec = GridLayout.spec(row, 1, 1f)
                    it.layoutParams = layoutParams
                    gridLayout.addView(it)
                }
                val tileRes = shuffledIcons.removeAt(0)
                addTile(btn, tileRes)
            }
        }
    }

    private val deckResource: Int = R.drawable.deck
    private var onGameChangeStateListener: (MemoryGameEvent) -> Unit = { _ -> }
    private val matchedPair: Stack<Tile> = Stack()
    private val logic: MemoryGameLogic = MemoryGameLogic(cols * rows / 2)

    private fun onClickTile(v: View) {
        val key = v.tag?.toString() ?: return
        val tile = tiles[key] ?: return
        tile.revealed = true
        matchedPair.push(tile)
        val matchResult = logic.process {
            tile.tileResource
        }
        onGameChangeStateListener(MemoryGameEvent(matchedPair.toList(), matchResult))
        if (matchResult != GameStates.Matching) {
            matchedPair.clear()
        }
    }

    fun setOnGameChangeListener(listener: (event: MemoryGameEvent) -> Unit) {
        onGameChangeStateListener = listener
    }

    private fun addTile(button: ImageButton, resourceImage: Int) {
        button.setOnClickListener(::onClickTile)
        val tile = Tile(button, resourceImage, deckResource)
        tiles[button.tag.toString()] = tile
    }

    fun getState(): IntArray {
        val state = IntArray(cols * rows)
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val key = "${row}x${col}"
                val tile = tiles[key]
                val index = row * cols + col
                state[index] = if (tile?.revealed == true) tile.tileResource else -1
            }
        }
        return state
    }

    fun setState(state: IntArray) {
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val key = "${row}x${col}"
                val tile = tiles[key]
                val index = row * cols + col
                if (index < state.size && tile != null) {
                    tile.revealed = state[index] != -1
                }
            }
        }
    }

    fun getIconMapping(): IntArray {
        val mapping = IntArray(cols * rows)
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val key = "${row}x${col}"
                val tile = tiles[key]
                val index = row * cols + col
                mapping[index] = tile?.tileResource ?: -1
            }
        }
        return mapping
    }

    fun setIconMapping(mapping: IntArray) {
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val key = "${row}x${col}"
                val tile = tiles[key]
                val index = row * cols + col
                if (index < mapping.size && tile != null && mapping[index] != -1) {
                    tile.button.tag = "${row}x${col}"
                    val newTile = Tile(tile.button, mapping[index], deckResource)
                    tiles[key] = newTile
                }
            }
        }
    }
}
