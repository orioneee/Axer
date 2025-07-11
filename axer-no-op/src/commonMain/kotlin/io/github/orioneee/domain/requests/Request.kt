package io.github.orioneee.domain.requests

data class Request(
    val method: String,
    val sendTime: Long,
    val host: String,
    val path: String,
    val body: String?,
    val headers: Map<String, String>,
)