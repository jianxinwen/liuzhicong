package xyz.yuanjihua.x

import android.graphics.Point

/**
 * Created by jianli on 2017/11/7
 */
open class ChessUtil {
    companion object {
        //
        //根据id求棋子坐标
        //
        fun id2Point(id:Int): Point {
            val root=4.0
            val y= Math.floor(id/root).toInt()
            val x=Math.floor(id%root).toInt()
            return Point(x,y)
        }
        //
        //根据棋子坐标求id
        //
        fun point2Id(point: Point):Int{
            val root=4
            return (point.y*root+point.x)
        }


        //
        fun getTop(p:Point):Point{
            if(p.y==0)return p
            else return Point(p.x,p.y-1)
        }
        fun getBottom(p:Point):Point{
            if(p.y==3)return p
            else return Point(p.x,p.y+1)
        }
        fun getLeft(p: Point):Point{
            if(p.x==0)return p
            else return Point(p.x-1,p.y)
        }
        fun getRight(p:Point):Point{
            if(p.x==3) return p
            else return Point(p.x+1,p.y)
        }

        //判断棋子是否可移动
        //返回可移动的方向个数
        fun isMovable(chessesArray:IntArray,id:Int):Int{

            var count=0
            val p=id2Point(id)

            val top=getTop(p)
            val bottom=getBottom(p)
            val left=getLeft(p)
            val right=getRight(p)

            if(chessesArray[point2Id(top)]==0) count++
            if(chessesArray[point2Id(bottom)]==0) count++
            if(chessesArray[point2Id(left)]==0) count++
            if(chessesArray[point2Id(right)]==0) count++
            return count
        }
    }
}