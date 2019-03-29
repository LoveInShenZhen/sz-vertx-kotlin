package sz.scaffold.tools

import jodd.util.StringUtil
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

//
// Created by kk on 17/9/10.
//
object StringChecker {
    /*********************************** 身份证验证开始  */
    /**
     * 身份证号码验证 1、号码的结构 公民身份号码是特征组合码,由十七位数字本体码和一位校验码组成排列顺序从左至右依次为：六位数字地址码,
     * 八位数字出生日期码,三位数字顺序码和一位数字校验码 2、地址码(前六位数）
     * 表示编码对象常住户口所在县(市、旗、区)的行政区划代码,按GB/T2260的规定执行 3、出生日期码（第七位至十四位）
     * 表示编码对象出生的年、月、日,按GB/T7408的规定执行,年、月、日代码之间不用分隔符 4、顺序码（第十五位至十七位）
     * 表示在同一地址码所标识的区域范围内,对同年、同月、同日出生的人编定的顺序号, 顺序码的奇数分配给男性,偶数分配给女性 5、校验码（第十八位数）
     * （1）十七位数字本体码加权求和公式 S = Sum(Ai * Wi), i = 0, ... , 16 ,先对前17位数字的权求和
     * Ai:表示第i位置上的身份证号码数字值 Wi:表示第i位置上的加权因子 Wi: 7 9 10 5 8 4 2 1 6 3 7 9 10 5 8 4
     * 2 （2）计算模 Y = mod(S, 11) （3）通过模得到对应的校验码 Y: 0 1 2 3 4 5 6 7 8 9 10 校验码: 1 0
     * X 9 8 7 6 5 4 3 2
     */

    /**
     * 功能：身份证的有效验证

     * @param IDStr 身份证号
     * *
     * @return 有效：返回"" 无效：返回String信息
     */
    fun IDCardValidate(IDStr: String): String {
        try {
            val ValCodeArr = arrayOf("1", "0", "X", "9", "8", "7", "6", "5", "4", "3", "2")
            val Wi = arrayOf("7", "9", "10", "5", "8", "4", "2", "1", "6", "3", "7", "9", "10", "5", "8", "4", "2")
            var Ai = ""
            // ================ 号码的长度 15位或18位 ================
            if (IDStr.length != 15 && IDStr.length != 18) {
                return "身份证号码长度应该为15位或18位"
            }
            // =======================(end)========================

            // ================ 数字 除最后以为都为数字 ================
            if (IDStr.length == 18) {
                Ai = IDStr.substring(0, 17)
            } else if (IDStr.length == 15) {
                Ai = IDStr.substring(0, 6) + "19" + IDStr.substring(6, 15)
            }
            if (isNumeric(Ai) == false) {
                return "身份证15位号码都应为数字 ; 18位号码除最后一位外,都应为数字"
            }
            // =======================(end)========================

            // ================ 出生年月是否有效 ================
            val strYear = Ai.substring(6, 10)// 年份
            val strMonth = Ai.substring(10, 12)// 月份
            val strDay = Ai.substring(12, 14)// 月份
            if (!isDate("$strYear-$strMonth-$strDay")) {
                return "身份证生日无效"
            }
            val gc = GregorianCalendar()
            val s = SimpleDateFormat("yyyy-MM-dd")
            try {
                if (gc.get(Calendar.YEAR) - Integer.parseInt(strYear) > 150 || gc.time.time - s.parse(
                        "$strYear-$strMonth-$strDay").time < 0) {
                    return "身份证生日不在有效范围"
                }
            } catch (ex: Exception) {
                return "身份证生日无效"
            }

            if (strMonth.toInt() > 12 || strMonth.toInt() == 0) {
                return "身份证月份无效"
            }
            if (strDay.toInt() > 31 || strDay.toInt() == 0) {
                return "身份证日期无效"
            }
            // =====================(end)=====================

            // ================ 地区码时候有效 ================
            val h = GetAreaCode()
            if (!h.containsKey(Ai.substring(0, 2))) {
                return "身份证地区编码错误"
            }
            // ==============================================

            // ================ 判断最后一位的值 ================
            var TotalmulAiWi = 0
            for (i in 0..16) {
                TotalmulAiWi += Integer.parseInt(Ai[i].toString()) * Integer.parseInt(Wi[i])
            }
            val modValue = TotalmulAiWi % 11
            val strVerifyCode = ValCodeArr[modValue]
            Ai += strVerifyCode

            if (IDStr.contains("x"))
                IDStr.replace("x".toRegex(), "X")
            if (IDStr.length == 18) {
                if (Ai != IDStr) {
                    return "身份证无效,不是合法的身份证号码"
                }
            } else {
                return ""
            }
            // =====================(end)=====================
        } catch (e: Exception) {
            return ""
        }

        return ""
    }

