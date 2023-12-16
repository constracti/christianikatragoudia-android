package gr.christianikatragoudia.app.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import gr.christianikatragoudia.app.data.DateTimeConverter
import gr.christianikatragoudia.app.data.Patch
import gr.christianikatragoudia.app.music.MusicNoteJsonAdapter
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private val moshi = Moshi.Builder()
    .add(DateTimeConverter())
    .add(MusicNoteJsonAdapter())
    .addLast(KotlinJsonAdapterFactory())
    .build()

const val DOMAIN_NAME = "christianikatragoudia.gr"

const val EMAIL_ADDRESS = "info@$DOMAIN_NAME"

const val BASE_URL = "https://$DOMAIN_NAME/"

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

interface WebAppService {

    @GET("wp-admin/admin-ajax.php?action=xt_app_patch_2")
    suspend fun getPatch(@Query("after") after: Int?, @Query("full") full: Boolean): Patch
}

object WebApp {

    val retrofitService : WebAppService by lazy {
        retrofit.create(WebAppService::class.java)
    }
}
