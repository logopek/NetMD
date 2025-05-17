package abv.logopek.netmd.netschoolapi

import CookieJarC
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.core.text.isDigitsOnly
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlinx.serialization.json.Json
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.FormBody
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import waitForNsessionId
import java.security.MessageDigest
import kotlin.math.log
import java.nio.charset.Charset
import java.util.Date

fun hashPassword(password: String, salt: String): String {
    // Equivalent of password.encode('windows-1251')
    val passwordBytesWindows1251 = password.toByteArray(Charset.forName("windows-1251"))

    // Equivalent of md5(password.encode('windows-1251')).hexdigest().encode()
    val md5 = MessageDigest.getInstance("MD5")
    val encodedPasswordBytes = md5.digest(passwordBytesWindows1251).toHexString().toByteArray()

    // Equivalent of md5(salt.encode() + encoded_password).hexdigest()
    val saltBytes = salt.encodeToByteArray()
    val combinedBytes = saltBytes + encodedPasswordBytes
    val pw2 = MessageDigest.getInstance("MD5").digest(combinedBytes).toHexString()

    // Equivalent of pw2[: len(password)]


    return pw2
}

// Extension function to convert ByteArray to Hex String
fun ByteArray.toHexString() = joinToString("") { "%02x".format(it) }

/*
// Example usage:
fun main() {
    val password = "mysecretpassword"
    val salt = "randomsalt"
    val hashedPassword = hashPassword(password, salt)
    println(hashedPassword)
}
*/

private fun dieOnBadStatus(response: Response){
    if(!response.isRedirect){
        throw HTTPStatusError()
    }
}

class NetSchoolApi(private val url: String) {
    var json = Json { ignoreUnknownKeys = true }
    var cookieJarC = CookieJarC()
    var client = OkHttpClient.Builder().cookieJar(cookieJarC).build()
    var baseurl = "$url/webapi"
    var host = url.replace("https://", "").replace("/", "")
    var headers = Headers.Builder().add("user-agent", "NetSchoolAPIKotlin/1.0.1").add("referer", "$url/authrorize/login")
    var studentId = -1
    var yearId = -1
    var schoolId = -1
    var loginData = mutableListOf("")
    var accessToken = ""

    private var loginFinished = false

    /**
     * Init login and grant access token, this method need to be called before all others
     * @param uname Username for account
     * @param password: Password
     * @param schoolNameOrId: School name or school id
     * @return Login success
     */
    fun login(uname: String, password: String ,schoolNameOrId: String): Boolean {
        var school = schoolNameOrId
        Log.d("NS", "Login request")
        var response = client.newCall(Request.Builder().url("$baseurl/logindata").build()).execute()
        if (!response.isSuccessful) {
            Log.d("NS", "Fail on logindata")
            throw NoResponseFromServer()
        }

        response = client.newCall(Request.Builder().url("$baseurl/auth/getdata").post("".toRequestBody("".toMediaTypeOrNull())).build()).execute()
        if (!response.isSuccessful) {
            Log.d("NS","Fail on getdata")
            throw NoResponseFromServer()
        }

        val loginData = json.decodeFromString<LoginData>(response.body!!.string());
        response.close()
        if (!school.isDigitsOnly()){
            var schoolList = schools(school)
            if (schoolList.isNotEmpty()){
                school = schoolList[0].id.toString()
            }
            else{
                return false
            }
        }
        val newLogin = LoginGet(loginType = "1", scid = school, un = uname, pw2=hashPassword(password, loginData.salt), lt=loginData.lt, ver = loginData.ver)
        Log.d("NS", "PW2: ${newLogin.pw2}, SALT: ${loginData.salt}")

        val form = FormBody.Builder()
            .add("lt", newLogin.lt)
            .add("loginType", "1")
            .add("scid", newLogin.scid)
            .add("un", newLogin.un)
            .add("pw2", newLogin.pw2)
            .add("ver", newLogin.ver)
            .add("pw", newLogin.pw2.substring(0, newLogin.pw2.length))
            .build()

        response = client.newCall(Request.Builder().url("$baseurl/login").headers(headers.build()).post(form).build()).execute()
        var c: MutableList<Cookie> = mutableListOf()
        for (setCookieString in response.headers("Set-Cookie")) {

            val cookie = Cookie.parse("$baseurl/logindata".toHttpUrl(), setCookieString)
            if (cookie != null) {
                c.add(cookie)
                // Опционально: логгировать разобранные куки
                println("Parsed cookie: ${cookie.name}=${cookie.value} for domain ${cookie.domain}")
            } else {
                // Опционально: логгировать ошибки парсинга, если формат заголовка некорректен
                println("Failed to parse Set-Cookie header: $setCookieString")
            }
        }
        client.cookieJar.saveFromResponse("$baseurl/logindata".toHttpUrl(), c.toList())
        val loginDataDecoded = json.decodeFromString<AuthResponse>(response.body!!.string());
        if (loginDataDecoded.at == ""){
            return false
        }
        accessToken = loginDataDecoded.at
        headers.add("at", accessToken)
        response.close()

        response = client.newCall(Request.Builder().url("$baseurl/student/diary/init").headers(headers.build()).build()).execute()
        if(!response.isSuccessful) throw NetSchoolApiError()
        val diaryInit = json.decodeFromString<DiaryResponse>(response.body!!.string())
        response.close()
        studentId = diaryInit.students[0].studentId


        response = client.newCall(Request.Builder().url("$baseurl/years/current").headers(headers.build()).build()).execute()

        yearId = json.decodeFromString<SchoolYearResponse>(response.body!!.string()).id

        loginFinished = true
        return true
    }

