package test.number;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import java.io.*;
import java.net.*;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddressUtils {

    /**
     * 获取本机的内网ip地址
     *
     * @return
     * @throws SocketException
     */
    public String getInnetIp() throws SocketException {
        String localip = null;// 本地IP，如果没有配置外网IP则返回它
        String netip = null;// 外网IP
        Enumeration<NetworkInterface> netInterfaces;
        netInterfaces = NetworkInterface.getNetworkInterfaces();
        InetAddress ip = null;
        boolean finded = false;// 是否找到外网IP
        while (netInterfaces.hasMoreElements() && !finded) {
            NetworkInterface ni = netInterfaces.nextElement();
            Enumeration<InetAddress> address = ni.getInetAddresses();
            while (address.hasMoreElements()) {
                ip = address.nextElement();
                if (!ip.isSiteLocalAddress() && !ip.isLoopbackAddress() && ip.getHostAddress().indexOf(":") == -1) {// 外网IP
                    netip = ip.getHostAddress();
                    finded = true;
                    break;
                } else if (ip.isSiteLocalAddress() && !ip.isLoopbackAddress() && ip.getHostAddress().indexOf(":") == -1) {// 内网IP
                    localip = ip.getHostAddress();
                }
            }
        }
        if (netip != null && !"".equals(netip)) {
            return netip;
        } else {
            return localip;
        }
    }

    /**
     * 获取本机的外网ip地址
     *
     * @return
     */
    public String getV4IP() {
        String ip = "";
        String chinaz = "http://ip.chinaz.com";

        StringBuilder inputLine = new StringBuilder();
        String read = "";
        URL url = null;
        HttpURLConnection urlConnection = null;
        BufferedReader in = null;
        try {
            url = new URL(chinaz);
            urlConnection = (HttpURLConnection) url.openConnection();
            in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
            while ((read = in.readLine()) != null) {
                inputLine.append(read + "\r\n");
            }
//System.out.println(inputLine.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        Pattern p = Pattern.compile("\\<dd class\\=\"fz24\">(.*?)\\<\\/dd>");
        Matcher m = p.matcher(inputLine.toString());
        if (m.find()) {
            String ipstr = m.group(1);
            ip = ipstr;
//System.out.println(ipstr);
        }
        return ip;
    }


    /**
     * 解析ip地址
     * 设置访问地址为https://api.map.baidu.com/location/ip
     * 设置请求参数为ip=[ip地址]  ak=[百度Web服务API的密钥]  coor=[经纬度的坐标类型]，详情见文档http://lbsyun.baidu.com/index.php?title=webapi/ip-api
     * 设置解码方式为UTF-8
     *
     * @param content   请求的参数
     * @param encoding 服务器端请求编码
     * @return
     * @throws UnsupportedEncodingException
     */
    public String getAddresses(String content, String encoding) throws UnsupportedEncodingException {
//设置访问地址
        String urlStr = "https://api.map.baidu.com/location/ip";
// 从http://whois.pconline.com.cn取得IP所在的省市区信息
        String returnStr = this.getResult(urlStr, content, encoding);
        JSONObject jsonObject = JSONUtil.parseObj(content);
        if (returnStr != null) {
// 处理返回的省市区信息
            System.out.println(jsonObject.get("content"));
            return jsonObject.toString();
        }
        return null;
    }


    /**
     * 访问目标地址并获取返回值
     *
     * @param urlStr   请求的地址
     * @param content  请求的参数
     * @param encoding 服务器端请求编码
     * @return
     */
    private String getResult(String urlStr, String content, String encoding) {
        URL url = null;
        HttpURLConnection connection = null;
        try {
            url = new URL(urlStr);
            // 采用HttpURLConnection建立连接
            connection = (HttpURLConnection) url.openConnection();
            // 设置超时时间，毫秒
            connection.setConnectTimeout(2000);
            // 设置读取数据超时时间，毫秒
            connection.setReadTimeout(33000);
            //打开输出、输入流设置方法
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            // 是否缓存
            connection.setUseCaches(false);
            // 打开连接
            connection.connect();
            // 打开输出流往对端服务器写数据
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            out.writeBytes(content);// 写数据,也就是提交你的表单 name=xxx&pwd=xxx
            out.flush();// 刷新
            out.close();// 关闭输出流
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), encoding));// 往对端写完数据对端服务器返回数据
// ,以BufferedReader流来读取
            StringBuffer buffer = new StringBuffer();
            String line = "";
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            reader.close();
            return buffer.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();// 关闭连接
            }
        }
        return null;
    }


    /**
     * 测试方法
     * 获取本机的内网ip，外网ip和指定ip的地址
     *
     * @param args
     */
    public static void main(String[] args) {
        AddressUtils addressUtils = new AddressUtils();
//step1.获得内网ip和外网ip，并输出到控制台
        String ip1 = "122.114.170.209";

//step2.根据外网ip地址，得到市级地理位置
        String address = "";
        try {
            //ak是百度的Web服务API的密钥，要自己申请哦，地址 http://lbsyun.baidu.com/apiconsole/key
            address = addressUtils.getAddresses("ip=" + ip1 + "&ak=************&coor=bd09ll", "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
