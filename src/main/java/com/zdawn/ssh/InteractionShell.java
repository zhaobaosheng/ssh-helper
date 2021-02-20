package com.zdawn.ssh;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.Session;

public class InteractionShell extends Shell {
	/**
	 * 输入流
	 */
	private InputStream in;
	/**
	 * 输出流
	 */
	private OutputStream os;
	/**
	 * 通道
	 */
	private ChannelShell channel;
	/**
	 * 输出结果
	 */
	private List<String> resultList = new ArrayList<>();
	/**
	 * 读cmd结果线程
	 */
	private Thread thread;
	/**
	 * 命令是否执行完成
	 */
	private boolean execCmdEnd = false;
	/**
	 * 创建交互通道
	 */
	public void createShellChannel(Session session) {
		try {
			channel = (ChannelShell)session.openChannel("shell");
			channel.setPty(true);
			channel.setPtyType("vt100");//去掉字体颜色 dump
			in = channel.getInputStream();
			os = channel.getOutputStream();
			channel.connect(connectTimeOut);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public void writeCmd(String cmd) {
		if(os==null) throw new RuntimeException("OutputStream is null");
		execCmdEnd = false;
		try {
			os.write(cmd.getBytes("utf-8"));
			os.write('\r');
			os.flush();
		} catch (Exception e) {
			execCmdEnd = true;
			throw new RuntimeException(e);
		}
	}
	
	public void readCmdResult() {
		if(in==null) throw new RuntimeException("InputStream is null");
		thread = new Thread(new Runnable() {
			@Override
			public void run() {
				InputStreamReader reader = null;
				StringBuilder sb = new StringBuilder();
				try {
					reader = new InputStreamReader(in,"utf-8");
					int ch = -1;
		            while ((ch = reader.read()) != -1) {
		            	if(ch=='\n' || ch=='\r') {
		            		resultList.add(sb.toString());
		            		sb = new StringBuilder();
		            	}else {
		            		sb.append((char)ch);
		            		if(isCmdEnd(sb.toString())) {
		            			execCmdEnd = true;
		            			resultList.add(sb.toString());
		            		}
		            	}
		            }
				} catch (InterruptedIOException e) {
				} catch (Exception e) {
					System.out.println(e.toString());
				} finally {
					try {
		                if(reader!=null) reader.close();
		            } catch (IOException e) {}
				}
			}
		});
		thread.start();
	}
	
	protected boolean isCmdEnd(String result) {
		if(result==null || "".equals(result)) return false;
		if(result.startsWith("[") && (result.endsWith("]# ") || result.endsWith("]$ "))) return true;
		return false;
	}
	
	public List<String> waitCmdExecResult() {
		List<String> cmdResult = new ArrayList<>();
		if(execCmdEnd) {
			cmdResult.addAll(resultList);
			resultList.clear();
		}else {
			while(!execCmdEnd) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {}
			}
			cmdResult.addAll(resultList);
			resultList.clear();
		}
		return cmdResult;
	}
	
	public void closeChannel() {
		try {
			if(in!=null) in.close();
		} catch (IOException e) {}
		try {
			if(os!=null) os.close();
		} catch (IOException e) {}
		if(thread!=null) thread.interrupt();
		if(channel!=null) channel.disconnect();
	}
	
	public static void main(String[] args) {
		InteractionShell shell = new InteractionShell();
		Session session = null;
		try {
			session = shell.connect("root", "sinosoft-123456","192.168.1.135");
			shell.createShellChannel(session);
			shell.readCmdResult();
			List<String> list = shell.waitCmdExecResult();
			for (String temp : list) {
				System.out.println(temp);
			}
			
			shell.writeCmd("ps -ef | grep java");
			list = shell.filterVt100Color(shell.waitCmdExecResult());
			for (String temp : list) {
				System.out.println(temp);
			}
			
			shell.writeCmd("firewall-cmd --zone=public --list-ports");
			list = shell.waitCmdExecResult();
			for (String temp : list) {
				System.out.println(temp);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			shell.closeChannel();
			shell.closeSession(session);
		}
	}
}
