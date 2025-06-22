package com.example.anthonyambscs499inventoryproject

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SmsManager
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class InventoryAdapter(
    private val context: Context,
    var items: MutableList<InventoryItem>,
    private val apiService: InventoryApiService
) : RecyclerView.Adapter<InventoryAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.recycler_view_row, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = items[position]

        holder.editTextItemName.setText(item.itemName)
        holder.editTextItemQty.setText(item.itemQty.toString())
        holder.notificationSwitch.isChecked = item.itemNotifs
        holder.buttonConfirmDelete.visibility = View.GONE
        holder.textViewConfirmDelete.visibility = View.GONE
        holder.buttonCancelDelete.visibility = View.GONE

        holder.editTextItemName.removeTextChangedListener(holder.nameWatcher)
        holder.editTextItemQty.removeTextChangedListener(holder.qtyWatcher)

        // holder for item name
        holder.nameWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val newName = s.toString()
                val currentItem = items[holder.adapterPosition]
                if (currentItem.itemName != newName) {
                    val updatedItem = currentItem.copy(itemName = newName)
                    apiService.updateItem(currentItem.itemId!!, updatedItem).enqueue(object : retrofit2.Callback<InventoryItem> {
                        override fun onResponse(call: retrofit2.Call<InventoryItem>, response: retrofit2.Response<InventoryItem>) {
                            if (response.isSuccessful) {
                                items[holder.adapterPosition] = updatedItem
                            } else {
                                Toast.makeText(context, "Failed to update name", Toast.LENGTH_SHORT).show()
                            }
                        }
                        override fun onFailure(call: retrofit2.Call<InventoryItem>, t: Throwable) {
                            Toast.makeText(context, "Update error: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        holder.editTextItemName.addTextChangedListener(holder.nameWatcher)

        // holder for item quantity
        holder.qtyWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val qtyStr = s.toString()
                val currentItem = items[holder.adapterPosition]
                val newQty = qtyStr.toIntOrNull() ?: return
                if (currentItem.itemQty != newQty) {
                    if (newQty == 0 && currentItem.itemNotifs && ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                        val message = "${currentItem.itemName} is out of stock."
                        sendSMS(message)
                    }
                    val updatedItem = currentItem.copy(itemQty = newQty)
                    apiService.updateItem(currentItem.itemId!!, updatedItem).enqueue(object : retrofit2.Callback<InventoryItem> {
                        override fun onResponse(call: retrofit2.Call<InventoryItem>, response: retrofit2.Response<InventoryItem>) {
                            if (response.isSuccessful) {
                                items[holder.adapterPosition] = updatedItem
                            } else {
                                Toast.makeText(context, "Failed to update quantity", Toast.LENGTH_SHORT).show()
                            }
                        }
                        override fun onFailure(call: retrofit2.Call<InventoryItem>, t: Throwable) {
                            Toast.makeText(context, "Update error: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        holder.editTextItemQty.addTextChangedListener(holder.qtyWatcher)

        // Delete button click
        holder.buttonDelete.setOnClickListener {
            holder.buttonConfirmDelete.visibility = View.VISIBLE
            holder.textViewConfirmDelete.visibility = View.VISIBLE
            holder.buttonCancelDelete.visibility = View.VISIBLE
        }

        // Cancel delete
        holder.buttonCancelDelete.setOnClickListener {
            holder.buttonConfirmDelete.visibility = View.GONE
            holder.textViewConfirmDelete.visibility = View.GONE
            holder.buttonCancelDelete.visibility = View.GONE
        }

        // Confirm delete
        holder.buttonConfirmDelete.setOnClickListener {
            val currentItem = items[holder.adapterPosition]
            apiService.deleteItem(currentItem.itemId!!).enqueue(object : retrofit2.Callback<Void> {
                override fun onResponse(call: retrofit2.Call<Void>, response: retrofit2.Response<Void>) {
                    if (response.isSuccessful) {
                        val pos = holder.adapterPosition
                        items.removeAt(pos)
                        notifyItemRemoved(pos)
                    } else {
                        Toast.makeText(context, "Failed to delete item", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: retrofit2.Call<Void>, t: Throwable) {
                    Toast.makeText(context, "Delete error: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        // Switch toggle for notifications
        holder.notificationSwitch.setOnCheckedChangeListener(null) // Remove old listener to prevent unwanted triggers
        holder.notificationSwitch.isChecked = item.itemNotifs
        holder.notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            val currentItem = items[holder.adapterPosition]
            if (currentItem.itemNotifs != isChecked) {
                val updatedItem = currentItem.copy(itemNotifs = isChecked)
                apiService.updateItem(currentItem.itemId!!, updatedItem).enqueue(object : retrofit2.Callback<InventoryItem> {
                    override fun onResponse(call: retrofit2.Call<InventoryItem>, response: retrofit2.Response<InventoryItem>) {
                        if (response.isSuccessful) {
                            items[holder.adapterPosition] = updatedItem
                        } else {
                            Toast.makeText(context, "Failed to update notification setting", Toast.LENGTH_SHORT).show()
                            // Revert switch state on failure
                            holder.notificationSwitch.isChecked = !isChecked
                        }
                    }
                    override fun onFailure(call: retrofit2.Call<InventoryItem>, t: Throwable) {
                        Toast.makeText(context, "Update error: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                        holder.notificationSwitch.isChecked = !isChecked
                    }
                })
            }
        }
    }

    // Helper to send SMS
    private fun sendSMS(message: String) {
        val smsManager = context.getSystemService(SmsManager::class.java)
        val phoneNumber = "+1234567890" // Change to real number
        smsManager.sendTextMessage(phoneNumber, null, message, null, null)
    }

    // ViewHolder class with TextWatchers references to manage removing them
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val editTextItemName: EditText = itemView.findViewById(R.id.editTextItemName)
        val editTextItemQty: EditText = itemView.findViewById(R.id.editTextItemQty)
        val notificationSwitch: Switch = itemView.findViewById(R.id.notificationSwitch)
        val buttonDelete: ImageButton = itemView.findViewById(R.id.buttonDelete)
        val buttonConfirmDelete: ImageButton = itemView.findViewById(R.id.buttonConfirmDelete)
        val buttonCancelDelete: Button = itemView.findViewById(R.id.buttonCancelDelete)
        val textViewConfirmDelete: TextView = itemView.findViewById(R.id.textViewConfirmDelete)

        var nameWatcher: TextWatcher? = null
        var qtyWatcher: TextWatcher? = null
    }
}
