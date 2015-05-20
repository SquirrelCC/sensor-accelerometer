package com.scconsulting.sensor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * @author impaler
 * This is the main surface that handles the onTouch events,
 * and draws the image to the screen.
 */
public class MainPanel extends SurfaceView implements SurfaceHolder.Callback {

	private int stateToSave;
	private static final String TAG = MainPanel.class.getSimpleName();
	private static final int EXPLOSION_SIZE = 200;
	
	private MainThread thread;
	public Explosion explosion;
	private Drawable bomb;
	private int height = 0;
	private int width = 0;
	private Paint paint;
	private float[] border;

	// the frames per second to be displayed
	private String avgFps;
	public void setAvgFps(String avgFps) {
		this.avgFps = avgFps;
	}

	public MainPanel(Context context) {
		super(context);

		// adding the callback (this) to the surface holder to intercept events
		getHolder().addCallback(this);

		// make the MainPanel focusable so it can handle events
		setFocusable(true);
		
		// Construct a Drawable from the image file
		bomb = getResources().getDrawable(R.drawable.colorbomb);
		
		// Initialize Paint
		paint = new Paint();
		paint.setColor(0xfff984ef);  // Pink

	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format,
			int width, int height) {
		
		this.height = height;
		this.width = width;
		
		// Set the size of the bomb drawable
		int h = bomb.getIntrinsicHeight();
		int w = bomb.getIntrinsicWidth();
		bomb.setBounds(
				(width/2) - (w/2),
				(height/2) - (h/2),
				(width/2) + (w/2),
				(height/2) + (h/2) );
		
		// Set up a narrow border, to be drawn on the Canvas
		border = new float[]{
				0, 0, width-1, 0, 
				width-1, 0, width-1, height-1,
				width-1, height-1, 0, height-1,
				0, height-1, 0, 0
		};
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// create the game loop thread
		thread = new MainThread(getHolder(), this);
		
		// at this point the surface is created and
		// we can safely start the game loop
		thread.setRunning(true);
		thread.start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		//Log.d(TAG, "Surface is being destroyed");
		
		// tell the thread to shut down and wait for it to finish
		// this is a clean shutdown
		boolean retry = true;
		while (retry) {
			try {
				thread.setRunning(false);
				thread.join();
				retry = false;
			} catch (InterruptedException e) {
				// try again shutting down the thread
			}
		}
		//Log.d(TAG, "Thread was shut down cleanly");
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			// handle touch
			// check if explosion is still active
			if (explosion == null || explosion.getState() == Explosion.STATE_DEAD) {
				
				// Set up an Explosion at the point of the Touch event
				explosion = new Explosion(EXPLOSION_SIZE, (int)event.getX(), (int)event.getY());
				
				// Set up an Explosion in the center of the screen
				//explosion = new Explosion(EXPLOSION_SIZE, width/2, height/2);
			}
		}
		return true;
	}
	
	/**
	 * Public method to set up an Explosion, if one is not already active.
	 */
	public void explode() {
		if (explosion == null || explosion.getState() == Explosion.STATE_DEAD) {
			explosion = new Explosion(EXPLOSION_SIZE, width/2, height/2);
		}
	}
	
	public void render(Canvas canvas) {

		if (canvas != null) {
			canvas.drawColor(Color.BLACK);
			
			// display frames per second
			//displayFps(canvas, avgFps);
	
			// display border
			canvas.drawLines(border, paint);
			
			// render explosions
			if (explosion == null || explosion.isDead()) {
				bomb.draw(canvas);
			}
			else {
				explosion.draw(canvas);
			}
		}
	}

	/**
	 * This is the update method.
	 * It iterates through all the objects,
	 * in this case just the explosion,
	 * and calls each object's update method.
	 */
	public void update() {
		// update explosions
		if (explosion != null && explosion.isAlive()) {
			explosion.update(getHolder().getSurfaceFrame());
		}
	}

	// Display frames per second on the canvas, as calculated in MainThread
	//private void displayFps(Canvas canvas, String fps) {
	//	if (canvas != null && fps != null) {
	//		Paint paint = new Paint();
	//		paint.setARGB(255, 255, 255, 255);
	//		canvas.drawText(fps, this.getWidth() - 50, 20, paint);
	//	}
	//}
	
	@Override
	  public Parcelable onSaveInstanceState() {

	    Bundle bundle = new Bundle();
	    bundle.putParcelable("instanceState", super.onSaveInstanceState());
	    bundle.putInt("stateToSave", this.stateToSave);

	    return bundle;
	  }

	  @Override
	  public void onRestoreInstanceState(Parcelable state) {

	    if (state instanceof Bundle) {
	      Bundle bundle = (Bundle) state;
	      this.stateToSave = bundle.getInt("stateToSave");
	      state = bundle.getParcelable("instanceState");
	    }
	    super.onRestoreInstanceState(state);
	  }

}
