package com.linkmoretech.order.service;

import java.util.Map;

import com.linkmoretech.order.common.request.ReqLongPay;
import com.linkmoretech.order.common.request.ReqLoongPayVerifySign;
import com.linkmoretech.order.common.response.ResLoongPay;

/**
 * 建行龙支付接口
 * @author jhb
 * @Date 2019年6月20日 下午7:23:46
 * @Version 1.0
 */
public interface AppLoongPayService {

	/** 下单
	 * @Description  
	 * @Author   GFF 
	 * @Version  v2.0
	 */
	ResLoongPay order(ReqLongPay longPay);

	/**
	 * @Description  通知消息
	 * @Author   GFF 
	 * @Version  v2.0
	 */
	boolean callbackMsg(Map<String, Object> map);

	/**
	 * @Description  校验
	 * @Author   GFF 
	 * @Version  v2.0
	 */
	boolean verifySigature(ReqLoongPayVerifySign verifySign);

}
