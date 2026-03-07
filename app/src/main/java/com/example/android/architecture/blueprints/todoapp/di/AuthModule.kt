package com.example.android.architecture.blueprints.todoapp.di

import com.example.android.architecture.blueprints.todoapp.auth.AuthGateway
import com.example.android.architecture.blueprints.todoapp.auth.CredentialManagerAuthGateway
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Singleton
    @Binds
    abstract fun bindAuthGateway(gateway: CredentialManagerAuthGateway): AuthGateway
}
