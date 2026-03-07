package com.example.android.architecture.blueprints.todoapp.di

import com.example.android.architecture.blueprints.todoapp.gpx.GpxParser
import com.example.android.architecture.blueprints.todoapp.gpx.XmlGpxParser
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class GpxModule {

    @Singleton
    @Binds
    abstract fun bindGpxParser(parser: XmlGpxParser): GpxParser
}
