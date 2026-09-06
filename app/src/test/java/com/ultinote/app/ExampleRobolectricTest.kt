package com.ultinote.app

import android.content.Context
import androidx.compose.ui.geometry.Offset
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.ultinote.app.canvas.RulerGeometry
import com.ultinote.app.canvas.RulerState
import com.ultinote.app.data.local.KomorebiDatabase
import com.ultinote.app.data.local.KomorebiRepository
import com.ultinote.app.data.local.SerializationHelpers
import com.ultinote.app.data.model.CoverStyle
import com.ultinote.app.data.model.DrawingStroke
import com.ultinote.app.data.model.PaperTemplate
import com.ultinote.app.data.model.ShapeAnnotation
import com.ultinote.app.data.model.ShapeType
import com.ultinote.app.data.model.StrokePoint
import com.ultinote.app.data.model.TextAnnotation
import com.ultinote.app.data.model.ToolType
import com.ultinote.app.R
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var database: KomorebiDatabase
    private lateinit var repository: KomorebiRepository
    private lateinit var context: Context

    @Before
    fun createDb() {
        context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, KomorebiDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = KomorebiRepository(context, database)
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        database.close()
    }

    @Test
    fun testAppNameResource() {
        val appName = context.getString(R.string.app_name)
        assertEquals("UltiNote", appName)
    }

    @Test
    fun testFontRepositoryOptions() {
        com.ultinote.app.ui.theme.FontRepository.setFont(com.ultinote.app.ui.theme.AppFontOption.EDITORIAL_SERIF)
        assertEquals(com.ultinote.app.ui.theme.AppFontOption.EDITORIAL_SERIF, com.ultinote.app.ui.theme.FontRepository.currentFont.value)

        val typography = com.ultinote.app.ui.theme.FontRepository.getTypography(com.ultinote.app.ui.theme.AppFontOption.EDITORIAL_SERIF)
        assertNotNull(typography)

        // Reset to default
        com.ultinote.app.ui.theme.FontRepository.setFont(com.ultinote.app.ui.theme.AppFontOption.MODERN_NEO_GROTESQUE)
        assertEquals(com.ultinote.app.ui.theme.AppFontOption.MODERN_NEO_GROTESQUE, com.ultinote.app.ui.theme.FontRepository.currentFont.value)
    }

    @Test
    fun testFolderSvgIconsCatalog() {
        val icons = com.ultinote.app.ui.components.AvailableFolderSvgIcons
        assertTrue(icons.isNotEmpty())
        assertTrue(icons.any { it.first == "calculate" })
        assertTrue(icons.any { it.first == "book" })
        assertTrue(icons.any { it.first == "palette" })
        assertTrue(icons.any { it.first == "code" })

        val icon = com.ultinote.app.ui.components.getFolderSvgIcon("calculate")
        assertNotNull(icon)
    }

    @Test
    fun testStrokeSerialization() {
        val stroke = DrawingStroke(
            points = listOf(StrokePoint(10f, 20f, 0.8f), StrokePoint(30f, 40f, 1.0f)),
            color = 0xFF4A6B56L,
            strokeWidth = 3.5f,
            toolType = ToolType.PEN_BALLPOINT
        )
        val json = SerializationHelpers.strokesToJson(listOf(stroke))
        val deserialized = SerializationHelpers.jsonToStrokes(json)

        assertEquals(1, deserialized.size)
        assertEquals(stroke.id, deserialized[0].id)
        assertEquals(2, deserialized[0].points.size)
        assertEquals(0xFF4A6B56L, deserialized[0].color)
    }

    @Test
    fun testShapeAndTextSerialization() {
        val shape = ShapeAnnotation(
            type = ShapeType.RECTANGLE,
            startX = 50f,
            startY = 50f,
            endX = 200f,
            endY = 150f,
            color = 0xFF1E2822L
        )
        val text = TextAnnotation(
            text = "Calculus Integration Formula",
            x = 60f,
            y = 70f,
            fontSize = 18f
        )

        val shapesJson = SerializationHelpers.shapesToJson(listOf(shape))
        val textJson = SerializationHelpers.textBlocksToJson(listOf(text))

        val loadedShapes = SerializationHelpers.jsonToShapes(shapesJson)
        val loadedText = SerializationHelpers.jsonToTextBlocks(textJson)

        assertEquals(1, loadedShapes.size)
        assertEquals(ShapeType.RECTANGLE, loadedShapes[0].type)
        assertEquals(1, loadedText.size)
        assertEquals("Calculus Integration Formula", loadedText[0].text)
    }

    @Test
    fun testFolderAndNoteCreation() = runBlocking {
        val folderId = repository.createFolder("Mathematics", null, "#769382")
        val folder = repository.getFolder(folderId)
        assertNotNull(folder)
        assertEquals("Mathematics", folder?.name)

        val noteId = repository.createNote(
            title = "Linear Algebra Book",
            folderId = folderId,
            coverStyle = CoverStyle.BOTANICAL,
            template = PaperTemplate.CORNELL
        )

        val note = repository.getNote(noteId)
        assertNotNull(note)
        assertEquals("Linear Algebra Book", note?.title)
        assertEquals(CoverStyle.BOTANICAL, note?.coverStyle)

        // Verify page was created
        val pages = repository.getPagesList(noteId)
        assertEquals(1, pages.size)
        assertEquals(PaperTemplate.CORNELL, pages[0].template)

        // Add a second page
        val page2 = repository.addPage(noteId, PaperTemplate.GRID)
        val updatedPages = repository.getPagesList(noteId)
        assertEquals(2, updatedPages.size)
        assertEquals(PaperTemplate.GRID, updatedPages[1].template)
    }

    @Test
    fun testRulerSnappingCalculation() {
        val ruler = RulerState(
            isVisible = true,
            center = Offset(300f, 300f),
            angle = 0f,
            length = 400f,
            height = 80f
        )
        // Top edge is at y = 300 - 40 = 260. A touch at (300, 265) is within 5px of top edge
        val snapped = RulerGeometry.snapPointToRulerEdge(Offset(300f, 265f), ruler, snapThreshold = 30f)
        assertNotNull(snapped)
        assertEquals(260f, snapped!!.y, 0.01f)
        assertEquals(300f, snapped.x, 0.01f)
    }
}
