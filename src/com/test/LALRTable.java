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
	//������
	
	private List<String> proList = new ArrayList<String>(); //���ղ���ʽ����
	
	private List<Production> productions = new ArrayList<Production>(); //����ʽ�µĴ洢��ʽ
	
	private Set<String> graSymSet = null; //�ķ�����
	
	public  List<String[]> token = null;//token����
	
	private List<Set<ItemLALR>> itemSetCluster = new ArrayList<Set<ItemLALR>>(); //��Ŀ���淶��
	
	private List<ItemSetCombine> itemSetClusterLALR = new ArrayList<ItemSetCombine>();
	
	private String[][] action = null; //LALR������
	
	private Map<String,Set<String>> firsts = null;
	

	public LALRTable(List<String> proList, Set<String> graSymSet,Map<String,Set<String>> firsts) {
		// TODO Auto-generated constructor stub
		this.graSymSet = graSymSet;
		this.proList = proList;
		this.firsts = firsts;
		setProductions();
	}
	
	//������ʽ���ϻ��ַ�ʽ�洢
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
	
	//����LALR(1)������
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
		for(int i = 0; i < m; i++){ //$���ŵ���
			action[i][n-1] = "##";
		}	
		//��rj��acc
		Production pro_item;
		String left_item;
		String[] rights_item;//��Ŀ�еĲ���ʽ���Ҳ�
		String search;
		String left;//��
		String[] rights = null;//��Ŀ��Ӧ�Ĳ���ʽ
		int status = -1;
		boolean flag = false;
		for(ItemSetCombine itemLALR : itemSetClusterLALR){
			status++;//��Ŀ����λ�ã����������е�״̬
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
							action[status][GetProduction.SYM_INT.get("$")] = "acc";//����
							continue;
						}
					}
					//������������ʱ���Ź�Լ
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
							System.out.println("1============�ƽ�-��Լ��ͻ==============��"+action[status][GetProduction.SYM_INT.get(search)]);
							System.out.print(left_item+"->"+"., ");
							System.out.println(search);
							continue;
						}
						
						int len = action[status][GetProduction.SYM_INT.get(search)].length();
						if(action[status][GetProduction.SYM_INT.get(search)].charAt(0) == 'r'){
							if(!(Integer.valueOf(action[status][GetProduction.SYM_INT.get(search)].substring(1, len))==curItemPosition)){
								System.out.println("1============��Լ-��Լ��ͻ==============");
								System.out.println("ԭ: action["+status+"]["+search+"]="+action[status][GetProduction.SYM_INT.get(search)]+", ��: r"
								+curItemPosition+", ��: "+search);
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
							System.out.println("2============�ƽ�-��Լ��ͻ==============");
							System.out.print(left_item + "->" + getProductiuonRight(rights) + ", ");
							System.out.println(search);
							continue;
						}
						
						int len1 = action[status][GetProduction.SYM_INT.get(search)].length();
						if(action[status][GetProduction.SYM_INT.get(search)].charAt(0) == 'r'){
							if(!(Integer.valueOf(action[status][GetProduction.SYM_INT.get(search)].substring(1, len1))==curItemPosition)){
								System.out.println("2============��Լ-��Լ��ͻ==============");
								System.out.println("ԭ: "+action[status][GetProduction.SYM_INT.get(search)]+", ��: r"
										+curItemPosition);
								continue;
							}
						}
						action[status][GetProduction.SYM_INT.get(search)] = "r" + String.valueOf(curItemPosition);
					}
				}
			}
		}
		System.out.println("=====r3��"+productions.get(3).returnLeft()+"->"+getProductiuonRight(productions.get(3).returnRights()));
		System.out.println("=====r4��"+productions.get(4).returnLeft()+"->"+getProductiuonRight(productions.get(4).returnRights()));
		
		return action;
	}
	

	//����LALR(1)����Ŀ���淶��
	public String[][] getItemSetSet(){
		List<Set<ItemLALR>> itemSetCluster_New= new ArrayList<Set<ItemLALR>>(); //���º����Ŀ���淶��
		Set<Set<ItemLALR>> itemSetCluster_Analysed= new HashSet<Set<ItemLALR>>(); //��������������Ŀ��
		Set<ItemLALR> statrtItemSet = new HashSet<ItemLALR>(); //��һ����Ŀ����0��
		String[] rights = productions.get(0).returnRights();
		String[] rights_item = new String[rights.length+1];
		rights_item[0] = ".";
		for(int i = 0; i < rights.length; i++){
			rights_item[i+1] = rights[i];
		}
		Production pro_item = new Production(productions.get(0).returnLeft(), rights_item);
		ItemLALR startItem  = new ItemLALR(pro_item, "$");
		statrtItemSet.add(startItem);
		itemSetCluster.add(getClosure(statrtItemSet));//����ʼ����ʽ�ıհ���������Ŀ���淶��
		Set<ItemLALR> set = null;//gotoת���õ�������״̬��
		List<List<Integer>> td = new ArrayList<List<Integer>>();//�洢ת���Ķ�̬��λ����
		List<Integer> od = null;//��̬��һά����
		int i = 0;
		int k = 0;
		Map<String,List<ItemLALR>> gotoItemListMap =null;
		List<ItemLALR> gotoItemList = null;
		int position = -1;
		while(true) {
			itemSetCluster_New.clear();
			itemSetCluster_New.addAll(itemSetCluster);
			for(Set<ItemLALR> itemSet : itemSetCluster_New) {//������Ŀ���淶�������Ŀ��
				if(itemSetCluster_Analysed.contains(itemSet)) {//����Ŀ���Ѿ��ҹ���
					continue;
				}
				gotoItemListMap = new HashMap<String,List<ItemLALR>>();
				itemSetCluster_Analysed.add(itemSet);
				Set<String> symSet =new HashSet<String>();
				String sym = "";
				for(ItemLALR item : itemSet){ //ɸѡ����
					gotoItemList = new ArrayList<ItemLALR>();//����goto�Ĳ���ʽ����
					pro_item = item.returnProItem();
					rights_item = pro_item.returnRights();
					for(int j = 0; j < rights_item.length; j++){
						if(rights_item[j].equals(".")){
							position = j;
							break;
						}
					}
					if(position+1 == rights_item.length){//.�����λ��
						continue;
					}
					gotoItemList.add(item);
					sym= rights_item[++position];//.��ķ���
					symSet.add(sym);
					if(gotoItemListMap.get(sym) == null){//��ֵ�ԣ��ķ����źͲ���ʽ
						gotoItemListMap.put(sym,gotoItemList);
					}else {
						gotoItemListMap.get(sym).add(item);
					}
					sym = "";
				}
				for(String s : symSet){//��goto״̬��
					if(gotoItemListMap.get(s)!=null){
						set = gotoSet(gotoItemListMap.get(s));
						if(set!=null && -1 == isContain(set)){ //��Ϊ�գ�����ԭ��Ŀ���淶��û�иü���
							itemSetCluster.add(set);//��������Ⱥ�˳�򣬼���
							od = new ArrayList<Integer>();
							od.add(i);
							od.add(GetProduction.SYM_INT.get(s));
							/**
							 * proSetCluster����set��set���ŵ���λ�ã��պ���++k�ǣ���ΪproSetCluster��ʼʱsize��1��
							 * ������λ����Ϊset�ı�ţ�����++k���������ɵ���Ŀ��set�ı��
							 */
							++k; 
							od.add(k);
							td.add(od);
						}else {
							if(set == null) {
								System.out.println("�գ�������");
							}else{//��Ŀ���淶���Ѿ����д���Ŀ�������ټ���
								od  = new ArrayList<Integer>();
								od.add(i);
								od.add(GetProduction.SYM_INT.get(s));
								int status = isContain(set);//����Ŀ���淶����Ѱ�ң����е���Ŀ�������
								od.add(status);
								td.add(od);
							}
						}
					}
				}//ѭ������goto״̬��
				i++;//��һ����Ŀ��
			}//ѭ����������Ŀ���淶�������Ŀ��
			if(itemSetCluster.size() == itemSetCluster_New.size()){
				break;
			}
		}//ѭ���������
		Set<String> pro_itemSet = null, pro_itemSet_other = null;		
		String s;
		Set<Integer> isFind = new HashSet<Integer>();
		Set<Integer> seem_heartSet;
		position = 0;//���ѭ������Ŀ����λ��
		for(Set<ItemLALR> itemSet : itemSetCluster){
			seem_heartSet = new HashSet<Integer>();
			if(isFind.contains(position)){
				position++;
				continue;
			}
			isFind.add(position);
			boolean flag = false;
			pro_itemSet = new HashSet<String>();
			//��ȡ��Ŀ����ȫ������ʽ��������������
			for(ItemLALR item : itemSet){
				s = "";
				s+=item.returnProItem().returnLeft();
				s+=getProductiuonRight(item.returnProItem().returnRights());
				pro_itemSet.add(s);
			}
			//��ͬ�ĵ���Ŀ��
			int fl = 0;
			int position2 = 0;//�ڲ�ѭ������Ŀ����λ��
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
					seem_heartSet.add(position2);//��ȡͬ�ĵ���Ŀ���ı��
					isFind.add(position2);
					flag = true;
				}
				position2++;
			}
			ItemSetCombine itemSetCom;
			int[] posAll; //��¼����Ŀ���ģ�����ԭ��Ŀ����λ��
			if(flag){//��ͬ�ĵ���Ŀ��
				//System.out.println("��ͬ�ĵ���Ŀ��"+"��ԭλ�ã�"+position);
				posAll = new int[seem_heartSet.size()+1];
				posAll[0] = position; //��һ���ǣ�ԭ��Ŀ��
				fl = 1;
				for(Integer in : seem_heartSet){
					posAll[fl] = in;
					fl++;
					//itemSet.addAll( itemSetCluster.get( in));
					itemSet = combineSet(itemSet,itemSetCluster.get( in));
				}
				itemSetCom = new ItemSetCombine(posAll,itemSet);
				itemSetClusterLALR.add(itemSetCom);
			}else{//û��ͬ�ĵ���Ŀ��
				//System.out.println("û��ͬ�ĵ���Ŀ��"+"��ԭλ�ã�"+position);
				posAll = new int[1];
				posAll[0] = position; 
				itemSetCom = new ItemSetCombine(posAll,itemSet);
				itemSetClusterLALR.add(itemSetCom);
			}
			position++;
		}
		
		//�洢
		int n = graSymSet.size();//�п�
		String[][] triple = new String[itemSetCluster.size()][n]; //�п��п�
		for(i = 0; i < td.size(); i++){
			triple[td.get(i).get(0)][td.get(i).get(1)] = String.valueOf(td.get(i).get(2));
		}
		//System.out.println("ԭ, �ߴ磺"+itemSetCluster.size());
	
		String[][] triple_new = new String[itemSetClusterLALR.size()][n];
		Map<Integer,Set<Integer>> map = new HashMap<Integer,Set<Integer>>();
		for(i = 0; i < itemSetClusterLALR.size(); i++){
			int[] posAll = itemSetClusterLALR.get(i).returnPosition_all();
			Set<Integer> set1 = new HashSet<Integer>();
			if(posAll.length > 1){//״̬��Ӧ����Ŀ������ϵ�
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
			if(psoAll.length > 1){//״̬��Ӧ����Ŀ������ϵ�
				//System.out.println("�����Ŀ����"+i);
				//System.out.println("ԭ��Ŀ��");
				for(int j = 0; j < psoAll.length; j++){
					//System.out.println(psoAll[j]);
					//ÿ������״̬
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
			}else{//������ϵ���Ŀ
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

	//goto����
	public Set<ItemLALR> gotoSet(List<ItemLALR> gotoItemList){
		Set<ItemLALR> set = new HashSet<ItemLALR>();
		ItemLALR item_new; //���������Ŀ
		Production pro_item;
		String[] rights_item;
		String[] rights_item_new;
		for(ItemLALR item : gotoItemList){//������ǰ��Ŀ��
			String left = item.returnProItem().returnLeft();
			rights_item = item.returnProItem().returnRights();
			rights_item_new = getGotoRights(rights_item);
			//.���ұ�һ���ķ��Ž���
			if(rights_item_new == null){
				System.out.println("��������");
				return null;
			}
			pro_item = new Production(left, rights_item_new);
			item_new = new ItemLALR(pro_item, item.returnSearch());
			set.add(item_new);
		}
		if(set.size() == 0){//goto�ǿյ�
			return null;
		}else {
			set.addAll(getClosure(set));
			return set;
		}
	}

	//��ȡһ����Ŀ���ıհ���closure
	public Set<ItemLALR> getClosure(Set<ItemLALR> itemSet){
		Set<ItemLALR> itemSetNew =  new HashSet<ItemLALR>();
		itemSetNew.addAll(itemSet);
		Set<String> sym_first_set = new HashSet<String>();
		ItemLALR itemNew;
		String search; //��������
		Production pro_item;//��Ŀ�ж�Ӧ�Ĳ���ʽ������ �㣩
		String[] rights_item;//����ʽ�Ҳ���������
		Production pro_item_new;//�²�������Ŀ
		String[] rights_item_new;//�²���ʽ�Ҳ���������
		while(true){
			itemSet.addAll(itemSetNew);
			for(ItemLALR item : itemSet){//������Ŀ�������Ŀ
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
				if(++position == rights_item.length){//.�����λ��
					continue;
				}
				String symAfterPoint = rights_item[position];//.��ķ���
				if(!Character.isUpperCase(symAfterPoint.charAt(0))){//�ս��
					continue;
				}
				String left = "";//����ʽ�󲿷��ţ����ս��
				for(Production product :  productions){//����ȫ���Ĳ���ʽ;
					left = product.returnLeft();
					if(sym_first_set.contains(left+search)){//�Ѿ��ҹ���
						continue;
					}
					if(left.equals(symAfterPoint)){ 
						String[] rights = product.returnRights();
						if(rights.length==1 && rights[0].equals(SomeWord.EMPTY)){//�ղ���ʽ
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
						if(position + 1 == rights_item.length){//���ֻ��һ������
							//��search���żӵ��ò���ʽ��
							itemNew = new ItemLALR(pro_item_new, search);
							if(!isContainItem(itemSetNew,itemNew)){
								itemSetNew.add(itemNew);
							}
						}else{//�����ս����� �����Ŵ��� ��first���ϵ�ÿһ�����ţ����ӵ��ò���ʽ��
							Set<String> symSet = new HashSet<String>();
							symSet.addAll(getFirstSetBySym(rights_item[position+1]));
							int k=position+1;
							for(int p = position + 1; p < rights_item.length; p++){//���Ŵ���first��
								if(k == p){
									if(getFirstSetBySym(rights_item[p]).contains(SomeWord.EMPTY)){
										if(p + 1 < rights_item.length){
											symSet.addAll(getFirstSetBySym(rights_item[p + 1]));//���Ϻ����һ�����ŵ�first����
											k++;
										}
									}else{
										symSet.remove(SomeWord.EMPTY);//ɾ��first���еĿմ�
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
				sym_first_set.add(symAfterPoint+search);//���룬�´ν������Ҹ÷������󲿵Ĳ���ʽ��
			}//ѭ����������Ŀ��
			if(itemSet.size() == itemSetNew.size()){ //����û��
				break;
			}
		}//ѭ���������
		return itemSet;
	}
	
	//�жϵ�ǰ��Ŀ���淶���Ƿ���gotoSet��Ŀ����
	//����У�����������Ŀ���淶���λ��position
	//Ч�ʲ��ߣ�����ʹ�ú�������Ҫʹ�ô˷�����ȡ��Ŀ����λ��
	public int isContain(Set<ItemLALR> gotoSet){
		int position = -1;
		int itemSetSize = -1;
		Set<String> setString = new HashSet<String>();
		int i = 0;
		for(Set<ItemLALR> itemSet : itemSetCluster){
			itemSetSize = itemSet.size();
			if(itemSetSize == gotoSet.size()){//�ߴ���ȣ��ٱȽϼ�������
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
	
	//�ۺ�������Ŀ��
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

	//���ҵ�����Ŀ���У��Ƿ����ĳ��Ŀ
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
	
	//��ȡ�ķ�����sym��first��
	public Set<String> getFirstSetBySym(String sym){
		Set<String> firstSet = new HashSet<String>();
		firstSet.addAll(firsts.get(sym));
		return firstSet;
	}
	
	//������ʽ�Ҳ�ת��Ϊ�ַ���
	public String getProductiuonRight(String[] rights){
		String right = "";
		if(rights != null){
			for(int i = 0; i < rights.length; i++){
				if(!rights[i].equals(SomeWord.EMPTY)){
					right += rights[i].trim() + " "; //��һ���ո񣬸���
				}
			}
		}else{
			right = null;
		}
		return right;
	}
	
	//������ͺ���ķ��ս��
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
