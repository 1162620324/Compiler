package com.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import javax.swing.JOptionPane;

public class Test {
	public static String[][] action = null; //分析表
	
	public static void main(String[] args) {
		File inputFile = new File("test.txt");
		try {
			BufferedReader bReader = new BufferedReader(
					new InputStreamReader(new FileInputStream(inputFile), "utf-8"));
			String text = "";
			while(bReader.ready()) {
				String line = bReader.readLine();  // 按行读入
				text += line + "\n";
			}
			bReader.close();
			
			System.out.println("\n\n------*-*------*-*------*-*---词法分析---*-*------*-*------*-*------\n");
			WordAnalyse wordAnalyse1 = new WordAnalyse(text, true);
			wordAnalyse1.analyse();
			wordAnalyse1.printTbmodel_lex_result();
			//wordAnalyse1.printTbmodel_DFA_result();
			wordAnalyse1.printTbmodel_wrong_result();
			
			System.out.println("\n\n------*-*------*-*------*-*---语法分析---*-*------*-*------*-*------\n");
			GetProduction getpro = new GetProduction();
			getpro.getPro("Grammar.txt"); //读入文法
			WordAnalyse wordAnalyse2 = new WordAnalyse(text, false);
			wordAnalyse2.analyse();
			LALRTable lalr = new LALRTable(getpro.proList, getpro.graSymSet, getpro.firsts);
			action = lalr.getLALRTable();
			
			System.out.println("\n\n------*-*------*-*------*-*---语义分析---*-*------*-*------*-*------\n");
			SemanticAnalyse seA = new SemanticAnalyse(wordAnalyse2.token, getpro.proList, action);
			seA.analyse();
			seA.print3Code();
			seA.print4Code();
			seA.printTbmodel_wrong_result();
			seA.printTbmodel_sym();
				
		}catch (FileNotFoundException e) {
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
