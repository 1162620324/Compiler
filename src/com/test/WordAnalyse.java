package com.test;

import java.util.ArrayList;
import java.util.List;

public class WordAnalyse {
	//词法分析实现
	
	private String text;
	private int length;
	private int row_num = 1;
	
	private List<String[]> tbmodel_lex_result = new ArrayList<String[]>();
	private List<String[]> tbmodel_DFA_result = new ArrayList<String[]>();
	private List<String[]> tbmodel_wrong_result = new ArrayList<String[]>();
	
	public  List<String[]> token = new ArrayList<String[]>();
	
	public List<String[]> getToken() {
		return token;
	}
	public void setToken(List<String[]> token) {
		this.token = token;
	}

	private boolean isGrammarAnalyse = false;
	
	public WordAnalyse(String text, boolean flag){
		this.text = text;
		length = text.length();
		if(flag == false) { //是否是词法分析
		    isGrammarAnalyse = true;
		}
	}
	
	public void analyse(){
		int cur = 0;
		while(cur<length){
			if(text.charAt(cur)=='\n' || text.charAt(cur)=='\r'){
				row_num++;
			}else if(text.charAt(cur)==' '){
				cur++;
				continue;
			}
			cur = handleChar(cur);
		}
	}
	
	//处理当前字符
	public int handleChar(int cur){
		if(SomeWord.LETTER_.indexOf(text.charAt(cur)) != -1){//如果是字母或下划线
			cur = isID(cur);
		}else if(SomeWord.DIGIT.indexOf(text.charAt(cur)) != -1){//如果是常数
			cur = isNum(cur);
		}else if(SomeWord.DELIMITER.indexOf(text.charAt(cur)) != -1){//如果是界符
			cur = isDELI(cur);
		}else if(SomeWord.OP.indexOf(text.charAt(cur)) != -1){ //如果是运算符
			cur = isOP(cur);
		}else{
			cur++;
		}
		return cur;
	}
	
	//标识符
	public int isID(int cur){
		String word = "";
		while(cur < length && (SomeWord.DIGIT.indexOf(text.charAt(cur)) != -1 ||
				SomeWord.LETTER_.indexOf(text.charAt(cur)) != -1)){
			word += String.valueOf(text.charAt(cur));
			cur++;
		}
		if(SomeWord.KEYWORD.containsKey(word)){ //关键字
			printInfo(word, SomeWord.KEYWORD.get(word), SomeWord.IDENTIFIER_DFA, row_num, "关键字");
		}else{
			printInfo(word, "IDN", SomeWord.IDENTIFIER_DFA, row_num, "标识符");
		}
		return cur;
	}
	
	//界符
	public int isDELI(int cur){
		String word = String.valueOf(text.charAt(cur));
		printInfo(word, SomeWord.DELM_MAP.get(word), SomeWord.DELIMITER_DFA, row_num, "界符");
		cur++;
		return cur;
	}	
	
	
	//识别运算符
	public int isOP(int cur){
		String word = "";
		while(cur < length && SomeWord.OP.indexOf(text.charAt(cur)) != -1 && word.length() < 2){
			word += String.valueOf(text.charAt(cur));
			cur++;
		}
		if(word.length() == 1){//单个运算符
			printInfo(word, SomeWord.OP_MAP.get(word), SomeWord.OP_DFA, row_num, "运算符");
		}
		else if(word.length() == 2){//两个运算符
			if(SomeWord.OP_MAP.containsKey(word)){//有对应的
				if(word.equals("/*")){//注释
					cur = isNOTES_1(cur);
				}else if(word.equals("//")){//注释
					cur = isNOTES_2(cur);
				}else{//匹配的运算符
					printInfo(word, SomeWord.OP_MAP.get(word), SomeWord.NUM_DECIMAL, row_num, "运算符");
				}
			}else{//2个运算符，没有对应的
				//形如，“x/*”或“x//”，x是运算符
				boolean flag = cur < length ? word.charAt(1) == '/' && (text.charAt(cur)=='*' ||  text.charAt(cur)=='/') : false;
				if(flag){ 
					word = String.valueOf(word.charAt(0));
					printInfo(word,SomeWord.OP_MAP.get(word),SomeWord.OP_DFA,row_num,"运算符");
					cur--;//回退到“/”，重新识别
				}
				else{
					printInfo(word, "不存在的运算符", null, row_num, "wrong");
				}
			}
		}
		return cur;
	}
	
	//识别注释/*...*/
	public int isNOTES_1(int cur){
		boolean isNoteEnd = false;
		boolean isTextEnd = false;
		while(cur < length){//注释内容
			if(text.charAt(cur) == '*'){
				try {
					if(text.charAt(cur + 1) == '/'){
						cur += 2;
						printInfo("/**/", SomeWord.OP_MAP.get("/*"), SomeWord.NOTES_1_DFA, row_num, "注释");
						isNoteEnd = true;
						break;
					}
				} catch (Exception e) {
					//溢出
					printInfo("/*", "缺少*/", null, row_num, "wrong");
					cur = length;
					isTextEnd = true;//防止重复打印
					break;
				}
			}else if(text.charAt(cur) == '\n' || text.charAt(cur) == '\r'){
				row_num++;
			}
			cur++;
		}
		if(cur == length && !isNoteEnd && !isTextEnd){
			printInfo("/*", "缺少*/", null, row_num, "wrong");
		}
		return cur;
	}

