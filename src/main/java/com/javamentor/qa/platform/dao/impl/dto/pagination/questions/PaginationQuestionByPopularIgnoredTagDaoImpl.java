package com.javamentor.qa.platform.dao.impl.dto.pagination.questions;

import com.javamentor.qa.platform.dao.abstracts.dto.pagination.PaginationDao;
import com.javamentor.qa.platform.dao.impl.dto.transformers.QuestionResultTransformer;
import com.javamentor.qa.platform.models.dto.QuestionDto;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository(value = "paginationQuestionByPopularIgnoredTag")
@SuppressWarnings(value = "unchecked")
public class PaginationQuestionByPopularIgnoredTagDaoImpl implements PaginationDao<QuestionDto> {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<QuestionDto> getItems(Map<String, Object> parameters) {

        List<Long> questionIds = (List<Long>) parameters.get("ids");

        return (List<QuestionDto>) em.unwrap(Session.class)
                .createQuery("select question.id as question_id, " +
                        " question.title as question_title," +
                        "u.fullName as question_authorName," +
                        "u.id as question_authorId, " +
                        "u.imageLink as question_authorImage," +
                        "question.description as question_description, " +
                        "COUNT (qv.question.id) AS question_viewCount, " +
                        "(select count(a.question.id) from Answer a where a.question.id=question_id) as question_countAnswer," +
                        "(select count(v.question.id) from VoteQuestion v where v.question.id=question_id) as question_countValuable," +
                        "question.persistDateTime as question_persistDateTime," +
                        "question.lastUpdateDateTime as question_lastUpdateDateTime, " +
                        " tag.id as tag_id,tag.name as tag_name, tag.description as tag_description " +
                        "from Question question  " +
                        "left join QuestionViewed qv on question.id = qv.question.id " +
                        "INNER JOIN  question.user u " +
                        "join question.tags tag " +
                        "where question.id IN :ids ORDER BY question_viewCount DESC")
                .setParameter("ids", questionIds)
                .unwrap(Query.class)
                .setResultTransformer(new QuestionResultTransformer())
                .getResultList();
    }

    @Override
    public int getCount(Map<String, Object> parameters) {
        long id = (long) parameters.get("id");
        return (int) (long) em.createQuery(
                "select count(q.id) " +
                        "from Question q " +
                        "join  q.tags tag " +
                        "join IgnoredTag ignoredTag on tag.id=ignoredTag.ignoredTag.id " +
                        "inner join User user on user.id=ignoredTag.user.id " +
                        "where  user.id in :id " /*and q.viewCount is not null*/)
                .setParameter("id", id)
                .getSingleResult();
    }
}
