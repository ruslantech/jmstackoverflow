package com.javamentor.qa.platform.service.abstracts.dto;

import com.javamentor.qa.platform.models.dto.PageDto;
import com.javamentor.qa.platform.models.dto.QuestionDto;
import java.util.Optional;

public interface QuestionDtoService {
     Optional<QuestionDto> getQuestionDtoById(Long id);

     PageDto<QuestionDto, Object> getPagination(int page, int size);

     PageDto<QuestionDto, Object> getPaginationPopular(int page, int size);

     PageDto<QuestionDto, Object> getPaginationWithoutAnswers (int page, int size);

    PageDto<QuestionDto, Object> getPaginationOrderedNew(int page, int size);
}
