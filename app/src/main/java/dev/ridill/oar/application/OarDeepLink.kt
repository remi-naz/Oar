package dev.ridill.oar.application

import android.content.Context
import android.content.Intent
import androidx.navigation3.runtime.NavKey
import dev.ridill.oar.core.ui.navigation.AddEditTransactionRoute
import dev.ridill.oar.core.ui.navigation.AllMoneyPilesRoute
import dev.ridill.oar.core.ui.navigation.AllSchedulesRoute
import dev.ridill.oar.core.ui.navigation.BackupSettingsRoute
import dev.ridill.oar.core.ui.navigation.INVALID_ID_LONG
import dev.ridill.oar.core.ui.navigation.MoneyPileDetailsRoute

/**
 * Encodes/decodes the target [NavKey] for the app's own notifications as typed Intent extras.
 * Internal-only: there is no URI scheme or manifest intent-filter, since nothing external
 * launches these intents.
 */
object OarDeepLink {
    private const val EXTRA_TARGET = "dev.ridill.oar.EXTRA_DEEPLINK_TARGET"
    private const val EXTRA_TRANSACTION_ID = "dev.ridill.oar.EXTRA_DEEPLINK_TX_ID"

    private const val EXTRA_PILE_ID = "dev.ridill.oar.EXTRA_DEEPLINK_PILE_ID"

    private const val TARGET_ADD_EDIT_TRANSACTION = "add_edit_transaction"
    private const val TARGET_ALL_SCHEDULES = "all_schedules"
    private const val TARGET_BACKUP_SETTINGS = "backup_settings"
    private const val TARGET_MONEY_PILE_DETAILS = "money_pile_details"
    private const val TARGET_ALL_MONEY_PILES = "all_money_piles"

    fun addEditTransactionIntent(
        context: Context,
        transactionId: Long
    ): Intent = base(context, TARGET_ADD_EDIT_TRANSACTION)
        .putExtra(EXTRA_TRANSACTION_ID, transactionId)

    fun allSchedulesIntent(context: Context): Intent = base(context, TARGET_ALL_SCHEDULES)

    fun backupSettingsIntent(context: Context): Intent = base(context, TARGET_BACKUP_SETTINGS)

    fun moneyPileDetailsIntent(context: Context, pileId: Long?): Intent =
        if (pileId != null) {
            base(context, TARGET_MONEY_PILE_DETAILS).putExtra(EXTRA_PILE_ID, pileId)
        } else {
            base(context, TARGET_ALL_MONEY_PILES)
        }

    private fun base(
        context: Context,
        target: String
    ): Intent = Intent(
        context,
        OarActivity::class.java
    ).putExtra(EXTRA_TARGET, target)

    /** Resolve the target [NavKey] encoded in [intent], or null if it carries no deep link. */
    fun resolve(intent: Intent?): NavKey? = when (intent?.getStringExtra(EXTRA_TARGET)) {
        TARGET_ADD_EDIT_TRANSACTION -> AddEditTransactionRoute(
            transactionId = intent.getLongExtra(EXTRA_TRANSACTION_ID, INVALID_ID_LONG)
        )

        TARGET_ALL_SCHEDULES -> AllSchedulesRoute
        TARGET_BACKUP_SETTINGS -> BackupSettingsRoute
        TARGET_MONEY_PILE_DETAILS -> MoneyPileDetailsRoute(
            pileId = intent.getLongExtra(EXTRA_PILE_ID, INVALID_ID_LONG)
        )

        TARGET_ALL_MONEY_PILES -> AllMoneyPilesRoute
        else -> null
    }
}
