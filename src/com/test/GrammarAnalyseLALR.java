package com.test;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Stack;

import com.util.Production;
import com.util.ProductionTree;


public class GrammarAnalyseLALR {
	//�﷨����ʵ��
	
	private List<String> proList = new ArrayList<String>(); //���ղ���ʽ����
	
	private List<Production> productions = new ArrayList<Production>(); //����ʽ�µĴ洢��ʽ
	
	public  List<String[]> token = null;//token����
	
	private String[][] action = null; //SLR������
	
	private List<String[]> tbmodel_wrong_result = new ArrayList<String[]>();
	
	private int treePositionWhole; //ʵʱ���µģ����ҷ�Χ�����Ӻ���ǰ��
	
	private List<ProductionTree>  treeList = new ArrayList<ProductionTree>(); //��Լ���Ӧ�Ĳ���ʽ�ļ���
	
	public GrammarAnalyseLALR(List<String[]> token,List<String> proList,String[][] action) {
		// TODO Auto-generated constructor stub
		this.token = token;
		this.proList = proList;
		this.action = action;
		setProductions();
	}
	
	//������ʽ���ϻ��ַ�ʽ�洢
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
	
	private int seq = 0;
	private Stack<String> symStack = new Stack<String>();
	//�﷨����
	public void analyse(){
		ProductionTree ptree;
		String[] end = new String[3];
		end[0]="$";
		end[2] = token.get(token.size()-1)[2];
		token.add(end);
		System.out.println("token���У�");
		for(String[] s : token){
			System.out.println(s[0]);
		}
		
		String[] tuple = new String[4];
		tuple = token.get(0);
		String a = tuple[0];
		String kind = tuple[1];
		String row_num =  tuple[2];
		String error = tuple[3];
		symStack.push("0");
		int i = 1;
		int flag_note = -1;
		while(true){
			System.out.println("��ջ��->ջ����ջ��");
			for(String s:symStack){
				System.out.println(s);
			}
			System.out.println();
			int top = Integer.valueOf(symStack.peek());//ջ������״̬
			int n = -1;
			if(kind!=null){
				System.out.println("kind��"+kind+"��"+a);
				if(kind.equals("NUM")){//����
					n = GetProduction.SYM_INT.get("num");
					if(error!=null){
						System.out.println("����");
						tbmodel_wrong_result.add(new String[]{error,row_num});
						error = null;
					}
				}else if(kind.equals("IDN")){//�Ǳ�ʶ��
					n = GetProduction.SYM_INT.get("id");
				}
				else if(kind.equals("OP")){
					n = GetProduction.SYM_INT.get(a);
					if(error!=null){
						System.out.println("���������");
						tbmodel_wrong_result.add(new String[]{error,row_num});
						error = null;
					}
				}
				else if(kind.equals("NOTE")){
					System.out.println(flag_note+"��"+i);
					if(error!=null && flag_note!=i){
						System.out.println("����ע��");
						tbmodel_wrong_result.add(new String[]{error,row_num});
						if(i<token.size()){
							tuple = token.get(i);
							a =tuple[0] ;//��һ���������
							kind = tuple[1];
							if(a==null){
								row_num =  tuple[2];
								error = tuple[3];
							}else{
								if(!a.equals("$")){
									row_num =  tuple[2];
									error = tuple[3];
								}
							}
							i++;
						}else{
							a = "$";//����
							kind = null;
							System.out.println("�������");
						}
						flag_note = i;
						continue;
					}
				}
				else{
					System.out.println("û�ж�Ӧ���ķ�����");
					return;
				}
			}else{
				if(GetProduction.SYM_INT.containsKey(a)){//��������
					n = GetProduction.SYM_INT.get(a);
				}
			}
			System.out.println(n+"��"+a);
			if(action[top][n].charAt(0)=='s'){
				String status = action[top][n].substring(1, action[top][n].length());
				System.out.println("ջ��״̬"+top+"������������ţ�"+a+"����"
						+action[top][n]+"���ƽ�"+a+"������״̬��"+status);
				symStack.push(a);
				symStack.push(status);
				if(i<token.size()){
					tuple = token.get(i);
					a =tuple[0] ;//��һ���������
					kind = tuple[1];
					if(a==null){
						row_num =  tuple[2];
						error = tuple[3];
					}else{
						if(!a.equals("$")){
							row_num =  tuple[2];
							error = tuple[3];
						}
					}
					i++;
				}else{
					a = "$";//����
					kind = null;
					System.out.println("�������");
				}
			}
			else if(action[top][n].charAt(0)=='r'){
				String status = action[top][n].substring(1, action[top][n].length());
				int m = Integer.valueOf(String.valueOf(status));
				Production  pro = productions.get(m);//��ȡ�ò���ʽ
				String right = getProductiuonRight(pro.returnRights()).trim().isEmpty() ? "empty" : getProductiuonRight(pro.returnRights());
				System.out.println("ջ��״̬"+top+"������������ţ�"+a+"����"
						+action[top][n]+"����"+pro.returnLeft()+"->"+right+"��Լ");
				int len = 0;
				if(right.equals(SomeWord.EMPTY)){ //���մ���Լ������ջ
					len = 0;
				}else{
					String[]  rights = pro.returnRights();//������ʽ�Ҳ��ķ��Ż��ֳ���
					for(int k=0;k<rights.length;k++){
						if(!rights[k].trim().isEmpty())
							len++;
					}
				}
				System.out.println("��ջ������"+len*2);
				String[] keys = new String[len];
				int k = 0;
				for(int j=0;j<len*2;j++){
					if((j+2)%2!=0){
						keys[k] = symStack.pop();
						k++;
					}else{
						symStack.pop();
					}
				}
				ptree = new ProductionTree(m, keys);
				treeList.add(ptree);
				String t = symStack.peek();
				String symLeft = pro.returnLeft();
				symStack.push(symLeft);
				String status_new = action[Integer.valueOf(t)][GetProduction.SYM_INT.get(symLeft)];
				System.out.println("1 ��״̬��"+action[Integer.valueOf(t)][GetProduction.SYM_INT.get(symLeft)]);
				symStack.push(status_new);
			}
			else if(action[top][n].equals("acc")){
				System.out.println("ջ��״̬"+top+"������������ţ�"+a+"�����ܡ��������");
				int treePosition = treeList.size()-1;
				treePositionWhole = treePosition;
				int proPosition = treeList.get(treePosition).getProPosition();
				//String left = productions.get(proPosition).returnLeft();
				String[] rights = productions.get(proPosition).returnRights();
				String[] nodes = new String[rights.length];
				for(int k=rights.length-1;k>=0;k--){
					String cur = rights[k];
					nodes[k] = printTreeRecur(1,cur,treePositionWhole,treePositionWhole);
				}
				break;
			}
			else{//���ô��������
				System.out.println("�������ţ�"+a+"������"+n+"��ջ��״̬��"+top);
				System.out.println(GetProduction.INT_SYM.get(n));
				System.out.println(action[top][n]);
				//break;
				i = i-2; //����һ������
				if(i<0){
					System.out.println("�����������䣡");
					tbmodel_wrong_result.add(new String[]{"����ķ��ţ�"+a,row_num});
					i = i+2;
					if(i<token.size()){
						tuple = token.get(i);
						a =tuple[0] ;//��һ���������
						kind = tuple[1];
						if(a==null){
							row_num =  tuple[2];
							error = tuple[3];
						}else{
							if(!a.equals("$")){
								row_num =  tuple[2];
								error = tuple[3];
							}
						}
						i++;
					}else{
						a = "$";//����
						kind = null;
						System.out.println("�������");
					}
					
				//break;
				}else{
					String sym_error_before = token.get(i)[0];
					int ret = errorHandler(top,sym_error_before,a,i);
					System.out.println("ret�ǣ�"+ret);
					if(ret!=0){
						 i =ret;
						 System.out.println("��ǰi�ǣ�"+i);
						 if(i<token.size()){
							 System.out.println("��ǰi�ǣ�"+i+"�����ţ�"+token.get(i)[0]);
						 }
					}else{
						i = i+1;
					}
					if(i<token.size()){
						tuple = token.get(i);
						a =tuple[0] ;//��һ���������
						kind = tuple[1];
						if(a==null){
							row_num =  tuple[2];
							error = tuple[3];
						}else{
							if(!a.equals("$")){
								row_num =  tuple[2];
								error = tuple[3];
							}
						}
						i++;
					}else{
						a = "$";//����
						kind = null;
						System.out.println("�������");
					}
				}
			}
		}
	}
	
