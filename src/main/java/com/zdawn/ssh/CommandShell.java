package com.zdawn.ssh;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;

public class CommandShell extends Shell {
	/**
	 * 执行一串命令
	 */
	public String execCmd(Session session,String command) {
		BufferedReader reader = null;
        Channel channel = null;
        StringBuilder sb = new StringBuilder();
        try {
        	channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);
            
            channel.setInputStream(null);
            ((ChannelExec) channel).setErrStream(System.err);

            channel.connect();
            InputStream in = channel.getInputStream();
            reader = new BufferedReader(new InputStreamReader(in,"utf-8"));
            String buf = null;
            while ((buf = reader.readLine()) != null) {
                sb.append(buf).append('\n');
            }
        } catch (Exception e) {
        	throw new RuntimeException(e);
        } finally {
            try {
                if(reader!=null) reader.close();
            } catch (IOException e) {}
            if(channel!=null) channel.disconnect();
        }
        return sb.toString();
	}
	
	public static void main(String[] args) {
		CommandShell shell = new CommandShell();
		Session session = shell.connect("root", "sinosoft-123456","192.168.1.135");
		System.out.println(shell.execCmd(session, "cd /home && ls -l"));
		shell.closeSession(session);
	}

}
