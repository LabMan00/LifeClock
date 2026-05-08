package com.example.lifeclock

import android.content.Context
import android.content.SharedPreferences
import java.time.LocalDate

object UserPreferences {
    private const val PREFS_NAME = "life_clock_prefs"
    private const val KEY_BIRTHDAY = "birthday"
    private const val KEY_LIFESPAN = "expected_lifespan"
    private const val KEY_PRECISE_LIFESPAN = "precise_lifespan"
    private const val KEY_IS_SETUP = "is_setup_complete"
    private const val KEY_GENDER = "gender"
    private const val KEY_LIFESPAN_SOURCE = "lifespan_source"
    private const val KEY_PERSONAL_LIFESPAN = "personal_lifespan"
    const val KEY_SALARY = "salary"
    const val KEY_PENSION = "pension"
    const val KEY_MEDICAL = "medical"
    const val KEY_UNEMPLOYMENT = "unemployment"
    const val KEY_WORK_INJURY = "work_injury"
    const val KEY_MATERNITY = "maternity"
    const val KEY_HOUSING_FUND = "housing_fund"
    private const val KEY_REGION_COUNTRY = "region_country"
    private const val KEY_REGION_PROVINCE = "region_province"
    private const val KEY_REGION_CITY = "region_city"

    // 五险一金默认费率（个人缴纳部分）
    const val RATE_PENSION = 0.08f
    const val RATE_MEDICAL = 0.02f
    const val RATE_UNEMPLOYMENT = 0.005f
    const val RATE_HOUSING_FUND = 0.12f

    const val GENDER_MALE = "male"
    const val GENDER_FEMALE = "female"

    const val SOURCE_PERSONAL = "personal"
    const val SOURCE_OVERALL = "overall"
    const val SOURCE_MALE = "male"
    const val SOURCE_FEMALE = "female"
    const val SOURCE_RETIREMENT = "retirement"

    const val CHINA_MALE_LIFESPAN = 76.05
    const val CHINA_FEMALE_LIFESPAN = 82.11
    const val CHINA_OVERALL_LIFESPAN = 79.08

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isSetupComplete(context: Context): Boolean =
        prefs(context).getBoolean(KEY_IS_SETUP, false)

    fun getBirthday(context: Context): String? =
        prefs(context).getString(KEY_BIRTHDAY, null)

    fun getExpectedLifespan(context: Context): Float {
        val precise = prefs(context).getFloat(KEY_PRECISE_LIFESPAN, -1f)
        return if (precise > 0f) precise
        else prefs(context).getInt(KEY_LIFESPAN, 80).toFloat()
    }

    fun getGender(context: Context): String? =
        prefs(context).getString(KEY_GENDER, null)

    fun saveUserData(context: Context, birthday: String, lifespan: Int, gender: String) {
        prefs(context).edit()
            .putString(KEY_BIRTHDAY, birthday)
            .putInt(KEY_LIFESPAN, lifespan)
            .putFloat(KEY_PRECISE_LIFESPAN, lifespan.toFloat())
            .putFloat(KEY_PERSONAL_LIFESPAN, lifespan.toFloat())
            .putString(KEY_GENDER, gender)
            .putBoolean(KEY_IS_SETUP, true)
            .apply()
    }

    fun getPersonalLifespan(context: Context): Float {
        val personal = prefs(context).getFloat(KEY_PERSONAL_LIFESPAN, -1f)
        return if (personal > 0f) personal
        else prefs(context).getInt(KEY_LIFESPAN, 80).toFloat()
    }

    fun updatePersonalLifespan(context: Context, lifespan: Float) {
        prefs(context).edit().putFloat(KEY_PERSONAL_LIFESPAN, lifespan).apply()
    }

    fun updateBirthday(context: Context, birthday: String) {
        prefs(context).edit().putString(KEY_BIRTHDAY, birthday).apply()
    }

    fun updateGender(context: Context, gender: String) {
        prefs(context).edit().putString(KEY_GENDER, gender).apply()
    }

    fun updateLifespan(context: Context, lifespan: Float, source: String = SOURCE_PERSONAL) {
        prefs(context).edit()
            .putFloat(KEY_PRECISE_LIFESPAN, lifespan)
            .putInt(KEY_LIFESPAN, lifespan.toInt())
            .putString(KEY_LIFESPAN_SOURCE, source)
            .apply()
    }

    fun getLifespanSource(context: Context): String =
        prefs(context).getString(KEY_LIFESPAN_SOURCE, SOURCE_PERSONAL) ?: SOURCE_PERSONAL

    fun lifespanLabel(source: String): String = when (source) {
        SOURCE_OVERALL -> "中国人均预期寿命"
        SOURCE_MALE -> "中国男性人均预期寿命"
        SOURCE_FEMALE -> "中国女性人均预期寿命"
        SOURCE_RETIREMENT -> "退休年龄"
        else -> "预计寿命"
    }

