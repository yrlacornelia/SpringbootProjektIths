package com.example.springbootprojektiths.controller;

import com.example.springbootprojektiths.CreateNewMessageForm;
import com.example.springbootprojektiths.EditMessageForm;
import com.example.springbootprojektiths.editUserForm;
import com.example.springbootprojektiths.entity.Message;
import com.example.springbootprojektiths.entity.User;
import com.example.springbootprojektiths.repository.MessageRepository;
import com.example.springbootprojektiths.repository.UserRepository;
import com.example.springbootprojektiths.service.MessageServices;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class WebController {

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    MessageServices messageServices;




    @RequestMapping(value = "/listmessages", method = RequestMethod.GET)
    public String listMessages(
            Model model,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size,
            @AuthenticationPrincipal OAuth2User principal) {

        //int githubid = principal.getAttribute("id");
        //long githubid = (long) githubid;

        Object idObject = principal.getAttribute("id");

        Integer idInteger = (Integer) idObject;
        var user = userRepository.findById(idInteger.longValue());
        model.addAttribute("person", user.get());

//        if (idObject instanceof Integer) {
//            Integer idInteger = (Integer) idObject;
//            var user = userRepository.findById(idInteger.longValue());
//            model.addAttribute("person", user.get());
//        } else if (idObject instanceof Long) {
//            var user = userRepository.findById((Long) idObject);
//            model.addAttribute("person", user.get());
//        }
        List<Message> messages = messageRepository.findAll();
        model.addAttribute("messages", messages);
   //     var user = userRepository.findById(id);
  //      model.addAttribute("person", user.get());
        int currentPage = page.orElse(1);
        int pageSize = size.orElse(5);

        Page<Message> messagePage = messageServices.findPaginated(PageRequest.of(currentPage - 1, pageSize));

        model.addAttribute("messagePage", messagePage);

        int totalPages = messagePage.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed()
                    .collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }

        return "posts";
    }

/*    @GetMapping("/posts")
    String posts(Model model ){
       List<Message> messages = messageRepository.findAll();
       // List<User> people = userRepository.findAll();
        model.addAttribute("messages", messages);
        return "posts";
    }*/

    @GetMapping("/createNewMessage")
    public String addMessage (Model model) {
        model.addAttribute("formData", new CreateNewMessageForm());
        return "createNewMessage";
    }

   @PostMapping("/createNewMessage")
   public String submitMessage(@Valid @ModelAttribute("formData") CreateNewMessageForm message,
            BindingResult bindingResult, Model model){
        if(bindingResult.hasErrors()){
            return"createNewMessage";
        }
        messageRepository.save(message.toEntity());
        return "redirect:/listmessages";
    }

    @GetMapping("/yourMessages")
    public String editMessages(Model model){
        List<Message> messages = messageRepository.findAll();
        model.addAttribute("messages", messages);
                return "yourMessages";
    }

    @GetMapping("/editMessage/{id}")
    public String editMessage(@PathVariable Long id, Model model){
        Optional<Message> message = messageRepository.findById(id);
        model.addAttribute("formData", new EditMessageForm(message.get().getId(), message.get().getTitle(), message.get().getChatMessage()));
        return "editMessage";
    }

    @PostMapping("/editMessage/{id}")
    public String submitEditMessage(@PathVariable Long id, Model model, @ModelAttribute("formData") EditMessageForm messageForm){
        Optional<Message> optionalMessage = messageRepository.findById(id);

        if (optionalMessage.isPresent()) {
            Message message = optionalMessage.get();

            // Update message attributes based on form data
            message.setTitle(messageForm.getTitle());
            message.setChatMessage(messageForm.getChatMessage());

            // Save the updated message
            messageRepository.save(message);

            // Redirect to a different page or return appropriate view name
            return "redirect:/listmessages";
        } else {
            // Handle case where message with given id is not found
            return "redirect:/error";
        }
    }

    @GetMapping("/userSettings")
    public String userPage( @AuthenticationPrincipal OAuth2User principal, Model model){
        Object idObject = principal.getAttribute("id");
        Integer idInteger = (Integer) idObject;
        Optional<User> userOptional = userRepository.findById(idInteger.longValue());
        User user = userOptional.get();
        model.addAttribute("userData", new editUserForm(user.getId(), user.getFullName(), user.getLoginName(), user.getMail()) );
              return "userSettings";
    }
    @PostMapping("/userSettings")
    public String editUser( @AuthenticationPrincipal OAuth2User principal, Model model, @ModelAttribute("userData") editUserForm userForm ){
        Object idObject = principal.getAttribute("id");
        Integer idInteger = (Integer) idObject;
        Optional<User> userOptional = userRepository.findById(idInteger.longValue());
        if (userOptional.isPresent()) {
            User user = userOptional.get();

            user.setFullName(userForm.getFullName());
            user.setLoginName(userForm.getLoginName());
            user.setMail(userForm.getMail());

            userRepository.save(user);
            return "userSettings";
        } else {
            return "redirect:/error";
        }
    }

    public static String UPLOAD_DIRECTORY = System.getProperty("user.dir") + "/uploads";

    @GetMapping("/uploadimage") public String displayUploadForm() {
        return "test";
    }

    @PostMapping("/upload") public String uploadImage(Model model, @RequestParam("image") MultipartFile file) throws IOException {
        StringBuilder fileNames = new StringBuilder();
        Path fileNameAndPath = Paths.get(UPLOAD_DIRECTORY, file.getOriginalFilename());
        fileNames.append(file.getOriginalFilename());
        Files.write(fileNameAndPath, file.getBytes());
        model.addAttribute("msg", "Uploaded images: " + fileNames.toString());
        return "test";
    }

/*    @PostMapping("/upload")
    @ResponseBody
    public String uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return "Please select a file to upload";
        }

        try {
            // Get the file and save it
            String fileName = file.getOriginalFilename();
            String filePath = "path/to/your/uploads/" + fileName; // Change this to your desired file path
            File dest = new File(filePath);
            file.transferTo(dest);
            return "File uploaded successfully";
        } catch (IOException e) {
            e.printStackTrace();
            return "Failed to upload file";
        }
    }*/

        //Prova denna när vi ska ladda upp profilbild?? Måste ändra i databasen först med flyway.
//    @PostMapping("/upload")
//    public String uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("userId") Long userId) {
//        // Ladda upp filen och spara den på servern
//        String fileName = file.getOriginalFilename();
//        String filePath = "/path/to/upload/directory/" + fileName;
//        try {
//            File dest = new File(filePath);
//            file.transferTo(dest);
//        } catch (IOException e) {
//            e.printStackTrace();
//            return "Failed to upload file.";
//        }
//
//        // Spara sökvägen till filen i användarprofilen
//        Optional<User> optionalUser = userRepository.findById(userId);
//        if (optionalUser.isPresent()) {
//            User user = optionalUser.get();
//            user.setProfilePicture(filePath);
//            userRepository.save(user);
//            return "File uploaded successfully.";
//        } else {
//            return "User not found.";
//        }
//    }
}



