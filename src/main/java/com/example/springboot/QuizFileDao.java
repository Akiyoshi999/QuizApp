package com.example.springboot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class QuizFileDao {

	private static final String FILE_PATH = "C:\\Users\\zundo\\workspace_java\\Spring\\quizzes.txt";

	// ファイルの読み込み
	public List<Quiz> read() throws IOException {

		System.out.println(FILE_PATH);
		Path path = Paths.get(FILE_PATH);
		List<String> lines = Files.readAllLines(path);

		List<Quiz> quizzes = new ArrayList<>();
		for (String line : lines) {
			quizzes.add(Quiz.fromString(line));
		}

		return quizzes;
	}

}
