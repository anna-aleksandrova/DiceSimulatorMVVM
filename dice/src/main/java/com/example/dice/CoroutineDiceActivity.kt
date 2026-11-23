package com.example.dice

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.dice.databinding.ActivityMainBinding

class CoroutineDiceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: DiceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val diceImageViews: List<ImageView> = listOf(
            binding.die1,
            binding.die2,
            binding.die3,
            binding.die4,
            binding.die5
        )

        binding.rollButton.setOnClickListener {
            viewModel.rollOrRestartDice()
        }

        viewModel.diceValues.observe(this) { values ->
            values.forEachIndexed { index, value ->
                if (index < diceImageViews.size) {
                    diceImageViews[index].setImageResource(getDiceDrawableRes(value))
                }
            }
        }

        viewModel.buttonText.observe(this) { label ->
            binding.rollButton.text = label
        }
    }

    private fun getDiceDrawableRes(value: Int): Int {
        return when (value) {
            1 -> R.drawable.die_1
            2 -> R.drawable.die_2
            3 -> R.drawable.die_3
            4 -> R.drawable.die_4
            5 -> R.drawable.die_5
            else -> R.drawable.die_6
        }
    }
}
