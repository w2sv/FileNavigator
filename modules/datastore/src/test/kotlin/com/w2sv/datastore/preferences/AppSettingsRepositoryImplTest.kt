package com.w2sv.datastore.preferences

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.w2sv.domain.model.settings.AppSettings
import com.w2sv.domain.model.settings.Theme
import com.w2sv.domain.model.settings.ThemeSettings
import java.nio.file.Path
import kotlin.io.path.createTempDirectory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.job
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AppSettingsRepositoryImplTest {

    private lateinit var tempDir: Path
    private lateinit var dataStoreScope: CoroutineScope

    @Before
    fun setUp() {
        tempDir = createTempDirectory()
    }

    @After
    fun tearDown() {
        dataStoreScope.coroutineContext.job.cancel()
        tempDir.toFile().deleteRecursively()
    }

    @Test
    fun `appSettings are persisted and retrieved`() =
        runTest {
            val expected = AppSettings(
                showStorageVolumeNames = false,
                theme = ThemeSettings(
                    theme = Theme.Dark,
                    useAmoledBlackTheme = true,
                    useDynamicColors = false
                )
            )
            var repository = createRepository()

            repository.updateAppSettings { expected }
            assertEquals(expected, repository.appSettings.first { it == expected })

            dataStoreScope.coroutineContext.job.cancelAndJoin()
            repository = createRepository()

            assertEquals(expected, repository.appSettings.first { it == expected })
        }

    @Test
    fun `updates based on persisted settings preserve earlier changes`() =
        runTest {
            val observationScope = CoroutineScope(SupervisorJob() + StandardTestDispatcher())
            var repository = createRepository(observationScope)

            repository.updateAppSettings { it.copy(showStorageVolumeNames = false) }
            assertTrue(repository.appSettings.value.showStorageVolumeNames)
            repository.updateAppSettings { settings ->
                settings.copy(theme = settings.theme.copy(theme = Theme.Dark))
            }

            observationScope.cancel()
            dataStoreScope.coroutineContext.job.cancelAndJoin()
            repository = createRepository()

            val saved = repository.appSettings.first { it.theme.theme == Theme.Dark }
            assertFalse(saved.showStorageVolumeNames)
            assertEquals(Theme.Dark, saved.theme.theme)
        }

    private fun createRepository(observationScope: CoroutineScope? = null): AppSettingsRepositoryImpl {
        dataStoreScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val dataStore = PreferenceDataStoreFactory.create(
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("preferences.preferences_pb").toFile() }
        )
        return AppSettingsRepositoryImpl(
            dataStore = dataStore,
            scope = observationScope ?: dataStoreScope
        )
    }
}
