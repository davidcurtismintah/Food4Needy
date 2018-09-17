package com.allow.food4needy.di

import com.allow.food4needy.start.StartActivity
import com.allow.food4needy.start.StartModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBindingModule {

    @ActivityScoped
    @ContributesAndroidInjector(modules = [StartModule::class])
    abstract fun startActivity(): StartActivity
}