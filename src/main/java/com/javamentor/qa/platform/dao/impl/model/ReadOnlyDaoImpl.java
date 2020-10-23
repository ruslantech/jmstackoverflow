package com.javamentor.qa.platform.dao.impl.model;

import com.javamentor.qa.platform.dao.util.SingleResultUtil;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public abstract class ReadOnlyDaoImpl<E, K> {
    @PersistenceContext
    private EntityManager entityManager;

    public List<E> getAll() {
        Class<E> clazz = (Class<E>) ((ParameterizedType) getClass().getGenericSuperclass())
                .getActualTypeArguments()[0];
        return entityManager.createQuery("from " + clazz.getName()).getResultList();
    }

    public boolean existsById(K id) {
        Class<E> clazz = (Class<E>) ((ParameterizedType) getClass().getGenericSuperclass())
                .getActualTypeArguments()[0];
        long count = (long) entityManager.createQuery("SELECT COUNT(e) FROM " + clazz.getName() + " e WHERE e.id =: id").setParameter("id", id).getSingleResult();
        return count > 0;
    }

    public Optional<E> getById(K id) {
        Class<E> clazz = (Class<E>) ((ParameterizedType) getClass().getGenericSuperclass())
                .getActualTypeArguments()[0];
        String hql = "FROM " + clazz.getName() + " WHERE id = :id";
        TypedQuery<E> query = (TypedQuery<E>) entityManager.createQuery(hql).setParameter("id", id);
        return SingleResultUtil.getSingleResultOrNull(query);
    }

    public Optional<E> getUserByEmail(String email) {
        Class<E> eClass = (Class<E>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        String hql = "FROM " + eClass.getName() + " WHERE email = :email";
        TypedQuery<E> query = (TypedQuery<E>) entityManager.createQuery(hql).setParameter("email", email);
        return SingleResultUtil.getSingleResultOrNull(query);
    }

    public Optional<E> getRoleByName(String name) {
        Class<E> eClass = (Class<E>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        String hql = "FROM " + eClass.getName() + " WHERE name = :name";
        TypedQuery<E> query = (TypedQuery<E>) entityManager.createQuery(hql).setParameter("name", name);
        return SingleResultUtil.getSingleResultOrNull(query);
    }

    public List<E> getAllByIds(Iterable<K> ids) {
        if (ids != null && ids.iterator().hasNext()) {
            Class<E> clazz = (Class<E>) ((ParameterizedType) getClass().getGenericSuperclass())
                    .getActualTypeArguments()[0];
            return entityManager.createQuery("from " + clazz.getName() + " e WHERE e.id IN :ids")
                    .setParameter("ids", ids).getResultList();
        } else {
            return new ArrayList<>();
        }
    }

    public boolean existsByAllIds(Collection<K> ids) {
        if (ids != null && ids.size() > 0) {
            Class<E> clazz = (Class<E>) ((ParameterizedType) getClass().getGenericSuperclass())
                    .getActualTypeArguments()[0];
            Long count = (Long) entityManager.createQuery("select count(*) from" + clazz.getName() + " e WHERE e.id IN :ids")
                    .setParameter("ids", ids).getSingleResult();
            return ids.size() == count;
        }
        return false;
    }
}
