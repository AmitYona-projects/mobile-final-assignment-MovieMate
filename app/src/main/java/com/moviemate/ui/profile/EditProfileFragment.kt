package com.moviemate.ui.profile

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.moviemate.R
import com.moviemate.databinding.FragmentEditProfileBinding
import com.moviemate.ui.viewmodel.UserViewModel
import com.moviemate.utils.CircleTransform
import com.squareup.picasso.Picasso

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!
    private val userViewModel: UserViewModel by activityViewModels()
    private var selectedImageUri: Uri? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            binding.profileImage.setImageURI(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        observeData()
    }

    private fun setupListeners() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.changePhotoText.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.profileImage.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.saveButton.setOnClickListener {
            val username = binding.usernameEditText.text.toString().trim()
            if (username.isEmpty()) {
                Toast.makeText(requireContext(), R.string.error_empty_fields, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            userViewModel.updateProfile(username, selectedImageUri)
        }
    }

    private fun observeData() {
        userViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.usernameEditText.setText(it.username)
                binding.emailEditText.setText(it.email)

                if (it.profileImageUrl.isNotEmpty() && selectedImageUri == null) {
                    Picasso.get()
                        .load(it.profileImageUrl)
                        .placeholder(R.drawable.circle_background)
                        .error(R.drawable.circle_background)
                        .transform(CircleTransform())
                        .into(binding.profileImage)
                }
            }
        }

        userViewModel.updateResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                it.onSuccess {
                    Toast.makeText(requireContext(), R.string.profile_updated, Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                it.onFailure { e ->
                    Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
                }
                userViewModel.clearResults()
            }
        }

        userViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.saveButton.isEnabled = !isLoading
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
