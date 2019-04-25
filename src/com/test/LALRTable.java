package com.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.util.ItemLALR;
import com.util.ItemSetCombine;
import com.util.Production;


public class LALRTable {
	//分析表
	
	private List<String> proList = new ArrayList<String>(); //接收产生式集合
	
	private List<Production> productions = new ArrayList<Production>(); //产生式新的存储方式
	
	private Set<String> graSymSet = null; //文法符号
	
	public  List<String[]> token = null;//token序列
	
	private List<Set<ItemLALR>> itemSetCluster = new ArrayList<Set<ItemLALR>>(); //项目集规范簇
	
	private List<ItemSetCombine> itemSetClusterLALR = new ArrayList<ItemSetCombine>();
	
	private String[][] action = null; //LALR分析表
	
	private Map<String,Set<String>> firsts = null;
	

	public LALRTable(List<String> proList, Set<String> graSymSet,Map<String,Set<String>> firsts) {
		// TODO Auto-generated constructor stub
		this.graSymSet = graSymSet;
		this.proList = proList;
		this.firsts = firsts;
		setProductions();
	}
	
	//将产生式集合换种方式存储
	public void setProductions() {
		String left;
		String right;
		Production production;
		for(String pro : proList){
			left = pro.split("->")[0].trim();
			right = pro.split("->")[1].trim();
			production = new Production(left, right.split(" "));
			productions.add(production);
		}
	}
	
	//构造LALR(1)分析表
	public String[][] getLALRTable(){
		String[][] triple = getItemSetSet();
		int m = itemSetClusterLALR.size(), n = graSymSet.size() + 1;
		action = new String[m][n];
		for(int i = 0; i < m; i++){
			for(int j = 0; j < n - 1; j++){
				if(triple[i][j] != null){
					if(Character.isUpperCase(GetProduction.INT_SYM.get(j).charAt(0))){
						action[i][j] = triple[i][j];
					}else{
						action[i][j] = "s" + triple[i][j];
					}
				}else{
					action[i][j] = "##";
				}
			}
		}
		for(int i = 0; i < m; i++){ //$符号的列
			action[i][n-1] = "##";
		}	
		//填rj和acc
		Production pro_item;
		String left_item;
		String[] rights_item;//项目中的产生式的右部
		String search;
		String left;//左部
		String[] rights = null;//项目对应的产生式
		int status = -1;
		boolean flag = false;
		for(ItemSetCombine itemLALR : itemSetClusterLALR){
			status++;//项目集的位置，即分析表中的状态
			for(ItemLALR item : itemLALR.returnItemSet()){
				pro_item = item.returnProItem();
				rights_item = pro_item.returnRights();
				if(rights_item[rights_item.length - 1].equals(".")){
					left_item = pro_item.returnLeft();
					String[] rights_without_point = new String[rights_item.length-1];
					for(int i = 0; i < rights_without_point.length; i++){
						rights_without_point[i] = rights_item[i];
					}
					if(!flag) {
						left = productions.get(0).returnLeft();
						rights = productions.get(0).returnRights();
						flag = left.equals(left_item) && item.returnSearch().equals("$") && 
								getProductiuonRight(rights).equals(getProductiuonRight(rights_without_point));
						if(flag){
							action[status][GetProduction.SYM_INT.get("$")] = "acc";//接受
							continue;
						}
					}
					//遇到搜索符号时，才归约
					search = item.returnSearch();
					int curItemPosition = 0;
					if(rights_item.length == 1){// E->.
						for(Production production : productions){
							rights = production.returnRights();
							if(production.returnLeft().equals(left_item)){
								if(rights.length == 1 && rights[0].equals(SomeWord.EMPTY)){
									break;
								}
							}
							curItemPosition++;
						}
						if(action[status][GetProduction.SYM_INT.get(search)].charAt(0) == 's'){
							System.out.println("1============移进-归约冲突==============，"+action[status][GetProduction.SYM_INT.get(search)]);
							System.out.print(left_item+"->"+"., ");
							System.out.println(search);
							continue;
						}
						
						int len = action[status][GetProduction.SYM_INT.get(search)].length();
						if(action[status][GetProduction.SYM_INT.get(search)].charAt(0) == 'r'){
							if(!(Integer.valueOf(action[status][GetProduction.SYM_INT.get(search)].substring(1, len))==curItemPosition)){
								System.out.println("1============归约-归约冲突==============");
								System.out.println("原: action["+status+"]["+search+"]="+action[status][GetProduction.SYM_INT.get(search)]+", 新: r"
								+curItemPosition+", 列: "+search);
								continue;
							}
							
						}
						action[status][GetProduction.SYM_INT.get(search)] = "r" + String.valueOf(curItemPosition);
					}else{//E->T.
						for(Production production : productions){
							rights = production.returnRights();
							if(production.returnLeft().equals(left_item)){
								if(getProductiuonRight(rights).equals(getProductiuonRight(rights_without_point))){
									break;
								}
							}
							curItemPosition++;
						}
						if(action[status][GetProduction.SYM_INT.get(search)].charAt(0) == 's'){
							System.out.println("2============移进-归约冲突==============");
							System.out.print(left_item + "->" + getProductiuonRight(rights) + ", ");
							System.out.println(search);
							continue;
						}
						
						int len1 = action[status][GetProduction.SYM_INT.get(search)].length();
						if(action[status][GetProduction.SYM_INT.get(search)].charAt(0) == 'r'){
							if(!(Integer.valueOf(action[status][GetProduction.SYM_INT.get(search)].substring(1, len1))==curItemPosition)){
								System.out.println("2============归约-归约冲突==============");
								System.out.println("原: "+action[status][GetProduction.SYM_INT.get(search)]+", 新: r"
										+curItemPosition);
								continue;
							}
						}
						action[status][GetProduction.SYM_INT.get(search)] = "r" + String.valueOf(curItemPosition);
					}
				}
			}
		}
		System.out.println("=====r3："+productions.get(3).returnLeft()+"->"+getProductiuonRight(productions.get(3).returnRights()));
		System.out.println("=====r4："+productions.get(4).returnLeft()+"->"+getProductiuonRight(productions.get(4).returnRights()));
		
		return action;
	}
	

