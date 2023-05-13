package com.awada.currencyexchange

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import com.awada.currencyexchange.api.Authentication
import com.awada.currencyexchange.api.ExchangeService
import com.awada.currencyexchange.api.model.Transaction
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionsFragment : Fragment() {
    private lateinit var listView: ListView
    private val transactions: ArrayList<Transaction> = ArrayList()
    private lateinit var adapter: TransactionAdapter

    private inner class TransactionAdapter(
        private val dataSource: List<Transaction>
    ) : BaseAdapter() {

        override fun getCount(): Int = dataSource.size

        override fun getItem(position: Int): Any = dataSource[position]

        override fun getItemId(position: Int): Long = dataSource[position].id?.toLong() ?: 0

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view: View = convertView ?: LayoutInflater.from(parent.context).inflate(
                R.layout.item_transaction,
                parent, false
            )

            val transactionLine = view.findViewById<TextView>(R.id.transaction_line)
            val dateAdded = view.findViewById<TextView>(R.id.date_added)
            val transactionType = view.findViewById<TextView>(R.id.transaction_type)

            val thisTransaction = dataSource[position]

            val usdAmount = String.format("%.2f", thisTransaction.usdAmount)
            val lbpAmount = String.format("%.2f", thisTransaction.lbpAmount)

            transactionLine.text = if (thisTransaction.usdToLbp == true) {
                "Transaction: $usdAmount USD to $lbpAmount LBP"
            } else {
                "Transaction: $lbpAmount LBP to $usdAmount USD"
            }

            transactionType.text = if (thisTransaction.usdToLbp == true) {
                "Type: USD to LBP"
            } else {
                "Type: LBP to USD"
            }

            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val date = dateFormat.parse(thisTransaction.dateAdded)
            val displayDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            dateAdded.text = "Date: ${displayDateFormat.format(date)}"

            return view
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fetchTransactions()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_transactions, container, false)
        listView = view.findViewById(R.id.listview)
        adapter = TransactionAdapter(transactions)
        listView.adapter = adapter
        return view
    }

    private fun fetchTransactions() {
        val token = Authentication.getToken()
        if (token != null) {
            ExchangeService.exchangeApi()
                .getTransactions("Bearer $token")
                .enqueue(object : Callback<List<Transaction>> {
                    override fun onFailure(call: Call<List<Transaction>>, t: Throwable) {
                        // Handle failure
                    }

                    override fun onResponse(
                        call: Call<List<Transaction>>,
                        response: Response<List<Transaction>>
                    ) {
                        val transactionList = response.body()
                        if (transactionList != null) {
                            transactions.addAll(transactionList)
                            adapter.notifyDataSetChanged()
                        }
                    }
                })
        }
    }
}
