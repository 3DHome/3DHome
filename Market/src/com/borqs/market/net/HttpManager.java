package com.borqs.market.net;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import com.borqs.market.utils.BLog;
import com.borqs.market.utils.QiupuHelper;
import com.borqs.market.utils.Utility;

/**
 * 
 * @author luopeng (luopeng@staff.sina.com.cn)
 */
public class HttpManager {

//	private static final String BOUNDARY = "7cd4a6d158c";
    private static final String BOUNDARY=getBoundry();
	static final String MP_BOUNDARY = "--" + BOUNDARY;
    static final String DEFAULT_CONTENT_TYPE = "application/x-www-form-urlencoded";
	private static final String END_MP_BOUNDARY = "--" + BOUNDARY + "--";
	private static final String MULTIPART_FORM_DATA = "multipart/form-data";
//    public static String UserAgent="os=android;client=com.borqs.market";    


	public static final String HTTPMETHOD_POST = "POST";
	public static final String HTTPMETHOD_GET = "GET";

	private static final int SET_CONNECTION_TIMEOUT = 20 * 1000;
	private static final int SET_SOCKET_TIMEOUT = 120 * 1000;
	/**
	 * 
	 * @param url 服务器地址
	 * @param method "GET"or “POST”
	 * @param params   存放参数的容器
	 * @return 响应结果
	 * @throws com.wutong.sdk.android.WutongException
	 */
	public static String openUrl(String url, String method, WutongParameters params) throws WutongException {
		String result = "";
		try {
            params.add("call_id", Long.toString(System.currentTimeMillis()));
            params.generateSignature();

			HttpClient client = getNewHttpClient();
			HttpUriRequest request = null;
			ByteArrayOutputStream bos = null;
			client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, NetStateManager.getAPN());
			if (method.equals(HTTPMETHOD_GET)) {
				url = url + "?" + Utility.encodeUrl(params);
				HttpGet get = new HttpGet(url);
				BLog.v("HttpManager", url);
				request = get;
			} else if (method.equals(HTTPMETHOD_POST)) {
				HttpPost post = new HttpPost(url);
				request = post;
				byte[] data = null;

				bos = new ByteArrayOutputStream();
				if (params.hasImage()) {
					params.paramToUpload(bos);
					post.setHeader("Content-Type", MULTIPART_FORM_DATA + "; boundary=" + BOUNDARY);
                    final String file = params.getImagePath();
					Utility.UploadImageUtils.revitionPostImageSize(file);
					imageContentToUpload(bos, file);
				} else {
                    post.setHeader("Content-Type", params.popupContentType());
					

					String postParam = Utility.encodeParameters(params);
					data = postParam.getBytes("UTF-8");
					bos.write(data);
				}
				data = bos.toByteArray();
				bos.close();
				ByteArrayEntity formEntity = new ByteArrayEntity(data);
				post.setEntity(formEntity);
			} else if (method.equals("DELETE")) {
				request = new HttpDelete(url);
			}
			request.setHeader( "user-agent",QiupuHelper.user_agent);
			HttpResponse response = client.execute(request);
			StatusLine status = response.getStatusLine();
			int statusCode = status.getStatusCode();

			if (statusCode != 200) {
				result = readHttpResponse(response);
				throw new WutongException(result, statusCode);
			}
			result = readHttpResponse(response);
			return result;
        } catch (IOException e) {
            throw new WutongException(e);
        } catch (SecurityException e) {
            throw new WutongException(e);
        }
    }
	
	private static HttpClient getNewHttpClient() {
		try {
			KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			trustStore.load(null, null);

			SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
			sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

			HttpParams params = new BasicHttpParams();

			HttpConnectionParams.setConnectionTimeout(params, 10000);
			HttpConnectionParams.setSoTimeout(params, 10000);

			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			registry.register(new Scheme("https", sf, 443));

			ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

			HttpConnectionParams.setConnectionTimeout(params, SET_CONNECTION_TIMEOUT);
			HttpConnectionParams.setSoTimeout(params, SET_SOCKET_TIMEOUT);
			HttpClient client = new DefaultHttpClient(ccm, params);
//			if (NetState.Mobile == NetStateManager.CUR_NETSTATE) {
//				// 获取当前正在使用的APN接入点
//				HttpHost proxy = NetStateManager.getAPN();
//				if (null != proxy) {
//					client.getParams().setParameter(ConnRouteParams.DEFAULT_PROXY, proxy);
//				}
//			}
			return client;
		} catch (Exception e) {
			return new DefaultHttpClient();
		}
	}

	private static class MySSLSocketFactory extends SSLSocketFactory {
		SSLContext sslContext = SSLContext.getInstance("TLS");

		public MySSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException,
				KeyManagementException, KeyStoreException, UnrecoverableKeyException {
			super(truststore);

			TrustManager tm = new X509TrustManager() {
				public void checkClientTrusted(X509Certificate[] chain, String authType)
						throws CertificateException {
				}

				public void checkServerTrusted(X509Certificate[] chain, String authType)
						throws CertificateException {
				}

				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			};

			sslContext.init(null, new TrustManager[] { tm }, null);
		}

		@Override
		public Socket createSocket(Socket socket, String host, int port, boolean autoClose)
				throws IOException {
			return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
		}

		@Override
		public Socket createSocket() throws IOException {
			return sslContext.getSocketFactory().createSocket();
		}
	}

	private static void imageContentToUpload(OutputStream out, String imgpath) throws WutongException {
		if(imgpath==null){
		    return;
		}
	    StringBuilder temp = new StringBuilder();
		
		temp.append(MP_BOUNDARY).append("\r\n");
		temp.append("Content-Disposition: form-data; name=\"pic\"; filename=\"")
				.append("news_image").append("\"\r\n");
		String filetype = "image/png";
		temp.append("Content-Type: ").append(filetype).append("\r\n\r\n");
		byte[] res = temp.toString().getBytes();
		FileInputStream input = null;
		try {
			out.write(res);
			 input = new FileInputStream(imgpath);
			byte[] buffer=new byte[1024*50];
			while(true){
				int count=input.read(buffer);
				if(count==-1){
					break;
				}
				out.write(buffer, 0, count);
			}
			out.write("\r\n".getBytes());
			out.write(("\r\n" + END_MP_BOUNDARY).getBytes());
		} catch (IOException e) {
			throw new WutongException(e);
		} finally {
			if (null != input) {
				try {
					input.close();
				} catch (IOException e) {
					throw new WutongException(e);
				}
			}
		}
	}

	/**
	 * 读取HttpResponse数据
	 * 
	 * @param response
	 * @return
	 */
	private static String readHttpResponse(HttpResponse response) {
		String result = "";
		HttpEntity entity = response.getEntity();
		InputStream inputStream;
		try {
			inputStream = entity.getContent();
			ByteArrayOutputStream content = new ByteArrayOutputStream();

			Header header = response.getFirstHeader("Content-Encoding");
			if (header != null && header.getValue().toLowerCase().indexOf("gzip") > -1) {
				inputStream = new GZIPInputStream(inputStream);
			}

			int readBytes = 0;
			byte[] sBuffer = new byte[512];
			while ((readBytes = inputStream.read(sBuffer)) != -1) {
				content.write(sBuffer, 0, readBytes);
			}
			result = new String(content.toByteArray());
			return result;
		} catch (IllegalStateException e) {
		} catch (IOException e) {
		}
		return result;
	}
	   /**
     * 产生11位的boundary
     */
    static String getBoundry() {
        StringBuffer _sb = new StringBuffer();
        for (int t = 1; t < 12; t++) {
            long time = System.currentTimeMillis() + t;
            if (time % 3 == 0) {
                _sb.append((char) time % 9);
            } else if (time % 3 == 1) {
                _sb.append((char) (65 + time % 26));
            } else {
                _sb.append((char) (97 + time % 26));
            }
        }
        return _sb.toString();
    }

}

