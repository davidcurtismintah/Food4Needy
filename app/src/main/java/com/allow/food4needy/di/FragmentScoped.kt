package com.allow.food4needy.di

import javax.inject.Scope

@Scope
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class FragmentScoped {
}