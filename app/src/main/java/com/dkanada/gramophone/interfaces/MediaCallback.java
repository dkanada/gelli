package com.dkanada.gramophone.interfaces;

import java.util.List;

public interface MediaCallback<T> {
    void onLoadMedia(List<T> media);
}
