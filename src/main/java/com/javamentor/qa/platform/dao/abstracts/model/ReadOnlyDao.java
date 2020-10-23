package com.javamentor.qa.platform.dao.abstracts.model;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReadOnlyDao<E, K> {
    List<E> getAll();

    boolean existsById(K id);

    Optional<E> getById(K id);

    List<E> getAllByIds(Iterable<K> ids);

    Optional<E> getUserByEmail(String email);

    boolean existsByAllIds(Collection<K> ids);

    Optional<E> getRoleByName(String name);
}
