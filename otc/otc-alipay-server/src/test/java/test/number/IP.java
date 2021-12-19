package test.number;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.text.UnicodeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HtmlUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;

import java.util.HashMap;
import java.util.Map;

public class IP {
    public static void main(String[] args)  {
       // https://tool.lu/ip/ajax.html
        findip1("122.114.170.209");
    //    run();
    }

    private static void run() {
        String ip = "27.192.0.0";
        String strUrl = "https://sp0.baidu.com/8aQDcjqpAAV3otqbppnN2DJv/api.php?query=";
        String centerUrl = "&co=&resource_id=5809&t=";
        String subUrl = "&ie=utf8&oe=gbk&cb=op_aladdin_callback&format=json&tn=baidu&cb=jQuery110206407212602169667_1607389234299";
        String timeUrl = "&_=";
        String url = strUrl + ip + centerUrl + System.currentTimeMillis() + subUrl + timeUrl + System.currentTimeMillis();
        String s = HttpUtil.get(url);
        System.out.println(UnicodeUtil.toString(s));
    }

   static  void findip(String ip){
        String findUrl = "http://ip.tongmengguo.com/?ip="+ip;
       String post = HttpUtil.get(findUrl);
       String s = HtmlUtil.removeHtmlTag(post, "<body></body>","<div></div>","<a></a>");
       System.out.println(s);
   }
    //uuid=ce79c666-4e69-4f84-cf09-881824f037f6; slim_session=%7B%22slim.flash%22%3A%5B%5D%7D; Hm_lvt_0fba23df1ee7ec49af558fb29456f532=1639641453,1639642173,1639703613,1639704402; _access=ad92977e77b33000418cb9583a688dee 44256602f059c71622297b0bcd933d9d; Hm_lpvt_0fba23df1ee7ec49af558fb29456f532=1639705204
    //uuid=ce79c666-4e69-4f84-cf09-881824f037f6; slim_session=%7B%22slim.flash%22%3A%5B%5D%7D; Hm_lvt_0fba23df1ee7ec49af558fb29456f532=1639641453,1639642173,1639703613,1639704402; _access=ad92977e77b33000418cb9583a688dee eeb2e5d155d99ba2842497d7e7b90b23"Hm_lpvt_0fba23df1ee7ec49af558fb29456f532=1639704411;
    //uuid=ce79c666-4e69-4f84-cf09-881824f037f6; slim_session=%7B%22slim.flash%22%3A%5B%5D%7D; Hm_lvt_0fba23df1ee7ec49af558fb29456f532=1639641453,1639642173,1639703613,1639704402; _access=ad92977e77b33000418cb9583a688dee44256602f059c71622297b0bcd933d9d; Hm_lpvt_0fba23df1ee7ec49af558fb29456f532=1639705204
    //uuid=ce79c666-4e69-4f84-cf09-881824f037f6; slim_session=%7B%22slim.flash%22%3A%5B%5D%7D; Hm_lvt_0fba23df1ee7ec49af558fb29456f532=1639641453,1639642173,1639703613,1639704402; _access=ad92977e77b33000418cb9583a688dee44256602f059c71622297b0bcd933d9d; Hm_lpvt_0fba23df1ee7ec49af558fb29456f532=1639705204
   //curl -L ip.tool.lu


    /**
     *      解密失败
      * @param ip
     */
    static void findip1(String ip){

        Map map = new HashMap<>();
        map.put("ip",ip);
       Long a =  System.currentTimeMillis()/1000;
       String s = UUID.fastUUID().toString();
       String s2 = MD5.md5("ad92977e77b33000418cb9583a688dee"+1639705204+"");
       System.out.println(s2);
       String cookie = "uuid=ce79c666-4e69-4f84-cf09-881824f037f6; slim_session=%7B%22slim.flash%22%3A%5B%5D%7D; Hm_lvt_0fba23df1ee7ec49af558fb29456f532=1639641453,1639642173,1639703613,1639704402; " +
               "Hm_lpvt_0fba23df1ee7ec49af558fb29456f532="+1639705204+";" +
               " _access=ad92977e77b33000418cb9583a688dee"+s2;

       System.out.println(cookie);

       HttpResponse execute =
               HttpRequest.post("https://tool.lu/ip/ajax.html")
                       .header("cookie",  cookie)
                       .header("referer", "https://tool.lu/ip")
                       .header("sec-ch-ua", "\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"96\", \"Google Chrome\";v=\"96\"")
                       .header("Content-Type", "application/x-www-form-urlencoded")
               .form(map).execute();
       //      .field("ip", "180.190.114.84")

       String body = execute.body();
       String decode = HttpUtil.decode(body, "UTF-8");

       String s1 = UnicodeUtil.toString(decode);


       System.out.println(s1);




   }


}