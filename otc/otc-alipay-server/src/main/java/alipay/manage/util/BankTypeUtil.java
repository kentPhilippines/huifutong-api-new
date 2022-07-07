package alipay.manage.util;

import alipay.manage.api.channel.util.zhaunshi.BankEnum;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
public class BankTypeUtil {
	private static final Log log = LogFactory.get();
	static   Map BANK_MAP = new ConcurrentHashMap();
	static   Map BANK_MAP_NAME = new ConcurrentHashMap();

	
	static {
		initbank();
	}


	private static void initbank() {
		log.info("【初始化银行卡code】");

		BANK_MAP.put("Bankcard", "Bankcard");
		BANK_MAP.put("Alipay", "Alipay");
		BANK_MAP.put("ALIPAY", "ALIPAY");
		BANK_MAP.put("Wechar", "Wechar");
		BANK_MAP.put("ABC", BankEnum.ABC.getBankId());
		BANK_MAP.put("USDT", BankEnum.USDT.getBankId());
		BANK_MAP.put("ARCU", BankEnum.ARCU.getBankId());
		BANK_MAP.put("PSBC", BankEnum.PSBC.getBankId());
		BANK_MAP.put("ASCB", BankEnum.ASCB.getBankId());
		BANK_MAP.put("AYCB", BankEnum.AYCB.getBankId());
		BANK_MAP.put("BANKWF", BankEnum.BANKWF.getBankId());
		BANK_MAP.put("BGB", BankEnum.BGB.getBankId());
		BANK_MAP.put("BHB", BankEnum.BHB.getBankId());
		BANK_MAP.put("BJBANK", BankEnum.BJBANK.getBankId());
		BANK_MAP.put("BJRCB", BankEnum.BJRCB.getBankId());
		BANK_MAP.put("BOC", BankEnum.BOC.getBankId());
		BANK_MAP.put("BOCD", BankEnum.BOCD.getBankId());
		BANK_MAP.put("BOCY", BankEnum.BOCY.getBankId());
		BANK_MAP.put("BOD", BankEnum.BOD.getBankId());
		BANK_MAP.put("BOHAIB", BankEnum.BOHAIB.getBankId());
		BANK_MAP.put("BOJZ", BankEnum.BOJZ.getBankId());
		BANK_MAP.put("BOP", BankEnum.BOP.getBankId());
		BANK_MAP.put("BOQH", BankEnum.BOQH.getBankId());
		BANK_MAP.put("BOSZ", BankEnum.BOSZ.getBankId());
		BANK_MAP.put("BOYK", BankEnum.BOYK.getBankId());
		BANK_MAP.put("BOZK", BankEnum.BOZK.getBankId());
		BANK_MAP.put("BSB", BankEnum.BSB.getBankId());
		BANK_MAP.put("BZMD", BankEnum.BZMD.getBankId());
		BANK_MAP.put("CBBQS", BankEnum.CBBQS.getBankId());
		BANK_MAP.put("CBKF", BankEnum.CBKF.getBankId());
		BANK_MAP.put("CCAB", BankEnum.CCAB.getBankId());
		BANK_MAP.put("CCB", BankEnum.CCB.getBankId());
		BANK_MAP.put("CCQTGB", BankEnum.CCQTGB.getBankId());
		BANK_MAP.put("CDB", BankEnum.CDB.getBankId());
		BANK_MAP.put("CDCB", BankEnum.CDCB.getBankId());
		BANK_MAP.put("CDRCB", BankEnum.CDRCB.getBankId());
		BANK_MAP.put("CEB", BankEnum.CEB.getBankId());
		BANK_MAP.put("CGNB", BankEnum.CGNB.getBankId());
		BANK_MAP.put("CIB", BankEnum.CIB.getBankId());






		BANK_MAP.put("CITIC", BankEnum.CITIC.getBankId());
		BANK_MAP.put("CMB", BankEnum.CMB.getBankId());
		BANK_MAP.put("CMBC", BankEnum.CMBC.getBankId());
		BANK_MAP.put("COMM", BankEnum.COMM.getBankId());
		BANK_MAP.put("CQBANK", BankEnum.CQBANK.getBankId());
		BANK_MAP.put("CRCBANK", BankEnum.CRCBANK.getBankId());
		BANK_MAP.put("CSCB", BankEnum.CSCB.getBankId());
		BANK_MAP.put("CSRCB", BankEnum.CSRCB.getBankId());
		BANK_MAP.put("CZBANK", BankEnum.CZBANK.getBankId());
		BANK_MAP.put("CZCB", BankEnum.CZCB.getBankId());
		BANK_MAP.put("CZRCB", BankEnum.CZRCB.getBankId());
		BANK_MAP.put("DAQINGB", BankEnum.DAQINGB.getBankId());
		BANK_MAP.put("DLB", BankEnum.DLB.getBankId());
		BANK_MAP.put("DRCBCL", BankEnum.DRCBCL.getBankId());
		BANK_MAP.put("DYCB", BankEnum.DYCB.getBankId());
		BANK_MAP.put("DYCCB", BankEnum.DYCCB.getBankId());
		BANK_MAP.put("DZBANK", BankEnum.DZBANK.getBankId());
		BANK_MAP.put("EGBANK", BankEnum.EGBANK.getBankId());
		BANK_MAP.put("FDB", BankEnum.FDB.getBankId());
		BANK_MAP.put("FJHXBC", BankEnum.FJHXBC.getBankId());
		BANK_MAP.put("FJNX", BankEnum.FJNX.getBankId());
		BANK_MAP.put("FSCB", BankEnum.FSCB.getBankId());
		BANK_MAP.put("GCB", BankEnum.GCB.getBankId());
		BANK_MAP.put("GDB", BankEnum.GDB.getBankId());
		BANK_MAP.put("GDRCC", BankEnum.GDRCC.getBankId());
		BANK_MAP.put("GLBANK", BankEnum.GLBANK.getBankId());
		BANK_MAP.put("GRCB", BankEnum.GRCB.getBankId());
		BANK_MAP.put("GSRCU", BankEnum.GSRCU.getBankId());
		BANK_MAP.put("GXRCU", BankEnum.GXRCU.getBankId());
		BANK_MAP.put("GYCB", BankEnum.GYCB.getBankId());
		BANK_MAP.put("GZB", BankEnum.GZB.getBankId());
		BANK_MAP.put("GZRCU", BankEnum.GZRCU.getBankId());
		BANK_MAP.put("HBC", BankEnum.HBC.getBankId());
		BANK_MAP.put("HANABANK", BankEnum.HANABANK.getBankId());
		BANK_MAP.put("H3CB", BankEnum.H3CB.getBankId());
		BANK_MAP.put("HBHSBANK", BankEnum.HBHSBANK.getBankId());
		BANK_MAP.put("HBRCU", BankEnum.HBRCU.getBankId());
		BANK_MAP.put("SPDB", BankEnum.SPDB.getBankId());


		BANK_MAP.put("HBYCBANK", BankEnum.HBYCBANK.getBankId());
		BANK_MAP.put("HZCCB", BankEnum.HZCCB.getBankId());
		BANK_MAP.put("HDBANK", BankEnum.HDBANK.getBankId());
		BANK_MAP.put("HKB", BankEnum.HKB.getBankId());
		BANK_MAP.put("HKBEA", BankEnum.HKBEA.getBankId());
		BANK_MAP.put("HNRCC", BankEnum.HNRCC.getBankId());


		BANK_MAP.put("HRXJB", BankEnum.HRXJB.getBankId());
		BANK_MAP.put("HZCB", BankEnum.HZCB.getBankId());
		BANK_MAP.put("ICBC", BankEnum.ICBC.getBankId());
		BANK_MAP.put("JHBANK", BankEnum.JHBANK.getBankId());
		BANK_MAP.put("JINCHB", BankEnum.JINCHB.getBankId());
		BANK_MAP.put("JLBANK", BankEnum.JLBANK.getBankId());
		BANK_MAP.put("JLRCU", BankEnum.JLRCU.getBankId());
		BANK_MAP.put("JNBANK", BankEnum.JNBANK.getBankId());
		BANK_MAP.put("JRCB", BankEnum.JRCB.getBankId());
		BANK_MAP.put("JSB", BankEnum.JSB.getBankId());
		BANK_MAP.put("JSBANK", BankEnum.JSBANK.getBankId());

		BANK_MAP.put("NJCB", BankEnum.NJCB.getBankId());
		BANK_MAP.put("MTBANK", BankEnum.MTBANK.getBankId());
		BANK_MAP.put("NBBANK", BankEnum.NBBANK.getBankId());
		BANK_MAP.put("NBYZ", BankEnum.NBYZ.getBankId());
		BANK_MAP.put("NCB", BankEnum.NCB.getBankId());
		BANK_MAP.put("NHB", BankEnum.NHB.getBankId());
		BANK_MAP.put("NHQS", BankEnum.NHQS.getBankId());
		BANK_MAP.put("NJCB", BankEnum.NJCB.getBankId());
		BANK_MAP.put("NXBANK", BankEnum.NXBANK.getBankId());
		BANK_MAP.put("NYNB", BankEnum.NYNB.getBankId());
		BANK_MAP.put("ORBANK", BankEnum.ORBANK.getBankId());



		BANK_MAP.put("TACCB", BankEnum.TACCB.getBankId());
		BANK_MAP.put("TCCB", BankEnum.TCCB.getBankId());
		BANK_MAP.put("TCRCB", BankEnum.TCRCB.getBankId());
		BANK_MAP.put("TZCB", BankEnum.TZCB.getBankId());
		BANK_MAP.put("URMQCCB", BankEnum.URMQCCB.getBankId());



		BANK_MAP.put("WHCCB", BankEnum.WHCCB.getBankId());
		BANK_MAP.put("WHRCB", BankEnum.WHRCB.getBankId());
		BANK_MAP.put("WJRCB", BankEnum.WJRCB.getBankId());
		BANK_MAP.put("WRCB", BankEnum.WRCB.getBankId());
		BANK_MAP.put("WRCB", BankEnum.WRCB.getBankId());
		BANK_MAP.put("WZCB", BankEnum.WZCB.getBankId());
		BANK_MAP.put("ZBCB", BankEnum.ZBCB.getBankId());


		BANK_MAP.put("ZBCB", BankEnum.ZBCB.getBankId());
        BANK_MAP.put("ZGCCB", BankEnum.ZGCCB.getBankId());
        BANK_MAP.put("ZJKCCB", BankEnum.ZJKCCB.getBankId());
        BANK_MAP.put("ZJNX", BankEnum.ZJNX.getBankId());
        BANK_MAP.put("ZJTLCB", BankEnum.ZJTLCB.getBankId());
        BANK_MAP.put("ZRCBANK", BankEnum.ZRCBANK.getBankId());
        BANK_MAP.put("ZYCBANK", BankEnum.ZYCBANK.getBankId());
        BANK_MAP.put("ZZBANK", BankEnum.ZZBANK.getBankId());
        BANK_MAP.put("SPABANK", BankEnum.SPABANK.getBankId());
        BANK_MAP.put("SPDB", BankEnum.SPDB.getBankId());
        BANK_MAP.put("HXBANK", BankEnum.HXBANK.getBankId());
        BANK_MAP.put("SZSNZ", BankEnum.SZSNZ.getBankId());

        BANK_MAP_NAME.put("ABC", BankEnum.ABC.getBankNameCn());
        BANK_MAP_NAME.put("SPDB", BankEnum.SPDB.getBankNameCn());
        BANK_MAP_NAME.put("SPABANK", BankEnum.SPABANK.getBankNameCn());
        BANK_MAP_NAME.put("ARCU", BankEnum.ARCU.getBankNameCn());
        BANK_MAP_NAME.put("PSBC", BankEnum.PSBC.getBankNameCn());
        BANK_MAP_NAME.put("ASCB", BankEnum.ASCB.getBankNameCn());
        BANK_MAP_NAME.put("AYCB", BankEnum.AYCB.getBankNameCn());
        BANK_MAP_NAME.put("BANKWF", BankEnum.BANKWF.getBankNameCn());
        BANK_MAP_NAME.put("BGB", BankEnum.BGB.getBankNameCn());
		BANK_MAP_NAME.put("BHB", BankEnum.BHB.getBankNameCn());
		BANK_MAP_NAME.put("BJBANK", BankEnum.BJBANK.getBankNameCn());
		BANK_MAP_NAME.put("BJRCB", BankEnum.BJRCB.getBankNameCn());
		BANK_MAP_NAME.put("BOC", BankEnum.BOC.getBankNameCn());
		BANK_MAP_NAME.put("BOCD", BankEnum.BOCD.getBankNameCn());
		BANK_MAP_NAME.put("BOCY", BankEnum.BOCY.getBankNameCn());
		BANK_MAP_NAME.put("BOD", BankEnum.BOD.getBankNameCn());
		BANK_MAP_NAME.put("BOHAIB", BankEnum.BOHAIB.getBankNameCn());
		BANK_MAP_NAME.put("BOJZ", BankEnum.BOJZ.getBankNameCn());
		BANK_MAP_NAME.put("BOP", BankEnum.BOP.getBankNameCn());
		BANK_MAP_NAME.put("BOQH", BankEnum.BOQH.getBankNameCn());
		BANK_MAP_NAME.put("BOSZ", BankEnum.BOSZ.getBankNameCn());
		BANK_MAP_NAME.put("BOYK", BankEnum.BOYK.getBankNameCn());
		BANK_MAP_NAME.put("BOZK", BankEnum.BOZK.getBankNameCn());
		BANK_MAP_NAME.put("BSB", BankEnum.BSB.getBankNameCn());
		BANK_MAP_NAME.put("BZMD", BankEnum.BZMD.getBankNameCn());
		BANK_MAP_NAME.put("CBBQS", BankEnum.CBBQS.getBankNameCn());
		BANK_MAP_NAME.put("CBKF", BankEnum.CBKF.getBankNameCn());
		BANK_MAP_NAME.put("CCAB", BankEnum.CCAB.getBankNameCn());
		BANK_MAP_NAME.put("CCB", BankEnum.CCB.getBankNameCn());
		BANK_MAP_NAME.put("CCQTGB", BankEnum.CCQTGB.getBankNameCn());
		BANK_MAP_NAME.put("CDB", BankEnum.CDB.getBankNameCn());
		BANK_MAP_NAME.put("CDCB", BankEnum.CDCB.getBankNameCn());
		BANK_MAP_NAME.put("CDRCB", BankEnum.CDRCB.getBankNameCn());
		BANK_MAP_NAME.put("CEB", BankEnum.CEB.getBankNameCn());
		BANK_MAP_NAME.put("CGNB", BankEnum.CGNB.getBankNameCn());
		BANK_MAP_NAME.put("CIB", BankEnum.CIB.getBankNameCn());






		BANK_MAP_NAME.put("CITIC", BankEnum.CITIC.getBankNameCn());
		BANK_MAP_NAME.put("CMB", BankEnum.CMB.getBankNameCn());
		BANK_MAP_NAME.put("CMBC", BankEnum.CMBC.getBankNameCn());
		BANK_MAP_NAME.put("COMM", BankEnum.COMM.getBankNameCn());
		BANK_MAP_NAME.put("CQBANK", BankEnum.CQBANK.getBankNameCn());
		BANK_MAP_NAME.put("CRCBANK", BankEnum.CRCBANK.getBankNameCn());
		BANK_MAP_NAME.put("CSCB", BankEnum.CSCB.getBankNameCn());
		BANK_MAP_NAME.put("CSRCB", BankEnum.CSRCB.getBankNameCn());
		BANK_MAP_NAME.put("CZBANK", BankEnum.CZBANK.getBankNameCn());
		BANK_MAP_NAME.put("CZCB", BankEnum.CZCB.getBankNameCn());
		BANK_MAP_NAME.put("CZRCB", BankEnum.CZRCB.getBankNameCn());
		BANK_MAP_NAME.put("DAQINGB", BankEnum.DAQINGB.getBankNameCn());
		BANK_MAP_NAME.put("DLB", BankEnum.DLB.getBankNameCn());
		BANK_MAP_NAME.put("DRCBCL", BankEnum.DRCBCL.getBankNameCn());
		BANK_MAP_NAME.put("DYCB", BankEnum.DYCB.getBankNameCn());
		BANK_MAP_NAME.put("DYCCB", BankEnum.DYCCB.getBankNameCn());
		BANK_MAP_NAME.put("DZBANK", BankEnum.DZBANK.getBankNameCn());
		BANK_MAP_NAME.put("EGBANK", BankEnum.EGBANK.getBankNameCn());
		BANK_MAP_NAME.put("FDB", BankEnum.FDB.getBankNameCn());
		BANK_MAP_NAME.put("FJHXBC", BankEnum.FJHXBC.getBankNameCn());
		BANK_MAP_NAME.put("FJNX", BankEnum.FJNX.getBankNameCn());
		BANK_MAP_NAME.put("FSCB", BankEnum.FSCB.getBankNameCn());
		BANK_MAP_NAME.put("GCB", BankEnum.GCB.getBankNameCn());
		BANK_MAP_NAME.put("GDB", BankEnum.GDB.getBankNameCn());
		BANK_MAP_NAME.put("GDRCC", BankEnum.GDRCC.getBankNameCn());
		BANK_MAP_NAME.put("GLBANK", BankEnum.GLBANK.getBankNameCn());
		BANK_MAP_NAME.put("GRCB", BankEnum.GRCB.getBankNameCn());
		BANK_MAP_NAME.put("GSRCU", BankEnum.GSRCU.getBankNameCn());
		BANK_MAP_NAME.put("GXRCU", BankEnum.GXRCU.getBankNameCn());
		BANK_MAP_NAME.put("GYCB", BankEnum.GYCB.getBankNameCn());
		BANK_MAP_NAME.put("GZB", BankEnum.GZB.getBankNameCn());
		BANK_MAP_NAME.put("GZRCU", BankEnum.GZRCU.getBankNameCn());
		BANK_MAP_NAME.put("HBC", BankEnum.HBC.getBankNameCn());
		BANK_MAP_NAME.put("HANABANK", BankEnum.HANABANK.getBankNameCn());
		BANK_MAP_NAME.put("H3CB", BankEnum.H3CB.getBankNameCn());
		BANK_MAP_NAME.put("HBHSBANK", BankEnum.HBHSBANK.getBankNameCn());
		BANK_MAP_NAME.put("HBRCU", BankEnum.HBRCU.getBankNameCn());



		BANK_MAP_NAME.put("HBYCBANK", BankEnum.HBYCBANK.getBankNameCn());
		BANK_MAP_NAME.put("HZCCB", BankEnum.HZCCB.getBankNameCn());
		BANK_MAP_NAME.put("HDBANK", BankEnum.HDBANK.getBankNameCn());
		BANK_MAP_NAME.put("HKB", BankEnum.HKB.getBankNameCn());
		BANK_MAP_NAME.put("HKBEA", BankEnum.HKBEA.getBankNameCn());
		BANK_MAP_NAME.put("HNRCC", BankEnum.HNRCC.getBankNameCn());


		BANK_MAP_NAME.put("HRXJB", BankEnum.HRXJB.getBankNameCn());
		BANK_MAP_NAME.put("HZCB", BankEnum.HZCB.getBankNameCn());
		BANK_MAP_NAME.put("ICBC", BankEnum.ICBC.getBankNameCn());
		BANK_MAP_NAME.put("JHBANK", BankEnum.JHBANK.getBankNameCn());
		BANK_MAP_NAME.put("JINCHB", BankEnum.JINCHB.getBankNameCn());
		BANK_MAP_NAME.put("JLBANK", BankEnum.JLBANK.getBankNameCn());
		BANK_MAP_NAME.put("JLRCU", BankEnum.JLRCU.getBankNameCn());
		BANK_MAP_NAME.put("JNBANK", BankEnum.JNBANK.getBankNameCn());
		BANK_MAP_NAME.put("JRCB", BankEnum.JRCB.getBankNameCn());
		BANK_MAP_NAME.put("JSB", BankEnum.JSB.getBankNameCn());
		BANK_MAP_NAME.put("JSBANK", BankEnum.JSBANK.getBankNameCn());

		BANK_MAP_NAME.put("NJCB", BankEnum.NJCB.getBankNameCn());
		BANK_MAP_NAME.put("MTBANK", BankEnum.MTBANK.getBankNameCn());
		BANK_MAP_NAME.put("NBBANK", BankEnum.NBBANK.getBankNameCn());
		BANK_MAP_NAME.put("NBYZ", BankEnum.NBYZ.getBankNameCn());
		BANK_MAP_NAME.put("NCB", BankEnum.NCB.getBankNameCn());
		BANK_MAP_NAME.put("NHB", BankEnum.NHB.getBankNameCn());
		BANK_MAP_NAME.put("NHQS", BankEnum.NHQS.getBankNameCn());
		BANK_MAP_NAME.put("NJCB", BankEnum.NJCB.getBankNameCn());
		BANK_MAP_NAME.put("NXBANK", BankEnum.NXBANK.getBankNameCn());
		BANK_MAP_NAME.put("NYNB", BankEnum.NYNB.getBankNameCn());
		BANK_MAP_NAME.put("ORBANK", BankEnum.ORBANK.getBankNameCn());


		BANK_MAP_NAME.put("TACCB", BankEnum.TACCB.getBankNameCn());
		BANK_MAP_NAME.put("TCCB", BankEnum.TCCB.getBankNameCn());
		BANK_MAP_NAME.put("TCRCB", BankEnum.TCRCB.getBankNameCn());
		BANK_MAP_NAME.put("TZCB", BankEnum.TZCB.getBankNameCn());
		BANK_MAP_NAME.put("URMQCCB", BankEnum.URMQCCB.getBankNameCn());


		BANK_MAP_NAME.put("WHCCB", BankEnum.WHCCB.getBankNameCn());
		BANK_MAP_NAME.put("USDT", BankEnum.USDT.getBankNameCn());
		BANK_MAP_NAME.put("WHRCB", BankEnum.WHRCB.getBankNameCn());
		BANK_MAP_NAME.put("WJRCB", BankEnum.WJRCB.getBankNameCn());
		BANK_MAP_NAME.put("WRCB", BankEnum.WRCB.getBankNameCn());
		BANK_MAP_NAME.put("WRCB", BankEnum.WRCB.getBankNameCn());
		BANK_MAP_NAME.put("WZCB", BankEnum.WZCB.getBankNameCn());
		BANK_MAP_NAME.put("ZBCB", BankEnum.ZBCB.getBankNameCn());
		BANK_MAP_NAME.put("SPDB", BankEnum.SPDB.getBankNameCn());

        BANK_MAP_NAME.put("ZBCB", BankEnum.ZBCB.getBankNameCn());
        BANK_MAP_NAME.put("ZGCCB", BankEnum.ZGCCB.getBankNameCn());
        BANK_MAP_NAME.put("ZJKCCB", BankEnum.ZJKCCB.getBankNameCn());
        BANK_MAP_NAME.put("ZJNX", BankEnum.ZJNX.getBankNameCn());
        BANK_MAP_NAME.put("ZJTLCB", BankEnum.ZJTLCB.getBankNameCn());
        BANK_MAP_NAME.put("ZRCBANK", BankEnum.ZRCBANK.getBankNameCn());
        BANK_MAP_NAME.put("ZYCBANK", BankEnum.ZYCBANK.getBankNameCn());
        BANK_MAP_NAME.put("ZZBANK", BankEnum.ZZBANK.getBankNameCn());
        BANK_MAP_NAME.put("HXBANK", BankEnum.HXBANK.getBankNameCn());
        BANK_MAP_NAME.put("SZSNZ", BankEnum.SZSNZ.getBankNameCn());
        BANK_MAP_NAME.put("ALIPAY", BankEnum.ALIPAY.getBankNameCn());
        BANK_MAP_NAME.put("Alipay", BankEnum.ALIPAY.getBankNameCn());
    }


	public static String getBank(String bankcode) {
		return (String) BANK_MAP.get(bankcode);
	}
	public static String getBankName(String bankcode) {
		return (String) BANK_MAP_NAME.get(bankcode);
	}







}
