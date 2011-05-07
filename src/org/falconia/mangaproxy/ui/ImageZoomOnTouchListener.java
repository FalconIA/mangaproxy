package org.falconia.mangaproxy.ui;

import android.content.Context;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;

import com.sonyericsson.zoom.DynamicZoomControl;


public class ImageZoomOnTouchListener implements OnTouchListener {

	/**
	 * Enum defining listener modes. Before the view is touched the listener is
	 * in the UNDEFINED mode. Once touch starts it can enter either one of the
	 * other two modes: If the user scrolls over the view the listener will
	 * enter PAN mode, if the user lets his finger rest and makes a longpress
	 * the listener will enter ZOOM mode.
	 */
	private enum Mode {
		UNDEFINED, PAN, ZOOM
	}

	/** Current listener mode */
	private Mode mMode = Mode.UNDEFINED;

	/** Zoom control to manipulate */
	private DynamicZoomControl mZoomControl;

	/** X-coordinate of previously handled touch event */
	private float mX;

	/** Y-coordinate of previously handled touch event */
	private float mY;

	/** X-coordinate of latest down event */
	private float mDownX;

	/** Y-coordinate of latest down event */
	private float mDownY;

	/** Velocity tracker for touch events */
	private VelocityTracker mVelocityTracker;

	/** Distance touch can wander before we think it's scrolling */
	private final int mScaledTouchSlop;

	/** Maximum velocity for fling */
	private final int mScaledMaximumFlingVelocity;

	public ImageZoomOnTouchListener(Context context) {
		mScaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
		mScaledMaximumFlingVelocity = ViewConfiguration.get(context)
				.getScaledMaximumFlingVelocity();
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		final int action = event.getAction();
		final float x = event.getX();
		final float y = event.getY();

		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(event);

		switch (action) {
		case MotionEvent.ACTION_DOWN: {
			mZoomControl.stopFling();
			mDownX = x;
			mDownY = y;
			mX = x;
			mY = y;
			break;
		}
		case MotionEvent.ACTION_MOVE: {
			final float dx = (x - mX) / v.getWidth();
			final float dy = (y - mY) / v.getHeight();

			if (mMode == Mode.PAN) {
				mZoomControl.pan(-dx, -dy);
			} else {
				final float scrollX = mDownX - x;
				final float scrollY = mDownY - y;

				final float dist = (float) Math.sqrt(scrollX * scrollX + scrollY * scrollY);

				if (dist >= mScaledTouchSlop) {
					mMode = Mode.PAN;
				}
			}

			mX = x;
			mY = y;
			break;
		}
		case MotionEvent.ACTION_UP: {
			if (mMode == Mode.PAN) {
				mVelocityTracker.computeCurrentVelocity(1000, mScaledMaximumFlingVelocity);
				mZoomControl.startFling(-mVelocityTracker.getXVelocity() / v.getWidth(),
						-mVelocityTracker.getYVelocity() / v.getHeight());
			} else {
				mZoomControl.startFling(0, 0);
			}

			mVelocityTracker.recycle();
			mVelocityTracker = null;
			mMode = Mode.UNDEFINED;
			break;
		}
		default: {
			mVelocityTracker.recycle();
			mVelocityTracker = null;
			mMode = Mode.UNDEFINED;
			break;
		}
		}

		return true;
	}

	/**
	 * Sets the zoom control to manipulate
	 * 
	 * @param control
	 *            Zoom control
	 */
	public void setZoomControl(DynamicZoomControl control) {
		mZoomControl = control;
	}
}
