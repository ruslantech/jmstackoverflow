package com.javamentor.qa.platform.dao.impl.model;


import java.util.Collection;
import java.util.List;
import java.util.Optional;

public abstract class ReadOnlyDaoImpl<E, K> {

    public List<E> getAll() {
        return null;
    }

    public boolean existsById(K id) {
        return false;
    }

    public Optional<E> getById(K id) {
        return null;
    }

    public List<E> getAllByIds(Iterable<K> ids) {
        return null;
    }

    public boolean existsByAllIds(Collection<K> ids) {
        return false;
    }
}
