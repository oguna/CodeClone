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

public class RejectCloneViewer {
	public void rejectClone(CompareParser cParser){
		LinkedList<Clone> rejectClones = new LinkedList<Clone>();
		rejectClones = cParser.getRejectClones();
		Clone rclone = new Clone();
		boolean bgflag = false;
		int tmp;
		int count=1;

		try {
			File file = new File("C:/cygwin64/home/y-yusuke/simian/bin/Result/HTML/reject.html");
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			pw.println("<html>");
			pw.println("<body>");
			pw.println("	<ul>");
			pw.println("	<table border='3'>");
			pw.println("	<tr bgcolor='yellow'>");
			pw.println("	<th>Id</th>");
			pw.println("	<th>Origin</th>");
			pw.println("	<th>Decompile</th>");
			pw.println("	<th>Compare</th>");
			pw.println("	</tr>");

			tmp = rejectClones.get(0).getId();
			for(int i=0 ; i < rejectClones.size() ; i++){
				rclone = rejectClones.get(i);
				if(tmp == rclone.getId()){
					//index.html配色
					if(bgflag) pw.println("	<tr bgcolor='#e0e0e0'>");
					else pw.println("	<tr bgcolor='white'>");
					//各html生成
					generate(pw,tmp,count,rclone);
					count++;
				}else{
					tmp = rclone.getId();
					count = 1;
					// index.html配色
					if(bgflag){
						pw.println("	<tr bgcolor='white'>");
						bgflag = false;
					}else{
						pw.println("	<tr bgcolor='#e0e0e0'>");
						bgflag = true;
					}
					//各html生成
					generate(pw,tmp,count,rclone);
					count++;
				}
			}
			pw.println("	</table>");
			pw.println("	</ul>");
			pw.println("</body>");
			pw.println("</html>");
			pw.close();
		} catch (FileNotFoundException e) {
			System.out.println(e);
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public void generate(PrintWriter pw,int tmp ,int count,Clone rclone){
		int linenum;
		String location;
		String str;
		int start;
		int end;

		try {
			/*
			 * index.html生成
			 */
			pw.println("		<td>Code Clone" + tmp + "-" + count + "</td>");
			pw.print("		<td><a href=" + "data/reject/" + tmp + "-" + count + "(origin).html>");
			pw.println(rclone.getFilename() +"</a></td>");

			/*
			 * ソースコード(origin).html生成
			 */
			File file2 = new File("C:/cygwin64/home/y-yusuke/simian/bin/Result/HTML/data/reject/" + tmp + "-" + count + "(origin).html");
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
			start= rclone.getStart();
			end = rclone.getEnd();
			for(int j = start ; j <= end ; j++){
				pw2.print(j);
				if(j != end) pw2.print(",");
			}
			pw2.println("]'>");

				//ソースコード(origin)本文
				location = rclone.getLocation();
				File readfile = new File(location);
				BufferedReader br = new BufferedReader(new FileReader(readfile));

				str = br.readLine();
				linenum = 1;
				while(str != null){
					if(start == linenum) pw2.println(StringEscapeUtils.escapeHtml4("<a name= \"label\" />"));
					str = StringEscapeUtils.escapeHtml4(str);
					pw2.println(str);
					str = br.readLine();
					//linenum++;
				}
				br.close();

			pw2.println("	</pre>");
			pw2.println("</body>");
			pw2.println("</html>");
			pw2.close();

			/*
			 * ソースコード(decompile).html生成
			 */
			location = location.replace("src/main","src2");
			File decompilefile = new File(location);
	        if (decompilefile.exists()) {
				File file3 = new File("C:/cygwin64/home/y-yusuke/simian/bin/Result/HTML/data/reject/" + tmp + "-" + count + "(decompile).html");
				PrintWriter pw3 = new PrintWriter(new BufferedWriter(new FileWriter(file3)));
				pw.print("		<td><a href=" + "data/reject/" + tmp + "-" + count + "(decompile).html>");
				pw.println(rclone.getFilename() +"</a></td>");

				pw3.println("<html>");
				pw3.println("<head>");
				pw3.println("	<script src='../scripts/shCore.js' type='text/javascript'></script>");
				pw3.println("	<script src='../scripts/shBrushJava.js' type='text/javascript'></script>");
				pw3.println("	<link href='../styles/shCoreDefault.css' rel='stylesheet' type='text/css'>");
				pw3.println("	<script type='text/javascript'>SyntaxHighlighter.all();</script>");
				pw3.println("</head>");
				pw3.println("<body>");
				pw3.println("	<pre class='brush: java; ruler: true; first-line: 1'>");

					//ソースコード(decompile)本文
					BufferedReader br2 = new BufferedReader(new FileReader(decompilefile));
					str = br2.readLine();
					//linenum = 1;
					while(str != null){
						//if(start == linenum) pw3.println("<a name='label' />");
						str = StringEscapeUtils.escapeHtml4(str);
						pw3.println(str);
						str = br2.readLine();
						//linenum++;
					}
					br2.close();

				pw3.println("	</pre>");
				pw3.println("</body>");
				pw3.println("</html>");
				pw3.close();

				/*
				 * 比較用html生成
				 */
				pw.print("		<td><a href=" + "data/reject/" + tmp + "-" + count + ".html>");
				pw.println(tmp + "-" + count +"</a></td>");
				pw.println("	</tr>");

				File file4 = new File("C:/cygwin64/home/y-yusuke/simian/bin/Result/HTML/data/reject/" + tmp + "-" + count + ".html");
				PrintWriter pw4 = new PrintWriter(new BufferedWriter(new FileWriter(file4)));
				pw4.println("<HTML>");
				pw4.println("	<HEAD></HEAD>");
				pw4.println("	<FRAMESET COLS='50%,50%'>");
				pw4.println("	<FRAME SRC='" +  tmp + "-" + count + "(origin).html' SCROLLING='YES'>");
				pw4.println("	<FRAME SRC='" +  tmp + "-" + count + "(decompile).html' SCROLLING='YES'>");
				pw4.println("	</FRAMESET>");
				pw4.println("</HTML>");
				pw4.close();
	        } else {
	        	pw.print("		<td>-</td>");
				pw.print("		<td>-</td>");
				pw.println("	</tr>");
	        }
		} catch (FileNotFoundException e) {
			System.out.println(e);
		} catch (IOException e) {
			System.out.println(e);
		}
	}
}
