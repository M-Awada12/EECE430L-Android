import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
object ExchangeService {
    private const val API_URL: String = "http://10.0.2.2:5000"
    fun exchangeApi():Exchange {
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        // Create an instance of our Exchange API interface.
        return retrofit.create(Exchange::class.java);
    }
    interface Exchange {
        @GET("/exchangeRate")
        fun getExchangeRates(): Call<ExchangeRates>
    }
}
