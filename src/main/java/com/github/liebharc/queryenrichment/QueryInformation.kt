package com.github.liebharc.queryenrichment

/**
 * Provides information about the query to steps.
 */
class QueryInformation(request: Request) {

    val hasReference: Boolean = request.attributes.any { atr -> atr.property == Attribute.reference }
}