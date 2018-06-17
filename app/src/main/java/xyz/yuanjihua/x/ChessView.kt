package xyz.yuanjihua.x

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast

/**
 * Created by jianli on 2017/11/7
 */
class ChessView:View {
    constructor(context: Context):super(context)
    constructor(context: Context, attr: AttributeSet):super(context,attr)
    constructor(context: Context, attr: AttributeSet, defStyle:Int):super(context,attr,defStyle)
    private  val paint: Paint//画笔
    private  var canvas: Canvas = Canvas()


    private var blankBitmap: Bitmap = Bitmap.createBitmap(1,1, Bitmap.Config.ARGB_4444)//空白棋子图片

    private  var redBitmap: Bitmap//红方棋子图片
    private var selectedRedBitmap: Bitmap//选中状态图片
    private lateinit var redChesses:Array<Point?>//红方棋子总数

    private  var blueBitmap: Bitmap//蓝方棋子图片
    private var selectedBlueBitmap: Bitmap//选中状态图片
    private lateinit var blueChesses:Array<Point?>//蓝方棋子总数

    private  var chessWidth=0f//棋子尺寸

    private var startX=0f//棋盘左上角x坐标
    private var startY=0f//棋盘左上角y坐标

    private var isDark=false//是否选中
    private var darkChessID:Int=-1//选中棋子的id
    private var fromChessPoint: Point = Point(-1,-1)//选中棋子的坐标
    private var toChessPoint: Point = Point(-1,-1)//棋子目的坐标

    private var player=0//

    //游戏状态控制器
    val gsc=GameStateController()

    //棋锁
    //哪方先解锁哪方先开局
    //默认蓝棋先解锁
    private var isLockedRed=true
    private var isLockedBlue=false

    //悔棋
    private var withdraw=Array<Point?>(2,{ _->null})

    var chessesArray= intArrayOf(//棋子布局
            1,1,1,1,
            1,0,0,1,
            2,0,0,2,
            2,2,2,2
    )
    private lateinit var chessTouchRectFArray: Array<RectF>//棋子可触摸区



    private  var panelWidth:Int=0//棋盘尺寸
    private var lineHeight:Float=0f//格子尺寸
    private val MAX_LINE=4//

    //
    //初始化
    //
    init {
        paint= Paint()
        with(paint){
            isAntiAlias=true
            isDither=true
            color= Color.BLACK
            style= Paint.Style.STROKE
        }

        redBitmap= BitmapFactory.decodeResource(resources,R.drawable.huaji)
        selectedRedBitmap= BitmapFactory.decodeResource(resources,R.drawable.huajiselected)
        blueBitmap= BitmapFactory.decodeResource(resources,R.drawable.rabbit)
        selectedBlueBitmap= BitmapFactory.decodeResource(resources,R.drawable.rabbitselected)

    }
    //
    //测量尺寸,并初始化棋谱，棋子尺寸
    //
    @SuppressLint("DrawAllocation")
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthSize=MeasureSpec.getSize(widthMeasureSpec)
        val widthMode=MeasureSpec.getMode(widthMeasureSpec)
        val heightSize=MeasureSpec.getSize(heightMeasureSpec)
        val heightMode=MeasureSpec.getMode(heightMeasureSpec)

        var width=Math.min(widthSize,heightSize)

        if(widthMode==MeasureSpec.UNSPECIFIED) width=heightSize
        else if(heightMode==MeasureSpec.UNSPECIFIED) width=widthSize

        setMeasuredDimension(width,width)
        //开始初始化
        panelWidth=width*4/5
        lineHeight=(panelWidth/(MAX_LINE-1)).toFloat()

        startX=lineHeight/(MAX_LINE-1)
        startY=lineHeight/(MAX_LINE-1)
        chessWidth=lineHeight/4

