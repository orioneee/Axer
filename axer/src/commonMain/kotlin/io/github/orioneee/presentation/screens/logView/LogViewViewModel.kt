package io.github.orioneee.presentation.screens.logView

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.aakira.napier.LogLevel
import io.github.orioneee.koin.IsolatedContext
import io.github.orioneee.room.dao.LogsDAO
import io.github.orioneee.utils.DataExporter
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

internal class LogViewViewModel : ViewModel() {
    private val dao: LogsDAO by IsolatedContext.koin.inject()

    private val _selectedTags = MutableStateFlow<List<String>>(listOf())
    private val _selectedLevels = MutableStateFlow<List<LogLevel>>(listOf())
    private val _isExporting = MutableStateFlow(false)
    private val _firstExportPointId = MutableStateFlow<Long?>(null)
    private val _lastExportPointId = MutableStateFlow<Long?>(null)

    val selectedTags = _selectedTags.asStateFlow()
    val selectedLevels = _selectedLevels.asStateFlow()
    val isExporting = _isExporting.asStateFlow()
    val firstExportPointId = _firstExportPointId.asStateFlow()
    val lastExportPointId = _lastExportPointId.asStateFlow()


    @OptIn(FlowPreview::class)
    val logs = dao.getAll().debounce(100)
    val filtredLogs = combine(
        logs,
        _selectedTags,
        _selectedLevels
    ) { logs, selectedTags, selectedLevels ->
        logs.filter { log ->
            (selectedTags.isEmpty() || log.tag in selectedTags) &&
                    (selectedLevels.isEmpty() || log.level in selectedLevels)
        }
    }
    val selectedForExport = combine(
        filtredLogs,
        _firstExportPointId,
        _lastExportPointId
    ) { logs, firstExportPointId, lastExportPointId ->
        if (firstExportPointId == null || lastExportPointId == null) return@combine emptyList()
        val isPresentFirst = logs.any { it.id == firstExportPointId }
        val isPresentLast = logs.any { it.id == lastExportPointId }
        if (!isPresentFirst) {
            _firstExportPointId.value = null
        }
        if (!isPresentLast) {
            _lastExportPointId.value = null
        }
        if (!isPresentFirst || !isPresentLast) return@combine emptyList()
        val indexOfFirst = logs.indexOfFirst { it.id == firstExportPointId }.takeIf {
            it >= 0
        } ?: return@combine emptyList()
        val indexOfLast = logs.indexOfLast { it.id == lastExportPointId }.takeIf {
            it >= 0
        } ?: return@combine emptyList()
        logs.subList(
            fromIndex = min(indexOfFirst, indexOfLast),
            toIndex = (max(indexOfFirst, indexOfLast) + 1).coerceAtMost(logs.size)
        )
    }
    val tags = dao.getUniqueTags()
    val levels = dao.getUniqueLevels()

    fun toggleTag(tag: String) {
        _selectedTags.value = if (_selectedTags.value.contains(tag)) {
            _selectedTags.value - tag
        } else {
            _selectedTags.value + tag
        }
    }

    fun toggleLevel(level: LogLevel) {
        _selectedLevels.value = if (_selectedLevels.value.contains(level)) {
            _selectedLevels.value - level
        } else {
            _selectedLevels.value + level
        }
    }

    fun clearTags() {
        _selectedTags.value = listOf()
    }

    fun clearLevels() {
        _selectedLevels.value = listOf()
    }

    fun clear() {
        viewModelScope.launch {
            dao.clear()
        }
    }

    fun onClickExport() {
        _isExporting.value = !_isExporting.value
        if (!_isExporting.value) {
            _firstExportPointId.value = null
            _lastExportPointId.value = null
        }
    }

    fun onSelectPoint(id: Long) {
        if (_firstExportPointId.value == null) {
            _firstExportPointId.value = id
        } else if (_lastExportPointId.value == null) {
            _lastExportPointId.value = id
        } else {
            _firstExportPointId.value = id
            _lastExportPointId.value = null
        }
    }

    fun onExport() {
        viewModelScope.launch {
            try {
                val logs = selectedForExport.first()
                DataExporter.exportLogs(logs)
            } catch (e: Exception) {
                // Handle export error, e.g., show a message to the user
            } finally {
                _isExporting.value = false
                _firstExportPointId.value = null
                _lastExportPointId.value = null
            }
        }
    }
}