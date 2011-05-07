package org.falconia.mangaproxy;

import org.falconia.mangaproxy.ui.ImageZoomOnTouchListener;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

import com.sonyericsson.zoom.DynamicZoomControl;
import com.sonyericsson.zoom.ImageZoomView;
import com.sonyericsson.zoom.ZoomState.AlignX;
import com.sonyericsson.zoom.ZoomState.AlignY;

public final class DebugActivity extends Activity {

	private ImageZoomView mZoomView;
	private DynamicZoomControl mZoomControl;

	private Bitmap mBitmap;

	private ImageZoomOnTouchListener mZoomListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.acrivity_debug);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		mZoomControl = new DynamicZoomControl();

		mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.download);

		mZoomListener = new ImageZoomOnTouchListener(getApplicationContext());
		mZoomListener.setZoomControl(mZoomControl);

		mZoomView = (ImageZoomView) findViewById(R.id.mvDebug);
		mZoomView.setZoomState(mZoomControl.getZoomState());
		mZoomView.setImage(mBitmap);
		mZoomView.setOnTouchListener(mZoomListener);

		mZoomControl.setAspectQuotient(mZoomView.getAspectQuotient());

		mZoomView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				// TODO Auto-generated method stub
				AppUtils.logV(this, "onGlobalLayout()");

				resetZoomState();
			}
		});
	}

	@Override
	public void onBackPressed() {
		finish();
		System.exit(0);
	}

	private void resetZoomState() {
		AppUtils.logD(this, "ZoomView Width: " + mZoomView.getWidth());
		AppUtils.logD(this, "ZoomView Height: " + mZoomView.getHeight());
		AppUtils.logD(this, "AspectQuotient: " + mZoomView.getAspectQuotient().get());

		mZoomControl.getZoomState().setAlignX(AlignX.Right);
		mZoomControl.getZoomState().setAlignY(AlignY.Top);
		mZoomControl.getZoomState().setPanX(0.0f);
		mZoomControl.getZoomState().setPanY(0.0f);
		// mZoomControl.getZoomState().setZoom(2f);
		mZoomControl.getZoomState().setZoom(1 / mZoomView.getAspectQuotient().get());
		mZoomControl.getZoomState().notifyObservers();
	}
}
