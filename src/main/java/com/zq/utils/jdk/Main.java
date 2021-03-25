/**  
 * @Title: Main.java
 * @Package com.zq.utils.jdk
 * @Description 
 * @author zq
 * @date 2021年3月24日 下午5:55:52
 * @Copyright 
 */

package com.zq.utils.jdk;

/**
 * @Description 
 * @author zq
 * @date 2021年3月24日 下午5:55:52
 * @see
 * @since 2021年3月24日 下午5:55:52
 */

/**
 * 正整数扩容高位补0；负整数高位补1；so 下边是错误答案。
 * @Description 
 * @author zq
 * @date 2021年3月25日 下午6:57:32
 * @see
 * @since 2021年3月25日 下午6:57:32
 */
public class Main {

    public Main() {
    }

    public static void main(String[] args) {
       System.out.println(new Main().solution("-1"));
    }
    public String solution(String t) {
        try {
            Short.parseShort(t);            
        }catch (NumberFormatException e) {
            return "请输入正确的数字。";
        }
        short data = Short.parseShort(t);
        byte tmp = -1;
        Byte[] bytes = new Byte[2];
        bytes[1] = (byte) (data&tmp);
        bytes[0] = (byte) ((data>>8)&tmp);
        String[] binaryStrs = new String[16];
        
        short tdata =data;
        short ct = 1;
        for(int i= 0;i<16;i++) {
            short m = (short) (tdata&ct);
            binaryStrs[15-i] = String.valueOf(m);
            tdata = (short) (tdata>>1);
        }
        String[] hexStrs = new String[4];
        short hdata = data;
        short ch = 15;
        for(int i= 0;i<4;i++) {
            short m = (short) (hdata&ch);
            hexStrs[3-i] = getHexData(m);
            hdata = (short) (hdata>>4);
        }
        System.out.println(Integer.toBinaryString(-1));
        System.out.println(Integer.toHexString(-1));
        return printStrs(binaryStrs)+","+ printStrs(hexStrs);
    }
    
    public  String getHexData(short m) {
        byte A = 0b00001010;
        byte B = 0b00001011;
        byte C = 0b00001100;
        byte D = 0b00001101;
        byte E = 0b00001110;
        byte F = 0b00001111;
        if(m==A) {
            return "A";
        }else if(m==B) {
            return "B";
        }else if(m==C) {
            return "C";
        }else if(m==D) {
            return "D";
        }else if(m==E) {
            return "E";
        }else if(m==F) {
            return "F";
        }else {
            return String.valueOf(m);
        }
    }
    
    public String printStrs(String[] strs) {
        StringBuilder t= new StringBuilder();
        for(int i=0;i<strs.length;i++){
            t.append(strs[i]);
        }
        return t.toString();
    }
}
