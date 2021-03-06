package com.javamentor.qa.platform.controllers.question;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.database.rider.core.api.dataset.DataSet;
import com.javamentor.qa.platform.AbstractIntegrationTest;
import com.javamentor.qa.platform.models.dto.*;
import com.javamentor.qa.platform.models.entity.question.CommentQuestion;
import com.javamentor.qa.platform.models.entity.question.Question;
import com.javamentor.qa.platform.models.entity.question.QuestionViewed;
import com.javamentor.qa.platform.models.entity.question.answer.Answer;
import com.javamentor.qa.platform.models.entity.question.answer.VoteAnswer;
import com.javamentor.qa.platform.models.entity.user.User;
import com.javamentor.qa.platform.models.entity.user.reputation.Reputation;
import com.javamentor.qa.platform.service.abstracts.model.ReputationService;
import com.javamentor.qa.platform.webapp.converters.AnswerConverter;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigInteger;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@DataSet(value = {"dataset/question/roleQuestionApi.yml",
        "dataset/question/usersQuestionApi.yml",
        "dataset/question/answerQuestionApi.yml",
        "dataset/question/questionQuestionApi.yml",
        "dataset/question/tagQuestionApi.yml",
        "dataset/question/question_has_tagQuestionApi.yml",
        "dataset/question/votes_on_question.yml",
        "dataset/comment/comment.yml",
        "dataset/comment/comment_question.yml",
        "dataset/question/question_viewed.yml",},
        useSequenceFiltering = true, cleanBefore = true, cleanAfter = false)
