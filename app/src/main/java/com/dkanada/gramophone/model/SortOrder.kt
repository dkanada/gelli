package com.dkanada.gramophone.model

import org.jellyfin.apiclient.model.entities.SortOrder as Order

enum class SortOrder(val api: Order) {
    ASCENDING(Order.Ascending),
    DESCENDING(Order.Descending);
}
