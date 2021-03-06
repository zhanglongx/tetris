package com.sumavision.tetris.lib.aliyun.push;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class AliSendSmsService extends BasePush{

	/**
	 * 发送短信<br/>
	 * <p>详细描述</p>
	 * <b>作者:</b>ldy<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年3月13日 下午1:09:01
	 * @param telephone 电话号码
	 * @param param 参数 -- "{\"code\":\"111111\"}"
	 */
	public Boolean sendSms(String telephone, String param) throws Exception{
        java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(new java.util.SimpleTimeZone(0, "GMT"));// 这里一定要设置GMT时区
        java.util.Map<String, String> paras = new java.util.HashMap<String, String>();
        // 1. 系统参数
        paras.put("SignatureMethod", "HMAC-SHA1");
        paras.put("SignatureNonce", java.util.UUID.randomUUID().toString());
        paras.put("AccessKeyId", accessKeyId);
        paras.put("SignatureVersion", "1.0");
        paras.put("Timestamp", df.format(new java.util.Date()));
        paras.put("Format", "JSON");
        // 2. 业务API参数
        paras.put("Action", "SendSms");
        paras.put("Version", "2017-05-25");
        paras.put("RegionId", region);
        paras.put("PhoneNumbers", telephone);
        paras.put("SignName", signName);
        paras.put("TemplateParam", param);
        paras.put("TemplateCode", templateCode);
        paras.put("OutId", "123");
        // 3. 去除签名关键字Key
        if (paras.containsKey("Signature"))
            paras.remove("Signature");
        // 4. 参数KEY排序
        java.util.TreeMap<String, String> sortParas = new java.util.TreeMap<String, String>();
        sortParas.putAll(paras);
        // 5. 构造待签名的字符串
        java.util.Iterator<String> it = sortParas.keySet().iterator();
        StringBuilder sortQueryStringTmp = new StringBuilder();
        while (it.hasNext()) {
            String key = it.next();
            sortQueryStringTmp.append("&").append(specialUrlEncode(key)).append("=").append(specialUrlEncode(paras.get(key)));
        }
        String sortedQueryString = sortQueryStringTmp.substring(1);// 去除第一个多余的&符号
        StringBuilder stringToSign = new StringBuilder();
        stringToSign.append("GET").append("&");
        stringToSign.append(specialUrlEncode("/")).append("&");
        stringToSign.append(specialUrlEncode(sortedQueryString));
        String sign = sign(accessKeySecret + "&", stringToSign.toString());
        // 6. 签名最后也要做特殊URL编码
        String signature = specialUrlEncode(sign);
        // 最终打印出合法GET请求的URL
        System.out.println("http://dysmsapi.aliyuncs.com/?Signature=" + signature + sortQueryStringTmp);
        String url = "http://dysmsapi.aliyuncs.com/?Signature=" + signature + sortQueryStringTmp;
        return sendResponse(url, 0);

	}
	
	public static Boolean sendResponse(String url, int num) throws Exception{
    	if(num<3){
    		String response = sendHttp(url);
            System.out.println(response);
            if(response == null){
            	num++;
            	Thread.sleep(5000);
            	return sendResponse(url, num);
            }else{
            	JSONObject respoJson = new JSONObject(response);
            	if(respoJson.getString("Message").equals("OK")){
            		return true;
            	}else{
            		num++;
            		Thread.sleep(5000);
                	return sendResponse(url, num);
            	}
            }
    	}else {
			return false;
		}
    }

    public static String sendHttp(String url) throws Exception{
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet get = new HttpGet(url);
        CloseableHttpResponse response = null;
        StringBuilder sBuilder = null;
        BufferedReader bReader = null;
        try{
        	response = httpClient.execute(get);
        	if(response.getStatusLine().getStatusCode() == 200){
	        	HttpEntity entity = response.getEntity();
	 	        InputStream in = entity.getContent();
	 	        sBuilder = new StringBuilder();
	 	        String line = "";
	 	        bReader = new BufferedReader(new InputStreamReader(in, "utf-8"));
	 	        while ((line=bReader.readLine()) != null) {
	 				sBuilder.append(line);
	 			}
	 	        EntityUtils.consume(entity);
	        }
        }catch(IOException e){
        	e.printStackTrace();
        }finally{
        	try {
        		if(httpClient != null) httpClient.close();
        		if(response != null) response.close();
        		if(bReader != null) bReader.close();
    		} catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sBuilder==null?null:sBuilder.toString();
	}
	
    public static String specialUrlEncode(String value) throws Exception {
        return java.net.URLEncoder.encode(value, "UTF-8").replace("+", "%20").replace("*", "%2A").replace("%7E", "~");
    }
    public static String sign(String accessSecret, String stringToSign) throws Exception {
        javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA1");
        mac.init(new javax.crypto.spec.SecretKeySpec(accessSecret.getBytes("UTF-8"), "HmacSHA1"));
        byte[] signData = mac.doFinal(stringToSign.getBytes("UTF-8"));
        return new sun.misc.BASE64Encoder().encode(signData);
    }
}
