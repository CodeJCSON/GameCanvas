package com.tangyx.game.holder;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.tangyx.game.R;
import com.tangyx.game.util.BitmapUtils;
import com.tangyx.game.util.SizeUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by tangyx on 2016/12/19.
 *
 */

public class GameView extends SurfaceView implements SurfaceHolder.Callback,Runnable{
    /**
     * 游戏状态
     */
    private final static int ING=0;//正在玩
    private final static int READY=1;//第一次进来，游戏加载状态到准备完成。
    private final static int PAUSE=2;//暂停，手离开了屏幕
    private static int GAME_STATE=READY;//默认加载状态到准备完成
    /**
     * surface的控制器
     */
    private SurfaceHolder mHolder;
    /**
     * 画布
     */
    private Canvas mCanvas;
    /**
     * 画笔
     */
    private Paint mPaint;
    /**
     * 游戏背景
     */
    private String mBackGround;
    private DrawBackground mDrawBackground;
    /**
     * 主角子弹
     */
    private Bitmap mPlayerBulletA;//黄色子弹
    private Bitmap mPlayerBulletB;//红色子弹
    private Bitmap mPlayerBulletC;//月牙形子弹
    private List<DrawPlayerBullet> mPlayerBullets;//主角子弹的集合，可以理解为弹夹
    private int mCountPlayerBullet;//子弹发出的速度，间隔多久加入一颗子弹。
    private int mTempBulletType=0;//模拟子弹类型的变化
    /**
     * 游戏主角
     */
    private DrawPlayer mPlayer;
    private int mSelectPlayer;
    /**
     * 是否暂停，或者退出了
     */
    private boolean isRun=true;

    public GameView(Context context,int player,String background,int cos) {
        super(context);
        this.mBackGround = background;
        this.mSelectPlayer = player;
        mHolder = getHolder();
        mHolder.addCallback(this);
    }


    /**
     * 初始化资源
     */
    private void initGame(){
        //初始化画笔
        mPaint = new Paint();
        mPaint.setColor(Color.WHITE);
        //加载背景图片资源
        mDrawBackground = new DrawBackground(getContext(),this.mBackGround);
        mPlayer = new DrawPlayer(getContext(),this.mSelectPlayer);
        mPlayerBulletA = BitmapUtils.ReadBitMap(getContext(),R.drawable.pl_bullet0);
        int wh = SizeUtils.dp2px(getContext(),10);
        mPlayerBulletA = BitmapUtils.getBitmap(mPlayerBulletA,wh,wh);
        mPlayerBulletB = BitmapUtils.ReadBitMap(getContext(),R.drawable.pl_bullet2);
        mPlayerBulletB = BitmapUtils.getBitmap(mPlayerBulletB,wh,wh);
        mPlayerBulletC = BitmapUtils.ReadBitMap(getContext(),R.drawable.pl_bullet4);
        mPlayerBulletC = BitmapUtils.getBitmap(mPlayerBulletC,wh,wh);
        mPlayerBullets = new ArrayList<>();
    }

    /**
     * 绘制内容以及更新内容
     */
    private void onGameDraw(){
        if(mCanvas==null)return;
        mCanvas.drawColor(Color.WHITE);
        //背景
        mDrawBackground.onDraw(mCanvas);
        mDrawBackground.updateGame();
        //主角
        mPlayer.onDraw(mCanvas);
        mPlayer.updateGame();
        //判断当前游戏状态
        switch (GAME_STATE){
            case ING:
                addPlayerBullet();
                break;
            case READY:
                mPlayer.onDrawCollect(mCanvas,getContext().getString(R.string.reading));
                break;
            case PAUSE:
                mPlayer.onDrawCollect(mCanvas,getContext().getString(R.string.conution));
                break;
        }
    }

