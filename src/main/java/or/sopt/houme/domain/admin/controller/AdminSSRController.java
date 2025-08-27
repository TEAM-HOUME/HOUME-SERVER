package or.sopt.houme.domain.admin.controller;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.admin.controller.dto.AdminLoginRequestDTO;
import or.sopt.houme.domain.admin.controller.dto.AdminSignUpRequestDTO;
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


    /**
     * 회원가입을 위한 페이지를 랜딩합니다
     * */
    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("adminSignUpRequestDTO", new AdminSignUpRequestDTO("", ""));
        return "admin/signup";
    }


    /**
     * 회원가입을 진행합니다
     * 회원가입이 완료되면 완성 페이지로 리다이렉트합니다
     * */
    @PostMapping("/register")
    public String register(@ModelAttribute AdminSignUpRequestDTO adminSignUpRequestDTO) {
        adminService.signUp(adminSignUpRequestDTO);
        return "redirect:/admin/register/success";
    }


    @GetMapping("/register/success")
    public String registerSuccess() {
        return "admin/signup-success";
    }

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
