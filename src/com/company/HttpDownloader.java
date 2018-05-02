package com.company;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
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
    private int cntDownloadedFiles = 0;
    private long cntDownloadedBytes = 0;

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
        cntDownloadedFiles = 0;
        cntDownloadedBytes = 0;
        ExecutorService executorService = Executors.newFixedThreadPool(threads);
        int fqty = 0;
        long startTime = System.currentTimeMillis();
        for(Map.Entry<String, List<String>> entry : urls.entrySet()){
            fqty += entry.getValue().size();
            executorService.execute(new DownloadHelper(entry.getKey(), entry.getValue()));
        }
        executorService.shutdown();
        try{
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        long elapsed = System.currentTimeMillis() - startTime;
        String elapsedStr = String.format("%.2f", (double)(elapsed)/1000);
        System.out.println("Завершено: "+100*cntDownloadedFiles/fqty+"%");
        System.out.println("Загружено: "+cntDownloadedFiles+" файлов, "+cntDownloadedBytes/1024+" KB");
        System.out.println("Время: "+elapsedStr+" секунды");
        System.out.println("Средняя скорость: "+(cntDownloadedBytes/elapsed)*1000/1024+" KB/s");
        return 0;
    }

    private synchronized void addCntDownloadedFiles(int cnt){
        this.cntDownloadedFiles += cnt;
    }

    private synchronized void addCntDownloadedBytes(long cnt){
        this.cntDownloadedBytes += cnt;
    }

    public int downloadFile(String url, List<String> fnames) {
        if(url == null || fnames == null || fnames.isEmpty())
            return -1;
        String elapsed = "";
        try {
//            TimeUnit.SECONDS.sleep(5);
            for(int i=0; i<fnames.size(); i++) Files.deleteIfExists(Paths.get(outputFolder, fnames.get(i)));
            long startTime = System.currentTimeMillis();
            URL urlObj = new URL(url);
            ReadableByteChannel rbc = Channels.newChannel(urlObj.openStream());
            String fname = fnames.get(0);
            FileOutputStream fileOutputStream = new FileOutputStream(Paths.get(outputFolder, fname).toString());
            fileOutputStream.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            fileOutputStream.close();
            //get seconds
            elapsed = String.format("%.2f", (double)(System.currentTimeMillis()-startTime)/1000);
            for(int i=1; i<fnames.size(); i++) Files.copy(Paths.get(outputFolder, fname), Paths.get(outputFolder, fnames.get(i)));
        } catch (Exception ex) {
            System.out.println("fail: " + ex.getMessage());
            ex.printStackTrace();
            return -1;
        }
        long fsize = new File(outputFolder+"/"+fnames.get(0)).length();

        addCntDownloadedFiles(fnames.size());
        addCntDownloadedBytes(fsize);

        if(fnames.size() == 1) System.out.println("Загружается файл: "+fnames.get(0)+"\nФайл "+fnames.get(0)+" загружен: "+fsize/1024+" KB за "+elapsed+" секунды\n");
        else System.out.println("Загружаются файлы: "+String.join(", ", fnames)+"\nФайлы "+String.join(", ", fnames)+" загружены: "+fsize/1024+" КБ за "+elapsed+" секунды\n");

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
