package com.cws.utils;

import com.alibaba.fastjson.JSONObject;
import com.cws.configure.PushConfigure;
import com.cws.pojo.QWeather;
import com.cws.pojo.Result;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * 获取和风天气
 * @author wangxinqi
 */
@Component
public class QWeatherUtil {
    public static Result getQWeather() {
        RestTemplate restTemplate = new RestTemplate();
        Map<String, String> map = new HashMap<>();
        map.put("QLocId", PushConfigure.getQLocId());
        map.put("QAK", PushConfigure.getQAK());
        //String res = restTemplate.getForObject("https://devapi.qweather.com/v7/weather/now?location={QLocId}&key={QAK}&lang=zh}", String.class, map);
        //JSONObject json = JSONObject.parseObject(res);

        String url = "https://devapi.qweather.com/v7/weather/now?lang=zh&location="+PushConfigure.getQLocId()+"&key="+PushConfigure.getQAK();
        System.out.println("url==="+url);
        byte[] oResult = restTemplate.exchange(url, HttpMethod.GET, null, byte[].class).getBody();
        String unGZipResult = unGZip(oResult);
        System.out.println(unGZipResult);
        JSONObject json = JSONObject.parseObject(unGZipResult);

        Result result = new Result();
        if (json == null) {
            //接口地址有误或者接口没调通
            result.setCode("500");
            result.setMessage("接口不通,请检查接口地址!");
            return result;
        }
//            获取接口响应状态
        String status = json.getString("code");
        if (!"200".equals(status)) {
//          //响应状态不为200即调用出错
            result.setCode(status);
            result.setMessage("天气接口调用报错:" + status);
            return result;
        }
        JSONObject now = json.getJSONObject("now");
        QWeather weather = new QWeather();
        weather.setTemp(now.getString("temp"));
        weather.setFeelsLike(now.getString("feelsLike"));
        weather.setText(now.getString("text"));
        weather.setWindDir(now.getString("windDir"));
        weather.setWindScale(now.getString("windScale"));
        result.setCode(status);
        result.setData(weather);
        return result;
    }

    private static String unGZip(byte[] oResult) {
        try{
            InputStream inputStream = new ByteArrayInputStream(oResult);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream);
            byte[] buf = new byte[4096];
            int len = -1;
            while ((len = gzipInputStream.read(buf, 0, buf.length)) != -1) {
                byteArrayOutputStream.write(buf, 0, len);
            }
            return new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
