package org.falconia.mangaproxy;

import org.falconia.mangaproxy.AppConst.ZoomMode;
import org.falconia.mangaproxy.ui.ImageZoomOnTouchListener;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

import com.sonyericsson.zoom.DynamicZoomControl;
import com.sonyericsson.zoom.ImageZoomView;
import com.sonyericsson.zoom.ZoomState.AlignX;
import com.sonyericsson.zoom.ZoomState.AlignY;

public final class DebugActivity extends Activity {

	private final class Configuration {
		private ZoomMode mZoomMode;
	}

	private Bitmap mBitmap;

	private ZoomMode mZoomMode = ZoomMode.FIT_WIDTH_AUTO_SPLIT;

	private ImageZoomView mZoomView;
	private DynamicZoomControl mZoomControl;
	private MenuItem mmiZoomFitWidthAutoSplit;
	private MenuItem mmiZoomFitWidth;
	private MenuItem mmiZoomFitHeight;
	private MenuItem mmiZoomFitScreen;

	private ImageZoomOnTouchListener mZoomListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.acrivity_debug);
		// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		Configuration conf = (Configuration) getLastNonConfigurationInstance();
		if (conf != null) {
			mZoomMode = conf.mZoomMode;
		}

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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_chapter, menu);
		mmiZoomFitWidthAutoSplit = menu.findItem(R.id.mmiZoomFitWidthAutoSplit);
		mmiZoomFitWidthAutoSplit.setChecked(mZoomMode == ZoomMode.FIT_WIDTH_AUTO_SPLIT);
		mmiZoomFitWidth = menu.findItem(R.id.mmiZoomFitWidth);
		mmiZoomFitWidth.setChecked(mZoomMode == ZoomMode.FIT_WIDTH);
		mmiZoomFitHeight = menu.findItem(R.id.mmiZoomFitHeight);
		mmiZoomFitHeight.setChecked(mZoomMode == ZoomMode.FIT_HEIGHT);
		mmiZoomFitScreen = menu.findItem(R.id.mmiZoomFitScreen);
		mmiZoomFitScreen.setChecked(mZoomMode == ZoomMode.FIT_SCREEN);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getGroupId() == R.id.mmgZoomGroup) {
			switch (item.getItemId()) {
			case R.id.mmiZoomFitWidthAutoSplit:
				mZoomMode = ZoomMode.FIT_WIDTH_AUTO_SPLIT;
				break;
			case R.id.mmiZoomFitWidth:
				mZoomMode = ZoomMode.FIT_WIDTH;
				break;
			case R.id.mmiZoomFitHeight:
				mZoomMode = ZoomMode.FIT_HEIGHT;
				break;
			case R.id.mmiZoomFitScreen:
				mZoomMode = ZoomMode.FIT_SCREEN;
				break;
			}
			mmiZoomFitWidthAutoSplit.setChecked(mZoomMode == ZoomMode.FIT_WIDTH_AUTO_SPLIT);
			mmiZoomFitWidth.setChecked(mZoomMode == ZoomMode.FIT_WIDTH);
			mmiZoomFitHeight.setChecked(mZoomMode == ZoomMode.FIT_HEIGHT);
			mmiZoomFitScreen.setChecked(mZoomMode == ZoomMode.FIT_SCREEN);
			mZoomControl.getZoomState().setDefaultZoom(calcuZoom(mZoomMode, mZoomView, mBitmap));
			mZoomControl.getZoomState().notifyObservers();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		Configuration conf = new Configuration();
		conf.mZoomMode = mZoomMode;
		return conf;
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
		mZoomControl.getZoomState().setDefaultZoom(calcuZoom(mZoomMode, mZoomView, mBitmap));
		mZoomControl.getZoomState().notifyObservers();
	}

	private float calcuZoom(ZoomMode mode, ImageZoomView view, Bitmap bitmap) {
		if (view.getAspectQuotient() == null || view.getAspectQuotient().get() == Float.NaN)
			return 1f;
		if (view == null || view.getWidth() == 0 || view.getHeight() == 0)
			return 1f;
		if (bitmap == null || bitmap.getWidth() == 0 || bitmap.getHeight() == 0)
			return 1f;

		if (mode == ZoomMode.FIT_SCREEN)
			return 1f;

		// aq = (bW / bH) / (vW / vH)
		float aq = view.getAspectQuotient().get();
		float zoom = 1f;

		if (mode == ZoomMode.FIT_WIDTH || mode == ZoomMode.FIT_WIDTH_AUTO_SPLIT) {
			// Over height
			if (aq < 1f) {
				zoom = 1f / aq;
			} else {
				zoom = 1f;
			}

			if (mode == ZoomMode.FIT_WIDTH_AUTO_SPLIT) {
				if (1f * bitmap.getWidth() / view.getWidth() > 1.5f
						&& bitmap.getWidth() > bitmap.getHeight()) {
					zoom *= 2f;
				}
			}
		} else if (mode == ZoomMode.FIT_HEIGHT) {
			// Over width
			if (aq > 1f) {
				zoom = aq;
			} else {
				zoom = 1f;
			}
		}

		return zoom;
	}
}
