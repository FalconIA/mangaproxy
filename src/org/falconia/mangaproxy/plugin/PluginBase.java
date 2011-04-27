package org.falconia.mangaproxy.plugin;

import org.falconia.mangaproxy.data.MangaList;
import org.falconia.mangaproxy.helper.HttpHelper;
import org.falconia.mangaproxy.helper.Regex;

public abstract class PluginBase implements IPlugin {
	protected static final String CHARSET_GBK = HttpHelper.CHARSET_GBK;
	protected static final String CHARSET_UTF8 = HttpHelper.CHARSET_UTF8;

	protected static final String DEFAULT_MANGA_URL_PREFIX = "comic/";
	protected static final String DEFAULT_MANGA_URL_POSTFIX = "/";

	protected int miSiteId;

	public PluginBase(int siteId) {
		this.miSiteId = siteId;
	}

	@Override
	public int getSiteId() {
		return this.miSiteId;
	}

	@Override
	public String getMangaUrl(int mangaId) {
		return getUrlBase() + getMangaUrlPrefix() + mangaId
				+ getMangaUrlPostfix();
	}

	protected String getGenreUrl(String genreId) {
		return getUrlBase() + genreId.replace('-', '/') + "/";
	}

	@Override
	public MangaList getMangaList(String source, int genreId) {
		return getMangaList(source, genreId, 1);
	}

	@Override
	public String getAllMangaListUrl() {
		return getGenreAllUrl();
	}

	@Override
	public MangaList getAllMangaList(String source) {
		return getAllMangaList(source, 1);
	}

	protected String checkId(String string) {
		string = Regex.match("^[\\s/]*(.+?)[\\s/]*$", string).get(1);
		return string.trim().replace('/', '-');
	}

	protected String checkName(String string) {
		return string.trim();
	}

	protected String checkGenreName(String string) {
		return checkName(string);
	}

	protected int bitsToInt(String string) {
		int result = 0;
		byte[] bits = string.getBytes();
		for (int n = bits.length - 1; n >= 0; n--)
			result = (result << 8) + bits[n];
		return result;
	}

	protected String intToBits(int i) {
		byte[] bits = new byte[4];
		for (int n = 0; n < 4; n++) {
			bits[n] = (byte) (i % 256);
			i /= 256;
		}
		return new String(bits);
	}

}
