package dev.ridill.oar.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dev.ridill.oar.core.data.db.FtsQueryFormatter
import dev.ridill.oar.core.data.db.OarDatabase
import dev.ridill.oar.core.domain.util.EventBus
import dev.ridill.oar.tags.data.local.TagPagedQueryBuilder
import dev.ridill.oar.tags.data.local.TagTrigramDao
import dev.ridill.oar.tags.data.local.TagsDao
import dev.ridill.oar.tags.data.repository.TagsRepositoryImpl
import dev.ridill.oar.tags.domain.repository.TagsRepository
import dev.ridill.oar.tags.domain.util.EditDistance
import dev.ridill.oar.tags.domain.util.FuzzyTagScorer
import dev.ridill.oar.tags.domain.util.TextNormalizer
import dev.ridill.oar.tags.domain.util.TrigramGenerator
import dev.ridill.oar.tags.presentation.addEditTag.AddEditTagViewModel
import dev.ridill.oar.tags.presentation.tagSelection.TagSelectionViewModel
import kotlinx.coroutines.CoroutineScope

@Module
@InstallIn(ViewModelComponent::class)
object TagModule {

    @Provides
    fun provideTagsDao(db: OarDatabase): TagsDao = db.tagsDao()

    @Provides
    fun provideTagTrigramDao(db: OarDatabase): TagTrigramDao = db.tagTrigramDao()

    @Provides
    fun provideTagPagedQueryBuilder(
        formatter: FtsQueryFormatter
    ): TagPagedQueryBuilder = TagPagedQueryBuilder(formatter)

    @Provides
    fun provideTextNormalizer(): TextNormalizer = TextNormalizer()

    @Provides
    fun provideTrigramGenerator(textNormalizer: TextNormalizer): TrigramGenerator =
        TrigramGenerator(textNormalizer)

    @Provides
    fun provideEditDistance(): EditDistance = EditDistance()

    @Provides
    fun provideFuzzyTagScorer(editDistance: EditDistance): FuzzyTagScorer =
        FuzzyTagScorer(editDistance)

    @Provides
    fun provideTagsRepository(
        dao: TagsDao,
        trigramDao: TagTrigramDao,
        queryBuilder: TagPagedQueryBuilder,
        textNormalizer: TextNormalizer,
        trigramGenerator: TrigramGenerator,
        fuzzyTagScorer: FuzzyTagScorer,
        db: OarDatabase,
        @ApplicationScope applicationScope: CoroutineScope
    ): TagsRepository = TagsRepositoryImpl(
        dao = dao,
        trigramDao = trigramDao,
        queryBuilder = queryBuilder,
        textNormalizer = textNormalizer,
        trigramGenerator = trigramGenerator,
        fuzzyTagScorer = fuzzyTagScorer,
        db = db,
        applicationScope = applicationScope
    )

    @Provides
    fun provideAddEditTagEventBus(): EventBus<AddEditTagViewModel.AddEditTagEvent> = EventBus()

    @Provides
    fun provideTagSelectionEventBus(): EventBus<TagSelectionViewModel.TagSelectionEvent> =
        EventBus()
}