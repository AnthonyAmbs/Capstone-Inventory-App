package com.example.anthonyambscs499inventoryproject

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity() {
    private var loggedIn: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent
        val apiService = RetrofitService.instance

        loggedIn = intent.getBooleanExtra("loggedIn", false)

        // if not logged in, go to login activity
        if (!loggedIn) {
            val login = Intent(this, Login::class.java)
            startActivity(login)
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        // Ask for SMS permission
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), 1)
        }

        val buttonAddItem = findViewById<Button>(R.id.addButton)
        val inventoryRecyclerView = findViewById<RecyclerView>(R.id.inventoryRecyclerView)
        var adapter = InventoryAdapter(this, mutableListOf(), apiService)
        inventoryRecyclerView.layoutManager = LinearLayoutManager(this)
        inventoryRecyclerView.adapter = adapter
        inventoryRecyclerView.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )

        // Divider for RecyclerView
        val divider = DividerItemDecoration(
            inventoryRecyclerView.context,
            DividerItemDecoration.VERTICAL
        )
        inventoryRecyclerView.addItemDecoration(divider)

        // Function for fetching inventory items
        fun fetchInventoryItems() {
            apiService.getAllInventory().enqueue(object : Callback<List<InventoryItem>> {
                override fun onResponse(
                    call: Call<List<InventoryItem>>,
                    response: Response<List<InventoryItem>>
                ) {
                    if (response.isSuccessful) {
                        val inventoryList = response.body()?.toMutableList() ?: mutableListOf()
                        adapter.items = inventoryList
                        adapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(this@MainActivity, "Failed to load items", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<List<InventoryItem>>, t: Throwable) {
                    Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        fetchInventoryItems()

        // Add item
        buttonAddItem.setOnClickListener {
            val newItem = InventoryItem(itemId = null, itemName = "Item", itemQty = 0)
            apiService.addItem(newItem).enqueue(object : Callback<InventoryItem> {
                override fun onResponse(call: Call<InventoryItem>, response: Response<InventoryItem>) {
                    if (response.isSuccessful) {
                        val addedItem = response.body()
                        addedItem?.let {
                            adapter.items.add(it)
                            adapter.notifyItemInserted(adapter.items.size - 1)
                        }
                    } else {
                        Toast.makeText(this@MainActivity, "Failed to add item", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<InventoryItem>, t: Throwable) {
                    Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}