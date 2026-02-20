package com.moviemate.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.moviemate.R
import com.moviemate.databinding.FragmentRegisterBinding
import com.moviemate.ui.viewmodel.AuthViewModel

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.registerButton.setOnClickListener {
            val username = binding.usernameEditText.text.toString().trim()
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), R.string.error_empty_fields, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            authViewModel.register(email, password, username)
        }

        authViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.registerButton.isEnabled = !isLoading
        }

        authViewModel.registerResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                it.onSuccess {
                    Toast.makeText(requireContext(),
                        "Registration successful! Welcome to MovieMate 🎬",
                        Toast.LENGTH_LONG).show()
                    authViewModel.clearResults()
                    findNavController().navigate(R.id.action_register_to_home)
                }
                it.onFailure { e ->
                    val msg = when {
                        e.message?.contains("email address is already in use") == true ->
                            "This email is already registered. Please login instead."
                        e.message?.contains("password is invalid") == true ->
                            "Password must be at least 6 characters."
                        e.message?.contains("network") == true ->
                            "No internet connection. Please check your network."
                        else -> e.message ?: getString(R.string.error_register_failed)
                    }
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
                    authViewModel.clearResults()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
