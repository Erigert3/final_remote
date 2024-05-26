package com.eazybytes.eazyschool.controller;

import com.eazybytes.eazyschool.service.LecturerService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.eazybytes.eazyschool.model.Courses;
import com.eazybytes.eazyschool.model.Person;
import com.eazybytes.eazyschool.repository.CoursesRepository;
import com.eazybytes.eazyschool.repository.PersonRepository;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("lecturer")
public class LecturerController {

    @Autowired
    private CoursesRepository coursesRepository;
    
    @Autowired
    PersonRepository personRepository;

    @GetMapping("/displayCourses")
    public ModelAndView displayCourses(HttpSession session, Authentication authentication) {
    	Person person = personRepository.readByEmail(authentication.getName());
        ModelAndView modelAndView = new ModelAndView("lecturer_courses.html");

        if (person != null) {
            // Fetch courses assigned to the logged-in lecturer
            List<Courses> courses = coursesRepository.findByLecturerPersonId(person.getPersonId());
            modelAndView.addObject("person", person);
            modelAndView.addObject("courses", courses);
        } else {
            // Handle case where the person is not in the session
            modelAndView.setViewName("redirect:/login"); // Redirect to login page or handle appropriately
        }

        return modelAndView;
    }
}
 