package com.w2sv.filenavigator.ui.screen.navigatorsettings

import android.content.Context
import app.cash.turbine.test
import com.w2sv.domain.model.filetype.PresetFileType
import com.w2sv.domain.model.navigatorconfig.NavigatorConfig
import com.w2sv.domain.repository.NavigatorConfigDataSource
import com.w2sv.domain.repository.PreferencesRepository
import com.w2sv.filenavigator.ui.MainDispatcherRule
import com.w2sv.persistedpreferences.PersistedPreference
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class NavigatorSettingsScreenViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val configMock = MutableStateFlow(NavigatorConfig.default)
    private val dataSource = mockk<NavigatorConfigDataSource>(relaxed = true) {
        every { config } returns configMock
    }
    private var savedShowAutoMoveIntroduction: Boolean? = null
    private val showAutoMoveIntroduction = mockk<PersistedPreference<Boolean>> {
        every { stateIn(any(), any()) } returns MutableStateFlow(true)
        every { save } returns { savedShowAutoMoveIntroduction = it }
    }
    private val preferencesRepository = mockk<PreferencesRepository>(relaxed = true) {
        every { showAutoMoveIntroduction } returns this@NavigatorSettingsScreenViewModelTest.showAutoMoveIntroduction
    }
    private val viewModel by lazy {
        NavigatorSettingsScreenViewModel(
            navigatorConfigDataSource = dataSource,
            preferencesRepository = preferencesRepository,
            navigatorIsRunning = MutableStateFlow(true),
            context = mockk<Context>(relaxed = true)
        )
    }

    @Test
    fun `configChangesHaveBeenApplied emits after applied config changes`() =
        runTest {
            viewModel.configChangesHaveBeenApplied.test {
                expectNoEvents()

                configMock.value = NavigatorConfig.default.toggleFileTypeEnablement(PresetFileType.Image.toFileType())
                advanceUntilIdle()
                awaitItem()

                configMock.value = NavigatorConfig.default
                advanceUntilIdle()
                awaitItem()
            }
        }

    @Test
    fun `reversibleConfig commits edited config through data source update`() =
        runTest {
            val reversibleConfig = viewModel.reversibleConfig

            advanceUntilIdle()
            val editedConfig = NavigatorConfig.default.toggleFileTypeEnablement(PresetFileType.Image.toFileType())

            reversibleConfig.value = editedConfig

            advanceUntilIdle()
            assertTrue(reversibleConfig.isDirty.first())

            reversibleConfig.commit()
            advanceUntilIdle()

            val updater = slot<suspend (NavigatorConfig) -> NavigatorConfig>()

            coVerify(exactly = 1) {
                dataSource.update(capture(updater))
            }

            assertEquals(
                editedConfig,
                updater.captured(NavigatorConfig.default)
            )
        }

    @Test
    fun `dismissAutoMoveIntroduction disables introduction preference`() =
        runTest {
            viewModel.dismissAutoMoveIntroduction()
            advanceUntilIdle()

            assertEquals(false, savedShowAutoMoveIntroduction)
        }
}
