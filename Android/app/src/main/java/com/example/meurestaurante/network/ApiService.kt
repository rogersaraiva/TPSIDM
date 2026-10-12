package com.example.meurestaurante.network

import com.example.meurestaurante.Prato
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface ApiService {
    @GET("pratos")
    suspend fun getPratos(): List<Prato>
}

object RetrofitClient {
    // Nota: 10.0.2.2 aponta para o localhost do computador a partir do Emulador Android.
    // O Express corre por padrão na porta 3000 (ex: http://10.0.2.2:3000/api/)
    private const val BASE_URL = "http://10.0.2.2:3000/api/"

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
