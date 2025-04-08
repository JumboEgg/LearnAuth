package ssafy.d210.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class IpfsController {

    @GetMapping("/ipfs/{cid}")
    public String handleIpfs(@PathVariable("cid") String cid) {
        return "redirect:/certificate.html?cid=" + cid;
    }
}