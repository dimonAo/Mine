package com.example.test_lib;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

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

//        byte[] toByte = new byte[]{67, 56, 48, 95, 86, 49, 46, 48};
////        byte[] toByte = new byte[]{-113, 31, 117, 0, 0, 120, -38, -25};
////        byte[] toByte = new byte[]{48, 55};
//        System.out.println(new String(toByte));


//        System.out.println((5000 * 100f / 8000 * 100f) / 100f);


        betweenDays("2017/12/02", "2018/03/12");
        betweenDaysForWeek("2017/12/02", "2018/03/12");

//        System.out.println(bytesToHexString(toByte));
    }

    private static final long ONE_DAY_MS = 24 * 60 * 60 * 1000;

    /**
     * 计算两个日期之间的日期
     *
     * @param startTime
     * @param endTime
     */
    private static void betweenDays(long startTime, long endTime, long mills_select, int code) {
        Date date_start = new Date(startTime);
        Date date_end = new Date(endTime);

        //计算日期从开始时间于结束时间的0时计算
        Calendar fromCalendar = Calendar.getInstance();
        fromCalendar.setTime(date_start);
        fromCalendar.set(Calendar.HOUR_OF_DAY, 0);
        fromCalendar.set(Calendar.MINUTE, 0);
        fromCalendar.set(Calendar.SECOND, 0);
        fromCalendar.set(Calendar.MILLISECOND, 0);
        Calendar toCalendar = Calendar.getInstance();
        toCalendar.setTime(date_end);
        toCalendar.set(Calendar.HOUR_OF_DAY, 0);
        toCalendar.set(Calendar.MINUTE, 0);
        toCalendar.set(Calendar.SECOND, 0);
        toCalendar.set(Calendar.MILLISECOND, 0);
        int s = (int) ((toCalendar.getTimeInMillis() - fromCalendar.getTimeInMillis()) / (ONE_DAY_MS));
        if (s > 0) {
            for (int i = 0; i <= s; i++) {
                long todayDate = fromCalendar.getTimeInMillis() + i * ONE_DAY_MS;
                /**
                 * yyyy-MM-dd E :2012-09-01
                 */
//                Log.i("打印日期", getCustonFormatTime(todayDate, "yyyy-MM-dd"));
                System.out.println(getCustonFormatTime(todayDate, "yyyy/MM/dd"));
            }
        } else {//此时在同一天之内
//            Log.i("打印日期", getCustonFormatTime(startTime, "yyyy-MM-dd"));
            System.out.println(getCustonFormatTime(startTime, "yyyy/MM/dd"));
        }
    }

    /**
     * 计算两个日期之间的日期
     *
     * @param startTime
     * @param endTime
     */
    private static void betweenDays(String startTime, String endTime) {
        Date date_start = new Date(convert2long(startTime, "yyyy/MM/dd"));
        Date date_end = new Date(convert2long(endTime, "yyyy/MM/dd"));

        //计算日期从开始时间于结束时间的0时计算
        Calendar fromCalendar = Calendar.getInstance();
        fromCalendar.setTime(date_start);
        fromCalendar.set(Calendar.HOUR_OF_DAY, 0);
        fromCalendar.set(Calendar.MINUTE, 0);
        fromCalendar.set(Calendar.SECOND, 0);
        fromCalendar.set(Calendar.MILLISECOND, 0);
        Calendar toCalendar = Calendar.getInstance();
        toCalendar.setTime(date_end);
        toCalendar.set(Calendar.HOUR_OF_DAY, 0);
        toCalendar.set(Calendar.MINUTE, 0);
        toCalendar.set(Calendar.SECOND, 0);
        toCalendar.set(Calendar.MILLISECOND, 0);
        int s = (int) ((toCalendar.getTimeInMillis() - fromCalendar.getTimeInMillis()) / (ONE_DAY_MS));
        if (s > 0) {
            for (int i = 0; i <= s; i++) {
                long todayDate = fromCalendar.getTimeInMillis() + i * ONE_DAY_MS;
//                Log.i("打印日期", getCustonFormatTime(todayDate, "yyyy-MM-dd"));
                System.out.println(getCustonFormatTime(todayDate, "yyyy/MM/dd"));
            }
        } else {//此时在同一天之内
//            Log.i("打印日期", getCustonFormatTime(startTime, "yyyy-MM-dd"));
//            System.out.println(getCustonFormatTime(startTime, "yyyy/MM/dd"));
            System.out.println(startTime);
        }
    }


    /**
     * 将日期格式的字符串转换为长整型
     *
     * @param date
     * @param format
     * @return
     */
    public static long convert2long(String date, String format) {
        try {
//            if (!.isEmpty(date)) {
//                if (StringUtils.isEmpty(format))
//                    format = SimpleDateUtil.TIME_FORMAT;
            SimpleDateFormat sf = new SimpleDateFormat(format);
            return sf.parse(date).getTime();
//            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0l;
    }


    /**
     * 格式化传入的时间
     *
     * @param time      需要格式化的时间
     * @param formatStr 格式化的格式
     * @return
     */
    public static String getCustonFormatTime(long time, String formatStr) {
        SimpleDateFormat format = new SimpleDateFormat(formatStr);
        Date d1 = new Date(time);
        return format.format(d1);
    }

    public static void betweenDaysForWeek(String mstart, String mend) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        long CONST_WEEK = 1000 * 3600 * 24 * 7;//一周毫秒数
        try {
            Date start = sdf.parse(mstart);
            Date end = sdf.parse(mend);

            Calendar startCal = Calendar.getInstance();
            startCal.setTime(start);

            Calendar endCal = Calendar.getInstance();
            endCal.setTime(end);

            Date now = new Date();
            Calendar nowCal = Calendar.getInstance();
            nowCal.setTime(now);

            //查找开始日期的那个星期的第一天
            int dayOfWeek = startCal.get(Calendar.DAY_OF_WEEK);
            startCal.add(Calendar.DATE, -(dayOfWeek - 1));//周日是第一天 所以-1
            //查找结束日期的那个星期的第一天
            dayOfWeek = endCal.get(Calendar.DAY_OF_WEEK);
            endCal.add(Calendar.DATE, 7 - (dayOfWeek - 1));

            //计算总共多少周
            int total = (int) ((endCal.getTimeInMillis() - startCal.getTimeInMillis()) / CONST_WEEK);


            for (int i = 0; i < total; i++) {
                HashMap<String, String> week = new HashMap<String, String>();
                week.put("index", String.valueOf(i + 1));
                week.put("title", "第" + (i + 1) + "周");
                startCal.add(Calendar.DATE, 1);
                String time = sdf.format(startCal.getTime());//第一天
                startCal.add(Calendar.DATE, 6);
                time += "~" + sdf.format(startCal.getTime());//最后一天
                week.put("time", time);
//                        weeks.add(week);
                System.out.println("第" + (i + 1) + "周 : " + time);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
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