	//构造LALR(1)的项目集规范族
	public String[][] getItemSetSet(){
		List<Set<ItemLALR>> itemSetCluster_New= new ArrayList<Set<ItemLALR>>(); //更新后的项目集规范簇
		Set<Set<ItemLALR>> itemSetCluster_Analysed= new HashSet<Set<ItemLALR>>(); //包含分析过的项目集
		Set<ItemLALR> statrtItemSet = new HashSet<ItemLALR>(); //第一个项目集，0号
		String[] rights = productions.get(0).returnRights();
		String[] rights_item = new String[rights.length+1];
		rights_item[0] = ".";
		for(int i = 0; i < rights.length; i++){
			rights_item[i+1] = rights[i];
		}
		Production pro_item = new Production(productions.get(0).returnLeft(), rights_item);
		ItemLALR startItem  = new ItemLALR(pro_item, "$");
		statrtItemSet.add(startItem);
		itemSetCluster.add(getClosure(statrtItemSet));//将开始产生式的闭包，放入项目集规范簇
		Set<ItemLALR> set = null;//goto转换得到的其他状态集
		List<List<Integer>> td = new ArrayList<List<Integer>>();//存储转换的动态二位数组
		List<Integer> od = null;//动态的一维数组
		int i = 0;
		int k = 0;
		Map<String,List<ItemLALR>> gotoItemListMap =null;
		List<ItemLALR> gotoItemList = null;
		int position = -1;
		while(true) {
			itemSetCluster_New.clear();
			itemSetCluster_New.addAll(itemSetCluster);
			for(Set<ItemLALR> itemSet : itemSetCluster_New) {//遍历项目集规范簇里的项目集
				if(itemSetCluster_Analysed.contains(itemSet)) {//该项目集已经找过了
					continue;
				}
				gotoItemListMap = new HashMap<String,List<ItemLALR>>();
				itemSetCluster_Analysed.add(itemSet);
				Set<String> symSet =new HashSet<String>();
				String sym = "";
				for(ItemLALR item : itemSet){ //筛选符号
					gotoItemList = new ArrayList<ItemLALR>();//放入goto的产生式集合
					pro_item = item.returnProItem();
					rights_item = pro_item.returnRights();
					for(int j = 0; j < rights_item.length; j++){
						if(rights_item[j].equals(".")){
							position = j;
							break;
						}
					}
					if(position+1 == rights_item.length){//.在最后位置
						continue;
					}
					gotoItemList.add(item);
					sym= rights_item[++position];//.后的符号
					symSet.add(sym);
					if(gotoItemListMap.get(sym) == null){//键值对：文法符号和产生式
						gotoItemListMap.put(sym,gotoItemList);
					}else {
						gotoItemListMap.get(sym).add(item);
					}
					sym = "";
				}
				for(String s : symSet){//找goto状态集
					if(gotoItemListMap.get(s)!=null){
						set = gotoSet(gotoItemListMap.get(s));
						if(set!=null && -1 == isContain(set)){ //不为空，并且原项目集规范簇没有该集合
							itemSetCluster.add(set);//按插入的先后顺序，加入
							od = new ArrayList<Integer>();
							od.add(i);
							od.add(GetProduction.SYM_INT.get(s));
							/**
							 * proSetCluster加入set，set的排到的位置（刚好是++k是，因为proSetCluster初始时size是1）
							 * ，将此位置设为set的编号，所以++k就是新生成的项目集set的编号
							 */
							++k; 
							od.add(k);
							td.add(od);
						}else {
							if(set == null) {
								System.out.println("空，不加入");
							}else{//项目集规范簇已经含有此项目集，不再加入
								od  = new ArrayList<Integer>();
								od.add(i);
								od.add(GetProduction.SYM_INT.get(s));
								int status = isContain(set);//在项目集规范簇里寻找，已有的项目集的序号
								od.add(status);
								td.add(od);
							}
						}
					}
				}//循环：找goto状态集
				i++;//下一个项目集
			}//循环：遍历项目集规范簇里的项目集
			if(itemSetCluster.size() == itemSetCluster_New.size()){
				break;
			}
		}//循环：最外层
		Set<String> pro_itemSet = null, pro_itemSet_other = null;		
		String s;
		Set<Integer> isFind = new HashSet<Integer>();
		Set<Integer> seem_heartSet;
		position = 0;//外层循环，项目集的位置
		for(Set<ItemLALR> itemSet : itemSetCluster){
			seem_heartSet = new HashSet<Integer>();
			if(isFind.contains(position)){
				position++;
				continue;
			}
			isFind.add(position);
			boolean flag = false;
			pro_itemSet = new HashSet<String>();
			//获取项目集的全部产生式，不包括搜索符
			for(ItemLALR item : itemSet){
				s = "";
				s+=item.returnProItem().returnLeft();
				s+=getProductiuonRight(item.returnProItem().returnRights());
				pro_itemSet.add(s);
			}
			//找同心的项目集
			int fl = 0;
			int position2 = 0;//内层循环，项目集的位置
			for(Set<ItemLALR> itemSet2 : itemSetCluster){
				if(isFind.contains(position2)){
					position2++;
					continue;
				}
				pro_itemSet_other = new HashSet<String>();
				pro_itemSet_other.addAll(pro_itemSet);
				for(ItemLALR item2 : itemSet2){
					s = "";
					s+= item2.returnProItem().returnLeft();
					s+=getProductiuonRight(item2.returnProItem().returnRights());
					pro_itemSet_other.add(s);
				}
				if(pro_itemSet_other.size() == pro_itemSet.size()){
					seem_heartSet.add(position2);//获取同心的项目集的编号
					isFind.add(position2);
					flag = true;
				}
				position2++;
			}
			ItemSetCombine itemSetCom;
			int[] posAll; //记录新项目集的，各个原项目集的位置
			if(flag){//有同心的项目集
				//System.out.println("有同心的项目集"+"，原位置："+position);
				posAll = new int[seem_heartSet.size()+1];
				posAll[0] = position; //第一个是，原项目集
				fl = 1;
				for(Integer in : seem_heartSet){
					posAll[fl] = in;
					fl++;
					//itemSet.addAll( itemSetCluster.get( in));
					itemSet = combineSet(itemSet,itemSetCluster.get( in));
				}
				itemSetCom = new ItemSetCombine(posAll,itemSet);
				itemSetClusterLALR.add(itemSetCom);
			}else{//没有同心的项目集
				//System.out.println("没有同心的项目集"+"，原位置："+position);
				posAll = new int[1];
				posAll[0] = position; 
				itemSetCom = new ItemSetCombine(posAll,itemSet);
				itemSetClusterLALR.add(itemSetCom);
			}
			position++;
		}
		
		//存储
		int n = graSymSet.size();//列宽
		String[][] triple = new String[itemSetCluster.size()][n]; //行宽、列宽
		for(i = 0; i < td.size(); i++){
			triple[td.get(i).get(0)][td.get(i).get(1)] = String.valueOf(td.get(i).get(2));
		}
		//System.out.println("原, 尺寸："+itemSetCluster.size());
	
		String[][] triple_new = new String[itemSetClusterLALR.size()][n];
		Map<Integer,Set<Integer>> map = new HashMap<Integer,Set<Integer>>();
		for(i = 0; i < itemSetClusterLALR.size(); i++){
			int[] posAll = itemSetClusterLALR.get(i).returnPosition_all();
			Set<Integer> set1 = new HashSet<Integer>();
			if(posAll.length > 1){//状态对应的项目集是组合的
				for(int j = 0;j < posAll.length; j++){
					set1.add(posAll[j]);
				}
				map.put(i, set1);
			}else {
				set1.add(posAll[0]);
				map.put(i, set1);
			}
		}
		
		for(i = 0; i < itemSetClusterLALR.size(); i++){
			int[] psoAll = itemSetClusterLALR.get(i).returnPosition_all();
			if(psoAll.length > 1){//状态对应的项目集是组合的
				//System.out.println("组合项目集："+i);
				//System.out.println("原项目集");
				for(int j = 0; j < psoAll.length; j++){
					//System.out.println(psoAll[j]);
					//每个其他状态
					for(int l = 0; l < n; l++){
						boolean flag = true;
						if(triple[psoAll[j]][l] != null){
							int value = Integer.valueOf(triple[psoAll[j]][l]);
							for(Map.Entry<Integer, Set<Integer>> entry: map.entrySet()){
								if(entry.getValue().contains(value)){
									triple_new[i][l] = String.valueOf(entry.getKey());
									flag = false;
									break;
								}
							}
							if(flag) {
								triple_new[i][l] = triple[psoAll[j]][l];
								break;
							}
						}
					}
				}
			}else{//不是组合的项目
				for(int l = 0; l < n; l++){
					boolean flag = true;
					if(triple[psoAll[0]][l] != null){
						int value = Integer.valueOf(triple[psoAll[0]][l]);
						for(Map.Entry<Integer, Set<Integer>> entry : map.entrySet()){
							if(entry.getValue().contains(value)){
								flag = false;
								triple_new[i][l] = String.valueOf(entry.getKey());
								break;
							}
						}
						if(flag){
							triple_new[i][l] = triple[psoAll[0]][l];
						}
					}
				}
			}
			
		}
		return triple_new;
	}

