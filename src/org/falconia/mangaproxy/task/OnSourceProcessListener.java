package org.falconia.mangaproxy.task;

public interface OnSourceProcessListener {

	int onSourceProcess(String source);

	void onPreSourceProcess();

	void onPostSourceProcess(int size);

}
