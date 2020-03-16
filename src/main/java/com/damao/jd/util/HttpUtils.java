package com.damao.jd.util;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.UUID;

@Component
public class HttpUtils {

    private PoolingHttpClientConnectionManager cm;

    public HttpUtils(){
        this.cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(100);
        cm.setDefaultMaxPerRoute(10);
    }

    /**
     * 请求配置信息
     * @return config
     */
    private RequestConfig getConfig() {
        RequestConfig config = RequestConfig.custom().setConnectTimeout(1000)
                .setConnectionRequestTimeout(500)
                .setSocketTimeout(10000).build();
        return config;
    }

    /**
     * 设置header信息
     * @param httpGet
     */
    private void setHeader(HttpGet httpGet) {
        httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:65.0) Gecko/20100101 Firefox/65.0");
        httpGet.addHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        httpGet.addHeader("Accept-Language", "en-US,en;q=0.8,zh-CN;q=0.5,zh;q=0.3");
        httpGet.addHeader("Referer", "https://www.jd.com/");
        httpGet.addHeader("DNT","1");
        httpGet.addHeader("Connection", "keep-alive");
        httpGet.addHeader("Upgrade-Insecure-Requests","1");
        httpGet.addHeader("TE", "Trailers");
    }

    /**
     * 根据url下载页面
     * @param url
     * @return
     */
    public String doGetHtml(String url){
        CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(cm).build();
        HttpGet httpGet = new HttpGet(url);
        setHeader(httpGet);
        httpGet.setConfig(this.getConfig());
        CloseableHttpResponse response = null;

        String content = "";

        try {
            response = httpClient.execute(httpGet);
            if(response.getStatusLine().getStatusCode() == 200){
                //若请求成功且响应不为空，content指向内容
                if(response.getEntity() != null){
                    content = EntityUtils.toString(response.getEntity(), "utf8");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(response != null){
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return content;
    }

    /**
     * 根据url下载图片
     * @param url
     * @return picName
     */
    public String doGetImage(String url) throws UnsupportedEncodingException {
        CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(cm).build();
        HttpGet httpGet = new HttpGet(url);
        setHeader(httpGet);
        httpGet.setConfig(this.getConfig());
        CloseableHttpResponse response = null;

        String picName = UUID.randomUUID() + url.substring(url.lastIndexOf('.'));

        try {
            response = httpClient.execute(httpGet);
            if(response.getStatusLine().getStatusCode() == 200){
                //若请求成功且响应不为空，content指向内容
                if(response.getEntity() != null){
                    OutputStream os = new FileOutputStream(
                            new File("C:\\Users\\handa\\Desktop\\images\\" + picName));
                    response.getEntity().writeTo(os);
                }else{
                    return "";
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(response != null){
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return picName;
    }

}
