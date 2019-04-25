package com.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.test.SomeWord;

public class FirstAndFollow {
	//��ȡ�ķ����ŵ�first����follow����
	
	// ��Ա����,����ʽ�����ս���������ս����
	ArrayList<Production> productions;
	ArrayList<String> terminals;
	ArrayList<String> nonterminals;
	HashMap<String, Set<String>> firsts;
	HashMap<String, Set<String>> follows;
	
	private List<String> proList = new ArrayList<String>();
	private Set<String> graSymSet = new HashSet<String>();
	
	private static Set<String> isFindFirst = new HashSet<String>();//�ж��Ƿ��ҹ���first

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

	// ������ʽ
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

	// ��÷��ս����
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
		//System.out.println("�ս��size��"+terminals.size()+"�����ս��size��"+nonterminals.size());
	}
		
	// ��ȡFirst��
	public Map<String,Set<String>> getFirst() {
		// �ս��ȫ�����first��
		Set<String> first;
		for (int i = 0; i < terminals.size(); i++) {//�ս����first���Ͼ����Լ�
			first = new HashSet<String>();
			first.add(terminals.get(i));
			firsts.put(terminals.get(i), first);
		}
		// �����з��ս��ע��һ��
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

		//�����
		//��ӿմ�
		for(Production pro:productions){
			left = pro.returnLeft();
			rights = pro.returnRights();
			if(rights.length==1 && rights[0].equals(SomeWord.EMPTY)){
				firsts.get(left).add(SomeWord.EMPTY);
			}
		}
		//��first��������Է��Ŵ��ģ�����Ҫ�Բ���ʽ�Ҳ����ŵ�first������ѡ��Ľ��кϲ�
		while(true){
			boolean change = false;
			//System.out.println("=========");
			for(Production pro:productions){
				left = pro.returnLeft();
				String s = left;
				rights = pro.returnRights();
				if(nonterminals.contains(rights[0])){//�Ҳ���һ�������Ƿ��ս��
					int firstSize = firsts.get(s).size();
					for(int i=0;i<rights.length;i++){
						if(!firsts.get(rights[i]).contains(SomeWord.EMPTY)){
							break;
						}
						//System.out.println("���ţ�"+rights[i]+"�������մ�");
						if(i+1<rights.length){//��һ�����ţ������
							//System.out.println("���");
							firsts.get(s).addAll(firsts.get(rights[i+1]));
						}
					}
					int firstSizeNew = firsts.get(s).size();
					boolean temp;
					if(firstSizeNew == firstSize){
						//System.out.println("���䣺"+left);
						temp =false;
					}else{
						//System.out.println("�䣺"+left);
						//System.out.println(firstSize+"��"+firstSizeNew);
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
//			System.out.println("���ţ�"+entry.getKey()+"��first���ϣ�");
//			for(String sym : entry.getValue()){
//				System.out.println(sym);
//			}
//		}
//		System.out.println("����");
		
		return firsts;
	}
	
	//�ݹ����first����
	/**
	 * @param left������ʽ�󲿷���
	 * @param right������ʽ�Ҳ����ż����еĵ�һ������
	 * @return
	 */
	public Set<String> getFirstRecur(String left,String right){
		if(left.equals(right)){
			return null;
		}
		Set<String> firstSet = new HashSet<String>();
		if(Character.isUpperCase(right.charAt(0))){ ///���ս��
			isFindFirst.add(right);//�������ķ��ս��
			String nextLeft,nextRight;
			nextLeft = right;
			for (int i = 0; i < productions.size(); i++) {
				if(productions.get(i).returnLeft().equals(right)){
					nextRight = productions.get(i).returnRights()[0];
					//System.out.println("���ţ�"+nextLeft+"��"+nextRight);
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
		}else if(!right.equals(SomeWord.EMPTY)){//ֻ����ս�����մ������
			//System.out.println("���ս����"+right+"���뵽��"+left+"��first��");
			firstSet.add(right);
			firsts.get(left).add(right);
		}
		return firstSet;
	}
	
	private Set<String> recurSet = new HashSet<String>();

	// ���Follow��
	public Map<String, Set<String>> getFollow(){
		// ���з��ս����follow����ʼ��һ��
		Set<String> follow  = null;
		for (int i = 0; i < nonterminals.size(); i++) {
			follow = new HashSet<String>();
			follows.put(nonterminals.get(i), follow);
		}
		System.out.println();
		//����ʽ�Ҳ������ҷ��ŵ�follow���ϼ���$
		for(Production p:productions){
			String[] rights = p.returnRights();
			String sym_right_most = rights[rights.length-1];
			if(nonterminals.contains(sym_right_most)){
				//System.out.println("���ţ�"+rights[rights.length-1]);
				follows.get(sym_right_most).add("$");
			}
		}
		for (int i = 0; i < nonterminals.size(); i++) {//ÿ�����ս��
			//System.out.println("��ǰ���ս����"+nonterminals.get(i));
			String left,cur,cur_right;
			String[] rights;
			cur = nonterminals.get(i);
			PRO:for(int j=0;j<productions.size();j++){ //����ȫ���Ĳ���ʽ
				rights = productions.get(j).returnRights();
				for(int k=0;k<rights.length;k++){//��������ʽ�Ҳ�
					if(rights[k]!=null){
						if(rights[k].equals(cur)){//���ս�������ڲ���ʽ�Ҳ�
							left = productions.get(j).returnLeft();
							if(k+1==rights.length){//���ս��λ�ڲ���ʽ����
								cur_right = null;
							}
							else{
								cur_right = rights[k+1];//���ս���ұߵķ���
								if(!Character.isUpperCase(cur_right.charAt(0))){//���ս��
									if(follows.get(cur).contains(cur_right)){
										//System.out.println(cur_right+"�Ѿ���"+cur+"��follow������");
										continue PRO;
									}
								}else{//�Ƿ��ս��
									boolean flag = true; //��ǰ�����ķ��ս���Ƿ����Ϊ���ҷ���
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
//			System.out.println("���ս����"+entry.getKey()+"��follow���ϣ�");
//			for(String sym : entry.getValue()){
//				System.out.println(sym);
//			}
//		}
//		System.out.println("����");
		return follows;
	}
	

	
	//�ݹ����follow�ļ��ϣ�
	/**
	 * @param proPosition����ǰ���ս�����ڲ���ʽ�ı��
	 * @param left����ǰ���ս����Ӧ�Ĳ���ʽ�󲿵ķ���
	 * @param cur����ǰ���ս��
	 * @param cur_right����ǰ���ս���ұߵķ���
	 * @param cur_rightPosition����ǰ���ս���ұߵķ��ţ��ڲ���ʽ�Ҳ����ż��ϵ�λ��
	 */
	public void getFollowRecur(int proPosition,String left,String cur,String cur_right,int cur_rightPosition) {
		if(cur_right==null){
//			System.out.println("��");
//			System.out.println("����ʽ��"+proList.get(proPosition));
//			System.out.println("����ʽ�󲿣�"+left+"����ǰ���ţ�"+cur);
			if(left.equals(cur)){
				//System.out.println(left+"��"+cur+"����������");
				return;
			}
			if(recurSet.contains(left)){
				//System.out.println(left+"��follow�Ѿ��ҹ���");
				if(follows.get(left)!=null){
					follows.get(cur).addAll(follows.get(left));
				}
				return;
			}
			String[] rights;
			String left_new,cur_new,cur_right_new;
			PRO:for(int j=0;j<productions.size();j++){ //����ȫ���Ĳ���ʽ
				rights = productions.get(j).returnRights();
				for(int k=0;k<rights.length;k++){//��������ʽ�Ҳ�
					if(rights[k]!=null){
						if(rights[k].equals(left)){//���ս��left�����ڲ���ʽ�Ҳ�
							cur_new = left;
							left_new = productions.get(j).returnLeft();
							if(k+1==rights.length){//���ս��λ�ڲ���ʽ����
								cur_right_new = null;
							}
							else{
								cur_right_new = rights[k+1];//���ս���ұߵķ���
								if(!Character.isUpperCase(cur_right_new.charAt(0))){//�ս��
									if(follows.get(cur).contains(cur_right_new)){
										//System.out.println(cur_right_new+"�Ѿ���"+cur+"��follow������");
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
			//System.out.println("����ʽ��"+proList.get(proPosition));
			//System.out.println("����ʽ�󲿣�"+left+"����ǰ���ţ�"+cur+"���ұߵķ��ţ�"+cur_right+"������λ�ã�"+cur_rightPosition);
			if(Character.isUpperCase(cur_right.charAt(0))){//��ǰ���ս�����ұ��Ƿ��ս��
				if(firsts.get(cur_right)!=null){
					follows.get(cur).addAll(firsts.get(cur_right));
					if(firsts.get(cur_right).contains(SomeWord.EMPTY)){//cur_right��first�������пմ�empty
						follows.get(cur).remove(SomeWord.EMPTY);//ȥ��empty
						String[] rights = productions.get(proPosition).returnRights();
						if(cur_rightPosition+2==rights.length){//�ÿմ����λ�����λ��
							getFollowRecur(proPosition,left,cur,null,cur_rightPosition);
						}else if(cur_rightPosition+2<rights.length){////�ÿմ���󣬺��滹�з���
							getFollowRecur(proPosition,left,cur,rights[cur_rightPosition+1],cur_rightPosition+1);
						}
					}
				}
			}
			else{//��ǰ���ս�����ұ����ս�������뵽follow��
				follows.get(cur).add(cur_right);
			}
		}
	}

}