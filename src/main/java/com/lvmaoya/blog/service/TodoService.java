package com.lvmaoya.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lvmaoya.blog.domain.entity.Todo;
import com.lvmaoya.blog.domain.entity.User;

import java.util.List;

public interface TodoService  extends IService<Todo> {
    List<Todo> getTodoList(String sortBy);

    List<Todo> getCurrentTodoList();

    Boolean order(Integer id, Integer prevId, Integer siblingTodoId);

    Boolean saveOrUpdateTodo(Todo todo);
}
