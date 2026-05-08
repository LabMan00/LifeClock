package com.example.lifeclock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.lifeclock.ui.theme.LifeClockTheme
import java.time.LocalDate

enum class Screen { SETUP, COUNTDOWN, GENDER_LIFESPAN, SETTINGS, INCOME, REGION }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LifeClockTheme {
                val context = LocalContext.current

                var currentScreen by remember {
                    mutableStateOf(
                        if (UserPreferences.isSetupComplete(context)) Screen.COUNTDOWN
                        else Screen.SETUP
                    )
                }

                var birthday by remember {
                    mutableStateOf(
                        UserPreferences.getBirthday(context)?.let { LocalDate.parse(it) }
                            ?: LocalDate.of(1990, 1, 1)
                    )
                }
                var lifespan by remember {
                    mutableStateOf(UserPreferences.getExpectedLifespan(context))
                }
                var lifespanSource by remember {
                    mutableStateOf(UserPreferences.getLifespanSource(context))
                }
                var personalLifespan by remember {
                    mutableStateOf(UserPreferences.getPersonalLifespan(context))
                }
                var gender by remember {
                    mutableStateOf(
                        UserPreferences.getGender(context) ?: UserPreferences.GENDER_MALE
                    )
                }

                val retirementAge = UserPreferences.calculateRetirementAge(birthday, gender)

                LaunchedEffect(birthday) {
                    if (lifespanSource == UserPreferences.SOURCE_RETIREMENT) {
                        val newAge = UserPreferences.calculateRetirementAge(birthday, gender)
                        lifespan = newAge
                        UserPreferences.updateLifespan(context, newAge, UserPreferences.SOURCE_RETIREMENT)
                    }
                }

                when (currentScreen) {
                    Screen.SETUP -> {
                        SetupScreen(
                            onSetupComplete = {
                                birthday = UserPreferences.getBirthday(context)?.let { LocalDate.parse(it) }!!
                                lifespan = UserPreferences.getExpectedLifespan(context)
                                lifespanSource = UserPreferences.getLifespanSource(context)
                                personalLifespan = UserPreferences.getPersonalLifespan(context)
                                gender = UserPreferences.getGender(context) ?: UserPreferences.GENDER_MALE
                                currentScreen = Screen.COUNTDOWN
                            }
                        )
                    }

                    Screen.COUNTDOWN -> {
                        val salary = UserPreferences.getSalary(context)
                        val pension = UserPreferences.getIncomeValue(
                            context, UserPreferences.KEY_PENSION, salary
                        )
                        val medical = UserPreferences.getIncomeValue(
                            context, UserPreferences.KEY_MEDICAL, salary
                        )
                        val unemployment = UserPreferences.getIncomeValue(
                            context, UserPreferences.KEY_UNEMPLOYMENT, salary
                        )
                        val workInjury = UserPreferences.getIncomeValue(
                            context, UserPreferences.KEY_WORK_INJURY, salary
                        )
                        val maternity = UserPreferences.getIncomeValue(
                            context, UserPreferences.KEY_MATERNITY, salary
                        )
                        val housingFund = UserPreferences.getIncomeValue(
                            context, UserPreferences.KEY_HOUSING_FUND, salary
                        )
                        val totalDeductions = pension + medical + unemployment + workInjury + maternity + housingFund
                        val netIncome = maxOf(0f, salary - totalDeductions)
                        CountdownScreen(
                            birthday = birthday,
                            personalLifespan = lifespan,
                            lifespanSource = lifespanSource,
                            retirementAge = retirementAge,
                            userPersonalLifespan = personalLifespan,
                            monthlySalary = salary,
                            monthlyPension = pension,
                            netMonthlyIncome = netIncome,
                            onNavigateToGenderLifespan = {
                                currentScreen = Screen.GENDER_LIFESPAN
                            },
                            onNavigateToSettings = {
                                currentScreen = Screen.SETTINGS
                            },
                            onSelectRetirement = {
                                lifespan = retirementAge
                                lifespanSource = UserPreferences.SOURCE_RETIREMENT
                                UserPreferences.updateLifespan(context, retirementAge, UserPreferences.SOURCE_RETIREMENT)
                            },
                            onSelectPersonal = {
                                lifespan = personalLifespan
                                lifespanSource = UserPreferences.SOURCE_PERSONAL
                                UserPreferences.updateLifespan(context, personalLifespan, UserPreferences.SOURCE_PERSONAL)
                            }
                        )
                    }

                    Screen.GENDER_LIFESPAN -> {
                        GenderLifespanScreen(
                            gender = gender,
                            onBack = { currentScreen = Screen.COUNTDOWN },
                            onSelectLifespan = { selectedLifespan, source ->
                                lifespan = selectedLifespan.toFloat()
                                lifespanSource = source
                                UserPreferences.updateLifespan(context, selectedLifespan.toFloat(), source)
                                currentScreen = Screen.COUNTDOWN
                            }
                        )
                    }

                    Screen.SETTINGS -> {
                        SettingsScreen(
                            birthday = birthday,
                            gender = gender,
                            personalLifespan = personalLifespan,
                            onBack = { currentScreen = Screen.COUNTDOWN },
                            onBirthdayChanged = { newBirthday ->
                                birthday = newBirthday
                                UserPreferences.updateBirthday(context, newBirthday.toString())
                            },
                            onGenderChanged = { newGender ->
                                gender = newGender
                                UserPreferences.updateGender(context, newGender)
                            },
                            onLifespanChanged = { newLifespan ->
                                personalLifespan = newLifespan
                                lifespan = newLifespan
                                lifespanSource = UserPreferences.SOURCE_PERSONAL
                                UserPreferences.updatePersonalLifespan(context, newLifespan)
                                UserPreferences.updateLifespan(context, newLifespan, UserPreferences.SOURCE_PERSONAL)
                            },
                            onNavigateToIncome = {
                                currentScreen = Screen.INCOME
                            },
                            onNavigateToRegion = {
                                currentScreen = Screen.REGION
                            }
                        )
                    }

                    Screen.INCOME -> {
                        IncomeScreen(
                            onBack = { currentScreen = Screen.SETTINGS },
                            onSalaryChanged = { _ -> }
                        )
                    }

                    Screen.REGION -> {
                        RegionPickerScreen(
                            onBack = { currentScreen = Screen.SETTINGS },
                            onRegionSelected = { _, _, _ -> }
                        )
                    }
                }
            }
        }
    }
}
