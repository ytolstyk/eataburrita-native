package com.tolstykh.eatABurrita.ui.main

import com.tolstykh.eatABurrita.data.BurritoDao
import com.tolstykh.eatABurrita.data.BurritoEntry
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TimeScreenViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var dao: BurritoDao

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        dao = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun stubDao(
        count: Int = 0,
        timestamp: Long? = null,
        entries: List<BurritoEntry> = emptyList(),
    ) {
        every { dao.getCount() } returns flowOf(count)
        every { dao.getLatestTimestamp() } returns flowOf(timestamp)
        every { dao.getEntriesSince(any()) } returns flowOf(entries)
    }

    @Test
    fun initialState_isLoading() {
        stubDao()
        val viewModel = TimeScreenViewModel(dao)
        assertEquals(TimeScreenViewModel.TimeScreenUIState.Loading, viewModel.timeScreenState.value)
    }

    @Test
    fun state_emitsSuccess_afterCollection() = runTest(testDispatcher) {
        stubDao(count = 3, timestamp = 1_000_000L)

        val viewModel = TimeScreenViewModel(dao)
        val states = mutableListOf<TimeScreenViewModel.TimeScreenUIState>()
        val job = launch { viewModel.timeScreenState.collect { states.add(it) } }
        advanceUntilIdle()
        job.cancel()

        assertTrue(states.any { it is TimeScreenViewModel.TimeScreenUIState.Success })
    }

    @Test
    fun state_success_containsCorrectCount() = runTest(testDispatcher) {
        stubDao(count = 7)

        val viewModel = TimeScreenViewModel(dao)
        val states = mutableListOf<TimeScreenViewModel.TimeScreenUIState>()
        val job = launch { viewModel.timeScreenState.collect { states.add(it) } }
        advanceUntilIdle()
        job.cancel()

        val success = states.filterIsInstance<TimeScreenViewModel.TimeScreenUIState.Success>().last()
        assertEquals(7, success.data.burritoCount)
    }

    @Test
    fun state_success_containsCorrectTimestamp() = runTest(testDispatcher) {
        stubDao(timestamp = 9_999_999L)

        val viewModel = TimeScreenViewModel(dao)
        val states = mutableListOf<TimeScreenViewModel.TimeScreenUIState>()
        val job = launch { viewModel.timeScreenState.collect { states.add(it) } }
        advanceUntilIdle()
        job.cancel()

        val success = states.filterIsInstance<TimeScreenViewModel.TimeScreenUIState.Success>().last()
        assertEquals(9_999_999L, success.data.lastTimestamp)
    }

    @Test
    fun state_success_nullTimestamp_mapsToZero() = runTest(testDispatcher) {
        stubDao(timestamp = null)

        val viewModel = TimeScreenViewModel(dao)
        val states = mutableListOf<TimeScreenViewModel.TimeScreenUIState>()
        val job = launch { viewModel.timeScreenState.collect { states.add(it) } }
        advanceUntilIdle()
        job.cancel()

        val success = states.filterIsInstance<TimeScreenViewModel.TimeScreenUIState.Success>().last()
        assertEquals(0L, success.data.lastTimestamp)
    }

    @Test
    fun state_dailyCounts_has30Elements() = runTest(testDispatcher) {
        stubDao()

        val viewModel = TimeScreenViewModel(dao)
        val states = mutableListOf<TimeScreenViewModel.TimeScreenUIState>()
        val job = launch { viewModel.timeScreenState.collect { states.add(it) } }
        advanceUntilIdle()
        job.cancel()

        val success = states.filterIsInstance<TimeScreenViewModel.TimeScreenUIState.Success>().last()
        assertEquals(30, success.data.dailyCounts.size)
    }

    @Test
    fun state_emitsError_onDaoException() = runTest(testDispatcher) {
        every { dao.getCount() } returns flow { throw RuntimeException("DB failure") }
        every { dao.getLatestTimestamp() } returns flowOf(null)
        every { dao.getEntriesSince(any()) } returns flowOf(emptyList())

        val viewModel = TimeScreenViewModel(dao)
        val states = mutableListOf<TimeScreenViewModel.TimeScreenUIState>()
        val job = launch { viewModel.timeScreenState.collect { states.add(it) } }
        advanceUntilIdle()
        job.cancel()

        assertTrue(states.any { it is TimeScreenViewModel.TimeScreenUIState.Error })
    }

    @Test
    fun addBurrito_callsDaoInsert() = runTest(testDispatcher) {
        stubDao()
        coEvery { dao.insert(any<BurritoEntry>()) } returns Unit

        val viewModel = TimeScreenViewModel(dao)
        viewModel.addBurrito()
        advanceUntilIdle()

        coVerify { dao.insert(any<BurritoEntry>()) }
    }
}
