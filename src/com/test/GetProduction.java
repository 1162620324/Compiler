package com.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.util.FirstAndFollow;
import com.util.GrammarUtil;

public class GetProduction {
	public List<String> proList = null; //接收产生式集合
	
	public Set<String> graSymSet = null; //文法符号
	
	public Map<String,Set<String>> firsts = null;
	public Map<String,Set<String>> follows = null;
	
	public static Map<String,Integer> SYM_INT = new HashMap<String,Integer>(); //键值对：文法符号和其编号（没有开始符号）
	
	public static Map<Integer,String> INT_SYM = new HashMap<Integer,String>();//键值对：其编号和文法符号（没有开始符号）
	
	public void getPro(String filename) {
		proList = GrammarUtil.setGrammar(filename);//获取文法中的产生式
		graSymSet = GrammarUtil.getGrammarSymbol(proList);//获得文法符号集合		
		if(graSymSet==null){
			System.out.println("文法格式有问题，请检查文法！");
		}else{
			FirstAndFollow ff = new FirstAndFollow(proList, graSymSet);
			firsts = ff.getFirst();
			follows = ff.getFollow();
			//设置键值对：文法符号——数字
			if(graSymSet!=null){
				int i=0;
				for(String sym:graSymSet){
					SYM_INT.put(sym, i);
					i++;
				}
				SYM_INT.put("$",i);
				for(Map.Entry<String, Integer> entry : SYM_INT.entrySet()){
					INT_SYM.put(entry.getValue(), entry.getKey());
				}
			}
		}
	}
}
