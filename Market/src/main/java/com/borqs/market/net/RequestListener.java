package com.borqs.market.net;

import java.io.IOException;

/**
 * 发起访问接口的请求时所需的回调接口
 * @author luopeng (luopeng@staff.sina.com.cn)
 */
public interface RequestListener {
    /**
     * 用于获取服务器返回的响应内容
     * @param response
     */
	void onComplete(String response);

	void onIOException(IOException e);

	void onError(WutongException e);

}
