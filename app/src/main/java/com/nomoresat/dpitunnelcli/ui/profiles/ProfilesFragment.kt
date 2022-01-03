package com.nomoresat.dpitunnelcli.ui.profiles

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.nomoresat.dpitunnelcli.R
import com.nomoresat.dpitunnelcli.data.usecases.*
import com.nomoresat.dpitunnelcli.ui.activity.editProfile.EditProfileActivity
import com.nomoresat.dpitunnelcli.databinding.FragmentProfilesBinding

class ProfilesFragment : Fragment() {

    private var _binding: FragmentProfilesBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val profilesViewModel by viewModels<ProfilesViewModel> {
        ProfilesViewModelFactory(
            fetchAllProfilesUseCase = FetchAllProfilesUseCase(requireContext().applicationContext),
            fetchProfileUseCase = FetchProfileUseCase(requireContext().applicationContext),
            deleteProfileUseCase = DeleteProfileUseCase(requireContext().applicationContext),
            renameProfileUseCase = RenameProfileUseCase(requireContext().applicationContext),
            settingsUseCase = SettingsUseCase(requireContext().applicationContext),
            enableDisableProfileUseCase = EnableDisableProfileUseCase(requireContext().applicationContext)
        )
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfilesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.profilesRecycler.adapter = ProfilesAdapter(
            profileListener = {
                resultLauncher.launch(Intent(context, EditProfileActivity::class.java)
                    .putExtra(EditProfileActivity.PROFILE_ID_KEY, it.id))
            },
            profileRenameListener = {
                val inputEditTextField = EditText(requireActivity())
                inputEditTextField.setText(it.title)
                inputEditTextField.maxLines = 1
                inputEditTextField.inputType = InputType.TYPE_TEXT_VARIATION_FILTER or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                val dialog = AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.dialog_profile_rename_title))
                    .setView(inputEditTextField)
                    .setPositiveButton(getString(R.string.dialog_profile_rename_positive)) { _, _ ->
                        profilesViewModel.rename(it.id!!, inputEditTextField.text.toString())
                    }
                    .setNegativeButton(getString(R.string.dialog_profile_rename_negative), null)
                    .create()
                dialog.show()
            },
            profileDeleteListener = {
                profilesViewModel.delete(it.id!!)
            },
            profileDefaultListener = {
                profilesViewModel.setDefaultProfile(it.id!!)
            },
            profileEnabledListener = {
                profilesViewModel.enableDisable(it.id!!)
            }
        )

        binding.profilesRecycler.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.VERTICAL, false
        )

        profilesViewModel.uiState.observe(viewLifecycleOwner) { state ->
            when(state) {
                is ProfilesViewModel.UIState.Normal -> {}
                is ProfilesViewModel.UIState.Error -> {
                    when(state.error) {
                        ProfilesViewModel.UIErrorType.ERROR_PROFILE_NAME_EMPTY -> {
                            Toast.makeText(requireContext(), R.string.empty_profile_name_failed, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }

        profilesViewModel.profiles.observe(viewLifecycleOwner) {
            (binding.profilesRecycler.adapter as ProfilesAdapter).bindProfiles(it)
        }

        binding.profilesAddProfileButton.setOnClickListener {
            resultLauncher.launch(Intent(context, EditProfileActivity::class.java)
                .putExtra(EditProfileActivity.PROFILE_ID_KEY, 0L))
        }
    }

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}