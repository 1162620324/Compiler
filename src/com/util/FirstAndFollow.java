package com.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.test.SomeWord;

public class FirstAndFollow {
	//获取文法符号的first集和follow集合
	
	// 成员变量,产生式集，终结符集，非终结符集
	ArrayList<Production> productions;
	ArrayList<String> terminals;
	ArrayList<String> nonterminals;
	HashMap<String, Set<String>> firsts;
	HashMap<String, Set<String>> follows;
	
	private List<String> proList = new ArrayList<String>();
	private Set<String> graSymSet = new HashSet<String>();
	
	private static Set<String> isFindFirst = new HashSet<String>();//判断是否找过了first

	public FirstAndFollow(List<String> proList,Set<String> graSymSet) {
		this.proList  = proList;
		this.graSymSet = graSymSet;
		productions = new ArrayList<Production>();
		terminals = new ArrayList<String>();
		nonterminals = new ArrayList<String>();
		firsts = new HashMap<String, Set<String>>();
		follows = new HashMap<String, Set<String>>();
		setProductions();
		setSym();
		//getFirst();
		//getFollow();
	}

	// 读产生式
	public void setProductions() {
		String left;
		String right;
		Production production;
		for(String pro:proList){
			left = pro.split("->")[0].trim();
			right = pro.split("->")[1].trim();
			production = new Production(left, right.split(" "));
			productions.add(production);
		}
	}

	// 获得非终结符集
	public void setSym() {
		for(String sym : graSymSet){
			if(Character.isUpperCase(sym.charAt(0))){
				nonterminals.add(sym);
			}else{
				if(sym.equals(SomeWord.EMPTY)){
					continue;
				}
				terminals.add(sym);
			}
		}
		//System.out.println("终结符size："+terminals.size()+"，非终结符size："+nonterminals.size());
	}
		
	// 获取First集
	public Map<String,Set<String>> getFirst() {
		// 终结符全部求出first集
		Set<String> first;
		for (int i = 0; i < terminals.size(); i++) {//终结符的first集合就是自己
			first = new HashSet<String>();
			first.add(terminals.get(i));
			firsts.put(terminals.get(i), first);
		}
		// 给所有非终结符注册一下
		for (int i = 0; i < nonterminals.size(); i++) {
			first = new HashSet<String>();
			firsts.put(nonterminals.get(i), first);
		}
		String left;
		String right;
		String[] rights;
		for (int i = 0; i < productions.size(); i++) {
			left = productions.get(i).returnLeft();
			if(isFindFirst.contains(left)){
				continue;
			}
			rights = productions.get(i).returnRights();
			right = rights[0];
			getFirstRecur(left,right);
		}

		//最后处理
		//添加空串
		for(Production pro:productions){
			left = pro.returnLeft();
			rights = pro.returnRights();
			if(rights.length==1 && rights[0].equals(SomeWord.EMPTY)){
				firsts.get(left).add(SomeWord.EMPTY);
			}
		}
		//求first集合是针对符号串的，所以要对产生式右部符号的first集合有选择的进行合并
		while(true){
			boolean change = false;
			//System.out.println("=========");
			for(Production pro:productions){
				left = pro.returnLeft();
				String s = left;
				rights = pro.returnRights();
				if(nonterminals.contains(rights[0])){//右部第一个符号是非终结符
					int firstSize = firsts.get(s).size();
					for(int i=0;i<rights.length;i++){
						if(!firsts.get(rights[i]).contains(SomeWord.EMPTY)){
							break;
						}
						//System.out.println("符号："+rights[i]+"，包含空串");
						if(i+1<rights.length){//后一个符号，如果有
							//System.out.println("添加");
							firsts.get(s).addAll(firsts.get(rights[i+1]));
						}
					}
					int firstSizeNew = firsts.get(s).size();
					boolean temp;
					if(firstSizeNew == firstSize){
						//System.out.println("不变："+left);
						temp =false;
					}else{
						//System.out.println("变："+left);
						//System.out.println(firstSize+"，"+firstSizeNew);
						temp = true;
					}
					change = change ||  temp;
					//System.out.println(change);
				}
			}
			if(!change){
				break;
			}
		}
//		for(Map.Entry<String, Set<String>> entry : firsts.entrySet()){
//			System.out.println("符号："+entry.getKey()+"的first集合：");
//			for(String sym : entry.getValue()){
//				System.out.println(sym);
//			}
//		}
//		System.out.println("结束");
		
		return firsts;
	}
	
