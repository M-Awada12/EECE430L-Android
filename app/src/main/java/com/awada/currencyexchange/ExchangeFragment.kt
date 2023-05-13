package com.awada.currencyexchange

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.fragment.app.Fragment
import com.awada.currencyexchange.api.ExchangeService
import com.awada.currencyexchange.api.model.ExchangeRates
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ExchangeFragment : Fragment() {

    private lateinit var resultTextView: TextView
    private lateinit var buyUsdTextView: TextView
    private lateinit var sellUsdTextView: TextView
    private lateinit var amountEditText: EditText
    private lateinit var spinner: Spinner
    private lateinit var calculateButton: Button
    private var buyRate: Float? = null
    private var sellRate: Float? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fetchRates()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_exchange, container, false)

        resultTextView = view.findViewById(R.id.result)
        buyUsdTextView = view.findViewById(R.id.txtBuyUsdRate)
        sellUsdTextView = view.findViewById(R.id.txtSellUsdRate)
        amountEditText = view.findViewById(R.id.amount)
        spinner = view.findViewById(R.id.spinner)
        calculateButton = view.findViewById(R.id.button)

        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.options,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        calculateButton.setOnClickListener {
            calculateExchange()
        }

        return view
    }

    private fun fetchRates() {
        ExchangeService.exchangeApi().getExchangeRates().enqueue(object : Callback<ExchangeRates> {
            override fun onResponse(call: Call<ExchangeRates>, response: Response<ExchangeRates>) {
                val responseBody: ExchangeRates? = response.body()
                responseBody?.let {
                    val buyRate = String.format("%.2f", it.lbpToUsd)
                    val sellRate = String.format("%.2f", it.usdToLbp)
                    buyUsdTextView.text = "$buyRate LBP"
                    sellUsdTextView.text = "$sellRate LBP"
                    this@ExchangeFragment.buyRate = it.lbpToUsd
                    this@ExchangeFragment.sellRate = it.usdToLbp
                }
            }

            override fun onFailure(call: Call<ExchangeRates>, t: Throwable) {
                // Handle failure
            }
        })
    }

    private fun calculateExchange() {
        val inputMethodManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view?.windowToken, 0)

        val amountInput = amountEditText.text.toString().toFloatOrNull()

        if (sellRate != null && buyRate != null && sellRate != 0f && buyRate != 0f && amountInput != null) {
            val selectedExchange = when (spinner.selectedItem.toString()) {
                "USD to LBP" -> amountInput * sellRate!!
                "LBP to USD" -> amountInput / buyRate!!
                else -> 0f
            }

            val resultText = if (spinner.selectedItem.toString() == "USD to LBP") {
                String.format("%.2f", selectedExchange) + " LBP"
            } else {
                String.format("%.2f", selectedExchange) + " USD"

            }

            resultTextView.text = resultText
        } else {
            resultTextView.text = "Invalid input"
        }
    }
}