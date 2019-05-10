package com.github.liebharc.queryenrichment

object FilterUtils {

    fun getSqlCriteria(criteria: List<QueryFilter>): String {
        val result = StringBuilder()
        var isFirst = true
        for (criterion in criteria) {

            if (!isFirst) {
                result.append(" and ")
            }

            result.append(criterion.toPlaceHolderString())
            isFirst = false
        }

        return result.toString()
    }
}
