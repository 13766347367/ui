package com.sanhui.ui.serial;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SetSerialData {


    private final StringBuffer  licensebuilder = new StringBuffer ();
    private final StringBuffer  weightbuilder = new StringBuffer ();
    private final String hand1 = "FE5C4B89";
    private final String hand2 = "0000006500000000";
    private String licensehand3 = "0000000100FFFF";
    private String weighthand3 = "0000000200FFFF";
    private final String foot = "FFFF";

    private String weightdata;
    public String weightdata2;
    public   String setport;
    private  int  iweightdata;
    public   int  iweightdata1=0;
    private List<String> list=new ArrayList<>();

    //发送显示屏的车牌参数
    public  byte[] licensebyte(String license)  {

        if(license != null) {
            licensebuilder.append(hand1);
            licensebuilder.append(numToHex8(32));
            licensebuilder.append(hand2);
            licensebuilder.append(numToHex8(13));
            licensebuilder.append(licensehand3);
            licensebuilder.append(numToHex8(8 ));
            licensebuilder.append(license);
            licensebuilder.append(foot);

        }
        byte[] outHexStrNolicense = hexStringToByteArray(licensebuilder.toString());

        licensebuilder.delete(0,licensebuilder.length());

        return outHexStrNolicense;
    }


    //发送显示屏的地磅参数
    public byte[] weightbyte(String weight) throws UnsupportedEncodingException {

        if(weight !=null ) {
            weightbuilder.append(hand1);
            weightbuilder.append(numToHex8(weight.length()+24));
            weightbuilder.append(hand2);
            weightbuilder.append(numToHex8(weight.length()+5));
            weightbuilder.append(weighthand3);
            weightbuilder.append(numToHex8(weight.length() ));
            weightbuilder.append(ch(weight));
            weightbuilder.append(foot);
        }
        byte[] outHexStrNoWeigh = hexStringToByteArray(weightbuilder.toString());

        weightbuilder.delete(0,weightbuilder.length());

        return outHexStrNoWeigh;
    }


    //16进制字符串转字节数组
    public static byte[] hexStringToByteArray(String hexString) {
        hexString = hexString.replaceAll(" ", "");
        int len = hexString.length();
        byte[] bytes = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            // 两位一组，表示一个字节,把这样表示的16进制字符串，还原成一个字节
            bytes[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4) + Character
                    .digit(hexString.charAt(i+1), 16));
        }
        return bytes;
    }


    //十进制转16进制字符串
    public static String numToHex8(int b) {
        return String.format("%02x", b);// 2表示需要两个16进制数
    }

    //判断第一个字符是否为中文
    public  boolean isChineseChar(char c) {
        return String.valueOf(c).matches("[\u4e00-\u9fa5]*");
    }
    //判断第一个字符是否为数字
    public  boolean isNumeric(char str){ return String.valueOf(str).matches("[0-9]*"); }

    //将字符串转为gb2312编码
    public  String ch(String gb) throws UnsupportedEncodingException {
        byte[] b=gb.getBytes("GB2312");
        String hexStr =  bytesToHexFun1(b);
        return hexStr;
    }



    public static String bytesToHexFun1(byte[] bytes) {
        char[] HEX_CHAR = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        // 一个byte为8位，可用两个十六进制位标识
        char[] buf = new char[bytes.length * 2];
        int a = 0;
        int index = 0;
        for(byte b : bytes) { // 使用除与取余进行转换
            if(b < 0) {
                a = 256 + b;
            } else {
                a = b;
            }
            buf[index++] = HEX_CHAR[a / 16];
            buf[index++] = HEX_CHAR[a % 16];
        }
        return new String(buf);
    }

    //得到重量，保存进数组
    public void getweight(String str){
        String[] strings = str.split("\\s+");  //截取重量
        if(strings.length>2 && !strings[1].equals("")){
            weightdata=strings[1];
            if(isNumeric(weightdata.charAt(0))) {
                setport = weightdata;
                iweightdata = Integer.parseInt(weightdata);
                if (iweightdata != 0 && iweightdata > 500) {

                    list.add(weightdata);

                } else if (iweightdata == 0 || iweightdata < 500) {
                    if (list.size() > 0) {

                        weightdata2 = frequencyOfListElement(list);

                        if (!weightdata2.equals("")) {
                            iweightdata1 = Integer.parseInt(weightdata2);
                        }

                        list.clear();
                    }
                }
            }
        }
    }



    //处理list里的数据，判断数据里存在最多的数据
    public static String frequencyOfListElement( List<String> list ) {
//        System.out.println(list.size());
        int max = 0;
        String maxNum = "";
        Map<String,Integer> map = new HashMap<>();
        for (String str : list) {
            if (map.get(str) != null){
                map.put(str,map.get(str)+1);
                if (map.get(str) > max){
                    max = map.get(str);
                    maxNum = str;
                }
            }else {
                map.put(str,1);
            }
        }
        return maxNum;
    }






}
