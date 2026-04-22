package fyi.kittens.ozone.di

import kotlin.time.Clock
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides
import fyi.kittens.ozone.api.ApiProvider
import fyi.kittens.ozone.app.AppWorkflow
import fyi.kittens.ozone.app.Supervisor
import fyi.kittens.ozone.notifications.NotificationsRepository
import fyi.kittens.ozone.store.PersistentStorage
import fyi.kittens.ozone.timeline.TimelineRepository
import fyi.kittens.ozone.user.MyProfileRepository

@Component
@SingleInApp
abstract class AppComponent(
  @get:Provides @get:SingleInApp protected val storage: PersistentStorage,
) {
  abstract val appWorkflow: AppWorkflow

  abstract val supervisors: Set<Supervisor>

  @Provides
  fun clock(): Clock = Clock.System

  protected val ApiProvider.bind: Supervisor
    @Provides @IntoSet get() = this

  protected val MyProfileRepository.bind: Supervisor
    @Provides @IntoSet get() = this

  protected val TimelineRepository.bind: Supervisor
    @Provides @IntoSet get() = this

  protected val NotificationsRepository.bind: Supervisor
    @Provides @IntoSet get() = this
}
