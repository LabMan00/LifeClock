package com.example.lifeclock // 应用包名

import android.content.Context // Android上下文，用于访问SharedPreferences
import android.content.SharedPreferences // Android轻量级键值存储
import java.time.LocalDate // Java 8日期类，用于退休年龄计算

// 用户偏好设置单例：管理所有持久化数据（生日、寿命、性别、收入、地区等）
object UserPreferences {
    // SharedPreferences文件名
    private const val PREFS_NAME = "life_clock_prefs"
    // 存储键：生日（ISO字符串，如"1990-01-01"）
    private const val KEY_BIRTHDAY = "birthday"
    // 存储键：整数寿命（旧版兼容，单位：岁）
    private const val KEY_LIFESPAN = "expected_lifespan"
    // 存储键：精确寿命（Float，用于倒计时计算）
    private const val KEY_PRECISE_LIFESPAN = "precise_lifespan"
    // 存储键：是否已完成初始设置
    private const val KEY_IS_SETUP = "is_setup_complete"
    // 存储键：性别（male/female）
    private const val KEY_GENDER = "gender"
    // 存储键：寿命来源（personal/overall/male/female/retirement）
    private const val KEY_LIFESPAN_SOURCE = "lifespan_source"
    // 存储键：用户个人设定的预期寿命（不受外部数据覆盖）
    private const val KEY_PERSONAL_LIFESPAN = "personal_lifespan"
    // 存储键：月薪（元）
    const val KEY_SALARY = "salary"
    // 存储键：养老保险金额（元/月）
    const val KEY_PENSION = "pension"
    // 存储键：医疗保险金额（元/月）
    const val KEY_MEDICAL = "medical"
    // 存储键：失业保险金额（元/月）
    const val KEY_UNEMPLOYMENT = "unemployment"
    // 存储键：工伤保险金额（元/月）
    const val KEY_WORK_INJURY = "work_injury"
    // 存储键：生育保险金额（元/月）
    const val KEY_MATERNITY = "maternity"
    // 存储键：住房公积金金额（元/月）
    const val KEY_HOUSING_FUND = "housing_fund"
    // 存储键：住房公积金费率（0.00-0.12）
    private const val KEY_HOUSING_FUND_RATE = "housing_fund_rate"
    // 存储键：五险一金缴费基数（默认等于月薪）
    const val KEY_CONTRIBUTION_BASE = "contribution_base"
    private const val KEY_DEPOSIT = "deposit"             // 每月存款
    private const val KEY_FIXED_EXPENSE = "fixed_expense" // 月固定支出
    // 存储键：地区-国家
    private const val KEY_REGION_COUNTRY = "region_country"
    // 存储键：地区-省份
    private const val KEY_REGION_PROVINCE = "region_province"
    // 存储键：地区-城市
    private const val KEY_REGION_CITY = "region_city"

    // ========== 五险一金默认费率（个人缴纳部分） ==========
    const val RATE_PENSION = 0.08f       // 养老保险：8%
    const val RATE_MEDICAL = 0.02f       // 医疗保险：2%
    const val RATE_UNEMPLOYMENT = 0.005f // 失业保险：0.5%
    const val RATE_HOUSING_FUND = 0.12f  // 住房公积金：12%（可调0-12%）

    // ========== 性别常量 ==========
    const val GENDER_MALE = "male"     // 男性
    const val GENDER_FEMALE = "female" // 女性

    // ========== 寿命来源常量 ==========
    const val SOURCE_PERSONAL = "personal"     // 用户个人设定
    const val SOURCE_OVERALL = "overall"       // 中国人均预期寿命
    const val SOURCE_MALE = "male"             // 中国男性人均预期寿命
    const val SOURCE_FEMALE = "female"         // 中国女性人均预期寿命
    const val SOURCE_RETIREMENT = "retirement" // 退休年龄

    // ========== 中国分性别精确预期寿命（2026年数据，国家卫健委） ==========
    const val CHINA_MALE_LIFESPAN = 76.05      // 男性：76.05岁
    const val CHINA_FEMALE_LIFESPAN = 82.11    // 女性：82.11岁
    const val CHINA_OVERALL_LIFESPAN = 79.08   // 人均：79.08岁

    // 获取SharedPreferences实例（私有模式，仅本应用可访问）
    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // 判断用户是否已完成初始设置
    fun isSetupComplete(context: Context): Boolean =
        prefs(context).getBoolean(KEY_IS_SETUP, false)

    // 获取生日字符串（ISO格式：yyyy-MM-dd）
    fun getBirthday(context: Context): String? =
        prefs(context).getString(KEY_BIRTHDAY, null)

