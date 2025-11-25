package com.uex.fablab.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class FileUploadExceptionAdvice {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxUpload(MaxUploadSizeExceededException ex, HttpServletRequest request) {
        String msg = URLEncoder.encode("La imagen supera el límite de 2 MB", StandardCharsets.UTF_8);
        String referer = request.getHeader("Referer");
        if (referer != null && referer.startsWith("http")) {
            // Redirige de vuelta a la página desde donde venía, añadiendo el mensaje de error
            String sep = referer.contains("?") ? "&" : "?";
            return "redirect:" + referer + sep + "error=" + msg;
        }
        // Fallback: página de alta de máquina
        return "redirect:/admin/add-machine?error=" + msg;
    }
}
