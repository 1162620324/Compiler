package Syntax_Analyze;

import java.util.*;

public class project2state {
	List<Production_Proj> project_Set = new ArrayList<Production_Proj>();//项目集闭包
	int name;//状态号
	
	public project2state(List<Production_Proj> project_Set, int name) {
		this.project_Set = project_Set;
		this.name = name;
	}
}
