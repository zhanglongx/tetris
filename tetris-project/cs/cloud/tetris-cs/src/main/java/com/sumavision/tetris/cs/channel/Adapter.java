package com.sumavision.tetris.cs.channel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;

import com.alibaba.fastjson.JSONObject;
import com.sumavision.tetris.commons.util.wrapper.HashMapWrapper;
import com.sumavision.tetris.commons.util.wrapper.StringBufferWrapper;
import com.sumavision.tetris.cs.channel.exception.ChannelUdpBroadCountIsFullException;

@Service
@Transactional(rollbackFor = Exception.class)
public class Adapter {
	public String changeHttpToFtp(String httpUrl) throws IOException {		
		File file = ResourceUtils.getFile("classpath:profile.json");

		String json = FileUtils.readFileToString(file);
		JSONObject jsonObject = JSONObject.parseObject(json);

		String ftpUserName = jsonObject.getString("ftpUserName");
		String ftpPassword = jsonObject.getString("ftpPassword");

		String ftpPath = null;

		String[] split = httpUrl.split(":");

		//有端口的Url地址
		if (split.length == 2) {
			String[] splite2 = split[1].split("//");
			
			String ip = splite2[1].substring(0, splite2[1].indexOf("/"));
			
			String path = splite2[1].substring(splite2[1].indexOf("/"));
			
			ftpPath = new StringBuilder("ftp://").append(ftpUserName).append(":").append(ftpPassword).append("@")
					.append(ip).append(path).toString();
		//没有端口的url地址
		} else if (split.length == 3) {
			String ip = split[1].split("//")[1];

			String path = split[2].substring(split[2].indexOf("/"));

			ftpPath = new StringBuilder("ftp://").append(ftpUserName).append(":").append(ftpPassword).append("@")
					.append(ip).append(path).toString();
		}

		return ftpPath;
	}
	
	public String getUdpUrlFromIpAndPort(String ip,String port){
		if (ip == null || port == null) return null;
		
		return new StringBufferWrapper().append("udp://@").append(ip).append(":").append(port).toString();
	}
	
	public HashMapWrapper<String, String> getIpAndPortFromUdpUrl(String url){
		if (url == null || url.isEmpty()) return null;
		
		String[] split = url.split(":");
		
		String ip = split[1].split("@")[1];
		
		String port = split[2];
		
		return new HashMapWrapper<String, String>().put("ip", ip)
				.put("port", port);
	}
	
	public int getNewId(List<Integer> ids) throws Exception{
		List<Integer> parentList = new ArrayList<Integer>();
		for (int i = 0; i < 256; i++) {
			parentList.add(i);
		}
		parentList.removeAll(ids);
		if (parentList.isEmpty()) throw new ChannelUdpBroadCountIsFullException();
		return parentList.get(0);
	}
}
