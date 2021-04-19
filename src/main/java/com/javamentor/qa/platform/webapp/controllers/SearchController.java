package com.javamentor.qa.platform.webapp.controllers;

import com.javamentor.qa.platform.models.dto.FoundEntryDto;
import com.javamentor.qa.platform.models.dto.PageDto;
import com.javamentor.qa.platform.models.entity.question.Question;
import com.javamentor.qa.platform.service.abstracts.model.SearchQuestionService;
import com.javamentor.qa.platform.service.impl.dto.SearchDtoServiceImpl;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Validated
@RequestMapping("/api/search")
@Api(value = "SearchApi")
public class SearchController {

    private SearchDtoServiceImpl searchDtoService;

    private static final int MAX_ITEMS_ON_PAGE = 100;

    @Autowired
    public void setSearchDtoService(SearchDtoServiceImpl searchDtoService) {
        this.searchDtoService = searchDtoService;
    }

    @GetMapping(path = "/")
    @ApiOperation(value = "Search Question")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Returns the List<Question>"),
    })
    public ResponseEntity<?> getAllSingleChatPagination(
            @ApiParam(name = "query", value = "Search query", required = true, example = "spring injection")
            @RequestParam("query") String query,
            @ApiParam(name = "page", value = "Number Page. type int", required = true, example = "1")
            @RequestParam("page") int page,
            @ApiParam(name = "size", value = "Number of entries per page.Type int." +
                    " Максимальное количество записей на странице " + MAX_ITEMS_ON_PAGE,
                    example = "10")
            @RequestParam("size") int size,
            @ApiParam(name = "sort", value = "Sort search results by", required = false, example = "votesCount")
            @RequestParam(value = "sort", required = false, defaultValue = "score") String sort,
            @ApiParam(name = "order", value = "Order search results by", required = false, example = "asc")
            @RequestParam(value = "order", required = false, defaultValue = "desc") String order
    ) {

        if (page <= 0 || size <= 0 || size > MAX_ITEMS_ON_PAGE) {
            return ResponseEntity.badRequest().body("Номер страницы и размер должны быть " +
                    "положительными. Максимальное количество записей на странице " + MAX_ITEMS_ON_PAGE);
        }

        PageDto<FoundEntryDto, Object> allFoundEntries = searchDtoService.getSearchDtoPagination(query, page, size, sort, order);


        return ResponseEntity.ok(allFoundEntries);
    }
}