	//递归查找first集合
	/**
	 * @param left，产生式左部符号
	 * @param right，产生式右部符号集合中的第一个符号
	 * @return
	 */
	public Set<String> getFirstRecur(String left,String right){
		if(left.equals(right)){
			return null;
		}
		Set<String> firstSet = new HashSet<String>();
		if(Character.isUpperCase(right.charAt(0))){ ///非终结符
			isFindFirst.add(right);//分析过的非终结符
			String nextLeft,nextRight;
			nextLeft = right;
			for (int i = 0; i < productions.size(); i++) {
				if(productions.get(i).returnLeft().equals(right)){
					nextRight = productions.get(i).returnRights()[0];
					//System.out.println("符号："+nextLeft+"，"+nextRight);
					if(getFirstRecur(nextLeft, nextRight)!=null){
						firstSet.addAll(getFirstRecur(nextLeft, nextRight));
						if(firstSet!=null && firstSet.size()!=0){
							firstSet.addAll(firstSet);
						}
					}
				}
			}
			if(firstSet!=null&& firstSet.size()!=0){
				firsts.get(left).addAll(firstSet);
			}
		}else if(!right.equals(SomeWord.EMPTY)){//只添加终结符，空串最后处理
			//System.out.println("将终结符："+right+"加入到："+left+"的first集");
			firstSet.add(right);
			firsts.get(left).add(right);
		}
		return firstSet;
	}
	
	private Set<String> recurSet = new HashSet<String>();

