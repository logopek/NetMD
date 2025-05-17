import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import java.util.concurrent.ConcurrentHashMap

class CookieJarC : CookieJar {
    private val cookiesByHost = ConcurrentHashMap<String, MutableList<Cookie>>()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val hostCookies = cookiesByHost.computeIfAbsent(url.host) { mutableListOf() }
        hostCookies.addAll(cookies)
        println("Saved ${cookies.size} cookies for ${url.host}")
        cookies.forEach { println("- $it") }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val hostCookies = cookiesByHost[url.host] ?: return emptyList()

        val validCookies = hostCookies

        println("Loading ${validCookies.size} cookies for ${url.host}")
        validCookies.forEach { println("- $it") }


        return validCookies
    }


}
suspend fun waitForNsessionId(
    cookieJarC: CookieJar,
    url: HttpUrl,
    timeoutMillis: Long = 15000L,
    checkIntervalMillis: Long = 300L
) {
    println("waitForNsessionId: Ожидание куки NSESSIONID для URL ${url.host} (таймаут: ${timeoutMillis}мс)...")
    try {
        withTimeout(timeoutMillis) {
            while (true) {
                val cookies: List<Cookie> = cookieJarC.loadForRequest(url)
                val nsessionIdCookie = cookies.find { it.name == "NSSESSIONID" }
                val esrnSec = cookies.find { it.name == "ESRNSec" }
                val tts = cookies.find { it.name == "TTSLogin" }
                val securekey = cookies.find { it.name == "securekey" }
                if (nsessionIdCookie != null && esrnSec != null && tts != null && securekey != null) {
                    return@withTimeout
                }

                delay(checkIntervalMillis)
            }
        }
    } catch (e: TimeoutCancellationException) {
        throw e
    } catch (e: Exception) {
        throw e
    }
}
