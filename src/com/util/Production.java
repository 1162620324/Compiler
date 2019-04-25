package com.util;

public class Production {
	//划分产生式的泛型
	
	String left;
	String[] rights;

	public Production(String left, String[] rights) {
		this.left = left;
		this.rights = new String[rights.length];
		int j = 0;
		for(int i=0;i<rights.length;i++){
			if(!rights[i].trim().isEmpty()){
				this.rights[j] = rights[i];
				j++;
			}
		}
	}

	public String[] returnRights() {
		return rights;
	}

	public String returnLeft() {
		return left;
	}
}