	public int errorHandler(int top, String sym_error_before, String sym_error, int position){
		//0�Ŵ�������1�Ŵ涯��or��תֵ
		System.out.println("����ǰ�ķ��ţ�"+sym_error_before);
		System.out.println("�������ķ��ţ�"+sym_error);
		Map<String,String> action_hope_value = new HashMap<String,String>();
		List<String>  action_hope_name = new ArrayList<String>();
		String[] hope = new String[4];
		boolean flag = false;
		for(int p=0;p<action[0].length;p++){
			if(!action[top][p].equals("##")){
				String sym = GetProduction.INT_SYM.get(p);
				action_hope_name.add(sym);
				action_hope_value.put(sym, action[top][p]);
			}
		}
		//���ܵĶ���or��ת��ֵ
		for(String s:action_hope_name){
			System.out.println("ϣ�������ķ��ţ�"+s);
		}
		boolean isDolloar = false;
		int position_new = 0;
		if(!sym_error.equals("$")){//�������յķ���
			isDolloar = true;
			int next_pos = position + 2;
			List<String[]> no_use = new ArrayList<String[]>();
			no_use.add(new String[]{token.get(next_pos-1)[0],token.get(next_pos-1)[2]});
			int i = next_pos;
			for(;i<token.size();i++){
				String[] next_token = token.get(i);
				String s = "";
				if(next_token[1]!=null){
					if(next_token[1].equals("NUM")){
						s = "num";
					}else if(next_token[1].equals("IDN")){
						System.out.println("��IDN");
						s = "id";
					}
				}else{
					s = next_token[0];
				}
				System.out.println("���ţ�"+next_token[0]+"��top="+top);
				if(!action_hope_name.contains(s)){
					no_use.add(new String[]{token.get(i)[0],token.get(i)[2]});
					continue;
				}
				break;
			}
			if(i<token.size()){
				for(String[] s:no_use){
					tbmodel_wrong_result.add(new String[]{"����ķ��ţ�"+s[0],s[1]});
					System.out.println("1����ķ��ţ�"+s[0]);
				}
				position_new = i;
				return position_new;
			}
		}
		else{
			//�������ķ�����$
		}
		System.out.println("��$ ���ţ�"+position_new);
		
		if(sym_error_before.equals(";")){
			System.out.println("���������ţ�"+sym_error);
			if(!sym_error.equals("$")){
				tbmodel_wrong_result.add(new String[]{"����ķ��ţ�"+sym_error,token.get(position)[2]});
			}
			token.remove(position);
			return position_new;
		}
		if(sym_error_before.equals("]")){
			if(!isHaveSymInStackByName("[")){
				System.out.println("���������ţ�"+sym_error);
				if(!sym_error.equals("$")){
					tbmodel_wrong_result.add(new String[]{"����ķ��ţ�"+sym_error,token.get(position)[2]});
				}
				token.remove(position);
				return position_new;
			}
		}
		if(sym_error_before.equals(")")){
			if(!isHaveSymInStackByName("(")){
				System.out.println("���������ţ�"+sym_error);
				if(!sym_error.equals("$")){
					tbmodel_wrong_result.add(new String[]{"����ķ��ţ�"+sym_error,token.get(position)[2]});
				}
				token.remove(position);
				return position_new;
			}
		}
		if(sym_error_before.equals("}")){
			if(!isHaveSymInStackByName("{")){
				System.out.println("���������ţ�"+sym_error);
				if(!sym_error.equals("$")){
					tbmodel_wrong_result.add(new String[]{"����ķ��ţ�"+sym_error,token.get(position)[2]});
				}
				token.remove(position);
				return position_new;
			}
		}
		
		if(action_hope_name.contains(")")){
			System.out.println("������");
			boolean flag1 =isHaveSymInStackByName("(");
			if(flag1){
				System.out.println("��ȫ ) ");
				tbmodel_wrong_result.add(new String[]{"ȱ��������  )",token.get(position)[2]});
				hope[0] = ")";
				hope[2] = token.get(position)[2];
				token.add(position+1, hope);
				flag = true;
				return position_new;
			}
		}
		if(action_hope_name.contains("]")){
			System.out.println("�з�����");
			boolean flag1 =isHaveSymInStackByName("[");
			if(flag1){
				System.out.println("��ȫ ] ");
				tbmodel_wrong_result.add(new String[]{"ȱ�ٷ���  ]",token.get(position)[2]});
				hope[0] = "]";
				hope[2] = token.get(position)[2];
				token.add(position+1, hope);
				flag = true;
				return position_new;
			}
		}
		if(action_hope_name.contains("}")){
			System.out.println("�з�����");
			boolean flag1 =isHaveSymInStackByName("{");
			if(flag1){
				System.out.println("��ȫ } ");
				tbmodel_wrong_result.add(new String[]{"ȱ�ٷ���  }",token.get(position)[2]});
				hope[0] = "}";
				hope[2] = token.get(position)[2];
				token.add(position+1, hope);
				flag = true;
				return position_new;
			}
		}
		
		
	   if(action_hope_name.contains(";")){
			//ȱ�ٷֺ�
			System.out.println("========ȱ�ֺ�=========");
			tbmodel_wrong_result.add(new String[]{"ȱ�ٷֺ�",token.get(position)[2]});
			hope[0] = ";";
			hope[2] = token.get(position)[2];
			token.add(position+1, hope);
			flag = true;
			return position_new;
		}else if(action_hope_name.contains("id")){
			System.out.println("����id");
			if(sym_error.equals("$")){
				System.out.println("========ȱid=========");
				tbmodel_wrong_result.add(new String[]{"ȱ��id",token.get(position)[2]});
				hope[0] = "<hope_id"+(seq++)+">";
				hope[1] = "IDN";
				hope[2] = token.get(position)[2];
				token.add(position+1, hope);
				flag = true;
				return position_new;
			}
			if(sym_error_before.equals("+") || sym_error_before.equals("-")
					||sym_error_before.equals("*")||sym_error_before.equals("/")
					|| sym_error_before.equals("$")){
				//���������
				//�����������
				//�id
				System.out.println("========ȱ�������=========");
				tbmodel_wrong_result.add(new String[]{"ȱ�������",token.get(position)[2]});
				hope[0] = "<hope_id"+(seq++)+">";
				hope[1] = "IDN";
				hope[2] = token.get(position)[2];
				token.add(position+1, hope);
				flag = true;
				return position_new;
			}
			if(sym_error_before.equals("=") || sym_error_before.equals(",")){
				//�Ⱥ��Ҳ�����
				//����������������ʽ��������
				//�id
				System.out.println("========ȱ�˱���=========");
				tbmodel_wrong_result.add(new String[]{"ȱ�ٱ���",token.get(position)[2]});
				hope[0] = "<hope_id"+(seq++)+">";
				hope[1] = "IDN";
				hope[2] = token.get(position)[2];
				token.add(position+1, hope);
				flag = true;
				return position_new;
			}
		}
		else if(action_hope_name.contains("=")){
			System.out.println("========ȱ�ٵȺ�=========");
			tbmodel_wrong_result.add(new String[]{"ȱ�ٵȺ�",token.get(position)[2]});
			hope[0] = "=";
			hope[2] = token.get(position)[2];
			token.add(position+1, hope);
			flag = true;
			return position_new;
		}
		else if(action_hope_name.contains("num")){
			System.out.println("========ȱnum=========");
			tbmodel_wrong_result.add(new String[]{"ȱ������",token.get(position)[2]});
			hope[0] = "-99999";
			hope[1] = "NUM";
			hope[2] = token.get(position)[2];
			token.add(position+1, hope);
			flag = true;
			return position_new;
		}
		
		else{
			if(!flag){
		        Random random = new Random();
		        int s = random.nextInt(action_hope_name.size()-1);
		        System.out.println("���ȡ�õ�����"+s);
				hope[0] = action_hope_name.get(s);
				if(hope[0].equals("num")){
					hope[0]  = "-9999";
					hope[1] = "NUM";
				}
				hope[2] = token.get(position)[2];
				token.add(position+1, hope);
			}
		}
		return position_new;
	}
	