    // 获取当前活跃的预期寿命（优先读取精确值，回退到整数旧值，默认80）
    fun getExpectedLifespan(context: Context): Float {
        val precise = prefs(context).getFloat(KEY_PRECISE_LIFESPAN, -1f) // 尝试读精确值
        return if (precise > 0f) precise                                  // 有精确值直接返回
        else prefs(context).getInt(KEY_LIFESPAN, 80).toFloat()            // 否则读旧Int值
    }

    // 获取性别
    fun getGender(context: Context): String? =
        prefs(context).getString(KEY_GENDER, null)

    // 保存初始设置数据（首次安装时调用）
    fun saveUserData(context: Context, birthday: String, lifespan: Int, gender: String) {
        prefs(context).edit()                           // 开始编辑
            .putString(KEY_BIRTHDAY, birthday)          // 保存生日
            .putInt(KEY_LIFESPAN, lifespan)             // 保存整数寿命（兼容旧版）
            .putFloat(KEY_PRECISE_LIFESPAN, lifespan.toFloat())  // 保存精确寿命
            .putFloat(KEY_PERSONAL_LIFESPAN, lifespan.toFloat()) // 保存个人寿命
            .putString(KEY_GENDER, gender)              // 保存性别
            .putBoolean(KEY_IS_SETUP, true)             // 标记已完成设置
            .apply()                                    // 异步写入磁盘
    }

    // 获取用户个人设定的寿命（不受中国人均/退休等外部数据影响）
    fun getPersonalLifespan(context: Context): Float {
        val personal = prefs(context).getFloat(KEY_PERSONAL_LIFESPAN, -1f) // 读个人值
        return if (personal > 0f) personal                                  // 有值直接返回
        else prefs(context).getInt(KEY_LIFESPAN, 80).toFloat()              // 回退到旧值
    }

    // 更新个人寿命设定
    fun updatePersonalLifespan(context: Context, lifespan: Float) {
        prefs(context).edit().putFloat(KEY_PERSONAL_LIFESPAN, lifespan).apply()
    }

    // 更新生日
    fun updateBirthday(context: Context, birthday: String) {
        prefs(context).edit().putString(KEY_BIRTHDAY, birthday).apply()
    }

    // 更新性别
    fun updateGender(context: Context, gender: String) {
        prefs(context).edit().putString(KEY_GENDER, gender).apply()
    }

    // 更新活跃寿命（同时保存精确值和来源）
    fun updateLifespan(context: Context, lifespan: Float, source: String = SOURCE_PERSONAL) {
        prefs(context).edit()
            .putFloat(KEY_PRECISE_LIFESPAN, lifespan) // 保存精确寿命值
            .putInt(KEY_LIFESPAN, lifespan.toInt())   // 保存整数寿命值（兼容）
            .putString(KEY_LIFESPAN_SOURCE, source)    // 保存寿命来源
            .apply()
    }

    // 获取寿命来源
    fun getLifespanSource(context: Context): String =
        prefs(context).getString(KEY_LIFESPAN_SOURCE, SOURCE_PERSONAL) ?: SOURCE_PERSONAL

    // 根据来源获取寿命标签文字（用于主页倒计时下方显示）
    fun lifespanLabel(source: String): String = when (source) {
        SOURCE_OVERALL -> "中国人均预期寿命"
        SOURCE_MALE -> "中国男性人均预期寿命"
        SOURCE_FEMALE -> "中国女性人均预期寿命"
        SOURCE_RETIREMENT -> "退休年龄"
        else -> "预计寿命" // 包括SOURCE_PERSONAL
    }

    // 根据来源获取主页标题（退休→"你的退休倒计时"，其他→"你的生命倒计时"）
    fun lifespanTitle(source: String): String = when (source) {
        SOURCE_RETIREMENT -> "你的退休倒计时"
        else -> "你的生命倒计时"
    }

    // 根据来源获取超时提示文字
    fun lifespanOverText(source: String): String = when (source) {
        SOURCE_RETIREMENT -> "已超过退休年龄"
        else -> "已超出预期寿命"
    }

    // 根据来源获取目标日期标签（退休→"退休日期"，其他→"预计结束"）
    fun targetDateLabel(source: String): String = when (source) {
        SOURCE_RETIREMENT -> "退休日期"
        else -> "预计结束"
    }