    /**
     * 添加主角子弹
     */
    private void addPlayerBullet(){
        mCountPlayerBullet++;
        int mCreatePlayerBullet = 7;//间接性循环加入子弹
        if(mCountPlayerBullet % mCreatePlayerBullet ==0){
            int bullet = mPlayer.getBulletType();
            float playerX = mPlayer.getPlayerX();
            float playerY = mPlayer.getPlayerY();
            float bulletX;
            switch (bullet){
                case DrawPlayerBullet.PLAYER_BULLET_A:
                    bulletX = playerX+(mPlayer.getWidth()-mPlayerBulletA.getWidth())/2;
                    mPlayerBullets.add(new DrawPlayerBullet(getContext(),bulletX, playerY,mPlayerBulletA,bullet));
                    break;
                case DrawPlayerBullet.PLAYER_BULLET_B:
                case DrawPlayerBullet.PLAYER_BULLET_C:
                    //中间
                    bulletX = playerX+(mPlayer.getWidth()-mPlayerBulletC.getWidth())/2;
                    mPlayerBullets.add(new DrawPlayerBullet(getContext(),bulletX, playerY,mPlayerBulletC,DrawPlayerBullet.PLAYER_BULLET_A));
                    //左边
                    bulletX = playerX - mPlayerBulletC.getWidth();
                    mPlayerBullets.add(new DrawPlayerBullet(getContext(),bulletX, playerY,mPlayerBulletC,DrawPlayerBullet.PLAYER_BULLET_B));
                    //右边
                    bulletX = playerX + mPlayer.getWidth();
                    mPlayerBullets.add(new DrawPlayerBullet(getContext(),bulletX, playerY,mPlayerBulletC,DrawPlayerBullet.PLAYER_BULLET_C));
                    break;
                case DrawPlayerBullet.PLAYER_BULLET_D:
                    //左边
                    bulletX = playerX;
                    mPlayerBullets.add(new DrawPlayerBullet(getContext(),bulletX,playerY,mPlayerBulletB,bullet));
                    //右边
                    bulletX = playerX+mPlayer.getWidth()-mPlayerBulletB.getWidth();
                    mPlayerBullets.add(new DrawPlayerBullet(getContext(),bulletX, playerY,mPlayerBulletB,bullet));
                    break;
            }
        }
        for (int i=0;i<mPlayerBullets.size();i++) {
            DrawPlayerBullet bullet = mPlayerBullets.get(i);
            if(bullet.isDead()){//如果有子弹已经飞出屏幕直接移除。
                mPlayerBullets.remove(bullet);
            }else{
                bullet.updateGame();
                bullet.onDraw(mCanvas);
            }
        }
        //模拟子弹类型变化
        mTempBulletType++;
        if(mTempBulletType == 50){
            mPlayer.setBulletType(DrawPlayerBullet.PLAYER_BULLET_D);
        }else if(mTempBulletType == 100){
            mPlayer.setBulletType(DrawPlayerBullet.PLAYER_BULLET_C);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()){
            case MotionEvent.ACTION_MOVE:
                if(GAME_STATE == ING){
                    mPlayer.setPlayerX(x);
                    mPlayer.setPlayerY(y);
                }
                break;
            case MotionEvent.ACTION_DOWN:
                if(GAME_STATE == PAUSE || GAME_STATE==READY){
                    float cx = mPlayer.getCollectX()+mPlayer.getCollect().getWidth();
                    float cy = mPlayer.getCollectY()+mPlayer.getCollect().getHeight();
                    //精准判断是否按中指定位置才能进行移动 就是按中fire
                    if((x>mPlayer.getCollectX()&&x<cx)&&(y>mPlayer.getCollectY()&&y<cy)){
                        GAME_STATE = ING;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if(GAME_STATE==ING){
                    GAME_STATE = PAUSE;
                }
                break;
        }
        return true;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        initGame();
        //启动线程，开始加载游戏内容
        Thread mGameThread = new Thread(this);
        mGameThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isRun=false;
    }


    /**
     * 游戏的绘制肯定不能阻塞UI，所以我们需要在线程中来完成绘制，通过锁定画布，解锁画笔来提交绘制的内容
     */
    @Override
    public void run() {
        while (isRun){
            //锁定画布
            synchronized (mHolder){
                mCanvas = mHolder.lockCanvas();
                long start = System.currentTimeMillis();
                onGameDraw();
                //执行完成后提交画布
                long end = System.currentTimeMillis();
                if(mCanvas!=null){
                    mHolder.unlockCanvasAndPost(mCanvas);
                }
                if(end-start<15){
                    try {
                        Thread.sleep(Math.max(0,15-(end - start)));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }
}
