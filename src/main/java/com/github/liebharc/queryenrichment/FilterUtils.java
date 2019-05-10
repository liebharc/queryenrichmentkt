package com.github.liebharc.queryenrichment;

import java.util.List;

public class FilterUtils {

    public static String getSqlCriteria(List<QueryFilter> criteria) {
        final StringBuilder result = new StringBuilder();
        boolean isFirst = true;
        for (QueryFilter criterion : criteria) {

            if (!isFirst) {
                result.append(" and ");
            }

            result.append(criterion.toPlaceHolderString());
            isFirst = false;
        }

        return result.toString();
    }

    private FilterUtils() {

    }
}
