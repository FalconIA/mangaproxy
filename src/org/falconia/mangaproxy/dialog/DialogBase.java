package org.falconia.mangaproxy.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;

@Deprecated
public abstract class DialogBase extends Dialog {

	public DialogBase(Context context) {
		super(context);
		getWindow().setBackgroundDrawable(new ColorDrawable(0x000000));
	}

}
