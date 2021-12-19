package test.number;

import otc.util.RSAUtils;

import java.util.List;

public class py {

    public static void main(String[] args) {
        List<String> strings = RSAUtils.genKeyPair();

        for (String s : strings){
            System.out.println(s);
        }

    }
}
