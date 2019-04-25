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
	//语义分析实现
	
		private List<String> proList = new ArrayList<String>(); //接收产生式集合
		
		private List<Production> productions = new ArrayList<Production>(); //产生式新的存储方式
		
		public  List<String[]> token = null;//token序列
		
		private String[][] action = null; //LALR分析表
		
		private List<ProductionTree>  treeList = new ArrayList<ProductionTree>(); //归约项对应的产生式的集合
		
		private int offset = 0; //符号表中的偏移量
		
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
		
		//将产生式集合换种方式存储
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
		
		//语法分析
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
			
			//状态和属性的键值对，0号位置存储状态，1号位置存储type，2号存储width，3号存储val，4号存addr，5号存储偏移量
			//6号存储truelist对应的位置，7号存储falselist对应的位置，补充 6号也可以存储nextlist
			String[] s_p = new String[8]; 
			
			s_p[0] = "0"; //
			stack.push(s_p);
			int i = 1;
			int flag_note = -1;
			String type = null,width = null;
			
			int seq = 0;
			int newTemp = 0;
			//数组引用时，计算数组寻址的深度
			int array_depth = 0; 
			//数组类型
			String arrayType = ""; 
			
			//符号表对应的符号集合
			Set<String> symTable = new HashSet<String>();
			//符号表，0：标识符，1：类型，2：偏移量，3：行号
			Set<String[]> symTableInfo = new HashSet<String[]>();
			
			
			int position = 0;
			//key:标识 -- value:跳转指令，存储符号的truelist、falselist和nextlist
			Map<String,List<String>> listMap = new HashMap<String,List<String>>();
			
			//声明过程时，声明的形参，0号位置存储参数名，1号位置存储参数的类型
			List<String[]> paramSet = null;
			//key:过程名 --value:形参列表 
			Map<String,List<String[]>> processMap = new HashMap<String,List<String[]>>();
			
			//调用过程时，传入的形参列表，0号位置存储参数名，1号位置存储参数的类型
			List<String[]> paramListCall =null;
			
			//中间代码，分析完成后统一打印
			List<String[]> list_34 = new ArrayList<String[]>();
			
			while(true){
				System.out.print("栈内符号: ");
				for(String[] sp :stack){
					System.out.print(sp[0] + " ");
				}
				System.out.println(" ");
				
				int top = Integer.valueOf(stack.peek()[0]);//栈顶，是状态
				//String property = stack.peek()[1];//对应的属性
				int n = -1;
				if(kind != null){
					//System.out.println("kind：" + kind + ", " + a);
					if(kind.equals("NUM")){//是数
						n = GetProduction.SYM_INT.get("num");
						if(error != null){
							System.out.println("进入");
							//tbmodel_wrong_result.addRow(new String[]{error,row_num});
							//System.out.println("line " + row_num + ": " + error);
							tbmodel_wrong_result.add(new String[]{error,row_num});
							error = null;
						}
					}else if(kind.equals("IDN")){//是标识符
						n = GetProduction.SYM_INT.get("id");
					}else if(kind.equals("OP")){
						n = GetProduction.SYM_INT.get(a);
						if(error!=null){
							System.out.println("进入运算符");
							//tbmodel_wrong_result.addRow(new String[]{error,row_num});
							//System.out.println("line " + row_num + ": " + error);
							tbmodel_wrong_result.add(new String[]{error,row_num});
							error = null;
						}
					}else if(kind.equals("NOTE")){
						System.out.println(flag_note+"，"+i);
						if(error!=null && flag_note!=i){
							System.out.println("进入注释");
							tbmodel_wrong_result.add(new String[]{error,row_num});
							//System.out.println("line " + row_num + ": " + error);
							if(i<token.size()){
								tuple = token.get(i);
								a =tuple[0] ;//下一个输入符号
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
								a = "$";//结束
								kind = null;
								System.out.println("读入完成");
							}
							flag_note = i;
							continue;
						}
					}else{
						System.out.println("没有对应的文法符号");
						return;
					}
				}else{
					if(GetProduction.SYM_INT.containsKey(a)){//其他符号
						n = GetProduction.SYM_INT.get(a);
					}
				}
				//System.out.println(n+", "+a);
				if(action[top][n].charAt(0)=='s'){//移进 
					String status = action[top][n].substring(1, action[top][n].length());
					System.out.println("栈顶状态: " + top + ", 遇到输入符号: " + a + ", 得 "
							+ action[top][n] + ", 移进" + a + ", 进入状态: " + status);
					//statusStack.push(a);
					s_p =new String[2];
					s_p[0] = a;
					if(kind!=null && kind.equals("IDN")){
						//移进id时，为id添加type
						s_p[1] = kind;
					}else if(SomeWord.OP.contains(a)){
						s_p[1] = "OP";
					}
					stack.push(s_p);
					
					s_p =new String[8];
					s_p[0] = status;  //状态
					stack.push(s_p);
					
					if(i < token.size()){
						tuple = token.get(i);
						a =tuple[0] ;//下一个输入符号
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
						a = "$";//结束
						kind = null;
						System.out.println("读入完成");
					}
				}else if(action[top][n].charAt(0)=='r'){ //归约
					String status = action[top][n].substring(1, action[top][n].length());
					int m = Integer.valueOf(String.valueOf(status));
					Production  pro = productions.get(m);//获取该产生式
					String right = getProductiuonRight(pro.returnRights()).trim().isEmpty() ? "empty" : getProductiuonRight(pro.returnRights());
					System.out.println("栈顶状态: " + top + ", 遇到输入符号: " + a + ", 得 "
							+ action[top][n] + ", 按   " + pro.returnLeft() + " -> " + right + " 归约");
					int len = 0;
					if(right.equals(SomeWord.EMPTY)){ //按空串归约，不出栈
						len = 0;
					}else{
						String[]  rights = pro.returnRights();//将产生式右部的符号划分出来
						for(int k=0;k<rights.length;k++){
							if(!rights[k].trim().isEmpty())
								len++;
						}
					}
					System.out.println("出栈个数: " + len * 2);
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
					System.out.println("新状态: " + status_new);

					s_p =new String[8];
					
//------*-*------*-*------*-*------*-*------*-*------*-*------*-*------*-*------*-*------*-*------*-*------*-*------*-*------*-*------*-*
					//语义动作
					String[] rights = pro.returnRights();
					String left = pro.returnLeft();
					
					if(left.equals("P")){ //P->empty
						if(rights.length == 1 && rights[0].equals(SomeWord.EMPTY)) {
							Map<Integer,String[]> modify = new HashMap<Integer,String[]>();
							int k1 = 0;
							for(String[] s34:list_34){ //三地址、四元式
								String[] s34_new = new String[4];
								s34_new[0] = s34[0];
								s34_new[2] = s34[2];
								s34_new[3] = s34[3];
								String s3 = s34[1];  //三地址   arg1
								String s4 = s34[2];  //四元式   arg2
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
							//用新生成的三地址和四元式替换原来的
							for(Map.Entry<Integer, String[]> entry:modify.entrySet()){
								int pos = entry.getKey();
								String[] s34_new = entry.getValue();
								list_34.remove(pos);
								list_34.add(pos, s34_new);
							}
						}
					}else if(left.equals("Pa")){  //Pa->empty
						//创建offset，初始化为0
						System.out.println("初始化");
						Map<Integer,String[]> modify = new HashMap<Integer,String[]>();
						int k1=0;
						for(String[] s34:list_34){//三地址、四元式
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
						//用新生成的三地址和四元式替换原来的
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
						    tbmodel_wrong_result.add(new String[]{id + ", 重复声明", row_num});
						}else{
							symTable.add(id);
							String[] s = new String[4];//0：标识符，1：类型，2：偏移量，3：行号
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
						paramSet = new ArrayList<String[]>();//声明过程时，声明的形参
						s_p[1] = "proc";
						String id =getElemByType("IDN");
						tbmodel_sym.add(new String[]{id,s_p[1],String.valueOf(offset),row_num});
					}else if(left.equals("Aa")){//Aa -> empty {gen(id,’=’,F.addr);}
						seq ++;
						String addrF = getElementByName("F")[4];
						String id = getElemByType("IDN");
						if(addrF!=null){//有addr信息
							String s3 = "";
							s3 += id + " = " + addrF;
							String s4 = "( =, ";
							s4 += addrF+ " , -  , "+id+ ")";
							System.out.println("Aa -> empty\t三地址码：" + s3 + ", 四元组：" + s4);
							list_34 =addList34(list_34, seq, new String[]{String.valueOf(seq),s3,s4,row_num});
							seq++;
						}
					}else if(left.equals("Ab")){//Ab -> empty {enter(id.lexeme,T.type,offset); offset=offset+T.width;}
						s_p[1] = type;               
						s_p[2] = width;
						String id = getElemByType("IDN");
						if(symTable.contains(id)){
							tbmodel_wrong_result.add(new String[]{id + ", 重复声明", row_num});
						}else{
							symTable.add(id);
							String[] s = new String[4];//0，标识符  1，类型  2，偏移量  3行号
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
						symTable.add(id); //形参加入到符号表中，在"过程"中可以直接使用
						String[] s = new String[4];//0，标识符  1，类型  2，偏移量  3行号
						s[0] = id;
						s[1] = getElementByName("X")[1] ;
						s[2] = String.valueOf(offset);
						s[3] = row_num;
						symTableInfo.add(s);
						if(paramSet != null){
							paramSet.add(new String[]{id,s[1]}); //参数名和类型
						}else{
							System.out.println("1形参表是空的");
						}
						tbmodel_sym.add(new String[]{id,getElementByName("X")[1],String.valueOf(offset),row_num});
						offset += Integer.valueOf(getElementByName("X")[2]);
					}else if(left.equals("Mb")){//Mb -> empty {enter(id.lexeme,X.type,offset); offset = offset+X.width;}
						String id = getElemByType("IDN");
						symTable.add(id);//形参加入到符号表中，在“过程”中可以直接使用
						String[] s = new String[4];//0，标识符  1，类型  2，偏移量  3行号
						s[0] = id;
						s[1] = getElementByName("X")[1] ;
						s[2] = String.valueOf(offset);
						s[3] = row_num;
						symTableInfo.add(s);
						if(paramSet != null){
							if(paramSet.contains(id)){
								tbmodel_wrong_result.add(new String[]{id + " 形参命名重复", row_num});
							}else{
								paramSet.add(new String[]{id,s[1]}); //参数名和类型
							}
						}else{
							System.out.println("2 形参表是空的");
						}
						tbmodel_sym.add(new String[]{id,getElementByName("X")[1],String.valueOf(offset),row_num});
						offset += Integer.valueOf(getElementByName("X")[2]);
					}else if(left.equals("C")){
						if(rights[0].equals(SomeWord.EMPTY)){//C-> empty {C.type=t; C.width=w;}
							s_p[1] = type;
							s_p[2] = width;
						}else{//C->[ num ] C {C.type=array(num.val,C1.type); C.width=num.val*C.width;}
							if(rights[0].equals("[") && rights[2].equals("]") && rights[3].equals("C")){
								String[] info = popSymInfo.get(0);  //弹出栈的第一个符号，即 C
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
								//从符号表中取id的符号信息
								String id = keys[0];
								if(!symTable.contains(id)){
									s_p[1] = "notdeclare";
									tbmodel_wrong_result.add(new String[]{id + "未声明就引用", row_num});
								}else{
									String[] info = getSymInfoById(id, symTableInfo);
									s_p[1] = info[1]; //type
								}
								if(getElementByName("[") != null){ //有数组
									String idType = getSymInfoById(id, symTableInfo)[1];
									if(idType.indexOf("int") == -1){//考虑数组
										tbmodel_wrong_result.add(new String[]{"数组下标表达式中: " + id + "不是int型变量", row_num});
									}
								}
							}else if(getProductiuonRight(rights).equals("num")){//F->num
								s_p[1] = "number"; //type
								if(getElementByName("[") != null){ //有数组
									String num = String.valueOf(keys[0]);
									if(num.indexOf(".") != -1){ //不是整型数
										if(Character.isDigit(num.charAt(num.indexOf(".")-1))){
											tbmodel_wrong_result.add(new String[]{"数组下标表达式中: " + num + "不是整数", row_num});
										}
									}
								}
							}
							s_p[3] = keys[0];//val
							s_p[4] = keys[0];//addr
						}else{ //F->( E ) {F.type=E.type; F.val=e.VAL; F.addr = E.addr}
							if(rights.length == 3){
								if(rights[0].equals("(") && rights[1].equals("E") && rights[2].equals(")")){
									//填充F的addr信息
									String[] elemE = popSymInfo.get(1);//弹出栈的第二个字符，即E
									s_p[1] = elemE[1]; //type
									s_p[3] = elemE[3];
									s_p[4] = elemE[4];
								}
							}
						}
					}else if(left.equals("E")){
						if(rights.length > 1){
							if(rights[0].equals("G") && rights[1].equals("E'")){
								//E->G E' {E.addr = newtemp(); gen(E.addr ,‘=’ ,E’.addr ,‘+’ ,G.addr); }
								String[] elemE_ = popSymInfo.get(0);
								String[] elemG = popSymInfo.get(1);
								String addrG = elemG[4];
								String addrE_ = elemE_[4];
								s_p[1] = elemG[1]; //type
								if(elemE_[3] == null && elemE_[4] == null){//E'是按empty归约的
									s_p[4] = addrG; //addr
									s_p[3] = elemG[3]; //val
								}else{
									if(elemE_[1].equals("notnumber")){
										s_p[1] = "notnumber";
									}
									//动作：生成临时变量，生成三地址
									String addrE = "t" + String.valueOf(newTemp);
									newTemp++;
									s_p[4] = addrE;
									String s3 = "";
									String s4 = "( ";
									String op = elemE_[2];
									s3 += addrE + " = " + addrG + op + addrE_;
									s4 += op + ", " + addrG + " , " + addrE_ + " , " + addrE + " )";
									System.out.println("E->G E'\t三地址码： " + s3 + ", 四元组: " + s4);
									list_34 = addList34(list_34, seq, new String[]{String.valueOf(seq), s3, s4, row_num});
									seq++;
									s_p[3] = elemG[3] + op + elemE_[3];
								}
							}
						}else if(rights[0].equals("L")){//E->L
							String addrE = "t" + String.valueOf(newTemp);
							String[] infoL  = popSymInfo.get(0);
							String addrL = infoL[4];
							System.out.println("新 value："+infoL[3]);
							System.out.println("addr："+infoL[4]);
							System.out.println("type："+infoL[1]);
							if(infoL[2]!=null){//数组
								//由于文法限制，此处若是数组，则会出现归约-归约冲突
								//等待合适的文法
								String valL = infoL[3].substring(0, infoL[3].indexOf("[")); //id
								newTemp++;
								String s3 = "";
								String s4 = "( =, ";
								s3 += addrE + " = " +valL +"["+addrL+"]";
								s4 +=valL +"["+addrL+"]"+" , -  , "+addrE+ " )";
								System.out.println("E->L   s3："+s3+"，s4："+s4);
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
							if(rights[1].equals("G") && rights[2].equals("E'")){//E'->+ G E'|-G E' {E’.addr = G.addr}
								//算术运算：加 减，优先级低
								String[] elemG = popSymInfo.get(1);;
								s_p[1] = elemG[1];
								if(!(s_p[1].equals("number")
										|| s_p[1].equals("int")||s_p[1].equals("float")||s_p[1].equals("double"))){
									tbmodel_wrong_result.add(new String[]{elemG[3]+"不是操作数",row_num});
									s_p[1] = "notnumber";
								}
								s_p[2] = keys[2]; //用width位置存储运算符
								s_p[3] = elemG[3];
								s_p[4] = elemG[4];
							}
						}
					}else if(left.equals("G")){
						if(rights[0].equals("F") && rights[1].equals("G'")){
							//G-> F G' {G.addr = newtemp(); gen(G.addr, ‘=’, F.addr, ‘+’, G’.addr);}
							String[] elemF = popSymInfo.get(1); //弹出栈时接收的，F
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
											tbmodel_wrong_result.add(new String[]{elemF[3] + "不是整型数", row_num});
										}
									}else{
										tbmodel_wrong_result.add(new String[]{elemF[3] + "不是数值变量", row_num});
									}
									s_p[1] = "notnumber";//指定为一个操作数类型，使分析继续下去
								}else if(infoL[1].equals("float")){
									if(!s_p[1].equals("number")){
										tbmodel_wrong_result.add(new String[]{elemF[3] + "不是浮点型变量", row_num});
									}
									s_p[1] = "notnumber";//指定为一个操作数类型，使分析继续下去
								}else if(infoL[1].equals("char")){
									tbmodel_wrong_result.add(new String[]{elemF[3] + "不是字符型变量", row_num});
									s_p[1] = "notchar";//指定为一个字符类型，使分析继续下去
								}
							}
							if(elemG_[3] == null && elemG_[4] == null){//G'是按empty归约的
								String addrG = addrF;
								s_p[4] = addrG;
								s_p[3] = elemF[3];//val
							}else {
								if(elemG_[3].equals("notnumber")){
									s_p[1] = "notnumber";//指定为一个操作数类型，使分析继续下去
								}else if(elemG_[3].equals("notchar")){
									s_p[1] = "notchar";//指定为一个字符类型，使分析继续下去
								}
								//动作：生成临时变量，生成三地址
								String addrG = "t" + String.valueOf(newTemp);
								newTemp++;
								s_p[4] = addrG;
								String s3 = "";
								String s4 = "( ";
								String op = elemG_[2];
								s3 += addrG + " = " + addrF + op + addrG_;
								s4 += op + ", " + addrF + " , " + addrG_ + " , " + addrG + " )";
								System.out.println("G-> F G'\t三地址码: " + s3 + ", 四元组: " + s4);
								list_34 =addList34(list_34, seq, new String[]{String.valueOf(seq),s3,s4,row_num});
								seq++;
								s_p[3] = elemF[3] + op + elemG_[3];//val
							}
						}
					}else if(left.equals("G'")) {
						if(rights.length>1) {
							if(rights[1].equals("F") && rights[2].equals("G'")){//G'->* F G' {G’.addr = F.addr }
								//算术运算：乘 除，优先级高
								String[] elemF = popSymInfo.get(1);
								s_p[1] = elemF[1];
								if(!(s_p[1].equals("number")
										|| s_p[1].equals("int")||s_p[1].equals("float")||s_p[1].equals("double"))){
									tbmodel_wrong_result.add(new String[]{elemF[3]+"不是操作数",row_num});
									s_p[1] = "notnumber";//指定为一个操作数类型，使分析继续下去
								}
								s_p[2] = keys[2]; //用width位置存储运算符
								s_p[3] = elemF[3];//val
								s_p[4] = elemF[4];//addr
							}
						}
					}else if(left.equals("S")) {
						if(rights.length > 1){
							if(rights[0].equals("L")){//S->L = E ; {gen(L.addr,‘=’,E.addr); S.nextlist=nil;}
								if(rights[1].equals("=") && rights[2].equals("E") && rights[3].equals(";")){
									String typeE = popSymInfo.get(1)[1];
									String typeL = popSymInfo.get(3)[1]; 
									//计算行号
									for(int k1 = i - 1; k1 >= 0; k1--){
										String[] return_tuple = token.get(k1);
										if(return_tuple[0].equals("=")){
											row_num = return_tuple[2];
											break;
										}
									}
									//判断等式左右类型是否一致
									if(!typeE.equals(typeL)){
										if(typeL.equals("number")){
											if(!(typeE.equals("int") || typeE.equals("float") || typeE.equals("double"))){
												tbmodel_wrong_result.add(new String[]{"等式两边的变量类型不一致",row_num});
											}
										}
									}
									String s3="",s4="( =,";
									String addrL = popSymInfo.get(3)[4];
									String addrE = popSymInfo.get(1)[4];
									s3 += addrL + " = " + addrE;
									s4 += addrE + " , - , " + addrL + " )";
									System.out.println("S->L = E ;\t三地址码: " + s3 + ", 四元组: " + s4);
									list_34 =addList34(list_34, seq, new String[]{String.valueOf(seq),s3,s4,row_num});
									seq++;
									
									//设置netxlist，初始为空。到while时填充
									List<String> nextlistS = new ArrayList<String>();
									s_p[6] = String.valueOf(position++);
									listMap.put(s_p[6],nextlistS);
								}
							}else if(rights[0].equals("return")){//S->return E ;
								String[] info = popSymInfo.get(1);
								String addrE = info[4];
								String type_return = info[1];
								String type_proc = getElementByName2("proc")[1];
								//计算行号
								for(int k1 = i - 1; k1 >= 0; k1--){
									String[] return_tuple = token.get(k1);
									if(return_tuple[0].equals("return")){
										row_num = return_tuple[2];
										break;
									}
								}
								//判断返回值是否为数组类型
								if(type_return.length() > 2 &&
										type_return.charAt(0) == 'a' && type_return.charAt(1) == 'r'){//数组类型
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
								//判断返回值类型和过程的类型是否一致
								if(!type_return.equals(type_proc)){
									if(type_return.equals("number")){
										if(!(type_proc.equals("int")||type_proc.equals("float")||type_proc.equals("double"))){
											tbmodel_wrong_result.add(new String[]{"返回值类型: " + type_return + ", 与过程的类型: " + type_proc + "不匹配", row_num});
										}
									}else{
										tbmodel_wrong_result.add(new String[]{"返回值类型: " + type_return + ", 与过程的类型: " + type_proc + "不匹配", row_num});
									}
								}
								String s3 = "return " + addrE;
								String s4 = "(return , - , -, " + addrE + ")";
								System.out.println("S->return E ;\t三地址码: " + s3 + ", 四元组: ");
								list_34 = addList34(list_34, seq, new String[]{String.valueOf(seq), s3, s4, row_num});
								seq++;
							}else if(rights[0].equals("if")){//S->if B then Sa S Sb else Sc S 
								//{backpatch(B.truelist=Sa.quad); backpatch(B.falselist=Sc.quad); S.nextlist=merge(merge(S1.nextlist,Sb.nextlist),S2.nextlist);}
								//回填
								String[] infoB = popSymInfo.get(7);
								String[] infoSa = popSymInfo.get(5);
								String[] infoSc = popSymInfo.get(1);
								String quadSa = infoSa[3];
								String quadSc = infoSc[3];
								List<String> truelistB = listMap.get(infoB[6]);
								List<String> falselistB = listMap.get(infoB[7]);
								//回填 B.truelist
								Map<Integer,String[]> modify = new HashMap<Integer,String[]>();
								for(String s : truelistB){
									int k1 = 0;
									for(String[] s34:list_34){//三地址、四元式
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
									//替换
									for(Map.Entry<Integer, String[]> entry:modify.entrySet()){
										int pos = entry.getKey();
										String[] s34_new = entry.getValue();
										list_34.remove(pos);
										list_34.add(pos, s34_new);
									}
								}
								//回填 B.falselist
								modify = new HashMap<Integer,String[]>();
								for(String s : falselistB){
									int k1=0;
									for(String[] s34:list_34){//三地址、四元式
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
									//替换
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
								//{backpatch(S1.nextlist,Sd.quad); backpatch(B.truelist,Se.quad); S.nextlist=B.falselist; gen(‘goto’,Sd.quad); }
								//回填
								String[] infoS1 = popSymInfo.get(0);
								String[] infoB = popSymInfo.get(3);
								String[] infoSd = popSymInfo.get(4);
								String[] infoSe = popSymInfo.get(1);
								String quadSd = infoSd[3];
								String quadSe = infoSe[3];
								
								//回填 S1.nextlist
								List<String> nextlistS1 = listMap.get(infoS1[6]);
								Map<Integer,String[]> modify = new HashMap<Integer,String[]>();
								for(String s : nextlistS1){
									int k1 = 0;
									for(String[] s34 : list_34){//三地址、四元式
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
									//替换
									for(Map.Entry<Integer, String[]> entry:modify.entrySet()){
										int pos = entry.getKey();
										String[] s34_new = entry.getValue();
										list_34.remove(pos);
										list_34.add(pos, s34_new);
									}
								}
								
								//回填 B.truelist
								List<String>truelistB = listMap.get(infoB[6]);
								modify = new HashMap<Integer,String[]>();
								for(String s : truelistB){
									int k1 = 0;
									for(String[] s34 : list_34){//三地址、四元式
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
									//替换
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
								
								//跳回布尔表达式判断
								String s3 = "goto " + quadSd;
								String s4 = "(j , - ,  -  , " + quadSd + " )";
								list_34 =addList34(list_34, seq, new String[]{String.valueOf(seq), s3, s4, row_num});
								seq++;
							}else if(rights[0].equals("call")){//S->call id ( Elist ) ;
								//{n=0; for q中的每个t do{gen(‘param’,t);  n=n+1;}; gen(‘call’,id.addr,‘,’,n);}
								if(rights[1].equals("id") && rights[2].equals("(") && rights[3].equals("Elist")){
									if(paramListCall == null){
										System.out.println("调用出错");
									}else{
										//添加错误判断，参数个数
										String id = keys[4];
										List<String[]> paramListProc = new ArrayList<String[]>();
										paramListProc = processMap.get(id);
										if(paramListProc == null){
											tbmodel_wrong_result.add(new String[]{"对非过程变量: " + id + "\t使用过程调用符号call", row_num});
										}else{
											if(paramListProc.size()!=paramListCall.size()){
												tbmodel_wrong_result.add(new String[]{"传输参数个数: " + paramListCall.size() + "\t与声明的过程不匹配", row_num});
											}else{
												String paramCall = null, paramCallType = null;
												List<String[]> paramListCallNew = new ArrayList<String[]>();
												//存的参数序列是倒序的，所以倒序打印
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
															tbmodel_wrong_result.add(new String[]{"传入参数: " + paramCall + ", 类型: " + paramCallType +
																	", 与声明的过程的参数类型: " + paramProcType + "不匹配", row_num});
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
									if(infoL_[4] == null){//按空串L'->empty 归约的，不是数组
										String[] infoId = getSymInfoById(id, symTableInfo);
										s_p[1] = infoId[1];//type
										s_p[3] = id;
										s_p[4] = id;
									}else{
										String addrL_ = infoL_[4];
										s_p[1] = infoL_[1];//数组的type
										s_p[2] = infoL_[2];//width
										s_p[3] = id+infoL_[3];//val
										s_p[4] =addrL_;
										if(a.equals("=")){//等号左边引用数组，对数组进行赋值
											String offsetL = "t" + String.valueOf(newTemp);
											newTemp++;
											s_p[4] =  offsetL;//addr
											String s3 = "";
											String s4 = "( =, ";
											s3 += offsetL + " = " + id + "["+addrL_+"]";
											s4 += id + "[" + addrL_ + "]" + " , -  , " + offsetL + " )";
											System.out.println("L->id L'\t三地址码: "+s3+", 四元组: "+s4);
											list_34 =addList34(list_34, seq, new String[]{String.valueOf(seq), s3, s4, row_num});
											seq++;
										}	
									}
								}else{
									s_p[1] = "notdeclare";
									tbmodel_wrong_result.add(new String[]{id + "未声明就引用", row_num});
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
									//L' 按空串归约，且不是数组类型
								}else{
									//数组类型
									String temp = "t" + String.valueOf(newTemp);
									newTemp++;
									String s3 = "";
									String s4 = "( *, ";
									s3 += temp + " = " +num +"*"+widthL_1;
									s4 += num+" , "+widthL_1+" , "+temp+ " )";
									System.out.println("1  L'->[ E ] L'\t三地址码: "+s3+", 四元式: "+s4);
									list_34 =addList34(list_34, seq, new String[]{String.valueOf(seq),s3,s4,row_num});
									seq++;
									
									String offsetL_ = "t" + String.valueOf(newTemp);
									newTemp++;
									s3 = "";
									s4 = "( +, ";
									s3 += offsetL_ + " = " +addrL_1 +"+"+temp;
									s4 += addrL_1+" , "+temp+" , "+offsetL_+ " )";
									System.out.println("2  L'->[ num ] L'\t三地址码: "+s3+", 四元式: "+s4);
									list_34 =addList34(list_34, seq, new String[]{String.valueOf(seq),s3,s4,row_num});
									seq++;
									
									int array_len = Integer.valueOf(infoL_1[2].substring(infoL_1[2].indexOf(",")+1,  infoL_1[2].length()));
									s_p[2] = String.valueOf(array_len*Integer.valueOf(widthL_1));
									String array_len_next = getArrayLen(array_depth,arrayType);
									array_depth++;
									s_p[2] += ","+array_len_next;
									s_p[3] = keys[3]+num+keys[1]+valL_1;//val，累加
									s_p[4] = offsetL_;//addr，也就是offset
								}
							}else{
								System.out.println("变量未声明");
							}
						}else{ //L'->empty
							String id = getElemByType("IDN");
							if(id!=null){
								if(!symTable.contains(id)){
									s_p[1] = "notdeclare";
									tbmodel_wrong_result.add(new String[]{id+"未声明就引用",row_num});
								}else{
									String[] info = getSymInfoById(id, symTableInfo);
									s_p[1] = info[1]; //type
									if(s_p[1].length() > 2 && s_p[1].charAt(0) == 'a' && s_p[1].charAt(1) == 'r'){//数组类型
										arrayType = info[1];//记录数组的类型
										array_depth = 0;//深度
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
										s_p[4]= info[2];//offset，base
									}else{
										if(getElementByName("]")!=null){
											tbmodel_wrong_result.add(new String[]{"对非数组类型变量"+id+"使用数组访问符",row_num});
										}
										System.out.println("===不是数组类型===");
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
							System.out.println("I->true\t三地址码: " + s3 + ", 四元式: "+s4);
							list_34 = addList34(list_34, seq, new String[]{String.valueOf(seq++),s3,s4,row_num});
						}else if(rights[0].equals("false")){
							//falselist
							s_p[7] = String.valueOf(position++);
							List<String> list = new ArrayList<String>();
							list.add(String.valueOf(seq));
							listMap.put(s_p[7], list);
							String s3 = "goto _";
							String s4 = "(j , - ,  -  , _ )";
							System.out.println("I->false\t三地址码: "+s3+", 四元式: "+s4);
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
										tbmodel_wrong_result.add(new String[]{"布尔表达式 "+addrE1+" "+logic_op+" "+addrE2+" 错误，不能比较",row_num});
									}
								}else if(typeE2.equals("number")){
									if(!(typeE1.equals("int")||typeE1.equals("float")||typeE1.equals("double"))){
										tbmodel_wrong_result.add(new String[]{"布尔表达式 "+addrE1+" "+logic_op+" "+addrE2+" 错误，不能比较",row_num});
									}
								}else{
									tbmodel_wrong_result.add(new String[]{"布尔表达式 "+addrE1+" "+logic_op+" "+addrE2+" 错误，不能比较",row_num});
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
							System.out.println("序号: "+(seq)+", 1  I->E Relop E\t三地址码: " + s3 + ", 四元组: " + s4);
							list_34 = addList34(list_34, seq, new String[]{String.valueOf(seq++), s3 ,s4, row_num});
							
							s3 = "goto _";
							s4 = "(j , - ,  -  , _ )";
							System.out.println("序号: "+(seq)+", 2  I->E Relop E\t三地址码: " + s3 + ", 四元组 : " + s4);
							list_34 = addList34(list_34, seq, new String[]{String.valueOf(seq++), s3, s4, row_num});
						}
					}else if(left.equals("B")){//B->H B'
						String[] infoB_ = popSymInfo.get(0);
						if(infoB_[6]==null || infoB_[7]==null){//按empty归约
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
							//回填
							Map<Integer,String[]> modify = new HashMap<Integer,String[]>();
							for(String s:falselistH1){
								int k1=0;
								for(String[] s34:list_34){//三地址、四元式
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
								//替换
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
						if(infoH_[6]==null || infoH_[7]==null){//按空串归约
							String[] infoi = popSymInfo.get(1);
							s_p[6] = infoi[6];
							s_p[7] = infoi[7];
						}else{
							s_p[6] = infoH_[6];
							s_p[7] = infoH_[7];
						}
					}else if(left.equals("H'")){//H'-> and Bmb I H'
						if(rights.length>1&&rights[0].equals("and")){
							//回填
							String[] infoi1 = getElementByName("I");
							String[] infoi2 = popSymInfo.get(1);
							String[] infoBmb = popSymInfo.get(2);
							String quad = infoBmb[3];
							List<String> truelisti1 = listMap.get(infoi1[6]);
							Map<Integer,String[]> modify = new HashMap<Integer,String[]>();
							for(String s:truelisti1){
								int k1=0;
								for(String[] s34:list_34){//三地址、四元式
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
					}else if(left.equals("Sb")){//Sb->empty {Sb.nextlist = makelist(nextquad) ; gen(‘goto _’);}
						List<String> nextlist = new ArrayList<String>();
						s_p[6] = String.valueOf(position++);
						nextlist.add(String.valueOf(seq));
						listMap.put(s_p[6], nextlist);
						//gen
						String s3 = "goto _";
						String s4 = "(j , - ,  -  , _ )";
						System.out.println("序号: " + seq + ", Sb->empty\t三地址码: " + s3 + ", 四元组: " + s4);
						list_34 = addList34(list_34, seq, new String[]{String.valueOf(seq++),s3,s4,row_num});
					}else if(left.equals("D")){//D->proc X id Dc ( M ) { P }
						if(rights.length==10 && rights[0].equals("proc")){
							String id = keys[7];
							processMap.put(id, paramSet);//过程名和其参数
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
								parmInfo[0] = addrE;		//参数名
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
								//初始化为只包含E.addr
								String[] infoE = popSymInfo.get(1);
								String[] paramInfo = new String[2];
								paramInfo[0] = infoE[4];
								paramInfo[1] = infoE[1];
								paramListCall = new ArrayList<String[]>();
								paramListCall.add(paramInfo);
								s_p[1] = "not null"; //表示Elist'不是按空归约的
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
					System.out.println("栈顶状态" + top + ", 遇到输入符号: " + a + "，接受. 分析完成");
					break;
				}else {//调用错误处理程序
					System.out.println("遇到符号: " + a + ", 错误！" + n + ", 栈顶状态: " + top);
					System.out.print(GetProduction.INT_SYM.get(n) + ": ");
					System.out.println(action[top][n]);
					i = i - 2; //回退一个符号, i为当前token序号
					if(i < 0){
						System.out.println("请检查输入的语句！");
						tbmodel_wrong_result.add(new String[]{"多余的符号：" + a, row_num});
						i = i + 2;
						if(i < token.size()){
							tuple = token.get(i);
							a =tuple[0] ;//下一个输入符号
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
							a = "$";//结束
							kind = null;
							System.out.println("读入完成");
						}
					}else {
						String sym_error_before = token.get(i)[0];
						int ret = errorHandler(top,sym_error_before,a,i);
						System.out.println("ret是: "+ret);
						if(ret!=0){
							 i =ret;
							 System.out.println("当前i是: "+i);
							 if(i<token.size()){
								 System.out.println("当前i是: "+i+", 符号: "+token.get(i)[0]);
							 }
						}else{
							i = i + 1;
						}
						if(i < token.size()){
							tuple = token.get(i);
							a = tuple[0] ;//下一个输入符号
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
							a = "$";//结束
							kind = null;
							System.out.println("读入完成");
						}
					}
					
				}
			}
		}
		
		private int hope_seq = 0;
		//错误处理
		public int errorHandler(int top,String sym_error_before,String sym_error,int position){
			//0号存列名，1号存动作or跳转值
			System.out.println("出错前的符号："+sym_error_before);
			System.out.println("引起出错的符号："+sym_error);
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
			//可能的动作or跳转的值
			for(String s:action_hope_name){
				System.out.println("希望遇到的符号："+s);
			}
			boolean isDolloar = false;
			int position_new = 0;
			if(!sym_error.equals("$")){//不是最终的符号
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
							System.out.println("是IDN");
							s = "id";
						}
					}else{
						s = next_token[0];
					}
					System.out.println("符号："+next_token[0]+"，top="+top);
					if(!action_hope_name.contains(s)){
						no_use.add(new String[]{token.get(i)[0],token.get(i)[2]});
						continue;
					}
					break;
				}
				if(i < token.size()){
					for(String[] s:no_use){
						tbmodel_wrong_result.add(new String[]{"多余的符号："+s[0],s[1]});
						//System.out.println("1多余的符号："+s[0]);
					}
					position_new = i;
					return position_new;
				}
			}else{
				//引起错误的符号是$
			}
			System.out.println("是$ 符号，" + position_new);
			
			if(sym_error_before.equals(";")){
				System.out.println("清除多余符号：" + sym_error);
				if(!sym_error.equals("$")){
					tbmodel_wrong_result.add(new String[]{"多余的符号："+sym_error,token.get(position)[2]});
				}
				token.remove(position);
				return position_new;
			}
			if(sym_error_before.equals("]")){
				if(!isHaveSymInStackByName("[")){
					System.out.println("清除多余符号：" + sym_error);
					if(!sym_error.equals("$")){
						tbmodel_wrong_result.add(new String[]{"多余的符号："+sym_error,token.get(position)[2]});
					}
					token.remove(position);
					return position_new;
				}
			}
			if(sym_error_before.equals(")")){
				if(!isHaveSymInStackByName("(")){
					System.out.println("清除多余符号：" + sym_error);
					if(!sym_error.equals("$")){
						tbmodel_wrong_result.add(new String[]{"多余的符号："+sym_error,token.get(position)[2]});
					}
					token.remove(position);
					return position_new;
				}
			}
			if(sym_error_before.equals("}")){
				if(!isHaveSymInStackByName("{")){
					System.out.println("清除多余符号：" + sym_error);
					if(!sym_error.equals("$")){
						tbmodel_wrong_result.add(new String[]{"多余的符号："+sym_error,token.get(position)[2]});
					}
					token.remove(position);
					return position_new;
				}
			}
			
			if(action_hope_name.contains(")")){
				System.out.println("有括号");
				boolean flag1 =isHaveSymInStackByName("(");
				if(flag1){
					System.out.println("补全 ) ");
					tbmodel_wrong_result.add(new String[]{"缺少右括号  )",token.get(position)[2]});
					hope[0] = ")";
					hope[2] = token.get(position)[2];
					token.add(position + 1, hope);
					flag = true;
					return position_new;
				}
			}
			if(action_hope_name.contains("]")){
				System.out.println("有方括号");
				boolean flag1 =isHaveSymInStackByName("[");
				if(flag1){
					System.out.println("补全 ] ");
					tbmodel_wrong_result.add(new String[]{"缺少符号  ]", token.get(position)[2]});
					hope[0] = "]";
					hope[2] = token.get(position)[2];
					token.add(position+1, hope);
					flag = true;
					return position_new;
				}
			}
			if(action_hope_name.contains("}")){
				System.out.println("有大括号");
				boolean flag1 =isHaveSymInStackByName("{");
				if(flag1){
					System.out.println("补全 } ");
					tbmodel_wrong_result.add(new String[]{"缺少符号  }",token.get(position)[2]});
					hope[0] = "}";
					hope[2] = token.get(position)[2];
					token.add(position+1, hope);
					flag = true;
					return position_new;
				}
			}
			
		    if(action_hope_name.contains(";")){
				//缺少分号
				System.out.println("========缺分号=========");
				tbmodel_wrong_result.add(new String[]{"缺少分号",token.get(position)[2]});
				hope[0] = ";";
				hope[2] = token.get(position)[2];
				token.add(position+1, hope);
				flag = true;
				return position_new;
			}else if(action_hope_name.contains("id")){
				System.out.println("期望id");
				if(sym_error.equals("$")){
					System.out.println("========缺id=========");
					tbmodel_wrong_result.add(new String[]{"缺少id",token.get(position)[2]});
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
					//运算符出错,期望运算分量,填补id
					System.out.println("========缺运算对象=========");
					tbmodel_wrong_result.add(new String[]{"缺少算对象",token.get(position)[2]});
					hope[0] = "<hope_id"+(hope_seq++)+">";
					hope[1] = "IDN";
					hope[2] = token.get(position)[2];
					token.add(position+1, hope);
					flag = true;
					return position_new;
				}
				if(sym_error_before.equals("=") || sym_error_before.equals(",")){
					//等号右部出错,期望遇到变量或表达式或左括号,填补id
					System.out.println("========缺运变量=========");
					tbmodel_wrong_result.add(new String[]{"缺少变量",token.get(position)[2]});
					hope[0] = "<hope_id"+(hope_seq++)+">";
					hope[1] = "IDN";
					hope[2] = token.get(position)[2];
					token.add(position+1, hope);
					flag = true;
					return position_new;
				}
			}else if(action_hope_name.contains("=")){
				System.out.println("========缺少等号=========");
				tbmodel_wrong_result.add(new String[]{"缺少等号",token.get(position)[2]});
				hope[0] = "=";
				hope[2] = token.get(position)[2];
				token.add(position+1, hope);
				flag = true;
				return position_new;
			}else if(action_hope_name.contains("num")){
				System.out.println("========缺num=========");
				tbmodel_wrong_result.add(new String[]{"缺少数字",token.get(position)[2]});
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
			        System.out.println("随机取得的数："+s);
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
		
		//栈中是否含有某个符号
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
		
		//将中间代码字符串加到list中，最后统一打印
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

		//将两个list合并到一起
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

		//根据深度，获取指定深度，数组的length
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
				System.out.println("读取完成 或者 出错，不是数组");
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

		//根据标识符名，在符号表取出其信息
		private String[] getSymInfoById(String id,Set<String[]> symTableInfo) {
			for(String[] s:symTableInfo){
				if(s[0].equals(id)){
					return s;
				}
			}
			return null;
		}
		
		
		//根据名字，获取栈中靠近栈顶的元素对应的状态
		public String[] getElementByName(String name){
			if(stack.isEmpty()){
				System.out.println("栈为空");
			}else{
				Enumeration<String[]> items = stack.elements();
				String[] s = null, s1 = null;
				//System.out.println("符号："+name);
				while(items.hasMoreElements()){
					s = items.nextElement();
					//System.out.println("元素："+s[0]);
					if(s[0].equals(name)){
						s1 = s;
						s1 = items.nextElement();//取符号对应的状态
						//System.out.println("1 元素："+s1[0]);
					}
				}
				return s1;
			}
			return null;
		}
		
		//根据名字，获取栈中靠近栈【底 】的符号对应的后一个符号的状态
		public String[] getElementByName2(String name){
			if(stack.isEmpty()){
				System.out.println("栈为空");
			}else{
				Enumeration<String[]> items = stack.elements();
				String[] s = null,s1=null;
				while(items.hasMoreElements()){
					s = items.nextElement();
					System.out.println("元素："+s[0]);
					if(s[0].equals(name)){
						s1 = items.nextElement();
						s1 = items.nextElement();
						s1 = items.nextElement();
						System.out.println("1 元素："+s1[0]);
					}
				}
				return s1;
			}
			return null;
		}
		
		//取栈中，靠近栈顶的id
		public String getElemByType(String type){
			if(stack.isEmpty()){
				System.out.println("栈为空");
				return null;
			}else{
				Enumeration<String[]> items = stack.elements();
				String[] s = null;
				String elem = null;
				while(items.hasMoreElements()){
					s = items.nextElement();
					if(s.length > 1){
						if(s[1] != null && s[1].equals(type)){
							//System.out.println("1：元素："+s[0]+"，"+s[1]);
							elem = s[0];
						}
					}
				}
				return elem;
			}
		}
		
		//将产生式右部转化为字符串
		public String getProductiuonRight(String[] rights){
			String right = "";
			if(rights!=null){
				for(int i = 0;i<rights.length;i++){
					if(!rights[i].equals(SomeWord.EMPTY)){
						right += rights[i] + " "; //加一个空格，隔开
					}
				}
				right = right.trim();
			}else{
				right = null;
			}
			return right;
		}
		
		public void print3Code() {  //打印三地址码
			int i = 0;
			System.out.println("\n------*-*---三地址码---*-*------");
			for(i = 0; i < tbmodel_34.size(); i++){
				System.out.println(tbmodel_34.get(i)[0] + "\t" + tbmodel_34.get(i)[1]);
			}
		}
		
		public void print4Code() {  //打印四元式
			int i = 0;
			System.out.println("\n------*-*---四元组---*-*------");
			for(i = 0; i < tbmodel_34.size(); i++){
				System.out.println(tbmodel_34.get(i)[0] + "\t" + tbmodel_34.get(i)[2]);
			}
		}
		
		//打印符号表
		public void printTbmodel_sym() {
			System.out.println("符号表: ");
			System.out.println("IDN\tTYPE\tOFFSET\tLINE");
			for(int i = 0; i < tbmodel_sym.size(); i++) {
				for(int j = 0; j < tbmodel_sym.get(i).length; j++) {
					System.out.print(tbmodel_sym.get(i)[j] + "\t");
				}
				System.out.println(" ");
			}
		}
		
		//打印错误信息
		public void printTbmodel_wrong_result() {
			System.out.println("错误信息: ");
			for(int i = 0; i < tbmodel_wrong_result.size(); i++) {
				for(int j = 0; j < tbmodel_wrong_result.get(i).length; j++) {
					System.out.print(tbmodel_wrong_result.get(i)[j] + "\t");
				}
				System.out.println(" ");
			}
		}
}
