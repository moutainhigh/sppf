package com.linkmoretech.order.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.linkmoretech.common.util.JsonUtil;
import com.linkmoretech.order.common.request.ReqTemplateMsg;
import com.linkmoretech.order.common.response.ResToken;
import com.linkmoretech.order.common.response.ResWechatUserList;
import com.linkmoretech.order.pay.wechat.core.ErrCode;
import com.linkmoretech.order.pay.wechat.core.HttpMethod;
import com.linkmoretech.order.pay.wechat.core.WeChat;
import com.linkmoretech.order.pay.wechat.vo.Token;
import com.linkmoretech.order.service.TokenService;
import com.linkmoretech.order.service.WechatService;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;

/**
 * 微信 服务处理，消息转换等
 * @author jhb
 * @version 1.0
 */
@Service
public class WechatServiceImpl implements WechatService {
	private  Logger log = LoggerFactory.getLogger(this.getClass());

	@Resource
	private TokenService tokenService; 
	
	/**
	 * 发布菜单
	 * @param menus
	 * @param accessToken
	 * @return
	 */
	public  JSONObject publishWxMenu(String menus,String accessToken){
		String url = WeChat.getMenuCreateUrl(accessToken);
		return WeChat.httpsRequest(url, HttpMethod.POST, menus);
	}

	/**
	 * 删除菜单
	 * @param accessToken
	 * @return
	 */
	public  JSONObject deleteWxMenu(String accessToken){
		String url = WeChat.getMenuDeleteUrl(accessToken);
		return WeChat.httpsRequest(url, HttpMethod.POST, null);
	}

	/**
	 * 群发接口
	 * @param mediaId
	 * @param msgType
	 * @param appId
	 * @param appSecret
	 * @return
	 */
	public  JSONObject sendAll(String mediaId,String msgType,String appId,String appSecret){
		JSONObject postObj = new JSONObject();
		JSONObject filter = new JSONObject();
		filter.put("is_to_all", true);
		postObj.put("filter", filter);
		JSONObject mpnews = new JSONObject();
		mpnews.put("media_id", mediaId);
		postObj.put("mpnews", mpnews);
		postObj.put("msgtype", msgType);
		String token = WeChat.getTokenUrl(appId, appSecret);
		return WeChat.httpsRequest(WeChat.getUploadNewsUrl(token), HttpMethod.POST, postObj.toString());
	}

