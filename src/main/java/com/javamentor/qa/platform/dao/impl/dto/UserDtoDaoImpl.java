package com.javamentor.qa.platform.dao.impl.dto;

import com.javamentor.qa.platform.dao.abstracts.dto.UserDtoDao;
import com.javamentor.qa.platform.dao.util.SingleResultUtil;
import com.javamentor.qa.platform.models.dto.TagDto;
import com.javamentor.qa.platform.models.dto.UserDto;
import com.javamentor.qa.platform.models.dto.UserDtoList;
import org.hibernate.Session;
import org.hibernate.transform.ResultTransformer;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.*;

@Repository
public class UserDtoDaoImpl implements UserDtoDao {

    @PersistenceContext
    private final EntityManager entityManager;

    public UserDtoDaoImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Optional<UserDto> getUserById(long id) {
        TypedQuery<UserDto> q = entityManager.createQuery(
                "select new com.javamentor.qa.platform.models.dto.UserDto(u.id, u.email, u.fullName," +
                        "u.imageLink, u.reputationCount)  " +
                        "from User u where u.id = :userId", UserDto.class)
                .setParameter("userId", id);
        return SingleResultUtil.getSingleResultOrNull(q);
    }

    @Override
    public int getTotalResultCountUsers() {
        return ((Number) entityManager.createQuery("select count(user) from User user").getSingleResult()).intValue();
    }

    @Override
    public int getCountUsersByName(String name) {
        return ((Number) entityManager.createQuery("select count(u) from User u WHERE lower(u.fullName) LIKE lower('%" + name + "%') ").getSingleResult()).intValue();
    }

    @Override
    public List<UserDtoList> getPageUserDtoListByReputationOverPeriodWithoutTags(int page, int size, int quantityOfDay) {

        return entityManager.unwrap(Session.class)
                .createQuery("select new com.javamentor.qa.platform.models.dto.UserDtoList" +
                        "(u.id, u.fullName, u.imageLink, " +
                        "(select coalesce( sum(ra.count),0) from Reputation ra where current_date-" +
                        "(:quantityOfDays)<date(ra.persistDate) and ra.user.id=u.id)) from User u" +
                        " left outer join Reputation r on u.id=r.user.id " +
                        "group by u.id order by sum(r.count) desc NULLS LAST, u.id")
                .setParameter("quantityOfDays", quantityOfDay)
                .unwrap(org.hibernate.query.Query.class)
                .setFirstResult(page * size - size)
                .setMaxResults(size)
                .getResultList();
    }

    @Override
    public List<UserDtoList> getListTagDtoWithTagsPeriodWithOnlyTags(List<Long> usersIds, int quantityOfDay) {
        return entityManager.unwrap(Session.class)
                .createQuery("   select u.id as user_id,t.name as tag_name,t.id as tag_id  " +
                        "from User u left join Question q on u.id=q.user.id " +
                        " join q.tags t left join Answer a on a.question.id=q.id" +
                        " where q.user.id in (:ids) and  current_date-(:quantityOfDays)<date(q.persistDateTime) or" +
                        " a.user.id  in (:ids) and  current_date-(:quantityOfDays)<date(a.persistDateTime) " +
                        " group by t,u.id order by count(t.id) desc,t.id")
                .setParameterList("ids", usersIds)
                .setParameter("quantityOfDays", quantityOfDay)
                .unwrap(org.hibernate.query.Query.class)
                .setResultTransformer(new UserDtoListTranformer())
                .getResultList();
    }

    @Override
    public List<UserDtoList> getPageUserDtoListByReputationWithoutTags(int page, int size) {
        return entityManager.unwrap(Session.class)
                .createQuery("select new com.javamentor.qa.platform.models.dto.UserDtoList" +
                        "(u.id, u.fullName, u.imageLink, " +
                        "(select coalesce(sum(ra.count), 0) from Reputation ra where ra.user.id = u.id)) " +
                        "from User u left outer join Reputation r on u.id = r.user.id " +
                        "group by u.id order by sum(r.count) desc NULLS LAST, u.id")
                .unwrap(org.hibernate.query.Query.class)
                .setFirstResult(page * size - size)
                .setMaxResults(size)
                .getResultList();
    }

