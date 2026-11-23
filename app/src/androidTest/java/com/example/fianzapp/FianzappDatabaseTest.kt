package com.example.fianzapp

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.fianzapp.data.db.FianzappDatabase
import com.example.fianzapp.data.db.LeaseEntity
import com.example.fianzapp.data.db.Phase
import com.example.fianzapp.data.db.PhotoEntity
import com.example.fianzapp.data.db.RoomEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** A4: the tables, in a database that lives only in memory. Runs on the emulator. */
@RunWith(AndroidJUnit4::class)
class FianzappDatabaseTest {

    private lateinit var db: FianzappDatabase

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, FianzappDatabase::class.java).build()
    }

    @After
    fun closeDb() = db.close()

    private suspend fun insertLeaseAndRoom(): Long {
        val leaseId = db.leaseDao().insert(
            LeaseEntity(
                address = "Calle Falsa 123",
                landlordName = "Invented Landlord",
                moveInDate = "2026-11-01",
                depositCents = 75050
            )
        )
        return db.roomDao().insert(RoomEntity(leaseId = leaseId, name = "Kitchen", typeId = "kitchen"))
    }

    @Test
    fun emptyTables_noLeaseAndNoRooms() = runBlocking {
        assertNull(db.leaseDao().observeLease().first())
        assertTrue(db.roomDao().observeRoomsOfLease(1).first().isEmpty())
        assertNull(db.roomDao().observeRoom(99).first())
    }

    @Test
    fun itemPhoto_latestOneWins_andDamageHasNoItem() = runBlocking {
        val roomId = insertLeaseAndRoom()
        val photos = db.photoDao()
        photos.insert(PhotoEntity(roomId = roomId, itemId = 1, phase = Phase.MOVE_IN, path = "a.jpg", takenAt = 1000))
        photos.insert(PhotoEntity(roomId = roomId, itemId = 1, phase = Phase.MOVE_IN, path = "b.jpg", takenAt = 2000))
        photos.insert(
            PhotoEntity(
                roomId = roomId, itemId = null, phase = Phase.MOVE_IN,
                path = "c.jpg", takenAt = 3000, note = "Cracked tile"
            )
        )

        assertEquals("b.jpg", photos.observeItemPhoto(roomId, 1, Phase.MOVE_IN).first()?.path)
        assertNull(photos.observeItemPhoto(roomId, 1, Phase.MOVE_OUT).first())
        assertEquals(listOf("Cracked tile"), photos.observeDamagePhotos(roomId).first().map { it.note })
    }

    @Test
    fun deletingRoom_deletesItsPhotoRows() = runBlocking {
        val roomId = insertLeaseAndRoom()
        db.photoDao().insert(PhotoEntity(roomId = roomId, itemId = 1, phase = Phase.MOVE_IN, path = "a.jpg", takenAt = 1000))
        val room = db.roomDao().observeRoom(roomId).first() ?: error("room not saved")

        db.roomDao().delete(room)

        assertTrue(db.photoDao().observePhotosOfRoom(roomId).first().isEmpty())
    }
}
