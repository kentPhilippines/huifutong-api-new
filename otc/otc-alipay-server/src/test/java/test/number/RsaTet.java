package test.number;

import otc.util.RSAUtils;
import otc.util.encode.XRsa;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

public class RsaTet {

    private static final String UTF_8 = "utf-8";
    private static final String ENCODE_TYPE = "md5";

    public static void main(String[] args) {
        System.out.println(md5("6216698400004335670"));;


    }

    public static String md5(String a) {
        String c = "";
        MessageDigest md5;
        String result = "";
        try {
            md5 = MessageDigest.getInstance(ENCODE_TYPE);
            md5.update(a.getBytes(UTF_8));
            byte[] temp;
            temp = md5.digest(c.getBytes(UTF_8));
            for (int i = 0; i < temp.length; i++)
                result += Integer.toHexString((0x000000ff & temp[i]) | 0xffffff00).substring(6);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
        }
        return result;
    }

    private static void test1() {
        String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCvRvYBBFtPRCxDb+OedlBzzJSYaPnROvE5Xq0H3IuyxoweFoOrMGmlY79LWSXzTV20x5XRgREunFT5DVXZd96h9xpWQ6As/JkG1H8RsmzrWxhZilub3Nh39zAMoX9PoZ8X7+HKVOsUnvPSvw6JIO8duZKMh8wwIDgryPjHnYx3vQIDAQAB";


        String privatekey = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAK9G9gEEW09ELENv4552UHPMlJho+dE68TlerQfci7LGjB4Wg6swaaVjv0tZJfNNXbTHldGBES6cVPkNVdl33qH3GlZDoCz8mQbUfxGybOtbGFmKW5vc2Hf3MAyhf0+hnxfv4cpU6xSe89K/Dokg7x25koyHzDAgOCvI+MedjHe9AgMBAAECgYBPuTYR1lp13mtHrS/aFrSXRzlbbXwbl/S6hH6tcbo08awvz0vKZLXY3VrYOc9SMCFsJc/WQB+BlOftdAxVYvlm6/8d/+l9O08Udni+Axs/DXjkWwgnc0Z9CMTy29UEe3+AfJy+casW13C/gSiHJycVqciVHboz1ZgF4w8GSZNw4QJBAP9Fsocuo9FmUZr06gz+xqwJyjZallAyzUYYzyIjRDnMoLIjVewMWLTej4NzLETawWNsdP6jm5BhOgfTyTZ6KGUCQQCvxuGyxx016coAxWqkgCJBfdPepPrWIqDUnGaIa8Q5REQBDyFIYrgCySOUB1zlk2nNvBdK6yDXFkSplGuTq+B5AkEAxU46yk/bYPvD8ZCB4199cfXzLQXhjsVlclu0pf86Oa6XOnkjbVqxqcMojAKWYfXvagineTY+BF+R+gDRUmEjHQJBAJL/eIpA+A25OSSR9FAjlAI/DysXv2nKxcNoFFsr/IqDq3dTRts5K3ew7B8EVUzsjcPu82/E2M7nIX7Lz3dMeskCQHERgD5Pu/EhexxaSg4TnkTS+CL4PGDrviFDVYD7dsInCPKJzeArdbZvdo3c88KvPo8uI3wrHMo7AAY++XmBAHc=";
        String password = "345678";

        XRsa rsa = new XRsa(publicKey, privatekey);
/*
        String asas = rsa.privateDecrypt(password);
        System.out.println("解密结果：" + asas);
        System.out.println("明文长度：" + asas.length());*/
        String publicEncrypt = rsa.publicEncrypt(password);
        System.out.println("公钥加密后的内容：" + publicEncrypt);
        System.out.println("密文长度：" + publicEncrypt.length());
        String privateEncrypt = rsa.privateEncrypt(password);
		System.out.println("私钥加密后的内容：" + privateEncrypt);
		String privateDecrypt2 = rsa.privateDecrypt(publicEncrypt);
		System.out.println("私钥解密公钥内容：" + privateDecrypt2);
		String publicDecrypt = rsa.publicDecrypt(privateEncrypt);
		System.out.println("公钥解密私钥加密后内容：" + publicDecrypt);

	}

	static void test() {
		Map<String, String> createKeys = XRsa.createKeys(1024);
		System.out.println("公钥：" + createKeys.get("publicKey"));
		System.out.println("私钥：" + createKeys.get("privateKey"));
		System.out.println("公钥加密");
		String asas = "accountId=AC1000&amount=200&amount=200&amount=200&amount=200&amount=200&amount=200&amount=200&amount=200&amount=200&amount=200&amount=200&amount=200&amount=200&amount=200&amount=200&amount=200&amount=200&amount=200&amount=200&amount=200&amount=200&amount=200&amount=200&amount=200&amount=200&amount=200&amount=200&amount=200&amount=200&amount=200&amount=200&amount=200&amount=200&amount=200&amount=200&amount=200&amount=200&amount=200&amount=200&amount=200&amount=200&amount=200&amount=200&amount=200&amount=200&amount=200&amount=200&amount=200&backCard=65465443213213&userName=%E7%9C%9F%E5%AE%9E%E6%95%B0%E6%8D%AE%E6%B5%8B%E8%AF%95&userId=test&ipAddr=127.0.0.1";
		XRsa aa = new XRsa(createKeys.get("publicKey"), createKeys.get("privateKey"));
		System.out.println("明文长度：" + asas.length());
		String publicEncrypt = aa.publicEncrypt(asas);
		System.out.println("公钥加密后的内容：" + publicEncrypt);
		System.out.println("密文长度：" + publicEncrypt.length());
		String privateEncrypt = aa.privateEncrypt("accountId=AC1000&amount=200&backCard=65465443213213&amount=200&amount=200&amount=200&amount=200&amount=200&amount=200&amount=200&amount=200&userName=%E7%9C%9F%E5%AE%9E%E6%95%B0%E6%8D%AE%E6%B5%8B%E8%AF%95&userId=test&amount=200&amount=200&amount=200&amount=200&amount=200&amount=200&amount=200&amount=200&amount=200&amount=200&amount=200&amount=200&ipAddr=127.0.0.1");
		System.out.println("私钥加密后的内容：" + privateEncrypt);
		//String privateDecrypt = aa.privateDecrypt(privateEncrypt);
		//	System.out.println("私钥解密私钥的加密内容："+privateDecrypt);
		String privateDecrypt2 = aa.privateDecrypt(publicEncrypt);
		System.out.println("私钥解密公钥内容：" + privateDecrypt2);
		String publicDecrypt = aa.publicDecrypt(privateEncrypt);
		System.out.println("公钥解密私钥加密后内容：" + publicDecrypt);
	}
}
