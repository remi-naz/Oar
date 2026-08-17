package dev.ridill.oar.settings.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import dev.ridill.oar.core.data.db.BaseDao
import dev.ridill.oar.settings.data.local.entity.CurrencyListEntity

@Dao
interface CurrencyListDao : BaseDao<CurrencyListEntity> {
    @Query(
        """
        SELECT currency_code
        FROM currency_list_table
        WHERE (currency_code LIKE :query || '%') OR (display_name LIKE '%' || :query || '%')
        ORDER BY display_name ASC, currency_code ASC
    """
    )
    fun getAllCurrencyCodesPaged(query: String): PagingSource<Int, String>

    @Query(
        """
        SELECT NOT EXISTS(
            SELECT currency_code FROM currency_list_table LIMIT 1
        )
    """
    )
    suspend fun isTableEmpty(): Boolean
}