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
	//�﷨����������
	//��ȡ�ķ��еĲ���ʽ
	public static List<String> setGrammar(String path) { //�����ķ���Ҫ���ķ����ż�����пո�
			// TODO Auto-generated method stub
			File file = new File(path);
			List<String> proList = new ArrayList<String>(); //��ȡ����ʽ
			try {
				BufferedReader br = 
						new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
				int tempbyte;
				String production = "";
				while ((tempbyte = br.read()) != -1) {
					if((char) tempbyte=='|'){//���ò���ʽ��ֿ�
						if(!production.trim().isEmpty())
							proList.add(production);
						int pos = production.indexOf("->");
						production = production.substring(0, pos+2);//��ȡ��->
					}else if((char) tempbyte!='\r' && (char) tempbyte!='\n'){
						production +=(char) tempbyte;
					}else if(!production.equals("")){//��ֹ�����մ�
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
			
			List<String> proList2 = new ArrayList<String>(); //��\tΪ�ո�
			System.out.println("����ʽ������"+proList.size());
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
				
		//��ȡ�ķ����ż���
		public static Set<String> getGrammarSymbol(List<String> proList){//��ȡ�ķ��е��ķ����ţ����ظ�
			Set<String> graSymSet = new HashSet<String>();
			String[] strs,strs_2 = {};
			for(String pro : proList){
				strs = pro.split("->");
				strs[0] = strs[0].trim();
				if(!strs[0].isEmpty()){
					graSymSet.add(strs[0]);
				}
				//����ʣ�µ��ַ���strs[1]
				try{
					strs_2 = strs[1].split(" ");//�Կո�ָ�õ�һ�����ķ�����
					for(int j=0;j<strs_2.length;j++){
						strs_2[j] = strs_2[j].trim();
						if(!strs_2[j].isEmpty()){
							if(!strs_2[j].equals(SomeWord.EMPTY)){//�մ�������
								graSymSet.add(strs_2[j]);
							}
						}
					}
				}catch(Exception e){
					System.out.println("�ķ���ʽ����");
					return null;
				}
			}
			graSymSet.remove("");
			System.out.println("�ķ����Ÿ�����"+graSymSet.size());
			//for(String sym : graSymSet){
				//System.out.println("�ķ����ţ�"+sym);
			//}
			return graSymSet;
		}
		
	
}
