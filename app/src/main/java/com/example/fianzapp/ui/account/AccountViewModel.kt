package com.example.fianzapp.ui.account

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.fianzapp.data.remote.isFirebaseConfigured
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** A6: sign-in with email and password, and sign-out, so one phone can switch accounts. */
class AccountViewModel(application: Application) : AndroidViewModel(application) {

    val configured: Boolean = isFirebaseConfigured(application)
    private val auth: FirebaseAuth? = if (configured) FirebaseAuth.getInstance() else null

    private val _userEmail = MutableStateFlow(auth?.currentUser?.let { it.email ?: it.uid })
    /** The signed-in account, or null when signed out. */
    val userEmail: StateFlow<String?> = _userEmail.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    private val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        _userEmail.value = firebaseAuth.currentUser?.let { it.email ?: it.uid }
    }

    init {
        auth?.addAuthStateListener(listener)
    }

    fun signIn(email: String, password: String) = run(email, password) { a, e, p ->
        a.signInWithEmailAndPassword(e, p)
    }

    fun createAccount(email: String, password: String) = run(email, password) { a, e, p ->
        a.createUserWithEmailAndPassword(e, p)
    }

    fun signOut() {
        auth?.signOut()
    }

    private fun run(
        email: String,
        password: String,
        call: (FirebaseAuth, String, String) -> com.google.android.gms.tasks.Task<*>
    ) {
        val a = auth ?: return
        if (email.isBlank() || password.isBlank()) return
        _busy.value = true
        _error.value = null
        call(a, email.trim(), password)
            .addOnSuccessListener { _busy.value = false }
            .addOnFailureListener {
                _busy.value = false
                _error.value = it.message ?: it.javaClass.simpleName
            }
    }

    override fun onCleared() {
        auth?.removeAuthStateListener(listener)
    }
}