	//识别注释//...
	public int isNOTES_2(int cur){
		while(cur < length && text.charAt(cur) != '\n'){ //一行之内，遇到换行符号\n后，跳出循环，即//的注释范围结束
			cur++;
		}
		printInfo("//", SomeWord.OP_MAP.get("//"), SomeWord.NOTES_2_DFA, row_num, "注释");
		return cur;
	}

	//数字
	public  int isNum(int cur){
		String word = "";
		if(cur < length && SomeWord.DIGIT_P.indexOf(text.charAt(cur)) != -1){//十进制数
			cur = isDecimal(cur);
		}else if(cur < length && '0' == text.charAt(cur)){//八进制或十六进制，或以零开头的小数
			word += text.charAt(cur);
			cur++;
			try{
				if(text.charAt(cur) == '.'){//以零开头的小数
					word += text.charAt(cur);
					cur++;//下一个是否位数字
					cur = isFraction(cur, word);
				}
				else if(text.charAt(cur) == 'x'){//十六进制
					cur++;//下一个字符
					cur = isHexadecimal(cur);
				}else if(SomeWord.DIGIT.indexOf(text.charAt(cur)) != -1){//0后是数字
					word += text.charAt(cur);
					cur = isOctonary(cur, word);//八进制判断
				}
				else{//0后不是数字
					word = "0";
					printInfo(word, "NUM", SomeWord.NUM_DECIMAL, row_num, "十进制数字"); // 0
				}
			}catch(Exception e){//单是一个0
				printInfo(word, "NUM", SomeWord.NUM_DECIMAL, row_num, "十进制数字"); // 0
			}
		}
		return cur;
	}
	
	//八进制
	public int isOctonary(int cur, String word){
		if(SomeWord.DIGIT_O.indexOf(text.charAt(cur)) != -1){//规范的八进制数
			cur++;
			try {
				while(cur < length && SomeWord.DIGIT_O.indexOf(text.charAt(cur)) != -1){
						word += String.valueOf(text.charAt(cur));
						cur++;
				}
			}catch (Exception e) {
				// TODO: handle exception
			}
			printInfo(word, "NUM", SomeWord.NUM_OCTONARY, row_num, "八进制数字");
		}else{
			printInfo(word, "不规范的八进制数", null, row_num, "wrong");
			cur++;
		}
		return cur;
	}
	
	//十六进制
	public int isHexadecimal(int cur){
		String word = "0x";
		try {
			if(SomeWord.DIGIT_H.indexOf(text.charAt(cur)) != -1){
				while(cur < length && SomeWord.DIGIT_H.indexOf(text.charAt(cur)) != -1){
					word += String.valueOf(text.charAt(cur));
					cur++;
				}
				printInfo(word, "NUM", SomeWord.NUM_HEXADECIMAL, row_num, "十六进制数字"); //不是最后位置
			}else{//检测错误，形如0xG12这样的错误
				word += text.charAt(cur);
				cur++;
				printInfo(word, "不规范的十六进制数", null, row_num, "wrong");
			}
		} catch (Exception e) {
			printInfo(word, "不规范的十六进制数", null, row_num, "wrong");
		}
		return cur;
	}		
	
	//十进制
	public int isDecimal(int cur){
		String word = "";
		while(cur < length && SomeWord.DIGIT.indexOf(text.charAt(cur)) != -1){
			word += String.valueOf(text.charAt(cur));
			cur++;
		}
		if(cur == length && SomeWord.DIGIT.indexOf(text.charAt(cur-1)) != -1){//整数，是最后位置，并且没有指数
			printInfo(word,"NUM",SomeWord.NUM_DECIMAL,row_num,"十进制数字");
		}else{
			try{
				if(text.charAt(cur) == '.'){//小数
					word += text.charAt(cur);
					cur++;
					cur = isFraction(cur, word);
				}else if(text.charAt(cur) == 'E'){//指数
					word += text.charAt(cur);
					cur++;
					cur = isExponential(cur,word);
				}else{//整数，并且不是最后位置
					printInfo(word, "NUM", SomeWord.NUM_DECIMAL, row_num, "十进制数字");
				}
			}catch (Exception e) {
				printInfo(word, "不规范的数", null, row_num, "wrong");
			}
		}
		return cur;
	}
	
