package dev.ridill.oar.moneyPiles.presentation.allPiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.oar.moneyPiles.domain.repository.AllPilesRepository
import javax.inject.Inject

@HiltViewModel
class AllPilesViewModel @Inject constructor(
    private val repo: AllPilesRepository,
) : ViewModel() {

    val pilesPagingData = repo.getAllPiles()
        .cachedIn(viewModelScope)
//
//    val totalSavedAmount = repo.getAllPiles()
//        .mapLatest { piles -> piles.sumOf { it.currentAmount } }
//        .asStateFlow(viewModelScope, Double.Zero)
}
