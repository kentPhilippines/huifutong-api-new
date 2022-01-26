package alipay.manage.api.channel.util.zhaunshi;
/*******************************************************************************
 * Copyright (c) 2019. diamond-pay .
 ******************************************************************************/
public enum BankEnum {

    CCB("BK0001", "中国建设银行"),

    ABC("BK0002", "中国农业银行"),
    USDT("USDT", "USDT"),

    ICBC("BK0003", "中国工商银行"),

    BOC("BK0004", "中国银行"),

    CMBC("BK0005", "中国民生银行"),

    CMB("BK0006", "招商银行"),

    CIB("BK0007", "兴业银行"),

    BJBANK("BK0008", "北京银行"),

    COMM("BK0009", "交通银行"),

    CEB("BK0010", "中国光大银行"),

    SPABANK("BK0011", "平安银行"),

    PSBC("BK0012", "中国邮政储蓄银行"),

    CITIC("BK0013", "中信银行"),

    HXBANK("BK0014", "华夏银行"),

    GCB("BK0015", "广州银行"),

    SPDB("BK0016", "上海浦东发展银行"),

    GDB("BK0017", "广东发展银行"),

    SRCB("BK0018", "深圳农村商业银行"),

    SCCB("BK0019", "三门峡银行"),

    BGB("BK0020", "广西北部湾银行"),

    YQCCB("BK0021", "阳泉银行"),

    SHBANK("BK0022", "上海银行"),

    JLBANK("BK0023", "吉林银行"),

    SHRCB("BK0024", "上海农村商业银行"),

    WHCCB("BK0025", "威海市商业银行"),

    BANKWF("BK0026", "潍坊银行"),

    BOZK("BK0027", "周口银行"),

    CSRCB("BK0028", "常熟农村商业银行"),

    KORLABANK("BK0029", "库尔勒市商业银行"),

    SDEB("BK0030", "顺德农商银行"),

    HURCB("BK0031", "湖北省农村信用社"),

    WRCB("BK0032", "无锡农村商业银行"),

    BOCY("BK0033", "朝阳银行"),

    CZBANK("BK0034", "浙商银行"),

    HDBANK("BK0035", "邯郸银行"),

    TACCB("BK0036", "泰安市商业银行"),

    BOD("BK0037", "东莞银行"),

    LYCB("BK0038", "辽阳市商业银行"),

    GDRCC("BK0039", "广东省农村信用社联合社"),

    LZYH("BK0040", "兰州银行"),

    SXCB("BK0041", "绍兴银行"),

    BOHAIB("BK0042", "渤海银行"),

    CZCB("BK0043", "浙江稠州商业银行"),

    GZRCU("BK0044", "贵州省农村信用社"),

    ZJKCCB("BK0045", "张家口市商业银行"),

    BOJZ("BK0046", "锦州银行"),

    JLRCU("BK0047", "吉林农信"),

    BOP("BK0048", "平顶山银行"),

    SRBANK("BK0049", "上饶银行"),

    SDRCU("BK0050", "山东农信"),

    SJBANK("BK0051", "盛京银行"),

    HKB("BK0052", "汉口银行"),

    GXRCU("BK0053", "广西省农村信用"),

    NXRCU("BK0054", "宁夏黄河农村商业银行"),

    BSB("BK0055", "包商银行"),

    JSBANK("BK0056", "江苏银行"),

    NYNB("BK0057", "广东南粤银行"),

    GRCB("BK0058", "广州农商银行"),

    BOSZ("BK0059", "苏州银行"),

    HZCB("BK0060", "杭州银行"),

    DZBANK("BK0061", "德州银行"),

    ORBANK("BK0062", "鄂尔多斯银行"),

    HBC("BK0063", "湖北银行"),

    JXBANK("BK0064", "嘉兴银行"),

    ZYCBANK("BK0065", "遵义市商业银行"),

    BODD("BK0066", "丹东银行"),

    HNRCC("BK0067", "湖南省农村信用社"),

    AYCB("BK0068", "安阳银行"),

    DYCCB("BK0069", "东营市商业银行"),

    CZRCB("BK0070", "常州农村信用联社"),

    EGBANK("BK0071", "恒丰银行"),

    CDB("BK0072", "国家开发银行"),

    HSBK("BK0073", "衡水银行"),

    ZGCCB("BK0074", "自贡市商业银行"),

    CDCB("BK0075", "成都银行"),

    JNBANK("BK0076", "济宁银行"),

    TCRCB("BK0077", "江苏太仓农村商业银行"),

    NJCB("BK0078", "南京银行"),

    ZZBANK("BK0079", "郑州银行"),

    LYBANK("BK0080", "洛阳银行"),

    DYCB("BK0081", "德阳商业银行"),

    ZBCB("BK0082", "齐商银行"),

    FSCB("BK0083", "抚顺银行"),

    SCRCU("BK0084", "四川省农村信用"),

    HBRCU("BK0085", "河北省农村信用社"),

    LSCCB("BK0086", "乐山市商业银行"),

    LSBANK("BK0087", "莱商银行"),

    CBKF("BK0088", "开封市商业银行"),

