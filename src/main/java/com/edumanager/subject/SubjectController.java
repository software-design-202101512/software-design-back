package com.edumanager.subject;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectRepository subjectRepository;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllSubjects() {
        List<Map<String, Object>> result = subjectRepository.findAll().stream()
                .map(s -> Map.<String, Object>of(
                        "id", s.getId(),
                        "name", s.getName(),
                        "category", s.getCategory() != null ? s.getCategory() : ""
                ))
                .toList();
        return ResponseEntity.ok(result);
    }
}
