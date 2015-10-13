package analyzer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;

import model.Clone;

import org.apache.commons.lang3.StringEscapeUtils;

import parser.CompareParser;

public class Viewer {
	public void newClone(CompareParser cParser){
		LinkedList<Clone> dnewClones = new LinkedList<Clone>();
		LinkedList<Clone> cnewClones = new LinkedList<Clone>();
		dnewClones = cParser.getDnewClones();
		cnewClones = cParser.getCnewClones();
		Clone dclone = new Clone();
		Clone cclone = new Clone();
		boolean bgflag = false;
		int tmp;
		int count=1;
		int linenum;
		String location;
		String str;
		int start;
		int end;

		try {
			File file = new File("C:/cygwin64/home/y-yusuke/simian/bin/Result/HTML/index.html");
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			pw.println("<html>");
			pw.println("<body>");
			pw.println("	<ul>");
			pw.println("	<table border='3'>");
			pw.println("	<tr bgcolor='yellow'>");
			pw.println("	<th>Id</th>");
			pw.println("	<th>Decompile</th>");
			pw.println("	<th>Origin</th>");
			pw.println("	<th>Compare</th>");
			pw.println("	</tr>");

			tmp = dnewClones.get(0).getId();
			for(int i=0 ; i < dnewClones.size() ; i++){
				dclone = dnewClones.get(i);
				cclone = cnewClones.get(i);
				if(tmp == dclone.getId()){
					/*
					 * index.html生成
					 */
					if(bgflag) pw.println("	<tr bgcolor='#e0e0e0'>");
					else pw.println("	<tr bgcolor='white'>");
					pw.println("		<td>Code Clone" + tmp + "-" + count + "</td>");
					pw.print("		<td><a href=" + "data/newclone/" + tmp + "-" + count + "(decompile).html>");
					pw.println(dclone.getFilename() +"</a></td>");
					pw.print("		<td><a href=" + "data/newclone/" + tmp + "-" + count + "(origin).html>");
					pw.println(cclone.getFilename() +"</a></td>");
					pw.print("		<td><a href=" + "data/newclone/" + tmp + "-" + count + ".html>");
					pw.println(tmp + "-" + count +"</a></td>");
					pw.println("	</tr>");

					/*
					 * ソースコード(decompile).html生成
					 */
					File file2 = new File("C:/cygwin64/home/y-yusuke/simian/bin/Result/HTML/data/newclone/" + tmp + "-" + count + "(decompile).html");
					PrintWriter pw2 = new PrintWriter(new BufferedWriter(new FileWriter(file2)));
					pw2.println("<html>");
					pw2.println("<head>");
					pw2.println("	<script src='../scripts/shCore.js' type='text/javascript'></script>");
					pw2.println("	<script src='../scripts/shBrushJava.js' type='text/javascript'></script>");
					pw2.println("	<link href='../styles/shCoreDefault.css' rel='stylesheet' type='text/css'>");
					pw2.println("	<script type='text/javascript'>SyntaxHighlighter.all();</script>");
					pw2.println("</head>");
					pw2.println("<body>");
					pw2.print("	<pre class='brush: java; ruler: true; first-line: 1; highlight: [");
					start= dclone.getStart();
					end = dclone.getEnd();
					for(int j = start ; j <= end ; j++){
						pw2.print(j);
						if(j != end) pw2.print(",");
					}
					pw2.println("]'>");

					//ソースコード(decompile)本文
					location = dclone.getLocation();
					File readfile = new File(location);
					BufferedReader br = new BufferedReader(new FileReader(readfile));

					str = br.readLine();
					linenum = 1;
					while(str != null){
						//if(start == linenum) pw2.println("<a name= \"label\" />");
						StringEscapeUtils.escapeHtml4(str);
						pw2.println(str);
						str = br.readLine();
						linenum++;
					}
					br.close();

					pw2.println("	</pre>");
					pw2.println("<body>");
					pw2.close();

					/*
					 * ソースコード(origin).html生成
					 */
					File file3 = new File("C:/cygwin64/home/y-yusuke/simian/bin/Result/HTML/data/newclone/" + tmp + "-" + count + "(origin).html");
					PrintWriter pw3 = new PrintWriter(new BufferedWriter(new FileWriter(file3)));
					pw3.println("<html>");
					pw3.println("<head>");
					pw3.println("	<script src='../scripts/shCore.js' type='text/javascript'></script>");
					pw3.println("	<script src='../scripts/shBrushJava.js' type='text/javascript'></script>");
					pw3.println("	<link href='../styles/shCoreDefault.css' rel='stylesheet' type='text/css'>");
					pw3.println("	<script type='text/javascript'>SyntaxHighlighter.all();</script>");
					pw3.println("</head>");
					pw3.println("<body>");
					pw3.print("	<pre class='brush: java; ruler: true; first-line: 1; highlight: [");
					start= cclone.getStart();
					end = cclone.getEnd();
					for(int j = start ; j <= end ; j++){
						pw3.print(j);
						if(j != end) pw3.print(",");
					}
					pw3.println("]'>");

					//ソースコード(origin)本文
					location = cclone.getLocation();
					File readfile2 = new File(location);
					BufferedReader br2 = new BufferedReader(new FileReader(readfile2));

					str = br2.readLine();
					linenum = 1;
					while(str != null){
						//if(start == linenum) pw3.println("<a name='label' />");
						StringEscapeUtils.escapeHtml4(str);
						pw3.println(str);
						str = br2.readLine();
						linenum++;
					}
					br2.close();

					pw3.println("	</pre>");
					pw3.println("<body>");
					pw3.close();

					/*
					 * 比較用html生成
					 */
					File file6 = new File("C:/cygwin64/home/y-yusuke/simian/bin/Result/HTML/data/newclone/" + tmp + "-" + count + ".html");
					PrintWriter pw6 = new PrintWriter(new BufferedWriter(new FileWriter(file6)));
					pw6.println("<HTML>");
					pw6.println("	<HEAD></HEAD>");
					pw6.println("	<FRAMESET COLS='50%,50%'>");
					pw6.println("	<FRAME SRC='" +  tmp + "-" + count + "(decompile).html' SCROLLING='YES'>");
					pw6.println("	<FRAME SRC='" +  tmp + "-" + count + "(origin).html' SCROLLING='YES'>");
					pw6.println("	</FRAMESET>");
					pw6.println("</HTML>");
					pw6.close();

					count++;

				}else{
					tmp = dclone.getId();
					count = 1;

					/*
					 * index.html生成
					 */
					if(bgflag){
						pw.println("	<tr bgcolor='white'>");
						bgflag = false;
					}else{
						pw.println("	<tr bgcolor='#e0e0e0'>");
						bgflag = true;
					}
					pw.println("		<td>Code Clone" + tmp + "-" + count + "</td>");
					pw.print("		<td><a href=" + "data/newclone/" + tmp + "-" + count + "(decompile).html>");
					pw.println(dclone.getFilename() +"</a></td>");
					pw.print("		<td><a href=" + "data/newclone/" + tmp + "-" + count + "(origin).html>");
					pw.println(cclone.getFilename() +"</a></td>");
					pw.print("		<td><a href=" + "data/newclone/" + tmp + "-" + count + ".html>");
					pw.println(tmp + "-" + count +"</a></td>");
					pw.println("	</tr>");

					/*
					 * ソースコード(decompile).html生成
					 */
					File file4 = new File("C:/cygwin64/home/y-yusuke/simian/bin/Result/HTML/data/newclone/" + tmp + "-" + count + "(decompile).html");
					PrintWriter pw4 = new PrintWriter(new BufferedWriter(new FileWriter(file4)));
					pw4.println("<html>");
					pw4.println("<head>");
					pw4.println("	<script src='../scripts/shCore.js' type='text/javascript'></script>");
					pw4.println("	<script src='../scripts/shBrushJava.js' type='text/javascript'></script>");
					pw4.println("	<link href='../styles/shCoreDefault.css' rel='stylesheet' type='text/css'>");
					pw4.println("	<script type='text/javascript'>SyntaxHighlighter.all();</script>");
					pw4.println("</head>");
					pw4.println("<body>");
					pw4.print("	<pre class='brush: java; ruler: true; first-line: 1; highlight: [");
					start= dclone.getStart();
					end = dclone.getEnd();
					for(int j = start ; j <= end ; j++){
						pw4.print(j);
						if(j != end) pw4.print(",");
					}
					pw4.println("]'>");

					//ソースコード(decompile)本文
					location = dclone.getLocation();
					File readfile = new File(location);
					BufferedReader br = new BufferedReader(new FileReader(readfile));

					str = br.readLine();
					linenum = 1;
					while(str != null){
						//if(start == linenum) pw4.println("<a name='label' />");
						StringEscapeUtils.escapeHtml4(str);
						pw4.println(str);
						str = br.readLine();
						linenum++;
					}
					br.close();

					pw4.println("	</pre>");
					pw4.println("<body>");
					pw4.close();

					/*
					 * ソースコード(origin).html生成
					 */
					File file5 = new File("C:/cygwin64/home/y-yusuke/simian/bin/Result/HTML/data/newclone/" + tmp + "-" + count + "(origin).html");
					PrintWriter pw5 = new PrintWriter(new BufferedWriter(new FileWriter(file5)));
					pw5.println("<html>");
					pw5.println("<head>");
					pw5.println("	<script src='../scripts/shCore.js' type='text/javascript'></script>");
					pw5.println("	<script src='../scripts/shBrushJava.js' type='text/javascript'></script>");
					pw5.println("	<link href='../styles/shCoreDefault.css' rel='stylesheet' type='text/css'>");
					pw5.println("	<script type='text/javascript'>SyntaxHighlighter.all();</script>");
					pw5.println("</head>");
					pw5.println("<body>");
					pw5.print("	<pre class='brush: java; ruler: true; first-line: 1; highlight: [");
					start= cclone.getStart();
					end = cclone.getEnd();
					for(int j = start ; j <= end ; j++){
						pw5.print(j);
						if(j != end) pw5.print(",");
					}
					pw5.println("]'>");

					//ソースコード(origin)本文
					location = cclone.getLocation();
					File readfile2 = new File(location);
					BufferedReader br2 = new BufferedReader(new FileReader(readfile2));

					str = br2.readLine();
					linenum = 1;
					while(str != null){
						//if(start == linenum) pw5.println("<a name='label' />");
						StringEscapeUtils.escapeHtml4(str);
						pw5.println(str);
						str = br2.readLine();
						linenum++;
					}
					br2.close();

					pw5.println("	</pre>");
					pw5.println("<body>");
					pw5.close();

					/*
					 * 比較用html生成
					 */
					File file7 = new File("C:/cygwin64/home/y-yusuke/simian/bin/Result/HTML/data/newclone/" + tmp + "-" + count + ".html");
					PrintWriter pw7 = new PrintWriter(new BufferedWriter(new FileWriter(file7)));
					pw7.println("<HTML>");
					pw7.println("	<HEAD></HEAD>");
					pw7.println("	<FRAMESET COLS='50%,50%'>");
					pw7.println("	<FRAME SRC='" +  tmp + "-" + count + "(decompile).html' SCROLLING='YES'>");
					pw7.println("	<FRAME SRC='" +  tmp + "-" + count + "(origin).html' SCROLLING='YES'>");
					pw7.println("	</FRAMESET>");
					pw7.println("</HTML>");
					pw7.close();

					count++;
				}
			}
			pw.println("	</table>");
			pw.println("	</ul>");
			pw.println("</body>");
			pw.close();
		} catch (FileNotFoundException e) {
			System.out.println(e);
		} catch (IOException e) {
			System.out.println(e);
		}
	}
}
