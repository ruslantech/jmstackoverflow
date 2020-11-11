package com.javamentor.qa.platform.controllers.user;

import com.github.database.rider.core.api.dataset.DataSet;
import com.javamentor.qa.platform.AbstractIntegrationTest;
import com.javamentor.qa.platform.models.dto.PageDto;
import com.javamentor.qa.platform.models.dto.TagDto;
import com.javamentor.qa.platform.models.dto.UserDtoList;
import com.javamentor.qa.platform.models.dto.UserRegistrationDto;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class UserControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DataSet(value = "dataset/user/userUserApi.yml", disableConstraints = true, cleanBefore = true, cleanAfter = true)
    public void shouldGetUserByName() throws Exception {
        PageDto<UserDtoList, Object> expected = new PageDto<>();
        expected.setCurrentPageNumber(1);
        expected.setTotalPageCount(1);
        expected.setTotalResultCount(3);
        expected.setItemsOnPage(10);

        List<UserDtoList> expectedItems = new ArrayList<>();

        expectedItems.add(new UserDtoList(1L, "Teat", "linkImage1", 2, Arrays.asList(new TagDto[]{})));
        expectedItems.add(new UserDtoList(2L, "Teat", "linkImage2", 1, Arrays.asList(new TagDto[]{})));
        expectedItems.add(new UserDtoList(4L, "Tob", "linkImage4", 4, Arrays.asList(new TagDto[]{})));
        expected.setItems(expectedItems);

        String resultContext = mockMvc.perform(get("/api/user/find")
                .param("name", "t")
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

        PageDto<UserDtoList, Object> actual = objectMapper.readValue(resultContext, PageDto.class);
        Assert.assertEquals(expected.toString(), actual.toString());
    }

    @Test
    @DataSet(value = "dataset/user/userUserApi.yml", disableConstraints = true, cleanBefore = true, cleanAfter = true)
    public void inabilityGetUserByNameWithWrongName() throws Exception {
        this.mockMvc.perform(get("/api/user/find")
                .param("name", "c")
                .param("page", "1")
                .param("size", "10"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("text/plain;charset=UTF-8"))
                .andExpect(content().string("User with this name does not exist"));
    }

    @Test
    @DataSet(value = "dataset/user/userUserApi.yml", disableConstraints = true, cleanBefore = true, cleanAfter = true)
    public void inabilityGetUserByNameWithNegativePage() throws Exception {
        this.mockMvc.perform(get("/api/user/find")
                .param("name", "t")
                .param("page", "-1")
                .param("size", "10"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("text/plain;charset=UTF-8"))
                .andExpect(content().string("The page number and size must be positive. Maximum number of records per page 100"));
    }

    @Test
    @DataSet(value = "dataset/user/userUserApi.yml", disableConstraints = true, cleanBefore = true, cleanAfter = true)
    public void inabilityGetUserByNameWithZeroSize() throws Exception {
        this.mockMvc.perform(get("/api/user/find")
                .param("name", "t")
                .param("page", "1")
                .param("size", "0"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("text/plain;charset=UTF-8"))
                .andExpect(content().string("The page number and size must be positive. Maximum number of records per page 100"));
    }

    @Test
    @DataSet(value = "dataset/user/userUserApi.yml", disableConstraints = true, cleanBefore = true, cleanAfter = true)
    public void inabilityGetUserByNameWithNegativeSize() throws Exception {
        this.mockMvc.perform(get("/api/user/find")
                .param("name", "t")
                .param("page", "1")
                .param("size", "-1"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("text/plain;charset=UTF-8"))
                .andExpect(content().string("The page number and size must be positive. Maximum number of records per page 100"));
    }

    @Test
    @DataSet(value = "dataset/user/userUserApi.yml", disableConstraints = true, cleanBefore = true, cleanAfter = true)
    public void inabilityGetUserByNameOnPageNotExists() throws Exception {
        this.mockMvc.perform(get("/api/user/find")
                .param("name", "t")
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