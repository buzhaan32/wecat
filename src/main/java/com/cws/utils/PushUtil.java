package com.cws.utils;

import com.cws.DateUtils.LunarCalendarFestivalUtils;
import com.cws.configure.PushConfigure;
import com.cws.pojo.QWeather;
import com.cws.pojo.Result;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpInMemoryConfigStorage;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.WxMpTemplateMsgService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * PushUtil
 *
 * @author cws
 * @date 2022/8/22 21:40
 */
public class PushUtil {

    private static WxMpTemplateMsgService wxService = null;

    /**
     * 消息推送主要业务代码
     */
    public static String push() {
        // 构建模板消息
        WxMpTemplateMessage templateMessage = WxMpTemplateMessage.builder()
                .templateId(PushConfigure.getTemplateId())
                .build();
        // 计算天数
        long loveDays = MemoryDayUtil.calculationLianAi(PushConfigure.getLoveDate());
        long birthdays = 0;
        if (PushConfigure.isUseLunar()) {
            // 如果使用农历生日
            birthdays = MemoryDayUtil.calculationBirthdayByLunar(PushConfigure.getBirthday());
        } else {
            birthdays = MemoryDayUtil.calculationBirthday(PushConfigure.getBirthday());
        }
        templateMessage.addData(new WxMpTemplateData("loveDays", loveDays + "", "#FF1493"));
        templateMessage.addData(new WxMpTemplateData("birthdays", birthdays + "", "#FFA500"));

        // 拿到农历日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date newDate = new Date();
        //根据日期取得星期几
        String week =  getWeek(newDate);
        LunarCalendarFestivalUtils festival = new LunarCalendarFestivalUtils();
        festival.initLunarCalendarInfo(sdf.format(newDate));
        templateMessage.addData(new WxMpTemplateData("date", sdf.format(newDate) + "  " + week, "#C1D8FF"));
        templateMessage.addData(new WxMpTemplateData("lunar", "农历" + festival.getLunarYear() + "年 " + festival.getLunarMonth() + "月" + festival.getLunarDay(), "#C1D8FF"));
        templateMessage.addData(new WxMpTemplateData("festival", festival.getLunarTerm() + " " + festival.getSolarFestival() + " " + festival.getLunarFestival(), "#C1D8FF"));

        StringBuilder messageAll = new StringBuilder();

        // 获取和风天气数据
        Result QweatherResult = QWeatherUtil.getQWeather();
        StringBuilder qMessageAll = new StringBuilder();
        if (!"200".equals(QweatherResult.getCode())) {
            qMessageAll.append("<br/>");
            qMessageAll.append(QweatherResult.getMessage());
            templateMessage.addData(new WxMpTemplateData("weather", "***", "#00FFFF"));
        } else {
            QWeather weather = (QWeather) QweatherResult.getData();
            templateMessage.addData(new WxMpTemplateData("city", "嘉善县", "#2A71E4"));
            templateMessage.addData(new WxMpTemplateData("feelsLike", weather.getFeelsLike(), "#EE212D"));
            templateMessage.addData(new WxMpTemplateData("text", weather.getText() + "", "#2A71E4"));
            templateMessage.addData(new WxMpTemplateData("temp", weather.getTemp() + "", "#EE212D"));
            templateMessage.addData(new WxMpTemplateData("windDir", weather.getWindDir() + "", "#EE212D"));
            templateMessage.addData(new WxMpTemplateData("windScale", weather.getWindScale() + "", "#EE212D"));
        }

        // 天行数据接口
        //彩虹屁
        Result rainbowResult = RainbowUtil.getRainbow();
        if (!"200".equals(rainbowResult.getCode())) {
            messageAll.append("<br/>");
            messageAll.append(rainbowResult.getMessage());
        } else {
            templateMessage.addData(new WxMpTemplateData("rainbow", (String) rainbowResult.getData(), "#FF69B4"));
        }
        //早安心语
        Result morningResult = MorningUtil.getMorning();
        if (!"200".equals(rainbowResult.getCode())) {
            messageAll.append("<br/>");
            messageAll.append(morningResult.getMessage());
        } else {
            templateMessage.addData(new WxMpTemplateData("morning", (String) morningResult.getData(), "#E98888"));
        }

        // 备注
        String remark = "❤";
        if (loveDays % 365 == 0) {
            remark = "\n今天是破壳入世" + (loveDays / 365) + "周年了!";
        }
        if (birthdays == 0) {
            remark = "\n今天是生日,生日快乐呀!";
        }
        if (loveDays % 365 == 0 && birthdays == 0) {
            remark = "\n今天是生日,也是破壳入世" + (loveDays / 365) + "周年纪念日最近更新!";
        }
        templateMessage.addData(new WxMpTemplateData("remark", remark, "#FF1493"));


        System.out.println(templateMessage.toJson());

        // 拿到service
        WxMpTemplateMsgService service = getService();

        int suc = 0;
        int err = 0;
        for (String userId : PushConfigure.getUserId()) {
            templateMessage.setToUser(userId);
            try {
                service.sendTemplateMsg(templateMessage);
                suc += 1;
            } catch (WxErrorException e) {
                err += 1;
                messageAll.append(suc).append("个成功!");
                messageAll.append(err).append("个失败!");
                messageAll.append("<br/>");
                messageAll.append(e.getMessage());
                return "推送结果:" + messageAll;
            }
        }

        return "成功推送给" + suc + "个用户!" + messageAll;
    }

    private static String getWeek(Date newDate) {
        String[] weeks = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
        Calendar cal = Calendar.getInstance();
        cal.setTime(newDate);int week_index = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if(week_index<0){
            week_index = 0;
        }
        return weeks[week_index];
    }

    /**
     * 获取 WxMpTemplateMsgService
     *
     * @return WxMpTemplateMsgService
     */
    private static WxMpTemplateMsgService getService() {
        if (wxService != null) {
            return wxService;
        }
        WxMpInMemoryConfigStorage wxStorage = new WxMpInMemoryConfigStorage();
        wxStorage.setAppId(PushConfigure.getAppId());
        wxStorage.setSecret(PushConfigure.getSecret());
        WxMpService wxMpService = new WxMpServiceImpl();
        wxMpService.setWxMpConfigStorage(wxStorage);
        wxService = wxMpService.getTemplateMsgService();
        return wxService;
    }
}
