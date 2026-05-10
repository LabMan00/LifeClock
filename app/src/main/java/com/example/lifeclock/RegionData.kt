package com.example.lifeclock

// 地区数据结构：名称 + 子级地区列表
data class Region(
    val name: String,                          // 地区名称
    val children: List<Region> = emptyList()   // 子级地区列表（省份→城市）
)

// 中国地区数据：国家→省份→城市三级结构 + 各省上年度在岗职工月平均工资（2024年估算，单位：元）
object RegionData {
    // 各省/直辖市上年度在岗职工月平均工资（用于养老金计算）
    val provinceAvgSalary: Map<String, Float> = mapOf(
        "北京市" to 13500f, "上海市" to 12500f, "天津市" to 8500f, "重庆市" to 7500f,
        "河北省" to 6200f, "山西省" to 6000f, "内蒙古" to 7000f,
        "辽宁省" to 6500f, "吉林省" to 6000f, "黑龙江省" to 5800f,
        "江苏省" to 9000f, "浙江省" to 9000f, "安徽省" to 6800f,
        "福建省" to 7500f, "江西省" to 6200f, "山东省" to 7500f,
        "河南省" to 6000f, "湖北省" to 7000f, "湖南省" to 6500f,
        "广东省" to 9500f, "广西" to 6200f, "海南省" to 7000f,
        "四川省" to 7000f, "贵州省" to 6200f, "云南省" to 6500f,
        "西藏" to 9000f, "陕西省" to 6800f, "甘肃省" to 5800f,
        "青海省" to 7000f, "宁夏" to 6500f, "新疆" to 6800f,
        "台湾省" to 10000f, "香港特别行政区" to 18000f, "澳门特别行政区" to 15000f
    )

