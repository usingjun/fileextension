package com.fileextension.main.controller;

import com.fileextension.main.dto.ExtensionListResponse;
import com.fileextension.main.entity.ExtensionType;
import com.fileextension.main.entity.FixedExtensions;
import com.fileextension.main.service.FileExtensionService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/extensions")
public class FileExtensionController {

    private final FileExtensionService fileExtensionService;

    FileExtensionController(FileExtensionService fileExtensionService) {
        this.fileExtensionService = fileExtensionService;
    }

    @GetMapping("")
    public String mainPage(
            @CookieValue(value = "user_guid", required = false) String userGuid,
            HttpServletResponse response,
            Model model
    ) {
        userGuid = ensureUserGuid(userGuid, response);
        loadCommonData(userGuid, model);
        return "extension-main";
    }

    @PostMapping("/fixed")
    public String selectFixedExtensions(
            @CookieValue(value = "user_guid", required = false) String userGuid,
            HttpServletResponse response,
            @RequestParam("extension") String extension
    ) {
        userGuid = ensureUserGuid(userGuid, response);

        fileExtensionService.toggleFixedExtensionByUserGuid(extension, userGuid);
        return "redirect:/extensions";
    }

    @PostMapping("/custom")
    public String addCustomExtensions(
            @CookieValue(value = "user_guid", required = false) String userGuid,
            HttpServletResponse response,
            @RequestParam("extension") String extension
            ) {
        userGuid = ensureUserGuid(userGuid, response);

        fileExtensionService.addCustomExtensionByUserGuid(extension, userGuid);
        return "redirect:/extensions";
    }

    @DeleteMapping("/custom")
    public String deleteCustomExtensions(
            @CookieValue(value = "user_guid", required = false) String userGuid,
            HttpServletResponse response,
            @RequestParam("extension") Long extensionId
    ) {
        userGuid = ensureUserGuid(userGuid, response);

        fileExtensionService.deleteCustomExtensionByUserGuid(extensionId, userGuid);
        return "redirect:/extensions";
    }

    @ExceptionHandler(RuntimeException.class)
    public String handleException(RuntimeException e,
                                  HttpServletRequest request,
                                  HttpServletResponse response,
                                  Model model) {

        String userGuid = null;
        if (request.getCookies() != null) {
            userGuid = Arrays.stream(request.getCookies())
                    .filter(c -> "user_guid".equals(c.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }

        userGuid = ensureUserGuid(userGuid, response);

        loadCommonData(userGuid, model);

        model.addAttribute("errorMessage", e.getMessage());

        return "extension-main";
    }

    private void loadCommonData(String userGuid, Model model) {
        model.addAttribute("allFixedExtensions", FixedExtensions.values());

        List<String> checkedFixedList = fileExtensionService
                .findExtensionsByUserGuidAndType(userGuid, ExtensionType.FIXED)
                .stream()
                .map(ExtensionListResponse::extension)
                .toList();
        model.addAttribute("checkedFixedList", checkedFixedList);

        List<ExtensionListResponse> customList = fileExtensionService
                .findExtensionsByUserGuidAndType(userGuid, ExtensionType.CUSTOM);
        model.addAttribute("customList", customList);
    }

    private String ensureUserGuid(String userGuid, HttpServletResponse response) {
        if (userGuid == null) {
            userGuid = UUID.randomUUID().toString();

            Cookie cookie = new Cookie("user_guid", userGuid);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            // 한달 유지
            cookie.setMaxAge(60 * 60 * 24 * 30);

            response.addCookie(cookie);
        }
        return userGuid;
    }
}
