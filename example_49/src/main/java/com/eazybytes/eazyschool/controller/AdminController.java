package com.eazybytes.eazyschool.controller;

import com.eazybytes.eazyschool.model.*;
import com.eazybytes.eazyschool.repository.CoursesRepository;
import com.eazybytes.eazyschool.repository.EazyClassRepository;
import com.eazybytes.eazyschool.repository.PersonRepository;
import com.eazybytes.eazyschool.repository.RolesRepository;
import com.eazybytes.eazyschool.service.CourseRequestService;
import com.eazybytes.eazyschool.service.LecturerService;
import com.eazybytes.eazyschool.service.PersonService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("admin")
public class AdminController {

    @Autowired
    PersonService personService;

    @Autowired
    EazyClassRepository eazyClassRepository;

    @Autowired
    PersonRepository personRepository;

    @Autowired
    CoursesRepository coursesRepository;


    @Autowired
    private RolesRepository roleRepository;

    @Autowired
	private CourseRequestService courseRequestService;

    @GetMapping("/addLecturer")
    public String showAddLecturerForm(Model model) {
        Person person = new Person();
        List<Roles> roles = roleRepository.findAll();
        model.addAttribute("lecturer", person);
        model.addAttribute("roles", roles);
        return "addLecturer";
    }

    @PostMapping("/addLecturer")
    public ModelAndView addLecturer(@ModelAttribute("lecturer") @Valid Person lecturer, BindingResult bindingResult, Model model) {
        ModelAndView modelAndView = new ModelAndView();

        if (bindingResult.hasErrors()) {
            modelAndView.setViewName("addLecturer"); 
            return modelAndView;
        }

        boolean isSaved = personService.createNewLecturer(lecturer);

        if (isSaved) {
            modelAndView.setViewName("redirect:/admin/lecturersList");
        } else {
            modelAndView.setViewName("redirect:/errorPage");
        }

        return modelAndView;
    }
    
    @PostMapping("/addLecturerToCourse")
    public ModelAndView addLecturerToCourse(Model model, @RequestParam("lecturerId") int lecturerId,
                                            @RequestParam("courseId") int courseId,
                                            HttpSession session) {
        ModelAndView modelAndView = new ModelAndView();
        
        Courses course = coursesRepository.findById(courseId).orElse(null);
        Person lecturer = personRepository.findById(lecturerId).orElse(null);
        
        if (course == null || lecturer == null) {
            modelAndView.setViewName("redirect:/admin/displayCourses?error=true");
            return modelAndView;
        }
        
        course.setLecturer(lecturer);
        
        coursesRepository.save(course);
        
        modelAndView.setViewName("redirect:/admin/displayCourses");
        return modelAndView;
    }
    
    @GetMapping("/viewLecturer")
    public String viewLecturer(@RequestParam("lecturerId") int lecturerId, Model model) {
        Person lecturer = personRepository.findById(lecturerId).orElse(null);

        if (lecturer != null) {
            model.addAttribute("lecturer", lecturer);
        } else {
            model.addAttribute("lecturer", null);
        }

        return "viewLecturer";
    }

    @GetMapping("/selectLecturerForCourse")
    public String selectLecturerForCourse(@RequestParam("courseId") int courseId, Model model) {
        List<Person> lecturers = personRepository.findByRoles_RoleName("LECTURER");
        model.addAttribute("lecturers", lecturers);
        model.addAttribute("courseId", courseId);
        return "selectLecturerForCourse"; 
    }


    
    @GetMapping("/lecturersList")
    public String getLecturersList(Model model) {
        List<Person> lecturers = personRepository.findByRoles_RoleName("LECTURER");
        
        model.addAttribute("lecturers", lecturers);
        
        return "lecturersList";
    }

