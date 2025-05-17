package abv.logopek.netmd


import abv.logopek.netmd.netschoolapi.AuthError
import abv.logopek.netmd.netschoolapi.Diary
import abv.logopek.netmd.netschoolapi.NetSchoolApi
import abv.logopek.netmd.netschoolapi.SchoolNotFoundError
import abv.logopek.netmd.ui.components.LessonView
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import abv.logopek.netmd.ui.theme.NetMDTheme
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Space
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import java.io.IOException
import kotlin.math.log

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NetMDTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    var currentDay: LocalDateTime? by remember { mutableStateOf(null) }
                    var context = LocalContext.current
                    checkLogin(context)
                    MainDiary(currentDay, innerPadding)
                }
            }
        }
    }

    fun checkLogin(context: Context){
        var sharedPreferences = getSharedPreferences("ltp", Context.MODE_PRIVATE)
        if (!sharedPreferences.contains("login") || !sharedPreferences.contains("password") || !sharedPreferences.contains("scid") || !sharedPreferences.contains("t_url")){
            startActivity(Intent(context, LoginActivity::class.java))
            finish()
        }
    }

    @Composable
    fun MainDiary(currentDay: LocalDateTime?, p: PaddingValues) {
        var t: Diary? by remember { mutableStateOf(null) }
        var sharedPreferences = getSharedPreferences("ltp", Context.MODE_PRIVATE)
        val url = sharedPreferences.getString("t_url", null)
        val login = sharedPreferences.getString("login", null)
        val password = sharedPreferences.getString("password", null)
        val scid = sharedPreferences.getString("scid", null)
        if(url == null || login == null || password == null || scid == null){
            sharedPreferences.edit {
                remove("login")
                remove("password")
                remove("scid")
                remove("t_url")
            }
            checkLogin(context = LocalContext.current)
            finish()
            return
        }
        val nsApi = NetSchoolApi(url)
        val context = LocalContext.current
        LaunchedEffect("1") {
            withContext(Dispatchers.IO) {
                nsApi.login(login, password, scid)
                var f = nsApi.diary(null, null)
                t = nsApi.diaryObjects(f)
            }
        }

        when(t){
            null -> {}
            else -> {
                val sortedWeekDays = t!!.weekDays.sortedBy { it.date }
                val sortedDiary = t!!.copy(weekDays = sortedWeekDays)
                t = sortedDiary
                LazyColumn(Modifier.padding(p)) {
                    items(t!!.weekDays) {
                        Box(Modifier.fillMaxWidth()){
                            Column(Modifier.padding(8.dp, 8.dp)) {
                                Text(DateTranslate.valueOf(it.date.dayOfWeek.name).value)
                                for (lesson in it.lessons) {
                                    LessonView(lesson)
                                    Spacer(Modifier.size(4.dp, 4.dp))
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






