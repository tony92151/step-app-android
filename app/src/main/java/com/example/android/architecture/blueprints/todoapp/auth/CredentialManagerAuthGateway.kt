package com.example.android.architecture.blueprints.todoapp.auth

import android.app.Activity
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.example.android.architecture.blueprints.todoapp.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import javax.inject.Inject

class CredentialManagerAuthGateway @Inject constructor() : AuthGateway {

    override suspend fun signIn(activity: Activity): Result<SignedInUser> {
        val serverClientId = activity.getString(R.string.default_web_client_id)
        if (serverClientId.isBlank() || serverClientId == "REPLACE_WITH_WEB_CLIENT_ID") {
            return Result.failure(
                IllegalStateException("Missing OAuth client id. Please set default_web_client_id.")
            )
        }

        return runCatching {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(serverClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val credentialManager = CredentialManager.create(activity)
            val result = credentialManager.getCredential(
                context = activity,
                request = request,
            )

            val credential = result.credential
            if (
                credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                SignedInUser(
                    id = googleIdTokenCredential.id,
                    displayName = googleIdTokenCredential.displayName,
                    email = googleIdTokenCredential.id,
                    profilePictureUri = googleIdTokenCredential.profilePictureUri?.toString(),
                )
            } else {
                throw IllegalStateException("Unsupported credential type")
            }
        }.recoverCatching { throwable ->
            if (throwable is GoogleIdTokenParsingException) {
                throw IllegalStateException("Failed to parse Google ID token", throwable)
            }
            throw throwable
        }
    }

    override suspend fun signOut(activity: Activity): Result<Unit> {
        return runCatching {
            CredentialManager.create(activity)
                .clearCredentialState(ClearCredentialStateRequest())
        }
    }
}
