package org.falconia.mangaproxy.ui;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

@Deprecated
public class ProgressView extends RelativeLayout {

	private final TextView mtvLoadingMsg;

	public ProgressView(Context context, AttributeSet attrs) {
		super(context, attrs);

		setClickable(false);
		setBackgroundColor(0xDD000000);// 0xDD000000
		this.mtvLoadingMsg = new TextView(context);
		this.mtvLoadingMsg.setTextColor(Color.WHITE);// 0xFFFFFF
		LayoutParams layoutParams = new LayoutParams(
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		layoutParams.addRule(CENTER_IN_PARENT);
		this.mtvLoadingMsg.setLayoutParams(layoutParams);
		addView(this.mtvLoadingMsg);
	}

	public void startProgress(String string) {
		setVisibility(VISIBLE);
		if (TextUtils.isEmpty(string))
			this.mtvLoadingMsg.setText(null);
		else
			this.mtvLoadingMsg.setText(string);
	}

	public void stopProgress() {
		setVisibility(GONE);
		this.mtvLoadingMsg.setText(null);
	}

}
