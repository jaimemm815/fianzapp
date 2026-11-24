package com.example.fianzapp.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface LeaseDao {
    @Insert
    suspend fun insert(lease: LeaseEntity): Long

    @Update
    suspend fun update(lease: LeaseEntity)

    /** The app keeps one lease: the first one saved. Null until the form is saved. */
    @Query("SELECT * FROM lease ORDER BY id LIMIT 1")
    fun observeLease(): Flow<LeaseEntity?>
}

@Dao
interface RoomDao {
    @Insert
    suspend fun insert(room: RoomEntity): Long

    @Delete
    suspend fun delete(room: RoomEntity)

    @Query("SELECT * FROM rooms WHERE leaseId = :leaseId ORDER BY id")
    fun observeRoomsOfLease(leaseId: Long): Flow<List<RoomEntity>>

    /** Null when the id does not exist or the room was deleted. */
    @Query("SELECT * FROM rooms WHERE id = :roomId")
    fun observeRoom(roomId: Long): Flow<RoomEntity?>
}

@Dao
interface PhotoDao {
    @Insert
    suspend fun insert(photo: PhotoEntity): Long

    @Update
    suspend fun update(photo: PhotoEntity)

    @Delete
    suspend fun delete(photo: PhotoEntity)

    @Query("SELECT * FROM photos WHERE roomId = :roomId ORDER BY takenAt")
    fun observePhotosOfRoom(roomId: Long): Flow<List<PhotoEntity>>

    /** The latest photo of one item in one phase, so that a retake replaces the old one on screen. */
    @Query(
        "SELECT * FROM photos WHERE roomId = :roomId AND itemId = :itemId AND phase = :phase " +
            "ORDER BY takenAt DESC LIMIT 1"
    )
    fun observeItemPhoto(roomId: Long, itemId: Int, phase: Phase): Flow<PhotoEntity?>

    /** Every photo of every room of one lease, for the report. */
    @Query(
        "SELECT photos.* FROM photos INNER JOIN rooms ON photos.roomId = rooms.id " +
            "WHERE rooms.leaseId = :leaseId ORDER BY photos.takenAt"
    )
    fun observePhotosOfLease(leaseId: Long): Flow<List<PhotoEntity>>

    /** Photos of damage that is not on the checklist. */
    @Query("SELECT * FROM photos WHERE roomId = :roomId AND itemId IS NULL ORDER BY takenAt")
    fun observeDamagePhotos(roomId: Long): Flow<List<PhotoEntity>>
}

@Dao
interface ReportDao {
    @Upsert
    suspend fun upsert(report: ReportEntity)

    @Query("SELECT * FROM reports WHERE leaseId = :leaseId AND phase = :phase")
    fun observeReport(leaseId: Long, phase: Phase): Flow<ReportEntity?>
}
