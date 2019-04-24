 package morphology_Analyze;

public class keyWord_Symbol {
	public String name;
	public int token;
	
	public keyWord_Symbol() {
	}
	
	public keyWord_Symbol(String name, int token) {
		this.name = name;
		this.token = token;
	}
	
	public static keyWord_Symbol[] newRecord() {
		keyWord_Symbol [] record = new keyWord_Symbol[48];
		String keys[] = {"int","float","char","boolean","string","void","do","while","return","true","false","scanner","println",
				"if","else","main","+", "-","*", "=", "<", ">",  "!", "?","(", ")", "[", "]", "{", "}", ";", ",","/","==","&",
				"|",">=","<=","!=",":","+=","-=","*=","/=","&&","||"};
		for(int i = 0; i < keys.length; i++) {
			record[i] = new keyWord_Symbol(keys[i], i+6);
		}
		
		return record;
	}
	
	public static int isKey_word(String str) {
		keyWord_Symbol[] record = newRecord();
		for(int i = 0; i < 16; i++) {
			if(str.equals(record[i].name)) {
				return record[i].token;
			}
		}
		return -1;
	}
	
	public int isSingle_Operation(char ch) {
		String str = String.valueOf(ch);
		keyWord_Symbol[] record = newRecord();
		for(int i = 15; i < 46; i++) {
			if(str.equals(record[i].name)) {
				return record[i].token;
			}
		}
		return -1;
	}
	
	public int isMutil_Operation(String ch) {
		keyWord_Symbol[] record = newRecord();
		for(int i = 14; i < 46; i++) {
			if(ch.equals(record[i].name)) {
				return record[i].token;
			}
		}
		return -1;
	}
	
	
}
