package com.util;

import java.util.Set;

public class ItemSetCombine {
	//ͬ�ĵ���Ŀ���ۺϺ����Ŀ��
	
	private int[] position_all; //��¼�ۺ�ǰ�ĸ�����Ŀ����λ����Ϣ 
	
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
