package com.awada.currencyexchange

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RadioGroup
import android.widget.Toast
import androidx.viewpager2.widget.ViewPager2
import com.awada.currencyexchange.api.Authentication
import com.awada.currencyexchange.api.ExchangeService
import com.awada.currencyexchange.api.model.Transaction
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var fab: FloatingActionButton
    private lateinit var transactionDialog: View
    private lateinit var menu: Menu
    private lateinit var tabLayout: TabLayout
    private lateinit var tabsViewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Authentication.initialize(this)
        setContentView(R.layout.activity_main)

        fab = findViewById(R.id.fab)
        fab.setOnClickListener {
            showDialog()
        }

        tabLayout = findViewById(R.id.tabLayout)
        tabsViewPager = findViewById(R.id.tabsViewPager)
        tabLayout.tabMode = TabLayout.MODE_FIXED
        tabLayout.isInlineLabel = true
        tabsViewPager.isUserInputEnabled = true

        val adapter = TabsPagerAdapter(supportFragmentManager, lifecycle)
        tabsViewPager.adapter = adapter

        TabLayoutMediator(tabLayout, tabsViewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Exchange"
                1 -> "Transactions"
                else -> ""
            }
        }.attach()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(
            if (Authentication.getToken() == null)
                R.menu.menu_logged_out else R.menu.menu_logged_in, menu
        )
        this.menu = menu ?: return false
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.login -> {
                startActivity(Intent(this, LoginActivity::class.java))
                true
            }
            R.id.register -> {
                startActivity(Intent(this, RegistrationActivity::class.java))
                true
            }
            R.id.logout -> {
                Authentication.clearToken()
                invalidateOptionsMenu()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun addTransaction(transaction: Transaction) {
        ExchangeService.exchangeApi().addTransaction(
            transaction,
            Authentication.getToken()?.let { "Bearer $it" }
        ).enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                if (response.isSuccessful) {
                    Snackbar.make(
                        fab, "Transaction added!", Snackbar.LENGTH_LONG
                    ).show()
                } else {
                    Snackbar.make(
                        fab, "Could not add transaction.", Snackbar.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                Snackbar.make(
                    fab, "Could not add transaction.", Snackbar.LENGTH_LONG
                ).show()
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
    }
