package com.example.lifeclock // 应用包名

import android.os.Bundle // Activity生命周期数据
import androidx.activity.ComponentActivity // Compose宿主Activity
import androidx.activity.compose.setContent // 设置Compose内容
import androidx.activity.enableEdgeToEdge // 启用边到边显示
import androidx.compose.runtime.LaunchedEffect // 组合后副作用
import androidx.compose.runtime.getValue // 委托属性读取
import androidx.compose.runtime.mutableStateOf // 可观察状态
import androidx.compose.runtime.remember // 跨重组记忆
import androidx.compose.runtime.setValue // 委托属性写入
import androidx.compose.ui.platform.LocalContext // 获取上下文
import com.example.lifeclock.ui.theme.LifeClockTheme // 应用主题
import java.time.LocalDate // Java日期类

// 页面导航枚举定义（应用所有页面）
enum class Screen { SETUP, COUNTDOWN, GENDER_LIFESPAN, SETTINGS, INCOME, BASIC_INFO } // 页面枚举（REGION已改为弹窗）

// 主Activity：管理全局用户状态和所有页面间的导航跳转
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { // Activity创建入口
        super.onCreate(savedInstanceState)               // 调用父类
        enableEdgeToEdge()                                // 启用边到边（内容延伸到状态栏和导航栏下）
        setContent {                                      // 设置Compose UI内容
            LifeClockTheme {                              // 应用主题包裹
                val context = LocalContext.current        // 获取Android上下文

                // ====== 全局状态变量 ======

                // 当前显示的页面（首次打开判断是否完成设置）
                var currentScreen by remember {
                    mutableStateOf(
                        if (UserPreferences.isSetupComplete(context)) Screen.COUNTDOWN // 已设置→主页
                        else Screen.SETUP                                               // 未设置→设置页
                    )
                }

                var birthday by remember {                                           // 用户的生日
                    mutableStateOf(
                        UserPreferences.getBirthday(context)?.let { LocalDate.parse(it) } // 从存储加载
                            ?: LocalDate.of(1990, 1, 1)                                  // 默认1990-01-01
                    )
                }
                var lifespan by remember {                                           // 当前活跃的预期寿命
                    mutableStateOf(UserPreferences.getExpectedLifespan(context))
                }
                var lifespanSource by remember {                                     // 寿命数据来源
                    mutableStateOf(UserPreferences.getLifespanSource(context))
                }
                var personalLifespan by remember {                                   // 用户个人设定的寿命（不受外部覆盖）
                    mutableStateOf(UserPreferences.getPersonalLifespan(context))
                }
                var gender by remember {                                             // 用户性别
                    mutableStateOf(
                        UserPreferences.getGender(context) ?: UserPreferences.GENDER_MALE // 默认男性
                    )
                }

                val retirementAge = UserPreferences.calculateRetirementAge(birthday, gender) // 根据生日和性别计算退休年龄

                // 当生日变更且当前为退休模式时，自动重新计算退休年龄
                LaunchedEffect(birthday) {
                    if (lifespanSource == UserPreferences.SOURCE_RETIREMENT) {       // 仅在退休模式下更新
                        val newAge = UserPreferences.calculateRetirementAge(birthday, gender) // 重新计算
                        lifespan = newAge                                            // 更新活跃寿命
                        UserPreferences.updateLifespan(context, newAge, UserPreferences.SOURCE_RETIREMENT) // 持久化
                    }
                }

                // ====== 页面路由 ======
                when (currentScreen) {
                    // 首次设置页面
                    Screen.SETUP -> {
                        SetupScreen(
                            onSetupComplete = {                                      // 设置完成回调
                                birthday = UserPreferences.getBirthday(context)?.let { LocalDate.parse(it) }!! // 读取生日
                                lifespan = UserPreferences.getExpectedLifespan(context)    // 读取寿命
                                lifespanSource = UserPreferences.getLifespanSource(context) // 读取寿命来源
                                personalLifespan = UserPreferences.getPersonalLifespan(context) // 读取个人寿命
                                gender = UserPreferences.getGender(context) ?: UserPreferences.GENDER_MALE // 读取性别
                                currentScreen = Screen.COUNTDOWN                     // 跳转到主页
                            }
                        )
                    }

                    // 主页倒计时
                    Screen.COUNTDOWN -> {
                        // 计算收入数据（薪资和各五险一金）
                        val salary = UserPreferences.getSalary(context)              // 月薪
                        val base = UserPreferences.getContributionBase(context, salary) // 缴费基数（默认=月薪）
                        val region = UserPreferences.getRegion(context)              // 读取地区
                        val province = region.second                                 // 省份（用于养老金计算）
                        // 退休后每月领取的养老金（基础养老金+个人账户养老金），若已手动设定则用手动值
                        val calculatedPension = UserPreferences.calculateMonthlyPension(salary, province, retirementAge)
                        val storedPension = UserPreferences.getPension(context)
                        val pension = if (storedPension >= 0f) storedPension else calculatedPension // 手动值优先
                        // 养老保险个人缴费（从月薪中扣除的部分，缴费基数×8%）
                        val pensionContribution = UserPreferences.getIncomeValue(     // 养老缴费
                            context, UserPreferences.KEY_PENSION, base)              // 按缴费基数×8%计算
                        val medical = UserPreferences.getIncomeValue(                // 医疗保险
                            context, UserPreferences.KEY_MEDICAL, base)              // 按缴费基数计算
                        val unemployment = UserPreferences.getIncomeValue(           // 失业保险
                            context, UserPreferences.KEY_UNEMPLOYMENT, base)
                        val workInjury = UserPreferences.getIncomeValue(             // 工伤保险
                            context, UserPreferences.KEY_WORK_INJURY, base)
                        val maternity = UserPreferences.getIncomeValue(              // 生育保险
                            context, UserPreferences.KEY_MATERNITY, base)
                        val housingFund = UserPreferences.getIncomeValue(            // 住房公积金
                            context, UserPreferences.KEY_HOUSING_FUND, base)
                        // 五险一金总扣除 = 养老缴费 + 医疗 + 失业 + 工伤 + 生育 + 公积金（不含退休后领取的养老金）
                        val totalDeductions = pensionContribution + medical + unemployment + workInjury + maternity + housingFund
                        val fixedExpense = UserPreferences.getFixedExpense(context)   // 月固定支出
                        val monthlyDeposit = UserPreferences.getDeposit(context)      // 每月存款
                        val netIncome = maxOf(0f, salary - totalDeductions - fixedExpense) // 月净收入 = 月薪 - 五险一金 - 固定支出
                        CountdownScreen(
                            birthday = birthday,                                     // 生日
                            personalLifespan = lifespan,                             // 当前活跃寿命
                            lifespanSource = lifespanSource,                         // 寿命来源
                            retirementAge = retirementAge,                           // 退休年龄
                            userPersonalLifespan = personalLifespan,                  // 用户个人寿命（用于菜单显示）
                            monthlySalary = salary,                                  // 月薪
                            monthlyPension = pension,                                // 月养老金
                            netMonthlyIncome = netIncome,                            // 月净收入
                            monthlyDeposit = monthlyDeposit,                         // 每月存款
                            onNavigateToGenderLifespan = {
                                currentScreen = Screen.GENDER_LIFESPAN               // 跳转人均预期寿命页
                            },
                            onNavigateToSettings = {
                                currentScreen = Screen.SETTINGS                      // 跳转设置页
                            },
                            onSelectRetirement = {                                   // 切换到退休模式
                                lifespan = retirementAge                             // 寿命设为退休年龄
                                lifespanSource = UserPreferences.SOURCE_RETIREMENT   // 来源标为退休
                                UserPreferences.updateLifespan(context, retirementAge, UserPreferences.SOURCE_RETIREMENT)
                            },
                            onSelectPersonal = {                                     // 切换到个人模式
                                lifespan = personalLifespan                          // 恢复个人设定
                                lifespanSource = UserPreferences.SOURCE_PERSONAL     // 来源标为个人
                                UserPreferences.updateLifespan(context, personalLifespan, UserPreferences.SOURCE_PERSONAL)
                            }
                        )
                    }

                    // 中国人均预期寿命详情页
                    Screen.GENDER_LIFESPAN -> {
                        GenderLifespanScreen(
                            gender = gender,                                         // 用户性别
                            onBack = { currentScreen = Screen.COUNTDOWN },            // 返回主页
                            onSelectLifespan = { selectedLifespan, source ->          // 选中某个人均寿命卡片
                                lifespan = selectedLifespan.toFloat()                 // 更新活跃寿命
                                lifespanSource = source                               // 更新来源
                                UserPreferences.updateLifespan(context, selectedLifespan.toFloat(), source)
                                currentScreen = Screen.COUNTDOWN                      // 返回主页
                            }
                        )
                    }

                    // 设置页面
                    Screen.SETTINGS -> {
                        SettingsScreen(
                            onBack = { currentScreen = Screen.COUNTDOWN },            // 返回主页
                            onNavigateToBasicInfo = {
                                currentScreen = Screen.BASIC_INFO                     // 进入基本信息页
                            },
                            onNavigateToIncome = {
                                currentScreen = Screen.INCOME                         // 进入收入设置页
                            }
                        )
                    }

                    // 基本信息页面（寿命、生日、性别、地区）
                    Screen.BASIC_INFO -> {
                        BasicInfoScreen(
                            birthday = birthday,                                     // 生日
                            gender = gender,                                         // 性别
                            personalLifespan = personalLifespan,                      // 个人寿命
                            onBack = { currentScreen = Screen.SETTINGS },             // 返回设置页
                            onBirthdayChanged = { newBirthday ->                      // 生日变更
                                birthday = newBirthday                                // 更新状态
                                UserPreferences.updateBirthday(context, newBirthday.toString()) // 持久化
                            },
                            onGenderChanged = { newGender ->                          // 性别变更
                                gender = newGender                                    // 更新状态
                                UserPreferences.updateGender(context, newGender)      // 持久化
                            },
                            onLifespanChanged = { newLifespan ->                      // 预期寿命变更
                                personalLifespan = newLifespan                        // 更新个人寿命
                                lifespan = newLifespan                                // 同时设为活跃寿命
                                lifespanSource = UserPreferences.SOURCE_PERSONAL      // 来源标为个人
                                UserPreferences.updatePersonalLifespan(context, newLifespan) // 持久化个人值
                                UserPreferences.updateLifespan(context, newLifespan, UserPreferences.SOURCE_PERSONAL)
                            }
                        )
                    }

                    // 收入设置页面
                    Screen.INCOME -> {
                        IncomeScreen(
                            onBack = { currentScreen = Screen.SETTINGS },             // 返回设置页
                            onSalaryChanged = { _ -> }                                // 薪资变更回调（暂无需额外处理）
                        )
                    }
                }
            }
        }
    }
}
