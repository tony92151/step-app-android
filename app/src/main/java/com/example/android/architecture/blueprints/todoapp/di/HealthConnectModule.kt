package com.example.android.architecture.blueprints.todoapp.di

import com.example.android.architecture.blueprints.todoapp.healthconnect.AndroidHealthConnectGateway
import com.example.android.architecture.blueprints.todoapp.healthconnect.HealthConnectGateway
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class HealthConnectModule {

    @Singleton
    @Binds
    abstract fun bindHealthConnectGateway(gateway: AndroidHealthConnectGateway): HealthConnectGateway
}
