package org.jdc.template.datasource.webservice.colors

import dagger.Component
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.jdc.template.inject.CommonTestModule
import org.jdc.template.log.JavaTree
import org.jdc.template.model.repository.IndividualRepositoryTestModule
import org.jdc.template.model.webservice.colors.ColorService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.MockitoAnnotations
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

class ColorServiceTest {

    @Inject
    lateinit var colorService: ColorService
    @Inject
    lateinit var mockWebServer: MockWebServer

    @BeforeEach
    fun setup() {
        MockitoAnnotations.initMocks(this)
        Timber.plant(JavaTree())

        val component = DaggerColorServiceTestComponent.builder().build()
        component.inject(this)
    }

    @Test
    fun getColors() {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(COLORS_RESPONSE))

        val request = colorService.colors().execute()
        assertTrue(request.isSuccessful)

        val colors = request.body()
        assertNotNull(colors)
        assertEquals(1, colors!!.colors.size)

        val color = colors.colors[0]
        assertEquals("White", color.colorName)
        assertEquals("#FFFFFF", color.hexValue)
    }

    @Test
    fun failedNetwork() {
        mockWebServer.enqueue(MockResponse().setResponseCode(500).setBody("""{"error": "Oh No!" }"""))
        val request = colorService.colors().execute()

        assertFalse(request.isSuccessful)
    }

    companion object {
        const val COLORS_RESPONSE = """
            {
                "colors": [
                    {
                       "colorName": "White",
                       "hexValue": "#FFFFFF"
                    }
                ]
            }
        """
    }
}

@Singleton
@Component(modules = [CommonTestModule::class, IndividualRepositoryTestModule::class])
interface ColorServiceTestComponent {
    fun inject(test: ColorServiceTest)
}