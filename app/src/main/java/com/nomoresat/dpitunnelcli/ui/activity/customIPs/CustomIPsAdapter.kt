package com.nomoresat.dpitunnelcli.ui.activity.customIPs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.nomoresat.dpitunnelcli.R
import com.nomoresat.dpitunnelcli.domain.entities.CustomIPEntry

class CustomIPsAdapter(val entryEditListener: (Int, CustomIPEntry) -> Unit,
                       val entryDeleteListener: (Int, CustomIPEntry) -> Unit): RecyclerView.Adapter<CustomIPsViewHolder>() {

    private var entries = listOf<CustomIPEntry>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomIPsViewHolder {
        return CustomIPsViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.view_holder_custom_ip_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: CustomIPsViewHolder, position: Int) {
        holder.onBind(position, entries[position], entryEditListener, entryDeleteListener)
    }

    override fun getItemCount(): Int = entries.size

    fun bindEntries(newEntries: List<CustomIPEntry>?) {
        newEntries?.let {
            entries = it
            notifyDataSetChanged()
        }
    }
}

class CustomIPsViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    val domain: TextView = itemView.findViewById(R.id.view_holder_custom_ip_domain)
    val ip: TextView = itemView.findViewById(R.id.view_holder_custom_ip_ip)
    val edit: MaterialButton = itemView.findViewById(R.id.view_holder_custom_ip_edit)
    val delete: MaterialButton = itemView.findViewById(R.id.view_holder_custom_ip_delete)

    fun onBind(entryPosition: Int,
               entry: CustomIPEntry,
               entryEditListener: (Int, CustomIPEntry) -> Unit,
               entryDeleteListener: (Int, CustomIPEntry) -> Unit) {
        domain.text = entry.domain

        ip.text = entry.ip

        edit.setOnClickListener {
            entryEditListener(entryPosition, entry)
        }

        delete.setOnClickListener{
            entryDeleteListener(entryPosition, entry)
        }
    }
}