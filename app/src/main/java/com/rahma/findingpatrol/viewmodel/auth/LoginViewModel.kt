package com.rahma.findingpatrol.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rahma.findingpatrol.data.repository.AuthRepository
import com.rahma.findingpatrol.data.model.auth.LoginResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

class LoginViewModel(private val repo: AuthRepository) : ViewModel() {

    fun login(username: String, password: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            // Switch to IO dispatcher for network/disk operations
            val result = withContext(Dispatchers.IO) {
                try {
                    val response: Response<LoginResponse> = repo.login(username, password)
                    if (response.isSuccessful && response.body() != null) {
                        // After login, fetch and save user details
                        val userFetched = repo.fetchAndSaveUser()
                        if (userFetched) {
                            Pair(true, "Login berhasil")
                        } else {
                            Pair(true, "Login berhasil (Gagal memuat profil)")
                        }
                    } else {
                        Pair(false, response.errorBody()?.string() ?: "Login gagal")
                    }
                } catch (e: Exception) {
                    Pair(false, e.message ?: "Terjadi kesalahan")
                }
            }
            
            // Back on Main thread to update UI
            onResult(result.first, result.second)
        }
    }
}
