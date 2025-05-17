package abv.logopek.netmd


import abv.logopek.netmd.netschoolapi.Diary
import abv.logopek.netmd.netschoolapi.NetSchoolApi
import abv.logopek.netmd.ui.components.LessonView
import abv.logopek.netmd.ui.theme.NetMDTheme
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import okhttp3.HttpUrl.Companion.toHttpUrl
import waitForNsessionId
import java.time.format.TextStyle
import kotlin.math.abs

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NetMDTheme {
                val currentDay = remember { mutableStateOf(Clock.System.todayIn(TimeZone.currentSystemDefault()).minus(Clock.System.todayIn(TimeZone.currentSystemDefault()).dayOfWeek.ordinal - DayOfWeek.MONDAY.ordinal, DateTimeUnit.DAY)) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column {


                        val context = LocalContext.current
                        val sharedPreferences = context.getSharedPreferences("ltp", Context.MODE_PRIVATE)
                        val url = sharedPreferences.getString("t_url", null)
                        val login = sharedPreferences.getString("login", null)
                        val password = sharedPreferences.getString("password", null)
                        val scid = sharedPreferences.getString("scid", null)

                        // Если данных нет, чистим и перенаправляем
                        if (url == null || login == null || password == null || scid == null) {
                            sharedPreferences.edit {
                                remove("login")
                                remove("password")
                                remove("scid")
                                remove("t_url")
                            }
                            checkLogin(context = context)

                        }
                        val nsApi = NetSchoolApi(url!!)
                        LaunchedEffect("1") {
                            withContext(Dispatchers.IO) {
                                nsApi.login(login!!, password!!, scid!!)

                            }
                        }
                        checkLogin(context)
                        MainDiary(currentDay, innerPadding, nsApi)
                    }

                }
            }
        }
    }

    private fun checkLogin(context: Context) {
        val sharedPreferences = getSharedPreferences("ltp", MODE_PRIVATE)
        if (!sharedPreferences.contains("login") || !sharedPreferences.contains("password") || !sharedPreferences.contains(
                "scid"
            ) || !sharedPreferences.contains("t_url")
        ) {
            startActivity(Intent(context, LoginActivity::class.java))
            finish()
        }
    }

    data class DiaryUiState(
        val date: LocalDate?, // Дата, к которой относится текущее состояние
        val diary: Diary? = null,
        val isLoading: Boolean = true,
        val error: String? = null
    )

    @Composable
    fun MainDiary(
        currentDay: MutableState<LocalDate>,
        p: PaddingValues,
        nsApi: NetSchoolApi?
    ) {


        var uiState by remember { mutableStateOf(DiaryUiState(date = currentDay.value)) }

        LaunchedEffect(currentDay.value, nsApi) {
            if (nsApi == null || currentDay.value == null) {
                uiState = DiaryUiState(
                    date = currentDay.value,
                    isLoading = false,
                    error = if (nsApi == null) "Ошибка: API клиент недоступен." else "Дата не выбрана."
                )
                return@LaunchedEffect
            }

            uiState = uiState.copy(date = currentDay.value, isLoading = true, error = null)

            try {
                withContext(Dispatchers.IO) {

                    waitForNsessionId(nsApi.client.cookieJar, nsApi.baseurl.toHttpUrl())

                    val initialDiaryData = nsApi.diary(currentDay.value, null)
                    val loadedDiary = nsApi.diaryObjects(initialDiaryData)

                    uiState = uiState.copy(diary = loadedDiary, isLoading = false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                uiState = uiState.copy(isLoading = false, error = "Ошибка загрузки данных: ${e.message}", diary = null)
            }
        }

        val swipeThreshold = 70.dp
        val density = LocalDensity.current

        Box(
            modifier = Modifier
                .padding(p)
                .fillMaxSize()
                .pointerInput(Unit) {
                    var totalHorizontalDragDistance = 0f
                        detectHorizontalDragGestures(
                            onDragStart = {
                                totalHorizontalDragDistance = 0f
                            },
                            onHorizontalDrag = { _: PointerInputChange, dragAmount: Float ->
                                totalHorizontalDragDistance += dragAmount

                            },
                            onDragEnd = {
                                val thresholdPx = with(density) { swipeThreshold.toPx() }

                                if (abs(totalHorizontalDragDistance) > thresholdPx && currentDay.value != null) {
                                    val newDate = if (totalHorizontalDragDistance < 0) {

                                        currentDay.value!!.plus(1, DateTimeUnit.WEEK)
                                    } else {

                                        currentDay.value!!.minus(1, DateTimeUnit.WEEK)
                                    }



                                    currentDay.value = newDate
                                }

                            },
                            onDragCancel = {
                                totalHorizontalDragDistance = 0f

                            }
                        )
                }
        ) {
            AnimatedContent(
                targetState = uiState,
                transitionSpec = {

                    val initialLoaded = initialState.diary != null && !initialState.isLoading && initialState.date != null
                    val targetLoaded = targetState.diary != null && !targetState.isLoading && targetState.date != null
                    val datesDifferent = initialLoaded && targetLoaded && initialState.date != targetState.date

                    when {

                        datesDifferent -> {

                            val direction = if (targetState.date!! > initialState.date!!) {
                                1
                            } else {
                                -1
                            }

                            if (direction == 1) {
                                slideInHorizontally(
                                    initialOffsetX = { fullWidth -> fullWidth },
                                    animationSpec = tween(300)
                                ) + fadeIn(animationSpec = tween(300)) togetherWith
                                        slideOutHorizontally(
                                            targetOffsetX = { fullWidth -> -fullWidth },
                                            animationSpec = tween(300)
                                        ) + fadeOut(animationSpec = tween(300))
                            } else {
                                slideInHorizontally(
                                    initialOffsetX = { fullWidth -> -fullWidth },
                                    animationSpec = tween(300)
                                ) + fadeIn(animationSpec = tween(300)) togetherWith
                                        slideOutHorizontally(
                                            targetOffsetX = { fullWidth -> fullWidth },
                                            animationSpec = tween(300)
                                        ) + fadeOut(animationSpec = tween(300))
                            }
                        }


                        targetState.isLoading -> {
                            ContentTransform(
                                targetContentEnter = fadeIn(animationSpec = tween(300)),
                                initialContentExit = fadeOut(animationSpec = tween(300))
                            )

                        }


                        initialState.isLoading || initialState.error != null -> {
                            ContentTransform(
                                targetContentEnter = fadeIn(animationSpec = tween(300)),
                                initialContentExit = fadeOut(animationSpec = tween(300))
                            )

                        }


                        else -> fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))

                    }.using(SizeTransform(clip = false))
                }, label = "DiaryWeekTransition"
            ) { targetUiState ->


                Box(modifier = Modifier.fillMaxSize()) {
                    when {
                        targetUiState.isLoading -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                        targetUiState.error != null -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(text = targetUiState.error, color = MaterialTheme.colorScheme.error)
                            }
                        }
                        targetUiState.diary == null || targetUiState.diary.weekDays.isEmpty() -> {

                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Данные дневника отсутствуют для этой недели.")
                            }
                        }
                        else -> {
                            val sortedWeekDays = targetUiState.diary.weekDays.sortedBy { it.date }
                            LazyColumn {
                                items(sortedWeekDays) { weekDay ->
                                    Box(Modifier.fillMaxWidth()) {
                                        Column(Modifier.padding(8.dp)) {
                                            val locale = LocalConfiguration.current.locales.get(0)
                                            Text(
                                                text =" ${weekDay.date.dayOfWeek.getDisplayName(TextStyle.FULL, locale)
                                                    .replaceFirstChar {
                                                        if (it.isLowerCase()) it.titlecase(locale) else it.toString()
                                                    }}  ${weekDay.date.date}",
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            Spacer(Modifier.height(4.dp))
                                            for (lesson in weekDay.lessons) {
                                                LessonView(lesson)
                                                Spacer(Modifier.height(4.dp))
                                            }
                                        }
                                    }
                                    HorizontalDivider()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}