    /**
     * 功能：设置地区编码

     * @return Hashtable 对象
     */
    private fun GetAreaCode(): Map<String, String> {
        return mapOf("11" to "北京",
                "12" to "天津",
                "13" to "河北",
                "14" to "山西",
                "15" to "内蒙古",
                "21" to "辽宁",
                "22" to "吉林",
                "23" to "黑龙江",
                "31" to "上海",
                "32" to "江苏",
                "33" to "浙江",
                "34" to "安徽",
                "35" to "福建",
                "36" to "江西",
                "37" to "山东",
                "41" to "河南",
                "42" to "湖北",
                "43" to "湖南",
                "44" to "广东",
                "45" to "广西",
                "46" to "海南",
                "50" to "重庆",
                "51" to "四川",
                "52" to "贵州",
                "53" to "云南",
                "54" to "西藏",
                "61" to "陕西",
                "62" to "甘肃",
                "63" to "青海",
                "64" to "宁夏",
                "65" to "新疆",
                "71" to "台湾",
                "81" to "香港",
                "82" to "澳门",
                "91" to "国外")
    }

    /**
     * 功能：判断字符串是否为数字

     * @param str
     * *
     * @return
     */
    private fun isNumeric(str: String): Boolean {
        val pattern = Pattern.compile("[0-9]*")
        val isNum = pattern.matcher(str)

        return isNum.matches()
    }

    /**
     * 功能：判断字符串是否为日期格式

     * @return
     */
    fun isDate(strDate: String): Boolean {
        val pattern = Pattern.compile("^((\\d{2}(([02468][048])|([13579][26]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])))))|(\\d{2}(([02468][1235679])|([13579][01345789]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|(1[0-9])|(2[0-8]))))))(\\s(((0?[0-9])|([1-2][0-3]))\\:([0-5]?[0-9])((\\s)|(\\:([0-5]?[0-9])))))?$")
        val m = pattern.matcher(strDate)

        return m.matches()
    }

    fun isMale(id: String): Boolean {
        val result = IDCardValidate(id)
        if (StringUtil.isEmpty(result)) {
            if (id.length == 15) {
                return id.substring(14, 15).toInt() % 2 != 0
            } else if (id.length == 18) {
                return id.substring(16, 17).toInt() % 2 != 0
            }
        }
        return true
    }

    /*********************************** 身份证验证结束  */

    /**
     * 检查 email输入是否正确 正确的书写格 式为 username@domain

     * @param value
     * *
     * @return
     */
    fun validEmail(value: String, length: Int): Boolean {
        return value.matches("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*".toRegex()) && value.length <= length
    }

    fun validMobile(str: String): Boolean {
        var p = Pattern.compile("^[1][3,4,5,7,8][0-9]{9}$") // 验证手机号
        var m = p.matcher(str)
        return m.matches()
    }

    /**
     * 电话号码验证

     * @param str
     * *
     * @return 验证通过返回true
     */
    fun validTelNum(str: String): Boolean {
        var p1 = Pattern.compile("^[0][1-9]{2,3}-?[0-9]{5,10}$") // 验证带区号的
        var p2 = Pattern.compile("^[1-9]{1}[0-9]{5,8}$") // 验证没有区号的

        if (str.length > 9) {
            val m = p1.matcher(str)
            return m.matches()
        } else {
            val m = p2!!.matcher(str)
            return m.matches()
        }
    }

    /**
     * 验证年龄0-120

     * @param value
     * *
     * @return
     */
    fun validAge(value: String): Boolean {
        return value.matches("120|((1[0-1]|\\d)?\\d)".toRegex())
    }


    /**
     * 检查字符串是 否含有HTML标签

     * @param value
     * *
     * @return
     */

    fun validHtmlTag(value: String): Boolean {
        return value.matches("<(\\S*?)[^>]*>.*?</\\1>|<.*? />".toRegex())
    }

    /**
     * 检查URL是 否合法

     * @param value
     * *
     * @return
     */
    fun validURL(value: String): Boolean {
        return value.matches("[a-zA-z]+://[^\\s]*".toRegex())
    }

    /**
     * 检查IP是否 合法

     * @param value
     * *
     * @return
     */
    fun validIP(value: String): Boolean {
        return value.matches("\\d{1,3}+\\.\\d{1,3}+\\.\\d{1,3}+\\.\\d{1,3}".toRegex())
    }

    /**
     * 检查QQ是否 合法,必须是数字,且首位不能为0,最长15位

     * @param value
     * *
     * @return
     */

    fun validQQ(value: String): Boolean {
        return value.matches("[1-9][0-9]{4,13}".toRegex())
    }

    /**
     * 检查邮编是否 合法

     * @param value
     * *
     * @return
     */
    fun validPostCode(value: String): Boolean {
        return value.matches("[1-9]\\d{5}(?!\\d)".toRegex())
    }


    /**
     * 检查车牌号 否合法

     * @param value
     * *
     * @return
     */
    fun validCarPlate(value: String): Boolean {
        return value.matches("^[\u4e00-\u9fa5]{1}[A-Z]{1}[A-Z_0-9]{4}[\u4e00-\u9fa5_A-Z_0-9]{1}$".toRegex())
    }
}