package com.g5.fokotoai.service;

import com.g5.fokotoai.dto.LearningProgressResponse;
import com.g5.fokotoai.dto.WeakVocabResponse;
import com.g5.fokotoai.entity.UserWordMetric;
import com.g5.fokotoai.entity.Vocabulary;
import com.g5.fokotoai.exception.AppException;
import com.g5.fokotoai.exception.ErrorCode;
import com.g5.fokotoai.mapper.ProgressMapper;
import com.g5.fokotoai.repository.StudentRepository;
import com.g5.fokotoai.repository.UserWordMetricRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProgressService {

    UserWordMetricRepository userWordMetricRepository;
    StudentRepository studentRepository;
    ProgressMapper progressMapper;

    private static final int DEFAULT_WEAK_VOCAB_LIMIT = 20;

    @Transactional(readOnly = true)
    public LearningProgressResponse getStudentProgress(Long studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw new AppException(ErrorCode.STUDENT_NOT_FOUND);
        }

        long totalStudied    = userWordMetricRepository.countByStudentStudentId(studentId);
        long totalMastered   = userWordMetricRepository.countByStudentStudentIdAndMastered(studentId, true);
        long totalInProgress = totalStudied - totalMastered;

        double masteredPercent = totalStudied == 0
                ? 0.0
                : Math.round((double) totalMastered / totalStudied * 10_000.0) / 100.0;

        return LearningProgressResponse.builder()
                .totalStudied(totalStudied)
                .totalMastered(totalMastered)
                .totalInProgress(totalInProgress)
                .masteredPercent(masteredPercent)
                .build();
    }

    @Transactional(readOnly = true)
    public List<WeakVocabResponse> getWeakVocabulary(Long studentId, Integer limit) {
        if (!studentRepository.existsById(studentId)) {
            throw new AppException(ErrorCode.STUDENT_NOT_FOUND);
        }

        int pageSize = (limit != null && limit > 0) ? limit : DEFAULT_WEAK_VOCAB_LIMIT;
        List<UserWordMetric> metrics = userWordMetricRepository
                .findWeakVocabByStudentId(studentId, PageRequest.of(0, pageSize));

        return metrics.stream()
                .map(metric -> progressMapper.toWeakVocabResponse(metric.getVocab(), metric))
                .toList();
    }

}