        chessTouchRectFArray= Array(chessesArray.size){_-> RectF(0f,0f,0f,0f) }
        redChesses= Array<Point?>(0){ _->null}
        blueChesses= Array<Point?>(0){ _->null}
        var row=0
        for(i in 1..chessesArray.size){

            chessTouchRectFArray[i-1]= RectF(
                    startX-chessWidth/2+((i-1)%MAX_LINE)*lineHeight,
                    startY-chessWidth/2+(row*lineHeight),
                    startX+chessWidth/2+((i-1)%MAX_LINE)*lineHeight,
                    startY+chessWidth/2+(row*lineHeight))

            if(i%MAX_LINE==0) row++
        }
        blankBitmap= Bitmap.createBitmap(lineHeight.toInt()/2,lineHeight.toInt()/2, Bitmap.Config.ARGB_4444)
        redBitmap= Bitmap.createScaledBitmap(redBitmap,lineHeight.toInt()/2,lineHeight.toInt()/2,true)
        blueBitmap= Bitmap.createScaledBitmap(blueBitmap,lineHeight.toInt()/2,lineHeight.toInt()/2,true)
        selectedRedBitmap= Bitmap.createScaledBitmap(selectedRedBitmap,lineHeight.toInt()/2,lineHeight.toInt()/2,true)
        selectedBlueBitmap= Bitmap.createScaledBitmap(selectedBlueBitmap,lineHeight.toInt()/2,lineHeight.toInt()/2,true)
    }


    //
    //绘制View
    //
    override fun onDraw(canvas: Canvas?) {
        this.canvas=canvas!!

        drawBoard(canvas)

        drawAllChesses(canvas,chessesArray)
    }
    //
    //判断点是否在矩形区域内
    //
    private fun RectF.hasPoint(p: Point)=(p.x in left..right && p.y in top..bottom)


    //
    //信息提示
    //
    private fun toast(str:String="ok"){
        Toast.makeText(context,str, Toast.LENGTH_LONG).show()
    }



    //
    //移动棋子
    //
    private fun moveChess(from: Point, to: Point, player:Int=0):Boolean{
        val fromChessId:Int=ChessUtil.point2Id(from)
        val fromChessValue=chessesArray[fromChessId]
        val toChessId:Int=ChessUtil.point2Id(to)
        val toChessValue=chessesArray[toChessId]
        if(toChessValue==0){//目标为空白区域才可以走

            if(isDark==false) return false
            val pow=2.0
            val distance=Math.sqrt(
                    Math.pow(to.x.toDouble()-from.x.toDouble(),pow)+
                            Math.pow(to.y.toDouble()-from.y.toDouble(),pow)
            )
            if(distance<=1){

                if(player==1){//走红棋
                    chessesArray[toChessId]=1
                    isLockedRed=true
                    isLockedBlue=false
                }else if(player==2){//走蓝棋
                    chessesArray[toChessId]=2
                    isLockedBlue=true
                    isLockedRed=false
                }

                chessesArray[darkChessID]=0
                isDark=false
                darkChessID=-1//表示没有被选中的
                val killedChesses= gsc.eatChessId(chessesArray,to,player)
                for (id in killedChesses)chessesArray[id]=0
            }
            //每走一步判断游戏状态
            if(gsc.isGameOver(player)){
                showDialog()
            }

        }
        invalidate()
        return true
    }


    //
    //点击棋子时
    //

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if(performClick()){ }
        event!!
        val action=event.action
        val x=event.x
        val y=event.y
        val touchPoint= Point(x.toInt(),y.toInt())
        if(action== MotionEvent.ACTION_UP){
            for(i in 0..15){
                if(chessTouchRectFArray[i].hasPoint(touchPoint)){
                    val fromChessValue=chessesArray[i]

                    when(fromChessValue){
                    //点击棋子
                    //预选红方
                        1->{
                            if(isLockedRed==true){
                                Log.d("debug","红棋被锁状态\n\n")
                                return false
                            }
                            if(isDark==false){
                                chessesArray[i]=11
                                darkChessID=i
                                isDark=true

                            }else{
                                if(chessesArray[i]!=1)return false
                                chessesArray[darkChessID]=1
                                chessesArray[i]=11
                                darkChessID=i
                            }
                            player=1
                        }
                    //点击棋子
                    //预选蓝方
                        2->{
                            if(isLockedBlue==true){
                                Log.d("debug","蓝棋被锁状态\n\n")
                                return false
                            }

                            if(isDark==false){
                                chessesArray[i]=22
                                darkChessID=i
                                isDark=true
                            }else{
                                if(chessesArray[i]!=2)return false
                                chessesArray[darkChessID]=2
                                chessesArray[i]=22
                                darkChessID=i
                            }
                            player=2
                        }
                    }
                    fromChessPoint=ChessUtil.id2Point(darkChessID)//初始坐标
                    toChessPoint=ChessUtil.id2Point(i)//目的坐标

                    //

                    if(i!=darkChessID && fromChessPoint!= Point(-1,-1))moveChess(fromChessPoint, toChessPoint,player)
                    invalidate()
                    return true
                }
            }


        }
        return true
    }


    override fun performClick(): Boolean {
        return super.performClick()
    }

    //
    //画棋盘线
    //
    private fun drawBoard(canvas: Canvas){

        for(i in 0..MAX_LINE){
            val startX=lineHeight/3
            val endX=lineHeight*3+startX

            val y=((0.333+i)*lineHeight)

            canvas.drawLine(startX,y.toFloat(),endX,y.toFloat(),paint)
            canvas.drawLine(y.toFloat(),startX,y.toFloat(),endX,paint)
            canvas.save()
        }
    }
    //
    //绘制双方棋子
    //
    private  fun drawAllChesses(canvas: Canvas, chessesArray:IntArray){


        var row=0
        for (i in 1..chessesArray.size){

            when(chessesArray[i-1]){
                1->{//绘制红方
                    canvas.drawBitmap(redBitmap,startX+(((i-1)%4)*lineHeight)-chessWidth,startY+(row*lineHeight)-chessWidth,paint)
                    redChesses+= Point(i-1,row)
                }
                2->{//绘制蓝方
                    canvas.drawBitmap(blueBitmap,startX+(((i-1)%4)*lineHeight)-chessWidth,startY+(row*lineHeight)-chessWidth,paint)
                    blueChesses+= Point(i-1,row)

                }
                0->{//绘制空白
                    canvas.drawBitmap(blankBitmap,startX+(((i-1)%4)*lineHeight)-chessWidth,startY+(row*lineHeight)-chessWidth,paint)

                }
                11->{//绘制红方选中状态
                    canvas.drawBitmap(selectedRedBitmap,startX+(((i-1)%4)*lineHeight)-chessWidth,startY+(row*lineHeight)-chessWidth,paint)

                }
                22->{//绘制蓝方选中状态
                    canvas.drawBitmap(selectedBlueBitmap,startX+(((i-1)%4)*lineHeight)-chessWidth,startY+(row*lineHeight)-chessWidth,paint)

                }

            }
            if(i%4==0) row++
        }
    }

    //
    //
    //游戏结束对话框
    //
    private fun showDialog(){
        val GAME_OVER_TEXT=if(gsc.whoWin()==1){"红方胜利"}else{"蓝方胜利"}
        AlertDialog.Builder(context)
                .setMessage("恭喜" + GAME_OVER_TEXT + ",是否再来一局？")
                .setCancelable(false)
                .setPositiveButton("确定", DialogInterface.OnClickListener{
                    _,_->playAgain()
                })
                .setNegativeButton("取消", DialogInterface.OnClickListener() {
                    _,_->{}
                })
                .show()

    }

    //
    //再来一局
    //
    //
    fun playAgain(){
        isLockedRed=true
        isLockedBlue=false
        chessesArray= intArrayOf(
                1,1,1,1,
                1,0,0,1,
                2,0,0,2,
                2,2,2,2
        )
        GameStateController.reset()
        invalidate()
    }

}