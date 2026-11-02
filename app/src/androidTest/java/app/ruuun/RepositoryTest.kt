package app.ruuun

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import app.ruuun.data.Effort
import app.ruuun.data.RunLog
import app.ruuun.data.RunMode
import app.ruuun.data.RunTag
import app.ruuun.data.RunnerSettings
import app.ruuun.data.RuuunDatabase
import app.ruuun.data.RuuunRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

/**
 * The storage layer, against a real Room database. Everything else is pure and covered
 * by JVM tests; this is the part that can only be exercised on a device.
 */
class RepositoryTest {

    private lateinit var db: RuuunDatabase
    private lateinit var repo: RuuunRepository

    private val today: LocalDate = LocalDate.of(2026, 11, 1)

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, RuuunDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repo = RuuunRepository(db.dao())
    }

    @After
    fun tearDown() = db.close()

    private suspend fun onboard(start: Int = 300, goal: Int = 2) =
        repo.completeSetup("Runner", start, goal, today)

    private suspend fun save(
        date: LocalDate = today,
        distanceM: Int = 300,
        effort: Effort = Effort.OKAY,
        tags: Set<RunTag> = emptySet(),
        note: String? = null,
        stops: Int? = null,
    ) = repo.saveRun(date, distanceM, effort, null, null, tags, note, stops = stops, now = 1L)

    @Test
    fun setup_writes_exactly_one_settings_row() = runTest {
        onboard(start = 500, goal = 3)
        val settings = repo.settings.first()
        assertNotNull(settings)
        assertEquals(1, settings!!.id)
        assertEquals(500, settings.startingTargetM)
        assertEquals(500, settings.currentTargetM)
        assertEquals(3, settings.weeklyGoal)
        assertEquals(false, settings.initialStoryPaceOnePerWeek)
        assertTrue(settings.onboardingComplete)
    }

    @Test
    fun one_run_a_week_freezes_the_gentle_story_pace() = runTest {
        onboard(goal = 1)
        assertTrue(repo.settings.first()!!.initialStoryPaceOnePerWeek)
        repo.updateSettings { it.copy(weeklyGoal = 3) }
        assertTrue(
            "changing the weekly goal must not thaw the pace",
            repo.settings.first()!!.initialStoryPaceOnePerWeek,
        )
    }

    @Test
    fun saving_a_run_persists_it_and_moves_the_mission() = runTest {
        onboard()
        val outcome = save(distanceM = 350, effort = Effort.EASY, stops = 0)
        assertNotNull(outcome)
        assertTrue(outcome!!.isNewRunDay)
        assertEquals(400, outcome.nextTargetM)
        assertEquals(400, repo.settings.first()!!.currentTargetM)

        val logs = repo.logs.first()
        assertEquals(1, logs.size)
        assertEquals(350, logs.single().distanceM)
        assertEquals("the mission at the time is stored", 300, logs.single().targetMAtLog)
    }

    @Test
    fun a_second_run_on_the_same_date_adds_distance_but_not_a_day() = runTest {
        onboard()
        save(distanceM = 200)
        val second = save(distanceM = 300)
        assertEquals(false, second!!.isNewRunDay)
        assertEquals(1, second.after.runDays)
        assertEquals(500L, second.after.totalDistanceM)
        assertEquals(50, second.after.xp)
    }

    @Test
    fun editing_a_run_recomputes_and_falls_back_to_a_safe_mission() = runTest {
        onboard()
        save(date = today.minusDays(3), distanceM = 1_500, effort = Effort.EASY, stops = 1)
        val raised = repo.settings.first()!!.currentTargetM
        assertTrue("a long run should have raised the mission", raised > 300)

        val log = repo.logs.first().single()
        repo.updateRun(log.id, log.date, 150, Effort.OKAY, null, null, emptySet(), null, stops = null, now = 2L)

        assertEquals(150, repo.logs.first().single().distanceM)
        assertEquals(
            "the mission falls back to the suggestion stored on the remaining run",
            300,
            repo.settings.first()!!.currentTargetM,
        )
    }

    @Test
    fun deleting_the_only_run_returns_the_mission_to_the_starting_distance() = runTest {
        onboard(start = 500)
        save(distanceM = 900, effort = Effort.EASY, stops = 2)
        assertTrue(repo.settings.first()!!.currentTargetM > 500)

        repo.deleteRun(repo.logs.first().single())
        assertTrue(repo.logs.first().isEmpty())
        assertEquals(500, repo.settings.first()!!.currentTargetM)
    }

    @Test
    fun tags_notes_and_mode_survive_a_write_and_read() = runTest {
        onboard()
        repo.saveRun(
            today, 400, Effort.HARD, 12, RunMode.WALK_JOG,
            setOf(RunTag.PARK, RunTag.BEACH, RunTag.FRIEND), "a note", stops = 3, now = 1L,
        )
        val log = repo.logs.first().single()
        assertEquals(setOf(RunTag.PARK, RunTag.BEACH, RunTag.FRIEND), log.tags)
        assertEquals(RunMode.WALK_JOG, log.mode)
        assertEquals(12, log.durationMinutes)
        assertEquals("a note", log.note)
        assertEquals(3, log.stops)
        assertEquals(LocalDate.of(2026, 11, 1), log.date)
    }

    @Test
    fun import_replaces_everything_atomically() = runTest {
        onboard()
        save(distanceM = 111)
        save(date = today.minusDays(1), distanceM = 222)

        val incoming = RunnerSettings(
            id = 1, nickname = "Imported", onboardedDate = LocalDate.of(2026, 6, 1),
            startingTargetM = 800, currentTargetM = 825, maxTargetM = 2_000,
            weeklyGoal = 1, initialStoryPaceOnePerWeek = true, onboardingComplete = true,
        )
        val incomingLogs = listOf(
            RunLog("x1", LocalDate.of(2026, 6, 2), 800, Effort.EASY, 800),
            RunLog("x2", LocalDate.of(2026, 6, 9), 900, Effort.OKAY, 800),
        )
        repo.replaceAll(incoming, incomingLogs)

        val settings = repo.settings.first()!!
        assertEquals("Imported", settings.nickname)
        assertEquals(825, settings.currentTargetM)
        val logs = repo.logs.first()
        assertEquals(2, logs.size)
        assertEquals(setOf("x1", "x2"), logs.map { it.id }.toSet())
    }

    @Test
    fun deleting_everything_leaves_no_settings_row() = runTest {
        onboard()
        save()
        repo.deleteEverything()
        assertNull(repo.settings.first())
        assertTrue(repo.logs.first().isEmpty())
    }

    @Test
    fun easier_and_harder_move_the_mission_and_respect_the_cap() = runTest {
        onboard(start = 300)
        repo.makeHarder()
        assertEquals(325, repo.settings.first()!!.currentTargetM)
        repo.makeEasier()
        assertEquals(300, repo.settings.first()!!.currentTargetM)

        repo.updateSettings { it.copy(maxTargetM = 300) }
        repo.makeHarder()
        assertEquals("cannot climb past the cap", 300, repo.settings.first()!!.currentTargetM)
    }

    @Test
    fun resetting_the_mission_never_lands_above_a_lowered_cap() = runTest {
        onboard(start = 800)
        repo.updateSettings { it.copy(maxTargetM = 500, currentTargetM = 500) }
        repo.resetTargetToStart()
        assertEquals(500, repo.settings.first()!!.currentTargetM)
    }

    @Test
    fun saving_without_onboarding_does_nothing() = runTest {
        assertNull(save())
        assertTrue(repo.logs.first().isEmpty())
    }

    @Test
    fun a_hundred_and_fifty_runs_persist_and_read_back() = runTest {
        onboard()
        repeat(150) { i ->
            repo.saveRun(
                today.minusDays(i.toLong()), 300 + i, Effort.entries[i % 3],
                null, null, emptySet(), null, stops = null, now = i.toLong(),
            )
        }
        val logs = repo.logs.first()
        assertEquals(150, logs.size)
        assertEquals("newest first", today, logs.first().date)
    }

    @Test
    fun counted_stops_set_the_next_runs_stop_goal() = runTest {
        onboard()
        val first = save(date = today.minusDays(2), distanceM = 1_100, stops = 8)!!
        assertNull("no goal before anything was counted", first.previousStopGoal)
        assertEquals(7, first.nextStopGoal)

        val second = save(date = today, distanceM = 1_150, stops = 6)!!
        assertEquals(7, second.previousStopGoal)
        assertEquals(5, second.nextStopGoal)
        val stored = repo.logs.first().first()
        assertEquals(6, stored.stops)
        assertEquals("the goal at the time is stored", 7, stored.stopsGoalAtLog)
    }

    @Test
    fun a_backdated_run_carries_no_stop_goal() = runTest {
        onboard()
        save(date = today, stops = 8)
        val earlier = save(date = today.minusDays(5), stops = 9)!!
        assertNull(earlier.previousStopGoal)
        assertNull(repo.logs.first().last().stopsGoalAtLog)
    }

    @Test
    fun adding_stops_to_an_old_run_keeps_the_mission() = runTest {
        // A 1.2 phone: it set the mission to 1.2 km before stops existed.
        onboard()
        repo.updateSettings { it.copy(currentTargetM = 1_200) }
        save(date = today.minusDays(1), distanceM = 1_100, effort = Effort.OKAY)
        assertEquals(1_200, repo.settings.first()!!.currentTargetM)

        val log = repo.logs.first().single()
        repo.updateRun(
            log.id, log.date, log.distanceM, log.effort, null, null, emptySet(), "First run!",
            stops = 8, now = 2L,
        )
        assertEquals(8, repo.logs.first().single().stops)
        assertEquals("a stop count alone keeps the mission", 1_200, repo.settings.first()!!.currentTargetM)
    }

    @Test
    fun correcting_the_latest_runs_stops_across_the_limit_replays_the_mission() = runTest {
        onboard()
        repo.updateSettings { it.copy(currentTargetM = 1_200) }
        save(distanceM = 1_300, effort = Effort.OKAY, stops = 6)
        assertEquals("6 stops hold the distance", 1_200, repo.settings.first()!!.currentTargetM)

        val log = repo.logs.first().single()
        repo.updateRun(log.id, log.date, log.distanceM, log.effort, null, null, emptySet(), null, stops = 1, now = 2L)
        assertEquals("1 stop lets it grow, as if logged that way", 1_350, repo.settings.first()!!.currentTargetM)

        repo.updateRun(log.id, log.date, log.distanceM, log.effort, null, null, emptySet(), null, stops = 6, now = 3L)
        assertEquals("and back again", 1_200, repo.settings.first()!!.currentTargetM)
    }

    @Test
    fun correcting_an_older_runs_stops_leaves_the_mission() = runTest {
        onboard()
        repo.updateSettings { it.copy(currentTargetM = 1_200) }
        save(date = today.minusDays(2), distanceM = 1_300, effort = Effort.OKAY, stops = 6)
        save(date = today, distanceM = 1_200, effort = Effort.OKAY, stops = 5)
        val mission = repo.settings.first()!!.currentTargetM
        val older = repo.logs.first().last()
        repo.updateRun(older.id, older.date, older.distanceM, older.effort, null, null, emptySet(), null, stops = 0, now = 9L)
        assertEquals(mission, repo.settings.first()!!.currentTargetM)
    }

    @Test
    fun a_new_journey_gets_the_five_km_cap() = runTest {
        onboard()
        assertEquals(5_000, repo.settings.first()!!.maxTargetM)
        assertEquals(2, repo.settings.first()!!.lowStopsLimit)
    }

    @Test
    fun beating_the_mission_with_many_stops_holds_it() = runTest {
        onboard()
        val outcome = save(distanceM = 1_100, effort = Effort.OKAY, stops = 8)!!
        assertEquals(300, outcome.nextTargetM)
        assertEquals(300, repo.settings.first()!!.currentTargetM)
    }

    @Test
    fun correcting_stops_after_a_manual_change_keeps_the_parents_mission() = runTest {
        onboard()
        repo.updateSettings { it.copy(currentTargetM = 1_200) }
        save(distanceM = 1_300, effort = Effort.OKAY, stops = 6)
        repo.makeHarder() // 1,225 m by hand
        val log = repo.logs.first().single()
        repo.updateRun(log.id, log.date, log.distanceM, log.effort, null, null, emptySet(), null, stops = 1, now = 2L)
        assertEquals(1_225, repo.settings.first()!!.currentTargetM)
    }
}
