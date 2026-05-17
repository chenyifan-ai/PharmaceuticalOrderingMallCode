package com.yao.pharmacymall.common;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 省市区三级数据（供地址级联选择）
 */
public final class ChinaRegionData {

    private ChinaRegionData() {
    }

    public static List<Map<String, Object>> tree() {
        List<Map<String, Object>> provinces = new ArrayList<>();
        addProvince(provinces, "北京市", city("北京市", "东城区", "西城区", "朝阳区", "海淀区", "丰台区"));
        addProvince(provinces, "上海市", city("上海市", "黄浦区", "徐汇区", "长宁区", "静安区", "浦东新区", "闵行区"));
        addProvince(provinces, "天津市", city("天津市", "和平区", "河东区", "河西区", "南开区", "滨海新区"));
        addProvince(provinces, "重庆市", city("重庆市", "渝中区", "江北区", "南岸区", "九龙坡区", "渝北区"));
        addProvince(provinces, "广东省",
                city("广州市", "天河区", "越秀区", "海珠区", "荔湾区", "白云区", "番禺区"),
                city("深圳市", "福田区", "罗湖区", "南山区", "宝安区", "龙岗区", "龙华区"),
                city("东莞市", "东城街道", "南城街道", "万江街道"),
                city("佛山市", "禅城区", "南海区", "顺德区"));
        addProvince(provinces, "浙江省",
                city("杭州市", "上城区", "拱墅区", "西湖区", "滨江区", "余杭区"),
                city("宁波市", "海曙区", "江北区", "鄞州区", "镇海区"),
                city("温州市", "鹿城区", "龙湾区", "瓯海区"));
        addProvince(provinces, "江苏省",
                city("南京市", "玄武区", "秦淮区", "建邺区", "鼓楼区", "江宁区"),
                city("苏州市", "姑苏区", "虎丘区", "吴中区", "相城区", "工业园区"),
                city("无锡市", "梁溪区", "锡山区", "惠山区"));
        addProvince(provinces, "山东省",
                city("济南市", "历下区", "市中区", "槐荫区", "历城区"),
                city("青岛市", "市南区", "市北区", "黄岛区", "崂山区"));
        addProvince(provinces, "四川省",
                city("成都市", "锦江区", "青羊区", "金牛区", "武侯区", "高新区"),
                city("绵阳市", "涪城区", "游仙区"));
        addProvince(provinces, "湖北省",
                city("武汉市", "江岸区", "江汉区", "硚口区", "汉阳区", "武昌区", "洪山区"),
                city("宜昌市", "西陵区", "伍家岗区"));
        addProvince(provinces, "湖南省",
                city("长沙市", "芙蓉区", "天心区", "岳麓区", "开福区", "雨花区"),
                city("株洲市", "荷塘区", "芦淞区"));
        addProvince(provinces, "河南省",
                city("郑州市", "中原区", "二七区", "金水区", "管城回族区"),
                city("洛阳市", "老城区", "西工区", "涧西区"));
        addProvince(provinces, "河北省",
                city("石家庄市", "长安区", "桥西区", "新华区"),
                city("保定市", "竞秀区", "莲池区"));
        addProvince(provinces, "福建省",
                city("福州市", "鼓楼区", "台江区", "仓山区"),
                city("厦门市", "思明区", "湖里区", "集美区"));
        addProvince(provinces, "安徽省",
                city("合肥市", "瑶海区", "庐阳区", "蜀山区", "包河区"),
                city("芜湖市", "镜湖区", "弋江区"));
        addProvince(provinces, "陕西省",
                city("西安市", "新城区", "碑林区", "莲湖区", "雁塔区", "未央区"),
                city("咸阳市", "秦都区", "渭城区"));
        addProvince(provinces, "辽宁省",
                city("沈阳市", "和平区", "沈河区", "皇姑区", "浑南区"),
                city("大连市", "中山区", "西岗区", "沙河口区", "甘井子区"));
        addProvince(provinces, "吉林省", city("长春市", "南关区", "宽城区", "朝阳区", "绿园区"));
        addProvince(provinces, "黑龙江省", city("哈尔滨市", "道里区", "南岗区", "道外区", "香坊区"));
        addProvince(provinces, "山西省", city("太原市", "小店区", "迎泽区", "杏花岭区", "万柏林区"));
        addProvince(provinces, "江西省", city("南昌市", "东湖区", "西湖区", "青云谱区", "红谷滩区"));
        addProvince(provinces, "云南省", city("昆明市", "五华区", "盘龙区", "官渡区", "西山区"));
        addProvince(provinces, "贵州省", city("贵阳市", "南明区", "云岩区", "花溪区", "观山湖区"));
        addProvince(provinces, "广西壮族自治区", city("南宁市", "兴宁区", "青秀区", "江南区", "西乡塘区"));
        addProvince(provinces, "海南省", city("海口市", "秀英区", "龙华区", "琼山区", "美兰区"));
        addProvince(provinces, "甘肃省", city("兰州市", "城关区", "七里河区", "西固区", "安宁区"));
        addProvince(provinces, "内蒙古自治区", city("呼和浩特市", "新城区", "回民区", "玉泉区", "赛罕区"));
        addProvince(provinces, "新疆维吾尔自治区", city("乌鲁木齐市", "天山区", "沙依巴克区", "新市区", "水磨沟区"));
        addProvince(provinces, "西藏自治区", city("拉萨市", "城关区", "堆龙德庆区"));
        addProvince(provinces, "宁夏回族自治区", city("银川市", "兴庆区", "西夏区", "金凤区"));
        addProvince(provinces, "青海省", city("西宁市", "城东区", "城中区", "城西区", "城北区"));
        return provinces;
    }

    private static void addProvince(List<Map<String, Object>> list, String province, Map<String, Object>... cities) {
        Map<String, Object> node = new LinkedHashMap<>();
        node.put("value", province);
        node.put("label", province);
        List<Map<String, Object>> children = new ArrayList<>();
        for (Map<String, Object> c : cities) {
            children.add(c);
        }
        node.put("children", children);
        list.add(node);
    }

    private static Map<String, Object> city(String name, String... districts) {
        Map<String, Object> node = new LinkedHashMap<>();
        node.put("value", name);
        node.put("label", name);
        List<Map<String, Object>> children = new ArrayList<>();
        for (String d : districts) {
            Map<String, Object> district = new LinkedHashMap<>();
            district.put("value", d);
            district.put("label", d);
            children.add(district);
        }
        node.put("children", children);
        return node;
    }
}