	//goto函数
	public Set<ItemLALR> gotoSet(List<ItemLALR> gotoItemList){
		Set<ItemLALR> set = new HashSet<ItemLALR>();
		ItemLALR item_new; //交换后的项目
		Production pro_item;
		String[] rights_item;
		String[] rights_item_new;
		for(ItemLALR item : gotoItemList){//遍历当前项目集
			String left = item.returnProItem().returnLeft();
			rights_item = item.returnProItem().returnRights();
			rights_item_new = getGotoRights(rights_item);
			//.和右边一个的符号交换
			if(rights_item_new == null){
				System.out.println("交换出错");
				return null;
			}
			pro_item = new Production(left, rights_item_new);
			item_new = new ItemLALR(pro_item, item.returnSearch());
			set.add(item_new);
		}
		if(set.size() == 0){//goto是空的
			return null;
		}else {
			set.addAll(getClosure(set));
			return set;
		}
	}

	//获取一个项目集的闭包，closure
	public Set<ItemLALR> getClosure(Set<ItemLALR> itemSet){
		Set<ItemLALR> itemSetNew =  new HashSet<ItemLALR>();
		itemSetNew.addAll(itemSet);
		Set<String> sym_first_set = new HashSet<String>();
		ItemLALR itemNew;
		String search; //搜索符号
		Production pro_item;//项目中对应的产生式（包含 点）
		String[] rights_item;//产生式右部符号数组
		Production pro_item_new;//新产生的项目
		String[] rights_item_new;//新产生式右部符号数组
		while(true){
			itemSet.addAll(itemSetNew);
			for(ItemLALR item : itemSet){//遍历项目集里的项目
				int position = -1;
				pro_item = item.returnProItem();
				rights_item = pro_item.returnRights();
				search = item.returnSearch();
				for(int i = 0; i < rights_item.length; i++){
					if(rights_item[i].equals(".")){
						position = i;
						break;
					}
				}
				if(++position == rights_item.length){//.在最后位置
					continue;
				}
				String symAfterPoint = rights_item[position];//.后的符号
				if(!Character.isUpperCase(symAfterPoint.charAt(0))){//终结符
					continue;
				}
				String left = "";//产生式左部符号，非终结符
				for(Production product :  productions){//遍历全部的产生式;
					left = product.returnLeft();
					if(sym_first_set.contains(left+search)){//已经找过了
						continue;
					}
					if(left.equals(symAfterPoint)){ 
						String[] rights = product.returnRights();
						if(rights.length==1 && rights[0].equals(SomeWord.EMPTY)){//空产生式
							rights_item_new = new String[1];
							rights_item_new[0] = ".";
						}else{
							rights_item_new = new String[rights.length+1];
							rights_item_new[0] = ".";
							for(int i = 0; i < rights.length; i++){
								rights_item_new[i+1] = rights[i];
							}
						}
						pro_item_new = new Production(left, rights_item_new);
						if(position + 1 == rights_item.length){//点后只有一个符号
							//将search符号加到该产生式中
							itemNew = new ItemLALR(pro_item_new, search);
							if(!isContainItem(itemSetNew,itemNew)){
								itemSetNew.add(itemNew);
							}
						}else{//将非终结符后的 【符号串】 的first集合的每一个符号，都加到该产生式中
							Set<String> symSet = new HashSet<String>();
							symSet.addAll(getFirstSetBySym(rights_item[position+1]));
							int k=position+1;
							for(int p = position + 1; p < rights_item.length; p++){//符号串的first集
								if(k == p){
									if(getFirstSetBySym(rights_item[p]).contains(SomeWord.EMPTY)){
										if(p + 1 < rights_item.length){
											symSet.addAll(getFirstSetBySym(rights_item[p + 1]));//并上后面第一个符号的first集合
											k++;
										}
									}else{
										symSet.remove(SomeWord.EMPTY);//删掉first集中的空串
									}
								}
							}
							for(String s: symSet){
								if(s.equals(SomeWord.EMPTY)){
									s = search;
								}
								itemNew = new ItemLALR(pro_item_new, s);
								if(!isContainItem(itemSetNew,itemNew)){
									itemSetNew.add(itemNew);
								}
							}
						}
					}
				}
				sym_first_set.add(symAfterPoint+search);//存入，下次将不再找该符号在左部的产生式了
			}//循环：遍历项目集
			if(itemSet.size() == itemSetNew.size()){ //个数没变
				break;
			}
		}//循环：最外层
		return itemSet;
	}
	
