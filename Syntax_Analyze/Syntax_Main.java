package Syntax_Analyze;

import java.io.BufferedReader;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import translator.*;


public class Syntax_Main {
	
	static String [] ps = readGrammer();
	static Map<Character,List<String>> psforfirstMap = new HashMap<Character,List<String>>(); 
	static String [][] psforfirst = transformGforFirst();
	
	static List<List<Production_Proj>> Project_Set_List = new ArrayList<List<Production_Proj>>();//所有的项目集
	static List<project2state> StateList = new ArrayList<>();//所有的状态集
	
	static int state_NUM =0;
	
	static String action_result = "action_result.txt";
	static String goto_result = "goto_result.txt";
	static BufferedWriter action_output = null;
	static BufferedWriter goto_output = null;
	
	public Syntax_Main() {
		 try {
			action_output = new BufferedWriter(new FileWriter(action_result));
			goto_output = new BufferedWriter(new FileWriter(goto_result));
		} catch (IOException e) {
			e.printStackTrace();
		}
		 
			
	}
	
	public static String[][] transformGforFirst(){
		String [] g = readGrammer();
		String [][] fs = new String[g.length][2];
		int i =0;
		for(String str: ps) {
			fs[i][0] = str.substring(0,str.indexOf("->"));
			fs[i][1] = str.substring(str.indexOf("->") +2, str.length());
			if(psforfirstMap == null) {
				psforfirstMap = new HashMap<Character, List<String>>();
			}
			if(!psforfirstMap.containsKey(fs[i][0].charAt(0))) {
				List<String> list = new ArrayList<>();
				list.add(fs[i][1]);
			}
			else {
				psforfirstMap.get(fs[i][0].charAt(0)).add(fs[i][1]);
			}
			i++;
		}
		return fs;
	}
	
