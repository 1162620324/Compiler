package com.test;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Stack;

import com.util.Production;
import com.util.ProductionTree;

public class SemanticAnalyse {
	//�������ʵ��
	
		private List<String> proList = new ArrayList<String>(); //���ղ���ʽ����
		
		private List<Production> productions = new ArrayList<Production>(); //����ʽ�µĴ洢��ʽ
		
		public  List<String[]> token = null;//token����
		
		private String[][] action = null; //LALR������
		
		private List<ProductionTree>  treeList = new ArrayList<ProductionTree>(); //��Լ���Ӧ�Ĳ���ʽ�ļ���
		
		private int offset = 0; //���ű��е�ƫ����
		
		private Stack<String[]> stack = new Stack<String[]>();
		
		private List<String[]> tbmodel_wrong_result = new ArrayList<String[]>();
		
		private List<String[]> tbmodel_sym = new ArrayList<String[]>();
		
		private List<String[]> tbmodel_34 = new ArrayList<String[]>();
		
		public SemanticAnalyse(List<String[]> token,List<String> proList,String[][] action) {
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
		
		//�﷨����
		public void analyse(){
			ProductionTree ptree;
			String[] end = new String[2];
			end[0]="$";
			token.add(end);
			
			String[] tuple = new String[4];
			tuple = token.get(0);
			String a = tuple[0];
			String kind = tuple[1];
			String row_num =  tuple[2];
			String error = tuple[3];
			
			//״̬�����Եļ�ֵ�ԣ�0��λ�ô洢״̬��1��λ�ô洢type��2�Ŵ洢width��3�Ŵ洢val��4�Ŵ�addr��5�Ŵ洢ƫ����
			//6�Ŵ洢truelist��Ӧ��λ�ã�7�Ŵ洢falselist��Ӧ��λ�ã����� 6��Ҳ���Դ洢nextlist
			String[] s_p = new String[8]; 
			
			s_p[0] = "0"; //
			stack.push(s_p);
			int i = 1;
			int flag_note = -1;
			String type = null,width = null;
			
			int seq = 0;
			int newTemp = 0;
			//��������ʱ����������Ѱַ�����
			int array_depth = 0; 
			//��������
			String arrayType = ""; 
			
			//���ű��Ӧ�ķ��ż���
			Set<String> symTable = new HashSet<String>();
			//���ű�0����ʶ����1�����ͣ�2��ƫ������3���к�
			Set<String[]> symTableInfo = new HashSet<String[]>();
			
			
			int position = 0;
			//key:��ʶ -- value:��תָ��洢���ŵ�truelist��falselist��nextlist
			Map<String,List<String>> listMap = new HashMap<String,List<String>>();
			
			//��������ʱ���������βΣ�0��λ�ô洢��������1��λ�ô洢����������
			List<String[]> paramSet = null;
			//key:������ --value:�β��б� 
			Map<String,List<String[]>> processMap = new HashMap<String,List<String[]>>();
			
			//���ù���ʱ��������β��б�0��λ�ô洢��������1��λ�ô洢����������
			List<String[]> paramListCall =null;
			
			//�м���룬������ɺ�ͳһ��ӡ
			List<String[]> list_34 = new ArrayList<String[]>();
			
			while(true){
				System.out.print("ջ�ڷ���: ");
				for(String[] sp :stack){
					System.out.print(sp[0] + " ");
				}
				System.out.println(" ");
				
				int top = Integer.valueOf(stack.peek()[0]);//ջ������״̬
				//String property = stack.peek()[1];//��Ӧ������
				int n = -1;
				if(kind != null){
					//System.out.println("kind��" + kind + ", " + a);
					if(kind.equals("NUM")){//����
						n = GetProduction.SYM_INT.get("num");
						if(error != null){
							System.out.println("����");
							//tbmodel_wrong_result.addRow(new String[]{error,row_num});
							//System.out.println("line " + row_num + ": " + error);
							tbmodel_wrong_result.add(new String[]{error,row_num});
							error = null;
						}
					}else if(kind.equals("IDN")){//�Ǳ�ʶ��
						n = GetProduction.SYM_INT.get("id");
					}else if(kind.equals("OP")){
						n = GetProduction.SYM_INT.get(a);
						if(error!=null){
							System.out.println("���������");
							//tbmodel_wrong_result.addRow(new String[]{error,row_num});
							//System.out.println("line " + row_num + ": " + error);
							tbmodel_wrong_result.add(new String[]{error,row_num});
							error = null;
						}
					}else if(kind.equals("NOTE")){
						System.out.println(flag_note+"��"+i);
						if(error!=null && flag_note!=i){
							System.out.println("����ע��");
							tbmodel_wrong_result.add(new String[]{error,row_num});
							//System.out.println("line " + row_num + ": " + error);
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
					}else{
						System.out.println("û�ж�Ӧ���ķ�����");
						return;
					}
				}else{
					if(GetProduction.SYM_INT.containsKey(a)){//��������
						n = GetProduction.SYM_INT.get(a);
					}
				}
				//System.out.println(n+", "+a);
				if(action[top][n].charAt(0)=='s'){//�ƽ� 
					String status = action[top][n].substring(1, action[top][n].length());
					System.out.println("ջ��״̬: " + top + ", �����������: " + a + ", �� "
							+ action[top][n] + ", �ƽ�" + a + ", ����״̬: " + status);
					//statusStack.push(a);
					s_p =new String[2];
					s_p[0] = a;
					if(kind!=null && kind.equals("IDN")){
						//�ƽ�idʱ��Ϊid���type
						s_p[1] = kind;
					}else if(SomeWord.OP.contains(a)){
						s_p[1] = "OP";
					}
					stack.push(s_p);
					
					s_p =new String[8];
					s_p[0] = status;  //״̬
					stack.push(s_p);
					
					if(i < token.size()){
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
				}else if(action[top][n].charAt(0)=='r'){ //��Լ
					String status = action[top][n].substring(1, action[top][n].length());
					int m = Integer.valueOf(String.valueOf(status));
					Production  pro = productions.get(m);//��ȡ�ò���ʽ
					String right = getProductiuonRight(pro.returnRights()).trim().isEmpty() ? "empty" : getProductiuonRight(pro.returnRights());
					System.out.println("ջ��״̬: " + top + ", �����������: " + a + ", �� "
							+ action[top][n] + ", ��   " + pro.returnLeft() + " -> " + right + " ��Լ");
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
					System.out.println("��ջ����: " + len * 2);
					String[] keys = new String[len];
					List<String[]> popSymInfo = new ArrayList<String[]>();
					int k = 0;
					for(int j = 0; j < len * 2; j++){
						if((j + 2) % 2 != 0) {
							keys[k] = stack.pop()[0];
							k++;
						}else{
							popSymInfo.add(stack.pop());
						}
					}
					ptree = new ProductionTree(m, keys);
					treeList.add(ptree);
					String t = stack.peek()[0];
					String symLeft = pro.returnLeft();

					s_p =new String[1];
					s_p[0] = symLeft;
					stack.push(s_p);
					
					String status_new = action[Integer.valueOf(t)][GetProduction.SYM_INT.get(symLeft)];
					System.out.println("��״̬: " + status_new);

					s_p =new String[8];
					
//------*-*------*-*------*-*------*-*------*-*------*-*------*-*------*-*------*-*------*-*------*-*------*-*------*-*------*-*------*-*
					//���嶯��
					String[] rights = pro.returnRights();
					String left = pro.returnLeft();
					
					if(left.equals("P")){ //P->empty
						if(rights.length == 1 && rights[0].equals(SomeWord.EMPTY)) {
							Map<Integer,String[]> modify = new HashMap<Integer,String[]>();
							int k1 = 0;
							for(String[] s34:list_34){ //����ַ����Ԫʽ
								String[] s34_new = new String[4];
								s34_new[0] = s34[0];
								s34_new[2] = s34[2];
								s34_new[3] = s34[3];
								String s3 = s34[1];  //����ַ   arg1
								String s4 = s34[2];  //��Ԫʽ   arg2
								String s3_new = s3;
								String s4_new = s4;
								//************************************************************************************************************
								if(s3.indexOf("_") != -1){
									s3_new = s3.substring(0, s3.indexOf("_")) + seq;
								}
								if(s4.indexOf("_") != -1){
									s4_new =  s4.substring(0, s4.indexOf("_")) + seq + " )";
								}
								s34_new[1] = s3_new;
								s34_new[2] = s4_new;
								modify.put(k1,s34_new);
								k1++;
								//****************************************************************************************************************
							}
							//�������ɵ�����ַ����Ԫʽ�滻ԭ����
							for(Map.Entry<Integer, String[]> entry:modify.entrySet()){
								int pos = entry.getKey();
								String[] s34_new = entry.getValue();
								list_34.remove(pos);
								list_34.add(pos, s34_new);
							}
						}
					}else if(left.equals("Pa")){  //Pa->empty
						//����offset����ʼ��Ϊ0
						System.out.println("��ʼ��");
						Map<Integer,String[]> modify = new HashMap<Integer,String[]>();
						int k1=0;
						for(String[] s34:list_34){//����ַ����Ԫʽ
							String[] s34_new = new String[4];
							s34_new[0] = s34[0];
							s34_new[2] = s34[2];
							s34_new[3] = s34[3];
							String s3 = s34[1];
							String s4 = s34[2];
							String s3_new = s3;
							String s4_new = s4;
							if(s3.indexOf("_") != -1){
								s3_new =  s3.substring(0, s3.indexOf("_"))+seq;
								
							}
							if(s4.indexOf("_") != -1){
								s4_new =  s4.substring(0, s4.indexOf("_"))+seq + " )";
							}
							s34_new[1] = s3_new;
							s34_new[2] = s4_new;
							modify.put(k1,s34_new);
							k1++;
						}
						//�������ɵ�����ַ����Ԫʽ�滻ԭ����
						for(Map.Entry<Integer, String[]> entry:modify.entrySet()){
							int pos = entry.getKey();
							String[] s34_new = entry.getValue();
							list_34.remove(pos);
							list_34.add(pos, s34_new);
						}
					}else if(left.equals("Da")){//Da -> empty { enter(id.lexeme,.T.type,offset); offset=offset+T.width; }
						String[] info = getElementByName("T");
						s_p[1] = info[1];
						s_p[2] = info[2];
						String id = getElemByType("IDN");
						if(symTable.contains(id)){
						    tbmodel_wrong_result.add(new String[]{id + ", �ظ�����", row_num});
						}else{
							symTable.add(id);
							String[] s = new String[4];//0����ʶ����1�����ͣ�2��ƫ������3���к�
							s[0] = id;
							s[1] = s_p[1] ;
							s[2] = String.valueOf(offset);
							s[3] = row_num;
							symTableInfo.add(s);
						}
						
						tbmodel_sym.add(new String[]{id, s_p[1], String.valueOf(offset), row_num}); 
						
						// enter(id.lexeme,T.type,offset)
						offset += Integer.valueOf(s_p[2]); //offset=offset+T.width
					}else if(left.equals("Db")){//Db -> empty {type=record; enter(id.lexeme,type,offset);}
						s_p[1] = "record";
						String id = getElemByType("IDN");
						tbmodel_sym.add(new String[]{id, s_p[1], String.valueOf(offset), row_num});
					}else if(left.equals("Dc")){ //Dc -> empty {type=proc; enter(id.lexeme,type,offset);}
						paramSet = new ArrayList<String[]>();//��������ʱ���������β�
						s_p[1] = "proc";
						String id =getElemByType("IDN");
						tbmodel_sym.add(new String[]{id,s_p[1],String.valueOf(offset),row_num});
					}else if(left.equals("Aa")){//Aa -> empty {gen(id,��=��,F.addr);}
						seq ++;
						String addrF = getElementByName("F")[4];
						String id = getElemByType("IDN");
						if(addrF!=null){//��addr��Ϣ
							String s3 = "";
							s3 += id + " = " + addrF;
							String s4 = "( =, ";
							s4 += addrF+ " , -  , "+id+ ")";
							System.out.println("Aa -> empty\t����ַ�룺" + s3 + ", ��Ԫ�飺" + s4);
							list_34 =addList34(list_34, seq, new String[]{String.valueOf(seq),s3,s4,row_num});
							seq++;
						}
					}else if(left.equals("Ab")){//Ab -> empty {enter(id.lexeme,T.type,offset); offset=offset+T.width;}
						s_p[1] = type;               
						s_p[2] = width;
						String id = getElemByType("IDN");
						if(symTable.contains(id)){
							tbmodel_wrong_result.add(new String[]{id + ", �ظ�����", row_num});
						}else{
							symTable.add(id);
							String[] s = new String[4];//0����ʶ��  1������  2��ƫ����  3�к�
							s[0] = id;
							s[1] = s_p[1] ;
							s[2] = String.valueOf(offset);
							s[3] = row_num;
							symTableInfo.add(s);
						}
						tbmodel_sym.add(new String[]{id,s_p[1],String.valueOf(offset),row_num});
						offset += Integer.valueOf(s_p[2]);
					}else if(left.equals("Ta")){//Ta -> empty {type=X.type; width=X.width;}
						String[] info = getElementByName("X");
						type = info[1];
						width = info[2];
					}else if(left.equals("Ma")){//Ma -> empty {enter(id.lexeme,X.type,offset); offset = offset+X.width;}
						String id = getElemByType("IDN");
						symTable.add(id); //�βμ��뵽���ű��У���"����"�п���ֱ��ʹ��
						String[] s = new String[4];//0����ʶ��  1������  2��ƫ����  3�к�
						s[0] = id;
						s[1] = getElementByName("X")[1] ;
						s[2] = String.valueOf(offset);
						s[3] = row_num;
						symTableInfo.add(s);
						if(paramSet != null){
							paramSet.add(new String[]{id,s[1]}); //������������
						}else{
							System.out.println("1�βα��ǿյ�");
						}
						tbmodel_sym.add(new String[]{id,getElementByName("X")[1],String.valueOf(offset),row_num});
						offset += Integer.valueOf(getElementByName("X")[2]);
					}else if(left.equals("Mb")){//Mb -> empty {enter(id.lexeme,X.type,offset); offset = offset+X.width;}
						String id = getElemByType("IDN");
						symTable.add(id);//�βμ��뵽���ű��У��ڡ����̡��п���ֱ��ʹ��
						String[] s = new String[4];//0����ʶ��  1������  2��ƫ����  3�к�
						s[0] = id;
						s[1] = getElementByName("X")[1] ;
						s[2] = String.valueOf(offset);
						s[3] = row_num;
						symTableInfo.add(s);
						if(paramSet != null){
							if(paramSet.contains(id)){
								tbmodel_wrong_result.add(new String[]{id + " �β������ظ�", row_num});
							}else{
								paramSet.add(new String[]{id,s[1]}); //������������
							}
						}else{
							System.out.println("2 �βα��ǿյ�");
						}
						tbmodel_sym.add(new String[]{id,getElementByName("X")[1],String.valueOf(offset),row_num});
						offset += Integer.valueOf(getElementByName("X")[2]);
					}else if(left.equals("C")){
						if(rights[0].equals(SomeWord.EMPTY)){//C-> empty {C.type=t; C.width=w;}
							s_p[1] = type;
							s_p[2] = width;
						}else{//C->[ num ] C {C.type=array(num.val,C1.type); C.width=num.val*C.width;}
							if(rights[0].equals("[") && rights[2].equals("]") && rights[3].equals("C")){
								String[] info = popSymInfo.get(0);  //����ջ�ĵ�һ�����ţ��� C
								s_p[1] = "array(" + keys[2] + "," + info[1] + ")";
								s_p[2] =String.valueOf(Integer.valueOf(keys[2]) * Integer.valueOf(info[2]));
							}
						}
					}else if(left.equals("T")){
						if(rights[0].equals("X") && rights[2].equals("C")){
							//T->X Ta C {T.type=C.type; T.width=C.width;}
							String[] info = popSymInfo.get(0);
							s_p[1] = info[1];
							s_p[2] = info[2];
						}
					}else if(left.equals("F")){
						if(rights.length == 1){ //F->id | num
							if(getProductiuonRight(rights).equals("id")){//F->id 
								//�ӷ��ű���ȡid�ķ�����Ϣ
								String id = keys[0];
								if(!symTable.contains(id)){
									s_p[1] = "notdeclare";
									tbmodel_wrong_result.add(new String[]{id + "δ����������", row_num});
								}else{
									String[] info = getSymInfoById(id, symTableInfo);
									s_p[1] = info[1]; //type
								}
								if(getElementByName("[") != null){ //������
									String idType = getSymInfoById(id, symTableInfo)[1];
									if(idType.indexOf("int") == -1){//��������
										tbmodel_wrong_result.add(new String[]{"�����±���ʽ��: " + id + "����int�ͱ���", row_num});
									}
								}
							}else if(getProductiuonRight(rights).equals("num")){//F->num
								s_p[1] = "number"; //type
								if(getElementByName("[") != null){ //������
									String num = String.valueOf(keys[0]);
									if(num.indexOf(".") != -1){ //����������
										if(Character.isDigit(num.charAt(num.indexOf(".")-1))){
											tbmodel_wrong_result.add(new String[]{"�����±���ʽ��: " + num + "��������", row_num});
										}
									}
								}
							}
							s_p[3] = keys[0];//val
							s_p[4] = keys[0];//addr
						}else{ //F->( E ) {F.type=E.type; F.val=e.VAL; F.addr = E.addr}
							if(rights.length == 3){
								if(rights[0].equals("(") && rights[1].equals("E") && rights[2].equals(")")){
									//���F��addr��Ϣ
									String[] elemE = popSymInfo.get(1);//����ջ�ĵڶ����ַ�����E
									s_p[1] = elemE[1]; //type
									s_p[3] = elemE[3];
									s_p[4] = elemE[4];
								}
							}
						}
					}else if(left.equals("E")){
						if(rights.length > 1){
							if(rights[0].equals("G") && rights[1].equals("E'")){
								//E->G E' {E.addr = newtemp(); gen(E.addr ,��=�� ,E��.addr ,��+�� ,G.addr); }
								String[] elemE_ = popSymInfo.get(0);
								String[] elemG = popSymInfo.get(1);
								String addrG = elemG[4];
								String addrE_ = elemE_[4];
								s_p[1] = elemG[1]; //type
								if(elemE_[3] == null && elemE_[4] == null){//E'�ǰ�empty��Լ��
									s_p[4] = addrG; //addr
									s_p[3] = elemG[3]; //val
								}else{
									if(elemE_[1].equals("notnumber")){
										s_p[1] = "notnumber";
									}
									//������������ʱ��������������ַ
									String addrE = "t" + String.valueOf(newTemp);
									newTemp++;
									s_p[4] = addrE;
									String s3 = "";
									String s4 = "( ";
									String op = elemE_[2];
									s3 += addrE + " = " + addrG + op + addrE_;
									s4 += op + ", " + addrG + " , " + addrE_ + " , " + addrE + " )";
									System.out.println("E->G E'\t����ַ�룺 " + s3 + ", ��Ԫ��: " + s4);
									list_34 = addList34(list_34, seq, new String[]{String.valueOf(seq), s3, s4, row_num});
									seq++;
									s_p[3] = elemG[3] + op + elemE_[3];
								}
							}
						}else if(rights[0].equals("L")){//E->L
							String addrE = "t" + String.valueOf(newTemp);
							String[] infoL  = popSymInfo.get(0);
							String addrL = infoL[4];
							System.out.println("�� value��"+infoL[3]);
							System.out.println("addr��"+infoL[4]);
							System.out.println("type��"+infoL[1]);
							if(infoL[2]!=null){//����
								//�����ķ����ƣ��˴��������飬�����ֹ�Լ-��Լ��ͻ
								//�ȴ����ʵ��ķ�
								String valL = infoL[3].substring(0, infoL[3].indexOf("[")); //id
								newTemp++;
								String s3 = "";
								String s4 = "( =, ";
								s3 += addrE + " = " +valL +"["+addrL+"]";
								s4 +=valL +"["+addrL+"]"+" , -  , "+addrE+ " )";
								System.out.println("E->L   s3��"+s3+"��s4��"+s4);
								//tbmodel_34.addRow(new String[]{String.valueOf(seq),s3,s4,row_num});
								list_34 =addList34(list_34, seq, new String[]{String.valueOf(seq),s3,s4,row_num});
								seq++;
								//s_p[5] = infoL[5];
							}
							s_p[1] = infoL[1];
							s_p[3] = infoL[3];
							s_p[4] = addrE;
						}
					}else if(left.equals("E'")){
						if(rights.length > 1){
							if(rights[1].equals("G") && rights[2].equals("E'")){//E'->+ G E'|-G E' {E��.addr = G.addr}
								//�������㣺�� �������ȼ���
								String[] elemG = popSymInfo.get(1);;
								s_p[1] = elemG[1];
								if(!(s_p[1].equals("number")
										|| s_p[1].equals("int")||s_p[1].equals("float")||s_p[1].equals("double"))){
									tbmodel_wrong_result.add(new String[]{elemG[3]+"���ǲ�����",row_num});
									s_p[1] = "notnumber";
								}
								s_p[2] = keys[2]; //��widthλ�ô洢�����
								s_p[3] = elemG[3];
								s_p[4] = elemG[4];
							}
						}
					}else if(left.equals("G")){
						if(rights[0].equals("F") && rights[1].equals("G'")){
							//G-> F G' {G.addr = newtemp(); gen(G.addr, ��=��, F.addr, ��+��, G��.addr);}
							String[] elemF = popSymInfo.get(1); //����ջʱ���յģ�F
							String[] elemG_ = popSymInfo.get(0); //G
							String[] infoL = getElementByName("L");
							String addrF = elemF[4];
							String addrG_ = elemG_[4];
							s_p[1] = elemF[1];//type
							if(infoL != null && !infoL[1].equals(s_p[1])){
								if(infoL[1].equals("int")){
									if(s_p[1].equals("number")){
										int pos = infoL[3].indexOf(".");
										if(pos != -1){
											tbmodel_wrong_result.add(new String[]{elemF[3] + "����������", row_num});
										}
									}else{
										tbmodel_wrong_result.add(new String[]{elemF[3] + "������ֵ����", row_num});
									}
									s_p[1] = "notnumber";//ָ��Ϊһ�����������ͣ�ʹ����������ȥ
								}else if(infoL[1].equals("float")){
									if(!s_p[1].equals("number")){
										tbmodel_wrong_result.add(new String[]{elemF[3] + "���Ǹ����ͱ���", row_num});
									}
									s_p[1] = "notnumber";//ָ��Ϊһ�����������ͣ�ʹ����������ȥ
								}else if(infoL[1].equals("char")){
									tbmodel_wrong_result.add(new String[]{elemF[3] + "�����ַ��ͱ���", row_num});
									s_p[1] = "notchar";//ָ��Ϊһ���ַ����ͣ�ʹ����������ȥ
								}
							}
							if(elemG_[3] == null && elemG_[4] == null){//G'�ǰ�empty��Լ��
								String addrG = addrF;
								s_p[4] = addrG;
								s_p[3] = elemF[3];//val
							}else {
								if(elemG_[3].equals("notnumber")){
									s_p[1] = "notnumber";//ָ��Ϊһ�����������ͣ�ʹ����������ȥ
								}else if(elemG_[3].equals("notchar")){
									s_p[1] = "notchar";//ָ��Ϊһ���ַ����ͣ�ʹ����������ȥ
								}
								//������������ʱ��������������ַ
								String addrG = "t" + String.valueOf(newTemp);
								newTemp++;
								s_p[4] = addrG;
								String s3 = "";
								String s4 = "( ";
								String op = elemG_[2];
								s3 += addrG + " = " + addrF + op + addrG_;
								s4 += op + ", " + addrF + " , " + addrG_ + " , " + addrG + " )";
								System.out.println("G-> F G'\t����ַ��: " + s3 + ", ��Ԫ��: " + s4);
								list_34 =addList34(list_34, seq, new String[]{String.valueOf(seq),s3,s4,row_num});
								seq++;
								s_p[3] = elemF[3] + op + elemG_[3];//val
							}
						}
					}else if(left.equals("G'")) {
						if(rights.length>1) {
							if(rights[1].equals("F") && rights[2].equals("G'")){//G'->* F G' {G��.addr = F.addr }
								//�������㣺�� �������ȼ���
								String[] elemF = popSymInfo.get(1);
								s_p[1] = elemF[1];
								if(!(s_p[1].equals("number")
										|| s_p[1].equals("int")||s_p[1].equals("float")||s_p[1].equals("double"))){
									tbmodel_wrong_result.add(new String[]{elemF[3]+"���ǲ�����",row_num});
									s_p[1] = "notnumber";//ָ��Ϊһ�����������ͣ�ʹ����������ȥ
								}
								s_p[2] = keys[2]; //��widthλ�ô洢�����
								s_p[3] = elemF[3];//val
								s_p[4] = elemF[4];//addr
							}
						}
					}else if(left.equals("S")) {
						if(rights.length > 1){
							if(rights[0].equals("L")){//S->L = E ; {gen(L.addr,��=��,E.addr); S.nextlist=nil;}
								if(rights[1].equals("=") && rights[2].equals("E") && rights[3].equals(";")){
									String typeE = popSymInfo.get(1)[1];
									String typeL = popSymInfo.get(3)[1]; 
									//�����к�
									for(int k1 = i - 1; k1 >= 0; k1--){
										String[] return_tuple = token.get(k1);
										if(return_tuple[0].equals("=")){
											row_num = return_tuple[2];
											break;
										}
									}
									//�жϵ�ʽ���������Ƿ�һ��
									if(!typeE.equals(typeL)){
										if(typeL.equals("number")){
											if(!(typeE.equals("int") || typeE.equals("float") || typeE.equals("double"))){
												tbmodel_wrong_result.add(new String[]{"��ʽ���ߵı������Ͳ�һ��",row_num});
											}
										}
									}
									String s3="",s4="( =,";
									String addrL = popSymInfo.get(3)[4];
									String addrE = popSymInfo.get(1)[4];
									s3 += addrL + " = " + addrE;
									s4 += addrE + " , - , " + addrL + " )";
									System.out.println("S->L = E ;\t����ַ��: " + s3 + ", ��Ԫ��: " + s4);
									list_34 =addList34(list_34, seq, new String[]{String.valueOf(seq),s3,s4,row_num});
									seq++;
									
									//����netxlist����ʼΪ�ա���whileʱ���
									List<String> nextlistS = new ArrayList<String>();
									s_p[6] = String.valueOf(position++);
									listMap.put(s_p[6],nextlistS);
								}
							}else if(rights[0].equals("return")){//S->return E ;
								String[] info = popSymInfo.get(1);
								String addrE = info[4];
								String type_return = info[1];
								String type_proc = getElementByName2("proc")[1];
								//�����к�
								for(int k1 = i - 1; k1 >= 0; k1--){
									String[] return_tuple = token.get(k1);
									if(return_tuple[0].equals("return")){
										row_num = return_tuple[2];
										break;
									}
								}
								//�жϷ���ֵ�Ƿ�Ϊ��������
								if(type_return.length() > 2 &&
										type_return.charAt(0) == 'a' && type_return.charAt(1) == 'r'){//��������
									String type_array = "";
									for(int k3 = type_return.lastIndexOf(",") + 1; k3 < type_return.length(); k3++){
										if(type_return.charAt(k3)==')'){
											break;
										}
										type_array += type_return.charAt(k3);
									}
									if(type_array.trim().equals("int")){
										type_return = "int";
									}
								}
								//�жϷ���ֵ���ͺ͹��̵������Ƿ�һ��
								if(!type_return.equals(type_proc)){
									if(type_return.equals("number")){
										if(!(type_proc.equals("int")||type_proc.equals("float")||type_proc.equals("double"))){
											tbmodel_wrong_result.add(new String[]{"����ֵ����: " + type_return + ", ����̵�����: " + type_proc + "��ƥ��", row_num});
										}
									}else{
										tbmodel_wrong_result.add(new String[]{"����ֵ����: " + type_return + ", ����̵�����: " + type_proc + "��ƥ��", row_num});
									}
								}
								String s3 = "return " + addrE;
								String s4 = "(return , - , -, " + addrE + ")";
								System.out.println("S->return E ;\t����ַ��: " + s3 + ", ��Ԫ��: ");
								list_34 = addList34(list_34, seq, new String[]{String.valueOf(seq), s3, s4, row_num});
								seq++;
							}else if(rights[0].equals("if")){//S->if B then Sa S Sb else Sc S 
								//{backpatch(B.truelist=Sa.quad); backpatch(B.falselist=Sc.quad); S.nextlist=merge(merge(S1.nextlist,Sb.nextlist),S2.nextlist);}
								//����
								String[] infoB = popSymInfo.get(7);
								String[] infoSa = popSymInfo.get(5);
								String[] infoSc = popSymInfo.get(1);
								String quadSa = infoSa[3];
								String quadSc = infoSc[3];
								List<String> truelistB = listMap.get(infoB[6]);
								List<String> falselistB = listMap.get(infoB[7]);
								//���� B.truelist
								Map<Integer,String[]> modify = new HashMap<Integer,String[]>();
								for(String s : truelistB){
									int k1 = 0;
									for(String[] s34:list_34){//����ַ����Ԫʽ
										if(s34[0].equals(s)){
											String[] s34_new = new String[4];
											s34_new[0] = s34[0];
											s34_new[2] = s34[2];
											s34_new[3] = s34[3];
											String s3 = s34[1];
											String s4 = s34[2];
											if(s3.indexOf("_") == -1){
												break;
											}
											String s3_new = s3.substring(0, s3.indexOf("_")) + quadSa;
											if(s4.indexOf("_") == -1){
												break;
											}
											String s4_new =  s4.substring(0, s4.indexOf("_")) + quadSa + " )";
											s34_new[1] = s3_new;
											s34_new[2] = s4_new;
											modify.put(k1, s34_new);
										}
										k1++;
									}
									//�滻
									for(Map.Entry<Integer, String[]> entry:modify.entrySet()){
										int pos = entry.getKey();
										String[] s34_new = entry.getValue();
										list_34.remove(pos);
										list_34.add(pos, s34_new);
									}
								}
								//���� B.falselist
								modify = new HashMap<Integer,String[]>();
								for(String s : falselistB){
									int k1=0;
									for(String[] s34:list_34){//����ַ����Ԫʽ
										if(s34[0].equals(s)){
											String[] s34_new = new String[4];
											s34_new[0] = s34[0];
											s34_new[2] = s34[2];
											s34_new[3] = s34[3];
											String s3 = s34[1];
											String s4 = s34[2];
											if(s3.indexOf("_") == -1){
												break;
											}
											String s3_new =  s3.substring(0, s3.indexOf("_")) + quadSc;
											if(s4.indexOf("_")==-1){
												break;
											}
											String s4_new = s4.substring(0, s4.indexOf("_")) + quadSc + " )";
											s34_new[1] = s3_new;
											s34_new[2] = s4_new;
											modify.put(k1,s34_new);
										}
										k1++;
									}
									//�滻
									for(Map.Entry<Integer, String[]> entry : modify.entrySet()){
										int pos = entry.getKey();
										String[] s34_new = entry.getValue();
										list_34.remove(pos);
										list_34.add(pos, s34_new);
									}
								}
								
								//S.nextlist
								String[] infoS1 = popSymInfo.get(4);
								String[] infoS2 = popSymInfo.get(0);
								String[] infoSb = popSymInfo.get(3);
								List<String> nextlistSb = listMap.get(infoSb[6]);
								List<String> nextlistS1 = listMap.get(infoS1[6]);
								List<String> nextlistS2 =listMap.get(infoS2[6]);
								List<String> nextlistS = merge(merge(nextlistS1, nextlistSb), nextlistS2);
								s_p[6] = String.valueOf(position++);
								listMap.put(s_p[6], nextlistS);
							}else if(rights[0].equals("while")){//S->while Sd B do Se S
								//{backpatch(S1.nextlist,Sd.quad); backpatch(B.truelist,Se.quad); S.nextlist=B.falselist; gen(��goto��,Sd.quad); }
								//����
								String[] infoS1 = popSymInfo.get(0);
								String[] infoB = popSymInfo.get(3);
								String[] infoSd = popSymInfo.get(4);
								String[] infoSe = popSymInfo.get(1);
								String quadSd = infoSd[3];
								String quadSe = infoSe[3];
								
								//���� S1.nextlist
								List<String> nextlistS1 = listMap.get(infoS1[6]);
								Map<Integer,String[]> modify = new HashMap<Integer,String[]>();
								for(String s : nextlistS1){
									int k1 = 0;
									for(String[] s34 : list_34){//����ַ����Ԫʽ
										if(s34[0].equals(s)){
											String[] s34_new = new String[4];
											s34_new[0] = s34[0];
											s34_new[2] = s34[2];
											s34_new[3] = s34[3];
											String s3 = s34[1];
											String s4 = s34[2];
											if(s3.indexOf("_") == -1){
												break;
											}
											String s3_new = s3.substring(0, s3.indexOf("_")) + quadSd;
											if(s4.indexOf("_") == -1){
												break;
											}
											String s4_new = s4.substring(0, s4.indexOf("_")) + quadSd + " )";
											s34_new[1] = s3_new;
											s34_new[2] = s4_new;
											modify.put(k1, s34_new);
										}
										k1++;
									}
									//�滻
									for(Map.Entry<Integer, String[]> entry:modify.entrySet()){
										int pos = entry.getKey();
										String[] s34_new = entry.getValue();
										list_34.remove(pos);
										list_34.add(pos, s34_new);
									}
								}
								
								//���� B.truelist
								List<String>truelistB = listMap.get(infoB[6]);
								modify = new HashMap<Integer,String[]>();
								for(String s : truelistB){
									int k1 = 0;
									for(String[] s34 : list_34){//����ַ����Ԫʽ
										if(s34[0].equals(s)){
											String[] s34_new = new String[4];
											s34_new[0] = s34[0];
											s34_new[2] = s34[2];
											s34_new[3] = s34[3];
											String s3 = s34[1];
											String s4 = s34[2];
											if(s3.indexOf("_") == -1){
												break;
											}
											String s3_new =  s3.substring(0, s3.indexOf("_")) + quadSe;
											if(s4.indexOf("_") == -1){
												break;
											}
											String s4_new = s4.substring(0, s4.indexOf("_")) + quadSe + " )";
											s34_new[1] = s3_new;
											s34_new[2] = s4_new;
											modify.put(k1,s34_new);
										}
										k1++;
									}
									//�滻
									for(Map.Entry<Integer, String[]> entry:modify.entrySet()){
										int pos = entry.getKey();
										String[] s34_new = entry.getValue();
										list_34.remove(pos);
										list_34.add(pos, s34_new);
									}
								}
								
								//S.nextlist
								List<String> falselistB = listMap.get(infoB[7]);
								s_p[6] = String.valueOf(position++);
								listMap.put(s_p[6],falselistB);
								
								//���ز������ʽ�ж�
								String s3 = "goto " + quadSd;
								String s4 = "(j , - ,  -  , " + quadSd + " )";
								list_34 =addList34(list_34, seq, new String[]{String.valueOf(seq), s3, s4, row_num});
								seq++;
							}else if(rights[0].equals("call")){//S->call id ( Elist ) ;
								//{n=0; for q�е�ÿ��t do{gen(��param��,t);  n=n+1;}; gen(��call��,id.addr,��,��,n);}
								if(rights[1].equals("id") && rights[2].equals("(") && rights[3].equals("Elist")){
									if(paramListCall == null){
										System.out.println("���ó���");
									}else{
										//��Ӵ����жϣ���������
										String id = keys[4];
										List<String[]> paramListProc = new ArrayList<String[]>();
										paramListProc = processMap.get(id);
										if(paramListProc == null){
											tbmodel_wrong_result.add(new String[]{"�Էǹ��̱���: " + id + "\tʹ�ù��̵��÷���call", row_num});
										}else{
											if(paramListProc.size()!=paramListCall.size()){
												tbmodel_wrong_result.add(new String[]{"�����������: " + paramListCall.size() + "\t�������Ĺ��̲�ƥ��", row_num});
											}else{
												String paramCall = null, paramCallType = null;
												List<String[]> paramListCallNew = new ArrayList<String[]>();
												//��Ĳ��������ǵ���ģ����Ե����ӡ
												int plen = paramListCall.size() - 1;
												for(int k1 = 0; k1 < paramListCall.size(); k1++){
													paramListCallNew.add(paramListCall.get(plen--));
												}
												int k1 = 0;
												for(String[] s:paramListCallNew){
													paramCall = s[0];
													paramCallType = s[1];
													String paramProcType = paramListProc.get(k1)[1];
													if(!paramProcType.equals(paramCallType)){
														boolean flag = (paramProcType.equals("int")||paramProcType.equals("float"))
																&&paramCallType.equals("number");
														if(!flag){
															tbmodel_wrong_result.add(new String[]{"�������: " + paramCall + ", ����: " + paramCallType +
																	", �������Ĺ��̵Ĳ�������: " + paramProcType + "��ƥ��", row_num});
														}
													}
													String s3 = "param " + paramCall;
													String s4 = "(j , - ,  -  , " + paramCall + " )";
													list_34 = addList34(list_34, seq, new String[]{String.valueOf(seq), s3, s4, row_num});
													seq++;
													k1++;
												}
												String s3 = "call " + id + " , " + paramListCall.size();
												String s4 = "(" + paramListCall.size() + " , call ,  -  , " + id + " )";
												list_34 = addList34(list_34, seq, new String[]{String.valueOf(seq), s3, s4, row_num});
												seq++;
											}
										}
									}
								}
							}
						}
					}else if(left.equals("L")){
						if(rights.length > 1){//L->id L' {L.addr = lookup(id.lexeme); if L.addr=nil then error;}
							if(rights[0].equals("id") && rights[1].equals("L'")){
								String id = keys[1];
								if(symTable.contains(id)){
									String[] infoL_ = popSymInfo.get(0);
									if(infoL_[4] == null){//���մ�L'->empty ��Լ�ģ���������
										String[] infoId = getSymInfoById(id, symTableInfo);
										s_p[1] = infoId[1];//type
										s_p[3] = id;
										s_p[4] = id;
									}else{
										String addrL_ = infoL_[4];
										s_p[1] = infoL_[1];//�����type
										s_p[2] = infoL_[2];//width
										s_p[3] = id+infoL_[3];//val
										s_p[4] =addrL_;
										if(a.equals("=")){//�Ⱥ�����������飬��������и�ֵ
											String offsetL = "t" + String.valueOf(newTemp);
											newTemp++;
											s_p[4] =  offsetL;//addr
											String s3 = "";
											String s4 = "( =, ";
											s3 += offsetL + " = " + id + "["+addrL_+"]";
											s4 += id + "[" + addrL_ + "]" + " , -  , " + offsetL + " )";
											System.out.println("L->id L'\t����ַ��: "+s3+", ��Ԫ��: "+s4);
											list_34 =addList34(list_34, seq, new String[]{String.valueOf(seq), s3, s4, row_num});
											seq++;
										}	
									}
								}else{
									s_p[1] = "notdeclare";
									tbmodel_wrong_result.add(new String[]{id + "δ����������", row_num});
									s_p[3] = id;
								}
							}
						}
					}else if(left.equals("L'")){
						if(rights.length>1){//L'-> [ E ] L'
							String[] infoE = popSymInfo.get(2);
							String num = infoE[4];
							if(num == null){
								num = infoE[3];
							}
							String[] infoL_1 = popSymInfo.get(0);
							String typeL_1 = infoL_1[1];
							s_p[1] =typeL_1 ;
							String widthL_1 = null;
							if(infoL_1[2] != null){
								widthL_1 = infoL_1[2].substring(0, infoL_1[2].indexOf(","));
							}
							String valL_1 = infoL_1[3];
							String addrL_1 = infoL_1[4];
							if(!typeL_1.equals("notdeclare")){
								if(widthL_1 == null){
									//L' ���մ���Լ���Ҳ�����������
								}else{
									//��������
									String temp = "t" + String.valueOf(newTemp);
									newTemp++;
									String s3 = "";
									String s4 = "( *, ";
									s3 += temp + " = " +num +"*"+widthL_1;
									s4 += num+" , "+widthL_1+" , "+temp+ " )";
									System.out.println("1  L'->[ E ] L'\t����ַ��: "+s3+", ��Ԫʽ: "+s4);
									list_34 =addList34(list_34, seq, new String[]{String.valueOf(seq),s3,s4,row_num});
									seq++;
									
									String offsetL_ = "t" + String.valueOf(newTemp);
									newTemp++;
									s3 = "";
									s4 = "( +, ";
									s3 += offsetL_ + " = " +addrL_1 +"+"+temp;
									s4 += addrL_1+" , "+temp+" , "+offsetL_+ " )";
									System.out.println("2  L'->[ num ] L'\t����ַ��: "+s3+", ��Ԫʽ: "+s4);
									list_34 =addList34(list_34, seq, new String[]{String.valueOf(seq),s3,s4,row_num});
									seq++;
									
									int array_len = Integer.valueOf(infoL_1[2].substring(infoL_1[2].indexOf(",")+1,  infoL_1[2].length()));
									s_p[2] = String.valueOf(array_len*Integer.valueOf(widthL_1));
									String array_len_next = getArrayLen(array_depth,arrayType);
									array_depth++;
									s_p[2] += ","+array_len_next;
									s_p[3] = keys[3]+num+keys[1]+valL_1;//val���ۼ�
									s_p[4] = offsetL_;//addr��Ҳ����offset
								}
							}else{
								System.out.println("����δ����");
							}
						}else{ //L'->empty
							String id = getElemByType("IDN");
							if(id!=null){
								if(!symTable.contains(id)){
									s_p[1] = "notdeclare";
									tbmodel_wrong_result.add(new String[]{id+"δ����������",row_num});
								}else{
									String[] info = getSymInfoById(id, symTableInfo);
									s_p[1] = info[1]; //type
									if(s_p[1].length() > 2 && s_p[1].charAt(0) == 'a' && s_p[1].charAt(1) == 'r'){//��������
										arrayType = info[1];//��¼���������
										array_depth = 0;//���
										String type_array = "";
										for(int k3=s_p[1].lastIndexOf(",")+1;k3<s_p[1].length();k3++ ){
											if(s_p[1].charAt(k3)==')'){
												break;
											}
											type_array+=s_p[1].charAt(k3);
										}
										if(type_array.trim().equals("int")){
											s_p[1] = "int";
											s_p[2] = "4";
											s_p[3] = "";
										}else if(s_p[1].equals("char")){
											s_p[1] = "char";
											s_p[2] = "1";
											s_p[3] = "";
										}else if(s_p[1].equals("real")){
											s_p[1] = "real";
											s_p[2] = "8";
											s_p[3] = "";
										}
										String array_len = getArrayLen(array_depth,arrayType);
										array_depth++;
										s_p[2] +=","+array_len;
										s_p[4]= info[2];//offset��base
									}else{
										if(getElementByName("]")!=null){
											tbmodel_wrong_result.add(new String[]{"�Է��������ͱ���"+id+"ʹ��������ʷ�",row_num});
										}
										System.out.println("===������������===");
									}
								}
							}
						}
					}else if(left.equals("Relop")){//Relop->==|<|>|.......
						String logic_op = keys[0];
						s_p[1] = "logic_op";//type
						s_p[3] = logic_op; //val
					}else if(left.equals("I")){//I->not B|( B )|E Relop E|true|false
						if(rights[0].equals("true")){
							//truelist
							s_p[6] = String.valueOf(position++);
							List<String> list = new ArrayList<String>();
							list.add(String.valueOf(seq));
							listMap.put(s_p[6], list);
							String s3 = "goto _";
							String s4 = "(j , - ,  -  , _ )";
							System.out.println("I->true\t����ַ��: " + s3 + ", ��Ԫʽ: "+s4);
							list_34 = addList34(list_34, seq, new String[]{String.valueOf(seq++),s3,s4,row_num});
						}else if(rights[0].equals("false")){
							//falselist
							s_p[7] = String.valueOf(position++);
							List<String> list = new ArrayList<String>();
							list.add(String.valueOf(seq));
							listMap.put(s_p[7], list);
							String s3 = "goto _";
							String s4 = "(j , - ,  -  , _ )";
							System.out.println("I->false\t����ַ��: "+s3+", ��Ԫʽ: "+s4);
							list_34 = addList34(list_34, seq, new String[]{String.valueOf(seq++),s3,s4,row_num});
						}else if(rights[0].equals("not")){
							String[] infoB = popSymInfo.get(0);
							s_p[6] = infoB[7];
							s_p[7] = infoB[6];
						}else if(rights[0].equals("(")){
							String[] infoB = popSymInfo.get(1);
							s_p[6] = infoB[6];
							s_p[7] = infoB[7];
						}else if(rights[0].equals("E")){
							String addrE1 = popSymInfo.get(2)[3];
							String addrE2 = popSymInfo.get(0)[3];
							String typeE1 = popSymInfo.get(2)[1];
							String typeE2 = popSymInfo.get(0)[1];
							String logic_op = popSymInfo.get(1)[3];
							if(!typeE1.equals(typeE2)){
								if(typeE1.equals("number")){
									if(!(typeE2.equals("int")||typeE2.equals("float")||typeE2.equals("double"))){
										tbmodel_wrong_result.add(new String[]{"�������ʽ "+addrE1+" "+logic_op+" "+addrE2+" ���󣬲��ܱȽ�",row_num});
									}
								}else if(typeE2.equals("number")){
									if(!(typeE1.equals("int")||typeE1.equals("float")||typeE1.equals("double"))){
										tbmodel_wrong_result.add(new String[]{"�������ʽ "+addrE1+" "+logic_op+" "+addrE2+" ���󣬲��ܱȽ�",row_num});
									}
								}else{
									tbmodel_wrong_result.add(new String[]{"�������ʽ "+addrE1+" "+logic_op+" "+addrE2+" ���󣬲��ܱȽ�",row_num});
								}
							}
							//truelist
							s_p[6] = String.valueOf(position++);
							List<String> list = new ArrayList<String>();
							list.add(String.valueOf(seq));
							if(listMap.containsKey(s_p[6])){
								
							}
							listMap.put(s_p[6], list);
							//falselist
							s_p[7] = String.valueOf(position++);
							list = new ArrayList<String>();
							list.add(String.valueOf(seq+1));
							listMap.put(s_p[7], list);
							
							String s3 = "if " + addrE1 + " " + logic_op + " " + addrE2 + "  goto _";
							String s4 = "(j " + logic_op + ", " + addrE1 + " ,  " + addrE2 + "  , _ )";
							System.out.println("���: "+(seq)+", 1  I->E Relop E\t����ַ��: " + s3 + ", ��Ԫ��: " + s4);
							list_34 = addList34(list_34, seq, new String[]{String.valueOf(seq++), s3 ,s4, row_num});
							
							s3 = "goto _";
							s4 = "(j , - ,  -  , _ )";
							System.out.println("���: "+(seq)+", 2  I->E Relop E\t����ַ��: " + s3 + ", ��Ԫ�� : " + s4);
							list_34 = addList34(list_34, seq, new String[]{String.valueOf(seq++), s3, s4, row_num});
						}
					}else if(left.equals("B")){//B->H B'
						String[] infoB_ = popSymInfo.get(0);
						if(infoB_[6]==null || infoB_[7]==null){//��empty��Լ
							String[] infoH = popSymInfo.get(1);
							s_p[6] = infoH[6];
							s_p[7] = infoH[7];
						}else{
							s_p[6] = infoB_[6];
							s_p[7] = infoB_[7];
						}
					}else if(left.equals("B'")){//B'->or Bma H B'
						if(rights.length>1&&rights[0].equals("or")){
							String[] infoH1 = getElementByName("H");
							String[] infoH2 = popSymInfo.get(1);
							String[] infoBma = popSymInfo.get(2);
							String quad = infoBma[3];
							List<String> falselistH1 = listMap.get(infoH1[7]);
							//����
							Map<Integer,String[]> modify = new HashMap<Integer,String[]>();
							for(String s:falselistH1){
								int k1=0;
								for(String[] s34:list_34){//����ַ����Ԫʽ
									if(s34[0].equals(s)){
										String[] s34_new = new String[4];
										s34_new[0] = s34[0];
										s34_new[2] = s34[2];
										s34_new[3] = s34[3];
										String s3 = s34[1];
										String s4 = s34[2];
										String s4_new = s4;
										String s3_new =  s3.substring(0, s3.indexOf("_")) + quad;
										if(s4.indexOf("_") != -1){
											s4_new =  s4.substring(0, s4.indexOf("_")) + quad + " )";
										}
										s34_new[1] = s3_new;
										s34_new[2] = s4_new;
										modify.put(k1,s34_new);
									}
									k1++;
								}
								//�滻
								for(Map.Entry<Integer, String[]> entry:modify.entrySet()){
									int pos = entry.getKey();
									String[] s34_new = entry.getValue();
									list_34.remove(pos);
									list_34.add(pos, s34_new);
								}
							}
							
							//truelist
							List<String> truelistH1 = listMap.get(infoH1[6]);
							List<String> truelistH2 = listMap.get(infoH2[6]);
							List<String> truelistNew = merge(truelistH1,truelistH2);
							s_p[6] = String.valueOf(position++);
							listMap.put(s_p[6], truelistNew);
							//falselist
							s_p[7] = String.valueOf(position++);
							List<String> falselistH2 = listMap.get(infoH2[7]);
							listMap.put(s_p[7], falselistH2);
						}
					}else if(left.equals("H")){//H->I H'
						String[] infoH_ = popSymInfo.get(0);
						if(infoH_[6]==null || infoH_[7]==null){//���մ���Լ
							String[] infoi = popSymInfo.get(1);
							s_p[6] = infoi[6];
							s_p[7] = infoi[7];
						}else{
							s_p[6] = infoH_[6];
							s_p[7] = infoH_[7];
						}
					}else if(left.equals("H'")){//H'-> and Bmb I H'
						if(rights.length>1&&rights[0].equals("and")){
							//����
							String[] infoi1 = getElementByName("I");
							String[] infoi2 = popSymInfo.get(1);
							String[] infoBmb = popSymInfo.get(2);
							String quad = infoBmb[3];
							List<String> truelisti1 = listMap.get(infoi1[6]);
							Map<Integer,String[]> modify = new HashMap<Integer,String[]>();
							for(String s:truelisti1){
								int k1=0;
								for(String[] s34:list_34){//����ַ����Ԫʽ
									if(s34[0].equals(s)){
										String[] s34_new = new String[4];
										s34_new[0] = s34[0];
										s34_new[2] = s34[2];
										s34_new[3] = s34[3];
										String s3 = s34[1];
										String s4 = s34[2];
										String s4_new = s4;
										String s3_new =  s3.substring(0, s3.indexOf("_"))+quad;
										if(s4.indexOf("_")!=-1){
											s4_new =  s4.substring(0, s4.indexOf("_"))+quad + " )";
										}
										s34_new[1] = s3_new;
										s34_new[2] = s4_new;
										modify.put(k1,s34_new);
									}
									k1++;
								}
								for(Map.Entry<Integer, String[]> entry:modify.entrySet()){
									int pos = entry.getKey();
									String[] s34_new = entry.getValue();
									list_34.remove(pos);
									list_34.add(pos, s34_new);
								}
							}
							
							//truelist
							List<String> truelisti2 = listMap.get(infoi2[6]);
							s_p[6] = String.valueOf(position++);
							listMap.put(s_p[6], truelisti2);
							//falselist
							s_p[7] = String.valueOf(position++);
							List<String> falselisti1 = listMap.get(infoi1[7]);
							List<String> falselisti2 =  listMap.get(infoi2[7]);
							List<String> falselistNew = merge(falselisti1,falselisti2);
							listMap.put(s_p[7], falselistNew);
						}
					}else if(left.equals("Sb")){//Sb->empty {Sb.nextlist = makelist(nextquad) ; gen(��goto _��);}
						List<String> nextlist = new ArrayList<String>();
						s_p[6] = String.valueOf(position++);
						nextlist.add(String.valueOf(seq));
						listMap.put(s_p[6], nextlist);
						//gen
						String s3 = "goto _";
						String s4 = "(j , - ,  -  , _ )";
						System.out.println("���: " + seq + ", Sb->empty\t����ַ��: " + s3 + ", ��Ԫ��: " + s4);
						list_34 = addList34(list_34, seq, new String[]{String.valueOf(seq++),s3,s4,row_num});
					}else if(left.equals("D")){//D->proc X id Dc ( M ) { P }
						if(rights.length==10 && rights[0].equals("proc")){
							String id = keys[7];
							processMap.put(id, paramSet);//�������������
						}
					}else if(left.equals("Elist")){ //Elist->E Elist' 
						if(rights.length == 2){
							if(rights[0].equals("E") && rights[1].equals("Elist'")){
								String[] infoE = popSymInfo.get(1);
								String[] infoElist_ = popSymInfo.get(0);
								String addrE = infoE[4];
								if(addrE==null){
									addrE = infoE[3];
								}
								String[] parmInfo = new String[2];
								parmInfo[0] = addrE;		//������
								parmInfo[1] = infoE[1]; //type
								if(infoElist_[1]==null){
									paramListCall = new ArrayList<String[]>();
								}
								paramListCall.add(parmInfo);
							}
						}
					}else if(left.equals("Elist'")){ //Elist'->, E Elist'
						if(rights.length==3){
							if(rights[0].equals(",")&&
								rights[1].equals("E")&&
								rights[2].equals("Elist'")){
								//��ʼ��Ϊֻ����E.addr
								String[] infoE = popSymInfo.get(1);
								String[] paramInfo = new String[2];
								paramInfo[0] = infoE[4];
								paramInfo[1] = infoE[1];
								paramListCall = new ArrayList<String[]>();
								paramListCall.add(paramInfo);
								s_p[1] = "not null"; //��ʾElist'���ǰ��չ�Լ��
							}
						}
					}else if(left.equals("Sa") || left.equals("Sc") || left.equals("Sd") || 
							left.equals("Se") || left.equals("Bma") || left.equals("Bmb")){//nextquad
						s_p[3] = String.valueOf(seq);
					}else if(left.equals("X")){
						if(rights.length==1){
							if(rights[0].equals("int")){//X->int {X.type=int; X.width=4;}
								s_p[1] = "int";
								s_p[2] = "4";
							}else if(rights[0].equals("real")){//X->real {X.type=real; X.width=8;}
								s_p[1] = "real";
								s_p[2] = "8";
							}else if(rights[0].equals("char")){//X->char {X.type=char; X.width=1;}
								s_p[1] = "char";
								s_p[2] = "1";
							}
						}
					}
					s_p[0] = status_new;
					stack.push(s_p);

//------*-*------*-*------*-*------*-*------*-*------*-*------*-*------*-*------*-*------*-*------*-*------*-*------*-*------*-*------*-*
				}else if(action[top][n].equals("acc")){
					for(String[] s34:list_34){
						tbmodel_34.add(new String[]{s34[0],s34[1],s34[2],s34[3]});
					}
					tbmodel_34.add(new String[]{String.valueOf(seq),"exit","_",row_num});
					position=0;
					list_34.clear();
					System.out.println("ջ��״̬" + top + ", �����������: " + a + "������. �������");
					break;
				}else {//���ô��������
					System.out.println("��������: " + a + ", ����" + n + ", ջ��״̬: " + top);
					System.out.print(GetProduction.INT_SYM.get(n) + ": ");
					System.out.println(action[top][n]);
					i = i - 2; //����һ������, iΪ��ǰtoken���
					if(i < 0){
						System.out.println("�����������䣡");
						tbmodel_wrong_result.add(new String[]{"����ķ��ţ�" + a, row_num});
						i = i + 2;
						if(i < token.size()){
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
					}else {
						String sym_error_before = token.get(i)[0];
						int ret = errorHandler(top,sym_error_before,a,i);
						System.out.println("ret��: "+ret);
						if(ret!=0){
							 i =ret;
							 System.out.println("��ǰi��: "+i);
							 if(i<token.size()){
								 System.out.println("��ǰi��: "+i+", ����: "+token.get(i)[0]);
							 }
						}else{
							i = i + 1;
						}
						if(i < token.size()){
							tuple = token.get(i);
							a = tuple[0] ;//��һ���������
							kind = tuple[1];
							if(a == null){
								row_num = tuple[2];
								error = tuple[3];
							}else{
								if(!a.equals("$")){
									row_num = tuple[2];
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
		
		private int hope_seq = 0;
		//������
		public int errorHandler(int top,String sym_error_before,String sym_error,int position){
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
				if(i < token.size()){
					for(String[] s:no_use){
						tbmodel_wrong_result.add(new String[]{"����ķ��ţ�"+s[0],s[1]});
						//System.out.println("1����ķ��ţ�"+s[0]);
					}
					position_new = i;
					return position_new;
				}
			}else{
				//�������ķ�����$
			}
			System.out.println("��$ ���ţ�" + position_new);
			
			if(sym_error_before.equals(";")){
				System.out.println("���������ţ�" + sym_error);
				if(!sym_error.equals("$")){
					tbmodel_wrong_result.add(new String[]{"����ķ��ţ�"+sym_error,token.get(position)[2]});
				}
				token.remove(position);
				return position_new;
			}
			if(sym_error_before.equals("]")){
				if(!isHaveSymInStackByName("[")){
					System.out.println("���������ţ�" + sym_error);
					if(!sym_error.equals("$")){
						tbmodel_wrong_result.add(new String[]{"����ķ��ţ�"+sym_error,token.get(position)[2]});
					}
					token.remove(position);
					return position_new;
				}
			}
			if(sym_error_before.equals(")")){
				if(!isHaveSymInStackByName("(")){
					System.out.println("���������ţ�" + sym_error);
					if(!sym_error.equals("$")){
						tbmodel_wrong_result.add(new String[]{"����ķ��ţ�"+sym_error,token.get(position)[2]});
					}
					token.remove(position);
					return position_new;
				}
			}
			if(sym_error_before.equals("}")){
				if(!isHaveSymInStackByName("{")){
					System.out.println("���������ţ�" + sym_error);
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
					token.add(position + 1, hope);
					flag = true;
					return position_new;
				}
			}
			if(action_hope_name.contains("]")){
				System.out.println("�з�����");
				boolean flag1 =isHaveSymInStackByName("[");
				if(flag1){
					System.out.println("��ȫ ] ");
					tbmodel_wrong_result.add(new String[]{"ȱ�ٷ���  ]", token.get(position)[2]});
					hope[0] = "]";
					hope[2] = token.get(position)[2];
					token.add(position+1, hope);
					flag = true;
					return position_new;
				}
			}
			if(action_hope_name.contains("}")){
				System.out.println("�д�����");
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
					hope[0] = "<hope_id" + (hope_seq++) + ">";
					hope[1] = "IDN";
					hope[2] = token.get(position)[2];
					token.add(position+1, hope);
					flag = true;
					return position_new;
				}
				if(sym_error_before.equals("+") || sym_error_before.equals("-")
						||sym_error_before.equals("*")||sym_error_before.equals("/")
						|| sym_error_before.equals("$")){
					//���������,�����������,�id
					System.out.println("========ȱ�������=========");
					tbmodel_wrong_result.add(new String[]{"ȱ�������",token.get(position)[2]});
					hope[0] = "<hope_id"+(hope_seq++)+">";
					hope[1] = "IDN";
					hope[2] = token.get(position)[2];
					token.add(position+1, hope);
					flag = true;
					return position_new;
				}
				if(sym_error_before.equals("=") || sym_error_before.equals(",")){
					//�Ⱥ��Ҳ�����,����������������ʽ��������,�id
					System.out.println("========ȱ�˱���=========");
					tbmodel_wrong_result.add(new String[]{"ȱ�ٱ���",token.get(position)[2]});
					hope[0] = "<hope_id"+(hope_seq++)+">";
					hope[1] = "IDN";
					hope[2] = token.get(position)[2];
					token.add(position+1, hope);
					flag = true;
					return position_new;
				}
			}else if(action_hope_name.contains("=")){
				System.out.println("========ȱ�ٵȺ�=========");
				tbmodel_wrong_result.add(new String[]{"ȱ�ٵȺ�",token.get(position)[2]});
				hope[0] = "=";
				hope[2] = token.get(position)[2];
				token.add(position+1, hope);
				flag = true;
				return position_new;
			}else if(action_hope_name.contains("num")){
				System.out.println("========ȱnum=========");
				tbmodel_wrong_result.add(new String[]{"ȱ������",token.get(position)[2]});
				hope[0] = "-99999";
				hope[1] = "NUM";
				hope[2] = token.get(position)[2];
				token.add(position+1, hope);
				flag = true;
				return position_new;
			}else{
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
		
		//ջ���Ƿ���ĳ������
		public boolean isHaveSymInStackByName(String name){
			boolean flag = false;
			Enumeration<String[]> items = stack.elements();
			String s1;
			while(items.hasMoreElements()){
				s1 = items.nextElement()[0];
				if(s1.equals(name)){
					flag = true;
					break;
				}
			}
			return flag;
		}
		
		//���м�����ַ����ӵ�list�У����ͳһ��ӡ
		private List<String[]> addList34(List<String[]> list_34, int seq, String[] s34) {
			boolean isContain = false;
			for(String[] s:list_34){
				if(s[0].equals(seq)){
					isContain = true;
					break;
				}
			}
			if(!isContain){
				list_34.add(s34);
			}
			return list_34;
		}

		//������list�ϲ���һ��
		private List<String> merge(List<String> list1, List<String> list2) {
			if(list1==null){
				return list2;
			}
			if(list2==null){
				return list1;
			}
			for(String s:list2){
				if(list1.contains(s)){
					continue;
				}
				list1.add(s);
			}
			return list1;
		}

		//������ȣ���ȡָ����ȣ������length
		private String getArrayLen(int depth, String arrayType) {
			String len = "",len2="";
			int dep = 0,pos=-1;
			for (int i = arrayType.length()-1; i >=0 ; i--) {
				char c = arrayType.charAt(i);
				if(c==','){
					if(dep==depth){
						pos = i;
						break;
					}
					++dep;
				}
			}
			if(pos==-1){
				System.out.println("��ȡ��� ���� ������������");
				return null;
			}else{
				for (int i = pos-1; i >=0 ; i--) {
					char c = arrayType.charAt(i);
					if(c=='('){
						break;
					}
					len += c;
				}
				for (int i = len.length()-1; i >=0; i--) {
					len2 += len.charAt(i);
				}
				return len2;
			}
			
		}

		//���ݱ�ʶ�������ڷ��ű�ȡ������Ϣ
		private String[] getSymInfoById(String id,Set<String[]> symTableInfo) {
			for(String[] s:symTableInfo){
				if(s[0].equals(id)){
					return s;
				}
			}
			return null;
		}
		
		
		//�������֣���ȡջ�п���ջ����Ԫ�ض�Ӧ��״̬
		public String[] getElementByName(String name){
			if(stack.isEmpty()){
				System.out.println("ջΪ��");
			}else{
				Enumeration<String[]> items = stack.elements();
				String[] s = null, s1 = null;
				//System.out.println("���ţ�"+name);
				while(items.hasMoreElements()){
					s = items.nextElement();
					//System.out.println("Ԫ�أ�"+s[0]);
					if(s[0].equals(name)){
						s1 = s;
						s1 = items.nextElement();//ȡ���Ŷ�Ӧ��״̬
						//System.out.println("1 Ԫ�أ�"+s1[0]);
					}
				}
				return s1;
			}
			return null;
		}
		
		//�������֣���ȡջ�п���ջ���� ���ķ��Ŷ�Ӧ�ĺ�һ�����ŵ�״̬
		public String[] getElementByName2(String name){
			if(stack.isEmpty()){
				System.out.println("ջΪ��");
			}else{
				Enumeration<String[]> items = stack.elements();
				String[] s = null,s1=null;
				while(items.hasMoreElements()){
					s = items.nextElement();
					System.out.println("Ԫ�أ�"+s[0]);
					if(s[0].equals(name)){
						s1 = items.nextElement();
						s1 = items.nextElement();
						s1 = items.nextElement();
						System.out.println("1 Ԫ�أ�"+s1[0]);
					}
				}
				return s1;
			}
			return null;
		}
		
		//ȡջ�У�����ջ����id
		public String getElemByType(String type){
			if(stack.isEmpty()){
				System.out.println("ջΪ��");
				return null;
			}else{
				Enumeration<String[]> items = stack.elements();
				String[] s = null;
				String elem = null;
				while(items.hasMoreElements()){
					s = items.nextElement();
					if(s.length > 1){
						if(s[1] != null && s[1].equals(type)){
							//System.out.println("1��Ԫ�أ�"+s[0]+"��"+s[1]);
							elem = s[0];
						}
					}
				}
				return elem;
			}
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
		
		public void print3Code() {  //��ӡ����ַ��
			int i = 0;
			System.out.println("\n------*-*---����ַ��---*-*------");
			for(i = 0; i < tbmodel_34.size(); i++){
				System.out.println(tbmodel_34.get(i)[0] + "\t" + tbmodel_34.get(i)[1]);
			}
		}
		
		public void print4Code() {  //��ӡ��Ԫʽ
			int i = 0;
			System.out.println("\n------*-*---��Ԫ��---*-*------");
			for(i = 0; i < tbmodel_34.size(); i++){
				System.out.println(tbmodel_34.get(i)[0] + "\t" + tbmodel_34.get(i)[2]);
			}
		}
		
		//��ӡ���ű�
		public void printTbmodel_sym() {
			System.out.println("���ű�: ");
			System.out.println("IDN\tTYPE\tOFFSET\tLINE");
			for(int i = 0; i < tbmodel_sym.size(); i++) {
				for(int j = 0; j < tbmodel_sym.get(i).length; j++) {
					System.out.print(tbmodel_sym.get(i)[j] + "\t");
				}
				System.out.println(" ");
			}
		}
		
		//��ӡ������Ϣ
		public void printTbmodel_wrong_result() {
			System.out.println("������Ϣ: ");
			for(int i = 0; i < tbmodel_wrong_result.size(); i++) {
				for(int j = 0; j < tbmodel_wrong_result.get(i).length; j++) {
					System.out.print(tbmodel_wrong_result.get(i)[j] + "\t");
				}
				System.out.println(" ");
			}
		}
}