	/**
	 * 获取ticket
	 * @param actionName
	 * @param sceneId
	 * @return
	 */
	public String getTicket(String actionName,Long sceneId) {
		//Token token = this.tokenService.getToken();
		Token token = null;
		//this.tokenService.getToken();
		String ticket = null;
		String ticketUrl = WeChat.getTicketUrl(token);
		JSONObject postObj = new JSONObject();
		postObj.put("action_name", actionName);
		Map<String,Object> sceneMap = new HashMap<String,Object>();
		Map<String,Object> idMap = new HashMap<String,Object>();
		idMap.put("scene_id", sceneId);
		sceneMap.put("scene", idMap);
		postObj.put("action_info", sceneMap);
		JSONObject jsonObject = WeChat.httpsRequest(ticketUrl, HttpMethod.POST, postObj.toString()); 
		if (null != jsonObject&&jsonObject.containsKey("errcode")) { 
			int errorCode = jsonObject.getInt("errcode");
			log.info("获取用户二维码tikcet errcode:{} errmsg:{}", errorCode, ErrCode.errMsg(errorCode));
			if(errorCode==40001){
				log.info("40001 access_token is invalid and reget access_token "); 
				//token = tokenService.resetToken();
				ticketUrl = WeChat.getTicketUrl(token);
				jsonObject = WeChat.httpsRequest(ticketUrl, HttpMethod.POST, postObj.toString()); 
			} 
			 
		}
		log.info(jsonObject.toString());
		if (null != jsonObject && !jsonObject.containsKey("errcode")) {
			try {
				ticket = jsonObject.getString("ticket");
			} catch (JSONException e) { }
		} 
		log.info("create scan ticket:{}",ticket);
		return ticket;
	}
	/**
	 * 获取js api ticket
	 * @param actionName
	 * @param sceneId
	 * @return
	 */
	public  String getJsApiTicket() {
		Token token = null;
		//this.tokenService.getToken();
		String ticket = null;
		String ticketUrl = WeChat.getJsApiTicket(token);
		JSONObject jsonObject = WeChat.httpsRequest(ticketUrl, HttpMethod.POST,null);
		if (jsonObject!=null && jsonObject.containsKey("ticket")) {
			try {
				ticket = jsonObject.getString("ticket");
			} catch (JSONException e) {
				ticket = null;	// 获取ticket失败
			}
		}else if(null != jsonObject){
//			ticket = jsonObject.getInt("errcode");
		}
		return ticket;
	}
	public Boolean sendMsg(Map<String,Object> param){
		//Token token = this.tokenService.getToken();
		Token token = null;
		//this.tokenService.getToken();
		String url = WeChat.getMsgSendUrl(token);
		JSONObject json = WeChat.httpsRequest(url, HttpMethod.POST, JSONObject.fromObject(param).toString());
		Boolean flag = false; 
		if (null != json &&json.get("errcode")!=null&& "0".equals(json.get("errcode").toString()) ){
			flag = true;
		}else if(null != json){
			flag = false;
		}
		 
		log.info("send msg :"+JSONObject.fromObject(param).toString());
		log.info("return msg:"+json.toString()); 
		log.info("success:"+flag); 
		return flag;
	}
	 /**
     * 以http方式发送请求,并将请求响应内容输出到文件
     * @param path    请求路径
     * @param method  请求方法
     * @param body    请求数据
     * @return 返回响应的存储到文件
     */
    public  File httpRequestToFile(String fileName,String path, String method, String body) {
        if(fileName==null||path==null||method==null){
            return null;
        }

        File file = null;
        HttpURLConnection conn = null;
        InputStream inputStream = null;
        FileOutputStream fileOut = null;
        try {
            URL url = new URL(path);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod(method);
            if (null != body) {
                OutputStream outputStream = conn.getOutputStream();
                outputStream.write(body.getBytes("UTF-8"));
                outputStream.close();
            }

            inputStream = conn.getInputStream();
            if(inputStream!=null){
                file = new File(fileName);
            }else{
                return file;
            }

            //写入到文件
            fileOut = new FileOutputStream(file);
            if(fileOut!=null){
                int c = inputStream.read();
                while(c!=-1){
                    fileOut.write(c);
                    c = inputStream.read();
                }
            }
        } catch (Exception e) {

        }finally{
            if(conn!=null){
                conn.disconnect();
            }

            /*
             * 必须关闭文件流
             * 否则JDK运行时，文件被占用其他进程无法访问
             */
            try {
                inputStream.close();
                fileOut.close();
            } catch (IOException execption) {

            }
        }
        return file;
    }

	@Override
	public ResToken resetToken(String appid, String appsecret, String key) {
		return null;
		//tokenService.resetToken(appid,appsecret,key);
	}

	@Override
	public void pushTemplateMsg(ReqTemplateMsg msg) {
		String url = WeChat.getTemplateMsg(msg.getToken());
		Map<String, Object> map = new HashMap<>();
		map.put("touser", msg.getOpenId());
		map.put("template_id", msg.getTemplateId());
		map.put("topcolor", msg.getTopcolor());
		map.put("data", msg.getData());
		String obj = JsonUtil.toJson(map);
		JSONObject json = WeChat.httpsRequest(url, HttpMethod.POST, obj);
		Boolean flag = false; 
		if (null != json &&json.get("errcode")!=null&& "0".equals(json.get("errcode").toString()) ){
			flag = true;
		}else if(null != json){
			flag = false;
		}
		log.info("return msg:"+json.toString()); 
		log.info("success:"+flag); 
	}

	@Override
	public ResWechatUserList getUserList(String token) {
		String url = WeChat.getUserListUrl(token);
		JSONObject json = WeChat.httpsRequest(url, HttpMethod.GET, null);
		Object total = json.get("total");
		Object count = json.get("count");
		Object data = json.get("data");
		JSONObject openids = JSONObject.fromObject(data);
		List<String> ids = (List<String>) openids.get("openid");
		ResWechatUserList user = new ResWechatUserList();
		user.setCount(Integer.parseInt(count.toString()));
		user.setTotal(Integer.parseInt(total.toString()));
		user.setOpenIds(ids);
		return user;
	} 
	
}



