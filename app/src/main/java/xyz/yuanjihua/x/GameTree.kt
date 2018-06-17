package xyz.yuanjihua.x

import android.graphics.Point
import android.util.Log


/**
 * Created by jianli on 2017/11/11
 */
class GameTree:ChessUtil(){
    companion object {
        private val PLAYER_AI=1
        private val PLAYER_MAN=2
        private val PLAYER_RED= PLAYER_AI
        private val PLAYER_BLUE= PLAYER_MAN

        private var DEPTH_MAX=2

    }
    var aiChesses=ArrayList<Point>()
    var manChesses=ArrayList<Point>()
    
    var root=Node(PLAYER_MAN)
    var last=root
    var scoreResult=0
    var chessesArray= intArrayOf(
            1,1,1,1,
            1,0,0,1,
            2,0,0,2,
            2,2,2,2
    )
    

    inner class Node(val player: Int){
        var sons=ArrayList<Node>()
        var parent:Node?=null
        var from:Point?=null
        var to:Point?=null

        fun getScore():Int{
            var score=0
            val killedChesses=GameStateController().eatChessId(chessesArray,to!!,player)
            when(killedChesses.size){
                0->score=0
                1->score=1
                2->score=2
            }
            if(player==2) score=-score
            return score
        }
        fun getParentPlayerType(player: Int):Int{
            if(player== PLAYER_AI) return PLAYER_MAN
            else return PLAYER_AI
        }

    }


    fun buildTree(){
        putAvailableChesses()
        for(i in 1..4){
            val nd=Node(PLAYER_AI)
            nd.parent=Node(nd.getParentPlayerType(nd.player))
            root=putSon(root,nd)
        }

        for(p in root.sons){
            Log.d("d","root.sons:${p.player}")
        }


        for(i in 0..root.sons.size){
            for(p in root.sons[i].sons){
                Log.d("d","root.sons[${i}].sons:${p.player}")
            }
        }


    }



    fun putSon(son:Node){

        root=putSon(root,son)
    }
    private fun getSonSize(parent:Node):Int{
        var size=0
        if(parent.player== PLAYER_AI){
            size=manChesses.size
        }else if(parent.player== PLAYER_MAN){
            size=aiChesses.size
        }
        return size
    }
    private fun isFull(node:Node):Boolean{
        if(node.sons.size<getSonSize(node)) return false
        return true
    }
    private fun putSon(node: Node,son:Node):Node{
        val p=last.parent
        if(isFull(p!!)){

        }




        return node
    }


    //
    fun putAvailableChesses(){
        for(id in 0 until  chessesArray.size){
            when(chessesArray[id]){
                1->{
                    if(isMovable(chessesArray,id)>0) aiChesses.add(id2Point(id))
                }
                2-> {
                    if(isMovable(chessesArray,id)>0) manChesses.add(id2Point(id))
                }
            }
        }

    }


}