    /**
     * @return diary as a String object
     * @param startDate Start of week
     * @param endDate End of week
     */
    fun diary(startDate: LocalDate?, endDate: LocalDate?): String {



        var response = client.newCall(Request.Builder().url("$baseurl/logindata").build()).execute()
        if (!response.isSuccessful) {
            Log.d("NS", "Fail on logindata")
            throw NoResponseFromServer()
        }
        var start = startDate
        var end = endDate
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val daysToSubtract = today.dayOfWeek.ordinal - DayOfWeek.MONDAY.ordinal
        if (start == null) {
            start = today.minus(daysToSubtract, DateTimeUnit.DAY)

        }
        if (end == null) {
            end = start.plus(6, DateTimeUnit.DAY)
        }
        Log.d("NS",start.toString())
        Log.d("NS", "$studentId - $yearId - $start - $end")
        val form = HttpUrl.Builder().scheme("https").host(host).addPathSegments("webapi/student/diary")
            .addQueryParameter("studentId", studentId.toString())
            .addQueryParameter("yearId", (yearId).toString())
            .addQueryParameter("weekStart", start.toString())
            .addQueryParameter("weekEnd", end.toString())

        response = client.newCall(Request.Builder().url(form.build()).headers(headers.build()).build()).execute()
        var d = response.body!!.string()
        return d
    }

    /**
     * @param diaryString String got from diary()
     * @return diary as Diary object
     * @see diary
     * @see Diary
     */
    fun diaryObjects(diaryString: String): Diary {

        //Log.d("NS-DECODE", json.decodeFromString<Diary>(diaryString).toString())
        return json.decodeFromString<Diary>(diaryString)
    }

    /**
     * Overdue tasks
     * @param startDate Start of week
     * @param endDate End of week
     * @see diary
     * @return List<Assignment>
     */
    fun overdue(startDate: LocalDate?, endDate: LocalDate?): List<Assignment> {

        var start = startDate
        var end = endDate
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val daysToSubtract = today.dayOfWeek.ordinal - DayOfWeek.MONDAY.ordinal
        if (start == null) {
            start = today.minus(daysToSubtract, DateTimeUnit.DAY)

        }
        if (end == null) {
            end = start.plus(6, DateTimeUnit.DAY)
        }


        val form = HttpUrl.Builder().scheme("https").host(host).addPathSegments("student/diary/pastMandatory")
            .addQueryParameter("studentId", studentId.toString())
            .addQueryParameter("yearId", yearId.toString())
            .addQueryParameter("weekStart", start.toString())
            .addQueryParameter("weekEnd", end.toString())
        var response = client.newCall(Request.Builder().url(form.build()).headers(headers.build()).build()).execute()
        return json.decodeFromString<List<Assignment>>(response.body!!.string())
    }

    /**
     * Destroy accessToken
     */
    fun logout(): Response {
        return client.newCall(Request.Builder().url("$baseurl/auth/logout").post("".toRequestBody()).build()).execute()
    }

    /**
     * @param name Name of school
     * @return List<Organization>
     */
    fun schools(name: String?): List<Organization> {

        val form = HttpUrl.Builder().scheme("https").host(host).addPathSegments("/webapi/schools/search")
            .addQueryParameter("name", name).build()
        val response = client.newCall(Request.Builder().url(form).headers(headers.build()).build()).execute()
        val schools = json.decodeFromString<List<Organization>>(response.body!!.string())
        Log.d("NS", schools.toString())
        return schools
    }

    /**
     * Returns Nothing now
     */
    fun downloadProfilePicture(userId: Int){
        val form = HttpUrl.Builder().scheme("https").host(host).addPathSegments("webapi/users/photo")
            .addQueryParameter("at", accessToken)
            .addQueryParameter("userId", userId.toString())

    }
}