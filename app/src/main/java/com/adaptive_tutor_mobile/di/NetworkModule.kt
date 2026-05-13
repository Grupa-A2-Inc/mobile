package com.adaptive_tutor_mobile.di

import android.webkit.CookieManager
import com.adaptive_tutor_mobile.data.remote.api.AuthApi
import com.adaptive_tutor_mobile.data.remote.api.CourseDetailApi
import com.adaptive_tutor_mobile.data.remote.api.EnrollmentApi
import com.adaptive_tutor_mobile.data.remote.api.LessonApi
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
import com.adaptive_tutor_mobile.data.remote.api.AdaptiveApi
import com.adaptive_tutor_mobile.data.remote.api.ProgressApi
import com.adaptive_tutor_mobile.data.remote.api.TestApi

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
    private val skipPaths = listOf(
        "/api/v1/auth/login",
        "/api/v1/auth/register",
        "/api/v1/auth/refresh",
        "/api/v1/auth/password-reset/request",
        "/api/v1/auth/password-reset/confirm",
        "/api/v1/auth/set-password"
    )

    override fun intercept(chain: Interceptor.Chain): Response {
        val req = chain.request()
        val path = req.url.encodedPath
        if (skipPaths.any { path.endsWith(it) }) {
            return chain.proceed(req)
        }
        val token = sessionStore.getAccessToken()
        val newReq = if (token != null) {
            req.newBuilder().header("Authorization", "Bearer $token").build()
        } else req
        return chain.proceed(newReq)
    }
}

// ── Token Refresh Authenticator ───────────────────────────────────────────────

class TokenRefreshAuthenticator(
    private val sessionStore: SessionStore,
    private val plainClientProvider: () -> OkHttpClient
) : okhttp3.Authenticator {

    override fun authenticate(route: okhttp3.Route?, response: Response): Request? {
        if (response.request.url.encodedPath.contains("auth/refresh")) return null
        if (response.code != 401) return null

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
    fun provideAuthApi(retrofit: Retrofit): AuthApi =
        retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideCourseApi(retrofit: Retrofit): com.adaptive_tutor_mobile.data.remote.api.CourseApi =
        retrofit.create(com.adaptive_tutor_mobile.data.remote.api.CourseApi::class.java)

    @Provides
    @Singleton
    fun provideEnrollmentApi(retrofit: Retrofit): EnrollmentApi =
        retrofit.create(EnrollmentApi::class.java)
    @Provides
    @Singleton
    fun provideProgressApi(retrofit: Retrofit): ProgressApi =
        retrofit.create(ProgressApi::class.java)

    @Provides
    @Singleton
    fun provideCourseDetailApi(retrofit: Retrofit): CourseDetailApi =
        retrofit.create(CourseDetailApi::class.java)

    @Provides
    @Singleton
    fun provideAdaptiveApi(retrofit: Retrofit): AdaptiveApi {
        return retrofit.create(AdaptiveApi::class.java)
    }

    // ------ Dev5: Lessons ------
    @Provides
    @Singleton
    fun provideLessonApi(retrofit: Retrofit): LessonApi {
        return retrofit.create(LessonApi::class.java)
    }

    @Provides
    @Singleton
    fun provideTestApi(retrofit: Retrofit): TestApi =
        retrofit.create(TestApi::class.java)
}
