package com.example.fianzapp.ui.report

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fianzapp.data.db.FianzappDatabase
import com.example.fianzapp.data.db.LeaseEntity
import com.example.fianzapp.data.db.Phase
import com.example.fianzapp.data.db.PhotoEntity
import com.example.fianzapp.data.db.ReportEntity
import com.example.fianzapp.R
import com.example.fianzapp.data.ChecklistRepository
import com.example.fianzapp.data.remote.FIRESTORE_LEASE_ID
import com.example.fianzapp.data.remote.ItemDocument
import com.example.fianzapp.data.remote.LEASES
import com.example.fianzapp.data.remote.REPORTS
import com.example.fianzapp.data.remote.ReportDocument
import com.example.fianzapp.data.remote.RoomDocument
import com.example.fianzapp.data.remote.isFirebaseConfigured
import com.example.fianzapp.domain.comparePhotos
import com.example.fianzapp.domain.parseLabels
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** What one report (move-in or move-out) covers, and its signatures once signed. */
data class PhaseSummary(
    val itemPhotos: Int,
    val damages: Int,
    val report: ReportEntity?
) {
    val isSigned: Boolean get() = report?.signedAt != null
    val hasPhotos: Boolean get() = itemPhotos + damages > 0
}

/** A6: sending a signed report to the landlord. */
sealed interface SendState {
    data object Idle : SendState
    data object Sending : SendState
    data object Sent : SendState
    data class Failed(val message: String) : SendState
}

sealed interface ReportUiState {
    data object Loading : ReportUiState
    data object NoLease : ReportUiState
    data class Ready(
        val lease: LeaseEntity,
        val rooms: Int,
        val moveIn: PhaseSummary,
        val moveOut: PhaseSummary
    ) : ReportUiState {
        fun summary(phase: Phase) = if (phase == Phase.MOVE_IN) moveIn else moveOut
    }
}

class ReportViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FianzappDatabase.getInstance(application)
    private val checklistRepository = ChecklistRepository(application)

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<ReportUiState> = db.leaseDao().observeLease()
        .flatMapLatest { lease ->
            if (lease == null) {
                flowOf(ReportUiState.NoLease)
            } else {
                combine(
                    db.roomDao().observeRoomsOfLease(lease.id),
                    db.photoDao().observePhotosOfLease(lease.id),
                    db.reportDao().observeReport(lease.id, Phase.MOVE_IN),
                    db.reportDao().observeReport(lease.id, Phase.MOVE_OUT)
                ) { rooms, photos, moveIn, moveOut ->
                    ReportUiState.Ready(
                        lease = lease,
                        rooms = rooms.size,
                        moveIn = summarise(photos, Phase.MOVE_IN, moveIn),
                        moveOut = summarise(photos, Phase.MOVE_OUT, moveOut)
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReportUiState.Loading)

    /**
     * Saves both signatures (as encodeSignature text) and the time. A report that is already
     * signed is never overwritten: once signed, it cannot change.
     */
    fun sign(leaseId: Long, phase: Phase, tenantSignature: String, landlordSignature: String) {
        if (tenantSignature.isBlank() || landlordSignature.isBlank()) return
        viewModelScope.launch {
            val existing = db.reportDao().observeReport(leaseId, phase).first()
            if (existing?.signedAt != null) return@launch
            db.reportDao().upsert(
                ReportEntity(
                    leaseId = leaseId,
                    phase = phase,
                    tenantSignature = tenantSignature,
                    landlordSignature = landlordSignature,
                    signedAt = System.currentTimeMillis()
                )
            )
        }
    }

    private val _sendState = MutableStateFlow<SendState>(SendState.Idle)
    val sendState: StateFlow<SendState> = _sendState.asStateFlow()

    /**
     * A6: writes the signed report to Firestore, without its photos, for the landlord to read.
     * The tenant must be signed in; the rules decide whether the write is allowed.
     */
    fun send(phase: Phase) {
        val context = getApplication<Application>()
        if (!isFirebaseConfigured(context)) return
        _sendState.value = SendState.Sending
        viewModelScope.launch {
            val document = try {
                buildDocument(phase)
            } catch (e: Exception) {
                null
            }
            if (document == null) {
                _sendState.value = SendState.Failed(context.getString(R.string.report_send_not_signed))
                return@launch
            }
            FirebaseFirestore.getInstance()
                .collection(LEASES).document(FIRESTORE_LEASE_ID)
                .collection(REPORTS).document(phase.name)
                .set(document)
                .addOnSuccessListener { _sendState.value = SendState.Sent }
                .addOnFailureListener { _sendState.value = SendState.Failed(it.message ?: it.javaClass.simpleName) }
        }
    }

    private suspend fun buildDocument(phase: Phase): ReportDocument? {
        val lease = db.leaseDao().observeLease().first() ?: return null
        val report = db.reportDao().observeReport(lease.id, phase).first() ?: return null
        val signedAt = report.signedAt ?: return null
        val rooms = db.roomDao().observeRoomsOfLease(lease.id).first()
        val photos = db.photoDao().observePhotosOfLease(lease.id).first()
        // Item names come from the checklist; if it cannot be read, the id is still sent.
        val itemNames = try {
            checklistRepository.loadChecklist().roomTypes.flatMap { it.items }.associate { it.id to it.name }
        } catch (e: Exception) {
            emptyMap()
        }
        return ReportDocument(
            phase = phase.name,
            address = lease.address,
            landlordName = lease.landlordName,
            moveInDate = lease.moveInDate,
            depositCents = lease.depositCents,
            keysReturnedDate = lease.keysReturnedDate,
            rooms = rooms.map { room ->
                val ofRoom = photos.filter { it.roomId == room.id }
                RoomDocument(
                    name = room.name,
                    typeId = room.typeId,
                    items = ofRoom.filter { it.phase == phase }
                        .groupBy { it.itemId to (if (it.itemId == null) it.id else 0L) }
                        .values.map { group -> group.maxBy { it.takenAt } }
                        .map { photo ->
                            ItemDocument(
                                itemId = photo.itemId,
                                name = photo.itemId?.let { itemNames[it] ?: "Item $it" }.orEmpty(),
                                note = photo.note,
                                takenAt = photo.takenAt,
                                latitude = photo.latitude,
                                longitude = photo.longitude,
                                comparison = photo.itemId?.let { id -> comparisonOf(ofRoom, id) }
                            )
                        }
                )
            },
            tenantSignature = report.tenantSignature,
            landlordSignature = report.landlordSignature,
            signedAt = signedAt,
            sentAt = System.currentTimeMillis()
        )
    }

    /** A10's answer for one item, when it has both photos. */
    private fun comparisonOf(photos: List<PhotoEntity>, itemId: Int): String? {
        fun latest(phase: Phase) = photos.filter { it.itemId == itemId && it.phase == phase }.maxByOrNull { it.takenAt }
        val moveIn = latest(Phase.MOVE_IN) ?: return null
        val moveOut = latest(Phase.MOVE_OUT) ?: return null
        return comparePhotos(parseLabels(moveIn.labels), parseLabels(moveOut.labels)).name
    }

    private fun summarise(photos: List<PhotoEntity>, phase: Phase, report: ReportEntity?): PhaseSummary {
        val ofPhase = photos.filter { it.phase == phase }
        return PhaseSummary(
            itemPhotos = ofPhase.count { it.itemId != null },
            damages = ofPhase.count { it.itemId == null },
            report = report
        )
    }
}
