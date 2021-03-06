/*
 * This File is a part of NekoLauncher
 * of Nekocraft
 */
package com.nekocraft.launcher;

import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.zip.*;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
/**
 *
 * @author gjz010
 */
public class DownloadThread extends Thread{
    private MinecraftStructure mc;
    private String current;
    private int tries=0;
    @Override
    public void run(){
        fetchCurrentVersion();
        mc=new MinecraftStructure();
        try {
            parseXML();
            updateGame();
        } catch (Exception ex) {
            NekoLauncher.handleException(ex);
        }
        LaunchThread launch=new LaunchThread(mc);
        launch.start();
    }
    private void fetchCurrentVersion(){ 
        LoginFrame.bar.setString("获取版本信息中...");
        FileWriter out = null;
        try {
            current=HTMLFetcher.getHTML(StaticRes.INFO_REPO+"current.xml", "UTF-8");
            out = new FileWriter(StaticRes.CURRENT_XML);
            BufferedWriter b=new BufferedWriter(out);
            b.write(current);
            b.flush();
        } catch (IOException ex) {
            NekoLauncher.handleException(ex);
        } finally {
            try {
                out.close();
            } catch (IOException ex) {
                NekoLauncher.handleException(ex);
            }
        }
    }
    private void updateGame()throws Exception{ 
        ///////////最烦1.6的结构了！ 在这里只写旧版本的更新。
        for (Library l:mc.getJars()){
            updateFile(l,0);
        }
        for (Library l:mc.getNatives()){
            updateFile(l,1);
        }
        for (Library l:mc.getLibs()){
            updateFile(l,2);
        }
        for (Library l:mc.getMods()){
            updateFile(l,3);
        }
    }
    private void updateFile(Library lib,int type) throws Exception{//////type值:0-jar 1-native 2-lib 3-mod
        LoginFrame.bar.setString("Updating "+lib.getName());
        LoginFrame.bar.setValue(0);
        File f=getFile(lib,type);
        String fmd5=FileDigest.getFileMD5(f);
        if(!lib.getMd5().equals(fmd5)){
            if(lib.getName().equals("minecraft.jar")){
                downloadMinecraft(f,lib);
                return;
            }
            if(lib.getName().equals("spoutcraft.jar")){
                downloadSpoutcraft(f,lib);
                return;
            }
            if(type==1){
                downloadNative(lib,f);
                return;
            }
            downloadFile(StaticRes.INFO_REPO+lib.getName(),f,lib);
        }
        
    }
    private void downloadFile(String url,File target,Library lib) throws Exception{
        
        //for(int t=0;t<=3;t++){
            /*
        int bytesum = 0;
        int byteread = 0;
        URL u=new URL(url);
        HttpURLConnection con=(HttpURLConnection)u.openConnection();
        con.setConnectTimeout(10000);
        con.setRequestMethod("GET");
        LoginFrame.bar.setMaximum(con.getContentLength());
        InputStream in=con.getInputStream();
        FileOutputStream out=new FileOutputStream(target);
        byte[] buffer = new byte[1024];
        while ((byteread = in.read(buffer)) != -1) {
                bytesum += byteread;
                LoginFrame.bar.setValue(bytesum);
                out.write(buffer, 0, byteread);
        }
        out.flush();
        out.close();*/
        for(int t=0;t<=3;t++){
        MulThreadDownloaderService mds=new MulThreadDownloaderService();
        mds.mulThreadDownloader(url, target);
        while(mds.current!=0){
            //Thread.sleep(10);
        }
        
        if(lib.getMd5().equals(FileDigest.getFileMD5(target))){
            System.out.println("完成");
            return;
        }}
      //  }
            throw new Exception(){
                @Override
                public String getMessage(){
                    return "Download Failed because MD5 verify not passed!";
                }
            };
        }
    private void downloadMinecraft(File target,Library lib)throws Exception{
        //获取Minecraft地址
        String version=mc.getMcversion();
        downloadFile(StaticRes.MC_REPO+version.replace(".", "_")+"/minecraft.jar",target,lib);
    }
    private void downloadSpoutcraft(File target,Library lib)throws Exception{
        String version=Integer.toString(mc.getScversion());
        StringBuilder u=new StringBuilder(StaticRes.SCP_REPO);
        u.append(version);
        u.append("/artifact/target/Spoutcraft.jar");
        downloadFile(u.toString(),target,lib);
    }
    private void downloadNative(Library lib,File target)throws Exception{
        
        if(System.getProperty("os.name").toLowerCase().replace(" ", "").contains(lib.getOs())){
        downloadFile(StaticRes.INFO_REPO+lib.getName(),target,lib);
        ZipFile zip=new ZipFile(target);
        ZipInputStream zis = new ZipInputStream(new FileInputStream(target), Charset.forName("UTF-8"));
        ZipEntry entry=null;
        while((entry=zis.getNextEntry())!=null){
            String filename=entry.getName();
            File temp=new File(StaticRes.NATIVES,filename);
            OutputStream os = new FileOutputStream(temp);
            InputStream is = zip.getInputStream(entry);
            int len = 0;
            while((len=is.read())!=-1){
                os.write(len);
            }
            os.flush();
            os.close();
            is.close();
            
        }
        }
    }
    private File getFile(Library lib,int type){
        //////type值:0-jar 1-native 2-lib 3-mod
        switch(type){
            case 0:
                return new File(StaticRes.BIN,lib.getName());//jar
            case 1:
                return new File(StaticRes.NATIVES,lib.getName());//native
            case 2:
                return new File(StaticRes.LIB,lib.getName());//lib
            case 3:
                return new File(StaticRes.MODS,lib.getName());//mod
        }
        return new File(StaticRes.MINECRAFT,lib.getName());
    }
    private void parseXML() throws Exception{
        DOMParser parser=new DOMParser();
        parser.parse(new InputSource(new StringReader(current)));
        Document doc=parser.getDocument();
        mc.setMcversion(doc.getElementsByTagName("mc").item(0).getTextContent());
        mc.setScversion(Integer.parseInt(doc.getElementsByTagName("scp").item(0).getTextContent()));
        NodeList files=doc.getElementsByTagName("jar");
        for(int i=0;i<files.getLength();i++){
          Library jar=new Library();
            for(int j=0;j<files.item(i).getAttributes().getLength();j++){
                if(files.item(i).getAttributes().item(j).getNodeName().equals("name")){
                    jar.setName(files.item(i).getAttributes().item(j).getNodeValue());
                }
                if(files.item(i).getAttributes().item(j).getNodeName().equals("md5")){
                    jar.setMd5(files.item(i).getAttributes().item(j).getNodeValue());
                }
            }
            mc.addJar(jar);
        }
        files=doc.getElementsByTagName("native");
        for(int i=0;i<files.getLength();i++){
          Library jar=new Library();
            for(int j=0;j<files.item(i).getAttributes().getLength();j++){
                if(files.item(i).getAttributes().item(j).getNodeName().equals("name")){
                    jar.setName(files.item(i).getAttributes().item(j).getNodeValue());
                }
                if(files.item(i).getAttributes().item(j).getNodeName().equals("md5")){
                    jar.setMd5(files.item(i).getAttributes().item(j).getNodeValue());
                }
                if(files.item(i).getAttributes().item(j).getNodeName().equals("os")){
                    jar.setOs(files.item(i).getAttributes().item(j).getNodeValue());
                }
            }
            mc.addNative(jar);
        }
        files=doc.getElementsByTagName("lib");
        for(int i=0;i<files.getLength();i++){
          Library jar=new Library();
            for(int j=0;j<files.item(i).getAttributes().getLength();j++){
                if(files.item(i).getAttributes().item(j).getNodeName().equals("name")){
                    jar.setName(files.item(i).getAttributes().item(j).getNodeValue());
                }
                if(files.item(i).getAttributes().item(j).getNodeName().equals("md5")){
                    jar.setMd5(files.item(i).getAttributes().item(j).getNodeValue());
                }
            }
            mc.addLib(jar);
        }
        files=doc.getElementsByTagName("mod");
        for(int i=0;i<files.getLength();i++){
          Library jar=new Library();
            for(int j=0;j<files.item(i).getAttributes().getLength();j++){
                if(files.item(i).getAttributes().item(j).getNodeName().equals("name")){
                    jar.setName(files.item(i).getAttributes().item(j).getNodeValue());
                }
                if(files.item(i).getAttributes().item(j).getNodeName().equals("md5")){
                    jar.setMd5(files.item(i).getAttributes().item(j).getNodeValue());
                }
            }
            mc.addMod(jar);
        }
        
        
        System.out.println(mc.toString());
    }

}
