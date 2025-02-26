package com.email_reply.email_reply_generator.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * Utility to help identify potentially unused files in the project.
 * Note: This is a simple detection tool and may produce false positives.
 */
@Component
public class UnusedFileDetector {
    
    private static final Logger logger = LoggerFactory.getLogger(UnusedFileDetector.class);
    
    // File types to check
    private static final Set<String> JAVA_EXTENSIONS = Set.of(".java");
    private static final Set<String> CONFIG_EXTENSIONS = Set.of(".properties", ".yml", ".yaml", ".xml");
    
    /**
     * Analyzes source code to find potentially unused Java classes
     * @param projectRoot Root directory of the project
     * @return List of potentially unused files
     */
    public List<String> findPotentiallyUnusedFiles(String projectRoot) {
        List<String> allFiles = new ArrayList<>();
        List<String> potentiallyUnused = new ArrayList<>();
        
        try {
            // Collect all Java files
            collectFiles(projectRoot, allFiles, JAVA_EXTENSIONS);
            
            // For each file, check how many times it's referenced in other files
            for (String file : allFiles) {
                String className = extractClassName(file);
                int references = countReferences(projectRoot, className);
                
                // Only the file itself references the class
                if (references <= 1) {
                    potentiallyUnused.add(file);
                    logger.debug("Potentially unused file: {} (references: {})", file, references);
                }
            }
            
            // Also check for unused configuration files
            List<String> configFiles = new ArrayList<>();
            collectFiles(projectRoot, configFiles, CONFIG_EXTENSIONS);
            for (String configFile : configFiles) {
                if (!isConfigFileReferenced(projectRoot, configFile)) {
                    potentiallyUnused.add(configFile);
                    logger.debug("Potentially unused config: {}", configFile);
                }
            }
        } catch (IOException e) {
            logger.error("Error analyzing files", e);
        }
        
        return potentiallyUnused;
    }
    
    private void collectFiles(String directory, List<String> files, Set<String> extensions) throws IOException {
        Files.walkFileTree(Paths.get(directory), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                String fileName = file.toString();
                extensions.stream()
                        .filter(fileName::endsWith)
                        .findFirst()
                        .ifPresent(ext -> files.add(fileName));
                return FileVisitResult.CONTINUE;
            }
        });
    }
    
    private String extractClassName(String filePath) {
        // Extract class name from file path
        String fileName = new File(filePath).getName();
        return fileName.substring(0, fileName.lastIndexOf('.'));
    }
    
    private int countReferences(String projectRoot, String className) throws IOException {
        int count = 0;
        List<String> allFiles = new ArrayList<>();
        collectFiles(projectRoot, allFiles, JAVA_EXTENSIONS);
        
        for (String file : allFiles) {
            String content = new String(Files.readAllBytes(Paths.get(file)));
            // Simple check - could be enhanced with more sophisticated parsing
            if (content.contains(className)) {
                count++;
            }
        }
        return count;
    }
    
    private boolean isConfigFileReferenced(String projectRoot, String configFile) throws IOException {
        String configName = new File(configFile).getName();
        List<String> allFiles = new ArrayList<>();
        collectFiles(projectRoot, allFiles, JAVA_EXTENSIONS);
        
        for (String file : allFiles) {
            String content = new String(Files.readAllBytes(Paths.get(file)));
            if (content.contains(configName)) {
                return true;
            }
        }
        return false;
    }
}
