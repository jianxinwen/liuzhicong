package xyz.yuanjihua.x

import android.app.Activity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : Activity() {

    private lateinit var chessView:ChessView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        chessView=findViewById(R.id.chessView)
        play_again.setOnClickListener {
            chessView.playAgain()
        }
        regret.setOnClickListener{
            GameTree().buildTree()
        }


    }
}
