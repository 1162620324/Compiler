package UI;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class ReadFile {
	
	/*
	 * 辅助文件读取函数
	 */
	public String readResult() throws IOException{
		String input="result_token.txt";

		FileInputStream incode=new FileInputStream(input);
		BufferedReader strcode=new BufferedReader(new InputStreamReader(incode));	
		String line="";
		String result="";
		while((line=strcode.readLine())!=null){
			result+=line+'\n';
		}
		return result;
	}
}
