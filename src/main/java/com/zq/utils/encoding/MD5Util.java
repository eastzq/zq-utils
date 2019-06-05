/**  
 * @Title: MD5EntryptHelper.java
 * @Package com.shine.commutil.encrypt.md5
 * @Description MD5加密处理类
 * @author 
 * @date 2019年2月25日 下午3:02:03
 * @Copyright
 */

package com.zq.utils.encoding;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @Description MD5加密处理类
 * @date 2019年2月25日 下午3:02:03
 * @seee
 * @since
 */
public class MD5Util {

    /**
     * <p>
     * Description:
     * </p>
     */
    public MD5Util() {

        // Auto-generated constructor stub
    }

    /**
     * @Description MD5加密处理
     * @param origin 源串请求
     * @return
     * @throws Exception String
     * @throws @author 
     * @date 2019年2月25日 下午3:49:12
     * @see
     */
    public static String MD5Encode(String origin) throws Exception {

        return MD5Encode(origin, null);
    }

    /**
     * @Description 进行md5加密
     * @param origin
     * @param charsetname
     * @return
     * @throws Exception String
     * @throws @author 
     * @date 2019年2月25日 下午3:48:45
     * @see
     */
    public static String MD5Encode(String origin, String charsetname) throws Exception {

        byte[] data = null;
        if (charsetname == null || "".contentEquals(charsetname)) {
            data = origin.getBytes();
        } else {
            try {
                data = origin.getBytes(charsetname);
            } catch (UnsupportedEncodingException e) {
                // Auto-generated catch block
                throw new Exception("不支持此字符集类型【" + charsetname + "】");
            }
        }

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("无法进行MD5摘要加密");
        }
        md.update(data, 0, data.length);
        byte[] retData = md.digest();
        // 转MD5
        StringBuffer retString = new StringBuffer();
        for (int i = 0; i < retData.length; i++) {
            retString.append(Integer.toHexString(0x0100 + (retData[i] & 0x00FF)).substring(1));
        }
        return retString.toString().toUpperCase();
    }

}