    private boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities() != null) {
            for (GrantedAuthority authority : auth.getAuthorities()) {
                if (authority.getAuthority().equals(role)) {
                    return true;
                }
            }
        }
        return false;
    }

    @RequestMapping("/displayClasses")
    public ModelAndView displayClasses(Model model) {
        List<EazyClass> eazyClasses = eazyClassRepository.findAll();
        ModelAndView modelAndView = new ModelAndView("classes.html");
        modelAndView.addObject("eazyClasses",eazyClasses);
        modelAndView.addObject("eazyClass", new EazyClass());
        return modelAndView;
    }

    @PostMapping("/addNewClass")
    public ModelAndView addNewClass(Model model, @ModelAttribute("eazyClass") EazyClass eazyClass) {
        eazyClassRepository.save(eazyClass);
        ModelAndView modelAndView = new ModelAndView("redirect:/admin/displayClasses");
        return modelAndView;
    }

    @RequestMapping("/deleteClass")
    public ModelAndView deleteClass(Model model, @RequestParam int id) {
        Optional<EazyClass> eazyClass = eazyClassRepository.findById(id);
        for(Person person : eazyClass.get().getPersons()){
            person.setEazyClass(null);
            personRepository.save(person);
        }
        eazyClassRepository.deleteById(id);
        ModelAndView modelAndView = new ModelAndView("redirect:/admin/displayClasses");
        return modelAndView;
    }

    @GetMapping("/displayStudents")
    public ModelAndView displayStudents(Model model, @RequestParam int classId, HttpSession session,
                                        @RequestParam(value = "error", required = false) String error) {
        String errorMessage = null;
        ModelAndView modelAndView = new ModelAndView("students.html");
        Optional<EazyClass> eazyClass = eazyClassRepository.findById(classId);
        modelAndView.addObject("eazyClass",eazyClass.get());
        modelAndView.addObject("person",new Person());
        session.setAttribute("eazyClass",eazyClass.get());
        if(error != null) {
            errorMessage = "Invalid Email entered!!";
            modelAndView.addObject("errorMessage", errorMessage);
        }
        return modelAndView;
    }

    @PostMapping("/addStudent")
    public ModelAndView addStudent(Model model, @ModelAttribute("person") Person person, HttpSession session) {
        ModelAndView modelAndView = new ModelAndView();
        EazyClass eazyClass = (EazyClass) session.getAttribute("eazyClass");
        Person personEntity = personRepository.readByEmail(person.getEmail());
        if(personEntity==null || !(personEntity.getPersonId()>0)){
            modelAndView.setViewName("redirect:/admin/displayStudents?classId="+eazyClass.getClassId()
                    +"&error=true");
            return modelAndView;
        }
        personEntity.setEazyClass(eazyClass);
        personRepository.save(personEntity);
        eazyClass.getPersons().add(personEntity);
        eazyClassRepository.save(eazyClass);
        modelAndView.setViewName("redirect:/admin/displayStudents?classId="+eazyClass.getClassId());
        return modelAndView;
    }

    @GetMapping("/deleteStudent")
    public ModelAndView deleteStudent(Model model, @RequestParam int personId, HttpSession session) {
        EazyClass eazyClass = (EazyClass) session.getAttribute("eazyClass");
        Optional<Person> person = personRepository.findById(personId);
        person.get().setEazyClass(null);
        eazyClass.getPersons().remove(person.get());
        EazyClass eazyClassSaved = eazyClassRepository.save(eazyClass);
        session.setAttribute("eazyClass",eazyClassSaved);
        ModelAndView modelAndView = new ModelAndView("redirect:/admin/displayStudents?classId="+eazyClass.getClassId());
        return modelAndView;
    }

    @GetMapping("/displayLecturers")
    public ModelAndView displayLecturers(Model model) {
        List<Person> lecturers = personRepository.findByRoles_RoleName("ROLE_LECTURER");
        ModelAndView modelAndView = new ModelAndView("lecturers.html");
        modelAndView.addObject("lecturers", lecturers);
        modelAndView.addObject("lecturer", new Person());
        return modelAndView;
    }


    @GetMapping("/displayCourses")
    public ModelAndView displayCourses(Model model) {
        //List<Courses> courses = coursesRepository.findByOrderByNameDesc();
        List<Courses> courses = coursesRepository.findAll(Sort.by("name").descending());
        ModelAndView modelAndView = new ModelAndView("courses_secure.html");
        modelAndView.addObject("courses",courses);
        modelAndView.addObject("course", new Courses());
        return modelAndView;
    }

    @PostMapping("/addNewCourse")
    public ModelAndView addNewCourse(Model model, @ModelAttribute("course") Courses course) {
        ModelAndView modelAndView = new ModelAndView();
        coursesRepository.save(course);
        modelAndView.setViewName("redirect:/admin/displayCourses");
        return modelAndView;
    }

    @GetMapping("/viewStudents")
    public ModelAndView viewStudents(Model model, @RequestParam int id
                 ,HttpSession session,@RequestParam(required = false) String error) {
        String errorMessage = null;
        ModelAndView modelAndView = new ModelAndView("course_students.html");
        Optional<Courses> courses = coursesRepository.findById(id);
        modelAndView.addObject("courses",courses.get());
        modelAndView.addObject("person",new Person());
        session.setAttribute("courses",courses.get());
        if(error != null) {
            errorMessage = "Invalid Email entered!!";
            modelAndView.addObject("errorMessage", errorMessage);
        }
        return modelAndView;
    }

    @PostMapping("/addStudentToCourse")
    public ModelAndView addStudentToCourse(Model model, @ModelAttribute("person") Person person,
                                           HttpSession session) {
        ModelAndView modelAndView = new ModelAndView();
        Courses courses = (Courses) session.getAttribute("courses");
        Person personEntity = personRepository.readByEmail(person.getEmail());
        if(personEntity==null || !(personEntity.getPersonId()>0)){
            modelAndView.setViewName("redirect:/admin/viewStudents?id="+courses.getCourseId()
                    +"&error=true");
            return modelAndView;
        }
        personEntity.getCourses().add(courses);
        courses.getPersons().add(personEntity);
        personRepository.save(personEntity);
        session.setAttribute("courses",courses);
        modelAndView.setViewName("redirect:/admin/viewStudents?id="+courses.getCourseId());
        return modelAndView;
    }

    @GetMapping("/deleteStudentFromCourse")
    public ModelAndView deleteStudentFromCourse(Model model, @RequestParam int personId,
                                                HttpSession session) {
        Courses courses = (Courses) session.getAttribute("courses");
        Optional<Person> person = personRepository.findById(personId);
        person.get().getCourses().remove(courses);
        courses.getPersons().remove(person);
        personRepository.save(person.get());
        session.setAttribute("courses",courses);
        ModelAndView modelAndView = new
                ModelAndView("redirect:/admin/viewStudents?id="+courses.getCourseId());
        return modelAndView;
    }
    
    @GetMapping("/viewRequests")
    public String viewRequests(Model model) {
        List<CourseRequest> requests = courseRequestService.getAllRequests();
        model.addAttribute("requests", requests);
        return "viewRequests"; 
    }


}
