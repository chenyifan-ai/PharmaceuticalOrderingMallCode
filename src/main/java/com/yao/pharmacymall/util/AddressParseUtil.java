package com.yao.pharmacymall.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 收货地址智能解析（从粘贴文本提取姓名、手机、省市区、详细地址）
 */
public final class AddressParseUtil {

    private static final Pattern PHONE_PATTERN = Pattern.compile("1[3-9]\\d{9}");
    private static final String[] PROVINCES = {
            "北京市", "天津市", "上海市", "重庆市",
            "河北省", "山西省", "辽宁省", "吉林省", "黑龙江省",
            "江苏省", "浙江省", "安徽省", "福建省", "江西省", "山东省",
            "河南省", "湖北省", "湖南省", "广东省", "海南省",
            "四川省", "贵州省", "云南省", "陕西省", "甘肃省", "青海省",
            "台湾省", "内蒙古自治区", "广西壮族自治区", "西藏自治区",
            "宁夏回族自治区", "新疆维吾尔自治区", "香港特别行政区", "澳门特别行政区"
    };

    private AddressParseUtil() {
    }

    public static Map<String, String> parse(String text) {
        Map<String, String> result = new HashMap<>();
        if (text == null || text.isBlank()) {
            return result;
        }
        String normalized = text.replaceAll("[\\r\\n]+", " ")
                .replaceAll("\\s+", " ")
                .trim();

        Matcher phoneMatcher = PHONE_PATTERN.matcher(normalized);
        if (phoneMatcher.find()) {
            result.put("phone", phoneMatcher.group());
            normalized = normalized.replace(result.get("phone"), " ").trim();
        }

        String province = null;
        String city = null;
        String district = null;
        int regionEnd = 0;

        for (String p : PROVINCES) {
            int idx = normalized.indexOf(p);
            if (idx >= 0) {
                province = p;
                regionEnd = idx + p.length();
                break;
            }
        }

        if (province != null) {
            String rest = normalized.substring(regionEnd).trim();
            Matcher cityMatcher = Pattern.compile("^(.+?(?:市|州|盟|地区))").matcher(rest);
            if (cityMatcher.find()) {
                city = cityMatcher.group(1);
                rest = rest.substring(city.length()).trim();
            }
            Matcher districtMatcher = Pattern.compile("^(.+?(?:区|县|市|旗))").matcher(rest);
            if (districtMatcher.find()) {
                district = districtMatcher.group(1);
                rest = rest.substring(district.length()).trim();
            }
            result.put("province", province);
            if (city != null) {
                result.put("city", city);
            }
            if (district != null) {
                result.put("district", district);
            }
            if (!rest.isEmpty()) {
                result.put("detail", rest);
            }
            String beforeRegion = normalized.substring(0, normalized.indexOf(province)).trim();
            if (!beforeRegion.isEmpty() && beforeRegion.length() <= 20) {
                result.put("name", beforeRegion.replaceAll("[,，、\\s]+$", ""));
            }
        } else if (!normalized.isEmpty()) {
            result.put("detail", normalized);
        }

        if (!result.containsKey("name")) {
            String candidate = normalized.split("[,，、\\s]")[0];
            if (candidate.length() >= 2 && candidate.length() <= 8 && !candidate.matches(".*\\d.*")) {
                result.put("name", candidate);
            }
        }

        return result;
    }
}
