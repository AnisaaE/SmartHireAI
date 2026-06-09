package com.smart_hire.document.api;

import java.util.List;

@FunctionalInterface
interface ViewMapper<SOURCE, TARGET> {

    TARGET map(SOURCE source);

    default List<TARGET> mapAll(Iterable<SOURCE> sourceItems) {
        List<TARGET> mappedItems = new java.util.ArrayList<>();
        for (SOURCE sourceItem : sourceItems) {
            mappedItems.add(map(sourceItem));
        }
        return List.copyOf(mappedItems);
    }
}
