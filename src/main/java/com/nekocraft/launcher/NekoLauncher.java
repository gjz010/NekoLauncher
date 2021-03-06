package com.nekocraft.launcher;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JTextArea;

public class NekoLauncher{
    public static LoginFrame mf;
    public static void main(String[] args){
        System.out.println("Woo Nekocraft Launcher!");
        try{
        initDir(StaticRes.MINECRAFT);
        initDir(StaticRes.BIN);
        initDir(StaticRes.LIB);
        initDir(StaticRes.NATIVES);
        initDir(StaticRes.MODS);
        if(!new File(".minecraft/options.txt").exists()){
        FileUtil.createFile(new File(".minecraft/options.txt").getAbsolutePath(),"lang:zh_CN");
        }
        }
        catch(Exception ex){
            handleException(ex);
        }
        mf=new LoginFrame();
        mf.setVisible(true);
    }
    private static JDialog exFrame;
    private static boolean exinit=false;
    private static JTextArea text;
    public static void handleException(Exception e){
        Logger.getLogger(NekoLauncher.class.getName()).log(Level.SEVERE, null, e);
        if (!exinit){
        exinit=true;
        exFrame=new JDialog();
        exFrame.setModal(true);
        exFrame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        exFrame.setAlwaysOnTop(true);
        exFrame.setSize(new Dimension(400, 500)); 
        Random random = new Random();//我会说这里有个彩蛋吗
        int r=random.nextInt(10);
        if(r==2){
            exFrame.setTitle("Yooooooooo!Something went wrong!");
        }
        else{
            exFrame.setTitle("Whoooooops!Something went wrong!");
        }
        exFrame.setResizable(false); 
        exFrame.setLocationRelativeTo(exFrame.getOwner());
        exFrame.addWindowListener(new WindowAdapter(){
         @Override
         public void windowClosing(WindowEvent e) {
         System.exit(0);   //退出程序
        }
        });
        text=new JTextArea();
        text.setLineWrap(true);
        text.setEditable(false);
        if(r==2){
            text.append("Yooooooooo!Something happened to the Launcher!\n");
        }else{
            text.append("Whoooooops!Something happened to the Launcher!\n");
        }
        text.append(e.toString()+"\n");
        
        StackTraceElement[] ste=e.getStackTrace();
        int i;
        for(i=0;i<ste.length;i++){
            text.append("	at ");
            text.append(ste[i].toString()+"\n");
        }
        exFrame.getContentPane().add(text);
        exFrame.setVisible(true);}
    }
    private static void initDir(File dir) throws Exception{
        if(!dir.exists()){
            dir.mkdir();
        }
    }
    
}
