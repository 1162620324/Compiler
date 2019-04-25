package com.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.test.SomeWord;

public class GrammarUtil {
	//语法分析工具类
	//获取文法中的产生式
	public static List<String> setGrammar(String path) { //导入文法，要求文法符号间必须有空格
			// TODO Auto-generated method stub
			File file = new File(path);
			List<String> proList = new ArrayList<String>(); //获取产生式
			try {
				BufferedReader br = 
						new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
				int tempbyte;
				String production = "";
				while ((tempbyte = br.read()) != -1) {
					if((char) tempbyte=='|'){//将该产生式拆分开
						if(!production.trim().isEmpty())
							proList.add(production);
						int pos = production.indexOf("->");
						production = production.substring(0, pos+2);//截取到->
					}else if((char) tempbyte!='\r' && (char) tempbyte!='\n'){
						production +=(char) tempbyte;
					}else if(!production.equals("")){//防止遇到空串
						if(!production.trim().isEmpty())
							proList.add(production);
						production = "";
					}
				}
				if(!production.trim().isEmpty())
					proList.add(production);
				br.close();
			} catch (Exception event) {
				event.printStackTrace();
			}
			
			List<String> proList2 = new ArrayList<String>(); //换\t为空格
			System.out.println("产生式个数："+proList.size());
			for(String pro:proList){
				pro = pro.replace("\t", " ");
				while(pro.charAt(pro.length()-1)==' '){
					pro = pro.substring(0,pro.length()-1);
				}
				//System.out.println(pro);
				proList2.add(pro);
			}
			return proList2;
		}
				
		//获取文法符号集合
		public static Set<String> getGrammarSymbol(List<String> proList){//获取文法中的文法符号，无重复
			Set<String> graSymSet = new HashSet<String>();
			String[] strs,strs_2 = {};
			for(String pro : proList){
				strs = pro.split("->");
				strs[0] = strs[0].trim();
				if(!strs[0].isEmpty()){
					graSymSet.add(strs[0]);
				}
				//处理剩下的字符串strs[1]
				try{
					strs_2 = strs[1].split(" ");//以空格分割，得到一个个文法符号
					for(int j=0;j<strs_2.length;j++){
						strs_2[j] = strs_2[j].trim();
						if(!strs_2[j].isEmpty()){
							if(!strs_2[j].equals(SomeWord.EMPTY)){//空串不加入
								graSymSet.add(strs_2[j]);
							}
						}
					}
				}catch(Exception e){
					System.out.println("文法格式错误");
					return null;
				}
			}
			graSymSet.remove("");
			System.out.println("文法符号个数："+graSymSet.size());
			//for(String sym : graSymSet){
				//System.out.println("文法符号："+sym);
			//}
			return graSymSet;
		}
		
	
}
