package org.mathieu.data.di

import io.ktor.client.HttpClient
import org.koin.dsl.module
import org.mathieu.data.local.CharacterLocal
import org.mathieu.data.local.LocationPreviewLocal
import org.mathieu.data.local.RMDatabase
import org.mathieu.data.local.RealmDatabase
import org.mathieu.data.remote.CharacterApi
import org.mathieu.data.remote.LocationApi
import org.mathieu.data.remote.createHttpClient
import org.mathieu.data.repositories.CharacterRepositoryImpl
import org.mathieu.data.repositories.LocationRepositoryImpl
import org.mathieu.domain.repositories.CharacterRepository
import org.mathieu.domain.repositories.LocationRepository

//https://rickandmortyapi.com/documentation/#rest
private const val RMAPI_URL = "https://rickandmortyapi.com/api/"

val dataModule = module {

    single<HttpClient> {
        createHttpClient(
            baseUrl = RMAPI_URL
        )
    }

    single<RealmDatabase> { RMDatabase() }

    single { CharacterLocal(get()) }

    single { LocationPreviewLocal(get()) }

    single { CharacterApi(get()) }

    single<CharacterRepository> {
        CharacterRepositoryImpl(
            get(),
            get(),
            get(),
            get()
        )
    }

    single { LocationApi(get()) }

    single<LocationRepository> { LocationRepositoryImpl(get()) }
}