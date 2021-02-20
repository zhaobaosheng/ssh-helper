package com.zdawn.ssh;

import java.io.File;
import java.util.Vector;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;

public class Xftp extends Shell {
	/**
	 * 通道
	 */
	private ChannelSftp channelSftp;
	
	public void openChannel(Session session) {
		 try {
			channelSftp = (ChannelSftp) session.openChannel("sftp");
			channelSftp.connect(connectTimeOut);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public void getFile(String src,String dst) {
		try {
			channelSftp.get(src, dst);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public void putFile(String src,String dst) {
		try {
			channelSftp.put(src, dst);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public void putFolder(String localPath,String remotePath) {
		try {
			File lfile = new File(localPath);
			if(!lfile.exists()) return ;
			if(lfile.isDirectory()) {
				String rPath = remotePath+'/'+lfile.getName();
				//判断目录是否存在
				SftpATTRS attrs = null;
				try {
					attrs = channelSftp.stat(rPath);
				} catch (Exception e) {}
				//没有创建
				if(attrs==null) channelSftp.mkdir(rPath);
				
				File[] listFiles = lfile.listFiles();
				for (File file : listFiles) {
					putFolder(file.getAbsolutePath(), rPath);
				}
			}else {
				putFile(lfile.getAbsolutePath(), remotePath+'/'+lfile.getName());
			}
		} catch (SftpException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void getFolder(String localPath,String remotePath) {
		try {
			@SuppressWarnings("unchecked")
			Vector<ChannelSftp.LsEntry> list = channelSftp.ls(remotePath);
			if(list!=null && list.size()>0) {
				for (LsEntry lsEntry : list) {
					if(".".equals(lsEntry.getFilename()) || "..".equals(lsEntry.getFilename())) continue;
					if(lsEntry.getAttrs().isDir()) {//目录
						String lPath = localPath+'/'+lsEntry.getFilename();
						File file = new File(lPath);
						if(!file.exists()) file.mkdir();
						getFolder(lPath, remotePath+'/'+lsEntry.getFilename());
					}else {//文件
						getFile(remotePath+'/'+lsEntry.getFilename(),localPath+'/'+lsEntry.getFilename());
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public void closeChannel() {
		if(channelSftp!=null) channelSftp.disconnect();
	}
	
	public static void main(String[] args) {
		Xftp xftp = new Xftp();
		Session session = null;
		try {
			session = xftp.connect("root", "sinosoft-123456","192.168.1.135");
			xftp.openChannel(session);
			xftp.putFolder("E:/mydesktop/temp/xftp", "/root/temp");
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			xftp.closeChannel();
			xftp.closeSession(session);
		}
	}
}
