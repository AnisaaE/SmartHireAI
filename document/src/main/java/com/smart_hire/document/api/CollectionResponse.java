package com.smart_hire.document.api;

import java.util.List;

record CollectionResponse<T>(
        List<T> items,
        int totalCount
) {
}