	//��ӡ�﷨������
	/**
	 * @param space_num����ǰ��ӡ���ַ���Ӧ����ǰ����϶��ٸ�'\t'
	 * @param cur����ǰ�ַ�������ʽ�Ҳ���ĳ���ַ�����Ϊ��Ѱ�Һ��
	 * @param treePosition����treeList�еģ����ҷ�Χ���Ӻ���ǰ��
	 * @param fatherPosition����ǰ���ҵĺ�̣��丸�ײ���ʽ���ݹ顰��㡱����treeList��λ�ã�
	 * @return
	 */
	public String printTreeRecur(int space_num,String cur,int treePosition,int fatherPosition){
		if(Character.isUpperCase(cur.charAt(0))){//���ս����������
			if(treePosition-1>=0){
				String node = "";
				String leftNext  = "";
				int proPosition;
				int treePositionNext = -1;
				for(int i = treePosition-1;i>=0;i--){
					proPosition = treeList.get(i).getProPosition();
					leftNext = productions.get(proPosition).returnLeft();
					if(leftNext.equals(cur)){//�ҵ����
						treePositionNext = i;
						break;
					}
				}
				if(treePositionNext==-1){
					System.out.println("����");
					return null;
				}
				treePositionWhole = treePositionNext; 
				String[] rights = productions.get(treeList.get(treePositionNext).getProPosition()).returnRights();
				String space = "";
				for(int i=0;i<space_num;i++){
					space += "\t";
				}
				String s  = space + cur;
				String[] nodes = new String[rights.length];
				for(int j = rights.length-1;j>=0;j--){
					String curNext = rights[j];	
					node = printTreeRecur(space_num+1,curNext,treePositionWhole,treePositionNext);
					nodes[j] = node;
				}
				for(int j =0;j<nodes.length;j++){
					s += "\n" + nodes[j];
				}
				return s;
			}
		}else{//�ս�����߿մ�����ֹͣ���Һ��
			if(cur.equals("id")){
				String[] keys = treeList.get(fatherPosition).getKeys();
				int position = treeList.get(fatherPosition).getProPosition();
				String[] rights = productions.get(position).returnRights();
				int symPosition = -1;
				for(int i=0;i<rights.length;i++){
					if(rights[i].equals("id")){
						symPosition = i;
						break;
					}
				}
				if(symPosition!=-1){
					cur +=" ֵΪ" + keys[keys.length-1-symPosition];
				}
			}else if(cur.equals("num")){
				String[] keys = treeList.get(fatherPosition).getKeys();
				int position = treeList.get(fatherPosition).getProPosition();
				String[] rights = productions.get(position).returnRights();
				int symPosition = -1;
				for(int i=0;i<rights.length;i++){
					if(rights[i].equals("num")){
						symPosition = i;
						break;
					}
				}
				if(symPosition!=-1){
					cur +=" ֵΪ" + keys[keys.length-1-symPosition];
				}
			}
			String space = "";
			for(int i=0;i<space_num-1;i++){ //�ս��û�к�̣�����ҪԤ����һ�����ŵ�λ��
				space += "\t";
			}
			String node = space + "\t" + cur;
			return node;
		}
		return null;
	}
	
