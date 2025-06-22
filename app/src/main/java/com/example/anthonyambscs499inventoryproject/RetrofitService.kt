package com.example.anthonyambscs499inventoryproject

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.http.*

interface InventoryApiService {
    // Inventory Endpoints
    @GET("api/inventory")
    fun getAllInventory(): Call<List<InventoryItem>>

    @POST("api/inventory")
    fun addItem(@Body item: InventoryItem): Call<InventoryItem>

    @PUT("api/inventory/{itemId}")
    fun updateItem(@Path("itemId") itemId: String, @Body item: InventoryItem): Call<InventoryItem>

    @DELETE("api/inventory/{itemId}")
    fun deleteItem(@Path("itemId") itemId: String): Call<Void>

    // User Endpoints
    @POST("api/users/register")
    fun addNewUser(@Body user: User): Call<Boolean>

    @POST("api/users/login")
    fun validateUser(@Body user: User): Call<Boolean>

    @GET("api/users/check-username")
    fun isUsernameUnique(@Query("username") username: String): Call<Boolean>
}


object RetrofitService {
    private const val BASE_URL = "https://aja-ngrok-app.ngrok.io/"

    val instance: InventoryApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(InventoryApiService::class.java)
    }
}