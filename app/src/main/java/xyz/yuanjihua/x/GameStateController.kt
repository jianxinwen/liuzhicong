package xyz.yuanjihua.x

import android.content.Context
import android.graphics.Point

/**
 * Created by jianli on 2017/11/7
 */
class GameStateController{




    private val kill1Matrix= arrayListOf<String>(//红方被击杀的情况
            "0122","0212","0121","0221",
            "2210","2120","1210","1220")
    private val kill2Matrix= arrayListOf<String>(//蓝方被击杀的情况
            "0211","0121","0212","0112",
            "1120","1210","2120","2110"
    )

    companion object {
        private var countOfRedChesses=6
        private var countOfBlueChesses=6
        fun reset(){
            countOfBlueChesses=6
            countOfRedChesses=6
        }
    }
    //
    //判断是否有吃棋
    //
    //@params chessesArr:棋子数组，p：落子位置，player：玩家
    //@return 被击杀棋子的id
    //
    fun eatChessId(chessesArr:IntArray, p: Point, player:Int):IntArray{
        val rowIds=IntArray(4)
        val colIds=IntArray(4)
        val rIntArray= ArrayList<Int>()
        var rowString:String=""//横向棋子排列字符串表示
        var colString:String=""//纵向棋子排列字符串表示
        for(i in 0..3){
            rowIds[i]=p.y*4+i
            colIds[i]=p.x+4*i
        }
        for(i in 0..3){
            rowString+=chessesArr[rowIds[i]].toString()
            colString+=chessesArr[colIds[i]].toString()
        }

        when(player){
            1->{//判断红方是否可以击杀蓝方棋子
                if(kill2Matrix.contains(rowString)){
                    for(id in rowIds) if(chessesArr[id]==2) rIntArray.add(id)
                }
                if(kill2Matrix.contains(colString)){
                    for (id in colIds) if(chessesArr[id]==2) rIntArray.add(id)
                }
                countOfBlueChesses-=rIntArray.size
            }
            2->{//判断蓝方是否可以击杀红方棋子
                if(kill1Matrix.contains(rowString)){
                    for(id in rowIds) if(chessesArr[id]==1) rIntArray.add(id)
                }
                if(kill1Matrix.contains(colString)){
                    for (id in colIds) if(chessesArr[id]==1) rIntArray.add(id)
                }
                countOfRedChesses-=rIntArray.size
            }
        }
        return rIntArray.toIntArray()
    }

    //
    //判断游戏是否结束
    //
    //
    fun isGameOver(rb:Int):Boolean{
        //简单判断
        return (countOfBlueChesses==0||countOfRedChesses==0)
    }
    //
    //判断谁赢了
    //
    //
    fun whoWin():Int{
        var ww=0
        if(countOfRedChesses==0) ww=2
        if(countOfBlueChesses==0) ww=1
        return ww
    }

}