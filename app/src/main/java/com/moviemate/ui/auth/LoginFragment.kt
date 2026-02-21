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
import com.moviemate.databinding.FragmentLoginBinding
import com.moviemate.ui.viewmodel.AuthViewModel
import com.moviemate.ui.viewmodel.UserViewModel

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), R.string.error_empty_fields, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            authViewModel.login(email, password)
        }

        authViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.loginButton.isEnabled = !isLoading
        }

        authViewModel.loginResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                it.onSuccess {
                    userViewModel.notifyUserLoggedIn()
                    findNavController().navigate(R.id.action_login_to_home)
                    authViewModel.clearResults()
                }
                it.onFailure { e ->
                    val msg = when {
                        e.message?.contains("badly formatted") == true ->
                            "The email address format is invalid."
                        else -> e.message ?: getString(R.string.error_login_failed)
                    }
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
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
