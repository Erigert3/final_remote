package com.eazybytes.eazyschool.controller;

import com.eazybytes.eazyschool.model.Courses;
import com.eazybytes.eazyschool.model.Person;
import com.eazybytes.eazyschool.constants.RequestType;
import com.eazybytes.eazyschool.model.CourseRequest;
import com.eazybytes.eazyschool.repository.CoursesRepository;
import com.eazybytes.eazyschool.repository.PersonRepository;
import com.eazybytes.eazyschool.service.CourseRequestService;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@Controller
@RequestMapping("student")
public class StudentController {

	@Autowired
    private CoursesRepository coursesRepository;
	
	@Autowired
    private PersonRepository personRepository;
	
	@Autowired
	private CourseRequestService courseRequestService;

	@GetMapping("/displayCourses")
    public ModelAndView displayCourses(Model model, HttpSession session) {
        Person person = (Person) session.getAttribute("loggedInPerson");
        List<Courses> courses = coursesRepository.findAll(Sort.by("name").descending());
        ModelAndView modelAndView = new ModelAndView("courses_enrolled.html");
        modelAndView.addObject("person",person);
        modelAndView.addObject("courses",courses);
        return modelAndView;
    }
	
	@GetMapping("/viewCourse")
    public String viewCourse(@RequestParam("personId") int personId, @RequestParam("courseId") int courseId, Model model) {
        
        Courses course = coursesRepository.findByCourseId(courseId);

        Person person = personRepository.findByPersonId(personId);

        model.addAttribute("course", course);
        model.addAttribute("person", person);

        return "viewCourse"; 
    }
	
	@PostMapping("/sendRegisterRequest")
	public String sendRegisterRequest(@RequestParam("courseId") int courseId,
	                                  @RequestParam("personId") int personId) {
	    CourseRequest registerRequest = new CourseRequest();
	    registerRequest.setStudentId(personId); 
	    registerRequest.setCourseId(courseId);
	    registerRequest.setRequestType(RequestType.REGISTER); 
	    
	    courseRequestService.saveRequest(registerRequest);

	    return "redirect:/student/registerRequestConfirmation"; 
	}
	
	@GetMapping("/registerRequestConfirmation")
    public String showConfirmationPage() {
        return "registerRequestConfirmation"; 
        }
	
	@PostMapping("/sendCancelRequest")
	public String sendCancelRequest(@RequestParam("courseId") int courseId,
	                                @RequestParam("personId") int personId) {
	    CourseRequest cancelRequest = new CourseRequest();
	    cancelRequest.setStudentId(personId); 
	    cancelRequest.setCourseId(courseId);
	    cancelRequest.setRequestType(RequestType.CANCEL); 

	    courseRequestService.saveRequest(cancelRequest);

	    return "redirect:/student/cancelRequestConfirmation"; 
	}
	
	@GetMapping("/cancelRequestConfirmation")
	public String showCancelConfirmationPage() {
	    return "cancelRequestConfirmation"; 
	}



}
