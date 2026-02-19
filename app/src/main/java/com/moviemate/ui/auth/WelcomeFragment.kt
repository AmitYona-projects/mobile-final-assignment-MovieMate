package com.moviemate.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.moviemate.R
import com.moviemate.databinding.FragmentWelcomeBinding
import com.moviemate.ui.viewmodel.AuthViewModel

class WelcomeFragment : Fragment() {

    private var _binding: FragmentWelcomeBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (authViewModel.isLoggedIn) {
            findNavController().navigate(R.id.action_welcome_to_home)
            return
        }

        binding.loginButton.setOnClickListener {
            findNavController().navigate(R.id.action_welcome_to_login)
        }

        binding.registerButton.setOnClickListener {
            findNavController().navigate(R.id.action_welcome_to_register)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
