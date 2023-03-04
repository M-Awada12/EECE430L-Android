package com.awada.currencyexchange

import ExchangeRates
import ExchangeService
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private var buyUsdTextView: TextView? = null
private var sellUsdTextView: TextView? = null

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buyUsdTextView = findViewById(R.id.txtBuyUsdRate)
        sellUsdTextView = findViewById(R.id.txtSellUsdRate)

        ExchangeService.exchangeApi().getExchangeRates().enqueue(object :
            Callback<ExchangeRates> {
            override fun onResponse(call: Call<ExchangeRates>, response:
            Response<ExchangeRates>) {
                val responseBody: ExchangeRates? = response.body()
                responseBody?.let {
                    val buyUsdRate = it.usdToLbp
                    val sellUsdRate = it.lbpToUsd
                    buyUsdRate?.let { rate -> buyUsdTextView?.text = getString(R.string.usd_buy_rate, rate)
                    }
                    sellUsdRate?.let { rate -> sellUsdTextView?.text = getString(R.string.usd_sell_rate, rate)
                    }
                }
            }
            override fun onFailure(call: Call<ExchangeRates>, t: Throwable) {
                // Handle failure here
            }
        })

    }
}