@WithMockUser(username = "principal@mail.ru", roles = {"ADMIN", "USER"})
@ActiveProfiles("local")
class QuestionControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AnswerConverter answerConverter;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void getAllDto() throws Exception {
        System.out.println("test");
    }

    @Test
    public void shouldSetTagForQuestionOneTag() throws Exception {

        List<Long> tagId = new ArrayList<>();
        tagId.add(new Long(1L));
        String jsonRequest = objectMapper.writeValueAsString(tagId);
        this.mockMvc.perform(MockMvcRequestBuilders
                .patch("/api/question/13/tag/add")
                .content(jsonRequest)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(content().string("Tags were added"))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldSetTagForQuestionWrongId() throws Exception {

        List<Long> tagId = new ArrayList<>();
        tagId.add(new Long(1L));
        String jsonRequest = objectMapper.writeValueAsString(tagId);
        this.mockMvc.perform(MockMvcRequestBuilders
                .patch("/api/question/1111/tag/add")
                .content(jsonRequest)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(content().string("Question not found"))
                .andExpect(status().isBadRequest());
    }


    @Test
    public void shouldSetTagForQuestionFewTag() throws Exception {

        List<Long> tag = new ArrayList<>();
        tag.add(new Long(1L));
        tag.add(new Long(2L));
        tag.add(new Long(3L));
        String jsonRequest = objectMapper.writeValueAsString(tag);
        this.mockMvc.perform(MockMvcRequestBuilders
                .patch("/api/question/13/tag/add")
                .content(jsonRequest)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(content().string("Tags were added"))
                .andExpect(status().isOk());

    }

    @Test
    public void shouldSetTagForQuestionNoTag() throws Exception {

        List<Long> tag = new ArrayList<>();
        tag.add(new Long(11L));
        String jsonRequest = objectMapper.writeValueAsString(tag);
        this.mockMvc.perform(MockMvcRequestBuilders
                .patch("/api/question/13/tag/add")
                .content(jsonRequest)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(content().string("Tag not found"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturnErrorMessageBadParameterWrongSizeQuestionWithoutAnswer() throws Exception {
        mockMvc.perform(get("/api/question/order/new")
                .param("page", "1")
                .param("size", "0"))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentTypeCompatibleWith("text/plain;charset=UTF-8"))
                .andExpect(content().string("?????????? ???????????????? ?? ???????????? ???????????? ???????? ????????????????????????????. ???????????????????????? ???????????????????? ?????????????? ???? ???????????????? 100"));
    }

    @Test
    public void shouldReturnErrorMessageBadParameterWrongPageQuestionWithoutAnswer() throws Exception {
        mockMvc.perform(get("/api/question/order/new")
                .param("page", "0")
                .param("size", "2"))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentTypeCompatibleWith("text/plain;charset=UTF-8"))
                .andExpect(content().string("?????????? ???????????????? ?? ???????????? ???????????? ???????? ????????????????????????????. ???????????????????????? ???????????????????? ?????????????? ???? ???????????????? 100"));
    }

    @Test
    void shouldAddQuestionStatusOk() throws Exception {

        QuestionCreateDto questionCreateDto = new QuestionCreateDto();
        questionCreateDto.setUserId(2L);
        questionCreateDto.setTitle("Question number one1");
        questionCreateDto.setDescription("Question Description493");
        List<TagDto> listTagsAdd = new ArrayList<>();
        listTagsAdd.add(new TagDto(5L, "Structured Query Language", "description"));
        questionCreateDto.setTags(listTagsAdd);

        String jsonRequest = objectMapper.writeValueAsString(questionCreateDto);

        this.mockMvc.perform(MockMvcRequestBuilders
                .post("/api/question/add")
                .contentType("application/json;charset=UTF-8")
                .content(jsonRequest))
                .andDo(print())
                .andExpect(status().isOk());
    }


    @Test
    public void shouldAddQuestionAnswerStatusOk() throws Exception {
        QuestionCreateDto questionCreateDto = new QuestionCreateDto();
        questionCreateDto.setUserId(1L);
        questionCreateDto.setTitle("Question number one1");
        questionCreateDto.setDescription("Question Description493");
        List<TagDto> listTagsAdd = new ArrayList<>();
        listTagsAdd.add(new TagDto(5L, "Structured Query Language", "description"));
        questionCreateDto.setTags(listTagsAdd);

        String jsonRequest = objectMapper.writeValueAsString(questionCreateDto);

        this.mockMvc.perform(MockMvcRequestBuilders
                .post("/api/question/add")
                .contentType("application/json;charset=UTF-8")
                .content(jsonRequest))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Question number one1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description").value("Question Description493"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.authorId").value(1));
    }


    @Test
    public void shouldAddQuestionResponseBadRequestUserNotFound() throws Exception {
        QuestionCreateDto questionCreateDto = new QuestionCreateDto();
        questionCreateDto.setUserId(2222L);
        questionCreateDto.setTitle("Question number one1");
        questionCreateDto.setDescription("Question Description493");
        List<TagDto> listTagsAdd = new ArrayList<>();
        listTagsAdd.add(new TagDto(5L, "Structured Query Language", "description"));
        questionCreateDto.setTags(listTagsAdd);

        String jsonRequest = objectMapper.writeValueAsString(questionCreateDto);

        this.mockMvc.perform(MockMvcRequestBuilders
                .post("/api/question/add")
                .contentType("application/json;charset=UTF-8")
                .content(jsonRequest))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("questionCreateDto.userId dont`t exist"));
    }

    @Test
    public void shouldAddQuestionResponseBadRequestTagsNotExist() throws Exception {
        QuestionCreateDto questionCreateDto = new QuestionCreateDto();
        questionCreateDto.setUserId(1L);
        questionCreateDto.setTitle("Question number one1");
        questionCreateDto.setDescription("Question Description493");
        questionCreateDto.setTags(null);

        String jsonRequest = objectMapper.writeValueAsString(questionCreateDto);

        this.mockMvc.perform(MockMvcRequestBuilders
                .post("/api/question/add")
                .contentType("application/json;charset=UTF-8")
                .content(jsonRequest))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("addQuestion.questionCreateDto.tags: ???????????????? tags ???????????? ???????? ??????????????????"));
    }


    @Test
    void shouldAddAnswerToQuestionResponseBadRequestQuestionNotFound() throws Exception {

        CreateAnswerDto createAnswerDto = new CreateAnswerDto();
        createAnswerDto.setHtmlBody("test answer");

        String jsonRequest = objectMapper.writeValueAsString(createAnswerDto);

        this.mockMvc.perform(MockMvcRequestBuilders
                .post("/api/question/2222/answer")
                .contentType("application/json;charset=UTF-8")
                .content(jsonRequest))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Question was not found"));
    }

    @Test
    void shouldGetAnswersListFromQuestionStatusOk() throws Exception {

        this.mockMvc.perform(MockMvcRequestBuilders
                .get("/api/question/10/answer")
                .contentType("application/json;charset=UTF-8")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetAnswersListFromQuestionResponseStatusOk() throws Exception {

        String resultContext = mockMvc.perform(MockMvcRequestBuilders
                .get("/api/question/10/answer")
                .param("page", "1")
                .param("size", "10"))
                .andReturn().getResponse().getContentAsString();

        List<AnswerDto> answerDtoListFromResponse = objectMapper.readValue(resultContext, new TypeReference<List<AnswerDto>>() {
        });
        List<AnswerDto> answerList = (List<AnswerDto>) entityManager
                .createQuery("SELECT new com.javamentor.qa.platform.models.dto.AnswerDto(a.id, u.id, q.id, " +
                        "a.htmlBody, a.persistDateTime, a.isHelpful, a.dateAcceptTime, " +
                        "(SELECT COUNT(av.answer.id) FROM VoteAnswer AS av WHERE av.answer.id =a.id), " +
                        "u.imageLink, u.fullName) " +
                        "FROM Answer as a " +
                        "INNER JOIN a.user as u " +
                        "JOIN a.question as q " +
                        "WHERE q.id = :questionId")
                .setParameter("questionId", 10L)
                .getResultList();

        Assert.assertEquals(answerDtoListFromResponse, answerList);


    }

    @Test
    void shouldGetAnswersListFromQuestionResponseBadRequestQuestionNotFound() throws Exception {

        this.mockMvc.perform(MockMvcRequestBuilders
                .get("/api/question/2222/answer")
                .contentType("application/json;charset=UTF-8")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Question was not found"));
    }

    @Test
    public void shouldReturnQuestionsWithGivenTags() throws Exception {
        Long a[] = {1L, 3L, 5L};
        List<Long> tagIds = Arrays.stream(a).collect(Collectors.toList());
        String jsonRequest = objectMapper.writeValueAsString(tagIds);

        String resultContext = mockMvc.perform(MockMvcRequestBuilders.get("/api/question/withTags")
                .content(jsonRequest)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .param("page", "1")
                .param("size", "3")
                .param("tagIds", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.currentPageNumber").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalPageCount").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalResultCount").value(3))
                .andExpect(MockMvcResultMatchers.jsonPath("$.itemsOnPage").value(3))
                .andExpect(MockMvcResultMatchers.jsonPath("$.items").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        PageDto<LinkedHashMap, Object> actual = objectMapper.readValue(resultContext, PageDto.class);

        int numberOfItemsOnPage = actual.getItems().size();
        Assert.assertTrue(numberOfItemsOnPage == 3);
    }

    @Test
    public void getQuestionSearchWithStatusOk() throws Exception {
        QuestionSearchDto questionSearchDto = new QuestionSearchDto("sql query in excel");
        String json = objectMapper.writeValueAsString(questionSearchDto);

        this.mockMvc.perform(MockMvcRequestBuilders
                .get("/api/question/search")
                .param("page", "1")
                .param("size", "5")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void getQuestionSearchWithSearchMessageNull() throws Exception {
        QuestionSearchDto questionSearchDto = new QuestionSearchDto(null);
        String json = objectMapper.writeValueAsString(questionSearchDto);

        this.mockMvc.perform(MockMvcRequestBuilders
                .get("/api/question/search")
                .param("page", "1")
                .param("size", "5")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getQuestionSearchWithSearchMessageIsEmpty() throws Exception {
        QuestionSearchDto questionSearchDto = new QuestionSearchDto("");
        String json = objectMapper.writeValueAsString(questionSearchDto);

        this.mockMvc.perform(MockMvcRequestBuilders
                .get("/api/question/search")
                .param("page", "1")
                .param("size", "5")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getQuestionSearchWithSearchByAuthor() throws Exception {
        QuestionSearchDto questionSearchDto = new QuestionSearchDto("author:3");
        String json = objectMapper.writeValueAsString(questionSearchDto);

        PageDto<QuestionDto, Object> expect = new PageDto<>();
        expect.setTotalResultCount(2);
        expect.setItemsOnPage(1);
        expect.setCurrentPageNumber(1);
        expect.setTotalPageCount(2);
        expect.setItemsOnPage(1);
        expect.setMeta(null);

        List<TagDto> tag = new ArrayList<>();
        tag.add(new TagDto(5L, "sql", "Structured Query Language (SQL) is a language for querying databases."));

        List<QuestionDto> items = new ArrayList<>();
        items.add(new QuestionDto(
                18L,
                "Question number seven",
                3L, "Tot", null,
                "Changes made in sql query in excel reflects changes only on single sheet",
                1, 3, 1,
                LocalDateTime.of(2020, 01, 02, 00, 00, 00),
                LocalDateTime.of(2020, 05, 02, 13, 58, 56), tag));

        expect.setItems(items);

        String jsonResult = objectMapper.writeValueAsString(expect);

        this.mockMvc.perform(MockMvcRequestBuilders
                .get("/api/question/search")
                .param("page", "1")
                .param("size", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(jsonResult));
    }

    @Test
    public void shouldReturnErrorMessageBadParameterMaxPageQuestionWithoutAnswer() throws Exception {
        mockMvc.perform(get("/api/question/order/new")
                .param("page", "2")
                .param("size", "200"))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentTypeCompatibleWith("text/plain;charset=UTF-8"))
                .andExpect(content().string("?????????? ???????????????? ?? ???????????? ???????????? ???????? ????????????????????????????. ???????????????????????? ???????????????????? ?????????????? ???? ???????????????? 100"));
    }

    @Test
    public void testPaginationQuestionsWithoutAnswer() throws Exception {

        this.mockMvc.perform(get("/api/question/withoutAnswer")
                .param("page", "1")
                .param("size", "1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.currentPageNumber").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalPageCount").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalResultCount").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.items").isArray())
                .andDo(print())
                .andExpect(status().isOk());
    }


    @Test
    @DataSet(value = {"dataset/question/roleQuestionApi.yml",
            "dataset/question/usersQuestionApi.yml",
            "dataset/question/answerQuestionApi.yml",
            "dataset/question/questionQuestionApi.yml",
            "dataset/question/tagQuestionApi.yml",
            "dataset/question/question_has_tagQuestionApi.yml",
            "dataset/question/votes_on_question.yml"},
            useSequenceFiltering = true, cleanBefore = true, cleanAfter = true)
    public void testIsQuestionWithoutAnswers() throws Exception {

        LocalDateTime persistDateTime = LocalDateTime.of(LocalDate.of(2020, 1, 2), LocalTime.of(0, 0, 0));
        LocalDateTime lastUpdateDateTime = LocalDateTime.of(LocalDate.of(2020, 1, 2), LocalTime.of(13, 58, 56));

        PageDto<QuestionDto, Object> expectPage = new PageDto<>();
        expectPage.setCurrentPageNumber(1);
        expectPage.setItemsOnPage(1);
        expectPage.setTotalPageCount(2);
        expectPage.setTotalResultCount(2);

        List<TagDto> tagsList = new ArrayList<>();
        tagsList.add(new TagDto(1L, "java", "Java is a popular high-level programming language."));

        List<QuestionDto> itemsList = new ArrayList<>();
        itemsList.add(new QuestionDto(10L,
                "Question number one",
                1L,
                "Teat",
                null,
                "Stream filter on list keeping some of the filtered values",
                3,
                2,
                1,
                persistDateTime,
                lastUpdateDateTime,
                tagsList));

        expectPage.setItems(itemsList);

        String result = mockMvc.perform(get("/api/question/withoutAnswer")
                .param("page", "1")
                .param("size", "1")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PageDto<QuestionDto, Object> actualPage = objectMapper.readValue(result, PageDto.class);
        Assert.assertEquals(expectPage.toString(), actualPage.toString());
    }

    @Test
    public void shouldReturnQuestionsWithoutSpecifiedTags() throws Exception {

        List<Long> withoutTagIds = new ArrayList<>();
        withoutTagIds.add(1L);
        withoutTagIds.add(2L);
        withoutTagIds.add(5L);

        String jsonWithoutTagIds = objectMapper.writeValueAsString(withoutTagIds);

        this.mockMvc.perform(MockMvcRequestBuilders
                .post("/api/question/withoutTags")
                .param("page", "1")
                .param("size", "10")
                .contentType("application/json;charset=UTF-8")
                .content(jsonWithoutTagIds))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.currentPageNumber").value(1))
                .andExpect(jsonPath("$.totalPageCount").value(1))
                .andExpect(jsonPath("$.totalResultCount").value(1))
                .andExpect(jsonPath("$.items[*].listTagDto[*].id", Matchers.hasItem(3)))
                .andExpect(jsonPath("$.items[*].listTagDto[*].id", Matchers.not(1)))
                .andExpect(jsonPath("$.items[*].listTagDto[*].id", Matchers.not(2)))
                .andExpect(jsonPath("$.items[*].listTagDto[*].id", Matchers.not(5)))
                .andExpect(jsonPath("$.itemsOnPage").value(10));
    }

    @Test
    public void shouldReturnEmptyPaginationIfTagIdsMissing() throws Exception {

        List<Long> withoutTagIds = new ArrayList<>();

        String jsonWithoutTagIds = objectMapper.writeValueAsString(withoutTagIds);

        this.mockMvc.perform(MockMvcRequestBuilders
                .post("/api/question/withoutTags")
                .param("page", "1")
                .param("size", "10")
                .contentType("application/json;charset=UTF-8")
                .content(jsonWithoutTagIds))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.currentPageNumber").value(1))
                .andExpect(jsonPath("$.totalPageCount").value(0))
                .andExpect(jsonPath("$.totalResultCount").value(0))
                .andExpect(jsonPath("$.items").isEmpty())
                .andExpect(jsonPath("$.itemsOnPage").value(10));
    }


    @Test
    void voteUpStatusOk() throws Exception {

        List<VoteAnswer> before = entityManager.createNativeQuery("select * from votes_on_answers").getResultList();
        int first = before.size();

        this.mockMvc.perform(MockMvcRequestBuilders
                .patch("/api/question/10/answer/51/upVote")
                .contentType("application/json;charset=UTF-8"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.userId").isNumber())
                .andExpect(jsonPath("$.answerId").isNumber())
                .andExpect(jsonPath("$.persistDateTime").isNotEmpty())
                .andExpect(jsonPath("$.vote").isNumber());

        List<VoteAnswer> after = entityManager.createNativeQuery("select * from votes_on_answers").getResultList();
        int second = after.size();
        Assert.assertEquals(first + 1, second);
    }

    @Test
    void voteUpQuestionIsNotExist() throws Exception {

        this.mockMvc.perform(MockMvcRequestBuilders
                .patch("/api/question/1/answer/1/upVote")
                .contentType("application/json;charset=UTF-8"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("text/plain;charset=UTF-8"))
                .andExpect(content().string("Question was not found"));

    }

    @Test
    void voteUpAnswerIsNotExist() throws Exception {

        this.mockMvc.perform(MockMvcRequestBuilders
                .patch("/api/question/10/answer/4/upVote")
                .contentType("application/json;charset=UTF-8"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("text/plain;charset=UTF-8"))
                .andExpect(content().string("Answer was not found"));
    }

    @Test
    void voteDownStatusOk() throws Exception {

        List<VoteAnswer> before = entityManager.createNativeQuery("select * from votes_on_answers").getResultList();
        int first = before.size();

        this.mockMvc.perform(MockMvcRequestBuilders
                .patch("/api/question/1/answer/3/downVote")
                .contentType("application/json;charset=UTF-8"))
                .andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.userId").isNumber())
                .andExpect(jsonPath("$.answerId").isNumber())
                .andExpect(jsonPath("$.persistDateTime").isNotEmpty())
                .andExpect(jsonPath("$.vote").isNumber());

        List<VoteAnswer> after = entityManager.createNativeQuery("select * from votes_on_answers").getResultList();
        int second = after.size();
        Assert.assertEquals(first + 1, second);
    }

    @Test
    void voteDownQuestionIsNotExist() throws Exception {

        this.mockMvc.perform(MockMvcRequestBuilders
                .patch("/api/question/1/answer/1/downVote")
                .contentType("application/json;charset=UTF-8"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("text/plain;charset=UTF-8"))
                .andExpect(content().string("Question was not found"));

    }

    @Test
    void voteDownAnswerIsNotExist() throws Exception {

        this.mockMvc.perform(MockMvcRequestBuilders
                .patch("/api/question/10/answer/4/downVote")
                .contentType("application/json;charset=UTF-8"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("text/plain;charset=UTF-8"))
                .andExpect(content().string("Answer was not found"));
    }

    @Test
    public void shouldAddCommentToQuestionResponseBadRequestQuestionNotFound() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders
                .post("/api/question/9999/comment")
                .content("This is very good question!")
                .accept(MediaType.TEXT_PLAIN_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("text/plain;charset=UTF-8"))
                .andExpect(content().string("Question not found"));
    }

    @Test
    public void shouldAddCommentToQuestionResponseCommentDto() throws Exception {
        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders
                .post("/api/question/10/comment")
                .content("This is very good question!")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.text").value("This is very good question!"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.persistDate", org.hamcrest.Matchers.containsString(LocalDate.now().toString())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastRedactionDate", org.hamcrest.Matchers.containsString(LocalDate.now().toString())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.commentType").value("QUESTION"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.userId").value(153))
                .andExpect(MockMvcResultMatchers.jsonPath("$.username").value("Uou"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.reputation").value(2))
                .andReturn();

        JSONObject dto = new JSONObject(result.getResponse().getContentAsString());

        List<CommentQuestion> resultList = entityManager.createNativeQuery("select * from comment_question where comment_id = " + dto.get("id")).getResultList();
        Assert.assertFalse(resultList.isEmpty());
    }

    @Test
    void shouldAddSecondCommentToQuestionStatusBadRequest() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders
                .post("/api/question/15/comment")
                .content("Test comment1")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        this.mockMvc.perform(MockMvcRequestBuilders
                .post("/api/question/15/comment")
                .content("Test comment2")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getCommentListByQuestionIdWithStatusOk() throws Exception {

        //?????????????????? ????????????????????, ???????????????? ???????? CommentQuestionDto
        String resultContext = this.mockMvc.perform(get("/api/question/10/comments"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<CommentQuestionDto> commentQuestionDtoFromResponseList = objectMapper.readValue(resultContext, List.class);

        Assert.assertTrue(!commentQuestionDtoFromResponseList.isEmpty());

        //?????????????????????? ???? ???? ???????????????????? ???????????????????????? ?? ???????????????????? ??????????????
        Query queryToCommentQuestionTable = entityManager.createNativeQuery("select count(*) from comment_question where question_id = ?");
        queryToCommentQuestionTable.setParameter(1, 10);
        BigInteger count = (BigInteger) queryToCommentQuestionTable.getSingleResult();

        Assert.assertTrue(commentQuestionDtoFromResponseList.size() == count.intValue());
    }

    @Test
    public void getCommentListByQuestionIdWithStatusQuestionNotFound() throws Exception {

        Question question = null;
        try {
            question = entityManager
                    .createQuery("from Question where id = :id", Question.class)
                    .setParameter("id", 130L)
                    .getSingleResult();
        } catch (NoResultException nre) {
            //ignore
        }

        this.mockMvc.perform(get("/api/question/130/comments"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Question not found"));

        Assert.assertTrue(question == null);
    }

    @Test
    public void shouldCreateVoteQuestionUp() throws Exception {
        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders
                .post("/api/question/19/upVote")).andReturn();


        JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());

        Assert.assertEquals(jsonObject.get("vote"), 1);
    }

    @Test
    public void shouldCreateVoteQuestionDown() throws Exception {

        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders
                .post("/api/question/19/downVote"))

                .andReturn();

        JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());

        Assert.assertEquals(jsonObject.get("vote"), -1);
    }

    @Test
    public void AddQuestionAsViewedStatusOk() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders
                .post("/api/question/14/view"))
                .andExpect(status().isOk());
    }

    @Test
    public void AddQuestionAsViewedIfSecondRequest() throws Exception {

        //?????????????? ?????????????????? ???????????? ?? ????
        Query queryBefore = entityManager.createNativeQuery("select * from question_viewed where user_id = 153");
        List<QuestionViewed> before = queryBefore.getResultList();
        int countBefore = before.size();

        //?????????????????? ?????? ???????? ????????????????????
        this.mockMvc.perform(MockMvcRequestBuilders
                .post("/api/question/14/view"))
                .andExpect(status().isOk());

        //?????????????? ???????????????? ?? ???????????????????? ???????????????????? ??????????????
        Query queryAfter = entityManager.createNativeQuery("select * from question_viewed where user_id = 153");
        Assert.assertEquals(countBefore + 1, queryAfter.getResultList().size());

    }

    @Test
    public void AddQuestionAsViewedIfSecondEqualRequest() throws Exception {

        //???????????? ???????????? ???????????????????? ????????????
        this.mockMvc.perform(MockMvcRequestBuilders
                .post("/api/question/15/view"))
                .andExpect(status().isOk());
        Query query = entityManager.createNativeQuery("select * from question_viewed where user_id = 153 and " +
                "question_id = ?", QuestionViewed.class);
        query.setParameter(1, 15L);
        //?????????????????? ?????????????????? ????????????
        QuestionViewed questionViewedFirst = (QuestionViewed) query.getSingleResult();

        //?????????????? ???????????????????? ?????????????? ?? ????
        Query queryBefore = entityManager.createNativeQuery("select * from question_viewed where user_id = 153",
                QuestionViewed.class);
        List<QuestionViewed> before = queryBefore.getResultList();
        int countBefore = before.size();

        //???????????? ???????????? ????????????????
        this.mockMvc.perform(MockMvcRequestBuilders
                .post("/api/question/10/view"))
                .andExpect(status().isOk());
        //?????????????? ???????????????????? ?????????????? ???????????????? ?? ????????????????????
        Query queryDoubleRequestAfter = entityManager.createNativeQuery("select * from question_viewed where " +
                "user_id = 153", QuestionViewed.class);
        int countAfter = queryDoubleRequestAfter.getResultList().size();
        Assert.assertEquals(countBefore, countAfter);

        //?????????????????? ???????????????????? ???? ???????????? ?????????? ?????????????? ???? ???????????????????? ?????????????????
        Query query2 = entityManager.createNativeQuery("select * from question_viewed where user_id = 153 and " +
                "question_id = ?", QuestionViewed.class);
        query2.setParameter(1, 15L);
        QuestionViewed questionViewedSecond = (QuestionViewed) query2.getSingleResult();
        Assert.assertEquals(questionViewedFirst.getId(), questionViewedSecond.getId());
        Assert.assertEquals(questionViewedFirst.getLocalDateTime(), questionViewedSecond.getLocalDateTime());
        Assert.assertEquals(questionViewedFirst.getUser().getId(), questionViewedSecond.getUser().getId());
        Assert.assertEquals(questionViewedFirst.getQuestion().getId(), questionViewedSecond.getQuestion().getId());

    }

    @Test
    public void AddQuestionAsViewedIsNotExist() throws Exception {

        this.mockMvc.perform(MockMvcRequestBuilders
                .post("/api/question/21/view"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("text/plain;charset=UTF-8"))
                .andExpect(content().string("Question not found"));
    }

    @Test
    void getQuestionDtoWithoutVotes() throws Exception {
        String resultContext = this.mockMvc.perform(get("/api/question/13"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        QuestionDto questionDto = objectMapper.readValue(resultContext, QuestionDto.class);

        Assertions.assertEquals(0, questionDto.getCountValuable());
    }

    @Test
    public void shouldAddPositiveReputationByQuestionVoteUp() throws Exception {
        try {
            String string = "FROM Reputation WHERE question.id =: questionId AND sender.id =: senderId";

            Query queryBefore = entityManager.createQuery(string, Reputation.class);
            queryBefore.setParameter("questionId", 19L);
            queryBefore.setParameter("senderId", 153L);

            Reputation reputationBefore = (Reputation) queryBefore.getSingleResult();

            MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders
                    .post("/api/question/19/upVote")).andReturn();

            Query queryAfter = entityManager.createQuery(string, Reputation.class);
            queryAfter.setParameter("questionId", 19L);
            queryAfter.setParameter("senderId", 153L);

            Reputation reputationAfter = (Reputation) queryAfter.getSingleResult();

            int votePoints = reputationAfter.getCount();
            long lastSenderId = reputationAfter.getId();

            Assert.assertTrue(reputationBefore != reputationAfter);
            Assert.assertEquals(lastSenderId, 153L);
            Assert.assertEquals(votePoints, 20);

        } catch (NoResultException ignore) {

        }
    }

    @Test
    public void shouldAddNegativeReputationByQuestionVoteDown() throws Exception {
        try {
            String string = "FROM Reputation WHERE question.id =: questionId AND sender.id =: senderId";

            Query queryBefore = entityManager.createQuery(string, Reputation.class);
            queryBefore.setParameter("questionId", 19L);
            queryBefore.setParameter("senderId", 153L);

            Reputation reputationBefore = (Reputation) queryBefore.getSingleResult();

            MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders
                    .post("/api/question/19/downVote")).andReturn();

            Query queryAfter = entityManager.createQuery(string, Reputation.class);
            queryAfter.setParameter("questionId", 19L);
            queryAfter.setParameter("senderId", 153L);

            Reputation reputationAfter = (Reputation) queryAfter.getSingleResult();

            int votePoints = reputationAfter.getCount();
            long lastSenderId = reputationAfter.getId();

            Assert.assertTrue(reputationBefore != reputationAfter);
            Assert.assertEquals(lastSenderId, 153L);
            Assert.assertEquals(votePoints, -20);

        } catch (NoResultException ignore) {

        }
    }
}