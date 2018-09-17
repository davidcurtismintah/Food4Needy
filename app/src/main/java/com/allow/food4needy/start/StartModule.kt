package com.allow.food4needy.start

import com.allow.food4needy.di.FragmentScoped
import com.allow.food4needy.start.adduser.AddUserFragment
import com.allow.food4needy.start.selectrole.SelectRoleFragment
import com.allow.food4needy.start.welcome.WelcomeFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class StartModule {

    @FragmentScoped
    @ContributesAndroidInjector
    abstract fun welcomeFragment(): WelcomeFragment

    @FragmentScoped
    @ContributesAndroidInjector
    abstract fun selectRoleFragment(): SelectRoleFragment

    @FragmentScoped
    @ContributesAndroidInjector
    abstract fun userDetailsFragment(): AddUserFragment
}