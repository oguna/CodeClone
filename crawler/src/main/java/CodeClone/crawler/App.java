package CodeClone.crawler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class App {
	public static int page = 10;
	public static int count = 1;
	public static int hasNext = 0;
	public static String url = null;
	public static String org = null;
	public static String remove1 = "/users";
	public static String remove2 = "/organizations";
	public static String remove3 = "/repositories";
	public static String remove4 = "#";
	public static String remove5 = "http://githubranking.com/";
	public static String remove6 = "https://github";
	public static LinkedList<String> URLlist = new LinkedList<String>();
	public static LinkedList<String> ORGlist = new LinkedList<String>();

	public static void main(String[] args) throws IOException{
		// TODO 自動生成されたメソッド・スタブ
		Repositories();
		Organizations();
	}

	public static void Repositories() throws IOException{
		File file = new File("Repositories2.sh");
		FileWriter fw = new FileWriter(file);
		url = "http://githubranking.com/repositories";

		fw.write("#!/bin/sh");fw.write("\n");
		fw.write("cd ../../cygdrive/e");fw.write("\n");

		while(count <= page){
			Remove(url,fw,1);
			url = "http://githubranking.com/repositories?page=";
			count++;
			url = url + String.valueOf(count);
		}fw.write("cd");fw.write("\n");fw.close();
		count = 1;
	}

	public static void Organizations() throws IOException{
		File file = new File("Organizations.sh");
		FileWriter fw = new FileWriter(file);
		url = "http://githubranking.com/organizations";

		fw.write("#!/bin/sh");fw.write("\n");
		fw.write("cd ../../cygdrive/e");fw.write("\n");

		URLlist = getOrganizationList(url);
		getOrganizationRepositories(fw);

		fw.write("cd");fw.write("\n");fw.close();
		count = 1;
	}

	public static LinkedList<String> getOrganizationList(String url) throws IOException{
		while(count <= page){
			Remove(url,null,2);
			url = "http://githubranking.com/organizations?page=";
			count++;
			url = url + String.valueOf(count);
		}count = 1;
		return URLlist;
	}

	public static void Remove(String url,FileWriter fw,int flag) throws IOException{
		Document document = Jsoup.connect(url).get();
		org.jsoup.select.Elements links = document.getElementsByTag("a");
		for(Element link : links){
			String linkHref = link.attr("href");
			if(linkHref.indexOf(remove1) == -1 && linkHref.indexOf(remove2) == -1 && linkHref.indexOf(remove3) == -1 &&
				linkHref.indexOf(remove4) == -1 && linkHref.indexOf(remove5) == -1 && linkHref.indexOf(remove6) == -1){
				if(flag == 1){
					fw.write("git clone ");
					fw.write("https://github.com");
					fw.write(linkHref);
					fw.write("\n");
				}else if(flag == 2){
					URLlist.add("http://githubranking.com" + linkHref);
					ORGlist.add(linkHref + "/");
				}
				else break;}}
	}

	public static void getOrganizationRepositories(FileWriter fw) throws IOException{
		for(String url : URLlist){
			org = ORGlist.poll();
			count = 1;

			if(org.equals("/pld-linux/")) continue;

			while(true){
				Document document = Jsoup.connect(url).timeout(10000).get();
				org.jsoup.select.Elements links = document.getElementsByTag("a");
				for(Element link : links){
					String linkHref = link.attr("href");
					String linkrel = link.attr("rel");
					if(linkHref.indexOf(org) != -1){
						fw.write("git clone ");
						fw.write("https://github.com" + linkHref);
						fw.write("\n");
					}
					if(linkrel.equals("next")) hasNext = 1;
				}
				if(hasNext == 1){
					url = "http://githubranking.com" + org + "?page=";
					count++;
					url = url + String.valueOf(count);
					hasNext = 0;
				}else break;
			}
		}
		URLlist.clear();
	}

}
