package dev.ridill.oar.moneyPiles.data.repository

import android.content.Context
import androidx.room.withTransaction
import dev.ridill.oar.R
import dev.ridill.oar.core.data.db.OarDatabase
import dev.ridill.oar.core.domain.model.BasicError
import dev.ridill.oar.core.domain.model.FundMovement
import dev.ridill.oar.core.domain.model.Result
import dev.ridill.oar.core.domain.model.RootError
import dev.ridill.oar.core.domain.util.rethrowIfCoroutineCancellation
import dev.ridill.oar.core.ui.util.UiText
import dev.ridill.oar.moneyPiles.domain.model.ContributionSource
import dev.ridill.oar.moneyPiles.domain.model.MoneyPileDetails
import dev.ridill.oar.moneyPiles.domain.model.PileContributionMode
import dev.ridill.oar.moneyPiles.domain.repository.MoneyPileRepository
import dev.ridill.oar.moneyPiles.domain.repository.PileFundMovementError
import dev.ridill.oar.moneyPiles.domain.repository.PileFundMovementRepository
import dev.ridill.oar.moneyPiles.domain.repository.PileFundMovementThrowable
import dev.ridill.oar.transactions.domain.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

internal class PileFundMovementRepositoryImpl(
    private val db: OarDatabase,
    private val pileRepo: MoneyPileRepository,
    private val transactionRepo: TransactionRepository,
    private val context: Context,
) : PileFundMovementRepository {
    override fun getPileById(id: Long): Flow<MoneyPileDetails?> = pileRepo
        .getPileDetailsFlow(id)

    override suspend fun movePileFund(
        pileId: Long,
        amount: Double,
        movement: FundMovement,
        timestamp: LocalDateTime,
        cycleId: Long
    ): Result<Unit, RootError> = withContext(Dispatchers.IO) {
        try {
            db.withTransaction {
                val pile = pileRepo.getPileDetails(pileId)
                    ?: throw PileFundMovementThrowable(PileFundMovementError.PileNotFound)
                var linkedTxId: Long? = null
                // Only add linked transaction if movement is into pile, and contribution mode is from balance
                if (movement == FundMovement.IN && pile.contributionMode == PileContributionMode.FROM_BALANCE) {
                    linkedTxId = transactionRepo.saveTransaction(
                        cycleId = cycleId,
                        amount = amount,
                        note = context.getString(R.string.pile_tx_linked_tx_note, pile.name),
                        timestamp = timestamp,
                        type = FundMovement.OUT,
                        excluded = false,
                        currency = pile.currency,
                    ).id
                }

                pileRepo.addEntryToPile(
                    pileId = pileId,
                    amount = amount,
                    movement = movement,
                    source = ContributionSource.MANUAL,
                    timestamp = timestamp,
                    transactionId = linkedTxId
                )

                Result.Success(Unit)
            }
        } catch (_: PileFundMovementThrowable) {
            Result.Error(
                PileFundMovementError.PileNotFound,
                UiText.StringResource(R.string.error_pile_not_found, isErrorText = true)
            )
        } catch (t: Throwable) {
            t.rethrowIfCoroutineCancellation()
            Result.Error(BasicError.UNKNOWN, UiText.StringResource(R.string.error_unknown))
        }
    }
}
