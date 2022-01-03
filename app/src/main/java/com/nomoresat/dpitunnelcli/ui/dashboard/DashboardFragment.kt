package com.nomoresat.dpitunnelcli.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.nomoresat.dpitunnelcli.R
import com.nomoresat.dpitunnelcli.databinding.FragmentDashboardBinding
import com.nomoresat.dpitunnelcli.utils.Constants
import android.content.Intent
import android.widget.Toast
import com.nomoresat.dpitunnelcli.data.usecases.*
import com.nomoresat.dpitunnelcli.ui.activity.settings.SettingsActivity


class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val dashboardViewModel by viewModels<DashboardViewModel> {
        DashboardViewModelFactory(
            daemonUseCase = DaemonUseCase(
                execPath = requireContext().applicationInfo.nativeLibraryDir + '/' + Constants.DPITUNNEL_BINARY_NAME,
                pidFilePath = Constants.DPITUNNEL_DAEMON_PID_FILE),
            fetchAllProfilesUseCase = FetchAllProfilesUseCase(requireContext().applicationContext),
            settingsUseCase = SettingsUseCase(requireContext().applicationContext),
            proxyUseCase = ProxyUseCase(),
            loadProxifiedAppsUseCase = LoadProxifiedAppsUseCase(requireContext().applicationContext)
        )
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageViewStatus = binding.dashboardStatusCardImage

        val progressBarLoading = binding.dashboardStatusCardProgress

        val buttonSettings = binding.dashboardStatusCardSettings
        buttonSettings.setOnClickListener {
            val intent = Intent(requireContext(), SettingsActivity::class.java)
            requireContext().startActivity(intent)
        }

        val buttonStartStop = binding.dashboardStatusCardStartStop
        buttonStartStop.setOnClickListener {
            dashboardViewModel.startStop()
        }

        val buttonRestart = binding.dashboardStatusCardRestart
        buttonRestart.setOnClickListener{
            dashboardViewModel.restart()
        }

        dashboardViewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is DashboardViewModel.UIState.Loading -> {
                    buttonSettings.isEnabled = true
                    progressBarLoading.visibility = View.VISIBLE
                    imageViewStatus.visibility = View.INVISIBLE
                    buttonStartStop.isClickable = false
                    buttonRestart.isClickable = false
                }
                is DashboardViewModel.UIState.Running -> {
                    buttonSettings.isEnabled = false
                    progressBarLoading.visibility = View.GONE
                    imageViewStatus.visibility = View.VISIBLE
                    imageViewStatus.setImageDrawable(AppCompatResources.getDrawable(requireContext().applicationContext, R.drawable.ic_check_circle_white_96dp))
                    imageViewStatus.setBackgroundColor(ContextCompat.getColor(requireContext().applicationContext, R.color.green_300))
                    buttonStartStop.text = getText(R.string.button_status_stop_text)
                    buttonStartStop.setIconResource(R.drawable.ic_stop_24)
                    buttonStartStop.isClickable = true
                    buttonRestart.isClickable = true
                }
                is DashboardViewModel.UIState.Stopped -> {
                    buttonSettings.isEnabled = true
                    progressBarLoading.visibility = View.GONE
                    imageViewStatus.visibility = View.VISIBLE
                    imageViewStatus.setImageDrawable(AppCompatResources.getDrawable(requireContext().applicationContext, R.drawable.ic_cancel_white_96dp))
                    imageViewStatus.setBackgroundColor(ContextCompat.getColor(requireContext().applicationContext, R.color.red_300))
                    buttonStartStop.text = getText(R.string.button_status_start_text)
                    buttonStartStop.setIconResource(R.drawable.ic_play_24dp)
                    buttonStartStop.isClickable = true
                    buttonRestart.isClickable = true
                }
                is DashboardViewModel.UIState.Error -> {
                    when(state.code) {
                        DashboardViewModel.UIError.NO_ONE_PROFILE -> {
                            Toast.makeText(requireContext(), R.string.no_one_profile_failed, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}