    fun lifespanTitle(source: String): String = when (source) {
        SOURCE_RETIREMENT -> "你的退休倒计时"
        else -> "你的生命倒计时"
    }

    fun lifespanOverText(source: String): String = when (source) {
        SOURCE_RETIREMENT -> "已超过退休年龄"
        else -> "已超出预期寿命"
    }

    fun targetDateLabel(source: String): String = when (source) {
        SOURCE_RETIREMENT -> "退休日期"
        else -> "预计结束"
    }

    /**
     * 根据2025年延迟退休政策计算退休年龄。
     * 男性原60岁，每4个月延迟1个月，逐步延迟至63岁（最多延36个月）。
     * 女性原55岁（管理/技术岗），每4个月延迟1个月，逐步延迟至58岁（最多延36个月）。
     */
    fun calculateRetirementAge(birthday: LocalDate, gender: String): Float {
        val baseAge = if (gender == GENDER_MALE) 60 else 55
        val maxDelayMonths = 36
        val originalRetireDate = birthday.plusYears(baseAge.toLong())
        val policyStart = LocalDate.of(2025, 1, 1)

        if (originalRetireDate.isBefore(policyStart)) {
            return baseAge.toFloat()
        }

        val monthsFrom2025 = (originalRetireDate.year - 2025) * 12 + originalRetireDate.monthValue
        val delayMonths = minOf((monthsFrom2025 + 3) / 4, maxDelayMonths)
        return baseAge + delayMonths / 12f
    }

    fun formatRetirementAge(age: Float): String {
        val years = age.toInt()
        val months = ((age - years) * 12).toInt()
        return if (months > 0) "${years}岁${months}个月" else "${years}岁"
    }

    fun getChinaAvgLifespan(gender: String): Double =
        if (gender == GENDER_FEMALE) CHINA_FEMALE_LIFESPAN else CHINA_MALE_LIFESPAN

    fun formatLifespan(years: Double): String {
        val totalDays = (years * 365.25).toLong()
        val y = totalDays / 365
        val remainingDays = totalDays % 365
        val m = remainingDays / 30
        val d = remainingDays % 30
        return "${y}年${m}个月${d}天"
    }

    fun getSalary(context: Context): Float =
        prefs(context).getFloat(KEY_SALARY, 0f)

    fun getPension(context: Context): Float =
        prefs(context).getFloat(KEY_PENSION, -1f)

    fun getMedical(context: Context): Float =
        prefs(context).getFloat(KEY_MEDICAL, -1f)

    fun getUnemployment(context: Context): Float =
        prefs(context).getFloat(KEY_UNEMPLOYMENT, -1f)

    fun getWorkInjury(context: Context): Float =
        prefs(context).getFloat(KEY_WORK_INJURY, -1f)

    fun getMaternity(context: Context): Float =
        prefs(context).getFloat(KEY_MATERNITY, -1f)

    fun getHousingFund(context: Context): Float =
        prefs(context).getFloat(KEY_HOUSING_FUND, -1f)

    fun updateSalary(context: Context, value: Float) {
        prefs(context).edit().putFloat(KEY_SALARY, value).apply()
    }

    fun updateIncomeItem(context: Context, key: String, value: Float) {
        prefs(context).edit().putFloat(key, value).apply()
    }

    fun getIncomeDefault(salary: Float, key: String): Float = when (key) {
        KEY_PENSION -> salary * RATE_PENSION
        KEY_MEDICAL -> salary * RATE_MEDICAL
        KEY_UNEMPLOYMENT -> salary * RATE_UNEMPLOYMENT
        KEY_HOUSING_FUND -> salary * RATE_HOUSING_FUND
        KEY_WORK_INJURY -> 0f
        KEY_MATERNITY -> 0f
        else -> 0f
    }

    fun getIncomeValue(context: Context, key: String, salary: Float): Float {
        val stored = prefs(context).getFloat(key, -1f)
        return if (stored >= 0f) stored else getIncomeDefault(salary, key)
    }

    fun getRegion(context: Context): Triple<String, String, String> {
        val country = prefs(context).getString(KEY_REGION_COUNTRY, "中国") ?: "中国"
        val province = prefs(context).getString(KEY_REGION_PROVINCE, "北京市") ?: "北京市"
        val city = prefs(context).getString(KEY_REGION_CITY, "东城区") ?: "东城区"
        return Triple(country, province, city)
    }

    fun updateRegion(context: Context, country: String, province: String, city: String) {
        prefs(context).edit()
            .putString(KEY_REGION_COUNTRY, country)
            .putString(KEY_REGION_PROVINCE, province)
            .putString(KEY_REGION_CITY, city)
            .apply()
    }

    fun clearAll(context: Context) {
        prefs(context).edit().clear().apply()
    }
}
