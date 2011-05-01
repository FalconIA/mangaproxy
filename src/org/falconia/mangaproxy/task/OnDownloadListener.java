package org.falconia.mangaproxy.task;

public interface OnDownloadListener {

	public void onPreDownload();

	public void onPostDownload(String result);

}