	//指数
	public int isExponential(int cur, String word){
		if(SomeWord.DIGIT.indexOf(text.charAt(cur)) != -1
				|| text.charAt(cur) == '+' || text.charAt(cur) == '-'){//E后是数字，或+，或-
			if(SomeWord.DIGIT.indexOf(text.charAt(cur)) == -1){//是+或-号
				word += text.charAt(cur);
				cur++;
				if(SomeWord.DIGIT.indexOf(text.charAt(cur)) == -1){//不是数字
					printInfo(word, "不规范的指数形式", null, row_num, "wrong");
					return cur;
				}
			}
			while(cur < length && SomeWord.DIGIT.indexOf(text.charAt(cur)) != -1){
				word += text.charAt(cur);
				cur++;
			}
			printInfo(word, "NUM", SomeWord.NUM_DECIMAL, row_num, "十进制数字");
		}
		else{
			word += text.charAt(cur);
			printInfo(word, "不规范的指数形式", null, row_num, "wrong");
			cur++;
		}
		return cur;
	}
	
	//小数
	public int isFraction(int cur, String word){
		if(SomeWord.DIGIT.indexOf(text.charAt(cur)) == -1){//小数点后不是数字
			word += text.charAt(cur);
			printInfo(word, "不规范的小数形式", null, row_num, "wrong");
			cur++;
		}else{
			while(cur < length && SomeWord.DIGIT.indexOf(text.charAt(cur)) != -1){
				word += text.charAt(cur);
				cur++;
			}
			printInfo(word, "NUM", SomeWord.NUM_DECIMAL, row_num, "十进制数字");
		}
		return cur;
	}
	
	//打印输出
	public void printInfo(String word, String byWord, String dfa, int row_num, String kind){
		String[] tuple = new String[4];
		boolean isWrong = kind.equals("wrong");
		if(!isGrammarAnalyse){ //词法分析
			if(isWrong){
				tbmodel_wrong_result.add(new String[] {word, byWord, String.valueOf(row_num)});
			}else{
				if(byWord != null){
					if(byWord.equals("NUM")){
						tbmodel_lex_result.add(new String[] {word, "< "+byWord+" , "+word+" >", String.valueOf(row_num), kind});
					}else if(byWord.equals("IDN")){
						tbmodel_lex_result.add(new String[] {word, "< "+byWord+" , "+word+">", String.valueOf(row_num), kind});
					}else{
						tbmodel_lex_result.add(new String[] {word, "< "+byWord+" , _ >", String.valueOf(row_num), kind});
					}
					tbmodel_DFA_result.add(new String[]{word, dfa});
				}else{
					System.out.println("空" + word + "," + kind);
				}
			}
		}else{ //语法分析
			if(isWrong){
				tuple[2] = String.valueOf(row_num);
				if(byWord.equals("缺少*/")){
					tuple[0] = null;
					tuple[1] = "NOTE";
					tuple[3] = "缺少*/";
					token.add(tuple);
					return;
				}else if(byWord.equals("不规范的八进制数")){
					tuple[0] = "0";
					tuple[1] = "NUM";
					tuple[3] = "不规范的八进制数：" + word;
				}else if(byWord.equals("不规范的十六进制数")){
					tuple[0] = "0";
					tuple[1] = "NUM";
					tuple[3] = "不规范的十六进制数：" + word;
				}
				else if(byWord.equals("不规范的数")){
					tuple[0] = "0";
					tuple[1] = "NUM";
					tuple[3] = "不规范的数：" + word;
				}
				else if(byWord.equals("不规范的指数形式")){
					tuple[0] = "0";
					tuple[1] = "NUM";
					tuple[3] = "不规范的指数形式：" + word;
				}else if(byWord.equals("不规范的小数形式")){
					tuple[0] = "0";
					tuple[1] = "NUM";
					tuple[3] = "不规范的小数形式：" + word;
				}else if(byWord.equals("不存在的运算符")){
					tuple[0] = String.valueOf(word.charAt(0));
					tuple[1] = "OP";
					tuple[3] = "不存在的运算符：" + word;
				}
				token.add(tuple);
			}else {
				if(!word.equals("/**/") && !word.equals("//")){
					tuple[0] = word;
					tuple[2] = String.valueOf(row_num);
					if(byWord != null){
						if(byWord.equals("NUM")){
							tuple[1] = "NUM";
						}else if(byWord.equals("IDN")){
							tuple[1] = "IDN";
						}
					}else {
						System.out.println("空" + word + "," + kind);
					}
					token.add(tuple);
				}
			}
		}
	}
	
	//打印token字
	public void printTbmodel_lex_result() {
		System.out.println("token字: ");
		for(int i = 0; i < tbmodel_lex_result.size(); i++) {
			for(int j = 0; j < tbmodel_lex_result.get(i).length; j++) {
				System.out.print(tbmodel_lex_result.get(i)[j] + "\t");
			}
			System.out.println(" ");
		}
	}
	
	//打印符号识别自动机
	public void printTbmodel_DFA_result() {
		System.out.println("符号识别自动机: ");
		for(int i = 0; i < tbmodel_DFA_result.size(); i++) {
			for(int j = 0; j < tbmodel_DFA_result.get(i).length; j++) {
				System.out.print(tbmodel_DFA_result.get(i)[j] + "\t");
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
