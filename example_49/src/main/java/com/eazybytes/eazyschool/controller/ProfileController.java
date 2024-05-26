package com.eazybytes.eazyschool.controller;
 
import com.eazybytes.eazyschool.model.Address;
import com.eazybytes.eazyschool.model.Person;
import com.eazybytes.eazyschool.model.Profile;
import com.eazybytes.eazyschool.repository.PersonRepository;
import lombok.extern.slf4j.Slf4j;
 
import java.io.File;
import java.io.IOException;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
 
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
 
@Slf4j
@Controller("profileControllerBean")
public class ProfileController {
 
    @Autowired
    PersonRepository personRepository;
 
    @RequestMapping("/displayProfile")
    public ModelAndView displayMessages(Model model, HttpSession session) {
        Person person = (Person) session.getAttribute("loggedInPerson");
        Profile profile = new Profile();
        profile.setName(person.getName());
        profile.setMobileNumber(person.getMobileNumber());
        profile.setEmail(person.getEmail());
        if(person.getAddress() !=null && person.getAddress().getAddressId()>0){
            profile.setAddress1(person.getAddress().getAddress1());
            profile.setAddress2(person.getAddress().getAddress2());
            profile.setCity(person.getAddress().getCity());
            profile.setState(person.getAddress().getState());
            profile.setZipCode(person.getAddress().getZipCode());
        }
        profile.setProfilePicture(person.getProfilePicture());
        ModelAndView modelAndView = new ModelAndView("profile.html");
        modelAndView.addObject("profile",profile);
        return modelAndView;
    }
 
    @PostMapping(value = "/updateProfile")
    public String updateProfile(@Valid @ModelAttribute("profile") Profile profile, Errors errors, @RequestParam("file") MultipartFile file,
            HttpSession session)
    {
        if(errors.hasErrors()){
            return "profile.html";
        }
        Person person = (Person) session.getAttribute("loggedInPerson");
        person.setName(profile.getName());
        person.setEmail(profile.getEmail());
        person.setMobileNumber(profile.getMobileNumber());
        if(person.getAddress() ==null || !(person.getAddress().getAddressId()>0)){
            person.setAddress(new Address());
        }
        person.getAddress().setAddress1(profile.getAddress1());
        person.getAddress().setAddress2(profile.getAddress2());
        person.getAddress().setCity(profile.getCity());
        person.getAddress().setState(profile.getState());
        person.getAddress().setZipCode(profile.getZipCode());
        if (!file.isEmpty()) {
            try {
                String baseDir = "C:\\Users\\User\\DetyreKursi_Java\\example_49\\src\\main\\resources\\static\\assets\\images"; // Change this to your actual directory path
                String originalFileName = file.getOriginalFilename();
                String filePath = baseDir + File.separator + originalFileName;
                File dest = new File(filePath);
                file.transferTo(dest);
                person.setProfilePicture(originalFileName); 
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Person savedPerson = personRepository.save(person);
        session.setAttribute("loggedInPerson", savedPerson);
        return "redirect:/displayProfile";
    }
}