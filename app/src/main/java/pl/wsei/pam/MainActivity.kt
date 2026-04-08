package pl.wsei.pam

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import pl.wsei.pam.lab01.Lab01Activity
import pl.wsei.pam.lab02.Lab02Activity
import pl.wsei.pam.lab03.Lab03Activity
import pl.wsei.pam.lab01.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun onClickMainBtnRunLab01(view: View) {
        startActivity(Intent(this, Lab01Activity::class.java))
    }

    fun onClickMainBtnRunLab02(view: View) {
        startActivity(Intent(this, Lab02Activity::class.java))
    }

    fun onClickMainBtnRunLab03(view: View) {
        startActivity(Intent(this, Lab03Activity::class.java))
    }

    fun onClickMainBtnRunLab06(view: View) {
        startActivity(Intent(this, pl.wsei.pam.lab06.MainActivity::class.java))
    }
}
