package com.lvmaoya.blog.todo.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lvmaoya.blog.todo.entity.Todo;
import com.lvmaoya.blog.todo.pojo.TodoQuery;
import com.lvmaoya.blog.todo.service.TodoService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/todo")
public class TodoController {
    @Resource
    private TodoService todoService;

    @PostMapping("/list")
    public IPage<Todo> getTodoList(@RequestBody(required = false) TodoQuery todoQuery){
       return todoService.getTodoList(todoQuery);
    }

    @PostMapping
    public Todo addOrUpdateTodo(@RequestBody Todo todo){
       return todoService.saveOrUpdateTodo(todo);
    }
    @PutMapping
    public Todo updateTodo(@RequestBody Todo todo){
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
    @DeleteMapping("/{id}")
    public Boolean order(@PathVariable Integer id){
        return todoService.deleteTodo(id);
    }
}
