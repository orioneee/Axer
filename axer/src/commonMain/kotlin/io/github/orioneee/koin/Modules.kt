package io.github.orioneee.koin

import io.github.orioneee.presentation.screens.database.TableDetailsViewModel
import io.github.orioneee.presentation.screens.database.allQueries.AllQueriesViewModel
import io.github.orioneee.presentation.screens.database.rawQuery.RawQueryViewModel
import io.github.orioneee.presentation.screens.database.tableList.ListDatabaseViewModel
import io.github.orioneee.presentation.screens.exceptions.ExceptionsViewModel
import io.github.orioneee.presentation.screens.logView.LogViewViewModel
import io.github.orioneee.presentation.screens.requests.RequestViewModel
import io.github.orioneee.room.AxerDatabase
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

internal object Modules {
    fun getModules() = listOf(
        getPlatformModules(),
        daoModule,
        viewModelModule,
    )

    val daoModule = module {
        single {
            val database: AxerDatabase = get()
            database.getRequestDao()
        }
        single {
            val database: AxerDatabase = get()
            database.getAxerExceptionDao()
        }
        single {
            val database: AxerDatabase = get()
            database.getLogsDao()
        }
    }

    val viewModelModule = module {
        viewModel { (requestId: Long?) ->
            RequestViewModel(
                requestDao = get(),
                requestId = requestId
            )
        }
        viewModel { (exceptionID: Long?) ->
            ExceptionsViewModel(
                exceptionDao = get(),
                exceptionID = exceptionID
            )
        }
        viewModel { (file: String, tableName: String) ->
            TableDetailsViewModel(file, tableName)
        }
        viewModel { (file: String) ->
            RawQueryViewModel(file)
        }
        viewModel {
            AllQueriesViewModel()
        }
        viewModel {
            LogViewViewModel()
        }
        viewModel {
            ListDatabaseViewModel()
        }
    }
}

expect fun getPlatformModules(): Module