	//������ʽ�Ҳ�ת��Ϊ�ַ���
	public String getProductiuonRight(String[] rights){
		String right = "";
		if(rights!=null){
			for(int i = 0;i<rights.length;i++){
				if(!rights[i].equals(SomeWord.EMPTY)){
					right += rights[i] + " "; //��һ���ո񣬸���
				}
			}
			right = right.trim();
		}else{
			right = null;
		}
		return right;
	}
	
	//��ջ����ջ��ɨ�裬ɨ�赽depth��ȵķ��š� ջ��ʼ��������������
	public String topToBottomStack(int depth,Stack<String> stack){
		int stack_len = stack.size();
		int stack_dep = stack_len/2;
		System.out.println("�׵���");
		for(String s:stack){
			System.out.println(s);
		}
		System.out.println("ջ����ȣ�"+stack_dep);
		System.out.println("ָ����ȣ�"+depth);
		String s3 = null;
		List<String[]> popSym = new ArrayList<String[]>();
		if(stack_dep>=depth){
			for(int i=0;i<depth;i++){
				popSym.add(new String[]{stack.pop(),stack.pop()});
				s3 = stack.peek();
			}
			for(int j = popSym.size()-1; j>=0;j--){
				String[] s = popSym.get(j);
				stack.push(s[1]);
				stack.push(s[0]);
			}
			return s3;
		}else{
			System.out.println("���Խ�磺"+depth);
			return null;
		}
	}
	
	public boolean isHaveSymInStackByName(String name){
		boolean flag = false;
		Enumeration<String> items = symStack.elements();
		String s1;
		while(items.hasMoreElements()){
			s1 = items.nextElement();
			if(s1.equals(name)){
				flag = true;
				break;
			}
		}
		return flag;
	}
	
}