    // ========== 退休年龄计算（2025年延迟退休政策） ==========
    // 男性：原60岁，每4个月延迟1个月，逐步延至63岁（最多延36个月）
    // 女性：原55岁（管理/技术岗），每4个月延迟1个月，逐步延至58岁（最多延36个月）
    fun calculateRetirementAge(birthday: LocalDate, gender: String): Float {
        val baseAge = if (gender == GENDER_MALE) 60 else 55   // 原法定退休年龄
        val maxDelayMonths = 36                                // 最大延迟月数（3年）
        val originalRetireDate = birthday.plusYears(baseAge.toLong()) // 原退休日期
        val policyStart = LocalDate.of(2025, 1, 1)             // 政策开始日期

        if (originalRetireDate.isBefore(policyStart)) {
            return baseAge.toFloat() // 2025年前退休，不受延迟政策影响
        }

        // 计算从2025年1月到原退休日期的月数
        val monthsFrom2025 = (originalRetireDate.year - 2025) * 12 + originalRetireDate.monthValue
        // 每4个月延迟1个月（整数除法向上取整等价于 (n+3)/4）
        val delayMonths = minOf((monthsFrom2025 + 3) / 4, maxDelayMonths)
        return baseAge + delayMonths / 12f // 返回精确退休年龄（含小数）
    }

    // 格式化退休年龄为"X岁Y个月"的显示文本
    fun formatRetirementAge(age: Float): String {
        val years = age.toInt()                    // 整数年
        val months = ((age - years) * 12).toInt()  // 剩余月数
        return if (months > 0) "${years}岁${months}个月" else "${years}岁"
    }

    // 根据性别获取对应的中国人均预期寿命
    // ========== 养老金计算（2024年城镇职工基本养老保险公式） ==========
    // 月养老金 = 基础养老金 + 个人账户养老金
    // 基础养老金 = (当地上年度在岗职工月平均工资 + 本人指数化月平均缴费工资) ÷ 2 × 缴费年限 × 1%
    // 个人账户养老金 = 个人账户储存额 ÷ 计发月数
    // 个人账户储存额 = 月薪 × 8% × 12 × 缴费年限
    // 计发月数根据退休年龄确定：50岁=195, 55岁=170, 60岁=139, 63岁=117
    fun calculateMonthlyPension(
        salary: Float,         // 月薪（元）
        province: String,      // 所在省份
        retirementAge: Float   // 实际退休年龄
    ): Float {
        if (salary <= 0f) return 0f                                               // 无薪资则养老金为0
        val localAvgSalary = RegionData.getProvinceSalary(province)               // 当地上年度月平均工资
        val contributionYears = maxOf(1f, retirementAge - 22f)                    // 缴费年限（假设22岁开始工作）
        val personalRate = 0.08f                                                   // 个人账户缴费比例8%

        // 基础养老金
        val basicPension = (localAvgSalary + salary) / 2f * contributionYears * 0.01f

        // 个人账户储存额
        val accountBalance = salary * personalRate * 12f * contributionYears

        // 计发月数（根据退休年龄线性插值）
        val payoutMonths = when {
            retirementAge <= 50f -> 195f
            retirementAge >= 63f -> 117f
            else -> {
                // 在已知数据点之间线性插值：50→195, 55→170, 60→139, 63→117
                val refAges = listOf(50f to 195f, 55f to 170f, 60f to 139f, 63f to 117f)
                var lower = 50f to 195f
                var upper = 63f to 117f
                for (i in 0 until refAges.size - 1) {
                    if (retirementAge >= refAges[i].first && retirementAge <= refAges[i + 1].first) {
                        lower = refAges[i]; upper = refAges[i + 1]; break
                    }
                }
                val ratio = (retirementAge - lower.first) / (upper.first - lower.first)
                lower.second - ratio * (lower.second - upper.second)
            }
        }

        val personalPension = accountBalance / payoutMonths                      // 个人账户养老金
        return basicPension + personalPension                                    // 月养老金总额
    }

    fun getChinaAvgLifespan(gender: String): Double =
        if (gender == GENDER_FEMALE) CHINA_FEMALE_LIFESPAN else CHINA_MALE_LIFESPAN

    // 将小数年份格式化为"X年X个月X天"
    fun formatLifespan(years: Double): String {
        val totalDays = (years * 365.25).toLong() // 总天数（一年按365.25天计）
        val y = totalDays / 365                    // 整数年
        val remainingDays = totalDays % 365         // 剩余天数
        val m = remainingDays / 30                  // 剩余月数（按30天/月）
        val d = remainingDays % 30                  // 剩余天数
        return "${y}年${m}个月${d}天"
    }

    // ========== 收入相关存取方法 ==========

    // 获取月薪（默认0）
    fun getSalary(context: Context): Float =
        prefs(context).getFloat(KEY_SALARY, 0f)

    // 获取养老保险金额（-1表示未设置）
    fun getPension(context: Context): Float =
        prefs(context).getFloat(KEY_PENSION, -1f)

    // 获取医疗保险金额
    fun getMedical(context: Context): Float =
        prefs(context).getFloat(KEY_MEDICAL, -1f)

    // 获取失业保险金额
    fun getUnemployment(context: Context): Float =
        prefs(context).getFloat(KEY_UNEMPLOYMENT, -1f)