	//判断当前项目集规范族是否含有gotoSet项目集，
	//如果有，返回其在项目集规范族的位置position
	//效率不高，后期使用很慢，不要使用此方法获取项目集的位置
	public int isContain(Set<ItemLALR> gotoSet){
		int position = -1;
		int itemSetSize = -1;
		Set<String> setString = new HashSet<String>();
		int i = 0;
		for(Set<ItemLALR> itemSet : itemSetCluster){
			itemSetSize = itemSet.size();
			if(itemSetSize == gotoSet.size()){//尺寸相等，再比较集合内容
				for(ItemLALR item:gotoSet){
					String s = "";
					s+= item.returnProItem().returnLeft() ;
					s+= getProductiuonRight(item.returnProItem().returnRights()) ;
					s += item.returnSearch();
					setString.add(s);
				}
				int gotoSetsizeAgo = setString.size();
				for(ItemLALR item:itemSet){
					String s = "";
					s+= item.returnProItem().returnLeft() ;
					s+= getProductiuonRight(item.returnProItem().returnRights()) ;
					s += item.returnSearch();
					setString.add(s);
				}
				if(gotoSetsizeAgo == setString.size()){
					position = i;
					break;
				}
			}
			i++;
		}
		return position;
	}
	
	//聚合两个项目集
	public Set<ItemLALR> combineSet(Set<ItemLALR> set1,Set<ItemLALR> set2){
		List<ItemLALR> list = new ArrayList<ItemLALR>();
		list.addAll(set2);
		Set<String> setString1 = new HashSet<String>();
		for(ItemLALR it : set1){
			String s =it.returnProItem().returnLeft()
					+ getProductiuonRight(it.returnProItem().returnRights())
						+ it.returnSearch();
			setString1.add(s);
		}
		int i = 0;
		for(ItemLALR it : set2){
			String s =it.returnProItem().returnLeft()
					+ getProductiuonRight(it.returnProItem().returnRights())
						+ it.returnSearch();
			if(!setString1.contains(s)){
				set1.add(list.get(i));
			}
			i++;
		}
		return set1;
	}

