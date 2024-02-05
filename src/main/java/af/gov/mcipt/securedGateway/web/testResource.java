package af.gov.mcipt.securedGateway.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/test")
public class testResource {
    
    @GetMapping("/")
    public String test(){
        return "test";
    }
}
