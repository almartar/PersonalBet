package com.example.personalbet.ui.welcome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.personalbet.MainActivity
import com.example.personalbet.config.AppConfigStore
import com.example.personalbet.databinding.FragmentWelcomeBinding
import com.example.personalbet.ui.help.TutorialDialog

class WelcomeFragment : Fragment() {

    private var _binding: FragmentWelcomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonStart.setOnClickListener {
            (requireActivity() as MainActivity).openHomeFromWelcome()
        }
        binding.buttonTutorial.setOnClickListener {
            showTutorial(markSeen = true)
        }
        if (!AppConfigStore.hasSeenTutorial(requireContext())) {
            showTutorial(markSeen = true)
        }
    }

    private fun showTutorial(markSeen: Boolean) {
        TutorialDialog.show(requireContext())
        if (markSeen) {
            AppConfigStore.markTutorialSeen(requireContext())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
