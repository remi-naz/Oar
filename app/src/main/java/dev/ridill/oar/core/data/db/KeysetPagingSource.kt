package dev.ridill.oar.core.data.db

import androidx.paging.PagingSource
import androidx.paging.PagingState
import dev.ridill.oar.core.domain.util.logE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Shared keyset-pagination `load()`/invalidation boilerplate for [PagingSource]s backed by a
 * [KeysetPagedQuery]. Subclasses provide only the feature-specific fetch and key-derivation
 * logic; [resolveLoadSize]/[onLoaded] are open solely to support bounded-list subclasses that cap
 * total rows returned across pages.
 */
abstract class KeysetPagingSource<Key : Any, Value : Any>(
    private val db: OarDatabase,
    applicationScope: CoroutineScope,
    invalidationTables: Set<String>
) : PagingSource<Key, Value>() {

    private val invalidationJob: Job = applicationScope.launch {
        db.invalidationTracker
            .createFlow(*invalidationTables.toTypedArray(), emitInitialState = false)
            .collect { invalidate() }
    }

    init {
        registerInvalidatedCallback { invalidationJob.cancel() }
    }

    /** Fetch up to [loadSize] rows starting from [cursor] in [direction], in display order. */
    protected abstract suspend fun fetch(
        cursor: Key?,
        direction: PageLoadDirection,
        loadSize: Int
    ): List<Value>

    /** Derive the cursor [Key] a row would be resumed from as a page boundary. */
    protected abstract fun keyOf(value: Value): Key

    /** Override to cap total rows returned across pages; defaults to the requested size. */
    protected open fun resolveLoadSize(requested: Int): Int = requested

    /** Called after each successful fetch with the row count, for bounded-list bookkeeping. */
    protected open fun onLoaded(rowCount: Int) {}

    /**
     * Resolves direction from [params], delegates the actual row fetch to [fetch], and reverses
     * prepended rows back to display order (they're fetched in reverse so [fetch] can reuse the
     * same "rows after the cursor" query shape for both directions).
     */
    final override suspend fun load(params: LoadParams<Key>): LoadResult<Key, Value> = try {
        val loadSize = resolveLoadSize(params.loadSize)
        if (loadSize <= 0) return LoadResult.Page(
            data = emptyList(),
            prevKey = null,
            nextKey = null
        )

        val direction = if (params is LoadParams.Prepend) PageLoadDirection.BACKWARD
        else PageLoadDirection.FORWARD

        val rows = fetch(params.key, direction, loadSize)
            .let { if (params is LoadParams.Prepend) it.reversed() else it }
        onLoaded(rows.size)

        LoadResult.Page(
            data = rows,
            prevKey = rows.firstOrNull()?.let(::keyOf),
            nextKey = rows.lastOrNull()?.let(::keyOf)?.takeIf { rows.size == loadSize }
        )
    } catch (e: Exception) {
        logE(e, KeysetPagingSource::class.simpleName) { "load()" }
        LoadResult.Error(e)
    }

    /** Always refresh from the start; keyset cursors aren't meaningful as a mid-list anchor. */
    override fun getRefreshKey(state: PagingState<Key, Value>): Key? = null
}
