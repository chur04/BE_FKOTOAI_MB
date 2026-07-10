package com.g5.fokotoai.service;

import com.g5.fokotoai.dto.request.GeneratedQuestion;
import com.g5.fokotoai.dto.request.WeakVocabItem;
import com.g5.fokotoai.entity.Student;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GeminiPromptProvider {

    public String buildQuizGenerationPrompt(List<WeakVocabItem> weakVocabs, String level) {
        StringBuilder vocabList = new StringBuilder() ;
        for (int i = 0; i < weakVocabs.size(); i++) {
            WeakVocabItem item = weakVocabs.get(i) ;
            vocabList.append(String.format("%d. %s (số lần sai: %d)\n",
                    i + 1, item.getWord(), item.getErrorCount())) ;
        }

        return """
                Bạn là một giáo viên tiếng Nhật chuyên nghiệp dạy kỳ thi JLPT.
                
                Hãy tạo ĐÚNG 10 câu hỏi trắc nghiệm tiếng Nhật cho học sinh cấp độ %s.
                
                Danh sách từ vựng học sinh đang học yếu (cần ôn tập):
                %s
                
                YÊU CẦU BẮT BUỘC:
                1. Mỗi câu hỏi PHẢI liên quan đến ít nhất một trong các từ vựng trên.
                2. Câu hỏi phải phù hợp với cấp độ JLPT %s (không quá dễ, không quá khó).
                3. Mỗi câu có đúng 4 lựa chọn: "A. ...", "B. ...", "C. ...", "D. ...".
                4. Chỉ có DUY NHẤT 1 đáp án đúng.
                5. Câu hỏi có thể là: điền từ vào chỗ trống, chọn nghĩa đúng, chọn cách đọc đúng.
                
                ĐỊNH DẠNG PHẢN HỒI (JSON THUẦN, không dùng markdown ```json```):
                [
                  {
                    "question": "Nội dung câu hỏi",
                    "options": ["A. lựa chọn 1", "B. lựa chọn 2", "C. lựa chọn 3", "D. lựa chọn 4"],
                    "correctAnswer": "A",
                    "explanation": "Giải thích ngắn gọn tại sao đáp án này đúng (bằng tiếng Việt)"
                  }
                ]
                
                Chỉ trả về JSON, không thêm bất kỳ văn bản nào khác.
                """.formatted(level, vocabList.toString(), level) ;
    }


    public String buildExplanationPrompt(List<String> wrongQuestions) {
        StringBuilder wrongList = new StringBuilder() ;
        for (int i = 0; i < wrongQuestions.size(); i++) {
            wrongList.append("--- Câu sai số ").append(i + 1).append(" ---\n") ;
            wrongList.append(wrongQuestions.get(i)).append("\n\n") ;
        }

        return """
                Bạn là gia sư tiếng Nhật thân thiện và chuyên nghiệp.
                
                Học sinh đã làm sai các câu sau. Hãy giải thích lý do sai và cách ghi nhớ đúng.
                
                %s
                
                YÊU CẦU:
                - Giải thích bằng tiếng Việt, ngắn gọn, dễ hiểu (2-4 câu mỗi giải thích).
                - Nêu rõ tại sao đáp án học sinh chọn SAI và đáp án đúng là gì.
                - Đưa ra mẹo ghi nhớ nếu có thể.
                
                ĐỊNH DẠNG PHẢN HỒI (JSON THUẦN):
                [
                  "Giải thích câu sai số 1...",
                  "Giải thích câu sai số 2..."
                ]
                
                Chỉ trả về JSON array, không thêm bất kỳ văn bản nào khác.
                """.formatted(wrongList.toString()) ;
    }


    public String formatWrongQuestion(int questionNumber, GeneratedQuestion question, String studentAnswer) {
        return String.format(
            "Câu %d: %s\nCác lựa chọn: %s\nĐáp án đúng: %s\nHọc sinh chọn: %s",
            questionNumber,
            question.getQuestion(),
            String.join(", ", question.getOptions()),
            question.getCorrectAnswer(),
            studentAnswer
        ) ;
    }


    public String buildChatbotSystemPrompt(Student student) {
        String level = student.getCurrentLevel() != null
                ? student.getCurrentLevel().name()
                : "N5" ;
        String studentName = student.getFullname() != null ? student.getFullname() : "bạn" ;

        return """
                Bạn là gia sư tiếng Nhật AI tên là "Sensei Fuko", chuyên dạy kỳ thi JLPT %s.
                
                THÔNG TIN HỌC SINH:
                - Tên: %s
                - Cấp độ đang học: JLPT %s
                
                HƯỚNG DẪN VAI TRÒ:
                - Luôn trả lời bằng tiếng Việt (trừ khi học sinh hỏi bằng tiếng Nhật thì phải giải thích cả hai ngôn ngữ).
                - Giải thích ngữ pháp, từ vựng và cách dùng một cách đơn giản, dễ hiểu.
                - Đưa ra ví dụ câu cụ thể khi giải thích từ hoặc ngữ pháp.
                - Khuyến khích học sinh và tạo môi trường học tập tích cực.
                - Chỉ trả lời các câu hỏi liên quan đến tiếng Nhật, JLPT, văn hóa Nhật Bản.
                - Nếu câu hỏi không liên quan đến tiếng Nhật, nhẹ nhàng hướng học sinh quay lại chủ đề học.
                - Giữ câu trả lời súc tích (không quá 400 từ) nhưng đầy đủ thông tin.
                
                Khi học sinh hỏi về từ vựng, hãy bao gồm: cách đọc (hiragana/katakana), nghĩa, ví dụ câu.
                Khi học sinh hỏi về ngữ pháp, hãy bao gồm: cấu trúc, ý nghĩa, ví dụ câu, lưu ý đặc biệt.
                """.formatted(level, studentName, level) ;
    }

    public String buildFullChatPrompt(String systemPrompt, String studentMessage) {
        return systemPrompt + "\n\n---\n\n**Câu hỏi của học sinh:**\n" + studentMessage ;
    }
}
