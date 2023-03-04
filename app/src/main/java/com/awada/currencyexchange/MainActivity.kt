package com.awada.currencyexchange

import ExchangeRates
import ExchangeService
import Transaction
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private var buyUsdTextView: TextView? = null
private var sellUsdTextView: TextView? = null
private var fab: FloatingActionButton? = null
private var transactionDialog: View? = null

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buyUsdTextView = findViewById(R.id.txtBuyUsdRate)
        sellUsdTextView = findViewById(R.id.txtSellUsdRate)
        fab = findViewById(R.id.fab)
        fab?.setOnClickListener { view -> showDialog() }

        fetchRates()
    }

    private fun fetchRates() {
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

    private fun showDialog() {
        transactionDialog = LayoutInflater.from(this)
            .inflate(R.layout.dialog_transaction, null, false)
        MaterialAlertDialogBuilder(this).setView(transactionDialog)
            .setTitle("Add Transaction")
            .setMessage("Enter transaction details")
            .setPositiveButton("Add") { dialog, _ ->
                val usdAmount = transactionDialog?.findViewById<TextInputLayout>(R.id.txtInptUsdAmount)?.editText?.text.toString().toFloat()
                val lpbAmount = transactionDialog?.findViewById<TextInputLayout>(R.id.txtInptLbpAmount)?.editText?.text.toString().toFloat()
                val radioGroup = transactionDialog?.findViewById<RadioGroup>(R.id.rdGrpTransactionType)
                var transactionType: Boolean? = null
                if (radioGroup != null)
                {
                    val checkedRadioButtonId = radioGroup.checkedRadioButtonId
                    if (checkedRadioButtonId == R.id.rdBtnBuyUsd)
                    {
                        transactionType = false
                    } else if (checkedRadioButtonId == R.id.rdBtnSellUsd)
                    {
                        transactionType = true
                    }
                }
                if (usdAmount > 0 && lpbAmount > 0 && transactionType != null)
                {
                    val transaction = Transaction()
                    transaction.usdAmount = usdAmount
                    transaction.lbpAmount = lpbAmount
                    transaction.usdToLbp = transactionType
                    addTransaction(transaction)
                    Toast.makeText(this@MainActivity,"Transaction added successfully", Toast.LENGTH_SHORT).show()
                }
                else if (usdAmount == null || lpbAmount == null || transactionType == null || usdAmount <= 0 || lpbAmount <= 0)
                {
                    Toast.makeText(this@MainActivity,"Invalid transaction details", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun addTransaction(transaction: Transaction) {

        ExchangeService.exchangeApi().addTransaction(transaction).enqueue(object :
            Callback<Any> {
            override fun onResponse(call: Call<Any>, response:
            Response<Any>) {
                Snackbar.make(fab as View, "Transaction added!",
                    Snackbar.LENGTH_LONG)
                    .show()
            }
            override fun onFailure(call: Call<Any>, t: Throwable) {
                Snackbar.make(fab as View, "Could not add transaction.",
                    Snackbar.LENGTH_LONG)
                    .show()
            }
        })
    }
}