    @Override
    public List<UserDtoList> getListTagDtoWithTagsWithOnlyTags(List<Long> usersIds) {
        return entityManager.unwrap(Session.class)
                .createQuery("select u.id as user_id, t.name as tag_name, t.id as tag_id " +
                        "from User u left join Question q on u.id = q.user.id " +
                        "join q.tags t left join Answer a on a.question.id = q.id " +
                        "where q.user.id in (:ids) or a.user.id  in (:ids) " +
                        "group by t,u.id order by count(t.id) desc,t.id")
                .setParameterList("ids", usersIds)
                .unwrap(org.hibernate.query.Query.class)
                .setResultTransformer(new UserDtoListTranformer())
                .getResultList();
    }

    @Override
    public List<UserDtoList> getPageUserDtoListByNameWithoutTags(int page, int size, String name) {

        return entityManager.unwrap(Session.class)
                .createQuery("select new com.javamentor.qa.platform.models.dto.UserDtoList" +
                        "(u.id, u.fullName, u.imageLink, u.reputationCount) from User u " +
                        " WHERE lower(u.fullName) LIKE lower('%" + name + "%') " + "order by u.id")
                .unwrap(org.hibernate.query.Query.class)
                .setFirstResult(page * size - size)
                .setMaxResults(size)
                .getResultList();

    }

    @Override
    public List<UserDtoList> getListTagDtoByUserNameWithOnlyTags(List<Long> usersIds, String name) {

        return entityManager.unwrap(Session.class)
                .createQuery("select u.id as user_id, t.name as tag_name, t.id as tag_id " +
                        " from User u left join Question q on u.id=q.user.id " +
                        " join q.tags t left join Answer a on a.question.id=q.id" +
                        " where q.user.id in (:ids) or a.user.id  in (:ids) " +
                        " group by t, u.id order by count(t.id) desc, t.id")
                .setParameterList("ids", usersIds)
                .unwrap(org.hibernate.query.Query.class)
                .setResultTransformer(new UserDtoListTranformer())
                .getResultList();

    }

    class UserDtoListTranformer implements ResultTransformer {

        private Map<Long, UserDtoList> tagDtoWithCountDtoMap = new LinkedHashMap<>();

        @Override
        public Object transformTuple(Object[] tuple, String[] aliases) {

            Map<String, Integer> aliasToIndexMap = aliasToIndexMap(aliases);
            Long userId = ((Number) tuple[0]).longValue();

            UserDtoList userDtoList = tagDtoWithCountDtoMap.computeIfAbsent(
                    userId,
                    id1 -> {
                        UserDtoList userDtoListTemp = new UserDtoList();
                        userDtoListTemp.setId(((Number) tuple[aliasToIndexMap.get("user_id")]).longValue());
                        userDtoListTemp.setTags(new ArrayList<>());

                        return userDtoListTemp;
                    }
            );

            userDtoList.getTags().add(
                    new TagDto(
                            ((Number) tuple[aliasToIndexMap.get("tag_id")]).longValue(),
                            ((String) tuple[aliasToIndexMap.get("tag_name")])
                    )
            );

            return userDtoList;
        }

        @Override
        public List transformList(List list) {
            return new ArrayList<>(tagDtoWithCountDtoMap.values());
        }

        public Map<String, Integer> aliasToIndexMap(
                String[] aliases) {

            Map<String, Integer> aliasToIndexMap = new LinkedHashMap<>();

            for (int i = 0; i < aliases.length; i++) {
                aliasToIndexMap.put(aliases[i], i);
            }
            return aliasToIndexMap;
        }
    }
}
