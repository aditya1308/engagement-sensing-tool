package com.sensing.engagement.tool.controller;

import com.sensing.engagement.tool.dto.QuestionResponse;
import com.sensing.engagement.tool.dto.QuestionUploadRequest;
import com.sensing.engagement.tool.service.QuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @PostMapping("/upload")
    public ResponseEntity<List<QuestionResponse>> uploadQuestions(@RequestBody QuestionUploadRequest request) {

        List<QuestionResponse> response = questionService.uploadQuestions(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(value = "/upload-excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<QuestionResponse>> uploadQuestionsExcel(@RequestParam("file") MultipartFile file) {

        List<QuestionResponse> response = questionService.uploadQuestionsFromExcel(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/export-excel")
    public ResponseEntity<byte[]> exportQuestionsExcel() {

        byte[] excelFile = questionService.exportQuestionsToExcel();
        double fileSizeInKB = Math.round((excelFile.length / 1024.0) * 100.0) / 100.0;
        log.info("Exported Questions to Excel, file size : {}", fileSizeInKB + " KB");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(ContentDisposition.attachment().filename("questions.xlsx").build());
        return new ResponseEntity<>(excelFile, headers, HttpStatus.OK);
    }
}