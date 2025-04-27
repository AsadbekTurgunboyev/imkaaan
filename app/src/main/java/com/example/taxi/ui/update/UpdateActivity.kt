package com.example.taxi.ui.update

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.taxi.databinding.ActivityUpdateBinding

class UpdateActivity : AppCompatActivity() {

    lateinit var viewBinding: ActivityUpdateBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityUpdateBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        viewBinding.updatingButton.setOnClickListener {
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=$packageName")
                    )
                )
            } catch (e: android.content.ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                    )
                )
            }

        }


    }


    companion object {

        private const val IS_PROMPT_MODE = "IS_PROMPT_MODE"

        fun open(
            activity: Activity
        ) {
            val intent = Intent(activity, UpdateActivity::class.java)
            activity.startActivity(intent)
        }
    }
}