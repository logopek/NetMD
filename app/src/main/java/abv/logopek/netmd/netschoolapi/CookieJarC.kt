import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import java.util.concurrent.ConcurrentHashMap

class CookieJarC : CookieJar {

    // Пример простого хранилища в памяти, привязанного к хосту
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

// Использование кастомного CookieJar:
/*
val customCookieJar = CustomCookieJar()

val clientWithCustomJar = OkHttpClient.Builder()
    .cookieJar(customCookieJar)
    .build()
*/