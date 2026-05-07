package com.adaptive_tutor_mobile.di

import android.webkit.CookieManager
import com.adaptive_tutor_mobile.data.remote.api.AuthApi
import com.adaptive_tutor_mobile.data.remote.dto.RefreshResponse
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

private const val BASE_URL = "https://api.adaptiveelearning.online/"

// ── WebKit-backed CookieJar ───────────────────────────────────────────────────

class WebKitCookieJar : CookieJar {
    private val cookieManager = CookieManager.getInstance().apply {
        setAcceptCookie(true)
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val urlString = url.toString()
        cookies.forEach { cookie ->
            cookieManager.setCookie(urlString, cookie.toString())
        }
        cookieManager.flush()
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val cookieString = cookieManager.getCookie(url.toString()) ?: return emptyList()
        return cookieString.split(";").mapNotNull { part ->
            Cookie.parse(url, part.trim())
        }
    }
}

// ── Auth Interceptor ──────────────────────────────────────────────────────────

class AuthInterceptor(private val sessionStore: SessionStore) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = sessionStore.getAccessToken()
        val request = if (token != null) {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        return chain.proceed(request)
    }
}

// ── Token Refresh Authenticator ───────────────────────────────────────────────

class TokenRefreshAuthenticator(
    private val sessionStore: SessionStore,
    private val plainClientProvider: () -> OkHttpClient
) : okhttp3.Authenticator {

    override fun authenticate(route: okhttp3.Route?, response: Response): Request? {
        // Avoid refresh loop — if the failing request is already the refresh endpoint, bail out
        if (response.request.url.encodedPath.contains("auth/refresh")) return null

        // Only handle 401
        if (response.code != 401) return null

        // Synchronous refresh using the plain client (no auth interceptor)
        return try {
            val plainClient = plainClientProvider()
            val refreshRequest = Request.Builder()
                .url("${BASE_URL}api/v1/auth/refresh")
                .post("".toRequestBody())
                .build()
            val refreshResponse = plainClient.newCall(refreshRequest).execute()
            if (refreshResponse.isSuccessful) {
                val body = refreshResponse.body?.string()
                val gson = Gson()
                val refreshDto = gson.fromJson(body, RefreshResponse::class.java)
                val newToken = refreshDto.accessToken
                if (newToken != null) {
                    sessionStore.saveAccessToken(newToken)
                    response.request.newBuilder()
                        .header("Authorization", "Bearer $newToken")
                        .build()
                } else {
                    sessionStore.emitForceLogout()
                    null
                }
            } else {
                sessionStore.emitForceLogout()
                null
            }
        } catch (e: Exception) {
            sessionStore.emitForceLogout()
            null
        }
    }
}

// ── Hilt Module ───────────────────────────────────────────────────────────────

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideCookieJar(): WebKitCookieJar = WebKitCookieJar()

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

    @Provides
    @Singleton
    @Named("plain")
    fun providePlainOkHttpClient(
        cookieJar: WebKitCookieJar,
        logging: HttpLoggingInterceptor
    ): OkHttpClient =
        OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .addInterceptor(logging)
            .build()

    @Provides
    @Singleton
    @Named("auth")
    fun provideAuthOkHttpClient(
        cookieJar: WebKitCookieJar,
        logging: HttpLoggingInterceptor,
        sessionStore: SessionStore,
        @Named("plain") plainClient: OkHttpClient
    ): OkHttpClient =
        OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .addInterceptor(AuthInterceptor(sessionStore))
            .authenticator(TokenRefreshAuthenticator(sessionStore) { plainClient })
            .addInterceptor(logging)
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(@Named("auth") client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideCourseApi(retrofit: Retrofit): com.adaptive_tutor_mobile.data.remote.api.CourseApi =
        retrofit.create(com.adaptive_tutor_mobile.data.remote.api.CourseApi::class.java)
}