	// 获得Follow集
	public Map<String, Set<String>> getFollow(){
		// 所有非终结符的follow集初始化一下
		Set<String> follow  = null;
		for (int i = 0; i < nonterminals.size(); i++) {
			follow = new HashSet<String>();
			follows.put(nonterminals.get(i), follow);
		}
		System.out.println();
		//产生式右部中最右符号的follow集合加入$
		for(Production p:productions){
			String[] rights = p.returnRights();
			String sym_right_most = rights[rights.length-1];
			if(nonterminals.contains(sym_right_most)){
				//System.out.println("符号："+rights[rights.length-1]);
				follows.get(sym_right_most).add("$");
			}
		}
		for (int i = 0; i < nonterminals.size(); i++) {//每个非终结符
			//System.out.println("当前非终结符："+nonterminals.get(i));
			String left,cur,cur_right;
			String[] rights;
			cur = nonterminals.get(i);
			PRO:for(int j=0;j<productions.size();j++){ //遍历全部的产生式
				rights = productions.get(j).returnRights();
				for(int k=0;k<rights.length;k++){//遍历产生式右部
					if(rights[k]!=null){
						if(rights[k].equals(cur)){//非终结符存在于产生式右部
							left = productions.get(j).returnLeft();
							if(k+1==rights.length){//非终结符位于产生式最右
								cur_right = null;
							}
							else{
								cur_right = rights[k+1];//非终结符右边的符号
								if(!Character.isUpperCase(cur_right.charAt(0))){//是终结符
									if(follows.get(cur).contains(cur_right)){
										//System.out.println(cur_right+"已经在"+cur+"的follow集合了");
										continue PRO;
									}
								}else{//是非终结符
									boolean flag = true; //当前分析的非终结符是否可能为最右符号
									int k1;
									for( k1 = k+1;k1<rights.length;k1++){
										String sym = rights[k1];
										if(terminals.contains(sym)){
											flag = false;
											break;
										}
										if(!firsts.get(rights[k1]).contains(SomeWord.EMPTY)){
											flag = false;
											break;
										}
									}
									if(flag){
										follows.get(cur).add("$");
									}
								}
							}
							getFollowRecur(j,left,cur,cur_right,k);
						}
					}
				}
			}
		}
		
//		for(Map.Entry<String, Set<String>> entry : follows.entrySet()){
//			System.out.println("非终结符："+entry.getKey()+"的follow集合：");
//			for(String sym : entry.getValue()){
//				System.out.println(sym);
//			}
//		}
//		System.out.println("结束");
		return follows;
	}
	

	
	//递归查找follow的集合，
	/**
	 * @param proPosition：当前非终结符所在产生式的编号
	 * @param left：当前非终结符对应的产生式左部的符号
	 * @param cur：当前非终结符
	 * @param cur_right：当前非终结符右边的符号
	 * @param cur_rightPosition：当前非终结符右边的符号，在产生式右部符号集合的位置
	 */
	public void getFollowRecur(int proPosition,String left,String cur,String cur_right,int cur_rightPosition) {
		if(cur_right==null){
//			System.out.println("空");
//			System.out.println("产生式："+proList.get(proPosition));
//			System.out.println("产生式左部："+left+"，当前符号："+cur);
			if(left.equals(cur)){
				//System.out.println(left+"，"+cur+"，不用找了");
				return;
			}
			if(recurSet.contains(left)){
				//System.out.println(left+"的follow已经找过了");
				if(follows.get(left)!=null){
					follows.get(cur).addAll(follows.get(left));
				}
				return;
			}
			String[] rights;
			String left_new,cur_new,cur_right_new;
			PRO:for(int j=0;j<productions.size();j++){ //遍历全部的产生式
				rights = productions.get(j).returnRights();
				for(int k=0;k<rights.length;k++){//遍历产生式右部
					if(rights[k]!=null){
						if(rights[k].equals(left)){//非终结符left存在于产生式右部
							cur_new = left;
							left_new = productions.get(j).returnLeft();
							if(k+1==rights.length){//非终结符位于产生式最右
								cur_right_new = null;
							}
							else{
								cur_right_new = rights[k+1];//非终结符右边的符号
								if(!Character.isUpperCase(cur_right_new.charAt(0))){//终结符
									if(follows.get(cur).contains(cur_right_new)){
										//System.out.println(cur_right_new+"已经在"+cur+"的follow集合了");
										follows.get(cur).addAll(follows.get(cur_new));
										continue PRO;
									}
								}
							}
							recurSet.add(left);
							getFollowRecur(j,left_new,cur_new,cur_right_new,k);
							if(follows.get(cur_new)!=null){
								follows.get(cur).addAll(follows.get(cur_new));
							}
						}
					}
					
				}
			}
		}else{
			//System.out.println("产生式："+proList.get(proPosition));
			//System.out.println("产生式左部："+left+"，当前符号："+cur+"，右边的符号："+cur_right+"，符号位置："+cur_rightPosition);
			if(Character.isUpperCase(cur_right.charAt(0))){//当前非终结符的右边是非终结符
				if(firsts.get(cur_right)!=null){
					follows.get(cur).addAll(firsts.get(cur_right));
					if(firsts.get(cur_right).contains(SomeWord.EMPTY)){//cur_right的first集合中有空串empty
						follows.get(cur).remove(SomeWord.EMPTY);//去掉empty
						String[] rights = productions.get(proPosition).returnRights();
						if(cur_rightPosition+2==rights.length){//用空代替后，位于最后位置
							getFollowRecur(proPosition,left,cur,null,cur_rightPosition);
						}else if(cur_rightPosition+2<rights.length){////用空代替后，后面还有符号
							getFollowRecur(proPosition,left,cur,rights[cur_rightPosition+1],cur_rightPosition+1);
						}
					}
				}
			}
			else{//当前非终结符的右边是终结符，加入到follow集
				follows.get(cur).add(cur_right);
			}
		}
	}

}