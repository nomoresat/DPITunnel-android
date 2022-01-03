package com.nomoresat.dpitunnelcli.ui.activity.proxifiedApps

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox
import com.nomoresat.dpitunnelcli.R
import com.nomoresat.dpitunnelcli.domain.entities.ProxifiedApp

class ProxifiedAppsAdapter(val appIsProxifiedListener: (String, Boolean) -> Unit): RecyclerView.Adapter<ProxifiedAppsViewHolder>() {

    private var apps = listOf<ProxifiedApp>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProxifiedAppsViewHolder {
        return ProxifiedAppsViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.view_holder_proxified_apps_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ProxifiedAppsViewHolder, position: Int) {
        holder.onBind(apps[position], appIsProxifiedListener)
    }

    override fun getItemCount(): Int = apps.size

    fun bindEntries(newApps: List<ProxifiedApp>?) {
        newApps?.let {
            apps = it
            notifyDataSetChanged()
        }
    }
}

class ProxifiedAppsViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    val icon: ImageView = itemView.findViewById(R.id.view_holder_proxified_apps_icon)
    val name: TextView = itemView.findViewById(R.id.view_holder_proxified_apps_name)
    val isProxified: MaterialCheckBox = itemView.findViewById(R.id.view_holder_proxified_apps_proxified_checkbox)

    fun onBind(app: ProxifiedApp,
               appIsProxifiedListener: (String, Boolean) -> Unit) {
        icon.setImageDrawable(app.icon)

        name.text = app.name

        isProxified.setOnCheckedChangeListener(null)
        isProxified.isChecked = app.isProxified
        isProxified.setOnCheckedChangeListener { _, isChecked ->
            appIsProxifiedListener(app.username, isChecked)
        }
    }
}