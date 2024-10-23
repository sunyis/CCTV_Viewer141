package com.eanyatonic.cctvViewer;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.common.util.ArrayUtils;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;

public class TVUrls {
    public static String defJson="""
                [{
                "name":"央视频",
                "url":["CCTV-1 综合","https://www.yangshipin.cn/tv/home?pid=600001859",
                                        "CCTV-2 财经","https://tv.cctv.com/live/cctv2/",
                                        "CCTV-3 综艺","https://tv.cctv.com/live/cctv3/",
                                        "CCTV-4 中文国际","https://tv.cctv.com/live/cctv4/",
                                        "CCTV-5 体育","https://tv.cctv.com/live/cctv5/",
                                        "CCTV-6 电影","https://tv.cctv.com/live/cctv6/",
                                        "CCTV-7 军事农业","https://tv.cctv.com/live/cctv7/",
                                        "CCTV-8 电视剧","https://tv.cctv.com/live/cctv8/",
                                        "CCTV-9 纪录","https://tv.cctv.com/live/cctvjilu/",
                                        "CCTV-10 科教","https://tv.cctv.com/live/cctv10/",
                                        "CCTV-11 戏曲","https://tv.cctv.com/live/cctv11/",
                                        "CCTV-12 社会与法","https://tv.cctv.com/live/cctv12/",
                                        "CCTV-13 新闻","https://tv.cctv.com/live/cctv13/",
                                        "CCTV-14 少儿","https://tv.cctv.com/live/cctvchild/",
                                        "CCTV-15 音乐","https://tv.cctv.com/live/cctv15/",
                                        "CCTV-16 奥林匹克","https://tv.cctv.com/live/cctv16/",
                                        "CCTV-17 农业农村","https://tv.cctv.com/live/cctv17/",
                                        "CCTV-5+ 体育赛事","https://tv.cctv.com/live/cctv5plus/",
                                        "CCTV4K","https://www.yangshipin.cn/tv/home?pid=600002264",
                                        "CCTV Europe","https://tv.cctv.com/live/cctveurope/",
                                        "CCTV America","https://tv.cctv.com/live/cctvamerica/",
                                        "北京卫视","https://www.yangshipin.cn/tv/home?pid=600002309",
                                        "江苏卫视","https://www.yangshipin.cn/tv/home?pid=600002521",
                                        "东方卫视","https://www.yangshipin.cn/tv/home?pid=600002483",
                                        "浙江卫视","https://www.yangshipin.cn/tv/home?pid=600002520",
                                        "湖南卫视","https://www.yangshipin.cn/tv/home?pid=600002475",
                                        "湖北卫视","https://www.yangshipin.cn/tv/home?pid=600002508",
                                        "广东卫视","https://www.yangshipin.cn/tv/home?pid=600002485",
                                        "广西卫视","https://www.yangshipin.cn/tv/home?pid=600002509",
                                        "黑龙江卫视","https://www.yangshipin.cn/tv/home?pid=600002498",
                                        "海南卫视","https://www.yangshipin.cn/tv/home?pid=600002506",
                                        "重庆卫视","https://www.yangshipin.cn/tv/home?pid=600002531",
                                        "深圳卫视","https://www.yangshipin.cn/tv/home?pid=600002481",
                                        "四川卫视","https://www.yangshipin.cn/tv/home?pid=600002516",
                                        "河南卫视","https://www.yangshipin.cn/tv/home?pid=600002525",
                                        "福建东南卫视","https://www.yangshipin.cn/tv/home?pid=600002484",
                                        "贵州卫视","https://www.yangshipin.cn/tv/home?pid=600002490",
                                        "江西卫视","https://www.yangshipin.cn/tv/home?pid=600002503",
                                        "辽宁卫视","https://www.yangshipin.cn/tv/home?pid=600002505",
                                        "安徽卫视","https://www.yangshipin.cn/tv/home?pid=600002532",
                                        "河北卫视","https://www.yangshipin.cn/tv/home?pid=600002493",
                                        "山东卫视","https://www.yangshipin.cn/tv/home?pid=600002513"]
                },
                {
                "name":"央视网",
                "url":["CCTV-1 综合","https://tv.cctv.com/live/cctv1/",
                                        "CCTV-2 财经","https://tv.cctv.com/live/cctv2/",
                                        "CCTV-3 综艺","https://tv.cctv.com/live/cctv3/",
                                        "CCTV-4 中文国际","https://tv.cctv.com/live/cctv4/",
                                        "CCTV-5 体育","https://tv.cctv.com/live/cctv5/",
                                        "CCTV-6 电影","https://tv.cctv.com/live/cctv6/",
                                        "CCTV-7 军事农业","https://tv.cctv.com/live/cctv7/",
                                        "CCTV-8 电视剧","https://tv.cctv.com/live/cctv8/",
                                        "CCTV-9 纪录","https://tv.cctv.com/live/cctvjilu/",
                                        "CCTV-10 科教","https://tv.cctv.com/live/cctv10/",
                                        "CCTV-11 戏曲","https://tv.cctv.com/live/cctv11/",
                                        "CCTV-12 社会与法","https://tv.cctv.com/live/cctv12/",
                                        "CCTV-13 新闻","https://tv.cctv.com/live/cctv13/",
                                        "CCTV-14 少儿","https://tv.cctv.com/live/cctvchild/",
                                        "CCTV-15 音乐","https://tv.cctv.com/live/cctv15/",
                                        "CCTV-16 奥林匹克","https://tv.cctv.com/live/cctv16/",
                                        "CCTV-17 农业农村","https://tv.cctv.com/live/cctv17/",
                                        "CCTV-5+ 体育赛事","https://tv.cctv.com/live/cctv5plus/",
                                        "CCTV Europe","https://tv.cctv.com/live/cctveurope/",
                                        "CCTV America","https://tv.cctv.com/live/cctvamerica/"]
                }
                ,
                {
                "name":"荔枝网",
                "url":["广东卫视","https://m.gdtv.cn/tvChannelDetail/43",
                                        "广东珠江","https://m.gdtv.cn/tvChannelDetail/44",
                                        "广东新闻","https://www.gdtv.cn/tvChannelDetail/45",
                                        "广东民生","https://www.gdtv.cn/tvChannelDetail/48",
                                        "广东体育","https://www.gdtv.cn/tvChannelDetail/47",
                                        "大湾区卫视","https://www.gdtv.cn/tvChannelDetail/51",
                                        "大湾区卫视海外","https://www.gdtv.cn/tvChannelDetail/46",
                                        "经济科教","https://www.gdtv.cn/tvChannelDetail/49",
                                        "广东影视","https://www.gdtv.cn/tvChannelDetail/53",
                                        "4k超高清","https://www.gdtv.cn/tvChannelDetail/16",
                                        "广东少儿","https://www.gdtv.cn/tvChannelDetail/54",
                                        "嘉佳卡通","https://www.gdtv.cn/tvChannelDetail/66",
                                        "南方购物","https://www.gdtv.cn/tvChannelDetail/42",
                                        "岭南戏曲","https://www.gdtv.cn/tvChannelDetail/15",
                                        "现代教育","https://www.gdtv.cn/tvChannelDetail/13",
                                        "广东移动","https://www.gdtv.cn/tvChannelDetail/74",
                                        "荔枝台","https://www.gdtv.cn/tvChannelDetail/100",
                                        "纪录片","https://www.gdtv.cn/tvChannelDetail/94",
                                        "GRTN健康频道","https://www.gdtv.cn/tvChannelDetail/99",
                                        "GRTN文化频道","https://www.gdtv.cn/tvChannelDetail/75",
                                        "GRTN生活频道","https://www.gdtv.cn/tvChannelDetail/102",
                                        "GRTN教育频道","https://www.gdtv.cn/tvChannelDetail/104"]
                },
                {
                "name":"河北",
                "url":[
                "河北卫视","https://web.cmc.hebtv.com/cms/rmt0336_html/19/19js/st/xdszb/10524916.shtml",
             "经济生活","https://web.cmc.hebtv.com/cms/rmt0336_html/19/19js/st/xdszb/10516507.shtml",
            "河北都市","https://web.cmc.hebtv.com/cms/rmt0336_html/19/19js/st/xdszb/10516509.shtml",
            "河北影视剧","https://web.cmc.hebtv.com/cms/rmt0336_html/19/19js/st/xdszb/10516510.shtml",
            "少儿科教","https://web.cmc.hebtv.com/cms/rmt0336_html/19/19js/st/xdszb/10516511.shtml",
            "河北公共","https://web.cmc.hebtv.com/cms/rmt0336_html/19/19js/st/xdszb/10516512.shtml",
             "农民频道","https://web.cmc.hebtv.com/cms/rmt0336_html/19/19js/st/xdszb/10516508.shtml",
                "河北卫视","https://www.hebtv.com/19/19js/st/xdszb/index.shtml"]
                }
                ]
            """;
    //https://api.cmc.hebtv.com/scms/api/com/article/getArticleList?catalogId=32557&siteId=1
    public static void loadFromJson(String jsonString ,SharedPreferences preferences){
        // JSON字符串

        // 使用Gson库将JSON字符串转换为Java对象
        Gson gson = new Gson();
        /*var aa="";
        for (var a:TVUrls.liveUrls2
             ) {
            Log.d("TAG","-----" +a.name);
            aa+="----\n";
            for (var b:a.getTvUrls()
                 ) {
                aa+="\""+b.name+"\",\""+b.url+"\",\n";
                System.out.println("\""+b.name+","+b.url+",");
                Log.d("TAG", "\""+b.name+","+b.url+",");
            }
        }*/
        var urls2=gson.fromJson(jsonString,TVUrlGroup[].class);
        TVUrlGroup cg=null;
        Gson gson2 = new Gson();
        try {

            cg = gson2.fromJson(preferences.getString("array_key", """
                   {
                    "name":"自定义",
                    "url":[]
                    }
                    """),TVUrlGroup.class);
        }catch (Exception ee){
            cg=new TVUrlGroup("自定义",new ArrayList<>());
        }
        var custom= new TVUrlGroup[]{cg};
        TVUrls.liveUrls2= ArrayUtils.concat(urls2,custom);

    }
    //public static Dictionary<String,String[]> liveUrls2=new Dictionary<String,String[]>();
    public static  TVUrlGroup[] liveUrls2 ;

}
