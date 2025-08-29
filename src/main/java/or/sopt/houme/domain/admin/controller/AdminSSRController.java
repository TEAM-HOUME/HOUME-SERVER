package or.sopt.houme.domain.admin.controller;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.admin.controller.dto.AdminLoginRequestDTO;
import or.sopt.houme.domain.admin.service.AdminService;
import or.sopt.houme.global.api.GeneralException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminSSRController {

    private final AdminService adminService;

    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("adminLoginRequestDTO", new AdminLoginRequestDTO("", ""));
        return "admin/login";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute AdminLoginRequestDTO adminLoginRequestDTO, Model model) {
        try {
            String token = adminService.login(adminLoginRequestDTO);
            return "redirect:/admin/dashboard?token=" + token;
        } catch (GeneralException e) {
            model.addAttribute("adminLoginRequestDTO", adminLoginRequestDTO);
            model.addAttribute("error", e.getErrorCode().getMsg());
            return "admin/login";
        }
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "admin/dashboard";
    }
}
