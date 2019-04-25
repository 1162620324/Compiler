package com.util;

import java.util.Set;

public class ItemSetCombine {
	//同心的项目集聚合后的项目集
	
	private int[] position_all; //记录聚合前的各个项目集的位置信息 
	
	private Set<ItemLALR> itemSet;
	
	public ItemSetCombine(int[] position_all,Set<ItemLALR> itemSet){
		this.position_all = position_all;
		this.itemSet = itemSet;
	}
	
	public int[] returnPosition_all() {
		return position_all;
	}

	public Set<ItemLALR> returnItemSet() {
		return itemSet;
	}
}
