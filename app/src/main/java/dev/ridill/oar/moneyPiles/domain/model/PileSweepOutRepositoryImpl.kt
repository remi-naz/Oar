package dev.ridill.oar.moneyPiles.domain.model

import androidx.room.withTransaction
import dev.ridill.oar.R
import dev.ridill.oar.aggregations.domain.repository.AggregationsRepository
import dev.ridill.oar.budgetCycles.domain.repository.BudgetCycleRepository
import dev.ridill.oar.core.data.db.OarDatabase
import dev.ridill.oar.core.domain.model.FundMovement
import dev.ridill.oar.core.domain.model.Result
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.core.domain.util.LocaleUtil
import dev.ridill.oar.core.domain.util.rethrowIfCoroutineCancellation
import dev.ridill.oar.core.ui.util.UiText
import dev.ridill.oar.moneyPiles.data.local.MoneyPileDao
import dev.ridill.oar.moneyPiles.data.local.entity.MoneyPileTransactionsEntity
import dev.ridill.oar.moneyPiles.data.local.view.MoneyPileTransactionDao
import dev.ridill.oar.moneyPiles.domain.repository.MoneyPileError
import dev.ridill.oar.moneyPiles.domain.repository.MoneyPileRepository
import dev.ridill.oar.moneyPiles.domain.repository.PileNotFoundThrowable
import dev.ridill.oar.moneyPiles.domain.repository.PileSweepOutRepository
import dev.ridill.oar.transactions.domain.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

internal class PileSweepOutRepositoryImpl(
    private val db: OarDatabase,
    private val pileDao: MoneyPileDao,
    private val pileTxDao: MoneyPileTransactionDao,
    private val pileRepo: MoneyPileRepository,
    private val cycleRepo: BudgetCycleRepository,
    private val aggRepo: AggregationsRepository,
    private val transactionRepo: TransactionRepository,
) : PileSweepOutRepository {

    override fun getPileDetails(pileId: Long): Flow<MoneyPileDetails?> =
        pileRepo.getPileDetailsFlow(pileId)

    override fun getSweepableAmount(pileId: Long): Flow<Double> =
        aggRepo.getAggregateForMoneyPile(pileId)

    override suspend fun sweepOutPile(
        pileId: Long,
        sweepAmount: Double,
        createLinkedTransaction: Boolean
    ): Result<Unit, MoneyPileError> = withContext(Dispatchers.IO) {
        db.withTransaction {
            try {
                val pile = pileDao.getPileById(pileId)
                    ?: throw PileNotFoundThrowable()

                val activeCycle = cycleRepo.requireActiveCycle()
                val timestampNow = DateUtil.now()
                pileTxDao.lockAllEntriesInPile(pile.id)
                val sweepOutEntity = MoneyPileTransactionsEntity(
                    pileId = pile.id,
                    amount = sweepAmount,
                    movement = FundMovement.OUT,
                    contributionSource = ContributionSource.SWEEP_OUT,
                    locked = true,
                    createdTimestamp = timestampNow,
                    transactionId = null
                )
                val sweepOutEntryId = pileTxDao.upsert(sweepOutEntity).first()
                if (createLinkedTransaction) {
                    val transaction = transactionRepo.saveTransaction(
                        cycleId = activeCycle.id,
                        amount = sweepAmount,
                        id = OarDatabase.DEFAULT_ID_LONG,
                        note = pile.name,
                        timestamp = timestampNow,
                        type = FundMovement.OUT,
                        tagId = null,
                        folderId = null,
                        scheduleId = null,
                        excluded = pile.contributionMode == PileContributionMode.FROM_BALANCE,
                        currency = LocaleUtil.currencyForCode(pile.currencyCode),
                    )
                    pileTxDao.setLinkedTransactionId(
                        id = sweepOutEntryId,
                        transactionId = transaction.id
                    )
                }

                val target = pile.targetRemainingAmount
                val newTarget = target?.minus(sweepAmount)
                pileDao.setTargetRemainingAmountForPile(pile.id, newTarget)
                val isFullSweepOut = target != null && sweepAmount >= target
                if (isFullSweepOut) {
                    pileDao.setCompletedTimestampForPile(pile.id, timestampNow)
                }

                Result.Success(Unit)
            } catch (_: PileNotFoundThrowable) {
                Result.Error(
                    MoneyPileError.NotFound,
                    UiText.StringResource(R.string.error_pile_not_found, isErrorText = true)
                )
            } catch (t: Throwable) {
                t.rethrowIfCoroutineCancellation()
                Result.Error(
                    MoneyPileError.Unknown,
                    UiText.StringResource(R.string.error_unknown, isErrorText = true)
                )
            }
        }
    }
}