// File: milktea-backend/src/main/java/com/milktea/app/common/util/PaginationUtil.java
package com.milktea.app.common.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PaginationUtil {

    public static Pageable createPageable(Integer page, Integer limit, String sortProperty, Sort.Direction direction) {
        int pageNumber = (page != null && page > 0) ? page - 1 : 0;
        int pageSize = (limit != null && limit > 0) ? limit : 10; // Default limit to 10

        if (sortProperty != null && !sortProperty.isEmpty()) {
            return PageRequest.of(pageNumber, pageSize, Sort.by(direction, sortProperty));
        } else {
            return PageRequest.of(pageNumber, pageSize);
        }
    }

    public static Pageable createPageable(Integer page, Integer limit) {
        return createPageable(page, limit, null, null);
    }
}