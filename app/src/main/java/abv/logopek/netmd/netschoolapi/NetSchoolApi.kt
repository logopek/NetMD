package abv.logopek.netmd.netschoolapi

import CookieJarC
import android.util.Log
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlinx.serialization.json.Json
import okhttp3.CookieJar
import okhttp3.FormBody
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
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
    var headers = Headers.Builder().add("user-agent", "NetSchoolAPIKotlin/1.0.0").add("referer", url)
    var studentId = -1
    var yearId = -1
    var schoolId = -1
    var loginData = mutableListOf("")
    var accessToken = ""

    private var loginFinished = false
    fun login(uname: String, password: String ,schoolNameOrId: String): Boolean {
        /**
         * Init login and grant access token, this method need to be called before all others
         * @param uname Username for account
         * @param password: Password
         * @param schoolNameOrId: For now, only a school id
         * @return Nothing
         */
        Log.d("NS", "Login request")
        var response = client.newCall(Request.Builder().url("$baseurl/logindata").build()).execute()
        if (!response.isSuccessful) {
            Log.d("NS", "Fail on logindata")
            throw NoResponseFromServer()
        }
        response.close()
        response = client.newCall(Request.Builder().url("$baseurl/auth/getdata").post("".toRequestBody("".toMediaTypeOrNull())).build()).execute()
        if (!response.isSuccessful) {
            Log.d("NS","Fail on getdata")
            throw NoResponseFromServer()
        }

        val loginData = json.decodeFromString<LoginData>(response.body!!.string());
        response.close()
        val newLogin = LoginGet(loginType = "1", scid = schoolNameOrId, un = uname, pw2=hashPassword(password, loginData.salt), lt=loginData.lt, ver = loginData.ver)
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

    fun diary(startDate: LocalDate?, endDate: LocalDate?): String {
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

        var response = client.newCall(Request.Builder().url(form.build()).headers(headers.build()).build()).execute()
        var d = response.body!!.string()
        return d
    }

    fun diaryObjects(diaryString: String): Diary {
        Log.d("NS-DECODE", json.decodeFromString<Diary>(diaryString).toString())
        return json.decodeFromString<Diary>(diaryString)
    }
}