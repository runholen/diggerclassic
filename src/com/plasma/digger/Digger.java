package com.plasma.digger;
import java.util.concurrent.TimeUnit;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.SensorListener;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.SurfaceHolder.Callback;
import android.view.View.OnTouchListener;
import android.widget.Toast;
//import com.google.android.gms.games.Games;
//import com.google.example.games.basegameutils.BaseGameActivity;

public class Digger extends Activity implements Callback, OnTouchListener, SelectionChooser{

	private SurfaceView surface;
	private SurfaceHolder holder;
	private Handler handler = null;
	private int w, h, xMarigin, yMarigin;
	private boolean paused = false;
	protected GameLoop gameLoop = null;
	private Paint backgroundPaint = null;
	private Triangle butt_left, butt_right, butt_up, butt_down;
	private Circle shoot;
	private Square menu;
	private int buttonColor = Color.argb(100, 150, 150, 150);
	private int blackColor = Color.argb(255, 255, 0, 0);
	private int buttonsMode = 0;
	private int bs = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);   
		setContentView(R.layout.activity_digger);
    	surface = (SurfaceView) findViewById(R.id.digger_surface);
    	holder = surface.getHolder();
    	surface.getHolder().addCallback(this);
    	surface.setKeepScreenOn(true);
    	surface.setOnTouchListener(this);
		handler = new Handler();
		backgroundPaint = new Paint();
		backgroundPaint.setColor(Color.WHITE);
		loadscores(Scores);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.digger, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(android.view.MenuItem item){
    	switch(item.getItemId()){
    	case R.id.about:
    		Digger.this.runOnUiThread(new CustomDialog(Digger.this,"About",null,getText(),false));
    		return true;
    	case R.id.settings:
    		startActivityForResult(new android.content.Intent(this, SettingsActivity.class), 0);
    		return true;
    	default:
    		return super.onOptionsItemSelected(item);
    	}
    }
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		initButtons();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		System.out.println("Surface changed "+(gameLoop!=null));
		this.w = width;
		this.h = height;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		w = holder.getSurfaceFrame().width();
		h = holder.getSurfaceFrame().height();
   		System.out.println("Surface created "+w+" "+h+" "+(gameLoop!=null));
		if (gameLoop == null){
			gameLoop = new GameLoop();
			gameLoop.start();
		}
		System.out.println("Gameloop: "+(gameLoop!=null));
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		System.out.println("onPause "+(gameLoop!=null));
		Input.pauseNow();
		//Main.pause = true;
    }
    
    @Override
	protected void onResume() {
		super.onResume();
		System.out.println("onResume "+(gameLoop!=null));
		//Main.pause = false;
    }

    @Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		System.out.println("surfaceDestroyed "+(gameLoop!=null));
	}
    
    @Override
    public void onDestroy(){
    	System.out.println("onDestroy "+(gameLoop!=null));
    	super.onDestroy();
    	System.out.println("onDestroy "+(gameLoop!=null));
    }
    
    // When an android device changes orientation usually the activity is destroyed and recreated with a new 
    // orientation layout. This method, along with a setting in the the manifest for this activity
    // tells the OS to let us handle it instead.
    //
    // This increases performance and gives us greater control over activity creation and destruction for simple 
    // activities. 
    // 
    // Must place this into the AndroidManifest.xml file for this activity in order for this to work properly 
    //   android:configChanges="keyboardHidden|orientation"
    //   optionally 
    //   android:screenOrientation="landscape"
    @Override
    public void onConfigurationChanged(Configuration newConfig) 
    {
    	System.out.println("onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
    }
    
    @Override
    public void finish(){
    	System.out.println("Finish "+(gameLoop!=null));
    	try {
			if (gameLoop != null) gameLoop.safeStop();
			stopDiggerThread();
		} finally {
			gameLoop = null;
			gamethread = null;
		}
    	super.finish();
    }
    
	private void draw() {
		// TODO thread safety - the SurfaceView could go away while we are drawing
		
		Canvas c = null;
		try {
			// NOTE: in the LunarLander they don't have any synchronization here,
			// so I guess this is OK. It will return null if the holder is not ready
			c = holder.lockCanvas();
			
			// TODO this needs to synchronize on something?
			if (c != null) {
				doDraw(c);
			}
		} finally {
			if (c != null) {
				holder.unlockCanvasAndPost(c);
			}
		}
	}
	
	//Bitmap b = null;
	//Canvas c = null;
	
	private void doDraw(Canvas c) {
		
		try{
			synchronized (this) {
				xMarigin = 0; yMarigin = 0;
				Bitmap bm = Pc.currentImage.getBitmap();
				//Bitmap bm = Bitmap.createBitmap(buffer, w, h, Bitmap.Config.RGB_565);
				if (buttonsMode == 5 || buttonsMode == 6 || buttonsMode == 7){
					c.drawColor(Color.BLACK);
					if (buttonsMode == 5){
						c.drawBitmap(bm, new Rect(0,0,bm.getWidth(),bm.getHeight()), new Rect(bs,0,w-bs,h), backgroundPaint);
					}
					else if (buttonsMode == 6){
						c.drawBitmap(bm, new Rect(0,0,bm.getWidth(),bm.getHeight()), new Rect((int)(bs*2.5),0,w,h), backgroundPaint);
					}
					else if (buttonsMode == 7){
						c.drawBitmap(bm, new Rect(0,0,bm.getWidth(),bm.getHeight()), new Rect(0,0,w-(int)(bs*2.5),h), backgroundPaint);
					}
				}
				else{
					c.drawBitmap(bm, new Rect(0,0,bm.getWidth(),bm.getHeight()), new Rect(0,0,w,h), backgroundPaint);
				}
				
				if (butt_left == null) initButtons();
				drawShape(c, butt_left);
				drawShape(c, butt_up);
				drawShape(c, butt_right);
				drawShape(c, butt_down);
				drawShape(c, shoot);
				if (Main.hasStarted == false) drawShape(c, menu);
			}
		}catch(Exception ex){
			ex.printStackTrace();
			//showError("Error", "An error occured:\n"+ex.toString(),false);
			try{Thread.sleep(1000);}catch(Exception ex2){}
		}
		//Canvas.save and Canvas restore will remove flickering?
	}
    
	protected class GameLoop extends Thread {
		protected volatile boolean running = true;
		
		@Override
		public void run() {
			System.out.println("Gameloop starting");
			init("",66);
			while (running) {
				try {
					// TODO don't like this hardcoding
					TimeUnit.MILLISECONDS.sleep(5);
					
					synchronized (this) {
						if (paused == false){
							draw();
						} //End-of if (paused == false)
					}
				} catch (InterruptedException ie) {
					running = false;
				} catch(Exception ex){
					running = false;
					ex.printStackTrace();
					//showError("Error during game-loop", ex.toString(),false);
				}
			}
			System.out.println("Gameloop finished");
		}

		public void safeStop() {
			running = false;
			interrupt();
		}
	}

	private int lastKey;
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int parsedAction = event.getAction() & MotionEvent.ACTION_MASK;
		//final int pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
		//int x = (int)event.getX(pointerIndex);
		//int y = (int)event.getY(pointerIndex);
		//System.out.println("Touched: "+pointerIndex+"  "+x+","+y);
		if (butt_left == null) initButtons();
		boolean up = parsedAction == MotionEvent.ACTION_POINTER_UP || parsedAction == MotionEvent.ACTION_UP;
		releaseAllKeys();
		for (int t = 0; t < event.getPointerCount(); t++){
			int x = (int)event.getX(t);
			int y = (int)event.getY(t);
			if (up == false) {
				if (Main.hasStarted == false && menu.contains(x, y)){
					if (showingMenu == false) showMenu();
					return true;
				}
				else if (butt_left.contains(x, y)){
					//releaseAllKeys();
					Input.processkey (0x4b);
				}
				else if (butt_right.contains(x, y)){
					//releaseAllKeys();
					Input.processkey (0x4d);
				}
				else if (butt_up.contains(x, y)){
					//releaseAllKeys();
					Input.processkey (0x48);
				}
				else if (butt_down.contains(x, y)){
					//releaseAllKeys();
					Input.processkey (0x50);
				}
				else if (shoot.contains(x, y)){
					//releaseAllKeys();
					Input.processkey (0x3b);
				}
				else{
					if (buttonsMode == 4){
						int xx = x * 320 / w;
						int yy = y * 200 / h;
						//diggerx, diggery;
						//System.out.println(x+" "+xx+" "+diggerx+" "+diggerv);
						//System.out.println(y+" "+yy+" "+diggery+" "+diggerh);
						int dx = Math.abs(xx-(diggerx+5));
						int dy = Math.abs(yy-(diggery+3));
						if (dx > dy){
							if (xx < diggerx) Input.processkey (0x4b); //Left
							else Input.processkey (0x4d); //Right
						}
						else{
							if (yy < diggery) Input.processkey (0x48); //Up
							else Input.processkey (0x50); //Down
						}
					}
					//KeyEvent.VK_SPACE: Input.processkey (0x3b);	break;
					//default:
					//key &= 0x7f;
					//if ((key>=65) && (key<=90))
					//	key+=(97-65);
					//Input.processkey (key); break;
				}
			}
			//space.touched(x,y);
		}
		/*
		else if (parsedAction == MotionEvent.ACTION_UP){
			if (butt_left == null) initButtons();
			if (butt_left.contains(x, y)){
				Input.processkey (0xcb);
			}
			else if (butt_right.contains(x, y)){
				Input.processkey (0xcd);
			}
			else if (butt_up.contains(x, y)){
				Input.processkey (0xc8);
			}
			else if (butt_down.contains(x, y)){
				Input.processkey (0xd0);
			}
			else if (shoot.contains(x, y)){
				Input.processkey (0xbb);
			}
			else{
				releaseAllKeys();
			}
			//default:
			//key &= 0x7f;
			//if ((key>=65) && (key<=90))
			//	key+=(97-65);
			//Input.processkey (0x80|key); break;
		}
		*/
		return true;
	}
	
	private void releaseAllKeys() {
		Input.processkey (0xcb);
		Input.processkey (0xcd);
		Input.processkey (0xc8);
		Input.processkey (0xd0);
		Input.processkey (0xbb);
	}

	private void initButtons(){
		int hh = h;
		buttonsMode = getButtonsMode();
		System.out.println("Controls: "+buttonsMode);
		int size = (int)(hh/3.5);
		if (buttonsMode == 1) size = (int)(hh/5.5);
		if (buttonsMode == 2) size = (int)(hh/4.5);
		if (buttonsMode == 3){
			size = (int)(hh/5.5);
			hh = (int)(hh/1.8);
		}
		if (buttonsMode == 4){
			size = 0; hh = 0;
		}
		if (buttonsMode == 5 || buttonsMode == 6 || buttonsMode == 7){
			size = (int)(hh/5.5);
		}
    	butt_left = new Triangle(new Rect(0,hh-size*2,size,hh-size*1),4);
    	butt_right = new Triangle(new Rect(size*2,hh-size*2,size*3,hh-size*1),6);
    	butt_up = new Triangle(new Rect(size,hh-size*3,size*2,hh-size*2),8);
    	butt_down = new Triangle(new Rect(size,hh-size*1,size*2,hh-size*0),2);
    	if (buttonsMode == 4){
    		hh = h;
    		size = (int)(hh/5.5);
			hh = (int)(hh/1.8);
    	}
    	menu = new Square(new Rect(0,hh-size,size,hh),true);
    	shoot = new Circle(w-size,hh-size,size/2);
    	if (buttonsMode == 5){
    		int rm = w-size;
    		butt_left = new Triangle(new Rect(rm,0,rm+size,size),4);
        	butt_right = new Triangle(new Rect(rm,(int)(size*1.5),rm+size,(int)(size*2.5)),6);
        	butt_up = new Triangle(new Rect(0,0,size,size),8);
        	butt_down = new Triangle(new Rect(0,(int)(size*1.5),size,(int)(size*2.5)),2);
        	shoot = new Circle(rm+size/2,hh-size/2,size/2);
    	}
    	if (buttonsMode == 6){
    		butt_up = new Triangle(new Rect((int)(size*0.75),0,(int)(size*1.75),size),8);
    		butt_left = new Triangle(new Rect(0,(int)(size*1.1),size,(int)(size*2.1)),4);
        	butt_right = new Triangle(new Rect((int)(size*1.5),(int)(size*1.1),(int)(size*2.5),(int)(size*2.1)),6); 	
        	butt_down = new Triangle(new Rect((int)(size*0.75),(int)(size*2.2),(int)(size*1.75),(int)(size*3.2)),2);
        	shoot = new Circle(size/2,hh-size/2,size/2);
    	}
    	if (buttonsMode == 7){
    		butt_up = new Triangle(new Rect(w-(int)(size*1.75),0,w-(int)(size*0.75),size),8);
    		butt_left = new Triangle( new Rect(w-(int)(size*2.5),(int)(size*1.1),w-(int)(size*1.5),(int)(size*2.1)),4);
        	butt_right = new Triangle(new Rect(w-(int)(size*1.0),(int)(size*1.1),w-(int)(size*0.0),(int)(size*2.1)),6); 	
        	butt_down = new Triangle(new Rect(w-(int)(size*1.75),(int)(size*2.2),w-(int)(size*0.75),(int)(size*3.2)),2);
        	shoot = new Circle(w-size/2,hh-size/2,size/2);
    	}
    	bs = size;
    }
 	
	private void drawShape(Canvas c, Circle ci){
    	Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    	//paint.setColor(android.graphics.Color.BLUE);
    	paint.setStyle(Paint.Style.FILL);
    	paint.setAntiAlias(true);
    	//c.drawRect(ci.bounds, paint);
    	paint.setColor(buttonColor);
    	c.drawCircle(ci.x, ci.y, ci.r, paint);
    }
	
	private void drawShape(Canvas c, Square s){
    	Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    	//paint.setColor(android.graphics.Color.BLUE);
    	paint.setStyle(Paint.Style.FILL);
    	paint.setAntiAlias(true);
    	//c.drawRect(ci.bounds, paint);
    	paint.setColor(buttonColor);
    	c.drawRect(s.bounds, paint);
    	if (s.menu){
    		int size = (int)(h/3.5);
    		paint.setColor(blackColor);
    		paint.setTextSize(size/6);
    		c.drawText("Menu", (float)(s.bounds.left+5), (float)(s.bounds.top+(s.bounds.bottom-s.bounds.top)/2+5), paint);
    	}
    }
	
	private void drawShape(Canvas c, Triangle t){
    	//Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    	//paint.setColor(android.graphics.Color.BLUE);
    	//paint.setStyle(Paint.Style.FILL);
    	//paint.setAntiAlias(true);
    	//c.drawRect(t.bounds, paint);
    	drawShape(c, t.points);
    }
	
	private void drawShape(Canvas c, Point[] p){
    	Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG); 
        paint.setStrokeWidth(2); 
        paint.setColor(buttonColor);      
        paint.setStyle(Paint.Style.FILL); 
        paint.setAntiAlias(true); 
        Path path = new Path(); 
        path.setFillType(Path.FillType.EVEN_ODD); 
        path.moveTo(p[0].x,p[0].y);
        for (int t = 1; t < p.length; t++){
        	path.lineTo(p[t].x, p[t].y);
        }
        path.lineTo(p[0].x,p[0].y); 
        path.close();
        c.drawPath(path, paint); 
    }
	
	//DIGGER SPECIFIC:
	
	/* WARNING! This code is ugly and highly non-object-oriented.
	It was ported from C almost mechanically! */
	static int MAX_RATE = 200, MIN_RATE = 40;

	int width = 320, height = 200, frametime = 66;
	Thread gamethread;

	String subaddr;

	Image pic;
	//Graphics picg;

	Bags Bags;
	Main Main;
	Sound Sound;
	Monster Monster;
	Scores Scores;
	Sprite Sprite;
	Drawing Drawing;
	Input Input;
	Pc Pc;

	// -----

	int diggerx=0,diggery=0,diggerh=0,diggerv=0,diggerrx=0,diggerry=0,digmdir=0,
		digdir=0,digtime=0,rechargetime=0,firex=0,firey=0,firedir=0,expsn=0,
		deathstage=0,deathbag=0,deathani=0,deathtime=0,startbonustimeleft=0,
		bonustimeleft=0,eatmsc=0,emocttime=0,emn=0;

	int emmask=0;

	byte emfield[]={	//[150]
	  0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
	  0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
	  0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
	  0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
	  0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
	  0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
	  0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
	  0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
	  0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
	  0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};

	boolean digonscr=false,notfiring=false,bonusvisible=false,bonusmode=false,diggervisible=false;

	long time,ftime = 50;
	int embox[]={8,12,12,9,16,12,6,9};	// [8]
	int deatharc[]={3,5,6,6,5,3,0};			// [7]
	private boolean showingMenu = false;

	public Digger () {
		Bags = new Bags (this);
		Main = new Main (this);
		Sound = new Sound (this);
		Monster = new Monster (this);
		Scores = new Scores (this);
		Sprite = new Sprite (this);
		Drawing = new Drawing (this);
		Input = new Input (this);
		Pc = new Pc (this);
	}
	boolean checkdiggerunderbag (int h,int v) {
	  if (digmdir==2 || digmdir==6)
		if ((diggerx-12)/20==h)
		  if ((diggery-18)/18==v || (diggery-18)/18+1==v)
			return true;
	  return false;
	}
	int countem () {
	  int x,y,n=0;
	  for (x=0;x<15;x++)
		for (y=0;y<10;y++)
		  if ((emfield[y*15+x]&emmask)!=0)
			n++;
	  return n;
	}
	void createbonus () {
	  bonusvisible=true;
	  Drawing.drawbonus(292,18);
	}
	public void destroy () {
		if (gamethread!=null)
			gamethread.stop ();
	}
	void diggerdie () {
	  int clbits;
	  switch (deathstage) {
		case 1:
		  if (Bags.bagy(deathbag)+6>diggery)
			diggery=Bags.bagy(deathbag)+6;
		  Drawing.drawdigger(15,diggerx,diggery,false);
		  Main.incpenalty();
		  if (Bags.getbagdir(deathbag)+1==0) {
			Sound.soundddie();
			deathtime=5;
			deathstage=2;
			deathani=0;
			diggery-=6;
		  }
		  break;
		case 2:
		  if (deathtime!=0) {
			deathtime--;
			break;
		  }
		  if (deathani==0)
			Sound.music(2);
		  clbits=Drawing.drawdigger(14-deathani,diggerx,diggery,false);
		  Main.incpenalty();
		  if (deathani==0 && ((clbits&0x3f00)!=0))
			Monster.killmonsters(clbits);
		  if (deathani<4) {
			deathani++;
			deathtime=2;
		  }
		  else {
			deathstage=4;
			if (Sound.musicflag)
			  deathtime=60;
			else
			  deathtime=10;
		  }
		  break;
		case 3:
		  deathstage=5;
		  deathani=0;
		  deathtime=0;
		  break;
		case 5:
		  if (deathani>=0 && deathani<=6) {
			Drawing.drawdigger(15,diggerx,diggery-deatharc[deathani],false);
			if (deathani==6)
			  Sound.musicoff();
			Main.incpenalty();
			deathani++;
			if (deathani==1)
			  Sound.soundddie();
			if (deathani==7) {
			  deathtime=5;
			  deathani=0;
			  deathstage=2;
			}
		  }
		  break;
		case 4:
		  if (deathtime!=0)
			deathtime--;
		  else
			Main.setdead(true);
	  }
	}
	void dodigger () {
	  newframe();
	  if (expsn!=0)
		drawexplosion();
	  else
		updatefire();
	  if (diggervisible)
		if (digonscr)
		  if (digtime!=0) {
			Drawing.drawdigger(digmdir,diggerx,diggery,notfiring && rechargetime==0);
			Main.incpenalty();
			digtime--;
		  }
		  else
			updatedigger();
		else
		  diggerdie();
	  if (bonusmode && digonscr) {
		if (bonustimeleft!=0) {
		  bonustimeleft--;
		  if (startbonustimeleft!=0 || bonustimeleft<20) {
			startbonustimeleft--;
			if ((bonustimeleft&1)!=0) {
			  Pc.ginten(0);
			  Sound.soundbonus();
			}
			else {
			  Pc.ginten(1);
			  Sound.soundbonus();
			}
			if (startbonustimeleft==0) {
			  Sound.music(0);
			  Sound.soundbonusoff();
			  Pc.ginten(1);
			}
		  }
		}
		else {
		  endbonusmode();
		  Sound.soundbonusoff();
		  Sound.music(1);
		}
	  }
	  if (bonusmode && !digonscr) {
		endbonusmode();
		Sound.soundbonusoff();
		Sound.music(1);
	  }
	  if (emocttime>0)
		emocttime--;
	}
	void drawemeralds () {
	  int x,y;
	  emmask=1<<Main.getcplayer();
	  for (x=0;x<15;x++)
		for (y=0;y<10;y++)
		  if ((emfield[y*15+x]&emmask)!=0)
			Drawing.drawemerald(x*20+12,y*18+21);
	}
	void drawexplosion () {
	  switch (expsn) {
		case 1:
		  Sound.soundexplode();
		case 2:
		case 3:
		  Drawing.drawfire(firex,firey,expsn);
		  Main.incpenalty();
		  expsn++;
		  break;
		default:
		  killfire();
		  expsn=0;
	  }
	}
	void endbonusmode () {
	  bonusmode=false;
	  Pc.ginten(0);
	}
	void erasebonus () {
	  if (bonusvisible) {
		bonusvisible=false;
		Sprite.erasespr(14);
	  }
	  Pc.ginten(0);
	}
	void erasedigger () {
	  Sprite.erasespr(0);
	  diggervisible=false;
	}
	public String getAppletInfo () {
		return "The Digger Remastered -- http://www.digger.org, Copyright (c) Andrew Jenner & Marek Futrega / MAF";
	}
	boolean getfirepflag () {
	  return Input.firepflag;
	}
	boolean hitemerald (int x,int y,int rx,int ry,int dir) {
	  boolean hit=false;
	  int r;
	  if (dir<0 || dir>6 || ((dir&1)!=0))
		return hit;
	  if (dir==0 && rx!=0)
		x++;
	  if (dir==6 && ry!=0)
		y++;
	  if (dir==0 || dir==4)
		r=rx;
	  else
		r=ry;
	  if ((emfield[y*15+x]&emmask)!=0) {
		if (r==embox[dir]) {
		  Drawing.drawemerald(x*20+12,y*18+21);
		  Main.incpenalty();
		}
		if (r==embox[dir+1]) {
		  Drawing.eraseemerald(x*20+12,y*18+21);
		  Main.incpenalty();
		  hit=true;
		  emfield[y*15+x]&=~emmask;
		}
	  }
	  return hit;
	}
	public void init (String submit, int speed) {

		if (gamethread != null) gamethread.stop ();

		subaddr = submit;

		try {
			frametime = speed;
			if (frametime>MAX_RATE)
				frametime = MAX_RATE;
			else if (frametime<MIN_RATE)
				frametime = MIN_RATE;
		}
		catch (Exception e) {
		}

		Pc.pixels = new int[65536];

		for (int i=0;i<2;i++) {
			//Pc.source[i] = new MemoryImageSource (Pc.width, Pc.height, new IndexColorModel (8, 4, Pc.pal[i][0], Pc.pal[i][1], Pc.pal[i][2]), Pc.pixels, 0, Pc.width);
			Pc.source[i] = new MemoryImageSource (i, Pc.width, Pc.height, Pc.pixels, 0, Pc.width);
			//Pc.source[i].setAnimated (true);
			Pc.image[i] = new Image(Pc.source[i]);
			Pc.source[i].newPixels ();
		}

		Pc.currentImage = Pc.image[0];
		Pc.currentSource = Pc.source[0];
		
		System.out.println("Starting gamethread");
		gamethread = new Thread (new GameStarter());
		gamethread.start ();

	}
	void initbonusmode () {
	  bonusmode=true;
	  erasebonus();
	  Pc.ginten(1);
	  bonustimeleft=250-Main.levof10()*20;
	  startbonustimeleft=20;
	  eatmsc=1;
	}
	void initdigger () {
	  diggerv=9;
	  digmdir=4;
	  diggerh=7;
	  diggerx=diggerh*20+12;
	  digdir=0;
	  diggerrx=0;
	  diggerry=0;
	  digtime=0;
	  digonscr=true;
	  deathstage=1;
	  diggervisible=true;
	  diggery=diggerv*18+18;
	  Sprite.movedrawspr(0,diggerx,diggery);
	  notfiring=true;
	  emocttime=0;
	  bonusvisible=bonusmode=false;
	  Input.firepressed=false;
	  expsn=0;
	  rechargetime=0;
	  emn=0;
	}
	/*
	public boolean keyDown (KeyEvent e, int key) {
		switch (key) {
			case KeyEvent.VK_LEFT: Input.processkey (0x4b);	break;
			case KeyEvent.VK_RIGHT: Input.processkey (0x4d);	break;
			case KeyEvent.VK_UP: Input.processkey (0x48);	break;
			case KeyEvent.VK_DOWN: Input.processkey (0x50);	break;
			case KeyEvent.VK_SPACE: Input.processkey (0x3b);	break;
			default:
				key &= 0x7f;
				if ((key>=65) && (key<=90))
					key+=(97-65);
				Input.processkey (key); break;
		}
		return true;
	}
	public boolean keyUp (KeyEvent e, int key) {
		switch (key) {
			case KeyEvent.VK_LEFT: Input.processkey (0xcb);	break;
			case KeyEvent.VK_RIGHT: Input.processkey (0xcd);	break;
			case KeyEvent.VK_UP: Input.processkey (0xc8);	break;
			case KeyEvent.VK_DOWN: Input.processkey (0xd0);	break;
			case KeyEvent.VK_SPACE: Input.processkey (0xbb);	break;
			default:
				key &= 0x7f;
				if ((key>=65) && (key<=90))
					key+=(97-65);
				Input.processkey (0x80|key); break;
		}
		return true;
	}
	*/
	void killdigger (int stage,int bag) {
	  if (deathstage<2 || deathstage>4) {
		digonscr=false;
		deathstage=stage;
		deathbag=bag;
	  }
	}
	void killemerald (int x,int y) {
	  if ((emfield[y*15+x+15]&emmask)!=0) {
		emfield[y*15+x+15]&=~emmask;
		Drawing.eraseemerald(x*20+12,(y+1)*18+21);
	  }
	}
	void killfire () {
	  if (!notfiring) {
		notfiring=true;
		Sprite.erasespr(15);
		Sound.soundfireoff();
	  }
	}
	void makeemfield () {
	  int x,y;
	  emmask=1<<Main.getcplayer();
	  for (x=0;x<15;x++)
		for (y=0;y<10;y++)
		  if (Main.getlevch(x,y,Main.levplan())=='C')
			emfield[y*15+x]|=emmask;
		  else
			emfield[y*15+x]&=~emmask;
	}
	void newframe () {
		Input.checkkeyb ();
	  time += frametime;
	  long l = time - Pc.gethrt ();
	  if (l>0) {
		  try {
			  Thread.sleep ((int)l);
		  }
		  catch (Exception e) {
		  }
	  }
	  Pc.currentSource.newPixels ();
	}
	int reversedir (int dir) {
	  switch (dir) {
		case 0: return 4;
		case 4: return 0;
		case 2: return 6;
		case 6: return 2;
	  }
	  return dir;
	}
	
	private class GameStarter implements Runnable{
		public void run() {
			System.out.println("Starting main thread");
			Main.main ();
			System.out.println("Main thread finished.");
		}
	}
	
	public void stopDiggerThread(){
		System.out.println("Stopping main thread");
		Main.running = false;
	}
	//public void start () {
		//requestFocus ();
	//}
	
	void updatedigger () {
	  int dir,ddir,clbits,diggerox,diggeroy,nmon;
	  boolean push = false;
	  Input.readdir();
	  dir=Input.getdir();
	  if (dir==0 || dir==2 || dir==4 || dir==6)
		ddir=dir;
	  else
		ddir=-1;
	  if (diggerrx==0 && (ddir==2 || ddir==6))
		digdir=digmdir=ddir;
	  if (diggerry==0 && (ddir==4 || ddir==0))
		digdir=digmdir=ddir;
	  if (dir==-1)
		digmdir=-1;
	  else
		digmdir=digdir;
	  if ((diggerx==292 && digmdir==0) || (diggerx==12 && digmdir==4) ||
		  (diggery==180 && digmdir==6) || (diggery==18 && digmdir==2))
		digmdir=-1;
	  diggerox=diggerx;
	  diggeroy=diggery;
	  if (digmdir!=-1)
		Drawing.eatfield(diggerox,diggeroy,digmdir);
	  switch (digmdir) {
		case 0:
		  Drawing.drawrightblob(diggerx,diggery);
		  diggerx+=4;
		  break;
		case 4:
		  Drawing.drawleftblob(diggerx,diggery);
		  diggerx-=4;
		  break;
		case 2:
		  Drawing.drawtopblob(diggerx,diggery);
		  diggery-=3;
		  break;
		case 6:
		  Drawing.drawbottomblob(diggerx,diggery);
		  diggery+=3;
		  break;
	  }
	  if (hitemerald((diggerx-12)/20,(diggery-18)/18,(diggerx-12)%20,
					 (diggery-18)%18,digmdir)) {
		  if (emocttime==0)
			   emn=0;

		Scores.scoreemerald();
		Sound.soundem();
		Sound.soundemerald(emn); //emocttime);
		emn++;
		if (emn==8) {
		  emn=0;
		  Scores.scoreoctave();
		}
		emocttime=9;
	  }
	  clbits=Drawing.drawdigger(digdir,diggerx,diggery,notfiring && rechargetime==0);
	  Main.incpenalty();
	  if ((Bags.bagbits()&clbits)!=0) {
		if (digmdir==0 || digmdir==4) {
		  push=Bags.pushbags(digmdir,clbits);
		  digtime++;
		}
		else
		  if (!Bags.pushudbags(clbits))
			push=false;
		if (!push) { /* Strange, push not completely defined */
		  diggerx=diggerox;
		  diggery=diggeroy;
		  Drawing.drawdigger(digmdir,diggerx,diggery,notfiring && rechargetime==0);
		  Main.incpenalty();
		  digdir=reversedir(digmdir);
		}
	  }
	  if (((clbits&0x3f00)!=0) && bonusmode)
		for (nmon=Monster.killmonsters(clbits);nmon!=0;nmon--) {
		  Sound.soundeatm();
		  Scores.scoreeatm();
		}
	  if ((clbits&0x4000)!=0) {
		Scores.scorebonus();
		initbonusmode();
	  }
	  diggerh=(diggerx-12)/20;
	  diggerrx=(diggerx-12)%20;
	  diggerv=(diggery-18)/18;
	  diggerry=(diggery-18)%18;
	}
	void updatefire () {
	  int clbits,b,mon,pix = 0;
	  if (notfiring) {
		if (rechargetime!=0)
		  rechargetime--;
		else
		  if (getfirepflag())
			if (digonscr) {
			  rechargetime=Main.levof10()*3+60;
			  notfiring=false;
			  switch (digdir) {
				case 0:
				  firex=diggerx+8;
				  firey=diggery+4;
				  break;
				case 4:
				  firex=diggerx;
				  firey=diggery+4;
				  break;
				case 2:
				  firex=diggerx+4;
				  firey=diggery;
				  break;
				case 6:
				  firex=diggerx+4;
				  firey=diggery+8;
			  }
			  firedir=digdir;
			  Sprite.movedrawspr(15,firex,firey);
			  Sound.soundfire();
			}
	  }
	  else {
		switch (firedir) {
		  case 0:
			firex+=8;
			pix=Pc.ggetpix(firex,firey+4)|Pc.ggetpix(firex+4,firey+4);
			break;
		  case 4:
			firex-=8;
			pix=Pc.ggetpix(firex,firey+4)|Pc.ggetpix(firex+4,firey+4);
			break;
		  case 2:
			firey-=7;
			pix=(Pc.ggetpix(firex+4,firey)|Pc.ggetpix(firex+4,firey+1)|
				 Pc.ggetpix(firex+4,firey+2)|Pc.ggetpix(firex+4,firey+3)|
				 Pc.ggetpix(firex+4,firey+4)|Pc.ggetpix(firex+4,firey+5)|
				 Pc.ggetpix(firex+4,firey+6))&0xc0;
			break;
		  case 6:
			firey+=7;
			pix=(Pc.ggetpix(firex,firey)|Pc.ggetpix(firex,firey+1)|
				 Pc.ggetpix(firex,firey+2)|Pc.ggetpix(firex,firey+3)|
				 Pc.ggetpix(firex,firey+4)|Pc.ggetpix(firex,firey+5)|
				 Pc.ggetpix(firex,firey+6))&3;
			break;
		}
		clbits=Drawing.drawfire(firex,firey,0);
		Main.incpenalty();
		if ((clbits&0x3f00)!=0)
		  for (mon=0,b=256;mon<6;mon++,b<<=1)
			if ((clbits&b)!=0) {
			  Monster.killmon(mon);
			  Scores.scorekill();
			  expsn=1;
			}
		if ((clbits&0x40fe)!=0)
		  expsn=1;
		switch (firedir) {
		  case 0:
			if (firex>296)
			  expsn=1;
			else
			  if (pix!=0 && clbits==0) {
				expsn=1;
				firex-=8;
				Drawing.drawfire(firex,firey,0);
			  }
			break;
		  case 4:
			if (firex<16)
			  expsn=1;
			else
			  if (pix!=0 && clbits==0) {
				expsn=1;
				firex+=8;
				Drawing.drawfire(firex,firey,0);
			  }
			break;
		  case 2:
			if (firey<15)
			  expsn=1;
			else
			  if (pix!=0 && clbits==0) {
				expsn=1;
				firey+=7;
				Drawing.drawfire(firex,firey,0);
			  }
			break;
		  case 6:
			if (firey>183)
			  expsn=1;
			else
			  if (pix!=0 && clbits==0) {
				expsn=1;
				firey-=7;
				Drawing.drawfire(firex,firey,0);
			  }
		}
	  }
	}

	public int getButtonsMode(){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		boolean firstTime = prefs.getBoolean("firstTime3", true);
		int mode = Integer.parseInt(prefs.getString("buttonsMode", "7"));
		if (firstTime){
			SharedPreferences.Editor e = prefs.edit();
			e.putBoolean("firstTime3", false);
			if (mode != 7){
				e.putString("buttonsMode", "7");
				e.commit();
				Toast t = Toast.makeText(this, "Controls changed to new mode"+"\n"+"This may be changed in settings.", Toast.LENGTH_LONG);
				t.show();
				mode = 7;
			}
			else{
				e.commit();
			}
		}
		return mode;
	}
	
	public void savescores(Scores scores) {
		SharedPreferences p = getSharedPreferences("Digger", Context.MODE_PRIVATE);
		SharedPreferences.Editor e = p.edit();
		for (int t = 0; t < 11; t++){
			e.putLong("ScoreHigh"+t, scores.scorehigh[t]);
			e.putString("ScoreInit"+t, scores.scoreinit[t]);
		}
		e.commit();
	}
	
	public void loadscores(Scores scores){
		SharedPreferences p = getSharedPreferences("Digger", Context.MODE_PRIVATE);
		System.out.println("Hiscores:");
		for (int t = 0; t < 11; t++){
			scores.scorehigh[t] = p.getLong("ScoreHigh"+t, 0);
			scores.scoreinit[t] = p.getString("ScoreInit"+t, "...");
			System.out.println(scores.scoreinit[t]+" "+scores.scorehigh[t]);
		}
	}
	
	private String getText(){
		String s = "Hi, and welcome to Digger Classic.\n";
		s += "Digger Classic is the authentic Digger experience.\n";
		s += "This version of the good old DOS-game Digger, now on Android, is not a remake, but is based on the Java port (by Marek Futrega) of Andrew Jenner's reverse-engineered open-source version of Digger for DOS. This makes for an authentic Digger experience with exact gameplay, just as you remember it from the good old days. Eg bags will not fall down when moving up from below, and you may reverse-shoot when doing this (may not work properly on some devices due to multitouch limitations).\n";
		s += "The Android port is developed and made available by Runar Holen. It is provided free of charge and with no ads.\n";
		s += "If you want to support the developer, you may allways drop a dollar or two at my paypal account runholen@gmail.com\n";
		s += "Here you may also send bug reports if you want.\n";
		s += "For more info about Digger, visit http://www.digger.org/\n";
		s += "Sound was originally not int the Java-port, but since then, Marek Futrega has also made a javascript version at http://www.futrega.org/digger/ with sound. The sound is mostly ported from this version (that you should also try out btw :-) )\n";
		return s;
	}
	
	public void showMenu(){
		showingMenu = true;
		String[] sos = {"About","Settings","Cancel"};
		Common.showSelectionDialog(this, this, 1, null, "Menu", null, sos, 2, false);
	}

	@Override
	public void selectionChoosed(int id, Object object, int itemIndex, String value) {
		if (itemIndex == 0){
			Digger.this.runOnUiThread(new CustomDialog(Digger.this,"About",null,getText(),false));
		}
		else if (itemIndex == 1){
			startActivityForResult(new android.content.Intent(this, SettingsActivity.class), 0);
		}
		showingMenu  = false;
	}
}
