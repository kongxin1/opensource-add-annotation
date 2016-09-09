package com.kx.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IntegerAndHtmlMap {
	public static Map<Integer, List<String>> map = null;
	static {
		map = new HashMap<Integer, List<String>>();
		List<String> list = new ArrayList<String>();
		list.add("MenuParser");
		map.put(1, list);
		list = new ArrayList<String>();
		list.add("ItemParser");
		list.add("NextPageParser");
		map.put(2, list);
		list = new ArrayList<String>();
		list.add("AlbumParser");
		map.put(3, list);
		list = new ArrayList<String>();
		list.add("ItemInAlbumParser");
		list.add("NextPageParser");
		map.put(4, list);
		list = new ArrayList<String>();
		list.add("ContentInItemOfAlbumParser");
		map.put(5, list);
		list = new ArrayList<String>();
		list.add("MusicParser");
		map.put(6, list);
	}
}
