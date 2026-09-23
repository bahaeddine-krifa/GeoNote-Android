package com.example.geonote.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.geonote.data.repository.AuthRepository
import com.example.geonote.databinding.ActivitySignupBinding
import com.example.geonote.ui.main.MainActivity
import kotlinx.coroutines.launch

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private val viewModel: AuthViewModel by viewModels {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
                AuthViewModel(AuthRepository()) as T
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        observeState()
    }

    private fun setupUI() {
        // Bouton Inscription
        binding.btnSignup.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()

            // 1. Validation champs vides
            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 2. Validation correspondance mots de passe
            if (password != confirmPassword) {
                binding.tilConfirmPassword.error = "Les mots de passe ne correspondent pas"
                return@setOnClickListener
            }
            binding.tilConfirmPassword.error = null

            // 3. Validation longueur minimale (Firebase exige ≥ 6)
            if (password.length < 6) {
                binding.tilPassword.error = "Minimum 6 caractères requis"
                return@setOnClickListener
            }
            binding.tilPassword.error = null

            // Lancement de l'inscription
            viewModel.signup(email, password)
        }

        // Lien vers Login
        binding.tvGoToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is AuthState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.cardAuth.alpha = 0.5f
                    }
                    is AuthState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.cardAuth.alpha = 1f
                        Toast.makeText(this@SignupActivity, "✅ Compte créé avec succès !", Toast.LENGTH_SHORT).show()
                        // Redirection directe vers l'accueil après inscription
                        startActivity(Intent(this@SignupActivity, MainActivity::class.java))
                        finishAffinity()
                    }
                    is AuthState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.cardAuth.alpha = 1f
                        Toast.makeText(this@SignupActivity, state.message, Toast.LENGTH_LONG).show()
                    }
                    is AuthState.Idle -> {}
                }
            }
        }
    }
}