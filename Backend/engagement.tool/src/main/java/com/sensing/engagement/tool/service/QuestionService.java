package com.sensing.engagement.tool.service;

import com.sensing.engagement.tool.dto.QuestionResponse;
import com.sensing.engagement.tool.dto.QuestionUploadRequest;
import com.sensing.engagement.tool.enums.QuestionType;
import com.sensing.engagement.tool.model.Question;
import com.sensing.engagement.tool.model.Theme;
import com.sensing.engagement.tool.repository.QuestionRepository;
import com.sensing.engagement.tool.repository.ThemeRepository;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final ThemeRepository themeRepository;

    @Transactional
    public List<QuestionResponse> uploadQuestions(QuestionUploadRequest request) {

        validateRequest(request);

        Theme theme = getOrCreateTheme(request.getTheme());

        List<Question> questions = request.getQuestions()
                .stream()
                .map(questionRequest -> Question.builder()
                        .theme(theme)
                        .question(questionRequest.getQuestion())
                        .questionType(questionRequest.getQuestionType())
                        .build())
                .toList();

        List<Question> savedQuestions = questionRepository.saveAll(questions);

        return savedQuestions.stream()
                .map(question -> mapToResponse(question, theme))
                .toList();
    }

    @Transactional(readOnly = true)
    public byte[] exportQuestionsToExcel() {

        List<Question> questions =
                questionRepository.findAll();

        try (
                Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream outputStream =
                        new ByteArrayOutputStream()
        ) {

            Sheet sheet =
                    workbook.createSheet("Questions");


            // Header row
            Row headerRow =
                    sheet.createRow(0);

            String[] headers = {
                    "Question Id",
                    "Theme",
                    "Question",
                    "Question Type",
                    "Active"
            };


            for (int i = 0; i < headers.length; i++) {

                Cell cell =
                        headerRow.createCell(i);

                cell.setCellValue(
                        headers[i]
                );
            }


            int rowIndex = 1;


            for (Question question : questions) {

                Row row =
                        sheet.createRow(rowIndex++);


                row.createCell(0)
                        .setCellValue(
                                question.getId().toString()
                        );


                row.createCell(1)
                        .setCellValue(
                                question.getTheme().getName()
                        );


                row.createCell(2)
                        .setCellValue(
                                question.getQuestion()
                        );


                row.createCell(3)
                        .setCellValue(
                                question.getQuestionType().name()
                        );


                row.createCell(4)
                        .setCellValue(
                                Boolean.TRUE.equals(
                                        question.getActive()
                                )
                        );
            }


            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }


            workbook.write(outputStream);

            return outputStream.toByteArray();

        } catch (IOException e) {

            throw new RuntimeException(
                    "Failed to export questions to Excel",
                    e
            );
        }
    }

    // NEW Excel upload
    @Transactional
    public List<QuestionResponse> uploadQuestionsFromExcel(
            MultipartFile file
    ) {

        validateExcelFile(file);

        List<Question> questions = new ArrayList<>();

        Map<String, Theme> themeCache =
                new HashMap<>();

        try (
                Workbook workbook =
                        WorkbookFactory.create(file.getInputStream())
        ) {

            if (workbook.getNumberOfSheets() == 0) {
                throw new IllegalArgumentException(
                        "Excel file does not contain any sheets"
                );
            }

            Sheet sheet = workbook.getSheetAt(0);

            DataFormatter formatter =
                    new DataFormatter();

            validateHeaders(sheet, formatter);


            /*
             * Read and validate the entire Excel first.
             */
            for (
                    int rowIndex = 1;
                    rowIndex <= sheet.getLastRowNum();
                    rowIndex++
            ) {

                Row row = sheet.getRow(rowIndex);

                if (row == null ||
                        isEmptyRow(row, formatter)) {
                    continue;
                }

                int excelRowNumber =
                        rowIndex + 1;

                String themeName =
                        getCellValue(
                                row,
                                0,
                                formatter
                        );

                String questionText =
                        getCellValue(
                                row,
                                1,
                                formatter
                        );

                String questionTypeValue =
                        getCellValue(
                                row,
                                2,
                                formatter
                        );


                validateExcelRow(
                        excelRowNumber,
                        themeName,
                        questionText,
                        questionTypeValue
                );


                QuestionType questionType =
                        parseQuestionType(
                                questionTypeValue,
                                excelRowNumber
                        );


                String themeKey =
                        themeName
                                .trim()
                                .toLowerCase(Locale.ROOT);


                Theme theme =
                        themeCache.computeIfAbsent(
                                themeKey,
                                key -> getOrCreateTheme(themeName)
                        );


                Question question =
                        Question.builder()
                                .theme(theme)
                                .question(
                                        questionText.trim()
                                )
                                .questionType(
                                        questionType
                                )
                                .active(true)
                                .build();


                questions.add(question);
            }

        } catch (IOException e) {

            throw new IllegalArgumentException(
                    "Unable to read Excel file",
                    e
            );
        }


        if (questions.isEmpty()) {
            throw new IllegalArgumentException(
                    "Excel file does not contain any questions"
            );
        }


        /*
         * Old question set is no longer used
         * for future surveys.
         */
        questionRepository.deactivateAllQuestions();


        /*
         * Completely new questions are inserted.
         */
        List<Question> savedQuestions =
                questionRepository.saveAll(questions);


        log.info("Uploaded {} questions", savedQuestions.size());
        return savedQuestions
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    private void validateExcelFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Excel file is required"
            );
        }

        String filename = file.getOriginalFilename();

        if (
                filename == null ||
                        !filename.toLowerCase(Locale.ROOT)
                                .endsWith(".xlsx")
        ) {

            throw new IllegalArgumentException(
                    "Only .xlsx files are supported"
            );
        }
    }

    private void validateHeaders(
            Sheet sheet,
            DataFormatter formatter
    ) {

        Row header = sheet.getRow(0);

        if (header == null) {
            throw new IllegalArgumentException(
                    "Excel header row is missing"
            );
        }

        String[] expectedHeaders = {
                "theme",
                "question",
                "questiontype"
        };


        for (int i = 0;
             i < expectedHeaders.length;
             i++) {

            String actualHeader =
                    normalizeHeader(
                            getCellValue(
                                    header,
                                    i,
                                    formatter
                            )
                    );

            if (!expectedHeaders[i]
                    .equals(actualHeader)) {

                throw new IllegalArgumentException(
                        "Invalid Excel header at column "
                                + (i + 1)
                                + ". Expected columns: "
                                + "Theme, Question, Question Type"
                );
            }
        }
    }


    private String normalizeHeader(String value) {

        return value
                .trim()
                .toLowerCase(Locale.ROOT)
                .replace(" ", "")
                .replace("_", "")
                .replace("-", "");
    }


    private String getCellValue(
            Row row,
            int column,
            DataFormatter formatter
    ) {

        Cell cell = row.getCell(
                column,
                Row.MissingCellPolicy.RETURN_BLANK_AS_NULL
        );

        if (cell == null) {
            return "";
        }

        return formatter
                .formatCellValue(cell)
                .trim();
    }


    private boolean isEmptyRow(
            Row row,
            DataFormatter formatter
    ) {

        for (int i = 0; i < 3; i++) {

            if (!getCellValue(
                    row,
                    i,
                    formatter
            ).isBlank()) {

                return false;
            }
        }

        return true;
    }


    private void validateExcelRow(
            int row,
            String theme,
            String question,
            String questionType
    ) {

        if (theme.isBlank()) {
            throw new IllegalArgumentException(
                    "Theme is required at Excel row " + row
            );
        }

        if (question.isBlank()) {
            throw new IllegalArgumentException(
                    "Question is required at Excel row " + row
            );
        }

        if (questionType.isBlank()) {
            throw new IllegalArgumentException(
                    "Question Type is required at Excel row " + row
            );
        }
    }


    private QuestionType parseQuestionType(
            String value,
            int row
    ) {

        String normalized =
                value.trim()
                        .toUpperCase(Locale.ROOT)
                        .replace(" ", "_")
                        .replace("-", "_");


        try {

            return QuestionType.valueOf(normalized);

        } catch (IllegalArgumentException e) {

            throw new IllegalArgumentException(
                    "Invalid Question Type at Excel row "
                            + row
                            + ": "
                            + value
                            + ". Allowed values: "
                            + Arrays.toString(
                            QuestionType.values()
                    )
            );
        }
    }


    private UUID parseQuestionId(
            String value,
            int row
    ) {

        try {

            return UUID.fromString(value.trim());

        } catch (IllegalArgumentException e) {

            throw new IllegalArgumentException(
                    "Invalid Question Id at Excel row "
                            + row
                            + ": "
                            + value
            );
        }
    }


    private QuestionResponse mapToResponse(
            Question question
    ) {

        return QuestionResponse.builder()
                .id(question.getId())
                .themeId(
                        question.getTheme().getId()
                )
                .theme(
                        question.getTheme().getName()
                )
                .question(
                        question.getQuestion()
                )
                .questionType(
                        question.getQuestionType()
                )
                .build();
    }

    private void validateRequest(QuestionUploadRequest request) {

        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }

        if (request.getTheme() == null || request.getTheme().isBlank()) {
            throw new IllegalArgumentException("Theme is required");
        }

        if (request.getQuestions() == null || request.getQuestions().isEmpty()) {
            throw new IllegalArgumentException("At least one question is required");
        }
    }

    private Theme getOrCreateTheme(String themeName) {

        return themeRepository.findByNameIgnoreCase(themeName.trim())
                .orElseGet(() -> themeRepository.save(
                        Theme.builder()
                                .name(themeName.trim())
                                .build()
                ));
    }

    private QuestionResponse mapToResponse(Question question, Theme theme) {

        return QuestionResponse.builder()
                .id(question.getId())
                .themeId(theme.getId())
                .theme(theme.getName())
                .question(question.getQuestion())
                .questionType(question.getQuestionType())
                .build();
    }
}