    YDRCB("BK0089", "尧都农商行"),

    HNRCU("BK0090", "河南省农村信用"),

    YNRCC("BK0091", "云南省农村信用社"),

    H3CB("BK0092", "内蒙古银行"),

    YXCCB("BK0093", "玉溪市商业银行"),

    FDB("BK0094", "富滇银行"),

    JSRCU("BK0095", "江苏省农村信用联合社"),

    XYBANK("BK0096", "信阳银行"),

    HANABANK("BK0097", "韩亚银行"),

    SZSBK("BK0098", "石嘴山银行"),

    JINCHB("BK0099", "晋城银行JCBANK"),

    FXCB("BK0100", "阜新银行"),

    WHRCB("BK0101", "武汉农村商业银行"),

    HBYCBANK("BK0102", "湖北银行宜昌分行"),

    TZCB("BK0103", "台州银行"),

    JXRCU("BK0104", "江西省农村信用"),

    ZRCBANK("BK0105", "张家港农村商业银行"),

    JSB("BK0106", "晋商银行"),

    JZBANK("BK0107", "晋中市商业银行"),

    FJHXBC("BK0108", "福建海峡银行"),

    XCYH("BK0109", "许昌银行"),

    NXBANK("BK0110", "宁夏银行"),

    NHB("BK0111", "南海农村信用联社"),

    XXBANK("BK0112", "新乡银行"),

    HSBANK("BK0113", "徽商银行"),

    JJBANK("BK0114", "九江银行"),

    NHQS("BK0115", "农信银清算中心"),

    JRCB("BK0116", "江苏江阴农村商业银行"),

    HZCCB("BK0117", "湖州市商业银行"),

    MTBANK("BK0118", "浙江民泰商业银行"),

    LANGFB("BK0119", "廊坊银行"),

    ASCB("BK0120", "鞍山银行"),

    SXRCCU("BK0121", "陕西信合"),

    CCQTGB("BK0122", "重庆三峡银行"),

    DLB("BK0123", "大连银行"),

    DRCBCL("BK0124", "东莞农村商业银行"),

    NBBANK("BK0125", "宁波银行"),

    XABANK("BK0126", "西安银行"),

    KLB("BK0127", "昆仑银行"),

    CRCBANK("BK0128", "重庆农村商业银行"),

    BOYK("BK0129", "营口银行"),

    KSRB("BK0130", "昆山农村商业银行"),

    HRXJB("BK0131", "华融湘江银行"),

    GLBANK("BK0132", "桂林银行"),

    ARCU("BK0133", "安徽省农村信用社"),

    BOQH("BK0134", "青海银行"),

    CDRCB("BK0135", "成都农商银行"),

    QDCCB("BK0136", "青岛银行"),

    HKBEA("BK0137", "东亚银行"),

    GSRCU("BK0138", "甘肃省农村信用"),

    ZJNX("BK0139", "浙江省农村信用社联合社"),

    HBHSBANK("BK0140", "湖北银行黄石分行"),

    WZCB("BK0141", "温州银行"),

    TRCB("BK0142", "天津农商银行"),

    URMQCCB("BK0143", "乌鲁木齐市商业银行"),

    XLBANK("BK0144", "中山小榄村镇银行"),

    CSCB("BK0145", "长沙银行"),

    WJRCB("BK0146", "吴江农商银行"),

    QLBANK("BK0147", "齐鲁银行"),

    YBCCB("BK0148", "宜宾市商业银行"),

    ZJTLCB("BK0149", "浙江泰隆商业银行"),

    JHBANK("BK0150", "金华银行"),

    BHB("BK0151", "河北银行"),

    GZB("BK0152", "赣州银行"),

    BZMD("BK0153", "驻马店银行"),

    NBYZ("BK0154", "鄞州银行"),

    LSBC("BK0155", "临商银行"),

    GYCB("BK0156", "贵阳市商业银行"),

    CQBANK("BK0157", "重庆银行"),

    BOCD("BK0158", "承德银行"),

    BJRCB("BK0159", "北京农村商业银行"),

    NCB("BK0160", "南昌银行"),

    DAQINGB("BK0161", "龙江银行"),

    TCCB("BK0162", "天津银行"),

    CGNB("BK0163", "南充市商业银行"),

    CBBQS("BK0164", "城市商业银行资金清算中心"),

    XTB("BK0165", "邢台银行"),

    XMBANK("BK0166", "厦门银行"),

    FJNX("BK0167", "福建农村信用社"),

    XIB("BK0168", "厦门国际银行"),

    HSBC("BK0169", "汇丰银行"),

    CCAB("BK0170", "长安银行"),
    SZSNZ("BK0171", "苏州农村商业银行"),
    ALIPAY("ALIPAY", "支付宝-蚂蚁金服" );

    private String bankId;

    private String bankNameCn;

    BankEnum(String bankId, String bankNameCn) {
        this.bankId = bankId;
        this.bankNameCn = bankNameCn;
    }

    public String getBankId() {
        return bankId;
    }

    public String getBankNameCn() {
        return bankNameCn;
    }

}