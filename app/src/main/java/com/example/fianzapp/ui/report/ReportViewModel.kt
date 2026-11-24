package com.example.fianzapp.ui.report

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fianzapp.data.db.FianzappDatabase
import com.example.fianzapp.data.db.LeaseEntity
import com.example.fianzapp.data.db.Phase
import com.example.fianzapp.data.db.PhotoEntity
import com.example.fianzapp.data.db.ReportEntity
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

    private fun summarise(photos: List<PhotoEntity>, phase: Phase, report: ReportEntity?): PhaseSummary {
        val ofPhase = photos.filter { it.phase == phase }
        return PhaseSummary(
            itemPhotos = ofPhase.count { it.itemId != null },
            damages = ofPhase.count { it.itemId == null },
            report = report
        )
    }
}
