package dev.ridill.oar.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.ridill.oar.account.data.repository.AuthRepositoryImpl
import dev.ridill.oar.account.domain.repository.AuthRepository
import dev.ridill.oar.account.domain.service.AuthService
import dev.ridill.oar.account.domain.service.FirebaseAuthService
import dev.ridill.oar.account.presentation.util.AuthorizationService
import dev.ridill.oar.account.presentation.util.CredentialService
import dev.ridill.oar.account.presentation.util.DefaultAuthorizationService
import dev.ridill.oar.account.presentation.util.DefaultCredentialService

@Module
@InstallIn(SingletonComponent::class)
object AccountModule {

    @Provides
    fun provideCredentialService(
        @ApplicationContext context: Context
    ): CredentialService = DefaultCredentialService(context)

    @Provides
    fun provideAuthService(): AuthService = FirebaseAuthService()

    @Provides
    fun provideAuthorizationService(
        @ApplicationContext context: Context,
        authService: AuthService
    ): AuthorizationService = DefaultAuthorizationService(context, authService)

    @Provides
    fun provideAuthRepository(
        credentialService: CredentialService,
        authService: AuthService,
        authorizationService: AuthorizationService
    ): AuthRepository = AuthRepositoryImpl(
        credentialService = credentialService,
        authService = authService,
        authorizationService = authorizationService
    )
}