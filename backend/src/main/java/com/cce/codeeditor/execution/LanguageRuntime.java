package com.cce.codeeditor.execution;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LanguageRuntime {
    JAVASCRIPT("node:18-alpine", "solution.js", "node solution.js"),
    PYTHON("python:3.11-alpine", "solution.py", "python solution.py"),
    JAVA("openjdk:17-alpine", "Solution.java", "javac Solution.java && java Solution"),
    CPP("gcc:13-alpine", "solution.cpp", "g++ -o solution solution.cpp && ./solution"),
    GO("golang:1.21-alpine", "solution.go", "go run solution.go"),
    RUST("rust:1.75-alpine", "solution.rs", "rustc solution.rs && ./solution");

    private final String dockerImage;
    private final String filename;
    private final String runCommand;

    public static LanguageRuntime fromString(String language) {
        return valueOf(language.toUpperCase());
    }
}
