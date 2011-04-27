package org.falconia.mangaproxy.menu;

import org.falconia.mangaproxy.R;
import org.falconia.mangaproxy.dialog.DialogBase;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public abstract class DialogMenuBase extends DialogBase implements
		View.OnClickListener {
	public static final int ID_ADD_TO_FAVORITES = 1;
	public static final int ID_CLEAR_HIGHLIGHT = 2;
	public static final int ID_REMOVE_FROM_FAVORITES = 3;
	public static final int ID_VIEW_SUMMARY = 4;

	private final View.OnClickListener mhOnBackClick;
	private final View.OnClickListener mhOnMenuItemClick;
	private IOnMenuItemClickListener mhOnMenuItemClickListener;

	private LayoutInflater m_hInflater;

	private TextView mtvMenuTitle;
	private LinearLayout mvgMenu;

	public DialogMenuBase(Activity activity,
			IOnMenuItemClickListener onMenuItemClickListener) {
		super(activity);

		this.mhOnBackClick = this;
		this.mhOnMenuItemClick = this;
		this.mhOnMenuItemClickListener = onMenuItemClickListener;

		this.m_hInflater = LayoutInflater.from(activity);

		setCancelable(true);
		setContentView(R.layout.a_base_dialog_menu);

		this.mvgMenu = (LinearLayout) findViewById(R.id.mvgMenu);

		init(activity);

		findViewById(R.id.mbtnBack).setOnClickListener(this.mhOnBackClick);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.mbtnBack:
			dismiss();
			break;
		case ID_ADD_TO_FAVORITES:
			break;
		case ID_CLEAR_HIGHLIGHT:
			break;
		case ID_REMOVE_FROM_FAVORITES:
			break;
		case ID_VIEW_SUMMARY:
			break;
		default:
			dismiss();
			this.mhOnMenuItemClickListener.onMenuItemClick(v.getId());
			break;
		}
	}

	private void init(Activity activity) {
		addMenuTitle();
		generateMenuItems();
	}

	private void addMenuTitle() {
		View mvgMenuTitle = this.m_hInflater.inflate(
				R.layout.default_dialog_title_view, null);
		this.mtvMenuTitle = (TextView) mvgMenuTitle
				.findViewById(R.id.mtvDialogTitle);
		this.mvgMenu.addView(mvgMenuTitle);
	}

	public void setMenuTitle(String title) {
		this.mtvMenuTitle.setText(title);
	}

	protected abstract void generateMenuItems();

	protected void addMenuItem(String menuTitle, int menuImage, int menuItemId) {
		View menuItem = this.m_hInflater.inflate(R.layout.dialog_menu_item,
				null);
		menuItem.setId(menuItemId);
		menuItem.setOnClickListener(this.mhOnMenuItemClick);
		((TextView) menuItem.findViewById(R.id.mtvMenuItem)).setText(menuTitle);
		((ImageView) menuItem.findViewById(R.id.mivMenuItem))
				.setImageResource(menuImage);
		this.mvgMenu.addView(menuItem);
	}

	public View getMenuItem(int id) {
		return this.mvgMenu.findViewById(id);
	}

}
