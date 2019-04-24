package Syntax_Analyze;

public class Production_Proj {
	
	public String LRtext;
	public char expected;
	public Production_Proj(String str, char expected) {
		this.LRtext = str;
		this.expected = expected;
	}
}
