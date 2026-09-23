package com.example.geonote.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()

    val currentUser = auth.currentUser

    suspend fun signInWithEmail(email: String, password: String) = auth.signInWithEmailAndPassword(email, password).await()
    suspend fun signUpWithEmail(email: String, password: String) = auth.createUserWithEmailAndPassword(email, password).await()

    suspend fun signInWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential).await()
    }

    fun signOut() = auth.signOut()
}