package com.example.equall.di

import com.example.equall.data.FakeHomeDataSource
import com.example.equall.data.HomeRepositoryImpl
import com.example.equall.domain.CheckEligibilityUseCase
import com.example.equall.domain.GetHomeDataUseCase
import com.example.equall.presentation.HomeViewModel

object HomeModule {
    fun createViewModel() = HomeViewModel(
        GetHomeDataUseCase(HomeRepositoryImpl(FakeHomeDataSource())), CheckEligibilityUseCase()
    )
}
