package io.github.orioneee.domain.database

internal data class RowItem (
    val schema: List<SchemaItem>,
    val cells: List<RoomCell?>
)