/*
 * Copyright 2026 adrc95 (Adrián Cabrera)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.adrc95.logronobus.core.testing

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val scheduler: TestCoroutineScheduler = TestCoroutineScheduler(),
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher(scheduler),
) {
    private val scopes = mutableListOf<CoroutineScope>()

    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    fun <T : CoroutineScope> track(scope: T): T {
        scopes += scope
        return scope
    }

    fun runViewModelTest(block: suspend TestScope.() -> Unit): TestResult =
        runTest(scheduler) {
            try {
                block()
            } finally {
                scopes.forEach { scope -> scope.coroutineContext[Job]?.cancelAndJoin() }
                scopes.clear()
            }
        }

    fun tearDown() {
        scopes.forEach(CoroutineScope::cancel)
        scopes.clear()
        // Run what the test left scheduled, then leave Main installed instead of resetting it: a
        // ViewModel scope that outlives the test body would otherwise resume on a dispatcher that no
        // longer exists, and that uncaught exception is reported against the following test. With
        // Main still present the late work lands on this scheduler, which nobody advances, so it is
        // harmless. setUp installs the next test's dispatcher.
        runCatching { testDispatcher.scheduler.advanceUntilIdle() }
    }
}