	/*
	 * 从文件中读取文法
	 * 返回文法的字符串
	 */
	public static String[] readGrammer() {
		String inputG="myG.txt";	
		try{
			FileInputStream inG=new FileInputStream(inputG);
			BufferedReader strG=new BufferedReader(new InputStreamReader(inG));
			String line="";
			int linecount=0;
			while((line=strG.readLine())!=null){
				linecount++;
			}
			String [] Gline=new String[linecount];
			linecount=0;
			inG=new FileInputStream(inputG);
			strG=new BufferedReader(new InputStreamReader(inG));
			while((line=strG.readLine())!=null){
				Gline[linecount]=line;
				linecount++;
			}
			return Gline;
		}catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/*
	 * 给定项目集闭包的初始产生式，生成完整闭包
	 * initList为初始产生式的项目集集合，clo为初始产生式项目
	 * 返回完整的项目集闭包
	 */
	public static List<Production_Proj> find_CLOSURE(List<Production_Proj> initList, Production_Proj clo){
		if(initList.size() == 0)
			initList.add(clo);
		
		String A = "",         //产生式左部
				a = "",			//产生式项目已识别部分
				B = "",			//产生式项目点号后第一个字符
				b = "",			//产生式项目点号第一个项目后的所有字符
				e = "";			//FIRST集
		String Exp = "^(\\w){1}->([^.]*).([^.]{0,1})([^.]*)$";
		
		Pattern p = Pattern.compile(Exp);
		Matcher m = p.matcher(clo.LRtext);
		
		try {
			while(m.find()) {
				A = m.group(1);
				a = m.group(2);
				B = m.group(3);
				b = m.group(4);
				if(a == null)
					a = "";
				if(B == null)
					B = "";
				if(b == null)
					b = "";
			}
			
			List<String> alist = null;
			if(B.equals(""))
				return initList;
			else
				alist = findn(B.charAt(0));
			
			
			e = getFirsts(b+clo.expected);
			for(int i = 0; i < e.length(); i++) {
				for(int j = 0; j < alist.size(); j++) {
					Production_Proj closure_next;
					closure_next = new Production_Proj(B + "->."+alist.get(j), e.charAt(i));
					if(!isExist(initList,closure_next)) {
						initList.add(closure_next);
						find_CLOSURE(initList, closure_next);
					}
				}
			}
			
			return initList;
		}catch(Exception e2){
			e2.printStackTrace();
			return initList;
		}
		
	}
	
	/*
	 * 求产生式右部的第一个字符
	 * 
	 */
	public static String itsFirst(char ch){

		String res = "";
		for(int i=0;i<psforfirst.length;i++){
			if(psforfirst[i][0].charAt(0)==ch){
				if(psforfirst[i][1].equals("")){
//					res+='#';
				}else{
					if(psforfirst[i][1].charAt(0)!=ch){
						res+=psforfirst[i][1].charAt(0);
					}
				}
			}
		}
		return res;
	}
	
	public static boolean exits(char ch,String str){
		for(int i=0;i<str.length();i++){
			if(ch==str.charAt(i)){
				return true;
			}
		}
		return false;
	}
	
	/*
	 * 获取FIRST集
	 */
	public static String getFirst(char ch){   // get First Set (ch)
		String result="";
		String itsfirstch="";
		if(isV(ch)){
			itsfirstch=itsFirst(ch);
			for(int i=0;i<itsfirstch.length();i++){
				if(!exits(itsfirstch.charAt(i),result))
					result+=getFirst(itsfirstch.charAt(i));
			}
		}
		else{/* it is T*/
			if(!exits(ch,result))
				result+=String.valueOf(ch);		
			//System.out.println("2");
		}
		return result;
	}

	/*
	 * 获取FIRST集
	 */
	public static String getFirsts(String set){   //  get First Set (String)
		String result="";
		result+=getFirst(set.charAt(0));
		return result;
	}
	
	/*
	 * 判断某个项目集闭包closure_Set中是否包含某个项目temp
	 * 包含返回true
	 */
	public static boolean isExist(List<Production_Proj> closure_Set, Production_Proj temp) {
		boolean res = false;
		for(int i =0; i < closure_Set.size(); i++) {
			Production_Proj forTest = closure_Set.get(i);
			if(forTest.LRtext.equals(temp.LRtext) && forTest.expected == temp.expected) {
				res = true;
			}
		}
		return res;
	}
	
	

	/*
	 * 寻找左部是head的产生式，并将所有产生式的右部放入List返回
	 */
	public static List<String> findn(char head){
		List<String> res = new ArrayList<String>();
		for(int i = 1; i < ps.length;i++) {
			if(ps[i].charAt(0) == head)
				res.add(ps[i].substring(3, ps[i].length()));
		}
		return res;
	}
	
	/*
	 * 从第一个项目集，通过遇到可能的输入字符，获取所有的项目集
	 */
	public static void getAllProject(project2state State) {
		Stack <Character> stack = new Stack<>();
		//将项目集中的每个项目的可能遇到的输入字符压栈，为后续转移生成新状态做准备
		for(int i = 0; i < State.project_Set.size(); i ++) {
			String LRtext = State.project_Set.get(i).LRtext;
			if(LRtext.indexOf('.') + 1 != LRtext.length()) {
				if(!stack.contains(LRtext.charAt(LRtext.indexOf('.')+1))) {
					stack.push(LRtext.charAt(LRtext.indexOf('.')+1));
				}
			}
		}
		
		int length = stack.size();
		for(int j = 0; j < length; j++) {
			char Successor_Ch = stack.pop();
			List<Production_Proj> new_ProjectSet = new ArrayList<>();
			//当状态State遇到输入字符Successor_Ch转移得到的新的项目集闭包即状态
			new_ProjectSet = get_Successor_Project(State.project_Set, Successor_Ch);

			if(!in_Project_Set_List(new_ProjectSet)) {//判断该新生成项目集闭包是否已经生成过
				state_NUM++;
				Project_Set_List.add(new_ProjectSet);
				project2state new_State = new project2state(new_ProjectSet, state_NUM);
				StateList.add(new_State);
				getAllProject(new_State);
			}
		}
	}

	/*
	 * 判断该项目集是否已经生成过
	 */
	public static boolean in_Project_Set_List(List<Production_Proj> test) {
		boolean res = false;
		for(int i =0; i < Project_Set_List.size();i++) {
			if(equals(Project_Set_List.get(i),test)) {
				res = true;
			}
		}
		return res;
	}
	
	/*
	 * 判断两个项目集是否相等
	 */
	public static boolean equals(List<Production_Proj> test1, List<Production_Proj> test2) {
		if(test1.size()!=test2.size())
			return false;
		else{
			for(int i=0;i<test2.size();i++){
				if(!isExist(test1,test2.get(i))){
					return false;
				}
			}
			return true;
		}
	}
	
	/*
	 * 项目集闭包project在遇到输入字符Successor后转移得到的新的项目集
	 */
	public static List<Production_Proj> get_Successor_Project(List<Production_Proj> project, char Successor){
		List<Production_Proj> successor_pro = new ArrayList<>();
		
		for(int i = 0; i < project.size(); i++) {
			String LRtext = project.get(i).LRtext;
			if(LRtext.indexOf('.') + 1 != LRtext.length()) {
				if(Successor == LRtext.charAt(LRtext.indexOf('.') +1)) {
					StringBuffer buffer=new StringBuffer(project.get(i).LRtext);
					buffer.setCharAt(project.get(i).LRtext.indexOf('.'), project.get(i).LRtext.charAt(project.get(i).LRtext.indexOf('.')+1));
					buffer.setCharAt(project.get(i).LRtext.indexOf('.')+1, '.');
					String bufferS=buffer.toString();
					//直接遇到successor字符得到的产生式项目，而其展望符必定是继承的
					Production_Proj new_product = new Production_Proj(bufferS, project.get(i).expected);
					if(!successor_pro.contains(new_product))
						successor_pro.add(new_product);
				}
			}
		}
		
		int len = successor_pro.size();
		for(int j = 0; j < len; j++) {
			successor_pro = find_CLOSURE(successor_pro, successor_pro.get(j));
		}
		return successor_pro;
	}
	
	
	/*
	 * 生成action表
	 */
	public static List<ACTION_TABLE> creat_Actiontable(){
		List<ACTION_TABLE> actiontable = new ArrayList<>();
		String VandT = "";
		
		for(String str: ps) {
			str = str.substring(0,1) + str.substring(3,str.length());
			VandT +=str;
		}
		VandT = VandT.substring(1,VandT.length());
		Stack<Character> stack = new Stack<>();
		for(int i =0; i < VandT.length(); i++) {
			if(!stack.contains(VandT.charAt(i)))
				stack.push(VandT.charAt(i));
		}
		stack.push('#');
		
		//按action表中的每行即每个状态遇到相应的状态求action表
		for(int i= 0; i < StateList.size();i++) {
			ACTION_TABLE one_Row = new ACTION_TABLE();
			for(char stackch: stack) {
				if(!(stackch <= 'Z' && stackch >= 'A')) {//如果是终结符，用于计算action表
					//状态转移函数
					int suc_State = find_Next_State(get_Successor_Project(StateList.get(i).project_Set, stackch));
					if(suc_State >= 0) {//如果遇到非终结符有后继状态，则使用移进动作，写入action表
						one_Row.ch.add(stackch);
						one_Row.action.add("S" + Integer.toString(suc_State));
					}
					else {//预见非终结符没有后继状态，则检查是否为归约
						int guiyue = isGuiYue(StateList.get(i).project_Set, stackch);
						if(guiyue > -1) {
							if(guiyue == 1) {
								one_Row.ch.add(stackch);
								one_Row.action.add("acc");
							}
							else {
								one_Row.ch.add(stackch);
								one_Row.action.add("r" + Integer.toString(guiyue));//用第几个产生式进行规约
							}
						}
						else {
							one_Row.ch.add(stackch);
							one_Row.action.add("error");
						}
					}
				}
			}
			actiontable.add(one_Row);
		}
		
		return actiontable;
	}
	
	/*
	 * 判断是否是归约
	 */
	public static int isGuiYue(List<Production_Proj> now, char inputCh) {
		for(Production_Proj pro: now) {
			if(pro.expected == inputCh){
				String Lrt = pro.LRtext;
				int posi = Lrt.indexOf('.');
				if(posi == Lrt.length()-1) {
					String use_product = Lrt.substring(0, posi) + Lrt.substring(posi+1,Lrt.length());
					int product_NUM = inGrammer(use_product);
					
					
					if(product_NUM > -1) {
						return product_NUM;
					}
				}
			}
		}
		return -1;
	}
	
	
	/*
	 * 寻找相应的文法产生式
	 */
	public static int inGrammer(String pro) {
		for(int i = 0; i < ps.length; i++) {
			if(ps[i].equals(pro)) {
				return i+1;
			}
		}
		return -1;
	}
	
	/*
	 * 给定项目集，在状态列表中寻找该项目集对应的状态号
	 */
	public static int find_Next_State(List<Production_Proj> nowState) {
		for(int i = 0; i < StateList.size(); i++) {
			if(equals(StateList.get(i).project_Set, nowState)) {
				return StateList.get(i).name;
			}
		}
		return -1;
		
	}
	
	/*
	 * 生成goto表
	 */
	public static List<GOTO_TABLE> creat_gototable(){
		List<GOTO_TABLE> goto_table = new ArrayList<>();
		String pro = "";
		
		for(String str: ps) {
			str = str.substring(0,1) + str.substring(3,str.length());
			pro += str;
		}
		pro = pro.substring(1,pro.length());
		
		Stack<Character> stack = new Stack<>();
		for(int i  =0; i < pro.length(); i++) {
			if(!(stack.contains(pro.charAt(i)))) {
				stack.push(pro.charAt(i));
			}
		}
		//按行进行计算
		for(int i = 0; i < StateList.size(); i++) {
			GOTO_TABLE row = new GOTO_TABLE();
			for(char stc: stack) {
				if(stc >= 'A' && stc <= 'Z') {
					int suc_state = find_Next_State(get_Successor_Project(StateList.get(i).project_Set, stc));
					if(suc_state >= 0) {
						row.ch.add(stc);
						row.go_to.add(Integer.toString(suc_state));
					}
					else {
						row.ch.add(stc);
						row.go_to.add("error");
					}
				}
			}
			goto_table.add(row);
		}
		return goto_table;
	}
	
	
	/*
	 * 分析主程序入口
	 */
	public String syntax_Main(String token_Str)throws IOException{
		first.setPs(psforfirst);
		String firstStr = "";
		
		int position_f = ps[0].indexOf("->");
		firstStr = ps[0].substring(0, position_f+2) + "." + 
					ps[0].substring(position_f +2, ps[0].length());//A->.B
		
		List<Production_Proj> first_Project_Set = new ArrayList<>();
		Production_Proj first_Project = new Production_Proj(firstStr, '#');//第一个项目产生式
		first_Project_Set.add(first_Project);	
		
		first_Project_Set = find_CLOSURE(first_Project_Set, first_Project);//第一个项目集闭包
		Project_Set_List.add(first_Project_Set);//添加第一个项目集
		project2state first_State = new project2state(first_Project_Set, 0);
		StateList.add(first_State);//添加第一个状态
		
		getAllProject(first_State);//获取所有的项目集闭包以及状态
		
		List<ACTION_TABLE> action_table = creat_Actiontable();//生成action表
		List<GOTO_TABLE> goto_table = creat_gototable();//生成goto表
		
		output(action_table, goto_table);
		
		return LR1_Analyze(token_Str, action_table,goto_table);
		
	}
	
	/*
	 * 判断是否是非终结符
	 */
	public static boolean isV(char ch) {
		if( ch >= 'A' && ch <= 'Z')
			return true;
		return false;
	}
	
	public static void output(List<ACTION_TABLE> action_table, List<GOTO_TABLE> goto_table) {
		try {
		for(int wa=0;wa<action_table.size();wa++){
			action_output.write(String.format(StateList.get(wa).name+": "));
			action_output.write(String.format(action_table.get(wa).ch+"")+System.getProperty("line.separator"));
			action_output.write(String.format(action_table.get(wa).action+"")+System.getProperty("line.separator"));
		}
		
		for(int wao=0;wao<goto_table.size();wao++){
			goto_output.write(String.format(StateList.get(wao).name+": "));
			goto_output.write(String.format(goto_table.get(wao).ch+"")+System.getProperty("line.separator"));
			goto_output.write(String.format(goto_table.get(wao).go_to+"")+System.getProperty("line.separator"));
		}
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * 语法分析主程序
	 */
	public static String LR1_Analyze(String token_Str, List<ACTION_TABLE> actiontable, List<GOTO_TABLE> gototable) throws IOException {
		
		String output_an="out_analyze.txt";
		String outsr="usewhat.txt";
		BufferedWriter output=new BufferedWriter(new FileWriter(output_an));
		BufferedWriter output2=new BufferedWriter(new FileWriter(outsr));
		
		Stack <Character> inputStack = new Stack <Character> ();
		Stack <Integer> statusStack = new Stack <Integer> ();
		inputStack.push('#');
		statusStack.push(0);
		
		String inputStr = token_Str+"#";
		String action;
		
		int point = 0;
		
		while(true) {
			output.write(String.format("Status Stack now have : "+
						putsStack(statusStack))+System.getProperty("line.separator"));
			output.write(String.format("Char Stack now have : "+
						putsStack(inputStack))+System.getProperty("line.separator"));
			
			int topStates = statusStack.peek();
			if((action = find_in_action(actiontable, topStates, inputStr.charAt(point)))!= null) {
				if(action.charAt(0) == 'S') {
					int temp = 0;
					temp = Integer.valueOf(action.substring(1,action.length()));
					output.write(String.format("Use Action->"+action)+System.getProperty("line.separator"));
					statusStack.push(temp);
					inputStack.push(inputStr.charAt(point));
					output.write(String.format("Status Stack push "+inputStr.charAt(point))+System.getProperty("line.separator"));
					output.write(String.format("Char Stack push : "+inputStr.charAt(point))+System.getProperty("line.separator"));
					point ++;
				}
				else if(action.charAt(0) == 'r') {
					int line = Integer.valueOf(action.substring(1,action.length()));
					output2.write(String.format(""+line)+System.getProperty("line.separator"));
					int len = ps[line-1].length()-3;
					for(int i =0; i < len; i++) {
						inputStack.pop();
						statusStack.pop();
					}
					
					output.write(String.format("Status and Char Stack pop "+len+" element(s)")+System.getProperty("line.separator"));
					int topstatesNow = statusStack.peek();
					inputStack.push(ps[line-1].charAt(0));
					String tmpStr = find_in_goto(gototable, topstatesNow, inputStack.peek());
					if(tmpStr != null) {
						statusStack.push(Integer.parseInt(tmpStr));
						output.write(String.format("Status Stack push "+Integer.parseInt(tmpStr))+System.getProperty("line.separator"));
					}
					output.write(String.format("Use Goto->"+action)+System.getProperty("line.separator"));
				}
				else if(action.equals("acc")) {
					System.out.println("Accepted");
					output.write(String.format("Now Accepted!")+System.getProperty("line.separator"));
					output2.write(String.format("1")+System.getProperty("line.separator"));
					output.flush();
					output.close();
					output2.flush();
					output2.close();
					return "Successfully Accepted";
				}
				else if(action.equals("error")) {
					System.out.println("Error in "+inputStr.charAt(point)+" character!");
					output.write(String.format("Error!")+System.getProperty("line.separator"));
					output.flush();
					output.close();
					if(inputStr.charAt(point)=='#')
						return "Error in the end!";
					return "Error !\n"+Translate.find_error(point);
				}
				
				
			}
		}
		
		
		
	}
	
	
	public static String putsStack(Stack<?> stack){
		String things="";
		for(Object item:stack){
			things+=item.toString();
		}
		return things;
	}
	
	/*
	 * 在action表中寻找相应
	 */
	public static String find_in_action(List <ACTION_TABLE> actiontab,int status,char ch){
		//actiontab.get(0).value
		List <Character> templist=actiontab.get(status).ch;
		int ret=0;
		String res="";
		for(Character item:templist){
			if(item==ch){
				res = actiontab.get(status).action.get(ret);
				return res;
			}
			ret++;
		}
		return null;
	}
	
	/*
	 * 在goto表中寻找相应
	 */
	public static String find_in_goto(List <GOTO_TABLE> gototab,int status,char ch){
		//actiontab.get(0).value
		List <Character> templist=gototab.get(status).ch;
		int ret=0;
		for(Character item:templist){
			if(item==ch){
				return gototab.get(status).go_to.get(ret);
			}
			ret++;
		}
		//System.out.println(res+"~~~~~~~~~~~~~~~~~~~~");
		return null;
	}	
	
	public static void main(String[] args) {
		Syntax_Main test = new Syntax_Main();
		try {
			test.syntax_Main("as(){}");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	
//	public static Set<Character> mygetFirst(char ch) {
//	Set<Character> first = new HashSet<Character>();
//	if(!isV(ch)) {
//		first.add(ch);
//	}else {
//		for(String s: psforfirstMap.get(ch)) {
//			if(s.length() == 0) {
//				first.add('\0');
//			}else {
//				if(!isV(s.charAt(0))) {
//					first.add(s.charAt(0));
//				}
//			}
//		}
//		
//		int originLen = first.size();
//		while(true) {
//			for(String s:psforfirstMap.get(ch)) {
//				if(s.length()>0&&isV(s.charAt(0))&&!(s.charAt(0)==ch)) {
//					for(Character c:mygetFirst(s.charAt(0))) {
//						if(!c.equals('\0')) {
//							first.add(c);
//						}
//					}
//				}
//			}
//			for(String s:psforfirstMap.get(ch)) {
//				boolean flag = true;
//				for(int i =0;i<s.length();i++) {
//					if(i+1<s.length()&&!(s.charAt(0)==ch)&&mygetFirst(s.charAt(i)).contains('\0')) {
//						for(Character c:mygetFirst(s.charAt(i+1))) {
//							first.add(c);
//						}
//					}else {
//						if(i == s.length()-1&&mygetFirst(s.charAt(i)).contains('\0'))
//							flag = false;
//						break;
//					}
//				}
//				if(!flag) {
//					first.add('\0');
//				}
//			}
//			if(first.size()!=originLen) {
//				originLen = first.size();
//				continue;
//			}else {
//				break;
//			}
//		}
//	}
//	return first;
//}
//
//public static Set<Character> mygetFirst(String s){
//	Set<Character> first = new HashSet<Character>();
//	boolean flag = true;
//	if(s.length()>0) {
//		for(Character c:mygetFirst(s.charAt(0))) {
//			if(!c.equals('\0')) {
//				first.add(c);
//			}
//		}
//	}
//	for(int i =0;i<s.length();i++) {
//		if(i+1<s.length()&&mygetFirst(s.charAt(i)).contains('\0')) {
//			for(Character c:mygetFirst(s.charAt(i+1))) {
//				first.add(c);
//			}
//		}else {
//			if(i == s.length()-1&&mygetFirst(s.charAt(i)).contains('\0'))
//				flag = false;
//			break;
//		}
//	}
//	if(!flag) {
//		first.add('\0');
//	}
//	return first;
//}
	
	
	
	
	
	
	
	
	
	
	
}
