package com.lvmaoya.blog.controller;

import com.lvmaoya.blog.domain.entity.Todo;
import com.lvmaoya.blog.service.TodoService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/todo")
public class TodoController {
    @Resource
    private TodoService todoService;

    @GetMapping("/list")
    public List<Todo> getTodoList(@RequestParam(required = false) String sortBy){
       return todoService.getTodoList(sortBy);
    }

    @PostMapping
    public Boolean addOrUpdateTodo(@RequestBody Todo todo){
       return todoService.saveOrUpdateTodo(todo);
    }

    @GetMapping
    public List<Todo> getCurrentTodo(){
        return todoService.getCurrentTodoList();
    }

    @GetMapping("/order")
    public Boolean order(@RequestParam Integer prevTodoId,@RequestParam Integer id ,@RequestParam Integer siblingTodoId){
        return todoService.order(id,prevTodoId,siblingTodoId);
    }
}