    // 获取工伤保险金额
    fun getWorkInjury(context: Context): Float =
        prefs(context).getFloat(KEY_WORK_INJURY, -1f)

    // 获取生育保险金额
    fun getMaternity(context: Context): Float =
        prefs(context).getFloat(KEY_MATERNITY, -1f)

    // 获取缴费基数（默认等于月薪，-1表示未设置）
    fun getContributionBase(context: Context, salary: Float): Float {
        val base = prefs(context).getFloat(KEY_CONTRIBUTION_BASE, -1f)
        return if (base >= 0f) base else salary // 未设置时默认等于月薪
    }

    // 更新缴费基数
    fun updateContributionBase(context: Context, value: Float) {
        prefs(context).edit().putFloat(KEY_CONTRIBUTION_BASE, value).apply()
    }

    // 获取每月存款（默认0）
    fun getDeposit(context: Context): Float =
        prefs(context).getFloat(KEY_DEPOSIT, 0f)

    // 更新每月存款
    fun updateDeposit(context: Context, value: Float) {
        prefs(context).edit().putFloat(KEY_DEPOSIT, value).apply()
    }

    // 获取月固定支出（默认0）
    fun getFixedExpense(context: Context): Float =
        prefs(context).getFloat(KEY_FIXED_EXPENSE, 0f)

    // 更新月固定支出
    fun updateFixedExpense(context: Context, value: Float) {
        prefs(context).edit().putFloat(KEY_FIXED_EXPENSE, value).apply()
    }

    // 获取住房公积金金额
    fun getHousingFund(context: Context): Float =
        prefs(context).getFloat(KEY_HOUSING_FUND, -1f)

    // 获取住房公积金费率（-1表示未设置，使用默认12%）
    fun getHousingFundRate(context: Context): Float =
        prefs(context).getFloat(KEY_HOUSING_FUND_RATE, -1f)

    // 更新住房公积金费率
    fun updateHousingFundRate(context: Context, rate: Float) {
        prefs(context).edit().putFloat(KEY_HOUSING_FUND_RATE, rate).apply()
    }

    // 更新月薪
    fun updateSalary(context: Context, value: Float) {
        prefs(context).edit().putFloat(KEY_SALARY, value).apply()
    }

    // 更新收入项（五险一金中的某一项）
    fun updateIncomeItem(context: Context, key: String, value: Float) {
        prefs(context).edit().putFloat(key, value).apply()
    }

    // 根据费率和月薪计算五险一金默认金额
    fun getIncomeDefault(context: Context, base: Float, key: String): Float = when (key) {
        KEY_PENSION -> base * RATE_PENSION                     // 养老：缴费基数×8%
        KEY_MEDICAL -> base * RATE_MEDICAL                     // 医疗：缴费基数×2%
        KEY_UNEMPLOYMENT -> base * RATE_UNEMPLOYMENT           // 失业：缴费基数×0.5%
        KEY_HOUSING_FUND -> {
            val rate = getHousingFundRate(context)              // 公积金：使用自定义费率
            if (rate >= 0f) base * rate else base * RATE_HOUSING_FUND // 缴费基数×费率
        }
        KEY_WORK_INJURY -> 0f  // 工伤：企业缴纳，个人为0
        KEY_MATERNITY -> 0f     // 生育：企业缴纳，个人为0
        else -> 0f
    }

    // 获取收入项实际值（优先已存储的值，否则按默认费率×缴费基数计算）
    fun getIncomeValue(context: Context, key: String, base: Float): Float {
        val stored = prefs(context).getFloat(key, -1f)    // 读取已存储的值
        return if (stored >= 0f) stored                    // 已设置则直接返回
        else getIncomeDefault(context, base, key)           // 未设置则按缴费基数×费率计算
    }

    // ========== 地区相关存取方法 ==========

    // 获取用户设置的地区（国家, 省份, 城市），默认中国北京东城区
    fun getRegion(context: Context): Triple<String, String, String> {
        val country = prefs(context).getString(KEY_REGION_COUNTRY, "中国") ?: "中国"
        val province = prefs(context).getString(KEY_REGION_PROVINCE, "北京市") ?: "北京市"
        val city = prefs(context).getString(KEY_REGION_CITY, "东城区") ?: "东城区"
        return Triple(country, province, city)
    }

    // 更新地区设置
    fun updateRegion(context: Context, country: String, province: String, city: String) {
        prefs(context).edit()
            .putString(KEY_REGION_COUNTRY, country)
            .putString(KEY_REGION_PROVINCE, province)
            .putString(KEY_REGION_CITY, city)
            .apply()
    }

    // 清除所有数据（用于重置应用）
    fun clearAll(context: Context) {
        prefs(context).edit().clear().apply()
    }
}
