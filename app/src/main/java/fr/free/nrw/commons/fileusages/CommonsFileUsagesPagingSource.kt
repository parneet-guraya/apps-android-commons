package fr.free.nrw.commons.fileusages

import androidx.paging.PagingSource
import androidx.paging.PagingState
import fr.free.nrw.commons.mwapi.OkHttpJsonApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CommonsFileUsagesPagingSource @Inject constructor(
    private val okHttpJsonApiClient: OkHttpJsonApiClient
) : PagingSource<String, FileUsage>() {

    lateinit var fileName: String


    override fun getRefreshKey(state: PagingState<String, FileUsage>): String? {
        return null
    }

    override suspend fun load(params: LoadParams<String>): LoadResult<String, FileUsage> {
        return withContext(Dispatchers.IO) {
            try {
                val continueKey: String? = params.key

                val response = okHttpJsonApiClient.getFileUsagesOnCommons(fileName, continueKey)
                    .blockingFirst()

                val isContinued = response.continueResponse != null

                val nextKey = if (isContinued) {
                    response.continueResponse!!.fuContinue
                } else {
                    null
                }
                // this comes null if there are no contributions to show for a file
                //TODO: handle this scenario
                val data = response.query.pages.first().fileUsage

                if (data == null) {
                    throw IllegalStateException("No contributions for this file")
                } else {
                    LoadResult.Page(
                        data = data,
                        prevKey = null,
                        nextKey = nextKey
                    )
                }

            } catch (e: Throwable) {
                println("error ${e.message}")
                LoadResult.Error(e)
            }
        }
    }
}

