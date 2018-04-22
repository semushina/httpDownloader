package com.company;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) {
        if(args.length != 3){
            System.out.println("Wrong arguments count "+args.length);
            return;
        }

        int threads;
        try{
            threads = Integer.parseInt(args[0]);
        }
        catch (Exception ex){
            System.out.println("Threads number must be integer");
            return;
        }
        if(threads < 1){
            System.out.println("Wrong threads number");
            return;
        }

        Map<String, List<String>> urls = readLinks(args[2]);
        if(urls == null){
            System.out.println("Wrong URL list file");
            return;
        }
        if(urls.size() == 0){
            System.out.println("URL list is empty");
            return;
        }
//        System.out.println(urls.toString());

        HttpDownloader httpDownloader = new HttpDownloader(args[1]);
        httpDownloader.setThreads(threads);
        httpDownloader.downloadFiles(urls);
    }

    private static Map<String, List<String>> readLinks(String fname){
        File file = new File(fname);
        if(!file.exists() || file.isDirectory()) return null;

        Map<String, List<String>> list = new HashMap<>();

        try(BufferedReader br = new BufferedReader(new FileReader(file))){
            String line;
            Pattern pt = Pattern.compile("(.*)\\s+(.*)");

            while((line = br.readLine()) != null){
                Matcher m = pt.matcher(line);
                if(!m.find()) continue;
                List<String> fnames = list.get(m.group(1));
                if(fnames == null) fnames = new ArrayList<>();
                fnames.add(m.group(2));
                list.put(m.group(1), fnames);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    private void printConnectionInfo(HttpURLConnection httpURLConnection) throws IOException {
        System.out.println("Connection info: ");
        System.out.println(httpURLConnection.getResponseCode());
        System.out.println(httpURLConnection.getContentLength());

        for(Map.Entry<String, List<String>> header : httpURLConnection.getHeaderFields().entrySet())
            System.out.println(header.getKey() + " = " + header.getValue());
    }

    private void printUrlInfo(URL url) {
        System.out.println(url.getHost());
        System.out.println(url.getPath());
        System.out.println(url.getPort());
        System.out.println(url.getProtocol());
        System.out.println(url.getQuery());


    }
}