    // 获取某省份的月平均工资（找不到则返回全国估算值7500）
    fun getProvinceSalary(province: String): Float = provinceAvgSalary[province] ?: 7500f
    val regions: List<Region> = listOf(
        Region("中国", listOf(
            // === 直辖市 ===
            Region("北京市", listOf(
                Region("东城区"), Region("西城区"), Region("朝阳区"), Region("丰台区"),
                Region("石景山区"), Region("海淀区"), Region("门头沟区"), Region("房山区"),
                Region("通州区"), Region("顺义区"), Region("昌平区"), Region("大兴区"),
                Region("怀柔区"), Region("平谷区"), Region("密云区"), Region("延庆区")
            )),
            Region("上海市", listOf(
                Region("黄浦区"), Region("徐汇区"), Region("长宁区"), Region("静安区"),
                Region("普陀区"), Region("虹口区"), Region("杨浦区"), Region("闵行区"),
                Region("宝山区"), Region("嘉定区"), Region("浦东新区"), Region("金山区"),
                Region("松江区"), Region("青浦区"), Region("奉贤区"), Region("崇明区")
            )),
            Region("天津市", listOf(
                Region("和平区"), Region("河东区"), Region("河西区"), Region("南开区"),
                Region("河北区"), Region("红桥区"), Region("东丽区"), Region("西青区"),
                Region("津南区"), Region("北辰区"), Region("武清区"), Region("宝坻区"),
                Region("滨海新区"), Region("宁河区"), Region("静海区"), Region("蓟州区")
            )),
            Region("重庆市", listOf(
                Region("渝中区"), Region("万州区"), Region("涪陵区"), Region("大渡口区"),
                Region("江北区"), Region("沙坪坝区"), Region("九龙坡区"), Region("南岸区"),
                Region("北碚区"), Region("綦江区"), Region("大足区"), Region("渝北区"),
                Region("巴南区"), Region("黔江区"), Region("长寿区"), Region("江津区"),
                Region("合川区"), Region("永川区"), Region("南川区"), Region("璧山区"),
                Region("铜梁区"), Region("潼南区"), Region("荣昌区"), Region("开州区"),
                Region("梁平区"), Region("武隆区"), Region("城口县"), Region("丰都县"),
                Region("垫江县"), Region("忠县"), Region("云阳县"), Region("奉节县"),
                Region("巫山县"), Region("巫溪县"), Region("石柱县"), Region("秀山县"),
                Region("酉阳县"), Region("彭水县")
            )),
            // === 省份 ===
            Region("河北省", listOf(
                Region("石家庄市"), Region("唐山市"), Region("秦皇岛市"), Region("邯郸市"),
                Region("邢台市"), Region("保定市"), Region("张家口市"), Region("承德市"),
                Region("沧州市"), Region("廊坊市"), Region("衡水市")
            )),
            Region("山西省", listOf(
                Region("太原市"), Region("大同市"), Region("阳泉市"), Region("长治市"),
                Region("晋城市"), Region("朔州市"), Region("晋中市"), Region("运城市"),
                Region("忻州市"), Region("临汾市"), Region("吕梁市")
            )),
            Region("内蒙古", listOf(
                Region("呼和浩特市"), Region("包头市"), Region("乌海市"), Region("赤峰市"),
                Region("通辽市"), Region("鄂尔多斯市"), Region("呼伦贝尔市"), Region("巴彦淖尔市"),
                Region("乌兰察布市"), Region("兴安盟"), Region("锡林郭勒盟"), Region("阿拉善盟")
            )),
            Region("辽宁省", listOf(
                Region("沈阳市"), Region("大连市"), Region("鞍山市"), Region("抚顺市"),
                Region("本溪市"), Region("丹东市"), Region("锦州市"), Region("营口市"),
                Region("阜新市"), Region("辽阳市"), Region("盘锦市"), Region("铁岭市"),
                Region("朝阳市"), Region("葫芦岛市")
            )),
            Region("吉林省", listOf(
                Region("长春市"), Region("吉林市"), Region("四平市"), Region("辽源市"),
                Region("通化市"), Region("白山市"), Region("松原市"), Region("白城市"),
                Region("延边州")
            )),
            Region("黑龙江省", listOf(
                Region("哈尔滨市"), Region("齐齐哈尔市"), Region("鸡西市"), Region("鹤岗市"),
                Region("双鸭山市"), Region("大庆市"), Region("伊春市"), Region("佳木斯市"),
                Region("七台河市"), Region("牡丹江市"), Region("黑河市"), Region("绥化市"),
                Region("大兴安岭地区")
            )),
            Region("江苏省", listOf(
                Region("南京市"), Region("无锡市"), Region("徐州市"), Region("常州市"),
                Region("苏州市"), Region("南通市"), Region("连云港市"), Region("淮安市"),
                Region("盐城市"), Region("扬州市"), Region("镇江市"), Region("泰州市"),
                Region("宿迁市")
            )),
            Region("浙江省", listOf(
                Region("杭州市"), Region("宁波市"), Region("温州市"), Region("嘉兴市"),
                Region("湖州市"), Region("绍兴市"), Region("金华市"), Region("衢州市"),
                Region("舟山市"), Region("台州市"), Region("丽水市")
            )),
            Region("安徽省", listOf(
                Region("合肥市"), Region("芜湖市"), Region("蚌埠市"), Region("淮南市"),
                Region("马鞍山市"), Region("淮北市"), Region("铜陵市"), Region("安庆市"),
                Region("黄山市"), Region("滁州市"), Region("阜阳市"), Region("宿州市"),
                Region("六安市"), Region("亳州市"), Region("池州市"), Region("宣城市")
            )),
            Region("福建省", listOf(
                Region("福州市"), Region("厦门市"), Region("莆田市"), Region("三明市"),
                Region("泉州市"), Region("漳州市"), Region("南平市"), Region("龙岩市"),
                Region("宁德市")
            )),
            Region("江西省", listOf(
                Region("南昌市"), Region("景德镇市"), Region("萍乡市"), Region("九江市"),
                Region("新余市"), Region("鹰潭市"), Region("赣州市"), Region("吉安市"),
                Region("宜春市"), Region("抚州市"), Region("上饶市")
            )),
            Region("山东省", listOf(
                Region("济南市"), Region("青岛市"), Region("淄博市"), Region("枣庄市"),
                Region("东营市"), Region("烟台市"), Region("潍坊市"), Region("济宁市"),
                Region("泰安市"), Region("威海市"), Region("日照市"), Region("临沂市"),
                Region("德州市"), Region("聊城市"), Region("滨州市"), Region("菏泽市")
            )),
            Region("河南省", listOf(
                Region("郑州市"), Region("开封市"), Region("洛阳市"), Region("平顶山市"),
                Region("安阳市"), Region("鹤壁市"), Region("新乡市"), Region("焦作市"),
                Region("濮阳市"), Region("许昌市"), Region("漯河市"), Region("三门峡市"),
                Region("南阳市"), Region("商丘市"), Region("信阳市"), Region("周口市"),
                Region("驻马店市"), Region("济源市")
            )),
            Region("湖北省", listOf(
                Region("武汉市"), Region("黄石市"), Region("十堰市"), Region("宜昌市"),
                Region("襄阳市"), Region("鄂州市"), Region("荆门市"), Region("孝感市"),
                Region("荆州市"), Region("黄冈市"), Region("咸宁市"), Region("随州市"),
                Region("恩施州"), Region("仙桃市"), Region("潜江市"), Region("天门市"),
                Region("神农架林区")
            )),
            Region("湖南省", listOf(
                Region("长沙市"), Region("株洲市"), Region("湘潭市"), Region("衡阳市"),
                Region("邵阳市"), Region("岳阳市"), Region("常德市"), Region("张家界市"),
                Region("益阳市"), Region("郴州市"), Region("永州市"), Region("怀化市"),
                Region("娄底市"), Region("湘西州")
            )),
            Region("广东省", listOf(
                Region("广州市"), Region("韶关市"), Region("深圳市"), Region("珠海市"),
                Region("汕头市"), Region("佛山市"), Region("江门市"), Region("湛江市"),
                Region("茂名市"), Region("肇庆市"), Region("惠州市"), Region("梅州市"),
                Region("汕尾市"), Region("河源市"), Region("阳江市"), Region("清远市"),
                Region("东莞市"), Region("中山市"), Region("潮州市"), Region("揭阳市"),
                Region("云浮市")
            )),
            Region("广西", listOf(
                Region("南宁市"), Region("柳州市"), Region("桂林市"), Region("梧州市"),
                Region("北海市"), Region("防城港市"), Region("钦州市"), Region("贵港市"),
                Region("玉林市"), Region("百色市"), Region("贺州市"), Region("河池市"),
                Region("来宾市"), Region("崇左市")
            )),
            Region("海南省", listOf(
                Region("海口市"), Region("三亚市"), Region("三沙市"), Region("儋州市"),
                Region("五指山市"), Region("琼海市"), Region("文昌市"), Region("万宁市"),
                Region("东方市"), Region("定安县"), Region("屯昌县"), Region("澄迈县"),
                Region("临高县"), Region("白沙县"), Region("昌江县"), Region("乐东县"),
                Region("陵水县"), Region("保亭县"), Region("琼中县")
            )),
            Region("四川省", listOf(
                Region("成都市"), Region("自贡市"), Region("攀枝花市"), Region("泸州市"),
                Region("德阳市"), Region("绵阳市"), Region("广元市"), Region("遂宁市"),
                Region("内江市"), Region("乐山市"), Region("南充市"), Region("眉山市"),
                Region("宜宾市"), Region("广安市"), Region("达州市"), Region("雅安市"),
                Region("巴中市"), Region("资阳市"), Region("阿坝州"), Region("甘孜州"),
                Region("凉山州")
            )),
            Region("贵州省", listOf(
                Region("贵阳市"), Region("六盘水市"), Region("遵义市"), Region("安顺市"),
                Region("毕节市"), Region("铜仁市"), Region("黔西南州"), Region("黔东南州"),
                Region("黔南州")
            )),
            Region("云南省", listOf(
                Region("昆明市"), Region("曲靖市"), Region("玉溪市"), Region("保山市"),
                Region("昭通市"), Region("丽江市"), Region("普洱市"), Region("临沧市"),
                Region("楚雄州"), Region("红河州"), Region("文山州"), Region("西双版纳州"),
                Region("大理州"), Region("德宏州"), Region("怒江州"), Region("迪庆州")
            )),
            Region("西藏", listOf(
                Region("拉萨市"), Region("日喀则市"), Region("昌都市"), Region("林芝市"),
                Region("山南市"), Region("那曲市"), Region("阿里地区")
            )),
            Region("陕西省", listOf(
                Region("西安市"), Region("铜川市"), Region("宝鸡市"), Region("咸阳市"),
                Region("渭南市"), Region("延安市"), Region("汉中市"), Region("榆林市"),
                Region("安康市"), Region("商洛市")
            )),
            Region("甘肃省", listOf(
                Region("兰州市"), Region("嘉峪关市"), Region("金昌市"), Region("白银市"),
                Region("天水市"), Region("武威市"), Region("张掖市"), Region("平凉市"),
                Region("酒泉市"), Region("庆阳市"), Region("定西市"), Region("陇南市"),
                Region("临夏州"), Region("甘南州")
            )),
            Region("青海省", listOf(
                Region("西宁市"), Region("海东市"), Region("海北州"), Region("黄南州"),
                Region("海南州"), Region("果洛州"), Region("玉树州"), Region("海西州")
            )),
            Region("宁夏", listOf(
                Region("银川市"), Region("石嘴山市"), Region("吴忠市"), Region("固原市"),
                Region("中卫市")
            )),
            Region("新疆", listOf(
                Region("乌鲁木齐市"), Region("克拉玛依市"), Region("吐鲁番市"), Region("哈密市"),
                Region("昌吉州"), Region("博尔塔拉州"), Region("巴音郭楞州"), Region("阿克苏地区"),
                Region("克孜勒苏州"), Region("喀什地区"), Region("和田地区"), Region("伊犁州"),
                Region("塔城地区"), Region("阿勒泰地区"), Region("石河子市"), Region("阿拉尔市"),
                Region("图木舒克市"), Region("五家渠市"), Region("北屯市"), Region("铁门关市"),
                Region("双河市"), Region("可克达拉市"), Region("昆玉市"), Region("胡杨河市")
            )),
            Region("台湾省", listOf(
                Region("台北市"), Region("高雄市"), Region("台中市"), Region("台南市"),
                Region("新北市"), Region("桃园市"), Region("基隆市"), Region("新竹市"),
                Region("嘉义市")
            )),
            Region("香港特别行政区", listOf(
                Region("中西区"), Region("东区"), Region("南区"), Region("湾仔区"),
                Region("九龙城区"), Region("观塘区"), Region("深水埗区"), Region("油尖旺区"),
                Region("黄大仙区"), Region("北区"), Region("西贡区"), Region("沙田区"),
                Region("大埔区"), Region("荃湾区"), Region("屯门区"), Region("元朗区"),
                Region("葵青区"), Region("离岛区")
            )),
            Region("澳门特别行政区", listOf(
                Region("花地玛堂区"), Region("圣安多尼堂区"), Region("大堂区"),
                Region("望德堂区"), Region("风顺堂区"), Region("嘉模堂区"),
                Region("圣方济各堂区"), Region("路氹城")
            ))
        )),
        // 预留其他国家
        Region("敬请期待", listOf(
            Region("敬请期待", listOf(Region("敬请期待")))
        ))
    )
}
