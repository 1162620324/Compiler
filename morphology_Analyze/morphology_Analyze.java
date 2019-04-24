package morphology_Analyze;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Stack;





public class morphology_Analyze {
	
																							///
	
	static ArrayList<Sym_Table> tablelist=new ArrayList<Sym_Table>();
	
	
	
	static ArrayList<digit_Value_Table> valuelist=new ArrayList<digit_Value_Table>();
	static keyWord_Symbol symbolRecord = new keyWord_Symbol();
	String result_token_File = "result_token.txt";
	BufferedWriter output_token = null;
	
	public morphology_Analyze() {
		try {
			output_token = new BufferedWriter(new FileWriter(result_token_File));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
/*
/*测试用例*/
/*

int i = 0;
while(i == 1){
    i++;
}
*/
//错误用例：
/*
1.....
1.05e;
"okokok
'ok
/*aas

 */
 
	
	public static boolean isLetter(char ch) {
		return ((ch >= 'a' && ch < 'z') || (ch >= 'A' && ch <= 'Z') || ch == '_');
	}
	
	public static boolean isDigit(char ch) {
		return (ch >= '0' && ch <= '9');
	}
	
	public static boolean isLegalDigit(String str) {
		if(str.matches("-?[0-9]+") || 
				str.matches("-?[0-9]+.[0-9]+")||
				str.matches("-?[1-9]+e([+-]?([0-9]+.)?[0-9]+)") ||
				str.matches("-?[0-9]+.[0-9]+e(-?([0-9]+.)?[0-9]+)"))
			return true;
		else
			return false;
	}
	
	/*
	 * 识别处理注释，以及判断注释相关的错误、
	 */
	private String [] handle_Note(String line, BufferedWriter result_Buffered, boolean isNote, String note) throws IOException {
		String return_Line = line;
		String return_isNote = "false";
		String return_note = note;
		String isContinue = "false";
		int Note_begin = 0;
		int Note_end = 0;
		String [] Return = new String [4];
		
		if(!isNote) {
			Note_begin = line.indexOf("/*");
			Note_end = line.indexOf("*/");
			if(Note_begin > -1) {
				if(Note_end > -1) {
					return_note = line.substring(Note_begin, Note_end+2);
					return_Line = line.substring(0, Note_begin) + line.substring(Note_end+2, line.length());
					System.out.println("注释是：" + return_note);
					result_Buffered.write("注释是： " + return_note + "\n");
					return_note = "";
					return_isNote = "false";
				}
				else {
					return_isNote = "true";
					return_note = line.substring(Note_begin, line.length());
					return_Line = line.substring(0, Note_begin);
				}
			}
		}
		else if(isNote) {
			Note_end = line.indexOf("*/");
			if(Note_end > -1) { 
				return_isNote = "false";
				return_note += line.substring(0, Note_end + 2);
				return_Line = line.substring(Note_end +2, line.length());
				System.out.println("注释： "+ return_note);
				result_Buffered.write("注释是：" + return_note + "\n");
				return_note = "";
			}
			else {
				return_note += line;
				isContinue = "true";
			}
		}
		
		Return [0] = return_Line;
		Return [1] = return_isNote;
		Return [2] = return_note;
		Return [3] = isContinue;
		
		return Return;
	}
	
	/*
	 * 处理标识符以及关键字程序
	 */
	private void handle_biaoshi_keyword(String tempWord, int pos, BufferedWriter result_Buffered) throws IOException {
		if(pos >= 0 && pos != 14 && pos != 15) {
			System.out.println(tempWord+"   <"+pos+",关键字>");
			result_Buffered.write(tempWord+"   <"+pos+",关键字>\n");
			System.out.println("main" + pos);
			output_token.write(String.format(String.valueOf(pos))+System.getProperty("line.separator"));
		}
		else if( pos == 14 || pos == 15) {
			if(pos == 14 ) {
				System.out.println(tempWord+"   <"+pos+",布尔常量 1>");  
				result_Buffered.write(tempWord+"   <"+pos+",布尔常量 1>\n");
				output_token.write(String.format("i")+System.getProperty("line.separator"));
			}
			else {
				System.out.println(tempWord+"   <"+pos+",布尔常量 0>\n");  
				result_Buffered.write(tempWord+"   <"+pos+",布尔常量 0>\n");
				output_token.write(String.format("j")+System.getProperty("line.separator"));
			}
		}
		else {
			Sym_Table new_symbol = new Sym_Table(tablelist.size(), 1, tempWord);
			tablelist.add(new_symbol);
			System.out.println(tempWord + "    <1,"+(tablelist.size()-1)+">");
			result_Buffered.write(tempWord + "    <1,"+(tablelist.size()-1)+">\n");
			output_token.write(String.format("y")+System.getProperty("line.separator"));
		}
	}
	
	/*
	 * 处理数字常量	
	 */
	private void handle_Digit(String tempword, BufferedWriter result_Buffered) throws IOException {
		if(isLegalDigit(tempword)) {
			if(tempword.indexOf(".") == -1 && tempword.indexOf("e") == -1) {
				System.out.println(tempword+"     <2,整数>");
				valuelist.add(new digit_Value_Table(valuelist.size(), tempword, "int"));
				result_Buffered.write(tempword+"     <2,整数>\n");
				output_token.write(String.format("z")+System.getProperty("line.separator"));
			}
			else if(tempword.indexOf("e") > -1 || tempword.indexOf(".") > -1) {
				System.out.println(tempword+"     <3,浮点数>");
				valuelist.add(new digit_Value_Table(valuelist.size(), tempword, "float"));
				result_Buffered.write(tempword+"     <3,浮点数>\n");
				output_token.write(String.format("x")+System.getProperty("line.separator"));
			}
		}
		else {
			System.out.println("ERROR: "+tempword+" 错误的数字输入！");
			result_Buffered.write("ERROR: "+tempword+" 错误的数字输入！\n");
		}
	}
	/*
	 * 词法分析主程序，从GUI得到用户输入
	 */
	public void Analyze_Main(String input) {
		String result_File = "result.txt";
		
		String input_File = "tmpCode.txt";
		
		Stack<String> stack1=new Stack<String>();   //  put in {,if } pop
		Stack<String> stack2=new Stack<String>();   //  put in (,if ) pop
		Stack<String> stack3=new Stack<String>();   //  put in [,if ] pop
		
		try {
			BufferedWriter outputtmp=new BufferedWriter(new FileWriter(input_File));
			outputtmp.write(input);
			outputtmp.flush();
			outputtmp.close();
			BufferedWriter result_Buffered = new BufferedWriter(new FileWriter(result_File));
			
			BufferedReader input_Code = new BufferedReader(
					new InputStreamReader(new FileInputStream(input_File)));
			
			String codeLine = null;
			int line_Count = 1;
			boolean isNote = false;
			int begin = 0;
			int end = 0;
			String Note = "";
			
			while((codeLine = input_Code.readLine()) != null) {
				
				System.out.println("Line " + line_Count + ": ");
				result_Buffered.write("Line " + line_Count + ": \n");
				line_Count++;
				
				if(codeLine.matches("^\\s*$")) {
					System.out.println("空行！ ");
					result_Buffered.write("空行！\n ");
				}
				
				//处理注释*****************************************************************>>
				String [] handleNote = handle_Note(codeLine, result_Buffered, isNote, Note);
				codeLine = handleNote[0];
				Note = handleNote[2];
				if(handleNote[1].equals("true")) {
					isNote = true;
				}
				else {
					isNote = false;
				}
				if(handleNote[3].equals("true"))
					continue;
				//处理注释****************************************************************>>
				
				char[] char_Of_Line =codeLine.toCharArray();
				for(int i = 0; i < char_Of_Line.length; i++) {
					String tempStr = "";
					char ch = char_Of_Line[i];
					
					//识别关键字或标识符*****************************************************>>
					if(isLetter(ch)) {
						while((isLetter(ch)||isDigit(ch)) && ch != '\0') {
							tempStr += ch;
							i++;
							if(i == char_Of_Line.length) {
								break;
							}
							ch = char_Of_Line[i];
						}
						int keyWord_pos = keyWord_Symbol.isKey_word(tempStr);
						System.out.println(keyWord_pos);
						handle_biaoshi_keyword(tempStr, keyWord_pos, result_Buffered);
						i--;
						tempStr = "";
					}
					//识别关键字或标识符*****************************************************>>
					
					//识别数字，包括整数，浮点数（科学计数法）*******************************************************>>
					else if(isDigit(ch)) {
						while(ch != '\0' && (isDigit(ch) || ch =='.' ||
								ch == 'e' || (ch == '-' && char_Of_Line[i-1] == 'e')
								||(ch == '+' && char_Of_Line[i-1] == 'e'))) {
							tempStr += ch;
							i++;
							if(i == char_Of_Line.length) {
								break;
							}
							ch =char_Of_Line[i];
						}
						handle_Digit(tempStr, result_Buffered);
						i--;
						tempStr = "";
					}
					//识别数字，包括整数，浮点数（科学计数法）*******************************************************>>
					
					//识别字符串常量
					else if(ch == '\'') {
						boolean isChar = false;
						for(int k = i+1; k < char_Of_Line.length; k++) {
							ch = char_Of_Line[k];
							if(ch == '\'') {
								isChar = true;
								break;
							}
							tempStr += ch;
							i = k+1;
						}
						if(isChar) {
							if(tempStr.length() > 1 || tempStr.length() == 0) {
								System.out.println("ERROR： 字符长度只能是1!");
							}
							else {
								System.out.println(tempStr+"     <4,字符常量>");
								valuelist.add(new digit_Value_Table(valuelist.size(),tempStr,"char"));
								result_Buffered.write(tempStr+"     <4,字符常量> \n");
								output_token.write(String.format("@")+System.getProperty("line.separator"));
							}
						}
						else {
							System.out.println("字符引号不封闭->            \'" + tempStr.charAt(0));
							result_Buffered.write("ERROR:字符串不封闭->         \'" + tempStr.charAt(0));
						
						}
					}
					else if(ch == '"') {
						boolean isString = false;
						for(int  k = i+1; k < char_Of_Line.length; k++) {
							ch = char_Of_Line[k];
							if(ch == '"') {
								isString = true;
								break;
							}
							tempStr += ch;
							i = k + 1;
						}
						if(isString) {
							System.out.println("\""+tempStr+"\""+"     <5,字符串常量>");
//							valuelist.add(new digit_Value_Table(valuelist.size(),tempStr, "String"));
							result_Buffered.write("\""+tempStr+"\""+"     <5,字符串常量>");
							output_token.write(String.format(String.valueOf("$"))+System.getProperty("line.separator"));
						}
						else {
							System.out.println("ERROR:字符串不封闭->       " + tempStr);
							result_Buffered.write("ERROR:字符串不封闭->         " + tempStr);
						}
						tempStr = "";
					}
					else if(ch == '/') {
						tempStr += ch;
						i++;
						if(i == char_Of_Line.length)
							break;
						ch = char_Of_Line[i];
						if(ch != '/' && ch != '*') {
							if(ch == '=') {
								tempStr += ch;
								System.out.println(tempStr+ "   <" + symbolRecord.isMutil_Operation(tempStr) + ", 操作码>");
								result_Buffered.write(tempStr+ "   <" + symbolRecord.isMutil_Operation(tempStr) + ", 操作码>\n");
							}
							else {
								i--;
								System.out.println(tempStr+"     <"+symbolRecord.isSingle_Operation('/')+",操作码>");
								result_Buffered.write((tempStr+"     <"+symbolRecord.isSingle_Operation('/')+",操作码>\n"));
								output_token.write(String.format("/")+System.getProperty("line.separator"));
							}
						}
						else {
							if(ch == '/') {
								i++;
								System.out.println(codeLine.substring(i-2) + "    是注释");
								result_Buffered.write((codeLine.substring(i-2)+"    是注释\n"));
								i = char_Of_Line.length;
							}
						}
						tempStr = "";
					}
					else if(symbolRecord.isSingle_Operation(ch) > 0) {
						if((ch == '+' || ch == '-' || ch == '*' || ch == '=' || ch == '<'
								|| ch == '>'|| ch == '!' || ch == '?') ) {
							tempStr += ch;
							i++;
							if(i > char_Of_Line.length) {
								System.out.println(tempStr+"         <"+symbolRecord.isSingle_Operation(ch)+",操作符>");
								result_Buffered.write(String.format(tempStr+"         <"+symbolRecord.isSingle_Operation(ch)+",操作符>\n"));
								output_token.write(String.format(tempStr+System.getProperty("line.separator")));
								break;
							}
							else if(char_Of_Line[i] == '=') {
								ch = char_Of_Line[i];
								tempStr += ch;
								System.out.println(tempStr + "   <"+symbolRecord.isMutil_Operation(tempStr)
										+ "操作码>");
								result_Buffered.write(tempStr + "   <"+symbolRecord.isMutil_Operation(tempStr)
										+ "操作码>\n");
								output_token.write(String.format(tempStr+System.getProperty("line.separator")));
							}
							else if(char_Of_Line[i] == char_Of_Line[i-1]) {
								ch = char_Of_Line[i];
								tempStr += ch;
								System.out.println(tempStr + "   <"+symbolRecord.isMutil_Operation(tempStr)
										+ "操作码>");
								result_Buffered.write(tempStr + "   <"+symbolRecord.isMutil_Operation(tempStr)
										+ "操作码>\n");
								output_token.write(String.format(tempStr+System.getProperty("line.separator")));
							}
							else {
								i--;
								System.out.println(tempStr+"         <"+symbolRecord.isSingle_Operation(ch)+",操作符>");
								result_Buffered.write(String.format(tempStr+"         <"+symbolRecord.isSingle_Operation(ch)+",操作符>\n"));
								output_token.write(String.format(tempStr+System.getProperty("line.separator")));
							}
							
						}
						else if(ch == '&' || ch == '|') {
							tempStr += ch;
							i++;
							if(i > char_Of_Line.length) {
								System.out.println(tempStr+"         <"+symbolRecord.isSingle_Operation(ch)+",操作符>");
								result_Buffered.write(String.format(tempStr+"         <"+symbolRecord.isSingle_Operation(ch)+",操作符>\n"));
								output_token.write(String.format(tempStr+System.getProperty("line.separator")));
								break;
							}
							else if(char_Of_Line[i] == char_Of_Line[i-1]) {
								ch = char_Of_Line[i];
								tempStr += ch;
								System.out.println(tempStr+"          <"+symbolRecord.isMutil_Operation(tempStr)+",操作符>");
								result_Buffered.write(tempStr+"          <"+symbolRecord.isMutil_Operation(tempStr)+",操作符>\n");
								output_token.write(String.format(tempStr+System.getProperty("line.separator")));
							}
							else {
								i--;
								System.out.println(tempStr+"         <"+symbolRecord.isSingle_Operation(ch)+",操作符>");
								result_Buffered.write(String.format(tempStr+"         <"+symbolRecord.isSingle_Operation(ch)+",操作符>\n"));
								output_token.write(String.format(tempStr+System.getProperty("line.separator")));
							}
						}
						else if(ch == '{') {
							tempStr += ch;
							stack1.push(tempStr);
							System.out.println(tempStr + "        <" + symbolRecord.isSingle_Operation(ch)+ ",界符>");
							result_Buffered.write(tempStr + "        <" + symbolRecord.isSingle_Operation(ch)+ ",界符>\n");
							output_token.write(String.format(tempStr+System.getProperty("line.separator")));
						}
						else if(ch == '}') {
							tempStr += ch;
							if(!stack1.empty()) {
								stack1.pop();
								System.out.println(tempStr + "        <" + symbolRecord.isSingle_Operation(ch) + ",界符>");
								result_Buffered.write(tempStr + "        <" + symbolRecord.isSingle_Operation(ch) + ",界符>\n");
								output_token.write(String.format(tempStr+System.getProperty("line.separator")));
							}
							else if(stack1.empty()) {
								System.out.println("ERROR : {}不匹配");
								result_Buffered.write("ERROR : {}不匹配\n");
							}
						}
						else if( ch == '(') {
							tempStr += ch;
							stack2.push(tempStr);
							System.out.println(tempStr + "        <" + symbolRecord.isSingle_Operation(ch) + ",界符>");
							result_Buffered.write(tempStr + "        <" + symbolRecord.isSingle_Operation(ch) + ",界符>\n");
							output_token.write(String.format(tempStr+System.getProperty("line.separator")));
						}
						else if(ch == ')') {
							tempStr += ch;
							if(!stack2.empty()) {
								stack2.pop();
								System.out.println(tempStr + "        <" + symbolRecord.isSingle_Operation(ch) + ",界符>");
								result_Buffered.write(tempStr + "        <" + symbolRecord.isSingle_Operation(ch) + ",界符>\n");
								output_token.write(String.format(tempStr+System.getProperty("line.separator")));
							}
							else if(stack2.empty()) {
								System.out.println("ERROR : ()不匹配");
								result_Buffered.write("ERROR : ()不匹配\n");
							}
						}
						else if(ch =='[') {
							tempStr += ch;
							stack3.push(tempStr);
							System.out.println(tempStr + "        <" + symbolRecord.isSingle_Operation(ch) + ",界符>");
							result_Buffered.write(tempStr + "        <" + symbolRecord.isSingle_Operation(ch) + ",界符>\n");
							output_token.write(String.format(tempStr+System.getProperty("line.separator")));
						}
						else if(ch == ']') {
							tempStr += ch;
							if(!stack3.empty()) {
								stack3.pop();
								System.out.println(tempStr + "        <" + symbolRecord.isSingle_Operation(ch) + ",界符>");
								result_Buffered.write(tempStr + "        <" + symbolRecord.isSingle_Operation(ch) + ",界符>\n");
								output_token.write(String.format(tempStr+System.getProperty("line.separator")));
							}
							else if(stack3.empty()) {
								System.out.println("ERROR : ()不匹配");
								result_Buffered.write("ERROR : ()不匹配\n");
							}
						}
						else {
							tempStr += ch;
							System.out.println(tempStr + "        <" + symbolRecord.isSingle_Operation(ch)+ ",界符>");
							result_Buffered.write(tempStr + "        <" + symbolRecord.isSingle_Operation(ch)+ ",界符>\n");
							output_token.write(String.format(tempStr+System.getProperty("line.separator")));
						}
					}
					else {
						if(ch != ' ' && ch != '\t') {
							System.out.printf("%-10c ERROR:\n", ch);
							result_Buffered.write(ch + "ERROR:\n");
						}
					}
				}
				result_Buffered.write("\n");
				
			}
			if(isNote) {
				System.out.println("注释不封闭");
				result_Buffered.write("ERROR：    注释不封闭");
				
			}
					
			result_Buffered.flush();
			result_Buffered.close();
			output_token.flush();
			output_token.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}
