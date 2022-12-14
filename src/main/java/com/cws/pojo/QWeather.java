package com.cws.pojo;

import lombok.Data;

/**
 * 和风天气数据
 * //开发版 https://devapi.qweather.com/v7/weather/now?location=101010100&key=你的KEY
 *
 *   "code": "200",
 *   "updateTime": "2020-06-30T22:00+08:00",
 *   "fxLink": "http://hfx.link/2ax1",
 *   "now": {
 *     "obsTime": "2020-06-30T21:40+08:00",
 *     "temp": "24",
 *     "feelsLike": "26",
 *     "icon": "101",
 *     "text": "多云",
 *     "wind360": "123",
 *     "windDir": "东南风",
 *     "windScale": "1",
 *     "windSpeed": "3",
 *     "humidity": "72",
 *     "precip": "0.0",
 *     "pressure": "1003",
 *     "vis": "16",
 *     "cloud": "10",
 *     "dew": "21"
 *   },
 *   "refer": {
 *     "sources": [
 *       "QWeather",
 *       "NMC",
 *       "ECMWF"
 *     ],
 *     "license": [
 *       "commercial license"
 *     ]
 *   }
 * }
 * @author wangxinqi
 */
@Data
public class QWeather {
    private String temp;
    private String feelsLike;
    private String text;
    private String windDir;
    private String windScale;

}
