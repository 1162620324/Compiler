package UI;

import java.io.IOException;


import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import Syntax_Analyze.Syntax_Main;
import morphology_Analyze.morphology_Analyze;
import translator.*;

//import com.cloudgarden.resource.SWTResourceManager;
 
public class Morphology_GUI extends org.eclipse.swt.widgets.Composite{

	private Button transform;
	private Button Syntax_Analyze;
	private Button Moephology_Button;
	private Text inputCode;
	private Label tiltle;
	String analyze_Result="";
	ReadFile readFile = new ReadFile();
	Syntax_Main syn = new Syntax_Main();

	
	
	public static void main(String[] args) {
		Display display = Display.getDefault();
		Shell shell = new Shell(display);
		Morphology_GUI inst = new Morphology_GUI(shell, SWT.NULL);
		Point size = inst.getSize();
		shell.setLayout(new FillLayout());
		shell.layout();
		if(size.x == 0 && size.y == 0) {
			inst.pack();
			shell.pack();
		} else {
			Rectangle shellBounds = shell.computeTrim(0, 0, size.x, size.y);
			shell.setSize(shellBounds.width, shellBounds.height);
		}
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

	}
	
	public Morphology_GUI(Composite parent, int style) {
		super(parent, style);
		initGUI();
	}
	/*
	 * 初始化GUI界面
	 */
	private void initGUI() {
		try {
			this.setSize(800, 600);
			this.setRedraw(true);
			//setTitle("");
//			this.setBackground();;
			FormLayout thisLayout = new FormLayout();
			this.setLayout(thisLayout);
			
			{
				Moephology_Button = new Button(this, SWT.PUSH | SWT.CENTER);
				FormData CFanalyzeLData = new FormData();
				CFanalyzeLData.left =  new FormAttachment(0, 1000, 650);
				CFanalyzeLData.top =  new FormAttachment(0, 1000, 100);
				CFanalyzeLData.width = 98;
				CFanalyzeLData.height = 35;
				Moephology_Button.setLayoutData(CFanalyzeLData);
				Moephology_Button.setText("\u8bcd\u6cd5\u5206\u6790");
			}
			{
				transform = new Button(this, SWT.PUSH | SWT.CENTER);
				FormData transformLData = new FormData();
				transformLData.left =  new FormAttachment(0, 1000, 650);
				transformLData.top =  new FormAttachment(0, 1000, 200);
				transformLData.width = 98;
				transformLData.height = 35;
				transform.setLayoutData(transformLData);
				transform.setText("token");
			}
			{
				Syntax_Analyze = new Button(this, SWT.PUSH | SWT.CENTER);
				FormData YFanalyzeLData = new FormData();
				YFanalyzeLData.left =  new FormAttachment(0, 1000, 650);
				YFanalyzeLData.top =  new FormAttachment(0, 1000, 300);
				YFanalyzeLData.width = 98;
				YFanalyzeLData.height = 35;
				Syntax_Analyze.setLayoutData(YFanalyzeLData);
				Syntax_Analyze.setText("\u8bed\u6cd5\u5206\u6790");
			}
			{
				tiltle = new Label(this, SWT.NONE);
				FormData label1LData = new FormData();
				label1LData.left =  new FormAttachment(0, 1000, 200);
				label1LData.top =  new FormAttachment(0, 1000,6);
				label1LData.width = 300;
				label1LData.height = 50;
				tiltle.setLayoutData(label1LData);
				tiltle.setText("          语法分析器");
				tiltle.setFont(new Font(Display.getCurrent(), "Cursive",20,SWT.NORMAL));
				tiltle.setForeground(new Color(Display.getCurrent(), 59, 49, 30));
//				tiltle.setBackground(SWTResourceManager.getColor(253, 245, 230));
			}
			{
				inputCode = new Text(this, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
				FormData CodeLData = new FormData();
				CodeLData.left =  new FormAttachment(0, 1000, 14);
				CodeLData.top =  new FormAttachment(0, 1000, 50);
				CodeLData.width = 600;
				CodeLData.height = 800;
				inputCode.setLayoutData(CodeLData);
				inputCode.setBackground(new Color(Display.getCurrent(), 240, 240, 240));
				inputCode.setForeground(new Color(Display.getCurrent(), 200, 149, 0));
				inputCode.setFont(new Font(Display.getCurrent(), "Cursive",16,SWT.NORMAL));
				inputCode.setText("");
			}
			transform.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					String neelu=inputCode.getText();
					inputCode.setText(Translate.translateIt(neelu));
					System.out.println(inputCode.getText());
				}
			});
			Syntax_Analyze.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					
					String neelu=inputCode.getText();
					try {
						String getRES=syn.syntax_Main(neelu);
						if(getRES.charAt(0)=='H'){
							inputCode.setForeground(new Color(Display.getCurrent(), 113, 215, 113));
					    }
						else{
							inputCode.setForeground(new Color(Display.getCurrent(), 223, 0, 0));
						}
						inputCode.setText(syn.syntax_Main(neelu));
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
				}
			});
			Moephology_Button.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
//					inputCode.setForeground(new Color(Display.getCurrent(), 159, 149, 222));
//					String neelu=inputCode.getText();
//					morphology_Analyze cfanalyze=new morphology_Analyze();
//					cfanalyze.Analyze_Main(neelu);
//					try {
//						inputCode.setText(readFile.readResult());
//					} catch (IOException e1) {
//						e1.printStackTrace();
//					}
					inputCode.setForeground(new Color(Display.getCurrent(), 159, 149, 222));
					String neelu=inputCode.getText();
					morphology_Analyze cfanalyze=new morphology_Analyze();
					cfanalyze.Analyze_Main(neelu);
					try {
						inputCode.setText(readFile.readResult());
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			});
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
