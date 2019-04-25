package com.util;

public class ProductionTree {
	private int proPosition;
	private String[] keys;
	
	public ProductionTree(int proPosition,String[] keys){
		this.proPosition = proPosition;
		this.keys = keys;
	}

	public int getProPosition() {
		return proPosition;
	}


	public String[] getKeys() {
		return keys;
	}
	
}
