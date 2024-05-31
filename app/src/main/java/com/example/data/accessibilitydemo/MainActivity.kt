package com.example.data.accessibilitydemo

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.data.accessibilitydemo.view.ArtBlock
import com.example.data.accessibilitydemo.view.OnArtBlockClickListener
import com.example.data.accessibilitydemo.view.SecureTestView

class MainActivity : AppCompatActivity() {

    private val secureTestView by lazy { findViewById<SecureTestView>(R.id.secure) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        //阻止secureTestView在非安全显示器上显示以及被截图
        secureTestView.setSecure(true)
        secureTestView.onArtBlockClickListener = object: OnArtBlockClickListener{
            override fun onArtBlockClick(artBlock: ArtBlock) {
                Toast.makeText(this@MainActivity, "点击了方块：${artBlock.charText}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}