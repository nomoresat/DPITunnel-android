package com.nomoresat.dpitunnelcli.ui.profiles

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.checkbox.MaterialCheckBox
import com.nomoresat.dpitunnelcli.R
import com.nomoresat.dpitunnelcli.data.usecases.FetchProfileUseCase
import com.nomoresat.dpitunnelcli.domain.entities.Profile

class ProfilesAdapter(val profileListener: (Profile) -> Unit,
                      val profileRenameListener: (Profile) -> Unit,
                      val profileDeleteListener: (Profile) -> Unit,
                      val profileDefaultListener: (Profile) -> Unit,
                      val profileEnabledListener: (Profile) -> Unit,): RecyclerView.Adapter<ProfileViewHolder>() {
    private var profiles = listOf<Profile>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        return ProfileViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.view_holder_profile_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        holder.onBind(profiles[position])
        holder.itemView.setOnClickListener {
            profileListener(profiles[position])
        }
        holder.options.setOnClickListener { view ->
            val popupMenu = PopupMenu(view.context, view)
            popupMenu.inflate(R.menu.profile_item_menu)
            popupMenu.setOnMenuItemClickListener {
                when(it.itemId) {
                    R.id.profile_item_menu_rename -> {
                        profileRenameListener(profiles[position])
                        true
                    }
                    R.id.profile_item_menu_delete -> {
                        profileDeleteListener(profiles[position])
                        true
                    }
                    R.id.profile_item_menu_default -> {
                        profileDefaultListener(profiles[position])
                        true
                    }
                    R.id.profile_item_menu_enabled -> {
                        profileEnabledListener(profiles[position])
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }
    }

    override fun getItemCount(): Int = profiles.size

    fun bindProfiles(newProfiles: List<Profile>?) {
        newProfiles?.let {
            profiles = it
            notifyDataSetChanged()
        }
    }
}

class ProfileViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    val card: MaterialCardView = itemView.findViewById(R.id.view_holder_profile_card)
    val title: TextView = itemView.findViewById(R.id.view_holder_profile_title)
    val options: ImageButton = itemView.findViewById(R.id.view_holder_profile_options)

    fun onBind(profile: Profile) {
        card.isChecked = profile.default || profile.enabled

        val titleStr = StringBuilder(profile.title ?: itemView.context.getString(R.string.unnamed_profile_name))
        if (profile.default)
            titleStr.append(" ").append(itemView.context.getString(R.string.default_profile_mark))
        title.text = titleStr.toString()
    }
}