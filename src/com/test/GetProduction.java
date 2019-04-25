package com.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.util.FirstAndFollow;
import com.util.GrammarUtil;

public class GetProduction {
	public List<String> proList = null; //���ղ���ʽ����
	
	public Set<String> graSymSet = null; //�ķ�����
	
	public Map<String,Set<String>> firsts = null;
	public Map<String,Set<String>> follows = null;
	
	public static Map<String,Integer> SYM_INT = new HashMap<String,Integer>(); //��ֵ�ԣ��ķ����ź����ţ�û�п�ʼ���ţ�
	
	public static Map<Integer,String> INT_SYM = new HashMap<Integer,String>();//��ֵ�ԣ����ź��ķ����ţ�û�п�ʼ���ţ�
	
	public void getPro(String filename) {
		proList = GrammarUtil.setGrammar(filename);//��ȡ�ķ��еĲ���ʽ
		graSymSet = GrammarUtil.getGrammarSymbol(proList);//����ķ����ż���		
		if(graSymSet==null){
			System.out.println("�ķ���ʽ�����⣬�����ķ���");
		}else{
			FirstAndFollow ff = new FirstAndFollow(proList, graSymSet);
			firsts = ff.getFirst();
			follows = ff.getFollow();
			//���ü�ֵ�ԣ��ķ����š�������
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
