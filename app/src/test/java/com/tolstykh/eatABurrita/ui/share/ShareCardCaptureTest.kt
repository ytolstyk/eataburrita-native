package com.tolstykh.eatABurrita.ui.share

import android.view.View
import android.view.ViewParent
import android.view.Window
import androidx.compose.ui.window.DialogWindowProvider
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ShareCardCaptureTest {

    @Test
    fun findDialogWindow_returnsWindow_whenDialogWindowProviderIsDirectParent() {
        val window = mockk<Window>()
        val dialogProvider = mockk<ViewParent>(moreInterfaces = arrayOf(DialogWindowProvider::class))
        every { (dialogProvider as DialogWindowProvider).window } returns window
        every { dialogProvider.parent } returns null

        val view = mockk<View>()
        every { view.parent } returns dialogProvider

        assertEquals(window, findDialogWindow(view))
    }

    @Test
    fun findDialogWindow_walksUpMultipleAncestorsToFindTheDialogWindow() {
        val window = mockk<Window>()
        val dialogProvider = mockk<ViewParent>(moreInterfaces = arrayOf(DialogWindowProvider::class))
        every { (dialogProvider as DialogWindowProvider).window } returns window
        every { dialogProvider.parent } returns null

        val middleParent = mockk<ViewParent>()
        every { middleParent.parent } returns dialogProvider

        val view = mockk<View>()
        every { view.parent } returns middleParent

        assertEquals(window, findDialogWindow(view))
    }

    @Test
    fun findDialogWindow_returnsNull_whenNoAncestorIsADialogWindowProvider() {
        // Regression guard: this is the exact scenario that shipped a bug — LocalView.current
        // resolved to the host Activity's view, whose ancestor chain has no DialogWindowProvider.
        val topParent = mockk<ViewParent>()
        every { topParent.parent } returns null

        val view = mockk<View>()
        every { view.parent } returns topParent

        assertNull(findDialogWindow(view))
    }

    @Test
    fun findDialogWindow_returnsNull_whenViewHasNoParent() {
        val view = mockk<View>()
        every { view.parent } returns null

        assertNull(findDialogWindow(view))
    }
}
