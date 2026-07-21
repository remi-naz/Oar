package dev.ridill.oar.moneyPiles.presentation.allPiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.oar.moneyPiles.domain.repository.AllPilesRepository
import javax.inject.Inject

@HiltViewModel
class AllPilesViewModel @Inject constructor(
    repo: AllPilesRepository,
) : ViewModel() {

    val pilesPagingData = repo.getAllPiles()
        .cachedIn(viewModelScope)
}
