package otc.util.number;

import cn.hutool.core.util.IdUtil;
import cn.hutool.log.LogFactory;
import otc.api.alipay.Common;

import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class Number {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

    // 创建一个空实例对象，类需要用的时候才赋值
    private static Number instance = null;
    private final static ReentrantLock lock = new ReentrantLock();

    private static int count = 1;

    // 使用单例模式，不允许直接创建实例
    private Number() {
    }

    private static String getNowDateStr() {
        return sdf.format(new Date());
    }

    private static String now = null;

    // 单例模式--懒汉模式
    public static synchronized Number getInstance() {
        if (instance == null) {
            instance = new Number();
        }
        return instance;
    }

    private static final Random RANDOM = new Random();

    /**
     * <p>调用这个方法即可</p>
     *
     * @param haed 头部订单编号
     * @return
     * @throws UnknownHostException
     */
    private static String GetRandom(final String haed) {
        String Newnumber = null;
        String dateStr = getNowDateStr();


        //判断是时间是否相同
        if (dateStr.equals(now)) {
            lock.lock();//加锁
            try {
                if (count >= 10000) {
                    count = 1;
                }
                if (count < 10) {
                    Newnumber = haed + getNowDateStr() + "000" + count;
                } else if (count < 100) {
                    Newnumber = haed + getNowDateStr() + "00" + count;
                } else if (count < 1000) {
                    Newnumber = haed + getNowDateStr() + "0" + count;
                } else {
                    Newnumber = haed + getNowDateStr() + count;
                }
                count++;
            } catch (Exception e) {
            } finally {
                lock.unlock();
            }
        } else {
            count = 1;
            now = getNowDateStr();
            lock.lock();//加锁
            try {
                if (count >= 10000) {
                    count = 1;
                }
                if (count < 10) {
                    Newnumber = haed + getNowDateStr() + "000" + count;
                } else if (count < 100) {
                    Newnumber = haed + getNowDateStr() + "00" + count;
                } else if (count < 1000) {
                    Newnumber = haed + getNowDateStr() + "0" + count;
                } else {
                    Newnumber = haed + getNowDateStr() + count;
                }
                count++;
            } catch (Exception e) {
            } finally {
                lock.unlock();
            }
        }
        return Newnumber;//返回的值
    }


    public static String getAppOreder() {
        return GenerateOrderNo.Generate(Common.Deals.YUCHUANG_FLOW);
    }

    public static String getWitOrder() {
        return GenerateOrderNo.Generate("ZBW");
    }

    public static int nextInt(final int startInclusive, final int endExclusive) {
        if (startInclusive == endExclusive) {
            return startInclusive;
        }
        return startInclusive + RANDOM.nextInt(endExclusive - startInclusive);
    }

    public static String getImg() {
        String objectId = IdUtil.objectId().toUpperCase();
        return Common.Medium.IMG_NUMBER + objectId;
    }


    public static String getMedum() {
        String objectId = IdUtil.objectId().toUpperCase();
        return Common.Medium.MM_NUMBER + objectId;
    }


    public static String getBank() {
        String objectId = IdUtil.objectId().toUpperCase();
        return Common.Medium.BANK_NUMBER + objectId;
    }


    public static String getOrderQr() {
        String objectId = IdUtil.objectId().toUpperCase();
        return Common.Deals.ORDERDEAL + objectId;
    }

    public static String getOrderQrCh() {
        return GenerateOrderNo.Generate(Common.Deals.ORDERDEAL_CHANNEL);
    }


    public static String getRunOrderId() {
        String objectId = GetRandom(Common.Deals.ORDERRUN + nextInt(1000, 10000));
        return objectId;
    }
    public static String getBDC() {
        String objectId = IdUtil.objectId().toUpperCase();
        return Common.Deals.WITDBC + objectId;
    }


    public static String getBDR() {
        String objectId = IdUtil.objectId().toUpperCase();
        return Common.Deals.WITDBR + objectId;
    }


    public static String getRechargeQr() {
        String objectId = IdUtil.objectId().toUpperCase();
        return Common.Deals.RECHARGE + objectId;
    }

    public static String getRechargeCa() {
        String objectId = IdUtil.objectId().toUpperCase();
        return Common.Deals.RECHARGE_CARD + objectId;
    }

    public static String getExc() {
        String objectId = IdUtil.objectId().toUpperCase();
        return Common.Deals.ORDEREXCE + objectId;
    }


    public static String getWitOrderQr() {
            return GetRandom(Common.Deals.ORDERWIT_QR);
    }


    public static String getWitOrderCa() {
            return GetRandom(Common.Deals.ORDERWIT_CA);
    }


}
