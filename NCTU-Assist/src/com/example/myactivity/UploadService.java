package com.example.myactivity;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.net.ftp.FTPClient;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.myactivity.staticdata.Constant;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
//import org.apache.commons.net.ftp.FTPSClient;


public class UploadService extends Service{

	//FTPClient can not support sftp(FTP over SSH)
	//private FTPClient mFTPClient ;
	private Thread mthread;
	private FTPClient mFTPClient;
	
	//SFTP
	private ChannelSftp command;
	private Session session;
	private Channel channel;
	
	//�脣漲璇�
	ProgressDialog pd;  
    long totalSize; 
    int count = 0;
	
    
    
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("deprecation")
	@Override
    public void onStart(Intent intent, int startId) {
	  super. onStart(intent,startId);
	  mthread = new Thread(ftpclient);
	  mthread.start();
	 
    }
	//銝餃���
	private Runnable ftpclient = new Runnable() {
	      public void run() {
//	    	  String ftpHost = "140.113.207.101";  
//	          String ftpUserName = "nol";   
//	          String ftpPassword = "netopt56680";
	    	  String sftpHost = "140.113.86.128";  
	          String sftpUserName = "moonorblue";   
	          String sftpPassword = "1234";
	          int port = 10022;
	          String sftpRemoteDirectory = "/home/moonorblue/public_html/htc_logdata";
	          String sftpRemoteSoftwareDirectory = "/home/moonorblue/public_html/htc_logdata_software";
	          boolean isConnect = false;
//	          String LocalDirectory = "/sdcard/HTCactivitylogger";	     
//	          int port = 56687;
	          
	          //crazyleaf storage path
	          //String LocalDirectory = "/storage/sdcard0/HTCactivitylogger";
	          String LocalDirectory = Environment.getExternalStorageDirectory().getPath()+"/HTCactivitylogger";
	          Log.i("ftp","in");
	          isConnect = sftpConnect(sftpHost,sftpUserName,sftpPassword,port);
	          
	          //Hardware
	          File dir = new File(LocalDirectory+"/Hardware/");
	          String[] child = dir.list();
	          
	          if(child == null){
	        	  Log.i("ftp", "hardware directory not have => failed" );
	          }else{
	              for(int i = 0; i < child.length; i++){
	                String fileName = child[i];
	                try {
	                	if(isConnect) {
	                		ftpUpload(LocalDirectory+"/Hardware/"+fileName,sftpRemoteDirectory);
	                	}
		                Deletefile(LocalDirectory+"/Hardware/"+fileName);
					} catch (IOException e) {
						Log.i("ftp", "upload hardware data failed "+e.toString());
					}
	              }
	          }
	          //Software
	          File dir2 = new File(LocalDirectory+"/Software/");
	          String[] child2 = dir2.list();
	          
	          if(child2 == null){
	        	  Log.i("ftp", "software directory not have => failed" );
	          }else{
	              for(int i = 0; i < child2.length && isConnect; i++){
	                String fileName = child2[i];
	                try {
	                	if(isConnect) {
	                		ftpUpload(LocalDirectory+"/Software/"+fileName,sftpRemoteSoftwareDirectory);
	                	}
		                Deletefile(LocalDirectory+"/Software/"+fileName);
					} catch (IOException e) {
						Log.i("ftp", "upload software data failed "+e.toString());
					}
	                /*try {
						ftpUpload(LocalDirectory+"/Software/"+fileName,sftpRemoteDirectory+"/Software/"+fileName);
		                Deletefile(LocalDirectory+"/Software/"+fileName);
					} catch (IOException e) {
						Log.i("ftp", "upload failed "+e.toString());
					}*/
	              }
	          }
	          
	          
	          /*if(ftpUpload(LocalDirectory2,"hello",ftpRemoteDirectory)==false)
	        	  Log.i("ftp", "upload failed" );*/
	          sftpDisconnect();
	      }
	    	 
	   };
 
	   protected void onPreExecute()  
	   {  
		    count = 0; 
	        pd = new ProgressDialog(this);  
	        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);  
	        pd.setMessage("Uploading ...");  
	        pd.setCancelable(false);  
	        pd.show();  
	        new Thread() 
			{
				public void run()
				{
					try
					{
						while (count <= 100)
						{ 
							pd.setProgress(count++);
							Thread.sleep(100); 
						}
						pd.cancel();
					}
					catch (InterruptedException e)
					{
						pd.cancel();
					}
				}
			}.start();	
	   }   
	 public void stopThread(){
	     if(mthread!=null){
	    	 mthread.interrupt();
	    	 mthread = null;
		 }
	 }
	 @Override
	 public void onDestroy() {
		 stopThread();
		 super.onDestroy();
	 }
	 public boolean sftpConnect(String host, String username,String password, int port) {
		 if (command != null) {  
	         sftpDisconnect();  
	     }  
		 try {
			JSch jsch = new JSch();
			session = jsch.getSession(username, host, port);
			session.setPassword(password);
	        java.util.Properties config = new java.util.Properties();
	        config.put("StrictHostKeyChecking", "no");
	        session.setConfig(config);
	        session.connect();
	        channel = session.openChannel("sftp");
	        channel.connect();
	        command = (ChannelSftp)channel;
				
			return command.isConnected();
		} catch(Exception e) {
			Log.i("ftp", "Error: could not connect to host " + host );
		}
		return false;
	}
	public void sftpDisconnect() {
		if(command != null) {  
	        command.exit();  
	    }  
	    if(session != null) {  
	        session.disconnect();  
	    }  
	    command = null;
	    //change to PREDICT_STATE
	    String state = Constant.PREDICT_STATE;
	    Intent stateIntent = new Intent();
	    stateIntent.setAction(Intent.ACTION_SENDTO);
	    stateIntent.putExtra(Constant.STATE, state);
	    LocalBroadcastManager.getInstance(this)
	              .sendBroadcast(stateIntent);
	 }
	 public boolean ftpChangeDirectory(String directory_path)
	 {
	     try {
	         return mFTPClient.changeWorkingDirectory(directory_path);
	     } catch(Exception e) {
	         Log.i("ftp", "Error: could not change directory to " + directory_path);
	     }

	     return false;
	 }
	 public boolean ftpUpload(String srcFilePath, String dstFilePath) throws IOException
	 {
		 FileInputStream srcFileStream = new FileInputStream(srcFilePath);
		 try {
			 command.put(srcFilePath, dstFilePath);
		 } catch (Exception e) {
			 Log.i("ftp", "upload failed"+srcFilePath);
			 return false; 
		 } finally {
			 if(srcFileStream != null) {
				 srcFileStream.close();
			 }
		 }
		 return true;
	 }
	 public boolean Deletefile(String srcFilePath)
	 {
		boolean status = false;
		File file = new File(srcFilePath);
		status = file.delete();
		return status;
	 }
	 
}
