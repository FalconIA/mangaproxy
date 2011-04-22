package org.falconia.mangaproxy.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;

public abstract class ADialogBase extends Dialog {

	public ADialogBase(Context context) {
		super(context);
		getWindow().setBackgroundDrawable(new ColorDrawable(0x000000));
	}

}
