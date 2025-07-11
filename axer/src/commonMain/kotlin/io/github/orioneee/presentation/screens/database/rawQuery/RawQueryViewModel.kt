package io.github.orioneee.presentation.screens.database.rawQuery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.orioneee.domain.database.QueryResponse
import io.github.orioneee.domain.database.SchemaItem
import io.github.orioneee.domain.database.SortColumn
import io.github.orioneee.processors.RoomReader
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
internal class RawQueryViewModel(
    private val name: String,
) : ViewModel() {
    private val reader = RoomReader()

    private val _currentQuery = MutableStateFlow("")
    val currentQuery = _currentQuery

    private val _queryResponse = MutableStateFlow<QueryResponse>(
        QueryResponse(
            rows = emptyList(),
            schema = emptyList()
        )
    )
    val queryResponse = _queryResponse.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asSharedFlow()

    private val _sortColumn = MutableStateFlow<SortColumn?>(null)
    val sortColumn = _sortColumn.asStateFlow()

    fun setQuery(query: String) {
        _currentQuery.tryEmit(query)
    }

    fun executeQuery() {
        viewModelScope.launch {
            _isLoading.value = true
            _queryResponse.value = QueryResponse(
                rows = emptyList(),
                schema = emptyList()
            )
            try {
                val response = reader.executeRawQuery(name, _currentQuery.value)
                _queryResponse.value = response
            } catch (e: Exception) {
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onClickSortColumn(
        schemaItem: SchemaItem
    ) {
        val currentSort = _sortColumn.value
        if (currentSort != null && currentSort.schemaItem.name == schemaItem.name) {
            _sortColumn.value = currentSort.copy(isDescending = !currentSort.isDescending)
        } else {
            val currentSchema = _queryResponse.value.schema
            val index = currentSchema.indexOfFirst { it.name == schemaItem.name }
            _sortColumn.value = SortColumn(
                index = index,
                schemaItem = schemaItem,
                isDescending = true
            )
        }
    }

    init {
        reader.axerDriver.changeDataFlow
            .debounce(100)
            .onEach {
                if(_currentQuery.value.startsWith("SELECT", ignoreCase = true)) {
                    executeQuery()
                }
            }
            .launchIn(viewModelScope)
    }

}