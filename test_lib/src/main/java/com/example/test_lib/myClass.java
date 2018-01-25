package com.example.test_lib;

public class myClass {

    public static void main(String[] args) {
//        System.out.print(0x19 & 0xFF);
        // 创建12个字节的字节缓冲区
//            ByteBuffer bb = ByteBuffer.wrap(new byte[12]);
//            // 存入字符串
//            bb.asCharBuffer().put("abdcef");
//            System.out.println(Arrays.toString(bb.array()));
//
//            // 反转缓冲区
//            bb.rewind();
//            // 设置字节存储次序
//            bb.order(ByteOrder.BIG_ENDIAN);
//            bb.asCharBuffer().put("abcdef");
//            System.out.println(Arrays.toString(bb.array()));
//
//            // 反转缓冲区
//            bb.rewind();
//            // 设置字节存储次序
//            bb.order(ByteOrder.LITTLE_ENDIAN);
//            bb.asCharBuffer().put("abcdef");
//            System.out.println(Arrays.toString(bb.array()));


//        System.out.println(byteToBit((byte) 45));
//        System.out.println(byteToBit((byte) 900));
//        System.out.println(byteToBit((byte) 388));
//        System.out.println((byte) 388);
//        System.out.println(1 << 3);
//        System.out.println(2 << 3);
//
//
//        System.out.println(bytes2Int(new byte[]{12, 14, 15}));

        byte[] toByte = new byte[]{67, 56, 48, 95, 86, 49, 46, 48};
//        byte[] toByte = new byte[]{-113, 31, 117, 0, 0, 120, -38, -25};
//        byte[] toByte = new byte[]{48, 55};
        System.out.println(new String(toByte));

//        System.out.println(bytesToHexString(toByte));
    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString().toUpperCase();
    }

    public static String byteToBit(byte b) {
        return "" + (byte) ((b >> 7) & 0x1) +
                (byte) ((b >> 6) & 0x1) +
                (byte) ((b >> 5) & 0x1) +
                (byte) ((b >> 4) & 0x1) +
                (byte) ((b >> 3) & 0x1) +
                (byte) ((b >> 2) & 0x1) +
                (byte) ((b >> 1) & 0x1) +
                (byte) ((b >> 0) & 0x1);
    }


    private static int bytes2Int(byte[] bs) {
        int retVal = 0;
        int len = bs.length < 4 ? bs.length : 4;
        for (int i = 0; i < len; i++) {
            retVal |= (bs[i] & 0xFF) << ((i & 0x03) << 3);
        }
        return retVal;

        // 如果确定足4位，可直接返回值
//        return (bs[0]&0xFF) | ((bs[1] & 0xFF)<<8) | ((bs[2] & 0xFF)<<16) | ((bs[3] & 0xFF)<<24);
    }

}
