/**
 * This class defines the static route for images
 */

package com.globalyofi.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // Upload directory path (stems from application.properties)
    @Value("${app.upload.dir}")
    private String uploadDir;

    // Makes the images from /uploads accessible from an http request
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Normalize the upload directory path
        Path path = Paths.get(uploadDir).toAbsolutePath().normalize();
        String filePath = path.toUri().toString();

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(filePath);
        // A request to http://localhost:8080/uploads/image1.png will serve the file
        // C:/myapp/uploads/image1.png
    }
}
