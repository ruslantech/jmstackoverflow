package com.javamentor.qa.platform.controllers.tag;

import com.github.database.rider.core.api.dataset.DataSet;
import com.javamentor.qa.platform.AbstractIntegrationTest;
import com.javamentor.qa.platform.models.dto.PageDto;
import com.javamentor.qa.platform.models.dto.TagDto;
import com.javamentor.qa.platform.models.dto.TagListDto;
import com.javamentor.qa.platform.models.dto.TagRecentDto;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@DataSet(value = {"dataset/question/roleQuestionApi.yml",
        "dataset/question/usersQuestionApi.yml",
        "dataset/question/questionQuestionApi.yml",
        "dataset/question/tagQuestionApi.yml",
        "dataset/question/question_has_tagQuestionApi.yml"}
        , cleanBefore = true, cleanAfter = true)
public class TagControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String POPULAR = "/api/tag/popular";
    private static final String RECENT = "/api/tag/recent";
    private static final String ALPHABET = "/api/tag/alphabet/order";
    private static final String ORDER_POPULAR = "/api/tag/order/popular";
    private static final String NAME = "/api/tag/name";
    private static final String BAD_REQUEST_MESSAGE = "Номер страницы и размер должны быть положительными. Максимальное количество записей на странице 100";


    // Тесты запросов популярных тэгов
    @Test
    public void requestGetTagDtoPaginationByPopular() throws Exception {
        PageDto<TagDto, Object> expected = new PageDto<>();
        expected.setCurrentPageNumber(1);
        expected.setTotalPageCount(1);
        expected.setTotalResultCount(5);
        expected.setItemsOnPage(10);

        List<TagDto> expectedItems = new ArrayList<>();
        expectedItems.add(new TagDto(1L, "java"));
        expectedItems.add(new TagDto(5L, "sql"));
        expectedItems.add(new TagDto(2L, "javaScript"));
        expectedItems.add(new TagDto(3L, "html"));
        expectedItems.add(new TagDto(4L, "bootstrap-4"));
        expected.setItems(expectedItems);

        String resultContext = mockMvc.perform(get(POPULAR)
                .param("page", "1")
                .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.currentPageNumber").isNotEmpty())
                .andExpect(jsonPath("$.totalPageCount").isNotEmpty())
                .andExpect(jsonPath("$.totalResultCount").isNotEmpty())
                .andExpect(jsonPath("$.items").isNotEmpty())
                .andExpect(jsonPath("$.itemsOnPage").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        PageDto<TagDto, Object> actual = objectMapper.readValue(resultContext, PageDto.class);
        Assert.assertEquals(expected.toString(), actual.toString());
    }

    @Test
    public void requestNegativePageGetTagDtoPaginationByPopular() throws Exception {
        mockMvc.perform(get(POPULAR)
                .param("page", "-1")
                .param("size", "10"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("text/plain;charset=UTF-8"))
                .andExpect(content().string(BAD_REQUEST_MESSAGE));
    }

    @Test
    public void requestNegativeSizeGetTagDtoPaginationByPopular() throws Exception {
        mockMvc.perform(get(POPULAR)
                .param("page", "1")
                .param("size", "0"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("text/plain;charset=UTF-8"))
                .andExpect(content().string(BAD_REQUEST_MESSAGE));
    }

    @Test
    public void requestIncorrectSizeGetTagDtoPaginationByPopular() throws Exception {
        mockMvc.perform(get(POPULAR)
                .param("page", "1")
                .param("size", "101"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("text/plain;charset=UTF-8"))
                .andExpect(content().string(BAD_REQUEST_MESSAGE));
    }

    @Test
    public void requestPageDontExistsGetTagDtoPaginationByPopular() throws Exception {
        mockMvc.perform(get(POPULAR)
                .param("page", "13")
                .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.currentPageNumber").isNotEmpty())
                .andExpect(jsonPath("$.totalPageCount").isNotEmpty())
                .andExpect(jsonPath("$.totalResultCount").isNotEmpty())
                .andExpect(jsonPath("$.items").isEmpty());
    }


    // Тесты запросов недавних тэгов
    @Test
    public void requestGetTagRecentDtoPagination() throws Exception {
        PageDto<TagRecentDto, Object> expected = new PageDto<>();
        expected.setCurrentPageNumber(1);
        expected.setTotalPageCount(1);
        expected.setTotalResultCount(5);
        expected.setItemsOnPage(10);

        List<TagRecentDto> expectedItems = new ArrayList<>();
        expectedItems.add(new TagRecentDto(1L, "java", 3));
        expectedItems.add(new TagRecentDto(5L, "sql", 3));
        expectedItems.add(new TagRecentDto(2L, "javaScript", 2));
        expectedItems.add(new TagRecentDto(3L, "html", 1));
        expectedItems.add(new TagRecentDto(4L, "bootstrap-4", 0));
        expected.setItems(expectedItems);

        String resultContext = mockMvc.perform(get(RECENT)
                .param("page", "1")
                .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.currentPageNumber").isNotEmpty())
                .andExpect(jsonPath("$.totalPageCount").isNotEmpty())
                .andExpect(jsonPath("$.totalResultCount").isNotEmpty())
                .andExpect(jsonPath("$.items").isNotEmpty())
                .andExpect(jsonPath("$.itemsOnPage").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        PageDto<TagRecentDto, Object> actual = objectMapper.readValue(resultContext, PageDto.class);
        Assert.assertEquals(expected.toString(), actual.toString());
    }

    @Test
    public void requestNegativePageGetTagRecentDtoPagination() throws Exception {
        mockMvc.perform(get(RECENT)
                .param("page", "-1")
                .param("size", "10"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("text/plain;charset=UTF-8"))
                .andExpect(content().string(BAD_REQUEST_MESSAGE));
    }

    @Test
    public void requestNegativeSizeGetTagRecentDtoPagination() throws Exception {
        mockMvc.perform(get(RECENT)
                .param("page", "1")
                .param("size", "0"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("text/plain;charset=UTF-8"))
                .andExpect(content().string(BAD_REQUEST_MESSAGE));
    }

    @Test
    public void requestIncorrectSizeGetTagRecentDtoPagination() throws Exception {
        mockMvc.perform(get(RECENT)
                .param("page", "1")
                .param("size", "101"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("text/plain;charset=UTF-8"))
                .andExpect(content().string(BAD_REQUEST_MESSAGE));
    }

    @Test
    public void requestPageDontExistsGetTagRecentDtoPagination() throws Exception {
        mockMvc.perform(get(RECENT)
                .param("page", "13")
                .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.currentPageNumber").isNotEmpty())
                .andExpect(jsonPath("$.totalPageCount").isNotEmpty())
                .andExpect(jsonPath("$.totalResultCount").isNotEmpty())
                .andExpect(jsonPath("$.items").isEmpty());
    }


    // Тесты запросов тэгов по алфавиту
    @Test
    public void requestGetTagDtoPaginationOrderByAlphabet() throws Exception {
        PageDto<TagListDto, Object> expected = new PageDto<>();
        expected.setCurrentPageNumber(1);
        expected.setTotalPageCount(1);
        expected.setTotalResultCount(5);
        expected.setItemsOnPage(10);

        List<TagListDto> expectedItems = new ArrayList<>();
        expectedItems.add(new TagListDto(4L, "bootstrap-4", "Bootstrap 4 is the fourth major version of the popular front-end component library. The Bootstrap framework aids in the creation of responsive, mobile-first websites and web apps.", 0, 0, 0));
        expectedItems.add(new TagListDto(3L, "html", "HTML (HyperText Markup Language) is the markup language for creating web pages and other information to be displayed in a web browser.", 1, 1, 0));
        expectedItems.add(new TagListDto(1L, "java", "Java is a popular high-level programming language.", 3, 0, 0));
        expectedItems.add(new TagListDto(2L, "javaScript", "For questions regarding programming in ECMAScript (JavaScript/JS) and its various dialects/implementations (excluding ActionScript).", 2, 1, 0));
        expectedItems.add(new TagListDto(5L, "sql", "Structured Query Language (SQL) is a language for querying databases.", 3, 3, 3));
        expected.setItems(expectedItems);

        String resultContext = mockMvc.perform(get(ALPHABET)
                .param("page", "1")
                .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.currentPageNumber").isNotEmpty())
                .andExpect(jsonPath("$.totalPageCount").isNotEmpty())
                .andExpect(jsonPath("$.totalResultCount").isNotEmpty())
                .andExpect(jsonPath("$.items").isNotEmpty())
                .andExpect(jsonPath("$.itemsOnPage").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        PageDto<TagListDto, Object> actual = objectMapper.readValue(resultContext, PageDto.class);
        Assert.assertEquals(expected.toString(), actual.toString());
    }

    @Test
    public void requestNegativePageGetTagDtoPaginationOrderByAlphabet() throws Exception {
        mockMvc.perform(get(ALPHABET)
                .param("page", "-1")
                .param("size", "10"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("text/plain;charset=UTF-8"))
                .andExpect(content().string(BAD_REQUEST_MESSAGE));
    }

    @Test
    public void requestNegativeSizeGetTagDtoPaginationOrderByAlphabet() throws Exception {
        mockMvc.perform(get(ALPHABET)
                .param("page", "1")
                .param("size", "0"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("text/plain;charset=UTF-8"))
                .andExpect(content().string(BAD_REQUEST_MESSAGE));
    }

    @Test
    public void requestIncorrectSizeGetTagDtoPaginationOrderByAlphabet() throws Exception {
        mockMvc.perform(get(ALPHABET)
                .param("page", "1")
                .param("size", "101"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("text/plain;charset=UTF-8"))
                .andExpect(content().string(BAD_REQUEST_MESSAGE));
    }

    @Test
    public void requestPageDontExistsGetTagDtoPaginationOrderByAlphabet() throws Exception {
        mockMvc.perform(get(ALPHABET)
                .param("page", "13")
                .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.currentPageNumber").isNotEmpty())
                .andExpect(jsonPath("$.totalPageCount").isNotEmpty())
                .andExpect(jsonPath("$.totalResultCount").isNotEmpty())
                .andExpect(jsonPath("$.items").isEmpty());
    }


    // Тест order popular
    @Test
    public void requestGetTagListDtoByPopularPagination() throws Exception {
        PageDto<TagListDto, Object> expected = new PageDto<>();
        expected.setCurrentPageNumber(1);
        expected.setTotalPageCount(1);
        expected.setTotalResultCount(5);
        expected.setItemsOnPage(10);

        List<TagListDto> expectedItems = new ArrayList<>();
        expectedItems.add(new TagListDto(1L, "java", "Java is a popular high-level programming language.", 3, 0, 0));
        expectedItems.add(new TagListDto(5L, "sql", "Structured Query Language (SQL) is a language for querying databases.", 3, 3, 3));
        expectedItems.add(new TagListDto(2L, "javaScript", "For questions regarding programming in ECMAScript (JavaScript/JS) and its various dialects/implementations (excluding ActionScript).", 2, 1, 0));
        expectedItems.add(new TagListDto(3L, "html", "HTML (HyperText Markup Language) is the markup language for creating web pages and other information to be displayed in a web browser.", 1, 1, 0));
        expectedItems.add(new TagListDto(4L, "bootstrap-4", "Bootstrap 4 is the fourth major version of the popular front-end component library. The Bootstrap framework aids in the creation of responsive, mobile-first websites and web apps.", 0, 0, 0));
        expected.setItems(expectedItems);

        String resultContext = mockMvc.perform(get(ORDER_POPULAR)
                .param("page", "1")
                .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.currentPageNumber").isNotEmpty())
                .andExpect(jsonPath("$.totalPageCount").isNotEmpty())
                .andExpect(jsonPath("$.totalResultCount").isNotEmpty())
                .andExpect(jsonPath("$.items").isNotEmpty())
                .andExpect(jsonPath("$.itemsOnPage").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        PageDto<TagListDto, Object> actual = objectMapper.readValue(resultContext, PageDto.class);
        Assert.assertEquals(expected.toString(), actual.toString());
    }

    @Test
    public void requestNegativePageGetTagListDtoByPopularPagination() throws Exception {
        mockMvc.perform(get(ORDER_POPULAR)
                .param("page", "-1")
                .param("size", "10"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("text/plain;charset=UTF-8"))
                .andExpect(content().string(BAD_REQUEST_MESSAGE));
    }

    @Test
    public void requestNegativeSizeGetTagListDtoByPopularPagination() throws Exception {
        mockMvc.perform(get(ORDER_POPULAR)
                .param("page", "1")
                .param("size", "0"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("text/plain;charset=UTF-8"))
                .andExpect(content().string(BAD_REQUEST_MESSAGE));
    }

    @Test
    public void requestIncorrectSizeGetTagListDtoByPopularPagination() throws Exception {
        mockMvc.perform(get(ORDER_POPULAR)
                .param("page", "1")
                .param("size", "101"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("text/plain;charset=UTF-8"))
                .andExpect(content().string(BAD_REQUEST_MESSAGE));
    }

    @Test
    public void requestPageDontExistsGetTagListDtoByPopularPagination() throws Exception {
        mockMvc.perform(get(ORDER_POPULAR)
                .param("page", "13")
                .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.currentPageNumber").isNotEmpty())
                .andExpect(jsonPath("$.totalPageCount").isNotEmpty())
                .andExpect(jsonPath("$.totalResultCount").isNotEmpty())
                .andExpect(jsonPath("$.items").isEmpty());
    }


    // Тест name
    @Test
    public void requestGetTagName() throws Exception {
        PageDto<TagListDto, Object> expected = new PageDto<>();
        expected.setCurrentPageNumber(1);
        expected.setTotalPageCount(1);
        expected.setTotalResultCount(2);
        expected.setItemsOnPage(10);

        List<TagListDto> expectedItems = new ArrayList<>();
        expectedItems.add(new TagListDto(1L, "java", "Java is a popular high-level programming language.", 3, 0, 0));
        expectedItems.add(new TagListDto(2L, "javaScript", "For questions regarding programming in ECMAScript (JavaScript/JS) and its various dialects/implementations (excluding ActionScript).", 2, 1, 0));
        expected.setItems(expectedItems);

        String resultContext = mockMvc.perform(get(NAME)
                .param("name", "java")
                .param("page", "1")
                .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.currentPageNumber").isNotEmpty())
                .andExpect(jsonPath("$.totalPageCount").isNotEmpty())
                .andExpect(jsonPath("$.totalResultCount").isNotEmpty())
                .andExpect(jsonPath("$.items").isNotEmpty())
                .andExpect(jsonPath("$.itemsOnPage").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        PageDto<TagListDto, Object> actual = objectMapper.readValue(resultContext, PageDto.class);
        Assert.assertEquals(expected.toString(), actual.toString());
    }

    @Test
    public void requestNegativePageGetTagName() throws Exception {
        mockMvc.perform(get(NAME)
                .param("name", "java")
                .param("page", "-1")
                .param("size", "10"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("text/plain;charset=UTF-8"))
                .andExpect(content().string(BAD_REQUEST_MESSAGE));
    }

    @Test
    public void requestNegativeSizeGetTagName() throws Exception {
        mockMvc.perform(get(NAME)
                .param("name", "java")
                .param("page", "1")
                .param("size", "0"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("text/plain;charset=UTF-8"))
                .andExpect(content().string(BAD_REQUEST_MESSAGE));
    }

    @Test
    public void requestIncorrectSizeGetTagName() throws Exception {
        mockMvc.perform(get(NAME)
                .param("name", "java")
                .param("page", "1")
                .param("size", "101"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("text/plain;charset=UTF-8"))
                .andExpect(content().string(BAD_REQUEST_MESSAGE));
    }

    @Test
    public void requestPageDontExistsGetTagName() throws Exception {
        mockMvc.perform(get(NAME)
                .param("name", "java")
                .param("page", "13")
                .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.currentPageNumber").isNotEmpty())
                .andExpect(jsonPath("$.totalPageCount").isNotEmpty())
                .andExpect(jsonPath("$.totalResultCount").isNotEmpty())
                .andExpect(jsonPath("$.items").isEmpty());
    }

}