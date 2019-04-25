package com.test;

import java.util.ArrayList;
import java.util.List;

public class WordAnalyse {
	//�ʷ�����ʵ��
	
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
		if(flag == false) { //�Ƿ��Ǵʷ�����
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
	
	//����ǰ�ַ�
	public int handleChar(int cur){
		if(SomeWord.LETTER_.indexOf(text.charAt(cur)) != -1){//�������ĸ���»���
			cur = isID(cur);
		}else if(SomeWord.DIGIT.indexOf(text.charAt(cur)) != -1){//����ǳ���
			cur = isNum(cur);
		}else if(SomeWord.DELIMITER.indexOf(text.charAt(cur)) != -1){//����ǽ��
			cur = isDELI(cur);
		}else if(SomeWord.OP.indexOf(text.charAt(cur)) != -1){ //����������
			cur = isOP(cur);
		}else{
			cur++;
		}
		return cur;
	}
	
	//��ʶ��
	public int isID(int cur){
		String word = "";
		while(cur < length && (SomeWord.DIGIT.indexOf(text.charAt(cur)) != -1 ||
				SomeWord.LETTER_.indexOf(text.charAt(cur)) != -1)){
			word += String.valueOf(text.charAt(cur));
			cur++;
		}
		if(SomeWord.KEYWORD.containsKey(word)){ //�ؼ���
			printInfo(word, SomeWord.KEYWORD.get(word), SomeWord.IDENTIFIER_DFA, row_num, "�ؼ���");
		}else{
			printInfo(word, "IDN", SomeWord.IDENTIFIER_DFA, row_num, "��ʶ��");
		}
		return cur;
	}
	
	//���
	public int isDELI(int cur){
		String word = String.valueOf(text.charAt(cur));
		printInfo(word, SomeWord.DELM_MAP.get(word), SomeWord.DELIMITER_DFA, row_num, "���");
		cur++;
		return cur;
	}	
	
	
	//ʶ�������
	public int isOP(int cur){
		String word = "";
		while(cur < length && SomeWord.OP.indexOf(text.charAt(cur)) != -1 && word.length() < 2){
			word += String.valueOf(text.charAt(cur));
			cur++;
		}
		if(word.length() == 1){//���������
			printInfo(word, SomeWord.OP_MAP.get(word), SomeWord.OP_DFA, row_num, "�����");
		}
		else if(word.length() == 2){//���������
			if(SomeWord.OP_MAP.containsKey(word)){//�ж�Ӧ��
				if(word.equals("/*")){//ע��
					cur = isNOTES_1(cur);
				}else if(word.equals("//")){//ע��
					cur = isNOTES_2(cur);
				}else{//ƥ��������
					printInfo(word, SomeWord.OP_MAP.get(word), SomeWord.NUM_DECIMAL, row_num, "�����");
				}
			}else{//2���������û�ж�Ӧ��
				//���磬��x/*����x//����x�������
				boolean flag = cur < length ? word.charAt(1) == '/' && (text.charAt(cur)=='*' ||  text.charAt(cur)=='/') : false;
				if(flag){ 
					word = String.valueOf(word.charAt(0));
					printInfo(word,SomeWord.OP_MAP.get(word),SomeWord.OP_DFA,row_num,"�����");
					cur--;//���˵���/��������ʶ��
				}
				else{
					printInfo(word, "�����ڵ������", null, row_num, "wrong");
				}
			}
		}
		return cur;
	}
	
	//ʶ��ע��/*...*/
	public int isNOTES_1(int cur){
		boolean isNoteEnd = false;
		boolean isTextEnd = false;
		while(cur < length){//ע������
			if(text.charAt(cur) == '*'){
				try {
					if(text.charAt(cur + 1) == '/'){
						cur += 2;
						printInfo("/**/", SomeWord.OP_MAP.get("/*"), SomeWord.NOTES_1_DFA, row_num, "ע��");
						isNoteEnd = true;
						break;
					}
				} catch (Exception e) {
					//���
					printInfo("/*", "ȱ��*/", null, row_num, "wrong");
					cur = length;
					isTextEnd = true;//��ֹ�ظ���ӡ
					break;
				}
			}else if(text.charAt(cur) == '\n' || text.charAt(cur) == '\r'){
				row_num++;
			}
			cur++;
		}
		if(cur == length && !isNoteEnd && !isTextEnd){
			printInfo("/*", "ȱ��*/", null, row_num, "wrong");
		}
		return cur;
	}

	//ʶ��ע��//...
	public int isNOTES_2(int cur){
		while(cur < length && text.charAt(cur) != '\n'){ //һ��֮�ڣ��������з���\n������ѭ������//��ע�ͷ�Χ����
			cur++;
		}
		printInfo("//", SomeWord.OP_MAP.get("//"), SomeWord.NOTES_2_DFA, row_num, "ע��");
		return cur;
	}

	//����
	public  int isNum(int cur){
		String word = "";
		if(cur < length && SomeWord.DIGIT_P.indexOf(text.charAt(cur)) != -1){//ʮ������
			cur = isDecimal(cur);
		}else if(cur < length && '0' == text.charAt(cur)){//�˽��ƻ�ʮ�����ƣ������㿪ͷ��С��
			word += text.charAt(cur);
			cur++;
			try{
				if(text.charAt(cur) == '.'){//���㿪ͷ��С��
					word += text.charAt(cur);
					cur++;//��һ���Ƿ�λ����
					cur = isFraction(cur, word);
				}
				else if(text.charAt(cur) == 'x'){//ʮ������
					cur++;//��һ���ַ�
					cur = isHexadecimal(cur);
				}else if(SomeWord.DIGIT.indexOf(text.charAt(cur)) != -1){//0��������
					word += text.charAt(cur);
					cur = isOctonary(cur, word);//�˽����ж�
				}
				else{//0��������
					word = "0";
					printInfo(word, "NUM", SomeWord.NUM_DECIMAL, row_num, "ʮ��������"); // 0
				}
			}catch(Exception e){//����һ��0
				printInfo(word, "NUM", SomeWord.NUM_DECIMAL, row_num, "ʮ��������"); // 0
			}
		}
		return cur;
	}
	
	//�˽���
	public int isOctonary(int cur, String word){
		if(SomeWord.DIGIT_O.indexOf(text.charAt(cur)) != -1){//�淶�İ˽�����
			cur++;
			try {
				while(cur < length && SomeWord.DIGIT_O.indexOf(text.charAt(cur)) != -1){
						word += String.valueOf(text.charAt(cur));
						cur++;
				}
			}catch (Exception e) {
				// TODO: handle exception
			}
			printInfo(word, "NUM", SomeWord.NUM_OCTONARY, row_num, "�˽�������");
		}else{
			printInfo(word, "���淶�İ˽�����", null, row_num, "wrong");
			cur++;
		}
		return cur;
	}
	
	//ʮ������
	public int isHexadecimal(int cur){
		String word = "0x";
		try {
			if(SomeWord.DIGIT_H.indexOf(text.charAt(cur)) != -1){
				while(cur < length && SomeWord.DIGIT_H.indexOf(text.charAt(cur)) != -1){
					word += String.valueOf(text.charAt(cur));
					cur++;
				}
				printInfo(word, "NUM", SomeWord.NUM_HEXADECIMAL, row_num, "ʮ����������"); //�������λ��
			}else{//����������0xG12�����Ĵ���
				word += text.charAt(cur);
				cur++;
				printInfo(word, "���淶��ʮ��������", null, row_num, "wrong");
			}
		} catch (Exception e) {
			printInfo(word, "���淶��ʮ��������", null, row_num, "wrong");
		}
		return cur;
	}		
	
	//ʮ����
	public int isDecimal(int cur){
		String word = "";
		while(cur < length && SomeWord.DIGIT.indexOf(text.charAt(cur)) != -1){
			word += String.valueOf(text.charAt(cur));
			cur++;
		}
		if(cur == length && SomeWord.DIGIT.indexOf(text.charAt(cur-1)) != -1){//�����������λ�ã�����û��ָ��
			printInfo(word,"NUM",SomeWord.NUM_DECIMAL,row_num,"ʮ��������");
		}else{
			try{
				if(text.charAt(cur) == '.'){//С��
					word += text.charAt(cur);
					cur++;
					cur = isFraction(cur, word);
				}else if(text.charAt(cur) == 'E'){//ָ��
					word += text.charAt(cur);
					cur++;
					cur = isExponential(cur,word);
				}else{//���������Ҳ������λ��
					printInfo(word, "NUM", SomeWord.NUM_DECIMAL, row_num, "ʮ��������");
				}
			}catch (Exception e) {
				printInfo(word, "���淶����", null, row_num, "wrong");
			}
		}
		return cur;
	}
	
	//ָ��
	public int isExponential(int cur, String word){
		if(SomeWord.DIGIT.indexOf(text.charAt(cur)) != -1
				|| text.charAt(cur) == '+' || text.charAt(cur) == '-'){//E�������֣���+����-
			if(SomeWord.DIGIT.indexOf(text.charAt(cur)) == -1){//��+��-��
				word += text.charAt(cur);
				cur++;
				if(SomeWord.DIGIT.indexOf(text.charAt(cur)) == -1){//��������
					printInfo(word, "���淶��ָ����ʽ", null, row_num, "wrong");
					return cur;
				}
			}
			while(cur < length && SomeWord.DIGIT.indexOf(text.charAt(cur)) != -1){
				word += text.charAt(cur);
				cur++;
			}
			printInfo(word, "NUM", SomeWord.NUM_DECIMAL, row_num, "ʮ��������");
		}
		else{
			word += text.charAt(cur);
			printInfo(word, "���淶��ָ����ʽ", null, row_num, "wrong");
			cur++;
		}
		return cur;
	}
	
	//С��
	public int isFraction(int cur, String word){
		if(SomeWord.DIGIT.indexOf(text.charAt(cur)) == -1){//С�����������
			word += text.charAt(cur);
			printInfo(word, "���淶��С����ʽ", null, row_num, "wrong");
			cur++;
		}else{
			while(cur < length && SomeWord.DIGIT.indexOf(text.charAt(cur)) != -1){
				word += text.charAt(cur);
				cur++;
			}
			printInfo(word, "NUM", SomeWord.NUM_DECIMAL, row_num, "ʮ��������");
		}
		return cur;
	}
	
	//��ӡ���
	public void printInfo(String word, String byWord, String dfa, int row_num, String kind){
		String[] tuple = new String[4];
		boolean isWrong = kind.equals("wrong");
		if(!isGrammarAnalyse){ //�ʷ�����
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
					System.out.println("��" + word + "," + kind);
				}
			}
		}else{ //�﷨����
			if(isWrong){
				tuple[2] = String.valueOf(row_num);
				if(byWord.equals("ȱ��*/")){
					tuple[0] = null;
					tuple[1] = "NOTE";
					tuple[3] = "ȱ��*/";
					token.add(tuple);
					return;
				}else if(byWord.equals("���淶�İ˽�����")){
					tuple[0] = "0";
					tuple[1] = "NUM";
					tuple[3] = "���淶�İ˽�������" + word;
				}else if(byWord.equals("���淶��ʮ��������")){
					tuple[0] = "0";
					tuple[1] = "NUM";
					tuple[3] = "���淶��ʮ����������" + word;
				}
				else if(byWord.equals("���淶����")){
					tuple[0] = "0";
					tuple[1] = "NUM";
					tuple[3] = "���淶������" + word;
				}
				else if(byWord.equals("���淶��ָ����ʽ")){
					tuple[0] = "0";
					tuple[1] = "NUM";
					tuple[3] = "���淶��ָ����ʽ��" + word;
				}else if(byWord.equals("���淶��С����ʽ")){
					tuple[0] = "0";
					tuple[1] = "NUM";
					tuple[3] = "���淶��С����ʽ��" + word;
				}else if(byWord.equals("�����ڵ������")){
					tuple[0] = String.valueOf(word.charAt(0));
					tuple[1] = "OP";
					tuple[3] = "�����ڵ��������" + word;
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
						System.out.println("��" + word + "," + kind);
					}
					token.add(tuple);
				}
			}
		}
	}
	
	//��ӡtoken��
	public void printTbmodel_lex_result() {
		System.out.println("token��: ");
		for(int i = 0; i < tbmodel_lex_result.size(); i++) {
			for(int j = 0; j < tbmodel_lex_result.get(i).length; j++) {
				System.out.print(tbmodel_lex_result.get(i)[j] + "\t");
			}
			System.out.println(" ");
		}
	}
	
	//��ӡ����ʶ���Զ���
	public void printTbmodel_DFA_result() {
		System.out.println("����ʶ���Զ���: ");
		for(int i = 0; i < tbmodel_DFA_result.size(); i++) {
			for(int j = 0; j < tbmodel_DFA_result.get(i).length; j++) {
				System.out.print(tbmodel_DFA_result.get(i)[j] + "\t");
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
