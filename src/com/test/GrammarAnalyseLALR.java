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
	//语法分析实现
	
	private List<String> proList = new ArrayList<String>(); //接收产生式集合
	
	private List<Production> productions = new ArrayList<Production>(); //产生式新的存储方式
	
	public  List<String[]> token = null;//token序列
	
	private String[][] action = null; //SLR分析表
	
	private List<String[]> tbmodel_wrong_result = new ArrayList<String[]>();
	
	private int treePositionWhole; //实时更新的，查找范围。（从后向前）
	
	private List<ProductionTree>  treeList = new ArrayList<ProductionTree>(); //归约项对应的产生式的集合
	
	public GrammarAnalyseLALR(List<String[]> token,List<String> proList,String[][] action) {
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
	
	private int seq = 0;
	private Stack<String> symStack = new Stack<String>();
	//语法分析
	public void analyse(){
		ProductionTree ptree;
		String[] end = new String[3];
		end[0]="$";
		end[2] = token.get(token.size()-1)[2];
		token.add(end);
		System.out.println("token序列：");
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
			System.out.println("（栈底->栈顶）栈：");
			for(String s:symStack){
				System.out.println(s);
			}
			System.out.println();
			int top = Integer.valueOf(symStack.peek());//栈顶，是状态
			int n = -1;
			if(kind!=null){
				System.out.println("kind："+kind+"，"+a);
				if(kind.equals("NUM")){//是数
					n = GetProduction.SYM_INT.get("num");
					if(error!=null){
						System.out.println("进入");
						tbmodel_wrong_result.add(new String[]{error,row_num});
						error = null;
					}
				}else if(kind.equals("IDN")){//是标识符
					n = GetProduction.SYM_INT.get("id");
				}
				else if(kind.equals("OP")){
					n = GetProduction.SYM_INT.get(a);
					if(error!=null){
						System.out.println("进入运算符");
						tbmodel_wrong_result.add(new String[]{error,row_num});
						error = null;
					}
				}
				else if(kind.equals("NOTE")){
					System.out.println(flag_note+"，"+i);
					if(error!=null && flag_note!=i){
						System.out.println("进入注释");
						tbmodel_wrong_result.add(new String[]{error,row_num});
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
				}
				else{
					System.out.println("没有对应的文法符号");
					return;
				}
			}else{
				if(GetProduction.SYM_INT.containsKey(a)){//其他符号
					n = GetProduction.SYM_INT.get(a);
				}
			}
			System.out.println(n+"，"+a);
			if(action[top][n].charAt(0)=='s'){
				String status = action[top][n].substring(1, action[top][n].length());
				System.out.println("栈顶状态"+top+"，遇到输入符号："+a+"，得"
						+action[top][n]+"，移进"+a+"，进入状态："+status);
				symStack.push(a);
				symStack.push(status);
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
			}
			else if(action[top][n].charAt(0)=='r'){
				String status = action[top][n].substring(1, action[top][n].length());
				int m = Integer.valueOf(String.valueOf(status));
				Production  pro = productions.get(m);//获取该产生式
				String right = getProductiuonRight(pro.returnRights()).trim().isEmpty() ? "empty" : getProductiuonRight(pro.returnRights());
				System.out.println("栈顶状态"+top+"，遇到输入符号："+a+"，得"
						+action[top][n]+"，按"+pro.returnLeft()+"->"+right+"归约");
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
				System.out.println("出栈个数："+len*2);
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
				System.out.println("1 新状态："+action[Integer.valueOf(t)][GetProduction.SYM_INT.get(symLeft)]);
				symStack.push(status_new);
			}
			else if(action[top][n].equals("acc")){
				System.out.println("栈顶状态"+top+"，遇到输入符号："+a+"，接受。分析完成");
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
			else{//调用错误处理程序
				System.out.println("遇到符号："+a+"，错误！"+n+"，栈顶状态："+top);
				System.out.println(GetProduction.INT_SYM.get(n));
				System.out.println(action[top][n]);
				//break;
				i = i-2; //回退一个符号
				if(i<0){
					System.out.println("请检查输入的语句！");
					tbmodel_wrong_result.add(new String[]{"多余的符号："+a,row_num});
					i = i+2;
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
					
				//break;
				}else{
					String sym_error_before = token.get(i)[0];
					int ret = errorHandler(top,sym_error_before,a,i);
					System.out.println("ret是："+ret);
					if(ret!=0){
						 i =ret;
						 System.out.println("当前i是："+i);
						 if(i<token.size()){
							 System.out.println("当前i是："+i+"，符号："+token.get(i)[0]);
						 }
					}else{
						i = i+1;
					}
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
				}
			}
		}
	}
	
	public int errorHandler(int top, String sym_error_before, String sym_error, int position){
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
			if(i<token.size()){
				for(String[] s:no_use){
					tbmodel_wrong_result.add(new String[]{"多余的符号："+s[0],s[1]});
					System.out.println("1多余的符号："+s[0]);
				}
				position_new = i;
				return position_new;
			}
		}
		else{
			//引起错误的符号是$
		}
		System.out.println("是$ 符号，"+position_new);
		
		if(sym_error_before.equals(";")){
			System.out.println("清除多余符号："+sym_error);
			if(!sym_error.equals("$")){
				tbmodel_wrong_result.add(new String[]{"多余的符号："+sym_error,token.get(position)[2]});
			}
			token.remove(position);
			return position_new;
		}
		if(sym_error_before.equals("]")){
			if(!isHaveSymInStackByName("[")){
				System.out.println("清除多余符号："+sym_error);
				if(!sym_error.equals("$")){
					tbmodel_wrong_result.add(new String[]{"多余的符号："+sym_error,token.get(position)[2]});
				}
				token.remove(position);
				return position_new;
			}
		}
		if(sym_error_before.equals(")")){
			if(!isHaveSymInStackByName("(")){
				System.out.println("清除多余符号："+sym_error);
				if(!sym_error.equals("$")){
					tbmodel_wrong_result.add(new String[]{"多余的符号："+sym_error,token.get(position)[2]});
				}
				token.remove(position);
				return position_new;
			}
		}
		if(sym_error_before.equals("}")){
			if(!isHaveSymInStackByName("{")){
				System.out.println("清除多余符号："+sym_error);
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
				token.add(position+1, hope);
				flag = true;
				return position_new;
			}
		}
		if(action_hope_name.contains("]")){
			System.out.println("有方括号");
			boolean flag1 =isHaveSymInStackByName("[");
			if(flag1){
				System.out.println("补全 ] ");
				tbmodel_wrong_result.add(new String[]{"缺少符号  ]",token.get(position)[2]});
				hope[0] = "]";
				hope[2] = token.get(position)[2];
				token.add(position+1, hope);
				flag = true;
				return position_new;
			}
		}
		if(action_hope_name.contains("}")){
			System.out.println("有方括号");
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
				//运算符出错
				//期望运算分量
				//填补id
				System.out.println("========缺运算对象=========");
				tbmodel_wrong_result.add(new String[]{"缺少算对象",token.get(position)[2]});
				hope[0] = "<hope_id"+(seq++)+">";
				hope[1] = "IDN";
				hope[2] = token.get(position)[2];
				token.add(position+1, hope);
				flag = true;
				return position_new;
			}
			if(sym_error_before.equals("=") || sym_error_before.equals(",")){
				//等号右部出错
				//期望遇到变量或表达式或左括号
				//填补id
				System.out.println("========缺运变量=========");
				tbmodel_wrong_result.add(new String[]{"缺少变量",token.get(position)[2]});
				hope[0] = "<hope_id"+(seq++)+">";
				hope[1] = "IDN";
				hope[2] = token.get(position)[2];
				token.add(position+1, hope);
				flag = true;
				return position_new;
			}
		}
		else if(action_hope_name.contains("=")){
			System.out.println("========缺少等号=========");
			tbmodel_wrong_result.add(new String[]{"缺少等号",token.get(position)[2]});
			hope[0] = "=";
			hope[2] = token.get(position)[2];
			token.add(position+1, hope);
			flag = true;
			return position_new;
		}
		else if(action_hope_name.contains("num")){
			System.out.println("========缺num=========");
			tbmodel_wrong_result.add(new String[]{"缺少数字",token.get(position)[2]});
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
	
	//打印语法分析树
	/**
	 * @param space_num，当前打印的字符，应该在前面加上多少个'\t'
	 * @param cur，当前字符（产生式右部的某个字符），为了寻找后继
	 * @param treePosition，在treeList中的，查找范围（从后向前）
	 * @param fatherPosition，当前查找的后继，其父亲产生式（递归“起点”）在treeList的位置，
	 * @return
	 */
	public String printTreeRecur(int space_num,String cur,int treePosition,int fatherPosition){
		if(Character.isUpperCase(cur.charAt(0))){//非终结符，继续找
			if(treePosition-1>=0){
				String node = "";
				String leftNext  = "";
				int proPosition;
				int treePositionNext = -1;
				for(int i = treePosition-1;i>=0;i--){
					proPosition = treeList.get(i).getProPosition();
					leftNext = productions.get(proPosition).returnLeft();
					if(leftNext.equals(cur)){//找到后继
						treePositionNext = i;
						break;
					}
				}
				if(treePositionNext==-1){
					System.out.println("出错");
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
		}else{//终结符或者空串，就停止查找后继
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
					cur +=" 值为" + keys[keys.length-1-symPosition];
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
					cur +=" 值为" + keys[keys.length-1-symPosition];
				}
			}
			String space = "";
			for(int i=0;i<space_num-1;i++){ //终结符没有后继，不需要预留下一个符号的位置
				space += "\t";
			}
			String node = space + "\t" + cur;
			return node;
		}
		return null;
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
	
	//从栈顶向栈底扫描，扫描到depth深度的符号。 栈中始终有奇数个符号
	public String topToBottomStack(int depth,Stack<String> stack){
		int stack_len = stack.size();
		int stack_dep = stack_len/2;
		System.out.println("底到顶");
		for(String s:stack){
			System.out.println(s);
		}
		System.out.println("栈的深度："+stack_dep);
		System.out.println("指定深度："+depth);
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
			System.out.println("深度越界："+depth);
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
