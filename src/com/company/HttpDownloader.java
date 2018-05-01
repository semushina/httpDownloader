package com.company;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HttpDownloader {
    private String outputFolder;
    private int threads = 1;

    HttpDownloader(){
        outputFolder = new File("").getAbsolutePath();
    }

    HttpDownloader(String outputFolder){
        this.outputFolder = new File(outputFolder).isAbsolute() ? outputFolder : new File("").getAbsolutePath() + "/" + outputFolder;
        new File(outputFolder).mkdirs();
    }

    public int setThreads(int threads){
        this.threads = threads;
        return 0;
    }

    public int downloadFiles(Map<String, List<String>> urls) {
        if(urls == null) return -1;
        ExecutorService executorService = Executors.newFixedThreadPool(threads);
        for(Map.Entry<String, List<String>> entry : urls.entrySet()){
            executorService.execute(new DownloadHelper(entry.getKey(), entry.getValue()));
        }
        executorService.shutdown();
        try{
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return 0;
    }

    public int downloadFile(String url, List<String> fnames) {
        if(url == null || fnames == null || fnames.isEmpty())
            return -1;
        try {
            TimeUnit.SECONDS.sleep(10);
            URL urlObj = new URL(url);
            ReadableByteChannel rbc = Channels.newChannel(urlObj.openStream());
            String fname = fnames.get(0);
            FileOutputStream fileOutputStream = new FileOutputStream(outputFolder+"/"+fname);
            fileOutputStream.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            fileOutputStream.close();
            if(fnames.size() > 1){
                for(int i=1; i<fnames.size(); i++)
                    Files.copy(new File(outputFolder+"/"+fname).toPath(), new File(outputFolder+"/"+fnames.get(i)).toPath());
            }
        } catch (Exception ex) {
            System.out.println("fail: " + ex.getMessage());
            ex.printStackTrace();
            return -1;
        }
        return 0;
    }

    public int downloadFile(String url, String fname) {
        return downloadFile(url, Arrays.asList(new String[]{fname}));
    }

    public int downloadFile(String url){
        try{
            return downloadFile(url, Paths.get(new URL(url).getFile()).getFileName().toString());
        }
        catch (Exception ex){
            System.out.println("fail to get file name");
            ex.printStackTrace();
        }
        return -1;
    }

    private class DownloadHelper implements Runnable{
        String url=null;
        List<String> fnames=null;

        DownloadHelper(String url, List<String> fnames){
            this.url = url;
            this.fnames = fnames;
        }

        @Override
        public void run() {
            downloadFile(url, fnames);
        }
    }
}