	//查找单个项目集中，是否包含某项目
	public boolean isContainItem(Set<ItemLALR> set,ItemLALR item){
		Set<String> setString = new HashSet<String>();
		for(ItemLALR it : set){
			String s =it.returnProItem().returnLeft()
					+ getProductiuonRight(it.returnProItem().returnRights())
						+ it.returnSearch();
			setString.add(s);
		}
		String itemString =item.returnProItem().returnLeft()
				+getProductiuonRight( item.returnProItem().returnRights())
						+item.returnSearch();
		if(setString.contains(itemString)){
			return true;
		}
		return false;
	}
	
	//获取文法符号sym的first集
	public Set<String> getFirstSetBySym(String sym){
		Set<String> firstSet = new HashSet<String>();
		firstSet.addAll(firsts.get(sym));
		return firstSet;
	}
	
	//将产生式右部转化为字符串
	public String getProductiuonRight(String[] rights){
		String right = "";
		if(rights != null){
			for(int i = 0; i < rights.length; i++){
				if(!rights[i].equals(SomeWord.EMPTY)){
					right += rights[i].trim() + " "; //加一个空格，隔开
				}
			}
		}else{
			right = null;
		}
		return right;
	}
	
	//交换点和后面的非终结符
	private String[] getGotoRights(String[] rights_item) {
		int position = -1;
		String[] rights_item_new = new String[rights_item.length];
		for(int i=0;i<rights_item.length;i++){
			if(rights_item[i].equals(".")){
				position = i;
				break;
			}
		}
		if(position==-1){
			return null;
		}else{
			for(int i=0;i<rights_item.length;i++){
				if(i==position){
					rights_item_new[i] = rights_item[i+1];
				}else if(i==position+1){
					rights_item_new[i] = rights_item[i-1];
				}else{
					rights_item_new[i] = rights_item[i];
				}
			}
			return rights_item_new;
		}
	}
	
}
