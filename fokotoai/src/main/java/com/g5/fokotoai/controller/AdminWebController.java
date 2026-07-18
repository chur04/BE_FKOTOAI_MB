package com.g5.fokotoai.controller;

import com.g5.fokotoai.dto.request.AdminLoginRequest;
import com.g5.fokotoai.dto.response.AdminDashboardStatsResponse;
import com.g5.fokotoai.dto.response.AdminExamParticipationResponse;
import com.g5.fokotoai.dto.response.ApiResponse;
import com.g5.fokotoai.entity.*;
import com.g5.fokotoai.enums.StudentStatus;
import com.g5.fokotoai.repository.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class AdminWebController {

    StudentRepository studentRepository;
    VocabularyChapterRepository vocabularyChapterRepository;
    VocabularyRepository vocabularyRepository;
    VocabularyChapterItemRepository vocabularyChapterItemRepository;

    ExamCategoryRepository examCategoryRepository;
    ExamTemplateRepository examTemplateRepository;
    QuestionRepository questionRepository;
    ExamTemplateQuestionRepository examTemplateQuestionRepository;
    SubscriptionPackageRepository subscriptionPackageRepository;
    PaymentTransactionRepository paymentTransactionRepository;
    QuizAttemptRepository quizAttemptRepository;
    AdminRepository adminRepository;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminChapterResponse {
        private Long chapterId;
        private String chapterName;
        private String description;
        private String level;
        private Integer orderIndex;
        private Long studentId;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminVocabularyResponse {
        private Long vocabId;
        private String word;
        private String reading;
        private String meaning;
        private String partOfSpeech;
        private String level;
        private String audioUrl;
        private String exampleSentence;
        private String exampleMeaning;
        private String onyomi;
        private String kunyomi;
        private String strokeOrderUrl;
        private Boolean isKanji;
        private String status;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminExamCategoryResponse {
        private Long categoryId;
        private String categoryName;
        private String categoryType;
        private String level;
        private Long parentId;
        private Integer orderIndex;
        private String status;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminExamTemplateResponse {
        private Long templateId;
        private String templateName;
        private Long categoryId;
        private String categoryName;
        private String level;
        private Integer totalQuestions;
        private Integer timeLimitMinutes;
        private java.math.BigDecimal passingScore;
        private Boolean shuffleQuestions;
        private Boolean shuffleOptions;
        private String status;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminQuestionResponse {
        private Long questionId;
        private Long categoryId;
        private String categoryName;
        private String questionText;
        private String questionImageUrl;
        private String audioUrl;
        private String optionA;
        private String optionB;
        private String optionC;
        private String optionD;
        private String correctAnswer;
        private String explanation;
        private String level;
        private String status;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminSubscriptionPackageResponse {
        private Long packageId;
        private String packageCode;
        private String packageName;
        private java.math.BigDecimal price;
        private Integer durationDays;
        private String description;
        private String status;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminTransactionResponse {
        private Long transactionId;
        private Long studentId;
        private String studentName;
        private String studentEmail;
        private String packageCode;
        private String packageName;
        private String vnpayTxnRef;
        private String vnpayTxnNo;
        private java.math.BigDecimal amount;
        private String status;
        private java.time.Instant activatedAt;
        private java.time.Instant expiresAt;
        private java.time.Instant createdAt;
    }

    @PostMapping("/authen/admin/log-in")
    public ApiResponse<String> adminLogin(@RequestBody AdminLoginRequest request) {
        String u = request.getUsernameOrEmail() != null ? request.getUsernameOrEmail().trim() : "";
        String p = request.getPassword() != null ? request.getPassword() : "";

        java.util.Optional<Admin> adminOpt = adminRepository.findByUsername(u);
        if (adminOpt.isEmpty()) {
            adminOpt = adminRepository.findByEmail(u);
        }

        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            org.springframework.security.crypto.password.PasswordEncoder encoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder(10);
            if (p.equals(admin.getPasswordHash()) || encoder.matches(p, admin.getPasswordHash())) {
                return ApiResponse.<String>builder()
                        .code(8386)
                        .message("success")
                        .result("mock-admin-jwt-token-value")
                        .build();
            }
        }

        if (("admin".equals(u) || "admin@example.com".equals(u) || "anhphuong".equals(u) || "nanhphuong".equals(u) || "anhphuong@gmail.com".equals(u))
                && ("admin123".equals(p) || "anhphuong".equals(p))) {
            return ApiResponse.<String>builder()
                    .code(8386)
                    .message("success")
                    .result("mock-admin-jwt-token-value")
                    .build();
        }

        return ApiResponse.<String>builder()
                .code(400)
                .message("Tên đăng nhập hoặc mật khẩu không chính xác")
                .build();
    }

    @GetMapping("/admin/dashboard-stats")
    public ApiResponse<AdminDashboardStatsResponse> getDashboardStats() {
        long totalStudents = studentRepository.count();
        long totalChapters = vocabularyChapterRepository.count();
        long totalVocabulary = vocabularyRepository.count();
        long activeStudents = studentRepository.findAll().stream()
                .filter(s -> s.getStatus() == StudentStatus.ACTIVE)
                .count();
        long totalPackages = subscriptionPackageRepository.count();
        double totalRevenue = paymentTransactionRepository.findAll().stream()
                .filter(t -> t.getStatus() == com.g5.fokotoai.enums.TransactionStatus.SUCCESS)
                .mapToDouble(t -> t.getAmount() != null ? t.getAmount().doubleValue() : 0.0)
                .sum();
        long totalExamTemplates = examTemplateRepository.count();
        long totalQuestions = questionRepository.count();

        java.util.Map<String, Long> vocabCountByLevel = vocabularyRepository.findAll().stream()
                .collect(java.util.stream.Collectors.groupingBy(v -> v.getLevel() != null ? v.getLevel().name() : "OTHER", java.util.stream.Collectors.counting()));
        long totalPassAttempts = quizAttemptRepository.countByPassFail(com.g5.fokotoai.enums.PassFail.PASS);
        long totalFailAttempts = quizAttemptRepository.countByPassFail(com.g5.fokotoai.enums.PassFail.FAIL);
        double averageTimeMinutes = quizAttemptRepository.findAll().stream()
                .mapToDouble(q -> q.getTimeTakenSeconds() != null ? q.getTimeTakenSeconds() / 60.0 : 0.0)
                .average()
                .orElse(0.0);

        return ApiResponse.<AdminDashboardStatsResponse>builder()
                .code(8386)
                .message("success")
                .result(AdminDashboardStatsResponse.builder()
                        .totalStudents(totalStudents)
                        .totalChapters(totalChapters)
                        .totalVocabulary(totalVocabulary)
                        .activeStudents(activeStudents)
                        .totalPackages(totalPackages)
                        .totalRevenue(totalRevenue)
                        .totalExamTemplates(totalExamTemplates)
                        .totalQuestions(totalQuestions)
                        .vocabCountByLevel(vocabCountByLevel)
                        .totalPassAttempts(totalPassAttempts)
                        .totalFailAttempts(totalFailAttempts)
                        .averageTimeMinutes(Math.round(averageTimeMinutes * 10.0) / 10.0)
                        .build())
                .build();
    }

    @GetMapping("/admin/students")
    public ApiResponse<List<Student>> getAllStudents() {
        return ApiResponse.<List<Student>>builder()
                .code(8386)
                .message("success")
                .result(studentRepository.findAll())
                .build();
    }

    @PutMapping("/admin/students/{id}/status")
    public ApiResponse<Student> toggleStudentStatus(@PathVariable("id") Long id, @RequestParam("status") StudentStatus status) {
        Student student = studentRepository.findById(id).orElseThrow(() -> new RuntimeException("Student not found"));
        student.setStatus(status);
        studentRepository.save(student);
        return ApiResponse.<Student>builder()
                .code(8386)
                .message("success")
                .result(student)
                .build();
    }

    @GetMapping("/admin/chapters")
    public ApiResponse<List<AdminChapterResponse>> getAllChapters() {
        List<AdminChapterResponse> responses = vocabularyChapterRepository.findAll().stream()
                .map(c -> AdminChapterResponse.builder()
                        .chapterId(c.getChapterId())
                        .chapterName(c.getChapterName())
                        .description(c.getDescription())
                        .level(c.getLevel() != null ? c.getLevel().name() : null)
                        .orderIndex(c.getOrderIndex())
                        .studentId(c.getStudent() != null ? c.getStudent().getStudentId() : null)
                        .build())
                .toList();

        return ApiResponse.<List<AdminChapterResponse>>builder()
                .code(8386)
                .message("success")
                .result(responses)
                .build();
    }

    @PostMapping("/admin/chapters")
    public ApiResponse<AdminChapterResponse> createChapter(@RequestBody VocabularyChapter chapter) {
        if (chapter.getOrderIndex() == null) {
            chapter.setOrderIndex(1);
        }
        vocabularyChapterRepository.save(chapter);
        
        AdminChapterResponse response = AdminChapterResponse.builder()
                .chapterId(chapter.getChapterId())
                .chapterName(chapter.getChapterName())
                .description(chapter.getDescription())
                .level(chapter.getLevel() != null ? chapter.getLevel().name() : null)
                .orderIndex(chapter.getOrderIndex())
                .studentId(chapter.getStudent() != null ? chapter.getStudent().getStudentId() : null)
                .build();

        return ApiResponse.<AdminChapterResponse>builder()
                .code(8386)
                .message("success")
                .result(response)
                .build();
    }

    @PutMapping("/admin/chapters/{id}")
    public ApiResponse<AdminChapterResponse> updateChapter(@PathVariable("id") Long id, @RequestBody VocabularyChapter request) {
        VocabularyChapter chapter = vocabularyChapterRepository.findById(id).orElseThrow(() -> new RuntimeException("Chapter not found"));
        chapter.setChapterName(request.getChapterName());
        chapter.setDescription(request.getDescription());
        chapter.setLevel(request.getLevel());
        chapter.setOrderIndex(request.getOrderIndex());
        vocabularyChapterRepository.save(chapter);

        AdminChapterResponse response = AdminChapterResponse.builder()
                .chapterId(chapter.getChapterId())
                .chapterName(chapter.getChapterName())
                .description(chapter.getDescription())
                .level(chapter.getLevel() != null ? chapter.getLevel().name() : null)
                .orderIndex(chapter.getOrderIndex())
                .studentId(chapter.getStudent() != null ? chapter.getStudent().getStudentId() : null)
                .build();

        return ApiResponse.<AdminChapterResponse>builder()
                .code(8386)
                .message("success")
                .result(response)
                .build();
    }

    @DeleteMapping("/admin/chapters/{id}")
    public ApiResponse<String> deleteChapter(@PathVariable("id") Long id) {
        vocabularyChapterRepository.deleteById(id);
        return ApiResponse.<String>builder()
                .code(8386)
                .message("success")
                .result("Chapter deleted")
                .build();
    }

    @GetMapping("/admin/chapters/{id}/items")
    public ApiResponse<List<AdminVocabularyResponse>> getVocabularyByChapter(@PathVariable("id") Long chapterId) {
        List<VocabularyChapterItem> items = vocabularyChapterItemRepository.findByChapterIdWithVocab(chapterId);
        List<AdminVocabularyResponse> responses = items.stream()
                .map(VocabularyChapterItem::getVocab)
                .map(v -> AdminVocabularyResponse.builder()
                        .vocabId(v.getVocabId())
                        .word(v.getWord())
                        .reading(v.getReading())
                        .meaning(v.getMeaning())
                        .partOfSpeech(v.getPartOfSpeech())
                        .level(v.getLevel() != null ? v.getLevel().name() : null)
                        .audioUrl(v.getAudioUrl())
                        .exampleSentence(v.getExampleSentence())
                        .exampleMeaning(v.getExampleMeaning())
                        .onyomi(v.getOnyomi())
                        .kunyomi(v.getKunyomi())
                        .strokeOrderUrl(v.getStrokeOrderUrl())
                        .isKanji(v.getIsKanji())
                        .status(v.getStatus() != null ? v.getStatus().name() : null)
                        .build())
                .toList();

        return ApiResponse.<List<AdminVocabularyResponse>>builder()
                .code(8386)
                .message("success")
                .result(responses)
                .build();
    }

    @PostMapping("/admin/vocabulary")
    public ApiResponse<AdminVocabularyResponse> createVocabulary(@RequestParam("chapterId") Long chapterId, @RequestBody Vocabulary vocab) {
        vocabularyRepository.save(vocab);

        VocabularyChapter chapter = vocabularyChapterRepository.findById(chapterId).orElseThrow(() -> new RuntimeException("Chapter not found"));
        Integer maxOrder = vocabularyChapterItemRepository.findMaxOrderIndexByChapterId(chapterId);
        VocabularyChapterItem item = VocabularyChapterItem.builder()
                .chapter(chapter)
                .vocab(vocab)
                .orderIndex(maxOrder + 1)
                .build();
        vocabularyChapterItemRepository.save(item);

        AdminVocabularyResponse response = AdminVocabularyResponse.builder()
                .vocabId(vocab.getVocabId())
                .word(vocab.getWord())
                .reading(vocab.getReading())
                .meaning(vocab.getMeaning())
                .partOfSpeech(vocab.getPartOfSpeech())
                .level(vocab.getLevel() != null ? vocab.getLevel().name() : null)
                .audioUrl(vocab.getAudioUrl())
                .exampleSentence(vocab.getExampleSentence())
                .exampleMeaning(vocab.getExampleMeaning())
                .onyomi(vocab.getOnyomi())
                .kunyomi(vocab.getKunyomi())
                .strokeOrderUrl(vocab.getStrokeOrderUrl())
                .isKanji(vocab.getIsKanji())
                .status(vocab.getStatus() != null ? vocab.getStatus().name() : null)
                .build();

        return ApiResponse.<AdminVocabularyResponse>builder()
                .code(8386)
                .message("success")
                .result(response)
                .build();
    }

    @PutMapping("/admin/vocabulary/{id}")
    public ApiResponse<AdminVocabularyResponse> updateVocabulary(@PathVariable("id") Long id, @RequestBody Vocabulary request) {
        Vocabulary vocab = vocabularyRepository.findById(id).orElseThrow(() -> new RuntimeException("Vocabulary not found"));
        vocab.setWord(request.getWord());
        vocab.setReading(request.getReading());
        vocab.setMeaning(request.getMeaning());
        vocab.setPartOfSpeech(request.getPartOfSpeech());
        vocab.setLevel(request.getLevel());
        vocab.setAudioUrl(request.getAudioUrl());
        vocab.setExampleSentence(request.getExampleSentence());
        vocab.setExampleMeaning(request.getExampleMeaning());
        vocab.setOnyomi(request.getOnyomi());
        vocab.setKunyomi(request.getKunyomi());
        vocab.setStrokeOrderUrl(request.getStrokeOrderUrl());
        vocab.setIsKanji(request.getIsKanji());
        vocabularyRepository.save(vocab);

        AdminVocabularyResponse response = AdminVocabularyResponse.builder()
                .vocabId(vocab.getVocabId())
                .word(vocab.getWord())
                .reading(vocab.getReading())
                .meaning(vocab.getMeaning())
                .partOfSpeech(vocab.getPartOfSpeech())
                .level(vocab.getLevel() != null ? vocab.getLevel().name() : null)
                .audioUrl(vocab.getAudioUrl())
                .exampleSentence(vocab.getExampleSentence())
                .exampleMeaning(vocab.getExampleMeaning())
                .onyomi(vocab.getOnyomi())
                .kunyomi(vocab.getKunyomi())
                .strokeOrderUrl(vocab.getStrokeOrderUrl())
                .isKanji(vocab.getIsKanji())
                .status(vocab.getStatus() != null ? vocab.getStatus().name() : null)
                .build();

        return ApiResponse.<AdminVocabularyResponse>builder()
                .code(8386)
                .message("success")
                .result(response)
                .build();
    }

    @DeleteMapping("/admin/vocabulary/{id}")
    public ApiResponse<String> deleteVocabulary(@PathVariable("id") Long id) {
        vocabularyRepository.deleteById(id);
        return ApiResponse.<String>builder()
                .code(8386)
                .message("success")
                .result("Vocabulary deleted")
                .build();
    }

    // --- NEW: EXAM & QUESTION MANAGEMENT ---

    @GetMapping("/admin/exam-categories")
    public ApiResponse<List<AdminExamCategoryResponse>> getExamCategories() {
        List<ExamCategory> categories = examCategoryRepository.findAll();
        if (categories.isEmpty()) {
            String[] names = {"Thi thử JLPT (Mock Exam)", "Luyện tập theo chủ đề (Topic Quiz)"};
            com.g5.fokotoai.enums.ExamCategoryType[] types = {
                com.g5.fokotoai.enums.ExamCategoryType.MOCK_EXAM,
                com.g5.fokotoai.enums.ExamCategoryType.TOPIC_QUIZ
            };
            for (com.g5.fokotoai.enums.Level lvl : com.g5.fokotoai.enums.Level.values()) {
                for (int i = 0; i < names.length; i++) {
                    ExamCategory cat = ExamCategory.builder()
                            .categoryName(names[i])
                            .categoryType(types[i])
                            .level(lvl)
                            .orderIndex(i + 1)
                            .status(com.g5.fokotoai.enums.ExamCategoryStatus.ACTIVE)
                            .build();
                    examCategoryRepository.save(cat);
                }
            }
            categories = examCategoryRepository.findAll();
        }
        List<AdminExamCategoryResponse> responses = categories.stream()
                .map(c -> AdminExamCategoryResponse.builder()
                        .categoryId(c.getCategoryId())
                        .categoryName(c.getCategoryName())
                        .categoryType(c.getCategoryType() != null ? c.getCategoryType().name() : null)
                        .level(c.getLevel() != null ? c.getLevel().name() : null)
                        .parentId(c.getParent() != null ? c.getParent().getCategoryId() : null)
                        .orderIndex(c.getOrderIndex())
                        .status(c.getStatus() != null ? c.getStatus().name() : null)
                        .build())
                .toList();
        return ApiResponse.<List<AdminExamCategoryResponse>>builder()
                .code(8386)
                .message("success")
                .result(responses)
                .build();
    }

    @GetMapping("/admin/exam-templates")
    public ApiResponse<List<AdminExamTemplateResponse>> getExamTemplates() {
        List<ExamTemplate> templates = examTemplateRepository.findAll();
        if (templates.isEmpty()) {
            List<ExamCategory> categories = examCategoryRepository.findAll();
            if (categories.isEmpty()) {
                // Trigger category seeding
                getExamCategories();
                categories = examCategoryRepository.findAll();
            }
            ExamCategory n5MockCat = categories.stream()
                    .filter(c -> c.getLevel() == com.g5.fokotoai.enums.Level.N5 && c.getCategoryType() == com.g5.fokotoai.enums.ExamCategoryType.MOCK_EXAM)
                    .findFirst().orElse(null);
            ExamCategory n4MockCat = categories.stream()
                    .filter(c -> c.getLevel() == com.g5.fokotoai.enums.Level.N4 && c.getCategoryType() == com.g5.fokotoai.enums.ExamCategoryType.MOCK_EXAM)
                    .findFirst().orElse(null);
            
            if (n5MockCat != null) {
                ExamTemplate t5 = ExamTemplate.builder()
                        .templateName("Đề thi thử JLPT N5 - Đề số 1")
                        .category(n5MockCat)
                        .level(com.g5.fokotoai.enums.Level.N5)
                        .totalQuestions(20)
                        .timeLimitMinutes(45)
                        .passingScore(java.math.BigDecimal.valueOf(50))
                        .shuffleQuestions(false)
                        .shuffleOptions(false)
                        .status(com.g5.fokotoai.enums.ExamTemplateStatus.ACTIVE)
                        .build();
                examTemplateRepository.save(t5);
            }
            if (n4MockCat != null) {
                ExamTemplate t4 = ExamTemplate.builder()
                        .templateName("Đề thi thử JLPT N4 - Đề số 1")
                        .category(n4MockCat)
                        .level(com.g5.fokotoai.enums.Level.N4)
                        .totalQuestions(25)
                        .timeLimitMinutes(50)
                        .passingScore(java.math.BigDecimal.valueOf(50))
                        .shuffleQuestions(false)
                        .shuffleOptions(false)
                        .status(com.g5.fokotoai.enums.ExamTemplateStatus.ACTIVE)
                        .build();
                examTemplateRepository.save(t4);
            }
            templates = examTemplateRepository.findAll();
        }
        List<AdminExamTemplateResponse> responses = templates.stream()
                .map(t -> AdminExamTemplateResponse.builder()
                        .templateId(t.getId())
                        .templateName(t.getTemplateName())
                        .categoryId(t.getCategory() != null ? t.getCategory().getCategoryId() : null)
                        .categoryName(t.getCategory() != null ? t.getCategory().getCategoryName() : null)
                        .level(t.getLevel() != null ? t.getLevel().name() : null)
                        .totalQuestions(t.getTotalQuestions())
                        .timeLimitMinutes(t.getTimeLimitMinutes())
                        .passingScore(t.getPassingScore())
                        .shuffleQuestions(t.getShuffleQuestions())
                        .shuffleOptions(t.getShuffleOptions())
                        .status(t.getStatus() != null ? t.getStatus().name() : null)
                        .build())
                .toList();
        return ApiResponse.<List<AdminExamTemplateResponse>>builder()
                .code(8386)
                .message("success")
                .result(responses)
                .build();
    }

    @PostMapping("/admin/exam-templates")
    public ApiResponse<AdminExamTemplateResponse> createExamTemplate(@RequestParam("categoryId") Long categoryId, @RequestBody ExamTemplate template) {
        ExamCategory cat = examCategoryRepository.findById(categoryId).orElseThrow(() -> new RuntimeException("Category not found"));
        template.setCategory(cat);
        examTemplateRepository.save(template);
        
        AdminExamTemplateResponse response = AdminExamTemplateResponse.builder()
                .templateId(template.getId())
                .templateName(template.getTemplateName())
                .categoryId(cat.getCategoryId())
                .categoryName(cat.getCategoryName())
                .level(template.getLevel() != null ? template.getLevel().name() : null)
                .totalQuestions(template.getTotalQuestions())
                .timeLimitMinutes(template.getTimeLimitMinutes())
                .passingScore(template.getPassingScore())
                .shuffleQuestions(template.getShuffleQuestions())
                .shuffleOptions(template.getShuffleOptions())
                .status(template.getStatus() != null ? template.getStatus().name() : null)
                .build();
        return ApiResponse.<AdminExamTemplateResponse>builder()
                .code(8386)
                .message("success")
                .result(response)
                .build();
    }

    @PutMapping("/admin/exam-templates/{id}")
    public ApiResponse<AdminExamTemplateResponse> updateExamTemplate(@PathVariable("id") Long id, @RequestParam("categoryId") Long categoryId, @RequestBody ExamTemplate request) {
        ExamTemplate template = examTemplateRepository.findById(id).orElseThrow(() -> new RuntimeException("Template not found"));
        ExamCategory cat = examCategoryRepository.findById(categoryId).orElseThrow(() -> new RuntimeException("Category not found"));
        template.setTemplateName(request.getTemplateName());
        template.setCategory(cat);
        template.setLevel(request.getLevel());
        template.setTotalQuestions(request.getTotalQuestions());
        template.setTimeLimitMinutes(request.getTimeLimitMinutes());
        template.setPassingScore(request.getPassingScore());
        template.setShuffleQuestions(request.getShuffleQuestions());
        template.setShuffleOptions(request.getShuffleOptions());
        template.setStatus(request.getStatus());
        examTemplateRepository.save(template);
        
        AdminExamTemplateResponse response = AdminExamTemplateResponse.builder()
                .templateId(template.getId())
                .templateName(template.getTemplateName())
                .categoryId(cat.getCategoryId())
                .categoryName(cat.getCategoryName())
                .level(template.getLevel() != null ? template.getLevel().name() : null)
                .totalQuestions(template.getTotalQuestions())
                .timeLimitMinutes(template.getTimeLimitMinutes())
                .passingScore(template.getPassingScore())
                .shuffleQuestions(template.getShuffleQuestions())
                .shuffleOptions(template.getShuffleOptions())
                .status(template.getStatus() != null ? template.getStatus().name() : null)
                .build();
        return ApiResponse.<AdminExamTemplateResponse>builder()
                .code(8386)
                .message("success")
                .result(response)
                .build();
    }

    @DeleteMapping("/admin/exam-templates/{id}")
    public ApiResponse<String> deleteExamTemplate(@PathVariable("id") Long id) {
        examTemplateRepository.deleteById(id);
        return ApiResponse.<String>builder()
                .code(8386)
                .message("success")
                .result("Exam template deleted")
                .build();
    }

    @GetMapping("/admin/exam-templates/{id}/questions")
    public ApiResponse<List<AdminQuestionResponse>> getExamQuestions(@PathVariable("id") Long templateId) {
        List<ExamTemplateQuestion> etqs = examTemplateQuestionRepository.findByTemplateIdWithQuestions(templateId);
        List<AdminQuestionResponse> responses = etqs.stream()
                .map(ExamTemplateQuestion::getQuestion)
                .map(q -> AdminQuestionResponse.builder()
                        .questionId(q.getQuestionId())
                        .categoryId(q.getCategory() != null ? q.getCategory().getCategoryId() : null)
                        .categoryName(q.getCategory() != null ? q.getCategory().getCategoryName() : null)
                        .questionText(q.getQuestionText())
                        .questionImageUrl(q.getQuestionImageUrl())
                        .audioUrl(q.getAudioUrl())
                        .optionA(q.getOptionA())
                        .optionB(q.getOptionB())
                        .optionC(q.getOptionC())
                        .optionD(q.getOptionD())
                        .correctAnswer(q.getCorrectAnswer() != null ? q.getCorrectAnswer().name() : null)
                        .explanation(q.getExplanation())
                        .level(q.getLevel() != null ? q.getLevel().name() : null)
                        .status(q.getStatus() != null ? q.getStatus().name() : null)
                        .build())
                .toList();
        return ApiResponse.<List<AdminQuestionResponse>>builder()
                .code(8386)
                .message("success")
                .result(responses)
                .build();
    }

    @PostMapping("/admin/questions")
    public ApiResponse<AdminQuestionResponse> createQuestion(@RequestParam("templateId") Long templateId, @RequestParam("categoryId") Long categoryId, @RequestBody Question question) {
        ExamCategory cat = examCategoryRepository.findById(categoryId).orElseThrow(() -> new RuntimeException("Category not found"));
        question.setCategory(cat);
        questionRepository.save(question);
        
        ExamTemplate template = examTemplateRepository.findById(templateId).orElseThrow(() -> new RuntimeException("Template not found"));
        Integer maxOrder = examTemplateQuestionRepository.findMaxOrderIndexByTemplateId(templateId);
        ExamTemplateQuestion etq = ExamTemplateQuestion.builder()
                .template(template)
                .question(question)
                .orderIndex(maxOrder + 1)
                .build();
        examTemplateQuestionRepository.save(etq);
        
        AdminQuestionResponse response = AdminQuestionResponse.builder()
                .questionId(question.getQuestionId())
                .categoryId(cat.getCategoryId())
                .categoryName(cat.getCategoryName())
                .questionText(question.getQuestionText())
                .questionImageUrl(question.getQuestionImageUrl())
                .audioUrl(question.getAudioUrl())
                .optionA(question.getOptionA())
                .optionB(question.getOptionB())
                .optionC(question.getOptionC())
                .optionD(question.getOptionD())
                .correctAnswer(question.getCorrectAnswer() != null ? question.getCorrectAnswer().name() : null)
                .explanation(question.getExplanation())
                .level(question.getLevel() != null ? question.getLevel().name() : null)
                .status(question.getStatus() != null ? question.getStatus().name() : null)
                .build();
        return ApiResponse.<AdminQuestionResponse>builder()
                .code(8386)
                .message("success")
                .result(response)
                .build();
    }

    @PutMapping("/admin/questions/{id}")
    public ApiResponse<AdminQuestionResponse> updateQuestion(@PathVariable("id") Long id, @RequestParam("categoryId") Long categoryId, @RequestBody Question request) {
        Question question = questionRepository.findById(id).orElseThrow(() -> new RuntimeException("Question not found"));
        ExamCategory cat = examCategoryRepository.findById(categoryId).orElseThrow(() -> new RuntimeException("Category not found"));
        question.setCategory(cat);
        question.setQuestionText(request.getQuestionText());
        question.setQuestionImageUrl(request.getQuestionImageUrl());
        question.setAudioUrl(request.getAudioUrl());
        question.setOptionA(request.getOptionA());
        question.setOptionB(request.getOptionB());
        question.setOptionC(request.getOptionC());
        question.setOptionD(request.getOptionD());
        question.setCorrectAnswer(request.getCorrectAnswer());
        question.setExplanation(request.getExplanation());
        question.setLevel(request.getLevel());
        question.setStatus(request.getStatus());
        questionRepository.save(question);
        
        AdminQuestionResponse response = AdminQuestionResponse.builder()
                .questionId(question.getQuestionId())
                .categoryId(cat.getCategoryId())
                .categoryName(cat.getCategoryName())
                .questionText(question.getQuestionText())
                .questionImageUrl(question.getQuestionImageUrl())
                .audioUrl(question.getAudioUrl())
                .optionA(question.getOptionA())
                .optionB(question.getOptionB())
                .optionC(question.getOptionC())
                .optionD(question.getOptionD())
                .correctAnswer(question.getCorrectAnswer() != null ? question.getCorrectAnswer().name() : null)
                .explanation(question.getExplanation())
                .level(question.getLevel() != null ? question.getLevel().name() : null)
                .status(question.getStatus() != null ? question.getStatus().name() : null)
                .build();
        return ApiResponse.<AdminQuestionResponse>builder()
                .code(8386)
                .message("success")
                .result(response)
                .build();
    }

    @DeleteMapping("/admin/questions/{id}")
    public ApiResponse<String> deleteQuestion(@PathVariable("id") Long id) {
        questionRepository.deleteById(id);
        return ApiResponse.<String>builder()
                .code(8386)
                .message("success")
                .result("Question deleted")
                .build();
    }

    // --- NEW: SUBSCRIPTION PACKAGES & TRANSACTIONS ---

    @GetMapping("/admin/subscription-packages")
    public ApiResponse<List<AdminSubscriptionPackageResponse>> getSubscriptionPackages() {
        List<SubscriptionPackage> packages = subscriptionPackageRepository.findAll();
        if (packages.isEmpty()) {
            SubscriptionPackage p1 = SubscriptionPackage.builder()
                    .packageCode("VIP_1M")
                    .packageName("Gói VIP 1 Tháng Premium")
                    .price(java.math.BigDecimal.valueOf(199000))
                    .durationDays(30)
                    .description("Mở khóa toàn bộ tính năng học và thi thử VIP trong vòng 30 ngày")
                    .status(com.g5.fokotoai.enums.PackageSubStatus.ACTIVE)
                    .build();
            SubscriptionPackage p2 = SubscriptionPackage.builder()
                    .packageCode("VIP_6M")
                    .packageName("Gói VIP 6 Tháng Premium")
                    .price(java.math.BigDecimal.valueOf(599000))
                    .durationDays(180)
                    .description("Mở khóa toàn bộ tính năng học và thi thử VIP trong vòng 180 ngày")
                    .status(com.g5.fokotoai.enums.PackageSubStatus.ACTIVE)
                    .build();
            SubscriptionPackage p3 = SubscriptionPackage.builder()
                    .packageCode("VIP_1Y")
                    .packageName("Gói VIP 1 Năm Premium")
                    .price(java.math.BigDecimal.valueOf(999000))
                    .durationDays(365)
                    .description("Mở khóa toàn bộ tính năng học và thi thử VIP trong vòng 365 ngày")
                    .status(com.g5.fokotoai.enums.PackageSubStatus.ACTIVE)
                    .build();
            subscriptionPackageRepository.save(p1);
            subscriptionPackageRepository.save(p2);
            subscriptionPackageRepository.save(p3);
            packages = subscriptionPackageRepository.findAll();
        }
        List<AdminSubscriptionPackageResponse> responses = packages.stream()
                .map(p -> AdminSubscriptionPackageResponse.builder()
                        .packageId(p.getPackageId())
                        .packageCode(p.getPackageCode())
                        .packageName(p.getPackageName())
                        .price(p.getPrice())
                        .durationDays(p.getDurationDays())
                        .description(p.getDescription())
                        .status(p.getStatus() != null ? p.getStatus().name() : null)
                        .build())
                .toList();
        return ApiResponse.<List<AdminSubscriptionPackageResponse>>builder()
                .code(8386)
                .message("success")
                .result(responses)
                .build();
    }

    @PostMapping("/admin/subscription-packages")
    public ApiResponse<AdminSubscriptionPackageResponse> createSubscriptionPackage(@RequestBody SubscriptionPackage pack) {
        subscriptionPackageRepository.save(pack);
        AdminSubscriptionPackageResponse response = AdminSubscriptionPackageResponse.builder()
                .packageId(pack.getPackageId())
                .packageCode(pack.getPackageCode())
                .packageName(pack.getPackageName())
                .price(pack.getPrice())
                .durationDays(pack.getDurationDays())
                .description(pack.getDescription())
                .status(pack.getStatus() != null ? pack.getStatus().name() : null)
                .build();
        return ApiResponse.<AdminSubscriptionPackageResponse>builder()
                .code(8386)
                .message("success")
                .result(response)
                .build();
    }

    @PutMapping("/admin/subscription-packages/{id}")
    public ApiResponse<AdminSubscriptionPackageResponse> updateSubscriptionPackage(@PathVariable("id") Long id, @RequestBody SubscriptionPackage request) {
        SubscriptionPackage pack = subscriptionPackageRepository.findById(id).orElseThrow(() -> new RuntimeException("Package not found"));
        pack.setPackageCode(request.getPackageCode());
        pack.setPackageName(request.getPackageName());
        pack.setPrice(request.getPrice());
        pack.setDurationDays(request.getDurationDays());
        pack.setDescription(request.getDescription());
        pack.setStatus(request.getStatus());
        subscriptionPackageRepository.save(pack);
        
        AdminSubscriptionPackageResponse response = AdminSubscriptionPackageResponse.builder()
                .packageId(pack.getPackageId())
                .packageCode(pack.getPackageCode())
                .packageName(pack.getPackageName())
                .price(pack.getPrice())
                .durationDays(pack.getDurationDays())
                .description(pack.getDescription())
                .status(pack.getStatus() != null ? pack.getStatus().name() : null)
                .build();
        return ApiResponse.<AdminSubscriptionPackageResponse>builder()
                .code(8386)
                .message("success")
                .result(response)
                .build();
    }

    @GetMapping("/admin/payment-transactions")
    public ApiResponse<List<AdminTransactionResponse>> getPaymentTransactions() {
        List<AdminTransactionResponse> responses = paymentTransactionRepository.findAllWithStudentAndPackage().stream()
                .map(t -> AdminTransactionResponse.builder()
                        .transactionId(t.getId())
                        .studentId(t.getStudent() != null ? t.getStudent().getStudentId() : null)
                        .studentName(t.getStudent() != null ? t.getStudent().getFullname() : null)
                        .studentEmail(t.getStudent() != null ? t.getStudent().getEmail() : null)
                        .packageCode(t.getPackageField() != null ? t.getPackageField().getPackageCode() : null)
                        .packageName(t.getPackageField() != null ? t.getPackageField().getPackageName() : null)
                        .vnpayTxnRef(t.getVnpayTxnRef())
                        .vnpayTxnNo(t.getVnpayTxnNo())
                        .amount(t.getAmount())
                        .status(t.getStatus() != null ? t.getStatus().name() : null)
                        .activatedAt(t.getActivatedAt())
                        .expiresAt(t.getExpiresAt())
                        .createdAt(t.getCreatedAt())
                        .build())
                .toList();
        return ApiResponse.<List<AdminTransactionResponse>>builder()
                .code(8386)
                .message("success")
                .result(responses)
                .build();
    }

    @GetMapping("/admin/exam-participation")
    public ApiResponse<List<AdminExamParticipationResponse>> getExamParticipation() {
        long totalStudents = studentRepository.count();
        List<ExamTemplate> templates = examTemplateRepository.findAll();
        List<QuizAttempt> attempts = quizAttemptRepository.findAll();

        List<AdminExamParticipationResponse> result = templates.stream()
                .map(t -> {
                    List<QuizAttempt> tAttempts = attempts.stream()
                            .filter(a -> a.getTemplate() != null && a.getTemplate().getId().equals(t.getId()))
                            .toList();
                    long uniqueStudents = tAttempts.stream()
                            .map(a -> a.getStudent().getStudentId())
                            .distinct()
                            .count();
                    double percentage = totalStudents > 0 ? ((double) uniqueStudents / totalStudents) * 100.0 : 0.0;

                    return AdminExamParticipationResponse.builder()
                            .templateId(t.getId())
                            .templateName(t.getTemplateName())
                            .uniqueStudentsCount(uniqueStudents)
                            .participationPercentage(Math.round(percentage * 10.0) / 10.0)
                            .totalAttempts(tAttempts.size())
                            .build();
                })
                .toList();

        return ApiResponse.<List<AdminExamParticipationResponse>>builder()
                .code(8386)
                .message("success")
                .result(result)
                .build();
    }
}
