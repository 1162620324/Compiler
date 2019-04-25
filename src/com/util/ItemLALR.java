package com.util;

public class ItemLALR {
	//LALR(1)µÄÏîÄ¿
	
	Production pro_item;
	String search;

	public ItemLALR(Production pro_item, String search) {
		this.pro_item = pro_item;
		this.search = search;
	}

	public String returnSearch() {
		return search;
	}

	public Production returnProItem() {
		return pro_item;
	}
}
