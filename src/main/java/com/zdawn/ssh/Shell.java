package com.zdawn.ssh;

import java.util.ArrayList;
import java.util.List;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class Shell {
	/**
	 * 连接超时
	 */
	protected int connectTimeOut = 30000;
	
	public Session connect(String user, String passwd, String host) {
		return connect(user, passwd, host,22);
    }
	
	public Session connect(String user, String passwd, String host,int port) {
		Session session = null;
		try {
			JSch jsch = new JSch();
			session = jsch.getSession(user, host, port);
			session.setPassword(passwd);
			
			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			
			session.connect(connectTimeOut);
		} catch (JSchException e) {
			throw new RuntimeException(e);
		}
		return session;
    }
	
	public void closeSession(Session session) {
		if(session!=null) {
			session.disconnect();
		}
	}
	
	public List<String> filterVt100Color(List<String> list) {
		List<String> filterList = new ArrayList<>();
		for (String tmp : list) {
			String one = tmp.replaceAll("\u001B\\[[\\d;]*[^\\d;]","");
			one = one.replaceAll("\\e\\[[\\d;]*[^\\d;]","");
			filterList.add(one);
		}
		return filterList;
	}

	public int getConnectTimeOut() {
		return connectTimeOut;
	}

	public void setConnectTimeOut(int connectTimeOut) {
		this.connectTimeOut = connectTimeOut;
	}
}
