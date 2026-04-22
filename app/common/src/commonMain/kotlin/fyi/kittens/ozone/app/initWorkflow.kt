package fyi.kittens.ozone.app

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import fyi.kittens.ozone.di.AppComponent
import fyi.kittens.ozone.di.create
import fyi.kittens.ozone.store.PersistentStorage

fun initWorkflow(
  coroutineScope: CoroutineScope,
  storage: PersistentStorage,
): AppWorkflow {
  val component = AppComponent::class.create(storage)
  val workflow = component.appWorkflow

  component.supervisors.forEach {
    coroutineScope.launch(SupervisorJob()) { it.start() }
  }

  return workflow
}
