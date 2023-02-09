package com.eikona.tech.util;



import java.nio.charset.StandardCharsets;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.eikona.tech.constants.ApplicationConstants;
import com.eikona.tech.constants.NumberConstants;


@Component
public class RequestExecutionUtil {
	
	@Value("${sap.login.username}")
	private String username;
	
	@Value("${sap.login.password}")
	private String password;
	
	public HttpGet getSAPGetRequest(String myurl) {

		HttpGet request = new HttpGet(myurl);
		request.setHeader(ApplicationConstants.HEADER_CONTENT_TYPE, ApplicationConstants.APPLICATION_JSON);
		String auth = username + ApplicationConstants.DELIMITER_COLON + password;
		byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.ISO_8859_1));
		String authHeader = ApplicationConstants.BASIC_AUTH + new String(encodedAuth);
		request.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
		return request;
	}
	
	public String executeHttpPostRequest(HttpPost request) {
		String responeData = null;
		try {
			int timeout = NumberConstants.THOUSAND;
			RequestConfig config = RequestConfig.custom().setConnectTimeout(timeout * NumberConstants.THOUSAND)
					.setConnectionRequestTimeout(timeout * NumberConstants.THOUSAND).setSocketTimeout(timeout * NumberConstants.THOUSAND).build();
			CloseableHttpClient httpclient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();

			HttpResponse response = httpclient.execute(request);
			responeData = EntityUtils.toString(response.getEntity());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return responeData;
	}
	
	public String executeHttpPutRequest(HttpPut request) {
		String responeData = null;
		try {
			int timeout = NumberConstants.THOUSAND;
			RequestConfig config = RequestConfig.custom().setConnectTimeout(timeout * NumberConstants.THOUSAND)
					.setConnectionRequestTimeout(timeout * NumberConstants.THOUSAND).setSocketTimeout(timeout * NumberConstants.THOUSAND).build();
			CloseableHttpClient httpclient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();

			HttpResponse response = httpclient.execute(request);
			responeData = EntityUtils.toString(response.getEntity());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return responeData;
	}

	public String executeHttpGetRequest(HttpGet request) {
		String responeData = null;
		try {
			int timeout = NumberConstants.THOUSAND;
			RequestConfig config = RequestConfig.custom().setConnectTimeout(timeout * NumberConstants.THOUSAND)
					.setConnectionRequestTimeout(timeout * NumberConstants.THOUSAND).setSocketTimeout(timeout * NumberConstants.THOUSAND).build();
			CloseableHttpClient httpclient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();

			HttpResponse response = httpclient.execute(request);
			responeData = EntityUtils.toString(response.getEntity());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return responeData;

	}
	
}
