package me.btieger.plugins

import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.KubernetesClientBuilder
import io.ktor.server.application.*
import me.btieger.Config
import me.btieger.services.UserService
import me.btieger.services.UserServiceImpl
import me.btieger.services.helm.HelmService
import me.btieger.services.DslService
import me.btieger.services.DslServiceImpl
import me.btieger.services.ServerService
import me.btieger.services.ServerServiceImpl
import me.btieger.services.ssl.SslService
import me.btieger.services.ssl.SslServiceMockImpl
import me.btieger.services.ssl.SslServiceOpenSslWrapperImpl
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin

fun Application.configureKoin(config: Config) {
    install(Koin) {
        modules(
            module {
                single(createdAtStart = true) { config }
                single<KubernetesClient>(createdAtStart = true) { KubernetesClientBuilder().build() }
                single<UserService>(createdAtStart = true) { UserServiceImpl() }
                single<ServerService>(createdAtStart = true) { ServerServiceImpl(get(), get(), get(), get()) }
                single<DslService>(createdAtStart = true) { DslServiceImpl(get(), get(), get(), get()) }
                single(createdAtStart = true) { HelmService(get()) }
                if (environment.developmentMode) {
                    single<SslService>(createdAtStart = true) { SslServiceMockImpl() }
                } else {
                    single<SslService>(createdAtStart = true) { SslServiceOpenSslWrapperImpl(get()) }
                }
            }
        )
    }
}
